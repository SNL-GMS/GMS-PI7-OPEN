package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TimeseriesUtility {

  public static Range<Instant> computeSpan(Collection<? extends Timeseries> timeseries) {
    Preconditions.checkNotNull(timeseries);

    return timeseries.stream().map(Timeseries::computeTimeRange)
        .reduce(Range::span)
        .orElseThrow(() -> new IllegalArgumentException("Must provide at least 1 timeseries"));
  }

  /**
   * Determines if any of the given series connect in time.
   *
   * @param series the series
   * @return true if any of the series from the input collection overlap in time
   */
  public static boolean anyConnected(Collection<? extends Timeseries> series) {
    Preconditions.checkNotNull(series);
    List<Range<Instant>> ranges = computeSortedTimeRanges(series);

    for (int i = 0; i < ranges.size() - 1; i++) {
      if (ranges.get(i).isConnected(ranges.get(i + 1))) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines if all of the given series connect in time.
   *
   * @param series the series
   * @return true if any of the series from the input collection overlap in time
   */
  public static boolean allConnected(Collection<? extends Timeseries> series) {
    Preconditions.checkNotNull(series);
    List<Range<Instant>> ranges = computeSortedTimeRanges(series);

    for (int i = 0; i < ranges.size() - 1; i++) {
      if (!ranges.get(i).isConnected(ranges.get(i + 1))) {
        return false;
      }
    }

    return true;
  }

  public static boolean noneOverlapped(Collection<? extends Timeseries> series) {
    Preconditions.checkNotNull(series);
    List<Range<Instant>> ranges = computeSortedTimeRanges(series);

    for (int i = 0; i < ranges.size() - 1; i++) {
      if (ranges.get(i).contains(ranges.get(i + 1).lowerEndpoint())) {
        return false;
      }
    }

    return true;
  }


  public static <T extends Timeseries> Map<Range<Instant>, List<T>> clusterByConnected(
      List<T> series) {
    if (series.isEmpty()) {
      return Map.of();
    }

    Map<Range<Instant>, List<T>> clusters = new HashMap<>();

    Iterator<T> iterator = series.stream().sorted().iterator();
    T next = iterator.next();

    //initialize starting cluster
    List<T> cluster = new ArrayList<>();
    cluster.add(next);
    Range<Instant> clusterRange = next.computeTimeRange();

    Range<Instant> nextRange;
    while (iterator.hasNext()) {
      next = iterator.next();
      nextRange = next.computeTimeRange();

      if (clusterRange.isConnected(nextRange)) {
        //can be clustered
        cluster.add(next);
        clusterRange = clusterRange.span(nextRange);
      } else {
        //finish this cluster and create a new one
        clusters.put(clusterRange, cluster);
        cluster = new ArrayList<>();
        cluster.add(next);
        clusterRange = nextRange;
      }
    }

    //put the final cluster
    clusters.put(clusterRange, cluster);
    return clusters;
  }

  /**
   * filters out all series contained by other series
   */
  public static <T extends Timeseries> List<T> filterEnclosed(List<T> series) {
    if (series.size() < 2) {
      return series;
    }

    //due to sorting by start time then end time, the smallest series always
    //come before the series that could enclose them
    //if we reverse this sorting, then we iterate in a interesting way
    Iterator<T> reversedIterator = series.stream()
        .sorted(Comparator.comparing(Timeseries::getStartTime)
            .thenComparing(Timeseries::getEndTime).reversed()).iterator();

    List<T> filtered = new ArrayList<>();
    T next = reversedIterator.next();
    filtered.add(next);

    Range<Instant> lastRange = next.computeTimeRange();
    Range<Instant> nextRange;
    while (reversedIterator.hasNext()) {
      next = reversedIterator.next();
      nextRange = next.computeTimeRange();

      if (!lastRange.encloses(nextRange)) {
        //keep this series as it wasn't enclosed, set it up to test future series
        filtered.add(next);
        lastRange = nextRange;
      }
    }

    return filtered;
  }

  /**
   * returns time ranges sorted by a timeseries' start time
   * @param series
   * @return
   */
  private static List<Range<Instant>> computeSortedTimeRanges(Collection<? extends Timeseries> series) {
    return series.stream()
        .sorted(Comparator.comparing(Timeseries::getStartTime))
        .map(Timeseries::computeTimeRange)
        .collect(Collectors.toList());
  }
}
