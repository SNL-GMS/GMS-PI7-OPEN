package gms.core.waveformqc.channelsohqc.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegment;
import gms.core.waveformqc.plugin.objects.SohStatusBit;
import gms.core.waveformqc.plugin.objects.SohStatusSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;


/**
 * Tests the Class {@link ChannelSohStatusParser}
 */
public class ChannelSohStatusParserTests {

  private final int statusLengthSecs = 10;
  private final AcquiredChannelSohType sohType = AcquiredChannelSohType.CLOCK_DIFFERENTIAL_IN_MICROSECONDS_OVER_THRESHOLD;

  @Test
  public void testCreateMasksStartsFalse() throws Exception {
    ChannelSohStatusSegment status = createMockChannelSohStatusSegment(false, 10,
        new UUID( 0L , 0L )
    );

    List<ChannelSohQcMask> sohQcMasks = ChannelSohStatusParser
        .parseStatuses(status);

    assertEquals(5, sohQcMasks.size());

    for (int i = 0; i < 5; i++) {
      ChannelSohQcMask sohQcMask = sohQcMasks.get(i);
      verifyChannelSohQcMask(sohQcMask,
          Instant.ofEpochSecond(statusLengthSecs * (2 * i + 1)),
          Instant.ofEpochSecond(statusLengthSecs * (2 * i + 2)));
    }
  }

  @Test
  public void testCreateMasksStartsTrue() throws Exception {
    ChannelSohStatusSegment status = createMockChannelSohStatusSegment(true, 10,
        new UUID( 0L , 0L )
    );

    List<ChannelSohQcMask> sohQcMasks = ChannelSohStatusParser
        .parseStatuses(status);

    assertEquals(5, sohQcMasks.size());

    for (int i = 0; i < 5; i++) {
      ChannelSohQcMask sohQcMask = sohQcMasks.get(i);
      verifyChannelSohQcMask(sohQcMask,
          Instant.ofEpochSecond(statusLengthSecs * (2 * i)),
          Instant.ofEpochSecond(statusLengthSecs * (2 * i + 1)));
    }
  }

  @Test
  public void testCreateMasksStartsTrueOnlyOneStatus() throws Exception {
    ChannelSohStatusSegment status = createMockChannelSohStatusSegment(true, 1,
        new UUID( 0L , 0L )
    );

    List<ChannelSohQcMask> sohQcMasks = ChannelSohStatusParser
        .parseStatuses(status);

    assertEquals(1, sohQcMasks.size());
    ChannelSohQcMask sohQcMask = sohQcMasks.get(0);
    verifyChannelSohQcMask(sohQcMask, Instant.ofEpochSecond(0),
        Instant.ofEpochSecond(statusLengthSecs));
  }

  @Test
  public void testCreateMasksStartsFalseOnlyOneStatus() throws Exception {
    ChannelSohStatusSegment status = createMockChannelSohStatusSegment(false, 1,
        new UUID( 0L , 0L )
    );

    List<ChannelSohQcMask> sohQcMasks = ChannelSohStatusParser
        .parseStatuses(status);

    assertEquals(0, sohQcMasks.size());
  }

  @Test
  public void testCreateMasksIgnoresMissingStatus() throws Exception {
    List<SohStatusSegment> sohStatusSegments = Arrays.asList(
        SohStatusSegment.from(Instant.ofEpochSecond(0), Instant.ofEpochSecond(statusLengthSecs),
            SohStatusBit.SET),
        SohStatusSegment.from(Instant.ofEpochSecond(statusLengthSecs),
            Instant.ofEpochSecond(2 * statusLengthSecs), SohStatusBit.MISSING),
        SohStatusSegment.from(Instant.ofEpochSecond(2 * statusLengthSecs),
            Instant.ofEpochSecond(3 * statusLengthSecs), SohStatusBit.UNSET),
        SohStatusSegment.from(Instant.ofEpochSecond(3 * statusLengthSecs),
            Instant.ofEpochSecond(4 * statusLengthSecs), SohStatusBit.MISSING),
        SohStatusSegment.from(Instant.ofEpochSecond(4 * statusLengthSecs),
            Instant.ofEpochSecond(5 * statusLengthSecs), SohStatusBit.SET)
    );
    ChannelSohStatusSegment status = ChannelSohStatusSegment
        .from(new UUID(0L, 0L), sohType, sohStatusSegments);

    List<ChannelSohQcMask> sohQcMasks = ChannelSohStatusParser
        .parseStatuses(status);

    assertEquals(2, sohQcMasks.size());
    verifyChannelSohQcMask(sohQcMasks.get(0), Instant.ofEpochSecond(0),
        Instant.ofEpochSecond(statusLengthSecs));
    verifyChannelSohQcMask(sohQcMasks.get(1), Instant.ofEpochSecond(4 * statusLengthSecs),
        Instant.ofEpochSecond(5 * statusLengthSecs));
  }

  @Test
  public void testCreateMasksNullExpectNullPointerExceptions() {
    assertThrows(NullPointerException.class, () -> ChannelSohStatusParser.parseStatuses(null));
  }

  private void verifyChannelSohQcMask(ChannelSohQcMask sohQcMask, Instant startTime,
      Instant endTime) {

    assertEquals(sohType, sohQcMask.getType());
    assertEquals(startTime, sohQcMask.getStartTime());
    assertEquals(endTime, sohQcMask.getEndTime());
  }

  private ChannelSohStatusSegment createMockChannelSohStatusSegment(
      boolean startingStatus, int numStatuses, UUID processingChannelId) {

    ChannelSohStatusSegment.Builder builder = ChannelSohStatusSegment
        .builder()
        .setChannelId(processingChannelId)
        .setType(sohType);

    boolean currentStatus = startingStatus;
    for (int i = 0; i < numStatuses; i++) {
      builder.addStatusSegment(Instant.ofEpochSecond(i * statusLengthSecs),
          Instant.ofEpochSecond((i + 1) * statusLengthSecs), currentStatus);
      currentStatus = !currentStatus;
    }

    return builder.build();
  }
}