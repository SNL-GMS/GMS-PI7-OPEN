package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * {@link FeatureMeasurementType} for {@link DurationMeasurementType}
 */
@AutoValue
public abstract class DurationMeasurementType implements
    FeatureMeasurementType<DurationMeasurementType> {

  public abstract String getFeatureMeasurementTypeName();

  @JsonCreator
  public static DurationMeasurementType from(
      @JsonProperty("featureMeasurementTypeName") String featureMeasurementTypeName) {
    return new AutoValue_DurationMeasurementType(featureMeasurementTypeName);
  }


  @Override
  public Class<DurationMeasurementType> getMeasurementValueType() {
    return DurationMeasurementType.class;
  }
}
