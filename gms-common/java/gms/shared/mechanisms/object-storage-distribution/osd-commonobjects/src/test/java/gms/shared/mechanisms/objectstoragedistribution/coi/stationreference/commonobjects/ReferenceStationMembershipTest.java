package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class ReferenceStationMembershipTest {

  private final UUID id = UUID.fromString("1712f988-ff83-4f3d-a832-a82a040221d9");
  private final UUID stationId = UUID.fromString("6712f988-ff83-4f3d-a832-a82a04022123");
  private final UUID siteId = UUID.fromString("9812f988-ff83-4f3d-a832-a82a04022154");
  private final String comment = "Question everything.";
  private final Instant actualTime = Instant.now().minusSeconds(100);
  private final Instant systemTime = Instant.now().minusNanos(1);
  private final StatusType status = StatusType.ACTIVE;

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.stationMember,
        ReferenceStationMembership.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceStationMembership.class);
  }

  @Test
  public void testReferenceStationMembCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceStationMembership.class, "create", comment, actualTime,
        systemTime, stationId, siteId, status);
  }

  @Test
  public void testReferenceStationMembFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceStationMembership.class, "from", id, comment, actualTime, systemTime,
        stationId, siteId, status);

  }

  /**
   * Test that arguments are saved correctly.
   */
  @Test
  public void testReferenceStationMembCreate() {
    ReferenceStationMembership m = ReferenceStationMembership.create(comment, actualTime,
        systemTime, stationId, siteId, status);
    final UUID expectedId = UUID.nameUUIDFromBytes(
        (m.getStationId().toString() + m.getSiteId()
            + m.getStatus() + m.getActualChangeTime()).getBytes(StandardCharsets.UTF_16LE));
    assertEquals(expectedId, m.getId());
    assertEquals(comment, m.getComment());
    assertEquals(actualTime, m.getActualChangeTime());
    assertEquals(systemTime, m.getSystemChangeTime());
    assertEquals(stationId, m.getStationId());
    assertEquals(status, m.getStatus());
  }


  /**
   * Test that arguments are saved correctly.  We check that the name was converted to uppercase.
   */
  @Test
  public void testReferenceStationMembFrom() {
    ReferenceStationMembership alias = ReferenceStationMembership.from(id, comment, actualTime,
        systemTime, stationId, siteId, status);
    assertEquals(id, alias.getId());
    assertEquals(comment, alias.getComment());
    assertEquals(actualTime, alias.getActualChangeTime());
    assertEquals(systemTime, alias.getSystemChangeTime());
    assertEquals(stationId, alias.getStationId());
    assertEquals(status, alias.getStatus());
  }


}
