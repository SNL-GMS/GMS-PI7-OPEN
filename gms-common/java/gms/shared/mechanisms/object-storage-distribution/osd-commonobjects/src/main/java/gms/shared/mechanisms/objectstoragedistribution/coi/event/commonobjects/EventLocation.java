package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.time.Instant;

/**
 * Define an EventLocation class for use with the processing results location solution.
 */
@AutoValue
public abstract class EventLocation {

  public abstract double getLatitudeDegrees();
  public abstract double getLongitudeDegrees();
  public abstract double getDepthKm();
  public abstract Instant getTime();

  /**
   * Create a new Location object from known attributes.
   * @param latitudeDegrees Latitude in degrees (-180.0 to 180.0).
   * @param longitudeDegrees Longitude in degrees (-180.0 to 180.0).
   * @param depthKm Depth in kilometers
   * @param time The date and time of the readings.
   * @return A Location object.
   */
  @JsonCreator
  public static EventLocation from(
      @JsonProperty("latitudeDegrees") double latitudeDegrees,
      @JsonProperty("longitudeDegrees") double longitudeDegrees,
      @JsonProperty("depthKm") double depthKm,
      @JsonProperty("time") Instant time)  {
    return new AutoValue_EventLocation(latitudeDegrees, longitudeDegrees, depthKm, time);
  }
}
