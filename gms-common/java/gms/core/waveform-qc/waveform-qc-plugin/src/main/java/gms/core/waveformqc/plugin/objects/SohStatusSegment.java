package gms.core.waveformqc.plugin.objects;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import java.time.Instant;

/**
 * Combines a status value and the range of time the status is valid for.
 */
@AutoValue
public abstract class SohStatusSegment {

  /**
   * Obtains the {@link Instant} startTime for this status.  The status condition begins at this
   * time.
   *
   * @return time as an Instant, not null
   */
  public abstract Instant getStartTime();

  /**
   * Obtains the {@link Instant} endTime for this status.  The status condition ends at this
   * time.
   *
   * @return time as an Instant, not null
   */
  public abstract Instant getEndTime();

  /**
   * Obtain the boolean status value for this status
   *
   * @return value as a boolean, not null
   */
  public abstract SohStatusBit getStatusBit();

  /**
   * Obtains a new {@link SohStatusSegment} from the provided startTime, endTime, and sohStatusBit
   *
   * @param startTime {@link Instant} sohStatusBit start time, not null
   * @param endTime sohStatusBit end time, not null
   * @param statusBit {@link SohStatusBit} for this sohStatusBit, not null
   * @return a SohStatusSegment object, not null
   * @throws NullPointerException if startTime, endTime, or sohStatusBit are null
   * @throws IllegalArgumentException if startTime is after endTime
   */
  public static SohStatusSegment from(Instant startTime, Instant endTime, SohStatusBit statusBit) {
    Preconditions
        .checkArgument(startTime.isBefore(endTime),
            "Error creating SohStatusSegment: startTime must be before endTime");
    return new AutoValue_SohStatusSegment(startTime, endTime, statusBit);
  }

  public static SohStatusSegment from(Instant startTime, Instant endTime, boolean statusBit) {
    return from(startTime, endTime, SohStatusBit.from(statusBit));
  }

  /**
   * Obtains a new SohStatusSegment with the same startTime and status as this object but with an updated
   * endTime.  Does not affect the current object.
   *
   * @param endTime endTime used in the new SohStatusSegment object
   * @return a new SohStatusSegment
   */
  public SohStatusSegment withEndTime(final Instant endTime) {
    return from(getStartTime(), endTime, getStatusBit());
  }
}
