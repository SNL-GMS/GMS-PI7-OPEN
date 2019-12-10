package gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.CoiTestingEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.ProvenanceRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.dataaccessobjects.CreationInformationDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.repository.jpa.utility.CreationInformationDaoConverter;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests store and query functionality of ProvenanceRepositoryJpa.
 */
public class ProvenanceRepositoryJpaTests {

  public static final String TEST_PERSISTENCE_UNIT_NAME = "emerging-unitDB";

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

  private static EntityManagerFactory entityManagerFactory;
  private ProvenanceRepository provenanceRepositoryJpa;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @BeforeClass
  public static void init() {
    entityManagerFactory = CoiTestingEntityManagerFactory.createTesting();
  }

  @Before
  public void setUp() {
    provenanceRepositoryJpa = new ProvenanceRepositoryJpa(entityManagerFactory);
  }

  @After
  public void tearDown() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      entityManager.getTransaction().begin();
      entityManager.createQuery("DELETE FROM CreationInformationDao").executeUpdate();
      entityManager.getTransaction().commit();
    } finally {
      entityManager.close();
    }

    provenanceRepositoryJpa = null;
  }

  @AfterClass
  public static void shutdown() {
    entityManagerFactory.close();
  }

  /**
   * Small check that the repository throws the appropriate exception when null is passed in.
   *
   * @throws Exception any jpa exception
   */
  @Test
  public void testStoreNullCreationInformation() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot store a null CreationInformation");
    provenanceRepositoryJpa.store(null);
  }

  @Test
  public void testFindByNullCreationInformationId() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot query with a null CreationInformation Id");
    provenanceRepositoryJpa.findCreationInformationById(null);
  }

  @Test
  public void testFindByDifferentCreationInformationId() {

    provenanceRepositoryJpa.store(creationInformationWithAnalystActionReference);

    List<CreationInformation> creationInformations =
        provenanceRepositoryJpa.findCreationInformationById(UUID.randomUUID());
    assertNotNull(creationInformations);
    assertTrue(creationInformations.isEmpty());
  }

  @Test
  public void testFindByCreationInformationId() {

    provenanceRepositoryJpa.store(creationInformationWithAnalystActionReference);

    List<CreationInformation> creationInformations =
        provenanceRepositoryJpa
            .findCreationInformationById(creationInformationWithAnalystActionReference.getId());
    assertNotNull(creationInformations);
    assertEquals(1, creationInformations.size());

    // validate input and retreived CreationInformation
    assertEquals(creationInformationWithAnalystActionReference, creationInformations.get(0));
  }

  /**
   * Test storing a CreationInformation.
   *
   * @throws Exception any jpa exception
   */
  @Test
  public void testStoreAnalystActionCreationInformation() {

    provenanceRepositoryJpa.store(creationInformationWithAnalystActionReference);

    assertStored(creationInformationWithAnalystActionReference);
  }

  /**
   * Test storing a CreationInformation.
   *
   * @throws Exception any jpa exception
   */
  @Test
  public void testStoreProcessingStepCreationInformation() {

    provenanceRepositoryJpa.store(creationInformationWithProcessingStepReference);

    assertStored(creationInformationWithProcessingStepReference);
  }

  /**
   * Checks that exactly one CreationInformation object is in the database, and that it matches the
   * input argument (creationInformation).
   *
   * @param creationInformation Expected CreationInformation
   */
  private void assertStored(CreationInformation creationInformation) {

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {

      // First check the CreationInformation was stored correctly
      List<CreationInformationDao> creationInformationDaos =
          entityManager.createQuery("from CreationInformationDao", CreationInformationDao.class)
              .getResultList();

      assertEquals(1, creationInformationDaos.size());

      // Some from the actual results are changed in code.  This is a workaround to avoid
      // trying to check equality while handling the persistence ids auto generated by the database.
      // Instead, just set all ids to the default value and use the standard equals operation.

      final CreationInformationDao epecatedCreationInformationDao =
          CreationInformationDaoConverter.toDao(creationInformation);
      final CreationInformationDao actualCreationInformationDao = creationInformationDaos.get(0);
      actualCreationInformationDao.setDaoId(0);
      assertEquals(epecatedCreationInformationDao, actualCreationInformationDao);
    } finally {

      entityManager.close();
    }
  }
}
