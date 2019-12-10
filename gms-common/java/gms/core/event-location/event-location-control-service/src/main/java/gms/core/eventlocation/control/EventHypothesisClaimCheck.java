package gms.core.eventlocation.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Objects;
import java.util.UUID;

/**
 * Claim check object containing the {@link UUID} of an {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis}
 * and the {@link UUID} of its parent {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event}
 */
@AutoValue
public abstract class EventHypothesisClaimCheck {

  public abstract UUID getEventHypothesisId();

  public abstract UUID getEventId();

  /**
   * Given a {@link UUID} of an {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis}
   * and a {@link UUID} of its parent {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event},
   * return a new {@link EventHypothesisClaimCheck}
   *
   * @param eventHypothesisId {@link UUID} of an {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis}
   * @param eventId {@link UUID} of the parent {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event}
   * @return New {@link EventHypothesisClaimCheck}.  Not null.
   */
  @JsonCreator
  public static EventHypothesisClaimCheck from(
      @JsonProperty("eventHypothesisId") UUID eventHypothesisId,
      @JsonProperty("eventId") UUID eventId
  ) {

    Objects.requireNonNull(eventHypothesisId,
        "EventHypothesisClaimCheck::from() requires non-null eventHypothesisId");
    Objects.requireNonNull(eventId, "EventHypothesisClaimCheck::from() requires non-null eventId");

    return new AutoValue_EventHypothesisClaimCheck(eventHypothesisId, eventId);
  }
}
