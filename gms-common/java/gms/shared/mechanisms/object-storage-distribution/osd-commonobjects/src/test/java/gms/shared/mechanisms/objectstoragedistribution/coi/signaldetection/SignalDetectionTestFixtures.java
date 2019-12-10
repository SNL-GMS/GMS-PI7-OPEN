package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.AmplitudeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Calibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroupType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.FirstMotionMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FirstMotionType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Response;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Defines objects used in testing
 */
public class SignalDetectionTestFixtures {

  public static final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  // QcMask Related Test Fixtures

  public static final QcMask qcMask;
  public static final QcMaskVersion qcMaskVersion;
  public static final QcMaskVersionDescriptor QC_MASK_VERSION_DESCRIPTOR;

  static {
    qcMask = QcMask.create(UUID.randomUUID(),
        List.of(QcMaskVersionDescriptor.from(UUID.randomUUID(), 3),
            QcMaskVersionDescriptor.from(UUID.randomUUID(), 1)),
        List.of(UUID.randomUUID(), UUID.randomUUID()), QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.LONG_GAP, "Rationale", Instant.now(), Instant.now().plusSeconds(2));

    qcMask.addQcMaskVersion(List.of(UUID.randomUUID(), UUID.randomUUID()),
        QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SPIKE, "Rationale SPIKE",
        Instant.now().plusSeconds(3), Instant.now().plusSeconds(4));

    qcMaskVersion = qcMask.getCurrentQcMaskVersion();

    QC_MASK_VERSION_DESCRIPTOR = qcMaskVersion.getParentQcMasks().iterator().next();
  }

  // Processing Station Reference Test Fixtures

  public static final double lat = 67.00459;
  public static final double lon = -103.00459;
  public static final double elev = 13.05;
  public static final double depth = 6.899;
  public static final double verticalAngle = 3.4;
  public static final double horizontalAngle = 5.7;
  public static final String description = "";
  private static final double sampleRate = 60.0;

  // Create a Response
  public static final UUID responseID = UUID.fromString("cccaa77a-b6a4-478f-b3cd-5c934ee6b999");
  public static final byte[] responseData = "2M64390-amYq45pi5qag".getBytes();
  public static final Response response = Response.from(responseID, responseData);

  // Create a Calibration
  public static final UUID calibID = UUID.fromString("5432a77a-b6a4-478f-b3cd-5c934ee6b000");
  public static final double factor = 1.2;
  public static final double factorError = 0.112;
  public static final double period = 14.5;
  public static final double timeShift = 2.24;
  public static final Calibration calibration = Calibration.from(calibID, factor, period,
      factorError, timeShift);


  // Create a Channel
  public static final UUID channelID = UUID.fromString("d07aa77a-b6a4-478f-b3cd-5c934ee6b812");
  public static final String channelName = "CHAN01";
  public static final ChannelType channelType = ChannelType.BROADBAND_HIGH_GAIN_EAST_WEST;
  public static final ChannelDataType channelDataType = ChannelDataType.SEISMIC_3_COMPONENT;
  public static final Channel channel = Channel.from(channelID, channelName, channelType,
      channelDataType, lat, lon, elev, depth, verticalAngle, horizontalAngle, sampleRate);

  // Create a Channel Segment
  public static final UUID PROCESSING_CHANNEL_1_ID = UUID
      .fromString("46947cc2-8c86-4fa1-a764-c9b9944614b7");
  public static final Instant SEGMENT_START = Instant.parse("1970-01-02T03:04:05.123Z");
  public static final Instant SEGMENT_END = SEGMENT_START.plusMillis(2000);
  public static final double SAMPLE_RATE = 2.0;
  public static final double[] WAVEFORM_POINTS = new double[]{1.1, 2.2, 3.3, 4.4, 5.5};
  public static final Waveform waveform1 = Waveform.withValues(SEGMENT_START, SAMPLE_RATE,
      WAVEFORM_POINTS);
  public static final Collection<Waveform> waveforms = Collections.singleton(waveform1);
  public static final UUID CHANNEL_SEGMENT_ID = UUID
      .fromString("57015315-f7b2-4487-b3e7-8780fbcfb413");
  public static final ChannelSegment<Waveform> channelSegment = ChannelSegment
      .from(CHANNEL_SEGMENT_ID,
          SignalDetectionTestFixtures.PROCESSING_CHANNEL_1_ID, "segmentName",
          ChannelSegment.Type.RAW, waveforms, CreationInfo.DEFAULT);

  // Create a Location
  public static final Location location = Location.from(1.2, 3.4, 7.8, 5.6);

  // Create a RelativePosition
  public static final RelativePosition relativePosition = RelativePosition
      .from(1.2, 3.4, 5.6);

