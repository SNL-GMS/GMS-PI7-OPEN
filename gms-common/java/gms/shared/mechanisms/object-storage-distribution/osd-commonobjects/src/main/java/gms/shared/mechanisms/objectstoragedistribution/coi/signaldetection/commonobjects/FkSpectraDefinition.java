package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Describes parameters used to calculate {@link FkSpectrum}, but does not include input
 * waveform data.  This allows this class to be used to describe general Fk Spectrum
 * calculations, instead of a calculation for a specific time interval.  Describes the slowness grid
 * independently for east and north dimensions, channel positioning, and the amount of waveform data
 * used to compute each Fk Spectra.
 */
@AutoValue
public abstract class FkSpectraDefinition {

  public static Builder builder() {
    return new AutoValue_FkSpectraDefinition.Builder();
  }

  public abstract Builder toBuilder();

  /**
   * Obtain the sample rate of the FkSpectra as a double (Units: Hz)
   *
   * @return startTime, not null.
   */
  public abstract double getSampleRateHz();

  /**
   * Obtain {@link Duration} used to offset the window of waveform data used to compute each Fk
   * Spectrum
   *
   * @return windowLead, not null
   */
  public abstract Duration getWindowLead();

  /**
   * Obtain {@link Duration} of waveform data used to compute each Fk Spectra
   *
   * @return {@code windowLength, > 0, not null }
   */
  public abstract Duration getWindowLength();

  /**
   * Obtain the low pass-band frequency used to calculate Fk Spectra
   *
   * @return {@code lowFrequency, in Hz, >= 0}
   */
  public abstract double getLowFrequencyHz();

  /**
   * Obtain the high pass-band frequency used to calculate Fk Spectra
   *
   * @return {@code highFrequency, in Hz, > lowFrequency }
   */
  public abstract double getHighFrequencyHz();

  /**
   * Obtain whether to use channel vertical position when computing time delays
   *
   * @return useChannelVerticalOffsets
   */
  public abstract boolean getUseChannelVerticalOffsets();

  public abstract boolean getNormalizeWaveforms();

  public abstract PhaseType getPhaseType();

  /**
   * Obtain first east slowness value in each Fk Spectra
   *
   * @return eastSlowStart, in s/km
   */
  public abstract double getSlowStartXSecPerKm();

  /**
   * Obtain step between east slowness values in each Fk Spectra
   *
   * @return eastSlowDelta, in s/km
   */
  public abstract double getSlowDeltaXSecPerKm();

  /**
   * Obtain total number of east slowness values in each Fk Spectra
   *
   * @return eastSlowCount
   */
  public abstract int getSlowCountX();

  /**
   * Obtain first north slowness value in each Fk Spectra
   *
   * @return eastSlowEnd, in s/km
   */
  public abstract double getSlowStartYSecPerKm();

  /**
   * Obtain step between north slowness values in each Fk Spectra
   *
   * @return northSlowStart, in s/km
   */
  public abstract double getSlowDeltaYSecPerKm();

  /**
   * Obtain total number of north slowness values in each Fk Spectra
   *
   * @return northSlowCount
   */
  public abstract int getSlowCountY();

  /**
   * Obtain the nominal sample rate of the waveforms used to compute the Fk Spectra
   *
   * @return {@literal waveformSampleRateHz, > 0 }
   */
  public abstract double getWaveformSampleRateHz();

  /**
   * Obtain the allowed deviation from waveformSampleRateHz
   *
   * @return {@literal waveformSampleRateToleranceHz, >= 0 }
   */
  public abstract double getWaveformSampleRateToleranceHz();

  /**
   * Obtain a {@link Location} defining the reference fk point all {@link RelativePosition}s are
   * derived from.
   *
   * @return referenceLocation, not null
   */
  public abstract Location getBeamPoint();

  /**
   * Obtains the minimum number of waveforms needed to create an FkSpectra
   *
   * @return the minimum number of waveforms needed to create an FkSpectra, greater than 1
   */
  public abstract int getMinimumWaveformsForSpectra();

  /**
   * Obtain {@link RelativePosition} by {@link Channel} UUID for any channel used to compute Fk
   * Power Spectra.  Used to compute time delays.
   *
   * @return relativePositions, not null
   */
  public abstract Map<UUID, RelativePosition> getRelativePositionsByChannelId();

