package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PreferredLocationSolution {

  public abstract LocationSolution getLocationSolution();

  @JsonCreator
  public static PreferredLocationSolution from(
      @JsonProperty("locationSolution") LocationSolution locationSolution) {
    return new AutoValue_PreferredLocationSolution(locationSolution);
  }
}
