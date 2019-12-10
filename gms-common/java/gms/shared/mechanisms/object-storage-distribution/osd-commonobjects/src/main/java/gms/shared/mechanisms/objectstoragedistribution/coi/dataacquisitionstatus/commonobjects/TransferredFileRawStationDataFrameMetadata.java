package gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * TransferredRawStationDataFrameMetaData is a binding of TransferredFile's generic metadata object
 * that contains metadata about a transferred RawStationDataFrame.
 */

@AutoValue
public abstract class TransferredFileRawStationDataFrameMetadata {

  public abstract Instant getPayloadStartTime();

  public abstract Instant getPayloadEndTime();

  public abstract UUID getStationId();

  public abstract Set<UUID> getChannelIds();

  /**
   * Creates an instance of TransferredFileRawStationDataFrameMetadata
   *
   * @return a TransferredFileRawStationDataFrameMetadata
   */
  @JsonCreator
  public static TransferredFileRawStationDataFrameMetadata from(
      @JsonProperty("payloadStartTime") Instant payloadStartTime,
      @JsonProperty("payloadEndTime") Instant payloadEndTime,
      @JsonProperty("stationId") UUID stationId,
      @JsonProperty("channelIds") Set<UUID> channelIds) {

    return new AutoValue_TransferredFileRawStationDataFrameMetadata(payloadStartTime,
        payloadEndTime, stationId, Collections.unmodifiableSet(channelIds));
  }

}

