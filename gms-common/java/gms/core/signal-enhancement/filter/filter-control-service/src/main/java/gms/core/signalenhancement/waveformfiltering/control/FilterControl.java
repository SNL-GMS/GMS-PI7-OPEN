package gms.core.signalenhancement.waveformfiltering.control;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

import gms.core.signalenhancement.waveformfiltering.coi.CoiRepository;
import gms.core.signalenhancement.waveformfiltering.configuration.FilterConfiguration;
import gms.core.signalenhancement.waveformfiltering.configuration.FilterParameters;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginRegistry;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentStorageResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.jetty.io.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterControl {

  private static final Logger logger = LoggerFactory.getLogger(FilterControl.class);

  private final FilterConfiguration configuration;
  private final PluginRegistry<FilterPlugin> registry;
  private final CoiRepository coiRepository;

  /**
   * Factory method for creating a FilterControl
   *
   * @param registry plugin registry, not null
   * @param coiClient coi client used to access waveforms, not null
   * @return a new FilterControl object
   */
  public static FilterControl create(FilterConfiguration configuration,
      PluginRegistry<FilterPlugin> registry, CoiRepository coiClient) {
    return new FilterControl(configuration, registry, coiClient);
  }

  private FilterControl(FilterConfiguration configuration, PluginRegistry<FilterPlugin> registry,
      CoiRepository coiRepository) {
    this.configuration = checkNotNull(configuration,
        "Error instantiating FilterControl: null FilterConfiguration");
    this.registry = checkNotNull(registry,
        "Error instantiating FilterControl: null PluginRegistry");
    this.coiRepository = checkNotNull(coiRepository,
        "Error instantiating FilterControl: null CoiClient");
  }

  /**
   * Execute Waveform qc processing using the provided {@link ChannelSegmentDescriptor}
   *
   * @param channelSegmentDescriptor object describing the filter processing request, not null
   * @return list of {@link UUID} to generated {@link ChannelSegment}, not null
   */
  public List<ChannelSegment<Waveform>> requestProcessing(
      ChannelSegmentDescriptor channelSegmentDescriptor) {

    Objects.requireNonNull(channelSegmentDescriptor,
        "FilterControl cannot process a null ChannelSegmentDescriptor");

    logger.info("Handling Filter processing request for descriptor: {}", channelSegmentDescriptor);

    Map<UUID, Double> channelIdToSampleRate;
    try {
      channelIdToSampleRate = coiRepository
          .getChannels(List.of(channelSegmentDescriptor.getChannelId()))
          .stream().collect(Collectors.toMap(ReferenceChannel::getVersionId,
              ReferenceChannel::getNominalSampleRate));
    } catch (IOException e) {
      throw new UncheckedIOException("Error retrieving channels: Filter processing cannot continue",
          e);
    }

    List<FilterParameters> filterParameters = configuration
        .getFilterParameters(channelIdToSampleRate.get(channelSegmentDescriptor.getChannelId()));

    return filterParameters.stream()
        .map(params -> requestProcessing(channelSegmentDescriptor, params))
        .collect(toList());
  }

  public ChannelSegment<Waveform> requestProcessing(ChannelSegmentDescriptor descriptor,
      FilterParameters parameters) {

    FilterPlugin plugin = registry
        .lookup(RegistrationInfo.create(parameters.getPluginName(), 1, 0, 0))
        .orElseThrow(() -> new NoSuchElementException(
            String.format("Plugin not found for name %s", parameters.getPluginName())));

    ChannelSegmentDescriptor processingDescriptor = plugin
        .getProcessingDescriptor(descriptor, parameters.getPluginParameters());

    ChannelSegment<Waveform> waveforms;
    try {
      waveforms = coiRepository.getWaveforms(processingDescriptor);
    } catch (IOException e) {
      throw new UncheckedIOException("Error retrieving data: Filter processing cannot continue", e);
    }

    //TODO: Generate channel id in lower level calls rather than passing it in
    String filterName = parameters.getPluginParameters().getOrDefault("name", "UNKNOWN").toString();
    String outputChannelIdString = String
        .format("%s/%s", waveforms.getName(), filterName);
    return executeProcessing(waveforms, plugin, parameters.getPluginParameters(),
        UUID.nameUUIDFromBytes(outputChannelIdString.getBytes()));
  }

  /**
   * Execute Waveform qc processing
   *
   * @param channelSegments Collection of input {@link ChannelSegment} of {@link Waveform}s to
   * filter
   * @param inputToOutputChannelIds Map providing output channel {@link UUID}s for every input
   * channel id
   * @param pluginParams Field Map containing plugin-specific parameters used within the {@link
   * FilterPlugin} implementation
   * @return list of filtered {@link ChannelSegment}s of {@link Waveform}s
   */
  public List<ChannelSegment<Waveform>> executeProcessing(
      Collection<ChannelSegment<Waveform>> channelSegments,
      Map<UUID, UUID> inputToOutputChannelIds, Map<String, Object> pluginParams) {
    logger.info("Executing filter processing for {} channelSegments, with parameters: {}",
        channelSegments.size(), pluginParams);

    checkNotNull(channelSegments, "Cannot execute filter processing with null channelSegments");
    checkNotNull(inputToOutputChannelIds,
        "Cannot execute filter processing with null inputToOutputChannelIds");
    checkNotNull(pluginParams, "Cannot execute filter processing with null pluginParams");

    //TODO: have streaming call pass plugin name, or pass plugin type separate and resolve plugin
    FilterPlugin plugin = registry
        .lookup(RegistrationInfo.create("linearWaveformFilterPlugin", 1, 0, 0))
        .orElseThrow(() -> new NoSuchElementException(
            String.format("Default plugin %s not found", "linearWaveformFilterPlugin")));

    return channelSegments.stream()
        .map(cs -> executeProcessing(cs, plugin, pluginParams,
            inputToOutputChannelIds.get(cs.getChannelId())))
        .collect(toList());
  }

  /**
   * Creates a new filtered {@link ChannelSegment} from the input {@link ChannelSegment} using the
   * supplied {@link FilterPlugin} and {@link FilterDefinition}. The new ChannelSegment is defined
   * using the a new output channel id that is not the same as the channel id referenced by the
   * input {@link ChannelSegment}.
   *
   * @param waveforms The input ChannelSegment supplying the waveforms to be filtered.
   * @param plugin The plugin filter algorithm.
   * @param pluginParams The filter definition object.
   * @param outputChannelId The Channel id for the new ChannelSegment.
   * @return The new ChannelSegment
   */
  public static ChannelSegment<Waveform> executeProcessing(
      ChannelSegment<Waveform> waveforms, FilterPlugin plugin, Map<String, Object> pluginParams,
      UUID outputChannelId) {
    checkNotNull(waveforms, "Cannot execute filter processing with null waveforms");
    checkNotNull(plugin, "Cannot execute filter processing with null plugin");
    checkNotNull(pluginParams, "Cannot execute filter processing with null pluginParams");
    checkNotNull(outputChannelId, "Cannot execute filter processing with null outputChannelId");

    logger.info("Executing filter Processing with plugin: {} and parameters: {}", plugin.getName(),
        pluginParams);
    Collection<Waveform> newWaveforms = plugin.filter(waveforms, pluginParams);

    //TODO: refactor FilterDefinition instead of extracting plugin parameters for channel name
    String filterName = pluginParams.getOrDefault("name", "UNKNOWN").toString();
    return ChannelSegment
        .create(outputChannelId, String.format("%s/%s", waveforms.getName(), filterName),
            ChannelSegment.Type.FILTER, newWaveforms, CreationInfo.DEFAULT);
  }

  public ChannelSegmentStorageResponse storeWaveforms(
      List<ChannelSegment<Waveform>> channelSegments) {
    logger.info("Storing {} filtered channelSegments", channelSegments.size());
    try {
      return coiRepository.storeChannelSegments(channelSegments);
    } catch (IOException e) {
      logger.error("Could not store channelSegments via COI", e);
      throw new RuntimeIOException(e);
    }

  }

}
