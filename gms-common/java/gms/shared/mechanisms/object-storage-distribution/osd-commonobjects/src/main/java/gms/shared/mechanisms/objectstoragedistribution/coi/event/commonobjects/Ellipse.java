package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.time.Duration;
import org.apache.commons.lang3.Validate;

@AutoValue
public abstract class Ellipse {

  public abstract ScalingFactorType getScalingFactorType();
  public abstract double getkWeight();
  public abstract double getConfidenceLevel();
  public abstract double getMajorAxisLength();
  public abstract double getMajorAxisTrend();
  public abstract double getMinorAxisLength();
  public abstract double getMinorAxisTrend();
  public abstract double getDepthUncertainty();
  public abstract Duration getTimeUncertainty();

  /**
   * Create an Ellipse from existing information.
   *
   * @return A new Ellipse object.
   */
  @JsonCreator
  public static Ellipse from(
      @JsonProperty("scalingFactorType") ScalingFactorType scalingFactorType,
      @JsonProperty("kWeight") double kWeight,
      @JsonProperty("confidenceLevel") double confidenceLevel,
      @JsonProperty("majorAxisLength") double majorAxisLength,
      @JsonProperty("majorAxisTrend") double majorAxisTrend,
      @JsonProperty("minorAxisLength") double minorAxisLength,
      @JsonProperty("minorAxisTrend") double minorAxisTrend,
      @JsonProperty("depthUncertainty") double depthUncertainty,
      @JsonProperty("timeUncertainty") Duration timeUncertainty) {

    Validate.notNaN(kWeight);
    Validate.notNaN(confidenceLevel);
    Validate.notNaN(majorAxisLength);
    Validate.notNaN(majorAxisTrend);
    Validate.notNaN(minorAxisLength);
    Validate.notNaN(minorAxisTrend);
    Validate.notNaN(depthUncertainty);

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

    return new AutoValue_Ellipse(scalingFactorType, kWeight, confidenceLevel,
        majorAxisLength, majorAxisTrend, minorAxisLength, minorAxisTrend,
        depthUncertainty, timeUncertainty);
  }
}
