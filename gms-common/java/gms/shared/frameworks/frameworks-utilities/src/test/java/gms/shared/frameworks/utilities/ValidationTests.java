package gms.shared.frameworks.utilities;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class ValidationTests {

  @Test
  void testThrowForNonUnique() {
    final String message = "All elements must be unique";

    AssertionUtilities.verifyThrowsWithAllInMessage(
        () -> Validation.throwForNonUnique(List.of(1, 2, 3, 3, 1), i -> i + 10, message),
        IllegalArgumentException.class,
        Set.of(message, "11", "13")
    );
  }

  @Test
  void testThrowForNonUniqueDoesNotThrowWhenAllUnique() {
    final Executable e = () -> Validation
        .throwForNonUnique(List.of(1, 2, 3), Function.identity(), "no message");

    assertDoesNotThrow(e);
  }

  @Test
  void testThrowForNonUniqueValidatesParameters() {

    final List<Integer> items = List.of();
    final Function<Integer, String> mapper = i -> Integer.toString(i);
    final String description = "";

    assertAll(
        () -> AssertionUtilities.verifyNullPointerException(
            () -> Validation.throwForNonUnique(null, mapper, description),
            "collection can't be null"),

        () -> AssertionUtilities.verifyNullPointerException(
            () -> Validation.throwForNonUnique(items, null, description),
            "mapper can't be null"),

        () -> AssertionUtilities.verifyNullPointerException(
            () -> Validation.throwForNonUnique(items, mapper, null),
            "description can't be null")
    );
  }

  @Test
  void testThrowForMethods() {
    final String expectedMessage = "exception expected message base";
    final List<Method> methods = Arrays.stream(Object.class.getMethods())
        .collect(Collectors.toList());

    final String actualMessage = assertThrows(IllegalArgumentException.class,
        () -> Validation.throwForMethods(expectedMessage, methods)).getMessage();

    assertAll(
        () -> assertTrue(actualMessage.startsWith(expectedMessage)),
        () -> assertTrue(
            methods.stream().map(Method::toGenericString).allMatch(actualMessage::contains))
    );
  }

  @Test
  void testThrowForMethodsValidatesParameters() {
    assertAll(
        () -> AssertionUtilities.verifyNullPointerException(
            () -> Validation.throwForMethods(null, List.of()), "Exception message can't be null"
        ),

        () -> AssertionUtilities.verifyNullPointerException(
            () -> Validation.throwForMethods("", null), "problemMethods can't be null"
        )
    );
  }
}
