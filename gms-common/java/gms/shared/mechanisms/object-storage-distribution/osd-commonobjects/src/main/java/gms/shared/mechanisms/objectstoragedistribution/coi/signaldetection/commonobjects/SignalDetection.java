package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.ParameterValidation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A Signal Detection represents the recording of the arrival of energy at a station (Figure 19).
 * Determining a consistent solution for a Signal Detection (e.g., its phase identification and
 * arrival time) is often an iterative process. A Signal Detection Hypothesis represents a proposed
 * explanation for a Signal Detection (e.g., it is a P phase at a particular time). A Signal
 * Detection can have multiple Signal Detection Hypotheses: for example, a computer algorithm could
 * create the original Signal Detection Hypothesis, assigning a phase and an arrival time; an
 * analyst reviewing that hypothesis may then choose to change the phase and/or the arrival time,
 * hence creating a new Signal Detection Hypothesis. Signal Detection has an ordered list of its
 * Signal Detection Hypothesis tracking how the Signal Detection was updated over time. Signal
 * Detection Hypothesis also includes an attribute to track Signal Detection Hypotheses that were
 * rejected during a particular processing stage (is rejected), in order to prevent their
 * re-creation in subsequent processing stages. Note that processing stage is tracked through the
 * Creation Info class attached to Signal Detection Hypothesis.
 */
public class SignalDetection {

  private final UUID id;
  private final String monitoringOrganization;
  private final UUID stationId;
  private final List<SignalDetectionHypothesis> signalDetectionHypotheses;
  private final UUID creationInfoId;

  /**
   * Obtains an instance from SignalDetection.
   *
   * @param id The {@link UUID} id assigned to the new SignalDetection.
   * @param stationId The station detecting this signal
   * @param signalDetectionHypotheses The set of {@link SignalDetectionHypothesis} objects assigned
   * to the new SignalDetection.
   * @param creationInfoId An identifier representing this object's provenance.
   */
  private SignalDetection(UUID id, String monitoringOrganization, UUID stationId,
      List<SignalDetectionHypothesis> signalDetectionHypotheses, UUID creationInfoId) {

    this.id = id;
    this.monitoringOrganization = monitoringOrganization;
    this.stationId = stationId;
    this.creationInfoId = creationInfoId;

    // Making a copy of the signal detection array so it is immutable.
    this.signalDetectionHypotheses = new ArrayList<>(signalDetectionHypotheses);
  }

  /**
   * Recreation factory method (sets the SignalDetection entity identity). Handles parameter
   * validation. Used for deserialization and recreating from persistence.
   *
   * @param id The {@link UUID} id assigned to the new SignalDetection.
   * @param stationId The station detecting this signal
   * @param signalDetectionHypotheses The set of {@link SignalDetectionHypothesis} objects assigned
   * to the new SignalDetection.
   * @param creationInfoId An identifier representing this object's provenance.
   * @return QcMask representing all the input parameters.
   * @throws IllegalArgumentException if any of the parameters are null
   */
  public static SignalDetection from(UUID id,
      String monitoringOrganization,
      UUID stationId,
      List<SignalDetectionHypothesis> signalDetectionHypotheses,
      UUID creationInfoId) {
    Objects.requireNonNull(id, "Cannot create SignalDetection from a null id");
    Objects.requireNonNull(monitoringOrganization,
        "Cannot create SignalDetection from a null monitoringOrganization");
    Objects.requireNonNull(stationId, "Cannot create SignalDetection from a null stationId");
    Objects.requireNonNull(signalDetectionHypotheses,
        "Cannot create SignalDetection from null SignalDetectionHypothesis");
    Objects.requireNonNull(creationInfoId, "SignalDetection's creation info id cannot be null");

    boolean onlyContainsRejected = true;
    for (SignalDetectionHypothesis sdh : signalDetectionHypotheses) {

      ParameterValidation.requireTrue(id::equals, sdh.getParentSignalDetectionId(),
          "SignalDetectionHypothesis with parent id=" + sdh.getParentSignalDetectionId() +
              "cannot be assigned to SignalDetection with id=" + id);

      if (!sdh.isRejected()) {
        onlyContainsRejected = false;
      }
    }
    if (!signalDetectionHypotheses.isEmpty() && onlyContainsRejected) {
      throw new IllegalArgumentException("Cannot create a SignalDetection containing only rejected"
          + " SignalDetectionHypotheses");
    }

    return new SignalDetection(id, monitoringOrganization, stationId, signalDetectionHypotheses,
        creationInfoId);
  }

