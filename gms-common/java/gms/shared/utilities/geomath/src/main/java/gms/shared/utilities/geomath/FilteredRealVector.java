package gms.shared.utilities.geomath;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import org.apache.commons.lang3.Validate;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

/**
 * A {@code RealVector} subclass that wraps another vector filtering out some of its elements.
 */
public class FilteredRealVector extends RealVector implements Serializable {

  private final RealVector innerVector;
  private final int dimension;
  private final int[] indices;
  private final BitSet inclusionBits;

  /**
   * Getter method for inclusion bit set
   * @return inclusion bit set
   */
  public BitSet getInclusionBits() { return inclusionBits; }

  /**
   * Factory method which uses a predicate to exclude dimensions of the source vector by filtering
   * on dimension.
   * @param vector the source vector
   * @param exclusionPredicate all dimensions of the source vector for which this predicate
   *   returns true are excluded from the result vector.
   * @return
   */
  public static FilteredRealVector filter(
      final RealVector vector,
      final IntPredicate exclusionPredicate) {

    Validate.notNull(vector);
    Validate.notNull(exclusionPredicate);

    BitSet exclusionBits = new BitSet(vector.getDimension());
    for (int i=0; i<vector.getDimension(); i++) {
      if (exclusionPredicate.test(i)) {
        exclusionBits.set(i);
      }
    }

    return new FilteredRealVector(vector, exclusionBits);
  }

  /**
   * Factory method which uses a predicate to include dimensions of the source vector.
   * @param vector the source vector
   * @param inclusionPredicate all dimensions of the source vector for which this predicate
   *   returns true are included in the result vector.
   * @return
   */
  public static FilteredRealVector include(
      final RealVector vector,
      final IntPredicate inclusionPredicate
  ) {
    return filter(vector, inclusionPredicate.negate());
  }

  /**
   * Factory method which uses a predicate to exclude dimensions of the source vector by filtering
   * on value.
   * @param vector the source vector
   * @param exclusionPredicate all dimensions corresponding to source vector values for which
   *   this predicate returns true are exluded from the result vector.
   * @return
   */
  public static FilteredRealVector filterValues(
      final RealVector vector,
      final DoublePredicate exclusionPredicate
  ) {

    Validate.notNull(vector);
    Validate.notNull(exclusionPredicate);

    BitSet exclusionBits = new BitSet(vector.getDimension());
    for (int i=0; i<vector.getDimension(); i++) {
      if (exclusionPredicate.test(vector.getEntry(i))) {
        exclusionBits.set(i);
      }
    }

    return new FilteredRealVector(vector, exclusionBits);
  }

  /**
   * Factory method which uses a predicate to include dimensions of the source vector by testing
   * values.
   * @param vector the source vector
   * @param inclusionPredicate all dimensions corresponding to source vector values for which
   *   this predicate returns true are included in the result vector.
   * @return
   */
  public static FilteredRealVector includeValues(
      final RealVector vector,
      final DoublePredicate inclusionPredicate
  ) {
    return filterValues(vector, inclusionPredicate.negate());
  }

  /**
   * Constructor
   * @param innerVector the source vector
   * @param exclusionBits set bits determine which dimensions of the source vector to exclude
   *   from the vector instance created by this constructor.
   */
  public FilteredRealVector(final RealVector innerVector, final BitSet exclusionBits) {

    Objects.requireNonNull(innerVector);

    this.innerVector = innerVector;

    // Create another BitSet with all bits from 0 - (dim - 1) set.
    this.inclusionBits = new BitSet(innerVector.getDimension());
    this.inclusionBits.set(0, innerVector.getDimension());

    final int innerDim = innerVector.getDimension();

    if (exclusionBits != null) {
      for (int i=exclusionBits.nextSetBit(0); i>=0 && i<innerDim;
        i = exclusionBits.nextSetBit(i+1)) {
        if (i < innerDim) {
          inclusionBits.clear(i);
        }
      }
    }

    this.dimension = inclusionBits.cardinality();
    this.indices = new int[this.dimension];

    int n = 0;
    for (int i=inclusionBits.nextSetBit(0);
        i>=0;
        i=inclusionBits.nextSetBit(i+1)) {
      indices[n++] = i;
    }

  }

  /**
   * Constructs an instance that includes all dimensions of the inner vector.
   * @param innerVector
   */
  public FilteredRealVector(final RealVector innerVector) {
    this(innerVector, null);
  }

  /**
   * Creates a new instance by excluding the provided dimension indices from this instance.
   * @param indices
   * @return
   */
  public FilteredRealVector filter(int... indices) {
    BitSet exclusionBits = new BitSet(this.innerVector.getDimension());

    exclusionBits.set(0, this.innerVector.getDimension());
    for (int index: this.indices) {
      exclusionBits.clear(index);
    }
    for (int index: indices) {
      exclusionBits.set(this.indices[index]);
    }
    return new FilteredRealVector(this.innerVector, exclusionBits);
  }

