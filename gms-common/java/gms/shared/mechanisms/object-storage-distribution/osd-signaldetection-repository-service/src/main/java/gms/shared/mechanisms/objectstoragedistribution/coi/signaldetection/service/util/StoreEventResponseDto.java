package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class StoreEventResponseDto {

  public abstract List<UUID> getStoredEvents();
  public abstract List<UUID> getUpdatedEvents();
  public abstract List<UUID> getErrorEvents();

  @JsonCreator
  public static StoreEventResponseDto from (
      @JsonProperty("storedEvents") List<UUID> storedEvents,
      @JsonProperty("updatedEvents") List<UUID> updatedEvents,
      @JsonProperty("errorEvents") List<UUID> errorEvents
  ) {
    return new AutoValue_StoreEventResponseDto(storedEvents, updatedEvents, errorEvents);
  }
}
