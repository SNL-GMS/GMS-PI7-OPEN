package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects;

import static javax.persistence.CascadeType.ALL;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.InformationSourceDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.RelativePositionDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.apache.commons.lang3.Validate;

/**
 * Define a Data Access Object to allow read and write access to the relational database.
 */
@Entity
@Table(name = "reference_site")
public class ReferenceSiteDao {

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

  @Column(name = "latitude")
  private double latitude;

  @Column(name = "longitude")
  private double longitude;

  @Column(name = "elevation")
  private double elevation;

  @Column(name = "comment")
  private String comment;

  @Embedded
  private InformationSourceDao source;

  @Embedded
  private RelativePositionDao position;

  @Column(name = "actual_time")
  private Instant actualTime;

  @Column(name = "system_time")
  private Instant systemTime;

  @OneToMany(cascade = ALL)
  private List<ReferenceAliasDao> aliases;

  /**
   * Default constructor for JPA.
   */
  public ReferenceSiteDao() {
  }

  /**
   * Create a DAO from the COI object.
   *
   * @param site The ReferenceStation object.
   */
  public ReferenceSiteDao(ReferenceSite site) throws NullPointerException {
    Validate.notNull(site);
    this.entityId = site.getEntityId();
    this.versionId = site.getVersionId();
    this.name = site.getName();
    this.description = site.getDescription();
    this.source = new InformationSourceDao(site.getSource());
    this.comment = site.getComment();
    this.latitude = site.getLatitude();
    this.longitude = site.getLongitude();
    this.elevation = site.getElevation();
    this.actualTime = site.getActualChangeTime();
    this.systemTime = site.getSystemChangeTime();
    this.position = RelativePositionDao.from(site.getPosition());
    this.aliases = site.getAliases().stream().map(ReferenceAliasDao::new)
        .collect(Collectors.toList());
  }

  /**
   * Convert this DAO into its corresponding COI object.
   *
   * @return A ReferenceStation COI object.
   */
  public ReferenceSite toCoi() {
    return ReferenceSite.from(getEntityId(), getVersionId(),
        getName(), getDescription(), getSource().toCoi(),
        getComment(), getLatitude(), getLongitude(), getElevation(), getActualTime(),
        getSystemTime(), getPosition().toCoi(),
        getAliases().stream().map(ReferenceAliasDao::toCoi).collect(Collectors.toList()));
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

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getElevation() {
    return elevation;
  }

  public void setElevation(double elevation) {
    this.elevation = elevation;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public InformationSourceDao getSource() {
    return source;
  }

  public void setSource(InformationSourceDao source) {
    this.source = source;
  }

  public RelativePositionDao getPosition() {
    return position;
  }

  public void setPosition(
      RelativePositionDao position) {
    this.position = position;
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

  public List<ReferenceAliasDao> getAliases() {
    return this.aliases;
  }

  public void setAliases(List<ReferenceAliasDao> aliases) {
    this.aliases = aliases;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceSiteDao that = (ReferenceSiteDao) o;

//    if (primaryKey != that.primaryKey) {
//      return false;
//    }
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
    if (position != null ? !position.equals(that.position) : that.position != null) {
      return false;
    }
    if (actualTime != null ? !actualTime.equals(that.actualTime) : that.actualTime != null) {
      return false;
    }
    if (systemTime != null ? !systemTime.equals(that.systemTime) : that.systemTime != null) {
      return false;
    }
    return aliases != null ? aliases.equals(that.aliases) : that.aliases == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
    result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    temp = Double.doubleToLongBits(latitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(longitude);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(elevation);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (source != null ? source.hashCode() : 0);
    result = 31 * result + (position != null ? position.hashCode() : 0);
    result = 31 * result + (actualTime != null ? actualTime.hashCode() : 0);
    result = 31 * result + (systemTime != null ? systemTime.hashCode() : 0);
    result = 31 * result + (aliases != null ? aliases.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceSiteDao{" +
        "primaryKey=" + primaryKey +
        ", entityId=" + entityId +
        ", versionId=" + versionId +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", latitude=" + latitude +
        ", longitude=" + longitude +
        ", elevation=" + elevation +
        ", comment='" + comment + '\'' +
        ", source=" + source +
        ", position=" + position +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        ", aliases=" + aliases +
        '}';
  }
}
