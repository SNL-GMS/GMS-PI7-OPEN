package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

public class SparkChannelSohCoiRouteHandlers {

  private static Logger logger = LoggerFactory.getLogger(SparkChannelSohCoiRouteHandlers.class);

  private static final ObjectMapper jsonObjectMapper = CoiObjectMapperFactory
      .getJsonObjectMapper();
  private static final ObjectMapper msgpackObjectMapper = CoiObjectMapperFactory
      .getMsgpackObjectMapper();


  private SparkChannelSohCoiRouteHandlers() {
    //Class used as collection of functions
  }

  /**
   * Retrieves an {@link AcquiredChannelSohAnalog} by identity.
   * <p>
   * Returns a JSON body with a single AcquiredChannelSohAnalog.
   * <p>
   * Returns HTTP status codes: 200 when query finds an AcquiredChannelSohAnalog by id 400 when the
   * {id} parameter is not a UUID 404 when no AcquiredChannelSohAnalog with the provided identity
   * exists
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param stationSohRepositoryInterface the interface used query param acquiredChannelSohId query
   * parameter AcquiredChannelSohAnalog identifier, not null
   * @return return HTTP response with body containing a single AcquiredChannelSohAnalog
   */
  public static String getAcquiredChannelSohAnalog(spark.Request request, spark.Response response,
      StationSohRepositoryInterface stationSohRepositoryInterface) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(stationSohRepositoryInterface);

    String channelId = request.params(":id");

    logger.info("getAcquiredChannelSohAnalog endpoint hit with id: {}", channelId);

    UUID acquiredChannelSohId = UUID.fromString(channelId);
    Optional<AcquiredChannelSohAnalog> result = stationSohRepositoryInterface
        .retrieveAcquiredChannelSohAnalogById(acquiredChannelSohId);

