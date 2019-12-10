package gms.core.signaldetection.staltapowerdetector;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.core.signaldetection.staltapowerdetector.StaLtaAlgorithm.AlgorithmType;
import gms.core.signaldetection.staltapowerdetector.StaLtaAlgorithm.WaveformTransformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;

/**
 * Parameters used in a call to the STA/LTA {@link StaLtaAlgorithm}
 */
@AutoValue
public abstract class StaLtaParameters {

  /**
   * Obtains the {@link AlgorithmType}
   *
   * @return {@link AlgorithmType}, not null
   */
  public abstract AlgorithmType getAlgorithmType();

  /**
   * Obtains the {@link WaveformTransformation}
   *
   * @return {@link WaveformTransformation}, not null
   */
  public abstract WaveformTransformation getWaveformTransformation();

  /**
   * Obtains the {@link Duration} the STA window leads the transformed sample
   *
   * @return {@link Duration}, not null
   */
  public abstract Duration getStaLead();

  /**
   * Obtains the {@link Duration} of the STA window, {@code > 0}
   *
   * @return {@link Duration}, not null
   */
  public abstract Duration getStaLength();

  /**
   * Obtains the {@link Duration} the LTA window leads the transformed sample
   *
   * @return {@link Duration}, not null
   */
  public abstract Duration getLtaLead();

  /**
   * Obtains the {@link Duration} of the LTA window, {@code > 0}
   *
   * @return {@link Duration}, not null
   */
  public abstract Duration getLtaLength();

  /**
   * Obtains the minimum waveform value not causing a trigger, {@code > 0}
   *
   * @return trigger threshold as a double
   */
  public abstract double getTriggerThreshold();

  /**
   * Obtains the maximum waveform value not causing a detrigger, {@code > 0}
   *
   * @return detrigger threshold as a double
   */
  public abstract double getDetriggerThreshold();

  /**
   * Before STA/LTA, fill gaps within a {@link ChannelSegment} of {@code duration < ltaLength} if
   * the {@link Waveform}s on each side of the gap have a{@link Waveform#getSampleRate()} difference
   * of less than this value
   *
   * @return interpolateGapsSampleRateTolerance as a double
   */
  public abstract double getInterpolateGapsSampleRateTolerance();

  /**
   * Before STA/LTA merge {@link Waveform}s within a {@link ChannelSegment} if their gap is {@code <
   * 1} sample and if the {@link Waveform}s on each side of the gap
   *
   * @return mergeWaveformsSampleRateTolerance as a double
   */
  public abstract double getMergeWaveformsSampleRateTolerance();

  /**
   * Obtains the minimum {@link Duration} (exclusive) of a merged waveform gap.
   *
   * @return {@link Duration}, not null
   */
  public abstract Duration getMergeWaveformsMinLength();

  /**
   * Obtains a new {@link StaLtaParameters} from the provided values.
   *
   * @param algorithmType an {@link AlgorithmType}, not null
   * @param waveformTransformation an {@link WaveformTransformation}, not null
   * @param staLead {@link Duration} the STA window leads the transformed sample, not null
   * @param staLength {@link Duration} of the STA window, {@code > 0}, not null
   * @param ltaLead {@link Duration} of the LTA window leads the transformed sample, not null
   * @param ltaLength {@link Duration} of the LTA window, {@code > 0}, not null
   * @param triggerThreshold minimum waveform value not causing a trigger, {@code > 0}
   * @param detriggerThreshold maximum waveform value not causing a detrigger, {@code > 0}
   * @param interpolateGapsSampleRateTolerance before STA/LTA, fill gaps within a {@link
   * ChannelSegment} of {@code duration < ltaLength} if the {@link Waveform}s on each side of the
   * gap have a {@link Waveform#getSampleRate()} difference of less than this value
   * @param mergeWaveformsSampleRateTolerance before STA/LTA, merge {@link Waveform}s within a
   * {@link ChannelSegment} if their gap is {@code < 1} sample and if the {@link Waveform}s on each
   * side of the gap have a {@link Waveform#getSampleRate()} difference of less than this value
   * @param mergeWaveformsMinLength minimum {@link Duration} of a gap to merge, exclusive, not null
   * @return {@link StaLtaParameters}, not null
   * @throws NullPointerException if algorithmType, waveformTransformation, staLead, staLength,
   * ltaLead, ltaLength, or mergeWaveformsMinLength are null
   * @throws IllegalArgumentException if staLength, ltaLength, triggerThreshold, or
   * detriggerThreshold are {@code <= 0}
   */
  @JsonCreator
  public static StaLtaParameters from(
      @JsonProperty("algorithmType") AlgorithmType algorithmType,
      @JsonProperty("waveformTransformation") WaveformTransformation waveformTransformation,
      @JsonProperty("staLead") Duration staLead,
      @JsonProperty("staLength") Duration staLength,
      @JsonProperty("ltaLead") Duration ltaLead,
      @JsonProperty("ltaLength") Duration ltaLength,
      @JsonProperty("triggerThreshold") double triggerThreshold,
      @JsonProperty("detriggerThreshold") double detriggerThreshold,
      @JsonProperty("interpolateGapsSampleRateTolerance") double interpolateGapsSampleRateTolerance,
      @JsonProperty("mergeWaveformsSampleRateTolerance") double mergeWaveformsSampleRateTolerance,
      @JsonProperty("mergeWaveformsMinLength") Duration mergeWaveformsMinLength) {

    ParameterValidation.validateWindowLengths(staLength, ltaLength, d -> !d.isNegative());
    ParameterValidation.validateTriggerThresholds(triggerThreshold, detriggerThreshold);

    return new AutoValue_StaLtaParameters(algorithmType, waveformTransformation, staLead, staLength,
        ltaLead,
        ltaLength, triggerThreshold, detriggerThreshold, interpolateGapsSampleRateTolerance,
        mergeWaveformsSampleRateTolerance, mergeWaveformsMinLength);
  }
}
