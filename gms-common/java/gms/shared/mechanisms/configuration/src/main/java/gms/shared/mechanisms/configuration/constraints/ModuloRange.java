package gms.shared.mechanisms.configuration.constraints;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a circular range of values where the maximum value being less than the minimum values
 * implies the range spans the zero point.  For example, if the ModularRange represents hours in a
 * day then the zero point is midnight and a range of (22, 2) is range beginning at hour 22 on one
 * day and ending on hour 2 of the next day.
 *
 * @param <T> type of the range beginning and ending values
 */
public abstract class ModuloRange<T> {

  // nanoseconds might seem like overkill, however to get correct results when
  // using LocalTime.MAX, you must allow for nanoseconds
  @JsonIgnore
  private final long modulo;

  ModuloRange(long modulo) {
    this.modulo = modulo;
  }

  public abstract T getMin();

  public abstract T getMax();

  public abstract long getDuration(T min, T max);

  public abstract T toSupportedValue(T val);

  private long getClockWiseDuration(T min, T max) {
    return (getDuration(min, max) + modulo) % modulo;
  }

  public boolean contains(T val) {
    return contains(val, true, false);
  }

  public boolean contains(T val, boolean includeMin, boolean includeMax) {
    val = toSupportedValue(val);

    long myLength = getClockWiseDuration(getMin(), getMax());
    long testLength = getClockWiseDuration(getMin(), val);

    return (0 < testLength && testLength < myLength) ||
        (val.equals(getMin()) && includeMin) ||
        (val.equals(getMax()) && includeMax);
  }
}
