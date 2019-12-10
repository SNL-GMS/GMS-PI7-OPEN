package gms.shared.mechanisms.objectstoragedistribution.coi.event.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.UUID;

public class PreferredEventHypothesisDto {

  private final UUID processingStageId;
  private final UUID eventHypothesisId;

  @JsonCreator
  public PreferredEventHypothesisDto(
      @JsonProperty("processingStageId") UUID processingStageId,
      @JsonProperty("eventHypothesisId") UUID eventHypothesisId) {
    this.processingStageId = Objects.requireNonNull(processingStageId);
    this.eventHypothesisId = Objects.requireNonNull(eventHypothesisId);
  }

  public UUID getProcessingStageId() {
    return processingStageId;
  }

  public UUID getEventHypothesisId() {
    return eventHypothesisId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PreferredEventHypothesisDto that = (PreferredEventHypothesisDto) o;

    if (processingStageId != null ? !processingStageId.equals(that.processingStageId)
        : that.processingStageId != null) {
      return false;
    }
    return eventHypothesisId != null ? eventHypothesisId.equals(that.eventHypothesisId)
        : that.eventHypothesisId == null;
  }

  @Override
  public int hashCode() {
    int result = processingStageId != null ? processingStageId.hashCode() : 0;
    result = 31 * result + (eventHypothesisId != null ? eventHypothesisId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "PreferredEventHypothesisDto{" +
        "processingStageId=" + processingStageId +
        ", eventHypothesisId=" + eventHypothesisId +
        '}';
  }
}
