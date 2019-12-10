package gms.core.signaldetection.association.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * HTTP client library used by {@link SignalDetectionAssociationControl} to retrieve {@link
 * SignalDetectionHypothesis} and store {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event}
 * through their respective COI services.
 *
 * Uses JSON format for transmitting/receiving data.
 */
public class SignalDetectionAssociationControlOsdGateway {

  static Logger logger = LoggerFactory.getLogger(SignalDetectionAssociationControlOsdGateway.class);

  // Validated base URL string for signal detection operations, including protocol, host name,
  //   and base path of signal detection coi operations.
  private final String signalDetectionBaseUrl;

  // Validated base URL string for event operations, including protocol, host name,
  //   and base path of event coi operations.
  private final String eventBaseUrl;

  // Validated base URL string for station operations, including protocol, host name,
  //   and base path of event coi operations.
  private final String referenceStationBaseUrl;

  // Repository interface used for retrieving ReferenceStations
  private final StationReferenceRepositoryInterface stationReferenceRepository;

  private final ObjectMapper jsonObjectMapper;

  // Returns a new SignalDetectionAssociationControlOsdGateway given the URL of the signal
  //   detection repository service.  Though we only need the URL string for Unirest calls and not
  //   an actual URL object, using a URL object ensures the URL string is valid.
  private SignalDetectionAssociationControlOsdGateway(URL baseUrl,
      StationReferenceRepositoryInterface stationReferenceRepository) {

    this.jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    this.signalDetectionBaseUrl = baseUrl.toString() + "/coi/signal-detections";
    this.eventBaseUrl = baseUrl.toString() + "/coi/events";
    this.referenceStationBaseUrl = baseUrl.toString() + "/coi/stations";
    this.stationReferenceRepository = stationReferenceRepository;
  }

  /**
   * Creates and returns a new {@link SignalDetectionAssociationControlOsdGateway}
   *
   * @param host The hostname of the signal detection repository service to be accessed by this
   * {@link SignalDetectionAssociationControlOsdGateway}.  Not Null.
   * @param port The port of the signal detection repository service to connect to. Not null.
   * @return A new {@link SignalDetectionAssociationControlOsdGateway}.  Not null.
   */
  public static SignalDetectionAssociationControlOsdGateway create(String host, Integer port,
      StationReferenceRepositoryInterface stationReferenceRepository)
      throws MalformedURLException {
    Objects.requireNonNull(host,
        "SignalDetectionAssociationControlOsdGateway::create() requires non-null \"host\" paramter");
    Objects.requireNonNull(port,
        "SignalDetectionAssociationControlOsdGateway::create() requires non-null \"port\" parameter");

    URL baseUrl = new URL("http://" + host + ":" + port);

    return new SignalDetectionAssociationControlOsdGateway(baseUrl, stationReferenceRepository);
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
  public List<SignalDetectionHypothesis> retrieveSignalDetectionHypotheses(
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
   * Given a {@link List} of {@link Event}s, stores the {@link Event}s via the signal detection
   * repository service and returns a {@link List} of the stored {@link Event}s.
   *
   * @param events {@link List} of {@link Event}s to store. Not null.
   * @return {@link List} of {@link Event}s that were successfully stored or updated.  Not null.
   */
  public List<UUID> storeOrUpdateEvents(List<Event> events) throws UnirestException, IOException {

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
    JsonNode responseJsonNode = this.jsonObjectMapper.readTree(response.getBody());

    // Create type reference so that we can deserialize the lists of UUIDs
    TypeReference listOfUuids = new TypeReference<List<UUID>>() {
    };

    // Deserialize the succesfully stored events' UUIDs
    List<UUID> storedEventIds;
    try {
      String storedEventIdsJson = responseJsonNode.get("storedEvents").toString();
      storedEventIds = this.jsonObjectMapper.readValue(storedEventIdsJson, listOfUuids);
    } catch (NullPointerException e) {
      throw new NullPointerException(
          "JSON field \"storedEvents\" does not exist in the response body.");
    } catch (IOException e) {
      throw new IOException(
          "Coult not deserialize JSON field \"storedEvents\" into List<UUID>:\n" + responseJsonNode
              .get("storedEvents").textValue());
    }

    // Deserialize the successfully updated events' UUIDs
    List<UUID> updatedEventIds;
    try {
      String updatedEventIdsJson = responseJsonNode.get("updatedEvents").toString();
      updatedEventIds = this.jsonObjectMapper.readValue(updatedEventIdsJson, listOfUuids);
    } catch (NullPointerException e) {
      throw new NullPointerException(
          "JSON field \"updatedEvents\" does not exist in the response body.");
    } catch (IOException e) {
      throw new IOException(
          "Coult not deserialize JSON field \"updatedEvents\" into List<UUID>:\n" + responseJsonNode
              .get("updatedEvents").textValue());
    }

    // Concatenate the stored and updated events' UUIDs to return
    return Stream.concat(
        storedEventIds.stream(),
        updatedEventIds.stream()
    ).collect(Collectors.toList());
  }

  /**
   * Retrieve list of station object references given a list of UUIDs.
   * @param stationIds List of UUIDs representing
   * @return
   */
  public Set<ReferenceStation> retrieveStations(List<UUID> stationIds) throws Exception {
    Objects.requireNonNull(stationIds, "stationIds must be non-null");
    return this.stationReferenceRepository.retrieveStations()
        .stream()
        .filter((station) -> stationIds.contains(station.getVersionId()))
        .collect(Collectors.toSet());
  }

  public List<EventHypothesis> retrieveEventHypotheses(Instant startTime, Instant endTime) throws UnirestException, IOException {
    Objects.requireNonNull(startTime, "startTime must be a non-null argument");
    Objects.requireNonNull(endTime, "endTime must be a non-null argument");
    Map<String, Object> requestBody = Map.of(
        "startTime", startTime,
        "endTime", endTime
    );

    String url = this.eventBaseUrl + "/query/time-lat-lon";
    HttpResponse<String> response = Unirest.post(url)
        .body(this.jsonObjectMapper.writeValueAsString(requestBody))
        .asString();

    int status = response.getStatus();
    if(status != 200) {
      if(status == 400) {
        throw new IllegalStateException("Failed to retrieve stations: " + requestBody);
      }
    }

    TypeReference listOfEvents = new TypeReference<List<Event>>() {};
    List<Event> events = this.jsonObjectMapper.readValue(response.getBody(), listOfEvents);
    return events.stream().map((obj) -> obj.getHypotheses())
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  public Optional<Set<ReferenceStation>> retrieveStations() {
    try {
      return Optional.of(this.stationReferenceRepository.retrieveStations().stream().collect(
          Collectors.toSet()));
    } catch (Exception e) {
      logger.error("Station Retrieval failed", e);
      return Optional.empty();
    }
  }
}
