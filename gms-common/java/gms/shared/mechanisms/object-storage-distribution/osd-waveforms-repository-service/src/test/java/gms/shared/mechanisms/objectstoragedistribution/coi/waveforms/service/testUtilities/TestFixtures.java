package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.testUtilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.*;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class TestFixtures {

  public static ObjectMapper objectMapper;
  public static ObjectMapper msgPackMapper;
  private static final String segmentStartDateString = "1970-01-02T03:04:05.123Z";
  private static final double SAMPLE_RATE = 2.0;
  private static final int SAMPLE_COUNT = 5;
  private static final long segmentLengthMillis = 2000;
  private static final double[] WAVEFORM_POINTS = new double[] {1.1, 2.2, 3.3, 4.4, 5.5};
  public static final Instant SEGMENT_START = Instant.parse(segmentStartDateString);
  public static final Instant SEGMENT_END = SEGMENT_START.plusMillis(segmentLengthMillis);
  public static final String stationName = "A1e5769fe-29e1-4e4c-8219-2f8f581038a1BC";
  public static final Instant startTime = Instant.parse("2010-05-20T00:59:59.108Z");
  public static final Instant endTime = Instant.parse("2010-05-20T01:00:01.991Z");
  private static final String sohIdString = "b38ae749-2833-4197-a8cb-4609ddd4342f";
  public static UUID acquiredChannelSohId = UUID.fromString(sohIdString);

  public static final UUID
      CHANNEL_SEGMENT_ID = UUID.fromString("627adaf7-417e-4ecd-9c5f-767a23d06bbc"),
      PROCESSING_CHANNEL_1_ID = UUID.fromString("cd412982-2996-46d8-812e-e30b5dc7eb62");

  public static final UUID PROCESSING_CALIBRATION_1_ID = UUID
      .fromString("ce7c377a-b6a4-478f-b3bd-5c934ee6b7ef");

  public static final UUID
      ACQUIRED_CHANNEL_SOH_1_ID = UUID.fromString("627adaf7-417e-4ecd-9c5f-767a23d06bbc"),
      ACQUIRED_CHANNEL_SOH_2_ID = UUID.fromString("ce7c377a-b6a4-478f-b3bd-5c934ee6b7ef"),
      RAW_STATION_DATA_FRAME_ID = UUID.fromString("fa943175-17c2-87d3-4b1f-b21d4ef92a81");

  public static final Waveform waveform1 = Waveform.from(SEGMENT_START, SAMPLE_RATE,
      SAMPLE_COUNT, WAVEFORM_POINTS);

  public static final List<Waveform> waveforms = List.of(waveform1);

  public static final RawStationDataFrame frame1 = RawStationDataFrame.from(
      UUID.randomUUID(), UUID.randomUUID(), Set.of(UUID.randomUUID()), AcquisitionProtocol.CD11,
      SEGMENT_START, SEGMENT_END.plusSeconds(10),
      Instant.parse("2016-05-06T07:08:09Z"), new byte[50],
      AuthenticationStatus.AUTHENTICATION_SUCCEEDED, CreationInfo.DEFAULT
  );

  public static final RawStationDataFrame frame2 = RawStationDataFrame.from(
      UUID.randomUUID(), UUID.randomUUID(), Set.of(UUID.randomUUID()), AcquisitionProtocol.CD11,
      frame1.getPayloadDataEndTime().plusSeconds(1),
      frame1.getPayloadDataEndTime().plusSeconds(11), Instant.parse("2015-01-01T12:34:56Z"),
      new byte[50], AuthenticationStatus.AUTHENTICATION_FAILED, CreationInfo.DEFAULT
  );

  public static final RawStationDataFrame frame3 = RawStationDataFrame.from(
      RAW_STATION_DATA_FRAME_ID, UUID.randomUUID(), Set.of(UUID.randomUUID()),
      AcquisitionProtocol.CD11,
      SEGMENT_START, SEGMENT_END.plusSeconds(10),
      Instant.parse("2016-05-06T07:08:09Z"), new byte[50],
      AuthenticationStatus.AUTHENTICATION_SUCCEEDED, CreationInfo.DEFAULT
  );
  public static final List<RawStationDataFrame> allFrames = List.of(frame1, frame2);

  public static final SoftwareComponentInfo softwareComponentInfo = new SoftwareComponentInfo(
      "unit test component name",
      "unit test component version");

  public static final CreationInfo creationInfo = new CreationInfo(
      "unit test creator name",
      softwareComponentInfo);

  private static final Location location = Location.from(0.1, 0.2, 0.3, 0.4);
  public static final BeamDefinition beamDefinition = BeamDefinition.from(PhaseType.P, 1.23, 4.56,
      false, true, false,
      40.0, 0.001, location,
      Map.of(UUID.randomUUID(), RelativePosition.from(1.0, 2.0, 3.0)),
      2);

  private static final Duration windowLead = Duration.ofMinutes(1);
  private static final Duration windowLength = Duration.ofMinutes(2);
  private static final PhaseType phaseType = PhaseType.P;
  private static final double slowStartX = -.5;
  private static final double slowDeltaX = 0.1;
  private static final double slowStartY = -.2;
  private static final double slowDeltaY = 0.2;

  private static final double fkSampleRate = 1.0;
  private static final int fkQual = 4;
  private static final double[][] fkSpectrumPower = new double[][] {
      {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5},
      {5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5},
      {-.5, -.4, -.3, -.2, -.1, 0, .1, .2, .3, .4, .5}
  };

  private static final double[][] fkSpectrumFstat = new double[][] {
      {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5},
      {5, 4, 3, 2, 1, 0, 1, 2, 3, 4, 5},
      {-.5, -.4, -.3, -.2, -.1, 0, .1, .2, .3, .4, .5}
  };

  private static final FkSpectrum fkSpectrum = FkSpectrum
      .from(fkSpectrumPower, fkSpectrumFstat, fkQual);

  private static final FkSpectra.Metadata fkMetadata = FkSpectra.Metadata.builder()
      .setPhaseType(phaseType)
      .setSlowStartX(slowStartX)
      .setSlowDeltaX(slowDeltaX)
      .setSlowStartY(slowStartY)
      .setSlowDeltaY(slowDeltaY)
      .build();

  private static final FkSpectra fkSpectra = FkSpectra.builder()
      .setStartTime(startTime)
      .setSampleRate(fkSampleRate)
      .withValues(List.of(fkSpectrum))
      .setMetadata(fkMetadata)
      .build();

  public static final BeamCreationInfo beamCreationInfo;
  public static final ChannelSegment<Waveform> channelSegment1;
  public static final ChannelSegment<Waveform> channelSegment2;
  public static final ChannelSegment<FkSpectra> fkSegment;
  public static final ChannelSegment<FkSpectra> fkSegment2;
  public static final AcquiredChannelSohAnalog channelSohAnalog;
  public static final AcquiredChannelSohAnalog channelSohAnalog2;
  public static final AcquiredChannelSohBoolean channelSohBoolean;
  public static final AcquiredChannelSohBoolean channelSohBoolean2;
  public static final String channelSegmentAsJson;
  public static final byte[] channelSegmentAsMsgPack;
  public static final String channelSohBooleanAsJson;
  public static final String channelSohBooleanListAsJson;
  public static final String channelSohAnalogAsJson;
  public static final String channelSohAnalogListAsJson;
  public static final List<AcquiredChannelSohBoolean> channelSohBooleanListAsList;
  public static final List<AcquiredChannelSohAnalog> channelSohAnalogListAsList;

  static {
    channelSegment1 = ChannelSegment.from(
        CHANNEL_SEGMENT_ID, PROCESSING_CHANNEL_1_ID, "ChannelName",
        ChannelSegment.Type.RAW, waveforms, CreationInfo.DEFAULT);

    channelSegment2 = ChannelSegment.from(
        CHANNEL_SEGMENT_ID, PROCESSING_CHANNEL_1_ID, "segmentName2",
        ChannelSegment.Type.RAW, waveforms, CreationInfo.DEFAULT);

    fkSegment = ChannelSegment.create(UUID.randomUUID(), "fk",
        ChannelSegment.Type.FK_SPECTRA,
        List.of(fkSpectra),
        CreationInfo.DEFAULT);

    fkSegment2 = ChannelSegment.create(UUID.randomUUID(), "fk 2",
        ChannelSegment.Type.FK_SPECTRA,
        List.of(fkSpectra.toBuilder().build()),
        CreationInfo.DEFAULT);

    channelSohAnalog = AcquiredChannelSohAnalog.from(ACQUIRED_CHANNEL_SOH_1_ID,
        UUID.randomUUID(), AcquiredChannelSohType.STATION_POWER_VOLTAGE,
        TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END, 1.5, CreationInfo.DEFAULT);

    channelSohAnalog2 = AcquiredChannelSohAnalog.from(ACQUIRED_CHANNEL_SOH_2_ID,
        UUID.randomUUID(), AcquiredChannelSohType.AUTHENTICATION_SEAL_BROKEN,
        TestFixtures.SEGMENT_START, TestFixtures.SEGMENT_END, 1.5, CreationInfo.DEFAULT);

    channelSohBoolean = AcquiredChannelSohBoolean.from(ACQUIRED_CHANNEL_SOH_1_ID,
        UUID.randomUUID(), AcquiredChannelSohType.DEAD_SENSOR_CHANNEL,
        SEGMENT_START, SEGMENT_END, true, CreationInfo.DEFAULT);

    channelSohBoolean2 = AcquiredChannelSohBoolean.from(ACQUIRED_CHANNEL_SOH_2_ID,
        UUID.randomUUID(), AcquiredChannelSohType.BACKUP_POWER_UNSTABLE,
        SEGMENT_START, SEGMENT_END, true, CreationInfo.DEFAULT);

    objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    msgPackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

    channelSegmentAsJson = toJson(channelSegment1);
    channelSegmentAsMsgPack = toMsgPack(channelSegment1);

    channelSohBooleanAsJson = toJson(channelSohBoolean);
    channelSohBooleanListAsList = generateMockBooleanSoh(
        UUID.fromString("b38ae749-2833-4197-a8cb-4609ddd4342f"),
        Instant.parse("2017-11-29T10:30:00.000Z"), Instant.parse("2017-11-29T10:35:00.000Z"));
    channelSohBooleanListAsJson = toJson(channelSohBooleanListAsList);

    channelSohAnalogAsJson = toJson(channelSohAnalog);
    channelSohAnalogListAsList = generateMockAnalogSoh(
        UUID.fromString("b38ae749-2833-4197-a8cb-4609ddd4342f"),
        Instant.parse("2017-11-29T10:30:00.000Z"), Instant.parse("2017-11-29T10:35:00.000Z"));
    channelSohAnalogListAsJson = toJson(channelSohAnalogListAsList);

    beamCreationInfo = BeamCreationInfo.from(UUID.randomUUID(),
        Instant.EPOCH, "beam name", Optional.of(UUID.randomUUID()), channelSegment1.getChannelId(),
        channelSegment1.getId(), Instant.EPOCH, Instant.EPOCH.plusSeconds(5), beamDefinition,
        Set.of(PROCESSING_CHANNEL_1_ID));
  }

  private static String toJson(Object object) {
    String json = "{Initialization error}";
    try {
      json = objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return json;
  }

  private static byte[] toMsgPack(Object object) {
    byte[] byteMessage = null;
    try {
      byteMessage = msgPackMapper.writeValueAsBytes(object);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    return byteMessage;
  }

  private static List<AcquiredChannelSohBoolean> generateMockBooleanSoh(UUID channelId,
      Instant startTime, Instant endTime) {

    CreationInfo creationInfo = new CreationInfo("unitTest", Instant.now(),
        new SoftwareComponentInfo("CoiWaveformHttpControllerTests", "1.0.0"));

    List<AcquiredChannelSohBoolean> mockResult = new ArrayList<>();
    Duration step = Duration.between(startTime, endTime).dividedBy(10);

    for (Instant time = startTime; endTime.isAfter(time); time = time.plus(step)) {
      mockResult.add(AcquiredChannelSohBoolean.create(channelId,
          AcquiredChannelSohType.AUTHENTICATION_SEAL_BROKEN, time, time.plus(step), false,
          creationInfo));

      mockResult.add(
          AcquiredChannelSohBoolean
              .create(channelId, AcquiredChannelSohType.CLIPPED, time,
                  time.plus(step), true, creationInfo));
    }

    return mockResult;
  }

  private static List<AcquiredChannelSohAnalog> generateMockAnalogSoh(UUID channelId,
      Instant startTime, Instant endTime) {

    CreationInfo creationInfo = new CreationInfo("unitTest", Instant.now(),
        new SoftwareComponentInfo("CoiWaveformHttpControllerTests", "1.0.0"));

    List<AcquiredChannelSohAnalog> mockResult = new ArrayList<>();
    Duration step = Duration.between(startTime, endTime).dividedBy(10);

    for (Instant time = startTime; endTime.isAfter(time); time = time.plus(step)) {
      mockResult.add(AcquiredChannelSohAnalog.create(channelId,
          AcquiredChannelSohType.STATION_POWER_VOLTAGE, time, time.plus(step), Math.random(),
          creationInfo));
    }

    return mockResult;
  }

}
