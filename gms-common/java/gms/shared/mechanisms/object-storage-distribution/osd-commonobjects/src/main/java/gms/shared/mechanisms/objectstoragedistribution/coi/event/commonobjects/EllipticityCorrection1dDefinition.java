package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.auto.value.AutoValue;
import java.io.IOException;


/**
 * Implementation of {@link FeaturePredictionCorrection} for ellipticity correction
 */
@AutoValue
public abstract class EllipticityCorrection1dDefinition implements FeaturePredictionCorrection {

  @JsonCreator
  public static EllipticityCorrection1dDefinition create() {
    return new AutoValue_EllipticityCorrection1dDefinition(
        FeaturePredictionCorrectionType.ELLIPTICITY_CORRECTION);
  }

  @Override
  public FeaturePredictionComponent computeCorrection(FeaturePredictionCorrectionVisitor visitor)
      throws IOException {
    return visitor.computeCorrection(this);
  }

  @Override
  public abstract FeaturePredictionCorrectionType getCorrectionType();

}
