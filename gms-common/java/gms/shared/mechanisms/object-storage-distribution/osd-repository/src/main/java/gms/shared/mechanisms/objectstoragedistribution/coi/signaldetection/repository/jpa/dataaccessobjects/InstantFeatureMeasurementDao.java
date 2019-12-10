package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import java.util.Objects;
import javax.persistence.Entity;

@Entity(name = "feature_measurement_duration_value")
public class InstantFeatureMeasurementDao extends FeatureMeasurementDao<InstantValue> {

  private InstantValueDao value;

  public InstantFeatureMeasurementDao() {
  }

  public InstantFeatureMeasurementDao(FeatureMeasurement<InstantValue> featureMeasurement) {
    super(featureMeasurement);
    this.value = new InstantValueDao(featureMeasurement.getMeasurementValue());
  }

  @Override
  public InstantValue toCoiMeasurementValue() {
    return this.value.toCoi();
  }

  public InstantValueDao getValue() {
    return value;
  }

  public void setValue(
      InstantValueDao value) {
    this.value = value;
  }

  @Override
  public boolean update(FeatureMeasurement<InstantValue> featureMeasurement) {
    return value.update(featureMeasurement.getMeasurementValue());
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
    InstantFeatureMeasurementDao that = (InstantFeatureMeasurementDao) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }

  @Override
  public String toString() {
    return "InstantFeatureMeasurementDao{" +
        "value=" + value +
        '}';
  }
}
