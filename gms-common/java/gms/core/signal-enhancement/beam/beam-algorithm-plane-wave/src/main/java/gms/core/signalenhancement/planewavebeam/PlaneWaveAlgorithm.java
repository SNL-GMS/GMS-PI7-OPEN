package gms.core.signalenhancement.planewavebeam;

import com.google.common.base.Preconditions;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.utilities.signalprocessing.validation.JitterPredicate;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the logic to generate plane wave beams.
 */
public class PlaneWaveAlgorithm {

  private static final double DECIMAL_PRECISION_CUTOFF = 1e-10;
  private static final Logger logger = LoggerFactory.getLogger(PlaneWaveAlgorithm.class);

  private final double nominalSampleRate;
  private final double sampleRateTolerance;
  private final double azimuth;
  private final double horizontalSlowness;
  private final double mediumVelocity;
  private final boolean snappedSampling;
  private final boolean coherent;
  private final boolean twoDimensional;
  private final PhaseType phaseType;
  private final Map<UUID, RelativePosition> relativePositionsByChannelId;
  private final int minimumWaveformsForBeam;

  private PlaneWaveAlgorithm(double nominalSampleRate,
      double sampleRateTolerance,
      double azimuth,
      double horizontalSlowness,
      double mediumVelocity,
      boolean snappedSampling,
      boolean coherent,
      boolean twoDimensional,
      PhaseType phaseType,
      Map<UUID, RelativePosition> relativePositionsByChannelId,
      int minimumWaveformsForBeam) {
    if (!twoDimensional) {
      double slowness = 1.0 / mediumVelocity;
      Validate.isTrue(slowness - horizontalSlowness > -1.0 * DECIMAL_PRECISION_CUTOFF,
          "Overall slowness cannot be less than horizontal slowness");
    }
    Objects.requireNonNull(relativePositionsByChannelId,
        "PlaneWaveBeam plugin cannot generate a beam with null RelativePositions");
    Validate.isTrue(snappedSampling, "Interpolated Sampling is not supported by PlaneWavePlugin");
    Objects.requireNonNull(phaseType,
        "PlaneWaveBeam plugin cannot generate a beam with null PhaseType");
    Preconditions.checkArgument(minimumWaveformsForBeam > 0,
        "PlaneWaveBeam plugin cannot generate a beam with minimum waveforms less than " +
            "or equal to zero");
    this.nominalSampleRate = nominalSampleRate;
    this.sampleRateTolerance = sampleRateTolerance;
    this.azimuth = azimuth;
    this.horizontalSlowness = horizontalSlowness;
    this.mediumVelocity = mediumVelocity;
    this.snappedSampling = snappedSampling;
    this.coherent = coherent;
    this.twoDimensional = twoDimensional;
    this.phaseType = phaseType;
    this.relativePositionsByChannelId = relativePositionsByChannelId;
    this.minimumWaveformsForBeam = minimumWaveformsForBeam;
  }

