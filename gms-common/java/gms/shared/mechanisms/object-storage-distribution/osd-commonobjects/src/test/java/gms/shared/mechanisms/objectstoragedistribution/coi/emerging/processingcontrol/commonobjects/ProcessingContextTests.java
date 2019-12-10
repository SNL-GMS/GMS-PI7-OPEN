package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import java.util.Optional;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link ProcessingContext} creation.
 */
public class ProcessingContextTests {

  private final UUID procStageIntervalId = UUID
      .fromString("aef6b278-98f6-4bc8-806f-eaff27fd6619");
  private final UUID procSeqIntervalId = UUID.randomUUID();
  private final UUID procStepIntervalId = UUID.randomUUID();
  private final UUID procActivityIntervalId = UUID.randomUUID();
  private final UUID analystId = UUID.randomUUID();
  private final StorageVisibility visibility = StorageVisibility.PUBLIC;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCreateAutomaticNullParameters() throws Exception {
    final UUID procStageIntervalId = UUID.randomUUID();
    final UUID procSeqIntervalId = UUID.randomUUID();
    final UUID procStepId = UUID.randomUUID();
    final StorageVisibility visibility = StorageVisibility.PUBLIC;

    TestUtilities.checkStaticMethodValidatesNullArguments(ProcessingContext.class,
        "createAutomatic", procStageIntervalId, procSeqIntervalId, procStepId, visibility);
  }

  @Test
  public void testCreateAutomatic() {
    final UUID procStageIntervalId = UUID.randomUUID();
    final UUID procSeqIntervalId = UUID.randomUUID();
    final UUID procStepId = UUID.randomUUID();
    final StorageVisibility visibility = StorageVisibility.PUBLIC;

    ProcessingContext context = ProcessingContext
        .createAutomatic(procStageIntervalId, procSeqIntervalId, procStepId, visibility);

    assertEquals(visibility, context.getStorageVisibility());

    final ProcessingStepReference expectedProcStepRef = ProcessingStepReference
        .from(procStageIntervalId,
            procSeqIntervalId, procStepId);

    Optional<ProcessingStepReference> actualProcStepRef = context.getProcessingStepReference();
    assertTrue(actualProcStepRef.isPresent());
    assertEquals(expectedProcStepRef, actualProcStepRef.get());
  }

  @Test
  public void testCreateInteractiveNullParameters() throws Exception {
    final UUID procStageIntervalId = UUID.randomUUID();
    final UUID procActivityIntervalId = UUID.randomUUID();
    final UUID analystId = UUID.randomUUID();
    final StorageVisibility visibility = StorageVisibility.PRIVATE;

    TestUtilities.checkStaticMethodValidatesNullArguments(ProcessingContext.class,
        "createInteractive", procStageIntervalId, procActivityIntervalId, analystId, visibility);
  }

  @Test
  public void testCreateInteractive() {
    final UUID procStageIntervalId = UUID.randomUUID();
    final UUID procActivityIntervalId = UUID.randomUUID();
    final UUID analystId = UUID.randomUUID();
    final StorageVisibility visibility = StorageVisibility.PRIVATE;

    ProcessingContext context = ProcessingContext
        .createInteractive(procStageIntervalId, procActivityIntervalId, analystId, visibility);

    assertEquals(visibility, context.getStorageVisibility());

    final AnalystActionReference expectedAnalystActionRef = AnalystActionReference
        .from(procStageIntervalId,
            procActivityIntervalId, analystId);

    Optional<AnalystActionReference> actualAnalystActionRef = context.getAnalystActionReference();
    assertTrue(actualAnalystActionRef.isPresent());
    assertEquals(expectedAnalystActionRef, actualAnalystActionRef.get());
  }

  @Test
  public void testCreateInteractiveInitiatedAutomaticNullParameters()
      throws Exception {
    final UUID procStageIntervalId = UUID.randomUUID();
    final UUID procActivityIntervalId = UUID.randomUUID();
    final UUID analystId = UUID.randomUUID();
    final UUID procSeqIntervalId = UUID.randomUUID();
    final UUID procStepId = UUID.randomUUID();
    final StorageVisibility visibility = StorageVisibility.PRIVATE;

    TestUtilities.checkStaticMethodValidatesNullArguments(ProcessingContext.class,
        "createInteractiveInitiatedAutomatic", procStageIntervalId, procActivityIntervalId,
        analystId,
        procSeqIntervalId, procStepId, visibility);
  }

  @Test
  public void testCreateInteractiveInitiatedAutomatic() {
    final UUID procStageIntervalId = UUID.randomUUID();
    final UUID procActivityIntervalId = UUID.randomUUID();
    final UUID analystId = UUID.randomUUID();
    final UUID procSeqIntervalId = UUID.randomUUID();
    final UUID procStepId = UUID.randomUUID();
    final StorageVisibility visibility = StorageVisibility.PRIVATE;

    ProcessingContext context = ProcessingContext
        .createInteractiveInitiatedAutomatic(procStageIntervalId, procActivityIntervalId, analystId,
            procSeqIntervalId, procStepId, visibility);

    assertEquals(visibility, context.getStorageVisibility());

    final AnalystActionReference expectedAnalystActionRef = AnalystActionReference
        .from(procStageIntervalId,
            procActivityIntervalId, analystId);

    Optional<AnalystActionReference> actualAnalystActionRef = context.getAnalystActionReference();
    assertTrue(actualAnalystActionRef.isPresent());
    assertEquals(expectedAnalystActionRef, actualAnalystActionRef.get());

    final ProcessingStepReference expectedProcStepRef = ProcessingStepReference
        .from(procStageIntervalId,
            procSeqIntervalId, procStepId);

    Optional<ProcessingStepReference> actualProcStepRef = context.getProcessingStepReference();
    assertTrue(actualProcStepRef.isPresent());
    assertEquals(expectedProcStepRef, actualProcStepRef.get());
  }

  @Test
  public void testSerializationForAutomaticContext() throws Exception {
    ProcessingContext processingContext = ProcessingContext.createAutomatic(
        procStageIntervalId, procSeqIntervalId, procStepIntervalId, visibility);
    TestUtilities.testSerialization(processingContext, ProcessingContext.class);
  }

  @Test
  public void testSerializationForInteractiveContext() throws Exception {
    ProcessingContext processingContext = ProcessingContext
        .createInteractive(procStageIntervalId, procActivityIntervalId, analystId, visibility);
    TestUtilities.testSerialization(processingContext, ProcessingContext.class);
  }

  @Test
  public void testSerializationForDualContext() throws Exception {
    ProcessingContext processingContext = ProcessingContext
        .createInteractiveInitiatedAutomatic(procStageIntervalId, procActivityIntervalId, analystId,
            procSeqIntervalId, procStepIntervalId, StorageVisibility.PRIVATE);
    TestUtilities.testSerialization(processingContext, ProcessingContext.class);
  }
}
