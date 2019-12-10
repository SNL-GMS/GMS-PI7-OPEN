package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * A value class for storing a double along with its units and uncertainty
 */
@AutoValue
public abstract class DoubleValue {

 public abstract double getValue();

 public abstract double getStandardDeviation();

 public abstract Units getUnits();

 @JsonCreator
 public static DoubleValue from(
     @JsonProperty("value") double value,
     @JsonProperty("standardDeviation") double standardDeviation,
     @JsonProperty("units") Units units) {
  return new AutoValue_DoubleValue(value, standardDeviation, units);
 }

}
