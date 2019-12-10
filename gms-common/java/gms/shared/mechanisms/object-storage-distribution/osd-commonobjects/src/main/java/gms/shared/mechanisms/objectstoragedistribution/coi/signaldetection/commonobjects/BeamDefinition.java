package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class BeamDefinition {

  public abstract PhaseType getPhaseType();

  public abstract double getAzimuth();

  public abstract double getSlowness();

  public abstract boolean isCoherent();

  public abstract boolean isSnappedSampling();

  public abstract boolean isTwoDimensional();

  public abstract double getNominalWaveformSampleRate();

  public abstract double getWaveformSampleRateTolerance();

  public abstract Location getBeamPoint();

  public abstract Map<UUID, RelativePosition> getRelativePositionsByChannelId();

  public abstract int getMinimumWaveformsForBeam();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_BeamDefinition.Builder();
  }

  @JsonCreator
  public static BeamDefinition from(
      @JsonProperty("phaseType") PhaseType phaseType,
      @JsonProperty("azimuth") double azimuth,
      @JsonProperty("slowness") double slowness,
      @JsonProperty("coherent") boolean coherent,
      @JsonProperty("snappedSampling") boolean snappedSampling,
      @JsonProperty("twoDimensional") boolean twoDimensional,
      @JsonProperty("nominalWaveformSampleRate") double nominalWaveformSampleRate,
      @JsonProperty("waveformSampleRateTolerance") double waveformSampleRateTolerance,
      @JsonProperty("beamPoint") Location beamPoint,
      @JsonProperty("relativePositionsByChannelId") Map<UUID, RelativePosition> relativePositionsByChannelId,
      @JsonProperty("minimumWaveformsForBeam") int minimumWaveformsForBeam) {

    return BeamDefinition.builder()
        .setPhaseType(phaseType)
        .setAzimuth(azimuth)
        .setSlowness(slowness)
        .setCoherent(coherent)
        .setSnappedSampling(snappedSampling)
        .setTwoDimensional(twoDimensional)
        .setNominalWaveformSampleRate(nominalWaveformSampleRate)
        .setWaveformSampleRateTolerance(waveformSampleRateTolerance)
        .setBeamPoint(beamPoint)
        .setRelativePositionsByChannelId(relativePositionsByChannelId)
        .setMinimumWaveformsForBeam(minimumWaveformsForBeam)
        .build();
  }



  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setPhaseType(PhaseType phaseType);

    public abstract Builder setAzimuth(double azimuth);

    public abstract Builder setSlowness(double slowness);

    public abstract Builder setCoherent(boolean coherent);

    public abstract Builder setSnappedSampling(boolean snappedSampling);

    public abstract Builder setTwoDimensional(boolean twoDimensional);

    public abstract Builder setNominalWaveformSampleRate(double nominalSampleRate);

    public abstract Builder setWaveformSampleRateTolerance(double sampleRateTolerance);

    public abstract Builder setBeamPoint(Location beamPoint);

    public abstract Builder setRelativePositionsByChannelId(
        Map<UUID, RelativePosition> relativePositionsByChannelId);

    abstract Map<UUID, RelativePosition> getRelativePositionsByChannelId();

    public abstract Builder setMinimumWaveformsForBeam(int minimumWaveformsForBeam);

    abstract BeamDefinition autoBuild();

    public BeamDefinition build() {
      setRelativePositionsByChannelId(ImmutableMap.copyOf(getRelativePositionsByChannelId()));
      BeamDefinition beamDefinition = autoBuild();

      Preconditions.checkState(beamDefinition.getAzimuth() >= 0
              && beamDefinition.getAzimuth() <= 360,
          "Error creating BeamDefinition, azimuth must be between 0 and 360, inclusive");

      Preconditions.checkState(beamDefinition.getMinimumWaveformsForBeam() >= 1,
          "Error creating BeamDefinition, minimum waveforms for beam must be at least 1");

      return beamDefinition;
    }

  }

}

