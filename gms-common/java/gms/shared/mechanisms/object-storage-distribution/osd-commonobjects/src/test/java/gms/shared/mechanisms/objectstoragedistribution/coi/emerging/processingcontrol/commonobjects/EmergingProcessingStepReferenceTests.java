package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Tests {@link ProcessingStepReference} creation
 */
public class EmergingProcessingStepReferenceTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testFromNullParameters() throws Exception {
    final UUID procStageIntervalId = UUID.randomUUID();
    final UUID procSeqIntervalId = UUID.randomUUID();
    final UUID procStepId = UUID.randomUUID();

    TestUtilities.checkStaticMethodValidatesNullArguments(ProcessingStepReference.class, "from",
        procStageIntervalId, procSeqIntervalId, procStepId);
  }

  @Test
  public void testFrom() {
    final UUID procStageIntervalId = UUID.randomUUID();
    final UUID procSeqIntervalId = UUID.randomUUID();
    final UUID procStepId = UUID.randomUUID();

    ProcessingStepReference procStepRef = ProcessingStepReference.from(procStageIntervalId,
        procSeqIntervalId, procStepId);

    assertEquals(procStageIntervalId, procStepRef.getProcessingStageIntervalId());
    assertEquals(procSeqIntervalId, procStepRef.getProcessingSequenceIntervalId());
    assertEquals(procStepId, procStepRef.getProcessingStepId());
  }

  @Test
  public void testSerialization() throws Exception {
    final ProcessingStepReference procStepRef = ProcessingStepReference.from(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    TestUtilities.testSerialization(procStepRef, ProcessingStepReference.class);
  }
}
