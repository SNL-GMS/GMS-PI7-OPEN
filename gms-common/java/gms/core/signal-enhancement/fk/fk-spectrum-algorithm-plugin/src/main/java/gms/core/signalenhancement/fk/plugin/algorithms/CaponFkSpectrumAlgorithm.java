package gms.core.signalenhancement.fk.plugin.algorithms;

import com.google.common.primitives.Ints;
import gms.core.signalenhancement.fk.plugin.algorithms.util.FftUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.utilities.signalprocessing.normalization.DeMeaner;
import gms.shared.utilities.signalprocessing.normalization.MaxAmplitudeNormalizer;
import org.apache.commons.lang3.Validate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default algorithm for the FK Spectrum plugin
 */
public class CaponFkSpectrumAlgorithm {

  private final boolean useChannelVerticalOffsets;
  private final boolean normalizeWaveforms;
  private final Map<UUID, RelativePosition> relativePositionMap;
  private final double waveformSampleRateHz;
  private final double waveformSampleRateToleranceHz;
  private final double mediumVelocityKmPerSec;
  private final double lowFrequency;
  private final double highFrequency;
  private final int eastSlowCount;
  private final double eastSlowStart;
  private final double eastSlowDelta;
  private final int northSlowCount;
  private final double northSlowStart;
  private final double northSlowDelta;
  private final Duration windowLength;
  private final Duration windowLead;
  private final double sampleRate;
  private final int minimumSamplesForSpectra;

  private static final double SEC_PER_NANO_SEC = 1.0e-9;
  /**
   * Epsilon constant used for mitigating measurement error.
   */
  private static final double EPSILON = 0.001;

  private CaponFkSpectrumAlgorithm(boolean useChannelVerticalOffsets,
      boolean normalizeWaveforms,
      Map<UUID, RelativePosition> relativePositionMap,
      double waveformSampleRateHz,
      double waveformSampleRateToleranceHz,
      double mediumVelocityKmPerSec,
      double lowFrequency,
      double highFrequency,
      int eastSlowCount, double eastSlowStart, double eastSlowDelta, int northSlowCount,
      double northSlowStart, double northSlowDelta, Duration windowLength, Duration windowLead,
      double sampleRate, int minimumSamplesForSpectra) {
    this.useChannelVerticalOffsets = useChannelVerticalOffsets;
    this.normalizeWaveforms = normalizeWaveforms;
    this.relativePositionMap = relativePositionMap;
    this.waveformSampleRateHz = waveformSampleRateHz;
    this.waveformSampleRateToleranceHz = waveformSampleRateToleranceHz;
    this.mediumVelocityKmPerSec = mediumVelocityKmPerSec;
    this.lowFrequency = lowFrequency;
    this.highFrequency = highFrequency;
    this.eastSlowCount = eastSlowCount;
    this.eastSlowStart = eastSlowStart;
    this.eastSlowDelta = eastSlowDelta;
    this.northSlowCount = northSlowCount;
    this.northSlowStart = northSlowStart;
    this.northSlowDelta = northSlowDelta;
    this.windowLength = windowLength;
    this.windowLead = windowLead;
    this.sampleRate = sampleRate;
    this.minimumSamplesForSpectra = minimumSamplesForSpectra;
  }

