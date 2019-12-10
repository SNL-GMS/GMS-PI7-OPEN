package gms.core.waveformqc.waveformsignalqc.algorithm;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Finds gaps of missing data in a {@link ChannelSegment}
 */
public class WaveformGapInterpreter {

  private WaveformGapInterpreter() {
  }

  /**
   * Finds missing data gaps in the {@link ChannelSegment} and uses the provided threshold to
   * determine if each gap is a {@link QcMaskType#LONG_GAP} or a {@link QcMaskType#REPAIRABLE_GAP}
   *
   * @param channelSegment find gaps in this {@link ChannelSegment}, not null
   * @param minLongGapLengthInSamples threshold between long and repairable gaps, must be greater
   * than or equal to 1
   * @return list of {@link WaveformGapQcMask} in the channelSegment, not null
   */
  public static List<WaveformGapQcMask> createWaveformGapQcMasks(
      ChannelSegment<Waveform> channelSegment,
      int minLongGapLengthInSamples) {

    Objects.requireNonNull(channelSegment,
        "WaveformGapInterpreter.updateQcMasks requires non-null channelSegment");

    if (minLongGapLengthInSamples < 1) {
      throw new IllegalArgumentException(
          "WaveformGapInterpreter.updateQcMasks requires minLongGapLengthInSamples > 0");
    }

    List<WaveformGapQcMask> masks = new ArrayList<>();

    Instant previousEndTime = channelSegment.getStartTime();
    for (Waveform waveform : channelSegment.getTimeseries()) {

      // Potential gap start and end times
      final Instant gapStart = previousEndTime;
      final Instant gapEnd = waveform.getStartTime();

      // Assuming waveform sample rate is in samples/sec calculate the number of missing samples
      // i.e. missing_ms * (samples/sec * 1sec/1000ms) = missing_samples
      // Subtract one from this to account for expected duration between two samples
      final Duration gapDuration = Duration.between(gapStart, gapEnd);
      final double missingSamples =
          (gapDuration.toMillis() * (waveform.getSampleRate() / 1000.0)) - 1;

      // There is a gap
      if (missingSamples >= 1) {

        final QcMaskType qcMaskType = (missingSamples >= minLongGapLengthInSamples)
            ? QcMaskType.LONG_GAP
            : QcMaskType.REPAIRABLE_GAP;

        masks.add(WaveformGapQcMask
            .create(qcMaskType, channelSegment.getChannelId(), channelSegment.getId(),
                gapStart, gapEnd));
      }

      previousEndTime = waveform.getEndTime();
    }

    return masks;
  }
}
