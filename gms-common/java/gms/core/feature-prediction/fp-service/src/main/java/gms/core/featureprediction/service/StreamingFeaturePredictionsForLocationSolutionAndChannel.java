package gms.core.featureprediction.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypesChecking;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;


public class StreamingFeaturePredictionsForLocationSolutionAndChannel {

  private final List<FeatureMeasurementType<?>> featureMeasurementTypes;
  private final LocationSolution eventLocation;
  private final List<Channel> receiverLocations;
  private final PhaseType phase;
  private final String model;
  private final List<FeaturePredictionCorrection> correctionDefinitions;
  private final ProcessingContext processingContext;

  private StreamingFeaturePredictionsForLocationSolutionAndChannel(
      List<FeatureMeasurementType<?>> featureMeasurementTypes,
      LocationSolution sourceLocation,
      List<Channel> receiverLocations,
      PhaseType phase,
      String model,
      List<FeaturePredictionCorrection> correctionDefinitions,
      ProcessingContext processingContext) {

    // Validate the input.
    Validate.notNull(featureMeasurementTypes);
    Validate.isTrue(featureMeasurementTypes.size() > 0);
    Validate.notNull(sourceLocation);
    Validate.notNull(receiverLocations);
    Validate.isTrue(receiverLocations.size() > 0);
    Validate.notNull(phase);
    Validate.notBlank(model);
    Validate.notNull(processingContext);

    this.featureMeasurementTypes = featureMeasurementTypes;
    this.eventLocation = sourceLocation;
    this.receiverLocations = receiverLocations;
    this.phase = phase;
    this.model = model;
    this.correctionDefinitions = (correctionDefinitions == null) ?
        new ArrayList<>() : correctionDefinitions;
    this.processingContext = processingContext;
  }

  public static StreamingFeaturePredictionsForLocationSolutionAndChannel from(
      List<FeatureMeasurementType<?>> featureMeasurementTypes,
      LocationSolution sourceLocation,
      List<Channel> receiverLocations,
      PhaseType phase,
      String model,
      List<FeaturePredictionCorrection> correctionDefinitions,
      ProcessingContext processingContext) {
    return new StreamingFeaturePredictionsForLocationSolutionAndChannel(
        featureMeasurementTypes,
        sourceLocation,
        receiverLocations,
        phase,
        model,
        correctionDefinitions,
        processingContext);
  }

  @JsonCreator
  public static StreamingFeaturePredictionsForLocationSolutionAndChannel create(
      @JsonProperty("featureMeasurementTypes") List<String> featureMeasurementTypes,
      @JsonProperty("sourceLocation") LocationSolution sourceLocation,
      @JsonProperty("receiverLocations") List<Channel> receiverLocations,
      @JsonProperty("phase") PhaseType phase,
      @JsonProperty("model") String model,
      @JsonProperty("corrections") List<FeaturePredictionCorrection> correctionDefinitions,
      @JsonProperty("processingContext") ProcessingContext processingContext) {
    return new StreamingFeaturePredictionsForLocationSolutionAndChannel(
        featureMeasurementTypes.stream().map(
            FeatureMeasurementTypesChecking::featureMeasurementTypeFromMeasurementTypeString)
            .collect(Collectors.toList()),
        sourceLocation,
        receiverLocations,
        phase,
        model,
        correctionDefinitions,
        processingContext);
  }

  @JsonIgnore
  public List<FeatureMeasurementType<?>> getFeatureMeasurementTypes() {
    return featureMeasurementTypes;
  }

  @JsonProperty("featureMeasurementTypes")
  public List<String> getFeatureMeasurementTypeNames() {
    return featureMeasurementTypes.stream()
        .map(FeatureMeasurementType::getFeatureMeasurementTypeName)
        .collect(
            Collectors.toList());
  }

  public LocationSolution getSourceLocation() {
    return eventLocation;
  }

  public List<Channel> getReceiverLocations() {
    return receiverLocations;
  }

  public PhaseType getPhase() {
    return phase;
  }

  public String getModel() {
    return model;
  }

  public List<FeaturePredictionCorrection> getCorrectionDefinitions() {
    return correctionDefinitions;
  }

  public ProcessingContext getProcessingContext() {
    return processingContext;
  }

  @Override
  public String toString() {
    return "StreamingFeaturePredictionsForSourceAndReceiverLocations{" +
        "featureMeasurementTypes=" + featureMeasurementTypes +
        ", eventLocation=" + eventLocation +
        ", receiverLocations=" + receiverLocations +
        ", phase=" + phase +
        ", model='" + model + '\'' +
        ", correctionDefinitions=" + correctionDefinitions +
        ", processingContext=" + processingContext +
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
    StreamingFeaturePredictionsForLocationSolutionAndChannel that = (StreamingFeaturePredictionsForLocationSolutionAndChannel) o;
    return Objects.equals(featureMeasurementTypes, that.featureMeasurementTypes) &&
        Objects.equals(eventLocation, that.eventLocation) &&
        Objects.equals(receiverLocations, that.receiverLocations) &&
        phase == that.phase &&
        Objects.equals(model, that.model) &&
        Objects.equals(correctionDefinitions, that.correctionDefinitions) &&
        Objects.equals(processingContext, that.processingContext);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(featureMeasurementTypes, eventLocation, receiverLocations, phase, model,
            correctionDefinitions, processingContext);
  }
}
