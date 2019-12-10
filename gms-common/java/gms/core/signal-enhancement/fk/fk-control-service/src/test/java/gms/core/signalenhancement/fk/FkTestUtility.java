package gms.core.signalenhancement.fk;

import gms.core.signalenhancement.fk.control.FkSpectraCommand;
import gms.core.signalenhancement.fk.control.configuration.FkAttributesParameters;
import gms.core.signalenhancement.fk.plugin.util.FkSpectraInfo;
import gms.core.signalenhancement.fk.service.ContentType;
import gms.core.signalenhancement.fk.service.fkspectra.FkRouteHandler;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.ProcessingResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkAttributes;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra.Metadata;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Objects used in Signal Detector Control HTTP testing
 */
public class FkTestUtility {

  private static final double SAMPLE_RATE = 40.0;

  public static final Instant ARRIVAL_TIME = Instant.EPOCH.plusSeconds(10);

  public static ChannelSegment<Waveform> randomWaveform(UUID channelId, Instant start,
      Instant end) {

    int sampleCount = sampleCount(start, end);
    return ChannelSegment.create(channelId, "Test Waveform", Type.RAW,
        List.of(Waveform.withValues(start, SAMPLE_RATE, randomValues(sampleCount).toArray())),
        creationInfo());
  }

  public static SignalDetectionHypothesis randomSignalDetectionHypothesis(
      UUID signalDetectionHypothesisId) {
    UUID channelSegmentId = UUID.randomUUID();

    return SignalDetectionHypothesis.builder(signalDetectionHypothesisId,
        UUID.randomUUID(),
        false,
        UUID.randomUUID())
        .addMeasurement(
            FeatureMeasurement.create(channelSegmentId,
                FeatureMeasurementTypes.ARRIVAL_TIME,
                InstantValue.from(ARRIVAL_TIME, Duration.ofSeconds(1))))
        .addMeasurement(
            FeatureMeasurement.create(channelSegmentId,
                FeatureMeasurementTypes.PHASE,
                PhaseTypeMeasurementValue.from(PhaseType.P, 1.0)))
        .build();
  }

  public static FkSpectraCommand defaultSpectraCommand() {
    return defaultSpectraCommand(Instant.EPOCH, Set.of(new UUID(0, 0)), Duration.ZERO,
        Duration.ofSeconds(4));
  }

  public static FkSpectraCommand defaultSpectraCommand(Instant start,
      Set<UUID> channelIds, Duration windowLead, Duration windowLength) {
    return FkSpectraCommand.builder()
        .setStartTime(start)
        .setSampleRate(1.0)
        .setSampleCount(1L)
        .setChannelIds(channelIds)
        .setWindowLead(windowLead)
        .setWindowLength(windowLength)
        .setLowFrequency(1.0)
        .setHighFrequency(5.0)
        .setUseChannelVerticalOffset(false)
        .setNormalizeWaveforms(false)
        .setPhaseType(PhaseType.P)
        .setOutputChannelId(new UUID(0, 0))
        .build();
  }

  public static List<SignalDetectionHypothesisDescriptor> defaultInputDescriptors() {
    return List.of(SignalDetectionHypothesisDescriptor
        .from(defaultSignalDetectionHypothesis(new UUID(0, 0), Instant.EPOCH)
                .toBuilder().addMeasurement(FeatureMeasurement
                    .create(UUID.randomUUID(), FeatureMeasurementTypes.PHASE,
                        PhaseTypeMeasurementValue.from(PhaseType.UNKNOWN, 1.0))).build(),
            new UUID(0, 0)));
  }

  public static ProcessingResponse defaultProcessingResponse() {
    return ProcessingResponse.builder()
        .addUpdated(new UUID(0, 0))
        .build();
  }


