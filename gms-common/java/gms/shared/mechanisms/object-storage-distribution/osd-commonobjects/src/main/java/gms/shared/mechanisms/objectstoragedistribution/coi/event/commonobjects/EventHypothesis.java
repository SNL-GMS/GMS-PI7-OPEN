package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.Validate;

/**
 * Represents a possible hypothesis for an Event.
 */
@AutoValue
public abstract class EventHypothesis {

  public abstract UUID getId();

  public abstract UUID getEventId();

  public abstract Set<UUID> getParentEventHypotheses();

  public abstract boolean isRejected();

  public abstract Set<LocationSolution> getLocationSolutions();

  public abstract Optional<PreferredLocationSolution> getPreferredLocationSolution();

  public abstract Set<SignalDetectionEventAssociation> getAssociations();

  /**
   * Creates an instance of an EventHypothesis
   *
   * @param id UUID assigned to the new EventHypothesis, not null
   * @param eventId UUID of the associated Event, not null
   * @param parentEventHypotheses {@code Set<UUID> of the ParentEventHypotheses, unmodifiable }
   * @param isRejected boolean
   * @param locationSolutions {@code Set<LocationSolution>, unmodifiable}
   * @param preferredLocationSolution The single PreferredLocationSolution associated with the
   * EventHypothesis, not null
   * @param associations {@code Set<SignalDetectionEventAssociation>, unmodifiable }
   * @return an Event Hypothesis
   */
  @JsonCreator
  public static EventHypothesis from(
      @JsonProperty("id") UUID id,
      @JsonProperty("eventId") UUID eventId,
      @JsonProperty("parentEventHypotheses") Set<UUID> parentEventHypotheses,
      @JsonProperty("rejected") boolean isRejected,
      @JsonProperty("locationSolutions") Set<LocationSolution> locationSolutions,
      @JsonProperty("preferredLocationSolution") PreferredLocationSolution preferredLocationSolution,
      @JsonProperty("associations") Set<SignalDetectionEventAssociation> associations) {

    Objects.requireNonNull(id,
        "Cannot create EventHypothesis from a null id");
    Objects.requireNonNull(eventId,
        "Cannot create EventHypothesis from a null eventId");
    Validate.notNull(parentEventHypotheses,
        "Cannot create Event Hypothesis from null parentEventHypotheses");
    Validate.notNull(locationSolutions,
        "Cannot create Event Hypothesis from null locationSolutions");
    if (isRejected) {
      Validate.isTrue(locationSolutions.isEmpty(),
          "Expected locationSolutions to be empty when isRejected=true");
      Validate.isTrue(preferredLocationSolution == null,
          "Expected preferredLocationSolution to be null when isRejected=true");
    } else {
      Validate.notNull(preferredLocationSolution,
          "Expected non-null preferredLocationSolution when EventHypothesis is not rejected");
      Validate.isTrue(locationSolutions.contains(preferredLocationSolution.getLocationSolution()),
          "Expected locationSolutions to contain preferredLocationSolution");
    }

    return new AutoValue_EventHypothesis(
        id,
        eventId,
        Collections.unmodifiableSet(parentEventHypotheses),
        isRejected,
        Collections.unmodifiableSet(locationSolutions),
        Optional.ofNullable(preferredLocationSolution),
        Collections.unmodifiableSet(associations));
  }
}