  // Create an FkSpectraDefinition
  private static final Duration windowLead = Duration.ofMinutes(3);
  private static final Duration windowLength = Duration.ofMinutes(2);
  private static final double fkSampleRate = 1/60.0;

  private static final double lowFrequency = 4.5;
  private static final double highFrequency = 6.0;

  private static final boolean useChannelVerticalOffsets = false;
  private static final boolean normalizeWaveforms = false;
  private static final PhaseType phaseType = PhaseType.P;

  private static final double eastSlowStart = 5;
  private static final double eastSlowDelta = 10;
  private static final int eastSlowCount = 25;
  private static final double northSlowStart = 5;
  private static final double northSlowDelta = 10;
  private static final int northSlowCount = 25;

  private static final double waveformSampleRateHz = 10.0;
  private static final double waveformSampleRateToleranceHz = 11.0;

  private static final Map<UUID, RelativePosition> relativePositions = Map.ofEntries(
      Map.entry(SignalDetectionTestFixtures.channelID, SignalDetectionTestFixtures.relativePosition)
  );

  // Beam Definition Test Fixtures
  private static final double azimuth = 37.5;
  private static final double slowness = 17.2;
  private static final double nominalSampleRate = 40.0;
  private static final double sampleRateTolerance = 2.0;

  private static boolean coherent = true;
  private static boolean snappedSampling = true;
  private static boolean twoDimensional = true;

  public static final BeamDefinition BEAM_DEFINITION = BeamDefinition
      .builder()
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
      .setMinimumWaveformsForBeam(1)
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
      .setSlowStartXSecPerKm(eastSlowStart)
      .setSlowDeltaXSecPerKm(eastSlowDelta)
      .setSlowCountX(eastSlowCount)
      .setSlowStartYSecPerKm(northSlowStart)
      .setSlowDeltaYSecPerKm(northSlowDelta)
      .setSlowCountY(northSlowCount)
      .setWaveformSampleRateHz(waveformSampleRateHz)
      .setWaveformSampleRateToleranceHz(waveformSampleRateToleranceHz)
      .setBeamPoint(location)
      .setRelativePositionsByChannelId(relativePositions)
      .setMinimumWaveformsForSpectra(2)
      .build();

  public static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  public static final UUID PROCESSING_GROUP_ID = UUID
      .fromString("31111111-1111-1111-1111-111111111111");
  public static final UUID CHANNEL_ID = UUID
      .fromString("32211111-1111-1111-1111-111111111111");
  public static final Instant CREATION_TIME = Instant.ofEpochSecond(7070);
  public static final Instant BEAM_START_TIME = Instant.ofEpochSecond(700000);
  public static final Instant BEAM_END_TIME = Instant.ofEpochSecond(700060);
  public static final Set<UUID> USED_INPUT_CHANNEL_IDS = new HashSet<>(Arrays.asList(
      UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));

  public static final BeamCreationInfo BEAM_CREATION_INFO = BeamCreationInfo.builder()
      .setId(ID)
      .setCreationTime(CREATION_TIME)
      .setName(ID.toString())
      .setProcessingGroupId(PROCESSING_GROUP_ID)
      .setChannelId(CHANNEL_ID)
      .setChannelSegmentId(CHANNEL_SEGMENT_ID)
      .setRequestedStartTime(BEAM_START_TIME)
      .setRequestedEndTime(BEAM_END_TIME)
      .setBeamDefinition(BEAM_DEFINITION)
      .setUsedInputChannelIds(USED_INPUT_CHANNEL_IDS)
      .build();

  // Create a Site
  public static final UUID siteID = UUID.fromString("ab7c377a-b6a4-478f-b3cd-5c934ee6b879");
  public static final String siteName = "SITE01";
  public static final Set<Channel> channels = Set.of(channel);
  public static final Site site = Site.from(siteID, siteName, lat, lon, elev, channels);

  // Create a Station
  public static final UUID stationID = UUID.fromString("ab7c377a-b6a4-478f-b3cd-5c934ee6b879");
  public static final String stationName = "STATION01";
  public static final StationType stationType = StationType.SeismicArray;
  public static final Set<Site> sites = Set.of(site);
  public static final Station station = Station
          .from(stationID, stationName, description, stationType, lat, lon, elev, sites);  public static final Set<Station> stations = Set.of(station);


  // Create Network
  public static final UUID networkID = UUID.fromString("407c377a-b6a4-478f-b3cd-5c934ee6b876");
  public static final String networkName = "Net01";
  public static final Network network = Network.from(networkID, networkName,
      NetworkOrganization.CTBTO, NetworkRegion.GLOBAL, stations);