  /**
   * Generate FK Spectra for the given {@link Waveform}s
   *
   * @param channelSegments {@link Waveform}s from which to generate FK Spectra
   * @return temporal sequence of FK Spectra
   * @throws IllegalArgumentException if the algorithm parameters are inconsistent or degenerate
   */
  public List<FkSpectrum> generateFk(Collection<ChannelSegment<Waveform>> channelSegments) {

    //
    // Validate input data
    //

    Objects.requireNonNull(channelSegments,
        "CaponFkSpectrumAlgorithm cannot generate FK Spectra with null channel segments");

    Validate.notEmpty(channelSegments,
        "CaponFkSpectrumAlgorithm cannot generate FK Spectra with zero channel segments");

    Validate.isTrue(channelSegments.stream()
            .map(ChannelSegment::getChannelId)
            .collect(Collectors.toSet()).size() == channelSegments.size(),
        "CaponFkSpectrumAlgorithm cannot generate an FKSpectra from duplicate channel segments");

    Validate.isTrue(relativePositionMap.keySet().containsAll(
        channelSegments
            .stream()
            .map(ChannelSegment::getChannelId)
            .collect(Collectors.toList())),
        "RelativePositionMap does not include a UUID key for each channel segment");

    // Determine the start and end points of the Spectra
    List<FkSpectrum> fkList = new ArrayList<>();
    // TODO: condition waveforms using WaveformUtilities (merge / interpolate)??? possible future
    //  work

    final List<RelativePosition> relativePositionList = channelSegments
        .stream()
        .map(channelSegment -> relativePositionMap.get(channelSegment.getChannelId()))
        .collect(Collectors.toList());

    List<ChannelSegment<Waveform>> fkChannelSegments =
        transformChannelSegmentsByWaveform(channelSegments, DeMeaner::demean);
    if (normalizeWaveforms) {
      fkChannelSegments = transformChannelSegmentsByWaveform(fkChannelSegments,
          MaxAmplitudeNormalizer::normalize);
    }

    Map<Instant, Long> startTimeMap = channelSegments.stream()
        .collect(Collectors.groupingBy(ChannelSegment::getStartTime, Collectors.counting()));

    // We use the most common start time as the start time of the data, meaning that when we deal
    // with jitter and snapping, we are modifying as few waveforms as possible.
    List<Instant> modalStart = new ArrayList<>();
    long modalStartCount = 0;
    for (Map.Entry<Instant, Long> startTimePair : startTimeMap.entrySet()) {
      if (startTimePair.getValue() > modalStartCount) {
        modalStart.clear();
        modalStartCount = startTimePair.getValue();
        modalStart.add(startTimePair.getKey());
      } else if (startTimePair.getValue() == modalStartCount) {
        modalStart.add(startTimePair.getKey());
      }
    }

    Instant jitterBaseStartTime = null;
    if (modalStart.size() == 1) {
      jitterBaseStartTime = modalStart.get(0);
    } else {
      Optional<Instant> possibleJitterBaseStartTime = channelSegments.stream()
          .flatMap(channelSegment -> channelSegment.getTimeseries().stream())
          .filter(this::validateSampleRate)
          .map(Waveform::getStartTime)
          .min(Instant::compareTo);

      if (possibleJitterBaseStartTime.isPresent()) {
        jitterBaseStartTime = possibleJitterBaseStartTime.get();
      }
    }

    Map<Instant, Long> endTimeMap = channelSegments.stream()
        .collect(Collectors.groupingBy(ChannelSegment::getEndTime, Collectors.counting()));

    // We use the most common end time as the end time of the data to ensure that we can fill as
    // much of the final fk as possible
    List<Instant> modalEnd = new ArrayList<>();
    long modalEndCount = 0;
    for (Map.Entry<Instant, Long> endTimePair : endTimeMap.entrySet()) {
      if (endTimePair.getValue() > modalEndCount) {
        modalEnd.clear();
        modalEndCount = endTimePair.getValue();
        modalEnd.add(endTimePair.getKey());
      } else if (endTimePair.getValue() == modalEndCount) {
        modalEnd.add(endTimePair.getKey());
      }
    }

    Instant endTime = null;
    if (modalEnd.size() == 1) {
      endTime = modalEnd.get(0);
    } else {
      Optional<Instant> possibleMaxEndTime = channelSegments.stream()
          .flatMap(channelSegment -> channelSegment.getTimeseries().stream())
          .filter(this::validateSampleRate)
          .map(Waveform::getEndTime)
          .max(Instant::compareTo);

      if (possibleMaxEndTime.isPresent()) {
        endTime = possibleMaxEndTime.get();
      }
    }

    if (jitterBaseStartTime != null && endTime != null) {
      Validate.isTrue(!jitterBaseStartTime.plus(windowLength).isAfter(endTime),
          "Initial window end extends beyond the end of the waveforms");

      Duration fkSamplePeriod = Duration.ofNanos((long) ((1 / sampleRate) * 1E9));

      /*
       * Fk Timeseries:
       *      |            |             |
       * 0    1    1.5     2      2.5    3    3.5
       * {____}     {______}       {_____}
       * [----------]
       *            [--------------]
       *                           [-----------]
       * Waveform Timeseries:
       * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
       * FKs are centered at 1, 2, and 3
       * The distance between 1 and 2, and 2 and 3 is the sample period for the fk, which is equal
       * to 1 / sampleRate
       * The distance between 0 and 1, 1.5 and 2, and 2.5 and 3 is the window lead (noted by {_})
       * The distance between 0 and 1.5, 1.5 and 2.5, and 2.5, and 3.5 is the window length
       * (noted by [-])
       */
      for (Instant windowStart = jitterBaseStartTime,
           fkStartTime = windowStart.plus(windowLead);
           !windowStart.plus(windowLength).isAfter(endTime);
           fkStartTime = fkStartTime.plus(fkSamplePeriod),
               windowStart = fkStartTime.minus(windowLead)) {
        generateSingleFk(fkChannelSegments, relativePositionList, windowStart,
            jitterBaseStartTime).ifPresent(fkList::add);
      }
    }

    return fkList;
  }

  private boolean validateSampleRate(Waveform waveform) {
    double sampleRateDifference = Math.abs(waveform.getSampleRate() - waveformSampleRateHz);
    return Double.compare(sampleRateDifference, waveformSampleRateToleranceHz) <= 0;
  }

  /**
   * Gets the subset of waveforms that can be used to calculate an FkSpectrum in the time window
   * defined by windowStart, windowEnd (inclusive).  This includes rejecting any waveforms that
   * have sample rates out of tolerance (according the setup of this algorithm), that are within
   * the allowable jitter (< 1/2 sample period off the jitterBaseStartTime), and contain both the
   * start and end times, once they have been snapped to the jitter sample
   *
   * @param channelSegments     The channel segments for which the usable subset will be calculated
   * @param windowStart         The start time of the FkSpectrum
   * @param windowEnd           The end time of the FkSpectrum
   * @param jitterBaseStartTime The reference point against which all waveforms are compared for
   *                            jitter.
   * @return A subset of waveforms suitable for calculating the FkSpectrum defined by the
   * windowStart and windowEnd.
   */
  private List<Waveform> getWaveformSubset(Collection<ChannelSegment<Waveform>> channelSegments,
      Instant windowStart,
      Instant windowEnd,
      Instant jitterBaseStartTime) {

    return channelSegments.stream()
        .flatMap(cs -> cs.getTimeseries().stream())
        .filter(this::validateSampleRate)
        .filter(waveform -> jitterCheck(waveform, jitterBaseStartTime))
        .map(waveform -> shiftWaveform(waveform, jitterBaseStartTime))
        .filter(waveform -> waveform.computeTimeRange().contains(windowStart))
        .filter(waveform -> waveform.computeTimeRange().contains(windowEnd))
        .collect(Collectors.toList());
  }

