package gms.shared.mechanisms.objectstoragedistribution.coi.event;


import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipse;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipsoid;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionComponent;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrectionType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationUncertainty;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class EventTestFixtures {

  public static final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  public static double lat = 23.9;
  public static double lon = -89.0;
  public static double depth = 0.06;
  public static double zeroDepth = 0.0;
  public static Instant time = Instant.EPOCH;

  public static double residual = 2.1;
  public static double weight = 0.87;
  public static boolean isDefining = false;

  private static final DoubleValue standardDoubleValue = DoubleValue.from(5, 1, Units.SECONDS);
  private static final InstantValue arrivalTimeMeasurement = InstantValue.from(
      time, Duration.ofMillis(1));
  private static final PhaseTypeMeasurementValue phaseMeasurement = PhaseTypeMeasurementValue.from(
      PhaseType.P, 0.5);

  public static FeatureMeasurement<InstantValue> arrivalTimeFeatureMeasurement
      = FeatureMeasurement
      .create(UUID.randomUUID(), FeatureMeasurementTypes.ARRIVAL_TIME, arrivalTimeMeasurement);
  public static final FeatureMeasurement<PhaseTypeMeasurementValue> phaseFeatureMeasurement
      = FeatureMeasurement
      .create(UUID.randomUUID(), FeatureMeasurementTypes.PHASE, phaseMeasurement);

  public static final int arrayLen = 1;

  //Create a LocationUncertainty with dummy values.
  public static final double xx = 0.0;
  public static final double xy = 0.0;
  public static final double xz = 0.0;
  public static final double xt = 0.0;
  public static final double yy = 0.0;
  public static final double yz = 0.0;
  public static final double yt = 0.0;
  public static final double zz = 0.0;
  public static final double zt = 0.0;
  public static final double tt = 0.0;
  public static final double stDevOneObservation = 0.0;

  //Create an Ellipse.
  public static final ScalingFactorType scalingFactorType = ScalingFactorType.CONFIDENCE;
  public static final ScalingFactorType scalingFactorType2 = ScalingFactorType.COVERAGE;
  public static final double kWeight = 0.0;
  public static final double confidenceLevel = 0.5;
  public static final double majorAxisLength = 0.0;
  public static final double majorAxisTrend = 0.0;
  public static final double majorAxisPlunge = 0.0;
  public static final double intermediateAxisLength = 0.0;
  public static final double intermediateAxisTrend = 0.0;
  public static final double intermediateAxisPlunge = 0.0;
  public static final double minorAxisLength = 0.0;
  public static final double minorAxisTrend = 0.0;
  public static final double minorAxisPlunge = 0.0;
  public static final double depthUncertainty = 0.0;
  public static final Duration timeUncertainty = Duration.ofSeconds(5);

  public static final Ellipse ellipse = Ellipse
      .from(scalingFactorType, kWeight, confidenceLevel, majorAxisLength, majorAxisTrend,
          minorAxisLength, minorAxisTrend, depthUncertainty, timeUncertainty);
  public static final Ellipsoid ellipsoid = Ellipsoid
      .from(scalingFactorType, kWeight, confidenceLevel,
          majorAxisLength, majorAxisTrend, majorAxisPlunge,
          intermediateAxisLength, intermediateAxisTrend,
          intermediateAxisPlunge, minorAxisLength, intermediateAxisTrend,
          intermediateAxisPlunge, timeUncertainty);
  public static final Set<Ellipse> ellipseSet = Set.of(ellipse);
  public static final Set<Ellipsoid> ellipsoidSet = Set.of(ellipsoid);

  public static final LocationUncertainty locationUncertainty = LocationUncertainty
      .from(xx, xy, xz, xt, yy, yz, yt, zz, zt, tt, stDevOneObservation,
          ellipseSet, ellipsoidSet);

  public static final LocationRestraint locationRestraint = LocationRestraint.from(
      RestraintType.FIXED,
      lat,
      RestraintType.FIXED,
      lon,
      DepthRestraintType.FIXED_AT_SURFACE,
      zeroDepth,
      RestraintType.FIXED,
      time
  );

  public static final EventLocation location = EventLocation.from(lat, lon, depth, time);

  public static final LocationBehavior locationBehavior = LocationBehavior
      .from(residual, weight, isDefining, UUID.randomUUID(), arrivalTimeFeatureMeasurement.getId());

  public static Set<LocationBehavior> locationBehaviors = Set.of(locationBehavior);

  public static final FeaturePrediction<NumericMeasurementValue> featurePrediction = FeaturePrediction
      .create(
          PhaseType.P,
          Optional.of(
              NumericMeasurementValue.from(
                  Instant.EPOCH,
                  DoubleValue.from(1.0, 2.0, Units.SECONDS))
          ),
          Set.of(FeaturePredictionComponent.from(DoubleValue.from(3.0, 4.0, Units.SECONDS),
              false,
              FeaturePredictionCorrectionType.BASELINE_PREDICTION)),
          false,
          FeatureMeasurementTypes.SLOWNESS, location,
          Location.from(1.0, 2.0, 4.0, 3.0),
          Optional.of(UUID.randomUUID()), Map.of());

  public static Set<FeaturePrediction<?>> featurePredictions = Set.of(featurePrediction);

  // Create a LocationSolution
  public static final LocationSolution locationSolution = LocationSolution
      .create(location, locationRestraint,
          locationUncertainty, locationBehaviors, featurePredictions);

  // Create an Event
  public static final Event event = Event.create(
      Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID()), Set.of(locationSolution),
      PreferredLocationSolution.from(locationSolution),
      "monitoringOrg", UUID.randomUUID());

  static {
    // mark event as final
    event.markFinal(event.getHypotheses().iterator().next());
  }

  // ------- SignalDetectionEventAssociation -------

  public static final UUID signalDetectionEventAssociationId = UUID.randomUUID();
  public static final UUID eventHypothesisId = UUID.randomUUID();
  public static final UUID signalDetectionHypothesisId = UUID.randomUUID();
  public static final boolean isRejected = false;

  public static final SignalDetectionEventAssociation signalDetectionEventAssociation = SignalDetectionEventAssociation
      .from(signalDetectionEventAssociationId, eventHypothesisId, signalDetectionHypothesisId,
          isRejected);
}
