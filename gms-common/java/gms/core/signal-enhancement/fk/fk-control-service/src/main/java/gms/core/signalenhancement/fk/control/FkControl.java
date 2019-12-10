package gms.core.signalenhancement.fk.control;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import gms.core.signalenhancement.fk.coi.client.CoiRepository;
import gms.core.signalenhancement.fk.control.configuration.FileBasedFkConfiguration;
import gms.core.signalenhancement.fk.control.configuration.FkAttributesParameters;
import gms.core.signalenhancement.fk.control.configuration.FkConfiguration;
import gms.core.signalenhancement.fk.control.configuration.FkSpectraParameters;
import gms.core.signalenhancement.fk.plugin.fkattributes.FkAttributesPlugin;
import gms.core.signalenhancement.fk.plugin.fkspectra.FkSpectraPlugin;
import gms.core.signalenhancement.fk.plugin.util.FkSpectraInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginRegistry;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.ProcessingResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.UpdateStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkAttributes;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility.FkSpectraUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import static java.util.stream.Collectors.toList;

public class FkControl {

  private static final Logger logger = LoggerFactory.getLogger(FkControl.class);
  private static final double BILLION = 1E9;
  private final PluginRegistry<FkSpectraPlugin> fkSpectraPluginRegistry;
  private final PluginRegistry<FkAttributesPlugin> fkAttributesPluginRegistry;
  private final CoiRepository coiRepository;
  private FileBasedFkConfiguration fileBasedFkConfiguration;
  private FkConfiguration fkConfiguration;

  private boolean initialized;

  /**
   * Factory method for creating a FkControl
   *
   * @param fkSpectraPluginRegistry    Fk Spectrum plugin registry, not null
   * @param fkAttributesPluginRegistry Fk Attributes plugin registry, not null
   * @param coiRepository              osd gateway access library, not null
   * @return a new FkControl object
   */
  public static FkControl create(
      PluginRegistry<FkSpectraPlugin> fkSpectraPluginRegistry,
      PluginRegistry<FkAttributesPlugin> fkAttributesPluginRegistry,
      CoiRepository coiRepository,
      FkConfiguration fkConfiguration) {

    Objects.requireNonNull(fkSpectraPluginRegistry,
        "Error creating FkControl: fkSpectraPluginRegistry cannot be null");
    Objects.requireNonNull(fkAttributesPluginRegistry,
        "Error creating FkControl: fkAttributesPluginRegistry cannot be null");
    Objects.requireNonNull(coiRepository,
        "Error creating FkControl: coiRepository cannot be null");
    Objects.requireNonNull(fkConfiguration,
        "Error creating FkControl: fkConfiguration cannot be null");

    return new FkControl(fkSpectraPluginRegistry,
        fkAttributesPluginRegistry,
        coiRepository,
        fkConfiguration);
  }

  /**
   * Constructs a FkControl with provided plugin fkSpectraPluginRegistry and OSD gateway access
   * library.
   *
   * @param fkSpectraPluginRegistry    Fk Spectrum plugin registry, not null
   * @param fkAttributesPluginRegistry Fk Attributes plugin registry, not null
   * @param coiRepository              osd gateway access library, not null
   */
  private FkControl(PluginRegistry<FkSpectraPlugin> fkSpectraPluginRegistry,
      PluginRegistry<FkAttributesPlugin> fkAttributesPluginRegistry,
      CoiRepository coiRepository,
      FkConfiguration fkConfiguration) {

    this.fkSpectraPluginRegistry = fkSpectraPluginRegistry;
    this.fkAttributesPluginRegistry = fkAttributesPluginRegistry;
    this.coiRepository = coiRepository;
    this.fkConfiguration = fkConfiguration;
    this.fileBasedFkConfiguration = null;
    this.initialized = false;
  }

