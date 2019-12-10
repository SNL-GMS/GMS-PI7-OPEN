package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Calibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroupType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Response;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.DigitizerManufacturers;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.DigitizerModels;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizer;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetworkMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSiteMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStationMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class TestFixtures {

  static final UUID
      UNKNOWN_UUID = UUID.fromString("515bcbe0-2c0d-48ec-83f5-9f11cfe30318"),
      CALIBRATION_ID = UUID.fromString("ce7c377a-b6a4-478f-b3bd-5c934ee6b7ef"),
      DIGITIZER_ID = UUID.fromString("0be27c41-3c14-479a-8f87-66a05e8b3936"),
      RESPONSE_ID = UUID.fromString("ce7c377a-b6a4-478f-b3bd-5c934ee6b7ea");

  private static final String comment = "This is a comment.";
  private static final String description = "This is a description.";

  private static final Instant actualTime = Instant.parse("1980-01-02T03:04:05.123Z");
  private static final Instant systemTime = Instant.parse("2010-11-07T06:05:04.321Z");

  private static final InformationSource source = InformationSource.create("Internet",
      actualTime, comment);

  private static final StatusType STATUS = StatusType.ACTIVE;

  public static final ChannelProcessingGroup channelProcessingGroup1 = ChannelProcessingGroup.from(
      UUID.randomUUID(),
      ChannelProcessingGroupType.BEAM,
      Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
      Instant.now().minusSeconds(6000),
      Instant.now().minusSeconds(10),
      "Status..",
      "Comment..");

  public static final ChannelProcessingGroup channelProcessingGroup2 = ChannelProcessingGroup.from(
      UUID.randomUUID(),
      ChannelProcessingGroupType.THREE_COMPONENT,
      Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
      Instant.now().minusSeconds(3000),
      Instant.now().minusSeconds(5),
      "Status..",
      "Comment..");

  // Create an FkSpectraDefinition
  public static final UUID channelID = UUID.fromString("d07aa77a-b6a4-478f-b3cd-5c934ee6b812");

  // Create a Location
  public static final Location location = Location.from(1.2, 3.4, 7.8, 5.6);

  // Create a RelativePosition
  public static final RelativePosition relativePosition = RelativePosition
      .from(1.2, 3.4, 5.6);


  // FkSpectraDefinition
  private static final Duration windowLead = Duration.ofMinutes(3);
  private static final Duration windowLength = Duration.ofMinutes(2);
  private static final double fkSampleRate = 1/60.0;

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

  private static final int minimumWaveformsForBeam = 2;

  private static final Map<UUID, RelativePosition> relativePositions = Map
      .ofEntries(
          Map.entry(TestFixtures.channelID, TestFixtures.relativePosition)
      );

  public static final BeamDefinition BEAM_DEFINITION = BeamDefinition.builder()
      .setPhaseType(PhaseType.P)
      .setAzimuth(22.3)
      .setSlowness(22.8)
      .setNominalWaveformSampleRate(27.8)
      .setWaveformSampleRateTolerance(2.3)
      .setCoherent(false)
      .setSnappedSampling(false)
      .setTwoDimensional(true)
      .setBeamPoint(location)
      .setRelativePositionsByChannelId(relativePositions)
      .setMinimumWaveformsForBeam(minimumWaveformsForBeam)
      .build();

  public static final BeamCreationInfo BEAM_CREATION_INFO = BeamCreationInfo.builder()
      .setId(new UUID(0, 0))
      .setCreationTime(Instant.EPOCH)
      .setName("TEST_FIXTURE")
      .setProcessingGroupId(new UUID(0, 1))
      .setChannelId(new UUID(0, 2))
      .setChannelSegmentId(new UUID(0, 3))
      .setRequestedStartTime(Instant.EPOCH.minusSeconds(65))
      .setRequestedEndTime(Instant.EPOCH.minusSeconds(5))
      .setBeamDefinition(BEAM_DEFINITION)
      .setUsedInputChannelIds(Set.of(channelID))
      .build();

  public static final FkSpectraDefinition FK_SPECTRA_DEFINITION = FkSpectraDefinition.builder()
      .setWindowLead(windowLead)
      .setWindowLength(windowLength)
      .setSampleRateHz(fkSampleRate)
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

  public static final FkSpectraDefinition FK_SPECTRA_DEFINITION_2 = FkSpectraDefinition.builder()
      .setWindowLead(windowLead)
      .setWindowLength(windowLength)
      .setSampleRateHz(fkSampleRate)
      .setLowFrequencyHz(lowFrequency + 1)
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

  private static final UUID CHANNEL_SEGMENT_ID = UUID.fromString("51111111-1111-1111-1111-111111111111");
  private static final UUID PROCESSING_GROUP_ID = UUID
      .fromString("71111111-1111-1111-1111-111111111111");
  public static final Set<UUID> USED_INPUT_CHANNEL_IDS = new HashSet<>(Arrays.asList(
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));

  // channels and digitizers
  static final String channelName = "CHN01";
  private static final ChannelType chanType = ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST;
  private static final ChannelDataType dataType = ChannelDataType.HYDROACOUSTIC_ARRAY;
  private static final double
      lat = 12.34, lon = 56.78, elev = 89.90,
      depth = 4321.0, vertAngle = 125.1, horizAngle = 216.2, sampleRate = 40,
      displacementNorth = 2.01, displacementEast = 2.95, displacementVert = 0.56;

  private static final RelativePosition position = RelativePosition.from(
      displacementNorth, displacementEast, displacementVert);

  private static final RelativePosition referencePosition = RelativePosition.from(
      displacementNorth, displacementEast, displacementVert)
;
  static final ReferenceDigitizer refDigitizer = ReferenceDigitizer.create(
      "digitizer name", DigitizerManufacturers.TRIMBLE,
      DigitizerModels.REFTEK, "12345", actualTime, systemTime,
      source, "comment", "description");

  static final ReferenceChannel refChannel = ReferenceChannel.create(
      channelName, chanType, dataType, "0",
      lat, lon, elev, depth, vertAngle,
      horizAngle, sampleRate, actualTime, systemTime, source, comment, referencePosition, List.of());

  static final Channel channel = Channel.from(
      refChannel.getVersionId(), channelName, chanType,
      dataType, lat, lon, elev, depth, vertAngle,
      horizAngle, sampleRate);

  ////////////////////////////////////////////////////////////////////////////////////

  // calibrations
  private static final double calibrationFactor = 2.5;
  private static final double calibrationFactorError = 0.9876;
  private static final double calibrationPeriod = 1.0;
  private static final double timeShift = 0.0;
  private static final double calibrationInterval = 1.0;

  static final ReferenceCalibration refCalibration = ReferenceCalibration.create(
      refChannel.getVersionId(), calibrationInterval, calibrationFactor,
      calibrationFactorError, calibrationPeriod, timeShift, actualTime, systemTime,
      source, "comment");
  static final Calibration calibration = Calibration.from(
      CALIBRATION_ID, calibrationFactor,
      calibrationPeriod, calibrationFactorError, timeShift);
  ////////////////////////////////////////////////////////////////////////////////////

  // responses
  private static final byte[] RESPONSE_DATA = new byte[]{
      (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5};
  public static final ReferenceResponse refResponse = ReferenceResponse.from(RESPONSE_ID,
      refChannel.getVersionId(), "response type", RESPONSE_DATA, "units",
      actualTime, systemTime, source, "comment");
  static final Response response = Response.from(RESPONSE_ID, RESPONSE_DATA);
  ////////////////////////////////////////////////////////////////////////////////////

  // sites
  static final String siteName = "SITE33";

  static final ReferenceSite refSite = ReferenceSite.create(
      siteName, description, source, comment, lat, lon, elev, actualTime,
      systemTime, referencePosition, List.of());

  static final Site
      slimSite = Site.from(refSite.getVersionId(), siteName, lat, lon, elev, Set.of()),
      fatSite = Site.from(refSite.getVersionId(), siteName, lat, lon, elev, Set.of(channel));
  ////////////////////////////////////////////////////////////////////////////////////

  // stations
  static final String stationName = "STA01";
  static final StationType stationType = StationType.Hydroacoustic;
  static final ReferenceStation refStation = ReferenceStation.create(
      stationName, description, StationType.Hydroacoustic, source, comment, lat, lon, elev,
      actualTime, systemTime, List.of());
  static final Station
      slimStation = Station.from(refStation.getVersionId(), stationName, description, stationType, lat, lon, elev, Set.of()),
      fatStation = Station.from(refStation.getVersionId(), stationName, description, stationType, lat, lon, elev, Set.of(fatSite));
  ////////////////////////////////////////////////////////////////////////////////////

  // networks
  static final String networkName = "NET01";
  private static final NetworkOrganization org = NetworkOrganization.CTBTO;
  private static final NetworkRegion region = NetworkRegion.GLOBAL;
  static final ReferenceNetwork refNetwork = ReferenceNetwork.create(
      networkName, description, org, region, source, comment, actualTime, systemTime);
  static final Network
      slimNetwork = Network.from(refNetwork.getVersionId(), networkName, org, region, Set.of()),
      fatNetwork = Network.from(refNetwork.getVersionId(), networkName, org, region, Set.of(fatStation));
  ////////////////////////////////////////////////////////////////////////////////////
  public static final ReferenceNetworkMembership networkMembership =
      ReferenceNetworkMembership.create("", actualTime, systemTime,
          refNetwork.getEntityId(), refStation.getEntityId(), STATUS);

  public static final ReferenceStationMembership stationMembership
      = ReferenceStationMembership.create("", actualTime, systemTime, refStation.getEntityId(), refSite.getEntityId(), STATUS);

  public static final ReferenceSiteMembership siteMembership
      = ReferenceSiteMembership.create("", actualTime, systemTime,
      refSite.getEntityId(), refChannel.getEntityId(), STATUS);
}
