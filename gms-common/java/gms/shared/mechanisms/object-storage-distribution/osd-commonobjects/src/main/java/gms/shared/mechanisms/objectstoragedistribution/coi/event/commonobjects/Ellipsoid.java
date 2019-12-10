package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.time.Duration;
import org.apache.commons.lang3.Validate;

@AutoValue
public abstract class Ellipsoid {

  public abstract ScalingFactorType getScalingFactorType();
  public abstract double getkWeight();
  public abstract double getConfidenceLevel();
  public abstract double getMajorAxisLength();
  public abstract double getMajorAxisTrend();
  public abstract double getMajorAxisPlunge();
  public abstract double getIntermediateAxisLength();
  public abstract double getIntermediateAxisTrend();
  public abstract double getIntermediateAxisPlunge();
  public abstract double getMinorAxisLength();
  public abstract double getMinorAxisTrend();
  public abstract double getMinorAxisPlunge();
  public abstract Duration getTimeUncertainty();

  /**
   * Creates an ellipsoid.
   * @param scalingFactorType
   * @param kWeight
   * @param confidenceLevel
   * @param majorAxisLength
   * @param majorAxisTrend
   * @param majorAxisPlunge
   * @param intermediateAxisLength
   * @param intermediateAxisTrend
   * @param intermediateAxisPlunge
   * @param minorAxisLength
   * @param minorAxisTrend
   * @param minorAxisPlunge
   * @param timeUncertainty
   *
   * @return A new Ellipsoid object.
   */
  @JsonCreator
  public static Ellipsoid from(
      @JsonProperty("scalingFactorType") ScalingFactorType scalingFactorType,
      @JsonProperty("kWeight") double kWeight,
      @JsonProperty("confidenceLevel") double confidenceLevel,
      @JsonProperty("majorAxisLength") double majorAxisLength,
      @JsonProperty("majorAxisTrend") double majorAxisTrend,
      @JsonProperty("majorAxisPlunge") double majorAxisPlunge,
      @JsonProperty("intermediateAxisLength") double intermediateAxisLength,
      @JsonProperty("intermediateAxisTrend") double intermediateAxisTrend,
      @JsonProperty("intermediateAxisPlunge") double intermediateAxisPlunge,
      @JsonProperty("minorAxisLength") double minorAxisLength,
      @JsonProperty("minorAxisTrend") double minorAxisTrend,
      @JsonProperty("minorAxisPlunge") double minorAxisPlunge,
      @JsonProperty("timeUncertainty") Duration timeUncertainty) {

    Validate.notNaN(kWeight);
    Validate.notNaN(confidenceLevel);
    Validate.notNaN(majorAxisLength);
    Validate.notNaN(majorAxisTrend);
    Validate.notNaN(majorAxisPlunge);
    Validate.notNaN(intermediateAxisLength);
    Validate.notNaN(intermediateAxisTrend);
    Validate.notNaN(intermediateAxisPlunge);
    Validate.notNaN(minorAxisLength);
    Validate.notNaN(minorAxisTrend);
    Validate.notNaN(minorAxisPlunge);

    Validate.isTrue(confidenceLevel >= 0.5 && confidenceLevel <= 1.0,
        "confidence level must be in range [0.5, 1]");
    if (scalingFactorType == ScalingFactorType.CONFIDENCE) {
      Validate.isTrue(kWeight == 0.0,
          "If scaling factor type is CONFIDENCE, kWeight must be 0.0");
    }
    else if (scalingFactorType == ScalingFactorType.COVERAGE) {
      Validate.isTrue(kWeight == Double.POSITIVE_INFINITY,
          "If scaling factor type is COVERAGE, kWeight must be infinity");
    }
    else if (scalingFactorType == ScalingFactorType.K_WEIGHTED) {
      Validate.isTrue(kWeight >= 0.0,
          "If scaling factor type is K_WEIGHTED, kWeight must be >= 0.0");
    }

    return new AutoValue_Ellipsoid(scalingFactorType, kWeight,
        confidenceLevel, majorAxisLength, majorAxisTrend,
        majorAxisPlunge, intermediateAxisLength, intermediateAxisTrend, intermediateAxisPlunge,
        minorAxisLength, minorAxisTrend, minorAxisPlunge, timeUncertainty);
  }
}
