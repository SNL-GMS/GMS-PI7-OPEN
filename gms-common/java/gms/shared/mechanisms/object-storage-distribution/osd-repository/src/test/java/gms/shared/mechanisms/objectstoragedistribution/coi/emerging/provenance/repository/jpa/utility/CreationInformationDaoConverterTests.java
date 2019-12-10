package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.utility;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.CreationInformationDao;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@link CreationInformationDaoConverter}.
 */
public class CreationInformationDaoConverterTests {

  private final UUID creationInformationId = UUID.randomUUID();

  private final Instant creationTime = Instant.now();

  private final UUID processingStageIntervalId = UUID.randomUUID();

  private final UUID processingActivityIntervalId = UUID.randomUUID();
  private final UUID analystId = UUID.randomUUID();

  private final UUID processingSequenceIntervalId = UUID.randomUUID();
  private final UUID processingStepId = UUID.randomUUID();

  private final AnalystActionReference analystActionReference =
      AnalystActionReference
          .from(processingStageIntervalId, processingActivityIntervalId, analystId);

  private final ProcessingStepReference processingStepReference =
      ProcessingStepReference
          .from(processingStageIntervalId, processingSequenceIntervalId, processingStepId);

  private final SoftwareComponentInfo softwareComponentInfo = new SoftwareComponentInfo("TestName",
      "TestVersion");
  private final CreationInformation creationInformationWithAnalystActionReference =
      CreationInformation
          .from(creationInformationId, creationTime, Optional.of(analystActionReference),
              Optional.empty(), softwareComponentInfo);
  private final CreationInformation creationInformationWithProcessingStepReference =
      CreationInformation.from(creationInformationId, creationTime, Optional.empty(),
          Optional.of(processingStepReference), softwareComponentInfo);

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testToDaoNullExpectIllegalArgumentException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create CreationInformationDao from a null CreationInformation");
    CreationInformationDaoConverter.toDao(null);
  }

  @Test
  public void testFromDaoNullQcMaskDaoExpectIllegalArgumentException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create CreationInformation from a null CreationInformationDao");
    CreationInformationDaoConverter.fromDao(null);
  }

  @Test
  public void testToDao() {
    CreationInformationDao dao = CreationInformationDaoConverter
        .toDao(creationInformationWithAnalystActionReference);

    assertNotNull(dao);
    assertEquals(creationInformationWithAnalystActionReference.getId(),
        dao.getId());
    assertEquals(creationInformationWithAnalystActionReference.getCreationTime(),
        dao.getCreationTime());
    assertEquals(creationInformationWithAnalystActionReference.getSoftwareInfo().getName(),
        dao.getSoftwareComponentName());
    assertEquals(creationInformationWithAnalystActionReference.getSoftwareInfo().getVersion(),
        dao.getSoftwareComponentVersion());

    assertTrue(
        creationInformationWithAnalystActionReference.getAnalystActionReference().isPresent());
    assertEquals(creationInformationWithAnalystActionReference.getAnalystActionReference().get()
            .getProcessingStageIntervalId(),
        dao.getProcessingStageIntervalId());
    assertEquals(creationInformationWithAnalystActionReference.getAnalystActionReference().get()
            .getProcessingActivityIntervalId(),
        dao.getProcessingActivityIntervalId());
    assertEquals(creationInformationWithAnalystActionReference.getAnalystActionReference().get()
            .getAnalystId(),
        dao.getAnalystId());
    Assert.assertNull(dao.getProcessingSequenceIntervalId());
    Assert.assertNull(dao.getProcessingStepId());

    dao = CreationInformationDaoConverter.toDao(creationInformationWithProcessingStepReference);

    assertNotNull(dao);
    assertEquals(creationInformationWithProcessingStepReference.getId(),
        dao.getId());
    assertEquals(creationInformationWithProcessingStepReference.getCreationTime(),
        dao.getCreationTime());
    assertEquals(creationInformationWithProcessingStepReference.getSoftwareInfo().getName(),
        dao.getSoftwareComponentName());
    assertEquals(creationInformationWithProcessingStepReference.getSoftwareInfo().getVersion(),
        dao.getSoftwareComponentVersion());

    assertTrue(
        creationInformationWithProcessingStepReference.getProcessingStepReference().isPresent());
    assertEquals(creationInformationWithProcessingStepReference.getProcessingStepReference().get()
            .getProcessingStageIntervalId(),
        dao.getProcessingStageIntervalId());
    assertEquals(creationInformationWithProcessingStepReference.getProcessingStepReference().get()
            .getProcessingSequenceIntervalId(),
        dao.getProcessingSequenceIntervalId());
    assertEquals(
        creationInformationWithProcessingStepReference.getProcessingStepReference().get()
            .getProcessingStepId(),
        dao.getProcessingStepId());
    Assert.assertNull(dao.getProcessingActivityIntervalId());
    Assert.assertNull(dao.getAnalystId());
  }

  @Test
  public void testToDaoFromDao() {
    CreationInformationDao creationInformationDao = CreationInformationDaoConverter
        .toDao(creationInformationWithAnalystActionReference);
    CreationInformation actualCreationInformation = CreationInformationDaoConverter
        .fromDao(creationInformationDao);
    assertEquals(creationInformationWithAnalystActionReference, actualCreationInformation);

    creationInformationDao = CreationInformationDaoConverter
        .toDao(creationInformationWithProcessingStepReference);
    actualCreationInformation = CreationInformationDaoConverter.fromDao(creationInformationDao);
    assertEquals(creationInformationWithProcessingStepReference, actualCreationInformation);
  }
}
