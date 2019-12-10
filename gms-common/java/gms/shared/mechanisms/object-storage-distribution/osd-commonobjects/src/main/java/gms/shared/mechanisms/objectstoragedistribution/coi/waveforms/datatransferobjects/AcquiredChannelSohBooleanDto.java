package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import java.time.Instant;
import java.util.UUID;

/**
 * Create class to allow transformation to and from JSON.
 */
public interface AcquiredChannelSohBooleanDto {

  @JsonCreator
  static AcquiredChannelSohBoolean from(
      @JsonProperty("id") UUID id,
      @JsonProperty("channelId") UUID channelId,
      @JsonProperty("type") AcquiredChannelSoh.AcquiredChannelSohType type,
      @JsonProperty("startTime") Instant startTime,
      @JsonProperty("endTime") Instant endTime,
      @JsonProperty("status") boolean status,
      @JsonProperty("creationInfo") CreationInfo creationInfo) {

    return AcquiredChannelSohBoolean.from(id, channelId, type,
        startTime, endTime, status, creationInfo);
  }
}
