package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 * Defined in section 2.4.5 of Data Model v2.1 to represent a piece of Station SOH data as received
 * in a packet (such as from the CD-1.1 protocol). The StatusType class parameter is mostly commonly
 * Boolean (i.e. it is a status 'bit'), but is parameterized to support things like Floats (e.g. a
 * measure of the voltage to the station).
 */
public abstract class AcquiredChannelSoh<StatusType> {

  private final UUID id, channelId;
  private final AcquiredChannelSohType type;
  private final Instant startTime, endTime;
  private final StatusType status;
  private final CreationInfo creationInfo;

  /**
   * Creates a AcquiredChannelSoh omitting ID.
   *
   * @param channelId the id of the processing channel
   * @param type The state of health type that will be represented by all of the times and statuses
   * held by this class.
   * @param startTime the startTime for the status
   * @param endTime the endTime for the status
   * @param status the Status of the State-Of-Health (e.g. a boolean or a float or something)
   * @param creationInfo metadata about when this object was created and by what/whom.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public AcquiredChannelSoh(UUID channelId, AcquiredChannelSohType type,
      Instant startTime, Instant endTime, StatusType status, CreationInfo creationInfo) {

    this(UUID.randomUUID(), channelId, type, startTime, endTime, status, creationInfo);
  }

  /**
   * Creates a AcquiredChannelSoh given all params.
   *
   * @param id the identifier for this entity
   * @param channelId the id of the processing channel
   * @param type The state of health type that will be represented by all of the times and statuses
   * held by this class.
   * @param startTime the startTime for the status
   * @param endTime the endTime for the status
   * @param status the Status of the State-Of-Health (e.g. a boolean or a float or something)
   * @param creationInfo metadata about when this object was created and by what/whom.
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public AcquiredChannelSoh(UUID id, UUID channelId, AcquiredChannelSohType type,
      Instant startTime, Instant endTime, StatusType status, CreationInfo creationInfo) {

    Validate.isTrue(startTime.isBefore(endTime));

    this.id = Objects.requireNonNull(id);
    this.channelId = Objects.requireNonNull(channelId);
    this.type = Objects.requireNonNull(type);
    this.startTime = Objects.requireNonNull(startTime);
    this.endTime = Objects.requireNonNull(endTime);
    this.status = Objects.requireNonNull(status);
    this.creationInfo = Objects.requireNonNull(creationInfo);
  }

  public UUID getId() {
    return this.id;
  }

  public UUID getChannelId() {
    return this.channelId;
  }

  /**
   * See list of enum values in AcquiredChannelSohType
   */
  public AcquiredChannelSohType getType() {
    return this.type;
  }

  /**
   * Gets the startTime this SOH information is for.
   */
  public Instant getStartTime() {
    return this.startTime;
  }

  /**
   * @return Gets the startTime this SOH information is for.
   */
  public Instant getEndTime() {
    return this.endTime;
  }

  /**
   * Gets the StatusType for this SOH information
   */
  public StatusType getStatus() {
    return this.status;
  }

  /**
   * Gets the CreationInfo for this object.
   */
  public CreationInfo getCreationInfo() {
    return creationInfo;
  }

  /**
   * Compares the state of this object against another.
   *
   * @param otherSoh the object to compare against
   * @return true if this object and the provided one have the same state, i.e. their values are
   * equal except for entity ID.  False otherwise.
   */
  public boolean hasSameState(AcquiredChannelSoh otherSoh) {
    return otherSoh != null &&
        Objects.equals(this.getChannelId(), otherSoh.getChannelId()) &&
        Objects.equals(this.getType(), otherSoh.getType()) &&
        Objects.equals(this.getStartTime(), otherSoh.getStartTime()) &&
        Objects.equals(this.getEndTime(), otherSoh.getEndTime()) &&
        Objects.equals(this.getStatus(), otherSoh.getStatus()) &&
        Objects.equals(this.getCreationInfo(), otherSoh.getCreationInfo());
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj == null || !(obj instanceof AcquiredChannelSoh)) {
      return false;
    }
    AcquiredChannelSoh otherSoh = (AcquiredChannelSoh) obj;
    return Objects.equals(this.getId(), otherSoh.getId()) &&
        hasSameState(otherSoh);
  }

  @Override
  public final int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (channelId != null ? channelId.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
    result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (creationInfo != null ? creationInfo.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AcquiredChannelSoh{" +
        "id=" + id +
        ", channelId=" + channelId +
        ", type=" + type +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", status=" + status +
        ", creationInfo=" + creationInfo +
        '}';
  }

  /**
   * Enumeration defined in section 2.4.5 of Data Model v2.1
   */
  public enum AcquiredChannelSohType {
    AUTHENTICATION_SEAL_BROKEN,
    BACKUP_POWER_UNSTABLE,
    CALIBRATION_UNDERWAY,       // aka. CALIBRATION
    CLIPPED,
    CLOCK_DIFFERENTIAL_IN_MICROSECONDS_OVER_THRESHOLD,
    CLOCK_DIFFERENTIAL_TOO_LARGE,
    DATA_TIME_MINUS_TIME_LAST_GPS_SYNCHRONIZATION_OVER_THRESHOLD,
    DEAD_SENSOR_CHANNEL,
    DIGITIZER_ANALOG_INPUT_SHORTED,
    DIGITIZER_CALIBRATION_LOOP_BACK,
    DIGITIZING_EQUIPMENT_OPEN,
    EQUIPMENT_HOUSING_OPEN,     // aka. EQUIPMENT_OPEN
    EQUIPMENT_MOVED,
    GPS_RECEIVER_OFF,           // aka. GPS_OFF
    GPS_RECEIVER_UNLOCKED,
    MAIN_POWER_FAILURE,
    STATION_POWER_VOLTAGE,      // analog soh value
    VAULT_DOOR_OPENED,
    ZEROED_DATA
  }

}
