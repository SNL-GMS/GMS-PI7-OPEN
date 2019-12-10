package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.CoiTestingEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskVersionDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility.QcMaskDaoConverter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

public class QcMaskRepositoryJpaTests {

  /**
   * Corresponds to JPA persistence unit defined in persistence.xml
   */

  private final UUID channelId = UUID.randomUUID();
  private final List<QcMaskVersionDescriptor> parents = Arrays.asList(
      QcMaskVersionDescriptor.from(UUID.randomUUID(), 1),
      QcMaskVersionDescriptor.from(UUID.randomUUID(), 2));
  private final UUID channelSegmentId1 = UUID.randomUUID();
  private final UUID channelSegmentId2 = UUID.randomUUID();
  private final UUID channelSegmentId3 = UUID.randomUUID();
  private final List<UUID> channelSegmentIdList1 = Collections
      .singletonList(channelSegmentId1);
  private final List<UUID> channelSegmentIdList2 = Arrays
      .asList(channelSegmentId3, channelSegmentId2);
  private final QcMaskType qcMaskType1 = QcMaskType.LONG_GAP;
  private final QcMaskCategory qcMaskCategory = QcMaskCategory.WAVEFORM_QUALITY;
  private final String rationale1 = "Rationale";
  private final String rationale2 = "Rationale SPIKE";
  private final Instant startTime1 = Instant.parse("2007-12-03T10:15:30.00Z");
  private final Instant startTime2 = Instant.parse("2007-12-03T10:35:30.00Z");
  private final Instant endTime1 = Instant.parse("2007-12-03T11:15:30.00Z");
  private final Instant endTime2 = Instant.parse("2007-12-03T10:45:30.00Z");

  private static EntityManagerFactory entityManagerFactory;
  private QcMaskRepository qcMaskRepositoryJpa;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @BeforeClass
  public static void init() {
    entityManagerFactory = CoiTestingEntityManagerFactory.createTesting();
  }

  @Before
  public void setUp() {
    qcMaskRepositoryJpa = QcMaskRepositoryJpa.create(entityManagerFactory);
  }

  @After
  public void tearDown() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      //weird issue with element collections require deletion via the entitymanager in order to cascade
      List<QcMaskVersionDao> versions = entityManager
          .createQuery("SELECT v FROM QcMaskVersionDao v", QcMaskVersionDao.class).getResultList();

      List<QcMaskDao> masks = entityManager
          .createQuery("SELECT q FROM QcMaskDao q", QcMaskDao.class).getResultList();

