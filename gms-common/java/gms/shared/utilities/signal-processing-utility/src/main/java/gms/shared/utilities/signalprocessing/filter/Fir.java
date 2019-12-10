package gms.shared.utilities.signalprocessing.filter;

import java.util.Arrays;
import java.util.Objects;

/**
 * Implements FIR filtering
 */
public class Fir {

  /**
   * Applies an FIR filter defined by the bCoefficients to the input:
   * output[n] = sum{k=0 to N-1}[bCoefficients(k) * input(n-k)]
   * where N = length(bCoefficients)
   *
   * bCoefficients must contain at least one element for the output to be defined.
   *
   * @param input input, not null
   * @param bCoefficients bCoefficients, not null
   * @return output, not null
   * @throws NullPointerException if input or bCoefficients are null
   * @throws IllegalArgumentException if bCoeffiecients is empty
   */
  public static double[] filter(double[] input, double[] bCoefficients) {

    Objects.requireNonNull(input, "FIR filtering requires non-null input signal");
    Objects.requireNonNull(bCoefficients, "FIR filtering requires non-null bCoefficients");

    if (bCoefficients.length == 0) {
      throw new IllegalArgumentException("FIR filtering requires non-empty bCoefficients");
    }

    // Delay contains the relevant input samples in reverse order, i.e.
    // delay[0] = x[n-0], delay[1] = x[n-1], ..., delay[N-1] = x[n - (N-1)]
    // Therefore, the convolution becomes a pairwise multiply and add operation, i.e.
    // output[n] = sum{k=0 to N-1}[bCoefficients(k) * input(n-k)]
    //           = sum{k=0 to N-1}[bCoefficients(k) * delay(k)]
    double[] delay = new double[bCoefficients.length];
    Arrays.fill(delay, 0.0);

    double[] output = new double[input.length];
    for (int n = 0; n < input.length; ++n) {
      shiftRight(delay);
      delay[0] = input[n];
      output[n] = pairwiseMultiplyAndSum(bCoefficients, delay);
    }

    return output;
  }

  /**
   * Assuming a and b are of equal length and non-null, compute the sum of the pairwise
   * multiplication of each element from a and b, i.e.
   *  sum{n=0, N-1}[a(n) * b(n)]  where N = length of a and b
   *  @param a double array, not null
   *  @param b double array, not null
   *  @return double containing the sum of pairwise multiplying a and b
   */
  private static double pairwiseMultiplyAndSum(double[] a, double[] b) {
    double sum = 0.0;
    for (int k = 0; k < a.length; ++k) {
      sum += a[k] * b[k];
    }

    return sum;
  }

  /**
   * Assuming a is not null, shift all of the elements of a to the right by one element.  Does not
   * change a[0]
   * @param a array to shift, not null
   */
  private static void shiftRight(double[] a) {
    for (int i = a.length - 1; i > 0; i--) {
      a[i] = a[i - 1];
    }
  }
}
