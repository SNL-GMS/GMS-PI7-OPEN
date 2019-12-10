package gms.core.waveformqc.waveformsignalqc.algorithm;

import static gms.core.waveformqc.waveformsignalqc.algorithm.TestUtility.createChannelSegment;
import static gms.core.waveformqc.waveformsignalqc.algorithm.TestUtility.createWaveform;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WaveformGapInterpreterTest {

  private static Instant shortGapStart;
  private static Instant shortGapEnd;
  private static Instant longGapStart;
  private static Instant longGapEnd;
  private static Instant shortGap2Start;
  private static Instant shortGap2End;

  private static ChannelSegment<Waveform> noGap;
  private static ChannelSegment<Waveform> oneShortGap;
  private static ChannelSegment<Waveform> oneLongGap;
  private static ChannelSegment<Waveform> oneLongOneShortGap;

  private static int minLongGapLengthInSamples = 2;

  @BeforeAll
  public static void setUp() throws Exception {

    final double samplesPerSec = 40.0;
    final double samplePeriodSec = 1.0 / 40.0;

    final Instant startTime = Instant.EPOCH;
    final Waveform first = createWaveform(startTime, startTime.plusSeconds(10), samplesPerSec);

    Instant startTime2 = first.getEndTime().plusMillis((long) (samplePeriodSec * 1000));
    final Waveform secondNoGap = createWaveform(startTime2, startTime2.plusSeconds(10),
        samplesPerSec);
    noGap = createChannelSegment(List.of(first, secondNoGap));

    shortGapStart = first.getEndTime();
    shortGapEnd = first.getEndTime().plusMillis((long) (samplePeriodSec * 1000 * 2));
    final Waveform secondShortGap = createWaveform(shortGapEnd, shortGapEnd.plusSeconds(10),
        samplesPerSec);
    oneShortGap = createChannelSegment(List.of(first, secondShortGap));

    longGapStart = first.getEndTime();
    longGapEnd = first.getEndTime().plusMillis((long) (samplePeriodSec * 1000 * 3));
    final Waveform secondLongGap = createWaveform(longGapEnd, longGapEnd.plusSeconds(10),
        samplesPerSec);
    oneLongGap = createChannelSegment(List.of(first, secondLongGap));

    shortGap2Start = secondLongGap.getEndTime();
    shortGap2End = secondLongGap.getEndTime().plusMillis((long) (samplePeriodSec * 1000 * 2));
    final Waveform thirdShortGap = createWaveform(shortGap2End, shortGap2End.plusSeconds(10),
        samplesPerSec);
    oneLongOneShortGap = createChannelSegment(List.of(first, secondLongGap, thirdShortGap));
  }

  @Test
  public void testCreateWaveformGapQcMasksNoGap() {
    List<WaveformGapQcMask> masks = WaveformGapInterpreter
        .createWaveformGapQcMasks(noGap, minLongGapLengthInSamples);
    assertEquals(0, masks.size());
  }

  @Test
  public void testCreateWaveformGapQcMasksOneRepairableGap() {
    List<WaveformGapQcMask> masks = WaveformGapInterpreter
        .createWaveformGapQcMasks(oneShortGap, minLongGapLengthInSamples);
    assertEquals(1, masks.size());
    assertEquals(shortGapStart, masks.get(0).getStartTime());
    assertEquals(shortGapEnd, masks.get(0).getEndTime());
    assertEquals(QcMaskType.REPAIRABLE_GAP, masks.get(0).getQcMaskType());
  }

  @Test
  public void testCreateWaveformGapQcMasksOneLongGap() {
    List<WaveformGapQcMask> masks = WaveformGapInterpreter
        .createWaveformGapQcMasks(oneLongGap, minLongGapLengthInSamples);
    assertEquals(1, masks.size());
    assertEquals(longGapStart, masks.get(0).getStartTime());
    assertEquals(longGapEnd, masks.get(0).getEndTime());
    assertEquals(QcMaskType.LONG_GAP, masks.get(0).getQcMaskType());
  }

  @Test
  public void testCreateWaveformGapQcMasksOneLongOneRepairableGap() {
    List<WaveformGapQcMask> masks = WaveformGapInterpreter
        .createWaveformGapQcMasks(oneLongOneShortGap, minLongGapLengthInSamples);
    assertEquals(2, masks.size());

    assertEquals(longGapStart, masks.get(0).getStartTime());
    assertEquals(longGapEnd, masks.get(0).getEndTime());
    assertEquals(QcMaskType.LONG_GAP, masks.get(0).getQcMaskType());

    assertEquals(shortGap2Start, masks.get(1).getStartTime());
    assertEquals(shortGap2End, masks.get(1).getEndTime());
    assertEquals(QcMaskType.REPAIRABLE_GAP, masks.get(1).getQcMaskType());
  }

  @Test
  public void testCreateWaveformGapQcMasksTwoRepairableGap() {
    List<WaveformGapQcMask> masks = WaveformGapInterpreter
        .createWaveformGapQcMasks(oneLongOneShortGap, 3);
    assertEquals(2, masks.size());

    assertEquals(longGapStart, masks.get(0).getStartTime());
    assertEquals(longGapEnd, masks.get(0).getEndTime());
    assertEquals(QcMaskType.REPAIRABLE_GAP, masks.get(0).getQcMaskType());

    assertEquals(shortGap2Start, masks.get(1).getStartTime());
    assertEquals(shortGap2End, masks.get(1).getEndTime());
    assertEquals(QcMaskType.REPAIRABLE_GAP, masks.get(1).getQcMaskType());
  }

  @Test
  public void testCreateNullChannelSegmentExpectNullPointerException() {
    final NullPointerException ex = assertThrows(NullPointerException.class,
        () -> WaveformGapInterpreter.createWaveformGapQcMasks(null, 1));
    assertEquals("WaveformGapInterpreter.updateQcMasks requires non-null channelSegment", ex.getMessage());
  }

  @Test
  public void testCreateNonPositiveThresholdExpectIllegalArgumentException() {
    final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> WaveformGapInterpreter.createWaveformGapQcMasks(oneLongOneShortGap, 0));
  }
}