package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypesChecking;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "predictionType", visible = true)
@JsonTypeIdResolver(FeaturePredictionIdResolver.class)
@AutoValue
public abstract class FeaturePrediction<T> {

  public abstract UUID getId();

  public abstract PhaseType getPhase();

  public abstract Optional<T> getPredictedValue();

  public abstract Set<FeaturePredictionComponent> getFeaturePredictionComponents();

  public abstract boolean isExtrapolated();

  @JsonIgnore
  public abstract FeatureMeasurementType<T> getPredictionType();

  public abstract EventLocation getSourceLocation();

  public abstract Location getReceiverLocation();

  public abstract Optional<UUID> getChannelId();

  public abstract Map<FeaturePredictionDerivativeType, DoubleValue> getFeaturePredictionDerivativeMap();

  public static <T> FeaturePrediction<T> from(
      UUID id,
      PhaseType phase,
      Optional<T> predictedValue,
      Set<FeaturePredictionComponent> featurePredictionComponents,
      boolean extrapolated,
      FeatureMeasurementType<T> predictionType,
      EventLocation sourceLocation,
      Location receiverLocation,
      Optional<UUID> channelId
  ) {
    return new AutoValue_FeaturePrediction<>(
        id,
        phase,
        predictedValue,
        featurePredictionComponents,
        extrapolated,
        predictionType,
        sourceLocation,
        receiverLocation,
        channelId,
        Map.of()
    );
  }

  public static <T> FeaturePrediction<T> from(
      UUID id,
      PhaseType phase,
      Optional<T> predictedValue,
      Set<FeaturePredictionComponent> featurePredictionComponents,
      boolean extrapolated,
      FeatureMeasurementType<T> predictionType,
      EventLocation sourceLocation,
      Location receiverLocation,
      Optional<UUID> channelId,
      Map<FeaturePredictionDerivativeType, DoubleValue> featurePredictionDerivativeMap
  ) {
    return new AutoValue_FeaturePrediction<>(
        id,
        phase,
        predictedValue,
        featurePredictionComponents,
        extrapolated,
        predictionType,
        sourceLocation,
        receiverLocation,
        channelId,
        featurePredictionDerivativeMap
    );
  }

  @JsonCreator
  public static <T> FeaturePrediction<T> from(
      @JsonProperty("id") UUID id,
      @JsonProperty("phase") PhaseType phase,
      @JsonProperty("predictedValue") Optional<T> predictedValue,
      @JsonProperty("featurePredictionComponents") Set<FeaturePredictionComponent> featurePredictionComponents,
      @JsonProperty("extrapolated") boolean extrapolated,
      @JsonProperty("predictionType") String predictionType,
      @JsonProperty("sourceLocation") EventLocation sourceLocation,
      @JsonProperty("receiverLocation") Location receiverLocation,
      @JsonProperty("channelId") Optional<UUID> channelId,
      @JsonProperty("featurePredictionDerivativeMap") Map<FeaturePredictionDerivativeType, DoubleValue> featurePredictionDerivativeMap) {

    FeatureMeasurementType<T> featureMeasurementType = FeatureMeasurementTypesChecking
        .featureMeasurementTypeFromMeasurementTypeString(predictionType);

    return new AutoValue_FeaturePrediction<>(id, phase, predictedValue,
        Collections.unmodifiableSet(featurePredictionComponents),
        extrapolated, featureMeasurementType, sourceLocation, receiverLocation, channelId,
        featurePredictionDerivativeMap);
  }

  public static <T> FeaturePrediction<T> create(
      PhaseType phase,
      Optional<T> predictedValue,
      Set<FeaturePredictionComponent> featurePredictionComponents,
      boolean extrapolated,
      FeatureMeasurementType<T> predictionType,
      EventLocation sourceLocation,
      Location receiverLocation,
      Optional<UUID> channelId,
      Map<FeaturePredictionDerivativeType, DoubleValue> featurePredictionDerivativeMap
  ) {
    return new AutoValue_FeaturePrediction<>(
        UUID.randomUUID(),
        phase,
        predictedValue,
        featurePredictionComponents,
        extrapolated,
        predictionType,
        sourceLocation,
        receiverLocation,
        channelId,
        featurePredictionDerivativeMap
    );
  }

  /**
   * The name of the feature measurement type. Only needed for serialization.
   */
  @JsonProperty("predictionType")
  public String getPredictionTypeName() {
    return getPredictionType().getFeatureMeasurementTypeName();
  }
}
