package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.io.IOException;


/**
 * Implementation of {@link FeaturePredictionCorrection} for elevation correction
 */
@AutoValue
public abstract class ElevationCorrection1dDefinition implements FeaturePredictionCorrection {

  /**
   * Static factory method returning a new {@link ElevationCorrection1dDefinition}
   *
   * @param useGlobalVelocity true if using global medium velocity.  false if phase specific.
   * @return a {@link ElevationCorrection1dDefinition}
   */
  @JsonCreator
  public static ElevationCorrection1dDefinition create(
      @JsonProperty("usingGlobalVelocity") boolean useGlobalVelocity
  ) {
    return new AutoValue_ElevationCorrection1dDefinition(
        useGlobalVelocity,
        FeaturePredictionCorrectionType.ELEVATION_CORRECTION
    );
  }

  @Override
  public FeaturePredictionComponent computeCorrection(FeaturePredictionCorrectionVisitor visitor)
      throws IOException {
    return visitor.computeCorrection(this);
  }

  /**
   * Is elevation corrected using a global medium velocity or is it phase specific?
   *
   * @return true if using global medium velocity.  false if phase specific.
   */
  public abstract boolean isUsingGlobalVelocity();

  @Override
  public abstract FeaturePredictionCorrectionType getCorrectionType();

}
