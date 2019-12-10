package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import java.time.Instant;

/**
 * Create class to allow transformation to and from JSON.
 */
public abstract class CreationInfoDto {

  @JsonCreator
  public CreationInfoDto(
      @JsonProperty("creatorName") String creatorName,
      @JsonProperty("creationTime") Instant creationTime,
      @JsonProperty("softwareInfo") SoftwareComponentInfo softwareInfo) {
  }
}
