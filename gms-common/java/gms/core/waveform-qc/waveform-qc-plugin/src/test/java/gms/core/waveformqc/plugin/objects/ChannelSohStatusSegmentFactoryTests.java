package gms.core.waveformqc.plugin.objects;


import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegment.Builder;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ChannelSohStatusSegmentFactoryTests {

  private final UUID expectedChannelId = UUID.randomUUID();
  private final AcquiredChannelSohType expectedType = AcquiredChannelSohType.CLIPPED;
  private final Instant expectedStartTime = Instant.EPOCH;
  private final Instant expectedEndTime = expectedStartTime.plus(Duration.ofSeconds(10));
  private final boolean expectedStatus = true;
  private final Duration threshold = Duration.ofSeconds(5);

  private final AcquiredChannelSohBoolean expectedSoh = AcquiredChannelSohBoolean
      .create(expectedChannelId, expectedType, expectedStartTime,
          expectedEndTime, true, CreationInfo.DEFAULT);

  @Test
  void testCreateNullArguments() {
    assertAll("ChannelSohStatusSegmentFactory#create Null Arguments:",
        () -> assertThrows(NullPointerException.class,
            () -> ChannelSohStatusSegmentFactory.create(null, threshold)),
        () -> assertThrows(NullPointerException.class,
            () -> ChannelSohStatusSegmentFactory.create(List.of(expectedSoh), null)));
  }

  @Test
  void testCreateIllegalArguments() {

    AcquiredChannelSohBoolean diffferentChannel = AcquiredChannelSohBoolean
        .create(UUID.randomUUID(), expectedType, expectedEndTime,
            expectedEndTime.plusSeconds(10), true, CreationInfo.DEFAULT);

    AcquiredChannelSohBoolean differentType = AcquiredChannelSohBoolean
        .create(expectedChannelId, AcquiredChannelSohType.DEAD_SENSOR_CHANNEL, expectedEndTime,
            expectedEndTime.plusSeconds(10), true, CreationInfo.DEFAULT);

    assertAll("ChannelSohStatusSegmentFactory#create Illegal Arguments:",
        () -> assertThrows(IllegalArgumentException.class,
            () -> ChannelSohStatusSegmentFactory.create(emptyList(), Duration.ZERO)),
        () -> assertThrows(IllegalArgumentException.class,
            () -> ChannelSohStatusSegmentFactory
                .create(List.of(expectedSoh, diffferentChannel), Duration.ZERO)),
        () -> assertThrows(IllegalArgumentException.class,
            () -> ChannelSohStatusSegmentFactory
                .create(List.of(expectedSoh, differentType), threshold)));
  }

  /**
   * Tests basic {@link Builder} functionality with a single status
   */
  @Test
  public void testCreateSingleSoh() {
    ChannelSohStatusSegment actual = ChannelSohStatusSegmentFactory
        .create(List.of(expectedSoh), threshold);

    assertEquals(expectedChannelId, actual.getChannelId());
    assertEquals(expectedType, actual.getType());

    assertEquals(1, actual.getStatusSegments().size());
    SohStatusSegment actualStatusSegment = actual.getStatusSegments().get(0);

    assertEquals(expectedStartTime, actualStatusSegment.getStartTime());
    assertEquals(expectedEndTime, actualStatusSegment.getEndTime());
    assertEquals(SohStatusBit.SET, actualStatusSegment.getStatusBit());
  }

  /**
   * Tests basic {@link Builder} functionality with multiple distinct statuses
   */
  @Test
  public void testCreateMultipleSohsDifferentStatus() {

    AcquiredChannelSohBoolean secondSoh = AcquiredChannelSohBoolean.create(expectedChannelId,
        expectedType, expectedEndTime, expectedEndTime.plusSeconds(10), false,
        CreationInfo.DEFAULT);

    ChannelSohStatusSegment actual = ChannelSohStatusSegmentFactory
        .create(List.of(expectedSoh, secondSoh), threshold);

    assertEquals(expectedChannelId, actual.getChannelId());
    assertEquals(expectedType, actual.getType());

    assertEquals(2, actual.getStatusSegments().size());

    SohStatusSegment actualSegment;

    actualSegment = actual.getStatusSegments().get(0);
    assertEquals(expectedSoh.getStartTime(), actualSegment.getStartTime());
    assertEquals(expectedSoh.getEndTime(), actualSegment.getEndTime());
    assertEquals(SohStatusBit.SET, actualSegment.getStatusBit());

    actualSegment = actual.getStatusSegments().get(1);
    assertEquals(secondSoh.getStartTime(), actualSegment.getStartTime());
    assertEquals(secondSoh.getEndTime(), actualSegment.getEndTime());
    assertEquals(SohStatusBit.UNSET, actualSegment.getStatusBit());
  }

  /**
   * Tests basic {@link Builder} functionality with multiple distinct statuses
   */
  @Test
  public void testCreateMultipleSohsSameStatus() {

    AcquiredChannelSohBoolean secondSoh = AcquiredChannelSohBoolean.create(expectedChannelId,
        expectedType, expectedEndTime, expectedEndTime.plusSeconds(10), true,
        CreationInfo.DEFAULT);

    ChannelSohStatusSegment actual = ChannelSohStatusSegmentFactory
        .create(List.of(expectedSoh, secondSoh), threshold);

    assertEquals(expectedChannelId, actual.getChannelId());
    assertEquals(expectedType, actual.getType());
    assertEquals(1, actual.getStatusSegments().size());

    SohStatusSegment actualSegment = actual.getStatusSegments().get(0);

    assertEquals(expectedSoh.getStartTime(), actualSegment.getStartTime());
    assertEquals(secondSoh.getEndTime(), actualSegment.getEndTime());
    assertEquals(SohStatusBit.SET, actualSegment.getStatusBit());
  }

  /**
   * Tests basic {@link Builder} functionality with multiple distinct statuses
   */
  @Test
  public void testCreateMultipleSohsSameStatusWithGap() {

    AcquiredChannelSohBoolean secondSoh = AcquiredChannelSohBoolean.create(expectedChannelId,
        expectedType, expectedEndTime.plusSeconds(10), expectedEndTime.plusSeconds(20), true,
        CreationInfo.DEFAULT);

    ChannelSohStatusSegment actual = ChannelSohStatusSegmentFactory
        .create(List.of(expectedSoh, secondSoh), threshold);

    assertEquals(expectedChannelId, actual.getChannelId());
    assertEquals(expectedType, actual.getType());
    assertEquals(3, actual.getStatusSegments().size());

    SohStatusSegment actualSegment;

    actualSegment = actual.getStatusSegments().get(0);
    assertEquals(expectedSoh.getStartTime(), actualSegment.getStartTime());
    assertEquals(expectedSoh.getEndTime(), actualSegment.getEndTime());
    assertEquals(SohStatusBit.SET, actualSegment.getStatusBit());

    actualSegment = actual.getStatusSegments().get(1);
    assertEquals(expectedSoh.getEndTime(), actualSegment.getStartTime());
    assertEquals(secondSoh.getStartTime(), actualSegment.getEndTime());
    assertEquals(SohStatusBit.MISSING, actualSegment.getStatusBit());

    actualSegment = actual.getStatusSegments().get(2);
    assertEquals(secondSoh.getStartTime(), actualSegment.getStartTime());
    assertEquals(secondSoh.getEndTime(), actualSegment.getEndTime());
    assertEquals(SohStatusBit.SET, actualSegment.getStatusBit());
  }

}
