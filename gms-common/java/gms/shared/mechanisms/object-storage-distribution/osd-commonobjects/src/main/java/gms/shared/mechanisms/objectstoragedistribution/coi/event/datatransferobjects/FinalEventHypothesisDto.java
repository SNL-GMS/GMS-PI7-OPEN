package gms.shared.mechanisms.objectstoragedistribution.coi.event.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.UUID;

public class FinalEventHypothesisDto {

  private final UUID eventHypothesisId;

  @JsonCreator
  public FinalEventHypothesisDto(
      @JsonProperty("eventHypothesisId") UUID eventHypothesisId) {
    this.eventHypothesisId = Objects.requireNonNull(eventHypothesisId);
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

    FinalEventHypothesisDto that = (FinalEventHypothesisDto) o;

    return eventHypothesisId != null ? eventHypothesisId.equals(that.eventHypothesisId)
        : that.eventHypothesisId == null;
  }

  @Override
  public int hashCode() {
    return eventHypothesisId != null ? eventHypothesisId.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "FinalEventHypothesisDto{" +
        "eventHypothesisId=" + eventHypothesisId +
        '}';
  }
}
