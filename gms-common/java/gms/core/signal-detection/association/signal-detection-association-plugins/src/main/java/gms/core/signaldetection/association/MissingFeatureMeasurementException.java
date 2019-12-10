package gms.core.signaldetection.association;

/**
 * Optional.isPresent() returns false for requested FeatureMeasurement, but true is expected.
 */
public class MissingFeatureMeasurementException extends Exception {

  /**
   * Optional.isPresent() returns false for requested FeatureMeasurement, but true is expected.
   * Message string may indicate FeatureMeasurementTypes.
   */
  public MissingFeatureMeasurementException(String message) {
    super(message);
  }
}
