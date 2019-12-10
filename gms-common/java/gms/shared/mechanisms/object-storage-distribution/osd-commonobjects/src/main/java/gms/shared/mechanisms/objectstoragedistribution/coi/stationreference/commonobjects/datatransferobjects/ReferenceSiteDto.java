package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceAlias;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReferenceSiteDto {
  @JsonCreator
  static ReferenceSite from(
      @JsonProperty("entityId") UUID entityId,
      @JsonProperty("versionId") UUID versionId,
      @JsonProperty("name") String name,
      @JsonProperty("description") String description,
      @JsonProperty("source") InformationSource source,
      @JsonProperty("comment") String comment,
      @JsonProperty("latitude") double latitude,
      @JsonProperty("longitude") double longitude,
      @JsonProperty("elevation") double elevation,
      @JsonProperty("actualChangeTime") Instant actualChangeTime,
      @JsonProperty("systemChangeTime") Instant systemChangeTime,
      @JsonProperty("position") RelativePosition position,
      @JsonProperty("aliases") List<ReferenceAlias> aliases) {

    return ReferenceSite.from(entityId, versionId, name, description, source, comment,
        latitude, longitude, elevation, actualChangeTime,
        systemChangeTime, position, aliases);
  }
}
