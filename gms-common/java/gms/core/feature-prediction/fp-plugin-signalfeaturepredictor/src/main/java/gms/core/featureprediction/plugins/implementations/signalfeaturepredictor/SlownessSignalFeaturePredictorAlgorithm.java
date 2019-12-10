package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import gms.core.featureprediction.plugins.DepthDistance1dModelSet;
import gms.core.featureprediction.plugins.TravelTime1dPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import org.apache.commons.lang3.Validate;


/**
 * Default algorithm for horizontal slowness in SignalFeaturePredictor plugin
 */
public class SlownessSignalFeaturePredictorAlgorithm {

  private final DepthDistance1dModelSet<double[], double[][]> earthModelsPlugin;
  private final String earthModelName;
  private final PhaseType phase;

  private SlownessSignalFeaturePredictorAlgorithm(
      DepthDistance1dModelSet<double[], double[][]> earthModelsPlugin,
      String earthModelName, PhaseType phase) {
    this.earthModelsPlugin = earthModelsPlugin;
    this.earthModelName = earthModelName;
    this.phase = phase;
  }


  /**
   * Estimate phase horizontal slowness for a given travel time and angular distance.
   *
   * @param travelTimeSec seconds to travel over angular distance
   * @param angleDeg angular degrees between event and receiver as measured from center of earth
   * @return phase horizontal slowness in seconds per degree
   */
  public double getPhaseSlowness(double travelTimeSec, double angleDeg) {
    return travelTimeSec / angleDeg;
  }


  /**
   * A mutable builder for a {@link SlownessSignalFeaturePredictorAlgorithm}.  The builder has two
   * phases. At inception, it is in the build phase in which it can be modified. Once the build()
   * method is called, the {@link Builder} transitions to the built phase, to create the {@link
   * SlownessSignalFeaturePredictorAlgorithm}.  Once the build() method is called, the {@link
   * Builder} can no longer be used.
   */
  public static final class Builder {

    private boolean built = false;

    private DepthDistance1dModelSet<double[], double[][]> depthDistance1dModelSet;
    private String earthModelName;
    private PhaseType phase;

    /**
     * Sets the earth model 1D plugin for {@link SlownessSignalFeaturePredictorAlgorithm}
     *
     * @param depthDistance1dModelSet for slowness estimation
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * SlownessSignalFeaturePredictorAlgorithm}
     */
    public Builder withDepthDistanceModelSet(DepthDistance1dModelSet<double[], double[][]>  depthDistance1dModelSet) {
      if (built) {
        throw new IllegalStateException("DefaultTravelTimeEstimation has already been built");
      }

      this.depthDistance1dModelSet = depthDistance1dModelSet;
      return this;
    }

    /**
     * Sets the name of the earth model for {@link SlownessSignalFeaturePredictorAlgorithm}
     *
     * @param earthModelName for slowness estimation
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * SlownessSignalFeaturePredictorAlgorithm}
     */
    public Builder withEarthModelName(String earthModelName) {
      if (built) {
        throw new IllegalStateException("DefaultTravelTimeEstimation has already been built");
      }

      this.earthModelName = earthModelName;
      return this;
    }

    /**
     * Sets the phase type for {@link SlownessSignalFeaturePredictorAlgorithm}
     *
     * @param phase phase of earth model
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * SlownessSignalFeaturePredictorAlgorithm}
     */
    public Builder withPhaseType(PhaseType phase) {
      if (built) {
        throw new IllegalStateException("DefaultTravelTimeEstimation has already been built");
      }

      this.phase = phase;
      return this;
    }


    /**
     * Builds the {@link SlownessSignalFeaturePredictorAlgorithm} from the parameters defined during
     * the build phase.
     *
     * @return a new {@link SlownessSignalFeaturePredictorAlgorithm}
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * SlownessSignalFeaturePredictorAlgorithm}, or if any parameters are set to illegal values.
     */
    public SlownessSignalFeaturePredictorAlgorithm build() {
      if (built) {
        throw new IllegalStateException("DefaultTravelTimeEstimation has already been built");
      }

      built = true;

      // validate algorithm parameters
      Validate.notNull(depthDistance1dModelSet, "depthDistance1dModelSet is null");
      Validate.notEmpty(earthModelName, "Earth model name is empty");
      Validate.notNull(phase, "PhaseType is null");
      Validate.isTrue(depthDistance1dModelSet.getEarthModelNames().contains(earthModelName),
          "Earth model, " + earthModelName + ", not in earth model 1D plugin set.");
      Validate
          .isTrue(depthDistance1dModelSet.getPhaseTypes(earthModelName).contains(phase),
              "Phase type, " + phase + ", does not exist in earth model, " + earthModelName
                  + ".");

      return new SlownessSignalFeaturePredictorAlgorithm(depthDistance1dModelSet, earthModelName,
          phase);
    }
  }

}