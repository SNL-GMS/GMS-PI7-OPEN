package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 * Define a class which represents a network, which is a collection of monitoring stations.
 */
public final class ReferenceNetwork {

  private final UUID entityId;
  private final UUID versionId;
  private final String name;
  private final String description;
  private final NetworkOrganization organization;  // monitoring organization
  private final NetworkRegion region;        // geographic region
  private final String comment;
  private final InformationSource source;        // source of the information
  private final Instant actualChangeTime;  // time information was created
  private final Instant systemChangeTime;  // time the information was entered into the system

  /**
   * Create a new ReferenceNetwork object.
   *
   * @param name The name of the network.
   * @param org The monitoring organization.
   * @param region The geographic region.
   * @param source The source of the information.
   * @param comment Comments.
   * @param actualChangeTime The time when this information was created.
   * @param systemChangeTime The date and time time the information was entered into the system
   * @return A new ReferenceNetwork object.
   */
  public static ReferenceNetwork create(String name, String description, NetworkOrganization org,
      NetworkRegion region, InformationSource source, String comment,
      Instant actualChangeTime, Instant systemChangeTime)
      throws NullPointerException {

    return new ReferenceNetwork(name, description, org, region, source, comment,
        actualChangeTime, systemChangeTime);
  }

  /**
   * Create a ReferenceNetwork object from existing data.
   *
   * @param entityId The id of the entity.
   * @param versionId the id of the version of the entity
   * @param name The name of the network.
   * @param org The monitoring organization.
   * @param region The geographic region.
   * @param source The source of the information.
   * @param comment Comments.
   * @param actualChangeTime The time when this information was created.
   * @param systemChangeTime The time the information was added to the system.
   * @return A ReferenceNetwork object.
   */
  public static ReferenceNetwork from(UUID entityId, UUID versionId,
      String name, String description, NetworkOrganization org,
      NetworkRegion region, InformationSource source, String comment, Instant actualChangeTime,
      Instant systemChangeTime) throws NullPointerException {

    Objects.requireNonNull(entityId);
    Objects.requireNonNull(versionId);
    Objects.requireNonNull(org);
    Objects.requireNonNull(region);
    Objects.requireNonNull(actualChangeTime);
    Validate.notEmpty(name);
    final UUID expectedEntityId = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_16LE));
    Validate.isTrue(expectedEntityId.equals(entityId),
        "Expected entityId to be " + expectedEntityId + " for name " + name);
    final UUID expectedVersionId = UUID.nameUUIDFromBytes((
        name + org + region + actualChangeTime)
        .getBytes(StandardCharsets.UTF_16LE));
    Validate.isTrue(expectedVersionId.equals(versionId),
        "Expected versionId to be " + expectedVersionId + " for other attributes");

    return new ReferenceNetwork(entityId, versionId, name, description, org,
        region, source, comment, actualChangeTime, systemChangeTime);
  }

  private ReferenceNetwork(String name, String description,
      NetworkOrganization org, NetworkRegion region,
      InformationSource source, String comment, Instant actualChangeTime, Instant systemChangeTime)
      throws NullPointerException {

    Validate.notEmpty(name);
    this.name = name.trim();
    this.description = Objects.requireNonNull(description);
    this.organization = Objects.requireNonNull(org);
    this.region = Objects.requireNonNull(region);
    this.source = Objects.requireNonNull(source);
    this.comment = Objects.requireNonNull(comment);
    this.actualChangeTime = Objects.requireNonNull(actualChangeTime);
    this.systemChangeTime = Objects.requireNonNull(systemChangeTime);
    this.entityId = UUID.nameUUIDFromBytes(this.name.getBytes(StandardCharsets.UTF_16LE));
    this.versionId = UUID.nameUUIDFromBytes((
        this.name + this.organization + this.region + this.actualChangeTime)
        .getBytes(StandardCharsets.UTF_16LE));
  }

  private ReferenceNetwork(UUID entityId, UUID versionId, String name, String description,
      NetworkOrganization org, NetworkRegion region,
      InformationSource source, String comment, Instant actualChangeTime, Instant systemChangeTime)
      throws NullPointerException {

    this.entityId = Objects.requireNonNull(entityId);
    this.versionId = Objects.requireNonNull(versionId);
    Validate.notEmpty(name);
    this.name = name.trim();
    this.description = Objects.requireNonNull(description);
    this.organization = Objects.requireNonNull(org);
    this.region = Objects.requireNonNull(region);
    this.source = Objects.requireNonNull(source);
    this.comment = Objects.requireNonNull(comment);
    this.actualChangeTime = Objects.requireNonNull(actualChangeTime);
    this.systemChangeTime = Objects.requireNonNull(systemChangeTime);
  }

  public UUID getEntityId() {
    return entityId;
  }

  public UUID getVersionId() {
    return versionId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public NetworkOrganization getOrganization() {
    return organization;
  }

  public NetworkRegion getRegion() {
    return region;
  }

  public String getComment() {
    return comment;
  }

  public InformationSource getSource() {
    return source;
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

    ReferenceNetwork that = (ReferenceNetwork) o;

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
    if (organization != that.organization) {
      return false;
    }
    if (region != that.region) {
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
    return systemChangeTime != null ? systemChangeTime.equals(that.systemChangeTime)
        : that.systemChangeTime == null;
  }

  @Override
  public int hashCode() {
    int result = entityId != null ? entityId.hashCode() : 0;
    result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (organization != null ? organization.hashCode() : 0);
    result = 31 * result + (region != null ? region.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (source != null ? source.hashCode() : 0);
    result = 31 * result + (actualChangeTime != null ? actualChangeTime.hashCode() : 0);
    result = 31 * result + (systemChangeTime != null ? systemChangeTime.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceNetwork{" +
        "entityId=" + entityId +
        ", versionId=" + versionId +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", organization=" + organization +
        ", region=" + region +
        ", comment='" + comment + '\'' +
        ", source=" + source +
        ", actualChangeTime=" + actualChangeTime +
        ", systemChangeTime=" + systemChangeTime +
        '}';
  }
}
