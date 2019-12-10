package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.RequestUtil;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.ResponseUtil;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.StoreFkChannelSegmentsDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.FkSpectraRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang.Validate;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 * Suite of route handler methods for handling signal detection http requests
 */
public class SignalDetectionRouteHandlers {

  private static Logger logger = LoggerFactory.getLogger(SignalDetectionRouteHandlers.class);

  /**
   * Serializes and deserializes signal detection common objects
   */

  public static final JavaType chanSegFk;
  public static final JavaType listChanSegFk;
  public static final JavaType listUuid;
  public static final ObjectReader listUuidReader;

  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private static final ObjectMapper messagePackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

  static {
    chanSegFk = objectMapper.getTypeFactory()
        .constructParametricType(ChannelSegment.class, FkSpectra.class);
    listChanSegFk = objectMapper.getTypeFactory().constructCollectionType(List.class, chanSegFk);
    listUuid = objectMapper.getTypeFactory().constructCollectionType(List.class, UUID.class);
    listUuidReader = objectMapper.readerFor(new TypeReference<List<UUID>>() {});
  }

  private final SignalDetectionRepository signalDetectionRepository;
  private final FkSpectraRepository fkRepository;

  private SignalDetectionRouteHandlers(
      SignalDetectionRepository signalDetectionRepository,
      FkSpectraRepository fkRepository) {
    this.signalDetectionRepository = signalDetectionRepository;
    this.fkRepository = fkRepository;
  }

  /**
   * Factory method for creating {@link SignalDetectionRouteHandlers}
   *
   * @param signalDetectionRepository Signal Detection Repository class for retrieving {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection}
   * objects from persistence
   * @return The route handlers object using the input repository
   */
  public static SignalDetectionRouteHandlers create(
      SignalDetectionRepository signalDetectionRepository,
      FkSpectraRepository fkRepository) {
    return new SignalDetectionRouteHandlers(Objects.requireNonNull(signalDetectionRepository),
        Objects.requireNonNull(fkRepository));
  }

  /**
   * Base method for retrieving Signal Detections. Handles the majority GET requests via parameters
   * and query strings.
   *
   * @param request The Spark Java Request object containing all request information
   * @param response The Spark Java response object provided to set things like status codes.
   * @return The response body representing the result of a query for Signal Detections. Spark Java
   * sets this into the response body automatically.
   */
  public String getSignalDetection(Request request, Response response) {
    Objects.requireNonNull(request);
    Objects.requireNonNull(response);

    Optional<UUID> id = Optional.ofNullable(request.params(":id")).map(UUID::fromString);

    if (RequestUtil.clientAcceptsJson(request)) {
      //return a single signal detection if provided an id, otherwise return all signal detections
      return ObjectSerialization.writeValue(
          id.isPresent() ? signalDetectionRepository.findSignalDetectionById(id.get())
              : signalDetectionRepository.retrieveAll());
    } else {
      return ResponseUtil.notAcceptable(request, response);
    }

  }

  /**
   * Query for retrieving Signal Detections with list of signal detection IDs via POST.
   *
   * @param request The Spark Java Request object containing all request information
   * @param response The Spark Java response object provided to set things like status codes.
   * @return The response body representing the result of a query for Signal Detections. Spark Java
   * sets this into the response body automatically.
   */
  public Object getSignalDetectionsByIds(Request request, Response response)
      throws Exception {
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");

    final boolean msgpack = requestIsMessagePack(request);
    final JsonNode postParams = msgpack ?
        messagePackMapper.readTree(request.bodyAsBytes()) : objectMapper.readTree(request.body());
    final Collection<UUID> ids = listUuidReader.readValue(postParams.get("ids"));

    logger.info(
        "getSignalDetectionsByIds endpoint hit with parameters: " + "ids = "
            + String.valueOf(ids));

    List<SignalDetection> signalDetsById = signalDetectionRepository
        .findSignalDetectionsByIds(ids);

    // Client requested msgpack
    if (shouldReturnMessagePack(request)) {
      response.type("application/msgpack");
      return messagePackMapper.writeValueAsBytes(signalDetsById);
    } else {
      return objectMapper.writeValueAsString(signalDetsById);
    }
  }

