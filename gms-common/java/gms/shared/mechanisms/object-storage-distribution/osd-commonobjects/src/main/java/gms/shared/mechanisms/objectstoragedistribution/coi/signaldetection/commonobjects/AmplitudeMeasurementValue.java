package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import java.time.Duration;
import java.time.Instant;

/**
 * Represents an amplitude measurement.
 *
 * Corresponds to AMPLITUDE_A5_OVER_2 FeatureMeasurementType
 */
@AutoValue
public abstract class AmplitudeMeasurementValue {

  /**
   * Gets the start time this amplitude measurement was made at.
   * @return the start time of the measurement
   */
  public abstract Instant getStartTime();

  /**
   * Gets the period of this amplitude measurement
   * @return the period
   */
  public abstract Duration getPeriod();

  /**
   * Gets the value of the measurement
   * @return the amplitude value
   */
  public abstract DoubleValue getAmplitude();

  /**
   * Creates an AmplitudeMeasurementValue
   * @param startTime     The start time       
   * @param period        The period
   * @param amplitude     The amplitude
   * @return              AmplitudeMeasurementValue
   *
   * @throws IllegalArgumentException if any of the parameters are null
   */
  @JsonCreator
  public static AmplitudeMeasurementValue from(
      @JsonProperty("startTime") Instant startTime,
      @JsonProperty("period") Duration period,
      @JsonProperty("amplitude") DoubleValue amplitude) {
    return new AutoValue_AmplitudeMeasurementValue(startTime, period, amplitude);
  }
}
