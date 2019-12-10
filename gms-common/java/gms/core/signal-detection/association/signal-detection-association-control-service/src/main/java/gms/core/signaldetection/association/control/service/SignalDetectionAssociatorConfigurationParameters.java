package gms.core.signaldetection.association.control.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class SignalDetectionAssociatorConfigurationParameters {

  public abstract String getPluginName();

  public abstract String getPluginVersion();

  public abstract String getGridModelFileName();

  public abstract int getGridSpacing();

  public abstract int getMaxStationsPerGrid();

  public abstract double getSigmaSlowness();

  public abstract List<String> getPhases();

  public abstract List<String> getForwardTransformationPhases();

  public abstract double getBeliefThreshold();

  public abstract boolean getPrimaryPhaseRequiredForSecondary();

  public abstract double getSigmaTime();

  public abstract double getChiLimit();

  public abstract boolean getFreezeArrivalsAtBeamPoints();

  public abstract double getGridCylinderRadiusDegrees();

  public abstract double getGridCylinderDepthKm();

  public abstract double getGridCylinderHeightKm();

  public abstract double getMinimumMagnitude();

  public abstract int getNumFirstSta();

  @JsonCreator
  public static SignalDetectionAssociatorConfigurationParameters from(
      @JsonProperty("pluginName") String pluginName,
      @JsonProperty("pluginVersion") String pluginVersion,
      @JsonProperty("gridModelFileName") String gridModelFileName,
      @JsonProperty("gridSpacing") int gridSpacing,
      @JsonProperty("maxStationsPerGrid") int maxStationsPerGrid,
      @JsonProperty("sigmaSlowness") double sigmaSlowness,
      @JsonProperty("phases") List<String> phases,
      @JsonProperty("forwardTransformationPhases") List<String> forwardTransformationPhases,
      @JsonProperty("beliefThreshold") double beliefThreshold,
      @JsonProperty("primaryPhaseRequiredForSecondary") boolean primaryPhaseRequiredForSecondary,
      @JsonProperty("sigmaTime") double sigmaTime,
      @JsonProperty("chiLimit") double chiLimit,
      @JsonProperty("freezeArrivalsAtBeamPoints") boolean freezeArrivalsAtBeamPoints,
      @JsonProperty("gridCylinderRadiusDegrees")double gridCylinderRadiusDegrees,
      @JsonProperty("gridCylinderDepthKm")double gridCylinderDepthKm,
      @JsonProperty("gridCylinderHeightKm")double gridCylinderHeightKm,
      @JsonProperty("minimumMagnitude")double minimumMagnitude,
      @JsonProperty("numFirstSta") int numFirstSta) {
    return new AutoValue_SignalDetectionAssociatorConfigurationParameters(
        pluginName,
        pluginVersion,
        gridModelFileName,
        gridSpacing,
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
        numFirstSta);
  }
}
