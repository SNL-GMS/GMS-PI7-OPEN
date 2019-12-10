package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.FirstMotionMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import java.util.Objects;
import javax.persistence.Entity;

@Entity(name = "feature_measurement_first_motion")
public class FirstMotionFeatureMeasurementDao extends FeatureMeasurementDao<FirstMotionMeasurementValue> {

  private FirstMotionMeasurementValueDao value;

  public FirstMotionFeatureMeasurementDao() {}

  public FirstMotionFeatureMeasurementDao(FeatureMeasurement<FirstMotionMeasurementValue> fm) {
    super(fm);
    this.value = new FirstMotionMeasurementValueDao(fm.getMeasurementValue());
  }

  @Override
  public FirstMotionMeasurementValue toCoiMeasurementValue() {
    return this.value.toCoi();
  }

  public FirstMotionMeasurementValueDao getValue() {
    return value;
  }

  public void setValue(
      FirstMotionMeasurementValueDao value) {
    this.value = value;
  }

  @Override
  public boolean update(FeatureMeasurement<FirstMotionMeasurementValue> updatedValue) {
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
    FirstMotionFeatureMeasurementDao that = (FirstMotionFeatureMeasurementDao) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }

  @Override
  public String toString() {
    return "FirstMotionFeatureMeasurementDao{" +
        "value=" + value +
        '}';
  }
}
