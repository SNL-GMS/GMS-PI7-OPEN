package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;

/**
 * Represents an occurrence of something the GMS system detects. Event is one of the primary data
 * objects of the system and it's primary result.
 */
public final class Event {

  private final UUID id;
  // these ID's are of SignalDetectionHypothesis objects that are not to be associated with this Event
  private final Set<UUID> rejectedSignalDetectionAssociations;
  private final String monitoringOrganization;
  private final Set<EventHypothesis> hypotheses;
  private final List<FinalEventHypothesis> finalEventHypothesisHistory;
  private final List<PreferredEventHypothesis> preferredEventHypothesisHistory;

  private Event(UUID id, Set<UUID> rejectedSignalDetectionAssociations,
      String monitoringOrganization, Set<EventHypothesis> hypotheses,
      List<FinalEventHypothesis> finalHypothesisHistory,
      List<PreferredEventHypothesis> preferredHypothesisHistory) {
    this.id = Objects.requireNonNull(id);
    this.rejectedSignalDetectionAssociations = Collections
        .unmodifiableSet(rejectedSignalDetectionAssociations);
    Validate.notEmpty(monitoringOrganization,
        "Cannot create Event with null or blank monitoringOrganization");
    this.monitoringOrganization = monitoringOrganization;
    Validate.notEmpty(hypotheses,
        "Event must have at least one hypothesis (was " + hypotheses + ")");
    this.hypotheses = new HashSet<>(Objects.requireNonNull(hypotheses));
    hypotheses.forEach(h -> Validate.isTrue(id.equals(h.getEventId()), String.format(
        "EventHypothesis has eventId not matching parent Event; event.id %s, EventHypothesis.eventId %s",
        id, h.getEventId())));
    this.finalEventHypothesisHistory = new ArrayList<>(
        Objects.requireNonNull(finalHypothesisHistory));
    this.preferredEventHypothesisHistory = new ArrayList<>(
        Objects.requireNonNull(preferredHypothesisHistory));
  }

  /**
   * Recreation factory method - generally used to reconstruct from serialization or database.
   *
   * @param id the id of the Event
   * @param rejectedSignalDetectionAssociations rejected signal detection associations of the event
   * @param monitoringOrganization the monitoring organization for the event
   * @param hypotheses the hypotheses of the event
   * @param finalHypothesisHistory history of the hypotheses that have been marked final for the
   * event
   * @param preferredHypothesisHistory history of the hypotheses that have been marked preferred for
   * the event
   * @return an Event
   */
  public static Event from(
      UUID id,
      Set<UUID> rejectedSignalDetectionAssociations,
      String monitoringOrganization,
      Set<EventHypothesis> hypotheses,
      List<FinalEventHypothesis> finalHypothesisHistory,
      List<PreferredEventHypothesis> preferredHypothesisHistory) {

    return new Event(id, rejectedSignalDetectionAssociations,
        monitoringOrganization, hypotheses, finalHypothesisHistory,
        preferredHypothesisHistory);
  }

  /**
   * Creates a new Event.  This is generally the method used by application code instead of
   * Event.from.  The resulting Event has one EventHypothesis, which is marked preferred.
   *
   * @param parentEventHypotheses the parent event hypotheses that are being merged to create this
   * event.  Cannot be null but can be empty, meaning the Event is being created entirely anew.
   * @param associatedSignalDetectionHypothesisIds ID's of SignalDetectionHypotheses this Event is
   * associated to. These are associated to the initial Event Hypothesis.
   * @param locationSolutions the location solutions for the Event.  These will be part of the
   * initial Event Hypothesis.
   * @param preferredLocationSolution the preferred location solution. This will be part of the
   * initial Event Hypothesis.  Can be null, meaning there is no preferred location solution.
   * @param monitoringOrganization the monitoring organization for the event. This cannot be null or
   * the empty string.
   * @param processingStageId refers to the processing stage this event was created for
   * @return an Event with one initial preferred EventHypothesis
   */
  public static Event create(
      Set<UUID> parentEventHypotheses,
      Set<UUID> associatedSignalDetectionHypothesisIds,
      Set<LocationSolution> locationSolutions,
      PreferredLocationSolution preferredLocationSolution,
      String monitoringOrganization,
      UUID processingStageId) {

    final UUID eventId = UUID.randomUUID();
    final EventHypothesis eh = createHypothesis(
        eventId, parentEventHypotheses, locationSolutions,
        preferredLocationSolution, associatedSignalDetectionHypothesisIds);
    return new Event(eventId, Set.of(), monitoringOrganization,
        Set.of(eh), List.of(), List.of(PreferredEventHypothesis.from(processingStageId, eh)));
  }

