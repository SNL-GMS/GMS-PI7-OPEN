package gms.core.signalenhancement.beam.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Set;
import java.util.UUID;

/**
 * Wrapper class containing all needed data in order to executeStreaming {@link BeamControl}
 * via streaming.
 */
@AutoValue
public abstract class BeamStreamingCommand {

  /**
   * Factory method for creating a standard BeamStreamingCommand
   *
   * @param outputChannelId Map of channel segments to output channel ids upon which the Beam
   * operation is performed and output to
   * @param waveforms Timeseries data used as input for the Beam
   * @param beamDefinition Parameters used by the Beam algorithm
   * @return A standard command object used for executing an Beam
   */
  @JsonCreator
  public static BeamStreamingCommand create(
      @JsonProperty("outputChannelId") UUID outputChannelId,
      @JsonProperty("waveforms") Set<ChannelSegment<Waveform>> waveforms,
      @JsonProperty("beamDefinition") BeamDefinition beamDefinition) {

    return builder()
        .setOutputChannelId(outputChannelId)
        .setWaveforms(waveforms)
        .setBeamDefinition(beamDefinition)
        .build();
  }

  public abstract UUID getOutputChannelId();

  public abstract Set<ChannelSegment<Waveform>> getWaveforms();

  public abstract BeamDefinition getBeamDefinition();

  public static Builder builder() {
    return new AutoValue_BeamStreamingCommand.Builder();
  }

  @AutoValue.Builder
  public static abstract class Builder {

    public abstract Builder setOutputChannelId(UUID outputChannelId);

    public abstract Builder setWaveforms(Set<ChannelSegment<Waveform>> waveforms);

    abstract Set<ChannelSegment<Waveform>> getWaveforms();

    public abstract Builder setBeamDefinition(BeamDefinition beamDefinition);

    abstract BeamStreamingCommand autoBuild();

    public BeamStreamingCommand build() {
      setWaveforms(ImmutableSet.copyOf(getWaveforms()));
      return autoBuild();
    }

  }

}