  public Object getSignalDetectionsHypothesesByIds(Request request, Response response) throws Exception {
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");

    final boolean msgpack = requestIsMessagePack(request);
    final JsonNode postParams = msgpack ?
        messagePackMapper.readTree(request.bodyAsBytes()) : objectMapper.readTree(request.body());
    final Collection<UUID> ids = listUuidReader.readValue(postParams.get("ids"));

    logger.info(
        "getSignalDetectionHypothesesByIds endpoint hit with parameters: " + "ids = "
            + String.valueOf(ids));

    final List<SignalDetectionHypothesis> hypotheses = signalDetectionRepository
        .findSignalDetectionHypothesesByIds(ids);

    // Client requested msgpack
    if (shouldReturnMessagePack(request)) {
      response.type("application/msgpack");
      return messagePackMapper.writeValueAsBytes(hypotheses);
    } else {
      return objectMapper.writeValueAsString(hypotheses);
    }
  }

  /**
   * Handles a request to store a {@link SignalDetection}, given an array of SignalDetections
   *
   * Returns HTTP status codes: 200 when successful 400 when the parameters are not valid
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   */
  public Object storeSignalDetections(Request request, Response response) throws Exception {

    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");
    Objects.requireNonNull(request.bodyAsBytes(), "Cannot store null signal detections");

    boolean msgPackProvided = messagePackProvided(request);

    SignalDetection[] signalDetections = (msgPackProvided ? messagePackMapper : objectMapper)
        .readValue(request.bodyAsBytes(), SignalDetection[].class);
    Objects.requireNonNull(signalDetections, "Got null SignalDetection[]");

    logger.info("storeSignalDetections endpoint hit with "
        + signalDetections.length + " objects");

    for (SignalDetection detection : signalDetections) {
      signalDetectionRepository.store(detection);
    }

    List<UUID> signalDetectionIds = Arrays.stream(signalDetections).map(SignalDetection::getId)
        .collect(Collectors.toList());

    response.status(200);
    boolean returnMsgPack = shouldReturnMessagePack(request);
    response.type(String.format("application/%s", returnMsgPack ? "msgpack" : "json"));

    return (returnMsgPack ? messagePackMapper : objectMapper)
        .writeValueAsBytes(signalDetectionIds);
  }

  /**
   * Handles a request to store a {@link BeamCreationInfo}, given an array of BeamCreationInfos
   *
   * Returns HTTP status codes: 200 when successful 400 when the parameters are not valid
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   */
  public String storeBeamCreationInfoFromArray(Request request, Response response)
      throws Exception {

    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");

    Validate.notNull(request.body(), "Cannot store null beam creation infos");
    BeamCreationInfo[] beamCreationInfos = objectMapper
        .readValue(request.body(), BeamCreationInfo[].class);
    Objects.requireNonNull(beamCreationInfos, "Got null BeamCreationInfo[]");

    logger.info(
        "storeBeamCreationInfoFromArray endpoint hit with "
            + beamCreationInfos.length + " objects");

    for (BeamCreationInfo beamCreationInfo : beamCreationInfos) {
      signalDetectionRepository.store(beamCreationInfo);
    }
    return "";
  }

  /**
   * Handles a request to retrieve {@link SignalDetection}'s, given a time range.
   *
   * Returns HTTP status codes: 200 when successful 400 when the parameters are not valid
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   */
  public Object getSignalDetectionsByTimeRange(Request request, Response response)
      throws Exception {
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");

    Instant startTime = Instant.parse(request.queryParams("start-time"));
    Instant endTime = Instant.parse(request.queryParams("end-time"));

    List<SignalDetection> dets = signalDetectionRepository.findSignalDetections(startTime, endTime);

    // Client requested msgpack
    if (shouldReturnMessagePack(request)) {
      response.type("application/msgpack");
      return messagePackMapper.writeValueAsBytes(dets);
    } else {
      return objectMapper.writeValueAsString(dets);
    }
  }

