package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.*;

/**
 * Represents a frame of data from a station; could be received via various protocols. It includes
 * the start/end time of the data, a reference by ID to the channel the data is for, the time it was
 * received, a raw payload (bytes) - this represents the whole raw frame, and the status of its
 * authentication.
 */
public final class RawStationDataFrame {

  private final UUID id;
  private final UUID stationId;
  private final Set<UUID> channelIds;
  private final AcquisitionProtocol acquisitionProtocol;
  private final Instant payloadDataStartTime;
  private final Instant payloadDataEndTime;
  private final Instant receptionTime;
  private final byte[] rawPayload;
  private final AuthenticationStatus authenticationStatus;
  private final CreationInfo creationInfo;

  /**
   * Enum for the status of authentication of a frame.
   */
  public enum AuthenticationStatus {
    NOT_APPLICABLE,
    AUTHENTICATION_FAILED,
    AUTHENTICATION_SUCCEEDED,
    NOT_YET_AUTHENITCATED
  }

  /**
   * Creates a RawStationDataFrame given all fields.
   *
   * @param id the identifier of the frame
   * @param stationId the station ID
   * @param channelIds the channel IDs
   * @param acquisitionProtocol acquisition protocol
   * @param payloadDataStartTime start time of the payload data
   * @param payloadDataEndTime end time of the payload data
   * @param receptionTime the time the frame was received
   * @param rawPayload the raw data of the frame
   * @param authenticationStatus the status of authentication for the frame
   * @param creationInfo provenance info about the frame
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  private RawStationDataFrame(UUID id, UUID stationId, Set<UUID> channelIds, AcquisitionProtocol acquisitionProtocol,
                              Instant payloadDataStartTime, Instant payloadDataEndTime,
                              Instant receptionTime, byte[] rawPayload,
                              AuthenticationStatus authenticationStatus, CreationInfo creationInfo) {

    Validate.isTrue(payloadDataEndTime.isAfter(payloadDataStartTime));
    Validate.notEmpty(channelIds);
    this.id = Objects.requireNonNull(id);
    this.stationId = Objects.requireNonNull(stationId);
    this.channelIds = Objects.requireNonNull(Collections.unmodifiableSet(channelIds));
    this.acquisitionProtocol = Objects.requireNonNull(acquisitionProtocol);
    this.payloadDataStartTime = Objects.requireNonNull(payloadDataStartTime);
    this.payloadDataEndTime = Objects.requireNonNull(payloadDataEndTime);
    this.receptionTime = Objects.requireNonNull(receptionTime);
    this.rawPayload = Objects.requireNonNull(rawPayload);
    this.authenticationStatus = Objects.requireNonNull(authenticationStatus);
    this.creationInfo = Objects.requireNonNull(creationInfo);
  }

  /**
   * Creates a RawStationDataFrame anew, generating an ID.
   *
   * @param stationId the station ID
   * @param channelIds the channel IDs
   * @param acquisitionProtocol acquisition protocol
   * @param payloadDataStartTime start time of the payload data
   * @param payloadDataEndTime end time of the payload data
   * @param receptionTime the time the frame was received
   * @param rawPayload the raw data of the frame
   * @param authenticationStatus the status of authentication for the frame
   * @param creationInfo provenance info about the frame
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static RawStationDataFrame create(UUID stationId, Set<UUID> channelIds,
                                           AcquisitionProtocol acquisitionProtocol,
                                           Instant payloadDataStartTime,
                                           Instant payloadDataEndTime, Instant receptionTime, byte[] rawPayload,
                                           AuthenticationStatus authenticationStatus, CreationInfo creationInfo) {

    return new RawStationDataFrame(UUID.randomUUID(), stationId, channelIds, acquisitionProtocol,
            payloadDataStartTime, payloadDataEndTime,
        receptionTime, rawPayload, authenticationStatus, creationInfo);
  }

  /**
   * Recreates a RawStationDataFrame with the provided id
   *
   * @param stationId the station ID
   * @param channelIds the channel IDs
   * @param acquisitionProtocol acquisition protocol
   * @param payloadDataStartTime start time of the payload data
   * @param payloadDataEndTime end time of the payload data
   * @param receptionTime the time the frame was received
   * @param rawPayload the raw data of the frame
   * @param authenticationStatus the status of authentication for the frame
   * @param creationInfo provenance info about the frame
   * @throws NullPointerException if any arg is null
   * @throws IllegalArgumentException if string arg is empty
   */
  public static RawStationDataFrame from(UUID id, UUID stationId, Set<UUID> channelIds,
                                         AcquisitionProtocol acquisitionProtocol,
                                         Instant payloadDataStartTime,
                                         Instant payloadDataEndTime, Instant receptionTime, byte[] rawPayload,
                                         AuthenticationStatus authenticationStatus, CreationInfo creationInfo) {


    return new RawStationDataFrame(id, stationId, channelIds, acquisitionProtocol,
            payloadDataStartTime, payloadDataEndTime,
            receptionTime, rawPayload, authenticationStatus, creationInfo);
  }

  public UUID getId() {
    return id;
  }

  public UUID getStationId() {
    return stationId;
  }

  public Set<UUID> getChannelIds() {
    return channelIds;
  }

  public AcquisitionProtocol getAcquisitionProtocol() {
    return acquisitionProtocol;
  }


  public Instant getPayloadDataStartTime() {
    return payloadDataStartTime;
  }

  public Instant getPayloadDataEndTime() {
    return payloadDataEndTime;
  }

  public Instant getReceptionTime() {
    return receptionTime;
  }

  public byte[] getRawPayload() {
    return rawPayload;
  }

  public AuthenticationStatus getAuthenticationStatus() {
    return authenticationStatus;
  }

  public CreationInfo getCreationInfo() {
    return creationInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RawStationDataFrame that = (RawStationDataFrame) o;
    return Objects.equals(id, that.id) &&
            Objects.equals(stationId, that.stationId) &&
            Objects.equals(channelIds, that.channelIds) &&
            acquisitionProtocol == that.acquisitionProtocol &&
            Objects.equals(payloadDataStartTime, that.payloadDataStartTime) &&
            Objects.equals(payloadDataEndTime, that.payloadDataEndTime) &&
            Objects.equals(receptionTime, that.receptionTime) &&
            Arrays.equals(rawPayload, that.rawPayload) &&
            authenticationStatus == that.authenticationStatus &&
            Objects.equals(creationInfo, that.creationInfo);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(id, stationId, channelIds, acquisitionProtocol, payloadDataStartTime, payloadDataEndTime, receptionTime, authenticationStatus, creationInfo);
    result = 31 * result + Arrays.hashCode(rawPayload);
    return result;
  }

  @Override
  public String toString() {
    return "RawStationDataFrame{" +
            "id=" + id +
            ", stationId=" + stationId +
            ", channelIds=" + channelIds +
            ", acquisitionProtocol=" + acquisitionProtocol +
            ", payloadDataStartTime=" + payloadDataStartTime +
            ", payloadDataEndTime=" + payloadDataEndTime +
            ", receptionTime=" + receptionTime +
            ", rawPayload=" + Arrays.toString(rawPayload) +
            ", authenticationStatus=" + authenticationStatus +
            ", creationInfo=" + creationInfo +
            '}';
  }
}
