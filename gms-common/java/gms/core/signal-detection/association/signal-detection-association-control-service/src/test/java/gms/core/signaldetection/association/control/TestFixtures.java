package gms.core.signaldetection.association.control;

import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterionDefinition;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationDefinition;
import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
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
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
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
  public static final EventLocation eventLocation = EventLocation.from(
      45.27125,
      130.55595,
      0.0,
      Instant.parse("2010-05-20T21:37:15.048Z")
  );

  public static final ReferenceStation referenceStation = ReferenceStation.create(
      "Basic Station",
      "A Sample description",
      StationType.UNKNOWN,
      InformationSource.create(
          "SOMEONE",
          Instant.now(),
          "Sample Reference"
      ),
      "Sample comment",
      0.0,
      0.0,
      50.0,
      Instant.now(),
      Instant.now(),
      List.of()
  );

  // List of SignalDetectionHypotheses used when creating the Event object in this class
  public static final List<SignalDetectionHypothesis> signalDetectionHypotheses = List.of(
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
  public static final List<SignalDetectionHypothesisDescriptor> signalDetectionHypothesesDescriptors = List.of(
      SignalDetectionHypothesisDescriptor.from(SignalDetectionHypothesis.from(
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
      ), UUID.randomUUID())
  );


  public static final WeightedEventCriteriaCalculationDefinition weightedEventCriteriaCalculationDefinition =
      WeightedEventCriteriaCalculationDefinition.create(
          0.65,
          0.65,
          0.65,
          0.65,
          0.65,
          0.65,
          0.65
      );
  public static final ArrivalQualityEventCriterionDefinition arrivalQualityEventCriterionDefinition =
      ArrivalQualityEventCriterionDefinition.create(
          0.65,
          0.65,
          0.65,
          0.65
      );

  public static final SignalDetectionAssociationParameters signalDetectionAssociationParameters =
      SignalDetectionAssociationParameters.create(
          PluginInfo.from("sample-plugin", "1.0.0"),
          SignalDetectionAssociatorDefinition.create(
              5,
              0.65,
              List.of(PhaseType.P),
              List.of(PhaseType.P),
              0.65,
              true,
              0.65,
              0.65,
              true,
              1.0,
              50.0,
              100.0,
              3.5,
              weightedEventCriteriaCalculationDefinition,
              arrivalQualityEventCriterionDefinition,
              5));

  
  public static Set<LocationBehavior> associatedLocationBehaviors = TestFixtures.signalDetectionHypotheses
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
  public static LocationSolution locationSolution = LocationSolution.create(
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

  // Create List of SignalDetectionHypothesisClaimChecks from the list of SignalDetectionHypotheses
  public static final List<SignalDetectionHypothesisClaimCheck> signalDetectionHypothesisClaimChecks =
      TestFixtures.signalDetectionHypotheses.stream()
          .map(sdh -> SignalDetectionHypothesisClaimCheck.from(
              sdh.getId(), sdh.getParentSignalDetectionId())).collect(Collectors.toList());

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
  public static final EventHypothesis associatedEventHypothesis = TestFixtures.associatedEvent
      .getHypotheses().iterator()
      .next();

  // Create an EventHypothesisClaimCheck from the associated EventHypothesis
  public static EventHypothesisClaimCheck eventHypothesisClaimCheck = EventHypothesisClaimCheck
      .from(TestFixtures.associatedEventHypothesis.getId(),
          TestFixtures.associatedEventHypothesis.getEventId());

  // Create a LocationSolution from the associated Event to use when creating the unassociated Event
  public static final LocationSolution unassociatedLocationSolution = LocationSolution.create(
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
  public static final Event unassociatedEvent = Event.from(
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
  public static final EventHypothesis unassociatedEventHypothesis = TestFixtures.unassociatedEvent
      .getHypotheses().iterator()
      .next();

  // List of Signal Detections
  public static final List<SignalDetection> associateSignalDetections = List.of(
    SignalDetection.from(UUID.randomUUID(),
        associatedEvent.getMonitoringOrganization(),
        UUID.randomUUID(),
        List.of(),
        UUID.randomUUID()),
      SignalDetection.from(UUID.randomUUID(),
          associatedEvent.getMonitoringOrganization(),
          UUID.randomUUID(),
          List.of(),
          UUID.randomUUID()),
      SignalDetection.from(UUID.randomUUID(),
          associatedEvent.getMonitoringOrganization(),
          UUID.randomUUID(),
          List.of(),
          UUID.randomUUID())
      );

  // List of Events with Associations
  public static final List<Event> associatedEvents = List.of(associatedEvent);

  public static final ProcessingContext processingContext =
      ProcessingContext.createAutomatic(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          StorageVisibility.valueOf("PUBLIC"));
}
