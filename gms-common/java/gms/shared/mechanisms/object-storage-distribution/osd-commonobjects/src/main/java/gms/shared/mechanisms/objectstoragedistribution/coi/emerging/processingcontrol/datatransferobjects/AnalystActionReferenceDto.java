package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import java.util.UUID;

/**
 * DTO for {@link AnalystActionReference}
 */
public interface AnalystActionReferenceDto {

  @JsonCreator
  static AnalystActionReference from(
      @JsonProperty("processingStageIntervalId") UUID processingStageIntervalId,
      @JsonProperty("processingActivityIntervalId") UUID processingActivityIntervalId,
      @JsonProperty("analystId") UUID analystId) {
    return AnalystActionReference
        .from(processingStageIntervalId, processingActivityIntervalId, analystId);
  }
}
