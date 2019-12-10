package gms.core.signaldetection.staltapowerdetector;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the STA/LTA algorithm (both standard and recursive).
 */
public class StaLtaAlgorithm {

  static final Logger logger = LoggerFactory.getLogger(StaLtaAlgorithm.class);

  /**
   * Type of STA/LTA algorithm implementation
   */
  public enum AlgorithmType {
    STANDARD,
    RECURSIVE
  }

  /**
   * Transformation applied to the input waveform samples prior to STA/LTA
   */
  public enum WaveformTransformation {
    RECTIFIED(Math::abs),
    SQUARED(d -> d * d);

    /**
     * {@link DoubleUnaryOperator} implementing the WaveformTransformation
     */
    private DoubleUnaryOperator operation;

    WaveformTransformation(DoubleUnaryOperator operation) {
      this.operation = operation;
    }

    /**
     * Obtains the waveform sample transformation operation implementing this {@link
     * WaveformTransformation}
     *
     * @return {@link DoubleUnaryOperator}, not null
     */
    public DoubleUnaryOperator getOperation() {
      return operation;
    }
  }

  /**
   * Computes STA/LTA trigger indices on waveform.  Transforms each data sample according to the
   * {@link WaveformTransformation}, computes STA/LTA using the STA and LTA windows defined by the
   * window lead and length parameters, and finds the waveform indices where the STA/LTA average
   * exceeds triggerThreshold at the beggining of the waveform or after previously descending below
   * detriggerThreshold.
   *
   * @param algorithmType an {@link AlgorithmType}, not null
   * @param waveformTransformation an {@link WaveformTransformation}, not null
   * @param staLeadSamples number of samples the STA window leads the transformed sample
   * @param staLengthSamples length of the STA window, {@code > 0 }
   * @param ltaLeadSamples number of samples the LTA window leads the transformed sample
   * @param ltaLengthSamples length of the LTA window, {@code > 0 }
   * @param triggerThreshold minimum waveform value not causing a trigger, {@code > 0 }
   * @param detriggerThreshold maximum waveform value not causing a detrigger, {@code > 0 }
   * @param waveform double[] of data to transform, not null
   * @return Set of signal detection sample indices, not null
   * @throws NullPointerException if waveform, algorithmType, or waveformTransformation are null
   * @throws IllegalArgumentException if STA or LTA window length {@code <= 0 };
   * @throws IllegalArgumentException if algorithmType is {@link AlgorithmType#RECURSIVE}
   */
  public Set<Integer> staLta(AlgorithmType algorithmType,
      WaveformTransformation waveformTransformation, int staLeadSamples, int staLengthSamples,
      int ltaLeadSamples, int ltaLengthSamples, double triggerThreshold, double detriggerThreshold,
      double[] waveform) {

    // Parameter validation.  Additional validation occurs in the transform and trigger operations
    Objects.requireNonNull(waveform, "STA/LTA cannot operate on a null waveform");
    Objects.requireNonNull(algorithmType, "STA/LTA requires non-null algorithmType");
    Objects
        .requireNonNull(waveformTransformation, "STA/LTA requires non-null waveformTransformation");

    // Compute triggers on the transformed waveform
    final Set<Integer> triggers = trigger(triggerThreshold, detriggerThreshold,
        transform(algorithmType, waveformTransformation, staLeadSamples, staLengthSamples,
            ltaLeadSamples, ltaLengthSamples, waveform));

    // Convert trigger sample indices to corresponding input waveform sample indices
    final int firstTransformed = firstTransformedIndex(staLeadSamples, ltaLeadSamples);
    return triggers.stream().map(i -> i + firstTransformed).collect(Collectors.toSet());
  }

