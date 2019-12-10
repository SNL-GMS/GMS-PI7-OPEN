package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

public class SparkRawDataFrameCoiRouteHandler {

  private static Logger logger = LoggerFactory.getLogger(SparkRawDataFrameCoiRouteHandler.class);

  private static final ObjectMapper jsonObjectMapper = CoiObjectMapperFactory
      .getJsonObjectMapper();
  private static final ObjectMapper msgpackObjectMapper = CoiObjectMapperFactory
      .getMsgpackObjectMapper();

  private SparkRawDataFrameCoiRouteHandler() {
    //private empty constructor denotes collection of functions
  }

  /**
   * Retrieves a list of raw station data frames, which may be empty.
   * <p>
   * Returns HTTP status codes: 200 when query is successful, even if there are no results; 400 if
   * any required query parameters are missing.
   * <p>
   * Query Parameters: start-time, not null, IS0-8601 format, required end-time, not null, IS0-8601
   * format, required station-name, optional
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param frameRepository the interface used to retrieve raw station data frames
   * @return HTTP response with body containing a list of RawStationDataFrame
   */
  public static Object getRawStationDataFrames(
      Request request,
      Response response,
      RawStationDataFrameRepositoryInterface frameRepository) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(frameRepository);

    String stationId = request.queryParams("station-id");
    String startTimeString = request.queryParams(Params.START_TIME);
    String endTimeString = request.queryParams(Params.END_TIME);

    logger.info(
        "getRawStationDataFrames endpoint hit with parameters:\n"
            + "station-id = {}\n"
            + "start-time = {}\n"
            + "end-time = {}",
        stationId, startTimeString, endTimeString);

    Instant startTime = Instant.parse(startTimeString);
    Instant endTime = Instant.parse(endTimeString);
    List<RawStationDataFrame> frames = stationId != null ?
        frameRepository.retrieveByStationId(UUID.fromString(stationId), startTime, endTime)
        : frameRepository.retrieveAll(startTime, endTime);

    // Client requested msgpack
    if (HandlerUtil.shouldReturnMessagePack(request)) {
      response.type(ContentTypes.MSGPACK);
      return msgpackObjectMapper.writeValueAsBytes(frames);
    } else {
      return jsonObjectMapper.writeValueAsString(frames);
    }
  }

  /**
   * Handles a request to store a {@link RawStationDataFrame}, given an array of raw station data
   * frames
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP)
   * @param rawStationDataFrameRepositoryInterface the repository interface to use
   * @return HTTP status codes: 200 when successful 400 when the parameters are not valid
   */
  public static List<UUID> storeRawStationDataFrames(Request request,
      Response response,
      RawStationDataFrameRepositoryInterface rawStationDataFrameRepositoryInterface)
      throws Exception {

    Validate.notNull(rawStationDataFrameRepositoryInterface);
    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");
    Validate.notNull(request.body(), "Cannot store null raw station data frames");

    RawStationDataFrame[] rawStationDataFrames;
    List<UUID> frameIDs = new ArrayList<>();

    String contentType = request.contentType();
    if (contentType.equalsIgnoreCase(ContentTypes.MSGPACK)) {
      rawStationDataFrames = msgpackObjectMapper.readValue(
          request.bodyAsBytes(), RawStationDataFrame[].class);
    } else {
      rawStationDataFrames = jsonObjectMapper.readValue(
          request.body(), RawStationDataFrame[].class);
    }
    Validate.notNull(rawStationDataFrames);
    logger.info("Got request to store {} RawStationDataFrames", rawStationDataFrames.length);
    final long t1 = System.currentTimeMillis();
    for (RawStationDataFrame frame : rawStationDataFrames) {
      //Try to store each frame, if one fails, log it but continue to try to store the other frames
      try {
        rawStationDataFrameRepositoryInterface.storeRawStationDataFrame(frame);
        frameIDs.add(frame.getId());
      }catch (Exception e){
        logger.error("Failed to store frame: " + frame.getId(), e);
      }

    }
    logger.info("Stored {} RawStationDataFrames in {} millis", rawStationDataFrames.length,
        System.currentTimeMillis() - t1);
    return frameIDs;
  }
}
