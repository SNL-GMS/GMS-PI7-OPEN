package gms.shared.utilities.signalprocessing.snr;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.utilities.signalprocessing.normalization.Transform;
import java.time.Duration;
import java.time.Instant;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SignalNoiseRatioTests {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private static Waveform waveform;
  private static Instant noiseWindowStart;
  private static Instant noiseWindowEnd;
  private static Instant signalWindowStart;
  private static Instant signalWindowEnd;
  private static Duration slidingWindowSize;
  private static double expectedSnr = 149.6665;

  @BeforeClass
  public static void init() {
    double[] waveformValues = new double[200];
    for (int i = 0; i < 150; i++) {
        waveformValues[i] = 1;
    }

    waveformValues[150] = 2;
    waveformValues[151] = 3;
    waveformValues[152] = -3;
    waveformValues[153] = -2;

    for (int i = 154; i < 200; i++) {
      waveformValues[i] = 1;
    }

    noiseWindowStart = Instant.EPOCH;
    waveform = Waveform.from(noiseWindowStart, 2, 200, waveformValues);

    noiseWindowEnd = waveform.computeSampleTime(144);
    signalWindowStart = waveform.computeSampleTime(146);
    signalWindowEnd = waveform.computeSampleTime(157);
    slidingWindowSize = Duration.ofSeconds(1);
  }

  @Test
  public void testGetSnrNullWaveform() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SNR cannot be calculated from a null waveform");
    SignalNoiseRatio.getSnr(null,
        noiseWindowStart,
        noiseWindowEnd,
        signalWindowStart,
        signalWindowEnd,
        slidingWindowSize,
        Transform.ABS);
  }

  @Test
  public void testGetSnrNullNoiseWindowStart() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SNR cannot be calculated from a null Noise Window Start");
    SignalNoiseRatio.getSnr(waveform,
        null,
        noiseWindowEnd,
        signalWindowStart,
        signalWindowEnd,
        slidingWindowSize,
        Transform.ABS);
  }

  @Test
  public void testGetSnrNullNoiseWindowEnd() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SNR cannot be calculated from a null Noise Window End");
    SignalNoiseRatio.getSnr(waveform,
        noiseWindowStart,
        null,
        signalWindowStart,
        signalWindowEnd,
        slidingWindowSize,
        Transform.ABS);
  }

  @Test
  public void testGetSnrNullSignalWindowStart() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SNR cannot be calculated from a null Signal Window Start");
    SignalNoiseRatio.getSnr(waveform,
        noiseWindowStart,
        noiseWindowEnd,
        null,
        signalWindowEnd,
        slidingWindowSize,
        Transform.ABS);
  }

  @Test
  public void testGetSnrNullSignalWindowEnd() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SNR cannot be calculated from a null Signal Window End");
    SignalNoiseRatio.getSnr(waveform,
        noiseWindowStart,
        noiseWindowEnd,
        signalWindowStart,
        null,
        slidingWindowSize,
        Transform.ABS);
  }

  @Test
  public void testGetSnrNullSlidingWindowSize() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SNR cannot be calculated from a null Sliding Window Size");
    SignalNoiseRatio.getSnr(waveform,
        noiseWindowStart,
        noiseWindowEnd,
        signalWindowStart,
        signalWindowEnd,
        null,
        Transform.ABS);
  }

  @Test
  public void testGetSnrNullTransform() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SNR cannot be calculated from a null Transform");
    SignalNoiseRatio.getSnr(waveform,
        noiseWindowStart,
        noiseWindowEnd,
        signalWindowStart,
        signalWindowEnd,
        slidingWindowSize,
        null);
  }

  public void testGetSnrNegativeNoiseWindow() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Noise Window Start must be before Noise Window End");
    SignalNoiseRatio.getSnr(waveform,
        noiseWindowEnd,
        noiseWindowStart,
        signalWindowStart,
        signalWindowEnd,
        slidingWindowSize,
        Transform.ABS);
  }

  public void testGetSnrNegativeSignalWindow() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Signal Window Start must be before Signal Window End");
    SignalNoiseRatio.getSnr(waveform,
        noiseWindowStart,
        noiseWindowEnd,
        signalWindowEnd,
        signalWindowStart,
        slidingWindowSize,
        Transform.ABS);
  }

  @Test
  public void testGetSnrOverlappingSignalWindow() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SNR cannot be calculated from a null Transform");
    SignalNoiseRatio.getSnr(waveform,
        noiseWindowStart,
        signalWindowStart,
        noiseWindowEnd,
        signalWindowEnd,
        slidingWindowSize,
        null);
  }

  @Test
  public void testGetSnrNoiseWindowStartsBeforeWaveform() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Noise window cannot start before the waveform start time");
    SignalNoiseRatio.getSnr(waveform,
        waveform.getStartTime().minusSeconds(1),
        noiseWindowEnd,
        signalWindowStart,
        signalWindowEnd,
        slidingWindowSize,
        Transform.ABS);
  }

  @Test
  public void testGetSnrNoiseWindowEndsAfterWaveform() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Noise window cannot end after the waveform end time");
    SignalNoiseRatio.getSnr(waveform,
        noiseWindowStart,
        waveform.getEndTime().plusNanos(10),
        waveform.getEndTime().plusNanos(2000),
        waveform.getEndTime().plusNanos(500000),
        slidingWindowSize,
        Transform.ABS);
  }

  @Test
  public void testGetSnrSignalWindowStartsBeforeWaveform() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Signal window cannot start before the waveform start time");
    SignalNoiseRatio.getSnr(waveform,
        signalWindowStart,
        signalWindowEnd,
        waveform.getStartTime().minusSeconds(1),
        noiseWindowEnd,
        slidingWindowSize,
        Transform.ABS);
  }

  @Test
  public void testGetSnrSignalWindowEndsAfterWaveform() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Signal window cannot end after the waveform end time");
    SignalNoiseRatio.getSnr(waveform,
        noiseWindowStart,
        noiseWindowEnd,
        signalWindowStart,
        waveform.getEndTime().plusSeconds(1),
        slidingWindowSize,
        Transform.ABS);
  }

  @Test
  public void testGetSnrSlidingWindowLargerThanSignalWindow() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Sliding window cannot be larger than the signal window");
    SignalNoiseRatio.getSnr(waveform,
        noiseWindowStart,
        noiseWindowEnd,
        signalWindowStart,
        signalWindowEnd,
        Duration.between(signalWindowStart, signalWindowEnd.plusNanos(5)),
        Transform.ABS);
  }

  @Test
  public void testGetSnr() {
    double snr = SignalNoiseRatio.getSnr(waveform,
        noiseWindowStart,
        noiseWindowEnd,
        signalWindowStart,
        signalWindowEnd,
        slidingWindowSize,
        Transform.ABS);
    assertEquals(expectedSnr, snr, .001);
  }

}