  /**
   * Generates a relatively default {@link FkSpectraDefinition}. Majority of values come from
   * default FkSpectraDefinition in definition.json
   *
   * @param windowLead Window Lead configuration
   * @param windowLength Window Length configuration
   * @param channelIds Channel {@link UUID}s to generate default relative positions for
   * @return A default spectra definition with the provided values set.
   */
  public static FkSpectraDefinition defaultSpectraDefinition(Duration windowLead,
      Duration windowLength, Collection<UUID> channelIds) {
    return FkSpectraDefinition.builder()
        .setSampleRateHz(1.0)
        .setWindowLead(windowLead)
        .setWindowLength(windowLength)
        .setLowFrequencyHz(1.0)
        .setHighFrequencyHz(5.0)
        .setUseChannelVerticalOffsets(true)
        .setNormalizeWaveforms(false)
        .setPhaseType(PhaseType.P)
        .setSlowStartXSecPerKm(-40.0)
        .setSlowDeltaXSecPerKm(1.0)
        .setSlowCountX(81)
        .setSlowStartYSecPerKm(-40.0)
        .setSlowDeltaYSecPerKm(1.0)
        .setSlowCountY(81)
        .setWaveformSampleRateHz(40.0)
        .setWaveformSampleRateToleranceHz(0.0001)
        .setBeamPoint(defaultLocation())
        .setRelativePositionsByChannelId(
            channelIds.stream().collect(Collectors.toMap(Function.identity(),
                c -> defaultRelativePosition())))
        .setMinimumWaveformsForSpectra(2)
        .build();
  }

  public static FkSpectraInfo spectraInfo(FkSpectraDefinition definition) {
    return FkSpectraInfo.create(
        definition.getLowFrequencyHz(),
        definition.getHighFrequencyHz(),
        definition.getSlowStartXSecPerKm(),
        definition.getSlowDeltaXSecPerKm(),
        definition.getSlowStartYSecPerKm(),
        definition.getSlowDeltaYSecPerKm());
  }

  public static FkSpectraInfo spectraInfo(FkSpectraCommand command,
      FkSpectraDefinition definition) {
    return FkSpectraInfo.create(
        command.getLowFrequency(),
        command.getHighFrequency(),
        command.getSlowStartX().orElseGet(definition::getSlowStartXSecPerKm),
        command.getSlowDeltaX().orElseGet(definition::getSlowDeltaXSecPerKm),
        command.getSlowStartY().orElseGet(definition::getSlowStartYSecPerKm),
        command.getSlowDeltaY().orElseGet(definition::getSlowDeltaYSecPerKm));
  }

  public static List<FkAttributesParameters> fkAttributesParameters(FkSpectraDefinition definition) {
    return List.of(
        FkAttributesParameters.from(
          "mockFkAttributesPlugin",
          Map.of("lowFrequency", definition.getLowFrequencyHz(),
              "highFrequency", definition.getHighFrequencyHz(),
              "eastSlowStart", definition.getSlowStartXSecPerKm(),
              "eastSlowDelta", definition.getSlowDeltaXSecPerKm(),
              "northSlowStart", definition.getSlowStartYSecPerKm(),
              "northSlowDelta", definition.getSlowDeltaYSecPerKm())
    ));
  }

  public static RelativePosition defaultRelativePosition() {
    return RelativePosition.from(0.0, 0.0, 0.0);
  }

  public static Location defaultLocation() {
    return Location.from(0.0, 0.0, 0.0, 0.0);
  }

  public static ChannelSegment<FkSpectra> defaultSpectraSegment(UUID channelId, Instant start,
      double sampleRate) {
    return ChannelSegment.from(new UUID(0, 0), channelId,
        "FkTest", Type.FK_SPECTRA, List.of(defaultSpectra(start, sampleRate)),
        CreationInfo.DEFAULT);
  }

  public static FkSpectra defaultSpectra(Instant start, double sampleRate) {
    return FkSpectra.builder()
        .setStartTime(start)
        .setSampleRate(sampleRate)
        .withValues(List.of(defaultSpectrum()))
        .setMetadata(defaultMetadata())
        .build();
  }

  private static FkSpectrum defaultSpectrum() {
    return FkSpectrum.from(
        new double[][]{{0.0, 1.0, 2.0}, {3.0, 4.0, 5.0}, {6.0, 7.0, 8.0}},
        new double[][]{{0.0, 2.0, 4.0}, {6.0, 8.0, 10.0}, {12.0, 14.0, 16.0}},
        1,
        List.of(defaultAttributes()));
  }

  private static Metadata defaultMetadata() {
    return FkSpectra.Metadata.builder()
        .setSlowStartX(-40.0)
        .setSlowDeltaX(1.0)
        .setSlowStartY(-40.0)
        .setSlowDeltaY(1.0)
        .setPhaseType(PhaseType.P)
        .build();
  }