  private Waveform shiftWaveform(Waveform waveform, Instant jitterBaseStartTime) {
    // snap
    if (jitterBaseStartTime.equals(waveform.getStartTime())) {
      return waveform;
    }

    Duration allowableJitter = Duration.ofNanos((long) ((1 / waveformSampleRateHz) * 1E9))
        .dividedBy(2);

    Duration positiveJitter = calculateJitter(waveform, jitterBaseStartTime, true);
    if (positiveJitter.compareTo(allowableJitter) < 0) {
      // waveform starts late, so we snap it back
      return Waveform.withValues(waveform.getStartTime().minus(positiveJitter),
          waveform.getSampleRate(),
          waveform.getValues());
    } else {
      Duration negativeJitter = calculateJitter(waveform, jitterBaseStartTime, false);
      return Waveform.withValues(waveform.getStartTime().plus(negativeJitter),
          waveform.getSampleRate(),
          waveform.getValues());
    }
  }

  private Duration calculateJitter(Waveform waveform,
      Instant jitterBaseStartTime,
      boolean isPositive) {
    if (jitterBaseStartTime.equals(waveform.getStartTime())) {
      return Duration.ofNanos(0);
    }

    Instant actualStart = waveform.getStartTime();
    Duration unfilledArea = Duration.between(jitterBaseStartTime, actualStart).abs();
    Duration samplePeriod = Duration.ofNanos((long) ((1 / waveformSampleRateHz) * 1E9));

    long unfilledSamples = unfilledArea.dividedBy(samplePeriod);

    Instant interpolatedStart;
    if (isPositive) {
      if (actualStart.isBefore(jitterBaseStartTime)) {
        interpolatedStart = actualStart.plus(samplePeriod.multipliedBy(unfilledSamples + 1));
      } else {
        interpolatedStart = actualStart.minus(samplePeriod.multipliedBy(unfilledSamples));
      }

      return Duration.between(jitterBaseStartTime, interpolatedStart);
    } else {
      if (actualStart.isBefore(jitterBaseStartTime)) {
        interpolatedStart = actualStart.plus(samplePeriod.multipliedBy(unfilledSamples));
      } else {
        interpolatedStart = actualStart.minus(samplePeriod.multipliedBy(unfilledSamples + 1));
      }

      return Duration.between(interpolatedStart, jitterBaseStartTime);
    }
  }

  /**
   * Determines if the provided waveform is within the jitter allowance
   *
   * @param waveform            The waveform to validate
   * @param jitterBaseStartTime The minimum jitter time to use for validating the waveform sample
   *                            jitter
   * @return True if the waveform's samples are off from the jitterBaseStartTime by less than 1 / 2
   * waveform sample period
   */
  private boolean jitterCheck(Waveform waveform, Instant jitterBaseStartTime) {

    if (jitterBaseStartTime.equals(waveform.getStartTime())) {
      return true;
    }

    // the number of samples needed to get us as close to min start without going before
    // this basically operates as a divide followed by rounding down, so we know we have the next
    // sample equal to or after the min time.  Then if this interpolated start time is not within
    // the allowable jitter range (.5*deltime on either side of the start), calculate the next
    // sample
    // (right before the min start time), and see if it's within the jitter range
    Duration allowableJitter = Duration.ofNanos((long) ((1 / waveformSampleRateHz) * 1E9));
    Duration positiveJitter = calculateJitter(waveform, jitterBaseStartTime, true);
    if (positiveJitter.compareTo(allowableJitter) < 0) {
      return true;
    } else {
      Duration negativeJitter = calculateJitter(waveform, jitterBaseStartTime, false);
      return negativeJitter.compareTo(allowableJitter) < 0;
    }
  }

  /**
   * Calculate the fstat given beam power (an fk element), average power, and the number of array
   * elements.
   *
   * @param fkPower     A calculated fk pixel (beam power) (Units: dB)
   * @param pAvg        Average of the powers of all the waveforms that went into the fk (Units: dB)
   * @param numChannels Number of channels used to create fk
   * @return The calculated fstat (Unitless)
   */
  private static double computeFStatistic(double fkPower, double pAvg, double numChannels) {
    return (numChannels - 1) * fkPower / (pAvg - fkPower + EPSILON);
  }

