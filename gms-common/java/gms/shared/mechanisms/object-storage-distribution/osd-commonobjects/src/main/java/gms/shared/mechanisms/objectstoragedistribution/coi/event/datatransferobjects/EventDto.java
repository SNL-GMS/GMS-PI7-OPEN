package gms.shared.mechanisms.objectstoragedistribution.coi.event.datatransferobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EventDto {

  private final UUID id;
  private final Set<UUID> rejectedSignalDetectionAssociations;
  private final String monitoringOrganization;
  private final Set<EventHypothesis> hypotheses;
  private final List<FinalEventHypothesisDto> finalEventHypothesisHistory;
  private final List<PreferredEventHypothesisDto> preferredEventHypothesisHistory;

  @JsonCreator
  EventDto(
      @JsonProperty("id") UUID id,
      @JsonProperty("rejectedSignalDetectionAssociations") Set<UUID> rejectedSignalDetectionAssociations,
      @JsonProperty("monitoringOrganization") String monitoringOrganization,
      @JsonProperty("hypotheses") Set<EventHypothesis> hypotheses,
      @JsonProperty("finalEventHypothesisHistory") List<FinalEventHypothesisDto> finalEventHypothesisHistory,
      @JsonProperty("preferredEventHypothesisHistory") List<PreferredEventHypothesisDto> preferredEventHypothesisHistory) {
    this.id = id;
    this.rejectedSignalDetectionAssociations = rejectedSignalDetectionAssociations;
    this.monitoringOrganization = monitoringOrganization;
    this.hypotheses = hypotheses;
    this.finalEventHypothesisHistory = finalEventHypothesisHistory;
    this.preferredEventHypothesisHistory = preferredEventHypothesisHistory;
  }

  public UUID getId() {
    return id;
  }

  public Set<UUID> getRejectedSignalDetectionAssociations() {
    return rejectedSignalDetectionAssociations;
  }

  public String getMonitoringOrganization() {
    return monitoringOrganization;
  }

  public Set<EventHypothesis> getHypotheses() {
    return hypotheses;
  }

  public List<FinalEventHypothesisDto> getFinalEventHypothesisHistory() {
    return finalEventHypothesisHistory;
  }

  public List<PreferredEventHypothesisDto> getPreferredEventHypothesisHistory() {
    return preferredEventHypothesisHistory;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EventDto eventDto = (EventDto) o;

    if (id != null ? !id.equals(eventDto.id) : eventDto.id != null) {
      return false;
    }
    if (rejectedSignalDetectionAssociations != null ? !rejectedSignalDetectionAssociations
        .equals(eventDto.rejectedSignalDetectionAssociations)
        : eventDto.rejectedSignalDetectionAssociations != null) {
      return false;
    }
    if (monitoringOrganization != null ? !monitoringOrganization
        .equals(eventDto.monitoringOrganization) : eventDto.monitoringOrganization != null) {
      return false;
    }
    if (hypotheses != null ? !hypotheses.equals(eventDto.hypotheses)
        : eventDto.hypotheses != null) {
      return false;
    }
    if (finalEventHypothesisHistory != null ? !finalEventHypothesisHistory
        .equals(eventDto.finalEventHypothesisHistory)
        : eventDto.finalEventHypothesisHistory != null) {
      return false;
    }
    return preferredEventHypothesisHistory != null ? preferredEventHypothesisHistory
        .equals(eventDto.preferredEventHypothesisHistory)
        : eventDto.preferredEventHypothesisHistory == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (rejectedSignalDetectionAssociations != null
        ? rejectedSignalDetectionAssociations.hashCode() : 0);
    result = 31 * result + (monitoringOrganization != null ? monitoringOrganization.hashCode() : 0);
    result = 31 * result + (hypotheses != null ? hypotheses.hashCode() : 0);
    result =
        31 * result + (finalEventHypothesisHistory != null ? finalEventHypothesisHistory.hashCode()
            : 0);
    result =
        31 * result + (preferredEventHypothesisHistory != null ? preferredEventHypothesisHistory
            .hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "EventDto{" +
        "id=" + id +
        ", rejectedSignalDetectionAssociations=" + rejectedSignalDetectionAssociations +
        ", monitoringOrganization='" + monitoringOrganization + '\'' +
        ", hypotheses=" + hypotheses +
        ", finalEventHypothesisHistory=" + finalEventHypothesisHistory +
        ", preferredEventHypothesisHistory=" + preferredEventHypothesisHistory +
        '}';
  }
}
