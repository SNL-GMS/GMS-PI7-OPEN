package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EventUtilities {

  public static <T> void assertNoneWithIdPresent(UUID id,
      Collection<T> items, Function<T, UUID> idExtractor) {
    assertHowManyWithIdPresent(id, items, idExtractor, 0);
  }

  public static <T> void assertExactlyOneWithIdPresent(UUID id,
      Collection<T> items, Function<T, UUID> idExtractor) {
    assertHowManyWithIdPresent(id, items, idExtractor, 1);
  }

  private static <T> void assertHowManyWithIdPresent(UUID id,
      Collection<T> items, Function<T, UUID> idExtractor, int expectedCount) {

    final List<T> matchingItems = items.stream()
        .filter(item -> idExtractor.apply(item).equals(id))
        .collect(Collectors.toList());
    if (matchingItems.size() != expectedCount) {
        throw new IllegalArgumentException(
            "Only expected " + expectedCount + " of these objects to have id "
                + id + ": " + matchingItems);
    }
  }

}
