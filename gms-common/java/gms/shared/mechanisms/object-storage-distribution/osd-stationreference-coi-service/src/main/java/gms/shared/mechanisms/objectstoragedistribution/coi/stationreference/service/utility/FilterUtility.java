package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.utility;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;

public class FilterUtility {

  /**
   * Filters a list by start time to include only ones active at that start time or later.  To do
   * this, the latest time t for any element in elems is found and then the elements are filtered by
   * that time.  The elements are grouped together (using the 'grouper' function) so that related
   * entities are filtered against each other; this is necessary since versions through time
   * supersede previous ones of the same entity. The resulting list will be returned sorted by time,
   * using the given timeExtractor.
   *
   * @param elems the list of elements to filter
   * @param startTime the start of the time range
   * @param timeExtractor a function which extracts an Instant from an element (e.g.
   * ReferenceNetwork::getActualChangeTime)
   * @param grouper a function to group related entities together so they can be filtered against
   * each other (e.g. ReferenceNetwork::getEntityId)
   * @param <T> the type of the elements to filter
   * @param <K> the type that groups related entities together (the return type of the 'grouper'
   * function); typically UUID.
   * @return a list of the elements, filtered by their times to include only those which would have
   * been active during the start time or later.
   */
  public static <T, K> List<T> filterByStartTime(List<T> elems, Instant startTime,
      Function<T, Instant> timeExtractor, Function<T, K> grouper) {

    Validate.notNull(elems);
    Validate.notNull(timeExtractor);
    Validate.notNull(grouper);
    Validate.notNull(startTime);

    // if this way of groupingBy looks strange, it's because it maintains the order
    // of the lists in the values of the map.
    LinkedHashMap<K, List<T>> groups = elems.stream()
        .collect(Collectors.groupingBy(
            grouper, LinkedHashMap::new,
            Collectors.mapping(Function.identity(), Collectors.toList())
        ));
    return groups.values().stream()
        .map(l -> filterByStartTime(l, startTime, timeExtractor))
        .flatMap(List::stream)
        .sorted(Comparator.comparing(timeExtractor))
        .collect(Collectors.toList());
  }

  /**
   * Filters a list by end time to include only ones active up to that time. The elements are
   * grouped together (using the 'grouper' function) so that related entities are filtered against
   * each other. The resulting list will be returned sorted by time, using the given timeExtractor.
   *
   * @param elems the list of elements to filter
   * @param endTime the end of the time range
   * @param timeExtractor a function which extracts an Instant from an element (e.g.
   * ReferenceNetwork::getActualChangeTime)
   * @param <T> the type of the elements to filter
   * @return a list of the elements, filtered by their times to include only those which would have
   * been active up to the end time or later.
   */
  public static <T> List<T> filterByEndTime(List<T> elems, Instant endTime,
      Function<T, Instant> timeExtractor) {

    Validate.notNull(elems);
    Validate.notNull(timeExtractor);
    Validate.notNull(endTime);

    return elems.stream()
        .filter(t -> isBeforeOrEqual(timeExtractor.apply(t), endTime))
        .collect(Collectors.toList());
  }

  /**
   * Filters a list by their times.  First, the latest time value t for an element in elems is found
   * for which t is less than or equal to rangeStart, and t is maximized.  Then, this t value is
   * used as the new time to filter the elements by.  This filters the elements by time assuming
   * that each subsequent version through time (which a later time value) supersedes the previous.
   *
   * @param elems the elements to filter
   * @param rangeStart the start of the time range
   * @param timeExtractor a function which extracts an Instant from an element (e.g.
   * ReferenceNetwork::getActualChangeTime)
   * @param <T> the type of the elements to filter
   * @return a filtered list of the elements by their start times
   */
  public static <T> List<T> filterByStartTime(List<T> elems, Instant rangeStart,
      Function<T, Instant> timeExtractor) {

    Validate.notNull(elems);
    Validate.notNull(timeExtractor);
    Validate.notNull(rangeStart);

    // Find the latest time that is before the start of the range.
    Optional<Instant> latestStart = elems.stream().map(timeExtractor)
        .filter(t -> isBeforeOrEqual(t, rangeStart))
        .max(Instant::compareTo);

    // Use the latest start time <= rangeStart if present, otherwise just use the start of the range.
    Instant actualRangeStart = latestStart.orElse(rangeStart);

    return elems.stream().filter(
        t -> isAfterOrEqual(timeExtractor.apply(t), actualRangeStart))
        .collect(Collectors.toList());
  }

  private static boolean isBeforeOrEqual(Instant time1, Instant time2) {
    return time1.isBefore(time2) || time1.equals(time2);
  }

  private static boolean isAfterOrEqual(Instant time1, Instant time2) {
    return time1.isAfter(time2) || time1.equals(time2);
  }

}
