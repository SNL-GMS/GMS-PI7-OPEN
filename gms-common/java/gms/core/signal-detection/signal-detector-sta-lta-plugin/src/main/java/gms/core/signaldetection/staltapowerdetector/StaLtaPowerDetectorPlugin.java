package gms.core.signaldetection.staltapowerdetector;

import gms.core.signaldetection.plugin.SignalDetectorPlugin;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility.WaveformUtility;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps algorithm logic to perform sta/lta power detection on waveforms
 */
public class StaLtaPowerDetectorPlugin implements SignalDetectorPlugin {

  private static final String PLUGIN_NAME = "staLtaPowerDetectorPlugin";

  private static final Logger logger = LoggerFactory.getLogger(StaLtaPowerDetectorPlugin.class);

  private StaLtaAlgorithm staLta = new StaLtaAlgorithm();

  @Override
  public String getName() {
    return PLUGIN_NAME;
  }

  public StaLtaAlgorithm getAlgorithm() {
    return staLta;
  }

  public void setAlgorithm(StaLtaAlgorithm staLtaAlgorithm) {
    staLta = staLtaAlgorithm;
  }

  /**
   * Executes the STA/LTA signal detector {@link StaLtaAlgorithm} on the {@link ChannelSegment} with
   * parameters deserializable to {@link StaLtaParameters} provided as a field map.
   *
   * @param channelSegment {@link ChannelSegment} to process for STA/LTA detections, not null
   * @return Set of {@link Instant} signal detection times, not null
   */
  @Override
  public Collection<Instant> detectSignals(ChannelSegment<Waveform> channelSegment,
      Map<String, Object> pluginParams) {

    logger.info("Executing STA/LTA signal detection on ChannelSegment");

    Objects.requireNonNull(channelSegment, "STA/LTA plugin cannot process null channelSegment");
    Objects.requireNonNull(pluginParams, "STA/LTA plugin cannot process null pluginParams");

    StaLtaParameters parameters = ObjectSerialization
        .fromFieldMap(pluginParams, StaLtaParameters.class);

    // Assuming each Waveform has a sample rate within a small delta of a nominal sample rate,
    // just use the first Waveform's sample rate to convert from time to sample counts
    final double presumedNominalSampleRate = channelSegment.getTimeseries().stream()
        .mapToDouble(Waveform::getSampleRate).findFirst().orElse(0.0);
    final double maxInterpolatedGapSampleCount = fractionalSamplesFromDuration(
        presumedNominalSampleRate, parameters.getLtaLength());

    // Condition channelSegment
    final List<Waveform> conditioned = WaveformUtility
        .interpolateWaveformGap(channelSegment.getTimeseries(),
            parameters.getInterpolateGapsSampleRateTolerance(),
            maxInterpolatedGapSampleCount);

    final List<Waveform> merged = WaveformUtility.mergeWaveforms(conditioned,
        parameters.getMergeWaveformsSampleRateTolerance(),
        fractionalSamplesFromDuration(presumedNominalSampleRate,
            parameters.getMergeWaveformsMinLength()));

    // Waveform conditioning merges waveforms where possible, so process each waveform independently
    final Set<Instant> triggerTimes = new HashSet<>();

    merged.forEach(wf -> {

      // Use sampleRate to convert the STA and LTA window parameters from Durations to sample counts
      final double samplesPerSec = wf.getSampleRate();
      final int staLead = samplesFromDuration(samplesPerSec, parameters.getStaLead());
      final int staLength = samplesFromDuration(samplesPerSec, parameters.getStaLength());
      final int ltaLead = samplesFromDuration(samplesPerSec, parameters.getLtaLead());
      final int ltaLength = samplesFromDuration(samplesPerSec, parameters.getLtaLength());

      // Invoke STA/LTA and convert trigger indices to trigger times
      final Set<Integer> triggerIndices = this.staLta
          .staLta(parameters.getAlgorithmType(), parameters.getWaveformTransformation(),
              staLead, staLength, ltaLead, ltaLength, parameters.getTriggerThreshold(),
              parameters.getDetriggerThreshold(), wf.getValues());

      triggerIndices.stream().map(wf::computeSampleTime).forEach(triggerTimes::add);
    });

    return triggerTimes;
  }

  /**
   * Computes the closest integer number of samples occurring at samplesPerSec required to span the
   * duration.
   *
   * @param samplesPerSec sample rate
   * @param duration a {@link Duration}
   * @return integer number of samples
   */
  private static int samplesFromDuration(double samplesPerSec, Duration duration) {
    return (int) Math.round(fractionalSamplesFromDuration(samplesPerSec, duration));
  }

  /**
   * Computes the number of samples (including fractions of samples) occurring at samplesPerSec
   * required to span the duration.
   *
   * @param samplesPerSec sample rate
   * @param duration a {@link Duration}
   * @return double number of samples
   */
  private static double fractionalSamplesFromDuration(double samplesPerSec, Duration duration) {
    final double secondsPart = samplesPerSec * duration.getSeconds();
    final double nanosPart = samplesPerSec * duration.getNano() / 1.0e9;
    return secondsPart + nanosPart;
  }
}
