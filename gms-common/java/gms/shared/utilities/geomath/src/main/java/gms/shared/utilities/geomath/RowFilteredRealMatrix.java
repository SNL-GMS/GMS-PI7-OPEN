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
 * order to contain a subset of the inner matrix's rows.
 */
public class RowFilteredRealMatrix extends AbstractRealMatrix implements Serializable {

  private final RealMatrix innerMatrix;
  private final int rowDimension;
  private final int[] rowIndices;
  private final BitSet rowInclusionBits;

  /**
   * Factory method for filtering rows by row number.
   * @param matrix
   * @param exclusionPredicate rows of the wrapper matrix which test true
   *   by the predicate are excluded from the result matrix.
   * @return
   */
  public static RowFilteredRealMatrix filterRows(
      final RealMatrix matrix,
      final IntPredicate exclusionPredicate) {
    Validate.notNull(matrix);
    Validate.notNull(exclusionPredicate);

    BitSet exclusionBits = new BitSet(matrix.getRowDimension());
    for (int row = 0; row < matrix.getRowDimension(); row++) {
      if (exclusionPredicate.test(row)) {
        exclusionBits.set(row);
      }
    }

    return new RowFilteredRealMatrix(matrix, exclusionBits);
  }

  /**
   * Factory method for including rows by row number.
   * @param matrix
   * @param inclusionPredicate rows of the wrapper matrix which test true
   *   by the predicate are included in the result matrix.
   * @return
   */
  public static RowFilteredRealMatrix includeRows(
      final RealMatrix matrix,
      final IntPredicate inclusionPredicate) {
    return filterRows(matrix, inclusionPredicate.negate());
  }


