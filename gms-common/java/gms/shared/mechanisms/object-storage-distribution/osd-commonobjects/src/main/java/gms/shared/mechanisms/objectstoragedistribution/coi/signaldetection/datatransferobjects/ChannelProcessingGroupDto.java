package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroupType;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;


/**
 * Create class to allow transformation to and from JSON.
 */
@JsonPropertyOrder({
    "id", "type", "channelIds",
    "actualChangeTime", "systemChangeTime", "status", "comment"})
public interface ChannelProcessingGroupDto {

  @JsonCreator
  static ChannelProcessingGroup from(
      @JsonProperty("id") UUID id,
      @JsonProperty("type") ChannelProcessingGroupType type,
      @JsonProperty("channelIds") Set<UUID> channelIds,
      @JsonProperty("actualChangeTime") Instant actualChangeTime,
      @JsonProperty("systemChangeTime") Instant systemChangeTime,
      @JsonProperty("status") String status,
      @JsonProperty("comment") String comment) {

    return ChannelProcessingGroup.from(id, type, channelIds,
        actualChangeTime, systemChangeTime, status, comment);
  }
}
