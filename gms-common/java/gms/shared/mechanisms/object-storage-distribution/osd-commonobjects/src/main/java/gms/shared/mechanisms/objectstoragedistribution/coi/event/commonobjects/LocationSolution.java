package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Define a LocationSolution class for process results.
 */
@AutoValue
public abstract class LocationSolution {

  public abstract UUID getId();

  public abstract EventLocation getLocation();

  public abstract LocationRestraint getLocationRestraint();

  public abstract Optional<LocationUncertainty> getLocationUncertainty();

  public abstract Set<FeaturePrediction<?>> getFeaturePredictions();

  public abstract Set<LocationBehavior> getLocationBehaviors();

  /**
   * Create a new LocationSolution.
   *
   * @param location A Location object, not null.
   * @param locationRestraint A LocationRestraint object, not null.
   * @return A LocationSolution object.
   */
  public static LocationSolution withLocationAndRestraintOnly(EventLocation location,
      LocationRestraint locationRestraint) {

    return new AutoValue_LocationSolution(UUID.randomUUID(),
        location,
        locationRestraint,
        Optional.empty(),
        Set.of(),
        Set.of());
  }

  /**
   * Create a new a LocationSolution.
   *
   * @param location A Location object, not null.
   * @param locationRestraint A LocationRestraint object, not null.
   * @param locationUncertainty A LocationUncertainty object, may be null.
   * @param locationBehaviors A set of LocationBehavior objects, not null.
   * @param featurePredictions A set of FeaturePrediction objects, not null.
   * @return A LocationSolution object.
   */
  public static LocationSolution create(
      EventLocation location,
      LocationRestraint locationRestraint,
      LocationUncertainty locationUncertainty,
      Set<LocationBehavior> locationBehaviors,
      Set<FeaturePrediction<?>> featurePredictions) {

    return new AutoValue_LocationSolution(UUID.randomUUID(), location,
        locationRestraint,
        Optional.ofNullable(locationUncertainty),
        Collections.unmodifiableSet(featurePredictions),
        Collections.unmodifiableSet(locationBehaviors));
  }

  /**
   * Recreate a LocationSolution from existing attributes.
   *
   * @param id the id of the location solution
   * @param location A Location object, not null.
   * @param locationRestraint A LocationRestraint object, not null.
   * @param locationUncertainty A LocationUncertainty object, may be null.
   * @param locationBehaviors A set of LocationBehavior objects, may be null.
   * @param featurePredictions A set of FeaturePrediction objects, may be null.
   * @return A LocationSolution object.
   */
  @JsonCreator
  public static LocationSolution from(
      @JsonProperty("id") UUID id,
      @JsonProperty("location") EventLocation location,
      @JsonProperty("locationRestraint") LocationRestraint locationRestraint,
      @JsonProperty("locationUncertainty") LocationUncertainty locationUncertainty,
      @JsonProperty("locationBehaviors") Set<LocationBehavior> locationBehaviors,
      @JsonProperty("featurePredictions") Set<FeaturePrediction<?>> featurePredictions) {

    return new AutoValue_LocationSolution(id, location,
        locationRestraint,
        Optional.ofNullable(locationUncertainty),
        Collections.unmodifiableSet(featurePredictions),
        Collections.unmodifiableSet(locationBehaviors));
  }
}
