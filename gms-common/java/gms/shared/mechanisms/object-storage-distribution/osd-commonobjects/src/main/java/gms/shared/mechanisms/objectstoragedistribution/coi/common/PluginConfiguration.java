package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;

@AutoValue
public abstract class PluginConfiguration {

  abstract ImmutableMap<String, Object> getParameters();

  public Optional<Object> getParameter(String key) {
    return Optional.ofNullable(getParameters().get(key));
  }

  public static PluginConfiguration from(Map<String, Object> parameters) {
    return builder().setParameters(parameters).build();
  }

  public static Builder builder() {
    return new AutoValue_PluginConfiguration.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    abstract Builder setParameters(ImmutableMap<String, Object> parameters);

    public Builder setParameters(Map<String, Object> parameters) {
      return setParameters(ImmutableMap.copyOf(parameters));
    }

    abstract ImmutableMap.Builder<String, Object> parametersBuilder();

    public Builder addParameter(String key, Object value) {
      parametersBuilder().put(key, value);
      return this;
    }

    public abstract PluginConfiguration build();
  }


}