  /**
   * Obtain a transformed STA/LTA waveform. First transforms the input waveform based on
   * waveformTransformation, then computes the STA/LTA waveform using the algorithmType.
   *
   * Output waveform is shorter than the input waveform since the STA/LTA transform at sample i
   * requires MAX(staLeadSamples, ltaLeadSamples) of startup and MAX([staLengthSamples -
   * staLeadSamples - 1], [ltaLengthSamples - ltaLeadSamples - 1]) of lag samples.
   *
   * The length of the output waveform is: waveform.length - startupSamples - lagSamples
   *
   * @param algorithmType an {@link AlgorithmType}, not null
   * @param waveformTransformation an {@link WaveformTransformation}, not null
   * @param staLeadSamples number of samples the STA window leads the transformed sample
   * @param staLengthSamples length of the STA window, > 0
   * @param ltaLeadSamples number of samples the LTA window leads the transformed sample
   * @param ltaLengthSamples length of the LTA window, > 0
   * @param waveform double[] of data to transform, not null
   * @return {@link DoubleStream} containing the STA/LTA transformed waveform, not null
   * @throws IllegalArgumentException if STA or LTA window length <= 0;
   * @throws IllegalArgumentException if algorithmType is {@link AlgorithmType#RECURSIVE}
   */
  static DoubleStream transform(AlgorithmType algorithmType,
      WaveformTransformation waveformTransformation, int staLeadSamples, int staLengthSamples,
      int ltaLeadSamples, int ltaLengthSamples, double[] waveform) {
    // Validate the STA and LTA windows
    ParameterValidation.validateWindowLengths(staLengthSamples, ltaLengthSamples, i -> i > 0);

    // TODO: if demeaning is required then the WaveformTransformation needs to be applied within
    // the STA/LTA loop since demeaning uses the samples used to compute a single STA/LTA point.

    // Compute the transformed input waveform (rectified, squared, etc.)
    double[] transformedWaveform = DoubleStream.of(waveform)
        .map(waveformTransformation.getOperation()).toArray();

    if (AlgorithmType.STANDARD == algorithmType) {
      return standardTransform(staLeadSamples, staLengthSamples,
          ltaLeadSamples, ltaLengthSamples, transformedWaveform);
    } else {
      throw new IllegalArgumentException("Recursive STA/LTA not implemented");
    }
  }

  /**
   * Compute the standard STA/LTA transform on waveform with the provided STA and LTA windows
   *
   * @param staLeadSamples number of samples the STA window leads the transformed sample
   * @param staLengthSamples length of the STA window
   * @param ltaLeadSamples number of samples the LTA window leads the transformed sample
   * @param ltaLengthSamples length of the LTA window
   * @param waveform double[] of data to transform, not null
   * @return DoubleStream containing the STA/LTA transformed waveform, not null
   */
  private static DoubleStream standardTransform(int staLeadSamples, int staLengthSamples,
      int ltaLeadSamples, int ltaLengthSamples, double[] waveform) {

    Instant start = Instant.now();
    // TODO: consider refactoring when implementing recursive algorithm to avoid duplication

    // Compute waveform STA and LTA over the defined windows
    IntToDoubleFunction sta = getAvgClosure(staLeadSamples, staLengthSamples, waveform);
    IntToDoubleFunction lta = getAvgClosure(ltaLeadSamples, ltaLengthSamples, waveform);

    // Compute STA/LTA for each sample.
    final int firstSample = firstTransformedIndex(staLeadSamples, ltaLeadSamples);
    final int lastSample = lastTransformedIndex(staLeadSamples, staLengthSamples, ltaLeadSamples,
        ltaLengthSamples, waveform.length);

    Instant finish = Instant.now();

    String message = String.format("standardTransform() execution time: %d ms", Duration.between(start, finish).toMillis());
    logger.info(message);
    return IntStream.rangeClosed(firstSample, lastSample)
        .mapToDouble(i -> sta.applyAsDouble(i) / lta.applyAsDouble(i));
  }

