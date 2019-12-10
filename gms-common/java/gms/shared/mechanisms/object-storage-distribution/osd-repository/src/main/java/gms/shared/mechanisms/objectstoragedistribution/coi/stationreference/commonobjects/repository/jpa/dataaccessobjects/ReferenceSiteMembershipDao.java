package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSiteMembership;
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
@Table(name="reference_site_membership")
public class ReferenceSiteMembershipDao {
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

  @Column(name="site_id")
  private UUID siteId;

  @Column(name="channel_id")
  private UUID channelId;

  @Column(name="status")
  private StatusType status;

  /**
   * Default constructor for JPA.
   */
  public ReferenceSiteMembershipDao() {}

  /**
   * Create a DAO from the given COI.
   * @param membership The ReferenceSiteMembership object.
   */
  public ReferenceSiteMembershipDao(ReferenceSiteMembership membership) {
    Objects.requireNonNull(membership);

    this.id = membership.getId();
    this.comment = membership.getComment();
    this.actualTime = membership.getActualChangeTime();
    this.systemTime = membership.getSystemChangeTime();
    this.siteId = membership.getSiteId();
    this.channelId = membership.getChannelId();
    this.status = membership.getStatus();
  }

  /**
   * Create a COI from this DAO.
   * @return A ReferenceSiteMembership object.
   */
  public ReferenceSiteMembership toCoi() {
    return ReferenceSiteMembership.from(getId(), getComment(), getActualTime(),
        getSystemTime(), getSiteId(), getChannelId(), status);
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

  public UUID getSiteId() {
    return siteId;
  }

  public void setSiteId(UUID siteId) {
    this.siteId = siteId;
  }

  public UUID getChannelId() {
    return channelId;
  }

  public void setChannelId(UUID channelId) {
    this.channelId = channelId;
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

    ReferenceSiteMembershipDao that = (ReferenceSiteMembershipDao) o;

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
    if (siteId != null ? !siteId.equals(that.siteId) : that.siteId != null) {
      return false;
    }
    if (channelId != null ? !channelId.equals(that.channelId) : that.channelId != null) {
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
    result = 31 * result + (siteId != null ? siteId.hashCode() : 0);
    result = 31 * result + (channelId != null ? channelId.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceSiteMembershipDao{" +
        "primaryKey=" + primaryKey +
        ", id=" + id +
        ", comment='" + comment + '\'' +
        ", actualTime=" + actualTime +
        ", systemTime=" + systemTime +
        ", siteId=" + siteId +
        ", digitizerId=" + channelId +
        ", status=" + status +
        '}';
  }
}
