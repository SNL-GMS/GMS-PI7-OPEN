package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import java.time.Instant;
import java.util.UUID;

public interface ReferenceNetworkDto {

  @JsonCreator
  static ReferenceNetwork from(
      @JsonProperty("entityId") UUID entityId,
      @JsonProperty("versionId") UUID versionId,
      @JsonProperty("name") String name,
      @JsonProperty("description") String description,
      @JsonProperty("organization") NetworkOrganization organization,
      @JsonProperty("region") NetworkRegion region,
      @JsonProperty("source") InformationSource source,
      @JsonProperty("comment") String comment,
      @JsonProperty("actualChangeTime") Instant actualChangeTime,
      @JsonProperty("systemChangeTime") Instant systemChangeTime) {

    return ReferenceNetwork.from(entityId, versionId, name, description,
        organization, region, source,
        comment, actualChangeTime, systemChangeTime);
  }
}

