package gms.core.signalenhancement.waveformfiltering.coi.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.core.signalenhancement.waveformfiltering.coi.HttpClientConfiguration;
import org.junit.jupiter.api.Test;

public class HttpClientConfigurationTests {

  @Test
  void testCreate() {
    final String expectedHost = "test-coi-client";
    final int expectedPort = 8070;
    final String expectedBasePath = "/coi/client/base";

    HttpClientConfiguration config = HttpClientConfiguration
        .create(expectedHost, expectedPort, expectedBasePath);

    assertNotNull(config);
    assertEquals(expectedHost, config.getHost());
    assertEquals(expectedPort, config.getPort());
    assertEquals(expectedBasePath, config.getBasePath());
  }

  @Test
  void testCreateNullArgumentsExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> HttpClientConfiguration.create(null, 7171, "/base/path"));

    assertThrows(NullPointerException.class,
        () -> HttpClientConfiguration.create("host", 7171, null));
  }

  @Test
  void testCreatePortTooLowExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> HttpClientConfiguration.create("host", -1, "/base/path/osd"));
  }

  @Test
  void testCreatePortTooHighExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> HttpClientConfiguration.create("host", 65536, "/base/path/osd"));
  }

  @Test
  void testCreateBasePathStartsWithoutSlashExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> HttpClientConfiguration.create("host", 65530, "base/path/osd"));
  }

  @Test
  void testCreateBasePathEndsWithSlashExpectIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class,
        () -> HttpClientConfiguration.create("host", 65530, "/base/path/osd/"));
  }
}
