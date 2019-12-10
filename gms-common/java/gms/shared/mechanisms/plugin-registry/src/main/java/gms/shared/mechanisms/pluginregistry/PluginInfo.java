package gms.shared.mechanisms.pluginregistry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Objects;

/**
 * Plugin registration information that is used in the plugin registry
 */
@AutoValue
public abstract class PluginInfo {

  public abstract String getName();

  public abstract String getVersion();

  /**
   * Checks registration information making sure the name or version is not null. If it is, it
   * throws an error and logs it in the logger
   *
   * @param name The name of the plugin
   * @param version The plugin version (Major:Minor:Patch)
   * @return New registration information object containing the name and version.
   */
  @JsonCreator
  public static PluginInfo from(
      @JsonProperty("name") String name,
      @JsonProperty("version") String version
  ) {

    Objects.requireNonNull(name,
        "Error instantiating PluginInfo, name cannot be null");
    Objects.requireNonNull(version,
        "Error instantiating PluginInfo, version cannot be null");

    return new AutoValue_PluginInfo(name, version);
  }
}
