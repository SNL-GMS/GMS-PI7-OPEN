package gms.shared.utilities.signalprocessing.normalization;

import static org.junit.Assert.assertArrayEquals;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DeMeanerTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private static double[] dataSet;
  private static double[] validationSet;

  @BeforeClass
  public static void setupClass() {
    dataSet = new double[100];
    validationSet = new double[100];

    double mean = 7.0;

    int offset = 1;
    boolean increment = true;
    for (int i = 0; i < dataSet.length / 2; i++) {
      dataSet[i] = offset + mean;
      dataSet[dataSet.length - 1 - i] = mean - offset;

      validationSet[i] = Math.abs(dataSet[i]) - mean;
      validationSet[validationSet.length - 1 - i] =
          Math.abs(dataSet[dataSet.length - 1 - i]) - mean;
      if (offset == 5) {
        offset--;
        increment = false;
      } else if (offset == 1) {
        offset++;
        increment = true;
      } else if (increment) {
        offset++;
      } else {
        offset--;
      }
    }
  }

  @Test
  public void testDemeanNullDataExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot demean data from a null dataset");
    DeMeaner.demean(null);
  }

  @Test
  public void testDemeanEmptyDataExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot demean data for an empty dataset");
    DeMeaner.demean(new double[]{});
  }

  @Test
  public void testDemean() {
    double[] demeanedData = DeMeaner.demean(dataSet);
    assertArrayEquals(validationSet, demeanedData, 0.0000001);
  }

}
