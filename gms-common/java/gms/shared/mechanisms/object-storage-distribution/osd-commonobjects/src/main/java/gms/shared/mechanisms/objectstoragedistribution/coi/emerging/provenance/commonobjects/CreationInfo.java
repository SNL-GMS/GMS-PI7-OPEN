package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects;

import java.time.Instant;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/**
 * Represents information about how an object was created, such as when and by who/what. See section
 * 2.2.2 'Creation Info' of the GMS Conceptual Data Model document.
 */
public class CreationInfo {

  private final Instant creationTime;
  private final String creatorName;
  private final SoftwareComponentInfo softwareInfo;

  public static final CreationInfo DEFAULT = new CreationInfo("Default creator name",
      SoftwareComponentInfo.DEFAULT);


  /**
   * Constructor which will use the current system time for the creation time.
   *
   * @param creatorName The name of the entity creating the associated object.
   * @param softwareInfo The SoftwareComponentInfo object.
   */
  public CreationInfo(/*UUID id, */ String creatorName, SoftwareComponentInfo softwareInfo) {
    this(creatorName, Instant.now(), softwareInfo);
  }

  /**
   * Constructor to use when the object is created from a DAO, it will set the creation time to the
   * value that is obtained from the database.
   *
   * @param creatorName The name of the entity creating the associated object.
   * @param creationTime The time the associated object was created.
   * @param softwareInfo The SoftwareComponentInfo object.
   */
  public CreationInfo(String creatorName, Instant creationTime,
      SoftwareComponentInfo softwareInfo) {
    Validate.notBlank(creatorName);
    this.creatorName = creatorName;
    this.creationTime = Objects.requireNonNull(creationTime);
    this.softwareInfo = Objects.requireNonNull(softwareInfo);
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public SoftwareComponentInfo getSoftwareInfo() {
    return softwareInfo;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CreationInfo)) {
      return false;
    }

    CreationInfo that = (CreationInfo) o;

    return Objects.equals(this.getCreationTime(), that.getCreationTime()) &&
        Objects.equals(this.getCreatorName(), that.getCreatorName()) &&
        Objects.equals(this.getSoftwareInfo(), that.getSoftwareInfo());
  }

  @Override
  public final int hashCode() {
    int result = 31;
    result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
    result = 31 * result + (creatorName != null ? creatorName.hashCode() : 0);
    result = 31 * result + (softwareInfo != null ? softwareInfo.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "CreationInfo{" +
        "creationTime=" + creationTime +
        ", creatorName='" + creatorName + '\'' +
        ", softwareInfo=" + softwareInfo +
        '}';
  }
}
