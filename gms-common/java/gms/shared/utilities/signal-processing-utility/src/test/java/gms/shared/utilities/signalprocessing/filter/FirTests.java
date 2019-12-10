package gms.shared.utilities.signalprocessing.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.DoublePredicate;
import java.util.stream.IntStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class FirTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  /**
   * Assert filtering an impulse returns the coefficients in their original order
   */
  @Test
  public void testFilterImpulse() throws Exception {
    final double[] bCoefficients = new double[]{1.0, 2.0, 3.0, 4.0, 5.0};
    final double[] impulse = new double[]{1.0, 0.0, 0.0, 0.0, 0.0};

    assertTrue(Arrays.equals(bCoefficients, Fir.filter(impulse, bCoefficients)));
  }

  /**
   * Assert filtering an impulse train returns repetitions of the coefficients in their original
   * order
   */
  @Test
  public void testFilterImpulseTrain() throws Exception {

    double[] bCoefficients = new double[]{9.5, 8.5, 7.5, 6.5, 4.5, 3.4, 2.5, 1.5, 0.5, .25, .125};

    final int repeats = 3;
    double[] impulseTrain = new double[bCoefficients.length * repeats];
    double[] expectedOutput = new double[bCoefficients.length * repeats];

    int k = 0;
    for (int i = 0; i < repeats; ++i) {
      for (int j = 0; j < bCoefficients.length; ++j) {
        impulseTrain[k] = j == 0 ? 1.0 : 0.0;
        expectedOutput[k] = bCoefficients[j];
        k = k + 1;
      }
    }

    double[] res = Fir.filter(impulseTrain, bCoefficients);
    assertTrue(Arrays.equals(expectedOutput, res));
  }

  /**
   * Assert filtering a step function of 1.0 returns the DC gain (sum of the filter coefficients)
   */
  @Test
  public void testFilterStep() throws Exception {
    double[] bCoefficients = new double[]{1.23, 9.87, 2.34, 8.76, 3.45, 7.65, 4.56, 6.54};
    double[] step = new double[bCoefficients.length];
    Arrays.fill(step, 1.0);

    assertEquals(Arrays.stream(bCoefficients).sum(),
        Fir.filter(step, bCoefficients)[bCoefficients.length - 1], Double.MIN_NORMAL);
  }

  /**
   * Assert filtering a seismic waveform matches a Matlab output
   */
  @Test
  public void testFilterSeismic() throws Exception {
    double[] output = Fir.filter(FirTestData.inputWaveform, FirTestData.bCoeffs);

    // TODO: determine if 1E-9 is a good threshold.  1E-10 fails.
    final DoublePredicate withinRange = d -> d < 1E-9;
    assertTrue(IntStream.range(0, output.length)
        .mapToDouble(i -> Math.abs(output[i] - FirTestData.expectedFilteredWaveform[i]))
        .allMatch(withinRange));
  }

  @Test
  public void testFilterEmptyInputs() throws Exception {
    double[] bCoefficients = new double[]{1.23, 9.87, 2.34, 8.76, 3.45, 7.65, 4.56, 6.54};
    double[] empty = new double[0];
    assertTrue(Arrays.equals(empty, Fir.filter(empty, bCoefficients)));
  }

  @Test
  public void testFilterEmptyCeofficientsExpectIllegalArgumentException() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("FIR filtering requires non-empty bCoefficients");
    Fir.filter(new double[]{1.0, 0.0, 0.0, 0.0, 0.0}, new double[]{});
  }

  @Test
  public void testFilterNullInputExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FIR filtering requires non-null input signal");
    Fir.filter(null, new double[]{1.0, 2.0});
  }

  @Test
  public void testFilterNullIBCoefficientsExpectNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FIR filtering requires non-null bCoefficients");
    Fir.filter(new double[]{1.0, 2.0}, null);
  }
}
