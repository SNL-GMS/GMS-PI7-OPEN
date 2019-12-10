package gms.core.waveformqc.waveformqccontrol.coi;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * COI access mechanism for the Waveform Qc Control class.
 */
public class CoiClient implements CoiRepository {

  private static final Logger logger = LoggerFactory.getLogger(CoiClient.class);
  private final ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  static final String BASE_URL = "/coi/";

  private String waveformCoiUrl;
  private String qcMaskCoiUrl;

  /**
   * Construct the COI client that is used to make service calls to the OSD COI service
   */
  public CoiClient(String waveformCoiUrl, String qcMaskCoiUrl) {
    this.waveformCoiUrl = waveformCoiUrl;
    this.qcMaskCoiUrl = qcMaskCoiUrl;
  }

  @Override
  public ChannelSegment<Waveform> getWaveforms(ChannelSegmentDescriptor descriptor)
      throws IOException {
    logger.info("CoiClient received request to getWaveforms for descriptor: {}", descriptor);
    checkNotNull(descriptor);

    HttpResponse<String> response;
    try {
      response = Unirest
          .post(waveformCoiUrl + BASE_URL + "/channel-segment")
          .header("accept", "application/json")
          .header("Content-Type", "application/json")
          .body(jsonObjectMapper.writeValueAsString(
              Map.of(
                  "channel-ids", List.of(descriptor.getChannelId()),
                  "start-time", descriptor.getStartTime(),
                  "end-time", descriptor.getEndTime(),
                  "with-waveforms", true)))
          .asString();
    } catch (UnirestException e) {
      throw new IOException("CoiClient unable to parse response to getWaveforms", e);
    }

    Map<UUID, ChannelSegment<Waveform>> channelSegmentsByChannelId;
    if (response.getStatus() == 200) {
      //comes back as Map<UUID, ChannelSegment<Waveform>>
      channelSegmentsByChannelId = jsonObjectMapper
          .readValue(response.getBody(),
              new TypeReference<Map<UUID, ChannelSegment<Waveform>>>() {
              });
    } else {
      throw new RuntimeException(
          String.format("CoiClient unable to getWaveforms: %d %s", response.getStatus(),
              response.getStatusText()));
    }

    checkState(!channelSegmentsByChannelId.isEmpty(),
        "Error retrieving waveforms: Insufficient Data");
    checkState(channelSegmentsByChannelId.size() == 1,
        "Error retrieving waveforms, single channel request should return a response with a single channel segment");

    logger.info("CoiClient getWaveforms request returned successfully");

    return channelSegmentsByChannelId.values().iterator().next();
  }

  @Override
  public List<QcMask> getQcMasks(ChannelSegmentDescriptor descriptor) throws IOException {
    logger.info("CoiClient received request to getQcMasks for descriptor: {}", descriptor);
    checkNotNull(descriptor);

    ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    HttpResponse<String> response;
    try {
      response = Unirest
          .post(qcMaskCoiUrl + BASE_URL + "/qc-mask")
          .header("accept", "application/json")
          .header("Content-Type", "application/json")
          .body(jsonObjectMapper.writeValueAsString(
              Map.of(
                  "channel-ids", List.of(descriptor.getChannelId()),
                  "start-time", descriptor.getStartTime(),
                  "end-time", descriptor.getEndTime())))
          .asString();
    } catch (UnirestException e) {
      throw new IOException("CoiClient unable to parse response to getQcMasks", e);
    }

    Map<UUID, List<QcMask>> qcMasksByChannelId;
    if (response.getStatus() == 200) {
      qcMasksByChannelId = jsonObjectMapper.readValue(
          response.getBody(),
          new TypeReference<Map<UUID, List<QcMask>>>() {
          });
    } else {
      throw new RuntimeException(
          String.format("CoiClient unable to getQcMasks: %d %s", response.getStatus(), response
              .getStatusText()));
    }

    logger.info("CoiClient getQcMasks request returned successfully");

    return qcMasksByChannelId.values().stream().flatMap(List::stream).collect(Collectors.toList());
  }

  @Override
  public List<AcquiredChannelSohBoolean> getChannelSoh(ChannelSegmentDescriptor descriptor)
      throws IOException {
    logger.info("CoiClient received request to getChannelSoh for descriptor: {}", descriptor);
    checkNotNull(descriptor);

    ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    HttpResponse<String> response;
    try {
      response = Unirest
          //TODO: When new "/coi"-based route is implemented, use POST instead of GET
          .get(waveformCoiUrl + BASE_URL + "/acquired-channel-soh/boolean")
          .header("accept", "application/json")
          .queryString(Map.of(
              "channel-id", descriptor.getChannelId().toString(),
              "start-time", descriptor.getStartTime().toString(),
              "end-time", descriptor.getEndTime().toString()))
          .asString();
    } catch (UnirestException e) {
      throw new IOException("CoiClient unable to parse response to getChannelSoh", e);
    }

    List<AcquiredChannelSohBoolean> sohBooleans;
    if (response.getStatus() == 200) {
      sohBooleans = jsonObjectMapper.readValue(
          response.getBody(),
          new TypeReference<List<AcquiredChannelSohBoolean>>() {
          });
    } else {
      throw new RuntimeException(
          String.format("CoiClient unable to getChannelSoh: %d %s", response.getStatus(), response
              .getStatusText()));
    }

    logger.info("CoiClient getChannelSoh request returned successfully");

    return sohBooleans;
  }

  /**
   * Qc Mask Control OSD COI access library operation to store QcMasks. This invokes an operation in
   * the OSD COI service over HTTP.
   *
   * @param qcMasks a collection of QcMasks, not null
   */
  @Override
  public void storeQcMasks(List<QcMask> qcMasks) throws IOException {
    logger.info("CoiClient received request to storeQcMasks for {} QcMasks", qcMasks.size());

    checkNotNull(qcMasks);
    if (qcMasks.isEmpty()) {
      logger.info("Store request contains no masks, returning...");
      return;
    }

    try {
      Unirest
          .post(qcMaskCoiUrl + BASE_URL + "/qc-masks")
          .header("accept", "application/json")
          .header("Content-Type", "application/json")
          .body(jsonObjectMapper.writeValueAsString(qcMasks))
          .asObject(String.class);
    } catch (UnirestException e) {
      throw new IOException("CoiClient unable to send storeQcMasks request", e);
    }

    logger.info("CoiClient storeQcMasks request returned successfully");
  }
}