  /**
   * Handles a request to retrieve {@link SignalDetection}'s, given an array of StationIDs and time
   * range.
   *
   * Returns HTTP status codes: 200 when successful 400 when the parameters are not valid
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   */
  public Object getSignalDetectionsByStationAndTimeRange(Request request, Response response)
      throws Exception {
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");

    JsonNode postParams;
    ObjectReader uuidListReader;
    if (requestIsMessagePack(request)) {
      postParams = messagePackMapper.readTree(request.bodyAsBytes());
      uuidListReader = messagePackMapper.readerFor(new TypeReference<List<UUID>>() {});
    } else {
      postParams = objectMapper.readTree(request.body());
      uuidListReader = objectMapper.readerFor(new TypeReference<List<UUID>>() {});
    }

    Collection<UUID> stationIds = uuidListReader.readValue(postParams.get("stationIds"));
    Instant startTime = Instant.parse(postParams.get("startTime").asText());
    Instant endTime = Instant.parse(postParams.get("endTime").asText());

    logger.info(
        "getSignalDetectionsByStationAndTimeRange endpoint hit with parameters: " + "stationIds = "
            + String.valueOf(stationIds) + " startTime = " + startTime + "endTime = "
            + endTime);

    Map<UUID, List<SignalDetection>> signalDetsByStaId = signalDetectionRepository
        .findSignalDetectionsByStationIds(
            stationIds, startTime, endTime);

    // Client requested msgpack
    if (shouldReturnMessagePack(request)) {
      response.type("application/msgpack");
      return messagePackMapper.writeValueAsBytes(signalDetsByStaId);
    } else {
      return objectMapper.writeValueAsString(signalDetsByStaId);
    }
  }

  /**
   * Handles a request to retrieve a {@link List} of {@link ChannelSegment} of {@link
   * FkSpectra} given a channel UUID, startTime, and endTime. Will return an empty list if no
   * {@link ChannelSegment}s are found.
   *
   * Returns HTTP status codes: 200 when successful 400 when the parameters are not valid
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   */
  public Object retrieveFkChannelSegments(Request request, Response response) throws Exception {
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");

    JsonNode postParams;
    if (requestIsMessagePack(request)) {
      postParams = messagePackMapper.readTree(request.bodyAsBytes());
    } else {
      postParams = objectMapper.readTree(request.body());
    }

    Validate.notNull(postParams.get("channelIds"),
        "retrieveFkChannelSegments(): channelIds parameter in POST body cannot be null");
    Validate.notNull(postParams.get("startTime"),
        "retrieveFkChannelSegments(): startTime parameter in POST body cannot be null");
    Validate.notNull(postParams.get("endTime"),
        "retrieveFkChannelSegments(): endTime parameter in POST body cannot be null");

    List<UUID> channelIds;
    if (requestIsMessagePack(request)) {
      channelIds = messagePackMapper.readValue(postParams.get("channelIds").toString(), listUuid);
    } else {
      channelIds = objectMapper.readValue(postParams.get("channelIds").toString(), listUuid);
    }

    Instant startTime = Instant.parse(postParams.get("startTime").textValue());
    Instant endTime = Instant.parse(postParams.get("endTime").textValue());

    if (endTime.isBefore(startTime)) {
      throw new IllegalArgumentException("startTime is before endTime");
    }

    List<ChannelSegment<FkSpectra>> fkChannelSegments = new ArrayList<>();
    for (UUID channelId : channelIds) {
      Optional<ChannelSegment<FkSpectra>> fk = fkRepository
          .retrieveFkSpectraByTime(channelId, startTime, endTime, true);

      if (fk.isPresent()) {
        fkChannelSegments.add(fk.get());
      }
    }

    if (shouldReturnMessagePack(request)) {
      response.type("application/msgpack");
      return messagePackMapper.writeValueAsBytes(fkChannelSegments);
    } else {
      return objectMapper.writeValueAsString(fkChannelSegments);
    }
  }

