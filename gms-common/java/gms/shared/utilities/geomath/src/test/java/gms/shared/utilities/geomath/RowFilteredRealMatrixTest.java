package gms.shared.utilities.geomath;

import static org.junit.jupiter.api.Assertions.*;

import java.util.BitSet;
import java.util.function.DoublePredicate;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RowFilteredRealMatrixTest {

  @Test
  public void testStaticFilterRows() {

    final double[][] data = new double[][] {
        { 3.0, 3.0, 1.0 },
        { Double.NaN, 1.0, 4.0 },
        { 2.0, 1.0, 10.0 },
        { 0.0, Double.NaN, 2.0},
        { 5.0, 1.0, 8.0 }
    };

    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data);

    RealMatrix realMatrix = RowFilteredRealMatrix.filterRows(innerMatrix,
        n -> {
          for (double v: data[n]) {
            if (Double.isNaN(v)) {
              return true;
            }
          }
          return false;
        });

    Assertions.assertEquals(3, realMatrix.getRowDimension());
    Assertions.assertEquals(innerMatrix.getColumnDimension(), realMatrix.getColumnDimension());
    Assertions.assertArrayEquals(data[4], realMatrix.getRowVector(2).toArray());
  }

  @Test
  public void testStaticIncludeRows() {

    final double[][] data = new double[][] {
        { 3.0, 3.0, 1.0 },
        { Double.NaN, 1.0, 4.0 },
        { 2.0, 1.0, 10.0 },
        { 0.0, Double.NaN, 2.0},
        { 5.0, 1.0, 8.0 }
    };

    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data);

    RealMatrix realMatrix = RowFilteredRealMatrix.includeRows(innerMatrix,
        n -> {
          for (double v: data[n]) {
            if (Double.isNaN(v)) {
              return true;
            }
          }
          return false;
        });

    Assertions.assertEquals(2, realMatrix.getRowDimension());
    Assertions.assertEquals(innerMatrix.getColumnDimension(), realMatrix.getColumnDimension());
    Assertions.assertArrayEquals(data[1], realMatrix.getRowVector(0).toArray());
  }

  @Test
  public void testFilterRowsByValue() {
    double[][] data = new double[][] {
        { 0.0, 0.0, 0.0 },
        { 1.0, 0.0, 2.0 },
        { 2.0, 1.0, 0.0 },
        { 0.0, 0.0, 0.0 },
        { 3.0, 1.0, 3.0 }
    };

    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data);

    // Rows with any zeros will be filtered.
    RealMatrix realMatrix = RowFilteredRealMatrix.filterRowsByValue(
        innerMatrix,
        v -> v == 0.0,
        false
    );

    Assertions.assertEquals(1, realMatrix.getRowDimension());
    Assertions.assertArrayEquals(data[4], realMatrix.getRowVector(0).toArray());

    // Only rows with all zeros will be filtered.
    realMatrix = RowFilteredRealMatrix.filterRowsByValue(
        innerMatrix,
        v -> v == 0.0,
        true
    );

    Assertions.assertEquals(3, realMatrix.getRowDimension());
    Assertions.assertArrayEquals(data[1], realMatrix.getRowVector(0).toArray());
  }

  @Test
  public void testIncludeRowsByValue() {
    double[][] data = new double[][] {
        { 0.0, 0.0, 0.0 },
        { 1.0, 0.0, 2.0 },
        { 2.0, 1.0, 0.0 },
        { 0.0, 0.0, 0.0 },
        { 3.0, 1.0, 3.0 }
    };

    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data);

    // Rows with any zeros will be filtered.
    RealMatrix realMatrix = RowFilteredRealMatrix.includeRowsByValue(
        innerMatrix,
        v -> v == 0.0,
        false
    );

    Assertions.assertEquals(4, realMatrix.getRowDimension());
    Assertions.assertArrayEquals(data[3], realMatrix.getRowVector(3).toArray());

    // Only rows with all zeros will be filtered.
    realMatrix = RowFilteredRealMatrix.includeRowsByValue(
        innerMatrix,
        v -> v == 0.0,
        true
    );

    Assertions.assertEquals(2, realMatrix.getRowDimension());
    Assertions.assertArrayEquals(data[0], realMatrix.getRowVector(0).toArray());
    Assertions.assertArrayEquals(data[3], realMatrix.getRowVector(1).toArray());
  }

  @Test
  public void testConstructorWithNoExclusionBits() {
    double[][] data = new double[7][5];
    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data);
    RowFilteredRealMatrix rowFilteredRealMatrix = new RowFilteredRealMatrix(innerMatrix);
    Assertions.assertEquals(innerMatrix.getRowDimension(),
        rowFilteredRealMatrix.getRowDimension());
  }

  @Test
  public void testExcludeSameRows() {

    RealMatrix innerMatrix1 = MatrixUtils.createRealMatrix(
        new double[][] {
        { 0.0, 0.0, 0.0 },
        { 1.0, 0.0, 2.0 },
        { 2.0, 1.0, 0.0 },
        { 0.0, 0.0, 0.0 },
        { 3.0, 1.0, 3.0 }
    });
    RealMatrix innerMatrix2 = MatrixUtils.createRealMatrix(
      new double[][] {
        { 1.0, 0.0, 2.0 },
        { 0.0, 0.0, 0.0 },
        { 2.0, 1.0, 0.0 },
        { 3.0, 1.0, 3.0 },
        { 0.0, 0.0, 0.0 }
    });

    DoublePredicate predicate = d -> d == 0.0;

    RowFilteredRealMatrix rfrm1 = RowFilteredRealMatrix.filterRowsByValue(
        innerMatrix1, predicate, true
    );
    RowFilteredRealMatrix rfrm2 = RowFilteredRealMatrix.filterRowsByValue(
        innerMatrix2, predicate, true
    );

    RowFilteredRealMatrix rfrm3 = rfrm1.excludeSameRows(rfrm2);

    Assertions.assertSame(rfrm1.getInnerMatrix(), rfrm3.getInnerMatrix());

    for (int dim=0; dim<rfrm2.getInnerRowDimension(); dim++) {
      Assertions.assertEquals(rfrm2.isIncluded(dim), rfrm3.isIncluded(dim));
    }
  }

  @Test
  public void testExcludeSameRowsWithDimensionMismatch() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      RowFilteredRealMatrix rfrm1 = RowFilteredRealMatrix.filterRows(
          MatrixUtils.createRealMatrix(5, 5), n -> n%2 == 0
      );
      RowFilteredRealMatrix rfrm2 = RowFilteredRealMatrix.filterRows(
          MatrixUtils.createRealMatrix(4, 5), n -> n%2 == 1
      );
      rfrm1 = rfrm1.excludeSameRows(rfrm2);
    });
  }

  @Test
  public void testTranspose() {

    double[][] data = new double[][] {
        {1.1, 1.2, 1.3, 1.4},
        {1.4, 1.5, 1.6, 1.7},
        {Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {1.8, 1.9, 2.0, 2.1}
    };

    // Will filter rows with any NaNs.
    RealMatrix filteredDerivs = RowFilteredRealMatrix.filterRowsByValue(
        MatrixUtils.createRealMatrix(data),
        v -> Double.isNaN(v),
        false
    );

    for (int row=0; row<data.length; row++) {
      Assertions.assertEquals(row != 2,
          ((RowFilteredRealMatrix) filteredDerivs).isIncluded(row));
    }

    RealMatrix transpose = filteredDerivs.transpose();

    Assertions.assertEquals(filteredDerivs.getRowDimension(), transpose.getColumnDimension());
    Assertions.assertEquals(filteredDerivs.getColumnDimension(), transpose.getRowDimension());

    for (int row=0; row<filteredDerivs.getRowDimension(); row++) {
      RealVector rowVector = filteredDerivs.getRowVector(row);
      RealVector colVector = transpose.getColumnVector(row);
      Assertions.assertEquals(rowVector, colVector);
    }

    RealMatrix cov1 = transpose.multiply(filteredDerivs);

    double[][] data2 = filteredDerivs.getData();
    Assertions.assertEquals(3, data2.length);
    Assertions.assertArrayEquals(data2[0], data[0]);
    Assertions.assertArrayEquals(data2[1], data[1]);
    Assertions.assertArrayEquals(data2[2], data[3]);

    RealMatrix derivMatrix2 = MatrixUtils.createRealMatrix(data2);

    RealMatrix cov2 = derivMatrix2.transpose().multiply(derivMatrix2);

    Assertions.assertEquals(cov1, cov2);
  }

  @Test
  public void testGetRowDimension() {
    double[][] data = new double[][] {
        { 3.0, 3.0, 1.0 },
        { Double.NaN, 1.0, 4.0 },
        { 2.0, 1.0, 10.0 },
        { 0.0, Double.NaN, 2.0},
        { 5.0, 1.0, 8.0 }
    };

    final RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data);
    final int[] exclusionIndices = TestUtil.matchingRowsAny(innerMatrix,
        v -> Double.isNaN(v));

    final BitSet bitSet = new BitSet(innerMatrix.getRowDimension());
    for (int n: exclusionIndices) {
      bitSet.set(n);
    }

    RealMatrix realMatrix = new RowFilteredRealMatrix(
        innerMatrix,
        bitSet);

    assertEquals(3, realMatrix.getRowDimension());
    assertEquals(innerMatrix.getColumnDimension(), realMatrix.getColumnDimension());

    Assertions.assertArrayEquals(data[0], realMatrix.getRowVector(0).toArray());
    Assertions.assertArrayEquals(data[2], realMatrix.getRowVector(1).toArray());
    Assertions.assertArrayEquals(data[4], realMatrix.getRowVector(2).toArray());
  }

  @Test
  public void testCreateMatrix() {
    final int numRows = 5;
    final int numCols = 10;
    RealMatrix realMatrix = MatrixUtils.createRealMatrix(1, 1);
    RowFilteredRealMatrix rowFilteredRealMatrix = new RowFilteredRealMatrix(
        realMatrix, null);
    RealMatrix realMatrix2 = rowFilteredRealMatrix.createMatrix(numRows, numCols);
    Assertions.assertTrue(realMatrix2 instanceof RowFilteredRealMatrix);
    Assertions.assertEquals(numRows, realMatrix2.getRowDimension());
    Assertions.assertEquals(numCols, realMatrix2.getColumnDimension());
  }

  @Test
  public void testCopy() {

    double[][] data = new double[][] {
        { 0.0, 0.0, 0.0 },
        { 1.0, 0.0, 2.0 },
        { 2.0, 1.0, 0.0 },
        { 0.0, 0.0, 0.0 },
        { 3.0, 1.0, 3.0 }
    };

    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data);
    int[] zeroIndices = TestUtil.matchingRowsAll(innerMatrix, v -> v == 0.0);
    BitSet bits = new BitSet(innerMatrix.getRowDimension());
    for (int n: zeroIndices) {
      bits.set(n);
    }

    RealMatrix realMatrix = new RowFilteredRealMatrix(innerMatrix, bits);
    RealMatrix realMatrix2 = realMatrix.copy();

    Assertions.assertNotSame(realMatrix, realMatrix2);

    Assertions.assertEquals(realMatrix, realMatrix2);
  }

  @Test
  public void getEntry() {
    final double[][] data = new double[][] {
        { 0.0, 0.0, 0.0 },
        { 1.0, 0.0, 2.0 },
        { 2.0, 1.0, 0.0 },
        { 0.0, 0.0, 0.0 },
        { 3.0, 1.0, 3.0 }
    };

    final RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data);
    final RealMatrix realMatrix = RowFilteredRealMatrix.filterRows(innerMatrix,
        n -> n == 3);
    Assertions.assertEquals(3.0, realMatrix.getEntry(3, 0));
  }

  @Test
  public void setEntry() {
    final double[][] data = new double[][] {
        { 0.0, 0.0, 0.0 },
        { 1.0, 0.0, 2.0 },
        { 2.0, 1.0, 0.0 },
        { 0.0, 0.0, 0.0 },
        { 3.0, 1.0, 3.0 }
    };

    final RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data);
    final RealMatrix realMatrix = RowFilteredRealMatrix.filterRows(innerMatrix,
        n -> n == 0 || n == 3);

    realMatrix.setEntry(0, 1, 10.0);
    Assertions.assertEquals(10.0, realMatrix.getEntry(0, 1));
    Assertions.assertEquals(10.0, innerMatrix.getEntry(1, 1));

    for (int row = 0; row < realMatrix.getRowDimension(); row++) {
      for (int col = 0; col < realMatrix.getColumnDimension(); col++) {
        realMatrix.setEntry(row, col, -1.0);
      }
    }

    for (int row=0; row<innerMatrix.getRowDimension(); row++) {
      if (row != 0 && row != 3) {
        for (int col = 0; col<innerMatrix.getColumnDimension(); col++) {
          Assertions.assertEquals(-1.0, innerMatrix.getEntry(row, col));
        }
      }
    }
  }

  @Test
  public void testSerial() {
    final double[][] data = new double[][] {
        { 0.0, 0.0, 0.0 },
        { 1.0, 0.0, 2.0 },
        { 2.0, 1.0, 0.0 },
        { 0.0, 0.0, 0.0 },
        { 3.0, 1.0, 3.0 }
    };

    final RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data);
    // Filter even rows
    final RealMatrix realMatrix = RowFilteredRealMatrix.filterRows(innerMatrix,
        n -> n%2 == 0);

    final RealMatrix realMatrix2 = TestUtil.serializeAndRecover(realMatrix);
    Assertions.assertNotSame(realMatrix, realMatrix2);
    Assertions.assertEquals(realMatrix, realMatrix2);
  }
}