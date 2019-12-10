package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.dataaccessobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileRawStationDataFrameMetadata;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Embeddable
public class TransferredFileRawStationDataFrameMetadataDao {

  @Column(nullable = false, name = "payload_start_time")
  private Instant payloadStartTime;
  @Column(nullable = false, name = "payload_end_time")
  private Instant payloadEndTime;
  @Column(name = "station_id")
  private UUID stationId;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(name = "channel_ids")
  // wondering why this is being initialized when it's later ignored?  If it's not initialized,
  // hibernate won't populate this collection on queries for some unknown reason...
  private Set<UUID> channelIds = new HashSet<>();

  public TransferredFileRawStationDataFrameMetadataDao() {
  }

  public TransferredFileRawStationDataFrameMetadataDao(
      TransferredFileRawStationDataFrameMetadata metadata) {
    this.payloadStartTime = metadata.getPayloadStartTime();
    this.payloadEndTime = metadata.getPayloadEndTime();
    this.stationId = metadata.getStationId();
    this.channelIds = metadata.getChannelIds();
  }

  public TransferredFileRawStationDataFrameMetadata toCoi() {
    return TransferredFileRawStationDataFrameMetadata
        .from(this.payloadStartTime, this.payloadEndTime, this.stationId, this.channelIds);
  }

  public Instant getPayloadStartTime() {
    return payloadStartTime;
  }

  public void setPayloadStartTime(Instant payloadStartTime) {
    this.payloadStartTime = payloadStartTime;
  }

  public Instant getPayloadEndTime() {
    return payloadEndTime;
  }

  public void setPayloadEndTime(Instant payloadEndTime) {
    this.payloadEndTime = payloadEndTime;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransferredFileRawStationDataFrameMetadataDao that = (TransferredFileRawStationDataFrameMetadataDao) o;
    return Objects.equals(payloadStartTime, that.payloadStartTime) &&
        Objects.equals(payloadEndTime, that.payloadEndTime) &&
        Objects.equals(stationId, that.stationId) &&
        Objects.equals(channelIds, that.channelIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(payloadStartTime, payloadEndTime, stationId, channelIds);
  }

  @Override
  public String toString() {
    return "TransferredFileRawStationDataFrameMetadataDao{" +
        "payloadStartTime=" + payloadStartTime +
        ", payloadEndTime=" + payloadEndTime +
        ", stationId=" + stationId +
        ", channelIds=" + channelIds +
        '}';
  }
}
