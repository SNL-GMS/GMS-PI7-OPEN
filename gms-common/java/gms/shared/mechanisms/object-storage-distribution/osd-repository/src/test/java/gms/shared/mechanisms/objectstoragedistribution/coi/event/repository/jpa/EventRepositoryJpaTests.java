package gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.CoiTestingEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.EventRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class EventRepositoryJpaTests {

  private static final EventRepository repo
      = new EventRepositoryJpa(CoiTestingEntityManagerFactory.createTesting());

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @BeforeClass
  public static void setup() {
    List<Event> errorEvents = new ArrayList<>();

    repo.storeEvents(
        List.of(
            TestFixtures.event,
            TestFixtures.event2,
            TestFixtures.event3,
            TestFixtures.event4,
            TestFixtures.event5,
            TestFixtures.event6,
            TestFixtures.event6a,
            TestFixtures.eventWithFeaturePredictions
        ),
        errorEvents
    );

    System.out.println("---1--- " + TestFixtures.event.getId());
    System.out.println("---2--- " + TestFixtures.event2.getId());
    System.out.println("---3--- " + TestFixtures.event3.getId());
    System.out.println("---4--- " + TestFixtures.event4.getId());
    System.out.println("---5--- " + TestFixtures.event5.getId());
    System.out.println("---6--- " + TestFixtures.event6.getId());
    System.out.println("---6a-- " + TestFixtures.event6a.getId());
    System.out.println("---efp- " + TestFixtures.eventWithFeaturePredictions);
    errorEvents.forEach(e -> System.out.println("------ " + e.getId()));

    assertEquals(List.of(TestFixtures.event6a), errorEvents);
  }

  @Test
  public void testStoreEventsRejectsNullArgs() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot store null events");
    repo.storeEvents(null, new ArrayList<>());
  }

  @Test
  public void testFindEventsByIds() {
    // test with all non-existent ID's, get back empty collection
    Collection<Event> results = repo.findEventsByIds(List.of(UUID.randomUUID(), UUID.randomUUID()));
    assertNotNull(results);
    assertTrue(results.isEmpty());
    // test with some non-existent ID's and one existent ID, get back one Event.
    results = repo.findEventsByIds(
        List.of(TestFixtures.event.getId(), UUID.randomUUID(), UUID.randomUUID()));
    assertNotNull(results);
    assertEquals(1, results.size());
    assertTrue(results.contains(TestFixtures.event));
    // test with both existent ID's, get back both events.
    results = repo
        .findEventsByIds(List.of(TestFixtures.event.getId(), TestFixtures.event2.getId()));
    assertNotNull(results);
    assertEquals(2, results.size());
    assertTrue(results.containsAll(List.of(TestFixtures.event, TestFixtures.event2)));
  }

  @Test
  public void testFindEventsByIdsRejectsNullArg() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot find events by null or empty eventIds");
    repo.findEventsByIds(null);
  }

  @Test
  public void testFindEventsByTimeAndLocationRejectsNullStartTime() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot find events by time and location with null start time");
    repo.findEventsByTimeAndLocation(null, Instant.EPOCH,
        1.0, 2.0, 3.0, 4.0);
  }

  @Test
  public void testFindEventsByTimeAndLocationRejectsNullEndTime() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot find events by time and location with null end time");
    repo.findEventsByTimeAndLocation(Instant.EPOCH, null,
        1.0, 2.0, 3.0, 4.0);
  }

  @Test
  public void testFindEventsByTimeAndLocation() {
    final double
        event1Lat = TestFixtures.location.getLatitudeDegrees(),
        event1Lon = TestFixtures.location.getLongitudeDegrees(),
        event2Lat = TestFixtures.location2.getLatitudeDegrees(),
        event2Lon = TestFixtures.location2.getLongitudeDegrees(),
        minLat = -90.0, maxLat = 90.0, minLon = -180.0, maxLon = 180.0;
    final Instant
        event1Time = TestFixtures.location.getTime(),
        event2Time = TestFixtures.location2.getTime();
    // find nothing because of time range (but lat/lon range is OK)
    Collection<Event> results = repo.findEventsByTimeAndLocation(
        event1Time.minusSeconds(10), event1Time.minusSeconds(5),
        minLat, maxLat, minLon, maxLon);
    assertNotNull(results);
    assertTrue(results.isEmpty());
    // find nothing because of lat/lon range (but time range is OK)
    results = repo.findEventsByTimeAndLocation(
        event1Time, event2Time,
        event2Lat + 1, event2Lat + 2, event2Lon + 1, event2Lon + 2);
    assertNotNull(results);
    assertTrue(results.isEmpty());
    // find event1 by it's time range and all-inclusive lat/lon range (but excludes event2)
    results = repo.findEventsByTimeAndLocation(
        event1Time.minusSeconds(1), event1Time.plusSeconds(1),
        minLat, maxLat, minLon, maxLon);
    assertNotNull(results);
    assertEquals(1, results.size());
    assertTrue(results.contains(TestFixtures.event));
    // find event2 by it's time range and all-inclusive lat/lon range (but excludes event1)
    results = repo.findEventsByTimeAndLocation(
        event2Time.minusSeconds(1), event2Time.plusSeconds(1),
        minLat, maxLat, minLon, maxLon);
    assertNotNull(results);
    assertEquals(1, results.size());
    assertTrue(results.contains(TestFixtures.event2));
    // find just event1 with all-inclusive time range but it's lat/lon range
    results = repo.findEventsByTimeAndLocation(
        event1Time, event2Time,
        event1Lat - 1, event1Lat + 1, event1Lon - 1, event1Lon + 1);
    assertNotNull(results);
    assertEquals(1, results.size());
    assertTrue(results.contains(TestFixtures.event));
    // find just event2 with all-inclusive time range but it's lat/lon range
    results = repo.findEventsByTimeAndLocation(
        event1Time, event2Time,
        event2Lat - 1, event2Lat + 1, event2Lon - 1, event2Lon + 1);
    assertNotNull(results);
    assertEquals(1, results.size());
    assertTrue(results.contains(TestFixtures.event2));
    // find both events
    results = repo.findEventsByTimeAndLocation(
        event1Time, event2Time,
        minLat, maxLat, minLon, maxLon);
    assertNotNull(results);
    assertEquals(2, results.size());
    assertTrue(results.containsAll(List.of(TestFixtures.event, TestFixtures.event2)));
  }

  @Test
  public void testUpdateSingleEvent() {
    Collection<Event> results = repo.findEventsByIds(List.of(TestFixtures.event3.getId()));
    Event eventBefore = results.iterator().next();

    assertEquals(TestFixtures.event3.getId(), eventBefore.getId());
    assertEquals(1, eventBefore.getHypotheses().size());

    // Uses duplicate event hypothesis
    eventBefore.addEventHypothesis(
        eventBefore.getHypotheses().iterator().next(),
        Set.of(),
        Set.of(TestFixtures.locationSolution7),
        PreferredLocationSolution.from(TestFixtures.locationSolution7)
    );

    repo.updateEvents(List.of(eventBefore), new ArrayList<>());

    results = repo.findEventsByIds(List.of(TestFixtures.event3.getId()));

    assertEquals(1, results.size());
    Event eventAfter = results.iterator().next();
    assertEquals(eventBefore.getId(), eventAfter.getId());
    assertEquals(2, eventAfter.getHypotheses().size());

    results = repo.findEventsByIds(List.of(TestFixtures.event4.getId()));
    eventBefore = results.iterator().next();

    assertEquals(TestFixtures.event4.getId(), eventBefore.getId());
    assertEquals(1, eventBefore.getHypotheses().size());

    EventHypothesis finalHypotheses = eventBefore.getHypotheses().iterator().next();

    eventBefore.markFinal(finalHypotheses);

    repo.updateEvents(List.of(eventBefore), new ArrayList<>());

    results = repo.findEventsByIds(List.of(TestFixtures.event4.getId()));

    assertEquals(1, results.size());
    eventAfter = results.iterator().next();
    assertEquals(eventBefore.getId(), eventAfter.getId());
    assertEquals(eventAfter.getFinal().get(), finalHypotheses);

    results = repo.findEventsByIds(List.of(TestFixtures.event5.getId()));
    eventBefore = results.iterator().next();

    assertEquals(TestFixtures.event5.getId(), eventBefore.getId());
    assertEquals(1, eventBefore.getHypotheses().size());

    EventHypothesis preferredHypotheses = eventBefore.getHypotheses().iterator().next();

    eventBefore.markPreferred(preferredHypotheses, UUID.randomUUID());

    repo.updateEvents(List.of(eventBefore), new ArrayList<>());

    results = repo.findEventsByIds(List.of(TestFixtures.event5.getId()));

    assertEquals(1, results.size());
    eventAfter = results.iterator().next();
    assertEquals(eventBefore.getId(), eventAfter.getId());
    assertEquals(eventAfter.getOverallPreferred(), preferredHypotheses);
  }

  @Test
  public void testMismatchOrgUpdate() {
    Collection<Event> results = repo.findEventsByIds(List.of(TestFixtures.event6.getId()));
    Event eventBefore = results.iterator().next();

    assertEquals(TestFixtures.event6.getId(), eventBefore.getId());
    assertEquals(1, eventBefore.getHypotheses().size());

    List<Event> errorEvents = new ArrayList<>();
    repo.updateEvents(List.of(TestFixtures.event6a), errorEvents);
    assertEquals(List.of(TestFixtures.event6a), errorEvents);
  }

  @Test
  public void testFailIfUpdateMissingEvent() {
    Collection<Event> results = repo.findEventsByIds(List.of(TestFixtures.event6.getId()));
    Event eventBefore = results.iterator().next();

    assertEquals(TestFixtures.event6.getId(), eventBefore.getId());
    assertEquals(1, eventBefore.getHypotheses().size());

    List<Event> errorEvents = new ArrayList<>();
    repo.updateEvents(List.of(TestFixtures.event6a, TestFixtures.unstoredEvent), errorEvents);

    assertEquals(List.of(TestFixtures.event6a, TestFixtures.unstoredEvent), errorEvents);
  }

  @Test
  public void testUpdateNullEvents() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Cannot update null events");

    repo.updateEvents(null, new ArrayList<>());
  }

  @Test
  public void testStoreEventsWithFeaturePredictions() {
    List<Event> errorEvents = new ArrayList<>();

    // Store event that already exists.
    repo.storeEvents(List.of(TestFixtures.eventWithFeaturePredictions), errorEvents);
    assertTrue(!errorEvents.isEmpty());
  }

  @Test
  public void testStoreEventsWithFeaturePredictionsWithOptionalValues() {
    List<Event> errorEvents = new ArrayList<>();

    repo.storeEvents(List.of(TestFixtures.eventNoInstantValue), errorEvents);
    assertTrue(errorEvents.isEmpty());
  }

  @Test
  @Ignore
  public void testAddFeaturePredictions() {
    List<Event> errorEvents = new ArrayList<>();
    Event origEvent = TestFixtures.eventWithFeaturePredictions;
    Event updatedEvent = TestFixtures.eventWithFeaturePredictionsModified;

    // Add feature predictions to existing event hypotheses.
    repo.updateEvents(List.of(updatedEvent), errorEvents);

    assertEquals(0, errorEvents.size());

    // Retrieve the updated event.
    Collection<Event> results = repo.findEventsByIds(List.of(origEvent.getId()));

    assertTrue(results.contains(TestFixtures.eventWithFeaturePredictionsModified));
  }

  @Test
  @Ignore
  public void testAddFeaturePredictions2() {
    List<Event> errorEvents = new ArrayList<>();
    Event origEvent = TestFixtures.eventWithFeaturePredictions;
    Event updatedEvent = TestFixtures.eventWithFeaturePredictions3;

    // Add feature predictions to existing event hypotheses.
    repo.updateEvents(List.of(updatedEvent), errorEvents);

    assertEquals(0, errorEvents.size());

    // Retrieve the updated event.
    Collection<Event> results = repo.findEventsByIds(List.of(origEvent.getId()));

    assertTrue(results.contains(TestFixtures.eventWithFeaturePredictions3));
  }
}
