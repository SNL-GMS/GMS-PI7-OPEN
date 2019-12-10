package gms.core.signaldetection.snronsettimeuncertainty;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.utilities.signalprocessing.snr.SignalNoiseRatio;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SnrOnsetTimeUncertaintyAlgorithm {

  private static final Logger logger = LoggerFactory.getLogger(SnrOnsetTimeUncertaintyAlgorithm.class);

  private SnrOnsetTimeUncertaintyAlgorithm() {

  }

  /**
   * Calculates the onset time uncertainty for the provided waveform, using the definied parameters
   * and onset time estimation
   * @param waveform The Waveform (not null) containing the data and onset time used to calculate
   * the uncertainty
   * @param onsetTime The Instant (not null) for which the uncertainty will be calculated
   * @param parameters The parameters (not null) for the algorithm to use when calculating the
   * uncertainty
   * @return the calculated onset time uncertainty for the waveform and onset time
   */
  public static double calculateUncertainty(Waveform waveform,
      Instant onsetTime,
      SnrOnsetTimeUncertaintyParameters parameters) {

    Instant start = Instant.now();

    Objects.requireNonNull(waveform,
        "Onset time uncertainty cannot be calculated from a null waveform");
    Objects.requireNonNull(onsetTime,
        "Onset time uncertainty cannot be calculated from a null onset time");
    Objects.requireNonNull(parameters,
        "Onset time uncertainty cannot be calculated from null parameters");

    Validate.isTrue(onsetTime.isAfter(waveform.getStartTime()) &&
        onsetTime.isBefore(waveform.getEndTime()),
        "Onset time uncertainty cannot be calculated when onset time is outside the " +
            "waveform window");

     // Determine the start and end times of the noise and signal windows for the waveform,
    // relying on waveform to handle sample time snapping
    Instant noiseWindowStart = onsetTime.minus(parameters.getNoiseWindowOffset());
    Instant noiseWindowEnd = noiseWindowStart.plus(parameters.getNoiseWindowSize());
    Instant signalWindowStart = onsetTime.minus(parameters.getSignalWindowOffset());
    Instant signalWindowEnd = signalWindowStart.plus(parameters.getSignalWindowSize());

    if (noiseWindowStart.isBefore(waveform.getStartTime()) ||
        noiseWindowEnd.isAfter(waveform.getEndTime()) ||
        signalWindowStart.isBefore(waveform.getStartTime()) ||
        signalWindowEnd.isAfter(waveform.getEndTime())) {
      logger.info(
          "Waveform bounds are insufficient for calculation of SNR with configured windows. Defaulting to configured max SNR.");
      return parameters.getMaxTimeUncertainty();
    }

    double snr = SignalNoiseRatio.getSnr(waveform,
        noiseWindowStart,
        noiseWindowEnd,
        signalWindowStart,
        signalWindowEnd,
        parameters.getSlidingWindowSize(),
        parameters.getTransform());

    double maxTimeUncertainty = parameters.getMaxTimeUncertainty();
    double minTimeUncertainty = parameters.getMinTimeUncertainty();
    double maxSnr = parameters.getMaxSnr();
    double minSnr = parameters.getMinSnr();

    if (snr < minSnr) {
      return maxTimeUncertainty;
    }

    if (snr > maxSnr){
      return minTimeUncertainty;
    }

    Instant finish = Instant.now();
    String message = String.format("execution time: %d ms", Duration.between(start, finish).toMillis());
    logger.info(message);
    if (minSnr >= 1.0) {
      double timeUncertaintyRange = maxTimeUncertainty - minTimeUncertainty;
      return Math.max(minTimeUncertainty,
          maxTimeUncertainty -
              (timeUncertaintyRange * Math.log10(snr / minSnr) / Math.log10(maxSnr / minSnr)));
    } else {
      return maxTimeUncertainty;
    }
  }

}
