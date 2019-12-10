package gms.core.eventlocation.control.service;

import gms.core.eventlocation.control.EventHypothesisClaimCheck;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestFixtures {

  // EventLocation used when creating the Event object in this class
  private static final EventLocation eventLocation = EventLocation.from(
      45.27125,
      130.55595,
      0.0,
      Instant.parse("2010-05-20T21:37:15.048Z")
  );

  private static final UUID signalDetectionId = UUID.randomUUID();

  // List of SignalDetectionHypotheses used when creating the Event object in this class
  private static final List<SignalDetectionHypothesis> signalDetectionHypotheses = List.of(
      SignalDetectionHypothesis.from(
          UUID.randomUUID(),
          TestFixtures.signalDetectionId,
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

  static final ReferenceStation referenceStation =
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
      );

  static final List<SignalDetection> signalDetections = List.of(
      SignalDetection.from(
          TestFixtures.signalDetectionId,
          "FAKE",
          referenceStation.getVersionId(),
          TestFixtures.signalDetectionHypotheses,
          UUID.randomUUID()
      )
  );


  // List of stations corresponding to the station ids in signal detections
  static final Set<ReferenceStation> stations = List.of(referenceStation).stream().collect(Collectors.toSet());

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
  private static LocationSolution locationSolution = LocationSolution.create(
      TestFixtures.eventLocation,
      LocationRestraint.from(
          RestraintType.UNRESTRAINED,
          null,
          RestraintType.UNRESTRAINED,
          null,
          DepthRestraintType.UNRESTRAINED,
          null,
          RestraintType.UNRESTRAINED,
          null
      ),
      null,
      TestFixtures.associatedLocationBehaviors,
      new HashSet<>()
  );

  // Create an Event with SignalDetectionHypothesis associations
  public static final Event associatedEvent = Event.create(
      new HashSet<>(),
      TestFixtures.signalDetectionHypotheses.stream().map(SignalDetectionHypothesis::getId).collect(
          Collectors.toSet()),
      Set.of(TestFixtures.locationSolution),
      PreferredLocationSolution.from(TestFixtures.locationSolution),
      "MyFakeMonitoringOrganization",
      UUID.randomUUID()
  );

  // Get the EventHypothesis from the associatedEvent
  static final EventHypothesis associatedEventHypothesis = TestFixtures.associatedEvent
      .getHypotheses().iterator()
      .next();

  // Create an EventHypothesisClaimCheck from the associated EventHypothesis
  static EventHypothesisClaimCheck eventHypothesisClaimCheck = EventHypothesisClaimCheck
      .from(TestFixtures.associatedEventHypothesis.getId(),
          TestFixtures.associatedEventHypothesis.getEventId());

  // Create a LocationSolution from the associated Event to use when creating the unassociated Event
  private static final LocationSolution unassociatedLocationSolution = LocationSolution.create(
      TestFixtures.associatedEventHypothesis.getLocationSolutions().iterator()
          .next()
          .getLocation(),
      TestFixtures.associatedEventHypothesis.getLocationSolutions().iterator()
          .next()
          .getLocationRestraint(),
      null,
      new HashSet<>(),
      new HashSet<>()
  );

  // Create an Event from the associatedEvent, without the associations applied
  private static final Event unassociatedEvent = Event.from(
      associatedEvent.getId(),
      new HashSet<UUID>(),
      associatedEvent.getMonitoringOrganization(),
      Set.of(
          EventHypothesis.from(
              TestFixtures.associatedEventHypothesis.getId(),
              associatedEvent.getId(),
              TestFixtures.associatedEventHypothesis.getParentEventHypotheses(),
              TestFixtures.associatedEventHypothesis.isRejected(),
              Set.of(unassociatedLocationSolution),
              PreferredLocationSolution.from(TestFixtures.unassociatedLocationSolution),
              new HashSet<>()
          )
      ),
      new ArrayList<>(),
      new ArrayList<>()
  );

  // Get the EventHypothesis from the un-associated event
  private static final EventHypothesis unassociatedEventHypothesis = TestFixtures.unassociatedEvent
      .getHypotheses().iterator()
      .next();
}
