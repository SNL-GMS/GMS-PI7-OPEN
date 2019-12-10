package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkAttributes;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility.FkSpectraUtility;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


public class TestFixtures {

  public static ChannelSegment<Waveform> buildChannelSegment(UUID channelId, double sampleRate,
      Instant start, Instant end) {
    int size = (int) (Duration.between(start, end).toSeconds() * sampleRate);
    return ChannelSegment
        .create(channelId, "TEST", Type.RAW, List.of(buildWaveform(start, sampleRate, size)),
            CreationInfo.DEFAULT);
  }

  public static ChannelSegment<Waveform> buildChannelSegment(UUID channelId, Instant start,
      double sampleRate,
      int sampleCount) {
    return ChannelSegment
        .create(channelId, "TEST", Type.RAW, List.of(buildWaveform(start, sampleRate, sampleCount)),
            CreationInfo.DEFAULT);
  }

  public static Waveform buildWaveform(Instant start, double sampleRate, int samplecount) {
    Random ran = new Random();
    double[] values = new double[samplecount];
    for (int i = 0; i < samplecount; i++) {
      values[i] = ran.nextInt(100000);
    }
    return Waveform.withValues(start, sampleRate, values);
  }

  public static final double SAMPLE_RATE = 2.0;
  public static final double SAMPLE_RATE2 = 5.0;

  public static final UUID SOH_BOOLEAN_ID = UUID.fromString("5f1a3629-ffaf-4190-b59d-5ca6f0646fd6");
  public static final UUID SOH_ANALOG_ID = UUID.fromString("b12c0b3a-4681-4ee3-82fc-4fcc292aa59f");
  public static final UUID CHANNEL_SEGMENT_ID = UUID
      .fromString("57015315-f7b2-4487-b3e7-8780fbcfb413");
  public static final UUID CHANNEL_SEGMENT_2_ID = UUID
      .fromString("67015315-f7b2-4487-b3e7-8780fbcfb413");
  public static final UUID CHANNEL_SEGMENT_6_ID = UUID
      .fromString("77015315-f7b2-4487-b3e7-8780fbcfb413");
  public static final UUID PROCESSING_CHANNEL_ID = UUID
      .fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final UUID PROCESSING_CHANNEL_ID2 = UUID
      .fromString("56947ac2-8c86-4fa1-a764-c9b9944614b7");
  public static final UUID PROCESSING_CHANNEL_ID3 = UUID
      .fromString("66947ac2-8c86-4fa1-a764-c9b9944614b7");
  public static final UUID PROCESSING_CHANNEL_ID6 = UUID
      .fromString("86947ac2-8c86-4fa1-a764-c9b9944614b7");
  public static final UUID FRAME_1_STATION_ID = UUID.fromString(
      "12347cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final UUID FRAME_2_STATION_ID = UUID.fromString(
      "23447cc2-8c86-4fa1-a764-c9b9944614b7");

  // TODO: this test fixtures data had overlapping channel segments,
  // but now changing that breaks the channel availability test.


  public static final String segmentStartDateString = "1970-01-02T03:04:05.123Z";

  public static final Instant SEGMENT_START = Instant.parse(segmentStartDateString);

  public static final double[] WAVEFORM_POINTS = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
  public static final double[] WAVEFORM_POINTS2 = new double[]{6, 7, 8, 9, 10};

  public static final Waveform waveform1 = Waveform.withValues(
      SEGMENT_START, SAMPLE_RATE, WAVEFORM_POINTS);
  public static final Waveform waveform1WithoutSamples = Waveform.withoutValues(
      SEGMENT_START, SAMPLE_RATE, WAVEFORM_POINTS.length);

  public static final Instant SEGMENT_START2 = waveform1.getEndTime().plusSeconds(1);

  public static final Waveform waveform2 = Waveform.withValues(
      SEGMENT_START2, SAMPLE_RATE2, WAVEFORM_POINTS2);
  public static final Waveform waveform3 = buildWaveform(
      Instant.ofEpochSecond(55555), 40.0, 400);

  public static final Waveform waveform4 = buildWaveform(
      waveform3.getEndTime().plusMillis(Double.valueOf((1 / 40.0) * 1000.0).intValue()),
      40.0, 400);
  public static final Waveform waveform5 = buildWaveform(waveform3.getEndTime().minusSeconds(1),
      40.0, 400);
  public static final Waveform waveform6 = buildWaveform(
      SEGMENT_START2, SAMPLE_RATE, WAVEFORM_POINTS.length);

