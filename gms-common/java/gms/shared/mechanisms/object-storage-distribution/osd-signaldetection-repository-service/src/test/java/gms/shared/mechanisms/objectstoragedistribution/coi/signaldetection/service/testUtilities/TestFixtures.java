package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.testUtilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Test Fixtures for QcMask, SignalDetection, and BeamCreationInfo
 */
public class TestFixtures {

  //////////////////////////////////////////
  public static final Instant startTime = Instant.parse("2010-05-20T00:59:59.108Z");
  public static final Instant endTime = Instant.parse("2010-05-20T01:00:01.991Z");

  public static final String monitoringOrganization = "CTBTO";

  public static final UUID
      id = UUID.randomUUID(),
      creationInfoId = UUID.randomUUID(),
      PROCESSING_CHANNEL_1_ID = UUID.fromString("cd412982-2996-46d8-812e-e30b5dc7eb62"),
      PROCESSING_CHANNEL_2_ID = UUID.fromString("d1f419f4-10b0-474f-8a95-13a1c89b284e"),
      PROCESSING_CHANNEL_3_ID = UUID.fromString("c39f4136-ced0-4c7f-a314-e6674ee07446"),
      STATION_1_ID = UUID.fromString("ba8a2aa5-ae09-46ea-a15c-87f222980572"),
      STATION_2_ID = UUID.fromString("af694ce7-474f-ced0-812e-96a135792468");

  //////////////////////////////////////////
  public static final QcMask qcMask, qcMask2, qcMask3;
  public static final QcMaskVersion qcMaskVersion, qcMask2Version, qcMask3Version;
  public static final QcMaskVersionDescriptor QC_MASK_VERSION_DESCRIPTOR, qcMask2VersionReference, qcMask3VersionReference;
  public static final List<QcMask> qcMaskList, qcMask2List, qcMask3List;
  public static final String qcMaskJson, qcMask2Json, qcMask3Json;
  public static byte[] qcMaskMsgPack, qcMask2MsgPack, qcMask3MsgPack;


  public static final SignalDetection signalDetection, signalDetection2;
  public static final List<SignalDetection> signalDetectionList, signalDetectionList2;
  public static final String signalDetectionJson, signalDetection2Json;
  public static byte[] signalDetectionMsgPack, signalDetection2MsgPack;


  public static final FeatureMeasurement<InstantValue> arrivalTimeMeasurement;
  public static final FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement;
  public static final List<FeatureMeasurement<?>> featureMeasurements, featureMeasurements2;

  //////////////////////////////////////////
  public static final Instant creationTime = Instant.EPOCH;
  public static final String name = "CHAN SEG NAME (test)";
  public static final UUID processingGroupId = UUID.randomUUID();
  public static final UUID beamChannelId = UUID.randomUUID();
  public static final UUID beamChannelSegmentId = UUID.randomUUID();
  public static final Instant requestedStartTime = Instant.ofEpochSecond(12345);
  public static final Instant requestedEndTime = requestedStartTime.plus(Duration.ofSeconds(90));
  public static final Set<UUID> usedInputChannelIds = new HashSet<>(Arrays.asList(
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));

  public static final BeamCreationInfo beamCreationInfo;

  //////////////////////////////////////////
  public static final UUID channelID = UUID.fromString("d07aa77a-b6a4-478f-b3cd-5c934ee6b812");
  public static final double azimuth = 37.5;
  public static final double slowness = 17.2;
  private static final PhaseType phaseType = PhaseType.P;
  public static final double nominalSampleRate = 20.0;
  public static final double sampleRateTolerance = 2.0;
  public static final Location location = Location.from(1.2, 3.4, 7.8, 5.6);
  public static final int minimumWaveformsForBeam = 2;

  public static boolean coherent = true;
  private static final boolean snappedSampling = true;
  public static boolean twoDimensional = true;

  public static final RelativePosition relativePosition = RelativePosition
      .from(1.2, 3.4, 5.6);

  public static final Map<UUID, RelativePosition> relativePositions = Map.ofEntries(
      Map.entry(channelID, TestFixtures.relativePosition));

