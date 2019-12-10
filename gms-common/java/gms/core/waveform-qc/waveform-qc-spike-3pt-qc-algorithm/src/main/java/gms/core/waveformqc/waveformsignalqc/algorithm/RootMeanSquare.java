package gms.core.waveformqc.waveformsignalqc.algorithm;

import java.util.Arrays;
import java.util.Objects;

/**
 * Utility computing a waveform's Root-Mean-Square.
 *    x_rms = sqrt([1/n] * [x_1^2 + x_2^2 + ... + x_n^2])
 */
public class RootMeanSquare {

  /**
   * Computes RMS ( sqrt([1/n] * [x_1^2 + x_2^2 + ... + x_n^2]) ) of the input values.
   * The input must have at least one data value.
   * @param input data samples, not empty, not null
   * @return RMS of the input data samples
   * @throws NullPointerException if input is null
   * @throws IllegalArgumentException if input is empty
   */
  public static double rms(double[] input) {
    Objects.requireNonNull(input, "RootMeanSquare requires non-null input signal");

    if(input.length == 0) {
      throw new IllegalArgumentException("RootMeanSquare requires non-empty input signal");
    }

    return Math.sqrt(Arrays.stream(input).map(d -> d* d).sum() / input.length);
  }
}
