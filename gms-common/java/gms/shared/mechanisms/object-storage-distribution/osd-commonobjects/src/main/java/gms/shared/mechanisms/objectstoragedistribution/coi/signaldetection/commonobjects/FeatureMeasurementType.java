package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes.Names;

/**
 * Enumeration for types of {@link FeatureMeasurement}
 */

// This is used to deserialize references of FeatureMeasurementType into particular implementing classes based on name.
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "featureMeasurementTypeName", visible = true)
public interface FeatureMeasurementType<T> {

  /**
   * Gets the class of the feature measurement.
   * @return the class
   */
  @JsonIgnore
  Class<T> getMeasurementValueType();

  /**
   * Gets the name of the feature measurement type.
   * @return the name
   */
  String getFeatureMeasurementTypeName();
}