  /**
   * Generates a plane wave beam for the provided channel segments, using the parameters provided
   * during construction of this PlaneWaveAlgorithm
   *
   * @param channelSegments The channel segments containing the wave forms to beam
   * @return a waveform representing the beam created from the channelSegments, or null if there are
   * no channel segments to beam from after filtering.
   * @throws IllegalArgumentException if the samplerates of the channelSegments are not unimodal
   */
  public List<Waveform> generateBeam(Collection<ChannelSegment<Waveform>> channelSegments) {
    Objects.requireNonNull(channelSegments,
        "PlaneWaveBeam plugin cannot generate a beam with null ChannelSegments");
    Validate.notEmpty(channelSegments, "PlaneWaveBeam plugin cannot generate a beam with no "
        + "ChannelSegments");
    Validate.isTrue(relativePositionsByChannelId.keySet().containsAll(
        channelSegments
            .stream()
            .map(ChannelSegment::getChannelId)
            .collect(Collectors.toList())),
        "Relative positions do not contain all channel segments");

    double[] slownessVector = createSlownessVector(horizontalSlowness,
        azimuth,
        mediumVelocity);

    Instant minStart = channelSegments.stream()
        .map(ChannelSegment::getStartTime)
        .min(Instant::compareTo)
        .orElseThrow(NoSuchElementException::new);

    Instant maxEnd = channelSegments.stream()
        .map(ChannelSegment::getEndTime)
        .max(Instant::compareTo)
        .orElseThrow(NoSuchElementException::new);

    logger.info("Generating beams for {} channel segments", channelSegments.size());
    List<ChannelSegment<Waveform>> shiftedChannelSegments = new ArrayList<>();
    for (ChannelSegment<Waveform> channelSegment : channelSegments) {
      RelativePosition position = relativePositionsByChannelId
          .get(channelSegment.getChannelId());
      double offset = (position.getEastDisplacementKm() * slownessVector[0]
          + position.getNorthDisplacementKm() * slownessVector[1] +
          position.getVerticalDisplacementKm() * slownessVector[2]) * -1;

      List<Waveform> shiftedWaveforms = new ArrayList<>();
      for (Waveform waveform : channelSegment.getTimeseries()) {
        int numPointsToShift = (int) Math.round(offset * waveform.getSampleRate());
        double[] shifted = shiftWaveformBySamples(waveform.getValues(), numPointsToShift);
        shiftedWaveforms.add(
            Waveform.withValues(waveform.getStartTime(), waveform.getSampleRate(), shifted));
      }

      shiftedChannelSegments.add(ChannelSegment.create(channelSegment.getChannelId(),
          channelSegment.getName(),
          channelSegment.getType(),
          shiftedWaveforms,
          channelSegment.getCreationInfo()));
    }

    // Break the waveforms out into the beams they will form

    // Map of end times, to the start time and the waveforms for the segment
    JitterPredicate jitterPredicate = new JitterPredicate(minStart);
    long nominalSamplePeriod = (long) (1E9 / nominalSampleRate);
    NavigableMap<Instant, Pair<Instant, List<Waveform>>> beamDurations = new TreeMap<>();
    logger.info("partitioning time into mini-beams");
    for (Instant current = minStart;
         current.compareTo(maxEnd) <= 0;
         current = current.plusNanos(nominalSamplePeriod)) {

      Instant previous;
      if (current.equals(minStart)) {
        previous = current;
      } else {
        previous = current.minusNanos(nominalSamplePeriod);
      }

      // make current effectively final
      Instant compareTime = current;

      List<Waveform> currentWaveforms = shiftedChannelSegments.stream()
          .flatMap(cs -> cs.getTimeseries().stream())
          .filter(this::isSampleRateInTolerance)
          .filter(jitterPredicate)
          .filter(waveform -> isBetweenInclusive(compareTime, waveform.getStartTime(),
              waveform.getEndTime()))
          .collect(Collectors.toList());

      Pair<Instant, List<Waveform>> previousDurationStart = beamDurations.get(previous);
      if (previousDurationStart != null) {

        // If the waveforms from the previous time step and the current time set are the same,
        // extend the previous waveform
        if (previousDurationStart.getValue().containsAll(currentWaveforms) &&
            currentWaveforms.containsAll(previousDurationStart.getValue())) {
          beamDurations.remove(previous);
          beamDurations.put(current, previousDurationStart);
        } else {
          // different set of waveforms, so start a new beam
          beamDurations.put(current, ImmutablePair.of(current, currentWaveforms));
        }
      } else {
        // first time step
        beamDurations.put(current, ImmutablePair.of(current, currentWaveforms));
      }
    }

    // Turn the beam durations into actual beams
    List<Waveform> waveforms = new ArrayList<>();

    for (Map.Entry<Instant, Pair<Instant, List<Waveform>>> miniBeam : beamDurations.entrySet()) {
      logger.info("generating beam segment between {} and {} from {} waveforms",
          miniBeam.getValue().getKey(), miniBeam.getKey(), miniBeam.getValue().getValue().size());
      // splice the waveforms
      List<double[]> beamComponents = new ArrayList<>();
      // TODO: Figure out how to handle this error case better - possibly related to an incoming CR
      if (miniBeam.getKey().equals(miniBeam.getValue().getKey())) {
        logger.info("skipping single-sample beam at {}" + miniBeam.getKey());
      } else {
        for (Waveform waveform : miniBeam.getValue().getValue()) {
          beamComponents.add(waveform.trim(miniBeam.getValue().getKey(), miniBeam.getKey()).getValues());
        }

        int numWaveforms = miniBeam.getValue().getValue().size();
        if (numWaveforms >= minimumWaveformsForBeam) {
          logger.info("Found sufficient waveforms to create beam");
          List<double[]> waveformData = miniBeam.getValue().getValue().stream()
              .map(waveform -> waveform.trim(miniBeam.getValue().getKey(), miniBeam.getKey()))
              .map(Waveform::getValues)
              .collect(Collectors.toList());

          int numSamples = (int) Duration.between(miniBeam.getValue().getKey(), miniBeam.getKey())
              .dividedBy(Duration.ofNanos(nominalSamplePeriod)) + 1;

          double[] sum = new double[numSamples];
          for (double[] waveformValues : waveformData) {
            sum = sum(sum, waveformValues);
          }

          double[] beam = Arrays.stream(sum)
              .map(val -> val / numWaveforms)
              .toArray();
          waveforms.add(Waveform.withValues(miniBeam.getValue().getKey(), nominalSampleRate, beam));
        }
      }
    }
    return waveforms;
  }

