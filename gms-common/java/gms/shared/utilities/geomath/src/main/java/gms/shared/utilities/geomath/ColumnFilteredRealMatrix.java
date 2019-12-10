package gms.shared.utilities.geomath;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import org.apache.commons.lang3.Validate;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * A {@code RealMatrix} subclass which wraps another {@code RealMatrix} in
 * order to contain a subset of the inner matrix's columns.
 */
public class ColumnFilteredRealMatrix extends AbstractRealMatrix implements Serializable {

  private final RealMatrix innerMatrix;
  private final int colDimension;
  private final int[] colIndices;
  private final BitSet colInclusionBits;

  /**
   * Factory method for filtering columns by column number.
   * @param matrix
   * @param exclusionPredicate columns of the wrapper matrix which test true
   *   by the predicate are excluded from the result matrix.
   * @return
   */
  public static ColumnFilteredRealMatrix filterColumns(
      final RealMatrix matrix,
      final IntPredicate exclusionPredicate) {
    Validate.notNull(matrix);
    Validate.notNull(exclusionPredicate);

    BitSet exclusionBits = new BitSet(matrix.getColumnDimension());
    for (int col = 0; col < matrix.getColumnDimension(); col++) {
      if (exclusionPredicate.test(col)) {
        exclusionBits.set(col);
      }
    }

    return new ColumnFilteredRealMatrix(matrix, exclusionBits);
  }

  /**
   * Factory method for including columns by column number.
   * @param matrix
   * @param inclusionPredicate columns of the wrapper matrix which test true
   *   by the predicate are included in the result matrix.
   * @return
   */
  public static ColumnFilteredRealMatrix includeColumns(
      final RealMatrix matrix,
      final IntPredicate inclusionPredicate) {
    return filterColumns(matrix, inclusionPredicate.negate());
  }

  /**
   * Factory method for filtering columns by values in the columns.
   * @param matrix
   * @param exclusionPredicate columns of the wrapper matrix with values which test true
   *   by the predicate are excluded from the result matrix.
   * @param all if true, then all values on a column must test true by the
   *   predicate for the row to be excluded from the result. If false, the
   *   column is excluded if any values test true.
   * @return
   */
  public static ColumnFilteredRealMatrix filterColumnsByValue(
      final RealMatrix matrix,
      final DoublePredicate exclusionPredicate,
      final boolean all
  ) {

    Validate.notNull(matrix);
    Validate.notNull(exclusionPredicate);

    Predicate<RealVector> colPredicate = cv -> {
      final int dim = cv.getDimension();
      for (int i=0; i<dim; i++) {
        boolean b = exclusionPredicate.test(cv.getEntry(i));
        if (all) {
          if (!b) {
            return false;
          }
        } else {
          if (b) {
            return true;
          }
        }
      }
      return all;
    };

    BitSet bits = new BitSet(matrix.getColumnDimension());
    for (int col=0; col<matrix.getColumnDimension(); col++) {
      if (colPredicate.test(matrix.getColumnVector(col))) {
        bits.set(col);
      }
    }

    return new ColumnFilteredRealMatrix(matrix, bits);
  }

  /**
   * Factory method for including columns by values in the columns.
   * @param matrix
   * @param inclusionPredicate columns of the wrapper matrix with values which test true
   *   by the predicate are included in the result matrix.
   * @param all if true, then all values on a column must test true by the
   *   predicate for the column to be included in the result. If false, the
   *   column is included if any values test true.
   * @return
   */
  public static ColumnFilteredRealMatrix includeColumnsByValue(
      final RealMatrix matrix,
      final DoublePredicate inclusionPredicate,
      final boolean all
  ) {
    return filterColumnsByValue(matrix, inclusionPredicate.negate(), !all);
  }

  /**
   * Constructor
   * @param innerMatrix
   * @param colExclusionBits set bits identify the columns of innerMatrix to
   *   be included from the result matrix.
   */
  public ColumnFilteredRealMatrix(final RealMatrix innerMatrix, final BitSet colExclusionBits) {
    Validate.notNull(innerMatrix);

    this.innerMatrix = innerMatrix;

    final int innerColumns = innerMatrix.getColumnDimension();

    // Create another BitSet with all bits from 0 - (dim - 1) set.
    this.colInclusionBits = new BitSet(innerColumns);
    this.colInclusionBits.set(0, innerColumns);

    if (colExclusionBits != null) {
      for (int i=colExclusionBits.nextSetBit(0); i>=0 && i<innerColumns;
          i = colExclusionBits.nextSetBit(i+1)) {
        if (i < innerColumns) {
          this.colInclusionBits.clear(i);
        }
      }
    }

    this.colDimension = colInclusionBits.cardinality();
    this.colIndices = new int[this.colDimension];

    int n = 0;
    for (int i=colInclusionBits.nextSetBit(0);
        i>=0;
        i=colInclusionBits.nextSetBit(i+1)) {
      this.colIndices[n++] = i;
    }
  }

