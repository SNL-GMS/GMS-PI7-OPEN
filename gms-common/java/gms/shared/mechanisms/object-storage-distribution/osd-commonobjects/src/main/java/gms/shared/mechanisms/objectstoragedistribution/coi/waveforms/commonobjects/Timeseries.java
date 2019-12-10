package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import java.time.Instant;

/**
 * Class which represents a time series.
 */
public abstract class Timeseries implements Comparable<Timeseries> {

  /**
   * The type of the Timeseries.
   */
  public enum Type {
    WAVEFORM, FK_SPECTRA, DETECTION_FEATURE_MAP
  }

  /**
   * Gets the start time of the timeseries
   *
   * @return the start time
   */
  public abstract Instant getStartTime();

  /**
   * Obtains the Timeseries sampleRate in units of samples per sec
   *
   * @return sample rate in samples per second
   */
  public abstract double getSampleRate();

  /**
   * Gets the number of samples in this timeseries. Note this may not match actual values present.
   *
   * @return number of samples this timeseries has (or should have)
   */
  public abstract long getSampleCount();

  /**
   * The type of the Timeseries
   */
  public abstract Type getType();

  /**
   * Lazily initialized endTime, provided as a property for performance
   */
  private Instant endTime;

  @JsonIgnore
  public Instant getEndTime() {
    if (endTime == null) {
      endTime = computeEndTime();
    }

    return endTime;
  }

  /**
   * Gets the sample period of the timeseries (time in seconds between samples)
   *
   * @return sample period (seconds)
   */
  @JsonIgnore
  public double getSamplePeriod() {
    return 1.0 / getSampleRate();
  }

  /**
   * Obtains the sampling time for index i in this timeseries
   *
   * @param i find the sampling time for the sample with this index
   * @return {@link Instant} i's sampling time, not null
   */
  public Instant computeSampleTime(long i) {
    Preconditions.checkArgument(i >= 0 && i < getSampleCount(),
        "index must be between 0 (inclusive) and sample count (exclusive)");

    //divided by a billion to get nanoseconds
    double nanosPerSample =  1E9 / getSampleRate();
    return getStartTime().plusNanos((long) (i * nanosPerSample));
  }

  /**
   * Calculate the expected end time for this timeseries.  Truncate the nanoseconds to prevent minor
   * time shifts.
   *
   * @return The end time as an Instant object.
   * @throws NullPointerException If the startTime input parameter is null.
   */
  public Instant computeEndTime() {
    return computeSampleTime(getSampleCount() - 1);
  }

  /**
   * Returns a tuple of the time range of the time series.
   *
   * @return a tuple of the earliest start time of any series in the input collection and the latest
   * end time of any series in the input collection
   * @throws IllegalArgumentException if the input collection is empty or null
   */
  public Range<Instant> computeTimeRange() {
    return Range.closed(getStartTime(), computeEndTime());
  }

  /**
   * Compares two Timeseries by their start time. However, if their times are equal than 1 is
   * returned. This is done to avoid weird behavior in collections such as SortedSet.
   *
   * @param ts the timeseries to compare this one to
   * @return int
   */
  @Override
  public int compareTo(Timeseries ts) {
    if (!getStartTime().equals(ts.getStartTime())) {
      return getStartTime().compareTo(ts.getStartTime());
    }
    if (!getEndTime().equals(ts.getEndTime())) {
      return getEndTime().compareTo(ts.getEndTime());
    }
    return this.equals(ts) ? 0 : 1;
  }
}
