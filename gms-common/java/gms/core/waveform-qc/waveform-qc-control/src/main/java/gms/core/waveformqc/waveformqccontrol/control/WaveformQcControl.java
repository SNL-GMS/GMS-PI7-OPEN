package gms.core.waveformqc.waveformqccontrol.control;

import static java.util.stream.Collectors.toList;

import com.google.common.base.Preconditions;
import gms.core.signaldetection.qccontrol.QcControlInterface;
import gms.core.waveformqc.plugin.WaveformQcPlugin;
import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegment;
import gms.core.waveformqc.waveformqccontrol.coi.CoiClient;
import gms.core.waveformqc.waveformqccontrol.coi.CoiRepository;
import gms.core.waveformqc.waveformqccontrol.configuration.QcConfiguration;
import gms.core.waveformqc.waveformqccontrol.configuration.QcParameters;
import gms.shared.frameworks.control.ControlContext;
import gms.shared.frameworks.pluginregistry.PluginRegistry;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class following our Control pattern for performing processing in the GMS. This class in
 * particular is responsible for handling quality control on waveforms acquired by the GMS.
 */

public class WaveformQcControl implements QcControlInterface {

  private static final Logger logger = LoggerFactory.getLogger(WaveformQcControl.class);

  private final QcConfiguration configuration;
  private final PluginRegistry registry;
  private final CoiRepository coiRepository;

  private WaveformQcControl(QcConfiguration configuration, PluginRegistry registry,
      CoiRepository coiRepository) {
    this.configuration = Preconditions.checkNotNull(configuration);
    this.registry = Preconditions.checkNotNull(registry);
    this.coiRepository = Preconditions.checkNotNull(coiRepository);
  }

  /**
   * Create {@link WaveformQcControl} from all it's dependencies
   * @param configuration the qc configuration to use, not null
   * @param registry the plugin registry to use, not null
   * @param coiRepository the coi repository to use, not null
   * @return a waveform QC control instance
   */
  public static WaveformQcControl create(
      QcConfiguration configuration, PluginRegistry registry,
      CoiRepository coiRepository) {
    return new WaveformQcControl(configuration, registry, coiRepository);
  }

  /**
   * Create {@link WaveformQcControl} from a {@link ControlContext}.
   * @param cxt the control context, not null
   * @return a waveform QC control instance
   */
  public static WaveformQcControl create(ControlContext cxt) {
    Objects.requireNonNull(cxt, "Cannot create from null context");
    final SystemConfig sysConfig = cxt.getSystemConfig();
    final URL waveformsCoiUrl = sysConfig.getUrlOfComponent("waveforms-coi");
    final URL sigDetCoiUrl = sysConfig.getUrlOfComponent("signal-detection-coi");
    final CoiRepository coiRepo = new CoiClient(
        waveformsCoiUrl.toString(), sigDetCoiUrl.toString());
    return create(
        QcConfiguration.create(cxt.getProcessingConfigurationRepository()),
        cxt.getPluginRegistry(), coiRepo);
  }

