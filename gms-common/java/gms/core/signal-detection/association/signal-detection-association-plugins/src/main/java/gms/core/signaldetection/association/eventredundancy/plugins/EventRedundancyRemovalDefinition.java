package gms.core.signaldetection.association.eventredundancy.plugins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class EventRedundancyRemovalDefinition {

  public abstract WeightedEventCriteriaCalculationDefinition getWeightedEventCriteriaCalculationDefinition();

  public abstract ArrivalQualityEventCriterionDefinition getArrivalQualityEventCriterionDefinition();


  @JsonCreator
  public static EventRedundancyRemovalDefinition create(
      @JsonProperty("weightedEventCriteriaCalculationDefinition")
          WeightedEventCriteriaCalculationDefinition weightedEventCriteriaCalculationDefinition,
      @JsonProperty("arrivalQualityEventCriterionDefinition")
          ArrivalQualityEventCriterionDefinition arrivalQualityEventCriterionDefinition
  ) {
    return new AutoValue_EventRedundancyRemovalDefinition(
        weightedEventCriteriaCalculationDefinition,
        arrivalQualityEventCriterionDefinition
    );
  }

}
