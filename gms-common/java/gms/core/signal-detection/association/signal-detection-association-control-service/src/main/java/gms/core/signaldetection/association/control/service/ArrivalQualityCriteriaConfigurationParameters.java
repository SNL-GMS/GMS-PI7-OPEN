package gms.core.signaldetection.association.control.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ArrivalQualityCriteriaConfigurationParameters {
  public abstract String getName();

  public abstract String getVersion();

  public abstract double getArrivalQualityAlpha();

  public abstract double getArrivalQualityBeta();

  public abstract double getArrivalQualityGamma();

  public abstract double getArrivalQualityThreshold();

  @JsonCreator
  public static ArrivalQualityCriteriaConfigurationParameters from(
      @JsonProperty("name") String name,
      @JsonProperty("version") String version,
      @JsonProperty("arrivalQualityAlpha") double arQualAlpha,
      @JsonProperty("arrivalQualityBeta") double arQualBeta,
      @JsonProperty("arrivalQualityGamma") double arQualGamma,
      @JsonProperty("arrivalQualityThreshold") double arQualThreshold
  ) {
    return new AutoValue_ArrivalQualityCriteriaConfigurationParameters(
        name,
        version,
        arQualAlpha,
        arQualBeta,
        arQualGamma,
        arQualThreshold
        );
  }

}
