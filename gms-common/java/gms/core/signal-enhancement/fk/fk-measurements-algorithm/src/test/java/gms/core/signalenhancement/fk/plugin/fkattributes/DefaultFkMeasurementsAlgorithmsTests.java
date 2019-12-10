package gms.core.signalenhancement.fk.plugin.fkattributes;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class DefaultFkMeasurementsAlgorithmsTests {
  private final double PRECISION = 0.001;
  private final double EXPECTED_SLOWNESS_UNCERTAINTY = 0.2900481473559255;
  private final double EXPECTED_FSTAT = 11.026701173043678;

  @Test
  public void testIndexOfFkMax() {
    // Test at (1, 2)
    double [][] testMap1 = {{0, 0, 0},
                            {0, 0, 0},
                            {0, 1, 0}};
    Pair<Double, Double> expected = Pair.of(1.0, 2.0);
    Pair<Double, Double> actual = DefaultFkMeasurementsAlgorithms.indexOfFkMax(testMap1);
    assertEquals(expected, actual);

    // Test at (0, 0)
    double [][] testMap2 = {{10, 2, 3},
                            {3, 2, 1},
                            {1, 0, 9}};
    Pair<Double, Double> expected2 = Pair.of(0.0, 0.0);
    Pair<Double, Double> actual2 = DefaultFkMeasurementsAlgorithms.indexOfFkMax(testMap2);
    assertEquals(expected2, actual2);

    // Test at (2, 2)
    double [][] testMap3 = {{0.001, 0.002, 0.003},
                            {0.001, 0.003, 0.003},
                            {0.001, 0.002, 0.0031}};
    Pair<Double, Double> expected3 = Pair.of(2.0, 2.0);
    Pair<Double, Double> actual3 = DefaultFkMeasurementsAlgorithms.indexOfFkMax(testMap3);
    assertEquals(expected3, actual3);
  }

  @Test
  public void testSlownessXComponent() {
    assertEquals(0.0, DefaultFkMeasurementsAlgorithms.slownessXComponent(-5, 1, 5), PRECISION);
    assertEquals(1, DefaultFkMeasurementsAlgorithms.slownessXComponent(0, 0.2, 5), PRECISION);
    assertEquals(3, DefaultFkMeasurementsAlgorithms.slownessXComponent(0, 0.1, 30), PRECISION);
  }

  @Test
  public void testSlownessYComponent() {
    assertEquals(0.0, DefaultFkMeasurementsAlgorithms.slownessYComponent(5, -1, 5), PRECISION);
    assertEquals(-1, DefaultFkMeasurementsAlgorithms.slownessYComponent(0, -0.2, 5), PRECISION);
    assertEquals(-4, DefaultFkMeasurementsAlgorithms.slownessYComponent(0, -0.1, 40), PRECISION);
  }

  @Test
  public void testSlownessOfIndex() {
    assertEquals(0.0, DefaultFkMeasurementsAlgorithms.slownessOfIndex(-5, 1, 5, -1, 5, 5), PRECISION);
    assertEquals(157.2533733278164, DefaultFkMeasurementsAlgorithms.slownessOfIndex(0, 0.2, 0, -0.2, 5, 5), PRECISION);
    assertEquals(555.9746332227937, DefaultFkMeasurementsAlgorithms.slownessOfIndex(0, 0.1, 0, -0.1, 30, 40), PRECISION);
  }

  @Test
  public void testAzimuthOfIndex() {
    assertEquals(0.0, DefaultFkMeasurementsAlgorithms.azimuthOfIndex(-5, 1, 5, -1, 5, 5), PRECISION);
    assertEquals(135.0, DefaultFkMeasurementsAlgorithms.azimuthOfIndex(0, 0.2, 0, -0.2, 5, 5), PRECISION);
    assertEquals(143.130, DefaultFkMeasurementsAlgorithms.azimuthOfIndex(0, 0.1, 0, -0.1, 30, 40), PRECISION);
  }

  @Test
  public void testSlownessUncertainty() {
    assertEquals(EXPECTED_SLOWNESS_UNCERTAINTY, DefaultFkMeasurementsAlgorithms.slownessUncertainty(10, 0, EXPECTED_FSTAT, 0.04), PRECISION);
  }

  @Test
  public void testAzimuthUncertainty() {
    assertEquals(1.9571319732727406, DefaultFkMeasurementsAlgorithms.azimuthUncertainty(8.491682159702084, EXPECTED_SLOWNESS_UNCERTAINTY), PRECISION);
  }
}
