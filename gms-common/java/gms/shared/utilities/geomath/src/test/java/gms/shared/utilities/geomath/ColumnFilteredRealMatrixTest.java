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

public class ColumnFilteredRealMatrixTest {

  @Test
  public void testStaticFilterColumns() {

    final double[][] data = new double[][] {
        { 3.0, 3.0, 1.0 },
        { Double.NaN, 1.0, 4.0 },
        { 2.0, 1.0, 10.0 },
        { 0.0, Double.NaN, 2.0},
        { 5.0, 1.0, 8.0 }
    };

    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data).transpose();

    RealMatrix realMatrix = ColumnFilteredRealMatrix.filterColumns(innerMatrix,
        n -> {
          for (double v: data[n]) {
            if (Double.isNaN(v)) {
              return true;
            }
          }
          return false;
        });

    Assertions.assertEquals(3, realMatrix.getColumnDimension());
    Assertions.assertEquals(innerMatrix.getRowDimension(), realMatrix.getRowDimension());
    Assertions.assertArrayEquals(innerMatrix.getColumnVector(4).toArray(),
        realMatrix.getColumnVector(2).toArray());
  }

  @Test
  public void testStaticIncludeColumns() {

    final double[][] data = new double[][] {
        { 3.0, 3.0, 1.0 },
        { Double.NaN, 1.0, 4.0 },
        { 2.0, 1.0, 10.0 },
        { 0.0, Double.NaN, 2.0},
        { 5.0, 1.0, 8.0 }
    };

    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data).transpose();

    RealMatrix realMatrix = ColumnFilteredRealMatrix.includeColumns(innerMatrix,
        n -> {
          for (double v: data[n]) {
            if (Double.isNaN(v)) {
              return true;
            }
          }
          return false;
        });

    Assertions.assertEquals(2, realMatrix.getColumnDimension());
    Assertions.assertEquals(innerMatrix.getRowDimension(), realMatrix.getRowDimension());
    Assertions.assertArrayEquals(data[1], realMatrix.getColumnVector(0).toArray());
  }

  @Test
  public void testFilterColumnsByValue() {
    double[][] data = new double[][] {
        { 0.0, 0.0, 0.0 },
        { 1.0, 0.0, 2.0 },
        { 2.0, 1.0, 0.0 },
        { 0.0, 0.0, 0.0 },
        { 3.0, 1.0, 3.0 }
    };

    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data).transpose();

    // Columns with any zeros will be filtered.
    RealMatrix realMatrix = ColumnFilteredRealMatrix.filterColumnsByValue(
        innerMatrix,
        v -> v == 0.0,
        false
    );

    Assertions.assertEquals(1, realMatrix.getColumnDimension());
    Assertions.assertArrayEquals(data[4], realMatrix.getColumnVector(0).toArray());

    // Only rows with all zeros will be filtered.
    realMatrix = ColumnFilteredRealMatrix.filterColumnsByValue(
        innerMatrix,
        v -> v == 0.0,
        true
    );

    Assertions.assertEquals(3, realMatrix.getColumnDimension());
    Assertions.assertArrayEquals(data[1], realMatrix.getColumnVector(0).toArray());
  }

  @Test
  public void testIncludeColumnsByValue() {
    double[][] data = new double[][] {
        { 0.0, 0.0, 0.0 },
        { 1.0, 0.0, 2.0 },
        { 2.0, 1.0, 0.0 },
        { 0.0, 0.0, 0.0 },
        { 3.0, 1.0, 3.0 }
    };

    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data).transpose();

    // Rows with any zeros will be filtered.
    RealMatrix realMatrix = ColumnFilteredRealMatrix.includeColumnsByValue(
        innerMatrix,
        v -> v == 0.0,
        false
    );

    Assertions.assertEquals(4, realMatrix.getColumnDimension());
    Assertions.assertArrayEquals(data[3], realMatrix.getColumnVector(3).toArray());

    // Only rows with all zeros will be filtered.
    realMatrix = ColumnFilteredRealMatrix.includeColumnsByValue(
        innerMatrix,
        v -> v == 0.0,
        true
    );

    Assertions.assertEquals(2, realMatrix.getColumnDimension());
    Assertions.assertArrayEquals(data[0], realMatrix.getColumnVector(0).toArray());
    Assertions.assertArrayEquals(data[3], realMatrix.getColumnVector(1).toArray());
  }

  @Test
  public void testConstructorWithNoExclusionBits() {
    double[][] data = new double[7][5];
    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data);
    ColumnFilteredRealMatrix colFilteredRealMatrix = new ColumnFilteredRealMatrix(innerMatrix);
    Assertions.assertEquals(innerMatrix.getColumnDimension(),
        colFilteredRealMatrix.getColumnDimension());

    int[] includedCols = colFilteredRealMatrix.includedInnerColumns();
    Assertions.assertArrayEquals(new int[] { 0, 1, 2, 3, 4}, includedCols);
  }

  @Test
  public void testExcludeSameColumns() {

    RealMatrix innerMatrix1 = MatrixUtils.createRealMatrix(
        new double[][] {
            { 0.0, 0.0, 0.0 },
            { 1.0, 0.0, 2.0 },
            { 2.0, 1.0, 0.0 },
            { 0.0, 0.0, 0.0 },
            { 3.0, 1.0, 3.0 }
        }).transpose();

    RealMatrix innerMatrix2 = MatrixUtils.createRealMatrix(
        new double[][] {
            { 1.0, 0.0, 2.0 },
            { 0.0, 0.0, 0.0 },
            { 2.0, 1.0, 0.0 },
            { 3.0, 1.0, 3.0 },
            { 0.0, 0.0, 0.0 }
        }).transpose();

    DoublePredicate predicate = d -> d == 0.0;

    ColumnFilteredRealMatrix cfrm1 = ColumnFilteredRealMatrix.filterColumnsByValue(
        innerMatrix1, predicate, true
    );
    ColumnFilteredRealMatrix cfrm2 = ColumnFilteredRealMatrix.filterColumnsByValue(
        innerMatrix2, predicate, true
    );

    ColumnFilteredRealMatrix cfrm3 = cfrm1.excludeSameColumns(cfrm2);

    Assertions.assertSame(cfrm1.getInnerMatrix(), cfrm3.getInnerMatrix());

    for (int dim=0; dim<cfrm2.getInnerColumnDimension(); dim++) {
      Assertions.assertEquals(cfrm2.isIncluded(dim), cfrm3.isIncluded(dim));
    }
  }

  @Test
  public void testExcludeSameColumnsWithDimensionMismatch() {
    Assertions.assertThrows(DimensionMismatchException.class, () -> {
      ColumnFilteredRealMatrix cfrm1 = ColumnFilteredRealMatrix.filterColumns(
          MatrixUtils.createRealMatrix(5, 5), n -> n%2 == 0
      );
      ColumnFilteredRealMatrix cfrm2 = ColumnFilteredRealMatrix.filterColumns(
          MatrixUtils.createRealMatrix(5, 4), n -> n%2 == 1
      );
      cfrm1 = cfrm1.excludeSameColumns(cfrm2);
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

    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data).transpose();

    // Will filter rows with any NaNs.
    RealMatrix filteredDerivs = ColumnFilteredRealMatrix.filterColumnsByValue(
        innerMatrix,
        v -> Double.isNaN(v),
        false
    );

    for (int col=0; col<data[0].length; col++) {
      Assertions.assertEquals(col != 2,
          ((ColumnFilteredRealMatrix) filteredDerivs).isIncluded(col));
    }

    RealMatrix transpose = filteredDerivs.transpose();

    Assertions.assertEquals(filteredDerivs.getColumnDimension(), transpose.getRowDimension());
    Assertions.assertEquals(filteredDerivs.getRowDimension(), transpose.getColumnDimension());

    for (int col=0; col<filteredDerivs.getColumnDimension(); col++) {
      RealVector colVector = filteredDerivs.getColumnVector(col);
      RealVector rowVector = transpose.getRowVector(col);
      Assertions.assertEquals(rowVector, colVector);
    }

    RealMatrix cov1 = transpose.multiply(filteredDerivs);

    double[][] data2 = filteredDerivs.getData();
    Assertions.assertEquals(4, data2.length);

    RealMatrix derivMatrix2 = MatrixUtils.createRealMatrix(data2);

    RealMatrix cov2 = derivMatrix2.transpose().multiply(derivMatrix2);

    Assertions.assertEquals(cov1, cov2);
  }

  @Test
  public void testGetColumnDimension() {
    double[][] data = new double[][] {
        { 3.0, 3.0, 1.0 },
        { Double.NaN, 1.0, 4.0 },
        { 2.0, 1.0, 10.0 },
        { 0.0, Double.NaN, 2.0},
        { 5.0, 1.0, 8.0 }
    };

    final RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data).transpose();
    final int[] exclusionIndices = TestUtil.matchingColumnsAny(innerMatrix,
        v -> Double.isNaN(v));

    final BitSet bitSet = new BitSet(innerMatrix.getColumnDimension());
    for (int n: exclusionIndices) {
      bitSet.set(n);
    }

    RealMatrix realMatrix = new ColumnFilteredRealMatrix(
        innerMatrix,
        bitSet);

    assertEquals(3, realMatrix.getColumnDimension());
    assertEquals(innerMatrix.getRowDimension(), realMatrix.getRowDimension());

    Assertions.assertArrayEquals(data[0], realMatrix.getColumnVector(0).toArray());
    Assertions.assertArrayEquals(data[2], realMatrix.getColumnVector(1).toArray());
    Assertions.assertArrayEquals(data[4], realMatrix.getColumnVector(2).toArray());
  }

  @Test
  public void testCreateMatrix() {
    final int numRows = 5;
    final int numCols = 10;
    RealMatrix realMatrix = MatrixUtils.createRealMatrix(1, 1);
    ColumnFilteredRealMatrix colFilteredRealMatrix = new ColumnFilteredRealMatrix(
        realMatrix, null);
    RealMatrix realMatrix2 = colFilteredRealMatrix.createMatrix(numRows, numCols);
    Assertions.assertTrue(realMatrix2 instanceof ColumnFilteredRealMatrix);
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

    RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data).transpose();
    int[] zeroIndices = TestUtil.matchingColumnsAll(innerMatrix, v -> v == 0.0);
    BitSet bits = new BitSet(innerMatrix.getColumnDimension());
    for (int n: zeroIndices) {
      bits.set(n);
    }

    RealMatrix realMatrix = new ColumnFilteredRealMatrix(innerMatrix, bits);
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

    final RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data).transpose();
    final RealMatrix realMatrix = ColumnFilteredRealMatrix.filterColumns(innerMatrix,
        n -> n == 3);
    Assertions.assertEquals(3.0, realMatrix.getEntry(0, 3));
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

    final RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data).transpose();
    final RealMatrix realMatrix = ColumnFilteredRealMatrix.filterColumns(innerMatrix,
        n -> n == 0 || n == 3);

    realMatrix.setEntry(1, 0, 10.0);
    Assertions.assertEquals(10.0, realMatrix.getEntry(1, 0));
    Assertions.assertEquals(10.0, innerMatrix.getEntry(1, 1));

    for (int row = 0; row < realMatrix.getRowDimension(); row++) {
      for (int col = 0; col < realMatrix.getColumnDimension(); col++) {
        realMatrix.setEntry(row, col, -1.0);
      }
    }

    for (int col=0; col<innerMatrix.getColumnDimension(); col++) {
      if (col != 0 && col != 3) {
        for (int row = 0; row<innerMatrix.getRowDimension(); row++) {
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

    final RealMatrix innerMatrix = MatrixUtils.createRealMatrix(data).transpose();
    // Filter even rows
    final RealMatrix realMatrix = ColumnFilteredRealMatrix.filterColumns(innerMatrix,
        n -> n%2 == 0);

    final RealMatrix realMatrix2 = TestUtil.serializeAndRecover(realMatrix);
    Assertions.assertNotSame(realMatrix, realMatrix2);
    Assertions.assertEquals(realMatrix, realMatrix2);
  }
}