  /**
   * Measures the quality of the fk spectrum.
   *
   * @param fk the two-dimensional array of FK values
   * @return a number representing the quality of the fk heat map
   */
  private static int computeFkQual(double[][] fk) {
    Objects.requireNonNull(fk, "FK spectrum cannot be null");

    if (fk.length == 0) {
      throw new IllegalArgumentException("FK spectrum must be non-empty");
    }
    if (fk[0].length == 0) {
      throw new IllegalArgumentException("FK spectrum must have non-empty rows");
    }

    double[][] zeroedFk = zeroFkNans(fk);

    List<Double> fkPeakValues = new ArrayList<>();

    for (int y = 0; y < zeroedFk.length - 1; y++) {
      for (int x = 0; x < zeroedFk[y].length - 1; x++) {
        if (isPeak(x, y, zeroedFk)) {
          fkPeakValues.add(zeroedFk[y][x]);
        }
      }
    }

    double fkRatio = 0;
    if (!fkPeakValues.isEmpty()) {
      fkPeakValues.sort(Collections.reverseOrder());

      double max = fkPeakValues.get(0);
      double secondMax = fkPeakValues.size() > 1 ? fkPeakValues.get(1) : 0;

      fkRatio = max / (secondMax + 0.000001);
    }

    int fkQual;

    if (fkRatio >= powerDecibel(6)) {
      fkQual = 1;
    } else if (fkRatio >= powerDecibel(4)) {
      fkQual = 2;
    } else if (fkRatio >= powerDecibel(1)) {
      fkQual = 3;
    } else {
      fkQual = 4;
    }

    return fkQual;
  }

  /**
   * Given an FK, zero out the NaN values. Necessary for accurate FK Quality calculation.
   *
   * @param fk a 2D FK Power array
   * @return an array containing the same values as the input with NaNs replaced with 0
   */
  private static double[][] zeroFkNans(double[][] fk) {
    double[][] zeroedFk = new double[fk.length][fk[0].length];
    for (int y = 0; y < fk.length; y++) {
      for (int x = 0; x < fk[0].length; x++) {
        if (Double.isNaN(fk[y][x])) {
          zeroedFk[y][x] = 0;
        } else {
          zeroedFk[y][x] = fk[y][x];
        }
      }
    }
    return zeroedFk;
  }

  /**
   * Converts the beam power measured by f-stat to decibels
   *
   * @param power the beam power returned by a call to f-stat
   * @return the beam power measured in decibels
   */
  private static double powerDecibel(double power) {
    return Math.pow(10, power / 10);
  }

  /**
   * Determines if an (x,y) coordinate within an fk spectrum is a local maximum by comparing it to
   * its neighbors
   *
   * @param xCoordinate x coordinate of the potential local maximum
   * @param yCoordinate y coordinate of the potential local maximum
   * @param fk          the two-dimensional array of FK values
   * @return the (x, y) coordinates of the maximum value
   */
  private static boolean isPeak(int xCoordinate, int yCoordinate, double[][] fk) {
    boolean isPeak = true;

    double val = fk[yCoordinate][xCoordinate];

    //Single-pixel FK has no peak
    if (fk.length == 1 && fk[0].length == 1) {
      return false;
    }

    int before = -1;
    int above = -1;
    int after = 1;
    int below = 1;

    //Handle cases when pixel is not fully surrounded by neighbors ('edge' of matrix)
    if (yCoordinate == 0) {
      //Ignore top edge.
      above = 0;
    } else if (yCoordinate == fk.length - 1) {
      //Ignore bottom edge
      below = 0;
    }

    if (xCoordinate == 0) {
      //Ignore left edge
      before = 0;
    } else if (xCoordinate == fk[0].length - 1) {
      //Ignore right edge
      after = 0;
    }

    for (int i = above; i <= below; i++) {
      for (int j = before; j <= after; j++) {
        if (fk[yCoordinate + i][xCoordinate + j] > val) {
          isPeak = false;
        }
      }
    }

    return isPeak;
  }

  private List<ChannelSegment<Waveform>> transformChannelSegmentsByWaveform(Collection<ChannelSegment<Waveform>> channelSegments,
      Function<double[], double[]> waveformTransform) {
    List<ChannelSegment<Waveform>> transformedChannelSegments = new ArrayList<>();

    for (ChannelSegment<Waveform> channelSegment : channelSegments) {
      List<Waveform> transformedWaveforms = new ArrayList<>();
      for (Waveform waveform : channelSegment.getTimeseries()) {
        transformedWaveforms.add(Waveform.from(
            waveform.getStartTime(),
            waveform.getSampleRate(),
            waveform.getSampleCount(),
            waveformTransform.apply(waveform.getValues())));
      }

      transformedChannelSegments.add(ChannelSegment.from(
          channelSegment.getId(),
          channelSegment.getChannelId(),
          channelSegment.getName(),
          channelSegment.getType(),
          transformedWaveforms,
          channelSegment.getCreationInfo()));
    }

    return transformedChannelSegments;
  }

