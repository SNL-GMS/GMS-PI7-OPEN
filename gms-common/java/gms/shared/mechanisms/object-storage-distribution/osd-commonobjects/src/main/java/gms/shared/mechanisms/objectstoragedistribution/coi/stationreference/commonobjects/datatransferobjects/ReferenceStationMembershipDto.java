package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStationMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import java.time.Instant;
import java.util.UUID;

public interface ReferenceStationMembershipDto {
  @JsonCreator
  static ReferenceStationMembership from(
      @JsonProperty("id") UUID id,
      @JsonProperty("comment") String comment,
      @JsonProperty("actualChangeTime") Instant actualChangeTime,
      @JsonProperty("systemChangeTime") Instant systemChangeTime,
      @JsonProperty("stationId") UUID stationId,
      @JsonProperty("siteId") UUID siteId,
      @JsonProperty("status") StatusType status) {
    return ReferenceStationMembership.from(id, comment, actualChangeTime,
        systemChangeTime, stationId, siteId, status);
  }
}

