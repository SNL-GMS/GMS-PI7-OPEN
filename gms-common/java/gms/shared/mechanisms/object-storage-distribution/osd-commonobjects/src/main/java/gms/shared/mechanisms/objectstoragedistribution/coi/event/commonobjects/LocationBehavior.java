package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.UUID;


/**
 * Define a class for LocationBehavior for the processing results location solution.
 * TODO: residual could be calculated as the FeatureMeasurement - the FeaturePrediction.
 */
@AutoValue
public abstract class LocationBehavior {
  
  public abstract double getResidual();
  public abstract double getWeight();
  public abstract boolean isDefining();
  public abstract UUID getFeaturePredictionId();
  public abstract UUID getFeatureMeasurementId();

  /**
   * Define a LocationBehavior from known attributes.

   * TODO: describe parameters
   * @param residual The difference between the feature measurement and the prediction.
   * @param weight
   * @param isDefining
   * @param featurePredictionId Not null.
   * @param featureMeasurementId Not null.
   * @return A LocationBehavior object.
   */
  @JsonCreator
  public static LocationBehavior from(
      @JsonProperty("residual") double residual,
      @JsonProperty("weight") double weight,
      @JsonProperty("defining") boolean isDefining,
      @JsonProperty("featurePredictionId") UUID featurePredictionId,
      @JsonProperty("featureMeasurementId") UUID featureMeasurementId)  {
    return new AutoValue_LocationBehavior(
        residual,
        weight,
        isDefining,
        featurePredictionId,
        featureMeasurementId);
  }
}
