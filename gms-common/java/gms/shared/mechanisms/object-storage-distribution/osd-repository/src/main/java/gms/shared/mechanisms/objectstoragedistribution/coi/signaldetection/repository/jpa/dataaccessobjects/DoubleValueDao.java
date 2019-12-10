package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DoubleValueDao implements Updateable<DoubleValue> {

  // These fields are Double objects so that they can be null,
  // because classes that use this may need to be null also.

  private Double value;

  @Column(name = "standard_deviation")
  private Double standardDeviation;

  private Units units;

  public DoubleValueDao() {}

  public DoubleValueDao(DoubleValue val) {
    Objects.requireNonNull(val, "Cannot create DoubleValueDao from null DoubleValue");
    this.value = val.getValue();
    this.standardDeviation = val.getStandardDeviation();
    this.units = val.getUnits();
  }

  public DoubleValue toCoi() {
    return DoubleValue.from(this.value, this.standardDeviation, this.units);
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  public double getStandardDeviation() {
    return standardDeviation;
  }

  public void setStandardDeviation(double standardDeviation) {
    this.standardDeviation = standardDeviation;
  }

  public Units getUnits() {
    return units;
  }

  public void setUnits(Units units) {
    this.units = units;
  }

  @Override
  public boolean update(DoubleValue updatedValue) {
    boolean updated = false;

    if (value != updatedValue.getValue()) {
      value = updatedValue.getValue();
      updated = true;
    }

    if (standardDeviation != updatedValue.getStandardDeviation()) {
      standardDeviation = updatedValue.getStandardDeviation();
      updated = true;
    }

    if (units != updatedValue.getUnits()) {
      units = updatedValue.getUnits();
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
    DoubleValueDao that = (DoubleValueDao) o;
    return Double.compare(that.value, value) == 0 &&
        Double.compare(that.standardDeviation, standardDeviation) == 0 &&
        units == that.units;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, standardDeviation, units);
  }

  @Override
  public String toString() {
    return "DoubleValueDao{" +
        "value=" + value +
        ", standardDeviation=" + standardDeviation +
        ", units=" + units +
        '}';
  }
}