  /**
   * Obtain an (int -> double) function which computes an average value from the provided waveform
   * values based on the integer parameter, leadSamples before the integer, and numSamples in the
   * average.
   *
   * @param leadSamples number of samples the average window leads the input sample
   * @param numSamples number of samples in the average window
   * @param waveform data values
   * @return {@link IntToDoubleFunction} computing waveform average value in a window around the
   * sample index
   */
  private static IntToDoubleFunction getAvgClosure(int leadSamples, int numSamples,
      double[] waveform) {

    return i -> IntStream.rangeClosed(i - leadSamples, i - leadSamples + numSamples - 1)
        .mapToDouble(j -> waveform[j]).sum() / numSamples;
  }

  /**
   * Finds the first index that will be transformed given the number of leading samples in the
   * STA and LTA windows
   *
   * @param staLeadSamples number of samples the STA window leads the transformed sample
   * @param ltaLeadSamples number of samples the LTA window leads the transformed sample
   * @return integer >= 0
   */
  private static int firstTransformedIndex(int staLeadSamples, int ltaLeadSamples) {
    return Math.max(staLeadSamples, ltaLeadSamples);
  }

  /**
   * Finds the last index in a waveform of the provided length that will be transformed given the
   * provided STA and LTA windows.  This is not necessarily the last sample in the waveform since
   * the windows may extend beyond the sample index.
   *
   * @param staLeadSamples number of samples the STA window leads the transformed sample
   * @param staLengthSamples STA window length
   * @param ltaLeadSamples number of samples the LTA window leads the transformed sample
   * @param ltaLengthSamples LTA window length
   * @param waveformLength input waveform length
   * @return integer <= waveformLength - 1
   */
  private static int lastTransformedIndex(int staLeadSamples, int staLengthSamples,
      int ltaLeadSamples, int ltaLengthSamples, int waveformLength) {

    final int numStaLagSamples = numLagSamples(staLeadSamples, staLengthSamples, waveformLength);
    final int numLtaLagSamples = numLagSamples(ltaLeadSamples, ltaLengthSamples, waveformLength);
    final int numLagSamples = Math.max(numStaLagSamples, numLtaLagSamples);

    return (numLagSamples > 0) ? waveformLength - 1 - numLagSamples : waveformLength - 1;
  }

  /**
   * Computes the number of samples a window with leadSamples and lengthSamples lags sample i
   *
   * @param leadSamples number of samples the window leads the transformed sample
   * @param lengthSamples window length
   * @param waveformLength input waveform length
   * @return integer <= waveformLength - 1
   */
  private static int numLagSamples(int leadSamples, int lengthSamples, int waveformLength) {
    return lengthSamples - leadSamples - 1;
  }

  /**
   * Finds indices where the waveform first exceeds the trigger threshold either after the beginning
   * of the waveform or after an index where the waveform drops below the detrigger threshold.
   *
   * @param triggerThreshold minimum waveform value not causing a trigger, > 0
   * @param detriggerThreshold maximum waveform value not causing a detrigger, > 0
   * @param waveform {@link DoubleStream} of waveform samples, not null
   * @return Set of trigger sample indices, not null
   * @throws IllegalArgumentException if triggerThreshold is <= 0; if detriggerThreshold <= 0
   */
  static Set<Integer> trigger(double triggerThreshold, double detriggerThreshold,
      DoubleStream waveform) {

    ParameterValidation.validateTriggerThresholds(triggerThreshold, detriggerThreshold);

    Set<Integer> triggers = new HashSet<>();

    // Tracks sample index
    int i = 0;

    // Initial state is untriggered
    boolean triggered = false;

    OfDouble wfIter = waveform.iterator();
    while (wfIter.hasNext()) {
      final double sample = wfIter.nextDouble();

      // Not currently triggered and sample exceeds trigger threshold -> create trigger
      if (!triggered && sample > triggerThreshold) {
        triggers.add(i);
        triggered = true;
      }

      // Currently triggered and sample drops below detrigger threshold -> allow triggering again
      else if (triggered && sample < detriggerThreshold) {
        triggered = false;
      }

      i = i + 1;
    }

    return triggers;
  }
}