  Optional<FkSpectrum> generateSingleFk(List<ChannelSegment<Waveform>> channelSegments,
      List<RelativePosition> relativePositions, Instant windowStart, Instant minStartTime) {
    List<Waveform> waveformSubset = getWaveformSubset(channelSegments,
        windowStart,
        windowStart.plus(windowLength),
        minStartTime);

    List<Waveform> windowedWaveforms = waveformSubset.stream()
        .map(waveform -> waveform.trim(windowStart, windowStart.plus(windowLength)))
        .collect(Collectors.toList());

    if (windowedWaveforms.size() < minimumSamplesForSpectra) {
      return Optional.empty();
    }

    long numSamples = windowedWaveforms.get(0).getSampleCount();
    List<double[]> ffts = windowedWaveforms.stream()
        .map(FftUtilities::computeFftWindow)
        .collect(Collectors.toList());

    List<double[]> realFfts = FftUtilities.getRealPartOfFfts(ffts);
    List<double[]> imaginaryFfts = FftUtilities.getImaginaryPartOfFfts(ffts);

    double delFrequency = waveformSampleRateHz / numSamples;

    double[] frequencyAxis = fftFreq((int) numSamples, delFrequency);

    /* frequencyBinIndices houses the bins of the bandpass filter imposed on the waveforms
     * before the FK spectrum is generated. */
    int[] frequencyBinIndices = findBinIndices(frequencyAxis, lowFrequency, highFrequency);

    // calculate the passband of the fft'd data.  That is, keep the values that correspond to the
    // indices in the frequencyAxis that pass the band pass filter
    // This is part of the outer summation in the FK formula
    int numFrequencyBins = frequencyBinIndices.length;
    double[] passbandFrequencies = new double[numFrequencyBins];
    for (int i = 0; i < numFrequencyBins; i++) {
      passbandFrequencies[i] = frequencyAxis[frequencyBinIndices[i]];
    }

    List<double[]> passBandRealFfts = new ArrayList<>();
    List<double[]> passBandImaginaryFfts = new ArrayList<>();
    for (int i = 0; i < realFfts.size() && i < imaginaryFfts.size(); i++) {
      double[] realFft = realFfts.get(i);
      double[] imaginaryFft = imaginaryFfts.get(i);
      double[] realPassBand = new double[numFrequencyBins];
      double[] imaginaryPassBand = new double[numFrequencyBins];
      for (int j = 0; j < numFrequencyBins; j++) {
        int binIndex = frequencyBinIndices[j];
        realPassBand[j] = realFft[binIndex];
        imaginaryPassBand[j] = imaginaryFft[binIndex];
      }

      passBandRealFfts.add(realPassBand);
      passBandImaginaryFfts.add(imaginaryPassBand);
    }

    int channelCount = windowedWaveforms.size();

    double sharedScalingFactor = 1 / (Math.pow(channelCount, 2) * Math.pow(numSamples, 2));

    // Calculate the average power
    double pAvg = 0.0;
    for (int i = 0; i < passBandRealFfts.size() && i < passBandImaginaryFfts.size(); i++) {
      double[] realFftPassBand = passBandRealFfts.get(i);
      double[] imaginaryFftPassBand = passBandImaginaryFfts.get(i);
      for (int j = 0; j < realFftPassBand.length && j < imaginaryFftPassBand.length; j++) {
        double absValue =
            Math.sqrt(Math.pow(realFftPassBand[j], 2) + Math.pow(imaginaryFftPassBand[j], 2));
        pAvg += Math.pow(absValue, 2);
      }
    }

    pAvg /= (Math.pow(numSamples, 2) * windowedWaveforms.size());

    double[][] fk = new double[northSlowCount][eastSlowCount];
    double[][] fstat = new double[northSlowCount][eastSlowCount];
    double slowNorth = northSlowStart;

    for (int north = 0; north < northSlowCount; slowNorth += northSlowDelta, north++) {
      double slowEast = eastSlowStart;
      for (int east = 0; east < eastSlowCount; slowEast += eastSlowDelta, east++) {
        // compute sz, the vertical slowness, accounting for measurement errors that could
        // result in a very small negative vertical slowness.
        final double ZERO_THRESHOLD = 1.0e-5;
        double verticalSlowness;
        if (useChannelVerticalOffsets) {  // compute 3D FK
          verticalSlowness =
              1.0 / (mediumVelocityKmPerSec * mediumVelocityKmPerSec)
                  - Math.pow(slowEast, 2)
                  - Math.pow(slowNorth, 2);
          if (verticalSlowness >= ZERO_THRESHOLD) {
            verticalSlowness = Math.sqrt(verticalSlowness);
          } else {
            verticalSlowness = Double.NaN;
          }
        } else {  // else compute 2D FK
          verticalSlowness = 0.0;
        }

        if (!Double.isNaN(verticalSlowness)) {
          double[] timeShifts = new double[channelCount];
          for (int i = 0; i < channelCount; i++) {
            RelativePosition position = relativePositions.get(i);
            timeShifts[i] = position.getNorthDisplacementKm() * slowNorth
                + position.getEastDisplacementKm() * slowEast
                + position.getVerticalDisplacementKm() + verticalSlowness;
          }

          // Java doesn't have complex numbers, so we use Euler's formula to handle the second
          // Fourier Transform
          // e^(i*theta) = cos(theta) + i*sin(theta)
          // or, if theta is negative (due to the even/odd properties of sine and cosine)
          // e^(-i*theta) = cos(theta) - i * sin(theta)
          // so e^(-2*pi*i*fn*timeshift) = cos(-2*pi*fn*timeshift) + i*sin(-2*pi*fn*timeshift)
          // where fn is the particular frequency in the passband
          double[][] thetas = new double[channelCount][numFrequencyBins];
          double[][] realSpatialFourierFactor = new double[channelCount][numFrequencyBins];
          double[][] imaginarySpatialFourierFactor = new double[channelCount][numFrequencyBins];
          for (int i = 0; i < channelCount; i++) {
            for (int j = 0; j < numFrequencyBins; j++) {
              thetas[i][j] = -2.0 * Math.PI * timeShifts[i] * passbandFrequencies[j];
              realSpatialFourierFactor[i][j] = Math.cos(thetas[i][j]);
              imaginarySpatialFourierFactor[i][j] = Math.sin(thetas[i][j]);
            }
          }

          double[][] realShiftedWaveforms = new double[channelCount][frequencyBinIndices.length];
          double[][] imaginaryShiftedWaveforms =
              new double[channelCount][frequencyBinIndices.length];
          for (int i = 0; i < realShiftedWaveforms.length; i++) {
            double[] realWaveformPassBand = passBandRealFfts.get(i);
            double[] imaginaryWaveformPassBand = passBandImaginaryFfts.get(i);
            for (int j = 0; j < realShiftedWaveforms[i].length; j++) {
              // We are multiplying two complex numbers together, without having the ability to
              // represent them properly, so it looks something like this
              // 1) FFT'd waveform value after passband = a + b*i
              // 2) Euler'd Spatial Fourier Factor = c + d*i
              // 1) * 2) = (a + b*i) (c + d*i) = a*c + (a*d + b*c)i - b*d (minus because of the i^2)
              // thus, the real portion of the new fft is a*c - b*d
              // and the imaginary portion if a*d + b*c
              realShiftedWaveforms[i][j] = realWaveformPassBand[j] * realSpatialFourierFactor[i][j]
                  - imaginaryWaveformPassBand[j] * imaginarySpatialFourierFactor[i][j];
              imaginaryShiftedWaveforms[i][j] =
                  realWaveformPassBand[j] * imaginarySpatialFourierFactor[i][j]
                      + imaginaryWaveformPassBand[j] * realSpatialFourierFactor[i][j];
            }
          }

          // Now we create the beam, and sum it up into a single value that will be the FkPixel
          double[] realUnaveragedBeam = new double[frequencyBinIndices.length];
          double[] imaginaryUnaveragedBeam = new double[frequencyBinIndices.length];
          double sum = 0;
          for (int j = 0; j < frequencyBinIndices.length; j++) {
            for (int i = 0; i < channelCount; i++) {
              realUnaveragedBeam[j] += realShiftedWaveforms[i][j];
              imaginaryUnaveragedBeam[j] += imaginaryShiftedWaveforms[i][j];
            }

            double absTimeStep = Math.sqrt(Math.pow(realUnaveragedBeam[j], 2)
                + Math.pow(imaginaryUnaveragedBeam[j], 2));
            sum += Math.pow(absTimeStep, 2);
          }

          fk[north][east] = sum * sharedScalingFactor;
        } else {
          fk[north][east] = Double.NaN;
        }

        fstat[north][east] = computeFStatistic(fk[north][east], pAvg, channelCount);
      }
    }

    int fkQual = computeFkQual(fk);

    return Optional.of(FkSpectrum.from(fk, fstat, fkQual));
  }

