package gms.core.signalenhancement.planewavebeam.plugin;

import gms.core.signalenhancement.beamcontrol.plugin.BeamPlugin;
import gms.core.signalenhancement.planewavebeam.PlaneWaveAlgorithm;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.utilities.geomath.MediumVelocities;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlaneWaveBeamPlugin implements BeamPlugin {

  private static final String PLUGIN_NAME = "planeWaveBeamPlugin";
  private static final String DEFAULT_MODEL_NAME = "ak135";

  @Override
  public String getName() {
    return PLUGIN_NAME;
  }

  @Override
  public PluginVersion getVersion() {
    return PluginVersion.from(1, 0, 0);
  }

  @Override
  public void initialize(Map<String, Object> parameterFieldMap) {

  }

  @Override
  public List<Waveform> beam(Collection<ChannelSegment<Waveform>> channelSegments,
      BeamDefinition beamDefinition) {
    Objects.requireNonNull(channelSegments,
        "PlaneWaveBeam cannot calculate beam from null channel segments");
    Objects.requireNonNull(beamDefinition,
        "PlaneWaveBeam cannot calculate beam from null definition");

    MediumVelocities mediumVelocities = new MediumVelocities();
    try {
      mediumVelocities.initialize(DEFAULT_MODEL_NAME);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to initialize MediumVelocities Utility", e);
    }

    PlaneWaveAlgorithm algorithm = new PlaneWaveAlgorithm.Builder()
        .withNominalSampleRate(beamDefinition.getNominalWaveformSampleRate())
        .withSnappedSampling(beamDefinition.isSnappedSampling())
        .withPhaseType(beamDefinition.getPhaseType())
        .withRelativePositions(beamDefinition.getRelativePositionsByChannelId())
        .withDimensionality(beamDefinition.isTwoDimensional())
        .withCoherence(beamDefinition.isCoherent())
        .withMediumVelocity(mediumVelocities.getMediumVelocity(beamDefinition.getBeamPoint(), beamDefinition.getPhaseType()))
        .withAzimuth(beamDefinition.getAzimuth())
        .withSampleRateTolerance(beamDefinition.getWaveformSampleRateTolerance())
        .withHorizontalSlowness(beamDefinition.getSlowness())
        .withMinimumWaveformsForBeam(beamDefinition.getMinimumWaveformsForBeam())
        .build();

    return algorithm.generateBeam(channelSegments);
  }
}
