package gms.dataacquisition.stationreceiver.cd11.dataframeparser.configuration;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;


public class DataframeParserConfigurationLoaderTest {

  @Test
  public void testLoader() {
    DataframeParserConfig config = DataframeParserConfigurationLoader.load();
    assertNotNull(config);
    assertNotNull(config.monitoredDirLocation);
    assertTrue(config.monitoredDirLocation.equals("shared-volume/dataframes/"));
  }
}
