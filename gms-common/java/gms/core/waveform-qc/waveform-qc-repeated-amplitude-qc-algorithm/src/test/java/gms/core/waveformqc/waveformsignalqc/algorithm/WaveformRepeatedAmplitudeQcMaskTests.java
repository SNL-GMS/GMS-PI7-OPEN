package gms.core.waveformqc.waveformsignalqc.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class WaveformRepeatedAmplitudeQcMaskTests {

  @Test
  public void testCreate() {
    Instant start = Instant.EPOCH;
    Instant end = Instant.MAX;
    UUID channelUuid = UUID.randomUUID();
    UUID channelSegmentUuid = UUID.randomUUID();
    WaveformRepeatedAmplitudeQcMask mask = WaveformRepeatedAmplitudeQcMask
        .create(start, end, channelUuid, channelSegmentUuid);

    assertEquals(start, mask.getStartTime());
    assertEquals(end, mask.getEndTime());
    assertEquals(channelUuid, mask.getChannelId());
    assertEquals(channelSegmentUuid, mask.getChannelSegmentId());
  }

  @Test
  public void testNullMaskParameterExceptionStartTime() {
    assertNullPointerWithMsg("WaveformRepeatedAmplitudeQcMask cannot have null startTime",
        () -> WaveformRepeatedAmplitudeQcMask.create(
            null, Instant.ofEpochSecond(2), UUID.randomUUID(), UUID.randomUUID()));
  }

  @Test
  public void testNullMaskParameterExceptionEndTime() {
    assertNullPointerWithMsg("WaveformRepeatedAmplitudeQcMask cannot have null endTime",
        () ->
            WaveformRepeatedAmplitudeQcMask
                .create(Instant.ofEpochSecond(1), null, UUID.randomUUID(), UUID.randomUUID()));
  }

  @Test
  public void testNullMaskParameterExceptionChannelId() {
    assertNullPointerWithMsg("WaveformRepeatedAmplitudeQcMask cannot have null channelId",
        () -> WaveformRepeatedAmplitudeQcMask
            .create(Instant.ofEpochSecond(1), Instant.ofEpochSecond(2), null, UUID.randomUUID()));
  }

  @Test
  public void testNullMaskParameterExceptionChannelSegmentId() {
    assertNullPointerWithMsg("WaveformRepeatedAmplitudeQcMask cannot have null channelSegmentId",
        () -> WaveformRepeatedAmplitudeQcMask.create(
            Instant.ofEpochSecond(1), Instant.ofEpochSecond(2), UUID.randomUUID(), null));
  }

  @Test
  public void testStartTimeAfterEndTimeExpectIllegalArgumentException() {
    final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> WaveformRepeatedAmplitudeQcMask
            .create(Instant.ofEpochSecond(2), Instant.ofEpochSecond(1), UUID.randomUUID(),
                UUID.randomUUID()));
    assertEquals("WaveformRepeatedAmplitudeQcMask startTime must be before endTime",
        ex.getMessage());
  }

  private static void assertNullPointerWithMsg(String msg, Executable exec) {
    final NullPointerException ex = assertThrows(NullPointerException.class, exec);
    assertEquals(msg, ex.getMessage(), "Expected particular exception msg");
  }
}