  /**
   * Constructor which includes all columns of the inner matrix.
   * @param innerMatrix
   */
  public ColumnFilteredRealMatrix(final RealMatrix innerMatrix) {
    this(innerMatrix, null);
  }

  /**
   * Creates a new instance using that same inner matrix as this instance,
   * but excluding the same columns as the specified other matrix.
   * @param otherMatrix
   * @return a new instance wrapping the same inner matrix as this instance, but
   *   excluding the same columns as otherMatrix.
   * @throws DimensionMismatchException if
   *   {@code this.getInnerColumnDimension() != otherMatrix.getInnerColumnDimension()}
   */
  public ColumnFilteredRealMatrix excludeSameColumns(final ColumnFilteredRealMatrix otherMatrix) {
    if (this.innerMatrix.getColumnDimension() != otherMatrix.getInnerColumnDimension()) {
      throw new DimensionMismatchException(this.innerMatrix.getColumnDimension(),
          otherMatrix.getInnerColumnDimension());
    }
    return includeColumns(this.innerMatrix, dim -> otherMatrix.isIncluded(dim));
  }

  /**
   * Get the column dimension of the inner matrix. The following always holds:
   * {@code this.getInnerColumnDimension() >= this.getColumnDimension()}
   * {@code this.getInnerMatrix().getRowDimension() == this.getRowDimension()};
   * @return
   */
  public int getInnerColumnDimension() {
    return this.innerMatrix.getColumnDimension();
  }

  /**
   * Returns whether or not the specified dimension of the inner matrix is included in
   * this matrix instance.
   * @param innerColumnDimension
   * @return
   */
  public boolean isIncluded(int innerColumnDimension) {
    if (innerColumnDimension < 0 || innerColumnDimension >= this.innerMatrix.getColumnDimension()) {
      throw new OutOfRangeException(
          innerColumnDimension, 0, this.innerMatrix.getColumnDimension() - 1);
    }
    return this.colInclusionBits.get(innerColumnDimension);
  }

  /**
   * Get the column indexes of the inner matrix included in this matrix.
   * @return an array of ints, {@code this.getDimension()} in length. The indexes will be in
   *   sorted order.
   */
  public int[] includedInnerColumns() {
    int[] result = new int[this.colDimension];
    int n=0;
    for (int c=0; c<this.getInnerColumnDimension(); c++) {
      if (isIncluded(c)) {
        result[n++] = c;
      }
    }
    return result;
  }

  /**
   * Get the inner matrix.
   * @return
   */
  public RealMatrix getInnerMatrix() {
    return this.innerMatrix;
  }

  // Used by copy()
  private ColumnFilteredRealMatrix(
      final RealMatrix innerMatrix,
      final int colDimension,
      final int[] colIndices,
      final BitSet colInclusionBits
  ) {
    this.innerMatrix = innerMatrix;
    this.colDimension = colDimension;
    this.colIndices = colIndices;
    this.colInclusionBits = colInclusionBits;
  }

  @Override
  public int getRowDimension() {
    return this.innerMatrix.getRowDimension();
  }

  @Override
  public int getColumnDimension() {
    return this.colDimension;
  }

  @Override
  public RealMatrix createMatrix(int rowDimension, int columnDimension)
      throws NotStrictlyPositiveException {
    // Return an instance of this class that includes all rows of the inner matrix.
    return new ColumnFilteredRealMatrix(
        MatrixUtils.createRealMatrix(rowDimension, columnDimension),
        null);
  }

  @Override
  public RealMatrix copy() {

    BitSet inclusionBitsCopy = new BitSet(this.colDimension);
    inclusionBitsCopy.or(this.colInclusionBits);

    return new ColumnFilteredRealMatrix(
        this.innerMatrix.copy(),
        this.colDimension,
        Arrays.copyOf(this.colIndices, this.colIndices.length),
        inclusionBitsCopy);
  }

  @Override
  public double getEntry(int row, int column) throws OutOfRangeException {
    MatrixUtils.checkMatrixIndex(this, row, column);
    return innerMatrix.getEntry(row, this.colIndices[column]);
  }

  @Override
  public void setEntry(int row, int column, double value) throws OutOfRangeException {
    MatrixUtils.checkMatrixIndex(this, row, column);
    this.innerMatrix.setEntry(row, this.colIndices[column], value);
  }
}
