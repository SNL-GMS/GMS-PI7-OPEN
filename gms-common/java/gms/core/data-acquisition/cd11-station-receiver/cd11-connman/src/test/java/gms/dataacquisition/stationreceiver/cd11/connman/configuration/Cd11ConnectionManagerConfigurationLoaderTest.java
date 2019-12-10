package gms.dataacquisition.stationreceiver.cd11.connman.configuration;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;


public class Cd11ConnectionManagerConfigurationLoaderTest {

  @Test
  public void testLoader() {
    Cd11ConnectionManagerConfig config = Cd11ConnectionManagerConfigurationLoader.load();

    assertNotNull(config);
    assertNotNull(config.connectionManagerIpAddress);
    assertNotNull(config.dataProviderIpAddress);

    assertTrue(config.connectionManagerIpAddress.equals("254.253.252.251"));
    assertTrue(config.connectionManagerPort == 64999);
    assertTrue(config.dataProviderIpAddress.equals("102.103.104.105"));
  }
}
