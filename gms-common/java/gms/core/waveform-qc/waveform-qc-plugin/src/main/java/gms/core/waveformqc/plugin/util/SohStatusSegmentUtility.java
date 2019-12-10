package gms.core.waveformqc.plugin.util;

import gms.core.waveformqc.plugin.objects.SohStatusSegment;
import java.time.Instant;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class SohStatusSegmentUtility {

  public static boolean noneOverlap(List<SohStatusSegment> orderedStatusSegments) {
    // SohStatusSegment values cannot overlap in time
    final BiPredicate<Instant, Instant> beforeOrEqual = (a, b) -> a.isBefore(b) || a.equals(b);
    final IntPredicate subsequent = i -> beforeOrEqual
        .test(orderedStatusSegments.get(i).getEndTime(),
            orderedStatusSegments.get(i + 1).getStartTime());

    return validateStatuses(orderedStatusSegments, subsequent);
  }

  public static boolean noAdjacentEqualStatuses(List<SohStatusSegment> orderedStatusSegments) {
    // Adjacent status values must have different state
    final IntPredicate differentStatus = i -> !orderedStatusSegments.get(i).getStatusBit()
        .equals(orderedStatusSegments.get(i + 1).getStatusBit());

    return validateStatuses(orderedStatusSegments, differentStatus);
  }

  /**
   * Validates all of the sohStatusSegments satisfy the provided predicate.  This operation provides
   * indexes to entries in the sohStatusSegments list to the IntPredicate.
   *
   * @param sohStatusSegments {@link SohStatusSegment} to validate
   * @param test validation {@link IntPredicate} applied to each status
   * @throws IllegalArgumentException if sohStatusSegments fail the predicate
   */
  private static boolean validateStatuses(List<SohStatusSegment> sohStatusSegments,
      IntPredicate test) {
    return IntStream.range(0, sohStatusSegments.size() - 1).allMatch(test);
  }
}
