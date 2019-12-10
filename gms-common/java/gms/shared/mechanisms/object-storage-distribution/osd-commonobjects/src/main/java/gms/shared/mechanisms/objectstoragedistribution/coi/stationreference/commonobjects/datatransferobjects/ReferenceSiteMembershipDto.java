package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSiteMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import java.time.Instant;
import java.util.UUID;

public interface ReferenceSiteMembershipDto {
  @JsonCreator
  static ReferenceSiteMembership from(
      @JsonProperty("id") UUID id,
      @JsonProperty("comment") String comment,
      @JsonProperty("actualChangeTime") Instant actualChangeTime,
      @JsonProperty("systemChangeTime") Instant systemChangeTime,
      @JsonProperty("siteId") UUID siteId,
      @JsonProperty("channelId") UUID channelId,
      @JsonProperty("status") StatusType status) {
    return ReferenceSiteMembership.from(id, comment, actualChangeTime,
        systemChangeTime, siteId, channelId, status);
  }
}
