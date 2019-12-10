package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.ParameterValidation;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.RequestUtil;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 * Contains handler functions for the service routes.
 */
public class QcMaskRouteHandlers {

  private static Logger logger = LoggerFactory.getLogger(QcMaskRouteHandlers.class);

  /**
   * Serializes and deserializes signal detection common objects
   */
  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private static final ObjectMapper messagePackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

  /**
   * Handles a request to retrieve QC Mask via {@link QcMaskRepository}.
   *
   * @param request the request (HTTP), not null
   * @param response the response (HTTP); this can be modified before responding, not null
   * @param qcMaskRepository the interface used, not null
   * @return Collection of QcMask resulting from the query, not null
   * @throws NullPointerException if request, response, or qcMaskRepository are null
   */
  public static String findCurrentByChannelIdAndTimeRange(Request request,
                                                          Response response,
                                                          QcMaskRepository qcMaskRepository)
      throws Exception {
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");
    Objects.requireNonNull(qcMaskRepository, "Cannot accept null qcMaskRepository");

    String channelIdString = request.queryParams("channel-id");
    String startTimeString = request.queryParams("start-time");
    String endTimeString = request.queryParams("end-time");

    logger.info(
        "findCurrentByChannelIdAndTimeRange endpoint hit with parameters: "
            + "channel-id = {}, start-time = {}, end-time = {}",
        channelIdString, startTimeString, endTimeString);

    // Registered ExceptionHandlers return 400 (Bad Request) if the parameters do not parse
    List<QcMask> qcMasks = qcMaskRepository
        .findCurrentByChannelIdAndTimeRange(
            UUID.fromString(channelIdString),
            Instant.parse(startTimeString),
            Instant.parse(endTimeString));

    // Client requested json
    if (RequestUtil.clientAcceptsMsgpack(request)) {

      response.type("application/msgpack");
      // ExceptionHandler will catch the exception and return a 500
      try {
        return messagePackMapper.writeValueAsString(qcMasks);
      } catch (Exception e) {
        logger.debug("Error converting QcMasks to msgpack", e);
        throw new Exception("Could not convert List<QcMask> to msgpack", e);
      }
    } else {
      response.type("application/json");
      // ExceptionHandler will catch the exception and return a 500
      try {
        return objectMapper.writeValueAsString(qcMasks);
      } catch (JsonProcessingException e) {
        logger.debug("Error converting QcMasks to json", e);
        throw new Exception("Could not convert List<QcMask> to json", e);
      }
    }
  }

  /**
   * Handles a request to retrieve a {@link QcMask} via {@link QcMaskRepository}.
   *
   * Returns a body with a map of ChannelId to {@code List<QcMask> }, either JSON or msgpack as per client
   * request header.
   *
   * Returns HTTP status codes: 200 when query returns a map with a list of QcMasks keyed by each
   * ChannelId in the provided array. Note that the returned QcMask list can be empty, signifying
   * that there are no QcMasks for that particular ChannelId.  Besides a ChannelId array, a valid
   * startTime and endTime must be provided. 400 when the parameters are not valid
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param qcMaskRepository the interface used
   * @return return HTTP response with body containing a map of a list of QcMasks, keyed by Channel
   * UUID
   */
  public static Object retrieveQcMasksByChannelIdsAndTimeRange(Request request,
                                                               Response response,
                                                               QcMaskRepository qcMaskRepository)
      throws Exception {
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");
    Objects.requireNonNull(qcMaskRepository, "Cannot accept null qcMaskRepository");

    JsonNode postParams = objectMapper.readTree(request.body());
    ObjectReader uuidListReader = objectMapper.readerFor(new TypeReference<List<UUID>>() {
    });
    Collection<UUID> channelIds = uuidListReader.readValue(postParams.get("channel-ids"));
    Instant startTime = Instant.parse(postParams.get("start-time").asText());
    Instant endTime = Instant.parse(postParams.get("end-time").asText());

    logger.info(
        "retrieveQcMasksByChannelIdsAndTimeRange endpoint hit with parameters: " + "channel-ids = "
            + channelIds + " start-time = " + startTime + "end-time = " + endTime);

    Map<UUID, List<QcMask>> qcMasksByChanId = new HashMap<>();
    for (UUID chanId : channelIds) {
      ParameterValidation.requireFalse(Instant::isAfter, startTime, endTime,
          "Cannot query for invalid time range: start must be less than or equal to end");

      List<QcMask> qcMasks = qcMaskRepository.findCurrentByChannelIdAndTimeRange(
          chanId, startTime, endTime);
      qcMasksByChanId.put(chanId, qcMasks);
    }

    // Client requested msgpack
    if (shouldReturnMessagePack(request)) {
      response.type("application/msgpack");
      return messagePackMapper.writeValueAsBytes(qcMasksByChanId);
    } else {
      return objectMapper.writeValueAsString(qcMasksByChanId);
    }
  }

  /**
   * Handles a request to store a {@link QcMask}, give an array of QcMasks.
   *
   * Returns HTTP status codes: 200 when successful 400 when the parameters are not valid
   *
   * @param req  the request (HTTP)
   * @param resp the response (HTTP); this can be modified before responding
   * @param repo the interface used
   * @return     Object response (either JSON or MessagePack, depending on request)
   */
  public static Object storeQcMasks(Request req,
                                    Response resp,
                                    QcMaskRepository repo)
      throws Exception {
    Objects.requireNonNull(req, "Cannot accept null Request");
    Objects.requireNonNull(resp, "Cannot accept null Response");
    Objects.requireNonNull(repo, "Cannot accept null QcMaskRepository");

    String contentType = req.contentType();
    String responseType = req.headers("Accept");
    resp.type(responseType);
    resp.status(200);

    QcMask[] qcMasks;
    
    if (contentType.equals("application/json")) {
      System.err.println("Deserializing JSON");
      qcMasks = objectMapper.readValue(req.body(), QcMask[].class); 
    } else if (contentType.equals("application/msgpack")) {
      System.err.println("Deserializing MessagePack");
      qcMasks = messagePackMapper.readValue(req.body(), QcMask[].class);
    } else {
      resp.status(406);
      return "Invalid Request";
    }

    logger.info("storeQcMasks endpoint hit with " + qcMasks.length + " masks");

    List<UUID> storedUUIDs = new ArrayList<>();
    for (QcMask mask : qcMasks) {
      repo.store(mask);
      storedUUIDs.add(mask.getId());
    }

    try
    {
      if (responseType.equals("application/json")) {
        return objectMapper.writeValueAsString(storedUUIDs);
      } else if (responseType.equals("application/msgpack")) {
        return messagePackMapper.writeValueAsBytes(storedUUIDs);        
      }

      resp.status(406);
    } catch (JsonProcessingException e) {
      logger.error("JsonProcessingException:");
      logger.error(e.toString());

      resp.status(500);
    }

    return "";
  }

  /**
   * State of health operation to determine if the signaldetection-repository-service is running.
   * Returns a message with the current time in plaintext.
   *
   * @return Response code 200 with a plaintext string containing the current time
   */
  public static String alive(
      spark.Request request,
      spark.Response response) {

    response.status(HttpStatus.OK_200);
    return "alive at " + Instant.now()
        .toString();
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
}
