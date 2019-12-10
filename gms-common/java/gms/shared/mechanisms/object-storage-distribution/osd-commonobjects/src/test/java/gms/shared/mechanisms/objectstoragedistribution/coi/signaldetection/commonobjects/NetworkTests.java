package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.SignalDetectionTestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkOrganization;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.NetworkRegion;
import org.junit.Test;


/**
 *
 */
public class NetworkTests {

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.network,
        Network.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(Network.class);
  }

  @Test
  public void fromOperationValidationTest() throws Exception {

    TestUtilities.checkStaticMethodValidatesNullArguments(
        Network.class, "from",
        SignalDetectionTestFixtures.networkID, SignalDetectionTestFixtures.networkName,
        NetworkOrganization.CTBTO, NetworkRegion.GLOBAL,
        SignalDetectionTestFixtures.stations);
  }

  @Test
  public void createOperationValidationTest() throws Exception {

    TestUtilities.checkStaticMethodValidatesNullArguments(
        Network.class, "create",
        SignalDetectionTestFixtures.networkName,
        NetworkOrganization.CTBTO, NetworkRegion.GLOBAL,
        SignalDetectionTestFixtures.stations);
  }

  @Test(expected = Exception.class)
  public void testEmptyName() {
    Network.create("",
        NetworkOrganization.CTBTO, NetworkRegion.GLOBAL,
        SignalDetectionTestFixtures.stations);
  }
}
