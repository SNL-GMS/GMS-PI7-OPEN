package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroupType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.ChannelProcessingGroupDao;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ChannelProcessingGroupDaoConverterTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
  }

  @Test
  public void testToDao() {
    ChannelProcessingGroup coi = createCpg();

    ChannelProcessingGroupDao dao = ChannelProcessingGroupDaoConverter.toDao(coi);

    checkThatCoiAndDaoFieldsMatch(coi, dao);
  }

  @Test
  public void testToDaoNullExpectedNullPointerException() {
    exception.expect(NullPointerException.class);
    ChannelProcessingGroupDaoConverter.toDao(null);
  }

  @Test
  public void testFromDao() {
    ChannelProcessingGroupDao dao = createCpgDao();

    ChannelProcessingGroup coi = ChannelProcessingGroupDaoConverter.fromDao(dao);

    checkThatCoiAndDaoFieldsMatch(coi, dao);
  }

  @Test
  public void testFromDaoNullExpectedNullPointerException() {
    exception.expect(NullPointerException.class);
    ChannelProcessingGroupDaoConverter.fromDao(null);
  }

  private void checkThatCoiAndDaoFieldsMatch(
      ChannelProcessingGroup coi, ChannelProcessingGroupDao dao) {

    assertEquals(dao.getActualChangeTime(), coi.getActualChangeTime());
    assertEquals(dao.getChannelIds(), coi.getChannelIds());
    assertEquals(dao.getComment(), coi.getComment());
    assertEquals(dao.getId(), coi.getId());
    assertEquals(dao.getStatus(), coi.getStatus());
    assertEquals(dao.getSystemChangeTime(), coi.getSystemChangeTime());
    assertEquals(dao.getType(), coi.getType());
  }

  private ChannelProcessingGroup createCpg() {
    return ChannelProcessingGroup.from(
        UUID.randomUUID(),
        ChannelProcessingGroupType.BEAM,
        Set.of(
            UUID.fromString("10000000-b6a4-478f-b3cd-5c934ee6b812"),
            UUID.fromString("20000000-b6a4-478f-b3cd-5c934ee6b812"),
            UUID.fromString("30000000-b6a4-478f-b3cd-5c934ee6b812")),
        Instant.now().minusSeconds(6000),
        Instant.now(),
        "status...",
        "comment.."
    );
  }

  private ChannelProcessingGroupDao createCpgDao() {
    ChannelProcessingGroupDao dao = new ChannelProcessingGroupDao();
    dao.setActualChangeTime(Instant.now().minusSeconds(6000));
    dao.setChannelIds(
        Set.of(
            UUID.fromString("10000000-b6a4-478f-b3cd-5c934ee6b812"),
            UUID.fromString("20000000-b6a4-478f-b3cd-5c934ee6b812"),
            UUID.fromString("30000000-b6a4-478f-b3cd-5c934ee6b812")));
    dao.setComment("comment..");
    dao.setId(UUID.randomUUID());
    dao.setStatus("status...");
    dao.setSystemChangeTime(Instant.now());
    dao.setType(ChannelProcessingGroupType.BEAM);
    return dao;
  }
}