  @Override
  public List<QcMaskVersionDescriptor> executeAutomatic(ChannelSegmentDescriptor segmentDescriptor) {
    Objects.requireNonNull(segmentDescriptor);
    final List<QcMask> qcMasks = requestQcProcessing(segmentDescriptor);
    logger.info("Waveform QC Request processed, with {} QcMasks created/updated", qcMasks.size());
    if (!qcMasks.isEmpty()) {
      logger.info("Storing {} created/updated QcMasks", qcMasks.size());
      final List<QcMaskVersionDescriptor> storedMaskDescriptors = storeQcMasks(qcMasks);
      logger.info("{} QcMasks stored", storedMaskDescriptors.size());
      return storedMaskDescriptors;
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * Request Waveform QC processing for the provided {@link ChannelSegmentDescriptor}.
   * Control will retrieve configuration for this descriptor and call {@link WaveformQcControl#requestQcProcessing(ChannelSegmentDescriptor, QcParameters)}
   *
   * @param descriptor A request for processing a particular range of channel data, not null
   * @return List of new or updated masks referenced by {@link QcMaskVersionDescriptor}s, not null
   */
  List<QcMask> requestQcProcessing(ChannelSegmentDescriptor descriptor) {
    Preconditions.checkNotNull(descriptor);

    logger.info("Handling Waveform QC Processing request for descriptor: {}", descriptor);

    List<QcParameters> qcParameters = configuration.getPluginConfigurations();

    return qcParameters.stream()
        .map(params -> requestQcProcessing(descriptor, params))
        .flatMap(List::stream).collect(toList());
  }

  /**
   * Request Waveform QC processing for the provided {@link ChannelSegmentDescriptor} and {@link QcParameters}.
   * Will generate processing descriptors based on the input descriptor, plugin, and configuration parameters.
   * This descriptor is used to retrieve the necessary data to run {@link WaveformQcControl#executeQcProcessing(WaveformQcPlugin, ChannelSegment, Collection, Collection, Map)}
   *
   * @param descriptor {@link ChannelSegmentDescriptor} that represents a range of channel data we would like processed, not null
   * @param parameters {@link QcParameters} that determines which plugin to run what parameters to provide to the plugin, not null
   * @return A List of new or updated {@link QcMask}s, not null
   */
  List<QcMask> requestQcProcessing(ChannelSegmentDescriptor descriptor,
      QcParameters parameters) {
    Preconditions.checkNotNull(descriptor);
    Preconditions.checkNotNull(parameters);

    logger.info("Handling Waveform QC Processing request for descriptor: {}, parameters: {}",
        descriptor, parameters);

    WaveformQcPlugin plugin = registry.get(parameters.getPluginName(), WaveformQcPlugin.class);

    ChannelSegmentDescriptor processingDescriptor = plugin
        .getProcessingDescriptor(descriptor, parameters.getPluginParams());

    ChannelSegment<Waveform> channelSegment;
    List<ChannelSohStatusSegment> sohStatuses;
    List<QcMask> existingQcMasks;
    try {
      channelSegment = coiRepository.getWaveforms(processingDescriptor);
      sohStatuses = coiRepository
          .getChannelSohStatuses(processingDescriptor,
              Duration.ofMillis(1000));
      existingQcMasks = coiRepository.getQcMasks(processingDescriptor);
    } catch (IOException e) {
      throw new UncheckedIOException("Error retrieving data: WaveformQc processing cannot continue",
          e);
    }

    return executeQcProcessing(plugin, channelSegment, sohStatuses, existingQcMasks,
        parameters.getPluginParams());
  }

  /**
   * Processing Execution method that handles the running of a plugin and any other data processing/mutations
   * that need to occur in order to create {@link QcMask}s.
   *
   * @param plugin Plugin used to create QcMasks.
   * @param waveforms Segment of Channel data providing waveform input to the plugin.
   * @param sohStatuses State of health status information provided as input to the plugin.
   * @param existingQcMasks Previously stored qc masks provided as input to the plugin.
   * @return A Stream of all created QcMasks, and the CreationInformation provenance for each new
   * mask version created.
   */
  static List<QcMask> executeQcProcessing(WaveformQcPlugin plugin,
      ChannelSegment<Waveform> waveforms,
      Collection<ChannelSohStatusSegment> sohStatuses, Collection<QcMask> existingQcMasks,
      Map<String, Object> parameterFieldMap) {
    logger.info("Executing Waveform QC Processing with plugin: {}", plugin.getName());
    Preconditions.checkNotNull(plugin);
    Preconditions.checkNotNull(waveforms);
    Preconditions.checkNotNull(sohStatuses);
    Preconditions.checkNotNull(existingQcMasks);
    Preconditions.checkNotNull(parameterFieldMap);

    List<QcMask> qcMasks = plugin
        .generateQcMasks(waveforms, sohStatuses, existingQcMasks, parameterFieldMap);

    logger.info("QcMask Processing completed, {} masks created", qcMasks.size());
    return qcMasks;
  }

  List<QcMaskVersionDescriptor> storeQcMasks(List<QcMask> qcMasks) {
    Preconditions.checkNotNull(qcMasks);
    if (qcMasks.isEmpty()) {
      return List.of();
    }
    try {
      coiRepository.storeQcMasks(qcMasks);
    } catch (IOException e) {
      throw new UncheckedIOException("Error storing QcMasks", e);
    }

    return qcMasks.stream().map(QcMask::getCurrentVersionAsReference).collect(toList());
  }
}
