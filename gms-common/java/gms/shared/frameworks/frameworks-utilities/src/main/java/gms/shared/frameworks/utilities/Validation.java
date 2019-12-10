package gms.shared.frameworks.utilities;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides utility operations to validate expected state and throw exceptions on failure.
 */
public class Validation {

  private Validation() {
  }

  /**
   * Verifies the result of applying the mapper function to all of the elements in the collection
   * are unique.  If there are duplicated elements throws an IllegalArgumentException with the
   * provided description combined with an entry for each duplicate element (obtained using the
   * mapped element's toString).
   *
   * @param collection results of applying mapper to elements in this collection must be unique
   * @param mapper {@link Function} to transform collection into Rs
   * @param description base message of the throws IllegalArgumentException
   * @param <T> element type of the input collection
   * @param <R> input collection elements mapped to this type
   * @throws IllegalArgumentException if the mapped collection contains duplicated elements
   */
  public static <T, R> void throwForNonUnique(Collection<T> collection, Function<T, R> mapper,
      String description) {

    Objects.requireNonNull(collection, "collection can't be null");
    Objects.requireNonNull(mapper, "mapper can't be null");
    Objects.requireNonNull(description, "description can't be null");

    final List<String> duplicateStrings = collection.stream()
        .collect(Collectors.groupingBy(mapper))
        .entrySet().stream()
        .filter(e -> e.getValue().size() > 1)
        .map(Entry::getKey)
        .sorted()
        .map(Object::toString)
        .collect(Collectors.toList());

    if (!duplicateStrings.isEmpty()) {
      throwForAll(description, duplicateStrings);
    }
  }

  /**
   * Throws an {@link IllegalArgumentException} with a message containing the provided description
   * followed by a list of the problemMethods containing the issues that require throwing the
   * exception.
   *
   * @param description error description for the exception message, not null
   * @param problemMethods {@link Method}s which have issues leading to the exception, not null
   * @throws IllegalArgumentException with a message created from the input parameters
   */
  public static void throwForMethods(String description, Collection<Method> problemMethods) {
    Objects.requireNonNull(description, "Exception message can't be null");
    Objects.requireNonNull(problemMethods, "problemMethods can't be null");

    throwForAll(description,
        problemMethods.stream().map(Method::toGenericString).collect(Collectors.toSet()));
  }

  /**
   * Throws an {@link IllegalArgumentException} with a message containing the provided description
   * followed by a list of the problem strings containing the issues that require throwing the
   * exception.
   *
   * @param description error description for the exception message, not null
   * @param problems Strings listing items with issues leading to the exception
   * @throws IllegalArgumentException with a message created from the input parameters
   */
  private static void throwForAll(String description, Collection<String> problems) {
    final StringBuilder messageBuilder = new StringBuilder(description);
    problems.forEach(p -> {
      messageBuilder.append("\t");
      messageBuilder.append(p);
    });

    throw new IllegalArgumentException(messageBuilder.toString());
  }
}
