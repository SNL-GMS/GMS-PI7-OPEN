package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class ReferenceNetworkMembershipTest {

  private final UUID id = UUID.fromString("1712f988-ff83-4f3d-a832-a82a040221d9");
  private final UUID networkId = UUID.fromString("6712f988-ff83-4f3d-a832-a82a04022123");
  private final UUID stationId = UUID.fromString("9812f988-ff83-4f3d-a832-a82a04022154");
  private final String comment = "Question everything.";
  private final Instant actualTime = Instant.now().minusSeconds(100);
  private final Instant systemTime = Instant.now().minusNanos(1);
  private final StatusType status = StatusType.ACTIVE;

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.netMember, ReferenceNetworkMembership.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceNetworkMembership.class);
  }

  @Test
  public void testReferenceNetworkMembCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceNetworkMembership.class, "create", comment, actualTime, systemTime,
        networkId, stationId, status);
  }

  @Test
  public void testReferenceNetworkMembFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceNetworkMembership.class, "from", id, comment, actualTime, systemTime,
        networkId, stationId, status);

  }

  /**
   * Test that arguments are saved correctly.
   */
  @Test
  public void testReferenceNetworkMembershipCreate() {
    ReferenceNetworkMembership m = ReferenceNetworkMembership.create(
        comment, actualTime, systemTime,
        networkId, stationId, status);
    final UUID expectedId = UUID.nameUUIDFromBytes(
        (m.getNetworkId().toString() + m.getStationId()
            + m.getStatus() + m.getActualChangeTime()).getBytes(StandardCharsets.UTF_16LE));
    assertEquals(expectedId, m.getId());
    assertEquals(comment, m.getComment());
    assertEquals(actualTime, m.getActualChangeTime());
    assertEquals(systemTime, m.getSystemChangeTime());
    assertEquals(networkId, m.getNetworkId());
    assertEquals(stationId, m.getStationId());
    assertEquals(status, m.getStatus());
  }

  /**
   * Test that arguments are saved correctly.  We check that the name was converted to uppercase.
   */
  @Test
  public void testReferenceNetworkMembFrom() {
    ReferenceNetworkMembership alias = ReferenceNetworkMembership.from(id, comment, actualTime,
        systemTime, networkId, stationId, status);
    assertEquals(id, alias.getId());
    assertEquals(comment, alias.getComment());
    assertEquals(actualTime, alias.getActualChangeTime());
    assertEquals(systemTime, alias.getSystemChangeTime());
    assertEquals(networkId, alias.getNetworkId());
    assertEquals(stationId, alias.getStationId());
    assertEquals(status, alias.getStatus());
  }


}
