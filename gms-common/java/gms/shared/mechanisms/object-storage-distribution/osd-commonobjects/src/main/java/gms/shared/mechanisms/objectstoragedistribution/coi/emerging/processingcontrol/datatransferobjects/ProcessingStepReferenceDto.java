package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import java.util.UUID;

/**
 * DTO for {@link ProcessingStepReference}
 */
public interface ProcessingStepReferenceDto {

  @JsonCreator
  static ProcessingStepReference from(
      @JsonProperty("processingStageIntervalId") UUID processingStageIntervalId,
      @JsonProperty("processingSequenceIntervalId") UUID processingSequenceIntervalId,
      @JsonProperty("processingStepId") UUID processingStepId) {
    return ProcessingStepReference
        .from(processingStageIntervalId, processingSequenceIntervalId, processingStepId);
  }
}
