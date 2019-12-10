package gms.shared.mechanisms.configuration.constraints;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * Contains a range of double values and utility operations to determine wheter other double values
 * are within the range.
 */
@AutoValue
public abstract class DoubleRange {

  /**
   * Obtains a new {@link DoubleRange} with the provided bounds
   *
   * @param min minimum bound, must be <= maximum
   * @param max maximum bound, must be >= minimum
   * @return {@link DoubleRange}, not null
   * @throws IllegalArgumentException if maximum < minimum
   */
  @JsonCreator
  public static DoubleRange from(
      @JsonProperty("min") double min,
      @JsonProperty("max") double max) {

    if (max < min) {
      throw new IllegalArgumentException("Minimum must be <= maximum but " + max + " < " + min);
    }

    return new AutoValue_DoubleRange(min, max);
  }

  /**
   * Obtains a new {@link DoubleRange} with minimum bound of negative infinity and maximum bound of
   * positive infinity.
   *
   * @return {@link DoubleRange}, not null
   */
  public static DoubleRange fromInfiniteRange() {
    return DoubleRange.from(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
  }

  /**
   * Obtains a new {@link DoubleRange} with the provided lower bound and maximum bound of positive
   * infinity.
   *
   * @param min minimum bound
   * @return {@link DoubleRange}, not null
   */
  public static DoubleRange fromUnboundedMax(double min) {
    return DoubleRange.from(min, Double.POSITIVE_INFINITY);
  }

  /**
   * Obtains a new {@link DoubleRange} with minimum bound of negative infinity and provided maximum
   * bound.
   *
   * @param max @param maximum bound
   * @return {@link DoubleRange}, not null
   */
  public static DoubleRange fromUnboundedMin(double max) {
    return DoubleRange.from(Double.NEGATIVE_INFINITY, max);
  }

  public abstract double getMin();

  public abstract double getMax();

  /**
   * Determine whether the provided value is within {@link DoubleRange#getMin()} and {@link
   * DoubleRange#getMax()}.  Both bounds are inclusive.
   *
   * @param val test if this value is in this {@link DoubleRange}
   * @return true if min <= val <= max; false otherwise
   */
  public boolean contains(double val) {
    return contains(val, true, true);
  }

  /**
   * Determine whether the provided value is within {@link DoubleRange#getMin()} and {@link
   * DoubleRange#getMax()}.  includeMin and includeMax specify wheter the minimum and maximum bounds
   * are inclusive or exclusive.
   *
   * @param val test if this value is in this {@link DoubleRange}
   * @param includeMin true if the minimum bound is inclusive, false if it is exclusive
   * @param includeMax true if the maximum bound is inclusive, false if it is exclusive
   * @return true if val is in this range when using the provided boundary conditions; false
   * otherwise
   */
  public boolean contains(double val, boolean includeMin, boolean includeMax) {

    // TODO: is this operation needed?  It is only used by contains(val)
    boolean gtMin = getMin() < val;
    boolean ltMax = val < getMax();

    boolean eqMin = Double.compare(getMin(), val) == 0;
    boolean eqMax = Double.compare(getMax(), val) == 0;

    return ((includeMin && eqMin) || gtMin) && ((includeMax && eqMax) || ltMax);
  }
}
