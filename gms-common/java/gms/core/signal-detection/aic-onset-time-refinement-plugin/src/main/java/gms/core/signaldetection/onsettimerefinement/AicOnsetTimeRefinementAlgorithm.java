package gms.core.signaldetection.onsettimerefinement;

import com.google.common.base.Preconditions;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.utilities.signalprocessing.normalization.DeMeaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

class AicOnsetTimeRefinementAlgorithm {

  private static final Logger logger = LoggerFactory.getLogger(AicOnsetTimeRefinementAlgorithm.class);

  private AicOnsetTimeRefinementAlgorithm() {

  }

  /**
   * Determines the refined onset time of the provided waveform by determining the minimum Akaike
   * Information Criterion based on a Noise-Noise model. More specifically, it calculates the
   * minimum AIC, where the AIC is given by AIC = length(noise) * ln(variance) + length(signal) *
   * ln(signal) and the noise and signal windows are given by splitting the AIC window on a variable
   * that iterates through each sample, beginning with 1 + order and ending with window.length -
   * order.
   *
   * @param waveform The waveform containing the onset time that will be refined.
   * @param previousOnsetTime The previously detected onset time.
   * @param parameters The {@link AicOnsetTimeRefinementParameters} to be used in the algorithm.
   * @return The refined onset time
   */
  public static Instant refineOnsetTime(Waveform waveform, Instant previousOnsetTime,
      AicOnsetTimeRefinementParameters parameters) {
    Objects.requireNonNull(waveform, "Onset time cannot be refined from a null waveform");
    Objects.requireNonNull(previousOnsetTime, "Onset time cannot be refined from a null onset " +
        "time");
    Preconditions.checkState(previousOnsetTime.compareTo(waveform.getStartTime()) >= 0 &&
            previousOnsetTime.compareTo(waveform.getEndTime()) <= 0,
        "Onset time cannot be refined when onset time is outside of waveform's time span");

    Instant startTime = previousOnsetTime.minus(parameters.getNoiseWindowSize());
    Instant endTime = previousOnsetTime.plus(parameters.getSignalWindowSize());

    if (startTime.isBefore(waveform.getStartTime()) || endTime.isAfter(waveform.getEndTime())) {
      logger.info( "Onset time cannot be refined due to insufficient waveform data");
      return previousOnsetTime;
    }

    Waveform aicWindow = waveform.window(startTime, endTime);

    int order = parameters.getOrder();

    double minAic = Double.POSITIVE_INFINITY;
    int minAicIndex = -1;
    double[] demeanedWaveform = DeMeaner.demean(aicWindow.getValues());
    for (int i = 1; i < demeanedWaveform.length - order; i++) {
      double[] noise = new double[i];
      System.arraycopy(demeanedWaveform, order, noise, 0, noise.length);
      double noiseVariance = variance(noise);

      double[] signal = new double[demeanedWaveform.length - (order + i)];
      System.arraycopy(demeanedWaveform, order + i, signal, 0, signal.length);
      double signalVariance = variance(signal);

      double aic =
          noise.length * Math.log(noiseVariance) + signal.length * Math.log(signalVariance);
      if (aic < minAic && aic > Double.NEGATIVE_INFINITY) {
        minAic = aic;
        minAicIndex = i + order;
      }
    }

    if (minAicIndex < 0) {
      return previousOnsetTime;
    }

    return aicWindow.computeSampleTime(minAicIndex);
  }

  private static double variance(double[] window) {
    double mean = mean(window);
    return Arrays.stream(window)
        .map(value -> Math.pow(value - mean, 2))
        .sum() / window.length;
  }

  private static double mean(double[] window) {
    return Arrays.stream(window)
        .sum() / window.length;
  }
}
