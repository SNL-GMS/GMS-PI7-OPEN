package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.AmplitudeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import java.util.Objects;
import javax.persistence.Entity;

@Entity(name = "feature_measurement_amplitude_value")
public class AmplitudeFeatureMeasurementDao extends FeatureMeasurementDao<AmplitudeMeasurementValue> {

  private AmplitudeMeasurementValueDao value;

  public AmplitudeFeatureMeasurementDao() {}

  public AmplitudeFeatureMeasurementDao(FeatureMeasurement<AmplitudeMeasurementValue> fm) {
    super(fm);
    this.value = new AmplitudeMeasurementValueDao(fm.getMeasurementValue());
  }

  @Override
  public AmplitudeMeasurementValue toCoiMeasurementValue() {
    return this.value.toCoi();
  }

  public AmplitudeMeasurementValueDao getValue() {
    return value;
  }

  public void setValue(
      AmplitudeMeasurementValueDao value) {
    this.value = value;
  }

  @Override
  public boolean update(FeatureMeasurement<AmplitudeMeasurementValue> updated) {
    return false;
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
    AmplitudeFeatureMeasurementDao that = (AmplitudeFeatureMeasurementDao) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }

  @Override
  public String toString() {
    return "AmplitudeFeatureMeasurementDao{" +
        "value=" + value +
        '}';
  }

}
