package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConfigurationLoaderTests {

  @Test
  public void testLoadConfiguration() {
    Configuration config = ConfigurationLoader.load();

    assertNotNull(config);
    assertEquals("/mechanisms/object-storage-distribution/signal-detection/",
        config.getBaseUrl());

    assertTrue(config.getPersistenceUrl().isPresent());
    assertEquals("jdbc:postgresql://postgresql-stationreceiver:5432/xmp_metadata", config.getPersistenceUrl().get());

    assertEquals(8081, config.getPort());
    assertEquals(1, config.getMinThreads());
    assertEquals(13, config.getMaxThreads());
    assertEquals(1000, config.getIdleTimeOutMillis());
  }
}