  /**
   * Initialization method used to set fkSpectraConfiguration and fkAttributesConfiguration for
   * control class and its bound plugins.
   */
  public void initialize() {
    logger.info("Loading configuration for control class");
    fileBasedFkConfiguration = coiRepository.getConfiguration();

    logger.info("Loading configuration for plugins");
    fkSpectraPluginRegistry.entrySet().forEach(e -> e.getPlugin()
        .initialize(coiRepository.getParameterFieldMap(e.getRegistration())));
    fkAttributesPluginRegistry.entrySet().forEach(e -> e.getPlugin()
        .initialize(coiRepository.getParameterFieldMap(e.getRegistration())));

    initialized = true;
  }

  /**
   * Execute Waveform qc processing using the provided {@link FkAnalysisCommand}
   *
   * @param descriptors object describing the Fk Spectrum processing request, not null
   * @return list of {@link UUID} to generated {@link ChannelSegment}, not null
   */
  public ProcessingResponse<SignalDetectionHypothesisDescriptor> measureFkFeatures(
      Collection<SignalDetectionHypothesisDescriptor> descriptors) {

    if (!initialized) {
      throw new IllegalStateException(
          "FkControl must be initialized before execution");
    }

    checkNotNull(descriptors);

    logger.info("FkControl executing claim check command{}", descriptors);

    checkState(!descriptors.isEmpty(),
        "FkControl cannot execute claim check from empty SignalDetectionHypothesisDescriptors");

    Predicate<SignalDetectionHypothesisDescriptor> needsUpdate = sdh ->
        !sdh.getSignalDetectionHypothesis()
            .getFeatureMeasurement(FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH).isPresent()
            && !sdh.getSignalDetectionHypothesis()
            .getFeatureMeasurement(FeatureMeasurementTypes.SLOWNESS).isPresent();

    // signal detection hypothesis -> arrival time measurement
    // window lead + window lag + arrival time = waveform bounds
    // signal detection hypothesis -> phase type measurement -> phase type -> fk definition
    // fk definition -> relative positions -> channels -> waveforms
    Map<Boolean, List<SignalDetectionHypothesisDescriptor>> hypothesesToUpdate =
        descriptors.stream().collect(Collectors.partitioningBy(needsUpdate));

    ProcessingResponse.Builder<SignalDetectionHypothesisDescriptor> responseBuilder = ProcessingResponse.builder();

    //add the unchanged ids
    hypothesesToUpdate.get(Boolean.FALSE)
        .forEach(responseBuilder::addUnchanged);

    Collection<SignalDetectionHypothesisDescriptor> updatedHypotheses = hypothesesToUpdate.get(Boolean.TRUE)
        .stream()
        .map(this::updateHypothesisWithFK)
        .collect(toList());

    Map<SignalDetectionHypothesisDescriptor, UpdateStatus> updateStatusMap = coiRepository
        .storeSignalDetectionHypotheses(updatedHypotheses);

    updateStatusMap.forEach((descriptor, status) -> {
      switch (status) {
        case UPDATED:
          responseBuilder.addUpdated(descriptor);
          break;
        case UNCHANGED:
          responseBuilder.addUnchanged(descriptor);
          break;
        default:
          //failed status and default
          responseBuilder.addFailed(descriptor);
          break;
      }
    });

    return responseBuilder.build();
  }

  private SignalDetectionHypothesisDescriptor updateHypothesisWithFK(
      SignalDetectionHypothesisDescriptor descriptor) {
    Optional<FeatureMeasurement<InstantValue>> possibleArrival =
        descriptor.getSignalDetectionHypothesis().getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME);
    Optional<FeatureMeasurement<PhaseTypeMeasurementValue>> possiblePhase =
        descriptor.getSignalDetectionHypothesis().getFeatureMeasurement(FeatureMeasurementTypes.PHASE);

    checkState(possibleArrival.isPresent(),
        "FkSpectra cannot be calculated from a SignalDetectionHypothesis without an " +
            "arrival time measurement");
    checkState(possiblePhase.isPresent(),
        "FkSpectra cannot be calculated from a SignalDetectionHypothesis without a " +
            "phase type measurement");
    Instant arrival = possibleArrival.get().getMeasurementValue().getValue();

    FkSpectraParameters fkSpectraParameters = fkConfiguration.getFkSpectraParameters(descriptor.getStationId());

