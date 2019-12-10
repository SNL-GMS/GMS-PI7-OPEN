package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Objects;

/**
 * Creates the major, minor, and patch version for current plugin. It then checks to see if version
 * created matches the plugin version
 */
@AutoValue
public abstract class PluginVersion {

  public abstract Integer getMajor();

  public abstract Integer getMinor();

  public abstract Integer getPatch();

  /**
   * Checks that the plugin versions (Major, Minor, Patch). If any of the values are null it throws
   * an error and logs it in the logger.
   *
   * @param major The major version
   * @param minor The minor version
   * @param patch The patch version
   */
  @JsonCreator
  public static PluginVersion from(
      @JsonProperty("major") Integer major,
      @JsonProperty("minor") Integer minor,
      @JsonProperty("patch") Integer patch) {
    Objects.requireNonNull(major,
        "Error instantiating plugin version, major value cannot be null.");
    Objects.requireNonNull(minor,
        "Error instantiating plugin version, minor value cannot be null.");
    Objects.requireNonNull(patch,
        "Error instantiating plugin version, patch value cannot be null.");

    return new AutoValue_PluginVersion(major, minor, patch);
  }
}
