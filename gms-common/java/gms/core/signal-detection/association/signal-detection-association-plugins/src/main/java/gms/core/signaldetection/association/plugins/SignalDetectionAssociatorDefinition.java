package gms.core.signaldetection.association.plugins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterion;
import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterionDefinition;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.util.List;
import java.util.Objects;

public class SignalDetectionAssociatorDefinition {

  private double minimumMagnitude;
  private int maxStationsPerGrid;
  private double sigmaSlowness;
  private List<PhaseType> phases;
  private List<PhaseType> forwardTransformationPhases;
  private double beliefThreshold;
  private boolean primaryPhaseRequiredForSecondary;
  private double sigmaTime;
  private double chiLimit;
  private boolean freezeArrivalsAtBeamPoints;
  private double gridCylinderRadiusDegrees;
  private double gridCylinderDepthKm;
  private double gridCylinderHeightKm;
  private WeightedEventCriteriaCalculationDefinition weightedEventCriteria;
  private ArrivalQualityEventCriterionDefinition arrivalQualityCriteria;
  private int numFirstSta;

  public SignalDetectionAssociatorDefinition(int maxStationsPerGrid, double sigmaSlowness,
      List<PhaseType> phases,
      List<PhaseType> forwardTransformationPhases, double beliefThreshold,
      boolean primaryPhaseRequiredForSecondary, double sigmaTime, double chiLimit,
      boolean freezeArrivalsAtBeamPoints,
      double gridCylinderRadiusDegrees,
      double gridCylinderDepthKm,
      double gridCylinderHeightKm,
      double minimumMagnitude,
      WeightedEventCriteriaCalculationDefinition weightedEventCriteria,
      ArrivalQualityEventCriterionDefinition arrivalQualityCriteria,
      int numFirstSta) {
    this.maxStationsPerGrid = maxStationsPerGrid;
    this.sigmaSlowness = sigmaSlowness;
    this.phases = phases;
    this.forwardTransformationPhases = forwardTransformationPhases;
    this.beliefThreshold = beliefThreshold;
    this.primaryPhaseRequiredForSecondary = primaryPhaseRequiredForSecondary;
    this.sigmaTime = sigmaTime;
    this.chiLimit = chiLimit;
    this.freezeArrivalsAtBeamPoints = freezeArrivalsAtBeamPoints;
    this.weightedEventCriteria = weightedEventCriteria;
    this.arrivalQualityCriteria = arrivalQualityCriteria;
    this.gridCylinderRadiusDegrees = gridCylinderRadiusDegrees;
    this.gridCylinderDepthKm = gridCylinderDepthKm;
    this.gridCylinderHeightKm = gridCylinderHeightKm;
    this.minimumMagnitude = minimumMagnitude;
    this.numFirstSta = numFirstSta;
  }

  @JsonCreator
  public static SignalDetectionAssociatorDefinition create(
      @JsonProperty("maxStationsPerGrid") int maxStationsPerGrid,
      @JsonProperty("sigmaSlowness") double sigmaSlowness,
      @JsonProperty("phases") List<PhaseType> phases,
      @JsonProperty("forwardTransformationPhases") List<PhaseType> forwardTransformationPhases,
      @JsonProperty("beliefThreshold") double beliefThreshold,
      @JsonProperty("primaryPhaseRequiredForSecondary") boolean primaryPhaseRequiredForSecondary,
      @JsonProperty("sigmaTime") double sigmaTime,
      @JsonProperty("chiLimit") double chiLimit,
      @JsonProperty("freezeArrivalsAtBeamPoints") boolean freezeArrivalsAtBeamPoints,
      @JsonProperty("gridCylinderRadiusDegrees") double gridCylinderRadiusDegrees,
      @JsonProperty("gridCylinderDepthKm") double gridCylinderDepthKm,
      @JsonProperty("gridCylinderHeightKm") double gridCylinderHeightKm,
      @JsonProperty("minimumMagnitude") double minimumMagnitude,
      @JsonProperty("weightedEventCriteria") WeightedEventCriteriaCalculationDefinition weightedEventCriteria,
      @JsonProperty("arrivalQualityCriteria") ArrivalQualityEventCriterionDefinition arrivalQualityCriteria,
      @JsonProperty("numFirstSta") int numFirstSta
  ) {

    Objects.requireNonNull(phases, "Cant have null list of phases");
    Objects.requireNonNull(forwardTransformationPhases,
        "Cant have null lust of forward transform phases");

    return new SignalDetectionAssociatorDefinition(
        maxStationsPerGrid,
        sigmaSlowness,
        phases,
        forwardTransformationPhases,
        beliefThreshold,
        primaryPhaseRequiredForSecondary,
        sigmaTime,
        chiLimit,
        freezeArrivalsAtBeamPoints,
        gridCylinderRadiusDegrees,
        gridCylinderDepthKm,
        gridCylinderHeightKm,
        minimumMagnitude,
        weightedEventCriteria,
        arrivalQualityCriteria,
        numFirstSta
    );
  }

