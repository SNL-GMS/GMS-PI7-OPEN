package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class ReferenceSiteMembershipTest {

  private final UUID id = UUID.fromString("1712f988-ff83-4f3d-a832-a82a040221d9");
  private final UUID siteId = UUID.fromString("6712f988-ff83-4f3d-a832-a82a04022123");
  private final UUID channelId = UUID.fromString("9812f988-ff83-4f3d-a832-a82a04022154");
  private final String comment = "Question everything.";
  private final Instant actualTime = Instant.now().minusSeconds(100);
  private final Instant systemTime = Instant.now().minusNanos(1);
  private final StatusType status = StatusType.ACTIVE;

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.siteMember, ReferenceSiteMembership.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceSiteMembership.class);
  }

  @Test
  public void testReferenceSiteMembCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceSiteMembership.class, "create", comment, actualTime,
        systemTime, siteId, channelId, status);
  }

  @Test
  public void testReferenceSiteMembFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceSiteMembership.class, "from", id, comment, actualTime, systemTime,
        siteId, channelId, status);

  }

  /**
   * Test that arguments are saved correctly.
   */
  @Test
  public void testReferenceSiteMembCreate() {
    ReferenceSiteMembership m = ReferenceSiteMembership.create(comment, actualTime,
        systemTime, siteId, channelId, status);
    final UUID expectedId = UUID.nameUUIDFromBytes(
        (m.getSiteId().toString() + m.getChannelId()
            + m.getStatus() + m.getActualChangeTime()).getBytes(StandardCharsets.UTF_16LE));
    assertEquals(expectedId, m.getId());
    assertEquals(comment, m.getComment());
    assertEquals(actualTime, m.getActualChangeTime());
    assertEquals(systemTime, m.getSystemChangeTime());
    assertEquals(siteId, m.getSiteId());
    assertEquals(channelId, m.getChannelId());
    assertEquals(status, m.getStatus());
  }


  /**
   * Test that arguments are saved correctly.  We check that the name was converted to uppercase.
   */
  @Test
  public void testReferenceSiteMembFrom() {
    ReferenceSiteMembership alias = ReferenceSiteMembership.from(id, comment, actualTime,
        systemTime, siteId, channelId, status);
    assertEquals(id, alias.getId());
    assertEquals(comment, alias.getComment());
    assertEquals(actualTime, alias.getActualChangeTime());
    assertEquals(systemTime, alias.getSystemChangeTime());
    assertEquals(siteId, alias.getSiteId());
    assertEquals(channelId, alias.getChannelId());
    assertEquals(status, alias.getStatus());
  }


}
