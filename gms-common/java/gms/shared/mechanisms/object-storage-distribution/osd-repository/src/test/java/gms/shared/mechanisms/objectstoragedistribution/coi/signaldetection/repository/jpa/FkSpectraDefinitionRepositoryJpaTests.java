package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.CoiTestingEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.FkSpectraDefinitionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.FkSpectraDefinitionDao;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class FkSpectraDefinitionRepositoryJpaTests {

  private EntityManagerFactory entityManagerFactory;

  private FkSpectraDefinitionRepository fkSpectraDefinitionRepositoryJpa;

  private final FkSpectraDefinition fkSpectraDefinitionA = TestFixtures.FK_SPECTRA_DEFINITION;

  private final FkSpectraDefinition fkSpectraDefinitionB = TestFixtures.FK_SPECTRA_DEFINITION_2;

  @BeforeEach
  public void setUp() {
    entityManagerFactory = CoiTestingEntityManagerFactory.createTesting();
    fkSpectraDefinitionRepositoryJpa = FkSpectraDefinitionRepositoryJpa
        .create(entityManagerFactory);
  }

  @AfterEach
  public void tearDown() {
    entityManagerFactory.close();
    entityManagerFactory = null;
    fkSpectraDefinitionRepositoryJpa = null;
  }

  @Test
  public void testCreateValidation() {
    assertThrows(NullPointerException.class, () -> FkSpectraDefinitionRepositoryJpa.create(null));
  }

  @Test
  public void testStoreValidation() {
    assertThrows(NullPointerException.class, () -> fkSpectraDefinitionRepositoryJpa.store(null));
    assertDoesNotThrow(() -> fkSpectraDefinitionRepositoryJpa.store(fkSpectraDefinitionA));
  }

  @Test
  public void testStore() {
    fkSpectraDefinitionRepositoryJpa.store(fkSpectraDefinitionA);
    assertStored(fkSpectraDefinitionA);
  }

  @Test
  public void testStoreDoesNotCreateDuplicates() {
    fkSpectraDefinitionRepositoryJpa.store(fkSpectraDefinitionA);
    fkSpectraDefinitionRepositoryJpa.store(fkSpectraDefinitionA);
    assertStored(fkSpectraDefinitionA);
  }

  @Test
  public void testRetrieveAll() {
    fkSpectraDefinitionRepositoryJpa.store(fkSpectraDefinitionA);
    fkSpectraDefinitionRepositoryJpa.store(fkSpectraDefinitionB);

    Collection<FkSpectraDefinition> fkSpectraDefinitions =
        fkSpectraDefinitionRepositoryJpa.retrieveAll();

    assertNotNull(fkSpectraDefinitions);
    assertEquals(2, fkSpectraDefinitions.size());

    assertTrue(fkSpectraDefinitions.contains(fkSpectraDefinitionA));
    assertTrue(fkSpectraDefinitions.contains(fkSpectraDefinitionB));
  }

  @Test
  public void testRetrieveAllExpectEmptyCollection() {
    Collection<FkSpectraDefinition> fkSpectraDefinitions =
        fkSpectraDefinitionRepositoryJpa.retrieveAll();

    assertNotNull(fkSpectraDefinitions);
    assertEquals(0, fkSpectraDefinitions.size());
  }

  private void assertStored(FkSpectraDefinition fkSpectraDefinition) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      List<FkSpectraDefinitionDao> fkSpectraDefinitionDaos = entityManager
          .createQuery("from FkSpectraDefinitionDao", FkSpectraDefinitionDao.class)
          .getResultList();

      assertEquals(1, fkSpectraDefinitionDaos.size());
      assertEquals(fkSpectraDefinition, fkSpectraDefinitionDaos.get(0).toCoi());

    } finally {
      entityManager.close();
    }
  }

}
