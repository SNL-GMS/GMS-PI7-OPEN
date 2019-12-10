package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConfigurationLoaderTests {

  @Test
  public void testLoadConfiguration() {
    Configuration config = ConfigurationLoader.load();

    assertNotNull(config);
    assertTrue(config.persistenceUrl.isPresent());
    assertEquals("jdbc:postgresql://postgresql-stationreceiver:5432/xmp_metadata", config.persistenceUrl.get());

    assertEquals(8081, config.port);
    assertEquals(1, config.minThreads);
    assertEquals(13, config.maxThreads);
    assertEquals(1000, config.idleTimeOutMillis);
  }
}
