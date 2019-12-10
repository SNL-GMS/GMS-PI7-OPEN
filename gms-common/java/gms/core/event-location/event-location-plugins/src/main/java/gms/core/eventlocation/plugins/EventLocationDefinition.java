package gms.core.eventlocation.plugins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gms.core.eventlocation.plugins.definitions.EventLocationDefinitionApacheLm;
import gms.core.eventlocation.plugins.definitions.EventLocationDefinitionGeigers;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import java.util.List;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type")
@JsonSubTypes({
    @Type(value = EventLocationDefinitionGeigers.class, name = "EventLocationDefinitionGeigers"),
    @Type(value = EventLocationDefinitionApacheLm.class, name = "EventLocationDefinitionApacheLm")
})
public class EventLocationDefinition {

  private int maximumIterationCount;
  private double convergenceThreshold;
  private double uncertaintyProbabilityPercentile;
  private String earthModel;
  private boolean applyTravelTimeCorrections;
  private ScalingFactorType scalingFactorType;
  private int kWeight;
  private double aprioriVariance;
  private int minimumNumberOfObservations;

  private List<LocationRestraint> locationRestraints;

  public EventLocationDefinition(
      int maximumIterationCount,
      double convergenceThreshold,
      double uncertaintyProbabilityPercentile,
      String earthModel,
      boolean applyTravelTimeCorrections,
      ScalingFactorType scalingFactorType,
      int kWeight,
      double aprioriVariance,
      int minimumNumberOfObservations,
      List<LocationRestraint> locationRestraints) {
    this.maximumIterationCount = maximumIterationCount;
    this.convergenceThreshold = convergenceThreshold;
    this.uncertaintyProbabilityPercentile = uncertaintyProbabilityPercentile;
    this.earthModel = earthModel;
    this.applyTravelTimeCorrections = applyTravelTimeCorrections;
    this.scalingFactorType = scalingFactorType;
    this.kWeight = kWeight;
    this.aprioriVariance = aprioriVariance;
    this.minimumNumberOfObservations = minimumNumberOfObservations;
    this.locationRestraints = locationRestraints;
  }

  @JsonCreator
  public static EventLocationDefinition create(
      @JsonProperty("maximumIterationCount") int maximumIterationCount,
      @JsonProperty("convergenceThreshold") double convergenceThreshold,
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

    return new EventLocationDefinition(maximumIterationCount, convergenceThreshold,
        uncertaintyProbabilityPercentile, earthModel, applyTravelTimeCorrections, scalingFactorType,
        kWeight, aprioriVariance, minimumNumberOfObservations, locationRestraints);
  }

  public int getMaximumIterationCount() {
    return maximumIterationCount;
  }

  public double getConvergenceThreshold() {
    return convergenceThreshold;
  }

  public double getUncertaintyProbabilityPercentile() {
    return uncertaintyProbabilityPercentile;
  }

  public String getEarthModel() {
    return earthModel;
  }

  public boolean isApplyTravelTimeCorrections() {
    return applyTravelTimeCorrections;
  }

  public ScalingFactorType getScalingFactorType() {
    return scalingFactorType;
  }

  public int getkWeight() {
    return kWeight;
  }

  public double getAprioriVariance() {
    return aprioriVariance;
  }

  public int getMinimumNumberOfObservations() {
    return minimumNumberOfObservations;
  }

  public List<LocationRestraint> getLocationRestraints() {
    return locationRestraints;
  }

  @Override
  public String toString() {
    return "EventLocationDefinition{" +
        "maximumIterationCount=" + maximumIterationCount +
        ", convergenceThreshold=" + convergenceThreshold +
        ", uncertaintyProbabilityPercentile=" + uncertaintyProbabilityPercentile +
        ", earthModel='" + earthModel + '\'' +
        ", applyTravelTimeCorrections=" + applyTravelTimeCorrections +
        ", scalingFactorType=" + scalingFactorType +
        ", kWeight=" + kWeight +
        ", aprioriVariance=" + aprioriVariance +
        ", minimumNumberOfObservations=" + minimumNumberOfObservations +
        ", locationRestraints=" + locationRestraints +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EventLocationDefinition that = (EventLocationDefinition) o;
    return maximumIterationCount == that.maximumIterationCount &&
        Double.compare(that.convergenceThreshold, convergenceThreshold) == 0 &&
        Double.compare(that.uncertaintyProbabilityPercentile, uncertaintyProbabilityPercentile) == 0
        &&
        applyTravelTimeCorrections == that.applyTravelTimeCorrections &&
        kWeight == that.kWeight &&
        Double.compare(that.aprioriVariance, aprioriVariance) == 0 &&
        minimumNumberOfObservations == that.minimumNumberOfObservations &&
        Objects.equals(earthModel, that.earthModel) &&
        scalingFactorType == that.scalingFactorType &&
        Objects.equals(locationRestraints, that.locationRestraints);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(maximumIterationCount, convergenceThreshold, uncertaintyProbabilityPercentile,
            earthModel,
            applyTravelTimeCorrections, scalingFactorType, kWeight, aprioriVariance,
            minimumNumberOfObservations, locationRestraints);
  }
}
