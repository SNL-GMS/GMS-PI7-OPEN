package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link AnalystActionReference} creation.
 */
public class AnalystActionReferenceTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreateNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(AnalystActionReference.class,
        "from", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
  }

  @Test
  public void testCreate() {
    final UUID procStageIntervalId = UUID.randomUUID();
    final UUID procActivityIntervalId = UUID.randomUUID();
    final UUID analystId = UUID.randomUUID();

    AnalystActionReference analystActionRef = AnalystActionReference.from(procStageIntervalId,
        procActivityIntervalId, analystId);

    assertEquals(procStageIntervalId, analystActionRef.getProcessingStageIntervalId());
    assertEquals(procActivityIntervalId, analystActionRef.getProcessingActivityIntervalId());
    assertEquals(analystId, analystActionRef.getAnalystId());
  }

  @Test
  public void testSerialization() throws Exception {
    final AnalystActionReference analystActionRef = AnalystActionReference.from(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    TestUtilities.testSerialization(analystActionRef, AnalystActionReference.class);
  }
}
