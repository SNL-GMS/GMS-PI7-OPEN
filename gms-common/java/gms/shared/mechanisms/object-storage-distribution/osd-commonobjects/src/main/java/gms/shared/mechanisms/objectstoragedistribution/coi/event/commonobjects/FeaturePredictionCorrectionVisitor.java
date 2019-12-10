package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import java.io.IOException;
import java.io.InvalidClassException;


/**
 * Interface for classes that would compute feature prediction corrections.  Follows the Visitor
 * design pattern.
 */
public interface FeaturePredictionCorrectionVisitor {

  /**
   * Computes elevation correction
   *
   * @param correction class containing the data needed to compute correction
   * @return correction in seconds or degrees (depending on feature type)
   * @throws IOException if unable to load 1D earth models
   */
  FeaturePredictionComponent computeCorrection(ElevationCorrection1dDefinition correction) throws IOException;

  /**
   * Computes Ellipticity correction
   *
   * @param correction class containing the data needed to compute correction
   * @return correction in seconds or degrees (depending on feature type)
   * @throws IOException if unable to load 1D earth models
   */
  FeaturePredictionComponent computeCorrection(EllipticityCorrection1dDefinition correction) throws IOException;

  /**
   * Default feature prediction correction.  This method would only be called in error.
   *
   * @param correction ignored feature prediction correction class
   * @return ignored return
   * @throws InvalidClassException if passed a FeaturePredictionCorrection class that is not
   * supported.
   */
  default DoubleValue computeCorrection(FeaturePredictionCorrection correction)
      throws InvalidClassException {
    throw new InvalidClassException(
        correction.getClass().getName() + " is not a supported FeaturePredictionCorrection class.");
  }

}