  public static final List<UUID> uuidList = List
      .of(PROCESSING_CHANNEL_ID, PROCESSING_CHANNEL_ID2, PROCESSING_CHANNEL_ID6);
  public static final List<UUID> uuidListForMerging = List.of(PROCESSING_CHANNEL_ID3);
  public static final List<UUID> uuidListChanSeg = List
      .of(CHANNEL_SEGMENT_ID, CHANNEL_SEGMENT_2_ID, CHANNEL_SEGMENT_6_ID);
  public static final List<Waveform> waveforms = List.of(waveform1);
  public static final List<Waveform> waveformsWithoutSamples = List.of(waveform1WithoutSamples);

  public static final List<Waveform> waveforms2 = List.of(waveform2);

  public static final ChannelSegment<Waveform> channelSegment = ChannelSegment.from(
      CHANNEL_SEGMENT_ID, PROCESSING_CHANNEL_ID, CHANNEL_SEGMENT_ID.toString(),
      ChannelSegment.Type.RAW, waveforms, CreationInfo.DEFAULT);

  public static final Instant SEGMENT_END = channelSegment.getEndTime();

  public static final ChannelSegment<Waveform> channelSegmentWithoutWaveforms = ChannelSegment.from(
      CHANNEL_SEGMENT_ID, PROCESSING_CHANNEL_ID, CHANNEL_SEGMENT_ID.toString(),
      ChannelSegment.Type.RAW, waveformsWithoutSamples, CreationInfo.DEFAULT);

  public static final ChannelSegment<Waveform> channelSegment2 = ChannelSegment.from(
      CHANNEL_SEGMENT_2_ID, PROCESSING_CHANNEL_ID, CHANNEL_SEGMENT_2_ID.toString(),
      ChannelSegment.Type.RAW, waveforms2, CreationInfo.DEFAULT);

  public static final ChannelSegment<Waveform> channelSegment6 = ChannelSegment.from(
      CHANNEL_SEGMENT_6_ID, PROCESSING_CHANNEL_ID6, CHANNEL_SEGMENT_6_ID.toString(),
      ChannelSegment.Type.RAW, List.of(waveform6), CreationInfo.DEFAULT);

  public static final Instant SEGMENT_END2 = channelSegment2.getEndTime();

  public static final AcquiredChannelSohBoolean channelSohBool = AcquiredChannelSohBoolean.from(
      SOH_BOOLEAN_ID, PROCESSING_CHANNEL_ID,
      AcquiredChannelSoh.AcquiredChannelSohType.DEAD_SENSOR_CHANNEL,
      SEGMENT_START, SEGMENT_END,
      true, CreationInfo.DEFAULT);

  public static final AcquiredChannelSohAnalog channelSohAnalog = AcquiredChannelSohAnalog.from(
      SOH_ANALOG_ID, PROCESSING_CHANNEL_ID,
      AcquiredChannelSoh.AcquiredChannelSohType.STATION_POWER_VOLTAGE,
      SEGMENT_START, SEGMENT_END,
      1.5, CreationInfo.DEFAULT);

  public static final RawStationDataFrame frame1 = RawStationDataFrame.from(
          UUID.randomUUID(), FRAME_1_STATION_ID, Set.of(UUID.randomUUID()), AcquisitionProtocol.CD11,
          SEGMENT_START, SEGMENT_END,
      SEGMENT_END.plusSeconds(10), new byte[50],
      AuthenticationStatus.AUTHENTICATION_SUCCEEDED, CreationInfo.DEFAULT
  );

  public static final RawStationDataFrame frame2 = RawStationDataFrame.from(
          UUID.randomUUID(), FRAME_2_STATION_ID, Set.of(UUID.randomUUID()), AcquisitionProtocol.CD11,
          SEGMENT_START2, SEGMENT_END2,
      SEGMENT_END2.plusSeconds(10), new byte[50],
      AuthenticationStatus.AUTHENTICATION_FAILED, CreationInfo.DEFAULT
  );

  public static final List<RawStationDataFrame> allFrames = List.of(frame1, frame2);


  // Create an FkSpectraDefinition
  private static final Duration windowLead = Duration.ofMinutes(3);
  private static final Duration windowLength = Duration.ofMinutes(2);
  private static final double sampleRate = 1/60.0;

  private static final double lowFrequency = 4.5;
  private static final double highFrequency = 6.0;

  private static final boolean useChannelVerticalOffsets = false;
  private static final boolean normalizeWaveforms = false;
  private static final PhaseType phaseType = PhaseType.P;

