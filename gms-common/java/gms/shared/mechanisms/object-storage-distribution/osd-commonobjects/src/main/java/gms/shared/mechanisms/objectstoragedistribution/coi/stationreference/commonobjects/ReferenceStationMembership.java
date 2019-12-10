package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ReferenceStationMembership {

  private final UUID id;
  private final String comment;
  private final Instant actualChangeTime;
  private final Instant systemChangeTime;
  private final UUID stationId;
  private final UUID siteId;
  private final StatusType status;

  /**
   * @return a new ReferenceStationMembership object
   */
  public static ReferenceStationMembership create(String comment, Instant actualChangeTime,
      Instant systemChangeTime, UUID stationId, UUID siteId,
      StatusType status) throws NullPointerException {

    return new ReferenceStationMembership(comment, actualChangeTime,
        systemChangeTime, stationId, siteId, status);
  }

  /**
   * @return a new ReferenceStationMembership object from existing data
   */
  public static ReferenceStationMembership from(UUID id, String comment,
      Instant actualChangeTime, Instant systemChangeTime,
      UUID stationId, UUID siteId,
      StatusType status) throws NullPointerException {
    return new ReferenceStationMembership(id, comment, actualChangeTime, systemChangeTime,
        stationId, siteId, status);
  }

  private ReferenceStationMembership(String comment, Instant actualTime,
      Instant systemTime, UUID stationId, UUID siteId,
      StatusType status) throws NullPointerException {
    this.comment = Objects.requireNonNull(comment);
    this.actualChangeTime = Objects.requireNonNull(actualTime);
    this.systemChangeTime = Objects.requireNonNull(systemTime);
    this.siteId = Objects.requireNonNull(siteId);
    this.stationId = Objects.requireNonNull(stationId);
    this.status = Objects.requireNonNull(status);
    this.id = UUID.nameUUIDFromBytes(
        (this.stationId.toString() + this.siteId
            + this.status + this.actualChangeTime).getBytes(StandardCharsets.UTF_16LE));
  }

  private ReferenceStationMembership(UUID id, String comment, Instant actualTime,
      Instant systemTime, UUID stationId, UUID siteId,
      StatusType status) throws NullPointerException {
    this.id = Objects.requireNonNull(id);
    this.comment = Objects.requireNonNull(comment);
    this.actualChangeTime = Objects.requireNonNull(actualTime);
    this.systemChangeTime = Objects.requireNonNull(systemTime);
    this.siteId = Objects.requireNonNull(siteId);
    this.stationId = Objects.requireNonNull(stationId);
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

  public UUID getStationId() {
    return stationId;
  }

  public StatusType getStatus() {
    return status;
  }

  /**
   * Compare two membership records, if the site ID and the station ID match, then they are
   * effectively duplicates.
   */
  public boolean isDuplicate(ReferenceStationMembership other) {
    return (this.stationId == other.getStationId() &&
        this.siteId == other.getSiteId());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceStationMembership that = (ReferenceStationMembership) o;

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
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (actualChangeTime != null ? actualChangeTime.hashCode() : 0);
    result = 31 * result + (systemChangeTime != null ? systemChangeTime.hashCode() : 0);
    result = 31 * result + (stationId != null ? stationId.hashCode() : 0);
    result = 31 * result + (siteId != null ? siteId.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceStationMembership{" +
        "id=" + id +
        ", comment='" + comment + '\'' +
        ", actualChangeTime=" + actualChangeTime +
        ", systemChangeTime=" + systemChangeTime +
        ", stationId=" + stationId +
        ", siteId=" + siteId +
        ", status=" + status +
        '}';
  }
}
