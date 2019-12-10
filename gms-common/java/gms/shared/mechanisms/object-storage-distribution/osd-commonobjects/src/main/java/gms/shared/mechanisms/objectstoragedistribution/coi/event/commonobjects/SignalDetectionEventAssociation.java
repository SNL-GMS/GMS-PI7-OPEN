package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.UUID;

@AutoValue
public abstract class SignalDetectionEventAssociation {

  public abstract UUID getId();
  public abstract UUID getEventHypothesisId();
  public abstract UUID getSignalDetectionHypothesisId();
  public abstract boolean isRejected();


  /**
   * Create an instance of SignalDetectionEventAssociation with all params
   *
   * @param id UUID assigned to the new SignalDetectionEventAssociation, not null
   * @param eventHypothesisId UUID of associatedEventHypothesis, not null
   * @param signalDetectionHypothesisId UUID of associatedSignalDetectionHypothesis, not null
   * @param isRejected boolean Ensures that any rejected Associations will not be re-formed in
   * subsequent processing stages
   */
  @JsonCreator
  public static SignalDetectionEventAssociation from(
      @JsonProperty("id") UUID id,
      @JsonProperty("eventHypothesisId") UUID eventHypothesisId,
      @JsonProperty("signalDetectionHypothesisId") UUID signalDetectionHypothesisId,
      @JsonProperty("rejected") boolean isRejected) {

    return new AutoValue_SignalDetectionEventAssociation(id, eventHypothesisId,
        signalDetectionHypothesisId, isRejected);
  }

  /**
   * Create an instance of SignalDetectionEventAssociation with a randomly-generated ID
   * and not rejected
   *
   * @param eventHypothesisId UUID of associatedEventHypothesis, not null
   * @param signalDetectionHypothesisId UUID of associatedSignalDetectionHypothesis, not null
   */
  public static SignalDetectionEventAssociation create(UUID eventHypothesisId,
      UUID signalDetectionHypothesisId) {

    return new AutoValue_SignalDetectionEventAssociation(UUID.randomUUID(), eventHypothesisId,
        signalDetectionHypothesisId, false);
  }

  /**
   * Creates a new SignalDetectionEventAssociation with the same id as the rejected association, but
   * with a separate provenance. Rejected associations have {@code <<id>> } references to EventHypothesis and
   * SignalDetectionHypothesis, but the EventHypothesis and SignalDetectionHypothesis do not have
   * {@code <<id>> } references to the rejected association object.
   */
  public SignalDetectionEventAssociation reject() {
    return new AutoValue_SignalDetectionEventAssociation(
        getId(), getEventHypothesisId(), getSignalDetectionHypothesisId(), true);
  }

  public boolean hasSameState(SignalDetectionEventAssociation other) {
    return other != null &&
        getEventHypothesisId().equals(other.getEventHypothesisId()) &&
        getSignalDetectionHypothesisId().equals(other.getSignalDetectionHypothesisId()) &&
        isRejected() == other.isRejected();
  }
}
