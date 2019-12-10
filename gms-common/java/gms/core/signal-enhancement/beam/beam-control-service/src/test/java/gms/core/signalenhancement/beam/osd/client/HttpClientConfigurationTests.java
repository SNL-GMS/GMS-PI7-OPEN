package gms.core.signalenhancement.beam.osd.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HttpClientConfigurationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void equalsAndHashcodeTest() {
    EqualsVerifier.forClass(HttpClientConfiguration.class)
        .usingGetClass()
        .verify();
  }

  @Test
  public void testCreate() {
    final String expectedHost = "test-gateway-client";
    final int expectedPort = 8070;
    final String expectedBasePath = "/gateway/client/base";

    HttpClientConfiguration config = HttpClientConfiguration
        .create(expectedHost, expectedPort, expectedBasePath);

    assertNotNull(config);
    assertEquals(expectedHost, config.getHost());
    assertEquals(expectedPort, config.getPort());
    assertEquals(expectedBasePath, config.getBasePath());
  }

  @Test
  public void testCreateNullHostExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("HttpClientConfiguration requires non-null host");
    HttpClientConfiguration.create(null, 7171, "/base/path");
  }

  @Test
  public void testCreateNullBasePathExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("HttpClientConfiguration requires non-null basePath");
    HttpClientConfiguration.create("host", 7171, null);
  }

  @Test
  public void testCreatePortTooLowExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "HttpClientConfiguration must use a port between 0 and 65535");
    HttpClientConfiguration.create("host", -1, "/base/path/osd");
  }

  @Test
  public void testCreatePortTooHighExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "HttpClientConfiguration must use a port between 0 and 65535");
    HttpClientConfiguration.create("host", 65536, "/base/path/osd");
  }

  @Test
  public void testCreateBasePathStartsWithoutSlashExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "HttpClientConfiguration must have a basePath beginning with a / and not ending with a /");
    HttpClientConfiguration.create("host", 65530, "base/path/osd");
  }

  @Test
  public void testCreateBasePathEndsWithSlashExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "HttpClientConfiguration must have a basePath beginning with a / and not ending with a /");
    HttpClientConfiguration.create("host", 65530, "/base/path/osd/");
  }
}
