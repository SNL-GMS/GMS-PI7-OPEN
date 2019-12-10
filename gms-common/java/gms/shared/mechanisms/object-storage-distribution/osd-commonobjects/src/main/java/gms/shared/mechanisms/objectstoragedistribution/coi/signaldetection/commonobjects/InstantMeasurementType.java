package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * {@link FeatureMeasurementType} for {@link InstantValue}
 */
@AutoValue
public abstract class InstantMeasurementType implements
    FeatureMeasurementType<InstantValue> {

  public abstract String getFeatureMeasurementTypeName();

  @JsonCreator
  public static InstantMeasurementType from(
      @JsonProperty("featureMeasurementTypeName") String featureMeasurementTypeName) {
    return new AutoValue_InstantMeasurementType(featureMeasurementTypeName);
  }


  @Override
  public Class<InstantValue> getMeasurementValueType() {
    return InstantValue.class;
  }
}
