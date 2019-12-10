package gms.core.signalenhancement.beam.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class BeamExecutorStationList {

  @JsonCreator
  public static BeamExecutorStationList from(
      @JsonProperty("stationIds") List<UUID> stationIds){

    return new AutoValue_BeamExecutorStationList(stationIds);
  }

  public abstract List<UUID> getStationIds();
}
