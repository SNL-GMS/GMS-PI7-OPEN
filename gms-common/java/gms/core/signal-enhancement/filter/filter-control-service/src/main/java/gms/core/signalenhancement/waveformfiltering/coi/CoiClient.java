package gms.core.signalenhancement.waveformfiltering.coi;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory.getJsonObjectMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentStorageResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoiClient implements CoiRepository {

  private static final Logger logger = LoggerFactory.getLogger(CoiClient.class);

  private static final String ACCEPT = "accept";
  private static final String APPLICATION_JSON = "application/json";
  private static final String CONTENT_TYPE = "Content-Type";

  private String coiStationReferenceUrl;
  private String coiWaveformUrl;

  /**
   * Construct the COI client that is used to make service calls to the OSD COI service
   */
  private CoiClient(String coiStationReferenceUrl, String coiWaveformUrl) {
    this.coiStationReferenceUrl = coiStationReferenceUrl;
    this.coiWaveformUrl = coiWaveformUrl;
  }

  /**
   * Obtains a new {@link CoiClient} configured to access the service at the provided
   * {@link HttpClientConfiguration}
   *
   *
   * @param coiStationReferenceConfiguration {@link HttpClientConfiguration}
   * describing how to access the station reference coi service, not null
   * @param coiWaveformConfiguration {@link HttpClientConfiguration}
   * describing how to access the waveform coi service, not null
   * @return CoiClient, not null
   * @throws NullPointerException if httpClientConfiguration is null
   */
  public static CoiClient create(
      HttpClientConfiguration coiStationReferenceConfiguration,
      HttpClientConfiguration coiWaveformConfiguration) {
    Objects.requireNonNull(coiWaveformConfiguration,
        "Cannot create CoiClient with null httpClientConfiguration");

    return new CoiClient(toUrl(coiStationReferenceConfiguration), toUrl(coiWaveformConfiguration));
  }

  private static String toUrl(HttpClientConfiguration coiWaveformsConfiguration) {
    return String.format("http://%s:%d%s", coiWaveformsConfiguration.getHost(),
        coiWaveformsConfiguration.getPort(), coiWaveformsConfiguration.getBasePath());
  }

  @Override
  public List<ReferenceChannel> getChannels(List<UUID> channelIds) throws IOException {
    checkNotNull(channelIds, "CoiClient getChannels requires non-null channelIds");

    HttpResponse<InputStream> response;
    try {
      response = Unirest
          .post(coiStationReferenceUrl + "/channels/query/versionIds")
          .header(ACCEPT, APPLICATION_JSON)
          .header(CONTENT_TYPE, APPLICATION_JSON)
          .body(getJsonObjectMapper().writeValueAsBytes(channelIds))
          .asBinary();
    } catch (UnirestException e) {
      throw new IOException("CoiClient unable to parse response to getChannels", e);
    }

    List<ReferenceChannel> channels;
    if (response.getStatus() == 200) {
      channels = getJsonObjectMapper()
          .readValue(response.getBody(), new TypeReference<List<ReferenceChannel>>() {
          });
    } else {
      throw new IOException(String
          .format("CoiClient unable to getChannels: %d %s", response.getStatus(),
              response.getStatusText()));
    }

    logger.info("CoiClient getChannels request returned successfully");
    return channels;
  }

  @Override
  public ChannelSegment<Waveform> getWaveforms(ChannelSegmentDescriptor descriptor)
      throws IOException {

    Objects.requireNonNull(descriptor,
        "CoiClient getWaveforms requires non-null descriptor");
    logger.info("CoiClient received request to getWaveforms for descriptor: {}", descriptor);

    if (descriptor.getEndTime().isBefore(descriptor.getStartTime())) {
      throw new IllegalArgumentException(
          "Cannot invoke getWaveforms with a descriptor with endTime before startTime");
    }

    ObjectMapper msgpackObjectMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

    HttpResponse<InputStream> response;
    try {
      response = Unirest
          .post(coiWaveformUrl + "/channel-segment")
          .header(ACCEPT, "application/msgpack")
          .header(CONTENT_TYPE, APPLICATION_JSON)
          .body(getJsonObjectMapper().writeValueAsBytes(
              Map.of(
                  "channel-ids", List.of(descriptor.getChannelId()),
                  "start-time", descriptor.getStartTime(),
                  "end-time", descriptor.getEndTime(),
                  "with-waveforms", true)))
          .asBinary();
    } catch (UnirestException e) {
      throw new IOException("CoiClient unable to parse response to getWaveforms", e);
    }

    Map<UUID, ChannelSegment<Waveform>> channelSegmentsByChannelId;
    if (response.getStatus() == 200) {
      //comes back as Map<UUID, ChannelSegment<Waveform>>
      channelSegmentsByChannelId = msgpackObjectMapper
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
  public ChannelSegmentStorageResponse storeChannelSegments(
      Collection<ChannelSegment<Waveform>> channelSegments) throws IOException {
    Objects.requireNonNull(channelSegments,
        "CoiClient storeChannelSegments requires non-null channelSegments");
    logger.info("CoiClient received request to store {} channelSegments", channelSegments.size());

    ObjectMapper jsonObjectMapper = getJsonObjectMapper();

    HttpResponse<String> response;
    try {
      response = Unirest
          .post(coiWaveformUrl + "/channel-segment/store")
          .header(ACCEPT, APPLICATION_JSON)
          .header(CONTENT_TYPE, APPLICATION_JSON)
          .body(jsonObjectMapper.writeValueAsString(channelSegments))
          .asString();
    } catch (UnirestException e) {
      throw new IOException("CoiClient unable to parse response to storeWaveforms", e);
    }

    ChannelSegmentStorageResponse storageResponse;
    if (response.getStatus() == 200) {
      storageResponse = jsonObjectMapper
          .readValue(response.getBody(), ChannelSegmentStorageResponse.class);
    } else {
      throw new RuntimeException(
          String.format("CoiClient failed to execute storeWaveforms: %d %s", response.getStatus(),
              response.getStatusText()));
    }

    checkNotNull(storageResponse);
    return storageResponse;
  }
}
