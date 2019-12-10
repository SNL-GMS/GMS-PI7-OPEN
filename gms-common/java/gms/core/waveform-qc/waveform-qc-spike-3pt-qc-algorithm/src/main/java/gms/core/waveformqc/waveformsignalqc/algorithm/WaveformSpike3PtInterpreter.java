package gms.core.waveformqc.waveformsignalqc.algorithm;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaveformSpike3PtInterpreter {

  private static final Logger logger = LoggerFactory.getLogger(WaveformSpike3PtInterpreter.class);

  public WaveformSpike3PtInterpreter() {
  }

  /**
   * Finds simple spikes in the waveform data of the input {@link ChannelSegment}. Uses the provided
   * number of lead and lag sample differences to determine the background sample difference.  A 3pt
   * spike must have sample differences exceeding this background by more than
   * rmsAmplitudeRatioThreshold.  A 3pt spike must also pass the three point test (consecutive
   * differences centered on the spike have different sign; the minimum of the two differences
   * exceeds minConsecutiveSampleDifferenceSpikeThreshold times the maximum of the two input
   * differences.
   *
   * @param channelSegment find spikes in this {@link ChannelSegment}, not null
   * @param minConsecutiveSampleDifferenceSpikeThreshold threshold for a spike mask (masks must
   * exceed this threshold), must be {@code > 0}
   * @param rmsLeadSampleDifferences number of sample differences before the 3pt difference used
   * when computing RMS, must be {@code >= 0}
   * @param rmsLagSampleDifferences number of samples differences after the 3pt difference used when
   * computing RMS, must be {@code >= 0}
   * @param rmsAmplitudeRatioThreshold amplitude ratio threshold for a spike mask (masks must exceed
   * this threshold), must be {@code > 1.0}
   * @return list of {@link WaveformSpike3PtQcMask} in the channelSegment, not null
   * @throws NullPointerException if channelSegment is null
   * @throws IllegalArgumentException if {@code minConsecutiveSampleDifferenceSpikeThreshold <= 0;
   * if rmsLeadSampleDifferences < 0; if rmsLagSampleDifferences < 0; if (rmsLeadSampleDifferences +
   * rmsLagSampleDifferences) < 2; if rmsAmplitudeRatioThreshold <= 1.0 }
   */
  public List<WaveformSpike3PtQcMask> createWaveformSpike3PtQcMasks(
      ChannelSegment<Waveform> channelSegment, double minConsecutiveSampleDifferenceSpikeThreshold,
      int rmsLeadSampleDifferences, int rmsLagSampleDifferences,
      double rmsAmplitudeRatioThreshold) {

    Objects.requireNonNull(channelSegment,
        "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires non-null channelSegment");

    if (minConsecutiveSampleDifferenceSpikeThreshold <= 0
        || minConsecutiveSampleDifferenceSpikeThreshold >= 1.0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires minConsecutiveSampleDifferenceSpikeThreshold > 0.0 and < 1.0");
    }

    if (rmsLeadSampleDifferences < 0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires rmsLeadSampleDifferences >= 0");
    }

    if (rmsLagSampleDifferences < 0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires rmsLagSampleDifferences >= 0");
    }

    if (rmsLeadSampleDifferences + rmsLagSampleDifferences < 2) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires (rmsLeadSampleDifferences + rmsLagSampleDifferences) >= 2");
    }

    if (rmsAmplitudeRatioThreshold <= 1.0) {
      throw new IllegalArgumentException(
          "WaveformSpike3PtInterpreter.createWaveformSpike3PtQcMasks requires rmsAmplitudeRatioThreshold > 1.0");
    }

    logger.info("Performing QC spike algorithm on " + channelSegment.getName() + " with threshold "
        + minConsecutiveSampleDifferenceSpikeThreshold);

    // Creates a mask on channelSegment at time i
    final Function<Instant, WaveformSpike3PtQcMask> createMask = i -> WaveformSpike3PtQcMask
        .create(channelSegment.getChannelId(), channelSegment.getId(), i);

    // Find groups of adjacent waveforms and combine their samples into a single double[]
    // Use the RMS(spike differences) / RMS(background differences) and 3pt check to find spikes.
    // Create a mask for each spike, then create the output mask list by combining masks from each
    // group of adjacent waveforms.
    return groupAdjacentWaveforms(channelSegment).stream()
        .map(wfs ->
            findSpikes(combineSamples(wfs), rmsLeadSampleDifferences, rmsLagSampleDifferences,
                rmsAmplitudeRatioThreshold, minConsecutiveSampleDifferenceSpikeThreshold).stream()
                .map(i -> timeForSample(wfs, i))
                .map(createMask))
        .flatMap(Function.identity())
        .collect(Collectors.toList());
  }

  /**
   * Create lists of the adjacent {@link Waveform}s from a {@link ChannelSegment}.  Each Waveform is
   * in exactly one list.  Each list is ordered by Waveform start time.
   *
   * @param channelSegment find adjacent waveforms within this ChannelSegment, not null
   * @return List of ordered Lists of adjacent Waveforms, not null
   */
  private static List<List<Waveform>> groupAdjacentWaveforms(
      ChannelSegment<Waveform> channelSegment) {

    // Output list
    List<List<Waveform>> adjacentWaveforms = new LinkedList<>();

    // Current list of adjacent waveforms
    LinkedList<Waveform> curAdjacent = new LinkedList<>();

    for (Waveform wf : channelSegment.getTimeseries()) {
      // Current list is empty -> start a new list with this waveform
      if (curAdjacent.isEmpty()) {
        curAdjacent.add(wf);
      }

      // Current list is not empty -> try to extend it with this waveform
      else {
        // Waveform extends the existing adjacent list
        if (isWaveformEndPointEvaluationValid(curAdjacent.getLast(), wf)) {
          curAdjacent.add(wf);
        }

        // Waveform is not adjacent to current list.  Terminate the current adjacent list and start
        // a new current adjacent waveforms list with the current waveform
        else {
          adjacentWaveforms.add(curAdjacent);
          curAdjacent = new LinkedList<>();
          curAdjacent.add(wf);
        }
      }
    }

    // Add the final adjacent waveforms list to the output list
    if (!curAdjacent.isEmpty()) {
      adjacentWaveforms.add(curAdjacent);
    }

    return adjacentWaveforms;
  }

  /**
   * Performs a test to see if end-point spike evaluation is valid to perform. If lastWaveform is
   * defined (not null) and the time separation between the last point of lastWaveform and the first
   * point of waveform is less than or equal to the maximum waveform sample period of the two input
   * waveforms then true is returned.
   *
   * @param lastWaveform The previous waveform
   * @param waveform The current waveform
   * @return True if end-point evaluation can be performed.
   */
  private static boolean isWaveformEndPointEvaluationValid(Waveform lastWaveform,
      Waveform waveform) {

    // exit with false if lastWaveform is null
    if (lastWaveform == null) {
      return false;
    }

    // only test the last waveform last point and the current waveform first point if the time
    // spacing between the those points is less than or equal to the maximum separation times
    // between points of each waveform (the inverse sample rate).
    long adjacentWaveformTimeSeparation = Duration
        .between(lastWaveform.getEndTime(), waveform.getStartTime()).toNanos();
    return (double) adjacentWaveformTimeSeparation / 1000000000 <= Math
        .max(1.0 / lastWaveform.getSampleRate(), 1.0 / waveform.getSampleRate());
  }

  /**
   * Combines all of the samples from the waveforms into a single double[]. Creates a new copy of
   * the data.
   *
   * @param waveforms list of {@link Waveform}, not null
   * @return double[] containing all of the Waveform samples, not null
   */
  private static double[] combineSamples(List<Waveform> waveforms) {
    final int totalLength = waveforms.stream().mapToInt(w -> w.getValues().length).sum();
    double[] samples = new double[totalLength];

    int next = 0;
    for (double[] src : waveforms.stream().map(Waveform::getValues).collect(Collectors.toList())) {
      System.arraycopy(src, 0, samples, next, src.length);
      next = next + src.length;
    }

    return samples;
  }

  /**
   * Finds spikes in the samples.  A spike must satisfy two tests: 1. RMS of the spike amplitude
   * difference to the previous and next points over background RMS sample differences must exceed
   * rmsAmplitudeRatioThreshold 2. {@link WaveformSpike3PtInterpreter#isBasic3PtSpike}
   *
   * @param samples waveform samples, not null
   * @param rmsLeadSampleDifferences number of sample differences prior to the spike to use in the
   * background
   * @param rmsLagSampleDifferences number of sample differences after the spike to use in the
   * background
   * @param rmsAmplitudeRatioThreshold for a spike to occur the ratio of the RMS of the two sample
   * differences centered on the spike to the RMS of the background sample differences must exceed
   * this threshold
   * @param minConsecutiveSampleDifferenceSpikeThreshold threshold used in the {@link
   * WaveformSpike3PtInterpreter#isBasic3PtSpike}
   * @return Collection of sample indexes containing spikes
   */
  private static Collection<Integer> findSpikes(double[] samples, int rmsLeadSampleDifferences,
      int rmsLagSampleDifferences, double rmsAmplitudeRatioThreshold,
      double minConsecutiveSampleDifferenceSpikeThreshold) {

    List<Integer> exceedThreshold = new LinkedList<>();

    // Begin and end checking for spikes centered at these indexes
    final int end = samples.length - 3 - rmsLagSampleDifferences;

    if (rmsLeadSampleDifferences <= end) {
      double[] lead = new double[rmsLeadSampleDifferences];
      double[] lag = new double[rmsLagSampleDifferences];
      double[] spike = new double[2];

      for (int start3pt = rmsLeadSampleDifferences; start3pt <= end; ++start3pt) {

        // Compute RMS of differences prior to the 3pt
        computeDifferences(samples, start3pt - rmsLeadSampleDifferences, lead);
        final double leadRms = RootMeanSquare.rms(lead);

        // Compute RMS of differences after the 3pt
        computeDifferences(samples, start3pt + 2, lag);
        final double lagRms = RootMeanSquare.rms(lag);

        // Compute overall background RMS from the lead and lag RMS.
        // Consider how RMS is computed, e.g.
        //    leadRms = sqrt((diff_0^2 + diff_1^2 + ... + diff_n^2) / numLeadSampleDifferences)
        //            = sqrt(leadSquare / numLeadSampleDifferences)
        //    leadSquare = leadRms^2 * numLeadSampleDifferences
        // backgroundRms = sqrt((leadSquare + lagSquare) / (numLeadSampleDifferences + numLagSampleDifferences))
        final double totalSquare =
            (lead.length * leadRms * leadRms) + (lag.length * lagRms * lagRms);
        final double backgroundRms = Math.sqrt(totalSquare / (lead.length + lag.length));

        // Compute RMS of the differences for the 3pts
        computeDifferences(samples, start3pt, spike);
        final double spikeRms = RootMeanSquare.rms(spike);

        // If the point passes the RMS test then perform this basic 3pt spike test
        if (spikeRms > rmsAmplitudeRatioThreshold * backgroundRms) {

          logger.info("spikeRms {} > rmsAmplitudeRatioThreshold {} * backgroundRms{}", spikeRms,
              rmsAmplitudeRatioThreshold, backgroundRms);

          if (isBasic3PtSpike(spike[0], spike[1], minConsecutiveSampleDifferenceSpikeThreshold)) {
            exceedThreshold.add(start3pt + 1);
          }
        }
      }
    }

    return exceedThreshold;
  }

  /**
   * Fill to[] with pairwise differences from the from[] array (to[0] = from[start+1] - from[start];
   * to[1] = from[start+2] - from[start+1]; ...). Assumes from.length > start + to.length
   *
   * @param from compute differences from this array
   * @param start first index into from[] used in a difference; the first difference is
   * from[start+1] - from[start]
   * @param to store differences in this array
   */
  private static void computeDifferences(double[] from, int start, double[] to) {
    for (int j = 0; j < to.length; ++j) {
      final int i = start + j;
      to[j] = from[i + 1] - from[i];
    }
  }

  /**
   * Perform the basic spike test. Returns true if the consecutive sample differences are of
   * opposite sign and the minimum of the two differences exceeds the spikeThreshold times the
   * maximum of the two input differences.
   *
   * @param difference0 The first consecutive sample point difference.
   * @param difference1 The second consective sample point difference.
   * @param spikeThreshold The spike threshold value.
   * @return True if a spike is present.
   */
  private static boolean isBasic3PtSpike(double difference0, double difference1,
      double spikeThreshold) {

    // if difference0 and difference1 are of different sign then test for spike
    if (difference0 * difference1 < 0.0) {
      // take absolute value of the difference and find the minimum and maximum
      double diff0Absolute = Math.abs(difference0);
      double diff1Absolute = Math.abs(difference1);
      double minDifference = Math.min(diff0Absolute, diff1Absolute);
      double maxDifference = Math.max(diff0Absolute, diff1Absolute);

      // if the minimum absolute difference exceeds the maximum absolute difference times the
      // threshold then create a spike mask.
      final boolean isSpike = (minDifference > spikeThreshold * maxDifference);

      logger.info(
          "Differences have different sign, (minDifference {} > spikeThreshold {} * maxDifference {}) ? = {}",
          minDifference, spikeThreshold, maxDifference, isSpike);

      return isSpike;
    }

    logger.info("Differences have the same sign, not a spike");

    return false;
  }

  /**
   * Determines the sample time within a group of adjacent waveforms.  Assumes the sample index
   * falls within the total number of samples of the input waveforms.
   *
   * @param adjacentWaveforms time ordered list of adjacent waveforms, not null
   * @param sample sample index occurring somewhere in the waveforms list
   * @return {@link Instant} corresponding to the sample index
   * @throws IllegalStateException if {@code sample is less 0; if sample is >= the total number of samples
   * in the adjacentWaveforms.}
   */
  private static Instant timeForSample(List<Waveform> adjacentWaveforms, int sample) {
    int sampleInWf = sample;

    final long totalLength = adjacentWaveforms.stream().mapToLong(Waveform::getSampleCount).sum();
    if (sample < 0 || sample >= totalLength) {
      throw new IllegalStateException("Sample index " + sample
          + " must be >= 0 and less than the total number of samples in the provided waveforms.");
    }

    // Iterate through the waveforms decrementing sampleInWf until it falls within a waveform's
    // sample count, then lookup that sample's time.
    Instant sampleTime = null;
    for (Waveform w : adjacentWaveforms) {
      if (sampleInWf >= w.getValues().length) {
        sampleInWf = sampleInWf - w.getValues().length;
      } else {
        sampleTime = w.computeSampleTime(sampleInWf);
        break;
      }
    }

    return sampleTime;
  }
}
