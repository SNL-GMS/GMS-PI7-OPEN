package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.FirstMotionMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FirstMotionType;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class FirstMotionMeasurementValueDao implements Updateable<FirstMotionMeasurementValue> {

  @Column(name = "first_motion")
  private FirstMotionType firstMotion;

  private double confidence;

  public FirstMotionMeasurementValueDao() {}

  public FirstMotionMeasurementValueDao(FirstMotionMeasurementValue val) {
    Objects.requireNonNull(val,
        "Cannot create FirstMotionMeasurementValueDao from null FirstMotionMeasurementValue");
    this.firstMotion = val.getValue();
    this.confidence = val.getConfidence();
  }

  public FirstMotionMeasurementValue toCoi() {
    return FirstMotionMeasurementValue.from(this.firstMotion, this.confidence);
  }

  public FirstMotionType getFirstMotion() {
    return firstMotion;
  }

  public void setFirstMotion(
      FirstMotionType firstMotion) {
    this.firstMotion = firstMotion;
  }

  public double getConfidence() {
    return confidence;
  }

  public void setConfidence(double confidence) {
    this.confidence = confidence;
  }

  @Override
  public boolean update(FirstMotionMeasurementValue updatedValue) {
    boolean updated = false;

    if (firstMotion != updatedValue.getValue()) {
      firstMotion = updatedValue.getValue();
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
    FirstMotionMeasurementValueDao that = (FirstMotionMeasurementValueDao) o;
    return Double.compare(that.confidence, confidence) == 0 &&
        firstMotion == that.firstMotion;
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstMotion, confidence);
  }

  @Override
  public String toString() {
    return "FirstMotionMeasurementValueDao{" +
        "firstMotion=" + firstMotion +
        ", confidence=" + confidence +
        '}';
  }
}