  /**
   * Compute the FFT Frequency Bin Array for an array with N Samples. Based on the NumPy library
   * function numpy.fft.fftfreq found here: https://docs.scipy
   * .org/doc/numpy/reference/generated/numpy.fft.fftfreq.html
   *
   * @param numSamples     the number of waveform samples
   * @param deltaFrequency the value on which the frequency bin values should be scaled
   * @return The calculated FFT Frequency Bin Array
   */
  static double[] fftFreq(int numSamples, double deltaFrequency) {

    double[] freqBinCenters = new double[numSamples];

    // Compute the 'range' of values for the sample frequency array that will be non-negative.
    // e.g. if n = 6, the resulting array polarity will be [0-2] are positive, [3-5] are negative
    // and if n = 5, the polarity would be [0-2] are positive, [3-4] are negative
    int positiveBinRange = (int) Math.ceil(numSamples / 2.0);

    int negativeBaseVal = (int) Math.ceil(-numSamples / 2.0);
    for (int i = 0; i < freqBinCenters.length; i++) {
      int baseVal;
      if (i < positiveBinRange) {
        baseVal = i;
      } else {
        baseVal = negativeBaseVal;
        negativeBaseVal++;
      }
      freqBinCenters[i] = baseVal * deltaFrequency;
    }

    return freqBinCenters;
  }

  /**
   * Iterates through an array of FFT sample frequencies and determines what indices apply for
   * frequency bins (e.g. are within the range between the low and high frequency)
   *
   * @param sampleFrequencies array of FFT sample frequencies
   * @param lowFrequency      lower bound for comparing sample frequencies (inclusive)
   * @param highFrequency     upper bound for comparing sample frequencies (inclusive)
   * @return an array of frequency bin indices
   */
  static int[] findBinIndices(double[] sampleFrequencies, double lowFrequency,
      double highFrequency) {
    List<Integer> binIndices = new ArrayList<>();
    for (int i = 0; i < sampleFrequencies.length; i++) {
      double absBinCenterVal = Math.abs(sampleFrequencies[i]);
      if (absBinCenterVal >= lowFrequency && absBinCenterVal <= highFrequency) {
        binIndices.add(i);
      }
    }
    return Ints.toArray(binIndices);
  }