  /**
   * Creates SignalDetection with an unrejected SignalDetectionHypothesis.
   */
  public static SignalDetection create(String monitoringOrganization, UUID stationId,
      List<FeatureMeasurement<?>> featureMeasurements, UUID creationInfoId) {

    SignalDetection signalDetection = SignalDetection
        .from(UUID.randomUUID(), monitoringOrganization, stationId,
            Collections.emptyList(), creationInfoId);

    signalDetection.addSignalDetectionHypothesis(featureMeasurements, creationInfoId);

    return signalDetection;
  }

  /**
   * Creates a new rejected {@link SignalDetectionHypothesis} for this SignalDetection by copying an
   * existing SignalDetectionHypothesis.
   *
   * @param creationInfoId The creation info for this SignalDetectionHypothesis.
   * @throws IllegalArgumentException if any parameters are null
   */
  public void reject(UUID signalDetectionHypothesisIdToClone, UUID creationInfoId) {
    Objects.requireNonNull(signalDetectionHypothesisIdToClone);

    List<SignalDetectionHypothesis> signalDetectionHypothesesToClone =
        signalDetectionHypotheses.stream()
            .filter(s -> signalDetectionHypothesisIdToClone.equals(s.getId()))
            .collect(Collectors.toList());

    if (signalDetectionHypothesesToClone.isEmpty()) {
      throw new IllegalArgumentException(
          "No SignalDetectionHypothesis exists for id=" + signalDetectionHypothesesToClone);
    }
    if (signalDetectionHypothesesToClone.size() != 1) {
      throw new IllegalArgumentException(
          signalDetectionHypothesesToClone.size() + " SignalDetectionHypotheses exist for id="
              + signalDetectionHypothesesToClone);
    }

    SignalDetectionHypothesis sdh = signalDetectionHypothesesToClone.get(0);
    signalDetectionHypotheses.add(sdh.toBuilder().setRejected(true).build());
  }

  public UUID getId() {
    return id;
  }

  public String getMonitoringOrganization() {
    return monitoringOrganization;
  }

  public UUID getStationId() {
    return stationId;
  }

  /**
   * Returns an unmodifiable list of signal detection hypotheses.
   */
  public List<SignalDetectionHypothesis> getSignalDetectionHypotheses() {
    return Collections.unmodifiableList(signalDetectionHypotheses);
  }

  public UUID getCreationInfoId() {
    return creationInfoId;
  }

  /**
   * Adds an unrejected SignalDetectionHypothesis to this SignalDetection
   */
  public void addSignalDetectionHypothesis(List<FeatureMeasurement<?>> featureMeasurements,
      UUID creationInfoId) {
    SignalDetectionHypothesis signalDetectionHypothesis = SignalDetectionHypothesis.create(
        getId(), featureMeasurements, creationInfoId);
    signalDetectionHypotheses.add(signalDetectionHypothesis);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SignalDetection that = (SignalDetection) o;

    return id.equals(that.id) && monitoringOrganization.equals(that.monitoringOrganization)
        && stationId.equals(that.stationId) && signalDetectionHypotheses
        .equals(that.signalDetectionHypotheses) && creationInfoId.equals(that.creationInfoId);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + monitoringOrganization.hashCode();
    result = 31 * result + stationId.hashCode();
    result = 31 * result + signalDetectionHypotheses.hashCode();
    result = 31 * result + creationInfoId.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "SignalDetection{" +
        "id=" + id +
        ", monitoringOrganization='" + monitoringOrganization + '\'' +
        ", stationId=" + stationId +
        ", signalDetectionHypotheses=" + signalDetectionHypotheses +
        ", creationInfoId=" + creationInfoId +
        '}';
  }
}