  public int getMaxStationsPerGrid() {
    return maxStationsPerGrid;
  }

  public double getSigmaSlowness() {
    return sigmaSlowness;
  }

  public List<PhaseType> getPhases() {
    return phases;
  }

  public List<PhaseType> getForwardTransformationPhases() {
    return forwardTransformationPhases;
  }

  public double getBeliefThreshold() {
    return beliefThreshold;
  }

  public boolean isPrimaryPhaseRequiredForSecondary() {
    return primaryPhaseRequiredForSecondary;
  }

  public double getSigmaTime() {
    return sigmaTime;
  }

  public double getChiLimit() {
    return chiLimit;
  }

  public boolean isFreezeArrivalsAtBeamPoints() {
    return freezeArrivalsAtBeamPoints;
  }

  public double getGridCylinderRadiusDegrees() { return gridCylinderRadiusDegrees; }

  public double getGridCylinderDepthKm() { return gridCylinderDepthKm; }

  public double getGridCylinderHeightKm() { return gridCylinderHeightKm; }

  public double getMinimumMagnitude() { return minimumMagnitude; }
  public WeightedEventCriteriaCalculationDefinition getWeightedEventCriteria() { return weightedEventCriteria; }

  public ArrivalQualityEventCriterionDefinition getArrivalQualityCriteria() {return arrivalQualityCriteria;}
  public int getNumFirstSta() {
    return numFirstSta;
  }

  @Override
  public String toString() {
    return "SignalDetectionAssociatorDefinition{" +
        "maxStationsPerGrid=" + maxStationsPerGrid +
        ", sigmaSlowness=" + sigmaSlowness +
        ", phases=" + phases +
        ", forwardTransformationPhases=" + forwardTransformationPhases +
        ", beliefThreshold=" + beliefThreshold +
        ", primaryPhaseRequiredForSecondary=" + primaryPhaseRequiredForSecondary +
        ", sigmaTime=" + sigmaTime +
        ", chiLimit=" + chiLimit +
        ", freezeArrivalsAtBeamPoints=" + freezeArrivalsAtBeamPoints +
        ", gridCylinderRadiusDegrees=" + gridCylinderRadiusDegrees +
        ", gridCylinderDepthKm=" + gridCylinderDepthKm +
        ", gridCylinderHeightKm=" + gridCylinderHeightKm +
        ", minimumMagnitude=" + minimumMagnitude +
        ", weightedEventCriteria=" + weightedEventCriteria.toString() +
        ", arrivalQualityCriteria=" + arrivalQualityCriteria.toString() +
        ", numFirstSta=" + numFirstSta +
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
    SignalDetectionAssociatorDefinition that = (SignalDetectionAssociatorDefinition) o;
    return maxStationsPerGrid == that.maxStationsPerGrid &&
        Double.compare(that.sigmaSlowness, sigmaSlowness) == 0 &&
        Double.compare(that.beliefThreshold, beliefThreshold) == 0 &&
        primaryPhaseRequiredForSecondary == that.primaryPhaseRequiredForSecondary &&
        Double.compare(that.sigmaTime, sigmaTime) == 0 &&
        Double.compare(that.chiLimit, chiLimit) == 0 &&
        freezeArrivalsAtBeamPoints == that.freezeArrivalsAtBeamPoints &&
        gridCylinderRadiusDegrees == that.gridCylinderRadiusDegrees &&
        gridCylinderDepthKm == that.gridCylinderDepthKm &&
        gridCylinderHeightKm == that.gridCylinderHeightKm &&
        that.getWeightedEventCriteria().equals(this.weightedEventCriteria) &&
        that.getArrivalQualityCriteria().equals(this.arrivalQualityCriteria)&&
        Objects.equals(phases, that.phases) &&
        Objects.equals(forwardTransformationPhases, that.forwardTransformationPhases) &&
        numFirstSta == that.numFirstSta;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(maxStationsPerGrid, sigmaSlowness, phases, forwardTransformationPhases,
            beliefThreshold,gridCylinderRadiusDegrees,
            gridCylinderDepthKm,
            gridCylinderHeightKm,
            primaryPhaseRequiredForSecondary, sigmaTime, chiLimit, freezeArrivalsAtBeamPoints,
            weightedEventCriteria, arrivalQualityCriteria,numFirstSta);
  }
}