  /**
   * Helper to convert the class into a Builder
   *
   * @return A Builder with all of the buildable fields from the class set
   */
  public Builder toBuilder() {
    return new Builder()
        .useChannelVerticalOffsets(useChannelVerticalOffsets)
        .withRelativePositionMap(relativePositionMap)
        .withWaveformSampleRateHz(waveformSampleRateHz)
        .withMediumVelocityKmPerSec(mediumVelocityKmPerSec)
        .withLowFrequency(lowFrequency)
        .withHighFrequency(highFrequency)
        .withEastSlowCount(eastSlowCount)
        .withEastSlowStart(eastSlowStart)
        .withEastSlowDelta(eastSlowDelta)
        .withNorthSlowCount(northSlowCount)
        .withNorthSlowStart(northSlowStart)
        .withNorthSlowDelta(northSlowDelta)
        .withWindowLead(windowLead)
        .withWindowLength(windowLength)
        .withSampleRate(sampleRate);
  }

  /**
   * A mutable builder for a {@link CaponFkSpectrumAlgorithm}.  The builder has two phases. At
   * inception, it is in the build phase in which it can be modified.  Once the build() method is
   * called, the {@link Builder} transitions to the built phase, to create the {@link
   * CaponFkSpectrumAlgorithm}.  Once the build() method is called, the {@link Builder} can no
   * longer be used.
   */
  public static final class Builder {

    private boolean built = false;
    private static final String ALREADY_BUILT_MESSAGE = "CaponFkSpectrumAlgorithm has already " +
        "been built";

    private boolean useChannelVerticalOffsets;
    private boolean normalizeWaveforms;
    private Map<UUID, RelativePosition> relativePositionMap;
    private double waveformSampleRateHz;
    private double waveformSampleRateToleranceHz;
    private double mediumVelocityKmPerSec;
    private double lowFrequency;
    private double highFrequency;
    private int eastSlowCount;
    private double eastSlowStart;
    private double eastSlowDelta;
    private int northSlowCount;
    private double northSlowStart;
    private double northSlowDelta;
    private Duration windowLength;
    private Duration windowLead;
    private double sampleRate;
    private int minimumWaveformsForSpectra;

    /**
     * Sets whether the {@link CaponFkSpectrumAlgorithm} should calculate 2D or 3D FK Spectra
     *
     * @param useChannelVerticalOffsets <code>true</code> if the FK spectra should be 3D,
     *                                  <code>false</code> if it should be 2D.
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder useChannelVerticalOffsets(boolean useChannelVerticalOffsets) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.useChannelVerticalOffsets = useChannelVerticalOffsets;
      return this;
    }

    /**
     * Sets whether the {@link CaponFkSpectrumAlgorithm} should calculate 2D or 3D FK Spectra
     *
     * @param normalizeWaveforms <code>true</code> if the input waveforms to generate the FK spectra
     *                           should be normalized, <code>false</code> if they should not.
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder normalizeWaveforms(boolean normalizeWaveforms) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.normalizeWaveforms = normalizeWaveforms;
      return this;
    }

    /**
     * Sets the RelativePositionMap, mapping station UUID to {@link RelativePosition} of stations
     *
     * @param relativePositionMap map of station UUID to {@link RelativePosition}s of station in
     *                            kilometers
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create an {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withRelativePositionMap(Map<UUID, RelativePosition> relativePositionMap) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.relativePositionMap = relativePositionMap;
      return this;
    }

    /**
     * Sets the {@link Waveform} sampling rate
     *
     * @param waveformSampleRateHz {@link Waveform} sampling rate in Hz
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withWaveformSampleRateHz(double waveformSampleRateHz) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.waveformSampleRateHz = waveformSampleRateHz;
      return this;
    }

    /**
     * Sets the tolerance for the {@link Waveform} sampling rate
     *
     * @param waveformSampleRateToleranceHz {@link Waveform} sampling rate tolerance in Hz
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withWaveformSampleRateToleranceHz(double waveformSampleRateToleranceHz) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.waveformSampleRateToleranceHz = waveformSampleRateToleranceHz;
      return this;
    }

    /**
     * Sets the medium velocity
     *
     * @param mediumVelocityKmPerSec medium velocity in kilometers per second
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withMediumVelocityKmPerSec(double mediumVelocityKmPerSec) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.mediumVelocityKmPerSec = mediumVelocityKmPerSec;
      return this;
    }

    /**
     * Sets the lower frequency limit for the band pass filter
     *
     * @param lowFrequency lower frequency limit in Hz for the band pass filter
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withLowFrequency(double lowFrequency) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.lowFrequency = lowFrequency;
      return this;
    }

    /**
     * Sets the higher frequency limit for the band pass filter
     *
     * @param highFrequency higher frequency limit in Hz for the band pass filter
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withHighFrequency(double highFrequency) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.highFrequency = highFrequency;
      return this;
    }

    /**
     * Sets the number of slowness samples in the eastward direction
     *
     * @param eastSlowCount number of slowness samples in the eastward direction
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withEastSlowCount(int eastSlowCount) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.eastSlowCount = eastSlowCount;
      return this;
    }

    /**
     * Sets the value of the first slowness sample in the eastward direction
     *
     * @param eastSlowStart value of the first slowness sample in the eastward direction in seconds
     *                      per kilometer
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withEastSlowStart(double eastSlowStart) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.eastSlowStart = eastSlowStart;
      return this;
    }

    /**
     * Sets the delta between slowness samples in the eastward direction
     *
     * @param eastSlowDelta delta in seconds per kilometer between slowness samples in eastward
     *                      direction
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withEastSlowDelta(double eastSlowDelta) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.eastSlowDelta = eastSlowDelta;
      return this;
    }

    /**
     * Sets the number of slowness samples in the northward direction
     *
     * @param northSlowCount number of slowness samples in the northward direction
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withNorthSlowCount(int northSlowCount) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.northSlowCount = northSlowCount;
      return this;
    }

    /**
     * Sets the value of the first slowness sample in the northward direction
     *
     * @param northSlowStart value of the first slowness sample in the northward direction in
     *                       seconds per kilometer
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withNorthSlowStart(double northSlowStart) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.northSlowStart = northSlowStart;
      return this;
    }

    /**
     * Sets the delta between slowness samples in the northward direction
     *
     * @param northSlowDelta delta in seconds per kilometer between slowness samples in eastward
     *                       direction
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withNorthSlowDelta(double northSlowDelta) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.northSlowDelta = northSlowDelta;
      return this;
    }

    /**
     * Sets the {@link Waveform} window length
     *
     * @param windowLength length of {@link Waveform} window
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withWindowLength(Duration windowLength) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.windowLength = windowLength;
      return this;
    }

    /**
     * Sets the {@link Waveform} window lead time
     *
     * @param windowLead length of {@link Waveform} window lead time
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withWindowLead(Duration windowLead) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.windowLead = windowLead;
      return this;
    }

    /**
     * Sets the {@link Waveform} delta between windows
     *
     * @param sampleRate the sample rate of the
     * {@link gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra}
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public Builder withSampleRate(double sampleRate) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.sampleRate = sampleRate;
      return this;
    }

    public Builder withMinimumWaveformsForSpectra(int minimumWaveformsForSpectra) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      this.minimumWaveformsForSpectra = minimumWaveformsForSpectra;
      return this;
    }

    /**
     * Builds the {@link CaponFkSpectrumAlgorithm} from the parameters defined during the build
     * phase.
     *
     * @return a new {@link CaponFkSpectrumAlgorithm}
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     *                               CaponFkSpectrumAlgorithm}
     */
    public CaponFkSpectrumAlgorithm build() {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_MESSAGE);
      }

