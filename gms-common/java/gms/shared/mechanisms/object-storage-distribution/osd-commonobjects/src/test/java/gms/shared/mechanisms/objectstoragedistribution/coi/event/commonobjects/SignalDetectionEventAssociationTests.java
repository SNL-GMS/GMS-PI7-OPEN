package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import java.util.UUID;
import org.junit.Test;

public class SignalDetectionEventAssociationTests {

  private final UUID id = UUID.randomUUID();
  private final UUID eventHypothesisId = UUID.randomUUID();
  private final UUID signalDetectionHypothesisId = UUID.randomUUID();
  private final boolean isRejected = false;

  @Test
  public void testFrom() {
    final SignalDetectionEventAssociation signalDetectionEventAssociation = SignalDetectionEventAssociation
        .from(id, eventHypothesisId, signalDetectionHypothesisId, isRejected);
    assertEquals(id, signalDetectionEventAssociation.getId());
    assertEquals(eventHypothesisId, signalDetectionEventAssociation.getEventHypothesisId());
    assertEquals(signalDetectionHypothesisId,
        signalDetectionEventAssociation.getSignalDetectionHypothesisId());
    assertEquals(isRejected, signalDetectionEventAssociation.isRejected());
  }

  @Test
  public void testCreate() {
    final SignalDetectionEventAssociation signalDetectionEventAssociation = SignalDetectionEventAssociation
        .create(eventHypothesisId, signalDetectionHypothesisId);
    assertEquals(eventHypothesisId, signalDetectionEventAssociation.getEventHypothesisId());
    assertEquals(signalDetectionHypothesisId,
        signalDetectionEventAssociation.getSignalDetectionHypothesisId());
    assertFalse(signalDetectionEventAssociation.isRejected());
  }

  /**
   * Reject method creates a new SignalDetectionEventAssociation with the same id as the rejected
   * association, but with a separate provenance.
   */
  @Test
  public void testReject() {
    final SignalDetectionEventAssociation signalDetectionEventAssociation = SignalDetectionEventAssociation
        .create(eventHypothesisId, signalDetectionHypothesisId);
    SignalDetectionEventAssociation signalDetectionEventAssociationRejected = signalDetectionEventAssociation
        .reject();
    assertEquals(signalDetectionEventAssociation.getId(),
        signalDetectionEventAssociationRejected.getId());
    assertEquals(signalDetectionEventAssociation.getEventHypothesisId(),
        signalDetectionEventAssociationRejected.getEventHypothesisId());
    assertEquals(signalDetectionEventAssociation.getSignalDetectionHypothesisId(),
        signalDetectionEventAssociationRejected.getSignalDetectionHypothesisId());
    assertFalse(signalDetectionEventAssociation.isRejected());
    assertTrue(signalDetectionEventAssociationRejected.isRejected());
  }

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(EventTestFixtures.signalDetectionEventAssociation,
        SignalDetectionEventAssociation.class);
  }

}
