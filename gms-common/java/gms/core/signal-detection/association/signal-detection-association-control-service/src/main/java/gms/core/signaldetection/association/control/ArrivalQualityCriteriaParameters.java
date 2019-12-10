package gms.core.signaldetection.association.control;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterionDefinition;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import java.util.Objects;
import java.util.Optional;

@AutoValue
public abstract class ArrivalQualityCriteriaParameters {
  public abstract Optional<PluginInfo> getPluginInfo();

  public abstract Optional<ArrivalQualityEventCriterionDefinition> getArrivalQualityEventCriterionDefinition();

  @JsonCreator
  public static ArrivalQualityCriteriaParameters create(
      @JsonProperty("pluginInfo") PluginInfo pluginInfo,
      @JsonProperty("weightedEventCriteriaCalculationDefinition") ArrivalQualityEventCriterionDefinition definition
  ) {
    return new AutoValue_ArrivalQualityCriteriaParameters(Optional.ofNullable(pluginInfo),
        Optional.ofNullable(definition)
    );
  }

  public static ArrivalQualityCriteriaParameters create(
      PluginInfo pluginInfo
  ) {
    Objects.requireNonNull(pluginInfo, "Null pluginInfo");

    return new AutoValue_ArrivalQualityCriteriaParameters(Optional.of(pluginInfo),
        Optional.empty());
  }

  public static ArrivalQualityCriteriaParameters create(
      ArrivalQualityEventCriterionDefinition signalDetectionAssociatorDefinition
  ) {
    Objects.requireNonNull(signalDetectionAssociatorDefinition,
        "Null weightedEventCriteriaCalculationDefinition");

    return new AutoValue_ArrivalQualityCriteriaParameters(Optional.empty(),
        Optional.of(signalDetectionAssociatorDefinition));
  }

}
