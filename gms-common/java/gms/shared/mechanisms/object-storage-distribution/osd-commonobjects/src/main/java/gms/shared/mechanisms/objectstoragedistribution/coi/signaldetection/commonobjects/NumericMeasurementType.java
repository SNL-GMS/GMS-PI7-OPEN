package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * {@link FeatureMeasurementType} for {@link NumericMeasurementValue}
 */
@AutoValue
public abstract class NumericMeasurementType implements
    FeatureMeasurementType<NumericMeasurementValue> {

  public abstract String getFeatureMeasurementTypeName();

  @JsonCreator
  public static NumericMeasurementType from(
      @JsonProperty("featureMeasurementTypeName") String featureMeasurementTypeName) {
    return new AutoValue_NumericMeasurementType(featureMeasurementTypeName);
  }


  @Override
  public Class<NumericMeasurementValue> getMeasurementValueType() {
    return NumericMeasurementValue.class;
  }
}
