package gms.shared.utilities.geomath;

import static gms.shared.utilities.geomath.TestUtil.matchingIndices;
import static gms.shared.utilities.geomath.TestUtil.nonMatchingValues;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.function.IntPredicate;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.function.Abs;
import org.apache.commons.math3.analysis.function.Acos;
import org.apache.commons.math3.analysis.function.Asin;
import org.apache.commons.math3.analysis.function.Atan;
import org.apache.commons.math3.analysis.function.Cbrt;
import org.apache.commons.math3.analysis.function.Ceil;
import org.apache.commons.math3.analysis.function.Cos;
import org.apache.commons.math3.analysis.function.Cosh;
import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.analysis.function.Expm1;
import org.apache.commons.math3.analysis.function.Floor;
import org.apache.commons.math3.analysis.function.Inverse;
import org.apache.commons.math3.analysis.function.Log;
import org.apache.commons.math3.analysis.function.Log10;
import org.apache.commons.math3.analysis.function.Log1p;
import org.apache.commons.math3.analysis.function.Power;
import org.apache.commons.math3.analysis.function.Rint;
import org.apache.commons.math3.analysis.function.Signum;
import org.apache.commons.math3.analysis.function.Sin;
import org.apache.commons.math3.analysis.function.Sinh;
import org.apache.commons.math3.analysis.function.Sqrt;
import org.apache.commons.math3.analysis.function.Tan;
import org.apache.commons.math3.analysis.function.Tanh;
import org.apache.commons.math3.analysis.function.Ulp;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.RealVectorPreservingVisitor;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * This was copied from the Apache Commons Math3 code base, modified to specifically test
 * FilteredRealVector, and changed from junit5 conventions to junit5.
 */
public class FilteredRealVectorTest {

  protected enum BinaryOperation {
    ADD, SUB, MUL, DIV
  }

  /**
   * <p>
   * This is an attempt at covering most particular cases of combining two
   * values. Here {@code x} is the value returned by
   * {@link #getPreferredEntryValue()}, while {@code y} and {@code z} are two
   * "normal" values.
   * </p>
   * <ol>
   *   <li>
   *     Addition: the following cases should be covered
   *     <ul>
   *       <li>{@code (2 * x) + (-x)}</li>
   *       <li>{@code (-x) + 2 * x}</li>
   *       <li>{@code x + y}</li>
   *       <li>{@code y + x}</li>
   *       <li>{@code y + z}</li>
   *       <li>{@code y + (x - y)}</li>
   *       <li>{@code (y - x) + x}</li>
   *     </ul>
   *     The values to be considered are:
   *     {@code x, y, z, 2 * x, -x, x - y, y - x}.
   *   </li>
   *   <li>
   *     Subtraction: the following cases should be covered
   *     <ul>
   *       <li>{@code (2 * x) - x}</li>
   *       <li>{@code x - y}</li>
   *       <li>{@code y - x}</li>
   *       <li>{@code y - z}</li>
   *       <li>{@code y - (y - x)}</li>
   *       <li>{@code (y + x) - y}</li>
   *     </ul>
   *     The values to be considered are: {@code x, y, z, x + y, y - x}.
   *   </li>
   *   <li>
   *     Multiplication
   *     <ul>
   *       <li>{@code (x * x) * (1 / x)}</li>
   *       <li>{@code (1 / x) * (x * x)}</li>
   *       <li>{@code x * y}</li>
   *       <li>{@code y * x}</li>
   *       <li>{@code y * z}</li>
   *     </ul>
   *     The values to be considered are: {@code x, y, z, 1 / x, x * x}.
   *   </li>
   *   <li>
   *     Division
   *     <ul>
   *       <li>{@code (x * x) / x}</li>
   *       <li>{@code x / y}</li>
   *       <li>{@code y / x}</li>
   *       <li>{@code y / z}</li>
   *     </ul>
   *     The values to be considered are: {@code x, y, z, x * x}.
   *   </li>
   * </ol>
   * Also to be considered {@code NaN}, {@code POSITIVE_INFINITY},
   * {@code NEGATIVE_INFINITY}, {@code +0.0}, {@code -0.0}.
   */
  private final double[] values;

  /**
   * Creates a new instance of {@link RealVector}, with specified entries.
   * The returned vector must be of the type currently tested. It should be
   * noted that some tests assume that no references to the specified
   * {@code double[]} are kept in the returned object: if necessary, defensive
   * copy of this array should be made.
   *
   * @param data the entries of the vector to be created
   * @return a new {@link RealVector} of the type to be tested
   */
  public RealVector create(double[] data, int... excluded) {
    BitSet exclusionBits = new BitSet(data.length);
    for (int n: excluded) {
      exclusionBits.set(n);
    }
    return new FilteredRealVector(
        MatrixUtils.createRealVector(data), exclusionBits);
  }

  /**
   * Creates a new instance of {@link RealVector}, with specified entries.
   * The type of the returned vector must be different from the type currently
   * tested. It should be noted that some tests assume that no references to
   * the specified {@code double[]} are kept in the returned object: if
   * necessary, defensive copy of this array should be made.
   *
   * @param data the entries of the vector to be created
   * @return a new {@link RealVector} of an alien type
   */
  public RealVector createAlien(double[] data){
    return new RealVectorTestImpl(data);
  }

  /**
   * Returns a preferred value of the entries, to be tested specifically. Some
   * implementations of {@link RealVector} (e.g. {@link OpenMapRealVector}) do
   * not store specific values of entries. In order to ensure that all tests
   * take into account this specific value, some entries of the vectors to be
   * tested are deliberately set to the value returned by the present method.
   * The default implementation returns {@code 0.0}.
   *
   * @return a value which <em>should</em> be present in all vectors to be
   * tested
   */
  public double getPreferredEntryValue() {
    return 0.0;
  }

  public FilteredRealVectorTest() {
    /*
     * Make sure that x, y, z are three different values. Also, x is the
     * preferred value (e.g. the value which is not stored in sparse
     * implementations).
     */
    final double x = getPreferredEntryValue();
    final double y = x + 1d;
    final double z = y + 1d;

    values =
        new double[] {
            Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
            0d, -0d, x, y, z, 2 * x, -x, 1 / x, x * x, x + y, x - y, y - x
        };
  }

  @Test
  public void testStaticFilter() {

    double[] values = new double[] {
        0.0,
        1.0,
        Double.NEGATIVE_INFINITY,
        5.0,
        Double.NaN,
        2.0,
        6.0,
        Double.NaN,
        Double.POSITIVE_INFINITY,
        3.0
    };

    final RealVector innerVector = MatrixUtils.createRealVector(values);

    RealVector rv = FilteredRealVector.filter(innerVector, n -> {
      double v = innerVector.getEntry(n);
      return Double.isNaN(v) || Double.isInfinite(v);
    });

    Assertions.assertFalse(rv.isNaN());
    Assertions.assertFalse(rv.isInfinite());

    int n = 0;
    for (int i=0; i<values.length; i++) {
      double d= values[i];
      if (!(Double.isNaN(d) || Double.isInfinite(d))) {
        Assertions.assertEquals(d, rv.getEntry(n));
        n++;
      }
    }

    Assertions.assertEquals(n, rv.getDimension());
  }

  @Test
  public void testStaticFilterValues() {

    double[] values = new double[] {
        0.0,
        1.0,
        Double.NEGATIVE_INFINITY,
        5.0,
        Double.NaN,
        2.0,
        6.0,
        Double.NaN,
        Double.POSITIVE_INFINITY,
        3.0
    };

    final RealVector innerVector = MatrixUtils.createRealVector(values);

    RealVector rv = FilteredRealVector.filterValues(innerVector, v -> {
      return Double.isNaN(v) || Double.isInfinite(v);
    });

    Assertions.assertFalse(rv.isNaN());
    Assertions.assertFalse(rv.isInfinite());

    int n = 0;
    for (int i=0; i<values.length; i++) {
      double d= values[i];
      if (!(Double.isNaN(d) || Double.isInfinite(d))) {
        Assertions.assertEquals(d, rv.getEntry(n));
        n++;
      }
    }

    Assertions.assertEquals(n, rv.getDimension());
  }

