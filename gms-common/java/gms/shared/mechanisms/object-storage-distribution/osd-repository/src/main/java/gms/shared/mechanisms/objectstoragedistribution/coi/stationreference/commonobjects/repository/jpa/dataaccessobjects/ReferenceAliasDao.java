package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceAlias;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="reference_alias")
public class ReferenceAliasDao implements Serializable {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(name="id", unique = true)
  private UUID id;

  @Column(name = "name")
  private String name;

  @Column(name = "status")
  private StatusType status;

  @Column(name = "comment")
  private String comment;

  @Column(name = "actual_time")
  private Instant actualTime;

  @Column(name = "system_time")
  private Instant systemTime;

  /**
   * Default constructor used by JPA.
   */
  public ReferenceAliasDao() {  }

  /**
   * Create a new DAO from the corresponding COI object.
   *
   * @param referenceAlias The COI object to use.
   */
  public ReferenceAliasDao(ReferenceAlias referenceAlias) {
    Objects.requireNonNull(referenceAlias);
    this.id = referenceAlias.getId();
    this.name = referenceAlias.getName();
    this.status = referenceAlias.getStatus();
    this.comment = referenceAlias.getComment();
    this.actualTime = referenceAlias.getActualChangeTime();
    this.systemTime = referenceAlias.getSystemChangeTime();
  }

  /**
   * Convert this DAO into a COI object.
   * @return A ReferenceAlias object.
   */
  public ReferenceAlias toCoi() {
    return ReferenceAlias.from(id, name, status, comment,
        actualTime, systemTime);
  }

  public long getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(long primaryKey) {
    this.primaryKey = primaryKey;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public StatusType getStatus() { return status; }

  public void setStatus( StatusType status) { this.status = status; }

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

    ReferenceAliasDao that = (ReferenceAliasDao) o;

    if (primaryKey != that.primaryKey) {
      return false;
    }
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
    if (actualTime != null ? !actualTime.equals(that.actualTime)
        : that.actualTime != null) {
      return false;
    }
    return systemTime != null ? systemTime.equals(that.systemTime)
        : that.systemTime == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (actualTime != null ? actualTime.hashCode() : 0);
    result = 31 * result + (systemTime != null ? systemTime.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceAliasDao{" +
        "primaryKey=" + primaryKey +
        ", id=" + id +
        ", name='" + name + '\'' +
        ", status=" + status +
        ", comment='" + comment + '\'' +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        '}';
  }
}
