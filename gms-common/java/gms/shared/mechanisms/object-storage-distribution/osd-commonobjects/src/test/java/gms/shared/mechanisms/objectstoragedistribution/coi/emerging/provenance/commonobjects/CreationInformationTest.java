package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * Tests {@link CreationInformation} creation and usage semantics
 */
public class CreationInformationTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private final UUID id = UUID.randomUUID();
  private final Instant creationTime = Instant.EPOCH;
  private UUID procStageIntervalId = UUID.randomUUID();
  private final SoftwareComponentInfo swInfo = new SoftwareComponentInfo("sw name", "sw version");

  private final Optional<AnalystActionReference> analystActionReference = Optional.of(
      AnalystActionReference.from(procStageIntervalId, UUID.randomUUID(),
          UUID.randomUUID()));

  private final Optional<ProcessingStepReference> processingStepReference = Optional
      .of(ProcessingStepReference
          .from(procStageIntervalId, UUID.randomUUID(), UUID.randomUUID()));

  @Test
  public void testCreateNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(CreationInformation.class,
        "create", analystActionReference, processingStepReference, swInfo);
  }

  @Test
  public void testMissingAnalystActionReference() {
    CreationInformation.create(Optional.empty(), processingStepReference, swInfo);
  }

  @Test
  public void testMissingProcessingStepReference() {
    CreationInformation.create(analystActionReference, Optional.empty(), swInfo);
  }

  @Test
  public void testMissingAnalystActionProcessingStepReferences() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "Cannot create CreationInformation. It must contain an AnalystActionReference or a ProcessingStepReference");
    CreationInformation.create(Optional.empty(), Optional.empty(), swInfo);
  }

  @Test
  public void testFromNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(CreationInformation.class,
        "from", id, creationTime, analystActionReference, processingStepReference, swInfo);
  }

  @Test
  public void testFrom() {

    CreationInformation creationInformation = CreationInformation
        .from(id, creationTime, analystActionReference, processingStepReference, swInfo);

    assertEquals(id, creationInformation.getId());
    assertEquals(creationTime, creationInformation.getCreationTime());
    assertEquals(analystActionReference, creationInformation.getAnalystActionReference());
    assertEquals(processingStepReference, creationInformation.getProcessingStepReference());
    assertEquals(swInfo, creationInformation.getSoftwareInfo());
  }

  @Test
  public void testEqualsAndHashcode() {
    final CreationInformation a = CreationInformation
        .create(analystActionReference, processingStepReference, swInfo);

    final CreationInformation b = CreationInformation
        .from(a.getId(), a.getCreationTime(), analystActionReference, processingStepReference,
            swInfo);

    assertEquals(a, b);
    assertEquals(b, a);
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void testEqualsExpectInequality() {
    // Different ids
    CreationInformation a = CreationInformation
        .create(analystActionReference, processingStepReference, swInfo);
    CreationInformation b = CreationInformation
        .create(analystActionReference, processingStepReference, swInfo);

    assertNotEquals(a, b);

    // Different Analyst Action Reference
    AnalystActionReference actionRef = AnalystActionReference
        .from(procStageIntervalId, UUID.randomUUID(), UUID.randomUUID());
    Optional<AnalystActionReference> diffActionRef = Optional.of(actionRef);

    b = CreationInformation.create(diffActionRef, processingStepReference, swInfo);

    assertNotEquals(a, b);

    // Different Processing Step Reference
    ProcessingStepReference stepRef = ProcessingStepReference
        .from(procStageIntervalId, UUID.randomUUID(), UUID.randomUUID());
    Optional<ProcessingStepReference> diffStepRef = Optional.of(stepRef);

    b = CreationInformation.create(analystActionReference, diffStepRef, swInfo);

    assertNotEquals(a, b);

    // Different Software Information
    SoftwareComponentInfo diffSwInfo = new SoftwareComponentInfo("diff sw name", "diff sw version");
    b = CreationInformation.create(analystActionReference, processingStepReference, diffSwInfo);

    assertNotEquals(a, b);
  }

  @Test
  public void testSerialization() throws Exception {
    final CreationInformation creationInfo = CreationInformation
        .create(analystActionReference, processingStepReference, swInfo);
    TestUtilities.testSerialization(creationInfo, CreationInformation.class);
  }
}
