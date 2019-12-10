package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import java.util.Objects;
import javax.persistence.Embeddable;

@Embeddable
public class PhaseTypeMeasurementValueDao implements Updateable<PhaseTypeMeasurementValue> {

  private PhaseType phase;

  private double confidence;

  public PhaseTypeMeasurementValueDao() {}

  public PhaseTypeMeasurementValueDao(PhaseTypeMeasurementValue val) {
    Objects.requireNonNull(val,
        "Cannot create PhaseTypeMeasurementValueDao from null PhaseTypeMeasurementValue");
    this.phase = val.getValue();
    this.confidence = val.getConfidence();
  }

  public PhaseTypeMeasurementValue toCoi() {
    return PhaseTypeMeasurementValue.from(this.phase, this.confidence);
  }

  public PhaseType getPhase() {
    return phase;
  }

  public void setPhase(PhaseType phase) {
    this.phase = phase;
  }

  public double getConfidence() {
    return confidence;
  }

  public void setConfidence(double confidence) {
    this.confidence = confidence;
  }

  @Override
  public boolean update(PhaseTypeMeasurementValue updatedValue) {
    boolean updated = false;

    if (phase != updatedValue.getValue()) {
      phase = updatedValue.getValue();
      updated = true;
    }

    if (confidence != updatedValue.getConfidence()) {
      confidence = updatedValue.getConfidence();
      updated = true;
    }

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
    PhaseTypeMeasurementValueDao that = (PhaseTypeMeasurementValueDao) o;
    return Double.compare(that.confidence, confidence) == 0 &&
        phase == that.phase;
  }

  @Override
  public int hashCode() {
    return Objects.hash(phase, confidence);
  }

  @Override
  public String toString() {
    return "PhaseTypeMeasurementValueDao{" +
        "phase=" + phase +
        ", confidence=" + confidence +
        '}';
  }
}
