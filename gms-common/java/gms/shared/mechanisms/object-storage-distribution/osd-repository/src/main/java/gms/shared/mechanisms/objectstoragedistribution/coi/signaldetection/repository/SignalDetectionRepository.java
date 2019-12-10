package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.UpdateStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface SignalDetectionRepository {

  /**
   * Batch stores multiple {@link SignalDetectionHypothesis}. Will not persist a hypothesis if no
   * updates were required (i.e. no differences between what is currently stored and what is being
   * stored)
   * @param hypothesisDescriptors Collection of {@link SignalDetectionHypothesisDescriptor}, not null, can be empty
   * @return A Map of hypothesis {@link SignalDetectionHypothesisDescriptor} to the {@link UpdateStatus}, describing whether a hypothesis:
   * - was updated in persistence
   * - was unchanged and not peristed
   * - failed to persist
   *
   * This map is guaranteed to have an id for every hypothesis in hypotheses
   */
  Map<SignalDetectionHypothesisDescriptor, UpdateStatus> store(
      Collection<SignalDetectionHypothesisDescriptor> hypothesisDescriptors);

  /**
   * Store for the first time the provided {@link SignalDetection} and all of its {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis}
   * and {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement}
   * objects.
   *
   * @param signalDetection store this SignalDetection and its supporting hypotheses, not null
   */
  void store(SignalDetection signalDetection);

  /**
   * Store for the first time the provided {@link BeamCreationInfo}
   *
   * @param beamCreationInfo store this BeamCreationInfo, not null
   */
  void store(BeamCreationInfo beamCreationInfo) throws Exception;

  /**
   * Retrieves all of the {@link SignalDetection}s stored in this {@link
   * SignalDetectionRepository}
   *
   * @return collection of SignalDetections, not null
   */
  Collection<SignalDetection> retrieveAll();

  /**
   * Retrieves the {@link SignalDetection} with the supplied id, or empty if not found.
   * @param id The UUID of the requested SignalDetection.
   * @return SignalDetection or Optional.empty.
   */
  Optional<SignalDetection> findSignalDetectionById(UUID id);

  /**
   * Retrieves the list of {@link SignalDetection} with the supplied ids, or empty if not found.
   * @param ids The UUIDs of the request SignalDetections
   * @return List of SignalDetections
   */
  List<SignalDetection> findSignalDetectionsByIds(Collection<UUID> ids);

  /**
   * Retrieves the list of {@link SignalDetectionHypothesis} with the supplied ids, or empty if not found.
   * @param ids The UUIDs of the request SignalDetectionHypothesis objects
   * @return List of SignalDetectionHypothesis
   */
  List<SignalDetectionHypothesis> findSignalDetectionHypothesesByIds(Collection<UUID> ids);

  /**
   * Retrieves the {@link SignalDetectionHypothesis} with the supplied id, or empty if not found.
   * @param id The UUID of the requested SignalDetectionHypothesis.
   * @return  SignalDetection or Optional.empty.
   */
  Optional<SignalDetectionHypothesis> findSignalDetectionHypothesisById(UUID id);

  /**
   * Finds Signal Detections in a particular time range.
   * A SignalDetection will be included in the results if it has any SignalDetectionHypothesis
   * with an arrival time within the requested time range (inclusive).
   * @param start the start of the time range
   * @param end the end of the time range
   * @return list of signal detections; may be empty
   */
  List<SignalDetection> findSignalDetections(Instant start, Instant end);

  /**
   * Finds Signal Detections for the given stations in a particular time range.
   * A SignalDetection will be included in the results if it has any SignalDetectionHypothesis
   * with an arrival time within the requested time range (inclusive).
   * 
   * @param stationIds the ids of the stations
   * @param start the start of the time range
   * @param end the end of the time range
   * @return a map from the id of the station to the signal detections found for it over the time range.
   * There will be a key/value pair in the map for every requested channelSegmentId,
   * but value lists may be empty.
   */
  Map<UUID, List<SignalDetection>> findSignalDetectionsByStationIds(
      Collection<UUID> stationIds, Instant start, Instant end);
}
