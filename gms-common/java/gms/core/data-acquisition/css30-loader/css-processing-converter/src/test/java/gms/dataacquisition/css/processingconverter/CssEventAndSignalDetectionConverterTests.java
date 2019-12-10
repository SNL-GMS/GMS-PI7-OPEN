package gms.dataacquisition.css.processingconverter;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipse;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationUncertainty;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.AmplitudeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.utilities.standardtestdataset.ReferenceStationFileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class CssEventAndSignalDetectionConverterTests {

  private static final ReferenceStationFileReader stationReader = mock(
      ReferenceStationFileReader.class);
  private static final Map<Integer, Integer> aridToWfid = mock(Map.class);
  private static CssEventAndSignalDetectionConverter converter;
  private static final double TOLERANCE = 0.000000000001;
  private static final UUID ARCES_ID = UUID.randomUUID();
  private static final Integer WFID = 12345;
  private static final UUID EXPECTED_SEGMENT_ID
      = UUID.nameUUIDFromBytes(String.valueOf(WFID).getBytes());

  @BeforeClass
  public static void setup() throws Exception {
    // mock the station reader
    when(stationReader.findStationIdByNameAndTime(anyString(), any(Instant.class)))
        .thenReturn(Optional.of(ARCES_ID));
    when(aridToWfid.get(anyInt())).thenReturn(WFID);
    final String basePath = "src/test/resources/processingfiles/ueb_test.";
    converter = new CssEventAndSignalDetectionConverter(
        basePath + "event", basePath + "origin", basePath + "origerr",
        basePath + "arrival", basePath + "assoc", basePath + "amplitude",
        stationReader, aridToWfid);
  }

  @Test
  public void testEventConversion() {
    //Check overall Event's are of expected size
    final Collection<Event> events = converter.getEvents();
    assertNotNull(events);
    assertEquals(118, events.size());

    //Check particular Event
    final UUID expectedOrid = UUID.nameUUIDFromBytes("48836076".getBytes());
    final List<Event> eventsWithOrid48836076 = events.stream()
        .filter(x -> x.getId().equals(expectedOrid))
        .collect(Collectors.toList());
    assertEquals(1, eventsWithOrid48836076.size());
    final Event event = eventsWithOrid48836076.get(0);
    assertEquals(expectedOrid, event.getId());
    assertEquals(0, event.getRejectedSignalDetectionAssociations().size());
    assertEquals(CssEventAndSignalDetectionConverter.MONITORING_ORG,
        event.getMonitoringOrganization());
    assertEquals(1, event.getHypotheses().size());
    assertEquals(1, event.getFinalEventHypothesisHistory().size());
    assertEquals(1, event.getPreferredEventHypothesisHistory().size());
    assertFalse(event.isRejected());

    //Check Event Hypothesis 
    final EventHypothesis eventHypothesis = event.getHypotheses().iterator().next();
    assertEquals(expectedOrid, eventHypothesis.getEventId());
    assertFalse(eventHypothesis.isRejected());
    assertEquals(1, eventHypothesis.getLocationSolutions().size());
    assertEquals(event.getId(), eventHypothesis.getEventId());
    assertTrue(eventHypothesis.getParentEventHypotheses().isEmpty());
    assertFalse(eventHypothesis.isRejected());
    assertEquals(event.getId(), eventHypothesis.getId());

    //Check Location Solution
    final LocationSolution locationSolution = eventHypothesis.getLocationSolutions()
        .iterator().next();
    assertEquals(event.getId(), locationSolution.getId());

    //Check Event Location
    final EventLocation location = locationSolution.getLocation();
    assertEquals(67.5141, location.getLatitudeDegrees(), TOLERANCE);
    assertEquals(32.8323, location.getLongitudeDegrees(), TOLERANCE);
    assertEquals(0.0, location.getDepthKm(), TOLERANCE);
    assertEquals(Instant.ofEpochSecond(1274387032).plusNanos(270000000), location.getTime());

    //Check Location Restraint
    final LocationRestraint locationRestraint = locationSolution.getLocationRestraint();
    assertEquals(DepthRestraintType.FIXED_AT_SURFACE, locationRestraint.getDepthRestraintType());
    assertFalse(locationRestraint.getDepthRestraintKm().isPresent());
    assertEquals(RestraintType.UNRESTRAINED, locationRestraint.getLatitudeRestraintType());
    assertEquals(RestraintType.UNRESTRAINED, locationRestraint.getLongitudeRestraintType());
    assertFalse(locationRestraint.getLatitudeRestraintDegrees().isPresent());
    assertFalse(locationRestraint.getLongitudeRestraintDegrees().isPresent());
    assertFalse(locationRestraint.getTimeRestraint().isPresent());

    //Check Location Uncertainty
    assertTrue(locationSolution.getLocationUncertainty().isPresent());
    final LocationUncertainty locationUncertainty = locationSolution.getLocationUncertainty().get();
    assertEquals(334.8672, locationUncertainty.getXx(), TOLERANCE);
    assertEquals(220.0772, locationUncertainty.getXy(), TOLERANCE);
    assertEquals(-1.0, locationUncertainty.getXz(), TOLERANCE);
    assertEquals(-19.3451, locationUncertainty.getXt(), TOLERANCE);
    assertEquals(407.1720, locationUncertainty.getYy(), TOLERANCE);
    assertEquals(-1, locationUncertainty.getXz(), TOLERANCE);
    assertEquals(16.4265, locationUncertainty.getYt(), TOLERANCE);
    assertEquals(-1, locationUncertainty.getZz(), TOLERANCE);
    assertEquals(-1, locationUncertainty.getZt(), TOLERANCE);
    assertEquals(4.8457, locationUncertainty.getTt(), TOLERANCE);
    assertEquals(1.1833, locationUncertainty.getStDevOneObservation(), TOLERANCE);

    //Test Ellipsoids (do't get any dta from css so its empty)
    assertTrue(locationUncertainty.getEllipsoids().isEmpty());

    //Test Ellipse
    final Ellipse ellipse = locationUncertainty.getEllipses().iterator().next();
    assertEquals(ScalingFactorType.CONFIDENCE, ellipse.getScalingFactorType());
    assertEquals(0.0, ellipse.getkWeight(), TOLERANCE);
    assertEquals(0.9, ellipse.getConfidenceLevel(), TOLERANCE);
    assertEquals(52.2742, ellipse.getMajorAxisLength(), TOLERANCE);
    assertEquals(40.34, ellipse.getMajorAxisTrend(), TOLERANCE);
    assertEquals(26.0914, ellipse.getMinorAxisLength(), TOLERANCE);
    assertEquals(-1.0, ellipse.getMinorAxisTrend(), TOLERANCE);
    assertEquals(-1, ellipse.getDepthUncertainty(), TOLERANCE);
    assertEquals(Duration.ofNanos((long) (3.624 * 1e9)), ellipse.getTimeUncertainty());

    //Test Feature Prediction, we don't get this data from css so
    assertEquals(0, locationSolution.getFeaturePredictions().size());

    //Check Preferred Location Solution
    assertTrue(eventHypothesis.getPreferredLocationSolution().isPresent());
    assertEquals(PreferredLocationSolution.from(locationSolution),
        eventHypothesis.getPreferredLocationSolution().get());

    //Check Signal Detection Association
    final Set<SignalDetectionEventAssociation> signalDetectionEventAssociations =
        eventHypothesis.getAssociations();
    assertEquals(4, signalDetectionEventAssociations.size());
    final UUID expectedArid = UUID.nameUUIDFromBytes("59210196".getBytes());
    final Set<SignalDetectionEventAssociation> filteredAssociations = signalDetectionEventAssociations
        .stream().filter(x -> x.getSignalDetectionHypothesisId().equals(expectedArid))
        .collect(Collectors.toSet());
    assertEquals(1, filteredAssociations.size());
    final SignalDetectionEventAssociation associationArid59210196 = filteredAssociations.iterator()
        .next();
    assertEquals(event.getId(), associationArid59210196.getEventHypothesisId());
    assertEquals(expectedArid, associationArid59210196.getSignalDetectionHypothesisId());
    assertFalse(associationArid59210196.isRejected());

    final Collection<SignalDetection> detections = converter.getSignalDetections();
    assertNotNull(detections);
    assertEquals(483, detections.size());

    //Check particular Signal Detection
    final List<SignalDetection> detectionsWithArid59210196 = detections.stream()
        .filter(x -> x.getId().equals(expectedArid))
        .collect(Collectors.toList());
    assertEquals(1, detectionsWithArid59210196.size());
    final SignalDetection signalDetection = detectionsWithArid59210196.get(0);
    assertEquals(expectedArid, signalDetection.getId());

    // creation info ID, monitoringOrg match (see public constants in converter)
    final UUID expectedCreationInfoId = CssEventAndSignalDetectionConverter.UNKNOWN_ID;
    assertEquals(expectedCreationInfoId, signalDetection.getCreationInfoId());
    assertEquals(CssEventAndSignalDetectionConverter.MONITORING_ORG,
        signalDetection.getMonitoringOrganization());
    assertEquals(ARCES_ID, signalDetection.getStationId());

    //test signal detection hypothesis
    assertEquals(1, signalDetection.getSignalDetectionHypotheses().size());
    SignalDetectionHypothesis signalDetectionHypothesis = signalDetection
        .getSignalDetectionHypotheses().get(0);
    assertEquals(expectedArid, signalDetectionHypothesis.getParentSignalDetectionId());
    assertFalse(signalDetectionHypothesis.isRejected());
    assertEquals(expectedCreationInfoId, signalDetectionHypothesis.getCreationInfoId());
    assertEquals(signalDetection.getId(), signalDetectionHypothesis.getParentSignalDetectionId());

    // Check basic properties of all feature measurements
    signalDetectionHypothesis.getFeatureMeasurements().forEach(fm -> checkFeatureMeasurement(fm));
    // Check Phase measurement
    final FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement = assertPhaseMeasurementPresentAndReturn(
        signalDetectionHypothesis);
    final PhaseTypeMeasurementValue phaseValue = phaseMeasurement.getMeasurementValue();
    assertEquals(PhaseType.Lg, phaseValue.getValue());
    assertEquals(1.0, phaseValue.getConfidence(), TOLERANCE);

    // Check arrival time measurement
    final Instant expectedArrivalTime = Instant.parse("2010-05-20T20:25:37.450Z");
    final FeatureMeasurement<InstantValue> arrivalTimeMeasurement = assertArrivalTimePresentAndReturn(
        signalDetectionHypothesis, expectedArrivalTime);
    final InstantValue arrivalTime = arrivalTimeMeasurement.getMeasurementValue();
    assertEquals(expectedArrivalTime, arrivalTime.getValue());
    assertEquals((int) (0.412 * 1e9), arrivalTime.getStandardDeviation().getNano());

    // Check azimuth measurement
    final FeatureMeasurement<NumericMeasurementValue> azimuthMeasurement = assertNumericMeasurementPresentAndReturn(
        signalDetectionHypothesis, FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
        expectedArrivalTime);
    final NumericMeasurementValue azimuth = azimuthMeasurement.getMeasurementValue();
    assertEquals(125.69, azimuth.getMeasurementValue().getValue(), TOLERANCE);
    assertEquals(2.60, azimuth.getMeasurementValue().getStandardDeviation(), TOLERANCE);
    assertEquals(Units.DEGREES, azimuth.getMeasurementValue().getUnits());

    // Check slowness measurement
    final FeatureMeasurement<NumericMeasurementValue> slownessMeasurement = assertNumericMeasurementPresentAndReturn(
        signalDetectionHypothesis, FeatureMeasurementTypes.SLOWNESS, expectedArrivalTime);
    final NumericMeasurementValue slowness = slownessMeasurement.getMeasurementValue();
    assertEquals(27.03, slowness.getMeasurementValue().getValue(), TOLERANCE);
    assertEquals(1.23, slowness.getMeasurementValue().getStandardDeviation(), TOLERANCE);
    assertEquals(Units.SECONDS_PER_DEGREE, slowness.getMeasurementValue().getUnits());

    // Check emergence angle measurement
    final FeatureMeasurement<NumericMeasurementValue> emergenceMeasurement = assertNumericMeasurementPresentAndReturn(
        signalDetectionHypothesis, FeatureMeasurementTypes.EMERGENCE_ANGLE, expectedArrivalTime);
    final NumericMeasurementValue emergence = emergenceMeasurement.getMeasurementValue();
    assertEquals(78.74, emergence.getMeasurementValue().getValue(), TOLERANCE);
    assertEquals(0.0, emergence.getMeasurementValue().getStandardDeviation(), TOLERANCE);
    assertEquals(Units.DEGREES, emergence.getMeasurementValue().getUnits());

    // Check rectilinearity measurement
    final FeatureMeasurement<NumericMeasurementValue> rectilinearityMeasurement = assertNumericMeasurementPresentAndReturn(
        signalDetectionHypothesis, FeatureMeasurementTypes.RECTILINEARITY, expectedArrivalTime);
    final NumericMeasurementValue rect = rectilinearityMeasurement.getMeasurementValue();
    assertEquals(0.679, rect.getMeasurementValue().getValue(), TOLERANCE);
    assertEquals(0.0, rect.getMeasurementValue().getStandardDeviation(), TOLERANCE);
    assertEquals(Units.UNITLESS, rect.getMeasurementValue().getUnits());

    // Check amplitude measurements
    final FeatureMeasurement<AmplitudeMeasurementValue> amplitudeMeasurement_a5_over_2 =
        assertAmplitudeMeasurementPresentAndReturn(signalDetectionHypothesis,
            FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2, Instant.ofEpochMilli((long) (1000L * 1274387141.925)));
    final AmplitudeMeasurementValue amplitude1 = amplitudeMeasurement_a5_over_2.getMeasurementValue();
    assertEquals(0.65, amplitude1.getAmplitude().getValue(), TOLERANCE);
    assertEquals(0.0, amplitude1.getAmplitude().getStandardDeviation(), TOLERANCE);
    assertEquals(Units.UNITLESS, amplitude1.getAmplitude().getUnits());
    assertEquals(Duration.ofNanos((long) (0.28 * 1e9)), amplitude1.getPeriod());
    final FeatureMeasurement<AmplitudeMeasurementValue> amplitudeMeasurement_alr_over_2 =
        assertAmplitudeMeasurementPresentAndReturn(signalDetectionHypothesis,
            FeatureMeasurementTypes.AMPLITUDE_ALR_OVER_2, Instant.ofEpochMilli((long) (1000L * 1274387137.45)));
    final AmplitudeMeasurementValue amplitude2 = amplitudeMeasurement_alr_over_2.getMeasurementValue();
    assertEquals(2.45, amplitude2.getAmplitude().getValue(), TOLERANCE);
    assertEquals(0.0, amplitude2.getAmplitude().getStandardDeviation(), TOLERANCE);
    assertEquals(Units.UNITLESS, amplitude2.getAmplitude().getUnits());
    assertEquals(Duration.ofSeconds(-1), amplitude2.getPeriod());

    // Check period measurement
    final FeatureMeasurement<NumericMeasurementValue> periodMeasurement = assertNumericMeasurementPresentAndReturn(
        signalDetectionHypothesis, FeatureMeasurementTypes.PERIOD, expectedArrivalTime);
    final NumericMeasurementValue period = periodMeasurement.getMeasurementValue();
    assertEquals(0.33, period.getMeasurementValue().getValue(), TOLERANCE);
    assertEquals(0.0, period.getMeasurementValue().getStandardDeviation(), TOLERANCE);
    assertEquals(Units.SECONDS, period.getMeasurementValue().getUnits());

    // Check snr measurement
    final FeatureMeasurement<NumericMeasurementValue> snrMeasurement = assertNumericMeasurementPresentAndReturn(
        signalDetectionHypothesis, FeatureMeasurementTypes.SNR, expectedArrivalTime);
    final NumericMeasurementValue snr = snrMeasurement.getMeasurementValue();
    assertEquals(10.25, snr.getMeasurementValue().getValue(), TOLERANCE);
    assertEquals(0.0, snr.getMeasurementValue().getStandardDeviation(), TOLERANCE);
    assertEquals(Units.UNITLESS, snr.getMeasurementValue().getUnits());

    //Check Final Event Hypothesis
    assertTrue(event.getFinal().isPresent());
    assertEquals(eventHypothesis,
        event.getFinalEventHypothesisHistory().get(0).getEventHypothesis());

    //Check Preferred
    assertEquals(1, event.getPreferredEventHypothesisHistory().size());
    assertEquals(eventHypothesis,
        event.getPreferredEventHypothesisHistory().get(0).getEventHypothesis());

    //Test Location Behaviors
    final Set<LocationBehavior> behaviors = locationSolution.getLocationBehaviors();
    // 4 assocs making 3 location behaviors each = 12 location behaviors
    assertEquals(12, behaviors.size());

    LocationBehavior timeLocationBehavior = assertLocationBehaviorAndReturn(locationSolution,
        arrivalTimeMeasurement.getId());
    assertEquals(-0.762, timeLocationBehavior.getResidual(), TOLERANCE);
    assertEquals(0.55, timeLocationBehavior.getWeight(), TOLERANCE);

    LocationBehavior azimuthLocationBehavior = assertLocationBehaviorAndReturn(locationSolution,
        azimuthMeasurement.getId());
    assertEquals(2.2, azimuthLocationBehavior.getResidual(), TOLERANCE);
    assertEquals(0.55, azimuthLocationBehavior.getWeight(), TOLERANCE);

    LocationBehavior slownessLocationBehavior = assertLocationBehaviorAndReturn(locationSolution,
        slownessMeasurement.getId());
    assertEquals(-4.74, slownessLocationBehavior.getResidual(), TOLERANCE);
    assertEquals(0.55, slownessLocationBehavior.getWeight(), TOLERANCE);

  }

  private static FeatureMeasurement<PhaseTypeMeasurementValue> assertPhaseMeasurementPresentAndReturn(
      SignalDetectionHypothesis hyp) {

    Optional<FeatureMeasurement<PhaseTypeMeasurementValue>> phaseMeasurement
        = hyp.getFeatureMeasurement(FeatureMeasurementTypes.PHASE);
    assertNotNull(phaseMeasurement);
    assertTrue(phaseMeasurement.isPresent());
    return phaseMeasurement.get();
  }

  private static FeatureMeasurement<AmplitudeMeasurementValue> assertAmplitudeMeasurementPresentAndReturn(
      SignalDetectionHypothesis hyp, FeatureMeasurementType<AmplitudeMeasurementValue> fmType,
      Instant expectedTime) {

    Optional<FeatureMeasurement<AmplitudeMeasurementValue>> measurement = hyp
        .getFeatureMeasurement(fmType);
    assertNotNull(measurement);
    assertTrue(measurement.isPresent());
    FeatureMeasurement<AmplitudeMeasurementValue> amplitudeMeasurement = measurement.get();
    assertEquals(expectedTime, amplitudeMeasurement.getMeasurementValue().getStartTime());
    return amplitudeMeasurement;
  }

  private static FeatureMeasurement<NumericMeasurementValue> assertNumericMeasurementPresentAndReturn(
      SignalDetectionHypothesis hyp, FeatureMeasurementType<NumericMeasurementValue> fmType,
      Instant expectedTime) {

    Optional<FeatureMeasurement<NumericMeasurementValue>> measurement = hyp
        .getFeatureMeasurement(fmType);
    assertNotNull(measurement);
    assertTrue(measurement.isPresent());
    FeatureMeasurement<NumericMeasurementValue> numericalMeasurement = measurement.get();
    assertEquals(expectedTime, numericalMeasurement.getMeasurementValue().getReferenceTime());
    return numericalMeasurement;
  }

  private static FeatureMeasurement<InstantValue> assertArrivalTimePresentAndReturn(
      SignalDetectionHypothesis hyp, Instant expectedTime) {

    Optional<FeatureMeasurement<InstantValue>> measurement = hyp.getFeatureMeasurement(
        FeatureMeasurementTypes.ARRIVAL_TIME);
    assertNotNull(measurement);
    assertTrue(measurement.isPresent());
    FeatureMeasurement<InstantValue> instantMeasurement = measurement.get();
    assertEquals(expectedTime, instantMeasurement.getMeasurementValue().getValue());
    return instantMeasurement;
  }

  private static void checkFeatureMeasurement(FeatureMeasurement<?> fm) {
    assertEquals(EXPECTED_SEGMENT_ID, fm.getChannelSegmentId());
  }

  private static LocationBehavior assertLocationBehaviorAndReturn(LocationSolution solution,
      UUID featureMeasurementId) {
    final Optional<LocationBehavior> locationBehaviorOptional = solution.getLocationBehaviors()
        .stream()
        .filter(lb -> lb.getFeatureMeasurementId().equals(featureMeasurementId))
        .findAny();
    assertNotNull(locationBehaviorOptional);
    assertTrue(locationBehaviorOptional.isPresent());
    LocationBehavior locationBehavior = locationBehaviorOptional.get();
    assertEquals(CssEventAndSignalDetectionConverter.UNKNOWN_ID,
        locationBehavior.getFeaturePredictionId());
    return locationBehavior;
  }
}
