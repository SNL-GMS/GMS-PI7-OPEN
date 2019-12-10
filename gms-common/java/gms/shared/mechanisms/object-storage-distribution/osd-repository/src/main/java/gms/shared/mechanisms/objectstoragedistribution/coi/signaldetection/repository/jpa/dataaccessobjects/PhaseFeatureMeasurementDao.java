package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import java.util.Objects;
import javax.persistence.Entity;

@Entity(name = "feature_measurement_phase")
public class PhaseFeatureMeasurementDao extends FeatureMeasurementDao<PhaseTypeMeasurementValue> {

  private PhaseTypeMeasurementValueDao value;

  public PhaseFeatureMeasurementDao() {}

  public PhaseFeatureMeasurementDao(FeatureMeasurement<PhaseTypeMeasurementValue> fm) {
    super(fm);
    this.value = new PhaseTypeMeasurementValueDao(fm.getMeasurementValue());
  }

  @Override
  public PhaseTypeMeasurementValue toCoiMeasurementValue() {
    return this.value.toCoi();
  }

  public PhaseTypeMeasurementValueDao getValue() {
    return value;
  }

  public void setValue(
      PhaseTypeMeasurementValueDao value) {
    this.value = value;
  }

  @Override
  public boolean update(FeatureMeasurement<PhaseTypeMeasurementValue> updatedValue) {
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
    PhaseFeatureMeasurementDao that = (PhaseFeatureMeasurementDao) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }

  @Override
  public String toString() {
    return "PhaseFeatureMeasurementDao{" +
        "value=" + value +
        '}';
  }
}
