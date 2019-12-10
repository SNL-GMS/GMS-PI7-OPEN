package gms.shared.mechanisms.objectstoragedistribution.coi.channelsegments.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.channelsegments.repository.ChannelSegmentsRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import gms.shared.utilities.service.ContentType;
import gms.shared.utilities.service.Request;
import gms.shared.utilities.service.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandlers {

  private final Logger logger = LoggerFactory.getLogger(RequestHandlers.class);

  private final TypeReference<List<UUID>> listOfUuidTypeReference = new TypeReference<>() {
  };

  private final ChannelSegmentsRepository channelSegmentsRepository;

  static RequestHandlers create(ChannelSegmentsRepository channelSegmentsRepository) {
    return new RequestHandlers(channelSegmentsRepository);
  }

  private RequestHandlers(ChannelSegmentsRepository channelSegmentsRepository) {
    this.channelSegmentsRepository = channelSegmentsRepository;
  }


  /**
   * Handles the /is-alive endpoint.
   *
   * Used to determine if the service is running.  Returns a 200 response with the current system
   * time.
   *
   * @param request {@link Request} object representing the HTTP request.
   * @param deserializer {@link ObjectMapper} to use for deserializing request body contents.
   * @return {@link Response}, representing the HTTP response.  Not Null.
   */
  public Response<String> isAlive(Request request, ObjectMapper deserializer) {

    return Response.success(Long.toString(System.currentTimeMillis()));
  }


  /**
   * Handles the /coi/channel-segments/query/segment-ids endpoint.
   *
   * Retrieve ChannelSegment<*> COI objects for the provided Channel Segment IDs, optionally with
   * underlying Timeseries data (e.g. Waveform, Fk Power Spectra) included based on the parameters
   * in the request body.
   *
   * @param request {@link Request} object representing the HTTP request.
   * @param deserializer {@link ObjectMapper} to use for deserializing request body contents.
   * @return {@link Response}, representing the HTTP response.  Not Null.
   */
  public Response<Collection<ChannelSegment<? extends Timeseries>>> retrieveBySegmentIds(
      Request request,
      ObjectMapper deserializer) {

    logger.info("Received request to retrieve channel segments by channel segment ids");

    final String IDS = "ids";
    final String WITH_TIMESERIES = "withTimeseries";

    // Deserialize request body into JsonNode
    JsonNode requestBodyJson;
    try {
      requestBodyJson = this.parseRequestBody(request, deserializer);
    } catch (IllegalArgumentException | IllegalStateException e) {
      logger.error(e.getMessage());
      return Response.clientError(e.getMessage());
    }

    // Deserialize UUIDs from request body JsonNode
    List<UUID> channelSegmentIds;
    try {
      channelSegmentIds = this
          .parseJsonField(IDS, requestBodyJson, this.listOfUuidTypeReference, deserializer);
    } catch (IllegalArgumentException | IllegalStateException e) {
      logger.error(e.getMessage());
      return Response.clientError(e.getMessage());
    }

    // Deserialize optional parameter withTimeSeries from request body JsonNode
    Boolean withTimeseries = Boolean.TRUE;
    try {

      withTimeseries = this
          .parseJsonField(WITH_TIMESERIES, requestBodyJson, Boolean.class, deserializer);
    } catch (IllegalArgumentException e) {

      logger.error(e.getMessage());
      return Response.clientError(e.getMessage());
    } catch (IllegalStateException e) {

      // Don't return client error, this parameter is optional
      logger.info(e.getMessage());
    }

    // Call ChannelSegmentRepository to retrieve ChannelSegments
    logger.info(
        "Retrieving channel segments " + channelSegmentIds + " with timeseries \"" + withTimeseries
            + "\"");

    Collection<ChannelSegment<? extends Timeseries>> channelSegments;
    try {
      channelSegments = this.channelSegmentsRepository
          .retrieveChannelSegmentsByIds(channelSegmentIds, withTimeseries);
    } catch (Exception e) {
      logger.error(e.getMessage());
      return Response.serverError(e.getMessage());
    }

    return Response.success(channelSegments);
  }


  /**
   * Handles the /coi/channel-segments/query/channel-ids endpoint.
   *
   * Retrieve the ChannelSegment<*> COI objects for the provided channel IDs between the provided
   * start and end times.
   *
   * @param request {@link Request} object representing the HTTP request.
   * @param deserializer {@link ObjectMapper} to use for deserializing request body contents.
   * @return {@link Response}, representing the HTTP response.  Not Null.
   */
  public Response<Collection<ChannelSegment<? extends Timeseries>>> retrieveByChannelIdsAndTime(
      Request request,
      ObjectMapper deserializer) {

    logger.info(
        "Received request to retrieve channel segments by channel ids, start time, and end time");

    final String CHANNEL_IDS = "channelIds";
    final String START_TIME = "startTime";
    final String END_TIME = "endTime";

    // Deserialize request body into JsonNode
    JsonNode requestBodyJson;
    try {
      requestBodyJson = this.parseRequestBody(request, deserializer);
    } catch (IllegalArgumentException | IllegalStateException e) {
      logger.error(e.getMessage());
      return Response.clientError(e.getMessage());
    }

    // Deserialize request body parameters
    List<UUID> channelIds;
    Instant startTime;
    Instant endTime;

    try {
      // Deserialize Channel UUIDs from request body JsonNode
      channelIds = this
          .parseJsonField(CHANNEL_IDS, requestBodyJson, this.listOfUuidTypeReference, deserializer);

      // Deserialize start time from request body JsonNode
      startTime = this.parseJsonField(START_TIME, requestBodyJson, Instant.class, deserializer);

      // Deserialize end time from request body JsonNode
      endTime = this.parseJsonField(END_TIME, requestBodyJson, Instant.class, deserializer);
    } catch (IllegalStateException | IllegalArgumentException e) {

      logger.error(e.getMessage());
      return Response.clientError(e.getMessage());
    }

    if(startTime.isAfter(endTime)) {
      String errormsg = "\"startTime\" parameter cannot be after \"endTime\" parameter";
      logger.error(errormsg);
      return Response.clientError(errormsg);
    }

    // Call ChannelSegmentRepository to retrieve ChannelSegments
    logger.info("Retrieving channel segments from channels " + channelIds
        + " between start time \"" + startTime + "\" and end time \"" + endTime + "\"");

    Collection<ChannelSegment<? extends Timeseries>> channelSegments;

    try {
      channelSegments = this.channelSegmentsRepository
          .retrieveChannelSegmentsByChannelIds(channelIds, startTime, endTime);
    } catch (Exception e) {
      logger.error(e.getMessage());
      return Response.serverError(e.getMessage());
    }

    return Response.success(channelSegments);
  }


  // Utility method for deserializing the request body into a JsonNode object
  //
  // Throws IllegalStateException if the request body is null or empty
  // Throws IllegalArgumentException if the request body cannot be deserialized into a JsonNode
  private JsonNode parseRequestBody(Request request, ObjectMapper deserializer) {

    // Deserialize request body into JsonNode
    JsonNode requestBodyJson;
    try {

      if (request.getContentType().isPresent() && request.getContentType().get()
          .equals(ContentType.APPLICATION_MSGPACK)) {

        // If content type header is present and it is MessagePack, deserialize request body via MessagePack

        byte[] requestBody = request.getRawBody();

        if (Objects.isNull(requestBody)) {
          String errorMsg = "Could not deserialize request body into JsonNode: request body is null";
          throw new IllegalArgumentException(errorMsg);
        }

        requestBodyJson = deserializer.readTree(request.getRawBody());
      } else {

        // If content type header is not present and it is JSON, deserialize request body via JSON

        String requestBody = request.getBody();
        if (Objects.isNull(requestBody)) {
          String errorMsg = "Could not deserialize request body into JsonNode: request body is null";
          throw new IllegalStateException(errorMsg);
        }

        requestBodyJson = deserializer.readTree(request.getBody());
      }
    } catch (IOException e) {
      String errorMsg = "Could not deserialize request body into JsonNode: malformed request body";
      throw new IllegalArgumentException(errorMsg);
    }

    // Check if request body JsonNode is null
    if (Objects.isNull(requestBodyJson)) {
      String errorMsg = "Could not deserialize request body into JsonNode: request body is empty";
      throw new IllegalStateException(errorMsg);
    }

    return requestBodyJson;
  }


  // Utility method for deserializing Json field into an instance of the provided class
  //
  // Throws IllegalStateException if the field does not exist
  // Throws IllegalArgumentException if the field cannot be deserialized into the requested object
  private <T> T parseJsonField(String fieldName, JsonNode requestBodyJsonNode,
      Class<T> classType, ObjectMapper deserializer) {

    T object;
    JsonNode objectJson = requestBodyJsonNode.get(fieldName);

    if (Objects.isNull(objectJson)) {
      String errorMsg = "JSON field \"" + fieldName + "\" does not exist in the request body";
      throw new IllegalStateException(errorMsg);
    }

    try {
      object = deserializer.readValue(objectJson.toString(), classType);
    } catch (IOException e) {
      String errorMsg =
          "Could not deserialize JSON field \"" + fieldName + "\" into " + classType;
      throw new IllegalArgumentException(errorMsg);
    }

    return object;
  }


  // Utility method for deserializing JsonField into an instance of the provided TypeReference
  //
  // Throws IllegalStateException if the field does not exist
  // Throws IllegalArgumentException if the field cannot be deserialized into the requested object
  private <T> T parseJsonField(String fieldName, JsonNode requestBodyJsonNode,
      TypeReference<T> classType, ObjectMapper deserializer) {

    T object;
    JsonNode objectJson = requestBodyJsonNode.get(fieldName);

    if (Objects.isNull(objectJson)) {
      String errorMsg = "JSON field \"" + fieldName + "\" does not exist in the request body";
      throw new IllegalStateException(errorMsg);
    }

    try {
      object = deserializer.readValue(objectJson.toString(), classType);
    } catch (IOException e) {
      String errorMsg =
          "Could not deserialize JSON field \"" + fieldName + "\" into " + classType.getType();
      throw new IllegalArgumentException(errorMsg);
    }

    return object;
  }
}
