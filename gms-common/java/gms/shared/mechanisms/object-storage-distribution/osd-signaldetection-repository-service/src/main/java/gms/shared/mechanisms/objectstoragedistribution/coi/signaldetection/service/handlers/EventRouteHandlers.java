package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.DataExistsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.EventRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.StoreEventResponseDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang.Validate;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 * Contains handler functions for the service routes.
 */
public class EventRouteHandlers {

  private static Logger logger = LoggerFactory.getLogger(EventRouteHandlers.class);

  /**
   * Serializes and deserializes signal detection common objects
   */
  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private static final ObjectMapper messagePackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

  private final EventRepository eventRepository;

  private EventRouteHandlers(EventRepository eventRepository){
      this.eventRepository = eventRepository;
  }

  /**
  * Factory method for creating {@link EventRouteHandlers}
  *
  * @param eventRepository Event Repository class
  * @return The route handlers object using the input repository
  */
  public static EventRouteHandlers create(EventRepository eventRepository) {
    return new EventRouteHandlers(Objects.requireNonNull(eventRepository));
  }

  /**
   * Handles a request to retrieve a {@link List} of {@link Event} given a collection of UUIDs
   *
   * Returns HTTP status codes: 200 when successful 400 when the parameters are not valid
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   */
  public Object retrieveEventsByIds(Request request, Response response) throws Exception {
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");

    JsonNode postParams = objectMapper.readTree(request.body());

    Validate.notNull(postParams,
        "retrieveEvents(): POST body cannot be null, expected list of IDs");

    //Extract IDs
    UUID[] eventIds;
    if (requestIsMessagePack(request)) {
      eventIds = messagePackMapper.readValue(postParams.toString(), UUID[].class);
    } else {
      eventIds = objectMapper.readValue(postParams.toString(), UUID[].class);
    }

    Collection<Event> eventCollection = eventRepository.findEventsByIds(Arrays.asList(eventIds));

    if(eventCollection.isEmpty()){
      response.type("text/plain");
      response.status(HttpStatus.NOT_FOUND_404);
      return
          "No events exist for the provided UUIDs.";
    }
    else {
      if (shouldReturnMessagePack(request)) {
        response.type("application/msgpack");
        return messagePackMapper.writeValueAsBytes(eventCollection);
      } else {
        return objectMapper.writeValueAsString(eventCollection);
      }
    }
  }

  /**
   * Handles a request to retrieve {@link Event}'s, given a time range and lat/long
   *
   * Returns HTTP status codes: 200 when successful 400 when the parameters are not valid
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   */
  public Object retrieveEventsByTimeAndLocation(Request request, Response response)
      throws Exception {
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");

    JsonNode postParams = objectMapper.readTree(request.body());
    Validate.notNull(postParams.get("startTime"),
        "retrieveEventsByTimeAndLocation(): startTime parameter in POST body cannot be null");
    Validate.notNull(postParams.get("endTime"),
        "retrieveEventsByTimeAndLocation(): endTime parameter in POST body cannot be null");

    //Extract start and end times
    Instant startTime = Instant.parse(postParams.get("startTime").asText());
    Instant endTime = Instant.parse(postParams.get("endTime").asText());

    //Extract optional params
    final double minLatitude = readOptionalJsonField(postParams, "minLatitude", -90);
    final double maxLatitude = readOptionalJsonField(postParams, "maxLatitude", 90);
    final double minLongitude = readOptionalJsonField(postParams, "minLongitude",-180);
    final double maxLongitude = readOptionalJsonField(postParams, "maxLongitude", 180);
    final Collection<Event> eventCollection = eventRepository.findEventsByTimeAndLocation(
        startTime, endTime, minLatitude, maxLatitude, minLongitude, maxLongitude);

    if (shouldReturnMessagePack(request)) {
      response.type("application/msgpack");
      return messagePackMapper.writeValueAsBytes(eventCollection);
    } else {
      return objectMapper.writeValueAsString(eventCollection);
    }
  }

