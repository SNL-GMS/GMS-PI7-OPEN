package gms.core.eventlocation.control;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.eventlocation.control.service.RequestHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * HTTP client library used by {@link EventLocationControl} to retrieve and store {@link
 * gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event}s through the
 * associated COI service.
 *
 * Uses JSON for transmitting/receiving data.
 */
public class EventLocationControlOsdGateway {

  private final Logger logger = LoggerFactory.getLogger(RequestHandlers.class);

  // Validated base URL string for event operations, including protocol, host name,
  //   and base path of event coi operations.
  private final String eventBaseUrl;

  // Validated base URL string for event operations, including protocol, host name,
  //   and base path of signal detection coi operations.
  private final String signalDetectionBaseUrl;

  // Repository interface used for retrieving ReferenceStations
  private final StationReferenceRepositoryInterface stationReferenceRepository;

  private final ObjectMapper jsonObjectMapper;

  // Returns a new EventLocationControlOsdGateway given the URL of the signal
  //   detection repository service.  Though we only need the URL string for Unirest calls and not
  //   an actual URL object, using a URL object ensures the URL string is valid.
  private EventLocationControlOsdGateway(URL baseUrl,
      StationReferenceRepositoryInterface stationReferenceRepository) {

    this.jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    this.eventBaseUrl = baseUrl.toString() + "/coi/events";
    this.signalDetectionBaseUrl = baseUrl.toString() + "/coi/signal-detections";

    this.stationReferenceRepository = stationReferenceRepository;
  }


  /**
   * Creates and returns a new {@link EventLocationControlOsdGateway}
   *
   * @param host The hostname of the signal detection repository service to be accessed by this
   * {@link EventLocationControlOsdGateway}.  Not Null.
   * @param port The port of the signal detection repository service to connect to. Not null.
   * @return A new {@link EventLocationControlOsdGateway}.  Not null.
   */
  public static EventLocationControlOsdGateway create(String host, Integer port,
      StationReferenceRepositoryInterface stationReferenceRepository)
      throws MalformedURLException {
    Objects.requireNonNull(host,
        "EventLocationControlOsdGateway::create() requires non-null \"host\" paramter");
    Objects.requireNonNull(port,
        "EventLocationControlOsdGateway::create() requires non-null \"port\" parameter");

    URL baseUrl = new URL("http://" + host + ":" + port);

    return new EventLocationControlOsdGateway(baseUrl, stationReferenceRepository);
  }


  /**
   * Given a {@link Collection} of {@link Event} {@link UUID}s, retrieve the associated {@link
   * Event}s via the signal detection repository service.
   *
   * @param ids {@link Collection} of {@link UUID}s to retrieve {@link Event}s for
   * @return {@link Collection} of retrieved {@link Event}s
   */
  public Set<Event> retrieveEvents(Collection<UUID> ids)
      throws UnirestException, IOException {

    HttpResponse<String> response = Unirest
        .post(this.eventBaseUrl + "/query/ids")
        .body(this.jsonObjectMapper.writeValueAsString(ids))
        .asString();

    // Handle HTTP responses that indicate some sort of failure
    if (response.getStatus() != 200) {
      throw new IllegalStateException(response.getBody());
    }

    TypeReference<Set<Event>> collectionOfEvents = new TypeReference<>() {
    };

    return this.jsonObjectMapper.readValue(response.getBody(), collectionOfEvents);
  }


  /**
   * Given a {@link List} of {@link Event}s, stores the {@link Event}s via the signal detection
   * repository service and returns a {@link List} of the stored {@link Event}s.
   *
   * @param events {@link List} of {@link Event}s to store. Not null.
   * @return {@link List} of {@link Event}s that were successfully stored or updated.  Not null.
   */
  public Collection<UUID> storeOrUpdateEvents(Collection<Event> events)
      throws UnirestException, IOException {

    Objects.requireNonNull(events,
        "SignalDetectionAssociationOsdGateway::storeEvents() requires non-null \"events\" parameter");

    // Execute HTTP request to store events
    HttpResponse<String> response = Unirest
        .post(this.eventBaseUrl)
        .body(this.jsonObjectMapper.writeValueAsString(events))
        .asString();

    // Handle HTTP responses that indicate some sort of failure
    int status = response.getStatus();
    if (status != 200) {
      if (status == 409) {
        throw new IllegalArgumentException(
            "One or more of the provided Event objects already exists in the database, and the repository tried to store them as new: "
                + events);
      } else if (status == 400) {
        throw new IllegalStateException("Failed to store events: " + events);
      } else if (status == 500) {
        // The server returns 500 if serializing the response fails
        throw new IllegalStateException("Server could not provide a response");
      }
    }

    // Deserialize response body into JsonNode
    JsonNode responseJsonNode = this.parseJson(response.getBody());

    // Create type reference so that we can deserialize the lists of UUIDs
    TypeReference<List<UUID>> listOfUuids = new TypeReference<>() {
    };

    // Deserialize the succesfully stored events' UUIDs
    List<UUID> storedEventIds = this.parseJsonField("storedEvents", responseJsonNode, listOfUuids);

    // Deserialize the successfully updated events' UUIDs
    List<UUID> updatedEventIds = this
        .parseJsonField("updatedEvents", responseJsonNode, listOfUuids);

    // Concatenate the stored and updated events' UUIDs to return
    return Stream.concat(
        storedEventIds.stream(),
        updatedEventIds.stream()
    ).collect(Collectors.toList());
  }


