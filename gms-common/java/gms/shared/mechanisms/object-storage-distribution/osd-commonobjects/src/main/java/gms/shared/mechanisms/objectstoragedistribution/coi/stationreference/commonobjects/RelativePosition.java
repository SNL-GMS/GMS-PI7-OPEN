package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * This class contains position information relative to a known Location reference point.
 */
@AutoValue
public abstract class RelativePosition {

  /**
   * Returns the number of kilometers north or south from a reference point
   *
   * @return The north/south displacement, in km
   */
  public abstract double getNorthDisplacementKm();

  /**
   * Returns the number of kilometers east or west from a reference point
   *
   * @return The east/west displacement, in km
   */
  public abstract double getEastDisplacementKm();

  /**
   * Returns the number of kilometers up or down from a reference point
   *
   * @return The up/down displacement, in km
   */
  public abstract double getVerticalDisplacementKm();

  /**
   * Create a new RelativePosition object from existing data.
   * @param northDisplacementKm The number of units north or south from the reference point.
   * @param eastDisplacementKm The number of units east or west from the reference point.
   * @param verticalDisplacementKm The number of units up or down from the reference point.
   * @return A new RelativePosition object.
   */
  @JsonCreator
  public static RelativePosition from(
      @JsonProperty("northDisplacementKm") double northDisplacementKm,
      @JsonProperty("eastDisplacementKm") double eastDisplacementKm,
      @JsonProperty("verticalDisplacementKm") double verticalDisplacementKm) {
    return new AutoValue_RelativePosition(northDisplacementKm, eastDisplacementKm,
        verticalDisplacementKm);
  }
}
