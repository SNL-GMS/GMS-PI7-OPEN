package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ConfigurationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testBuilder() {
    Configuration.Builder builder = Configuration
        .builder();

    Configuration config = builder.build();

    assertNotNull(config.getBaseUrl());
    assertNotNull(config.getPersistenceUrl());

    assertTrue(config.getMinThreads() > 0);
    assertTrue(config.getMaxThreads() > 0);
    assertTrue(config.getMaxThreads() >= config.getMinThreads());

    assertTrue(config.getPort() >= 0);
    assertTrue(config.getPort() <= 65535);

    assertTrue(config.getIdleTimeOutMillis() >= 0);
  }

  @Test
  public void testBuilderOverrides() {
    Configuration.Builder builder = Configuration.builder();

    builder.setBaseUrl("baseUrl");
    builder.setPersistenceUrl("persistenceUrl");
    builder.setIdleTimeOutMillis(2);
    builder.setMaxThreads(3);
    builder.setMinThreads(2);
    builder.setPort(8080);

    Configuration config = builder.build();

    assertEquals(config.getBaseUrl(), "baseUrl");
    assertTrue(config.getPersistenceUrl().isPresent());
    assertEquals(config.getPersistenceUrl().get(), "persistenceUrl");
    assertEquals(config.getIdleTimeOutMillis(), 2);
    assertEquals(config.getMaxThreads(), 3);
    assertEquals(config.getMinThreads(), 2);
    assertEquals(config.getPort(), 8080);
  }

  @Test
  public void testBuilderAllowsNullPersistenceUrlOverride() {
    Configuration.Builder builder = Configuration.builder();
    builder.setPersistenceUrl(null);

    Configuration config = builder.build();
    assertFalse(config.getPersistenceUrl().isPresent());
  }

  @Test
  public void testBuilderMinThreadsNotLessThanOne() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "Configuration minThreads must be >= 1");
    Configuration.builder().setMinThreads(0).build();
  }

  @Test
  public void testBuilderMaxThreadsNotLessThanOne() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "Configuration maxThreads must be >= 1");
    Configuration.builder().setMaxThreads(0).build();
  }

  @Test
  public void testBuilderMaxThreadsNotLessThanMinThreads() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "Configuration cannot have maxThreads less than minThreads");
    Configuration.builder().setMaxThreads(1).setMinThreads(10).build();
  }

  @Test
  public void testBuilderIdleTimeOutMillisNotLessThanOne() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "Configuration idleTimeOutMillis must be >= 1");
    Configuration.builder().setIdleTimeOutMillis(0).build();
  }

  @Test
  public void testValidPortRange() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage(
        "Configuration must use a port between 0 and 65535");
    Configuration.builder().setPort(-1).build();
  }

  @Test
  public void testBuilderNullChecks() throws Exception {
    Configuration.Builder builder = Configuration.builder();
    TestUtilities.checkMethodValidatesNullArguments(builder, "setBaseUrl", "url");
  }
}
