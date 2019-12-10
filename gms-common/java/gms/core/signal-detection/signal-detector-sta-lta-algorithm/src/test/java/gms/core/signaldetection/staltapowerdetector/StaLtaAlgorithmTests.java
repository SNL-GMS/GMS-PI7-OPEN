package gms.core.signaldetection.staltapowerdetector;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.core.signaldetection.staltapowerdetector.StaLtaAlgorithm.AlgorithmType;
import gms.core.signaldetection.staltapowerdetector.StaLtaAlgorithm.WaveformTransformation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaLtaAlgorithmTests {

  private static final Logger logger = LoggerFactory.getLogger(StaLtaAlgorithmTests.class);

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  // STA / LTA Tests

  @Test
  public void testStaLta() {
    // STA is current sample, LTA is two previous samples
    // STA =      [-, -, 100,    50,     1,       1, 1, 1, 200]
    // LTA =      [-, -,   1,  50.5,    75,    50.5, 1, 1,   1]
    // STA/LTA =  [-, -, 100,  0.99, 0.013, 0.00026, 1, 1, 200]
    // Triggers = [-, -,   Y,     N,     N,       N, N, N,   Y]
    final double[] waveform = new double[]{1, 1, 100, 50, 1, 1, 1, 1, 200};

    logger.info("Calling STA/LTA algorithm");
    final Set<Integer> triggers = new StaLtaAlgorithm()
        .staLta(AlgorithmType.STANDARD, WaveformTransformation.RECTIFIED, 0, 1, 2, 2, 5.0, 3.0,
            waveform);

    assertNotNull(triggers);
    assertEquals(2, triggers.size());
    assertTrue(triggers.contains(2));
    assertTrue(triggers.contains(8));
  }

  @Test
  public void testStaLtaWaveformNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("STA/LTA cannot operate on a null waveform");
    new StaLtaAlgorithm()
        .staLta(AlgorithmType.STANDARD, WaveformTransformation.RECTIFIED, 0, 1, 2, 2, 5.0, 3.0,
            null);
  }

  @Test
  public void testStaLtaAlgorithmTypeNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("STA/LTA requires non-null algorithmType");
    new StaLtaAlgorithm()
        .staLta(null, WaveformTransformation.RECTIFIED, 0, 1, 2, 2, 5.0, 3.0, new double[]{});
  }

  @Test
  public void testStaLtaWaveformTransformationNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("STA/LTA requires non-null waveformTransformation");
    new StaLtaAlgorithm().staLta(AlgorithmType.STANDARD, null, 0, 1, 2, 2, 5.0, 3.0, new double[]{});
  }

  @Test
  public void testSmeExample() {
    final int staLeadSamples = 3;
    final int staLengthSamples = 4;
    final int ltaLeadSamples = 103;
    final int ltaLengthSamples = 100;
    final double triggerThreshold = 10.0;
    final double detriggerThreshold = 6.0;

    // Test the STA/LTA transform matches SME expectations
    double[] transformed = StaLtaAlgorithm
        .transform(AlgorithmType.STANDARD, WaveformTransformation.SQUARED, staLeadSamples,
            staLengthSamples, ltaLeadSamples, ltaLengthSamples, SmeExampleData.data).toArray();

    assertEquals(SmeExampleData.sta.length, SmeExampleData.lta.length);
    double[] expectedTransformed = new double[SmeExampleData.sta.length - ltaLeadSamples];
    for(int i = ltaLeadSamples; i < SmeExampleData.sta.length; ++i) {
      expectedTransformed[i - ltaLeadSamples] = SmeExampleData.sta[i] / SmeExampleData.lta[i];
    }

    assertArrayEquals(expectedTransformed, transformed, 1e-10);

    // Test data is at 20 samples/sec and has detections at 100.0s and 200.15s from the start
    final double sampleRate = 20.0;
    final Set<Integer> expectedDetections = Set.of(
        (int) (100.0 * sampleRate),
        (int) (200.15 * sampleRate));

    // Test detections match SME expected detections
    final Set<Integer> detections = new StaLtaAlgorithm()
        .staLta(AlgorithmType.STANDARD, WaveformTransformation.SQUARED, staLeadSamples,
            staLengthSamples, ltaLeadSamples, ltaLengthSamples, triggerThreshold,
            detriggerThreshold,
            SmeExampleData.data);

    assertEquals(expectedDetections.size(), detections.size());
    assertTrue(expectedDetections.containsAll(detections));
    assertTrue(detections.containsAll(expectedDetections));
  }

  // Transform algorithm tests

  @Test
  public void testOutputLengthStaLeads() {
    final int length = 100;
    final double[] transformed = StaLtaAlgorithm
        .transform(AlgorithmType.STANDARD, WaveformTransformation.RECTIFIED, 2,
            2, 6, 4, new double[length]).toArray();

    assertEquals(length - 6, transformed.length);
  }

  @Test
  public void testOutputLengthStaLags() {
    final int length = 200;
    final double[] transformed =
        StaLtaAlgorithm.transform(AlgorithmType.STANDARD, WaveformTransformation.RECTIFIED, 2,
            4, 6, 5, new double[length]).toArray();

    // Sta length is 4 and lead is 2.  For sample i the STA window is: [i-2, i-1, i, i+1]
    // This is one lagging sample, so input.length - 2 is the last index that can be computed

    // Input length - ltaLeadSamples - staLagSamples
    assertEquals(length - 6 - 1, transformed.length);
  }

  @Test
  public void testOutputLengthStaBeginsBeforeSta() {
    final int length = 100;
    final double[] transformed = StaLtaAlgorithm
        .transform(AlgorithmType.STANDARD, WaveformTransformation.RECTIFIED, 10,
            2, 6, 4, new double[length]).toArray();

    assertEquals(length - 10, transformed.length);
  }

  @Test
  public void testOutputLengthLtaLags() {
    final int length = 200;
    final double[] transformed =
        StaLtaAlgorithm.transform(AlgorithmType.STANDARD, WaveformTransformation.RECTIFIED, 2,
            4, 6, 13, new double[length]).toArray();

    // LTA length is 13 and lead is 6.  For sample i the LTA window is: [i-6, i-5, ..., i+6]
    // This is 6 lagging samples, so input.length - 6 is the last index that can be computed

    // Input length - leadSamples - lagSamples
    assertEquals(length - 6 - 6, transformed.length);
  }

  @Test
  public void testNegativeOutputLength() {
    final double[] result = StaLtaAlgorithm
        .transform(AlgorithmType.STANDARD, WaveformTransformation.RECTIFIED, 0, 6, 10, 9,
            new double[10]).toArray();

    assertNotNull(result);
    assertEquals(0, result.length);
  }

  @Test
  public void testStandardRectified() {
    final int length = 100;
    final int staLeadSamples = 2;
    final int staLengthSamples = 2;
    final int ltaLeadSamples = 6;
    final int ltaLengthSamples = 4;
    final double[] waveform = DoubleStream.iterate(0.0, d -> d - 1.1).limit(length).toArray();

    // Compute expected results
    final double[] rectified = Arrays.stream(waveform).map(Math::abs).toArray();
    final double[] expected = getExpectedStaLta(length, staLeadSamples, staLengthSamples,
        ltaLeadSamples, ltaLengthSamples, rectified);

    // Get actual results
    final double[] transformResult = StaLtaAlgorithm
        .transform(AlgorithmType.STANDARD, WaveformTransformation.RECTIFIED, staLeadSamples,
            staLengthSamples, ltaLeadSamples, ltaLengthSamples, waveform).toArray();

    assertArrayEquals(expected, transformResult, 0);
  }

  @Test
  public void testStandardSquared() {
    final int length = 100;
    final int staLeadSamples = 2;
    final int staLengthSamples = 2;
    final int ltaLeadSamples = 6;
    final int ltaLengthSamples = 4;
    final double[] waveform = DoubleStream.iterate(0.0, d -> d - 1.1).limit(length).toArray();

    // Compute expected results
    final double[] squared = Arrays.stream(waveform).map(d -> d * d).toArray();
    final double[] expected = getExpectedStaLta(length, staLeadSamples, staLengthSamples,
        ltaLeadSamples, ltaLengthSamples, squared);

    // Get actual results
    final double[] transformResult = StaLtaAlgorithm
        .transform(AlgorithmType.STANDARD, WaveformTransformation.SQUARED, staLeadSamples,
            staLengthSamples, ltaLeadSamples, ltaLengthSamples, waveform).toArray();

    assertArrayEquals(expected, transformResult, 0);
  }

  /**
   * Compute the average value of the input values between start and end (inclusive)
   *
   * @param values compute average from these values
   * @param start first sample in the average, inclusive
   * @param end last sample in the average, inclusive
   * @return average of the input samples
   */
  private static double average(double[] values, int start, int end) {
    final int len = (end - start) + 1;
    final double sum = IntStream.rangeClosed(start, end).mapToDouble(i -> values[i]).sum();
    return sum / len;
  }

  /**
   * Computes an expected STA/LTA waveform.
   */
  private double[] getExpectedStaLta(int length, int staLeadSamples, int staLengthSamples,
      int ltaLeadSamples, int ltaLengthSamples, double[] waveform) {

    return IntStream.range(ltaLeadSamples, length).mapToDouble(i ->
        average(waveform, i - staLeadSamples, i - staLeadSamples + staLengthSamples - 1) /
            average(waveform, i - ltaLeadSamples, i - ltaLeadSamples + ltaLengthSamples - 1)
    ).toArray();
  }

  @Test
  public void testStaLag() {
    final int length = 102;
    final int staLeadSamples = 2;
    final int staLengthSamples = 5;
    final int staLagSamples = staLengthSamples - staLeadSamples - 1;

    final int ltaLeadSamples = 8;
    final int ltaLengthSamples = 6;

    // Constant waveform until the last two samples.  These will only ever be in the lagging STA.
    final double[] waveform = new double[length];
    Arrays.fill(waveform, 2.0);
    waveform[length - 1] = 10.0;
    waveform[length - 2] = 10.0;

    // fistIndex = 8 (ltaLead) and lastIndex = 99 (102 - 1 - staLagSamples)
    final int lastIndex = length - 1 - staLagSamples;
    final double[] expected = new double[lastIndex - ltaLeadSamples + 1];

    // STA/LTA equal until the lag portion.  LTA is always 2.0
    Arrays.fill(expected, 1.0);

    // STA samples: [2.0, 2.0, 2.0, 10.0, 10.0]; STA = 26/5 = 5.2; STA/LTA = 5.2/2 = 2.6
    expected[expected.length - 1] = 2.6;

    // STA samples: [2.0, 2.0, 2.0, 2.0, 10.0]; STA = 18/5 = 3.6; STA/LTA = 3.6/2 = 1.8
    expected[expected.length - 2] = 1.8;

    // Get actual results
    final double[] transformResult = StaLtaAlgorithm
        .transform(AlgorithmType.STANDARD, WaveformTransformation.RECTIFIED, staLeadSamples,
            staLengthSamples, ltaLeadSamples, ltaLengthSamples, waveform).toArray();

    assertArrayEquals(expected, transformResult, 0);
  }

  @Test
  public void testRecursiveExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Recursive STA/LTA not implemented");
    StaLtaAlgorithm.transform(AlgorithmType.RECURSIVE, WaveformTransformation.RECTIFIED, 2,
        4, 6, 6, new double[10]);
  }

  @Test
  public void testNegativeStaLengthExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("STA window must have positive length");
    StaLtaAlgorithm.transform(AlgorithmType.STANDARD, WaveformTransformation.RECTIFIED, 2,
        -4, 4, 2, new double[10]);
  }

  @Test
  public void testNegativeLtaLengthExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("LTA window must have positive length");
    StaLtaAlgorithm.transform(AlgorithmType.STANDARD, WaveformTransformation.RECTIFIED, 2,
        4, 4, -2, new double[10]);
  }

  // Triggering algorithm tests

  @Test
  public void testTrigger() {
    Set<Integer> triggers = StaLtaAlgorithm
        .trigger(5.0, 4.0, Arrays.stream(new double[]{1, 2, 3, 4, 5.01, 6, 4.5, 3.99}));

    assertNotNull(triggers);
    assertEquals(1, triggers.size());
    assertTrue(triggers.contains(4));
  }

  @Test
  public void testNoTrigger() {
    Set<Integer> triggers = StaLtaAlgorithm
        .trigger(5.0, 4.0, Arrays.stream(new double[]{1, 2, 3, 4, 5.0, 5.0, 4, 3.99}));

    assertNotNull(triggers);
    assertEquals(0, triggers.size());
  }

  @Test
  public void testNoDetrigger() {
    Set<Integer> triggers = StaLtaAlgorithm
        .trigger(5.0, 4.0, Arrays.stream(new double[]{20.0, 4.0, 30.0, 4.0, 6.0, 5.0, 4.0, 10.0}));

    assertNotNull(triggers);
    assertEquals(1, triggers.size());
    assertTrue(triggers.contains(0));
  }

  @Test
  public void testNaNDoesNotTrigger() {
    // Transformed waveform will contain a Double.NaN when the LTA is 0 (x/0.0D = NaN)
    Set<Integer> triggers = StaLtaAlgorithm
        .trigger(5.0, 4.0, Arrays.stream(new double[]{1, 2, 3, 4, Double.NaN, 6, 4.5, 3.99}));

    assertNotNull(triggers);
    assertEquals(1, triggers.size());
    assertTrue(triggers.contains(5));
  }

  @Test
  public void testPositiveInfinityTriggers() {
    Set<Integer> triggers = StaLtaAlgorithm
        .trigger(5.0, 4.0,
            Arrays.stream(new double[]{1, 2, 3, 4, Double.POSITIVE_INFINITY, 6, 4.5, 3.99}));

    assertNotNull(triggers);
    assertEquals(1, triggers.size());
    assertTrue(triggers.contains(4));
  }

  @Test
  public void testNegativeInfinityDoesNotTrigger() {
    Set<Integer> triggers = StaLtaAlgorithm
        .trigger(5.0, 4.0,
            Arrays.stream(new double[]{1, 2, 3, 4, Double.NEGATIVE_INFINITY, 6, 4.5, 3.99}));

    assertNotNull(triggers);
    assertEquals(1, triggers.size());
    assertTrue(triggers.contains(5));
  }

  @Test
  public void testNegativeTriggerThresholdExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("STA/LTA trigger threshold must be positive");
    StaLtaAlgorithm.trigger(-5.0, 4.0, Arrays.stream(new double[]{}));
  }

  @Test
  public void testNegativeDetriggerThresholdExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("STA/LTA detrigger threshold must be positive");
    StaLtaAlgorithm.trigger(5.0, -4.0, Arrays.stream(new double[]{}));
  }
}
