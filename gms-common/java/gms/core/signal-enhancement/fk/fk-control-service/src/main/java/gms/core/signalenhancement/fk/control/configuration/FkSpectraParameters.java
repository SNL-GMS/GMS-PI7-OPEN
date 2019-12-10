package gms.core.signalenhancement.fk.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class FkSpectraParameters {

  public abstract String getPluginName();

  public abstract FkSpectraDefinition getDefinition();

  public abstract UUID getOutputChannelId();

  @JsonCreator
  public static FkSpectraParameters from(
      @JsonProperty("pluginName") String pluginName,
      @JsonProperty("definition") FkSpectraDefinition definition,
      @JsonProperty("outputChannelId") UUID outputChannelId) {
    Preconditions.checkNotNull(pluginName);
    Preconditions.checkNotNull(definition);
    Preconditions.checkNotNull(outputChannelId);

    return new AutoValue_FkSpectraParameters(pluginName, definition, outputChannelId);
  }
}
