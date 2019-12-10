package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ReferenceSiteMembership {
  private final UUID id;
  private final String comment;
  private final Instant actualChangeTime;
  private final Instant systemChangeTime;
  private final UUID siteId;
  private final UUID channelId;
  private final StatusType status;

  /**
   *
   * @param comment
   * @param actualChangeTime
   * @param systemChangeTime The date and time time the information was entered into the system
   * @param siteId
   * @param channelId
   * @param status
   * @return a new ReferenceSiteMembership object
   * @throws NullPointerException
   */
  public static ReferenceSiteMembership create(String comment, Instant actualChangeTime,
      Instant systemChangeTime, UUID siteId, UUID channelId,
      StatusType status) throws NullPointerException {

    return new ReferenceSiteMembership(comment, actualChangeTime,
        systemChangeTime, siteId, channelId, status);
  }

  /**
   *
   * @param id
   * @param comment
   * @param actualChangeTime
   * @param systemChangeTime
   * @param siteId
   * @param channelId
   * @param status
   * @return a new ReferenceSiteMembership object from existing data
   * @throws NullPointerException
   */
  public static ReferenceSiteMembership from(UUID id, String comment,
      Instant actualChangeTime, Instant systemChangeTime,
      UUID siteId, UUID channelId,
      StatusType status) throws NullPointerException {
    return new ReferenceSiteMembership(id, comment, actualChangeTime, systemChangeTime,
        siteId, channelId, status);
  }

  private ReferenceSiteMembership(String comment, Instant actualTime,
      Instant systemTime, UUID siteId, UUID channelId,
      StatusType status) throws NullPointerException {
    this.comment = Objects.requireNonNull(comment);
    this.actualChangeTime = Objects.requireNonNull(actualTime);
    this.systemChangeTime = Objects.requireNonNull(systemTime);
    this.siteId = Objects.requireNonNull(siteId);
    this.channelId = Objects.requireNonNull(channelId);
    this.status = Objects.requireNonNull(status);
    this.id = UUID.nameUUIDFromBytes(
        (this.siteId.toString() + this.channelId
            + this.status + this.actualChangeTime).getBytes(StandardCharsets.UTF_16LE));
  }

  private ReferenceSiteMembership(UUID id, String comment, Instant actualTime,
      Instant systemTime, UUID siteId, UUID channelId,
      StatusType status) throws NullPointerException {
    this.id = Objects.requireNonNull(id);
    this.comment = Objects.requireNonNull(comment);
    this.actualChangeTime = Objects.requireNonNull(actualTime);
    this.systemChangeTime = Objects.requireNonNull(systemTime);
    this.siteId = Objects.requireNonNull(siteId);
    this.channelId = Objects.requireNonNull(channelId);
    this.status = Objects.requireNonNull(status);
  }

  public UUID getId() {
    return id;
  }

  public String getComment() {
    return comment;
  }

  public Instant getActualChangeTime() {
    return actualChangeTime;
  }

  public Instant getSystemChangeTime() {
    return systemChangeTime;
  }

  public UUID getSiteId() {
    return siteId;
  }

  public UUID getChannelId() {
    return channelId;
  }

  public StatusType getStatus() { return status; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceSiteMembership that = (ReferenceSiteMembership) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) {
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
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (actualChangeTime != null ? actualChangeTime.hashCode() : 0);
    result = 31 * result + (systemChangeTime != null ? systemChangeTime.hashCode() : 0);
    result = 31 * result + (siteId != null ? siteId.hashCode() : 0);
    result = 31 * result + (channelId != null ? channelId.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceSiteMembership{" +
        "id=" + id +
        ", comment='" + comment + '\'' +
        ", actualChangeTime=" + actualChangeTime +
        ", systemChangeTime=" + systemChangeTime +
        ", siteId=" + siteId +
        ", channelId=" + channelId +
        ", status=" + status +
        '}';
  }
}
