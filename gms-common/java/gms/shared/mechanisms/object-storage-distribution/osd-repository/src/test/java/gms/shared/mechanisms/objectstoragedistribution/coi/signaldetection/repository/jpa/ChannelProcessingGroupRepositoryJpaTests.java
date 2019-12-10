package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.CoiTestingEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ChannelProcessingGroupRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.ChannelProcessingGroupDao;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class ChannelProcessingGroupRepositoryJpaTests {

  private EntityManagerFactory entityManagerFactory;

  private ChannelProcessingGroupRepository channelProcessingGroupRepositoryJpa;

  private final ChannelProcessingGroup channelProcessingGroupA = TestFixtures.channelProcessingGroup1;

  private final ChannelProcessingGroup channelProcessingGroupB = TestFixtures.channelProcessingGroup2;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    entityManagerFactory = CoiTestingEntityManagerFactory.createTesting();
    channelProcessingGroupRepositoryJpa = ChannelProcessingGroupRepositoryJpa
        .create(entityManagerFactory);
  }

  @After
  public void tearDown() {
    entityManagerFactory.close();
    entityManagerFactory = null;
    channelProcessingGroupRepositoryJpa = null;
  }

  @Test
  public void testCreateNullEntityManagerFactoryExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage(
        "Cannot create ChannelProcessingGroupRepositoryJpa with a null EntityManagerFactory");
    ChannelProcessingGroupRepositoryJpa.create(null);
  }

  @Test
  public void testStore() throws Exception {
    assertFalse(channelProcessingGroupRepositoryJpa
        .channelProcessingGroupExists(channelProcessingGroupA.getId()));
    channelProcessingGroupRepositoryJpa.createChannelProcessingGroup(channelProcessingGroupA);
    assertTrue(channelProcessingGroupRepositoryJpa
        .channelProcessingGroupExists(channelProcessingGroupA.getId()));
    assertStored(channelProcessingGroupA);
  }

  @Test
  public void testStoreDoesNotCreateDuplicates() throws Exception {
    channelProcessingGroupRepositoryJpa.createChannelProcessingGroup(channelProcessingGroupA);
    channelProcessingGroupRepositoryJpa.createChannelProcessingGroup(channelProcessingGroupA);
    assertStored(channelProcessingGroupA);
  }

  @Test
  public void testStoreNull() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot store a null ChannelProcessingGroup");
    channelProcessingGroupRepositoryJpa.createChannelProcessingGroup(null);
  }

  private void assertStored(ChannelProcessingGroup channelProcessingGroup) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    try {
      List<ChannelProcessingGroupDao> ChannelProcessingGroupDaos = entityManager
          .createQuery("from ChannelProcessingGroupDao", ChannelProcessingGroupDao.class)
          .getResultList();

      assertEquals(1, ChannelProcessingGroupDaos.size());
      assertTrue(ChannelProcessingGroupEqualsDao(
          channelProcessingGroup,
          ChannelProcessingGroupDaos.get(0)));

    } finally {
      entityManager.close();
    }
  }

  @Test
  public void testRetrieveAll() throws Exception {
    channelProcessingGroupRepositoryJpa.createChannelProcessingGroup(channelProcessingGroupA);
    channelProcessingGroupRepositoryJpa.createChannelProcessingGroup(channelProcessingGroupB);

    Collection<ChannelProcessingGroup> ChannelProcessingGroups =
        channelProcessingGroupRepositoryJpa.retrieveAll();

    assertNotNull(ChannelProcessingGroups);
    assertEquals(2, ChannelProcessingGroups.size());

    try {
      ChannelProcessingGroups.contains(channelProcessingGroupA);
    } catch (Exception e) {
      e.toString();
    }
    assertTrue(ChannelProcessingGroups.contains(channelProcessingGroupA));
    assertTrue(ChannelProcessingGroups.contains(channelProcessingGroupB));
  }

  @Test
  public void testRetrieveAllExpectEmptyCollection() throws Exception {
    Collection<ChannelProcessingGroup> ChannelProcessingGroups =
        channelProcessingGroupRepositoryJpa.retrieveAll();

    assertNotNull(ChannelProcessingGroups);
    assertEquals(0, ChannelProcessingGroups.size());
  }

  /**
   * Determines if the {@link ChannelProcessingGroup} and {@link ChannelProcessingGroupDao} have the
   * same information
   *
   * @param channelProcessingGroup a ChannelProcessingGroup, not null
   * @param dao a ChannelProcessingGroupDao, not null
   * @return true if ChannelProcessingGroup and dao have the same information
   */
  public static boolean ChannelProcessingGroupEqualsDao(
      ChannelProcessingGroup channelProcessingGroup,
      ChannelProcessingGroupDao dao) {

    return channelProcessingGroup.getActualChangeTime().equals(dao.getActualChangeTime())
        && channelProcessingGroup.getComment().equals(dao.getComment())
        && channelProcessingGroup.getChannelIds().equals(dao.getChannelIds())
        && channelProcessingGroup.getId().equals(dao.getId())
        && channelProcessingGroup.getStatus().equals(dao.getStatus())
        && channelProcessingGroup.getSystemChangeTime().equals(dao.getSystemChangeTime())
        && channelProcessingGroup.getType().equals(dao.getType());
  }

  private static String SetOfUuidsToString(Set<UUID> uuids) {
    return String.join(",", uuids.stream()
        .map(UUID::toString)
        .collect(Collectors.toList()));
  }
}
