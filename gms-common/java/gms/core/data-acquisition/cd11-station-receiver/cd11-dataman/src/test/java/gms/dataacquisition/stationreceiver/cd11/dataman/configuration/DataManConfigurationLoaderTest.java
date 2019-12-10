package gms.dataacquisition.stationreceiver.cd11.dataman.configuration;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;


public class DataManConfigurationLoaderTest {

  @Test
  public void testLoader() {
    DataManConfig config = DataManConfigurationLoader.load();

    assertNotNull(config);
    assertNotNull(config.fsOutputDirectory);
    assertNotNull(config.expectedDataProviderIpAddress);
    assertNotNull(config.dataConsumerIpAddress);

    assertTrue(config.fsOutputDirectory.equals("/tmp/"));
    assertTrue(config.expectedDataProviderIpAddress.equals("205.204.203.202"));
    assertTrue(config.dataConsumerIpAddress.equals("101.102.103.104"));
  }
}