  /**
   * Shifts the provided double array by the provided size, padding any empty values with 0.
   *
   * @param waveformData the data to shift
   * @param shiftSize    the number of places to shift, with negative values shifting left and
   *                     positive
   *                     values shifting right.
   * @return a double array that has been shifted by the specified number of points
   */
  private double[] shiftWaveformBySamples(double[] waveformData, int shiftSize) {
    double[] shifted = new double[waveformData.length];
    for (int i = 0; i < shifted.length; i++) {
      if (shiftSize > 0) {
        if (i >= shiftSize && i - shiftSize > 0) {
          shifted[i] = waveformData[i - shiftSize];
        } else {
          shifted[i] = 0;
        }
      } else if (shiftSize < 0) {
        if (i < waveformData.length - Math.abs(shiftSize)) {
          shifted[i] = waveformData[i + Math.abs(shiftSize)];
        } else {
          shifted[i] = 0;
        }
      } else {
        shifted[i] = waveformData[i];
      }
    }

    return shifted;
  }

  private boolean isBetweenInclusive(Instant compareTime, Instant start, Instant end) {
    return compareTime.equals(start) || (compareTime.isAfter(start) && compareTime.isBefore(end)) ||
        compareTime.equals(end);
  }

  private boolean isSampleRateInTolerance(Waveform waveform) {
    return Math.abs(waveform.getSampleRate() - nominalSampleRate) <= sampleRateTolerance;
  }

  /**
   * Adds two double arrays
   *
   * @param base   the double array storing the values that will be added to.
   * @param values the double array that will be added to base
   * @return the base array with the new values added to it.
   */
  private double[] sum(double[] base, double[] values) {
    double[] sum = new double[base.length];
    for (int i = 0; i < base.length && i < values.length; i++) {
      if (coherent) {
        sum[i] = base[i] + values[i];
      } else {
        sum[i] = base[i] + Math.abs(values[i]);
      }
    }

    return sum;
  }

