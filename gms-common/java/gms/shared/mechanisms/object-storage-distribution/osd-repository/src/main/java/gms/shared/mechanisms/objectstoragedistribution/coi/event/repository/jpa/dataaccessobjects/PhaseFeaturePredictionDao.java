package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.PhaseTypeMeasurementValueDao;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Entity;

@Entity(name = "feature_prediction_phase")
public class PhaseFeaturePredictionDao extends FeaturePredictionDao<PhaseTypeMeasurementValue> {

  private PhaseTypeMeasurementValueDao value;

  public PhaseFeaturePredictionDao() {
  }

  public PhaseFeaturePredictionDao(FeaturePrediction<PhaseTypeMeasurementValue> fm) {
    super(fm);

    Optional<PhaseTypeMeasurementValue> predictedValueOptional = fm.getPredictedValue();

    if (predictedValueOptional.isPresent()) {

      this.value = new PhaseTypeMeasurementValueDao(predictedValueOptional.get());
    } else {

      this.value = null;
    }
  }

  @Override
  public Optional<PhaseTypeMeasurementValue> toCoiPredictionValue() {

    if (Objects.nonNull(this.value)) {

      return Optional.of(this.value.toCoi());
    } else {

      return Optional.empty();
    }
  }

  public PhaseTypeMeasurementValueDao getValue() {
    return value;
  }

  public void setValue(
      PhaseTypeMeasurementValueDao value) {
    this.value = value;
  }

  @Override
  public boolean update(FeaturePrediction<PhaseTypeMeasurementValue> updatedValue) {

    Optional<PhaseTypeMeasurementValue> predictedValueOptional = updatedValue.getPredictedValue();

    if (predictedValueOptional.isPresent()) {

      if (Objects.nonNull(this.value)) {

        return value.update(predictedValueOptional.get());
      } else {

        this.value = new PhaseTypeMeasurementValueDao(predictedValueOptional.get());
        return true;
      }
    } else {

      if (Objects.isNull(this.value)) {

        return false;
      } else {

        this.value = null;
        return true;
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PhaseFeaturePredictionDao that = (PhaseFeaturePredictionDao) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
