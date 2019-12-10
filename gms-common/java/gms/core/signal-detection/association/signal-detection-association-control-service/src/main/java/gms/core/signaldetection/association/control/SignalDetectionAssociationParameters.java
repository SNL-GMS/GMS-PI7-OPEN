package gms.core.signaldetection.association.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorDefinition;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import java.util.Objects;
import java.util.Optional;

@AutoValue
public abstract class SignalDetectionAssociationParameters {

  public abstract Optional<PluginInfo> getPluginInfo();

  public abstract Optional<SignalDetectionAssociatorDefinition> getSignalDetectionAssociatorDefinition();

  @JsonCreator
  public static SignalDetectionAssociationParameters create(
      @JsonProperty("pluginInfo") PluginInfo pluginInfo,
      @JsonProperty("signalDetectionAssociatorDefinition") SignalDetectionAssociatorDefinition signalDetectionAssociatorDefinition
  ) {

    return new AutoValue_SignalDetectionAssociationParameters(Optional.ofNullable(pluginInfo),
        Optional.ofNullable(signalDetectionAssociatorDefinition));
  }

  public static SignalDetectionAssociationParameters create(
      PluginInfo pluginInfo
  ) {
    Objects.requireNonNull(pluginInfo, "Null pluginInfo");

    return new AutoValue_SignalDetectionAssociationParameters(Optional.of(pluginInfo),
        Optional.empty());
  }

  public static SignalDetectionAssociationParameters create(
      SignalDetectionAssociatorDefinition signalDetectionAssociatorDefinition
  ) {
    Objects.requireNonNull(signalDetectionAssociatorDefinition,
        "Nulll signalDetectionAssociatorDefinition");

    return new AutoValue_SignalDetectionAssociationParameters(Optional.empty(),
        Optional.of(signalDetectionAssociatorDefinition));
  }

}
