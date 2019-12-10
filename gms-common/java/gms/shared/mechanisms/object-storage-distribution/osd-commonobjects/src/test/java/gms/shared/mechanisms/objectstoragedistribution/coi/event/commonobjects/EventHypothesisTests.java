package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class EventHypothesisTests {

  private UUID id;
  private UUID eventId;
  private UUID parentEventHypothesis;
  private Set<UUID> parentEventHypotheses;
  private boolean isRejected;
  private LocationSolution locationSolution;
  private Set<LocationSolution> locationSoutions;
  private PreferredLocationSolution preferredLocationSolution;
  private SignalDetectionEventAssociation signalDetectionEventAssociation;
  private Set<SignalDetectionEventAssociation> signalDetectionEventAssociations;
  private EventHypothesis eventHypothesis;

  @Before
  public void setup() {
    id = UUID.fromString("407c377a-b6a4-478f-b3cd-5c934ee6b876");
    eventId = UUID.fromString("5432a77a-b6a4-478f-b3cd-5c934ee6b000");
    parentEventHypothesis = UUID
        .fromString("cccaa77a-b6a4-478f-b3cd-5c934ee6b999");
    parentEventHypotheses = Set.of(parentEventHypothesis);
    isRejected = false;
    locationSolution = EventTestFixtures.locationSolution;
    locationSoutions = Set.of(locationSolution);
    preferredLocationSolution = PreferredLocationSolution
        .from(locationSolution);
    signalDetectionEventAssociation = EventTestFixtures.signalDetectionEventAssociation;
    signalDetectionEventAssociations = Set
        .of(signalDetectionEventAssociation);

    eventHypothesis = EventHypothesis.from(id, eventId,
        parentEventHypotheses, isRejected, locationSoutions, preferredLocationSolution,
        signalDetectionEventAssociations);
  }

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testFrom() {
    final EventHypothesis evtHyp = EventHypothesis.from(id, eventId,
        parentEventHypotheses, isRejected, locationSoutions, preferredLocationSolution,
        signalDetectionEventAssociations);
    assertNotNull(evtHyp);
    assertEquals(eventId, evtHyp.getEventId());
    assertEquals(Set.of(parentEventHypothesis), evtHyp.getParentEventHypotheses());
    assertEquals(isRejected, evtHyp.isRejected());
    assertEquals(Set.of(locationSolution), evtHyp.getLocationSolutions());
    assertEquals(Optional.of(preferredLocationSolution), evtHyp.getPreferredLocationSolution());
    assertEquals(Set.of(signalDetectionEventAssociation), evtHyp.getAssociations());
  }

  /**
   * Tests that if isRejected is True, then locationSolutions should be empty. If not empty, throws
   * an IllegalArgumentException.
   */
  @Test
  public void testIsRejectedTrueAndLocationSolutionsNotEmpty() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Expected locationSolutions to be empty when isRejected=true");
    EventHypothesis.from(id, eventId, parentEventHypotheses, true, locationSoutions, null,
        signalDetectionEventAssociations);
  }

  /**
   * Tests that if isRejected is True, then preferredLocationSolution should be null. If not null,
   * throws an IllegalArgumentException.
   */
  @Test
  public void testIsRejectedTrueAndPreferredLocationSolutionNotNull() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Expected preferredLocationSolution to be null when isRejected=true");
    EventHypothesis
        .from(id, eventId, parentEventHypotheses, true, Set.of(), preferredLocationSolution,
            signalDetectionEventAssociations);
  }

  /**
   * Tests that if isRejected is False, then preferredLocationSolution should not be null. If null,
   * throws a NullPointerException.
   */
  @Test
  public void testIsRejectedFalseAndPreferredLocationSolutionNull() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Expected non-null preferredLocationSolution when EventHypothesis is not rejected");
    EventHypothesis.from(id, eventId, parentEventHypotheses, false, locationSoutions, null,
        signalDetectionEventAssociations);
  }

  /**
   * Tests that if isRejected is False, then locationSolutions should contain a
   * preferredLocationSolution. If not, throws an IllegalArgumentException.
   */
  @Test
  public void testIsRejectedFalseAndLocationSolutionHasPreferredLocationSolution() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Expected locationSolutions to contain preferredLocationSolution");
    EventHypothesis
        .from(id, eventId, parentEventHypotheses, false, Set.of(), preferredLocationSolution,
            signalDetectionEventAssociations);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testReturnedParentEventHypothesisImmutable() {
    eventHypothesis.getParentEventHypotheses().add(UUID.randomUUID());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testReturnedLocationSolutionsImmutable() {
    eventHypothesis.getLocationSolutions().add(locationSolution);
  }
}
