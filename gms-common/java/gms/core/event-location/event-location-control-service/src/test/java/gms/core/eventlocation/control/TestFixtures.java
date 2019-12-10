package gms.core.eventlocation.control;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

class TestFixtures {

  static {
    System.out.println(FeatureMeasurement.create(
        UUID.randomUUID(),
        InstantMeasurementType
            .from(FeatureMeasurementTypes.ARRIVAL_TIME.getFeatureMeasurementTypeName()),
        InstantValue.from(
            Instant.now(),
            Duration.ofDays(100)
        )
    ).getFeatureMeasurementType() == FeatureMeasurementTypes.ARRIVAL_TIME);
  }

  // List of stations corresponding to the station ids in signal detections
  static final List<ReferenceStation> stations = List.of(
      ReferenceStation.create(
          "station",
          "station",
          StationType.SeismicArray,
          InformationSource.create(
              "station",
              Instant.now(),
              "station"
          ),
          "station",
          0.1,
          2.3,
          4.5,
          Instant.now(),
          Instant.now(),
          List.of()
      )
  );

  static final List<SignalDetection> signalDetections = List.of(
      SignalDetection.create(
          "cool",
          TestFixtures.stations.get(0).getVersionId(),
          List.of(
              FeatureMeasurement.create(
                  UUID.randomUUID(),
                  FeatureMeasurementTypes.SLOWNESS,
                  NumericMeasurementValue
                      .from(Instant.now(), DoubleValue.from(1.2, 3.4, Units.SECONDS_PER_DEGREE))
              ),
              FeatureMeasurement.create(
                  UUID.randomUUID(),
                  FeatureMeasurementTypes.ARRIVAL_TIME,
                  InstantValue.from(
                      Instant.now(),
                      Duration.ofDays(100)
                  )
              ),
              FeatureMeasurement.create(
                  UUID.randomUUID(),
                  FeatureMeasurementTypes.PHASE,
                  PhaseTypeMeasurementValue.from(PhaseType.P, 1.2)
              )
          ),
          UUID.randomUUID()
      )
  );

  // List of SignalDetectionHypotheses used when creating the Event object in this class
  static final List<SignalDetectionHypothesis> signalDetectionHypotheses = List.of(
      SignalDetectionHypothesis.from(
          UUID.randomUUID(),
          UUID.randomUUID(),
          false,
          List.of(
              FeatureMeasurement.create(
                  UUID.randomUUID(),
                  FeatureMeasurementTypes.ARRIVAL_TIME,
                  InstantValue.from(Instant.EPOCH, Duration.ofMillis(0))
              ),
              FeatureMeasurement.create(
                  UUID.randomUUID(),
                  FeatureMeasurementTypes.PHASE,
                  PhaseTypeMeasurementValue.from(
                      PhaseType.P,
                      0.0)
              )
          ),
          UUID.randomUUID()
      )
  );

  // EventLocation used when creating the Event object in this class
  private static final EventLocation eventLocation = EventLocation.from(
      45.27125,
      130.55595,
      0.0,
      Instant.parse("2010-05-20T21:37:15.048Z")
  );

  private static Set<LocationBehavior> associatedLocationBehaviors = TestFixtures.signalDetectionHypotheses
      .stream()
      .flatMap(sdh -> sdh.getFeatureMeasurements().stream())
      .map(fm ->
          LocationBehavior.from(
              0.0,
              1.0,
              true,
              UUID.fromString("00000000-0000-0000-0000-000000000000"),
              fm.getId()
          )
      ).collect(Collectors.toSet());

  // LocationSolution used when creating the Event object in this class
  static LocationSolution locationSolution = LocationSolution.create(
      TestFixtures.eventLocation,
      new LocationRestraint.Builder().build(),
      null,
      TestFixtures.associatedLocationBehaviors,
      Set.of(
          FeaturePrediction.create(
              PhaseType.P,
              Optional.of(NumericMeasurementValue.from(
                  Instant.EPOCH,
                  DoubleValue.from(
                      1.0,
                      0.0,
                      Units.SECONDS_PER_DEGREE
                  )
              )),
              Set.of(),
              false,
              FeatureMeasurementTypes.SLOWNESS,
              EventLocation.from(80.0, 80.0, 100.0, Instant.EPOCH),
              Location.from(60.0, 60.0, 100.0, 0.0),
              Optional.of(UUID.randomUUID()),
              Map.of()
          ),
          FeaturePrediction.create(
              PhaseType.P,
              Optional.of(InstantValue.from(
                  Instant.EPOCH,
                  Duration.ZERO
              )),
              Set.of(),
              false,
              FeatureMeasurementTypes.ARRIVAL_TIME,
              EventLocation.from(80.0, 80.0, 100.0, Instant.EPOCH),
              Location.from(60.0, 60.0, 100.0, 0.0),
              Optional.of(UUID.randomUUID()),
              Map.of()
          ),
          FeaturePrediction.create(
              PhaseType.P,
              Optional.of(PhaseTypeMeasurementValue.from(
                  PhaseType.I,
                  0.0
              )),
              Set.of(),
              false,
              FeatureMeasurementTypes.PHASE,
              EventLocation.from(80.0, 80.0, 100.0, Instant.EPOCH),
              Location.from(60.0, 60.0, 100.0, 0.0),
              Optional.of(UUID.randomUUID()),
              Map.of()
          )
      )
  );

  static LocationSolution locationSolutionFixedAtDepth = LocationSolution.create(
      TestFixtures.eventLocation,
      new LocationRestraint.Builder()
          .setDepthRestraint(1.0)
          .build(),
      null,
      TestFixtures.associatedLocationBehaviors,
      new HashSet<>()
  );

  static LocationSolution locationSolutionFixedAtSurface = LocationSolution.create(
      TestFixtures.eventLocation,
      new LocationRestraint.Builder()
          .setDepthRestraintAtSurface()
          .build(),
      null,
      TestFixtures.associatedLocationBehaviors,
      new HashSet<>()
  );

  // Create an Event with SignalDetectionHypothesis associations
  static final Event event = Event.create(
      new HashSet<>(),
      TestFixtures.signalDetections.stream().flatMap(
          sd -> sd.getSignalDetectionHypotheses().stream()).map(SignalDetectionHypothesis::getId)
          .collect(
              Collectors.toSet()),
      Set.of(TestFixtures.locationSolution),
      PreferredLocationSolution.from(TestFixtures.locationSolution),
      "MyFakeMonitoringOrganization",
      UUID.randomUUID()
  );
}
