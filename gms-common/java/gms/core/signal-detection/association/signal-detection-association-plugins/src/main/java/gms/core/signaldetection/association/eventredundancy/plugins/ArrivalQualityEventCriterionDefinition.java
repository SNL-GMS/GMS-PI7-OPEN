package gms.core.signaldetection.association.eventredundancy.plugins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ArrivalQualityEventCriterionDefinition {

  public abstract double getArrivalQualityAlpha();

  public abstract double getArrivalQualityBeta();

  public abstract double getArrivalQualityGamma();

  public abstract double getArrivalQualityThreshold();

  @JsonCreator
  public static ArrivalQualityEventCriterionDefinition create(
      @JsonProperty("arrivalQualityAlpha") double arrivalQualityAlpha,
      @JsonProperty("arrivalQualityBeta") double arrivalQualityBeta,
      @JsonProperty("arrivalQualityGamma") double arrivalQualityGamma,
      @JsonProperty("arrivalQualityThreshold") double arrivalQualityThreshold
  ) {
    return new AutoValue_ArrivalQualityEventCriterionDefinition(
      arrivalQualityAlpha,
      arrivalQualityBeta,
      arrivalQualityGamma,
      arrivalQualityThreshold
    );
  }

}
