package gms.core.signalenhancement.fk.plugin.algorithms;

import gms.core.signalenhancement.fk.plugin.fkspectra.FkSpectraPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.utilities.geomath.MediumVelocities;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class CaponFkSpectraPlugin implements FkSpectraPlugin {

  private static final String PLUGIN_NAME = "caponFkSpectraPlugin";
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
  public List<FkSpectra> generateFk(Collection<ChannelSegment<Waveform>> channelSegments,
      FkSpectraDefinition definition) {
    Objects.requireNonNull(channelSegments,
        PLUGIN_NAME + " cannot generate FK spectra from null channel segments");
    Objects.requireNonNull(definition,
        PLUGIN_NAME + " cannot generate FK spectra from null FK spectra definition");

    MediumVelocities mediumVelocities = new MediumVelocities();
    try {
      mediumVelocities.initialize(DEFAULT_MODEL_NAME);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to initialize MediumVelocities Utility", e);
    }

    CaponFkSpectrumAlgorithm algorithm = new CaponFkSpectrumAlgorithm.Builder()
        .useChannelVerticalOffsets(definition.getUseChannelVerticalOffsets())
        .normalizeWaveforms(definition.getNormalizeWaveforms())
        .withRelativePositionMap(definition.getRelativePositionsByChannelId())
        .withWaveformSampleRateHz(definition.getWaveformSampleRateHz())
        .withMediumVelocityKmPerSec(mediumVelocities
            .getMediumVelocity(definition.getBeamPoint(), definition.getPhaseType()))
        .withLowFrequency(definition.getLowFrequencyHz())
        .withHighFrequency(definition.getHighFrequencyHz())
        .withEastSlowCount(definition.getSlowCountX())
        .withEastSlowStart(definition.getSlowStartXSecPerKm())
        .withEastSlowDelta(definition.getSlowDeltaXSecPerKm())
        .withNorthSlowCount(definition.getSlowCountY())
        .withNorthSlowStart(definition.getSlowStartYSecPerKm())
        .withNorthSlowDelta(definition.getSlowDeltaYSecPerKm())
        .withWindowLead(definition.getWindowLead())
        .withWindowLength(definition.getWindowLength())
        .withSampleRate(definition.getSampleRateHz())
        .withMinimumWaveformsForSpectra(definition.getMinimumWaveformsForSpectra())
        .build();

    FkSpectra.Builder spectra = FkSpectra.builder()
        .setStartTime(
            channelSegments.iterator().next().getStartTime().plus(definition.getWindowLead()))
        .setSampleRate(definition.getSampleRateHz())
        .withValues(algorithm.generateFk(channelSegments));

    spectra.metadataBuilder()
        .setPhaseType(definition.getPhaseType())
        .setSlowStartX(definition.getSlowStartXSecPerKm())
        .setSlowDeltaX(definition.getSlowDeltaXSecPerKm())
        .setSlowStartY(definition.getSlowStartYSecPerKm())
        .setSlowDeltaY(definition.getSlowDeltaYSecPerKm());

    return List.of(spectra.build());
  }
}
