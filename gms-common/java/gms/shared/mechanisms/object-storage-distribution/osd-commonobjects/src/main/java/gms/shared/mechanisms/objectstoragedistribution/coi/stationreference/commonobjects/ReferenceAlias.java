package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

public final class ReferenceAlias {

  private final UUID id;
  private final String name;
  private final StatusType status;
  private final String comment;
  private final Instant actualChangeTime;
  private final Instant systemChangeTime;

  /**
   * Create a new ReferenceSiteAlias.
   *
   * @param name The alias name.
   * @param status Status value, if null defaults to ACTIVE.
   * @param comment Comment about the alias.
   * @param actualChangeTime actual change time
   * @param systemChangeTime system change time
   * @return The new ReferenceSiteAlias object.
   */
  public static ReferenceAlias create(String name, StatusType status,
      String comment, Instant actualChangeTime, Instant systemChangeTime) {
    return new ReferenceAlias(UUID.randomUUID(), name, status, comment,
        actualChangeTime, systemChangeTime);
  }

  /**
   * Recreate a ReferenceSiteAlias from existing data.
   *
   * @param id The assigned UUID.
   * @param name The alias name.
   * @param status Status value.
   * @param comment Comment about the alias.
   * @param actualChangeTime The actual change time.
   * @param systemChangeTime The system change time.
   * @return The ReferenceSiteAlias object.
   */
  public static ReferenceAlias from(UUID id, String name, StatusType status,
      String comment, Instant actualChangeTime, Instant systemChangeTime) {
    return new ReferenceAlias(id, name, status, comment, actualChangeTime, systemChangeTime);
  }

  /**
   * Private constructor.
   * @param id The assigned UUID.
   * @param name The alias name.
   * @param status Status value.
   * @param comment Comment about the alias.
   * @param actualChangeTime The actual change time.
   * @param systemChangeTime The system change time.
   */
  protected ReferenceAlias(UUID id, String name, StatusType status, String comment,
      Instant actualChangeTime, Instant systemChangeTime) {
    this.id = Objects.requireNonNull(id);
    Validate.notEmpty(name);
    this.name = name.trim();
    this.status = Objects.requireNonNull(status);
    this.comment = Objects.requireNonNull(comment);
    this.actualChangeTime = Objects.requireNonNull(actualChangeTime);
    this.systemChangeTime = Objects.requireNonNull(systemChangeTime);
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public StatusType getStatus() { return status; }

  public String getComment() {
    return comment;
  }

  public Instant getActualChangeTime() {
    return actualChangeTime;
  }

  public Instant getSystemChangeTime() {
    return systemChangeTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceAlias that = (ReferenceAlias) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (status != that.status) {
      return false;
    }
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) {
      return false;
    }
    if (actualChangeTime != null ? !actualChangeTime.equals(that.actualChangeTime)
        : that.actualChangeTime != null) {
      return false;
    }
    return systemChangeTime != null ? systemChangeTime.equals(that.systemChangeTime)
        : that.systemChangeTime == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (actualChangeTime != null ? actualChangeTime.hashCode() : 0);
    result = 31 * result + (systemChangeTime != null ? systemChangeTime.hashCode() : 0);
    return result;
  }
}
