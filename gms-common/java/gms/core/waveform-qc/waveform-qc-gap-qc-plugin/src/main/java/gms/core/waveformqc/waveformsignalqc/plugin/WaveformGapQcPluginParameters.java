package gms.core.waveformqc.waveformsignalqc.plugin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * Parameters for a particular invocation of the {@link WaveformGapQcPlugin}
 */
@AutoValue
public abstract class WaveformGapQcPluginParameters {

  public abstract int getMinLongGapLengthInSamples();

  /**
   * Obtains a {@link WaveformGapQcPluginParameters} from the provided threshold separating long and
   * repairable data gaps
   *
   * @param minLongGapLengthInSamples minimum length of a long gap, must be positive
   * @return WaveformGapQcPluginParameters, not null
   * @throws IllegalArgumentException if minLongGapLengthInSamples is not positive
   */
  @JsonCreator
  public static WaveformGapQcPluginParameters from(
      @JsonProperty("minLongGapLengthInSamples") int minLongGapLengthInSamples) {

    if (minLongGapLengthInSamples <= 0) {
      throw new IllegalArgumentException(
          "WaveformGapQcPluginParameters requires a positive minLongGapLengthInSamples");
    }

    return new AutoValue_WaveformGapQcPluginParameters(minLongGapLengthInSamples);
  }
}
