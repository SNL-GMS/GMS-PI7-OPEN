package gms.core.signaldetection.snronsettimeuncertainty;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;

public class TestFixtures {

  static final Waveform WAVEFORM;
  static final Waveform LOW_SNR_WAVEFORM;
  static final Waveform HIGH_SNR_WAVEFORM;
  static final double EXPECTED_UNCERTAINTY = 0.1872137;
  static final Duration EXPECTED_UNCERTAINTY_DURATION = Duration
      .ofNanos((long) (EXPECTED_UNCERTAINTY * 1E9));
  static final Instant PICK;

  static {
    // reverse engineer a waveform with an snr inside the snr window (5 - 50)
    // find the values of the demeaned waveform, assuming the noise values are all 1, and
    // the signal window is as described below;
    double[] waveformValues = new double[140];
    double desiredSnr = 40.0;
    double[] signalWindow = new double[]{2, 2, 3, 3, -3, -3, -2, -2};
    double signalSlidingAverageMax = 3.0;
    double noiseAverage = signalSlidingAverageMax / desiredSnr;
    double originalMean = 1.0;

    // build the waveform noise values, including the first two of the sample windows
    for (int i = 0; i < waveformValues.length - 10; i++) {
      // add mean back to un-demean the waveform, taken into account transform used on the waveform
      // when calculating SNR
      if (i % 2 == 0) {
        waveformValues[i] = noiseAverage + originalMean;
      } else {
        waveformValues[i] = -noiseAverage + originalMean;
      }
    }

    // build the signal window
    for (int i = waveformValues.length - 10, j = 0;
        i < waveformValues.length - 2 && j < signalWindow.length; i++, j++) {
      waveformValues[i] = signalWindow[j] + originalMean;
    }

    waveformValues[137] = noiseAverage + originalMean;
    waveformValues[138] = -noiseAverage + originalMean;
    waveformValues[139] = noiseAverage + originalMean;

    WAVEFORM = Waveform.from(Instant.EPOCH, 2, 140, waveformValues);

    PICK = WAVEFORM.computeSampleTime(133);

    double[] lowSnrWaveformValues = new double[140];
    for (int i = 0; i < lowSnrWaveformValues.length; i++) {
      if (i % 2 == 0) {
        lowSnrWaveformValues[i] = noiseAverage + originalMean;
      } else {
        lowSnrWaveformValues[i] = -noiseAverage + originalMean;
      }
    }

    LOW_SNR_WAVEFORM = Waveform.from(Instant.EPOCH, 2, 140, lowSnrWaveformValues);

    double[] highSnrWaveformValues = new double[140];
    for (int i = 0; i < highSnrWaveformValues.length - 10; i++) {
      highSnrWaveformValues[i] = 1;
    }

    highSnrWaveformValues[130] = 2;
    highSnrWaveformValues[131] = 2;
    highSnrWaveformValues[132] = 3;
    highSnrWaveformValues[133] = 3;
    highSnrWaveformValues[134] = -3;
    highSnrWaveformValues[135] = -3;
    highSnrWaveformValues[136] = -2;
    highSnrWaveformValues[137] = -2;
    highSnrWaveformValues[138] = 1;
    highSnrWaveformValues[139] = 1;

    HIGH_SNR_WAVEFORM = Waveform.from(Instant.EPOCH, 2, 140, highSnrWaveformValues);
  }
}
