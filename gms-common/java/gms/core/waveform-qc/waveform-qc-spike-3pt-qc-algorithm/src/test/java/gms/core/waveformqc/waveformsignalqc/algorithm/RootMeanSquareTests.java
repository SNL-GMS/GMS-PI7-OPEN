package gms.core.waveformqc.waveformsignalqc.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import org.junit.jupiter.api.Test;

public class RootMeanSquareTests {

  @Test
  public void testRmsDc() {
    final double dcValue = 17.0;
    final double[] dcInput = new double[20];
    Arrays.fill(dcInput, dcValue);

    assertEquals(dcValue, RootMeanSquare.rms(dcInput), 1e-100);
  }

  @Test
  public void testRms() {
    final double[] input = DoubleStream.iterate(1.2, d -> d + 1.001).limit(15).toArray();
    final double expectedRms = Math.sqrt(Arrays.stream(input).map(d -> d * d).sum() / input.length);

    assertEquals(expectedRms, RootMeanSquare.rms(input), 1e-100);
  }

  @Test
  public void testRmsNegatives() throws Exception {
    final double[] input = DoubleStream.iterate(-1.2, d -> d - 1.001).limit(15).toArray();
    final double expectedRms = Math.sqrt(Arrays.stream(input).map(d -> d * d).sum() / input.length);

    assertEquals(expectedRms, RootMeanSquare.rms(input), 1e-100);
  }

  @Test
  public void testRmsNullInputExpectNullPointerException() {
    assertEquals("RootMeanSquare requires non-null input signal",
        assertThrows(NullPointerException.class, () -> RootMeanSquare.rms(null))
            .getMessage());
  }

  @Test
  public void testRmsEmptyInputExpectIllegalArgumentException() {
    assertEquals("RootMeanSquare requires non-empty input signal",
        assertThrows(IllegalArgumentException.class, () -> RootMeanSquare.rms(new double[]{}))
            .getMessage());
  }
}
