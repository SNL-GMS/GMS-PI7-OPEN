package gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;


public class Cd11DataProviderConfigurationLoaderTest {

  @Test
  public void testLoader() {
    Cd11DataProviderConfig config = Cd11DataProviderConfigurationLoader.load();

    assertNotNull(config);
    assertNotNull(config.connectionManagerIpAddress);
    assertNotNull(config.stationName);

    assertTrue(config.connectionManagerIpAddress.equals("254.253.252.251"));
    assertTrue(config.connectionManagerPort == 64999);
    assertTrue(config.stationName.equals("TEST"));
  }
}
