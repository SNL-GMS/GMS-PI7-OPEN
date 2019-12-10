package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ReferenceDigitizerMembership {
  private final UUID id;
  private final String comment;
  private final Instant actualChangeTime;
  private final Instant systemChangeTime;
  private final UUID digitizerId;
  private final UUID channelId;
  private final StatusType status;

  /**
   * Create a new ReferenceDigitizerMembership object
   * @param comment Comments
   * @param actualChangeTime The date and time time the information was originally generated
   * @param systemChangeTime The date and time time the information was entered into the system
   * @param digitizerId The member digitizer's id
   * @param channelId The member channel's id
   * @param status
   * @return A new ReferenceDigitizerMembership object
   * @throws NullPointerException
   */
  public static ReferenceDigitizerMembership create(String comment, Instant actualChangeTime,
      Instant systemChangeTime, UUID digitizerId, UUID channelId,
      StatusType status) throws NullPointerException {
    return new ReferenceDigitizerMembership(comment, actualChangeTime,
        systemChangeTime, digitizerId, channelId, status);
  }

  /**
   * Create a ReferenceDigitizerMembership object from existing data
   * @param id The object's id
   * @param comment Comments
   * @param actualChangeTime The date and time time the information was originally generated
   * @param systemChangeTime The date and time time the information was entered into the system
   * @param digitizerId The member digitizer's id
   * @param channelId The member channel's id
   * @param status
   * @return A new ReferenceDigitizerMembership object from existing data
   * @throws NullPointerException
   */
  public static ReferenceDigitizerMembership from(UUID id, String comment,
      Instant actualChangeTime, Instant systemChangeTime,
      UUID digitizerId, UUID channelId,
      StatusType status) throws NullPointerException {
    return new ReferenceDigitizerMembership(id, comment, actualChangeTime, systemChangeTime,
        digitizerId, channelId, status);
  }

  private ReferenceDigitizerMembership(String comment, Instant actualTime,
      Instant systemTime, UUID digitizerId, UUID channelId,
      StatusType status) throws NullPointerException {
    this.comment = Objects.requireNonNull(comment);
    this.actualChangeTime = Objects.requireNonNull(actualTime);
    this.systemChangeTime = Objects.requireNonNull(systemTime);
    this.digitizerId = Objects.requireNonNull(digitizerId);
    this.channelId = Objects.requireNonNull(channelId);
    this.status = Objects.requireNonNull(status);
    this.id = UUID.nameUUIDFromBytes(
        (this.channelId.toString() + this.digitizerId
            + this.status + this.actualChangeTime).getBytes(StandardCharsets.UTF_16LE));
  }

  /**
   * Private constructor.
   * @param id The object's id
   * @param comment Comments
   * @param actualTime The date and time time the information was originally generated
   * @param systemTime The date and time time the information was entered into the system
   * @param digitizerId The member digitizer's id
   * @param channelId The member channel's id
   * @param status
   * @throws NullPointerException
   */
  private ReferenceDigitizerMembership(UUID id, String comment, Instant actualTime,
      Instant systemTime, UUID digitizerId, UUID channelId,
      StatusType status) throws NullPointerException {
    this.id = Objects.requireNonNull(id);
    this.comment = Objects.requireNonNull(comment);
    this.actualChangeTime = Objects.requireNonNull(actualTime);
    this.systemChangeTime = Objects.requireNonNull(systemTime);
    this.digitizerId = Objects.requireNonNull(digitizerId);
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

  public UUID getChannelId() {
    return channelId;
  }

  public UUID getDigitizerId() {
    return digitizerId;
  }

  public StatusType getStatus() { return status; }

  /**
   * Compare two membership records, if the digitizer ID and the channel ID match,
   * then they are effectively duplicates.
   * @param other
   * @return
   */
  public boolean isDuplicate(ReferenceDigitizerMembership other) {
    return (this.digitizerId == other.getDigitizerId() &&
        this.channelId == other.getChannelId());
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ReferenceDigitizerMembership that = (ReferenceDigitizerMembership) o;

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
    if (digitizerId != null ? !digitizerId.equals(that.digitizerId) : that.digitizerId != null) {
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
    result = 31 * result + (digitizerId != null ? digitizerId.hashCode() : 0);
    result = 31 * result + (channelId != null ? channelId.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ReferenceDigitizerMembership{" +
        "id=" + id +
        ", comment='" + comment + '\'' +
        ", actualChangeTime=" + actualChangeTime +
        ", systemChangeTime=" + systemChangeTime +
        ", digitizerId=" + digitizerId +
        ", channelId=" + channelId +
        ", status=" + status +
        '}';
  }
}
