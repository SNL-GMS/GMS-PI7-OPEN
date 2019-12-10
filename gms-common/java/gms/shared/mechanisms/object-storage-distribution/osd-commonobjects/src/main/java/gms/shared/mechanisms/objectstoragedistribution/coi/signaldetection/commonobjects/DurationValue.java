package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.time.Duration;

/**
 * Represents a duration of time with a standard deviation (also a duration).
 */
@AutoValue
public abstract class DurationValue {

  /**
   * The value of the duration
   * @return the value
   */
  public abstract Duration getValue();

  /**
   * The standard deviation of the value
   * @return standard deviation (as a Duration)
   */
  public abstract Duration getStandardDeviation();

  /**
   * Creates a DurationValue.
   * @param value             The duration      
   * @param standardDeviation The standard deviation
   * @return                  DurationValue
   *
   * @throws IllegalArgumentException if any of the parameters are null
   */
  @JsonCreator
  public static DurationValue from(
      @JsonProperty("value")Duration value,
      @JsonProperty("standardDeviation")Duration standardDeviation) {
    return new AutoValue_DurationValue(value, standardDeviation);
  }
}
