package gms.shared.frameworks.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class ServerConfigTests {

  private static final int PORT = 8080;
  private static final int MIN_THREADS = 5;
  private static final int MAX_THREADS = 10;
  private static final Duration IDLE_TIMEOUT = Duration.ofMillis(500);

  @Test
  void testFrom() {
    final ServerConfig config = ServerConfig.from(
        PORT, MIN_THREADS, MAX_THREADS, IDLE_TIMEOUT);
    assertNotNull(config);
    assertEquals(PORT, config.getPort());
    assertEquals(MIN_THREADS, config.getMinThreadPoolSize());
    assertEquals(MAX_THREADS, config.getMaxThreadPoolSize());
    assertEquals(IDLE_TIMEOUT, config.getThreadIdleTimeout());
  }

  @Test
  void testPortNumberNegativeValidation() {
    final int[] badPorts = new int[]{-Integer.MAX_VALUE - 1, 0, 65536, 123456789};
    for (int p : badPorts) {
      assertIllegalArgumentExceptionThrown(
          () -> ServerConfig.from(p, MIN_THREADS, MAX_THREADS, IDLE_TIMEOUT),
          "Port number " + p + " is not in range [0, 65535]");
    }
  }

  @Test
  void testMinThreadPoolSizeValidation() {
    assertValidationOnNonPositiveValues(
        s -> ServerConfig.from(PORT, s, MAX_THREADS, IDLE_TIMEOUT),
        s -> "min thread pool size is " + s + ", must be > 0"
    );
  }

  @Test
  void testMaxThreadPoolSizeValidation() {
    assertValidationOnNonPositiveValues(
        s -> ServerConfig.from(PORT, MIN_THREADS, s, IDLE_TIMEOUT),
        s -> "max thread pool size is " + s + ", must be > 0"
    );
  }

  @Test
  void testThreadPoolSizeMinNotGreaterThanMaxValidation() {
    final int min = 5, max = 4;
    assertIllegalArgumentExceptionThrown(
        () -> ServerConfig.from(PORT, min, max, IDLE_TIMEOUT),
        String.format("min thread pool size must be <= max thread pool size (min=%d, max=%d)",
            min, max));
  }

  @Test
  void testThreadIdleTimeoutValidation() {
    final Duration d = Duration.ofMillis(-1);
    assertIllegalArgumentExceptionThrown(
        () -> ServerConfig.from(PORT, MIN_THREADS, MAX_THREADS, d),
        "thread timeout is " + d + ", must not be negative");
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


  private static void assertIllegalArgumentExceptionThrown(Executable r, String message) {
    final String actualMessage = assertThrows(IllegalArgumentException.class, r).getMessage();
    assertTrue(actualMessage.contains(message), "Expected exception message '"
        + actualMessage + "' to contain message '" + message + "'");
  }
}
