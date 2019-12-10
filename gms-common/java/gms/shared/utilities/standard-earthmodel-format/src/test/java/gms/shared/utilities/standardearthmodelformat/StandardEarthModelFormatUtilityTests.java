package gms.shared.utilities.standardearthmodelformat;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

public class StandardEarthModelFormatUtilityTests {

  @Test
  public void testParseSampleString() throws IOException {
    double[] expectedDistances = new double[] {
        0.0, 0.2, 0.5, 1.0, 2.0, 3.0, 4.0, 6.0, 8.0, 10.0, 15.0, 20.0, 30.0
    };

    double[] expectedValues = new double[] {
        0.2, 0.5, 1.3, 1.7, 2.4, 2.5, 2.6, 2.8, 2.9, 3.1,  3.3,  3.4,  3.5
    };

    BufferedReader reader = new BufferedReader(
        new StringReader(
             "# Distance-dependent modelling error(s)\n"
            + "  13    1\n"
            + "        0.0     0.2     0.5     1.0\t2.0     3.0     4.0     6.0\n"
            + "        8.0     10.0    15.0\t20.0    30.0\n"
            + "#\n"
            + "        0.2\n"
            + "        0.5\n"
            + "        1.3\n"
            + "        1.7\n"
            + "        2.4\n"
            + "        2.5\n"
            + "        2.6\n"
            + "        2.8\n"
            + "        2.9\n"
            + "        3.1\n"
            + "        3.3\n"
            + "        3.4\n"
            + "        3.5"));

    Triple<double[], double[], double[][]> table = StandardEarthModelFormatUtility
        .retrieveErrorsUncertainties(reader);

    assertNotNull(table);
    assertNotNull(table.getLeft());
    assertArrayEquals(expectedDistances, table.getLeft(), 1E-15);
    assertArrayEquals(expectedValues, table.getRight()[0], 1E-15);
    assertNull(table.getMiddle());

  }

  @Test
  public void testInterpolation() {
    double[] distances = new double[] {
        0.0, 0.1, 0.2, 0.6, 1.0, 1.5, 1.7, 1.9, 2.0, 2.5,  3.0,  4.0, 4.5
    };

    double[] expectedPointValues = new double[distances.length];

    for (int i = 0; i < distances.length; i++) {
      expectedPointValues[i] = sigmoid(distances[i]);
    }

    for (int i = 0; i < distances.length; i++) {
      assertEquals(
        StandardEarthModelFormatUtility
            .interpolateUncertainties(distances[i], distances, expectedPointValues),
          expectedPointValues[i], 1E-15);
    }

    for (int i = 1; i < distances.length; i++) {
      double distance = distances[i-1] + (distances[i] - distances[i-1]) / 2.0;
      assertEquals(StandardEarthModelFormatUtility
              .interpolateUncertainties(distance, distances, expectedPointValues),
          sigmoid(distance), 1E-3);
    }
  }

  private double sigmoid(double x) {
    return 1.0/(1.0 + Math.exp(-x));
  }
}
