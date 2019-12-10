package gms.core.signaldetection.association.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationDefinition;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import java.util.Objects;
import java.util.Optional;

@AutoValue
public abstract class WeightedEventCriteriaParameters {
  public abstract Optional<PluginInfo> getPluginInfo();

  public abstract Optional<WeightedEventCriteriaCalculationDefinition> getWeightedEventCriteriaCalculationDefinition();

  @JsonCreator
  public static WeightedEventCriteriaParameters create(
      @JsonProperty("pluginInfo") PluginInfo pluginInfo,
      @JsonProperty("weightedEventCriteriaCalculationDefinition") WeightedEventCriteriaCalculationDefinition definition
  ) {
    return new AutoValue_WeightedEventCriteriaParameters(Optional.ofNullable(pluginInfo),
        Optional.ofNullable(definition)
    );
  }

  public static WeightedEventCriteriaParameters create(
      PluginInfo pluginInfo
  ) {
    Objects.requireNonNull(pluginInfo, "Null pluginInfo");

    return new AutoValue_WeightedEventCriteriaParameters(Optional.of(pluginInfo),
        Optional.empty());
  }

  public static WeightedEventCriteriaParameters create(
      WeightedEventCriteriaCalculationDefinition signalDetectionAssociatorDefinition
  ) {
    Objects.requireNonNull(signalDetectionAssociatorDefinition,
        "Null weightedEventCriteriaCalculationDefinition");

    return new AutoValue_WeightedEventCriteriaParameters(Optional.empty(),
        Optional.of(signalDetectionAssociatorDefinition));
  }
}
