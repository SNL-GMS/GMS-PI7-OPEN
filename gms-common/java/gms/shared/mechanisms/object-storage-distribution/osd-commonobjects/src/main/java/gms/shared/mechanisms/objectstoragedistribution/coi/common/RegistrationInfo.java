package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Objects;

/**
 * Plugin registration information that is used in the plugin registry
 */
@AutoValue
public abstract class RegistrationInfo {

  public abstract String getName();

  public abstract PluginVersion getVersion();

  /**
   * Checks registration information making sure the name or version is not null. If it is, it
   * throws an error and logs it in the logger
   *
   * @param name The name of the plugin
   * @param version The plugin version (Major:Minor:Patch)
   * @return New registration information object containing the name and version.
   */
  @JsonCreator
  public static RegistrationInfo from(
      @JsonProperty("name") String name,
      @JsonProperty("version") PluginVersion version) {
    Objects.requireNonNull(name,
        "Error instantiating RegistrationInfo, name cannot be null");
    Objects.requireNonNull(version,
        "Error instantiating RegistrationInfo, version cannot be null");

    return new AutoValue_RegistrationInfo(name, version);
  }

  public static RegistrationInfo create(String name, int major, int minor, int patch) {
    return from(name, PluginVersion.from(major, minor, patch));
  }
}