      entityManager.getTransaction().begin();
      versions.forEach(entityManager::remove);
      masks.forEach(entityManager::remove);
      entityManager.getTransaction().commit();

    } finally {
      entityManager.close();
    }

    qcMaskRepositoryJpa = null;
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
  public void testStoreNullQcMask() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot store a null QcMask");
    qcMaskRepositoryJpa.store(null);
  }

  /**
   * Test storing a qcmask with one version, with the exact same object count and values coming back
   * from a query.
   *
   * @throws Exception any jpa exception
   */
  @Test
  public void testStoreQcMask() {
    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList1, qcMaskCategory, qcMaskType1,
            rationale1, startTime1, endTime1);

    qcMaskRepositoryJpa.store(qcMask);

    assertStored(qcMask);
  }

  /**
   * Test storing a qcmask with one version, adding another version and restoring the qcmask. This
   * tests that previously persisted qcmasks and versions are not duplicated in the database
   *
   * @throws Exception any jpa exception
   */
  @Test
  public void testStoreQcMaskNewVersion() {
    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList1, qcMaskCategory, qcMaskType1,
            rationale1, startTime1, endTime1);

    qcMaskRepositoryJpa.store(qcMask);

    qcMask.addQcMaskVersion(channelSegmentIdList2, QcMaskCategory.STATION_SOH,
        QcMaskType.STATION_PROBLEM,
        rationale2,
        startTime2, endTime2);

    qcMaskRepositoryJpa.store(qcMask);

    assertStored(qcMask);
  }

  @Test
  public void testStoreRejectedQcMaskVersion() {
    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList1, qcMaskCategory, qcMaskType1,
            rationale1, startTime1, endTime1);
    qcMask.reject("reject", Collections.emptyList());

    qcMaskRepositoryJpa.store(qcMask);

    assertStored(qcMask);
  }

  @Test
  public void testFindByChannelIdAndTimeRangeNullParameters() throws Exception {
    TestUtilities.checkMethodValidatesNullArguments(qcMaskRepositoryJpa,
        "findCurrentByChannelIdAndTimeRange", channelId,
        startTime1, endTime1);
  }

  @Test
  public void testFindByChannelIdAndTimeRangeStartAfterEnd() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "Cannot query for invalid time range: start must be less than or equal to end");
    qcMaskRepositoryJpa
        .findCurrentByChannelIdAndTimeRange(channelId, endTime1, startTime1);
  }

  @Test
  public void testFindByChannelIdAndTimeRangeDifferentChannelId() {
    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList1, qcMaskCategory, qcMaskType1,
            rationale1, startTime1, endTime1);

    qcMaskRepositoryJpa.store(qcMask);

    List<QcMask> qcMasks = qcMaskRepositoryJpa
        .findCurrentByChannelIdAndTimeRange(new UUID(0L, 0L), startTime1, endTime1);
    assertNotNull(qcMasks);
    assertTrue(qcMasks.isEmpty());
  }

  @Test
  public void testFindByChannelIdAndTimeRangeInvalidTimeRanges() {
    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList1, qcMaskCategory, qcMaskType1,
            rationale1, startTime1, endTime1);

    qcMaskRepositoryJpa.store(qcMask);

    //Test time range overlaps from the qcmask range
    List<QcMask> qcMasks = qcMaskRepositoryJpa
        .findCurrentByChannelIdAndTimeRange(channelId,
            startTime1.minus(10, ChronoUnit.MINUTES),
            startTime1.minus(1, ChronoUnit.MINUTES));
    assertNotNull(qcMasks);
    assertTrue(qcMasks.isEmpty());
  }

  @Test
  public void testFindByChannelIdAndTimeRange() {
    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList1, qcMaskCategory, qcMaskType1,
            rationale1, startTime1, endTime1);

    qcMaskRepositoryJpa.store(qcMask);

    //out of range
    List<QcMask> qcMasks = qcMaskRepositoryJpa
        .findCurrentByChannelIdAndTimeRange(channelId,
            startTime1.minus(10, ChronoUnit.MINUTES),
            startTime1.minus(1, ChronoUnit.MINUTES));
    assertNotNull(qcMasks);
    assertTrue(qcMasks.isEmpty());

    qcMasks = qcMaskRepositoryJpa
        .findCurrentByChannelIdAndTimeRange(channelId,
            endTime1.plus(1, ChronoUnit.MINUTES),
            endTime1.plus(10, ChronoUnit.MINUTES));
    assertNotNull(qcMasks);
    assertTrue(qcMasks.isEmpty());

    //start time inclusive
    qcMasks = qcMaskRepositoryJpa
        .findCurrentByChannelIdAndTimeRange(channelId,
            startTime1.minus(10, ChronoUnit.MINUTES), startTime1);
    assertNotNull(qcMasks);
    assertEquals(1, qcMasks.size());

    //end time inclusive
    qcMasks = qcMaskRepositoryJpa.findCurrentByChannelIdAndTimeRange(channelId,
        endTime1, endTime1.plus(10, ChronoUnit.MINUTES));
    assertNotNull(qcMasks);
    assertEquals(1, qcMasks.size());

    //inside range
    qcMasks = qcMaskRepositoryJpa.findCurrentByChannelIdAndTimeRange(channelId,
        startTime1.plus(1, ChronoUnit.MINUTES),
        endTime1.minus(1, ChronoUnit.MINUTES));
    assertNotNull(qcMasks);
    assertEquals(1, qcMasks.size());

    //encompassing range
    qcMasks = qcMaskRepositoryJpa.findCurrentByChannelIdAndTimeRange(channelId,
        startTime1.minus(1, ChronoUnit.MINUTES),
        endTime1.plus(1, ChronoUnit.MINUTES));
    assertNotNull(qcMasks);
    assertEquals(1, qcMasks.size());

  }

  @Test
  public void testFindByChannelIdAndTimeRangeOnlyLatestVersion() {
    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList1, qcMaskCategory, qcMaskType1,
            rationale1, startTime1, endTime1);

    qcMask.addQcMaskVersion(channelSegmentIdList2, qcMaskCategory, qcMaskType1,
        rationale2, startTime2, endTime2);

    qcMaskRepositoryJpa.store(qcMask);

    //the first time range is larger than the second, so we'll query by that to make sure both versions are included
    List<QcMask> qcMasks = qcMaskRepositoryJpa
        .findCurrentByChannelIdAndTimeRange(channelId,
            startTime1, endTime1);
    assertNotNull(qcMasks);
    //we only want the latest version, all other versions aren't loaded
    assertEquals(1, qcMasks.size());
    assertEquals(1, qcMasks.get(0).getQcMaskVersions().size());
    assertEquals(qcMask.getCurrentQcMaskVersion(), qcMasks.get(0).getCurrentQcMaskVersion());

    //query a range that includes an old version but not the most recent
    qcMasks = qcMaskRepositoryJpa
        .findCurrentByChannelIdAndTimeRange(channelId, startTime1,
            startTime1.plus(1, ChronoUnit.MINUTES));
    assertNotNull(qcMasks);
    assertTrue(qcMasks.isEmpty());
  }

  /**
   * Checks that exactly one qcmask is in the database, and that it matches the passed in qcmask
   * by constructing a QcMask from its respective dao and version dao objects in the database.
   *
   * @param expectedQcMask Expected qcMask
   */
  private void assertStored(QcMask expectedQcMask) {

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      List<QcMaskDao> qcMaskDaos = entityManager
          .createQuery("select m from QcMaskDao m", QcMaskDao.class)
          .getResultList();

      assertEquals(1, qcMaskDaos.size());
      QcMaskDao qcMaskDao = qcMaskDaos.get(0);

      List<QcMaskVersionDao> qcMaskVersionDaos = entityManager
          .createQuery("select v from QcMaskVersionDao v", QcMaskVersionDao.class).getResultList();

      QcMask actualQcMask = QcMaskDaoConverter.fromDao(qcMaskDao, qcMaskVersionDaos);
      assertEquals(expectedQcMask, actualQcMask);
    } finally {

      entityManager.close();
    }
  }

}
