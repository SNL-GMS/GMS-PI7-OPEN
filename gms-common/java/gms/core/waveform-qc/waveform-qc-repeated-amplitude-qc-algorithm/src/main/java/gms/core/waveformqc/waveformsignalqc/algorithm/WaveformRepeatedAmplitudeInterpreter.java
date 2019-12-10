package gms.core.waveformqc.waveformsignalqc.algorithm;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Finds waveform sections containing repeated adjacent amplitude values.  Two parameters define a
 * repeated amplitude value: 1. n, the number of repeated samples 2. d, the maximum amplitude
 * difference between two samples considered a repeated value
 *
 * This algorithm searches for series of samples {@code s_i, s_i+1, ... of length >= n } where all
 * samples are within d of s_i.  That is, all samples in the repeated series must be within a
 * defined distance of the first sample in the series.
 */
public class WaveformRepeatedAmplitudeInterpreter {

  /**
   * Obtains a new {@link WaveformRepeatedAmplitudeInterpreter}
   */
  public WaveformRepeatedAmplitudeInterpreter() {
  }

  /**
   * Searches the channelSegment for series of length minRepeatedSamples or greater where all values
   * in the series are within maxDeltaFromStartAmplitude of the first value in the series.
   *
   * @param channelSegment {@link ChannelSegment} to search for repeated amplitudes, not null
   * @param minRepeatedSamples minimum number of repeated adjacent values in a series, {@code > 1 }
   * @param maxDeltaFromStartAmplitude maximum difference between a sample and the first sample in
   * the series that is considered a repeat, {@code >= 0.0}
   * @return List of {@link WaveformRepeatedAmplitudeQcMask} found in the channelSegment, not null
   * @throws NullPointerException if channelSegment is null
   * @throws IllegalArgumentException if {@code minRepeatedSamples is <= 1 or
   * maxDeltaFromStartAmplitude < 0.0 }
   */
  public List<WaveformRepeatedAmplitudeQcMask> createWaveformRepeatedAmplitudeQcMasks(
      ChannelSegment<Waveform> channelSegment, int minRepeatedSamples,
      double maxDeltaFromStartAmplitude) {

    Objects.requireNonNull(channelSegment,
        "WaveformRepeatedAmplitudeInterpreter.createWaveformRepeatedAmplitudeQcMasks requires non-null channelSegment");

    if (minRepeatedSamples <= 1) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeInterpreter.createWaveformRepeatedAmplitudeQcMasks requires minRepeatedSamples > 1");
    }

    if (maxDeltaFromStartAmplitude < 0.0) {
      throw new IllegalArgumentException(
          "WaveformRepeatedAmplitudeInterpreter.createWaveformRepeatedAmplitudeQcMasks requires maxDeltaFromStartAmplitude >= 0.0");
    }

    // A function to find repeated adjacent amplitude values in a waveform using the repeated
    // adjacent amplitude definition parameters provided to this operation
    Function<Waveform, Stream<WaveformRepeatedAmplitudeQcMask>> findRepeatsClosure =
        w -> findRepeats(channelSegment, w, minRepeatedSamples, maxDeltaFromStartAmplitude);

    // Find repeated adjacent amplitude values in each waveform
    return channelSegment.getTimeseries().stream()
        .flatMap(findRepeatsClosure)
        .collect(Collectors.toList());
  }

  /**
   * Finds series of repeated adjacent amplitude values in the waveform
   *
   * @param channelSegment {@link ChannelSegment} containing the waveform to search for repeated
   * adjacent amplitudes, not null
   * @param waveform {@link Waveform} to search for repeated adjacent amplitudes, not null
   * @param minRepeatedSamples minimum number of repeated adjacent values in a series, {@code > 1}
   * @param maxDeltaFromStartAmplitude maximum difference between a sample and the first sample in
   * the series that is considered a repeat, {@code >= 0.0 }
   * @return Stream of {@link WaveformRepeatedAmplitudeQcMask} found in the waveform
   */
  private static Stream<WaveformRepeatedAmplitudeQcMask> findRepeats(
      ChannelSegment<Waveform> channelSegment,
      Waveform waveform, int minRepeatedSamples, double maxDeltaFromStartAmplitude) {

    final double[] values = waveform.getValues();

    // Given a start amplitude, create a predicate to determine if values[i] is within
    // maxDeltaFromStartAmplitude
    final Function<Double, IntPredicate> outsideThresholdClosure =
        startValue -> i -> Math.abs(values[i] - startValue) > maxDeltaFromStartAmplitude;

    List<WaveformRepeatedAmplitudeQcMask> masks = new ArrayList<>();
    int currentStart = 0;
    while (currentStart < values.length - minRepeatedSamples) {
      // Find the index of the first value not in a repeated amplitude series from values[i]
      int firstBeyondThreshold = IntStream.range(currentStart + 1, values.length)
          .filter(outsideThresholdClosure.apply(values[currentStart]))
          .findFirst()
          .orElse(values.length);

      // Create a WaveformRepeatedAmplitudeQcMask if a series was found
      final int numWithinThresold = firstBeyondThreshold - currentStart;
      if (numWithinThresold >= minRepeatedSamples) {
        masks.add(WaveformRepeatedAmplitudeQcMask.create(
            waveform.computeSampleTime(currentStart),
            waveform.computeSampleTime(firstBeyondThreshold - 1),
            channelSegment.getChannelId(), channelSegment.getId()));
      }

      currentStart = firstBeyondThreshold;
    }

    return masks.stream();
  }
}
