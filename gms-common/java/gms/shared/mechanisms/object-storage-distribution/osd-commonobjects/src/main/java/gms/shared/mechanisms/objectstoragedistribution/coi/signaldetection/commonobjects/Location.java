package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * Represents a point on the earth in latitude, longitude, elevation, and depth (offset below
 * elevation)
 */
@AutoValue
public abstract class Location {

  /**
   * Obtain elevation of a point on the earth, in km
   *
   * @return elevation, in km
   */
  public abstract double getLatitudeDegrees();

  /**
   * Obtain longitude of a point on the earth, in degrees
   *
   * @return longitude, in degrees
   */
  public abstract double getLongitudeDegrees();

  /**
   * Obtain offset below elevation of a point on the earth, in km
   *
   * @return depth, in km
   */
  public abstract double getDepthKm();

  /**
   * Obtain elevation of a point on the earth, in lm
   *
   * @return elevation, in km
   */
  public abstract double getElevationKm();

  /**
   * Obtain a new Location.
   *
   * @param latitudeDegrees The latitude value of a point on the earth, in degrees
   * @param longitudeDegrees The longitude value of a point on the earth, in degrees
   * @param depthKm The offset below elevation of a point on the earth, in km
   * @param elevationKm The elevation at ground-level of a point on the earth, in km
   * @return {@link Location}
   */
  @JsonCreator
  public static Location from(
      @JsonProperty("latitudeDegrees") double latitudeDegrees,
      @JsonProperty("longitudeDegrees") double longitudeDegrees,
      @JsonProperty("depthKm") double depthKm,
      @JsonProperty("elevationKm") double elevationKm) {
    return new AutoValue_Location(latitudeDegrees, longitudeDegrees, depthKm, elevationKm);
  }
}
