package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.DurationMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import java.util.Objects;
import javax.persistence.Entity;

@Entity(name = "feature_measurement_duration_value")
public class DurationFeatureMeasurementDao extends FeatureMeasurementDao<DurationMeasurementValue> {

  private DurationMeasurementValueDao value;

  public DurationFeatureMeasurementDao() {
  }

  public DurationFeatureMeasurementDao(FeatureMeasurement<DurationMeasurementValue> featureMeasurement) {
    super(featureMeasurement);
    this.value = new DurationMeasurementValueDao(featureMeasurement.getMeasurementValue());
  }

  @Override
  public DurationMeasurementValue toCoiMeasurementValue() {
    return this.value.toCoi();
  }

  public DurationMeasurementValueDao getValue() {
    return value;
  }

  public void setValue(
      DurationMeasurementValueDao value) {
    this.value = value;
  }

  @Override
  public boolean update(FeatureMeasurement<DurationMeasurementValue> updatedValue) {
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
    DurationFeatureMeasurementDao that = (DurationFeatureMeasurementDao) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }

  @Override
  public String toString() {
    return "DurationFeatureMeasurementDao{" +
        "value=" + value +
        '}';
  }
}