  private static final double slowStartX = 5;
  private static final double slowDeltaX = 10;
  private static final int slowCountX = 25;
  private static final double slowStartY = 5;
  private static final double slowDeltaY = 10;
  private static final int slowCountY = 25;

  private static final double waveformSampleRateHz = 10.0;
  private static final double waveformSampleRateToleranceHz = 11.0;

  public static final UUID channelID = UUID.fromString("d07aa77a-b6a4-478f-b3cd-5c934ee6b812");

  // Create a Location
  public static final Location location = Location.from(1.2, 3.4, 7.8, 5.6);

  // Create a RelativePosition
  public static final RelativePosition relativePosition = RelativePosition
      .from(1.2, 3.4, 5.6);

  private static final Map<UUID, RelativePosition> relativePositions = Map.ofEntries(
      Map.entry(channelID, relativePosition)
  );

  public static final FkSpectraDefinition FK_SPECTRA_DEFINITION = FkSpectraDefinition.builder()
      .setWindowLead(windowLead)
      .setWindowLength(windowLength)
      .setSampleRateHz(sampleRate)
      .setLowFrequencyHz(lowFrequency)
      .setHighFrequencyHz(highFrequency)
      .setUseChannelVerticalOffsets(useChannelVerticalOffsets)
      .setNormalizeWaveforms(normalizeWaveforms)
      .setPhaseType(phaseType)
      .setSlowStartXSecPerKm(slowStartX)
      .setSlowDeltaXSecPerKm(slowDeltaX)
      .setSlowCountX(slowCountX)
      .setSlowStartYSecPerKm(slowStartY)
      .setSlowDeltaYSecPerKm(slowDeltaY)
      .setSlowCountY(slowCountY)
      .setWaveformSampleRateHz(waveformSampleRateHz)
      .setWaveformSampleRateToleranceHz(waveformSampleRateToleranceHz)
      .setBeamPoint(location)
      .setRelativePositionsByChannelId(relativePositions)
      .setMinimumWaveformsForSpectra(2)
      .build();

  // FkSpectraCreationInformation input.
  public static final UUID fkChannelSegmentId1 = UUID
      .fromString("11111111-1111-1111-1111-111111111111");
  public static final Set<UUID> usedInputChannelIds = new HashSet<>(Arrays.asList(
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));

  public static ChannelSegment<FkSpectra> buildUniqueFkInput(
      Instant fkReferenceStartTime) {
    UUID newChannelSegmentId = UUID.randomUUID();

    double[][] fkPower1 = new double
        [FK_SPECTRA_DEFINITION.getSlowCountY()]
        [FK_SPECTRA_DEFINITION.getSlowCountX()];

    for (int i = 0; i < FK_SPECTRA_DEFINITION.getSlowCountY(); i++) {
      for (int j = 0; j < FK_SPECTRA_DEFINITION.getSlowCountX(); j++) {
        if (j == 0) {
          fkPower1[i][j] = Double.NaN;
        } else {
          fkPower1[i][j] = (i * 10) + (j + 1);
        }
      }
    }

    double[][] fkPower2 = new double
        [FK_SPECTRA_DEFINITION.getSlowCountY()]
        [FK_SPECTRA_DEFINITION.getSlowCountX()];

    for (int i = 0; i < FK_SPECTRA_DEFINITION.getSlowCountY(); i++) {
      for (int j = 0; j < FK_SPECTRA_DEFINITION.getSlowCountX(); j++) {
        if (j == 1) {
          fkPower2[i][j] = Double.NaN;
        } else {
          fkPower2[i][j] = (i * 15) + (j + 1);
        }
      }
    }

    double[][] fkFstat1 = new double
        [FK_SPECTRA_DEFINITION.getSlowCountY()]
        [FK_SPECTRA_DEFINITION.getSlowCountX()];

    for (int i = 0; i < FK_SPECTRA_DEFINITION.getSlowCountY(); i++) {
      for (int j = 0; j < FK_SPECTRA_DEFINITION.getSlowCountX(); j++) {
        if (j == 0) {
          fkFstat1[i][j] = Double.NaN;
        } else {
          fkFstat1[i][j] = (i * 20) + (j + 1);
        }
      }
    }

    double[][] fkFstat2 = new double
        [FK_SPECTRA_DEFINITION.getSlowCountY()]
        [FK_SPECTRA_DEFINITION.getSlowCountX()];

    for (int i = 0; i < FK_SPECTRA_DEFINITION.getSlowCountY(); i++) {
      for (int j = 0; j < FK_SPECTRA_DEFINITION.getSlowCountX(); j++) {
        if (j == 1) {
          fkFstat2[i][j] = Double.NaN;
        } else {
          fkFstat2[i][j] = (i * 25) + (j + 1);
        }
      }
    }

    FkAttributes fkAttributes1 = FkAttributes.from(
        1.23,
        4.56,
        7.89,
        0.12,
        3.45
    );

    FkAttributes fkAttributes2 = FkAttributes.from(
        0.98,
        7.65,
        4.32,
        1.09,
        8.76
    );

    FkSpectra.Builder fkSpectra = FkSpectra.builder()
        .setStartTime(fkReferenceStartTime)
        .setSampleRate(FK_SPECTRA_DEFINITION.getSampleRateHz())
        .withValues(List.of(
            FkSpectrum.from(
                fkPower1, fkFstat1, 1, List.of(fkAttributes1)),
            FkSpectrum.from(
                fkPower2, fkFstat2, 1, List.of(fkAttributes2))))
        .setMetadata(FkSpectraUtility.createMetadataFromDefinition(FK_SPECTRA_DEFINITION));

    return ChannelSegment.from(
        newChannelSegmentId,
        channelID,
        newChannelSegmentId.toString(),
        ChannelSegment.Type.FK_SPECTRA,
        List.of(fkSpectra.build()),
        CreationInfo.DEFAULT
    );
  }