  public static final BeamDefinition beamDefinition;

  //////////////////////////////////////////
  public static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  public static final ObjectMapper msgPackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

  public static JavaType chanSegFk;
  public static JavaType listChanSegFk;
  public static JavaType listUuid;
  public static JavaType listSigDet;
  public static JavaType mapUuidListSigDet;

  static {
    chanSegFk = objectMapper.getTypeFactory()
        .constructParametricType(ChannelSegment.class, FkSpectra.class);
    listChanSegFk = objectMapper.getTypeFactory().constructCollectionType(List.class, chanSegFk);
    listUuid = objectMapper.getTypeFactory().constructCollectionType(List.class, UUID.class);

    listSigDet = objectMapper.getTypeFactory().constructCollectionType(List.class, SignalDetection.class);
    JavaType uuid = objectMapper.getTypeFactory().constructType(UUID.class);
    mapUuidListSigDet = objectMapper.getTypeFactory().constructMapType(Map.class, uuid, listSigDet);

    ////////////////////////////////////////////////////
    qcMask = QcMask.create(
        UUID.randomUUID(),
        Arrays.asList(
            QcMaskVersionDescriptor.from(UUID.randomUUID(), 3),
            QcMaskVersionDescriptor.from(UUID.randomUUID(), 1)),
        Arrays.asList(UUID.randomUUID(), UUID.randomUUID()),
        QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.LONG_GAP,
        "Rationale",
        Instant.now(),
        Instant.now().plusSeconds(2));

    qcMask.addQcMaskVersion(
        Arrays.asList(UUID.randomUUID(), UUID.randomUUID()),
        QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.SPIKE,
        "Rationale SPIKE",
        Instant.now().plusSeconds(3),
        Instant.now().plusSeconds(4));

    qcMaskVersion = qcMask.getCurrentQcMaskVersion();
    QC_MASK_VERSION_DESCRIPTOR = qcMaskVersion.getParentQcMasks().iterator().next();
    qcMaskList = List.of(qcMask);

    qcMask2 = QcMask.create(
        PROCESSING_CHANNEL_2_ID,
        Arrays.asList(
            QcMaskVersionDescriptor.from(UUID.randomUUID(), 3),
            QcMaskVersionDescriptor.from(UUID.randomUUID(), 1)),
        Arrays.asList(UUID.randomUUID(), UUID.randomUUID()),
        QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.LONG_GAP,
        "Rationale",
        Instant.now(),
        Instant.now().plusSeconds(2));

    qcMask2.addQcMaskVersion(
        Arrays.asList(UUID.randomUUID(), UUID.randomUUID()),
        QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.SPIKE,
        "Rationale SPIKE",
        Instant.now().plusSeconds(3),
        Instant.now().plusSeconds(4));

    qcMask2Version = qcMask2.getCurrentQcMaskVersion();
    qcMask2VersionReference = qcMask2Version.getParentQcMasks().iterator().next();
    qcMask2List = List.of(qcMask2);

    qcMask3 = QcMask.create(
        PROCESSING_CHANNEL_3_ID,
        Arrays.asList(
            QcMaskVersionDescriptor.from(UUID.randomUUID(), 3),
            QcMaskVersionDescriptor.from(UUID.randomUUID(), 1)),
        Arrays.asList(UUID.randomUUID(), UUID.randomUUID()),
        QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.LONG_GAP,
        "Rationale",
        Instant.now(),
        Instant.now().plusSeconds(2));

    qcMask3.addQcMaskVersion(
        Arrays.asList(UUID.randomUUID(), UUID.randomUUID()),
        QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.SPIKE,
        "Rationale SPIKE",
        Instant.now().plusSeconds(3),
        Instant.now().plusSeconds(4));

    qcMask3Version = qcMask3.getCurrentQcMaskVersion();
    qcMask3VersionReference = qcMask3Version.getParentQcMasks().iterator().next();
    qcMask3List = List.of(qcMask3);

    qcMaskJson = toJson(qcMask);
    qcMask2Json = toJson(qcMask2);
    qcMask3Json = toJson(qcMask3);

    qcMaskMsgPack = toMsgPack(qcMask);
    qcMask2MsgPack = toMsgPack(qcMask2);
    qcMask3MsgPack = toMsgPack(qcMask3);

    ////////////////////////////////////////////////////
    arrivalTimeMeasurement = FeatureMeasurement.from(
        id, STATION_1_ID,
        FeatureMeasurementTypes.ARRIVAL_TIME,
        InstantValue.from(Instant.now(), Duration.ofMillis(0)));
    phaseMeasurement = FeatureMeasurement.from(
        id, STATION_1_ID,
        FeatureMeasurementTypes.PHASE,
        PhaseTypeMeasurementValue.from(PhaseType.P, 0.5));

    featureMeasurements = List.of(arrivalTimeMeasurement, phaseMeasurement);
    featureMeasurements2 = List.of(arrivalTimeMeasurement, phaseMeasurement);

    signalDetection = SignalDetection.from(
        id, monitoringOrganization, STATION_1_ID, Collections.emptyList(), creationInfoId);
    signalDetection.addSignalDetectionHypothesis(featureMeasurements, creationInfoId);

    signalDetection2 = SignalDetection.from(
        id, monitoringOrganization, STATION_2_ID, Collections.emptyList(), creationInfoId);
    signalDetection2.addSignalDetectionHypothesis(featureMeasurements2, creationInfoId);

    signalDetectionList = List.of(signalDetection);
    signalDetectionList2 = List.of(signalDetection2);

    signalDetectionJson = toJson(signalDetection);
    signalDetection2Json = toJson(signalDetection2);

    signalDetectionMsgPack = toMsgPack(signalDetection);
    signalDetection2MsgPack = toMsgPack(signalDetection2);

    ////////////////////////////////////////////////////
    beamDefinition = BeamDefinition.builder()
        .setPhaseType(phaseType)
        .setAzimuth(azimuth)
        .setSlowness(slowness)
        .setCoherent(coherent)
        .setSnappedSampling(snappedSampling)
        .setTwoDimensional(twoDimensional)
        .setNominalWaveformSampleRate(nominalSampleRate)
        .setWaveformSampleRateTolerance(sampleRateTolerance)
        .setBeamPoint(location)
        .setRelativePositionsByChannelId(relativePositions)
        .setMinimumWaveformsForBeam(minimumWaveformsForBeam)
        .build();

    beamCreationInfo = BeamCreationInfo.builder()
        .generatedId()
        .setCreationTime(creationTime)
        .setName(name)
        .setProcessingGroupId(processingGroupId)
        .setChannelId(beamChannelId)
        .setChannelSegmentId(beamChannelSegmentId)
        .setRequestedStartTime(requestedStartTime)
        .setRequestedEndTime(requestedEndTime)
        .setBeamDefinition(beamDefinition)
        .setUsedInputChannelIds(usedInputChannelIds)
        .build();
  }

