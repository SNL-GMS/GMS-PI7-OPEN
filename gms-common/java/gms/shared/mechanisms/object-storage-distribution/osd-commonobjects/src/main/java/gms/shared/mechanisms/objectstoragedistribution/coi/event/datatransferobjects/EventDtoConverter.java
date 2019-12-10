package gms.shared.mechanisms.objectstoragedistribution.coi.event.datatransferobjects;

import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.util.StdConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FinalEventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredEventHypothesis;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class EventDtoConverter {

  private EventDtoConverter() {
  }

  public static final StdDelegatingSerializer SERIALIZER = new StdDelegatingSerializer(
      new StdConverter<Event, EventDto>() {
        @Override
        public EventDto convert(Event e) {
          return EventDtoConverter.toDto(e);
        }
      });

  public static final StdDelegatingDeserializer<Event> DESERIALIZER = new StdDelegatingDeserializer<>(
      new StdConverter<EventDto, Event>() {
        @Override
        public Event convert(EventDto dto) {
          return EventDtoConverter.fromDto(dto);
        }
      });

  public static EventDto toDto(Event e) {
    Objects.requireNonNull(e, "Refusing to create DTO from null event");

    return new EventDto(e.getId(), e.getRejectedSignalDetectionAssociations(),
        e.getMonitoringOrganization(), e.getHypotheses(), getFinalHistoryDtos(e),
        getPreferredHistoryDtos(e));
  }

  public static Event fromDto(EventDto dto) {
    Objects.requireNonNull(dto, "Cannot create Event from null EventDto");

    return Event.from(
        dto.getId(), dto.getRejectedSignalDetectionAssociations(),
        dto.getMonitoringOrganization(), dto.getHypotheses(),
        convertFinalHistory(dto),
        convertPreferredHistory(dto));
  }

  private static List<FinalEventHypothesis> convertFinalHistory(EventDto dto) {
    return dto.getFinalEventHypothesisHistory()
        .stream()
        .map(f -> getEventHypothesisById(dto, f.getEventHypothesisId()))  // Stream<EventHypothesis>
        .map(
            FinalEventHypothesis::from)                                  // Stream<FinalEventHypothesis>
        .collect(Collectors.toList());
  }

  private static List<PreferredEventHypothesis> convertPreferredHistory(EventDto dto) {
    return dto.getPreferredEventHypothesisHistory().stream()
        .map(p -> PreferredEventHypothesis.from(p.getProcessingStageId(),
            getEventHypothesisById(dto, p.getEventHypothesisId())))
        .collect(Collectors.toList());
  }

  private static EventHypothesis getEventHypothesisById(EventDto e, UUID id) {

    return e.getHypotheses().stream()
        .filter(eh -> eh.getId().equals(id))
        .findFirst()
        .orElseThrow(
            () -> new IllegalStateException("Expected to find EventHypothesis with id " + id));
  }

  private static List<PreferredEventHypothesisDto> getPreferredHistoryDtos(Event e) {
    return e.getPreferredEventHypothesisHistory().stream()
        // Make pairs with Left as processingStageId, right as EventHypothesis ID
        .map(p -> new PreferredEventHypothesisDto(
            p.getProcessingStageId(), p.getEventHypothesis().getId()))
        .collect(Collectors.toList());
  }

  private static List<FinalEventHypothesisDto> getFinalHistoryDtos(Event e) {
    return e.getFinalEventHypothesisHistory().stream()
        .map(f -> new FinalEventHypothesisDto(f.getEventHypothesis().getId()))
        .collect(Collectors.toList());
  }
}