  public static ChannelSegment<FkSpectra> buildUniqueFkInput2() {
    return buildUniqueFkInput2(Instant.now().minusSeconds(60 * 60 * 5));
  }

  public static ChannelSegment<FkSpectra> buildUniqueFkInput2(
      Instant fkReferenceStartTime) {
    UUID newChannelSegmentId = UUID.randomUUID();

    double[][] fkPower1 = new double
        [FK_SPECTRA_DEFINITION.getSlowCountY()]
        [FK_SPECTRA_DEFINITION.getSlowCountX()];

    for (int i = 0; i < FK_SPECTRA_DEFINITION.getSlowCountY(); i++) {
      for (int j = 0; j < FK_SPECTRA_DEFINITION.getSlowCountX(); j++) {
        if (j == 0) {
          fkPower1[i][j] = Double.NaN;
        } else {
          fkPower1[i][j] = (i * 10) + (j + 1);
        }
      }
    }

    double[][] fkPower2 = new double
        [FK_SPECTRA_DEFINITION.getSlowCountY()]
        [FK_SPECTRA_DEFINITION.getSlowCountX()];

    for (int i = 0; i < FK_SPECTRA_DEFINITION.getSlowCountY(); i++) {
      for (int j = 0; j < FK_SPECTRA_DEFINITION.getSlowCountX(); j++) {
        if (j == 1) {
          fkPower2[i][j] = Double.NaN;
        } else {
          fkPower2[i][j] = (i * 15) + (j + 1);
        }
      }
    }

    double[][] fkFstat1 = new double
        [FK_SPECTRA_DEFINITION.getSlowCountY()]
        [FK_SPECTRA_DEFINITION.getSlowCountX()];

    for (int i = 0; i < FK_SPECTRA_DEFINITION.getSlowCountY(); i++) {
      for (int j = 0; j < FK_SPECTRA_DEFINITION.getSlowCountX(); j++) {
        if (j == 0) {
          fkFstat1[i][j] = Double.NaN;
        } else {
          fkFstat1[i][j] = (i * 20) + (j + 1);
        }
      }
    }

    double[][] fkFstat2 = new double
        [FK_SPECTRA_DEFINITION.getSlowCountY()]
        [FK_SPECTRA_DEFINITION.getSlowCountX()];

    for (int i = 0; i < FK_SPECTRA_DEFINITION.getSlowCountY(); i++) {
      for (int j = 0; j < FK_SPECTRA_DEFINITION.getSlowCountX(); j++) {
        if (j == 1) {
          fkFstat2[i][j] = Double.NaN;
        } else {
          fkFstat2[i][j] = (i * 25) + (j + 1);
        }
      }
    }

    FkAttributes fkAttributes1 = FkAttributes.from(
        1.23,
        4.56,
        7.89,
        0.12,
        3.45
    );

    FkAttributes fkAttributes2 = FkAttributes.from(
        0.98,
        7.65,
        4.32,
        1.09,
        8.76
    );


    FkSpectra.Builder fkSpectra = FkSpectra.builder()
        .setStartTime(fkReferenceStartTime)
        .setSampleRate(FK_SPECTRA_DEFINITION.getSampleRateHz())
        .withValues(List.of(
            FkSpectrum.from(
                fkPower1, fkFstat1, 1, List.of(fkAttributes1)),
            FkSpectrum.from(
                fkPower2, fkFstat2, 1, List.of(fkAttributes2))))
        .setMetadata(FkSpectraUtility.createMetadataFromDefinition(FK_SPECTRA_DEFINITION));

    return ChannelSegment.from(
        newChannelSegmentId,
        channelID,
        newChannelSegmentId.toString(),
        ChannelSegment.Type.FK_SPECTRA,
        List.of(fkSpectra.build()),
        CreationInfo.DEFAULT);
  }

