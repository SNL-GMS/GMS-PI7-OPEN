package gms.core.signalenhancement.planewavebeam.util;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class BeamValidationData {

  private final double nominalSampleRate;
  private final double sampleRateTolerance;
  private final double azimuth;
  private final double horizontalSlowness;
  private final double mediumVelocity;
  private final boolean snappedSampling;
  private final boolean coherent;
  private final boolean twoDimensional;
  private final Instant startTime;
  private final PhaseType phaseType;
  private final Map<UUID, RelativePosition> relativePositionsByChannelId;
  private final Collection<ChannelSegment<Waveform>> channelSegments;
  private final Waveform beam;

  public BeamValidationData(double nominalSampleRate,
      double sampleRateTolerance,
      double azimuth,
      double horizontalSlowness,
      double mediumVelocity,
      boolean snappedSampling,
      boolean coherent,
      boolean twoDimensional,
      Instant startTime,
      PhaseType phaseType,
      Map<UUID, RelativePosition> relativePositionsByChannelId,
      Collection<ChannelSegment<Waveform>> channelSegments,
      Waveform beam) {
    this.nominalSampleRate = nominalSampleRate;
    this.sampleRateTolerance = sampleRateTolerance;
    this.azimuth = azimuth;
    this.horizontalSlowness = horizontalSlowness;
    this.mediumVelocity = mediumVelocity;
    this.snappedSampling = snappedSampling;
    this.coherent = coherent;
    this.twoDimensional = twoDimensional;
    this.startTime = startTime;
    this.phaseType = phaseType;
    this.relativePositionsByChannelId = relativePositionsByChannelId;
    this.channelSegments = channelSegments;
    this.beam = beam;
  }

  public double getNominalSampleRate() {
    return nominalSampleRate;
  }

  public double getSampleRateTolerance() {
    return sampleRateTolerance;
  }

  public double getAzimuth() {
    return azimuth;
  }

  public double getHorizontalSlowness() {
    return horizontalSlowness;
  }

  public double getMediumVelocity() {
    return mediumVelocity;
  }

  public boolean isSnappedSampling() {
    return snappedSampling;
  }

  public boolean isCoherent() {
    return coherent;
  }

  public boolean isTwoDimensional() {
    return twoDimensional;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public PhaseType getPhaseType() {
    return phaseType;
  }

  public Map<UUID, RelativePosition> getRelativePositionsByChannelId() {
    return relativePositionsByChannelId;
  }

  public Collection<ChannelSegment<Waveform>> getChannelSegments() {
    return channelSegments;
  }

  public Waveform getBeam() {
    return beam;
  }
}