    if (!result.isPresent()) {
      return HttpErrorHandlers
          .Http404Custom(request, response, "No result found for ChannelSohAnalog ID provided");
    }
    return jsonObjectMapper.writeValueAsString(result);
  }

  /**
   * Retrieves an {@link AcquiredChannelSohBoolean} by identity.
   * <p>
   * Returns a JSON body with a single AcquiredChannelSohBoolean.
   * <p>
   * Returns HTTP status codes: 200 when query finds an AcquiredChannelSohBoolean by id 400 when the
   * {id} parameter is not a UUID 404 when no AcquiredChannelSohBoolean with the provided identity
   * exists
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param stationSohRepositoryInterface the interface used param acquiredChannelSohId
   * AcquiredChannelSohBoolean identifier, not null
   * @return HTTP response with body containing a single AcquiredChannelSohBoolean
   */
  public static String getAcquiredChannelSohBoolean(spark.Request request, spark.Response response,
      StationSohRepositoryInterface stationSohRepositoryInterface) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(stationSohRepositoryInterface);

    String channelId = request.params(":id");

    logger.info("getAcquiredChannelSohAnalog endpoint hit with id: {}", channelId);

    UUID acquiredChannelSohId = UUID.fromString(channelId);
    Optional<AcquiredChannelSohBoolean> result = stationSohRepositoryInterface
        .retrieveAcquiredChannelSohBooleanById(acquiredChannelSohId);

    if (!result.isPresent()) {
      return HttpErrorHandlers
          .Http404Custom(request, response, "No result found for ChannelSohBoolean ID provided");
    }

    return jsonObjectMapper.writeValueAsString(result);
  }

  /**
   * Obtains the {@link AcquiredChannelSohBoolean} objects stored for the provided ProcessingChannel
   * identity between the start and end times.
   * <p>
   * Returns a JSON body with a list of AcquiredChannelSohBoolean.  The list is empty when the query
   * succeeds without results.
   * <p>
   * Returns HTTP status codes: 200 when query is successful, even if there are no results 400 if
   * any required query parameters are missing
   * <p>
   * Query Parameters: channelId ProcessingChannel identifier, not null startTime query start time,
   * not null endTime query end time, not null
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param stationSohRepositoryInterface the interface used param acquiredChannelSohId
   * @return HTTP response with body containing a list of AcquiredChannelSohBoolean
   */
  public static String getAcquiredChannelSohBooleanTimeRange(spark.Request request,
      spark.Response response, StationSohRepositoryInterface stationSohRepositoryInterface)
      throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(stationSohRepositoryInterface);

    String channelId = request.queryParams(Params.CHANNEL_ID);
    String startTimeString = request.queryParams(Params.START_TIME);
    String endTimeString = request.queryParams(Params.END_TIME);

    logger.info(
        "getAcquiredChannelSohBooleanTimeRange endpoint hit with parameters:\n"
            + "channel-id = {}\n"
            + "start-time = {}\n"
            + "end-time = {}",
        channelId, startTimeString, endTimeString);

    UUID channelID = UUID.fromString(channelId);
    Instant startTime = Instant.parse(startTimeString);
    Instant endTime = Instant.parse(endTimeString);
    return jsonObjectMapper.writeValueAsString(stationSohRepositoryInterface
        .retrieveBooleanSohByProcessingChannelAndTimeRange(channelID, startTime, endTime));
  }

  /**
   * Obtains the {@link AcquiredChannelSohBoolean} objects stored for the provided ProcessingChannel
   * identity between the start and end times.
   * <p>
   * Returns a JSON body with a list of AcquiredChannelSohBoolean.  The list is empty when the query
   * succeeds without results.
   * <p>
   * Returns HTTP status codes: 200 when query is successful, even if there are no results 400 if
   * any required query parameters are missing
   * <p>
   * Query Parameters: channelId ProcessingChannel identifier, not null startTime query start time,
   * not null endTime query end time, not null
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param stationSohRepositoryInterface the interface used param acquiredChannelSohId
   * @return HTTP response with body containing a list of AcquiredChannelSohBoolean
   */
  public static String getAcquiredChannelSohAnalogTimeRange(spark.Request request,
      spark.Response response, StationSohRepositoryInterface stationSohRepositoryInterface)
      throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(stationSohRepositoryInterface);

    String channelId = request.queryParams(Params.CHANNEL_ID);
    String startTimeString = request.queryParams(Params.START_TIME);
    String endTimeString = request.queryParams(Params.END_TIME);

    logger.info("getAcquiredChannelSohAnalogTimeRange endpoint hit with parameters:\n"
            + "channel-id = {}\n"
            + "start-time = {}\n"
            + "end-time = {}",
        channelId, startTimeString, endTimeString);

    UUID channelID = UUID.fromString(channelId);
    Instant startTime = Instant.parse(startTimeString);
    Instant endTime = Instant.parse(endTimeString);
    return jsonObjectMapper.writeValueAsString(stationSohRepositoryInterface
        .retrieveAnalogSohByProcessingChannelAndTimeRange(channelID, startTime,
            endTime));
  }

  /**
   * Handles a request to store a {@link AcquiredChannelSohAnalog}, given an array of acquired
   * channel soh analogs
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP)
   * @param stationSohRepositoryInterface the repository interface to use
   * @return HTTP status codes: 200 when successful 400 when the parameters are not valid
   */
  public static List<UUID> storeAcquiredChannelSohsAnalog(Request request, Response response,
      StationSohRepositoryInterface stationSohRepositoryInterface) throws Exception {

    Validate.notNull(stationSohRepositoryInterface);
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");
    Validate.notNull(request.body(), "Cannot store null acquired channel sohs analog");

    AcquiredChannelSohAnalog[] sohsAnalog;
    List<UUID> sohIDs = new ArrayList<>();

    String contentType = request.contentType();
    if (contentType.equalsIgnoreCase(ContentTypes.MSGPACK)) {
      sohsAnalog = msgpackObjectMapper
          .readValue(request.bodyAsBytes(), AcquiredChannelSohAnalog[].class);
    } else {
      sohsAnalog = jsonObjectMapper.readValue(request.body(), AcquiredChannelSohAnalog[].class);
    }
    Validate.notNull(sohsAnalog);
    logger.info("Got request to store {} AcquiredChannelSohAnalog", sohsAnalog.length);
    final long t1 = System.currentTimeMillis();
    for (AcquiredChannelSohAnalog soh : sohsAnalog) {
      stationSohRepositoryInterface.storeAnalogSoh(soh);
      sohIDs.add(soh.getId());
    }
    logger.info("Stored {} AcquiredChannelSohAnalog in {} millis", sohsAnalog.length,
        System.currentTimeMillis() - t1);
    return sohIDs;
  }

  /**
   * Handles a request to store a {@link AcquiredChannelSohBoolean}, given an array of acquired
   * channel soh booleans
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP)
   * @param stationSohRepositoryInterface the repository interface to use
   * @return HTTP status codes: 200 when successful 400 when the parameters are not valid
   */
  public static List<UUID> storeAcquiredChannelSohsBoolean(Request request, Response response,
      StationSohRepositoryInterface stationSohRepositoryInterface) throws Exception {

    Validate.notNull(stationSohRepositoryInterface);
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");
    Validate.notNull(request.body(), "Cannot store null acquired channel sohs boolean");

    AcquiredChannelSohBoolean[] sohsBoolean;
    List<UUID> sohIDs = new ArrayList<>();

    String contentType = request.contentType();
    if (contentType.equalsIgnoreCase(ContentTypes.MSGPACK)) {
      sohsBoolean = msgpackObjectMapper
          .readValue(request.bodyAsBytes(), AcquiredChannelSohBoolean[].class);
    } else {
      sohsBoolean = jsonObjectMapper.readValue(request.body(), AcquiredChannelSohBoolean[].class);
    }
    Validate.notNull(sohsBoolean);
    logger.info("Got request to store {} AcquiredChannelSohBoolean", sohsBoolean.length);
    final long t1 = System.currentTimeMillis();
    for (AcquiredChannelSohBoolean soh : sohsBoolean) {
      stationSohRepositoryInterface.storeBooleanSoh(soh);
      sohIDs.add(soh.getId());
    }
    logger.info("Stored {} AcquiredChannelSohBoolean in {} millis", sohsBoolean.length,
        System.currentTimeMillis() - t1);
    return sohIDs;
  }
}