  private static String toJson(Object object) {
    String json = "{Initialization error}";
    try {
      json = objectMapper.writeValueAsString(List.of(object));
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

  //////////////////////////////////////////

  // Create an FkSpectraDefinition
  private static final Duration windowLead = Duration.ofMinutes(3);
  private static final Duration windowLength = Duration.ofMinutes(2);
  private static final double sampleRate = 1/60.0;

  private static final double lowFrequency = 4.5;
  private static final double highFrequency = 6.0;

  private static final boolean useChannelVerticalOffsets = false;
  private static final boolean normalizeWaveforms = false;
  private static final PhaseType fkPhaseType = PhaseType.P;

  private static final double slowStartX = 5;
  private static final double slowDeltaX = 10;
  private static final int slowCountX = 25;
  private static final double slowStartY = 5;
  private static final double slowDeltaY = 10;
  private static final int slowCountY = 25;
  private static final int quality = 4;

  private static final double waveformSampleRateHz = 10.0;
  private static final double waveformSampleRateToleranceHz = 11.0;

  public static final UUID fkChannelID = UUID.fromString("d07aa77a-b6a4-478f-b3cd-5c934ee6b812");

  // Create a Location
  public static final Location fkLocation = Location.from(1.2, 3.4, 7.8, 5.6);

  // Create a RelativePosition
  public static final RelativePosition fkRelativePosition = RelativePosition
      .from(1.2, 3.4, 5.6);

  private static final Map<UUID, RelativePosition> fkRelativePositions = Map.ofEntries(
      Map.entry(fkChannelID, fkRelativePosition)
  );

  public static final FkSpectraDefinition FK_SPECTRA_DEFINITION = FkSpectraDefinition.builder()
      .setWindowLead(windowLead)
      .setWindowLength(windowLength)
      .setSampleRateHz(sampleRate)
      .setLowFrequencyHz(lowFrequency)
      .setHighFrequencyHz(highFrequency)
      .setUseChannelVerticalOffsets(useChannelVerticalOffsets)
      .setNormalizeWaveforms(normalizeWaveforms)
      .setPhaseType(fkPhaseType)
      .setSlowStartXSecPerKm(slowStartX)
      .setSlowDeltaXSecPerKm(slowDeltaX)
      .setSlowCountX(slowCountX)
      .setSlowStartYSecPerKm(slowStartY)
      .setSlowDeltaYSecPerKm(slowDeltaY)
      .setSlowCountY(slowCountY)
      .setWaveformSampleRateHz(waveformSampleRateHz)
      .setWaveformSampleRateToleranceHz(waveformSampleRateToleranceHz)
      .setBeamPoint(location)
      .setRelativePositionsByChannelId(fkRelativePositions)
      .setMinimumWaveformsForSpectra(2)
      .build();

  public static final Instant fkCreationInfoCreationTime2 = Instant.ofEpochSecond(90000);
  public static final Set<UUID> fkUsedInputChannelIds = new HashSet<>(Arrays.asList(
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));

  public static ChannelSegment<FkSpectra> BuildUniqueFkInput(Instant fkReferenceStartTime) {
    UUID newChannelSegmentId = UUID.randomUUID();

    double[][] fkData1 = new double
        [FK_SPECTRA_DEFINITION.getSlowCountY()]
        [FK_SPECTRA_DEFINITION.getSlowCountX()];

    for (int i = 0; i < FK_SPECTRA_DEFINITION.getSlowCountY(); i++) {
      for (int j = 0; j < FK_SPECTRA_DEFINITION.getSlowCountX(); j++) {
        fkData1[i][j] = j + 1;
      }
    }

    double[][] fkData2 = new double
        [FK_SPECTRA_DEFINITION.getSlowCountY()]
        [FK_SPECTRA_DEFINITION.getSlowCountX()];

    for (int i = 0; i < FK_SPECTRA_DEFINITION.getSlowCountY(); i++) {
      for (int j = 0; j < FK_SPECTRA_DEFINITION.getSlowCountX(); j++) {
        fkData2[i][j] = (j + 1) * 10;
      }
    }

    //TODO: fstat and quality
    FkSpectra.Builder fkSpectra = FkSpectra.builder()
        .setStartTime(fkReferenceStartTime)
        .setSampleRate(FK_SPECTRA_DEFINITION.getSampleRateHz())
        .withValues(List.of(
            FkSpectrum.from(
                fkData1, fkData1, quality),
            FkSpectrum.from(
                fkData2, fkData2, quality)));

    fkSpectra.metadataBuilder()
        .setPhaseType(FK_SPECTRA_DEFINITION.getPhaseType())
        .setSlowStartX(FK_SPECTRA_DEFINITION.getSlowStartXSecPerKm())
        .setSlowDeltaX(FK_SPECTRA_DEFINITION.getSlowDeltaXSecPerKm())
        .setSlowStartY(FK_SPECTRA_DEFINITION.getSlowStartYSecPerKm())
        .setSlowDeltaY(FK_SPECTRA_DEFINITION.getSlowDeltaYSecPerKm());

    return ChannelSegment.from(
        newChannelSegmentId,
        channelID,
        newChannelSegmentId.toString(),
        ChannelSegment.Type.FK_SPECTRA,
        List.of(fkSpectra.build()),
        CreationInfo.DEFAULT
    );
  }

}
