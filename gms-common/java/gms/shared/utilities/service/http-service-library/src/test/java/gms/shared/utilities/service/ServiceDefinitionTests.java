package gms.shared.utilities.service;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.Test;

public class ServiceDefinitionTests {

  @Test
  public void testBuilderUsesDefaults() {
    final ServiceDefinition def = ServiceDefinition.builder().build();
    assertEquals(Defaults.JSON_MAPPER, def.getJsonMapper());
    assertEquals(Defaults.MSGPACK_MAPPER, def.getMsgpackMapper());
    assertEquals(Defaults.PORT, def.getPort());
    assertEquals(Defaults.MIN_THREAD_POOL_SIZE, def.getMinThreadPoolSize());
    assertEquals(Defaults.MAX_THREAD_POOL_SIZE, def.getMaxThreadPoolSize());
    assertEquals(Defaults.THREAD_IDLE_TIMEOUT_MILLIS, def.getThreadIdleTimeoutMillis());
    assertNotNull(def.getRoutes());
    assertTrue(def.getRoutes().isEmpty());
  }

  @Test
  public void testBuilder() {
    final Route r = createRoute();
    final ObjectMapper jsonMapper = new ObjectMapper();
    final ObjectMapper msgpackMapper = new ObjectMapper();
    // set some properties of the object mapper to not match the defaults
    // so it can be determined that they were actually taken in by the ServiceDefinition
    jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
    msgpackMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    final ServiceDefinition def = ServiceDefinition.builder()
        .setJsonMapper(jsonMapper)
        .setMsgpackMapper(msgpackMapper)
        .setPort(1234)
        .setMinThreadPoolSize(5)
        .setMaxThreadPoolSize(13)
        .setThreadIdleTimeoutMillis(5000)
        .setRoutes(Set.of(r))
        .build();
    assertEquals(jsonMapper, def.getJsonMapper());
    assertEquals(msgpackMapper, def.getMsgpackMapper());
    assertEquals(1234, def.getPort());
    assertEquals(5, def.getMinThreadPoolSize());
    assertEquals(13, def.getMaxThreadPoolSize());
    assertEquals(5000, def.getThreadIdleTimeoutMillis());
    assertTrue("Expected the added route to be contained: " + r,
        def.getRoutes().contains(r));
  }

  @Test
  public void testPortNumberNegativeValidation() {
    final int[] badPorts = new int[]{-Integer.MAX_VALUE - 1, 0, 65536, 123456789};
    for (int p : badPorts) {
      assertIllegalArgumentExceptionThrown(
          () -> ServiceDefinition.builder().setPort(p).build(),
          "Port number " + p + " is not in range [0, 65535]");
    }
  }

  @Test
  public void testMinThreadPoolSizeValidation() {
    assertValidationOnNonPositiveValues(
        s -> ServiceDefinition.builder().setMinThreadPoolSize(s).build(),
        s -> "min thread pool size is " + s + ", must be > 0"
    );
  }

  @Test
  public void testMaxThreadPoolSizeValidation() {
    assertValidationOnNonPositiveValues(
        s -> ServiceDefinition.builder().setMaxThreadPoolSize(s).build(),
        s -> "max thread pool size is " + s + ", must be > 0"
    );
  }

  @Test
  public void testThreadPoolSizeMinNotGreaterThanMaxValidation() {
    final int min = 5, max = 4;
    assertIllegalArgumentExceptionThrown(
        () -> ServiceDefinition.builder().setMinThreadPoolSize(5).setMaxThreadPoolSize(4).build(),
        String.format("min thread pool size must be <= max thread pool size (min=%d, max=%d)",
            min, max));
  }

  @Test
  public void testThreadIdleTimeoutValidation() {
    assertValidationOnNonPositiveValues(
        s -> ServiceDefinition.builder().setThreadIdleTimeoutMillis(s).build(),
        s -> "thread timeout is " + s + ", must be > 0"
    );
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRoutesImmutable() {
    ServiceDefinition.builder().build().getRoutes().add(createRoute());
  }

  private static void assertIllegalArgumentExceptionThrown(Runnable r, String message) {
    try {
      r.run();
      fail(message);
    } catch (IllegalArgumentException ex) {
      assertTrue("Expected exception message " + ex.getMessage() + " to contain message " + message,
          ex.getMessage() != null && ex.getMessage().contains(message));
    }
  }

  private static void assertValidationOnNonPositiveValues(Consumer<Integer> c,
      Function<Integer, String> errorMessageFunction) {

    final int[] badValues = new int[]{-Integer.MAX_VALUE, -1, 0};
    for (int s : badValues) {
      assertIllegalArgumentExceptionThrown(
          () -> c.accept(s),
          errorMessageFunction.apply(s));
    }
  }

  private static Route createRoute() {
    return Route.create("/foo",
        (req, deserializer) -> Response.success("hello"));
  }

}