  @JsonCreator
  static FkSpectraDefinition from(
      @JsonProperty("sampleRateHz") double sampleRateHz,
      @JsonProperty("windowLead") Duration windowLead,
      @JsonProperty("windowLength") Duration windowLength,
      @JsonProperty("lowFrequencyHz") double lowFrequencyHz,
      @JsonProperty("highFrequencyHz") double highFrequencyHz,
      @JsonProperty("useChannelVerticalOffsets") boolean useChannelVerticalOffsets,
      @JsonProperty("normalizeWaveforms") boolean normalizeWaveforms,
      @JsonProperty("phaseType") PhaseType phaseType,
      @JsonProperty("slowStartXSecPerKm") double slowStartXSecPerKm,
      @JsonProperty("slowDeltaXSecPerKm") double slowDeltaXSecPerKm,
      @JsonProperty("slowCountX") int slowCountX,
      @JsonProperty("slowStartYSecPerKm") double slowStartYSecPerKm,
      @JsonProperty("slowDeltaYSecPerKm") double slowDeltaYSecPerKm,
      @JsonProperty("slowCountY") int slowCountY,
      @JsonProperty("waveformSampleRateHz") double waveformSampleRateHz,
      @JsonProperty("waveformSampleRateToleranceHz") double waveformSampleRateToleranceHz,
      @JsonProperty("beamPoint") Location beamPoint,
      @JsonProperty("relativePositionsByChannelId") Map<UUID, RelativePosition> relativePositionsByChannelId,
      @JsonProperty("minimumWaveformsForSpectra") int minimumWaveformsForSpectra) {

    return FkSpectraDefinition.builder()
        .setSampleRateHz(sampleRateHz)
        .setWindowLead(windowLead)
        .setWindowLength(windowLength)
        .setLowFrequencyHz(lowFrequencyHz)
        .setHighFrequencyHz(highFrequencyHz)
        .setUseChannelVerticalOffsets(useChannelVerticalOffsets)
        .setNormalizeWaveforms(normalizeWaveforms)
        .setPhaseType(phaseType)
        .setSlowStartXSecPerKm(slowStartXSecPerKm)
        .setSlowDeltaXSecPerKm(slowDeltaXSecPerKm)
        .setSlowCountX(slowCountX)
        .setSlowStartYSecPerKm(slowStartYSecPerKm)
        .setSlowDeltaYSecPerKm(slowDeltaYSecPerKm)
        .setSlowCountY(slowCountY)
        .setWaveformSampleRateHz(waveformSampleRateHz)
        .setWaveformSampleRateToleranceHz(waveformSampleRateToleranceHz)
        .setBeamPoint(beamPoint)
        .setRelativePositionsByChannelId(relativePositionsByChannelId)
        .setMinimumWaveformsForSpectra(minimumWaveformsForSpectra)
        .build();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setSampleRateHz(double sampleRate);

    public abstract Builder setWindowLead(Duration windowLead);

    public abstract Builder setWindowLength(Duration windowLength);

    public abstract Builder setLowFrequencyHz(double lowFrequency);

    public abstract Builder setHighFrequencyHz(double highFrequency);

    public abstract Builder setUseChannelVerticalOffsets(boolean useChannelVerticalOffsets);

    public abstract Builder setNormalizeWaveforms(boolean normalizeWaveforms);

    public abstract Builder setPhaseType(PhaseType phaseType);

      public abstract Builder setSlowStartXSecPerKm(double slowStartX);

    public abstract Builder setSlowDeltaXSecPerKm(double slowDeltaX);

    public abstract Builder setSlowCountX(int slowCountX);

    public abstract Builder setSlowStartYSecPerKm(double slowStartY);

    public abstract Builder setSlowDeltaYSecPerKm(double slowDeltaY);

    public abstract Builder setSlowCountY(int slowCountY);

    public abstract Builder setWaveformSampleRateHz(double nominalWaveformSampleRateHz);

    public abstract Builder setWaveformSampleRateToleranceHz(double nominalWaveformSampleRateToleranceHz);

    public abstract Builder setBeamPoint(Location referenceLocation);

    public abstract Builder setRelativePositionsByChannelId(Map<UUID, RelativePosition> relativePositions);

    abstract Map<UUID, RelativePosition> getRelativePositionsByChannelId();

    public abstract Builder setMinimumWaveformsForSpectra(int minimumWaveformsForSpectra);

    protected abstract FkSpectraDefinition autobuild();

    public FkSpectraDefinition build() {
      setRelativePositionsByChannelId(ImmutableMap.copyOf(getRelativePositionsByChannelId()));
      FkSpectraDefinition definition = autobuild();

      Preconditions.checkState(definition.getWindowLength().compareTo(Duration.ZERO) > 0,
          "FkSpectraDefinition requires windowLength of Duration > 0");
      Preconditions.checkState(definition.getSampleRateHz() > 0.0,
          "FkSpectraDefinition requires sampleRate > 0.0");
      Preconditions.checkState(definition.getLowFrequencyHz() >= 0.0,
          "FkSpectraDefinition requires lowFrequency >= 0.0");
      Preconditions.checkState(definition.getHighFrequencyHz() > definition.getLowFrequencyHz(),
          "FkSpectraDefinition requires lowFrequency < highFrequency");
      Preconditions.checkState(definition.getSlowCountX() > 0,
          "FkSpectraDefinition requires slowCountX > 0");
      Preconditions.checkState(definition.getSlowCountY() > 0,
          "FkSpectraDefinition requires slowCountY > 0");
      Preconditions.checkState(definition.getWaveformSampleRateHz() > 0.0,
          "FkSpectraDefinition requires waveformSampleRateHz > 0.0");
      Preconditions.checkState(definition.getWaveformSampleRateToleranceHz() >= 0.0,
          "FkSpectraDefinition requires waveformSampleRateToleranceHz >= 0.0");
      Preconditions.checkState(definition.getMinimumWaveformsForSpectra() > 1,
          "FkSpectraDefinition requires minimumWaveformsForSpectra > 1");

      return definition;
    }
  }

}
