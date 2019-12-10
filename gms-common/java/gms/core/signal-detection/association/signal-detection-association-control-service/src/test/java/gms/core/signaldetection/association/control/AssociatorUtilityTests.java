package gms.core.signaldetection.association.control;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AssociatorUtilityTests {

  private final EventHypothesis eventHypothesis = TestFixtures.unassociatedEventHypothesis;
  private final List<SignalDetectionHypothesis> signalDetectionHypotheses = TestFixtures.signalDetectionHypotheses;

  @Test
  void testAssociateToEvent_NullSignals() {

    Assertions.assertThrows(NullPointerException.class, () ->
        AssociatorUtility.associateToEvent(null, this.eventHypothesis)
    );
  }

  @Test
  void testAssociateToEvent_NullEvent() {
    Assertions.assertThrows(NullPointerException.class, () ->
        AssociatorUtility.associateToEvent(this.signalDetectionHypotheses, null)
    );
  }

  @Test
  void testAssociateToEvent_EmptySignalDetectionHypotheses() {

    Set<SignalDetectionHypothesis> signalDetectionHypotheses = new HashSet<>();

    Assertions.assertThrows(IllegalArgumentException.class, () ->
        AssociatorUtility.associateToEvent(signalDetectionHypotheses, this.eventHypothesis)
    );
  }

  @Test
  void testAssociateToEvent() {

    Set<UUID> signalDetectionHypothesisIds = this.signalDetectionHypotheses.stream()
        .map(SignalDetectionHypothesis::getId).collect(
            Collectors.toSet());

    EventHypothesis newEventHypothesis = AssociatorUtility
        .associateToEvent(this.signalDetectionHypotheses, this.eventHypothesis);

    Set<SignalDetectionEventAssociation> signalDetectionEventAssociations = newEventHypothesis
        .getAssociations();

    Set<UUID> associatedSignalDetectionHypothesisIds = signalDetectionEventAssociations.stream()
        .map(SignalDetectionEventAssociation::getSignalDetectionHypothesisId).collect(
            Collectors.toSet());

    Assertions.assertEquals(signalDetectionHypothesisIds, associatedSignalDetectionHypothesisIds);

    Set<UUID> locationBehaviorMeasurementIds = new HashSet<>();
    this.signalDetectionHypotheses.forEach(sdh ->
        sdh.getFeatureMeasurements().forEach(fm ->
            locationBehaviorMeasurementIds.add(fm.getId())));

    this.eventHypothesis.getLocationSolutions().forEach(ls ->
        ls.getLocationBehaviors().forEach(lb ->
            locationBehaviorMeasurementIds.add(lb.getFeatureMeasurementId())
        )
    );

    Set<UUID> associatedLocationBehaviorMeasurementIds = new HashSet<>();
    newEventHypothesis.getLocationSolutions().forEach(ls ->
        ls.getLocationBehaviors().forEach(lb ->
            associatedLocationBehaviorMeasurementIds.add(lb.getFeatureMeasurementId())
        )
    );

    Assertions
        .assertEquals(locationBehaviorMeasurementIds, associatedLocationBehaviorMeasurementIds);
  }
}
