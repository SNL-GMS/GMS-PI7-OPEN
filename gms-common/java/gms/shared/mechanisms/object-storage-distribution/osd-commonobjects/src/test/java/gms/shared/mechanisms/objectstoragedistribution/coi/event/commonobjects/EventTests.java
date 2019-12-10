package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class EventTests {

  private UUID eventId, processingStageId;
  private Set<UUID> rejectedSignalDetectionAssociations;
  private String monitoringOrg;
  private Set<LocationSolution> locationSolutions;
  private PreferredLocationSolution preferredSolution;
  private EventHypothesis hypothesis;
  private List<PreferredEventHypothesis> preferredHistory;
  private Event event;
  private EventHypothesis unknownHypothesis;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setup() {
    eventId = UUID.randomUUID();
    processingStageId = UUID.randomUUID();
    rejectedSignalDetectionAssociations = Set.of(UUID.randomUUID());
    monitoringOrg = "monitoring org";
    final LocationSolution locationSolution = EventTestFixtures.locationSolution;
    locationSolutions = Set.of(locationSolution);
    preferredSolution = PreferredLocationSolution.from(locationSolution);
    final UUID hypothesisId = UUID.randomUUID();
    hypothesis = EventHypothesis.from(
        hypothesisId, eventId, Set.of(), false, locationSolutions,
        preferredSolution,
        Set.of(SignalDetectionEventAssociation.create(hypothesisId, UUID.randomUUID())));
    preferredHistory = List.of(PreferredEventHypothesis.from(processingStageId, hypothesis));
    event = Event.from(eventId, rejectedSignalDetectionAssociations,
        monitoringOrg, Set.of(hypothesis), List.of(), preferredHistory);
    unknownHypothesis = EventHypothesis.from(
        UUID.randomUUID(),
        // below: random eventId
        UUID.randomUUID(), Set.of(), false, locationSolutions,
        preferredSolution, Set.of());
  }

  @Test
  public void testEqualsHashcode() {
    TestUtilities.checkClassEqualsAndHashcode(Event.class);
  }

  @Test
  public void testFromRejectsNullArgs() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(Event.class, "from",
        eventId, rejectedSignalDetectionAssociations, monitoringOrg, Set.of(hypothesis),
        List.of(), preferredHistory);
  }

  @Test
  public void testFrom() {
    final Event event = Event.from(eventId, rejectedSignalDetectionAssociations,
        monitoringOrg, Set.of(hypothesis), List.of(), preferredHistory);
    assertNotNull(event);
    assertEquals(eventId, event.getId());
    assertEquals(rejectedSignalDetectionAssociations, event.getRejectedSignalDetectionAssociations());
    assertEquals(monitoringOrg, event.getMonitoringOrganization());
    assertEquals(Set.of(hypothesis), event.getHypotheses());
    assertEquals(List.of(), event.getFinalEventHypothesisHistory());
    assertEquals(preferredHistory, event.getPreferredEventHypothesisHistory());
    assertEquals(hypothesis, event.getOverallPreferred());
  }

  @Test
  public void testFromEmptyMonitoringOrg() {
    expectException(IllegalArgumentException.class,
        "Cannot create Event with null or blank monitoringOrganization");
    Event.from(eventId, rejectedSignalDetectionAssociations,
        "", Set.of(hypothesis), List.of(), preferredHistory);
  }

  @Test
  public void testFromEmptyHypotheses() {
    expectException(IllegalArgumentException.class,
        "Event must have at least one hypothesis");
    Event.from(eventId, rejectedSignalDetectionAssociations,
        monitoringOrg, Set.of(), List.of(), preferredHistory);
  }

  @Test
  public void testFromHypothesisHasEventIdNotMatchingEvent() {
    expectException(IllegalArgumentException.class,
        "EventHypothesis has eventId not matching parent Event");
    Event.from(eventId, rejectedSignalDetectionAssociations,
        monitoringOrg, Set.of(unknownHypothesis), List.of(), preferredHistory);
  }

  @Test
  public void testCreateRejectsNullArgs() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(Event.class, "create",
        Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID()), locationSolutions,
        preferredSolution, monitoringOrg, processingStageId);
  }

  @Test
  public void testCreate() {
    final Set<UUID> parentEventHypotheses = Set.of(UUID.randomUUID(), UUID.randomUUID());
    final Set<UUID> associatedSdhIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
    final Event e = Event.create(parentEventHypotheses, associatedSdhIds,
        locationSolutions, preferredSolution, monitoringOrg, processingStageId);
    assertNotNull(e);
    assertEquals(monitoringOrg, e.getMonitoringOrganization());
    assertFalse(e.isRejected());  // event has not been rejected
    // assert properties of the only EventHypothesis that gets created internally
    assertNotNull(e.getHypotheses());
    assertEquals(1, e.getHypotheses().size());  // expect only initial hypothesis
    final EventHypothesis initialHyp = e.getHypotheses().iterator().next();
    assertNotNull(initialHyp);
    assertEquals(e.getId(), initialHyp.getEventId());
    assertEquals(parentEventHypotheses, initialHyp.getParentEventHypotheses());
    assertEquals(false, initialHyp.isRejected());
    assertEquals(locationSolutions, initialHyp.getLocationSolutions());
    final Optional<PreferredLocationSolution> preferredLocationSolution = initialHyp.getPreferredLocationSolution();
    assertTrue("Expected initial hypothesis from Event.create to have a preferred location solution",
        preferredLocationSolution.isPresent());
    assertEquals(preferredSolution, preferredLocationSolution.get());
    final Set<SignalDetectionEventAssociation> expectedAssociations = associatedSdhIds.stream()
        .map(id -> SignalDetectionEventAssociation.create(initialHyp.getId(), id))
        .collect(Collectors.toSet());
    assertAssociationSetsHaveSameState(expectedAssociations, initialHyp.getAssociations());
    assertEquals(initialHyp, e.getOverallPreferred());
    // assert the hypothesis is in the only entry in the preferred history
    assertEquals("Expect one entry in preferred history after Event.create",
        1, e.getPreferredEventHypothesisHistory().size());
    final PreferredEventHypothesis onlyPreferred = e.getPreferredEventHypothesisHistory().get(0);
    assertNotNull(onlyPreferred);
    assertEquals(initialHyp, onlyPreferred.getEventHypothesis());
    assertEquals(processingStageId, onlyPreferred.getProcessingStageId());
    // assert the final history is empty (no hypothesis has never been marked final)
    assertTrue("Expected no final hypothesis history after Event.create because nothing has ever been marked final",
        e.getFinalEventHypothesisHistory().isEmpty());
  }

  @Test
  public void testRejectNullHypothesis() throws Exception {
    TestUtilities.checkMethodValidatesNullArguments(event, "reject", hypothesis);
  }

  @Test
  public void testRejectHypothesisNotPartOfEvent() {
    expectException(IllegalArgumentException.class,
        "hypothesis to reject must be contained in overall set of hypotheses");
    event.reject(unknownHypothesis);
  }

  @Test
  public void testReject() {
    final EventHypothesis rejectedHyp = event.reject(hypothesis);
    assertNotNull(rejectedHyp);
    assertTrue(rejectedHyp.isRejected());
    assertEquals(Set.of(hypothesis.getId()), rejectedHyp.getParentEventHypotheses());
    assertTrue(event.getHypotheses().contains(rejectedHyp));
  }

  @Test
  public void testAddEventHypothesisRejectsNullArgs() throws Exception {
    TestUtilities.checkMethodValidatesNullArguments(event, "addEventHypothesis",
        hypothesis, Set.of(UUID.randomUUID()), locationSolutions, preferredSolution);
  }

  @Test
  public void testAddEventHypothesisNotPartOfEvent() {
    expectException(IllegalArgumentException.class,
        "parentEventHypothesis must be contained in overall set of hypotheses");
    final EventHypothesis unknownHypothesis = EventHypothesis.from(
        UUID.randomUUID(), eventId, Set.of(), false, locationSolutions,
        preferredSolution, Set.of());
    event.addEventHypothesis(unknownHypothesis, Set.of(), locationSolutions, preferredSolution);
  }

  @Test
  public void testAddEventHypothesisEmptyLocationSolutions() {
    expectException(IllegalArgumentException.class,
        "Cannot add event hypothesis with null or empty location solutions");
    event.addEventHypothesis(hypothesis, Set.of(), Set.of(), preferredSolution);
  }

  @Test
  public void testAddEventHypothesis() {
    final UUID associatedSdhId = UUID.randomUUID();
    final EventHypothesis newEh = event.addEventHypothesis(hypothesis,
        Set.of(associatedSdhId), locationSolutions, preferredSolution);
    assertNotNull(newEh);
    assertTrue(event.getHypotheses().contains(newEh));
    assertEquals(event.getId(), newEh.getEventId());
    assertEquals(Set.of(hypothesis.getId()), newEh.getParentEventHypotheses());
    assertEquals(locationSolutions, newEh.getLocationSolutions());
    assertTrue(newEh.getPreferredLocationSolution().isPresent());
    assertEquals(preferredSolution, newEh.getPreferredLocationSolution().get());
    assertEquals(1, newEh.getAssociations().size());
    final SignalDetectionEventAssociation expectedAssoc = SignalDetectionEventAssociation.create(
        newEh.getId(), associatedSdhId);
    final SignalDetectionEventAssociation onlyAssoc = newEh.getAssociations().iterator().next();
    assertTrue(expectedAssoc.hasSameState(onlyAssoc));
  }

  @Test
  public void testMarkPreferredRejectsNullArgs() throws Exception {
    TestUtilities.checkMethodValidatesNullArguments(event,
        "markPreferred",
        hypothesis, processingStageId);
  }

  @Test
  public void testMarkPreferredHypothesisNotPartOfEvent() {
    expectException(IllegalArgumentException.class,
        "hypothesis to be marked as preferred must be contained in overall set of hypotheses");
    event.markPreferred(unknownHypothesis, processingStageId);
  }

  @Test
  public void testMarkPreferred() {
    final EventHypothesis newEh = event.addEventHypothesis(
        hypothesis, Set.of(UUID.randomUUID()), locationSolutions, preferredSolution);
    assertNotNull(newEh);
    final UUID differentProcessingStage = UUID.randomUUID();
    event.markPreferred(newEh, differentProcessingStage);
    final List<PreferredEventHypothesis> preferredHistory = event.getPreferredEventHypothesisHistory();
    assertFalse(preferredHistory.isEmpty());
    final PreferredEventHypothesis latestPreferred = preferredHistory.get(preferredHistory.size() - 1);
    assertNotNull(latestPreferred);
    assertEquals(newEh, latestPreferred.getEventHypothesis());
    assertEquals(differentProcessingStage, latestPreferred.getProcessingStageId());
    assertEquals(newEh, event.getOverallPreferred());
  }

  @Test
  public void testGetPreferredForProcessingStageRejectsNullArgs() throws Exception {
    TestUtilities.checkMethodValidatesNullArguments(event,
        "getPreferredForProcessingStage",
        processingStageId);
  }

  @Test
  public void testGetPreferredForProcessingStage() {
    final EventHypothesis newEh = addHypothesis();
    assertNotNull(newEh);
    final UUID differentProcessingStage = UUID.randomUUID();
    event.markPreferred(newEh, differentProcessingStage);
    Optional<EventHypothesis> preferredForStage
        = event.getPreferredForProcessingStage(differentProcessingStage);
    assertNotNull(preferredForStage);
    assertTrue(preferredForStage.isPresent());
    assertEquals(newEh, preferredForStage.get());
    // ask for an EH but an unknown processing stage, get back empty.
    preferredForStage = event.getPreferredForProcessingStage(UUID.randomUUID());
    assertNotNull(preferredForStage);
    assertFalse(preferredForStage.isPresent());
  }

  @Test
  public void testMarkFinalRejectsNullArg() throws Exception {
    TestUtilities.checkMethodValidatesNullArguments(event, "markFinal",
        hypothesis);
  }

  @Test
  public void testGetFinalAndMarkFinal() {
    // initially, nothing is marked final
    Optional<EventHypothesis> finalEh = event.getFinal();
    assertNotNull(finalEh);
    assertFalse(finalEh.isPresent());
    // and the final history is empty
    assertTrue(event.getFinalEventHypothesisHistory().isEmpty());
    // add a hypothesis.  Verify there is still no final
    final EventHypothesis newEh = addHypothesis();
    assertNotNull(newEh);
    finalEh = event.getFinal();
    assertNotNull(finalEh);
    assertFalse(finalEh.isPresent());
    assertTrue(event.getFinalEventHypothesisHistory().isEmpty());
    // now mark the hypothesis as final
    event.markFinal(newEh);
    finalEh = event.getFinal();
    assertNotNull(finalEh);
    assertTrue(finalEh.isPresent());
    // check the final is in the history now
    assertEquals(1, event.getFinalEventHypothesisHistory().size());
    final FinalEventHypothesis finalHistory = event.getFinalEventHypothesisHistory().get(0);
    assertNotNull(finalHistory);
    assertEquals(newEh, finalHistory.getEventHypothesis());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testReturnedRejectedSignalDetectionAssociationsImmutable() {
    event.getRejectedSignalDetectionAssociations().add(UUID.randomUUID());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testReturnedHypothesesImmutable() {
    event.getHypotheses().add(hypothesis);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testReturnedFinalEventHypothesisHistoryImmutable() {
    event.getFinalEventHypothesisHistory().add(FinalEventHypothesis.from(hypothesis));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testReturnedPreferredEventHypothesisHistoryImmutable() {
    event.getPreferredEventHypothesisHistory().add(
        PreferredEventHypothesis.from(UUID.randomUUID(), hypothesis));
  }

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(EventTestFixtures.event, Event.class);
  }

  private void expectException(Class<? extends Throwable> c, String msgSubstring) {
    assertFalse("No point in asserting a msgSubstring that is empty (always matches)",
        msgSubstring.isEmpty());
    exception.expect(c);
    exception.expectMessage(msgSubstring);
  }

  private EventHypothesis addHypothesis() {
    return event.addEventHypothesis(
        hypothesis, Set.of(UUID.randomUUID()), locationSolutions, preferredSolution);
  }

  private static void assertAssociationSetsHaveSameState(
      Set<SignalDetectionEventAssociation> expected,
      Set<SignalDetectionEventAssociation> actual) {

    assertEquals("Expect sizes to match for assocation sets to have same state",
        expected.size(), actual.size());
    for (SignalDetectionEventAssociation expectedAssoc : expected) {
      final Optional<SignalDetectionEventAssociation> result = actual.stream()
          .filter(assoc -> assoc.hasSameState(expectedAssoc))
          .findAny();
      assertTrue("Expected to find with same state this object: "
          + expectedAssoc + " in set: " + actual , result.isPresent());
    }
  }


}
