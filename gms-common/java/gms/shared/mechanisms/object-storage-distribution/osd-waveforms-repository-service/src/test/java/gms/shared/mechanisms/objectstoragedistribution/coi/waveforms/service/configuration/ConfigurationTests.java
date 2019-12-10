package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    assertNotNull(config.persistenceUrl);

    assertTrue(config.minThreads > 0);
    assertTrue(config.maxThreads > 0);
    assertTrue(config.maxThreads >= config.minThreads);

    assertTrue(config.port >= 0);
    assertTrue(config.port <= 65535);

    assertTrue(config.idleTimeOutMillis >= 0);
  }

  @Test
  public void testBuilderOverrides() {
    Configuration.Builder builder = Configuration.builder();

    builder.setPersistenceUrl("persistenceUrl");
    builder.setIdleTimeOutMillis(2);
    builder.setMaxThreads(3);
    builder.setMinThreads(2);
    builder.setPort(8080);

    Configuration config = builder.build();

    assertTrue(config.persistenceUrl.isPresent());
    assertEquals(config.persistenceUrl.get(), "persistenceUrl");
    assertEquals(config.idleTimeOutMillis, 2);
    assertEquals(config.maxThreads, 3);
    assertEquals(config.minThreads, 2);
    assertEquals(config.port, 8080);
  }

  @Test
  public void testBuilderAllowsNullPersistenceUrlOverride() {
    Configuration.Builder builder = Configuration.builder();
    builder.setPersistenceUrl(null);

    Configuration config = builder.build();
    assertFalse(config.persistenceUrl.isPresent());
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
}
