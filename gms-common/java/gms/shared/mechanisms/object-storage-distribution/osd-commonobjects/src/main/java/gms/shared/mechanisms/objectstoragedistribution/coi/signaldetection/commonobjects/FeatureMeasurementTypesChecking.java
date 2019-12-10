package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;


import java.util.Map;

/**
 * Used for turning String's into FeatureMeasurementType's and Class objects.
 * Mainly used for compile-time checking.
 */
public class FeatureMeasurementTypesChecking {
  
  private static final Map<String, FeatureMeasurementType<?>> typeStringToFeatureMeasurementTypeInstance = 
      FeatureMeasurementTypes.getTypeStringToFeatureMeasurementTypeInstance();

  /**
   * Returns the {@link FeatureMeasurementType} that is named by the given String.
   * @param stringType the name of the measurement type
   * @param <V> the type of the returned measurement type
   * @return the measurement type
   */
  @SuppressWarnings("unchecked")
  public static <V> FeatureMeasurementType<V> featureMeasurementTypeFromMeasurementTypeString(
      String stringType) {

    if (!typeStringToFeatureMeasurementTypeInstance.containsKey(stringType)) {
      throw new IllegalArgumentException("Unknown type for feature measurement: " + stringType);
    }
    return (FeatureMeasurementType<V>) typeStringToFeatureMeasurementTypeInstance.get(stringType);
  }

  /**
   * Returns the class of a particular {@link FeatureMeasurementType} by it's name.
   * @param id the name of the {@link FeatureMeasurementType}
   * @return the class of that feature measurement type
   */
  public static Class<?> measurementValueClassFromMeasurementTypeString(String id) {
    if (!typeStringToFeatureMeasurementTypeInstance.containsKey(id)) {
      throw new IllegalArgumentException("Unknown type for feature measurement: " + id);
    }
    return typeStringToFeatureMeasurementTypeInstance.get(id).getMeasurementValueType();
  }
}

