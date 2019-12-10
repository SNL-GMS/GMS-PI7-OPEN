package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;


/**
 * Interface for the Visitor pattern to mark a class as "visitable".  The class passes itself to the
 * visitor to compute a feature prediction correction.
 */
@JsonDeserialize(using = FeaturePredictionCorrectionDeserializer.class)
public interface FeaturePredictionCorrection {

  /**
   * Implementing class passes itself to the visitor, {@link FeaturePredictionCorrectionVisitor}.
   *
   * @param visitor the visitor class where the correction is computed
   * @return the feature correction in seconds
   * @throws IOException if unable to load 1D earth models
   */
  FeaturePredictionComponent computeCorrection(FeaturePredictionCorrectionVisitor visitor)
      throws IOException;

  /**
   * Returns the type of correction being computed
   *
   * @return the type of correction being computed
   */
  FeaturePredictionCorrectionType getCorrectionType();
}