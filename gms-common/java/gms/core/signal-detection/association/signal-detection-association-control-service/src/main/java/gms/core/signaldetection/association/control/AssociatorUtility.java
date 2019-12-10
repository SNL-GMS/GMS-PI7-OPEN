package gms.core.signaldetection.association.control;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationUncertainty;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.Validate;


/**
 * A class for associating signals and locations with EventHypothesis objects
 */
class AssociatorUtility {

  private AssociatorUtility() {

  }

  /**
   * Associates a Set of SignalDetectionHypothesis objects with an EventHypothesis.  Does not modify
   * the provided EventHypothesis.  Creates a new EventHypothesis with the new associations
   * applied.
   *
   * @param detectionHypotheses {@link Set} of {@link SignalDetectionHypothesis} to associate with
   * the provided {@link EventHypothesis}
   * @param eventHypothesis {@link EventHypothesis} with which to associate the provided {@link Set}
   * of {@link SignalDetectionHypothesis}
   * @return New {@link EventHypothesis} with the requested associations applied
   */
  public static EventHypothesis associateToEvent(
      Collection<SignalDetectionHypothesis> detectionHypotheses,
      EventHypothesis eventHypothesis) {
    Objects.requireNonNull(eventHypothesis,
        "Cannot associate SignalDetectionHypothesis objects with null EventHypothesis.");
    Validate.notEmpty(detectionHypotheses,
        "Cannot associate empty list of SignalDetectionHypothesis objects with EventHypothesis.");
    Validate.isTrue(eventHypothesis.getPreferredLocationSolution().isPresent(),
        "Cannot associate SignalDetectionHypothesis objects with EventHypothesis that has no PreferredLocationSolutions.");

    // Create new list of Associations - we are adding to this list so we need to copy it
    Set<SignalDetectionEventAssociation> newAssociations = new HashSet<>(
        eventHypothesis.getAssociations());

    // Create and add new associations from provided SignalDetectionHypotheses
    detectionHypotheses.forEach(dh ->
        newAssociations.add(
            SignalDetectionEventAssociation.create(
                eventHypothesis.getId(),
                dh.getId())
        )
    );

    // Get preferred LocationSolution
    LocationSolution preferredLocationSolution = eventHypothesis.getPreferredLocationSolution()
        .orElseThrow(() -> new IllegalStateException("PreferredEventHypothesis not present"))
        .getLocationSolution();

    // Create new list of LocationBehaviors
    Set<LocationBehavior> newBehaviors = new HashSet<>(
        preferredLocationSolution.getLocationBehaviors());

    // Create and add new LocationBehaviors from provided SignalDetectionHypotheses
    detectionHypotheses.forEach(dh ->
        dh.getFeatureMeasurements().forEach(fm ->
            newBehaviors.add(
                LocationBehavior.from(
                    0.0,
                    1.0,
                    false,
                    UUID.fromString("00000000-0000-0000-0000-000000000000"),
                    fm.getId()
                )
            )
        )
    );

    LocationUncertainty locationUncertainty = preferredLocationSolution.getLocationUncertainty()
        .orElse(null);

    // Create new PreferredLocationSolution to add to new EventHypothesis
    PreferredLocationSolution newPreferredLocationSolution = PreferredLocationSolution.from(
        LocationSolution.from(
            preferredLocationSolution.getId(),
            preferredLocationSolution.getLocation(),
            preferredLocationSolution.getLocationRestraint(),
            locationUncertainty,
            newBehaviors,
            preferredLocationSolution.getFeaturePredictions()
        )
    );

    // Create new LocationSolutions to add to new EventHypothesis
    HashSet<LocationSolution> newLocationSolutions = new HashSet<>();
    newLocationSolutions.add(newPreferredLocationSolution.getLocationSolution());
    eventHypothesis.getLocationSolutions().forEach(ls -> {
      if (!ls.getId().equals(preferredLocationSolution.getId())) {
        newLocationSolutions.add(ls);
      }
    });

    return EventHypothesis.from(
        eventHypothesis.getId(),
        eventHypothesis.getEventId(),
        eventHypothesis.getParentEventHypotheses(),
        eventHypothesis.isRejected(),
        newLocationSolutions,
        newPreferredLocationSolution,
        newAssociations
    );
  }
}
