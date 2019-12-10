package gms.core.signaldetection.signaldetectorcontrol.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

@AutoValue
public abstract class OnsetTimeUncertaintyParameters implements PluginParameters {

  @JsonCreator
  public static OnsetTimeUncertaintyParameters from(
      @JsonProperty("pluginName") String pluginName,
      @JsonProperty("pluginParameters") Map<String, Object> pluginParameters) {
    return new AutoValue_OnsetTimeUncertaintyParameters(pluginName,
        ImmutableMap.copyOf(pluginParameters));
  }
}
