package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import java.util.Optional;

/**
 * DTO for the {@link gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext}
 */
public interface ProcessingContextDto {

  @JsonCreator
  static ProcessingContext from(
      @JsonProperty("analystActionReference") Optional<AnalystActionReference> analystActionReference,
      @JsonProperty("processingStepReference") Optional<ProcessingStepReference> processingStepReference,
      @JsonProperty("storageVisibility") StorageVisibility storageVisibility) {
    return ProcessingContext
        .from(analystActionReference, processingStepReference, storageVisibility);
  }

}
