package gms.core.signalenhancement.fk.coi.client.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CoiRepositoryConfigurationLoaderTests {

  @Test
  public void testLoadConfiguration() {
    CoiClientConfiguration config = CoiClientConfigurationLoader.load();

    assertNotNull(config);
    assertEquals("jdbc:postgresql://osd-postgres:5432/xmp_metadata",
        config.getPersistenceUrl());
  }
}