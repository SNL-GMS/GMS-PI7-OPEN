package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Data class that represents a Waveform which is more generally known as a timeseries.
 */
@AutoValue
public abstract class Waveform extends Timeseries {

  /**
   * Creates a waveform by providing all arguments, except endTime which is computed in the base
   * class.
   */
  public static Waveform from(Instant startTime, double sampleRate,
      long sampleCount, double[] values) {
    Preconditions.checkArgument(sampleRate > 0.0, "Cannot create"
        + "Waveform with negative sample rate");
    Preconditions.checkArgument(sampleCount > 0, "Cannot create"
        + "Waveform with negative sample count");
    Preconditions.checkArgument(values.length == 0 || values.length == sampleCount,
        "Sample Count must match values count when values are provided");
    return new AutoValue_Waveform(Type.WAVEFORM, startTime, sampleRate, sampleCount, values);
  }

  /**
   * Creates a Waveform with its values.
   */
  public static Waveform withValues(Instant startTime, double sampleRate,
      double[] values) {
    return from(startTime, sampleRate, values.length, values);
  }

  /**
   * Creates a waveform that omits values.
   */
  public static Waveform withoutValues(Instant startTime, double sampleRate, long sampleCount) {
    return from(startTime, sampleRate, sampleCount, new double[]{});
  }

  /**
   * The time at which the Waveform beings.
   */
  public abstract Instant getStartTime();

  /**
   * The sample rate (a measurement of how many data points there are per unit * time)
   */
  public abstract double getSampleRate();

  /**
   * The number of samples in this waveform.
   */
  public abstract long getSampleCount();

  /**
   * The data points of this Waveform.
   */
  public abstract double[] getValues();

  /**
   * Gets the first value
   *
   * @return double
   */
  public double getFirstSample() {
    Preconditions
        .checkState(getValues().length != 0, "Cannot get first sample because values are empty");

    return getValues()[0];
  }

  /**
   * Gets the last value
   *
   * @return double
   */
  public double getLastSample() {
    Preconditions
        .checkState(getValues().length != 0, "Cannot get last sample because values are empty");

    return getValues()[(int) getSampleCount() - 1];
  }

  /**
   * Trims out data from this waveform and removes a new one that only contains the data within the
   * specified time bounds.
   *
   * @param start the start of the time bound
   * @param end the end of the time bound
   * @return A new waveform with data only within the given time range
   * @throws NullPointerException if start or end are null
   * @throws IllegalArgumentException if start is not before end, or if the requested time range is
   * completely outside the range of data this waveform has.
   */
  public Waveform trim(Instant start, Instant end) {
    Validate.notNull(start);
    Validate.notNull(end);
    Validate.isTrue(this.intersects(start, end),
        String.format("Cannot trim waveform to time range it doesn't have; current range is "
                + "[%s, %s], requested is [%s, %s]", this.getStartTime(), this.getEndTime(),
            start, end));
    if (getStartTime().equals(start) && getEndTime().equals(end)) {
      return this;  // waveform is already exactly trimmed
    }
    Instant newStart = getStartTime().isAfter(start) ? getStartTime() : start;
    Instant newEnd = getEndTime().isBefore(end) ? getEndTime() : end;
    return window(newStart, newEnd);
  }

  /**
   * Windows this waveform to be within the specified time bounds.
   *
   * @param start the start of the time bound
   * @param end the end of the time bound
   * @return A few possibilities: 1.) A new Waveform that contains the narrowed (windowed) set of
   * data from this Waveform. It will have updated start/end times and sample count.  Note that the
   * start/end times of the new Waveform may not equal the requested start/end times, as they
   * reflect where data actually begins and ends. 2.) This exact Waveform if this ones' start/end
   * times are equal to requested range. 3.) A new Waveform with empty points (sampleRate=0,
   * sampleCount=0, values=[]) if the requested range is completely outside the range of this
   * Waveform.
   * @throws NullPointerException if start or end are null
   * @throws IllegalArgumentException if start is not before end.
   */
  public Waveform window(Instant start, Instant end) {
    Validate.notNull(start);
    Validate.notNull(end);
    Validate.isTrue(!start.isBefore(this.getStartTime()),
        "new start is before startTime of this waveform");
    Validate.isTrue(!end.isAfter(this.getEndTime()),
        "new end is after endTime of this waveform");
    Validate.isTrue(!start.isAfter(end), "new start must be <= new end");

    // Already exactly windowed?  Great, just return this!
    if (this.getStartTime().equals(start) && this.getEndTime().equals(end)) {
      return this;
    }

    Triple<Integer, Integer, Long> newIndicesAndSampleCount = this
        .computeIndicesAndSampleCount(start, end);
    Instant newStart = computeSampleTime(newIndicesAndSampleCount.getLeft());

    // If Waveform doesn't have samples, return new waveform with the new time range.
    if (this.getValues().length == 0) {
      return Waveform
          .withoutValues(newStart, this.getSampleRate(), newIndicesAndSampleCount.getRight());
    }
    // copy values into new array; adding one to upper index because Arrays.copyOfRange is end-exclusive
    // but the upper index is to be included.
    double[] newValues = Arrays.copyOfRange(this.getValues(),
        newIndicesAndSampleCount.getLeft(), newIndicesAndSampleCount.getMiddle() + 1);
    return Waveform.withValues(newStart, getSampleRate(), newValues);
  }

  /**
   * Computes the new lower/upper indices (zero-indexed) of the Waveform and new sample count given
   * a new start and end time range.
   *
   * @param newStart the new start time
   * @param newEnd the new end time
   * @return a 3-tuple of (lowerIndex, upperIndex, sampleCount).  lowerIndex/upperIndex are
   * zero-indexed (array indices), sampleCount is one-indexed.
   */
  private Triple<Integer, Integer, Long> computeIndicesAndSampleCount(Instant newStart,
      Instant newEnd) {
    Validate.isTrue(!newStart.isBefore(this.getStartTime()),
        "newStart is before startTime");
    Validate.isTrue(!newEnd.isAfter(this.getEndTime()),
        "newEnd is after endTime");
    Validate.isTrue(!newStart.isAfter(newEnd), "newStart must be <= newEnd");

    final double samplesPerMilli = getSampleRate() / 1000.0;
    final Duration fromStartToNewStart = Duration.between(this.getStartTime(), newStart);
    final Duration fromNewEndToEnd = Duration.between(newEnd, this.getEndTime());
    int lowerIndex = (int) Math.ceil(fromStartToNewStart.toMillis() * samplesPerMilli);
    final long samplesRemovedRight = (long) Math.ceil(fromNewEndToEnd.toMillis() * samplesPerMilli);
    int upperIndex = (int) (getSampleCount() - 1 - samplesRemovedRight);
    Validate.isTrue(lowerIndex >= 0 && lowerIndex < getSampleCount(),
        String.format("Lower index must in range[0, %d) but was %d",
            getSampleCount(), lowerIndex));
    Validate.isTrue(upperIndex >= 0 && upperIndex < getSampleCount(),
        String.format("Upper index must be in range [0, %d) but was %d",
            getSampleCount(), upperIndex));
    Validate.isTrue(lowerIndex <= upperIndex,
        "lower index must be less than upper index");
    final long newSampleCount = getSampleCount() - (lowerIndex + samplesRemovedRight);
    return Triple.of(lowerIndex, upperIndex, newSampleCount);
  }

  private boolean intersects(Instant start, Instant end) {
    return (getEndTime().equals(start) || getEndTime().isAfter(start)) &&
        (getStartTime().equals(end) || getStartTime().isBefore(end));
  }

}
