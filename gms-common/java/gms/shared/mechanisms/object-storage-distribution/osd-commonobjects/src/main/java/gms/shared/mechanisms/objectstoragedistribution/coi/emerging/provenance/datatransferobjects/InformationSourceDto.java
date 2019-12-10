package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import java.time.Instant;

/**
 * DTO for the {@link InformationSource}
 */
public interface InformationSourceDto {

  @JsonCreator
  static InformationSource from(
      @JsonProperty("originatingOrganization") String originatingOrganization,
      @JsonProperty("informationTime") Instant informationTime,
      @JsonProperty("reference") String reference) {
    return InformationSource.from(originatingOrganization, informationTime, reference);
  }
}

