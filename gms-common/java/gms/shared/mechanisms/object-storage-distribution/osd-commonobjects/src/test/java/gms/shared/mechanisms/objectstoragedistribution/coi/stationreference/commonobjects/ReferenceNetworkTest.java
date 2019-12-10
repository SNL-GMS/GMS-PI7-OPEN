package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.Test;

public class ReferenceNetworkTest {

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.network, ReferenceNetwork.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(ReferenceNetwork.class);
  }

  @Test
  public void testReferenceNetworkCreateNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceNetwork.class, "create",
        TestFixtures.networkName,
        TestFixtures.description,
        TestFixtures.networkOrg,
        TestFixtures.networkRegion,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.actualTime,
        TestFixtures.systemTime);
  }

  @Test
  public void testReferenceNetworkFromNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        ReferenceNetwork.class, "from",
        TestFixtures.networkId,
        TestFixtures.networkVersionId,
        TestFixtures.networkName,
        TestFixtures.description,
        TestFixtures.networkOrg,
        TestFixtures.networkRegion,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.actualTime,
        TestFixtures.systemTime);
  }

  /**
   * Test that arguments are saved correctly.  We check that the name was converted to uppercase.
   */
  @Test
  public void testReferenceNetworkCreate() {
    ReferenceNetwork net = ReferenceNetwork.create(
        TestFixtures.networkName,
        TestFixtures.description,
        TestFixtures.networkOrg,
        TestFixtures.networkRegion,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.actualTime,
        TestFixtures.systemTime);
    assertEquals(TestFixtures.networkName, net.getName());
    assertEquals(TestFixtures.description, net.getDescription());
    assertEquals(TestFixtures.networkOrg, net.getOrganization());
    assertEquals(TestFixtures.networkRegion, net.getRegion());
    assertEquals(TestFixtures.comment, net.getComment());
    assertEquals(TestFixtures.actualTime, net.getActualChangeTime());
    assertEquals(TestFixtures.systemTime, net.getSystemChangeTime());
    assertEquals(UUID.nameUUIDFromBytes(net.getName().getBytes(StandardCharsets.UTF_16LE)),
        net.getEntityId());
    assertEquals(UUID.nameUUIDFromBytes(
        (net.getName() + net.getOrganization() + net.getRegion() + net.getActualChangeTime())
            .getBytes(StandardCharsets.UTF_16LE)),
        net.getVersionId());
  }


  /**
   * Test that arguments are saved correctly.  We check that the name was converted to uppercase.
   */
  @Test
  public void testReferenceNetworkFrom() {
    ReferenceNetwork network = ReferenceNetwork.from(
        TestFixtures.networkId,
        TestFixtures.networkVersionId,
        TestFixtures.networkName,
        TestFixtures.description,
        TestFixtures.networkOrg,
        TestFixtures.networkRegion,
        TestFixtures.source,
        TestFixtures.comment,
        TestFixtures.actualTime,
        TestFixtures.systemTime);
    assertEquals(TestFixtures.networkId, network.getEntityId());
    assertEquals(TestFixtures.networkVersionId, network.getVersionId());
    assertEquals(TestFixtures.networkName, network.getName());
    assertEquals(TestFixtures.description, network.getDescription());
    assertEquals(TestFixtures.networkOrg, network.getOrganization());
    assertEquals(TestFixtures.networkRegion, network.getRegion());
    assertEquals(TestFixtures.comment, network.getComment());
    assertEquals(TestFixtures.actualTime, network.getActualChangeTime());
    assertEquals(TestFixtures.systemTime, network.getSystemChangeTime());
  }

}
