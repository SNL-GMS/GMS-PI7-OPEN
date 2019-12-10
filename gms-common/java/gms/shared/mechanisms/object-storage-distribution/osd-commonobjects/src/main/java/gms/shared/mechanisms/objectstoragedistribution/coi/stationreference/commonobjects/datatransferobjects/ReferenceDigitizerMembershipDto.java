package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizerMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import java.time.Instant;
import java.util.UUID;

public interface ReferenceDigitizerMembershipDto {
  @JsonCreator
  static ReferenceDigitizerMembership from(
      @JsonProperty("id") UUID id,
      @JsonProperty("comment") String comment,
      @JsonProperty("actualChangeTime") Instant actualChangeTime,
      @JsonProperty("systemChangeTime") Instant systemChangeTime,
      @JsonProperty("digitizerId") UUID digitizerId,
      @JsonProperty("channelId") UUID channelId,
      @JsonProperty("status") StatusType status) {
    return ReferenceDigitizerMembership.from(id, comment, actualChangeTime,
        systemChangeTime, digitizerId, channelId, status);
  }
}
