package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class NumericMeasurementValueDao implements Updateable<NumericMeasurementValue> {

  @Column(name = "reference_time")
  private Instant referenceTime;

  @Column(name = "measurement_value")
  private DoubleValueDao measurementValue;

  public NumericMeasurementValueDao() {}

  public NumericMeasurementValueDao(NumericMeasurementValue val) {
    Objects.requireNonNull(val, "Cannot create NumericMeasurementValueDao from null NumericMeasurementValue");
    this.referenceTime = val.getReferenceTime();
    this.measurementValue = new DoubleValueDao(val.getMeasurementValue());
  }

  public NumericMeasurementValue toCoi() {
    return NumericMeasurementValue.from(this.referenceTime, this.measurementValue.toCoi());
  }

  public Instant getReferenceTime() {
    return referenceTime;
  }

  public void setReferenceTime(Instant referenceTime) {
    this.referenceTime = referenceTime;
  }

  public DoubleValueDao getMeasurementValue() {
    return measurementValue;
  }

  public void setMeasurementValue(
      DoubleValueDao measurementValue) {
    this.measurementValue = measurementValue;
  }

  @Override
  public boolean update(NumericMeasurementValue updatedValue) {
    boolean updated = false;

    if (!referenceTime.equals(updatedValue.getReferenceTime())) {
      referenceTime = updatedValue.getReferenceTime();
      updated = true;
    }

    return updated || measurementValue.update(updatedValue.getMeasurementValue());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NumericMeasurementValueDao that = (NumericMeasurementValueDao) o;
    return Objects.equals(referenceTime, that.referenceTime) &&
        Objects.equals(measurementValue, that.measurementValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(referenceTime, measurementValue);
  }

  @Override
  public String toString() {
    return "NumericMeasurementValueDao{" +
        "referenceTime=" + referenceTime +
        ", measurementValue=" + measurementValue +
        '}';
  }
}
