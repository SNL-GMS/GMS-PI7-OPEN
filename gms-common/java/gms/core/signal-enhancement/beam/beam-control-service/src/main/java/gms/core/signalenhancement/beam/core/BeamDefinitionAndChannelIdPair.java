package gms.core.signalenhancement.beam.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import java.util.UUID;

@AutoValue
public abstract class BeamDefinitionAndChannelIdPair {

  @JsonCreator
  public static BeamDefinitionAndChannelIdPair from(
      @JsonProperty("beamOutputId") UUID outputChannelId,
      @JsonProperty("beamDefinition") BeamDefinition beamDef) {

    return new AutoValue_BeamDefinitionAndChannelIdPair(outputChannelId, beamDef);
  }

  public abstract UUID getBeamOutputId();

  public abstract BeamDefinition getBeamDefinition();
}