  /**
   * Handles a request to store an {@link Event}
   *
   * Returns 200 if the store was successful
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP)
   */
  public Object storeEvents(Request request, Response response) throws Exception, DataExistsException {
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");

    JsonNode postParams = objectMapper.readTree(request.body());

    Validate.notNull(postParams,
        "storeEvents(): POST body cannot be null, expected list of Events");

    String contentType = request.contentType();
    String responseType = request.headers("Accept");
    response.type(responseType);
    response.status(200);

    //Get the events
    Event[] events;
    if (requestIsMessagePack(request)) {
      events = messagePackMapper.readValue(postParams.toString(), Event[].class);
    } else {
      events = objectMapper.readValue(postParams.toString(), Event[].class);
    }

    //Attempt to store the events
    List<Event> eventsList = Arrays.asList(events);
    List<Event> errorEventsList = new ArrayList<>();
    Collection<Event> existingEventsInOsd = eventRepository.findEventsByIds(
        eventsList.stream().map(Event::getId).collect(Collectors.toList()));
    Collection<UUID> existingEventIds = existingEventsInOsd.stream().map(Event::getId).collect(
        Collectors.toList());
    Collection<Event> existingEvents = eventsList.stream()
        .filter(event -> existingEventIds.contains(event.getId()))
        .collect(Collectors.toList());
    Collection<Event> newEvents = eventsList.stream()
        .filter(e -> !existingEventIds.contains(e.getId())).collect(Collectors.toList());
    try {
      eventRepository.storeEvents(newEvents, errorEventsList);
      eventRepository.updateEvents(existingEvents, errorEventsList);
    } catch(DataExistsException e) {
      response.status(409);
      return "Data already exists: " + e;
    } catch (Exception e){
      response.status(400);
      return "Failed to store events: " + e;
    }

    //Now return the stored events' UUIDs
    List<UUID> storedUUIDs = newEvents.stream()
        //TODO: do we need to compare UUIDs?
        .filter(e -> !errorEventsList.stream().map(Event::getId)
            .collect(Collectors.toSet()).contains(e.getId()))
        .map(Event::getId)
        .collect(Collectors.toList());

    List<UUID> updatedUUIDs = existingEventsInOsd.stream()
        //TODO: do we need to compare UUIDs?
        .filter(e -> !errorEventsList.stream().map(Event::getId)
            .collect(Collectors.toSet()).contains(e.getId()))
        .map(Event::getId)
        .collect(Collectors.toList());

    StoreEventResponseDto dto = StoreEventResponseDto.from(
        storedUUIDs,
        updatedUUIDs,
        errorEventsList.stream().map(Event::getId)
            .collect(Collectors.toList()));

    try
    {
      if (shouldReturnMessagePack(request)) {
        return messagePackMapper.writeValueAsBytes(dto);
      }
      else {
        return objectMapper.writeValueAsString(dto);
      }
    } catch (JsonProcessingException e) {
      logger.error("JsonProcessingException:");
      logger.error(e.toString());

      response.status(500);
      return e.toString();
    }
  }

  /**
   * State of health operation to determine if the signaldetection-repository-service is running.
   * Returns a message with the current time in plaintext.
   *
   * @return Response code 200 with a plaintext string containing the current time
   */
  public String alive(
      spark.Request request,
      spark.Response response) {

    response.status(HttpStatus.OK_200);
    return "alive at " + Instant.now()
        .toString();
  }

  /**
   * Determines if the {@link Request} body's content type is MessagePack
   *
   * @param request {@link Request}
   * @return True if the {@link Request} body's content type is message pack
   */
  private static boolean requestIsMessagePack(Request request) {
    return (request.contentType() != null && request.contentType().equals("application/msgpack"));
  }

  /**
   * Determines if the {@link Request} indicates the client accepts message pack
   *
   * @param request Request, not null
   * @return true if the client accepts application/msgpack
   */
  private static boolean shouldReturnMessagePack(Request request) {
    String accept = request.headers("Accept");
    return accept != null && accept.contains("application/msgpack");
  }

  /**
   * Determines if the {@link Request} indicates the client provided a message pack body
   *
   * @param request Request, not null
   * @return true if the client provided application/msgpack
   */
  private static boolean messagePackProvided(Request request) {
    String accept = request.headers("Content-Type");
    return accept != null && accept.contains("application/msgpack");
  }

  private static double readOptionalJsonField(JsonNode object, String name, double defaultValue) {
    if (object.get(name) == null) {
      return defaultValue;
    }
    final JsonNode node = object.get(name);
    Validate.isTrue(jsonNodeIsNumber(node),
        "Expected " + name + " to be number but is " + node.getNodeType());
    return node.asDouble();
  }

  private static boolean jsonNodeIsNumber(JsonNode node) {
    return node != null && node.getNodeType().equals(JsonNodeType.NUMBER);
  }
}
