package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class StationProcessingInterval {

  public abstract UUID getId();

  public abstract UUID getStationId();

  public abstract List<UUID> getProcessingIds();

  public abstract Instant getStartTime();

  public abstract Instant getEndTime();

  @JsonCreator
  public static StationProcessingInterval from(
      @JsonProperty("id") UUID id,
      @JsonProperty("stationId") UUID stationId,
      @JsonProperty("processingIds") List<UUID> processingIds,
      @JsonProperty("startTime") Instant startTime,
      @JsonProperty("endTime") Instant endTime) {
    Preconditions.checkNotNull(id);
    Preconditions.checkNotNull(stationId);
    Preconditions.checkNotNull(processingIds);
    Preconditions.checkArgument(!processingIds.isEmpty(),
        "Error creating StationProcessingInterval: processingIds cannot be empty");
    Preconditions.checkNotNull(startTime);
    Preconditions.checkNotNull(endTime);
    Preconditions.checkArgument(startTime.isBefore(endTime),
        "Error creating StationProcessingInterval: startTime must be before endTime");

    return new AutoValue_StationProcessingInterval(id, stationId, processingIds, startTime,
        endTime);
  }
}
