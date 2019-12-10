package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentStorageResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.BeamRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.FkSpectraRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains handler functions for the service routes.
 */
public class SparkWaveformCoiRouteHandler {

  private static Logger logger = LoggerFactory.getLogger(SparkWaveformCoiRouteHandler.class);

  /**
   * Serializes and deserializes signal detection common objects
   */
  private static final ObjectMapper jsonObjectMapper = CoiObjectMapperFactory
      .getJsonObjectMapper();
  private static final ObjectMapper msgpackObjectMapper = CoiObjectMapperFactory
      .getMsgpackObjectMapper();

  private static final JavaType CHANNEL_SEGMENT_WAVEFORM_TYPE = msgpackObjectMapper
      .getTypeFactory()
      .constructParametricType(
          ChannelSegment.class, Waveform.class);
  private static final JavaType LIST_CHANNEL_SEGMENT_WAVEFORM_TYPE = msgpackObjectMapper
      .getTypeFactory()
      .constructCollectionType(List.class, CHANNEL_SEGMENT_WAVEFORM_TYPE);

  private static final ObjectReader uuidListReader = jsonObjectMapper
      .readerFor(new TypeReference<List<UUID>>() {
      });

  private SparkWaveformCoiRouteHandler() {
    //private empty constructor denotes collection of functions
  }

  /**
   * Handles a request to retrieve {@link ChannelSegment} via {@link WaveformRepository}.
   * <p>
   * Returns ad body with a single ChannelSegment, either JSON or msgpack as per client request
   * header.
   * <p>
   * Returns HTTP status codes: 200 when query finds an ChannelSegment by id, startTime, endTime 400
   * when the parameters is not valid 400 when no ChannelSegment with the provided identity exists
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param waveformRepository the interface used
   * @return return HTTP response with body containing a single AcquiredChannelSohAnalog
   */
  public static Object retrieveChannelSegment(spark.Request request,
      spark.Response response,
      WaveformRepository waveformRepository) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(waveformRepository);

    logger.info("retrieveChannelSegment endpoint hit with query parameters: {}",
        request.queryParams());

    String channelId = request.queryParams(Params.CHANNEL_ID);
    String startTimeString = request.queryParams(Params.START_TIME);
    String endTimeString = request.queryParams(Params.END_TIME);
    String withWaveforms = request.queryParams(Params.WITH_WAVEFORMS);

    UUID channelID = UUID.fromString(channelId);
    Instant startTime = Instant.parse(startTimeString);
    Instant endTime = Instant.parse(endTimeString);
    Boolean waveforms = withWaveforms == null || Boolean.parseBoolean(withWaveforms);

    Optional<ChannelSegment<Waveform>> cs = waveformRepository
        .retrieveChannelSegment(channelID, startTime, endTime, waveforms);

