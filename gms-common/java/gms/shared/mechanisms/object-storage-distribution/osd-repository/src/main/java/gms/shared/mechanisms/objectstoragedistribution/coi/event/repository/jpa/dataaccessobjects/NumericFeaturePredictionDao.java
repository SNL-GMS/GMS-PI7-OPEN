package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.NumericMeasurementValueDao;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Entity;

@Entity(name = "feature_prediction_numeric_value")
public class NumericFeaturePredictionDao extends FeaturePredictionDao<NumericMeasurementValue> {

  private NumericMeasurementValueDao value;

  public NumericFeaturePredictionDao() {
  }

  public NumericFeaturePredictionDao(FeaturePrediction<NumericMeasurementValue> featurePrediction) {
    super(featurePrediction);

    Optional<NumericMeasurementValue> predictedValueOptional = featurePrediction.getPredictedValue();

    if (predictedValueOptional.isPresent()) {

      this.value = new NumericMeasurementValueDao(predictedValueOptional.get());
    } else {

      this.value = null;
    }
  }

  @Override
  Optional<NumericMeasurementValue> toCoiPredictionValue() {

    if (Objects.nonNull(this.value)) {

      return Optional.of(this.value.toCoi());
    } else {

      return Optional.empty();
    }
  }

  public NumericMeasurementValueDao getValue() {
    return value;
  }

  public void setValue(
      NumericMeasurementValueDao value) {
    this.value = value;
  }

  @Override
  public boolean update(FeaturePrediction<NumericMeasurementValue> updatedValue) {

    Optional<NumericMeasurementValue> predictedValueOptional = updatedValue.getPredictedValue();

    if (predictedValueOptional.isPresent()) {

      if (Objects.nonNull(this.value)) {

        return value.update(predictedValueOptional.get());
      } else {

        this.value = new NumericMeasurementValueDao(predictedValueOptional.get());
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
    NumericFeaturePredictionDao that = (NumericFeaturePredictionDao) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
