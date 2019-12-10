package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;


import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 * A class to represent a GMS reference site.
 *
 */
public final class ReferenceSite {

  private final UUID entityId;
  private final UUID versionId;
  private final String name;
  private final String description;
  private final String comment;
  private final InformationSource source;
  private final double latitude;
  private final double longitude;
  private final double elevation;
  private final Instant actualChangeTime;
  private final Instant systemChangeTime;
  private final RelativePosition position;
  private final List<ReferenceAlias> aliases;

  /**
   * Create a new ReferenceSite object.
   *
   * @param name The site name.
   * @param description A description of the site.
   * @param source The source of the information.
   * @param comment Information about the site.
   * @param latitude Site's latitude.
   * @param longitude Site's longitude.
   * @param elevation Site's elevation.
   * @param actualChangeTime Date-time when the information was created.
   * @param systemChangeTime The date and time time the information was entered into the system
   * @param position The relative position.
   * @param aliases A list of aliases for this site.
   * @return A ReferenceSite object.
   */
  public static ReferenceSite create(String name, String description, InformationSource source,
      String comment, double latitude, double longitude, double elevation, Instant actualChangeTime,
      Instant systemChangeTime, RelativePosition position, List<ReferenceAlias> aliases) {

    return new ReferenceSite(name, description,
        source, comment, latitude, longitude,
        elevation, actualChangeTime, systemChangeTime, position, aliases);

  }

  /**
   * Create a ReferenceSite object from existing information.
   *
   * @param entityId the id of the entity
   * @param versionId the version id
   * @param name The site name.
   * @param description A description of the site.
   * @param comment Information about the site.
   * @param source The source of the information.
   * @param latitude Site's latitude.
   * @param longitude Site's longitude.
   * @param elevation Site's elevation.
   * @param actualChangeTime Date-time when the information was created.
   * @param systemChangeTime Date-time when the information was added to GMS.
   * @param position The relative position.
   * @param aliases A list of aliases for this site.
   * @return A ReferenceSite object.
   */

