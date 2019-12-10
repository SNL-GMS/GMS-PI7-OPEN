package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * A Data Transfer Object (DTO) for the RawStationDataFrame class. Annotates some properties so that
 * Jackson knows how to deserialize them - part of the reason this is necessary is that the original
 * RawStationDataFrame class is immutable. This class is a Jackson 'Mix-in annotations' class.
 */
public interface RawStationDataFrameDto {

  @JsonCreator
  static RawStationDataFrame from(
          @JsonProperty("id") UUID id,
          @JsonProperty("stationId") UUID stationId,
          @JsonProperty("channelIds") Set<UUID> channelIds,
          @JsonProperty("acquisitionProtocol") AcquisitionProtocol acquisitionProtocol,
          @JsonProperty("payloadDataStartTime") Instant payloadDataStartTime,
          @JsonProperty("payloadDataEndTime") Instant payloadDataEndTime,
          @JsonProperty("receptionTime") Instant receptionTime,
          @JsonProperty("rawPayload") byte[] rawPayload,
          @JsonProperty("authenticationStatus") AuthenticationStatus authenticationStatus,
          @JsonProperty("creationInfo") CreationInfo creationInfo) {

    return RawStationDataFrame.from(id, stationId, channelIds, acquisitionProtocol,
        payloadDataStartTime, payloadDataEndTime, receptionTime, rawPayload,
        authenticationStatus, creationInfo);
  }
}