    // Client requested msgpack
    if (HandlerUtil.shouldReturnMessagePack(request)) {
      response.type(ContentTypes.MSGPACK);
      return msgpackObjectMapper.writeValueAsBytes(cs);
    } else {
      return jsonObjectMapper.writeValueAsString(cs);
    }
  }

  /**
   * Handles a request to retrieve {@link ChannelSegment} via {@link WaveformRepository}.
   * <p>
   * Returns ad body with a single ChannelSegment, either JSON or msgpack as per client request
   * header.
   * <p>
   * Returns HTTP status codes: 200 when query finds an ChannelSegment by id, startTime, endTime 400
   * when the parameters is not valid 400 when no ChannelSegment with the provided identity exists
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param waveformRepository the interface used
   * @return return HTTP response with body containing a single AcquiredChannelSohAnalog
   */
  public static Object retrieveChannelSegmentsByChannelIdsOrChannelSegmentIds(spark.Request request,
      spark.Response response,
      WaveformRepository waveformRepository) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(waveformRepository);

    JsonNode postParams = jsonObjectMapper.readTree(request.body());

    JsonNode withWaveformsParam = postParams.get(Params.WITH_WAVEFORMS);
    boolean withWaveforms = withWaveformsParam == null || withWaveformsParam.asBoolean();
    JsonNode channelIdsNode = postParams.get(Params.CHANNEL_IDS);
    JsonNode idsNode = postParams.get("ids");

    if (channelIdsNode == null && idsNode == null) {
      throw new IllegalArgumentException(
          "Missing required arg; one of channel-ids or ids (UUID[])");
    }

    if (channelIdsNode != null) {
      Instant startTime = Instant.parse(postParams.get(Params.START_TIME).asText());
      Instant endTime = Instant.parse(postParams.get(Params.END_TIME).asText());
      Collection<UUID> channelIds = uuidListReader.readValue(channelIdsNode);
      logger.info(
          "retrieveChannelSegmentsByChannelIds endpoint hit with parameters:\n"
              + "channel-ids = {}\n"
              + "start-time = {}\n"
              + "end-time = {}\n"
              + "with-waveforms = {}",
          channelIds, startTime, endTime, withWaveforms);

      Map<UUID, ChannelSegment<Waveform>> segmentsByChanId
          = waveformRepository.retrieveChannelSegments(
          channelIds, startTime, endTime, withWaveforms);
      // Client requested msgpack
      if (HandlerUtil.shouldReturnMessagePack(request)) {
        response.type(ContentTypes.MSGPACK);
        return msgpackObjectMapper.writeValueAsBytes(segmentsByChanId);
      } else {
        return jsonObjectMapper.writeValueAsString(segmentsByChanId);
      }
    } else {
      Collection<UUID> ids = uuidListReader.readValue(idsNode);
      logger.info(
          "retrieveChannelSegmentsByChannelIds endpoint hit with parameters:\n"
              + "ids = {}\n"
              + "with-waveforms = {}",
          ids, withWaveforms);

      Map<UUID, ChannelSegment<Waveform>> segmentsById
          = waveformRepository.retrieveChannelSegments(ids, withWaveforms);
      // Client requested msgpack
      if (HandlerUtil.shouldReturnMessagePack(request)) {
        response.type(ContentTypes.MSGPACK);
        return msgpackObjectMapper.writeValueAsBytes(segmentsById);
      } else {
        return jsonObjectMapper.writeValueAsString(segmentsById);
      }
    }
  }

  /**
   * Handles a request to store Channel Segment msgpack data.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   */
  public static Object storeChannelSegments(spark.Request request,
      spark.Response response, WaveformRepository waveformRepository)
      throws IOException {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(waveformRepository);

    List<ChannelSegment<Waveform>> channelSegments;
    String contentType = request.contentType();
    if (contentType.equalsIgnoreCase(ContentTypes.MSGPACK)) {
      channelSegments = msgpackObjectMapper.readValue(
          request.bodyAsBytes(), LIST_CHANNEL_SEGMENT_WAVEFORM_TYPE);
    } else {
      channelSegments = jsonObjectMapper.readValue(
          request.body(), LIST_CHANNEL_SEGMENT_WAVEFORM_TYPE);
    }
    Validate.notNull(channelSegments);
    logger.info("Request to store {} ChannelSegment<Waveform> received", channelSegments.size());
    final long t1 = System.currentTimeMillis();
    ChannelSegmentStorageResponse storageResponse = waveformRepository.store(channelSegments);
    logger.info("Stored {} ChannelSegment<Waveform> in {} millis", channelSegments.size(),
        (System.currentTimeMillis() - t1));
    return jsonObjectMapper.writeValueAsString(storageResponse);
  }

  /**
   * Handles a request to store a beamed ChannelSegment and a BeamCreationInfo.
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding, such as to set an
   * error code.
   */
  public static String storeBeamResult(spark.Request request,
      spark.Response response, WaveformRepository waveformRepository,
      BeamRepositoryInterface beamRepositoryInterface)
      throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(waveformRepository);
    Validate.notNull(beamRepositoryInterface);

    JsonNode postParams;

    final String contentType = request.contentType();
    if (contentType.equalsIgnoreCase(ContentTypes.MSGPACK)) {
      postParams = msgpackObjectMapper.readTree(request.bodyAsBytes());
    } else {
      postParams = jsonObjectMapper.readTree(request.body());
    }

    JsonNode segmentsNode = postParams.get("segments");
    Validate.notNull(segmentsNode, "Must specify 'segments' (ChannelSegment[])");
    JsonNode beamCreationInfosNode = postParams.get("beamCreationInfos");
    Validate
        .notNull(beamCreationInfosNode, "Must specify 'beamCreationInfos' (BeamCreationInfo[])");
    List<ChannelSegment<Waveform>> segments = jsonObjectMapper
        .convertValue(segmentsNode, LIST_CHANNEL_SEGMENT_WAVEFORM_TYPE);
    Validate.notNull(segments, "Cannot accept null segments to store");
    List<BeamCreationInfo> cis = jsonObjectMapper.convertValue(beamCreationInfosNode,
        jsonObjectMapper.getTypeFactory()
            .constructCollectionType(ArrayList.class, BeamCreationInfo.class));
    Validate.notNull(cis, "Cannot accept null beam creation info's to store");
    Map<BeamCreationInfo, ChannelSegment<Waveform>> data = associateCreationInfoToSegment(cis,
        segments);
    for (Map.Entry<BeamCreationInfo, ChannelSegment<Waveform>> e : data.entrySet()) {
      beamRepositoryInterface.storeBeam(e.getValue(), e.getKey());
    }
    return "";
  }

  private static Map<BeamCreationInfo, ChannelSegment<Waveform>> associateCreationInfoToSegment(
      List<BeamCreationInfo> cis, List<ChannelSegment<Waveform>> segments) {
    // check size first; validating for one-to-one means they must have same cardinality.
    Validate.isTrue(segments.size() == cis.size(),
        String.format(
            "segments and beamCreationInfo's differ in size; %s segments, %s beamCreationInfo's",
            segments.size(), cis.size()));
    // check that all beamCreationInfo's have a unique channel segment ID.
    List<UUID> segmentIds = cis.stream()
        .map(BeamCreationInfo::getChannelSegmentId)
        .collect(Collectors.toList());
    Validate.isTrue(segmentIds.size() == new HashSet<>(segmentIds).size(),
        "BeamCreationInfo's contain duplicate segmentId's: " + segmentIds);
    // organize the segments by their id
    Map<UUID, ChannelSegment<Waveform>> segmentsById = segments.stream()
        .collect(Collectors.toMap(ChannelSegment::getId, Function.identity()));
    // iterate over the creation info's, looking up the corresponding segment for it.
    // throw an exception if the creation info does not have a corresponding segment.
    Map<BeamCreationInfo, ChannelSegment<Waveform>> result = new HashMap<>();
    for (BeamCreationInfo ci : cis) {
      ChannelSegment<Waveform> cs = segmentsById.get(ci.getChannelSegmentId());
      if (cs == null) {
        throw new IllegalArgumentException(
            "BeamCreationInfo does not relate to a channel segment: " + ci);
      }
      result.put(ci, cs);
    }
    return result;
  }

  /**
   * Handles a request to retrieve data availability via {@link WaveformRepository}.
   * <p>
   * Returns ad body with a double.
   * <p>
   * Returns HTTP status codes: 200 when query finds an ChannelSegment by id, startTime, endTime 400
   * when the parameters is not valid 400 when no ChannelSegment with the provided identity exists
   *
   * @param request the request (HTTP)
   * @param response the response (HTTP); this can be modified before responding
   * @param waveformRepository the interface used
   * @return return HTTP response with body containing a single data percentage
   */
  public static Object batchCalculateChannelAvailability(spark.Request request,
      spark.Response response,
      WaveformRepository waveformRepository) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(waveformRepository);
    JsonNode postParams = jsonObjectMapper.readTree(request.body());

    JsonNode channelIdsNode = postParams.get("channel-ids");

    if (channelIdsNode == null) {
      throw new IllegalArgumentException(
          "Missing required arg; one of channel-ids (UUID[])");
    }

    Instant startTime = Instant.parse(postParams.get(Params.START_TIME).asText());
    Instant endTime = Instant.parse(postParams.get(Params.END_TIME).asText());
    List<UUID> channelIds = uuidListReader.readValue(channelIdsNode);
    logger.info(
        "batchCalculateChannelAvailability endpoint hit with parameters:\n"
            + "channel-ids = {}\n"
            + "start-time = {}\n"
            + "end-time = {}",
        channelIds, startTime, endTime);

    return jsonObjectMapper.writeValueAsString(waveformRepository
        .calculateChannelAvailability(channelIds, startTime, endTime));
  }

  public static Object retrieveFkChannelSegmentsByChannelIds(
      spark.Request request, spark.Response response,
      FkSpectraRepository fkRepository) throws Exception {

    Validate.notNull(request);
    Validate.notNull(response);
    Validate.notNull(fkRepository);

    JsonNode postParams = jsonObjectMapper.readTree(request.body());
    JsonNode channelIdsNode = postParams.get("channel-ids");
    JsonNode startTimeNode = postParams.get(Params.START_TIME);
    JsonNode endTimeNode = postParams.get(Params.END_TIME);
    Validate.notNull(channelIdsNode, "Must specify channel-ids");
    Validate.notNull(startTimeNode, "Must specify start-time");
    Validate.notNull(endTimeNode, "Must specify end-time");
    Instant startTime = Instant.parse(postParams.get(Params.START_TIME).asText());
    Instant endTime = Instant.parse(postParams.get(Params.END_TIME).asText());
    Collection<UUID> channelIds = uuidListReader.readValue(channelIdsNode);
    List<ChannelSegment<FkSpectra>> results = new ArrayList<>();
    for (UUID chanId : channelIds) {
      results.addAll(fkRepository.segmentsForProcessingChannel(chanId, startTime, endTime));
    }
    // Client requested msgpack
    if (HandlerUtil.shouldReturnMessagePack(request)) {
      response.type(ContentTypes.MSGPACK);
      return msgpackObjectMapper.writeValueAsBytes(results);
    } else {
      return jsonObjectMapper.writeValueAsString(results);
    }
  }

}