  public static double azimuth = 103.1992;
  public static double slowness = 85.1;
  public static boolean coherent = true;
  public static boolean snappedSampling = true;
  public static boolean twoDimensional = true;
  public static double nominalSampleRate = 15;
  public static double sampleRateTolerance = 1;
  public static int minimumWaveformsForBeam = 2;
  public static Map<UUID, RelativePosition> beamRelativePositions = Map.ofEntries(
      Map.entry(TestFixtures.channelID, TestFixtures.relativePosition));

  public static final BeamDefinition BEAM_DEFINITION = BeamDefinition
      .from(phaseType, azimuth, slowness, coherent, snappedSampling, twoDimensional,
          nominalSampleRate, sampleRateTolerance, location, beamRelativePositions,
          minimumWaveformsForBeam);


  public static Pair<ChannelSegment<Waveform>, BeamCreationInfo> buildUniqueBeamInput(){
    return buildUniqueBeamInput(Instant.EPOCH.plus(1, ChronoUnit.DAYS), UUID.randomUUID());
  }
  public static Pair<ChannelSegment<Waveform>, BeamCreationInfo> buildUniqueBeamInput(
      Instant creationTime, UUID processingGroupId) {
    UUID channelSegmentId = UUID.randomUUID();

    Instant requestedStartTime = creationTime.minusSeconds(65);
    BeamCreationInfo beamCreationInfo = buildBeamCreationInfo(creationTime, requestedStartTime,
        requestedStartTime.plusSeconds(60), processingGroupId, channelSegmentId);

    double[] beamData = new double[60 * 60 + 1];
    Waveform beamWaveform = Waveform.from(
        requestedStartTime, 60, 3601, beamData);

    ChannelSegment<Waveform> beamChannelSegment = ChannelSegment.from(
        channelSegmentId,
        channelID,
        channelSegmentId.toString(),
        Type.DETECTION_BEAM,
        List.of(beamWaveform),
        CreationInfo.DEFAULT
    );

    return new ImmutablePair<>(beamChannelSegment, beamCreationInfo);
  }

  private static BeamCreationInfo buildBeamCreationInfo(Instant creationTime,
      Instant requestedStartTime, Instant requestedEndTime, UUID processingGroupId,
      UUID channelSegmentId) {
    return BeamCreationInfo.builder()
        .generatedId()
        .setCreationTime(creationTime)
        .setName(channelSegmentId.toString())
        .setProcessingGroupId(processingGroupId)
        .setChannelId(channelID)
        .setChannelSegmentId(channelSegmentId)
        .setRequestedStartTime(requestedStartTime)
        .setRequestedEndTime(requestedEndTime)
        .setBeamDefinition(BEAM_DEFINITION)
        .setUsedInputChannelIds(usedInputChannelIds)
        .build();
  }

  public static final double[][] FK_SPECTRUM_POWER = new double
      [FK_SPECTRA_DEFINITION.getSlowCountY()]
      [FK_SPECTRA_DEFINITION.getSlowCountX()];

  public static final double[][] FK_SPECTRUM_FSTAT = new double
      [FK_SPECTRA_DEFINITION.getSlowCountY()]
      [FK_SPECTRA_DEFINITION.getSlowCountX()];

  public static final List<FkAttributes> FK_SPECTRUM_ATTRIBUTES = List.of(FkAttributes.from(
      1.23,
      4.56,
      7.89,
      0.12,
      3.45
  ));

  public static final FkSpectrum FK_SPECTRUM = FkSpectrum.from(
      FK_SPECTRUM_POWER, FK_SPECTRUM_FSTAT, 1, FK_SPECTRUM_ATTRIBUTES);


}
