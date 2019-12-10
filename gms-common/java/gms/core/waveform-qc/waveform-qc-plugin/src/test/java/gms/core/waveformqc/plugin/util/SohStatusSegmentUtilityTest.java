package gms.core.waveformqc.plugin.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.core.waveformqc.plugin.objects.SohStatusSegment;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class SohStatusSegmentUtilityTest {

  @Test
  void testNoneOverlap() {

    List<SohStatusSegment> overlappingStatusSegments = List.of(
        SohStatusSegment.from(Instant.EPOCH, Instant.ofEpochSecond(60), true),
        SohStatusSegment.from(Instant.ofEpochSecond(59), Instant.MAX, true));

    assertFalse(SohStatusSegmentUtility.noneOverlap(overlappingStatusSegments));

    List<SohStatusSegment> nonOverlappingStatusSegments = List.of(
        SohStatusSegment.from(Instant.EPOCH, Instant.ofEpochSecond(60), true),
        SohStatusSegment.from(Instant.ofEpochSecond(60), Instant.MAX, true));

    assertTrue(SohStatusSegmentUtility.noneOverlap(nonOverlappingStatusSegments));
  }

  @Test
  void testNoAdjacentEqualStatuses() {

    List<SohStatusSegment> adjacentEqualStatus = List.of(
        SohStatusSegment.from(Instant.EPOCH, Instant.ofEpochSecond(1), true),
        SohStatusSegment.from(Instant.ofEpochSecond(1), Instant.MAX, true));

    assertFalse(SohStatusSegmentUtility.noAdjacentEqualStatuses(adjacentEqualStatus));

    List<SohStatusSegment> adjacentDifferentStatus = List.of(
        SohStatusSegment.from(Instant.EPOCH, Instant.ofEpochSecond(1), true),
        SohStatusSegment.from(Instant.ofEpochSecond(1), Instant.ofEpochSecond(2), false),
        SohStatusSegment.from(Instant.ofEpochSecond(2), Instant.ofEpochSecond(3), true));

    assertTrue(SohStatusSegmentUtility.noneOverlap(adjacentDifferentStatus));
  }
}