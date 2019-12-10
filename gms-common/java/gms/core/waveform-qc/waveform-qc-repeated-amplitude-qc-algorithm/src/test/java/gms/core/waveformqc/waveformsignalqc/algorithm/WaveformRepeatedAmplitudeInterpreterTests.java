package gms.core.waveformqc.waveformsignalqc.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.DoubleStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WaveformRepeatedAmplitudeInterpreterTests {

  private static final Instant start = Instant.EPOCH;
  private static final int durationSecs = 10;
  private static final Instant end = Instant.EPOCH.plusSeconds(durationSecs);

  private static final UUID channelId = UUID.randomUUID();

  private static final int sampleRate = 2;
  private static final double nanosPerSample = 1.0e9 / sampleRate;

  private final ChannelSegment<Waveform> oneToZeroChannelSegment = createChannelSegment(List.of(
      createWaveform(createOneToZeroValues(), start)));

  private final ChannelSegment<Waveform> multipleRepeatsChannelSegment = createChannelSegment(
      List.of(createWaveform(createMultipleRepeatsValues(), start)));

  private final ChannelSegment<Waveform> multipleChannelSegments = createChannelSegment(
      List.of(createWaveform(createMultipleRepeatsValues(), start),
          createWaveform(createMultipleRepeatsValues(), start.plusMillis(10500))));

  private final ChannelSegment<Waveform> backToBackRepeatesChannelSegment = createChannelSegment(
      List.of(createWaveform(createBackToBackRepeats(), start)));

  private static ChannelSegment<Waveform> createChannelSegment(List<Waveform> waveforms) {
    return ChannelSegment
        .create(channelId, "test", ChannelSegment.Type.RAW, waveforms, CreationInfo.DEFAULT);
  }

  private static Waveform createWaveform(double[] values, Instant start) {
    return Waveform.withValues(start, sampleRate, values);
  }

  private static double[] createOneToZeroValues() {
    final double step = 1 / 25.0;
    return DoubleStream.iterate(step, d -> d + step).limit(21).toArray();
  }

  private static double[] createMultipleRepeatsValues() {
    return new double[]{
        0.0, 0.0, 0.0, 10, 20, 30, 40, 50, 60, 70,
        80.0, 80.0, 80.0, 0, 1, 2, 3, 4, 5, 6,
        7
    };
  }

  private static double[] createBackToBackRepeats() {
    return new double[]{
        0.0, 0.0, 3, 3, 3, 3, 3, 3, 3, 3,
        10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
        0, 0};
  }

  private WaveformRepeatedAmplitudeInterpreter waveformRepeatedAmplitudeInterpreter;

  @BeforeEach
  public void setUp() {
    waveformRepeatedAmplitudeInterpreter = new WaveformRepeatedAmplitudeInterpreter();
  }

  @Test
  public void testCreateWaveformRepeatedAmplitudeQcMasksEntireWaveformRepeats() throws Exception {
    final int minRepeatedSamples = 3;
    final double maxDeltaFromStart = 1.0;

    List<WaveformRepeatedAmplitudeQcMask> repeats = waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(oneToZeroChannelSegment, minRepeatedSamples,
            maxDeltaFromStart);

    assertEquals(1, repeats.size());

    WaveformRepeatedAmplitudeQcMask mask = repeats.get(0);
    assertEquals(channelId, mask.getChannelId());
    assertEquals(oneToZeroChannelSegment.getId(), mask.getChannelSegmentId());
    assertEquals(start, mask.getStartTime());
    assertEquals(end, mask.getEndTime());
  }

  @Test
  public void testCreateWaveformRepeatedAmplitudeQcMasksBackToBackRepeats() {
    final int minRepeatedSamples = 3;
    final double maxDeltaFromStart = 1.0;

    List<WaveformRepeatedAmplitudeQcMask> repeats = waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(backToBackRepeatesChannelSegment,
            minRepeatedSamples, maxDeltaFromStart);

    assertEquals(2, repeats.size());
    assertTrue(repeats.stream().map(WaveformRepeatedAmplitudeQcMask::getChannelId)
        .allMatch(backToBackRepeatesChannelSegment.getChannelId()::equals));
    assertTrue(repeats.stream().map(WaveformRepeatedAmplitudeQcMask::getChannelSegmentId)
        .allMatch(backToBackRepeatesChannelSegment.getId()::equals));

    repeats.sort(Comparator.comparing(WaveformRepeatedAmplitudeQcMask::getStartTime));

    Waveform maskWaveform = backToBackRepeatesChannelSegment.getTimeseries().get(0);
    WaveformRepeatedAmplitudeQcMask firstMask = repeats.get(0);
    assertEquals(maskWaveform.computeSampleTime(2), firstMask.getStartTime());
    assertEquals(maskWaveform.computeSampleTime(9), firstMask.getEndTime());

    WaveformRepeatedAmplitudeQcMask secondMask = repeats.get(1);
    assertEquals(maskWaveform.computeSampleTime(10), secondMask.getStartTime());
    assertEquals(maskWaveform.computeSampleTime(19), secondMask.getEndTime());
  }

  @Test
  public void testCreateWaveformRepeatedAmplitudeQcMasksMultipleRepeats() {
    final int minRepeatedSamples = 3;
    final double maxDeltaFromStart = 0.5;

    List<WaveformRepeatedAmplitudeQcMask> repeats = waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(multipleRepeatsChannelSegment, minRepeatedSamples,
            maxDeltaFromStart);

    assertEquals(2, repeats.size());

    assertTrue(repeats.stream().map(WaveformRepeatedAmplitudeQcMask::getChannelSegmentId)
        .allMatch(multipleRepeatsChannelSegment.getId()::equals));

    assertTrue(repeats.stream().map(WaveformRepeatedAmplitudeQcMask::getChannelId)
        .allMatch(channelId::equals));

    repeats.sort(Comparator.comparing(WaveformRepeatedAmplitudeQcMask::getStartTime));

    WaveformRepeatedAmplitudeQcMask mask = repeats.get(0);
    assertEquals(start, mask.getStartTime());
    assertEquals(start.plusNanos((long) (2 * nanosPerSample)), mask.getEndTime());

    mask = repeats.get(1);
    assertEquals(start.plusNanos((long) (10 * nanosPerSample)), mask.getStartTime());
    assertEquals(start.plusNanos((long) (12 * nanosPerSample)), mask.getEndTime());
  }

  @Test
  public void testCreateWaveformRepeatedAmplitudeQcMasksMultipleChannelSegments() {
    final int minRepeatedSamples = 2;
    final double maxDeltaFromStart = 0.0000001;

    List<WaveformRepeatedAmplitudeQcMask> repeats = waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(multipleChannelSegments, minRepeatedSamples,
            maxDeltaFromStart);

    assertEquals(4, repeats.size());

    assertTrue(repeats.stream().map(WaveformRepeatedAmplitudeQcMask::getChannelSegmentId)
        .allMatch(multipleChannelSegments.getId()::equals));

    assertTrue(repeats.stream().map(WaveformRepeatedAmplitudeQcMask::getChannelId)
        .allMatch(channelId::equals));

    repeats.sort(Comparator.comparing(WaveformRepeatedAmplitudeQcMask::getStartTime));

    WaveformRepeatedAmplitudeQcMask mask = repeats.get(0);
    assertEquals(start, mask.getStartTime());
    assertEquals(start.plusNanos((long) (2 * nanosPerSample)), mask.getEndTime());

    mask = repeats.get(1);
    assertEquals(start.plusNanos((long) (10 * nanosPerSample)), mask.getStartTime());
    assertEquals(start.plusNanos((long) (12 * nanosPerSample)), mask.getEndTime());

    mask = repeats.get(2);
    assertEquals(start.plusNanos((long) (21 * nanosPerSample)), mask.getStartTime());
    assertEquals(start.plusNanos((long) (23 * nanosPerSample)), mask.getEndTime());

    mask = repeats.get(3);
    assertEquals(start.plusNanos((long) (31 * nanosPerSample)), mask.getStartTime());
    assertEquals(start.plusNanos((long) (33 * nanosPerSample)), mask.getEndTime());
  }

  @Test
  public void testCreateNoRepeatedAmplitudesExpectNoMasks() {
    // Use repeats outside configured range

    final int minRepeatedSamples = 3;
    final double maxDeltaFromStart = 1.0 / 1000;

    List<WaveformRepeatedAmplitudeQcMask> repeats = waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(oneToZeroChannelSegment, minRepeatedSamples,
            maxDeltaFromStart);

    // Samples change by 1/25 which is > maxDeltaFromStart
    assertEquals(0, repeats.size());
  }

  @Test
  public void testCreateNotEnoughRepeatedAmplitudesExpectNoMasks() {
    final int minRepeatedSamples = 4;
    final double maxDeltaFromStart = 0.5;

    List<WaveformRepeatedAmplitudeQcMask> repeats = waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(multipleRepeatsChannelSegment, minRepeatedSamples,
            maxDeltaFromStart);

    // Repeats are only 3 samples long so expect no repeated amplitude masks
    assertEquals(0, repeats.size());
  }

  @Test
  public void testCreateMinRepeatedSamplesTooLowExpectIllegalArgumentException() {
    final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> waveformRepeatedAmplitudeInterpreter
            .createWaveformRepeatedAmplitudeQcMasks(oneToZeroChannelSegment, 1, 1.0));
    assertEquals(
        "WaveformRepeatedAmplitudeInterpreter.createWaveformRepeatedAmplitudeQcMasks requires " +
            "minRepeatedSamples > 1",
        ex.getMessage());
  }

  @Test
  public void testCreateDeviationNotPositiveExpectIllegalArgumentException() {
    final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> waveformRepeatedAmplitudeInterpreter
            .createWaveformRepeatedAmplitudeQcMasks(oneToZeroChannelSegment, 2,
                0.0 - Double.MIN_NORMAL));
    assertEquals(
        "WaveformRepeatedAmplitudeInterpreter.createWaveformRepeatedAmplitudeQcMasks requires " +
            "maxDeltaFromStartAmplitude >= 0.0", ex.getMessage());
  }

  @Test
  public void testCreateNullChannelSegmentExpectNullPointerException() {
    final NullPointerException ex = assertThrows(NullPointerException.class,
        () -> waveformRepeatedAmplitudeInterpreter
            .createWaveformRepeatedAmplitudeQcMasks(null, 2, 1.0));
    assertEquals(
        "WaveformRepeatedAmplitudeInterpreter.createWaveformRepeatedAmplitudeQcMasks requires " +
            "non-null channelSegment", ex.getMessage());
  }
}
