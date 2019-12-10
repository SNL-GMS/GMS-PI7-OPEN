package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStationMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="reference_station_membership")
public class ReferenceStationMembershipDao {
  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(unique = true)
  private UUID id;

  @Column(name="comment")
  private String comment;

  @Column(name="actual_time")
  private Instant actualTime;

  @Column(name="system_time")
  private Instant systemTime;

  @Column(name="station_id")
  private UUID stationId;

  @Column(name="site_id")
  private UUID siteId;

  @Column(name="status")
  private StatusType status;

  /**
   * Default constructor for JPA.
   */
  public ReferenceStationMembershipDao() {}

  /**
   * Create a DAO from the given COI.
   * @param membership The ReferenceStationMembership object.
   */
  public ReferenceStationMembershipDao(ReferenceStationMembership membership) {
    Objects.requireNonNull(membership);

    this.id = membership.getId();
    this.comment = membership.getComment();
    this.actualTime = membership.getActualChangeTime();
    this.systemTime = membership.getSystemChangeTime();
    this.stationId = membership.getStationId();
    this.siteId = membership.getSiteId();
    this.status = membership.getStatus();
  }

  /**
   * Create a COI from this DAO.
   * @return A ReferenceStationMembership object.
   */
  public ReferenceStationMembership toCoi() {
    return ReferenceStationMembership.from(getId(), getComment(), getActualTime(),
        getSystemTime(), getStationId(), getSiteId(), status);
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

  public UUID getStationId() {
    return stationId;
  }

  public void setStationId(UUID stationId) {
    this.stationId = stationId;
  }

  public UUID getSiteId() {
    return siteId;
  }

  public void setSiteId(UUID siteId) {
    this.siteId = siteId;
  }

  public StatusType getStatus() { return status; }

  public void setStatus( StatusType status) { this.status = status; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceStationMembershipDao that = (ReferenceStationMembershipDao) o;

    if (primaryKey != that.primaryKey) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) {
      return false;
    }
    if (actualTime != null ? !actualTime.equals(that.actualTime) : that.actualTime != null) {
      return false;
    }
    if (systemTime != null ? !systemTime.equals(that.systemTime) : that.systemTime != null) {
      return false;
    }
    if (stationId != null ? !stationId.equals(that.stationId) : that.stationId != null) {
      return false;
    }
    if (siteId != null ? !siteId.equals(that.siteId) : that.siteId != null) {
      return false;
    }
    return status == that.status;
  }

  @Override
  public int hashCode() {
    int result = (int) (primaryKey ^ (primaryKey >>> 32));
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (actualTime != null ? actualTime.hashCode() : 0);
    result = 31 * result + (systemTime != null ? systemTime.hashCode() : 0);
    result = 31 * result + (stationId != null ? stationId.hashCode() : 0);
    result = 31 * result + (siteId != null ? siteId.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceStationMembershipDao{" +
        "primaryKey=" + primaryKey +
        ", id=" + id +
        ", comment='" + comment + '\'' +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        ", stationId=" + stationId +
        ", siteId=" + siteId +
        ", status=" + status +
        '}';
  }
}
