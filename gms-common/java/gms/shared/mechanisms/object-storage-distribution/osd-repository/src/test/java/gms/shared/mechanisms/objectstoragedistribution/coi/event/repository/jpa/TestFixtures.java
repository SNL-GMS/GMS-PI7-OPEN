package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipse;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipsoid;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionComponent;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrectionType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FinalEventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationUncertainty;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredEventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class          TestFixtures {

  private static final double lat = 23.9;
  private static final double lon = -89.0;
  private static final double depth = 0.06;
  private static final double zeroDepth = 0.0;
  public static final Instant time = Instant.EPOCH.plusSeconds(60);

  private static final double residual = 2.1;
  private static final double weight = 0.87;
  private static final boolean isDefining = false;
  public static final FeatureMeasurement<InstantValue> arrivalTimeMeasurement = FeatureMeasurement
      .create(UUID.randomUUID(), FeatureMeasurementTypes.ARRIVAL_TIME,
          InstantValue.from(time, Duration.ofMillis(5)));
  public static final FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement = FeatureMeasurement
      .create(UUID.randomUUID(), FeatureMeasurementTypes.PHASE,
          PhaseTypeMeasurementValue.from(PhaseType.P, 0.5));

  //Create an Ellipse.
  private static final ScalingFactorType scalingFactorType = ScalingFactorType.CONFIDENCE;
  private static final ScalingFactorType scalingFactorType2 = ScalingFactorType.COVERAGE;
  private static final double kWeight = 0.0;
  private static final double confidenceLevel = 0.5;
  private static final double majorAxisLength = 0.0;
  private static final double majorAxisTrend = 0.0;
  private static final double majorAxisPlunge = 0.0;
  private static final double intermediateAxisLength = 0.0;
  private static final double intermediateAxisTrend = 0.0;
  private static final double intermediateAxisPlunge = 0.0;
  private static final double minorAxisLength = 0.0;
  private static final double minorAxisTrend = 0.0;
  private static final double minorAxisPlunge = 0.0;
  private static final double depthUncertainty = 0.0;
  private static final Duration timeUncertainty = Duration.ofSeconds(5);

  private static final Ellipse ellipse = Ellipse
      .from(scalingFactorType, kWeight, confidenceLevel, majorAxisLength, majorAxisTrend,
          minorAxisLength, minorAxisTrend, depthUncertainty, timeUncertainty);
  private static final Ellipsoid ellipsoid = Ellipsoid
      .from(scalingFactorType, kWeight, confidenceLevel,
          majorAxisLength, majorAxisTrend, majorAxisPlunge,
          intermediateAxisLength, intermediateAxisTrend,
          intermediateAxisPlunge, minorAxisLength, intermediateAxisTrend,
          intermediateAxisPlunge, timeUncertainty);

  private static final LocationUncertainty locationUncertainty = LocationUncertainty
      .from(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
          0.0, 0.0, 0.0, Set.of(ellipse), Set.of(ellipsoid));

  private static final LocationRestraint locationRestraint = LocationRestraint.from(
      RestraintType.FIXED,
      lat,
      RestraintType.FIXED,
      lon,
      DepthRestraintType.FIXED_AT_SURFACE,
      zeroDepth,
      RestraintType.FIXED,
      time);

  static final EventLocation location = EventLocation.from(
      1, 1, depth, time);
  static final EventLocation location2 = EventLocation.from(
      50, 50, depth, time.plusSeconds(60));
  private static final EventLocation location3 = EventLocation.from(
      25, 25, depth, time.plusSeconds(160));

  private static final LocationBehavior locationBehavior = LocationBehavior
      .from(residual, weight, isDefining, UUID.randomUUID(), UUID.randomUUID());

  private static final Set<LocationBehavior> locationBehaviors = Set.of(locationBehavior);

  private static final Set<FeaturePrediction<?>> featurePredictionsEmpty = Set.of();

  // Create FeaturePrediction objects.
  private static UUID fp1Uuid = UUID.randomUUID();
  private static UUID fp2Uuid = UUID.randomUUID();
  private static final FeaturePrediction<NumericMeasurementValue> featurePrediction1 = FeaturePrediction.from(
      fp1Uuid,
      PhaseType.P,
      Optional.of(NumericMeasurementValue.from(Instant.EPOCH,DoubleValue.from(1.0, 0.1, Units.SECONDS))),
      Set.of(
          FeaturePredictionComponent.from(
              DoubleValue.from(1.0, 0.1, Units.SECONDS),
              false,
              FeaturePredictionCorrectionType.BASELINE_PREDICTION),
          FeaturePredictionComponent.from(
              DoubleValue.from(11.0, 0.11, Units.SECONDS),
              false,
              FeaturePredictionCorrectionType.BASELINE_PREDICTION)),
      false,
      FeatureMeasurementTypes.SOURCE_TO_RECEIVER_DISTANCE,
      EventLocation.from(1.0, 1.0, 1.0, Instant.now()),
      Location.from(1.0, 1.0, 1.0, 1.0),
      Optional.of(UUID.randomUUID()));
  private static final FeaturePrediction<NumericMeasurementValue> featurePrediction2 = FeaturePrediction.from(
      fp2Uuid,
      PhaseType.P,
      Optional.of(NumericMeasurementValue.from(Instant.EPOCH, DoubleValue.from(2.0, 0.2, Units.SECONDS))),
      Set.of(
          FeaturePredictionComponent.from(
              DoubleValue.from(2.0, 0.2, Units.SECONDS),
              false,
              FeaturePredictionCorrectionType.BASELINE_PREDICTION),
          FeaturePredictionComponent.from(
              DoubleValue.from(22.0, 0.22, Units.SECONDS),
              false,
              FeaturePredictionCorrectionType.BASELINE_PREDICTION)),
      false,
      FeatureMeasurementTypes.SOURCE_TO_RECEIVER_DISTANCE,
      EventLocation.from(2.0, 2.0, 2.0, Instant.now()),
      Location.from(2.0, 2.0, 2.0, 2.0),
      Optional.of(UUID.randomUUID()));
  private static final FeaturePrediction<InstantValue> featurePrediction3 = FeaturePrediction.from(
      fp2Uuid,
      PhaseType.P,
      Optional.of(InstantValue.from(Instant.EPOCH, Duration.ZERO)),
      Set.of(
          FeaturePredictionComponent.from(
              DoubleValue.from(3.0, 0.3, Units.SECONDS),
              false,
              FeaturePredictionCorrectionType.BASELINE_PREDICTION),
          FeaturePredictionComponent.from(
              DoubleValue.from(33.0, 0.33, Units.SECONDS),
              false,
              FeaturePredictionCorrectionType.BASELINE_PREDICTION)),
      false,
      FeatureMeasurementTypes.ARRIVAL_TIME,
      EventLocation.from(3.0, 2.0, 2.0, Instant.now()),
      Location.from(3.0, 3.0, 3.0, 3.0),
      Optional.of(UUID.randomUUID()));

  private static final FeaturePrediction<NumericMeasurementValue> featurePredictionNoInstantValue = FeaturePrediction.from(
      UUID.randomUUID(),
      PhaseType.P,
      Optional.empty(),
      Set.of(),
      false,
      FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
      EventLocation.from(3.0, 2.0, 2.0, Instant.now()),
      Location.from(3.0, 3.0, 3.0, 3.0),
      Optional.of(UUID.randomUUID()));

  // Create a LocationSolution
  private static final LocationSolution locationSolution = LocationSolution
      .create(location, locationRestraint,
          locationUncertainty, locationBehaviors, featurePredictionsEmpty);
  private static final LocationSolution locationSolution2 = LocationSolution
      .create(location2, locationRestraint,
          locationUncertainty, locationBehaviors, featurePredictionsEmpty);
  static final LocationSolution locationSolution3 = LocationSolution
      .create(location3, locationRestraint,
          locationUncertainty, locationBehaviors, featurePredictionsEmpty);
  static final LocationSolution locationSolution4 = LocationSolution
      .create(location3, locationRestraint,
          locationUncertainty, locationBehaviors, featurePredictionsEmpty);
  static final LocationSolution locationSolution5 = LocationSolution
      .create(location3, locationRestraint,
          locationUncertainty, locationBehaviors, featurePredictionsEmpty);
  static final LocationSolution locationSolution6 = LocationSolution
      .create(location3, locationRestraint,
          locationUncertainty, locationBehaviors, featurePredictionsEmpty);
  static final LocationSolution locationSolution7 = LocationSolution
      .create(location3, locationRestraint,
          locationUncertainty, locationBehaviors, featurePredictionsEmpty);


  // Create some Event's
  static final Event event = Event.create(
      Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID()), Set.of(locationSolution),
      PreferredLocationSolution.from(locationSolution),
      "monitoringOrg", UUID.randomUUID());

  // TODO: give this different location solutions
  static final Event event2 = Event.create(
      Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID()), Set.of(locationSolution2),
      PreferredLocationSolution.from(locationSolution2),
      "monitoringOrg 2", UUID.randomUUID());

  static final Event event3 = Event.create(
      Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID()), Set.of(locationSolution3),
      PreferredLocationSolution.from(locationSolution3),
      "monitoringOrg 3", UUID.randomUUID());

  static final Event event4 = Event.create(
      Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID()), Set.of(locationSolution4),
      PreferredLocationSolution.from(locationSolution4),
      "monitoringOrg 4", UUID.randomUUID());

  static final Event event5 = Event.create(
      Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID()), Set.of(locationSolution5),
      PreferredLocationSolution.from(locationSolution5),
      "monitoringOrg 5", UUID.randomUUID());

  static final Event event6 = Event.create(
      Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID()), Set.of(locationSolution6),
      PreferredLocationSolution.from(locationSolution6),
      "monitoringOrg 6", UUID.randomUUID());


  static final Event event6a = Event.from(
      event6.getId(),
      Set.of(),
      "monitoringOrg 6oops",
      event6.getHypotheses(),
      List.of(),
      List.of(PreferredEventHypothesis
          .from(UUID.randomUUID(), event6.getHypotheses().iterator().next())));

  static final Event unstoredEvent = Event.create(
      Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID()), Set.of(locationSolution3),
      PreferredLocationSolution.from(locationSolution3),
      "monitoringOrg unstored", UUID.randomUUID());


  private static UUID locSolId = UUID.randomUUID();
  private static UUID eventId = UUID.randomUUID();
  private static UUID eventNoInstantValueId = UUID.randomUUID();
  private static UUID processingStageId = UUID.randomUUID();
  private static String monitoringOrg = "monitoringOrg 7";
  private static UUID ehId = UUID.randomUUID();
  private static UUID parentEhId = UUID.randomUUID();
  private static UUID parentEhNoInstantValueId = UUID.randomUUID();

  private static final LocationSolution locationSolutionWithFp1 = LocationSolution
      .from(UUID.randomUUID(), location3, locationRestraint, locationUncertainty, locationBehaviors,
          Set.of(featurePrediction1));
  private static final LocationSolution locationSolutionWithFp1Modified = LocationSolution
      .from(UUID.randomUUID(), location3, locationRestraint, locationUncertainty, locationBehaviors,
          Set.of(featurePrediction1, featurePrediction2));
  private static final LocationSolution locationSolutionWithFp2 = LocationSolution
      .from(UUID.randomUUID(), location3, locationRestraint, locationUncertainty, locationBehaviors,
          Set.of(featurePrediction2));
  private static final LocationSolution locationSolutionWithFp3 = LocationSolution
      .from(UUID.randomUUID(), location3, locationRestraint, locationUncertainty, locationBehaviors,
          Set.of(featurePrediction3));
  static final LocationSolution locationSolutionNoInsantValue = LocationSolution
      .from(UUID.randomUUID(), location3, locationRestraint,
          locationUncertainty, locationBehaviors, Set.of(featurePredictionNoInstantValue));

  private static EventHypothesis eh1 = EventHypothesis.from(
      ehId, eventId, Set.of(parentEhId), false,
      Set.of(locationSolutionWithFp1),
      PreferredLocationSolution.from(locationSolutionWithFp1),
      Set.of());
  private static EventHypothesis eh1Modified = EventHypothesis.from(
      ehId, eventId, Set.of(parentEhId), false,
      Set.of(locationSolutionWithFp1Modified, locationSolutionWithFp2),
      PreferredLocationSolution.from(locationSolutionWithFp1Modified),
      Set.of());
  private static EventHypothesis eh2 = EventHypothesis.from(
      UUID.randomUUID(), eventId, Set.of(parentEhId), false,
      Set.of(locationSolutionWithFp2),
      PreferredLocationSolution.from(locationSolutionWithFp2),
      Set.of());
  private static EventHypothesis eh3 = EventHypothesis.from(
      UUID.randomUUID(), eventId, Set.of(parentEhId), false,
      Set.of(locationSolutionWithFp3),
      PreferredLocationSolution.from(locationSolutionWithFp3),
      Set.of());

  private static EventHypothesis ehNoInstantValue = EventHypothesis.from(
      UUID.randomUUID(), eventNoInstantValueId, Set.of(parentEhNoInstantValueId), false,
      Set.of(locationSolutionNoInsantValue),
      PreferredLocationSolution.from(locationSolutionNoInsantValue),
      Set.of());


  static final Event eventWithFeaturePredictions = Event.from(
      eventId,
      Set.of(),
      monitoringOrg,
      Set.of(eh1),
      List.of(FinalEventHypothesis.from(eh1)),
      List.of(PreferredEventHypothesis.from(processingStageId, eh1)));
  static final Event eventWithFeaturePredictionsModified = Event.from(
      eventId,
      Set.of(),
      monitoringOrg,
      Set.of(eh1Modified, eh2),
      List.of(FinalEventHypothesis.from(eh1Modified)),
      List.of(PreferredEventHypothesis.from(processingStageId, eh1Modified)));
  static final Event eventWithFeaturePredictions3 = Event.from(
      eventId,
      Set.of(),
      monitoringOrg,
      Set.of(eh3),
      List.of(FinalEventHypothesis.from(eh3)),
      List.of(PreferredEventHypothesis.from(processingStageId, eh3)));

  static final Event eventNoInstantValue = Event.from(
      eventNoInstantValueId,
      Set.of(),
      monitoringOrg,
      Set.of(ehNoInstantValue),
      List.of(FinalEventHypothesis.from(ehNoInstantValue)),
      List.of(PreferredEventHypothesis.from(processingStageId, ehNoInstantValue)));


}
