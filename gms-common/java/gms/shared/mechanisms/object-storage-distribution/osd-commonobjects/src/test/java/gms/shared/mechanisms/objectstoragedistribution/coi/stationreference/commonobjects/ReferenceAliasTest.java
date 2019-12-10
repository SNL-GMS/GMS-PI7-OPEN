package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class ReferenceAliasTest {

  private final UUID id = UUID.fromString("8052f988-ff83-4f3d-a832-a82a04022119");
  private final String name = "FOO";
  private final String comment = "This is a comment.";
  private final StatusType status = StatusType.INACTIVE;
  private final Instant actualTime = Instant.now().minusSeconds(10);
  private final Instant systemTime = Instant.now();

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.stationAlias, ReferenceAlias.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceAlias.class);
  }

  @Test
  public void testReferenceStationAliasCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceAlias.class, "create",
        name, status, comment, actualTime, systemTime);
  }

  @Test
  public void testReferenceStationAliasFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceAlias.class, "from", id, name,
        status, comment, actualTime, systemTime);
  }

  @Test
  public void testReferenceStationAliasCreate() {
    ReferenceAlias alias = ReferenceAlias.create(name, status, comment,
        actualTime, systemTime);
    assertNotEquals(id, alias.getId());
    assertEquals(name, alias.getName());
    assertEquals(status, alias.getStatus());
    assertEquals(comment, alias.getComment());
    assertEquals(actualTime, alias.getActualChangeTime());
    assertEquals(systemTime, alias.getSystemChangeTime());
  }

  @Test
  public void testReferenceStationAliasFrom() {
    ReferenceAlias alias = ReferenceAlias.from(id, name, status, comment,
        actualTime, systemTime);
    assertEquals(id, alias.getId());
    assertEquals(name, alias.getName());
    assertEquals(status, alias.getStatus());
    assertEquals(comment, alias.getComment());
    assertEquals(actualTime, alias.getActualChangeTime());
    assertEquals(systemTime, alias.getSystemChangeTime());
  }


}