  /**
   * Handles a request to retrieve a {@link ChannelSegment} of {@link FkSpectra} given a {@link
   * ChannelSegment} {@link UUID}.
   *
   * Returns HTTP status codes... - 200: {@link ChannelSegment} is found - 404: {@link
   * ChannelSegment} is not found - 400: parameters are not valid
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   */
  public Object retrieveFkChannelSegmentsBySegmentIds(Request request, Response response)
      throws Exception {
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");

    JsonNode postParams;
    if (requestIsMessagePack(request)) {
      postParams = messagePackMapper.readTree(request.bodyAsBytes());
    } else {
      postParams = objectMapper.readTree(request.body());
    }

    Validate.notNull(postParams.get("channelSegmentIds"),
        "retrieveFkChannelSegments(): channelSegmentId parameter in POST body cannot be null");
    Validate.notNull(postParams.get("withFkSpectra"),
        "retrieveFkChannelSegments(): withFkSpectra parameter in POST body cannot be null");

    List<UUID> channelSegmentIds;
    boolean withFkSpectra;
    if (requestIsMessagePack(request)) {
      channelSegmentIds = messagePackMapper
          .readValue(postParams.get("channelSegmentIds").toString(), listUuid);
      withFkSpectra = messagePackMapper
          .readValue(postParams.get("withFkSpectra").toString(), Boolean.class);
    } else {
      channelSegmentIds = objectMapper
          .readValue(postParams.get("channelSegmentIds").toString(), listUuid);
      withFkSpectra = objectMapper
          .readValue(postParams.get("withFkSpectra").toString(), Boolean.class);
    }

    List<ChannelSegment<FkSpectra>> fkChannelSegments = new ArrayList<>();

    for (UUID id : channelSegmentIds) {
      Optional<ChannelSegment<FkSpectra>> fkChannelSegmentOptional = fkRepository
          .retrieveFkChannelSegment(id, withFkSpectra);
      if (fkChannelSegmentOptional.isPresent()) {
        ChannelSegment<FkSpectra> fkChannelSegment = fkChannelSegmentOptional.get();
        if (withFkSpectra) {
          fkChannelSegments.add(fkChannelSegmentOptional.get());
        } else {
          // We have to make a new channel segment manually without any FkSpectrums
          Collection<FkSpectra> timeseries = new HashSet<>();
          fkChannelSegment.getTimeseries().forEach(fk ->
              timeseries.add(fk.toBuilder().withoutValues(fk.getSampleCount()).build()));

          fkChannelSegments.add(
              ChannelSegment.from(
                  fkChannelSegment.getId(),
                  fkChannelSegment.getChannelId(),
                  fkChannelSegment.getName(),
                  fkChannelSegment.getType(),
                  timeseries,
                  fkChannelSegment.getCreationInfo()
              )
          );
        }
      }
    }

    if (shouldReturnMessagePack(request)) {
      response.type("application/msgpack");
      return messagePackMapper.writeValueAsBytes(fkChannelSegments);
    } else {
      return objectMapper.writeValueAsString(fkChannelSegments);
    }
  }

  /**
   * Handles a request to store a pair of {@link ChannelSegment}s of {@link FkSpectra}s
   * Returns 200 if the store was successful
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP)
   */
  public Object storeFkSpectra(Request request, Response response) throws Exception {
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");

    StoreFkChannelSegmentsDto fkChannelSegmentsDto;
    if (requestIsMessagePack(request)) {
      fkChannelSegmentsDto = ObjectSerialization
          .readMessagePack(request.bodyAsBytes(), StoreFkChannelSegmentsDto.class);
    } else {
      fkChannelSegmentsDto = ObjectSerialization
          .readValue(request.body(), StoreFkChannelSegmentsDto.class);
    }

    Set<UUID> fkChannelSegmentIds = fkChannelSegmentsDto.getChannelSegments().stream()
        .map(ChannelSegment::getId).collect(Collectors.toSet());

    if (fkRepository.fkChannelSegmentRecordsExist(fkChannelSegmentsDto.getChannelSegments())) {
      response.type("text/plain");
      response.status(HttpStatus.CONFLICT_409);
      return "One or more of the ChannelSegment<FkSpectra> ids already exist in the database";
    }

    for (int i = 0; i < fkChannelSegmentsDto.getChannelSegments().size(); i++) {
      fkRepository.storeFkSpectra(fkChannelSegmentsDto.getChannelSegments().get(i)
      );
    }

    List<UUID> ids = fkChannelSegmentsDto.getChannelSegments().stream().map(c -> c.getId())
        .collect(Collectors.toList());

    response.status(HttpStatus.OK_200);
    if (shouldReturnMessagePack(request)) {
      response.type("application/msgpack");
      return messagePackMapper.writeValueAsBytes(ids);
    } else {
      return objectMapper.writeValueAsString(ids);
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

}
