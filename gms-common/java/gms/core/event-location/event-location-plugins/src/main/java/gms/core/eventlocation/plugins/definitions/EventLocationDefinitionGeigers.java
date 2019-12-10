package gms.core.eventlocation.plugins.definitions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.core.eventlocation.plugins.EventLocationDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import java.util.List;
import java.util.Objects;

public class EventLocationDefinitionGeigers extends EventLocationDefinition {

  private int convergenceCount;
  private boolean levenbergMarquardtEnabled;
  private double lambda0;
  private double lambdaX;
  private double deltaNormThreshold;
  private double singularValueWFactor;
  private double maximumWeightedPartialDerivative;
  private double dampingFactorStep;
  private double deltamThreshold;
  private int depthFixedIterationCount;

  public EventLocationDefinitionGeigers(
      int maximumIterationCount,
      double convergenceThreshold,
      double uncertaintyProbabilityPercentile,
      String earthModel,
      boolean applyTravelTimeCorrections,
      ScalingFactorType scalingFactorType,
      int kWeight,
      double aprioriVariance,
      int minimumNumberOfObservations,
      int convergenceCount,
      boolean levenbergMarquardtEnabled,
      double lambda0,
      double lambdaX,
      double deltaNormThreshold,
      double singularValueWFactor,
      double maximumWeightedPartialDerivative,
      double dampingFactorStep,
      double deltamThreshold,
      int depthFixedIterationCount,
      List<LocationRestraint> locationRestraints) {

    super(maximumIterationCount, convergenceThreshold, uncertaintyProbabilityPercentile, earthModel,
        applyTravelTimeCorrections, scalingFactorType, kWeight, aprioriVariance,
        minimumNumberOfObservations, locationRestraints);

    this.convergenceCount = convergenceCount;
    this.levenbergMarquardtEnabled = levenbergMarquardtEnabled;
    this.lambda0 = lambda0;
    this.lambdaX = lambdaX;
    this.deltaNormThreshold = deltaNormThreshold;
    this.singularValueWFactor = singularValueWFactor;
    this.maximumWeightedPartialDerivative = maximumWeightedPartialDerivative;
    this.dampingFactorStep = dampingFactorStep;
    this.deltamThreshold = deltamThreshold;
    this.depthFixedIterationCount = depthFixedIterationCount;
  }

  @JsonCreator
  public static EventLocationDefinitionGeigers create(
      @JsonProperty("maximumIterationCount") int maximumIterationCount,
      @JsonProperty("convergenceThreshold") double convergenceThreshold,
      @JsonProperty("uncertaintyProbabilityPercentile") double uncertaintyProbabilityPercentile,
      @JsonProperty("earthModel") String earthModel,
      @JsonProperty("applyTravelTimeCorrections") boolean applyTravelTimeCorrections,
      @JsonProperty("scalingFactorType") ScalingFactorType scalingFactorType,
      @JsonProperty("kWeight") int kWeight,
      @JsonProperty("aprioriVariance") double aprioriVariance,
      @JsonProperty("minimumNumberOfObservations") int minimumNumberOfObservations,
      @JsonProperty("convergenceCount") int convergenceCount,
      @JsonProperty("levenbergMarquardtEnabled") boolean levenbergMarquardtEnabled,
      @JsonProperty("lambda0") double lambda0,
      @JsonProperty("lambdaX") double lambdaX,
      @JsonProperty("deltaNormThreshold") double deltaNormThreshold,
      @JsonProperty("singularValueWFactor") double singularValueWFactor,
      @JsonProperty("maximumWeightedPartialDerivative") double maximumWeightedPartialDerivative,
      @JsonProperty("dampingFactorStep") double dampingFactorStep,
      @JsonProperty("deltamThreshold") double deltamThreshold,
      @JsonProperty("depthFixedIterationCount") int depthFixedIterationCount,
      @JsonProperty("locationRestraint") List<LocationRestraint> locationRestraints) {

      Objects.requireNonNull(earthModel, "Null earthModel");

      return new EventLocationDefinitionGeigers(
          maximumIterationCount,
          convergenceThreshold,
          uncertaintyProbabilityPercentile,
          earthModel,
          applyTravelTimeCorrections,
          scalingFactorType,
          kWeight,
          aprioriVariance,
          minimumNumberOfObservations,
          convergenceCount,
          levenbergMarquardtEnabled,
          lambda0,
          lambdaX,
          deltaNormThreshold,
          singularValueWFactor,
          maximumWeightedPartialDerivative,
          dampingFactorStep,
          deltamThreshold,
          depthFixedIterationCount,
          locationRestraints);
    }

    public int getConvergenceCount () {
      return convergenceCount;
    }

    public boolean isLevenbergMarquardtEnabled () {
      return levenbergMarquardtEnabled;
    }

    public double getLambda0 () {
      return lambda0;
    }

    public double getLambdaX () {
      return lambdaX;
    }

    public double getDeltaNormThreshold () {
      return deltaNormThreshold;
    }

    public double getSingularValueWFactor () {
      return singularValueWFactor;
    }

    public double getMaximumWeightedPartialDerivative () {
      return maximumWeightedPartialDerivative;
    };

    public double getDampingFactorStep () {
      return dampingFactorStep;
    }

    public double getDeltamThreshold () {
      return deltamThreshold;
    }

    public int getDepthFixedIterationCount () {
      return depthFixedIterationCount;
    }


  @Override
  public String toString() {
    return "EventLocationDefinitionGeigers{" +
        "convergenceCount=" + convergenceCount +
        ", levenbergMarquardtEnabled=" + levenbergMarquardtEnabled +
        ", lambda0=" + lambda0 +
        ", lambdaX=" + lambdaX +
        ", deltaNormThreshold=" + deltaNormThreshold +
        ", singularValueWFactor=" + singularValueWFactor +
        ", maximumWeightedPartialDerivative=" + maximumWeightedPartialDerivative +
        ", dampingFactorStep=" + dampingFactorStep +
        ", deltamThreshold=" + deltamThreshold +
        ", depthFixedIterationCount=" + depthFixedIterationCount +
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
    if (!super.equals(o)) {
      return false;
    }
    EventLocationDefinitionGeigers that = (EventLocationDefinitionGeigers) o;
    return convergenceCount == that.convergenceCount &&
        levenbergMarquardtEnabled == that.levenbergMarquardtEnabled &&
        Double.compare(that.lambda0, lambda0) == 0 &&
        Double.compare(that.lambdaX, lambdaX) == 0 &&
        Double.compare(that.deltaNormThreshold, deltaNormThreshold) == 0 &&
        Double.compare(that.singularValueWFactor, singularValueWFactor) == 0 &&
        Double.compare(that.maximumWeightedPartialDerivative, maximumWeightedPartialDerivative) == 0
        &&
        Double.compare(that.dampingFactorStep, dampingFactorStep) == 0 &&
        Double.compare(that.deltamThreshold, deltamThreshold) == 0 &&
        depthFixedIterationCount == that.depthFixedIterationCount;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(super.hashCode(), convergenceCount, levenbergMarquardtEnabled, lambda0, lambdaX,
            deltaNormThreshold, singularValueWFactor, maximumWeightedPartialDerivative,
            dampingFactorStep, deltamThreshold, depthFixedIterationCount);
  }
}

