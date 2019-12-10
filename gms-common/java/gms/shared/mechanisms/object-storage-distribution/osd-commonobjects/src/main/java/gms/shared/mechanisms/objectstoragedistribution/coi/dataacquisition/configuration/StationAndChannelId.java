package gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.UUID;

@AutoValue
public abstract class StationAndChannelId {

  public abstract UUID getStationId();

  public abstract UUID getChannelId();

  @JsonCreator
  public static StationAndChannelId from(
      @JsonProperty("stationId") UUID stationId,
      @JsonProperty("channelId") UUID channelId) {
    return new AutoValue_StationAndChannelId(stationId, channelId);
  }

}
