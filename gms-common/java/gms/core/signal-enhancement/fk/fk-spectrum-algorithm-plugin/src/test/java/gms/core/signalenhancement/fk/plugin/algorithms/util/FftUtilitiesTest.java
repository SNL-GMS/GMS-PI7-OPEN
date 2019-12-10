package gms.core.signalenhancement.fk.plugin.algorithms.util;

import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FftUtilitiesTest {

  private static final double[] MIDBAND_PASSED_EVEN = {
      0.0,      // bin 0 (real only)
      0.0,      // bin n/2 (real only)
      0.0, 0.0, // bin 1, etc...
      0.0, 0.0,
      0.0, 0.0,
      1.0, 1.0,
      1.0, 1.0,
      1.0, 1.0,
      1.0, 1.0,
      1.0, 1.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0  // bin n/2 - 1
  };

  private static final double[] HIGHBAND_PASSED_EVEN = {
      0.0,
      1.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      1.0, 1.0,
  };

  private static final double[] LOWBAND_PASSED_EVEN = {
      1.0,
      0.0,
      1.0, 1.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
  };

  private static final double[] NOTCH_EVEN = {
      0.0,
      0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      1.0, 1.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
  };

  private static final double[] MIDBAND_PASSED_ODD = {
      0.0,
      0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      1.0, 1.0,
      1.0, 1.0,
      1.0, 1.0,
      1.0, 1.0,
      1.0, 1.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0
  };

  private static final double[] HIGHBAND_PASSED_ODD = {
      0.0,
      1.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      1.0, 1.0,
      1.0
  };

  private static final double[] LOWBAND_PASSED_ODD = {
      1.0,
      0.0,
      1.0, 1.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0
  };

  private static final double[] NOTCH_ODD = {
      0.0,
      0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      1.0, 1.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0, 0.0,
      0.0
  };

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testCalculateFft() {
    // create 1000 samples from a 10 Hz sine wave
    double[] values = new double[1000];
    for (int i = 0; i < 1000; ++i) {
      values[i] = Math.sin((double) i / 1000 * 2 * Math.PI * 10.0);
    }

    // create a waveform with those values
    Waveform waveform = Waveform.from(
        Instant.ofEpochSecond(1514764800), // 01-JAN-2018 00:00:00 GMT
        1,
        1000,
        values);
    ArrayList<Waveform> waveformList = new ArrayList<>();
    waveformList.add(waveform);

    double[] result = FftUtilities.computeFft(waveformList).get(0);

    // For a 10 Hz sine wave, the imaginary part of the 10th bin should be non-zero.
    // All other values should be zero (or very close to it).
    // result[i] for even values of i are the real parts of the FFT.
    // result[i] for odd values of i are the imaginary parts of the FFT.
    final double NEAR_ZERO = 1.0e-8;
    final double DEFINITELY_NOT_ZERO = 300.0;
    assertTrue(Math.abs(result[2 * 10 + 1]) > DEFINITELY_NOT_ZERO);  // Im[10] is not zero
    for (int i = 0; i < 500; ++i) {
      assertTrue(Math.abs(result[2 * i]) < NEAR_ZERO);
      if (i == 10) {
        continue;
      }
      assertTrue(Math.abs(result[2 * i + 1]) < NEAR_ZERO);
    }
  }

  @Test
  public void testBandPassEvenNumberedArray() {
    double[] testFft = new double[HIGHBAND_PASSED_EVEN.length];
    Arrays.fill(testFft, 1.0);

    double[] mid = FftUtilities.freqBandPassFilter(testFft, testFft.length, 4, 8);
    assertTrue(Arrays.equals(mid, MIDBAND_PASSED_EVEN));

    double[] high = FftUtilities.freqBandPassFilter(testFft, testFft.length, 12, 13);
    assertTrue(Arrays.equals(high, HIGHBAND_PASSED_EVEN));

    double[] low = FftUtilities.freqBandPassFilter(testFft, testFft.length, 0, 1);
    assertTrue(Arrays.equals(low, LOWBAND_PASSED_EVEN));

    double[] notch = FftUtilities.freqBandPassFilter(testFft, testFft.length, 5, 5);
    assertTrue(Arrays.equals(notch, NOTCH_EVEN));
  }

  @Test
  public void testBandPassOddNumberedArray() {
    double[] testFft = new double[HIGHBAND_PASSED_ODD.length];
    Arrays.fill(testFft, 1.0);

    double[] mid = FftUtilities.freqBandPassFilter(testFft, testFft.length, 4, 8);
    assertTrue(Arrays.equals(mid, MIDBAND_PASSED_ODD));

    double[] high = FftUtilities.freqBandPassFilter(testFft, testFft.length, 12, 13);
    assertTrue(Arrays.equals(high, HIGHBAND_PASSED_ODD));

    double[] low = FftUtilities.freqBandPassFilter(testFft, testFft.length, 0, 1);
    assertTrue(Arrays.equals(low, LOWBAND_PASSED_ODD));

    double[] notch = FftUtilities.freqBandPassFilter(testFft, testFft.length, 5, 5);
    assertTrue(Arrays.equals(notch, NOTCH_ODD));
  }


  @Test
  public void testCalculateFftSquaredMagnitudeNullFftsExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("calculateFftSquaredMagnitudes requires non-null ffts");

    FftUtilities.calculateFftSquaredMagnitudes(null);
  }

  @Test
  public void testCalculateFftSquaredMagnitude() {
    final List<double[]> INPUT_FFTS = new ArrayList<>() {{
      add(new double[]{5.0, 10.0, 9.0, 2.0, 3.0, 8.0, 7.0, 4.0, 5.0, 6.0});
      add(new double[]{1.0, 10.0, 9.0, 2.0, 3.0, 8.0, 7.0, 4.0, 5.0, 6.0, 11.0});
      add(new double[]{5.0, 10.0, 9.0, 2.0, 3.0, 8.0, 7.0, 4.0, 11.0});
    }};

    final List<double[]> EXPECTED_OUTPUT_FFTS = new ArrayList<>() {{
      add(new double[]{25.0, 85.0, 73.0, 65.0, 61.0, 100.0});
      add(new double[]{1.0, 85.0, 73.0, 65.0, 61.0, 221.0});
      add(new double[]{25.0, 85.0, 73.0, 65.0, 221.0});
    }};

    final List<double[]> fftSquareMagnitudes = FftUtilities
        .calculateFftSquaredMagnitudes(INPUT_FFTS);

    for (int i = 0; i < EXPECTED_OUTPUT_FFTS.size(); i++) {
      Assert.assertArrayEquals(EXPECTED_OUTPUT_FFTS.get(i), fftSquareMagnitudes.get(i), 0.0);
    }
  }

  @Test
  public void testCalculateFftMagnitudeNullFftsExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("calculateFftMagnitudes requires non-null ffts");

    FftUtilities.calculateFftMagnitudes(null);
  }

  @Test
  public void testCalculateFftMagnitude() {
    final List<double[]> INPUT_FFTS = new ArrayList<>() {{
      add(new double[]{5.0, 10.0, 9.0, 2.0, 3.0, 8.0, 7.0, 4.0, 5.0, 6.0});
      add(new double[]{1.0, 10.0, 9.0, 2.0, 3.0, 8.0, 7.0, 4.0, 5.0, 6.0, 11.0});
      add(new double[]{5.0, 10.0, 9.0, 2.0, 3.0, 8.0, 7.0, 4.0, 11.0});
    }};

    final List<double[]> EXPECTED_OUTPUT_FFTS = new ArrayList<>() {{
      add(new double[]{5.0, 9.21954, 8.544, 8.06226, 7.81025, 10.0});
      add(new double[]{1.0, 9.21954, 8.544, 8.06226, 7.81025, 14.86607});
      add(new double[]{5.0, 9.21954, 8.544, 8.06226, 14.86607});
    }};

    List<double[]> fftMagnitudes = FftUtilities.calculateFftMagnitudes(INPUT_FFTS);

    for (int i = 0; i < EXPECTED_OUTPUT_FFTS.size(); i++) {
      Assert.assertArrayEquals(EXPECTED_OUTPUT_FFTS.get(i), fftMagnitudes.get(i), Math.pow(10, -5));
    }
  }

  @Test
  public void testGetRealPartOfFfts() {
    final List<double[]> INPUT_FFTS = new ArrayList<>() {{
      add(new double[]{5.0, 10.0, 9.0, 2.0, 3.0, 8.0, 7.0, 4.0, 5.0, 6.0});
      add(new double[]{1.0, 10.0, 9.0, 2.0, 3.0, 8.0, 7.0, 4.0, 5.0, 6.0, 11.0});
      add(new double[]{5.0, 10.0, 9.0, 2.0, 3.0, 8.0, 7.0, 4.0, 11.0});
    }};

    final List<double[]> EXPECTED_OUTPUT = new ArrayList<>() {{
      add(new double[]{5.0, 9.0, 3.0, 7.0, 5.0, 10.0});
      add(new double[]{1.0, 9.0, 3.0, 7.0, 5.0, 11.0});
      add(new double[]{5.0, 9.0, 3.0, 7.0, 11.0});
    }};

    List<double[]> fftReals = FftUtilities.getRealPartOfFfts(INPUT_FFTS);

    for (int i = 0; i < EXPECTED_OUTPUT.size(); i++) {
      Assert.assertArrayEquals(EXPECTED_OUTPUT.get(i), fftReals.get(i), 0.0001);
    }
  }

  @Test
  public void testGetImaginaryPartOfFfts() {
    final List<double[]> INPUT_FFTS = new ArrayList<>() {{
      add(new double[]{5.0, 10.0, 9.0, 2.0, 3.0, 8.0, 7.0, 4.0, 5.0, 6.0});
      add(new double[]{1.0, 10.0, 9.0, 2.0, 3.0, 8.0, 7.0, 4.0, 5.0, 6.0, 11.0});
      add(new double[]{5.0, 10.0, 9.0, 2.0, 3.0, 8.0, 7.0, 4.0, 11.0});
    }};

    final List<double[]> EXPECTED_OUTPUT = new ArrayList<>() {{
      add(new double[]{0.0, 2.0, 8.0, 4.0, 6.0, 0.0});
      add(new double[]{0.0, 2.0, 8.0, 4.0, 6.0, 10.0});
      add(new double[]{0.0, 2.0, 8.0, 4.0, 10.0});
    }};

    List<double[]> fftImags = FftUtilities.getImaginaryPartOfFfts(INPUT_FFTS);

    for (int i = 0; i < EXPECTED_OUTPUT.size(); i++) {
      Assert.assertArrayEquals(EXPECTED_OUTPUT.get(i), fftImags.get(i), 0.0001);
    }
  }
}