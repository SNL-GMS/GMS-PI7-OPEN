package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceAlias;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StatusType;
import java.time.Instant;
import java.util.UUID;

public interface ReferenceAliasDto {

  @JsonCreator
  static ReferenceAlias from(
      @JsonProperty("id") UUID id,
      @JsonProperty("name") String name,
      @JsonProperty("status") StatusType status,
      @JsonProperty("comment") String comment,
      @JsonProperty("actualChangeTime") Instant actualChangeTime,
      @JsonProperty("systemChangeTime") Instant systemChangeTime) {

    return ReferenceAlias.from(id, name, status, comment,
        actualChangeTime, systemChangeTime);
  }
}

