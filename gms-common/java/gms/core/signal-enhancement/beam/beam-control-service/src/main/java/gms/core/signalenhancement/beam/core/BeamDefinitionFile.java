package gms.core.signalenhancement.beam.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class BeamDefinitionFile {

  @JsonCreator
  public static BeamDefinitionFile from(
      @JsonProperty("phaseType") PhaseType phaseType,
      @JsonProperty("coherent") boolean coherent,
      @JsonProperty("snappedSampling") boolean snappedSampling,
      @JsonProperty("twoDimensional") boolean twoDimensional,
      @JsonProperty("nominalWaveformSampleRate") double nominalWaveformSampleRate,
      @JsonProperty("waveformSampleRateTolerance") double waveformSampleRateTolerance,
      @JsonProperty("beamGrid") List<SlownessAzimuthPair> beamGrid,
      @JsonProperty("beamPoint") Location beamPoint,
      @JsonProperty("relativePositionsByChannelId") Map<UUID, RelativePosition> relativePositionsByChannelId,
      @JsonProperty("minimumWaveformsForBeam") int minimumWaveformsForBeam) {

    return new AutoValue_BeamDefinitionFile.Builder()
        .setPhaseType(phaseType)
        .setCoherent(coherent)
        .setSnappedSampling(snappedSampling)
        .setTwoDimensional(twoDimensional)
        .setNominalWaveformSampleRate(nominalWaveformSampleRate)
        .setWaveformSampleRateTolerance(waveformSampleRateTolerance)
        .setBeamGrid(beamGrid)
        .setBeamPoint(beamPoint)
        .setRelativePositionsByChannelId(relativePositionsByChannelId)
        .setMinimumWaveformsForBeam(minimumWaveformsForBeam)
        .build();
  }

  public abstract PhaseType getPhaseType();

  public abstract boolean isCoherent();

  public abstract boolean isSnappedSampling();

  public abstract boolean isTwoDimensional();

  public abstract double getNominalWaveformSampleRate();

  public abstract double getWaveformSampleRateTolerance();

  public abstract List<SlownessAzimuthPair> getBeamGrid();

  public abstract Location getBeamPoint();

  public abstract Map<UUID, RelativePosition> getRelativePositionsByChannelId();

  public abstract int getMinimumWaveformsForBeam();

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setPhaseType(PhaseType phaseType);

    public abstract Builder setBeamGrid(List<SlownessAzimuthPair> beamGrid);

    public abstract Builder setCoherent(boolean coherent);

    public abstract Builder setSnappedSampling(boolean snappedSampling);

    public abstract Builder setTwoDimensional(boolean twoDimensional);

    public abstract Builder setNominalWaveformSampleRate(double nominalSampleRate);

    public abstract Builder setWaveformSampleRateTolerance(double sampleRateTolerance);

    public abstract Builder setBeamPoint(Location beamPoint);

    public abstract Builder setRelativePositionsByChannelId(
        Map<UUID, RelativePosition> relativePositionsByChannelId);

    public abstract Builder setMinimumWaveformsForBeam(int minimumWaveformsForBeam);

    abstract BeamDefinitionFile build();
  }

}