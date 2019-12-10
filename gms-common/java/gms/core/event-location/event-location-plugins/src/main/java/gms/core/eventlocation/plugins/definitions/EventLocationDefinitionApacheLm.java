package gms.core.eventlocation.plugins.definitions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.core.eventlocation.plugins.EventLocationDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import java.util.List;
import java.util.Objects;

public class EventLocationDefinitionApacheLm extends EventLocationDefinition {

  public EventLocationDefinitionApacheLm(int maximumIterations, double convergenceTolerance,
      double uncertaintyProbabilityPercentile, String earthModel,
      boolean applyTravelTimeCorrections,
      ScalingFactorType scalingFactorType, int kWeight, double aprioriVariance,
      int minimumNumberOfObservations, List<LocationRestraint> locationRestraints) {
    super(maximumIterations, convergenceTolerance, uncertaintyProbabilityPercentile, earthModel,
        applyTravelTimeCorrections, scalingFactorType, kWeight, aprioriVariance,
        minimumNumberOfObservations, locationRestraints);
  }

  //TODO: See if this should have anything else (by looking at parameters to Apaches solver, etc
  @JsonCreator
  public static EventLocationDefinitionApacheLm create(
      @JsonProperty("maximumIterations") int maximumIterations,
      @JsonProperty("convergenceTolerance") double convergenceTolerance,
      @JsonProperty("uncertaintyProbabilityPercentile") double uncertaintyProbabilityPercentile,
      @JsonProperty("earthModel") String earthModel,
      @JsonProperty("applyTravelTimeCorrections") boolean applyTravelTimeCorrections,
      @JsonProperty("scalingFactorType") ScalingFactorType scalingFactorType,
      @JsonProperty("kWeight") int kWeight,
      @JsonProperty("aprioriVariance") double aprioriVariance,
      @JsonProperty("minimumNumberOfObservations") int minimumNumberOfObservations,
      @JsonProperty("locationRestraints") List<LocationRestraint> locationRestraints
  ) {

    Objects.requireNonNull(earthModel, "Null earthModel");

    return new EventLocationDefinitionApacheLm(
        maximumIterations,
        convergenceTolerance,
        uncertaintyProbabilityPercentile,
        earthModel,
        applyTravelTimeCorrections,
        scalingFactorType,
        kWeight,
        aprioriVariance,
        minimumNumberOfObservations,
        locationRestraints
    );
  }
}
