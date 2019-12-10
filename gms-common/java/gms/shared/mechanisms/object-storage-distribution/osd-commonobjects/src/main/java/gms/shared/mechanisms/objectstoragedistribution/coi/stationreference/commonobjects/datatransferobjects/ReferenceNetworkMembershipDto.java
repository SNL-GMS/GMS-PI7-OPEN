package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetworkMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import java.time.Instant;
import java.util.UUID;

public interface ReferenceNetworkMembershipDto {
  @JsonCreator
  static ReferenceNetworkMembership from(
      @JsonProperty("id") UUID id,
      @JsonProperty("comment") String comment,
      @JsonProperty("actualChangeTime") Instant actualChangeTime,
      @JsonProperty("systemChangeTime") Instant systemChangeTime,
      @JsonProperty("networkId") UUID networkId,
      @JsonProperty("stationId") UUID stationId,
      @JsonProperty("status") StatusType status) {
    return ReferenceNetworkMembership.from(id, comment, actualChangeTime,
        systemChangeTime, networkId, stationId, status);
  }
}

