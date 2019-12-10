package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import com.google.common.base.Preconditions;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "signal_detection_hypothesis")
public class SignalDetectionHypothesisDao implements Updateable<SignalDetectionHypothesis> {

  @Id
  @GeneratedValue
  private long daoId;

  @Column(updatable = false)
  private UUID signalDetectionHypothesisId;

  @ManyToOne(optional = false)
  private SignalDetectionDao parentSignalDetection;

  @Column
  private boolean isRejected;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private InstantFeatureMeasurementDao arrivalTimeMeasurement;

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private PhaseFeatureMeasurementDao phaseMeasurement;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<FeatureMeasurementDao<?>> otherMeasurements;

  @Column(updatable = false)
  private UUID creationInfoId;

  public SignalDetectionHypothesisDao() {
  }

  public SignalDetectionHypothesisDao(UUID signalDetectionHypothesisId,
      SignalDetectionDao parentSignalDetection, boolean isRejected,
      InstantFeatureMeasurementDao arrivalTimeMeasurement,
      PhaseFeatureMeasurementDao phaseMeasurement,
      List<FeatureMeasurementDao<?>> otherMeasurements,
      UUID creationInfoId) {
    this.signalDetectionHypothesisId = signalDetectionHypothesisId;
    this.parentSignalDetection = parentSignalDetection;
    this.isRejected = isRejected;
    this.arrivalTimeMeasurement = arrivalTimeMeasurement;
    this.phaseMeasurement = phaseMeasurement;
    this.otherMeasurements = otherMeasurements;
    this.creationInfoId = creationInfoId;
  }

  public long getDaoId() {
    return daoId;
  }

  public void setDaoId(long daoId) {
    this.daoId = daoId;
  }

  public UUID getSignalDetectionHypothesisId() {
    return signalDetectionHypothesisId;
  }

  public void setSignalDetectionHypothesisId(UUID signalDetectionHypothesisId) {
    this.signalDetectionHypothesisId = signalDetectionHypothesisId;
  }

  public SignalDetectionDao getParentSignalDetection() {
    return parentSignalDetection;
  }

  public void setParentSignalDetection(
      SignalDetectionDao parentSignalDetection) {
    this.parentSignalDetection = parentSignalDetection;
  }

  public boolean isRejected() {
    return isRejected;
  }

  public void setRejected(boolean rejected) {
    isRejected = rejected;
  }

  public List<FeatureMeasurementDao<?>> getFeatureMeasurements() {
    return otherMeasurements;
  }

  public void setFeatureMeasurements(
      List<FeatureMeasurementDao<?>> otherMeasurements) {
    this.otherMeasurements = otherMeasurements;
  }

  public InstantFeatureMeasurementDao getArrivalTimeMeasurement() {
    return arrivalTimeMeasurement;
  }

  public void setArrivalTimeMeasurement(
      InstantFeatureMeasurementDao arrivalTimeMeasurement) {
    this.arrivalTimeMeasurement = arrivalTimeMeasurement;
  }

  public PhaseFeatureMeasurementDao getPhaseMeasurement() {
    return phaseMeasurement;
  }

  public void setPhaseMeasurement(
      PhaseFeatureMeasurementDao phaseMeasurement) {
    this.phaseMeasurement = phaseMeasurement;
  }

  public UUID getCreationInfoId() {
    return creationInfoId;
  }

  public void setCreationInfoId(UUID creationInfoId) {
    this.creationInfoId = creationInfoId;
  }

  @Override
  public boolean update(SignalDetectionHypothesis updatedValue) {
    boolean updated = false;

    Preconditions.checkState(updatedValue.getId().equals(signalDetectionHypothesisId),
        "Cannot update SignalDetectionHypothesisDao from an unrelated SignalDetectionHypothesis");

    if (isRejected != updatedValue.isRejected()) {
      isRejected = updatedValue.isRejected();
      updated = true;
    }

    Optional<FeatureMeasurement<InstantValue>> updatedArrivalTime =
        updatedValue.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME);
    if (arrivalTimeMeasurement == null && updatedArrivalTime.isPresent()) {
      arrivalTimeMeasurement = new InstantFeatureMeasurementDao(updatedArrivalTime.get());
      updated = true;
    } else if (arrivalTimeMeasurement != null && !updatedArrivalTime.isPresent()) {
      arrivalTimeMeasurement = null;
      updated = true;
    } else if (arrivalTimeMeasurement != null && updatedArrivalTime.isPresent()) {
      updated |= arrivalTimeMeasurement.update(updatedArrivalTime.get());
    }