  /**
   * Rejects the given event hypothesis.
   *
   * @param parentEventHypothesis the hypothesis to reject.  It must be part of the existing event
   * already (e.g. by calling addEventHypothesis on it).
   * @return the newly-created rejected hypothesis.  The Event is also mutated in that it adds this
   * rejected hypothesis to itself.
   */
  public EventHypothesis reject(EventHypothesis parentEventHypothesis) {
    validateHypothesisPartOfEvent(parentEventHypothesis, "hypothesis to reject");
    return addEventHypothesis(EventHypothesis.from(UUID.randomUUID(), this.id,
        Set.of(parentEventHypothesis.getId()), true, Set.of(),
        null, Set.of()));
  }

  /**
   * Adds a hypothesis to this event.
   *
   * @param parentEventHypothesis the parent hypothesis that the new hypothesis was based on.
   * @param associatedSignalDetectionHypothesisIds ID's of SignalDetectionHypotheses the new
   * hypothesis is associated to.  These are associated to the hypothesis.
   * @param locationSolutions location solutions for the new hypothesis
   * @param preferredLocationSolution the preferred location solution of the new hypothesis; can be
   * null, meaning there is no preferred location solution.
   * @return the newly added EventHypothesis.  This operation also mutates Event by adding the
   * hypothesis to it.
   */
  public EventHypothesis addEventHypothesis(EventHypothesis parentEventHypothesis,
      Set<UUID> associatedSignalDetectionHypothesisIds,
      Set<LocationSolution> locationSolutions,
      PreferredLocationSolution preferredLocationSolution) {

    Validate.notNull(parentEventHypothesis, "Cannot add event hypothesis with null parent");
    Validate.notNull(associatedSignalDetectionHypothesisIds,
        "Cannot add event hypothesis with null associated signal detection hypothesis ID's");
    Validate.notEmpty(locationSolutions,
        "Cannot add event hypothesis with null or empty location solutions");
    validateHypothesisPartOfEvent(parentEventHypothesis, "parentEventHypothesis");
    return addEventHypothesis(
        createHypothesis(
            this.id,
            Set.of(parentEventHypothesis.getId()),
            locationSolutions,
            preferredLocationSolution,
            associatedSignalDetectionHypothesisIds
        )
    );
  }

  /**
   * Adds the hypothesis to this event.  Asserts that the hypothesis (ID) is not already part of the
   * event.  This method also centralizes when the hypotheses collection is mutated (added to).
   *
   * @param eh the hypothesis to add
   * @return the hypothesis that was added
   */
  private EventHypothesis addEventHypothesis(EventHypothesis eh) {
    Validate.notNull(eh, "Cannot add null EventHypothesis");
    EventUtilities.assertNoneWithIdPresent(eh.getId(), this.hypotheses, EventHypothesis::getId);
    this.hypotheses.add(eh);
    return eh;
  }