  public static ReferenceSite from(UUID entityId, UUID versionId, String name, String description,
      InformationSource source, String comment, double latitude,
      double longitude, double elevation, Instant actualChangeTime, Instant systemChangeTime,
      RelativePosition position, List<ReferenceAlias> aliases) {

    Objects.requireNonNull(entityId);
    Objects.requireNonNull(versionId);
    Validate.notEmpty(name);
    Objects.requireNonNull(actualChangeTime);
    final UUID expectedEntityId = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_16LE));
    Validate.isTrue(expectedEntityId.equals(entityId),
        "Expected entityId to be " + expectedEntityId + " for name " + name);
    final UUID expectedVersionId = UUID.nameUUIDFromBytes(
        (name + latitude + longitude + elevation + actualChangeTime)
            .getBytes(StandardCharsets.UTF_16LE));
    Validate.isTrue(expectedVersionId.equals(versionId),
        "Expected versionId to be " + expectedVersionId + " for other attributes");

    return new ReferenceSite(entityId, versionId, name, description, source, comment, latitude, longitude,
        elevation, actualChangeTime, systemChangeTime, position, aliases);

  }

  private ReferenceSite(String name, String description,
      InformationSource source, String comment, double latitude,
      double longitude, double elevation, Instant actualChangeTime, Instant systemChangeTime,
      RelativePosition position, List<ReferenceAlias> aliases) {

    Validate.notEmpty(name);
    this.name = name.trim();
    this.description = Objects.requireNonNull(description);
    this.comment = Objects.requireNonNull(comment);
    this.source = Objects.requireNonNull(source);
    this.latitude = latitude;
    this.longitude = longitude;
    this.elevation = elevation;
    this.actualChangeTime = Objects.requireNonNull(actualChangeTime);
    this.systemChangeTime = Objects.requireNonNull(systemChangeTime);
    this.position = Objects.requireNonNull(position);
    this.aliases = Objects.requireNonNull(aliases);
    this.entityId = UUID.nameUUIDFromBytes(this.name.getBytes(StandardCharsets.UTF_16LE));
    this.versionId = UUID.nameUUIDFromBytes(
        (this.name + this.latitude
            + this.longitude + this.elevation + this.actualChangeTime)
            .getBytes(StandardCharsets.UTF_16LE));
  }

  /**
   * Private constructor.
   *
   * @param entityId the id of the entity
   * @param versionId the version id
   * @param name The site name.
   * @param description A description of the site.
   * @param comment Information about the site.
   * @param source The source of the information.
   * @param latitude Site's latitude.
   * @param longitude Site's longitude.
   * @param elevation Site's elevation.
   * @param actualChangeTime Date-time when the information was created.
   * @param systemChangeTime Date-time when the information was added to GMS.
   * @param position The relative position.
   * @param aliases A list of aliases for this site.
   * @return A ReferenceSite object.   */
  private ReferenceSite(UUID entityId, UUID versionId, String name, String description,
      InformationSource source, String comment, double latitude,
      double longitude, double elevation, Instant actualChangeTime, Instant systemChangeTime,
      RelativePosition position, List<ReferenceAlias> aliases) {

    this.entityId = Objects.requireNonNull(entityId);
    this.versionId = Objects.requireNonNull(versionId);
    Validate.notEmpty(name);
    this.name = name.trim();
    this.description = Objects.requireNonNull(description);
    this.comment = Objects.requireNonNull(comment);
    this.source = Objects.requireNonNull(source);
    this.latitude = latitude;
    this.longitude = longitude;
    this.elevation = elevation;
    this.actualChangeTime = Objects.requireNonNull(actualChangeTime);
    this.systemChangeTime = Objects.requireNonNull(systemChangeTime);
    this.position = Objects.requireNonNull(position);
    this.aliases = Objects.requireNonNull(aliases);
  }

  /**
   * Add an alias to the list if it's not a duplicate.
   * @param alias
   */
  public void addAlias(ReferenceAlias alias) {
    this.aliases.add(alias);
  }

  public UUID getEntityId() {
    return entityId;
  }

  public UUID getVersionId() { return versionId; }

  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }

  public String getDescription() { return description; }

  public InformationSource getSource() {
    return source;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public double getElevation() {
    return elevation;
  }

  public Instant getActualChangeTime() {
    return actualChangeTime;
  }

  public Instant getSystemChangeTime() {
    return systemChangeTime;
  }

  public RelativePosition getPosition() {
    return position;
  }

  public List<ReferenceAlias> getAliases() {
    return aliases;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceSite that = (ReferenceSite) o;

    if (Double.compare(that.latitude, latitude) != 0) {
      return false;
    }
    if (Double.compare(that.longitude, longitude) != 0) {
      return false;
    }
    if (Double.compare(that.elevation, elevation) != 0) {
      return false;
    }
    if (entityId != null ? !entityId.equals(that.entityId) : that.entityId != null) {
      return false;
    }
    if (versionId != null ? !versionId.equals(that.versionId) : that.versionId != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (description != null ? !description.equals(that.description) : that.description != null) {
      return false;
    }
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) {
      return false;
    }
    if (source != null ? !source.equals(that.source) : that.source != null) {
      return false;
    }
    if (actualChangeTime != null ? !actualChangeTime.equals(that.actualChangeTime)
        : that.actualChangeTime != null) {
      return false;
    }
    if (systemChangeTime != null ? !systemChangeTime.equals(that.systemChangeTime)
        : that.systemChangeTime != null) {
      return false;
    }
    if (position != null ? !position.equals(that.position) : that.position != null) {
      return false;
    }
    return aliases != null ? aliases.equals(that.aliases) : that.aliases == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = entityId != null ? entityId.hashCode() : 0;
    result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (source != null ? source.hashCode() : 0);
    temp = Double.doubleToLongBits(latitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(longitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(elevation);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (actualChangeTime != null ? actualChangeTime.hashCode() : 0);
    result = 31 * result + (systemChangeTime != null ? systemChangeTime.hashCode() : 0);
    result = 31 * result + (position != null ? position.hashCode() : 0);
    result = 31 * result + (aliases != null ? aliases.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceSite{" +
        "entityId=" + entityId +
        ", versionId=" + versionId +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", comment='" + comment + '\'' +
        ", source=" + source +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", elevation=" + elevation +
        ", actualChangeTime=" + actualChangeTime +
        ", systemChangeTime=" + systemChangeTime +
        ", position=" + position +
        ", aliases=" + aliases +
        '}';
  }
}