  /**
   * Factory method for filtering rows by values in the rows.
   * @param matrix
   * @param exclusionPredicate rows of the wrapper matrix which test true
   *   by the predicate are excluded from the result matrix.
   * @param all if true, then all values on a row must test true by the
   *   predicate for the row to be excluded from the result. If false, the
   *   row is excluded if any values test true.
   * @return
   */
  public static RowFilteredRealMatrix filterRowsByValue(
      final RealMatrix matrix,
      final DoublePredicate exclusionPredicate,
      final boolean all
  ) {

    Validate.notNull(matrix);
    Validate.notNull(exclusionPredicate);

    Predicate<RealVector> rowPredicate = rv -> {
      final int dim = rv.getDimension();
      for (int i=0; i<dim; i++) {
        boolean b = exclusionPredicate.test(rv.getEntry(i));
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

    BitSet bits = new BitSet(matrix.getRowDimension());
    for (int row=0; row<matrix.getRowDimension(); row++) {
      if (rowPredicate.test(matrix.getRowVector(row))) {
        bits.set(row);
      }
    }

    return new RowFilteredRealMatrix(matrix, bits);
  }

  /**
   * Factory method for including rows by values in the rows.
   * @param matrix
   * @param inclusionPredicate rows of the wrapper matrix which test true
   *   by the predicate are included in the result matrix.
   * @param all if true, then all values on a row must test true by the
   *   predicate for the row to be included in the result. If false, the
   *   row is included if any values test true.
   * @return
   */
  public static RowFilteredRealMatrix includeRowsByValue(
      final RealMatrix matrix,
      final DoublePredicate inclusionPredicate,
      final boolean all
  ) {
    return filterRowsByValue(matrix, inclusionPredicate.negate(), !all);
  }

  /**
   * Constructor
   * @param innerMatrix
   * @param rowExclusionBits set bits identify the rows of innerMatrix to
   *   be included from the result matrix.
   */
  public RowFilteredRealMatrix(final RealMatrix innerMatrix, final BitSet rowExclusionBits) {
    Validate.notNull(innerMatrix);

    this.innerMatrix = innerMatrix;

    final int innerRows = innerMatrix.getRowDimension();

    // Create another BitSet with all bits from 0 - (dim - 1) set.
    this.rowInclusionBits = new BitSet(innerRows);
    this.rowInclusionBits.set(0, innerRows);

    if (rowExclusionBits != null) {
      for (int i=rowExclusionBits.nextSetBit(0); i>=0 && i<innerRows;
          i = rowExclusionBits.nextSetBit(i+1)) {
        if (i < innerRows) {
          this.rowInclusionBits.clear(i);
        }
      }
    }

    this.rowDimension = rowInclusionBits.cardinality();
    this.rowIndices = new int[this.rowDimension];

    int n = 0;
    for (int i=rowInclusionBits.nextSetBit(0);
        i>=0;
        i=rowInclusionBits.nextSetBit(i+1)) {
      this.rowIndices[n++] = i;
    }
  }

  /**
   * Constructor which includes all rows of the inner matrix.
   * @param innerMatrix
   */
  public RowFilteredRealMatrix(final RealMatrix innerMatrix) {
    this(innerMatrix, null);
  }

  /**
   * Creates a new instance using that same inner matrix as this instance,
   * but excluding the same rows as the specified other matrix.
   * @param otherMatrix
   * @return a new instance wrapping the same inner matrix as this instance, but
   *   excluding the same rows as otherMatrix.
   * @throws DimensionMismatchException if
   *   {@code this.getInnerRowDimension() != otherMatrix.getInnerRowDimension()}
   */
  public RowFilteredRealMatrix excludeSameRows(final RowFilteredRealMatrix otherMatrix) {
    if (this.innerMatrix.getRowDimension() != otherMatrix.getInnerRowDimension()) {
      throw new DimensionMismatchException(this.innerMatrix.getRowDimension(),
          otherMatrix.getInnerRowDimension());
    }
    return includeRows(this.innerMatrix, dim -> otherMatrix.isIncluded(dim));
  }

  /**
   * Creates a new instance using that same inner matrix as this instance,
   * but excluding the same rows as the specified other vector
   * @param v vector identifying rows to exclude
   * @return a new instance wrapping the same inner matrix as this instance, but
   *   excluding the same rows as vector, v.
   */
  public RowFilteredRealMatrix excludeSameRows(final FilteredRealVector v) {
    if (this.innerMatrix.getRowDimension() != v.getInnerVectorDimension()) {
      throw new DimensionMismatchException(this.innerMatrix.getRowDimension(), v.getInnerVectorDimension());
    }
    return includeRows(this.innerMatrix, dim -> v.isIncluded(dim));
  }

  /**
   * Get the row dimension of the inner matrix. The following always holds:
   * {@code this.getInnerRowDimension() >= this.getRowDimension()}
   * {@code this.getInnerMatrix().getRowDimension() == this.getInnerRowDimension()};
   * @return
   */
  public int getInnerRowDimension() {
    return this.innerMatrix.getRowDimension();
  }

  /**
   * Returns a BitSet indicating which rows of the underlying matrix are excluded
   * @return BitSet indicating excluded rows
   */
  public BitSet getExclusionBitSet() {
    BitSet exclusionBits = (BitSet)(this.rowInclusionBits.clone());
    exclusionBits.flip(0, exclusionBits.length());
    return exclusionBits;
  }

  /**
   * Returns whether or not the specified dimension of the inner matrix is included in
   * this matrix instance.
   * @param innerRowDimension
   * @return
   */
  public boolean isIncluded(int innerRowDimension) {
    if (innerRowDimension < 0 || innerRowDimension >= this.innerMatrix.getRowDimension()) {
      throw new OutOfRangeException(
          innerRowDimension, 0, this.innerMatrix.getRowDimension() - 1);
    }
    return this.rowInclusionBits.get(innerRowDimension);
  }

  /**
   * Get the inner matrix.
   * @return
   */
  public RealMatrix getInnerMatrix() {
    return this.innerMatrix;
  }

  // Used by copy()
  private RowFilteredRealMatrix(
      final RealMatrix innerMatrix,
      final int rowDimension,
      final int[] rowIndices,
      final BitSet rowInclusionBits
  ) {
    this.innerMatrix = innerMatrix;
    this.rowDimension = rowDimension;
    this.rowIndices = rowIndices;
    this.rowInclusionBits = rowInclusionBits;
  }

  /**
   * Returns the requested column as a FilteredRealVector
   * @param columnIndex index of requested column
   * @return column of matrix as a FilteredRealVector
   * @throws OutOfRangeException
   */
  public FilteredRealVector getColumnVector(final int columnIndex) throws OutOfRangeException {
    // TODO: write unit test for this method
    final int maxIndex = innerMatrix.getColumnDimension() - 1;
    if (columnIndex < 0 ||  maxIndex < columnIndex) {
      throw new OutOfRangeException(columnIndex, 0, maxIndex);
    }
    BitSet exclusionBits = (BitSet) (rowInclusionBits.clone());
    exclusionBits.flip(0, rowInclusionBits.length());
    return new FilteredRealVector(innerMatrix.getColumnVector(columnIndex), exclusionBits);
  }

  @Override
  public int getRowDimension() {
    return rowDimension;
  }

  @Override
  public int getColumnDimension() {
    return this.innerMatrix.getColumnDimension();
  }

  @Override
  public RealMatrix createMatrix(int rowDimension, int columnDimension)
      throws NotStrictlyPositiveException {
    // Return an instance of this class that includes all rows of the inner matrix.
    return new RowFilteredRealMatrix(
        MatrixUtils.createRealMatrix(rowDimension, columnDimension),
        null);
  }

  @Override
  public RealMatrix copy() {

    BitSet inclusionBitsCopy = new BitSet(this.rowDimension);
    inclusionBitsCopy.or(this.rowInclusionBits);

    return new RowFilteredRealMatrix(
        this.innerMatrix.copy(),
        this.rowDimension,
        Arrays.copyOf(this.rowIndices, this.rowIndices.length),
        inclusionBitsCopy);
  }

  @Override
  public double getEntry(int row, int column) throws OutOfRangeException {
    MatrixUtils.checkMatrixIndex(this, row, column);
    return innerMatrix.getEntry(this.rowIndices[row], column);
  }

  @Override
  public void setEntry(int row, int column, double value) throws OutOfRangeException {
    MatrixUtils.checkMatrixIndex(this, row, column);
    this.innerMatrix.setEntry(this.rowIndices[row], column, value);
  }
}
