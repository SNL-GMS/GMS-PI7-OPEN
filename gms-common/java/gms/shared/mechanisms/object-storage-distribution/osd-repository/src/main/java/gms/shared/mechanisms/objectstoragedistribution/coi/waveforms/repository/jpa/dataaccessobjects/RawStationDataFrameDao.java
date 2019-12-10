package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.CreationInfoDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import org.apache.commons.lang3.Validate;

import javax.persistence.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "raw_station_data_frame")
//    indexes = {@Index(columnList = "station_id, acquisition_protocol, payload_data_start_time")})
public class RawStationDataFrameDao {

  @Id
  @GeneratedValue
  private long primaryKey;

  @Column(unique = true)
  private UUID id;

  @Column(name = "station_id")
  private UUID stationId;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "channel_ids")
  private Set<UUID> channelIds;


  @Column(name = "acquisition_protocol")
  private AcquisitionProtocol acquisitionProtocol;

  @Column(nullable = false, name = "payload_data_start_time")
  private Instant payloadDataStartTime;

  @Column(nullable = false, name = "payload_data_end_time")
  private Instant payloadDataEndTime;

  @Column(nullable = false, name = "reception_time")
  private Instant receptionTime;

  @Lob
  @Column(nullable = false, name = "raw_payload_blob")
  private byte[] rawPayload;

  @Column(nullable = false, name = "authentication_status")
  private AuthenticationStatus authenticationStatus;

  @Embedded
  private CreationInfoDao creationInfo;

  /**
   * Default no-arg constructor (for use by JPA)
   */
  public RawStationDataFrameDao() {
  }


  public RawStationDataFrameDao(RawStationDataFrame df) {
    Validate.notNull(df);
    this.id = df.getId();
    this.stationId = df.getStationId();
    this.channelIds = df.getChannelIds();
    this.acquisitionProtocol = df.getAcquisitionProtocol();
    this.payloadDataStartTime = df.getPayloadDataStartTime();
    this.payloadDataEndTime = df.getPayloadDataEndTime();
    this.receptionTime = df.getReceptionTime();
    this.rawPayload = df.getRawPayload();
    this.authenticationStatus = df.getAuthenticationStatus();
    this.creationInfo = new CreationInfoDao(df.getCreationInfo());
  }

  public RawStationDataFrame toCoi() {
    return RawStationDataFrame.from(this.id, this.stationId, this.channelIds, this.acquisitionProtocol,
            this.payloadDataStartTime, this.payloadDataEndTime,
            this.receptionTime, this.rawPayload,
            this.authenticationStatus, this.creationInfo.toCoi());
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

  public UUID getStationId() {
    return stationId;
  }

  public void setStationId(UUID stationId) {
    this.stationId = stationId;
  }

  public Set<UUID> getChannelIds() {
    return channelIds;
  }

  public void setChannelIds(Set<UUID> channelIds) {
    this.channelIds = channelIds;
  }

  public AcquisitionProtocol getAcquisitionProtocol() {
    return acquisitionProtocol;
  }

  public void setAcquisitionProtocol(
          AcquisitionProtocol acquisitionProtocol) {
    this.acquisitionProtocol = acquisitionProtocol;
  }


  public Instant getPayloadDataStartTime() {
    return payloadDataStartTime;
  }

  public void setPayloadDataStartTime(Instant payloadDataStartTime) {
    this.payloadDataStartTime = payloadDataStartTime;
  }

  public Instant getPayloadDataEndTime() {
    return payloadDataEndTime;
  }

  public void setPayloadDataEndTime(Instant payloadDataEndTime) {
    this.payloadDataEndTime = payloadDataEndTime;
  }

  public Instant getReceptionTime() {
    return receptionTime;
  }

  public void setReceptionTime(Instant receptionTime) {
    this.receptionTime = receptionTime;
  }

  public byte[] getRawPayload() {
    return rawPayload;
  }

  public void setRawPayload(byte[] rawPayload) {
    this.rawPayload = rawPayload;
  }

  public AuthenticationStatus getAuthenticationStatus() {
    return authenticationStatus;
  }

  public void setAuthenticationStatus(AuthenticationStatus authenticationStatus) {
    this.authenticationStatus = authenticationStatus;
  }

  public CreationInfoDao getCreationInfo() {
    return creationInfo;
  }

  public void setCreationInfo(
          CreationInfoDao creationInfo) {
    this.creationInfo = creationInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RawStationDataFrameDao that = (RawStationDataFrameDao) o;
    return primaryKey == that.primaryKey &&
            Objects.equals(id, that.id) &&
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
    int result = Objects.hash(primaryKey, id, stationId, channelIds, acquisitionProtocol, payloadDataStartTime, payloadDataEndTime, receptionTime, authenticationStatus, creationInfo);
    result = 31 * result + Arrays.hashCode(rawPayload);
    return result;
  }

  @Override
  public String toString() {
    return "RawStationDataFrameDao{" +
            "primaryKey=" + primaryKey +
            ", id=" + id +
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
