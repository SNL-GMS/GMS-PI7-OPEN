package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ReferenceNetworkMembership {

  private final UUID id;
  private final String comment;
  private final Instant actualChangeTime;
  private final Instant systemChangeTime;
  private final UUID networkId;
  private final UUID stationId;
  private final StatusType status;

  /**
   * @return ReferenceNetworkMembership object
   */
  public static ReferenceNetworkMembership create(String comment, Instant actualChangeTime,
      Instant systemChangeTime, UUID networkId, UUID stationId, StatusType status)
      throws NullPointerException {

    return new ReferenceNetworkMembership(comment, actualChangeTime,
        systemChangeTime, networkId, stationId, status);
  }

  /**
   * @return a new ReferenceNetworkMembership object
   */
  public static ReferenceNetworkMembership from(UUID id, String comment,
      Instant actualChangeTime, Instant systemChangeTime,
      UUID networkId, UUID stationId, StatusType status) throws NullPointerException {
    return new ReferenceNetworkMembership(id, comment, actualChangeTime, systemChangeTime,
        networkId, stationId, status);
  }

  private ReferenceNetworkMembership(String comment, Instant actualTime,
      Instant systemTime, UUID networkId, UUID stationId, StatusType status)
      throws NullPointerException {

    this.comment = Objects.requireNonNull(comment);
    this.actualChangeTime = Objects.requireNonNull(actualTime);
    this.systemChangeTime = Objects.requireNonNull(systemTime);
    this.networkId = Objects.requireNonNull(networkId);
    this.stationId = Objects.requireNonNull(stationId);
    this.status = Objects.requireNonNull(status);
    this.id = UUID.nameUUIDFromBytes(
        (this.networkId.toString() + this.stationId
            + this.status + this.actualChangeTime).getBytes(StandardCharsets.UTF_16LE));
  }

  private ReferenceNetworkMembership(UUID id, String comment, Instant actualTime,
      Instant systemTime, UUID networkId, UUID stationId, StatusType status)
      throws NullPointerException {
    this.id = Objects.requireNonNull(id);
    this.comment = Objects.requireNonNull(comment);
    this.actualChangeTime = Objects.requireNonNull(actualTime);
    this.systemChangeTime = Objects.requireNonNull(systemTime);
    this.networkId = Objects.requireNonNull(networkId);
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

  public UUID getNetworkId() {
    return networkId;
  }

  public UUID getStationId() {
    return stationId;
  }

  public StatusType getStatus() {
    return status;
  }

  /**
   * Compare two membership records, if the network ID and the station ID match, then they are
   * effectively duplicates.
   */
  public boolean isDuplicate(ReferenceNetworkMembership other) {
    return (this.stationId == other.getStationId() &&
        this.networkId == other.getNetworkId());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceNetworkMembership that = (ReferenceNetworkMembership) o;

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
    if (networkId != null ? !networkId.equals(that.networkId) : that.networkId != null) {
      return false;
    }
    if (stationId != null ? !stationId.equals(that.stationId) : that.stationId != null) {
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
    result = 31 * result + (networkId != null ? networkId.hashCode() : 0);
    result = 31 * result + (stationId != null ? stationId.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceNetworkMembership{" +
        "id=" + id +
        ", comment='" + comment + '\'' +
        ", actualChangeTime=" + actualChangeTime +
        ", systemChangeTime=" + systemChangeTime +
        ", networkId=" + networkId +
        ", stationId=" + stationId +
        ", status=" + status +
        '}';
  }
}