  // ------- Event -------

  // ------- EventHypothesis -------

  // ------- SignalDetectionEventAssociation -------
  private final UUID signalDetectionEventAssociationId = UUID
      .fromString("407c377a-b6a4-478f-b3cd-5c934ee6b876");
  private final UUID eventHypothesisId = UUID.fromString("5432a77a-b6a4-478f-b3cd-5c934ee6b000");
  private final UUID signalDetectionHypothesisId = UUID
      .fromString("cccaa77a-b6a4-478f-b3cd-5c934ee6b999");
  private final boolean isRejected = false;

  // Everything below will be removed later.

  // ProcessingCalibration
  public static final UUID PROCESSING_CALIBRATION_1_ID = UUID
      .fromString("ce7c377a-b6a4-478f-b3bd-5c934ee6b7ef");

  public static final Calibration processingCalibration1 =
      Calibration.from(PROCESSING_CALIBRATION_1_ID, 1.4, 1.0, 1.1, 1.1);

  public static final UUID PROCESSING_CALIBRATION_2_ID = UUID
      .fromString("cb3bffe0-553e-4398-b2a7-4026699ae9f2");

  public static final Calibration processingCalibration2 =
      Calibration.from(PROCESSING_CALIBRATION_2_ID, 2.4, 2.0, 2.1, 2.1);

  public static final Channel processingChannel01 = Channel.from(
      PROCESSING_CHANNEL_1_ID, "CHN01", ChannelType.BROADBAND_HIGH_GAIN_VERTICAL,
      ChannelDataType.HYDROACOUSTIC_ARRAY, 12.34, 45.67, 1001.0, 123.0, 180.0, 90.0, 40.0);

  public static final UUID PROCESSING_CHANNEL_2_ID = UUID
      .fromString("2bc8381f-8443-443a-83c8-cbbbe29ed796");

  public static final Channel processingChannel02 = Channel.from(
      PROCESSING_CHANNEL_2_ID, "CHN02", ChannelType.BROADBAND_HIGH_GAIN_VERTICAL,
      ChannelDataType.HYDROACOUSTIC_ARRAY, 12.34, 45.67, 1001.0,
      123.0, 180.0, 90.0, 40.0);

  // ProcessingSite
  public static final Set<Channel> processingChannels
      = Set.of(processingChannel01, processingChannel02);

  public static final UUID PROCESSING_SITE_ID = UUID
      .fromString("36c1ba53-b124-4286-9c2c-72647e749e32");

  public static final Site processingSite = Site.from(PROCESSING_SITE_ID,
      "SITE33", 12.34, 56.78, 78.90,
      processingChannels);

  public static final ChannelProcessingGroup channelProcessingGroup =
      ChannelProcessingGroup.create(
          ChannelProcessingGroupType.BEAM,
          new HashSet<>(List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())),
          Instant.now().minusSeconds(5),
          Instant.now().minusSeconds(30),
          "Status",
          "Comment");


  public static final DoubleValue standardDoubleValue = DoubleValue.from(5, 1, Units.SECONDS);
  public static final InstantValue arrivalTimeMeasurement = InstantValue.from(
      Instant.EPOCH, Duration.ofMillis(1));
  public static final PhaseTypeMeasurementValue phaseMeasurement = PhaseTypeMeasurementValue.from(
      PhaseType.P, 0.5);
  public static final FirstMotionMeasurementValue firstMotionMeasurement = FirstMotionMeasurementValue.from(
      FirstMotionType.UP, 0.5);
  public static final AmplitudeMeasurementValue amplitudeMeasurement = AmplitudeMeasurementValue.from(
      Instant.EPOCH, Duration.ofMillis(1), standardDoubleValue);
  public static final InstantValue instantMeasurement = InstantValue.from(
      Instant.EPOCH, Duration.ofMillis(1));

  public static final FeatureMeasurement<InstantValue> arrivalTimeFeatureMeasurement
      = FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.ARRIVAL_TIME, arrivalTimeMeasurement);
  public static final FeatureMeasurement<PhaseTypeMeasurementValue> phaseFeatureMeasurement
      = FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.PHASE, phaseMeasurement);
  public static final FeatureMeasurement<FirstMotionMeasurementValue> firstMotionFeatureMeasurement
      = FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.FIRST_MOTION, firstMotionMeasurement);
  public static final FeatureMeasurement<AmplitudeMeasurementValue> amplitudeFeatureMeasurement
      = FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2, amplitudeMeasurement);
  public static final FeatureMeasurement<InstantValue> instantFeatureMeasurement
      = FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.ARRIVAL_TIME, instantMeasurement);
}
