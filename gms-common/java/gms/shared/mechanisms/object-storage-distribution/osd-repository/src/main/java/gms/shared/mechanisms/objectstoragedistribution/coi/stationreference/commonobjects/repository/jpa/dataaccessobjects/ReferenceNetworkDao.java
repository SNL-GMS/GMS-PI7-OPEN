package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.InformationSourceDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "reference_network")
public class ReferenceNetworkDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(name = "entity_id")
  private UUID entityId;

  @Column(name = "version_id", unique = true)
  private UUID versionId;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "org")
  private NetworkOrganization organization;

  @Column(name = "region")
  private NetworkRegion region;

  @Embedded
  private InformationSourceDao source;

  @Column(name = "comment")
  private String comment;

  @Column(name = "actual_time")
  private Instant actualTime;

  @Column(name = "system_time")
  private Instant systemTime;

  /**
   * Default constructor for JPA.
   */
  public ReferenceNetworkDao() {
  }

  /**
   * Create a DAO from the corresponding COI object.
   *
   * @param referenceNetwork The ReferenceNetwork object.
   */
  public ReferenceNetworkDao(ReferenceNetwork referenceNetwork) throws NullPointerException {
    Objects.requireNonNull(referenceNetwork);
    this.name = referenceNetwork.getName();
    this.description = referenceNetwork.getDescription();
    this.entityId = referenceNetwork.getEntityId();
    this.versionId = referenceNetwork.getVersionId();
    this.organization = referenceNetwork.getOrganization();
    this.region = referenceNetwork.getRegion();
    this.source = new InformationSourceDao(referenceNetwork.getSource());
    this.comment = referenceNetwork.getComment();
    this.actualTime = referenceNetwork.getActualChangeTime();
    this.systemTime = referenceNetwork.getSystemChangeTime();
  }

  /**
   * Convert this DAO into a COI object.
   *
   * @return The ReferenceNetwork object.
   */
  public ReferenceNetwork toCoi() {
    return ReferenceNetwork.from(getEntityId(), getVersionId(), getName(),
        getDescription(), getOrganization(), getRegion(),
        getSource().toCoi(), getComment(), getActualTime(), getSystemTime());
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public void setEntityId(UUID id) {
    this.entityId = id;
  }

  public UUID getVersionId() {
    return versionId;
  }

  public void setVersionId(UUID id) {
    this.versionId = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public NetworkOrganization getOrganization() {
    return organization;
  }

  public void setOrganization(
      NetworkOrganization organization) {
    this.organization = organization;
  }

  public NetworkRegion getRegion() {
    return region;
  }

  public void setRegion(
      NetworkRegion region) {
    this.region = region;
  }

  public InformationSourceDao getSource() {
    return source;
  }

  public void setSource(InformationSourceDao source) {
    this.source = source;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Instant getActualTime() {
    return actualTime;
  }

  public void setActualTime(Instant actualTime) {
    this.actualTime = actualTime;
  }

  public Instant getSystemTime() {
    return systemTime;
  }

  public void setSystemTime(Instant systemTime) {
    this.systemTime = systemTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceNetworkDao that = (ReferenceNetworkDao) o;

    if (primaryKey != that.primaryKey) {
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
    if (organization != that.organization) {
      return false;
    }
    if (region != that.region) {
      return false;
    }
    if (source != null ? !source.equals(that.source) : that.source != null) {
      return false;
    }
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) {
      return false;
    }
    if (actualTime != null ? !actualTime.equals(that.actualTime) : that.actualTime != null) {
      return false;
    }
    return systemTime != null ? systemTime.equals(that.systemTime) : that.systemTime == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
    result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (organization != null ? organization.hashCode() : 0);
    result = 31 * result + (region != null ? region.hashCode() : 0);
    result = 31 * result + (source != null ? source.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (actualTime != null ? actualTime.hashCode() : 0);
    result = 31 * result + (systemTime != null ? systemTime.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceNetworkDao{" +
        "primaryKey=" + primaryKey +
        ", entityId=" + entityId +
        ", versionId=" + versionId +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", organization=" + organization +
        ", region=" + region +
        ", source=" + source +
        ", comment='" + comment + '\'' +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        '}';
  }
}