  public static FkAttributes defaultAttributes() {
    return FkAttributes.from(1.0, 2.0, 3.0, 4.0, 5.0);
  }

  public static FeatureMeasurement<NumericMeasurementValue> defaultAzimuthMeasurement() {
    return FeatureMeasurement.from(new UUID(0, 0),
        new UUID(0, 1), FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH,
        NumericMeasurementValue.from(ARRIVAL_TIME, DoubleValue.from(1.0, 3.0, Units.DEGREES)));
  }

  public static FeatureMeasurement<NumericMeasurementValue> defaultSlownessMeasurement() {
    return FeatureMeasurement.from(new UUID(0, 0),
        new UUID(0, 1), FeatureMeasurementTypes.SLOWNESS,
        NumericMeasurementValue
            .from(ARRIVAL_TIME, DoubleValue.from(2.0, 4.0, Units.SECONDS_PER_DEGREE)));
  }

  private static CreationInfo creationInfo() {
    return CreationInfo.DEFAULT;
  }

  private static DoubleStream randomValues(int count) {
    return new Random().doubles(count);
  }

  private static int sampleCount(Instant start, Instant end) {
    return (int) Math
        .ceil(FkTestUtility.SAMPLE_RATE * (end.toEpochMilli() - start.toEpochMilli()) / 1E3);
  }

  public static SignalDetectionHypothesis defaultSignalDetectionHypothesis(UUID arrivalSegmentId,
      Instant arrivalTime) {
    return SignalDetectionHypothesis.builder(new UUID(0, 0),
        new UUID(0, 0), false, new UUID(0, 0))
        .addMeasurement(
            FeatureMeasurement.create(arrivalSegmentId, FeatureMeasurementTypes.ARRIVAL_TIME,
                InstantValue.from(arrivalTime, Duration.ZERO))).build();
  }

  /**
   * Determines whether the {@link ContentType} is acceptable to {@link
   * FkRouteHandler#interactiveSpectra(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return whether the contentType is acceptable to the spectra interface
   */
  public static boolean spectraRequestTypeFilter(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON
        || contentType == ContentType.APPLICATION_MSGPACK;
  }

  /**
   * Determines whether the {@link ContentType} is unacceptable to {@link
   * FkRouteHandler#interactiveSpectra(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return whether the contentType is unacceptable to the spectra interface
   */
  public static boolean invalidSpectraRequestTypeFilter(ContentType contentType) {
    return !spectraRequestTypeFilter(contentType);
  }

  /**
   * Determines whether the {@link ContentType} is acceptable to {@link
   * FkRouteHandler#interactiveSpectra(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return wheter the contentType is acceptable to the spectra interface
   */
  public static boolean spectraResponseTypeFilter(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON
        || contentType == ContentType.APPLICATION_MSGPACK
        || contentType == ContentType.APPLICATION_ANY;
  }

  /**
   * Determines whether the {@link ContentType} is unacceptable to {@link
   * FkRouteHandler#interactiveSpectra(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return whether the contentType is unacceptable to the spectra interface
   */
  public static boolean invalidSpectraResponseTypeFilter(ContentType contentType) {
    return !spectraResponseTypeFilter(contentType);
  }

  public static boolean analysisRequestTypeFilter(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON
        || contentType == ContentType.APPLICATION_MSGPACK;
  }

  public static boolean invalidAnalysisRequestTypeFilter(ContentType contentType) {
    return !analysisRequestTypeFilter(contentType);
  }

  public static boolean analysisResponseTypeFilter(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON
        || contentType == ContentType.APPLICATION_MSGPACK
        || contentType == ContentType.APPLICATION_ANY;
  }

  public static boolean invalidAnalysisResponseTypeFilter(ContentType contentType) {
    return !analysisResponseTypeFilter(contentType);
  }

  /**
   * Obtain the {@link ContentType}s passing the provided filter
   *
   * @param filter {@link ContentType} predicate
   * @return stream of ContentType matching the filter
   */
  public static Stream<ContentType> getContentTypeStream(Predicate<ContentType> filter) {
    return Arrays.stream(ContentType.values()).filter(filter);
  }
}