  @Test
  public void testStaticInclude() {

    double[] values = new double[] {
        0.0,
        1.0,
        3.0,
        5.0,
        8.0,
        2.0,
        6.0,
        1.0,
        4.0,
        3.0
    };

    final RealVector innerVector = MatrixUtils.createRealVector(values);

    RealVector rv = FilteredRealVector.include(innerVector, n -> {
      double v = innerVector.getEntry(n);
      return v > 3.0;
    });

    int n = 0;
    for (int i=0; i<values.length; i++) {
      double d= values[i];
      if (d > 3.0) {
        Assertions.assertEquals(d, rv.getEntry(n));
        n++;
      }
    }

    Assertions.assertEquals(n, rv.getDimension());
  }

  @Test
  public void testStaticIncludeValues() {

    double[] values = new double[] {
        0.0,
        1.0,
        Double.NEGATIVE_INFINITY,
        5.0,
        Double.NaN,
        2.0,
        6.0,
        Double.NaN,
        Double.POSITIVE_INFINITY,
        3.0
    };

    final RealVector innerVector = MatrixUtils.createRealVector(values);

    RealVector rv = FilteredRealVector.includeValues(innerVector, v -> {
      return Double.isNaN(v) || Double.isInfinite(v);
    });

    Assertions.assertTrue(rv.isNaN());

    int n = 0;
    for (int i=0; i<values.length; i++) {
      double d= values[i];
      if ((Double.isNaN(d) || Double.isInfinite(d))) {
        Assertions.assertEquals(Double.doubleToLongBits(d),
            Double.doubleToLongBits(rv.getEntry(n)));
        n++;
      }
    }

    Assertions.assertEquals(n, rv.getDimension());
  }

  @Test
  public void testFilter() {

    final double[] values = new double[] {
        1.0,
        Double.NaN,
        2.0,
        Double.POSITIVE_INFINITY,
        1.0,
        Double.NaN,
        Double.POSITIVE_INFINITY,
        3.0,
        Double.NEGATIVE_INFINITY,
        10.0
    };

    FilteredRealVector filteredRealVector = FilteredRealVector.filter(
        MatrixUtils.createRealVector(values), n -> {
          return Double.isNaN(values[n]);
        }
    );

    Assertions.assertFalse(filteredRealVector.isNaN());
    Assertions.assertTrue(filteredRealVector.isInfinite());

    List<Integer> intList = new ArrayList<>();
    for (int i=0; i<filteredRealVector.getDimension(); i++) {
      if (Double.isInfinite(filteredRealVector.getEntry(i))) {
        intList.add(i);
      }
    }

    filteredRealVector = filteredRealVector.filter(
        intList.stream().mapToInt(Integer::intValue).toArray());

    Assertions.assertEquals(5, filteredRealVector.getDimension());
    Assertions.assertFalse(filteredRealVector.isNaN());
    Assertions.assertFalse(filteredRealVector.isInfinite());
  }

  @Test
  public void testFilterValues() {
    double[] data = new double[] {
      1.0, 2.2, Double.NaN, 0.0, Double.POSITIVE_INFINITY, 3.4, 4.0
    };
    RealVector innerVector = MatrixUtils.createRealVector(data);
    // Filter out NaNs and Infs
    RealVector realVector = FilteredRealVector.filterValues(innerVector,
        v -> Double.isNaN(v) || Double.isInfinite(v));
    Assertions.assertFalse(realVector.isNaN());
    Assertions.assertFalse(realVector.isInfinite());
    // Filter out ones that don't have fractional parts.
    realVector = FilteredRealVector.filterValues(realVector, v -> v == Math.floor(v));
    Assertions.assertEquals(2, realVector.getDimension());
    Assertions.assertEquals(2.2, realVector.getEntry(0));
    Assertions.assertEquals(3.4, realVector.getEntry(1));
  }

  @Test
  public void testIncludeValues() {
    double[] data = new double[] {
        1.0, 2.2, Double.NaN, 0.0, Double.POSITIVE_INFINITY, 3.4, 4.0
    };
    RealVector innerVector = MatrixUtils.createRealVector(data);
    // Include only NaNs and Infs
    RealVector realVector = FilteredRealVector.includeValues(innerVector,
        v -> Double.isNaN(v) || Double.isInfinite(v));
    Assertions.assertTrue(realVector.isNaN());
    Assertions.assertEquals(2, realVector.getDimension());
    Assertions.assertTrue(Double.isInfinite(realVector.getEntry(1)));

    // Include only the NaN
    realVector = FilteredRealVector.includeValues(realVector, v -> Double.isNaN(v));
    Assertions.assertEquals(1, realVector.getDimension());
    Assertions.assertEquals(Double.NaN, realVector.getEntry(0));
  }

  @Test
  public void testConstructor() {
    double[] data = new double[10];
    // Make the bitset longer than data to see what happens.
    BitSet exclusionBits = new BitSet(16);
    // Should only result in the last 2 items being excluded.
    exclusionBits.set(8, 16);

    FilteredRealVector frv = new FilteredRealVector(MatrixUtils.createRealVector(data),
        exclusionBits);

    Assertions.assertEquals(8, frv.getDimension());
  }

