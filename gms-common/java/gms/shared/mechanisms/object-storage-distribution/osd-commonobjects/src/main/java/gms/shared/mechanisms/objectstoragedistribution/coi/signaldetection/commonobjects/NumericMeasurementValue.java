package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import java.time.Instant;

/**
 * Represents a numerical measurement made at a particular time.
 *
 * Corresponds to SOURCE_TO_RECEIVER_AZIMUTH, RECEIVER_TO_SOURCE_AZIMUTH,
 * SLOWNESS, SOURCE_TO_RECEIVER_DISTANCE, EMERGENCE_ANGLE, RECTILINEARITY, SNR
 * FeatureMeasurementTypes
 */
@AutoValue
public abstract class NumericMeasurementValue {

  /**
   * Gets the time this measurement was made at.
   * @return reference time
   */
  public abstract Instant getReferenceTime();

  /**
   * Gets the value of this measurement
   * @return the value
   */
  public abstract DoubleValue getMeasurementValue();

  /**
   * Recreation factory method (sets the NumericalMeasurement entity identity).
   * Used for deserialization and recreating from persistence.
   * @param referenceTime     The reference time       
   * @param measurementValue The measurement's value
   * @return                 NumericMeasurementValue
   *
   * @throws IllegalArgumentException if any of the parameters are null
   */
  @JsonCreator
  public static NumericMeasurementValue from(
      @JsonProperty("referenceTime")Instant referenceTime,
      @JsonProperty("measurementValue")DoubleValue measurementValue) { 
    return new AutoValue_NumericMeasurementValue(referenceTime, measurementValue);
  }
}