    FkSpectraDefinition definition = fkSpectraParameters.getDefinition();
    Instant windowStart = arrival.minus(definition.getWindowLead());
    Instant windowEnd = windowStart.plus(definition.getWindowLength());

    Collection<ChannelSegment<Waveform>> channelSegments = retrieveWaveformSegments(
        definition.getRelativePositionsByChannelId().keySet(), windowStart, windowEnd);

    FkSpectraPlugin spectraPlugin = retrievePlugin(fkSpectraParameters.getPluginName(),
        fkSpectraPluginRegistry);

    List<FkAttributesParameters> fkAttributesParameters = fkConfiguration
        .getFkAttributesParameters(descriptor.getStationId());

    List<FkAttributesPlugin> fkAttributesPlugins = fkAttributesParameters.stream()
        .map(parameters -> retrievePlugin(parameters.getPluginName(), fkAttributesPluginRegistry))
        .collect(Collectors.toList());

    StringBuilder builder = new StringBuilder();
    builder.append(descriptor.getStationId().toString());
    builder.append(" ");
    builder.append(fkSpectraParameters.getDefinition().getUseChannelVerticalOffsets() ? "3D " : "2D ");
    builder.append(fkSpectraParameters.getDefinition().getBeamPoint().toString());

    Location beamPoint = fkSpectraParameters.getDefinition().getBeamPoint();
    builder.append(beamPoint.getLatitudeDegrees());
    builder.append(", ");
    builder.append(beamPoint.getLongitudeDegrees());
    builder.append(", ");
    builder.append(beamPoint.getDepthKm());
    builder.append(", ");
    builder.append(beamPoint.getElevationKm());

    String channelSegmentName = builder.toString();
    UUID outputChannelId = UUID.nameUUIDFromBytes(channelSegmentName.getBytes());

    ChannelSegment<FkSpectra> spectraSegment = createFkSpectraSegment(spectraPlugin,
        fkAttributesPlugins,
        definition,
        channelSegments,
        arrival,
        outputChannelId,
        channelSegmentName);

    coiRepository.storeFkSpectras(List.of(spectraSegment), StorageVisibility.PUBLIC);

    // For now, we assume there is always only 1 FkAttributesPlugin, and hence only one entry in the
    // FkSpectrum's FkAttributes list.  There will need to be some sort of resolution once there are
    // more FkAttributesPlugins.
    if (spectraSegment.getTimeseries().size() == 1 &&
        spectraSegment.getTimeseries().get(0).getValues().size() == 1 &&
        spectraSegment.getTimeseries().get(0).getValues().get(0).getAttributes().size() == 1) {
      FkSpectrum spectrum = spectraSegment.getTimeseries().get(0).getValues().get(0);
      FkAttributes attributes = spectrum.getAttributes().get(0);

      DoubleValue azimuth = DoubleValue.from(attributes.getAzimuth(),
          attributes.getAzimuthUncertainty(),
          Units.DEGREES);
      NumericMeasurementValue azimuthValue = NumericMeasurementValue.from(arrival, azimuth);
      FeatureMeasurement<NumericMeasurementValue> azimuthMeasurement =
          FeatureMeasurement.create(spectraSegment.getId(),
              FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH,
              azimuthValue);

      DoubleValue slowness = DoubleValue.from(attributes.getSlowness(),
          attributes.getSlownessUncertainty(),
          Units.SECONDS_PER_DEGREE);
      NumericMeasurementValue slownessValue = NumericMeasurementValue.from(arrival, slowness);
      FeatureMeasurement<NumericMeasurementValue> slownessMeasurement =
          FeatureMeasurement.create(spectraSegment.getId(),
              FeatureMeasurementTypes.SLOWNESS,
              slownessValue);

      return SignalDetectionHypothesisDescriptor.from(descriptor.getSignalDetectionHypothesis().toBuilder()
          .addMeasurement(azimuthMeasurement)
          .addMeasurement(slownessMeasurement)
          .build(), descriptor.getStationId());
    }