  /**
   * Marks the given hypothesis as preferred for the given processing stage.
   *
   * @param eventHypothesis the hypothesis to mark as preferred; not null and must be part of the
   * Event already (i.e. was in a call to addEventHypothesis)
   * @param processingStageId the processing stage to mark the hypothesis preferred for
   */
  public void markPreferred(EventHypothesis eventHypothesis, UUID processingStageId) {
    Validate.notNull(eventHypothesis,
        "Cannot mark null EventHypothesis as preferred");
    Validate.notNull(processingStageId,
        "Cannot mark EventHypothesis preferred for null processingStageId");
    validateHypothesisPartOfEvent(eventHypothesis, "hypothesis to be marked as preferred");
    Validate.isTrue(this.hypotheses.contains(eventHypothesis),
        "Must add EventHypothesis to Event first before marking as preferred");
    this.preferredEventHypothesisHistory.add(PreferredEventHypothesis.from(
        processingStageId, eventHypothesis));
  }

  /**
   * Gets the preferred hypothesis for the given processing stage.
   *
   * @param processingStageId the processing stage
   * @return the preferred hypothesis for the given stage, or empty if there is no preferred
   * hypothesis for the given stage.
   */
  public Optional<EventHypothesis> getPreferredForProcessingStage(
      UUID processingStageId) {
    Validate.notNull(processingStageId,
        "Cannot get preferred EventHypothesis for null processingStageId");
    final List<PreferredEventHypothesis> hypotheses = this.preferredEventHypothesisHistory.stream()
        .filter(peh -> peh.getProcessingStageId().equals(processingStageId))
        .collect(Collectors.toList());
    if (hypotheses.isEmpty()) {
      return Optional.empty();
    }
    final PreferredEventHypothesis p = hypotheses.get(hypotheses.size() - 1);
    return Optional.of(p.getEventHypothesis());
  }

  /**
   * Gets the overall preferred hypothesis, meaning the one that was preferred the most recently.
   *
   * @return the overall preferred hypothesis
   */
  public EventHypothesis getOverallPreferred() {
    final List<PreferredEventHypothesis> history = this.getPreferredEventHypothesisHistory();
    return history.get(history.size() - 1).getEventHypothesis();
  }

  /**
   * Marks the given hypothesis as final.
   *
   * @param eventHypothesis the hypothesis to mark as final; cannot be null and must be part of the
   * Event already (i.e. was in a call to addEventHypothesis)
   */
  public void markFinal(EventHypothesis eventHypothesis) {
    Validate.notNull(eventHypothesis, "Cannot mark null EventHypothesis as final");
    validateHypothesisPartOfEvent(eventHypothesis, "final hypothesis");
    this.finalEventHypothesisHistory.add(FinalEventHypothesis.from(eventHypothesis));
  }

  /**
   * Gets the (currently) final hypothesis.
   *
   * @return the final hypothesis, or empty if no hypothesis has (ever) been marked final
   */
  public Optional<EventHypothesis> getFinal() {
    if (this.finalEventHypothesisHistory.isEmpty()) {
      return Optional.empty();
    }
    final FinalEventHypothesis f = this.finalEventHypothesisHistory.get(
        this.finalEventHypothesisHistory.size() - 1);
    return Optional.of(f.getEventHypothesis());
  }

  /**
   * Creates a hypothesis using the given parameters.  Helper method used a couple places in the
   * class. One useful thing it does is create the SignalDetectionEventAssociation's from the given
   * SignalDetectionHypothesis ID's.
   *
   * @param eventId the id of the event
   * @param parentEventHypotheses the parent hypotheses for the new hypothesis
   * @param locationSolutions the location solutions for the new hypothesis
   * @param preferredLocationSolution the preferred location solution for the new hypothesis
   * @param associatedSignalDetectionHypothesisIds the associations for the new hypothesis
   * @return a new hypothesis from the given params
   */
  private static EventHypothesis createHypothesis(UUID eventId,
      Set<UUID> parentEventHypotheses,
      Set<LocationSolution> locationSolutions,
      PreferredLocationSolution preferredLocationSolution,
      Set<UUID> associatedSignalDetectionHypothesisIds) {

    Validate.notNull(associatedSignalDetectionHypothesisIds,
        "Cannot create EventHyptohesis will null associatedSignalDetectionHypothesisIds");
    final UUID ehId = UUID.randomUUID();
    final Set<SignalDetectionEventAssociation> associations = associatedSignalDetectionHypothesisIds
        .stream()
        .map(sdhId -> SignalDetectionEventAssociation.create(ehId, sdhId))
        .collect(Collectors.toSet());
    return EventHypothesis.from(ehId, eventId,
        parentEventHypotheses, false, locationSolutions,
        preferredLocationSolution, associations);
  }

