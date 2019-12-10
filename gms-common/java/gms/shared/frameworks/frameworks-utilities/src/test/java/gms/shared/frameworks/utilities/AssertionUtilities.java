package gms.shared.frameworks.utilities;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.function.Executable;

/**
 * Reusable operations to validate {@link Executable}s throw expected exceptions with expected
 * messages.
 */
class AssertionUtilities {

  /**
   * Verifies the {@link Executable} throws a NullPointerException with a message containing the
   * provided message.
   *
   * @param executable {@link Executable} to run, not null
   * @param message expected exception message to contain this message, not null
   */
  static void verifyNullPointerException(Executable executable, String message) {
    verifyThrows(executable, NullPointerException.class, message);
  }

  /**
   * Verifies the {@link Executable} throws a IllegalArgumentException with a message containing the
   * provided message.
   *
   * @param executable {@link Executable} to run, not null
   * @param message expected exception message to contain this message, not null
   */
  static void verifyIllegalArgumentException(Executable executable, String message) {
    verifyThrows(executable, IllegalArgumentException.class, message);
  }

  /**
   * Verifies the {@link Executable} throws an instance of throwableClass with a message containing
   * the provided message.
   *
   * @param <T> throwable class type
   * @param executable {@link Executable} to run, not null
   * @param throwableClass expected throwable type, not null
   * @param message expected exception message to contain this message, not null
   */
  private static <T extends Throwable> void verifyThrows(Executable executable,
      Class<T> throwableClass, String message) {

    verifyThrowsWithAllInMessage(executable, throwableClass, List.of(message));
  }

  /**
   * Verifies the {@link Executable} throws an instance of throwableClass with a message containing
   * each of the provided messages.
   *
   * @param <T> throwable class type
   * @param executable {@link Executable} to run, not null
   * @param throwableClass expected throwable type, not null
   * @param messageComponents expected exception message to contain each of these messages, not
   * null
   */
  static <T extends Throwable> void verifyThrowsWithAllInMessage(Executable executable,
      Class<T> throwableClass,
      Collection<String> messageComponents) {

    final String actualMessage = assertThrows(throwableClass, executable).getMessage();
    assertAll(messageComponents.stream().map(e -> () -> assertTrue(actualMessage.contains(e),
        "Expected msg to contain: '" + e + "', but not in message which was: '" + actualMessage
            + "'")));
  }
}