  /**
   * Calculates the horizontalSlowness vector from the provided horizontalSlowness, azimuth,
   * incidence, and medium
   * velocity.  If the PlaneWaveAlgorithm is configured to calculate 2 dimensionaly beams, the
   * medium velocity is ignored.
   *
   * @param horizontalSlowness the horizontalSlowness of the wave for which the beam is being
   *                           calculated
   * @param azimuth            the azimuth of the wave in degrees
   * @param mediumVelocity     the medium velocity of the wave
   * @return the horizontalSlowness vector for the wave
   */
  private double[] createSlownessVector(double horizontalSlowness,
      double azimuth,
      double mediumVelocity) {
    double azimuthRadians = Math.toRadians(azimuth - 180);
    double[] slowness = new double[3];

    slowness[0] = horizontalSlowness * Math.sin(azimuthRadians);
    slowness[1] = horizontalSlowness * Math.cos(azimuthRadians);
    if (twoDimensional) {
      slowness[2] = 0;
    } else {
      slowness[2] =
          Math.sqrt(Math.pow((1.0 / mediumVelocity), 2.0) - horizontalSlowness * horizontalSlowness);
    }

    for (int i = 0; i < 3; i++) {
      if (Math.abs(slowness[i]) < DECIMAL_PRECISION_CUTOFF || Double.isNaN(slowness[i])) {
        slowness[i] = 0.0;
      }
    }

    return slowness;
  }

  /**
   * A mutable builder for a PlaneWaveAlgorithm.  The builder has two phases: a build phase in which
   * it can be modified to reflect add parameters to the PlaneWaveAlgorithm.  When the build()
   * method is called, the Builder transitions to the built phase, to create the PlaneWaveAlgorithm.
   * Once the build() method is called, the Builder can no longer be used.
   */
  public static final class Builder {

    private boolean built = false;

    private double nominalSampleRate;
    private double sampleRateTolerance;
    private double azimuth;
    private double horizontalSlowness;
    private double mediumVelocity;
    private boolean snappedSampling;
    private boolean coherent;
    private boolean twoDimensional;
    private PhaseType phaseType;
    private Map<UUID, RelativePosition> relativePositionsByChannelId;
    private int minimumWaveformsForBeam;

    /**
     * Sets the nominalSampleRate that will be used by the PlaneWaveAlgorithm.
     *
     * @param nominalSampleRate the target sample rate for the waveforms.
     * @return this Builder
     * @throws IllegalStateException if the Builder has already been used to create a
     *                               PlaneWaveAlgorithm
     */
    public Builder withNominalSampleRate(double nominalSampleRate) {
      if (built) {
        throw new IllegalStateException("PlaneWaveAlgorithm has already been built");
      }

      this.nominalSampleRate = nominalSampleRate;
      return this;
    }

    /**
     * Sets the sampleRateTolerance that will be used by the PlaneWaveAlgorithm.
     *
     * @param sampleRateTolerance the tolerance beyond which a sample rate will not be used.
     * @return this Builder
     * @throws IllegalStateException if the Builder has already been used to create a
     *                               PlaneWaveAlgorithm
     */
    public Builder withSampleRateTolerance(double sampleRateTolerance) {
      if (built) {
        throw new IllegalStateException("PlaneWaveAlgorithm has already been built");
      }

      this.sampleRateTolerance = sampleRateTolerance;
      return this;
    }

    /**
     * Sets the azimuth that will be used by the PlaneWaveAlgorithm.
     *
     * @param azimuth the azimuth of the wave
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a
     *                               PlaneWaveAlgorithm
     */
    public Builder withAzimuth(double azimuth) {
      if (built) {
        throw new IllegalStateException("PlaneWaveAlgorithm has already been built");
      }

      this.azimuth = azimuth;
      return this;
    }

    /**
     * Sets the horizontalSlowness that will be used by the PlaneWaveAlgorithm.
     *
     * @param horizontalSlowness the horizontalSlowness of the wave
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a
     *                               PlaneWaveAlgorithm
     */
    public Builder withHorizontalSlowness(double horizontalSlowness) {
      if (built) {
        throw new IllegalStateException("PlaneWaveAlgorithm has already been built");
      }

      this.horizontalSlowness = horizontalSlowness;
      return this;
    }

    /**
     * Sets the medium velocity that will be by the PlaneWaveAlgorithm
     *
     * @param mediumVelocity the medium velocity of the wave.
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a
     *                               PlaneWaveAlgorithm
     */
    public Builder withMediumVelocity(double mediumVelocity) {
      if (built) {
        throw new IllegalStateException("PlaneWaveAlgorithm has already been built");
      }

      this.mediumVelocity = mediumVelocity;
      return this;
    }