      Objects.requireNonNull(relativePositionMap,
          "CaponFkSpectrumAlgorithm cannot be built from null relative positions");

      Validate.isTrue(!relativePositionMap.isEmpty(),
          "CaponFkSpectrumAlgorithm cannot be built form empty relative positions");

      Objects.requireNonNull(windowLead,
          "CaponFkSpectrumAlgorithm cannot be built from null window lead");

      Objects.requireNonNull(windowLength,
          "CaponFkSpectrumAlgorithm cannot be built from null window length");

      Validate.isTrue(Double.compare(waveformSampleRateHz, 0) > 0,
          "Waveform sample rate (Hz) must be greater than 0");

      Validate.isTrue(Double.compare(waveformSampleRateToleranceHz, 0) >= 0,
          "Waveform sample rate tolerance (Hz) must be greater than or equal to 0");

      Validate.isTrue(
          windowLead.getSeconds() + windowLead.getNano() * SEC_PER_NANO_SEC
              < windowLength.getSeconds() + windowLength.getNano() * SEC_PER_NANO_SEC,
          "Window length must be greater than window lead");

      Validate.isTrue(0.0 < windowLength.getSeconds() + windowLength.getNano() * SEC_PER_NANO_SEC,
          "Window length must be positive.");

      Validate.isTrue(0.0 <= windowLead.getSeconds() + windowLead.getNano() * SEC_PER_NANO_SEC,
          "Window lead must be non-negative.");

      Validate.isTrue(0.0 <= lowFrequency,
          "Low frequency of band pass filter must be non-negative");

      Validate.isTrue(lowFrequency < highFrequency,
          "Low frequency of band pass filter must be less than the high frequency");

      Validate.isTrue(highFrequency <= waveformSampleRateHz / 2.0,
          "High frequency of band pass filter must not be greater than the Nyquest freqency, "
              + waveformSampleRateHz / 2.0);

      Validate.isTrue(minimumWaveformsForSpectra > 1,
          "Minimum waveforms required to create an FkSpectrum must be greater than 1");

      built = true;
      return new CaponFkSpectrumAlgorithm(useChannelVerticalOffsets,
          normalizeWaveforms,
          relativePositionMap,
          waveformSampleRateHz,
          waveformSampleRateToleranceHz,
          mediumVelocityKmPerSec,
          lowFrequency, highFrequency, eastSlowCount, eastSlowStart, eastSlowDelta,
          northSlowCount, northSlowStart, northSlowDelta, windowLength, windowLead,
          sampleRate, minimumWaveformsForSpectra);
    }
  }

}

