package gms.core.waveformqc.waveformsignalqc.plugin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * Parameters for a particular invocation of the {@link WaveformSpike3PtQcPlugin}
 */
@AutoValue
public abstract class WaveformSpike3PtQcPluginParameters {

  /**
   * Threshold defining a spike when the ratio of minimum absolute difference of adjacent samples
   * with the maximum absolute difference is less than this threshold.
   *
   * @return a positive integer
   */
  public abstract double getMinConsecutiveSampleDifferenceSpikeThreshold();

  public abstract double getRmsAmplitudeRatioThreshold();

  public abstract int getRmsLeadSampleDifferences();

  public abstract int getRmsLagSampleDifferences();

  /**
   * Obtains a {@link WaveformSpike3PtQcPluginParameters} from the provided threshold defining a
   * spike between 3 consecutive points.
   *
   * @param minConsecutiveSampleDifferenceSpikeThreshold minimum threshold for a spike, must be
   * positive
   * @return WaveformSpike3PtQcPluginParameters, not null
   * @throws IllegalArgumentException if minConsecutiveSampleDifferenceSpikeThreshold is not
   * positive
   */
  @JsonCreator
  public static WaveformSpike3PtQcPluginParameters from(
      @JsonProperty("minConsecutiveSampleDifferenceSpikeThreshold") double minConsecutiveSampleDifferenceSpikeThreshold,
      @JsonProperty("rmsAmplitudeRatioThreshold") double rmsAmplitudeRatioThreshold,
      @JsonProperty("rmsLeadSampleDifferences") int rmsLeadSampleDifferences,
      @JsonProperty("rmsLagSampleDifferences") int rmsLagSampleDifferences) {

    if (minConsecutiveSampleDifferenceSpikeThreshold <= 0
        || minConsecutiveSampleDifferenceSpikeThreshold >= 1.0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginParameters requires minConsecutiveSampleDifferenceSpikeThreshold > 0.0 and < 1.0");
    }

    if (rmsLeadSampleDifferences < 0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginParameters requires rmsLeadSampleDifferences >= 0");
    }

    if (rmsLagSampleDifferences < 0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginParameters requires rmsLagSampleDifferences >= 0");
    }

    if (rmsLeadSampleDifferences + rmsLagSampleDifferences < 2) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginParameters requires (rmsLeadSampleDifferences + rmsLagSampleDifferences) >= 2");
    }

    if (rmsAmplitudeRatioThreshold <= 1.0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtQcPluginParameters requires rmsAmplitudeRatioThreshold > 1.0");
    }

    return new AutoValue_WaveformSpike3PtQcPluginParameters(
        minConsecutiveSampleDifferenceSpikeThreshold,
        rmsAmplitudeRatioThreshold, rmsLeadSampleDifferences, rmsLagSampleDifferences);
  }
}