  /**
   * Returns a new instance that filters the same dimensions as another
   * {@code FilteredRealVector}. The inner vector of the new instance is the same as
   * for this instance. The inner vector of {@code otherVector} may be different than
   * that of this instance, but it must have the same dimension.
   * @param otherVector
   * @return a new instance with the same dimension as {@code otherVector}
   * @throws DimensionMismatchException if
   *   {@code this.getInnerVectorDimension() != otherVector.getInnerVectorDimension()}
   */
  public FilteredRealVector excludeSameDimensions(final FilteredRealVector otherVector) {
    if (this.innerVector.getDimension() != otherVector.getInnerVectorDimension()) {
      throw new DimensionMismatchException(this.innerVector.getDimension(),
          otherVector.getInnerVectorDimension());
    }
    return include(this.innerVector, dim -> otherVector.isIncluded(dim));
  }

  /**
   * Tests whether two instances filter on the same dimensions.
   * They must have the same inner vector dimensions.
   * @param vector1
   * @param vector2
   * @return
   * @throws DimensionMismatchException if the inner vector dimensions do
   *   not match.
   */
  public static boolean filtersSameDimensions(
      final FilteredRealVector vector1,
      final FilteredRealVector vector2
  ) throws DimensionMismatchException {
    if (vector1.getInnerVectorDimension() != vector2.getInnerVectorDimension()) {
      throw new DimensionMismatchException(vector1.getInnerVectorDimension(),
          vector2.getInnerVectorDimension());
    }
    for (int dim=0; dim<vector1.getInnerVectorDimension(); dim++) {
      if (vector1.isIncluded(dim) != vector2.isIncluded(dim)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns a new instance that filters the same dimensions as the rows filtered
   * by a {@code RowFilteredRealMatrix}.
   * The inner vector of the new instance is the same as
   * for this instance.
   * @param matrix
   * @return a new instance with the same dimension as the row dimension of matrix.
   * @throws DimensionMismatchException if
   *   {@code this.getInnerVectorDimension() != matrix.getInnerRowDimension()}
   */
  public FilteredRealVector excludeSameDimensions(final RowFilteredRealMatrix matrix) {
    if (this.innerVector.getDimension() != matrix.getInnerRowDimension()) {
      throw new DimensionMismatchException(this.innerVector.getDimension(),
          matrix.getInnerRowDimension());
    }
    return include(this.innerVector, dim -> matrix.isIncluded(dim));
  }

  /**
   * Private constructor used by copy()
   * @param innerVector
   * @param dimension
   * @param indices
   * @param inclusionBits
   */
  private FilteredRealVector(
      final RealVector innerVector,
      final int dimension,
      final int[] indices,
      final BitSet inclusionBits) {
    this.innerVector = innerVector;
    this.dimension = dimension;
    this.indices = indices;
    this.inclusionBits = inclusionBits;
  }

  /**
   * Compute the sum of this vector and {@code v}.
   * Returns a new vector. Does not change instance data.
   *
   * @param v Vector to be added.
   * @return {@code this} + {@code v}.
   * @throws DimensionMismatchException if {@code v} is not the same size as
   * {@code this} vector.
   */
  public FilteredRealVector add(RealVector v) throws DimensionMismatchException {
    checkVectorDimensions(v);
    RealVector resultInner = this.innerVector.copy();
    for (int outerDim=0; outerDim<v.getDimension(); outerDim++) {
      int innerDim = this.indices[outerDim];
      resultInner.setEntry(innerDim,
          resultInner.getEntry(innerDim) + v.getEntry(outerDim));
    }
    return FilteredRealVector.include(resultInner, n -> this.isIncluded(n));
  }

  /**
   * Subtract {@code v} from this vector.
   * Returns a new vector. Does not change instance data.
   *
   * @param v Vector to be subtracted.
   * @return {@code this} - {@code v}.
   * @throws DimensionMismatchException if {@code v} is not the same size as
   * {@code this} vector.
   */
  public FilteredRealVector subtract(RealVector v) throws DimensionMismatchException {
    checkVectorDimensions(v);
    RealVector resultInner = this.innerVector.copy();
    for (int outerDim=0; outerDim<v.getDimension(); outerDim++) {
      int innerDim = this.indices[outerDim];
      double tmp1 = resultInner.getEntry(innerDim);
      double tmp2 = v.getEntry(outerDim);
      double tmp3 = tmp1 - tmp2;
      resultInner.setEntry(innerDim, tmp3);
    }
    return FilteredRealVector.include(resultInner, n -> this.isIncluded(n));
  }

  /**
   * Returns the inner vector.
   * @return
   */
  public RealVector getInnerVector() {
    return this.innerVector;
  }

  /**
   * Returns the number of dimensions of the wrapped vector.
   * @return
   */
  public int getInnerVectorDimension() {
    return this.innerVector.getDimension();
  }

  /**
   * Returns whether or not the specified dimension of the inner vector is
   * included in this vector.
   * @param innerDimension
   * @return
   * @throws OutOfRangeException if innerDimension is not in [0 - (this.getInnerDimension() - 1)]
   */
  public boolean isIncluded(int innerDimension) throws OutOfRangeException {
    if (innerDimension < 0 || innerDimension >= this.innerVector.getDimension()) {
      throw new OutOfRangeException(LocalizedFormats.INDEX,
          innerDimension, 0, this.innerVector.getDimension() - 1);
    }
    return this.inclusionBits.get(innerDimension);
  }

  /**
   * Returns copy of this vector.
   * @return copy of this vector.
   */
  public FilteredRealVector copyFilteredRealVector() {
    BitSet inclusionBits = new BitSet(this.dimension);
    inclusionBits.or(this.inclusionBits);
    return new FilteredRealVector(
        this.innerVector.copy(),
        this.dimension,
        Arrays.copyOf(this.indices, this.indices.length),
        inclusionBits);
  }

  /**
   * Element by element multiply on this vector
   * @param v  vector to multiply with this vector
   * @throws DimensionMismatchException
   */
  public void ebeMultiply(FilteredRealVector v) throws DimensionMismatchException {
    checkVectorDimensions(v);
    for (int i=0; i<innerVector.getDimension(); i++) {
      if (isIncluded(i)) {
        innerVector.setEntry(i, innerVector.getEntry(i) * v.innerVector.getEntry(i));
      }
    }
  }

  @Override
  public int getDimension() {
    return dimension;
  }

  @Override
  public double getEntry(int index) throws OutOfRangeException {
    checkIndex(index);
    return innerVector.getEntry(indices[index]);
  }

  @Override
  public void setEntry(int index, double value) throws OutOfRangeException {
    checkIndex(index);
    innerVector.setEntry(indices[index], value);
  }

  @Override
  public RealVector append(RealVector v) {
    double[] values = new double[this.dimension + v.getDimension()];
    for (int i=0; i<this.dimension; i++) {
      values[i] = this.getEntry(i);
    }
    for(int i=0, j=dimension; i<v.getDimension(); i++, j++) {
      values[j] = v.getEntry(i);
    }
    return MatrixUtils.createRealVector(values);
  }

  @Override
  public RealVector append(double d) {
    double[] values = new double[this.dimension + 1];
    for (int i=0; i<this.dimension; i++) {
      values[i] = getEntry(i);
    }
    values[this.dimension] = d;
    return MatrixUtils.createRealVector(values);
  }

  @Override
  public RealVector getSubVector(int index, int n)
      throws NotPositiveException, OutOfRangeException {
    if (n < 0) {
      throw new NotPositiveException(LocalizedFormats.NUMBER_OF_ELEMENTS_SHOULD_BE_POSITIVE, n);
    }
    checkIndex(index);
    checkIndex(index + n - 1);
    double[] values = new double[n];
    for (int i=0;i<n;i++) {
      values[i] = getEntry(index + i);
    }
    return MatrixUtils.createRealVector(values);
  }

  @Override
  public void setSubVector(int index, RealVector v) throws OutOfRangeException {
    checkIndex(index);
    checkIndex(index + v.getDimension() - 1);
    for (int i=0; i<v.getDimension(); i++) {
      setEntry(index + i, v.getEntry(i));
    }
  }

  @Override
  public boolean isNaN() {
    for (int i=0; i<this.dimension; i++) {
      if (Double.isNaN(this.getEntry(i))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isInfinite() {
    if (isNaN()) {
      return false;
    }
    for (int i=0; i<this.dimension; i++) {
      if (Double.isInfinite(this.getEntry(i))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public RealVector copy() {
    BitSet inclusionBits = new BitSet(this.dimension);
    inclusionBits.or(this.inclusionBits);
    return new FilteredRealVector(
        this.innerVector.copy(),
        this.dimension,
        Arrays.copyOf(this.indices, this.indices.length),
        inclusionBits);
  }

  @Override
  public RealVector ebeDivide(RealVector v) throws DimensionMismatchException {
    checkVectorDimensions(v);
    double[] values = new double[this.dimension];
    for (int i=0; i<this.dimension; i++) {
      values[i] = this.getEntry(i)/v.getEntry(i);
    }
    return MatrixUtils.createRealVector(values);
  }

  @Override
  public RealVector ebeMultiply(RealVector v) throws DimensionMismatchException {
    checkVectorDimensions(v);
    double[] values = new double[this.dimension];
    for (int i=0; i<this.dimension; i++) {
      values[i] = this.getEntry(i)*v.getEntry(i);
    }
    return MatrixUtils.createRealVector(values);
  }

  @Override
  public int hashCode() {
    if (isNaN()) {
      return 9;
    }
    int hc = 1;
    for (int i=0; i<this.dimension; i++) {
      long bits = Double.doubleToLongBits(this.getEntry(i));
      hc = 31*hc + (int) (bits ^ (bits >>>32));
    }
    return hc;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof RealVector) {
      RealVector other = (RealVector) o;
      if (this.dimension == other.getDimension()) {
        for (int i=0; i<this.dimension; i++) {
          double d1 = this.getEntry(i);
          double d2 = other.getEntry(i);
          if (Double.doubleToLongBits(d1) != Double.doubleToLongBits(d2)) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

}
