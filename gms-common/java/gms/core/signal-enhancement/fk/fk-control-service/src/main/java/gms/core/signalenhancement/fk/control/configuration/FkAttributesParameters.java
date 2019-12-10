package gms.core.signalenhancement.fk.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

@AutoValue
public abstract class FkAttributesParameters {

  public abstract String getPluginName();

  public abstract ImmutableMap<String, Object> getPluginParameters();

  @JsonCreator
  public static FkAttributesParameters from(
      @JsonProperty("pluginName") String pluginName,
      @JsonProperty("pluginParameters")Map<String, Object> pluginParameters) {
    return new AutoValue_FkAttributesParameters(pluginName,
        ImmutableMap.copyOf(pluginParameters));
  }

}
