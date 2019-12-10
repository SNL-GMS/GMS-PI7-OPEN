package gms.core.waveformqc.waveformqccontrol.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Map;

@AutoValue
public abstract class QcParameters {

  public abstract String getPluginName();

  public abstract Map<String, Object> getPluginParams();

  @JsonCreator
  public static QcParameters from(
      @JsonProperty("pluginName") String pluginName,
      @JsonProperty("pluginParams") Map<String, Object> pluginParams) {

    return new AutoValue_QcParameters.Builder()
        .setPluginName(pluginName)
        .setPluginParams(pluginParams)
        .build();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setPluginName(String pluginName);

    public abstract Builder setPluginParams(Map<String, Object> pluginParams);

    abstract QcParameters build();
  }
}
