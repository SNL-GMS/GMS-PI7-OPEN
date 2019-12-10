package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;

/**
 * {@link FeatureMeasurementType} for {@link PhaseTypeMeasurementValue}
 */
@AutoValue
public abstract class PhaseMeasurementType implements
    FeatureMeasurementType<PhaseTypeMeasurementValue> {

  public abstract String getFeatureMeasurementTypeName();

  @JsonCreator
  public static PhaseMeasurementType from(
      @JsonProperty("featureMeasurementTypeName") String featureMeasurementTypeName) {
    return new AutoValue_PhaseMeasurementType(featureMeasurementTypeName);
  }

  @Override
  public Class<PhaseTypeMeasurementValue> getMeasurementValueType() {
    return PhaseTypeMeasurementValue.class;
  }
}
