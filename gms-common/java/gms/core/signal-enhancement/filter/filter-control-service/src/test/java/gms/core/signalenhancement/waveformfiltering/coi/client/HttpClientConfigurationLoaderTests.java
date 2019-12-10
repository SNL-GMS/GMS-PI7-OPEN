package gms.core.signalenhancement.waveformfiltering.coi.client;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.core.signalenhancement.waveformfiltering.coi.HttpClientConfiguration;
import gms.core.signalenhancement.waveformfiltering.coi.HttpClientConfigurationLoader;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HttpClientConfigurationLoaderTests {

  private URL propertiesUrl;

  @BeforeEach
  public void setUp() {
    final String filePath = "gms/core/signalenhancement/waveformfiltering/coi/client/coiClient.properties";
    propertiesUrl = Thread.currentThread().getContextClassLoader().getResource(filePath);
    assertNotNull(propertiesUrl);
  }

  @Test
  void testLoad() {
    HttpClientConfiguration config = HttpClientConfigurationLoader
        .load("coi_waveforms_", propertiesUrl);
    assertNotNull(config);
    assertEquals("osd-waveforms-repository-service", config.getHost());
    assertEquals(8080, config.getPort());
    assertEquals("/mechanisms/object-storage-distribution/waveforms",
        config.getBasePath());
  }

  @Test
  void testLoadNullArgumentsExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> HttpClientConfigurationLoader.load(null, propertiesUrl));

    assertThrows(NullPointerException.class,
        () -> HttpClientConfigurationLoader.load("prefix_", null));
  }
}
