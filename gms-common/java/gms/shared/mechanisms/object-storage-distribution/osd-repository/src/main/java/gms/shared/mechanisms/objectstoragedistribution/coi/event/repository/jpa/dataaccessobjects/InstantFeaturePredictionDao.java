package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.InstantValueDao;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Entity;

@Entity(name = "feature_prediction_duration_value")
public class InstantFeaturePredictionDao extends FeaturePredictionDao<InstantValue> {

  private InstantValueDao value;

  public InstantFeaturePredictionDao() {

  }

  InstantFeaturePredictionDao(FeaturePrediction<InstantValue> featurePrediction) {
    super(featurePrediction);

    Optional<InstantValue> predictedValueOptional = featurePrediction.getPredictedValue();

    if (predictedValueOptional.isPresent()) {

      this.value = new InstantValueDao(predictedValueOptional.get());
    } else {

      this.value = null;
    }
  }

  @Override
  Optional<InstantValue> toCoiPredictionValue() {

    if (Objects.nonNull(this.value)) {

      return Optional.of(this.value.toCoi());
    } else {

      return Optional.empty();
    }
  }

  public InstantValueDao getValue() {
    return value;
  }

  public void setValue(
      InstantValueDao value) {
    this.value = value;
  }

  @Override
  public boolean update(FeaturePrediction<InstantValue> featurePrediction) {

    Optional<InstantValue> predictedValueOptional = featurePrediction.getPredictedValue();

    if (predictedValueOptional.isPresent()) {

      if (Objects.nonNull(this.value)) {

        return value.update(predictedValueOptional.get());
      } else {

        this.value = new InstantValueDao(predictedValueOptional.get());
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
}
