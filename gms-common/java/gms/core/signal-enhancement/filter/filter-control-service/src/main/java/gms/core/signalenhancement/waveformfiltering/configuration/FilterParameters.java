package gms.core.signalenhancement.waveformfiltering.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;

@AutoValue
public abstract class FilterParameters {

  public abstract String getPluginName();

  public abstract ImmutableMap<String, Object> getPluginParameters();

  @JsonCreator
  public static FilterParameters from(
      @JsonProperty("pluginName") String pluginName,
      @JsonProperty("pluginParameters") Map<String, Object> pluginParameters) {

    return new AutoValue_FilterParameters(pluginName, ImmutableMap.copyOf(pluginParameters));
  }
}
