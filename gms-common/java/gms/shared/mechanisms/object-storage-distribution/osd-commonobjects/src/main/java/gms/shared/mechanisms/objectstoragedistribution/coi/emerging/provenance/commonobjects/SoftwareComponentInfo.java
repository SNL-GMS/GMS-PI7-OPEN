package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects;

import java.util.Objects;
import org.apache.commons.lang3.Validate;

/**
 * Define a class to represent the software component which was used to create the
 * associated information.
 */
public class SoftwareComponentInfo {

  private final String name;
  private final String version;

  public static final SoftwareComponentInfo DEFAULT
      = new SoftwareComponentInfo("Default name", "Default version");

  public SoftwareComponentInfo(String name, String version)
      throws NullPointerException, IllegalArgumentException {

    Validate.notBlank(name);
    Validate.notBlank(version);
    this.name = name;
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SoftwareComponentInfo)) {
      return false;
    }
    SoftwareComponentInfo that = (SoftwareComponentInfo) o;

    return Objects.equals(this.getName(), that.getName()) &&
        Objects.equals(this.getVersion(), that.getVersion());
  }

  @Override
  public final int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (version != null ? version.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SoftwareComponentInfo{" +
        "name='" + name + '\'' +
        ", version='" + version + '\'' +
        '}';
  }
}
