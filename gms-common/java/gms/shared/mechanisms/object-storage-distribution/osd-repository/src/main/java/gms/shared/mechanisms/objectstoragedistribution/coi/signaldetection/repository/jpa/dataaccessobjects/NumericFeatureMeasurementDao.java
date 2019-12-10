package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import java.util.Objects;
import javax.persistence.Entity;

@Entity(name = "feature_measurement_numeric_value")
public class NumericFeatureMeasurementDao extends FeatureMeasurementDao<NumericMeasurementValue> {

  private NumericMeasurementValueDao value;

  public NumericFeatureMeasurementDao() {
  }

  public NumericFeatureMeasurementDao(FeatureMeasurement<NumericMeasurementValue> fm) {
    super(fm);
    this.value = new NumericMeasurementValueDao(fm.getMeasurementValue());
  }

  @Override
  public NumericMeasurementValue toCoiMeasurementValue() {
    return this.value.toCoi();
  }

  public NumericMeasurementValueDao getValue() {
    return value;
  }

  public void setValue(
      NumericMeasurementValueDao value) {
    this.value = value;
  }

  @Override
  public boolean update(FeatureMeasurement<NumericMeasurementValue> updatedValue) {
    return value.update(updatedValue.getMeasurementValue());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    NumericFeatureMeasurementDao that = (NumericFeatureMeasurementDao) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }

  @Override
  public String toString() {
    return "NumericFeatureMeasurementDao{" +
        "value=" + value +
        '}';
  }
}