    Optional<FeatureMeasurement<PhaseTypeMeasurementValue>> updatedPhaseType =
        updatedValue.getFeatureMeasurement(FeatureMeasurementTypes.PHASE);
    if (phaseMeasurement == null && updatedPhaseType.isPresent()) {
      phaseMeasurement = new PhaseFeatureMeasurementDao(updatedPhaseType.get());
      updated = true;
    } else if (phaseMeasurement != null && !updatedPhaseType.isPresent()) {
      phaseMeasurement = null;
      updated = true;
    } else if (phaseMeasurement != null && updatedPhaseType.isPresent()) {
      updated |= phaseMeasurement.update(updatedPhaseType.get());
    }

    Map<UUID, FeatureMeasurementDao> originalMeasurementsByUuid = otherMeasurements.stream()
        .collect(Collectors.toMap(FeatureMeasurementDao::getId, Function.identity()));

    Map<UUID, FeatureMeasurement> updatedMeasurementsByUuid = updatedValue.getFeatureMeasurements()
        .stream()
        .collect(Collectors.toMap(FeatureMeasurement::getId, Function.identity()));

    // remove the arrival and phase measurements from the measurements by UUID so we don't add them
    // a second time
    if (arrivalTimeMeasurement != null) {
      updatedMeasurementsByUuid.remove(arrivalTimeMeasurement.getId());
    }

    if (phaseMeasurement != null) {
      updatedMeasurementsByUuid.remove(phaseMeasurement.getId());
    }

    // sublist for the things that were removed during the update
    List<FeatureMeasurementDao<?>> removed = otherMeasurements.stream()
        .filter(measurement -> !updatedMeasurementsByUuid.containsKey(measurement.getId()))
        .collect(Collectors.toList());

    // sublist for the things that were added
    List<FeatureMeasurementDao<?>> added = updatedMeasurementsByUuid.values().stream()
        .filter(measurement -> !originalMeasurementsByUuid.containsKey(measurement.getId()))
        .map(FeatureMeasurementDao::fromCoi)
        .collect(Collectors.toList());

    // sublist for the things that were modified
    boolean modified = updatedMeasurementsByUuid.values().stream()
        .filter(measurement -> originalMeasurementsByUuid.containsKey(measurement.getId()))
        .map(measurement -> originalMeasurementsByUuid.get(measurement.getId()).update(measurement))
        .reduce(false, (previous, newValue) -> previous || newValue);

    updated |= !removed.isEmpty();
    updated |= !added.isEmpty();
    updated |= modified;

    List<FeatureMeasurementDao<?>> finalMeasurements = new ArrayList<>(otherMeasurements);
    finalMeasurements.removeAll(removed);
    finalMeasurements.addAll(added);

    otherMeasurements = finalMeasurements;

    return updated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SignalDetectionHypothesisDao that = (SignalDetectionHypothesisDao) o;
    return daoId == that.daoId &&
        isRejected == that.isRejected &&
        Objects.equals(signalDetectionHypothesisId, that.signalDetectionHypothesisId) &&
        Objects.equals(parentSignalDetection, that.parentSignalDetection) &&
        Objects.equals(otherMeasurements, that.otherMeasurements) &&
        Objects.equals(arrivalTimeMeasurement, that.arrivalTimeMeasurement) &&
        Objects.equals(phaseMeasurement, that.phaseMeasurement) &&
        Objects.equals(creationInfoId, that.creationInfoId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(daoId, signalDetectionHypothesisId, parentSignalDetection, isRejected,
        otherMeasurements, arrivalTimeMeasurement, phaseMeasurement, creationInfoId);
  }

  @Override
  public String toString() {
    return "SignalDetectionHypothesisDao{" +
        "daoId=" + daoId +
        ", signalDetectionHypothesisId=" + signalDetectionHypothesisId +
        ", parentSignalDetection=" + parentSignalDetection +
        ", isRejected=" + isRejected +
        ", featureMeasurements=" + otherMeasurements +
        ", arrivalTimeMeasurement=" + arrivalTimeMeasurement +
        ", phaseMeasurement=" + phaseMeasurement +
        ", creationInfoId=" + creationInfoId +
        '}';
  }
}
