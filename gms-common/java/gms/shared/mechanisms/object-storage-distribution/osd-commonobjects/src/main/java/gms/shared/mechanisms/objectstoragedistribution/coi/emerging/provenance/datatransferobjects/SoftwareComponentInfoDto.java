package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Create class to allow transformation to and from JSON.
 */
public abstract class SoftwareComponentInfoDto {

  @JsonCreator
  SoftwareComponentInfoDto(@JsonProperty("name") String name,
      @JsonProperty("version") String version) {
  }
}