  /**
   * Validates that the given hypothesis is part of this Event, throwing an exception if it's not.
   * This is useful when marking events as preferred or final, for instance.
   *
   * @param hypothesis the hypothesis to check
   * @param hypothesisName a descriptive string to use as part of the exception message when the
   * validation fails
   */
  private void validateHypothesisPartOfEvent(EventHypothesis hypothesis, String hypothesisName) {
    Validate.notNull(hypothesis, "null hypothesis could not be " + hypothesisName);
    Validate.isTrue(getHypotheses().contains(hypothesis),
        hypothesisName + " must be contained in overall set of hypotheses");
  }

  /**
   * Gets the ID of this event.
   *
   * @return the id
   */
  public UUID getId() {
    return id;
  }

  /**
   * Returns whether this event is rejected.
   *
   * @return true if the overall preferred event is rejected, false otherwise.
   */
  public boolean isRejected() {
    return getOverallPreferred().isRejected();
  }

  /**
   * Returns the rejected SignalDetection Association IDs, immutable
   */
  public Set<UUID> getRejectedSignalDetectionAssociations() {
    return Collections.unmodifiableSet(this.rejectedSignalDetectionAssociations);
  }

  /**
   * Gets the monitoring organization name of this event.
   *
   * @return the monitoring org
   */
  public String getMonitoringOrganization() {
    return monitoringOrganization;
  }

  /**
   * Returns the hypotheses of this event, immutable
   */
  public Set<EventHypothesis> getHypotheses() {
    return Collections.unmodifiableSet(hypotheses);
  }

  /**
   * Returns the history of final hypotheses of this event, immutable
   */
  public List<FinalEventHypothesis> getFinalEventHypothesisHistory() {
    return Collections.unmodifiableList(finalEventHypothesisHistory);
  }

  /**
   * Returns the history of the preferred hypotheses of this event, immutable
   */
  public List<PreferredEventHypothesis> getPreferredEventHypothesisHistory() {
    return Collections.unmodifiableList(this.preferredEventHypothesisHistory);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Event event = (Event) o;

    if (id != null ? !id.equals(event.id) : event.id != null) {
      return false;
    }
    if (rejectedSignalDetectionAssociations != null ? !rejectedSignalDetectionAssociations
        .equals(event.rejectedSignalDetectionAssociations)
        : event.rejectedSignalDetectionAssociations != null) {
      return false;
    }
    if (monitoringOrganization != null ? !monitoringOrganization
        .equals(event.monitoringOrganization)
        : event.monitoringOrganization != null) {
      return false;
    }
    if (hypotheses != null ? !hypotheses.equals(event.hypotheses) : event.hypotheses != null) {
      return false;
    }
    if (finalEventHypothesisHistory != null ? !finalEventHypothesisHistory
        .equals(event.finalEventHypothesisHistory) : event.finalEventHypothesisHistory != null) {
      return false;
    }
    return preferredEventHypothesisHistory != null ? preferredEventHypothesisHistory
        .equals(event.preferredEventHypothesisHistory)
        : event.preferredEventHypothesisHistory == null;
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
    return "Event{" +
        "id=" + id +
        ", rejectedSignalDetectionAssociations=" + rejectedSignalDetectionAssociations +
        ", monitoringOrganization='" + monitoringOrganization + '\'' +
        ", hypotheses=" + hypotheses +
        ", finalEventHypothesisHistory=" + finalEventHypothesisHistory +
        ", preferredEventHypothesisHistory=" + preferredEventHypothesisHistory +
        '}';
  }
}

