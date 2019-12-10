package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.CoiTestingEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.FilterDefinitionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.FilterDefinitionDao;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FilterDefinitionRepositoryJpaTests {

  private EntityManagerFactory entityManagerFactory;

  private FilterDefinitionRepository filterDefinitionRepositoryJpa;

  private final FilterDefinition filterDefinitionA = FilterDefinition
      .firBuilder()
      .setName("filter name string")
      .setDescription("filter description")
      .setFilterPassBandType(FilterPassBandType.BAND_PASS)
      .setLowFrequencyHz(12.34)
      .setHighFrequencyHz(99.99)
      .setOrder(12)
      .setFilterSource(FilterSource.USER)
      .setFilterCausality(FilterCausality.CAUSAL)
      .setZeroPhase(false)
      .setSampleRate(42.42)
      .setSampleRateTolerance(24.24)
      .setbCoefficients(new double[]{-12.34, 57.89, 64.0})
      .setGroupDelaySecs(4687.3574)
      .build();

  private final FilterDefinition filterDefinitionB = FilterDefinition
      .firBuilder()
      .setName("filter name string B")
      .setDescription("filter description B")
      .setFilterPassBandType(FilterPassBandType.LOW_PASS)
      .setLowFrequencyHz(34.12)
      .setHighFrequencyHz(88.77)
      .setOrder(15)
      .setFilterSource(FilterSource.SYSTEM)
      .setFilterCausality(FilterCausality.NON_CAUSAL)
      .setZeroPhase(true)
      .setSampleRate(24.24)
      .setSampleRateTolerance(43.42)
      .setbCoefficients(new double[]{1.234, 5.789, 6.40})
      .setGroupDelaySecs(3574.4687)
      .build();

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    entityManagerFactory = CoiTestingEntityManagerFactory.createTesting();
    filterDefinitionRepositoryJpa = FilterDefinitionRepositoryJpa.create(entityManagerFactory);
  }

  @After
  public void tearDown() {
    entityManagerFactory.close();
    entityManagerFactory = null;
    filterDefinitionRepositoryJpa = null;
  }

  @Test
  public void testCreateNullEntityManagerFactoryExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Cannot create FilterDefinitionRepositoryJpa with a null EntityManagerFactory");
    FilterDefinitionRepositoryJpa.create(null);
  }

  @Test
  public void testStore() {
    filterDefinitionRepositoryJpa.store(filterDefinitionA);
    assertStored(filterDefinitionA);
  }

  @Test
  public void testStoreDoesNotCreateDuplicates() {
    filterDefinitionRepositoryJpa.store(filterDefinitionA);
    filterDefinitionRepositoryJpa.store(filterDefinitionA);
    assertStored(filterDefinitionA);
  }

  @Test
  public void testStoreNull() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot store a null FilterDefinition");
    filterDefinitionRepositoryJpa.store(null);
  }

  private void assertStored(FilterDefinition filterDefinition) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      List<FilterDefinitionDao> filterDefinitionDaos = entityManager
          .createQuery("from FilterDefinitionDao", FilterDefinitionDao.class).getResultList();

      assertEquals(1, filterDefinitionDaos.size());
      assertTrue(filterDefinitionEqualsDao(filterDefinition, filterDefinitionDaos.get(0)));

    } finally {
      entityManager.close();
    }
  }

  @Test
  public void testRetrieveAll() {
    filterDefinitionRepositoryJpa.store(filterDefinitionA);
    filterDefinitionRepositoryJpa.store(filterDefinitionB);

    Collection<FilterDefinition> filterDefinitions = filterDefinitionRepositoryJpa.retrieveAll();

    assertNotNull(filterDefinitions);
    assertEquals(2, filterDefinitions.size());
    assertTrue(filterDefinitions.contains(filterDefinitionA));
    assertTrue(filterDefinitions.contains(filterDefinitionB));
  }

  @Test
  public void testRetrieveAllExpectEmptyCollection() {
    Collection<FilterDefinition> filterDefinitions = filterDefinitionRepositoryJpa.retrieveAll();

    assertNotNull(filterDefinitions);
    assertEquals(0, filterDefinitions.size());
  }

  /**
   * Determines if the {@link FilterDefinition} and {@link FilterDefinitionDao} have the same
   * information
   *
   * @param filterDefinition a FilterDefinition, not null
   * @param dao a FilterDefinitionDao, not null
   * @return true if filterDefinition and dao have the same information
   */
  public static boolean filterDefinitionEqualsDao(FilterDefinition filterDefinition,
      FilterDefinitionDao dao) {

    return Double.compare(filterDefinition.getLowFrequencyHz(), dao.getLowFrequencyHz()) == 0
        && Double.compare(filterDefinition.getHighFrequencyHz(), dao.getHighFrequencyHz()) == 0
        && filterDefinition.getOrder() == dao.getFilterOrder()
        && Double.compare(filterDefinition.getSampleRate(), dao.getSampleRate()) == 0
        && Double.compare(filterDefinition.getSampleRateTolerance(), dao.getSampleRateTolerance())
        == 0
        && Double.compare(filterDefinition.getGroupDelaySecs(), dao.getGroupDelaySecs()) == 0
        && filterDefinition.getName().equals(dao.getName())
        && filterDefinition.getFilterPassBandType() == dao.getFilterPassBandType()
        && filterDefinition.getFilterSource() == dao.getFilterSource()
        && Arrays.equals(filterDefinition.getaCoefficients(), dao.getACoefficients())
        && Arrays.equals(filterDefinition.getbCoefficients(), dao.getBCoefficients());
  }
}
