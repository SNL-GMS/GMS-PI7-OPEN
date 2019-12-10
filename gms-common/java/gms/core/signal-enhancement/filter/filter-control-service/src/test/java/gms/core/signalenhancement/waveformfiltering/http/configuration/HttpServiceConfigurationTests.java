package gms.core.signalenhancement.waveformfiltering.http.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HttpServiceConfigurationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testBuilder() {
    HttpServiceConfiguration.Builder builder = HttpServiceConfiguration
        .builder();

    HttpServiceConfiguration config = builder.build();

    assertNotNull(config.getBaseUrl());

    assertTrue(config.getMinThreads() > 0);
    assertTrue(config.getMaxThreads() > 0);
    assertTrue(config.getMaxThreads() >= config.getMinThreads());

    assertTrue(config.getPort() >= 0);
    assertTrue(config.getPort() <= 65535);

    assertTrue(config.getIdleTimeOutMillis() >= 0);
  }

  @Test
  public void testBuilderOverrides() {
    HttpServiceConfiguration.Builder builder = HttpServiceConfiguration.builder();

    builder.setBaseUrl("baseUrl");
    builder.setIdleTimeOutMillis(2);
    builder.setMaxThreads(3);
    builder.setMinThreads(2);
    builder.setPort(8080);

    HttpServiceConfiguration config = builder.build();

    assertEquals("baseUrl", config.getBaseUrl());
    assertEquals(2, config.getIdleTimeOutMillis());
    assertEquals(3, config.getMaxThreads());
    assertEquals(2, config.getMinThreads());
    assertEquals(8080, config.getPort());
  }

  @Test
  public void testBuilderMinThreadsNotLessThanOne() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "HttpServiceConfiguration minThreads must be >= 1");
    HttpServiceConfiguration.builder().setMinThreads(0).build();
  }

  @Test
  public void testBuilderMaxThreadsNotLessThanOne() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "HttpServiceConfiguration maxThreads must be >= 1");
    HttpServiceConfiguration.builder().setMaxThreads(0).build();
  }

  @Test
  public void testBuilderMaxThreadsNotLessThanMinThreads() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "HttpServiceConfiguration cannot have maxThreads less than minThreads");
    HttpServiceConfiguration.builder().setMaxThreads(1).setMinThreads(10).build();
  }

  @Test
  public void testBuilderIdleTimeOutMillisNotLessThanOne() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "HttpServiceConfiguration idleTimeOutMillis must be >= 1");
    HttpServiceConfiguration.builder().setIdleTimeOutMillis(0).build();
  }

  @Test
  public void testValidPortRange() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "HttpServiceConfiguration must use a port between 0 and 65535");
    HttpServiceConfiguration.builder().setPort(-1).build();
  }

  @Test
  public void testBuilderNullChecks() throws Exception {
    HttpServiceConfiguration.Builder builder = HttpServiceConfiguration.builder();
    TestUtilities.checkMethodValidatesNullArguments(builder, "setBaseUrl", "url");
  }
}
