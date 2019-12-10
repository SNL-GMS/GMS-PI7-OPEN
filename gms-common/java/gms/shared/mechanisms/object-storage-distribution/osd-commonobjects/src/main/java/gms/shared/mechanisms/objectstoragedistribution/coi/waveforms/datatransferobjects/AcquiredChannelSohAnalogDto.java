package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import java.time.Instant;
import java.util.UUID;

/**
 * Create class to allow transformation to and from JSON.
 */
public interface AcquiredChannelSohAnalogDto {

  @JsonCreator
  static AcquiredChannelSohAnalog from(
      @JsonProperty("id") UUID id,
      @JsonProperty("channelId") UUID channelId,
      @JsonProperty("type") AcquiredChannelSoh.AcquiredChannelSohType type,
      @JsonProperty("startTime") Instant startTime,
      @JsonProperty("endTime") Instant endTime,
      @JsonProperty("status") double status,
      @JsonProperty("creationInfo") CreationInfo creationInfo) {

    return AcquiredChannelSohAnalog.from(id, channelId, type,
        startTime, endTime, status, creationInfo);
  }
}
