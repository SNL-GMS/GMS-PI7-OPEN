package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * See {@link SignalDetection} and {@link FeatureMeasurement} for detailed description of
 * SignalDetectionHypothesis.
 */
@AutoValue
public abstract class SignalDetectionHypothesis {

  public abstract UUID getId();

  public abstract UUID getParentSignalDetectionId();

  public abstract boolean isRejected();

  @JsonIgnore
  abstract ImmutableMap<FeatureMeasurementType<?>, FeatureMeasurement<?>> getFeatureMeasurementsByType();

  public abstract UUID getCreationInfoId();

  /**
   * Recreation factory method (sets the SignalDetection entity identity). Handles parameter
   * validation. Used for deserialization and recreating from persistence.
   *
   * @param id The {@link UUID} id assigned to the new SignalDetection.
   * @param parentSignalDetectionId This hypothesis' parent SignalDetection object.
   * @param isRejected Determines if this is a valid hypothesis
   * @param featureMeasurements The measurements used to make this hypothesis
   * @param creationInfoId An identifier representing this object's provenance.
   * @throws IllegalArgumentException if any of the parameters are null or featureMeasurements does
   * not contain an arrival time.
   */
  @JsonCreator
  public static SignalDetectionHypothesis from(
      @JsonProperty("id") UUID id,
      @JsonProperty("parentSignalDetectionId") UUID parentSignalDetectionId,
      @JsonProperty("rejected") boolean isRejected,
      @JsonProperty("featureMeasurements") Collection<FeatureMeasurement<?>> featureMeasurements,
      @JsonProperty("creationInfoId") UUID creationInfoId) {

    // Validate that phase and arrival time measurements are provided
    boolean hasArrival = false;
    boolean hasPhase = false;
    for (FeatureMeasurement fm : featureMeasurements) {
      hasArrival |= fm.getFeatureMeasurementType() == FeatureMeasurementTypes.ARRIVAL_TIME;
      hasPhase |= fm.getFeatureMeasurementType() == FeatureMeasurementTypes.PHASE;
    }
    if (!(hasArrival & hasPhase)) {
      throw new IllegalArgumentException(
          "Feature Measurements must contain an Arrival Time and Phase");
    }

    final Builder builder = builder(id, parentSignalDetectionId, isRejected, creationInfoId);
    featureMeasurements.forEach(builder::addMeasurement);
    return builder.build();
  }

  public static SignalDetectionHypothesis create(UUID parentSignalDetectionId,
      FeatureMeasurement<InstantValue> arrivalTimeMeasurement,
      FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement,
      UUID creationInfoId) {

    return create(parentSignalDetectionId, List.of(arrivalTimeMeasurement, phaseMeasurement),
        creationInfoId);
  }

  public static SignalDetectionHypothesis create(UUID parentSignalDetectionId,
      Collection<FeatureMeasurement<?>> measurements,
      UUID creationInfoId) {
    return from(UUID.randomUUID(), parentSignalDetectionId, false, measurements, creationInfoId);
  }

  public Collection<FeatureMeasurement<?>> getFeatureMeasurements() {
    return getFeatureMeasurementsByType().values().asList();
  }

  /**
   * Returns a particular type of feature measurement, if it exists.
   *
   * @param type the type of the measurement
   * @return the measurement if it is present, otherwise Optional.empty.
   */
  @JsonIgnore
  @SuppressWarnings("unchecked")
  public <T> Optional<FeatureMeasurement<T>> getFeatureMeasurement(
      FeatureMeasurementType<T> type) {
    Objects.requireNonNull(type, "Cannot get feature measurement by null type");
    // Cast is safe by virtue of FeatureMeasurementTypesStaticChecking
    return Optional.ofNullable((FeatureMeasurement<T>) getFeatureMeasurementsByType().get(type));
  }

  public static Builder builder(UUID id, UUID parentSignalDetectionId,
      boolean isRejected, UUID creationInfoId) {
    return new AutoValue_SignalDetectionHypothesis.Builder()
        .setId(id)
        .setParentSignalDetectionId(parentSignalDetectionId)
        .setRejected(isRejected)
        .setCreationInfoId(creationInfoId);
  }

  public abstract Builder toBuilder();

  public Builder withMeasurements(Collection<FeatureMeasurementType<?>> types) {
    return toBuilder().setFeatureMeasurementsByType(
        ImmutableMap.copyOf(Maps.filterKeys(getFeatureMeasurementsByType(), types::contains)));
  }

  public Builder withoutMeasurements(Collection<FeatureMeasurementType<?>> types) {
    return toBuilder().setFeatureMeasurementsByType(
        ImmutableMap
            .copyOf(Maps.filterKeys(getFeatureMeasurementsByType(), key -> !types.contains(key))));
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public Builder generateId() {
      return setId(UUID.randomUUID());
    }

    protected abstract Builder setId(UUID id);

    public abstract Builder setParentSignalDetectionId(UUID parentSignalDetectionId);

    public abstract Builder setRejected(boolean rejected);

    public abstract Builder setCreationInfoId(UUID creationInfoId);

    protected abstract Builder setFeatureMeasurementsByType(
        ImmutableMap<FeatureMeasurementType<?>, FeatureMeasurement<?>> featureMeasurementsByType);

    protected abstract ImmutableMap.Builder<FeatureMeasurementType<?>, FeatureMeasurement<?>> featureMeasurementsByTypeBuilder();

    public <T> Builder addMeasurement(FeatureMeasurement<T> measurement) {
      Objects.requireNonNull(measurement, "Cannot add null measurement");
      featureMeasurementsByTypeBuilder().put(measurement.getFeatureMeasurementType(), measurement);
      return this;
    }

    public abstract SignalDetectionHypothesis build();
  }
}