    return descriptor;
  }

  /**
   * Execute Waveform qc processing using the provided {@link FkSpectraCommand}
   *
   * @param command object describing the Fk Spectrum processing request, not null
   * @return list of generated {@link ChannelSegment}, not null
   */
  public List<ChannelSegment<FkSpectra>> generateFkSpectra(FkSpectraCommand command) {
    checkNotNull(command);
    checkState(initialized, "FkControl must be initialized before execution");

    logger.info("FkControl executing spectra command:\n{}", command.logString());

    final double samplePeriod = 1.0 / command.getSampleRate();
    long nanosToEnd = (long) (samplePeriod * BILLION * (command.getSampleCount() - 1));

    Range<Instant> waveformRange = computeRange(command.getStartTime(),
        command.getStartTime().plusNanos(nanosToEnd), command.getWindowLead(),
        command.getWindowLength());

    Collection<ChannelSegment<Waveform>> channelSegments = retrieveWaveformSegments(
        command.getChannelIds(), waveformRange.lowerEndpoint(), waveformRange.upperEndpoint());

    FkSpectraParameters fkSpectraParameters = fileBasedFkConfiguration
        .createFkSpectraParameters(command, getModalSampleRate(channelSegments));

    FkSpectraPlugin fkSpectraPlugin =
        retrievePlugin(fkSpectraParameters.getPluginName(), fkSpectraPluginRegistry);

    List<FkAttributesParameters> fkAttributesParameters = fileBasedFkConfiguration
        .createFkAttributesParameters(fkSpectraParameters.getDefinition());

    List<FkAttributesPlugin> attributesPlugins = fkAttributesParameters.stream()
        .map(parameters -> retrievePlugin(parameters.getPluginName(), fkAttributesPluginRegistry))
        .collect(Collectors.toList());

    ChannelSegment<FkSpectra> spectraChannelSegment = createFkSpectraSegment(
        fkSpectraPlugin, attributesPlugins,
        fkSpectraParameters.getDefinition(),
        channelSegments,
        command.getStartTime(), command.getOutputChannelId(), "MKAR");

    return List.of(spectraChannelSegment);
  }

  private Collection<ChannelSegment<Waveform>> retrieveWaveformSegments(Set<UUID> channelIds,
      Instant windowStart, Instant windowEnd) {
    Collection<ChannelSegment<Waveform>> channelSegments = coiRepository
        .findWaveformsByChannelsAndTime(
            channelIds, windowStart, windowEnd);

    if (channelSegments.isEmpty()) {
      throw new IllegalArgumentException("No waveforms found on interval "
          + windowStart + " to " + windowEnd
          + " for IDs " + channelIds.toString());
    }

    return channelSegments;
  }

  private <T extends Plugin> T retrievePlugin(String pluginName,
      PluginRegistry<T> pluginRegistry) {

    Optional<T> possiblePlugin = pluginRegistry
        .lookup(RegistrationInfo.create(pluginName, 1, 0, 0));

    Preconditions.checkState(possiblePlugin.isPresent(),
        "Cannot execute Fk spectra plugin: plugin not found for %s", pluginName);

    return possiblePlugin.get();
  }

  static double getModalSampleRate(Collection<ChannelSegment<Waveform>> channelSegments) {
    List<Double> wfSampeRates = channelSegments.stream()
        .map(ChannelSegment::getTimeseries)
        .flatMap(List::stream)
        .map(Waveform::getSampleRate).collect(toList());

    Map<Double, Integer> sampleRateFreqs = new HashMap<>();
    for (Double sampleRate : wfSampeRates) {
      if (sampleRateFreqs.containsKey(sampleRate)) {
        sampleRateFreqs.put(sampleRate, sampleRateFreqs.get(sampleRate) + 1);
      } else {
        sampleRateFreqs.put(sampleRate, 1);
      }
    }

    return Collections.max(sampleRateFreqs.entrySet(), Map.Entry.comparingByValue()).getKey();
  }

  private static Range<Instant> computeRange(Instant start, Instant end, Duration lead,
      Duration length) {
    return Range.closed(start.minus(lead), end.plus(length.minus(lead)));
  }

  /**
   * Creates a new {@link ChannelSegment} as results from Fk Spectra from the input {@link
   * ChannelSegment} using the supplied {@link FkSpectraPlugin}. The new ChannelSegment is defined
   * using the a new output channel id that is not the same as the channel id referenced by the
   * input {@link ChannelSegment}.
   *
   * @param fkSpectraPlugin      The plugin Fk Spectra algorithm
   * @param fkAttributesPlugins  The plugins for Fk Attributes generation given an Fk Spectrum
   * @param fkSpectrumDefinition The Fk Spectrum definition identifying window lead and length,
   *                             low/high frequencies, sample rate, etc. used by the Fk Spectra
   *                             plugin
   * @param channelSegments      The input ChannelSegment supplying the waveforms as input to Fk
   *                             Spectra
   * @return The new ChannelSegment of type FkSpectra
   */
  private ChannelSegment<FkSpectra> createFkSpectraSegment(FkSpectraPlugin fkSpectraPlugin,
      List<FkAttributesPlugin> fkAttributesPlugins,
      FkSpectraDefinition fkSpectrumDefinition,
      Collection<ChannelSegment<Waveform>> channelSegments, Instant startTime,
      UUID outputChannelId, String channelSegmentName) {

    // Create a collection of FkSpectrum to be aggregated by new ChannelSegment<FkSpectra>.
    List<FkSpectra> fkSpectraList = fkSpectraPlugin
        .generateFk(channelSegments, fkSpectrumDefinition);

    logger
        .info("Fk Spectrum on ChannelSegments output {} FkSpectra",
            fkSpectraList.size());

    // Instantiate a List of FkSpectrum objects to be aggregated by
    // ChannelSegment<FkSpectra>.

    UUID channelSegmentId = UUID.randomUUID();

    //TODO: Fix how ChannelSegment's name is derived
    //TODO: Work out creation info problem

    List<FkSpectrum> fkSpectrumListWithAttributes = new ArrayList<>();

    fkSpectraList.get(0).getValues().forEach(spectrum -> {
      FkSpectrum.Builder builder = spectrum.toBuilder();
      fkAttributesPlugins.forEach(plugin -> {
        FkSpectraInfo info = FkSpectraInfo.create(
            fkSpectrumDefinition.getLowFrequencyHz(),
            fkSpectrumDefinition.getHighFrequencyHz(),
            fkSpectrumDefinition.getSlowStartXSecPerKm(),
            fkSpectrumDefinition.getSlowDeltaXSecPerKm(),
            fkSpectrumDefinition.getSlowStartYSecPerKm(),
            fkSpectrumDefinition.getSlowDeltaYSecPerKm()
        );
        builder.setAttributes(plugin.generateFkAttributes(info, spectrum));
      });
      fkSpectrumListWithAttributes.add(builder.build());
    });

    FkSpectra.Builder fkSpectraBuilder = FkSpectra.builder()
        .setStartTime(startTime)
        .setSampleRate(fkSpectrumDefinition.getSampleRateHz())
        .withValues(fkSpectrumListWithAttributes)
        .setMetadata(FkSpectraUtility.createMetadataFromDefinition(fkSpectrumDefinition));

    String pluginsUsedNames = String
        .format("spectra: %s, attributes: [%s]]", fkSpectraPlugin.getName(),
            fkAttributesPlugins.stream().map(Plugin::getName)
                .collect(Collectors.joining(",")));

    String pluginsUsedVersions = String
        .format("spectra: %s, attributes: [%s]]", fkSpectraPlugin.getVersion().toString(),
            fkAttributesPlugins.stream().map(Plugin::getVersion).map(PluginVersion::toString)
                .collect(Collectors.joining(",")));

    return ChannelSegment.from(channelSegmentId, outputChannelId,
       channelSegmentName, ChannelSegment.Type.FK_SPECTRA,
        List.of(fkSpectraBuilder.build()),
        new CreationInfo("FkAnalysis-Control-Service",
            new SoftwareComponentInfo(pluginsUsedNames, pluginsUsedVersions)));
  }

}
