package gms.core.signaldetection.association.eventredundancy.plugins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class WeightedEventCriteriaCalculationDefinition {

  public abstract double getPrimaryTimeWeight();

  public abstract double getSecondaryTimeWeight();

  public abstract double getArrayAzimuthWeight();

  public abstract double getThreeComponentAzimuthWeight();

  public abstract double getArraySlowWeight();

  public abstract double getThreeComponentSlowWeight();

  public abstract double getWeightThreshold();

  @JsonCreator
  public static WeightedEventCriteriaCalculationDefinition create(
      @JsonProperty("primaryTimeWeight") double primaryTimeWeight,
      @JsonProperty("secondaryTimeWeight") double secondaryTimeWeight,
      @JsonProperty("arrayAzimuthWeight") double arrayAzimuthWeight,
      @JsonProperty("threeComponentAzimuthWeight") double threeComponentAzimuthWeight,
      @JsonProperty("arraySlowWeight") double arraySlowWeight,
      @JsonProperty("threeComponentSlowWeight") double threeComponentSlowWeight,
      @JsonProperty("weightThreshold") double weightThreshold
  ) {
    return new AutoValue_WeightedEventCriteriaCalculationDefinition(
        primaryTimeWeight,
        secondaryTimeWeight,
        arrayAzimuthWeight,
        threeComponentAzimuthWeight,
        arraySlowWeight,
        threeComponentSlowWeight,
        weightThreshold
    );
  }

}
