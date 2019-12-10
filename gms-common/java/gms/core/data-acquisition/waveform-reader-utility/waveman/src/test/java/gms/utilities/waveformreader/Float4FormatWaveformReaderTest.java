package gms.utilities.waveformreader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import org.junit.Test;

public class Float4FormatWaveformReaderTest {

  private final double[] expectedSamples = new double[]{
      -1.752464684, 0.120620452, 2.759449316, 1.704584928, 2.710169428,
      1.1074317, -3.961746845, -0.876007436, 0.616515837, 0.136018155};

  @Test
  public void testReadTestData() throws Exception {
    // Get an InputStream for the test file.
    InputStream is = this.getClass().getResourceAsStream("/css/WFS4/f4.w");
    assertNotNull("Could not find test data file", is);
    double[] samples = new Float4FormatWaveformReader().read(
        is, expectedSamples.length, 0);
    assertArrayEquals(expectedSamples, samples, 1e-3);
  }

}
