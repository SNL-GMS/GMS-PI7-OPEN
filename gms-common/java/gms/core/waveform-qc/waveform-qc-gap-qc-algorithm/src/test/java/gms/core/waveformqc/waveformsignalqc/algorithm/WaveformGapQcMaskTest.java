package gms.core.waveformqc.waveformsignalqc.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class WaveformGapQcMaskTest {

  @Test
  public void testWaveformGapQcMaskCreation() {
    UUID channelUuid = UUID.randomUUID();
    UUID channelSegmentUuid = UUID.randomUUID();

    WaveformGapQcMask testWaveformGapQcMask = WaveformGapQcMask
        .create(QcMaskType.REPAIRABLE_GAP, channelUuid, channelSegmentUuid,
            Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2));

    assertEquals(QcMaskType.REPAIRABLE_GAP, testWaveformGapQcMask.getQcMaskType());
    assertEquals(Instant.ofEpochSecond(1), testWaveformGapQcMask.getStartTime());
    assertEquals(Instant.ofEpochSecond(2), testWaveformGapQcMask.getEndTime());
    assertEquals(channelUuid, testWaveformGapQcMask.getChannelId());
    assertEquals(channelSegmentUuid, testWaveformGapQcMask.getChannelSegmentId());

    testWaveformGapQcMask = WaveformGapQcMask
        .create(QcMaskType.LONG_GAP, channelUuid, channelSegmentUuid, Instant.ofEpochSecond(1),
            Instant.ofEpochSecond(2));

    assertEquals(QcMaskType.LONG_GAP, testWaveformGapQcMask.getQcMaskType());
  }

  @Test
  public void testNullMaskParameterExceptionMaskType() {
    assertThrowsWithMsg(NullPointerException.class,
        "Error creating WaveformGapQcMask: qcMaskType cannot be null",
        () -> WaveformGapQcMask
            .create(null, UUID.randomUUID(), UUID.randomUUID(),
                Instant.ofEpochSecond(1), Instant.ofEpochSecond(2)));
  }

  @Test
  public void testNullMaskParameterExceptionChannelId() {
    assertThrowsWithMsg(NullPointerException.class,
        "Error creating WaveformGapQcMask: channelId cannot be null",
        () -> WaveformGapQcMask
            .create(QcMaskType.LONG_GAP, null, UUID.randomUUID(),
                Instant.ofEpochSecond(1), Instant.ofEpochSecond(2)));
  }

  @Test
  public void testNullMaskParameterExceptionChannelSegmentId() {
    assertThrowsWithMsg(NullPointerException.class,
        "Error creating WaveformGapQcMask: channelSegmentId cannot be null",
        () -> WaveformGapQcMask
            .create(QcMaskType.LONG_GAP, UUID.randomUUID(), null,
                Instant.ofEpochSecond(1), Instant.ofEpochSecond(2)));
  }

  @Test
  public void testNullMaskParameterExceptionStartTime() {
    assertThrowsWithMsg(NullPointerException.class,
        "Error creating WaveformGapQcMask: startTime cannot be null",
        () -> WaveformGapQcMask
            .create(QcMaskType.LONG_GAP, UUID.randomUUID(), UUID.randomUUID(),
                null, Instant.ofEpochSecond(2)));
  }

  @Test
  public void testNullMaskParameterExceptionEndTime() {
    assertThrowsWithMsg(NullPointerException.class,
        "Error creating WaveformGapQcMask: endTime cannot be null",
        () -> WaveformGapQcMask
            .create(QcMaskType.LONG_GAP, UUID.randomUUID(), UUID.randomUUID(),
                Instant.ofEpochSecond(1), null));
  }

  @Test
  public void testStartTimeAfterEndTimeExpectIllegalArgumentException() {
    assertThrowsWithMsg(IllegalArgumentException.class,
        "Error creating WaveformGapQcMask: startTime must be before endTime",
        () -> WaveformGapQcMask.create(QcMaskType.LONG_GAP, UUID.randomUUID(),
            UUID.randomUUID(), Instant.ofEpochSecond(1), Instant.ofEpochSecond(1)));
  }

  @Test
  public void testWrongMaskTypeExpectIllegalArgumentException() {
    assertThrowsWithMsg(IllegalArgumentException.class,
        "Error creating WaveformGapQcMask: qcMaskType must be either LONG_GAP or REPAIRABLE_GAP",
        () -> WaveformGapQcMask.create(QcMaskType.CALIBRATION, UUID.randomUUID(), UUID.randomUUID(),
            Instant.ofEpochSecond(1), Instant.ofEpochSecond(2)));
  }

  private static <T extends Throwable> void assertThrowsWithMsg(
      Class<T> exClass, String msg, Executable exec) {
    final T ex = assertThrows(exClass, exec);
    assertEquals(msg, ex.getMessage(), "Expected exeception to have particular message");
  }
}
