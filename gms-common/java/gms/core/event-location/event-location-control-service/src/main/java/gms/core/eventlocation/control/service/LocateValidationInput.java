package gms.core.eventlocation.control.service;

import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class used to contain validated input for {@link gms.core.eventlocation.control.EventLocationControl#locateValidation(LocateValidationInput)}.
 * This object contains two {@link Collection}s of data that must be validated.  The {@link
 * EventHypothesis} object must have {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis}
 * {@link java.util.UUID}s that match those in the {@link Collection} of {@link
 * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection}s.
 * The {@link Collection} of {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection}s
 * must contain {@link gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation}
 * {@link java.util.UUID}s that match those in the provided {@link Collection} of {@link
 * gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation}s.
 * {@link LocateValidationInput} performs this validation prior to instantiation via {@link
 * LocateValidationInput#create(EventHypothesis, List, Set)}.
 */
@AutoValue
public abstract class LocateValidationInput {

  public abstract EventHypothesis getEventHypothesis();

  public abstract List<SignalDetectionHypothesis> getSignalDetectionHypotheses();

  public abstract List<ReferenceStation> getReferenceStations();

  /**
   * Creates and returns a new {@link LocateValidationInput} object.  This method validates
   * consistency of input data, and creates the {@link List}s of data required by the {@link
   * gms.core.eventlocation.plugins.EventLocatorPlugin} used by {@link
   * gms.core.eventlocation.control.EventLocationControl}
   *
   * @param eventHypothesis {@link EventHypothesis} object containing {@link
   * SignalDetectionEventAssociation} objects with {@link UUID}s of {@link
   * SignalDetectionHypothesis} contained in the provided {@link SignalDetection}s.  Not null.
   * @param signalDetections {@link List} of {@link SignalDetection} objects containing the {@link
   * SignalDetectionHypothesis} objects referenced by the {@link SignalDetectionEventAssociation}
   * objects contained in the provided {@link EventHypothesis}.  The {@link SignalDetection}s in
   * this {@link List} must contain {@link ReferenceStation} {@link UUID}s present in the provided
   * {@link List} of {@link ReferenceStation}s.  Not empty, not null.
   * @param referenceStations {@link List} of {@link ReferenceStation}s associated with the {@link
   * ReferenceStation} {@link UUID}s contained in each provided {@link SignalDetection}.  Not empty,
   * not null.
   * @return New {@link LocateValidationInput}, not null.
   */
  public static LocateValidationInput create(
      EventHypothesis eventHypothesis,
      List<SignalDetection> signalDetections,
      Set<ReferenceStation> referenceStations
  ) {

    // Validate input is not null
    Objects.requireNonNull(eventHypothesis, "Null eventHypothesis");
    Objects.requireNonNull(signalDetections, "Null signalDetections");
    Objects.requireNonNull(referenceStations, "Null referenceStations");

    // Validate signalDetections is not empty
    if (signalDetections.isEmpty()) {

      throw new IllegalArgumentException("Empty signalDetections");
    }

    // Validate referenceSTations is not empty
    if (referenceStations.isEmpty()) {

      throw new IllegalArgumentException("Empty referenceStations");
    }

    // Extract all associated Signal Detection Hypothesis UUIDs contained in the provided EventHypothesis
    Set<UUID> associatedSigDetHypothesisIds = eventHypothesis.getAssociations().stream().map(
        SignalDetectionEventAssociation::getSignalDetectionHypothesisId)
        .collect(Collectors.toSet());

    // Extract the correct Signal Detection Hypotheses from the provided Signal Detections using the
    // associated Signal Detection Hypothesis UUIDs from the provided EventHypothesis
    List<SignalDetectionHypothesis> associatedSigDetHypotheses = signalDetections.stream()
        .flatMap(sd -> sd.getSignalDetectionHypotheses().stream())
        .filter(sdh -> associatedSigDetHypothesisIds.contains(sdh.getId())).collect(
            Collectors.toList());

    // Validate the size of the associated Signal Detection Hypothesis matches the number of associated
    // Signal Detection Hypothesis UUIDs
    if (associatedSigDetHypotheses.size() != associatedSigDetHypothesisIds.size()) {

      throw new IllegalArgumentException(
          "One or more associated Signal Detection Hypotheses was not provided");
    }

    // Extract all Reference Station UUIDs from the provided Signal Detections
    List<UUID> signalDetectionStationIds = signalDetections.stream()
        .map(SignalDetection::getStationId).collect(
            Collectors.toList());

    // Collect list of Reference Stations that correspond with the each element in the list of Signal Detection Hypotheses
    List<ReferenceStation> associatedReferenceStations = signalDetectionStationIds.stream()
        .flatMap(id ->
            referenceStations.stream()
                .filter(station -> station.getVersionId().equals(id))
        ).collect(Collectors.toList());

    // Validate the size of the provided Reference Stations matches the number of unique Reference
    // Station UUIDs contained in the provided Signal Detections
    if (signalDetectionStationIds.size() != associatedReferenceStations.size()) {

      throw new IllegalArgumentException(
          "Missing one or more Reference Stations associated with the provided Signal Detections");
    }

    return new AutoValue_LocateValidationInput(eventHypothesis, associatedSigDetHypotheses,
        associatedReferenceStations);
  }
}
