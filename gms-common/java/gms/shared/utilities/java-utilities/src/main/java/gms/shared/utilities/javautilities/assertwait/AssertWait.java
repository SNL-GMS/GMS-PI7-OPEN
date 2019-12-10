package gms.shared.utilities.javautilities.assertwait;

import java.util.function.BooleanSupplier;
import org.apache.commons.lang3.Validate;


public final class AssertWait {

  private static String MAX_TIMEOUT_EXPIRED_ERROR_MESSAGE =
      "Max wait time expired before the expected value appeared.";
  private static long SLEEP_TIME_MS = 5;

  private static long minWaitTimeMs() {
    return SLEEP_TIME_MS * 2;
  }

  public static void assertTrueWait(BooleanSupplier test, long maxWaitTimeMs) {
    assertTrueWait(MAX_TIMEOUT_EXPIRED_ERROR_MESSAGE, test, maxWaitTimeMs);
  }

  public static void assertTrueWait(String errorMsg, BooleanSupplier test, long maxWaitTimeMs) {
    // Validate input.
    Validate.notNull(test);
    Validate.isTrue(maxWaitTimeMs > 0);
    maxWaitTimeMs = (maxWaitTimeMs < minWaitTimeMs()) ? minWaitTimeMs() : maxWaitTimeMs;

    // Set the end time.
    long endTime = System.nanoTime() + (maxWaitTimeMs * 1000000);

    // Wait for the expected value, or for the wait time to expire (whichever comes first).
    do {
      // Test if the expected value has appeared.
      if (test.getAsBoolean()) {
        return;
      }

      // Sleep for a short period of time.
      try {
        Thread.sleep(SLEEP_TIME_MS);
      } catch (InterruptedException e) {
        // Ignore.
      }
    } while (System.nanoTime() < endTime);

    throw new AssertionError(errorMsg);
  }

  public static void assertFalseWait(BooleanSupplier test, long maxWaitTimeMs) {
    assertFalseWait(MAX_TIMEOUT_EXPIRED_ERROR_MESSAGE, test, maxWaitTimeMs);
  }

  public static void assertFalseWait(String errorMsg, BooleanSupplier test, long maxWaitTimeMs) {
    // Validate input.
    Validate.notNull(test);
    Validate.isTrue(maxWaitTimeMs > 0);
    maxWaitTimeMs = (maxWaitTimeMs < minWaitTimeMs()) ? minWaitTimeMs() : maxWaitTimeMs;

    // Set the end time.
    long endTime = System.nanoTime() + (maxWaitTimeMs * 1000000);

    // Wait for the expected value, or for the wait time to expire (whichever comes first).
    do {
      // Test if the expected value has appeared.
      if (!test.getAsBoolean()) {
        return;
      }

      // Sleep for a short period of time.
      try {
        Thread.sleep(SLEEP_TIME_MS);
      } catch (InterruptedException e) {
        // Ignore.
      }
    } while (System.nanoTime() < endTime);

    throw new AssertionError(errorMsg);
  }
}