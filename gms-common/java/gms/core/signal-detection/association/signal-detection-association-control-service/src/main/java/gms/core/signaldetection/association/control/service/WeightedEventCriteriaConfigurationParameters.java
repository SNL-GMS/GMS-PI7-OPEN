package gms.core.signaldetection.association.control.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class WeightedEventCriteriaConfigurationParameters {
  public abstract String getName();

  public abstract String getVersion();

  public abstract double getPrimaryTimeWeight();

  public abstract double getSecondaryTimeWeight();

  public abstract double getArrayAzimuthWeight();

  public abstract double getThreeComponentAzimuthWeight();

  public abstract double getArraySlowWeight();

  public abstract double getThreeComponentSlowWeight();

  public abstract double getWeightThreshold();

  @JsonCreator
  public static WeightedEventCriteriaConfigurationParameters from(
      @JsonProperty("name") String name,
      @JsonProperty("version") String version,
      @JsonProperty("primaryTimeWeight") double primaryTimeWeight,
      @JsonProperty("secondaryTimeWeight") double secondaryTimeWeight,
      @JsonProperty("arrayAzimuthWeight") double arrayAzimuthWeight,
      @JsonProperty("threeComponentAzimuthWeight") double threeComponentAzimuthWeight,
      @JsonProperty("arraySlowWeight") double arraySlowWeight,
      @JsonProperty("threeComponentSlowWeight") double threeComponentSlowWeight,
      @JsonProperty("weightThreshold") double weightThreshold
  ) {
    return new AutoValue_WeightedEventCriteriaConfigurationParameters(
        name,
        version,
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