    /**
     * Sets whether the PlaneWaveAlgorithm should calculate a coherent beam.
     *
     * @param snappedSampling <code>true</code> of the beam should be calculated using snapped
     *                        sampling, <code>false</code> if it should be calculated using
     *                        interpolated sampling.
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a
     *                               PlaneWaveAlgorithm
     */
    public Builder withSnappedSampling(boolean snappedSampling) {
      if (built) {
        throw new IllegalStateException("PlaneWaveAlgorithm has already been built");
      }

      this.snappedSampling = snappedSampling;
      return this;
    }

    /**
     * Sets whether the PlaneWaveAlgorithm should calculate a coherent beam.
     *
     * @param coherent <code>true</code> of the beam should be coherent, <code>false</code> if it
     *                 should be incoherent
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a
     *                               PlaneWaveAlgorithm
     */
    public Builder withCoherence(boolean coherent) {
      if (built) {
        throw new IllegalStateException("PlaneWaveAlgorithm has already been built");
      }

      this.coherent = coherent;
      return this;
    }

    /**
     * Sets the dimensionality of the wave for the PlaneWaveAlgorithm's calculations.
     *
     * @param twoDimensional <code>true</code> if a 2 dimensional wave should be calculated,
     *                       <code>false</code> otherwise
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a
     *                               PlaneWaveAlgorithm
     */
    public Builder withDimensionality(boolean twoDimensional) {
      if (built) {
        throw new IllegalStateException("PlaneWaveAlgorithm has already been built");
      }

      this.twoDimensional = twoDimensional;
      return this;
    }

    /**
     * Sets the PhaseType of the medium that will be used to calculate the beam.
     *
     * @param phaseType the phaseType of the medium
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a
     *                               PlaneWaveAlgorithm
     */
    public Builder withPhaseType(PhaseType phaseType) {
      if (built) {
        throw new IllegalStateException("PlaneWaveAlgorithm has already been built");
      }

      this.phaseType = phaseType;
      return this;
    }

    /**
     * Sets the RelativePositions of the ChannelSegments that will be used to calculate the beam.
     * The RelativePositions are with regard to ...
     *
     * @param relativePositionsByChannelId the RelativePositions of the ChannelSegments, accessible
     *                                     by ChannelId
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a
     *                               PlaneWaveAlgorithm
     */
    public Builder withRelativePositions(Map<UUID, RelativePosition> relativePositionsByChannelId) {
      if (built) {
        throw new IllegalStateException("PlaneWaveAlgorithm has already been built");
      }

      this.relativePositionsByChannelId = relativePositionsByChannelId;
      return this;
    }

    /**
     * Sets the minimum number of waveforms need to calculate a beam.
     *
     * @param minimumWaveformsForBeam the minimum number of waveforms need to create a beam.
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a
     *                               PlaneWaveAlgorithm
     */
    public Builder withMinimumWaveformsForBeam(int minimumWaveformsForBeam) {
      if (built) {
        throw new IllegalStateException("PlaneWaveAlgorithm has already been built");
      }

      this.minimumWaveformsForBeam = minimumWaveformsForBeam;
      return this;
    }

    /**
     * Builds the PlaneWaveAlgorithm from the parameters defined during the build phase.
     *
     * @return a new PlaneWaveAlgorithm
     * @throws IllegalStateException if the Builder has already been used to create a
     *                               PlaneWaveAlgorithm
     */
    public PlaneWaveAlgorithm build() {
      if (built) {
        throw new IllegalStateException("PlaneWaveAlgorithm has already been built");
      }

      built = true;
      return new PlaneWaveAlgorithm(nominalSampleRate,
          sampleRateTolerance,
          azimuth,
          horizontalSlowness,
          mediumVelocity,
          snappedSampling,
          coherent,
          twoDimensional,
          phaseType,
          relativePositionsByChannelId,
          minimumWaveformsForBeam);
    }
  }

}
