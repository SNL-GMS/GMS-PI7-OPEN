package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * DTO for the {@link CreationInformation}
 */
public interface CreationInformationDto {

  @JsonCreator
  static CreationInformation from(
      @JsonProperty("id") UUID id,
      @JsonProperty("creationTime") Instant creationTime,
      @JsonProperty("analystActionReference") Optional<AnalystActionReference> analystActionReference,
      @JsonProperty("processingStepReference") Optional<ProcessingStepReference> processingStepReference,
      @JsonProperty("softwareInfo") SoftwareComponentInfo softwareInfo) {
    return CreationInformation
        .from(id, creationTime, analystActionReference, processingStepReference, softwareInfo);
  }
}