  /**
   * Given a list of {@link SignalDetection} {@link UUID}s, retrieves a list of the associated
   * {@link SignalDetection} from the signal detection repository service.
   *
   * @param signalDetectionIds List of {@link UUID}s of {@link SignalDetection} to retrieve from the
   * signal detection repository service. Not null.
   * @return List of {@link SignalDetection} retrieved from the signal detection repository service.
   * Not null.
   */
  List<SignalDetection> retrieveSignalDetections(
      List<UUID> signalDetectionIds) throws UnirestException, IOException {

    Objects.requireNonNull(signalDetectionIds,
        "EventLocationGeigersOsdGateway::retrieveSignalDetections requires non-null \"signalDetectionIds\" parameter");

    ObjectNode requestBodyJson = this.jsonObjectMapper.createObjectNode()
        .putPOJO("ids", this.jsonObjectMapper.writeValueAsString(signalDetectionIds));

    // Execute HTTP request to retrieve signal detection hypotheses
    HttpResponse<String> response = Unirest
        .post(this.signalDetectionBaseUrl + "/query/ids")
        .body(requestBodyJson.toString())
        .asString();

    // Deserialize JSON body into List<SignalDetection>
    TypeReference listOfSignalDetection = new TypeReference<List<SignalDetection>>() {
    };

    return this.jsonObjectMapper
        .readValue(response.getBody(), listOfSignalDetection);
  }


  /**
   * Given a list of {@link SignalDetectionHypothesis} {@link UUID}s, retrieves a list of the
   * associated {@link SignalDetectionHypothesis} from the signal detection repository service.
   *
   * @param signalDetectionHypothesisIds List of {@link UUID}s of {@link SignalDetectionHypothesis}
   * to retrieve from the signal detection repository service. Not null.
   * @return List of {@link SignalDetectionHypothesis} retrieved from the signal detection
   * repository service.  Not null.
   */
  List<SignalDetectionHypothesis> retrieveSignalDetectionHypotheses(
      List<UUID> signalDetectionHypothesisIds) throws UnirestException, IOException {

    Objects.requireNonNull(signalDetectionHypothesisIds,
        "SignalDetectionAssociationControlOsdGateway::retrieveSignalDetectionHypotheses requires non-null \"signalDetectionHypothesisIds\" parameter");

    ObjectNode requestBodyJson = this.jsonObjectMapper.createObjectNode()
        .putPOJO("ids", this.jsonObjectMapper.writeValueAsString(signalDetectionHypothesisIds));

    // Execute HTTP request to retrieve signal detection hypotheses
    HttpResponse<String> response = Unirest
        .post(this.signalDetectionBaseUrl + "/hypotheses/query/ids")
        .body(requestBodyJson.toString())
        .asString();

    // Deserialize JSON body into List<SignalDetectionHypothesis>
    TypeReference listOfSignalDetectionHypothesis = new TypeReference<List<SignalDetectionHypothesis>>() {
    };

    return this.jsonObjectMapper
        .readValue(response.getBody(), listOfSignalDetectionHypothesis);
  }


  /**
   * Given a {@link List} of {@link ReferenceStation} {@link UUID}s, retrieves a {@link List} of the
   * associated {@link ReferenceStation}s from the database
   *
   * @param stationId {@link ReferenceStation} {@link UUID} to retrieve from the database.  Not
   * null.
   * @return {@link ReferenceStation} with the provided {@link UUID}s
   */
  Optional<ReferenceStation> retrieveStation(UUID stationId) {

    Objects.requireNonNull(stationId, "Null stationIds");

    ReferenceStation station = null;
    try {
      List<ReferenceStation> stations = this.stationReferenceRepository
          .retrieveStationsByVersionIds(List.of(stationId));

      if (stations.size() > 1) {
        throw new IllegalStateException("Retrieved multiple stations with multiple entity ids");
      }

      station = stations.get(0);
    } catch (Exception e) {
      this.logger
          .error("Could not retrieve station with id - operation threw a generic exception: {}\n{}",
              stationId, e.getMessage());
    }

    if (Objects.isNull(station)) {
      return Optional.empty();
    } else {
      return Optional.of(station);
    }
  }


  // Utility method for deserializing a Json String into a JsonNode object
  private JsonNode parseJson(String jsonString) throws IOException {

    Objects.requireNonNull(jsonString, "Cannot deserialize null String into JsonNode");

    return Objects.requireNonNull(this.jsonObjectMapper.readTree(jsonString),
        "Provided json string deserialized into null JsonNode");
  }


  // Utility method for deserializing JsonField into an instance of the provided TypeReference
  //
  // Throws IllegalStateException if the field does not exist
  // Throws IllegalArgumentException if the field cannot be deserialized into the requested object
  private <T> T parseJsonField(String fieldName, JsonNode jsonNode, TypeReference<T> classType) {

    T object;
    JsonNode objectJson = jsonNode.get(fieldName);

    if (Objects.isNull(objectJson)) {
      String errorMsg = "JSON field \"" + fieldName + "\" does not exist in the request body";
      throw new IllegalStateException(errorMsg);
    }

    try {
      object = this.jsonObjectMapper.readValue(objectJson.toString(), classType);
    } catch (IOException e) {
      String errorMsg =
          "Could not deserialize JSON field \"" + fieldName + "\" into " + classType.getType();
      throw new IllegalArgumentException(errorMsg);
    }

    return object;
  }
}
