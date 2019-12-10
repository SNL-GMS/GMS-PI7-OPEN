package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.google.auto.value.AutoValue;
import java.util.UUID;

/**
 * Represents a measure of some kind of feature.
 *
 * A Signal Detection Hypothesis typically will have many measurements associated with it, captured
 * with the Feature Measurement class. Feature Measurement has been made generic to accommodate any
 * new types of measurement that may be added in the future. Each Feature Measurement has a type
 * indicated with the feature measurement type attribute, a value, and a reference to the Channel
 * Segment on which it was calculated. As shown in the association above, each Signal Detection
 * Hypothesis is required to have at least an arrival time Feature Measurement. The additional
 * Feature Measurements are a "zero to many" relationship, because they are not required by the
 * system.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "featureMeasurementType", visible = true)
@JsonTypeIdResolver(FeatureMeasurementIdResolver.class)
@AutoValue
public abstract class FeatureMeasurement<V> {

  /**
   * The id of the measurement
   *
   * @return the id
   */
  public abstract UUID getId();

  /**
   * The channel segment id the measurement is related to
   *
   * @return channel segment id
   */
  public abstract UUID getChannelSegmentId();

  /**
   * Type of the measurement.  Matches up to getMeasurementValue().
   *
   * @return type
   */
  @JsonIgnore
  public abstract FeatureMeasurementType<V> getFeatureMeasurementType();

  /**
   * Value of the measurement.  Matches up to getFeatureMeasurementType().
   *
   * @return the value of the measurement
   */
  public abstract V getMeasurementValue();

  /**
   * The name of the feature measurement type. Only needed for serialization.
   */
  @JsonProperty("featureMeasurementType")
  public String getFeatureMeasurementTypeName() {
    return getFeatureMeasurementType().getFeatureMeasurementTypeName();
  }

  /**
   * Recreates a FeatureMeasurement from a serialized form.
   *
   * @param id id of the measurement
   * @param channelSegmentId segment id the measurement is related to
   * @param stringType the type of the measurement as a string
   * @param measurementValue the value of the measurement
   * @param <V> type param of the measurement
   * @return a FeatureMeasurement
   */
  @JsonCreator
  public static <V> FeatureMeasurement<V> from(
      @JsonProperty("id") UUID id,
      @JsonProperty("channelSegmentId") UUID channelSegmentId,
      @JsonProperty("featureMeasurementType") String stringType,
      @JsonProperty("measurementValue") V measurementValue) {

    final FeatureMeasurementType<V> featureMeasurementType = FeatureMeasurementTypesChecking
        .featureMeasurementTypeFromMeasurementTypeString(stringType);

    return from(id, channelSegmentId, featureMeasurementType, measurementValue);
  }

  /**
   * Recreates a FeatureMeasurement with all args.
   *
   * @param id id of the measurement
   * @param channelSegmentId segment id the measurement is related to
   * @param type the type of the measurement
   * @param measurementValue the value of the measurement
   * @param <V> type param of the measurement
   * @return a FeatureMeasurement
   */
  public static <V> FeatureMeasurement<V> from(
      UUID id, UUID channelSegmentId,
      FeatureMeasurementType<V> type,
      V measurementValue) {
    return new AutoValue_FeatureMeasurement<>(id, channelSegmentId, type, measurementValue);
  }

  /**
   * Creates a new FeatureMeasurement.
   *
   * @param channelSegmentId segment id the measurement is related to
   * @param type the type of the measurement
   * @param measurementValue the value of the measurement
   * @param <V> type param of the measurement
   * @return a FeatureMeasurement
   */
  public static <V> FeatureMeasurement<V> create(UUID channelSegmentId,
      FeatureMeasurementType<V> type, V measurementValue) {
    return new AutoValue_FeatureMeasurement<>(UUID.randomUUID(), channelSegmentId, type,
        measurementValue);
  }
}