  @Test
  public void testFiltersSameDimensions() {

    IntPredicate predicate1 = n -> n%2 == 0;
    IntPredicate predicate2 = n -> n%2 == 1;

    RealVector innerVec1 = MatrixUtils.createRealVector(new double[10]);
    RealVector innerVec2 = MatrixUtils.createRealVector(new double[10]);

    FilteredRealVector vector1 = FilteredRealVector.include(
        innerVec1, n -> true);
    FilteredRealVector vector2 = FilteredRealVector.include(
        innerVec1, n -> true);

    Assertions.assertTrue(FilteredRealVector.filtersSameDimensions(
        vector1, vector2
    ));

    vector1 = FilteredRealVector.include(
        innerVec1, predicate1);
    vector2 = FilteredRealVector.include(
        innerVec1, predicate2);

    Assertions.assertFalse(FilteredRealVector.filtersSameDimensions(
        vector1, vector2
    ));

    vector2 = FilteredRealVector.include(MatrixUtils.createRealVector(new double[11]),
        n -> true);

    final FilteredRealVector vec1 = vector1;
    final FilteredRealVector vec2 = vector2;

    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      Assertions.assertFalse(FilteredRealVector.filtersSameDimensions(
          vec1, vec2
      ));
    });
  }

  @Test
  public void testConstructorWithNoExclusionBits() {
    RealVector rv = new FilteredRealVector(MatrixUtils.createRealVector(new double[10]));
    Assertions.assertEquals(10, rv.getDimension());
  }

  @Test
  public void testExcludeSameDimensions() {
    double[] data = new double[] { 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0 };
    RealVector innerVector = MatrixUtils.createRealVector(data);

    RealVector rv1 = new FilteredRealVector(innerVector);
    Assertions.assertEquals(innerVector.getDimension(), rv1.getDimension());

    FilteredRealVector evenElementsOnly = FilteredRealVector.include(innerVector, n -> n%2 == 0);

    for (int i=0; i<evenElementsOnly.getDimension(); i++) {
      Assertions.assertEquals(data[2*i], evenElementsOnly.getEntry(i));
    }

    Assertions.assertEquals(data.length/2, evenElementsOnly.getDimension());

    RealVector rv2 = ((FilteredRealVector) rv1).excludeSameDimensions(evenElementsOnly);

    Assertions.assertNotSame(rv1, rv2);

    Assertions.assertEquals(rv2.getDimension(), evenElementsOnly.getDimension());
    Assertions.assertEquals(rv2, evenElementsOnly);
  }

  @Test
  public void testExcludeSameDimensionsAsMatrix() {
    double[] data = new double[] {
        0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0
    };

    RealVector innerVector = MatrixUtils.createRealVector(data);

    // Includes rows not evenly divisible by 3.
    RowFilteredRealMatrix matrix = RowFilteredRealMatrix.includeRows(
        MatrixUtils.createRealMatrix(data.length, 5), row -> row%3 != 0);

    FilteredRealVector rv1 = new FilteredRealVector(innerVector);
    Assertions.assertEquals(innerVector.getDimension(), rv1.getDimension());

    rv1 = rv1.excludeSameDimensions(matrix);

    Assertions.assertEquals(rv1.getDimension(), matrix.getRowDimension());
    Assertions.assertSame(innerVector, rv1.getInnerVector());

    for (int i=0; i<data.length; i++) {
      Assertions.assertEquals(i%3 != 0, rv1.isIncluded(i));
    }

  }

  @Test
  public void testGetInnerVector() {
    RealVector innerVector = MatrixUtils.createRealVector(new double[20]);
    RealVector realVector = FilteredRealVector.filter(innerVector,
        n -> n%2 == 1);

    Assertions.assertEquals(innerVector.getDimension()/2, realVector.getDimension());
    Assertions.assertSame(innerVector, ((FilteredRealVector) realVector).getInnerVector());
  }

  @Test
  public void testGetInnerDimension() {
    RealVector innerVector = MatrixUtils.createRealVector(new double[10]);
    RealVector realVector = FilteredRealVector.filter(innerVector,
        n -> n%2 == 0);
    Assertions.assertEquals(innerVector.getDimension()/2, realVector.getDimension());
    Assertions.assertEquals(innerVector.getDimension(),
        ((FilteredRealVector) realVector).getInnerVectorDimension());
  }

  @Test
  public void testIsIncluded() {
    double[] data = new double[] {
      1.0, 3.0, 4.2, 5.1, 2.2, 3.0, 1.0, 0.0, 3.1
    };
    RealVector innerVector = MatrixUtils.createRealVector(data);
    RealVector realVector = FilteredRealVector.includeValues(innerVector,
        v -> v > 2.5);
    for (int i=0; i<data.length; i++) {
      Assertions.assertEquals(data[i] > 2.5,
          ((FilteredRealVector) realVector).isIncluded(i));
    }
  }

  @Test
  public void testAdd() {

    double[] data = new double[] {
        1.0,
        Double.NaN,
        2.0,
        Double.NaN,
        3.0,
        4.0,
        5.0
    };

    RealVector innerVector = MatrixUtils.createRealVector(data);

    // Will filter all the NaNs.
    FilteredRealVector filtered1 = FilteredRealVector.filterValues(innerVector,
        v -> Double.isNaN(v));

    // Create an ArrayVector of the same dimension as filtered1 with all 1s.
    double[] ones = new double[filtered1.getDimension()];
    Arrays.fill(ones, 1.0);

    FilteredRealVector filtered2 = (FilteredRealVector) filtered1.add(
        MatrixUtils.createRealVector(ones)
    );

    Assertions.assertEquals(filtered1.getInnerVectorDimension(),
        filtered2.getInnerVectorDimension());

    int n = 0;
    for (int d = 0; d<filtered1.getInnerVectorDimension(); d++) {
      Assertions.assertEquals(filtered1.isIncluded(d), filtered2.isIncluded(d));
      if (filtered2.isIncluded(d)) {
        Assertions.assertEquals(data[d], filtered1.getEntry(n));
        Assertions.assertEquals(data[d] + 1.0, filtered2.getEntry(n));
        n++;
      }
    }
  }

  @Test
  public void testSubtract() {

    double[] data = new double[] {
        1.0,
        Double.NaN,
        2.0,
        Double.NaN,
        3.0,
        4.0,
        5.0
    };

    RealVector innerVector = MatrixUtils.createRealVector(data);

    // Will filter all the NaNs.
    FilteredRealVector filtered1 = FilteredRealVector.filterValues(innerVector,
        v -> Double.isNaN(v));

    // Create an ArrayVector of the same dimension as filtered1 with all 1s.
    double[] ones = new double[filtered1.getDimension()];
    Arrays.fill(ones, 1.0);

    FilteredRealVector filtered2 = (FilteredRealVector) filtered1.subtract(
        MatrixUtils.createRealVector(ones)
    );

    Assertions.assertEquals(filtered1.getInnerVectorDimension(),
        filtered2.getInnerVectorDimension());

    int n = 0;
    for (int d = 0; d<filtered1.getInnerVectorDimension(); d++) {
      Assertions.assertEquals(filtered1.isIncluded(d), filtered2.isIncluded(d));
      if (filtered2.isIncluded(d)) {
        Assertions.assertEquals(data[d], filtered1.getEntry(n));
        Assertions.assertEquals(data[d] - 1.0, filtered2.getEntry(n));
        n++;
      }
    }
  }

  @Test
  public void testGetDimension() {
    final double x = getPreferredEntryValue();
    final double[] data1 = {x, x, x, x};
    Assertions.assertEquals(data1.length - 2, create(data1, 1, 3).getDimension());
    final double y = x + 1;
    final double[] data2 = {y, y, y, y};
    Assertions.assertEquals(data2.length, create(data2).getDimension());
    Assertions.assertEquals(data2.length - 1, create(data2, 0).getDimension());
  }

  @Test
  public void testGetEntry() {
    final double x = getPreferredEntryValue();
    final double[] data = {x, 1d, 2d, x, x};
    final RealVector v = create(data);
    for (int i = 0; i < data.length; i++) {
      Assertions.assertEquals(data[i], v.getEntry(i),"entry " + i);
    }
  }

  @Test
  public void testGetEntryInvalidIndex1() {
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      create(new double[4]).getEntry(-1);
    });
  }

  @Test
  public void testGetEntryInvalidIndex2() {
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      create(new double[4], 3).getEntry(3);
    });
  }

  @Test
  public void testSetEntry() {
    final double x = getPreferredEntryValue();
    final double[] data = {x, 1d, 2d, x, x};
    final double[] expected = MathArrays.copyOf(data);
    final RealVector actual = create(data);

    /*
     * Try setting to any value.
     */
    for (int i = 0; i < data.length; i++) {
      final double oldValue = data[i];
      final double newValue = oldValue + 1d;
      expected[i] = newValue;
      actual.setEntry(i, newValue);
      Assertions.assertArrayEquals(expected, actual.toArray(), "while setting entry #" + i);
      expected[i] = oldValue;
      actual.setEntry(i, oldValue);
    }

    /*
     * Try setting to the preferred value.
     */
    for (int i = 0; i < data.length; i++) {
      final double oldValue = data[i];
      final double newValue = x;
      expected[i] = newValue;
      actual.setEntry(i, newValue);
      Assertions.assertArrayEquals(expected, actual.toArray(), "while setting entry #" + i);
      expected[i] = oldValue;
      actual.setEntry(i, oldValue);
    }
  }

  @Test
  public void testSetEntryInvalidIndex1() {
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      create(new double[4]).setEntry(-1, getPreferredEntryValue());
    });
  }

  @Test
  public void testSetEntryInvalidIndex2() {
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      create(new double[4], 3).setEntry(3, getPreferredEntryValue());
    });
  }

  @Test
  public void testAddToEntry() {

    final double x = getPreferredEntryValue();
    final double[] data1 = {x, 1d, 2d, x, x};

    final double[] expected = MathArrays.copyOf(data1);
    final RealVector actual = create(data1);

    /*
     * Try adding any value.
     */
    double increment = 1d;
    for (int i = 0; i < data1.length; i++) {
      final double oldValue = data1[i];
      expected[i] += increment;
      actual.addToEntry(i, increment);
      Assertions.assertArrayEquals(expected, actual.toArray(),
          "while incrementing entry #" + i);
      expected[i] = oldValue;
      actual.setEntry(i, oldValue);
    }

    /*
     * Try incrementing so that result is equal to preferred value.
     */
    for (int i = 0; i < data1.length; i++) {
      final double oldValue = data1[i];
      increment = x - oldValue;
      expected[i] = x;
      actual.addToEntry(i, increment);
      Assertions.assertArrayEquals(expected, actual.toArray(),
          "while incrementing entry #" + i);
      expected[i] = oldValue;
      actual.setEntry(i, oldValue);
    }
  }

  @Test
  public void testAddToEntryInvalidIndex1() {
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      create(new double[3]).addToEntry(-1, getPreferredEntryValue());
    });
  }

  @Test
  public void testAddToEntryInvalidIndex2() {
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      create(new double[3], 1).addToEntry(3, getPreferredEntryValue());
    });
  }

  private void doTestAppendVector(final String message, final RealVector v1,
      final RealVector v2, final double delta) {

    final int n1 = v1.getDimension();
    final int n2 = v2.getDimension();
    final RealVector v = v1.append(v2);
    Assertions.assertEquals(n1 + n2, v.getDimension(), message);

    for (int i = 0; i < n1; i++) {
      final String msg = message + ", entry #" + i;
      if (delta > 0.0) {
        Assertions.assertEquals(v1.getEntry(i), v.getEntry(i), delta, message);
      } else {
        Assertions.assertEquals(v1.getEntry(i), v.getEntry(i), message);
      }
    }
    for (int i = 0; i < n2; i++) {
      final String msg = message + ", entry #" + (n1 + i);
      if (delta > 0.0) {
        Assertions.assertEquals(v2.getEntry(i), v.getEntry(n1 + i), delta, message);
      } else {
        Assertions.assertEquals(v2.getEntry(i), v.getEntry(n1 + i), message);
      }
    }
  }

  @Test
  public void testAppendVector() {

    final double x = getPreferredEntryValue();
    final double[] data1 =  {x, 1d, 2d, x, x};
    final double[] data2 =  {x, x, 3d, x, 4d, x};

    doTestAppendVector("same type", create(data1), create(data2), 0d);
    doTestAppendVector("mixed types", create(data1), createAlien(data2), 0d);

    RealVector rv1 = create(data1, matchingIndices(data1, x));
    RealVector rv2 = create(data2, matchingIndices(data2, x));

    Assertions.assertEquals(2, rv1.getDimension());
    Assertions.assertEquals(2, rv2.getDimension());

    doTestAppendVector("filtered of x", rv1, rv2, 0d);
  }

  private void doTestAppendScalar(final String message, final RealVector v,
      final double d, final double delta) {

    final int n = v.getDimension();
    final RealVector w = v.append(d);
    Assertions.assertEquals(n + 1, w.getDimension(), message);
    for (int i = 0; i < n; i++) {
      final String msg = message + ", entry #" + i;
      if (delta > 0.0) {
        Assertions.assertEquals(v.getEntry(i), w.getEntry(i), delta, msg);
      } else {
        Assertions.assertEquals(v.getEntry(i), w.getEntry(i), msg);
      }
    }
    final String msg = message + ", entry #" + n;
    if (delta > 0.0) {
      Assertions.assertEquals(d, w.getEntry(n), delta, msg);
    } else {
      Assertions.assertEquals(d, w.getEntry(n), msg);
    }
  }

  @Test
  public void testAppendScalar() {
    final double x = getPreferredEntryValue();
    final double[] data = new double[] {x, 1d, 2d, x, x};

    doTestAppendScalar("", create(data), 1d, 0d);
    doTestAppendScalar("", create(data, matchingIndices(data, x)), x, 0d);
  }

  @Test
  public void testGetSubVector() {
    final double x = getPreferredEntryValue();
    final double[] data = {x, x, x, 1d, x, 2d, x, x, 3d, x, x, x, 4d, x, x, x};
    final int index = 1;
    final int n = data.length - 5;
    final RealVector actual = create(data).getSubVector(index, n);
    final double[] expected = new double[n];
    System.arraycopy(data, index, expected, 0, n);
    Assertions.assertArrayEquals(expected, actual.toArray());

    int[] xindices = matchingIndices(data, x);
    RealVector rv = create(data, xindices);
    Assertions.assertEquals(data.length - xindices.length, rv.getDimension());

    RealVector sv = rv.getSubVector(1, 2);

    Assertions.assertEquals(2, sv.getDimension());
    Assertions.assertEquals(2d, sv.getEntry(0));
    Assertions.assertEquals(3d, sv.getEntry(1));
  }

  @Test
  public void testGetSubVectorInvalidIndex1() {
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      final int n = 10;
      create(new double[n], n-2, n-1).getSubVector(-1, 2);
    });
  }

  @Test
  public void testGetSubVectorInvalidIndex2() {
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      final int n = 10;
      create(new double[n], n-2, n-1).getSubVector(n-2, 2);
    });
  }

  @Test
  public void testGetSubVectorInvalidIndex3() {
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      final int n = 10;
      create(new double[n]).getSubVector(0, n + 1);
    });
  }

  @Test
  public void testGetSubVectorInvalidIndex4() {
    Assertions.assertThrows(NotPositiveException.class, () -> {
      final int n = 10;
      create(new double[n]).getSubVector(3, -2);
    });
  }

  @Test
  public void testSetSubVectorSameType() {
    final double x = getPreferredEntryValue();
    final double[] expected = {x, x, x, 1d, x, 2d, x, x, 3d, x, x, x, 4d, x, x, x};
    final double[] sub = {5d, x, 6d, 7d, 8d};
    final RealVector actual = create(expected);
    final int index = 2;
    actual.setSubVector(index, create(sub));

    for (int i = 0; i < sub.length; i++){
      expected[index + i] = sub[i];
    }

    Assertions.assertArrayEquals(expected, actual.toArray());

    final RealVector rv = create(expected, matchingIndices(expected, x));
    final double[] expected2 = nonMatchingValues(expected, x);

    int start = 1;
    int n = rv.getDimension() - 2;
    double[] sub2 = new double[n];
    Arrays.fill(sub2, x);

    rv.setSubVector(start, create(sub2));
    for (int i=start; i<start+n; i++) {
      expected2[i] = x;
    }

    Assertions.assertArrayEquals(expected2, rv.toArray());
  }

  @Test
  public void testSetSubVectorMixedType() {
    final double x = getPreferredEntryValue();
    final double[] expected = {x, x, x, 1d, x, 2d, x, x, 3d, x, x, x, 4d, x, x, x};
    final double[] sub = {5d, x, 6d, 7d, 8d};
    final RealVector actual = create(expected);
    final int index = 2;
    actual.setSubVector(index, createAlien(sub));

    for (int i = 0; i < sub.length; i++){
      expected[index + i] = sub[i];
    }
    Assertions.assertArrayEquals(expected, actual.toArray());
  }

  @Test
  public void testSetSubVectorInvalidIndex1() {
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      create(new double[10]).setSubVector(-1, create(new double[2]));
    });
  }

  @Test
  public void testSetSubVectorInvalidIndex2() {
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      int n = 10;
      create(new double[n], n-1).setSubVector(n-1, create(new double[2]));
    });
  }

  @Test
  public void testSetSubVectorInvalidIndex3() {
    Assertions.assertThrows(OutOfRangeException.class, () -> {
      int n = 10;
      create(new double[n], n-1).setSubVector(n-2, create(new double[2]));
    });
  }

  @Test
  public void testIsNaN() {
    double[] values = new double[] {0, Double.NaN, 1, 2, Double.NaN};
    final RealVector v = create(values, matchingIndices(values, Double.NaN));

    Assertions.assertFalse(v.isNaN());
    v.setEntry(1, Double.NaN);
    Assertions.assertTrue(v.isNaN());
  }

  @Test
  public void testIsInfinite() {

    double[] values = new double[] {
        0,
        Double.POSITIVE_INFINITY,
        1,
        Double.POSITIVE_INFINITY,
        2
    };

    final RealVector v = create(values, matchingIndices(values, Double.POSITIVE_INFINITY));

    Assertions.assertFalse(v.isInfinite());
    v.setEntry(0, Double.NEGATIVE_INFINITY);
    Assertions.assertTrue(v.isInfinite());
    v.setEntry(1, Double.NaN);
    Assertions.assertFalse(v.isInfinite());
  }

  protected void doTestEbeBinaryOperation(final BinaryOperation op, final boolean mixed, boolean ignoreSpecial) {
    final double[] data1 = new double[values.length * values.length];
    final double[] data2 = new double[values.length * values.length];
    int k = 0;
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values.length; j++) {
        data1[k] = values[i];
        data2[k] = values[j];
        ++k;
      }
    }
    final RealVector v1 = create(data1);
    final RealVector v2 = mixed ? createAlien(data2) : create(data2);
    final RealVector actual;
    switch (op) {
      case ADD:
        actual = v1.add(v2);
        break;
      case SUB:
        actual = v1.subtract(v2);
        break;
      case MUL:
        actual = v1.ebeMultiply(v2);
        break;
      case DIV:
        actual = v1.ebeDivide(v2);
        break;
      default:
        throw new AssertionError("unexpected value");
    }
    final double[] expected = new double[data1.length];
    for (int i = 0; i < expected.length; i++) {
      switch (op) {
        case ADD:
          expected[i] = data1[i] + data2[i];
          break;
        case SUB:
          expected[i] = data1[i] - data2[i];
          break;
        case MUL:
          expected[i] = data1[i] * data2[i];
          break;
        case DIV:
          expected[i] = data1[i] / data2[i];
          break;
        default:
          throw new AssertionError("unexpected value");
      }
    }
    for (int i = 0; i < expected.length; i++) {
      boolean isSpecial = Double.isNaN(expected[i]) || Double.isInfinite(expected[i]);
      if (!(isSpecial && ignoreSpecial)) {
        final String msg = "entry #"+i+", left = "+data1[i]+", right = " + data2[i];
        Assertions.assertEquals(expected[i], actual.getEntry(i), msg);
      }
    }
  }

  private void doTestEbeBinaryOperationDimensionMismatch(final BinaryOperation op) {
    final int n = 10;
    switch (op) {
      case ADD:
        create(new double[n]).add(create(new double[n + 1]));
        break;
      case SUB:
        create(new double[n]).subtract(create(new double[n + 1]));
        break;
      case MUL:
        create(new double[n]).ebeMultiply(create(new double[n + 1]));
        break;
      case DIV:
        create(new double[n]).ebeDivide(create(new double[n + 1]));
        break;
      default:
        throw new AssertionError("unexpected value");
    }
  }

  @Test
  public void testAddSameType() {
    doTestEbeBinaryOperation(BinaryOperation.ADD, false, false);
  }

  @Test
  public void testAddMixedTypes() {
    doTestEbeBinaryOperation(BinaryOperation.ADD, true, false);
  }

  @Test
  public void testAddDimensionMismatch() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      doTestEbeBinaryOperationDimensionMismatch(BinaryOperation.ADD);
    });
  }

  @Test
  public void testSubtractSameType() {
    doTestEbeBinaryOperation(BinaryOperation.SUB, false, false);
  }

  @Test
  public void testSubtractMixedTypes() {
    doTestEbeBinaryOperation(BinaryOperation.SUB, true, false);
  }

  @Test
  public void testSubtractDimensionMismatch() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      doTestEbeBinaryOperationDimensionMismatch(BinaryOperation.SUB);
    });
  }

  @Test
  public void testEbeMultiplySameType() {
    doTestEbeBinaryOperation(BinaryOperation.MUL, false, false);
  }

  @Test
  public void testEbeMultiplyMixedTypes() {
    doTestEbeBinaryOperation(BinaryOperation.MUL, true, false);
  }

  @Test
  public void testEbeMultiplyDimensionMismatch() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      doTestEbeBinaryOperationDimensionMismatch(BinaryOperation.MUL);
    });
  }

  @Test
  public void testEbeDivideSameType() {
    doTestEbeBinaryOperation(BinaryOperation.DIV, false, false);
  }

  @Test
  public void testEbeDivideMixedTypes() {
    doTestEbeBinaryOperation(BinaryOperation.DIV, true, false);
  }

  @Test
  public void testEbeDivideDimensionMismatch() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      doTestEbeBinaryOperationDimensionMismatch(BinaryOperation.DIV);
    });
  }

  private void doTestGetDistance(final boolean mixed) {
    final double x = getPreferredEntryValue();
    final double[] data1 = new double[] { x, x, 1d, x, 2d, x, x, 3d, x };
    final double[] data2 = new double[] { 4d, x, x, 5d, 6d, 7d, x, x, 8d };
    final RealVector v1 = create(data1);
    final RealVector v2;
    if (mixed) {
      v2 = createAlien(data2);
    } else {
      v2 = create(data2);
    }
    final double actual = v1.getDistance(v2);
    double expected = 0d;
    for (int i = 0; i < data1.length; i++) {
      final double delta = data2[i] - data1[i];
      expected += delta * delta;
    }
    expected = FastMath.sqrt(expected);
    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void testGetDistanceSameType() {
    doTestGetDistance(false);
  }

  @Test
  public void testGetDistanceMixedTypes() {
    doTestGetDistance(true);
  }

  @Test
  public void testGetDistanceDimensionMismatch() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      create(new double[4]).getDistance(createAlien(new double[5]));
    });
  }

  @Test
  public void testGetNorm() {
    final double x = getPreferredEntryValue();
    final double[] data = new double[] { x, x, 1d, x, 2d, x, x, 3d, x };
    final RealVector v = create(data);
    final double actual = v.getNorm();
    double expected = 0d;
    for (int i = 0; i < data.length; i++) {
      expected += data[i] * data[i];
    }
    expected = FastMath.sqrt(expected);
    Assertions.assertEquals(expected, actual);
  }

  private void doTestGetL1Distance(final boolean mixed) {
    final double x = getPreferredEntryValue();
    final double[] data1 = new double[] { x, x, 1d, x, Double.NaN, 2d, x, x, 3d, x };
    final double[] data2 = new double[] { 4d, x, x, 5d, Double.NaN, 6d, 7d, x, x, 8d };
    final RealVector v1 = create(data1, matchingIndices(data1, Double.NaN));
    final RealVector v2;
    if (mixed) {
      v2 = createAlien(nonMatchingValues(data2, Double.NaN));
    } else {
      v2 = create(data2, matchingIndices(data2, Double.NaN));
    }
    final double actual = v1.getL1Distance(v2);
    double expected = 0d;
    for (int i = 0; i < data1.length; i++) {
      final double delta = data2[i] - data1[i];
      if (!Double.isNaN(delta)) {
        expected += FastMath.abs(delta);
      }
    }
    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void testGetL1DistanceSameType() {
    doTestGetL1Distance(false);
  }

  @Test
  public void testGetL1DistanceMixedTypes() {
    doTestGetL1Distance(true);
  }

  @Test
  public void testGetL1DistanceDimensionMismatch() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      create(new double[4], 3).getL1Distance(createAlien(new double[4]));
    });
  }

  @Test
  public void testGetL1Norm() {
    final double x = getPreferredEntryValue();
    final double[] data = new double[] { x, x, 1d, x, Double.POSITIVE_INFINITY,
        2d, Double.POSITIVE_INFINITY, x, x, 3d, x };
    final RealVector v = create(data, matchingIndices(data, Double.POSITIVE_INFINITY));
    final double actual = v.getL1Norm();
    double expected = 0d;
    for (int i = 0; i < data.length; i++) {
      if (!Double.isInfinite(data[i])) {
        expected += FastMath.abs(data[i]);
      }
    }
    Assertions.assertEquals(expected, actual);
  }

  private void doTestGetLInfDistance(final boolean mixed) {
    final double x = getPreferredEntryValue();
    final double[] data1 = new double[] { x, x, Double.NaN, 1d, x, 2d, x, x, 3d, x };
    final double[] data2 = new double[] { 4d, x, Double.NaN, x, 5d, 6d, 7d, x, x, 8d };
    final RealVector v1 = create(data1, matchingIndices(data1, Double.NaN));
    final RealVector v2;
    if (mixed) {
      v2 = createAlien(nonMatchingValues(data2, Double.NaN));
    } else {
      v2 = create(data2, matchingIndices(data2, Double.NaN));
    }
    final double actual = v1.getLInfDistance(v2);
    double expected = 0d;
    for (int i = 0; i < data1.length; i++) {
      final double delta = data2[i] - data1[i];
      if (!Double.isNaN(delta)) {
        expected = FastMath.max(expected, FastMath.abs(delta));
      }
    }
    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void testGetLInfDistanceSameType() {
    doTestGetLInfDistance(false);
  }

  @Test
  public void testGetLInfDistanceMixedTypes() {
    doTestGetLInfDistance(true);
  }

  @Test
  public void testGetLInfDistanceDimensionMismatch() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      create(new double[4], 3).getLInfDistance(createAlien(new double[4]));
    });
  }

  @Test
  public void testGetLInfNorm() {
    final double x = getPreferredEntryValue();
    final double[] data = new double[] {
        x, x, 1d, Double.NEGATIVE_INFINITY, x, 2d, x,
        Double.NEGATIVE_INFINITY, x, 3d, x
    };
    final RealVector v = create(data, matchingIndices(data, Double.NEGATIVE_INFINITY));
    final double actual = v.getLInfNorm();
    double expected = 0d;
    for (int i = 0; i < data.length; i++) {
      if (!Double.isInfinite(data[i])) {
        expected = FastMath.max(expected, FastMath.abs(data[i]));
      }
    }
    Assertions.assertEquals(expected, actual);
  }

  private void doTestMapBinaryOperation(final BinaryOperation op, final boolean inPlace) {
    final double[] expected = new double[values.length];
    for (int i = 0; i < values.length; i++) {
      final double d = values[i];
      for (int j = 0; j < expected.length; j++) {
        switch (op) {
          case ADD:
            expected[j] = values[j] + d;
            break;
          case SUB:
            expected[j] = values[j] - d;
            break;
          case MUL:
            expected[j] = values[j] * d;
            break;
          case DIV:
            expected[j] = values[j] / d;
            break;
          default:
            throw new AssertionError("unexpected value");
        }
      }
      final RealVector v = create(values);
      final RealVector actual;
      if (inPlace) {
        switch (op) {
          case ADD:
            actual = v.mapAddToSelf(d);
            break;
          case SUB:
            actual = v.mapSubtractToSelf(d);
            break;
          case MUL:
            actual = v.mapMultiplyToSelf(d);
            break;
          case DIV:
            actual = v.mapDivideToSelf(d);
            break;
          default:
            throw new AssertionError("unexpected value");
        }
      } else {
        switch (op) {
          case ADD:
            actual = v.mapAdd(d);
            break;
          case SUB:
            actual = v.mapSubtract(d);
            break;
          case MUL:
            actual = v.mapMultiply(d);
            break;
          case DIV:
            actual = v.mapDivide(d);
            break;
          default:
            throw new AssertionError("unexpected value");
        }
      }
      Assertions.assertArrayEquals(expected, actual.toArray(), 1.0e-12, Double.toString(d));
    }
  }

  @Test
  public void testMapAdd() {
    doTestMapBinaryOperation(BinaryOperation.ADD, false);
  }

  @Test
  public void testMapAddToSelf() {
    doTestMapBinaryOperation(BinaryOperation.ADD, true);
  }

  @Test
  public void testMapSubtract() {
    doTestMapBinaryOperation(BinaryOperation.SUB, false);
  }

  @Test
  public void testMapSubtractToSelf() {
    doTestMapBinaryOperation(BinaryOperation.SUB, true);
  }

  @Test
  public void testMapMultiply() {
    doTestMapBinaryOperation(BinaryOperation.MUL, false);
  }

  @Test
  public void testMapMultiplyToSelf() {
    doTestMapBinaryOperation(BinaryOperation.MUL, true);
  }

  @Test
  public void testMapDivide() {
    doTestMapBinaryOperation(BinaryOperation.DIV, false);
  }

  @Test
  public void testMapDivideToSelf() {
    doTestMapBinaryOperation(BinaryOperation.DIV, true);
  }

  private void doTestMapFunction(final UnivariateFunction f,
      final boolean inPlace) {
    final double[] data = new double[values.length + 6];
    System.arraycopy(values, 0, data, 0, values.length);
    data[values.length + 0] = 0.5 * FastMath.PI;
    data[values.length + 1] = -0.5 * FastMath.PI;
    data[values.length + 2] = FastMath.E;
    data[values.length + 3] = -FastMath.E;
    data[values.length + 4] = 1.0;
    data[values.length + 5] = -1.0;
    final double[] expected = new double[data.length];
    for (int i = 0; i < data.length; i++) {
      expected[i] = f.value(data[i]);
    }
    final RealVector v = create(data);
    final RealVector actual;
    if (inPlace) {
      actual = v.mapToSelf(f);
      Assertions.assertSame(v, actual);
    } else {
      actual = v.map(f);
    }
    Assertions.assertArrayEquals(expected, actual.toArray(), 1E-16, f.getClass().getSimpleName());
  }

  protected UnivariateFunction[] createFunctions() {
    return new UnivariateFunction[] {
        new Power(2.0), new Exp(), new Expm1(), new Log(), new Log10(),
        new Log1p(), new Cosh(), new Sinh(), new Tanh(), new Cos(),
        new Sin(), new Tan(), new Acos(), new Asin(), new Atan(),
        new Inverse(), new Abs(), new Sqrt(), new Cbrt(), new Ceil(),
        new Floor(), new Rint(), new Signum(), new Ulp()
    };
  }

  @Test
  public void testMap() {
    final UnivariateFunction[] functions = createFunctions();
    for (UnivariateFunction f : functions) {
      doTestMapFunction(f, false);
    }
  }

  @Test
  public void testMapToSelf() {
    final UnivariateFunction[] functions = createFunctions();
    for (UnivariateFunction f : functions) {
      doTestMapFunction(f, true);
    }
  }

  private void doTestOuterProduct(final boolean mixed) {
    final double[] dataU = values;
    final RealVector u = create(dataU);
    final double[] dataV = new double[values.length + 3];
    System.arraycopy(values, 0, dataV, 0, values.length);
    dataV[values.length] = 1d;
    dataV[values.length] = -2d;
    dataV[values.length] = 3d;
    final RealVector v;
    if (mixed) {
      v = createAlien(dataV);
    } else {
      v = create(dataV);
    }
    final RealMatrix uv = u.outerProduct(v);
    Assertions.assertEquals(dataU.length, uv
        .getRowDimension(), "number of rows");
    Assertions.assertEquals(dataV.length, uv
        .getColumnDimension(), "number of columns");
    for (int i = 0; i < dataU.length; i++) {
      for (int j = 0; j < dataV.length; j++) {
        final double expected = dataU[i] * dataV[j];
        final double actual = uv.getEntry(i, j);
        Assertions.assertEquals(expected, actual, dataU[i] + " * " + dataV[j]);
      }
    }
  }

  @Test
  public void testOuterProductSameType() {
    doTestOuterProduct(false);
  }

  @Test
  public void testOuterProductMixedTypes() {
    doTestOuterProduct(true);
  }

  private void doTestProjection(final boolean mixed) {
    final double x = getPreferredEntryValue();
    final double[] data1 = {
        x, 1d, x, x, 2d, x, x, x, 3d, x, x, x, x
    };
    final double[] data2 = {
        5d, -6d, 7d, x, x, -8d, -9d, 10d, 11d, x, 12d, 13d, -15d
    };
    double dotProduct = 0d;
    double norm2 = 0d;
    for (int i = 0; i < data1.length; i++){
      dotProduct += data1[i] * data2[i];
      norm2 += data2[i] * data2[i];
    }
    final double s = dotProduct / norm2;
    final double[] expected = new double[data1.length];
    for (int i = 0; i < data2.length; i++) {
      expected[i] = s * data2[i];
    }
    final RealVector v1 = create(data1);
    final RealVector v2;
    if (mixed) {
      v2 = createAlien(data2);
    } else {
      v2 = create(data2);
    }
    final RealVector actual = v1.projection(v2);
    Assertions.assertArrayEquals(expected, actual.toArray());
  }

  @Test
  public void testProjectionSameType() {
    doTestProjection(false);
  }

  @Test
  public void testProjectionMixedTypes() {
    doTestProjection(true);
  }

  @Test
  public void testProjectionNullVector() {
    Assertions.assertThrows(MathArithmeticException.class, () -> {
      create(new double[4], 3).projection(create(new double[4], 3));
    });
  }

  @Test
  public void testProjectionDimensionMismatch() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      final RealVector v1 = create(new double[5], 3, 4);
      final RealVector v2 = create(new double[5], 3);
      v2.set(1.0);
      v1.projection(v2);
    });
  }

  @Test
  public void testSet() {
    for (int i = 0; i < values.length; i++) {
      final double expected = values[i];
      final RealVector v = create(values);
      v.set(expected);
      for (int j = 0; j < values.length; j++) {
        Assertions.assertEquals(expected, v.getEntry(j), "entry #" + j);
      }
    }
  }

  @Test
  public void testToArray() {
    final double[] data = create(values).toArray();
    Assertions.assertNotSame(values, data);
    for (int i = 0; i < values.length; i++) {
      Assertions.assertEquals(values[i], data[i], "entry #" + i);
    }
  }

  private void doTestUnitVector(final boolean inPlace) {
    final double x = getPreferredEntryValue();
    final double[] data = {
        x, 1d, x, x, Double.NaN, 2d, x, x, x, Double.NaN, 3d, x, x, x, x
    };
    double norm = 0d;
    for (int i = 0; i < data.length; i++) {
      if (!Double.isNaN(data[i])) {
        norm += data[i] * data[i];
      }
    }
    norm = FastMath.sqrt(norm);
    final double[] expected = nonMatchingValues(data, Double.NaN);
    for (int i = 0; i < expected.length; i++) {
      expected[i] = expected[i] / norm;
    }
    final RealVector v = create(data, matchingIndices(data, Double.NaN));
    final RealVector actual;
    if (inPlace) {
      v.unitize();
      actual = v;
    } else {
      actual = v.unitVector();
      Assertions.assertNotSame(v, actual);
    }
    Assertions.assertArrayEquals(expected, actual.toArray());
  }

  @Test
  public void testUnitVector() {
    doTestUnitVector(false);
  }

  @Test
  public void testUnitize() {
    doTestUnitVector(true);
  }

  private void doTestUnitVectorNullVector(final boolean inPlace) {
    final double[] data = {
        0d, 0d, 0d, 0d, 0d
    };
    if (inPlace) {
      create(data).unitize();
    } else {
      create(data).unitVector();
    }
  }

  @Test
  public void testUnitVectorNullVector() {
    Assertions.assertThrows(ArithmeticException.class, () -> {
      doTestUnitVectorNullVector(false);
    });
  }

  @Test
  public void testUnitizeNullVector() {
    Assertions.assertThrows(ArithmeticException.class, () -> {
      doTestUnitVectorNullVector(true);
    });
  }

  private void doTestCombine(final boolean inPlace, final boolean mixed) {
    final int n = values.length * values.length;
    final double[] data1 = new double[n];
    final double[] data2 = new double[n];
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values.length; j++) {
        final int index = values.length * i + j;
        data1[index] = values[i];
        data2[index] = values[j];
      }
    }
    final RealVector v1 = create(data1);
    final RealVector v2 = mixed ? createAlien(data2) : create(data2);
    final double[] expected = new double[n];
    for (int i = 0; i < values.length; i++) {
      final double a1 = values[i];
      for (int j = 0; j < values.length; j++) {
        final double a2 = values[j];
        for (int k = 0; k < n; k++) {
          expected[k] = a1 * data1[k] + a2 * data2[k];
        }
        final RealVector actual;
        if (inPlace) {
          final RealVector v1bis = v1.copy();
          actual = v1bis.combineToSelf(a1, a2, v2);
          Assertions.assertSame(v1bis, actual);
        } else {
          actual = v1.combine(a1, a2, v2);
        }
        Assertions.assertArrayEquals(expected,
            actual.toArray(), "a1 = " + a1 + ", a2 = " + a2);
      }
    }
  }

  private void doTestCombineDimensionMismatch(final boolean inPlace, final boolean mixed) {
    final RealVector v1 = create(new double[10]);
    final RealVector v2;
    if (mixed) {
      v2 = createAlien(new double[15]);
    } else {
      v2 = create(new double[15]);
    }
    if (inPlace) {
      v1.combineToSelf(1.0, 1.0, v2);
    } else {
      v1.combine(1.0, 1.0, v2);
    }
  }

  @Test
  public void testCombineSameType() {
    doTestCombine(false, false);
  }

  @Test
  public void testCombineMixedTypes() {
    doTestCombine(false, true);
  }

  @Test
  public void testCombineDimensionMismatchSameType() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      doTestCombineDimensionMismatch(false, false);
    });
  }

  @Test
  public void testCombineDimensionMismatchMixedTypes() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      doTestCombineDimensionMismatch(false, true);
    });
  }

  @Test
  public void testCombineToSelfSameType() {
    doTestCombine(true, false);
  }

  @Test
  public void testCombineToSelfMixedTypes() {
    doTestCombine(true, true);
  }

  @Test
  public void testCombineToSelfDimensionMismatchSameType() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      doTestCombineDimensionMismatch(true, false);
    });
  }

  @Test
  public void testCombineToSelfDimensionMismatchMixedTypes() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      doTestCombineDimensionMismatch(true, true);
    });
  }

  @Test
  public void testCopy() {
    final RealVector v = create(values, 0, values.length - 1);
    final RealVector w = v.copy();
    Assertions.assertNotSame(v, w);
    double[] expected = new double[values.length -  2];
    System.arraycopy(values, 1, expected, 0, values.length - 2);
    Assertions.assertArrayEquals(expected, w.toArray());
  }

  private void doTestDotProductRegularValues(final boolean mixed) {
    final double x = getPreferredEntryValue();
    final double[] data1 = {
        x, 1d, x, x, 2d, x, x, x, 3d, x, x, x, x
    };
    final double[] data2 = {
        5d, -6d, 7d, x, x, -8d, -9d, 10d, 11d, x, 12d, 13d, -15d
    };
    double expected = 0d;
    for (int i = 0; i < data1.length; i++){
      expected += data1[i] * data2[i];
    }
    final RealVector v1 = create(data1);
    final RealVector v2;
    if (mixed) {
      v2 = createAlien(data2);
    } else {
      v2 = create(data2);
    }
    final double actual = v1.dotProduct(v2);
    Assertions.assertEquals(expected, actual);
  }

  private void doTestDotProductSpecialValues(final boolean mixed) {
    for (int i = 0; i < values.length; i++) {
      final double[] data1 = {
          values[i]
      };
      final RealVector v1 = create(data1);
      for (int j = 0; j < values.length; j++) {
        final double[] data2 = {
            values[j]
        };
        final RealVector v2;
        if (mixed) {
          v2 = createAlien(data2);
        } else {
          v2 = create(data2);
        }
        final double expected = data1[0] * data2[0];
        final double actual = v1.dotProduct(v2);
        Assertions.assertEquals(expected,
            actual, 1.0e-12, data1[0] + " * " + data2[0]);
      }
    }
  }

  private void doTestDotProductDimensionMismatch(final boolean mixed) {
    final double[] data1 = new double[10];
    final double[] data2 = new double[data1.length + 1];
    final RealVector v1 = create(data1);
    final RealVector v2;
    if (mixed) {
      v2 = createAlien(data2);
    } else {
      v2 = create(data2);
    }
    v1.dotProduct(v2);
  }

  @Test
  public void testDotProductSameType() {
    doTestDotProductRegularValues(false);
    doTestDotProductSpecialValues(false);
  }

  @Test
  public void testDotProductDimensionMismatchSameType() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      doTestDotProductDimensionMismatch(false);
    });
  }

  @Test
  public void testDotProductMixedTypes() {
    doTestDotProductRegularValues(true);
    doTestDotProductSpecialValues(true);
  }

  @Test
  public void testDotProductDimensionMismatchMixedTypes() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      doTestDotProductDimensionMismatch(true);
    });
  }

  private void doTestCosine(final boolean mixed) {
    final double x = getPreferredEntryValue();
    final double[] data1 = {
        x, 1d, x, x, 2d, x, x, x, 3d, x, x, x, x
    };
    final double[] data2 = {
        5d, -6d, 7d, x, x, -8d, -9d, 10d, 11d, x, 12d, 13d, -15d
    };
    double norm1 = 0d;
    double norm2 = 0d;
    double dotProduct = 0d;
    for (int i = 0; i < data1.length; i++){
      norm1 += data1[i] * data1[i];
      norm2 += data2[i] * data2[i];
      dotProduct += data1[i] * data2[i];
    }
    norm1 = FastMath.sqrt(norm1);
    norm2 = FastMath.sqrt(norm2);
    final double expected = dotProduct / (norm1 * norm2);
    final RealVector v1 = create(data1);
    final RealVector v2;
    if (mixed) {
      v2 = createAlien(data2);
    } else {
      v2 = create(data2);
    }
    final double actual = v1.cosine(v2);
    Assertions.assertEquals(expected, actual);

  }

  @Test
  public void testCosineSameType() {
    doTestCosine(false);
  }

  @Test
  public void testCosineMixedTypes() {
    doTestCosine(true);
  }

  @Test
  public void testCosineLeftNullVector() {
    Assertions.assertThrows(MathArithmeticException.class, () -> {
      double[] data1 = new double[]{ Double.NaN, 0, 0, 0};
      double[] data2 = new double[]{1, 0, 0, Double.NaN };
      final RealVector v = create(data1, matchingIndices(data1, Double.NaN));
      final RealVector w = create(data2, matchingIndices(data2, Double.NaN));
      v.cosine(w);
    });
  }

  @Test
  public void testCosineRightNullVector() {
    Assertions.assertThrows(MathArithmeticException.class, () -> {
      double[] data1 = new double[]{ Double.NaN, 0, 0, 0};
      double[] data2 = new double[]{1, 0, 0, Double.NaN };
      final RealVector v = create(data1, matchingIndices(data1, Double.NaN));
      final RealVector w = create(data2, matchingIndices(data2, Double.NaN));
      w.cosine(v);
    });
  }

  public void testCosineDimensionMismatch() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      final RealVector v = create(new double[] {1, 2, 3});
      final RealVector w = create(new double[] {1, 2, 3, 4});
      v.cosine(w);
    });
  }

  @Test
  public void testEquals() {

    double[] data = new double[] {0, Double.NaN, 1, 2};
    final RealVector v = create(data, matchingIndices(data, Double.NaN));

    Assertions.assertTrue(v.equals(v));
    Assertions.assertTrue(v.equals(v.copy()));
    Assertions.assertFalse(v.equals(null));
    Assertions.assertFalse(v.equals(v.getSubVector(0, v.getDimension() - 1)));
    Assertions.assertTrue(v.equals(v.getSubVector(0, v.getDimension())));

    Random random = new Random(909900L);
    int n = 50;
    data = new double[2*n];
    for (int i=0; i<n; i++) {
      data[2*i] = random.nextDouble();
      data[2*i+ 1] = Double.NaN;
    }

    // Should be equal even though they are different classes.
    RealVector rv1 = create(data, matchingIndices(data, Double.NaN));
    RealVector rv2 = new ArrayRealVector(nonMatchingValues(data, Double.NaN));

    Assertions.assertEquals(rv1, rv2);
    Assertions.assertEquals(rv1.hashCode(), rv2.hashCode());
  }

  @Test
  public void testSerial()  {
    RealVector v = create(new double[] { 0, 1, 2 }, 2);
    Assertions.assertArrayEquals(v.toArray(), TestUtil.serializeAndRecover(v).toArray());
  }

  @Test
  public void testMinMax() {
    double[] data = new double[] {
        Double.POSITIVE_INFINITY, 0, -6, 4, Double.POSITIVE_INFINITY, 12, 7
    };
    final RealVector v1 = create(data, matchingIndices(data, Double.POSITIVE_INFINITY));
    Assertions.assertEquals(1, v1.getMinIndex());
    Assertions.assertEquals(-6, v1.getMinValue(), 1.0e-12);
    Assertions.assertEquals(3, v1.getMaxIndex());
    Assertions.assertEquals(12, v1.getMaxValue(), 1.0e-12);
    final RealVector v2 = create(new double[] {Double.NaN, 3, Double.NaN, -2});
    Assertions.assertEquals(3, v2.getMinIndex());
    Assertions.assertEquals(-2, v2.getMinValue(), 1.0e-12);
    Assertions.assertEquals(1, v2.getMaxIndex());
    Assertions.assertEquals(3, v2.getMaxValue(), 1.0e-12);
    final RealVector v3 = create(new double[] {Double.NaN, Double.NaN});
    Assertions.assertEquals(-1, v3.getMinIndex());
    Assertions.assertTrue(Double.isNaN(v3.getMinValue()));
    Assertions.assertEquals(-1, v3.getMaxIndex());
    Assertions.assertTrue(Double.isNaN(v3.getMaxValue()));
    final RealVector v4 = create(new double[0]);
    Assertions.assertEquals(-1, v4.getMinIndex());
    Assertions.assertTrue(Double.isNaN(v4.getMinValue()));
    Assertions.assertEquals(-1, v4.getMaxIndex());
    Assertions.assertTrue(Double.isNaN(v4.getMaxValue()));
  }

  /*
   * TESTS OF THE VISITOR PATTERN
   */

  /** The whole vector is visited. */
  @Test
  public void testWalkInDefaultOrderPreservingVisitor1() {
    final double[] data = new double[] {
        0d, 1d, 0d, 0d, 2d, 0d, 0d, 0d, 3d
    };
    final RealVector v = create(data);
    final RealVectorPreservingVisitor visitor;
    visitor = new RealVectorPreservingVisitor() {

      private int expectedIndex;

      public void visit(final int actualIndex, final double actualValue) {
        Assertions.assertEquals(expectedIndex, actualIndex);
        Assertions.assertEquals(
            data[actualIndex], actualValue, Integer.toString(actualIndex));
        ++expectedIndex;
      }

      public void start(final int actualSize, final int actualStart,
          final int actualEnd) {
        Assertions.assertEquals(data.length, actualSize);
        Assertions.assertEquals(0, actualStart);
        Assertions.assertEquals(data.length - 1, actualEnd);
        expectedIndex = 0;
      }

      public double end() {
        return 0.0;
      }
    };
    v.walkInDefaultOrder(visitor);
  }

  /** Visiting an invalid subvector. */
  @Test
  public void testWalkInDefaultOrderPreservingVisitor2() {
    final RealVector v = create(new double[5]);
    final RealVectorPreservingVisitor visitor;
    visitor = new RealVectorPreservingVisitor() {

      public void visit(int index, double value) {
        // Do nothing
      }

      public void start(int dimension, int start, int end) {
        // Do nothing
      }

      public double end() {
        return 0.0;
      }
    };
    try {
      v.walkInDefaultOrder(visitor, -1, 4);
      Assertions.fail();
    } catch (OutOfRangeException e) {
      // Expected behavior
    }
    try {
      v.walkInDefaultOrder(visitor, 5, 4);
      Assertions.fail();
    } catch (OutOfRangeException e) {
      // Expected behavior
    }
    try {
      v.walkInDefaultOrder(visitor, 0, -1);
      Assertions.fail();
    } catch (OutOfRangeException e) {
      // Expected behavior
    }
    try {
      v.walkInDefaultOrder(visitor, 0, 5);
      Assertions.fail();
    } catch (OutOfRangeException e) {
      // Expected behavior
    }
    try {
      v.walkInDefaultOrder(visitor, 4, 0);
      Assertions.fail();
    } catch (NumberIsTooSmallException e) {
      // Expected behavior
    }
  }

  /** Visiting a valid subvector. */
  @Test
  public void testWalkInDefaultOrderPreservingVisitor3() {
    final double[] data = new double[] {
        0d, 1d, 0d, 0d, 2d, 0d, 0d, 0d, 3d
    };
    final int expectedStart = 2;
    final int expectedEnd = 7;
    final RealVector v = create(data);
    final RealVectorPreservingVisitor visitor;
    visitor = new RealVectorPreservingVisitor() {

      private int expectedIndex;

      public void visit(final int actualIndex, final double actualValue) {
        Assertions.assertEquals(expectedIndex, actualIndex);
        Assertions.assertEquals(
            data[actualIndex], actualValue, Integer.toString(actualIndex));
        ++expectedIndex;
      }

      public void start(final int actualSize, final int actualStart,
          final int actualEnd) {
        Assertions.assertEquals(data.length, actualSize);
        Assertions.assertEquals(expectedStart, actualStart);
        Assertions.assertEquals(expectedEnd, actualEnd);
        expectedIndex = expectedStart;
      }

      public double end() {
        return 0.0;
      }
    };
    v.walkInDefaultOrder(visitor, expectedStart, expectedEnd);
  }

  /**
   * Minimal implementation of the {@link RealVector} abstract class, for
   * mixed types unit tests.
   */
  public static class RealVectorTestImpl extends RealVector
      implements Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 20120706L;

    /** Entries of the vector. */
    protected double data[];

    public RealVectorTestImpl(double[] d) {
      data = d.clone();
    }

    private UnsupportedOperationException unsupported() {
      return new UnsupportedOperationException("Not supported, unneeded for test purposes");
    }

    @Override
    public RealVector copy() {
      return new RealVectorTestImpl(data);
    }

    @Override
    public RealVector ebeMultiply(RealVector v) {
      throw unsupported();
    }

    @Override
    public RealVector ebeDivide(RealVector v) {
      throw unsupported();
    }

    @Override
    public double getEntry(int index) {
      checkIndex(index);
      return data[index];
    }

    @Override
    public int getDimension() {
      return data.length;
    }

    @Override
    public RealVector append(RealVector v) {
      throw unsupported();
    }

    @Override
    public RealVector append(double d) {
      throw unsupported();
    }

    @Override
    public RealVector getSubVector(int index, int n) {
      throw unsupported();
    }

    @Override
    public void setEntry(int index, double value) {
      checkIndex(index);
      data[index] = value;
    }

    @Override
    public void setSubVector(int index, RealVector v) {
      throw unsupported();
    }

    @Override
    public boolean isNaN() {
      throw unsupported();
    }

    @Override
    public boolean isInfinite() {
      throw unsupported();
    }
  }

}