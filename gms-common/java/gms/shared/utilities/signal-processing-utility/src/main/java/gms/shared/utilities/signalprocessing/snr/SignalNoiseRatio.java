package gms.shared.utilities.signalprocessing.snr;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.utilities.signalprocessing.normalization.DeMeaner;
import gms.shared.utilities.signalprocessing.normalization.Transform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.OptionalDouble;
import org.apache.commons.lang3.Validate;

public class SignalNoiseRatio {

  private static final double BILLION = 1e+9;

  /**
   * Calculates the signal to noise ratio of the provided waveform.
   *
   * @param waveform the waveform for which the signal to noise ratio will be calculated
   * @return the signal to noise ratio for the provided waveform
   */
  public static double getSnr(Waveform waveform,
      Instant noiseWindowStart,
      Instant noiseWindowEnd,
      Instant signalWindowStart,
      Instant signalWindowEnd,
      Duration slidingWindowSize,
      Transform transform) {

    Objects.requireNonNull(waveform,
        "SNR cannot be calculated from a null waveform");
    Objects.requireNonNull(noiseWindowStart,
        "SNR cannot be calculated from a null Noise Window Start");
    Objects.requireNonNull(noiseWindowEnd,
        "SNR cannot be calculated from a null Noise Window End");
    Objects.requireNonNull(signalWindowStart,
        "SNR cannot be calculated from a null Signal Window Start");
    Objects.requireNonNull(signalWindowEnd,
        "SNR cannot be calculated from a null Signal Window End");
    Objects.requireNonNull(slidingWindowSize,
        "SNR cannot be calculated from a null Sliding Window Size");
    Objects.requireNonNull(transform,
        "SNR cannot be calculated from a null Transform");

    Validate.isTrue(noiseWindowStart.isBefore(noiseWindowEnd),
        "Noise Window Start must be before Noise Window End");
    Validate.isTrue(signalWindowStart.isBefore(signalWindowEnd),
        "Signal Window Start must be before Signal Window End");

    boolean signalOverlapsNoise =
        (signalWindowStart.isAfter(noiseWindowStart) &&
            signalWindowStart.isBefore(noiseWindowEnd)) ||
            (signalWindowEnd.isAfter(noiseWindowStart) &&
                signalWindowEnd.isBefore(noiseWindowEnd));

    Validate.isTrue(!signalOverlapsNoise, "Noise and Signal Windows cannot overlap");

    Validate.isTrue(!noiseWindowStart.isBefore(waveform.getStartTime()),
        "Noise window cannot start before the waveform start time");
    Validate.isTrue(!noiseWindowEnd.isAfter(waveform.getEndTime()),
        "Noise window cannot end after the waveform end time");
    Validate.isTrue(!signalWindowStart.isBefore(waveform.getStartTime()),
        "Signal window cannot start before the waveform start time");
    Validate.isTrue(!signalWindowEnd.isAfter(waveform.getEndTime()),
        "Signal window cannot end after the waveform end time");

    Validate.isTrue(Duration.between(signalWindowStart, signalWindowEnd).compareTo(slidingWindowSize) >= 0,
        "Sliding window cannot be larger than the signal window");

    Waveform demeanedWaveform = Waveform.withValues(waveform.getStartTime(),
        waveform.getSampleRate(),
        DeMeaner.demean(waveform.getValues()));

    Waveform noiseWaveform = demeanedWaveform.trim(noiseWindowStart, noiseWindowEnd);
    Waveform signalWaveform = demeanedWaveform.trim(signalWindowStart, signalWindowEnd);

    OptionalDouble noiseAverage = Arrays.stream(noiseWaveform.getValues())
        .map(transform.getTransformFunction())
        .average();

    double signalAverage = calculateMaxSlidingAverage(signalWaveform,
        slidingWindowSize,
        transform);

    if (noiseAverage.isPresent()) {
      return signalAverage / noiseAverage.getAsDouble();
    } else {
      throw new IllegalArgumentException("Cannot calculate SNR when noise average " +
          "cannot be calculated");
    }
  }

  /**
   * Calculates the maximum sliding average for a waveform
   *
   * @param window The waveform for which the maximum sliding average will be calculated
   * @param slidingWindowSize The size of the sliding window over which to calculate the average
   * @param transform The function to apply to the individual datapoints before calculating the
   * average
   * @return The maximum of all the averages of the sliding windows in the waveform;
   */
  private static double calculateMaxSlidingAverage(Waveform window,
      Duration slidingWindowSize,
      Transform transform) {

    Instant startTime = window.getStartTime();
    Instant endTime = window.getStartTime().plusNanos(slidingWindowSize.toNanos());

    double maxAverage = Double.MIN_VALUE;
    long nanosecondsBetweenSamples = (long) (window.getSamplePeriod() * BILLION);

    while (endTime.isBefore(window.getEndTime()) || endTime.equals(window.getEndTime())) {

      Waveform averageWindow = window.window(startTime, endTime);

      OptionalDouble possibleAverage = Arrays.stream(averageWindow.getValues())
          .map(transform.getTransformFunction())
          .average();

      if (possibleAverage.isPresent() && possibleAverage.getAsDouble() > maxAverage) {
        maxAverage = possibleAverage.getAsDouble();
      }

      startTime = startTime.plusNanos(nanosecondsBetweenSamples);
      endTime = endTime.plusNanos(nanosecondsBetweenSamples);
    }

    return maxAverage;
  }
}
