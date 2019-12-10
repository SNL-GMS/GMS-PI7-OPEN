package gms.core.waveformqc.waveformsignalqc.plugin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * Parameters for a particular invocation of the {@link WaveformRepeatedAmplitudeQcPlugin}
 */
@AutoValue
public abstract class WaveformRepeatedAmplitudeQcPluginParameters {

  public abstract int getMinSeriesLengthInSamples();

  public abstract double getMaxDeltaFromStartAmplitude();

  public abstract double getMaskMergeThresholdSeconds();

  /**
   * Obtains a {@link WaveformRepeatedAmplitudeQcPluginParameters} from the provided minimum
   * repeated amplitude series length and the maximum sample deviation from the series' initial
   * amplitude
   *
   * @param minSeriesLengthInSamples minimum number of samples in a repeated adjacent amplitude
   * values mask, {@code > 1}
   * @param maxDeltaFromStartAmplitude maximum deviation a sample may have from the repeated
   * adjacent amplitude series' initial amplitude value to still be considered a repeated value,
   * {@code >= 0.0}
   * @param maskMergeThresholdSeconds exclusive duration (i.e. time difference between masks must be
   * less than this duration) between two repeated adjacent amplitude value masks that should be
   * merged into a single mask, {@code >= 0.0}
   * @return WaveformRepeatedAmplitudeQcPluginParameters, not null
   * @throws IllegalArgumentException if {@code minSeriesLengthInSamples is <= 1;
   * maxDeltaFromStartAmplitude is < 0.0; or maskMergeThresholdSeconds is < 0.0 }
   */
  @JsonCreator
  public static WaveformRepeatedAmplitudeQcPluginParameters from(
      @JsonProperty("minSeriesLengthInSamples") int minSeriesLengthInSamples,
      @JsonProperty("maxDeltaFromStartAmplitude") double maxDeltaFromStartAmplitude,
      @JsonProperty("maskMergeThresholdSeconds") double maskMergeThresholdSeconds) {

    if (minSeriesLengthInSamples <= 1) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcPluginParameters requires minSeriesLengthInSamples >= 2");
    }

    if (maxDeltaFromStartAmplitude < 0.0) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcPluginParameters requires maxDeltaFromStartAmplitude >= 0.0");
    }

    if (maskMergeThresholdSeconds < 0.0) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeQcPluginParameters requires maskMergeThresholdSeconds >= 0.0");
    }

    return new AutoValue_WaveformRepeatedAmplitudeQcPluginParameters(minSeriesLengthInSamples,
        maxDeltaFromStartAmplitude, maskMergeThresholdSeconds);
  }
}
