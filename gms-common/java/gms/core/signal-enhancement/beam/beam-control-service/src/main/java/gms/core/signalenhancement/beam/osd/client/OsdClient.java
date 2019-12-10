package gms.core.signalenhancement.beam.osd.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.signalenhancement.beam.service.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.google.common.base.Preconditions.checkState;

/**
 * Beam Service's Access library for the OSD.
 * <p>
 * This is a placeholder implementation used to show the initial pattern for control class
 * interfaces to the OSD.
 */
public class OsdClient {

  private static final Logger logger = LoggerFactory.getLogger(OsdClient.class);

  private final HttpClientConfiguration channelGroupServiceConfig;
  private final HttpClientConfiguration waveformServiceConfig;

  /**
   * Construct the client that is used to make service calls to the OSD service
   */
  private OsdClient(HttpClientConfiguration channelGroupServiceConfig,
      HttpClientConfiguration waveformServiceConfig) {
    this.channelGroupServiceConfig = Objects.requireNonNull(channelGroupServiceConfig);
    this.waveformServiceConfig = Objects.requireNonNull(waveformServiceConfig);
  }

  /**
   * Obtains a new {@link OsdClient} configured to access the service at the provided {@link
   * HttpClientConfiguration}
   *
   * @param channelGroupServiceConfig {@link HttpClientConfiguration} describing how to access the
   *                                  OSD service to lookup channel processing group, not null
   * @param waveformServiceConfig     {@link HttpClientConfiguration} describing how to access
   *                                                                 the OSD
   *                                  service to lookup and store waveforms, not null
   * @return OsdClient, not null
   * @throws NullPointerException if httpClientConfiguration is null
   */
  public static OsdClient create(HttpClientConfiguration channelGroupServiceConfig,
      HttpClientConfiguration waveformServiceConfig) {
    Objects.requireNonNull(channelGroupServiceConfig,
        "Cannot create OsdClient with null channelGroupServiceConfig");
    Objects.requireNonNull(waveformServiceConfig,
        "Cannot create OsdClient with null waveformServiceConfig");

    return new OsdClient(channelGroupServiceConfig, waveformServiceConfig);
  }

  /**
   * Obtains the {@link Map} for the plugin with the provided {@link RegistrationInfo}
   *
   * @param registrationInfo {@link RegistrationInfo}, not null
   * @return {@link Map}, not null
   * @throws NullPointerException if registrationInfo is null
   */
  public Map<String, Object> loadPluginConfiguration(RegistrationInfo registrationInfo) {
    Objects.requireNonNull(registrationInfo,
        "loadPluginConfiguration require non-null RegistrationInfo");

    // TODO: is this right?
    return Map.of();
  }

  /**
   * Performs a POST on the OSD to retrieve the data required for an invoke operation in Beam
   * Service.
   *
   * @param processingGroupId load input data for this {@link UUID} to processing group
   * @param startTime         load input data inclusively beginning at this time, not null
   * @param endTime           load input data inclusively ending at this time, not null
   * @return Set of available channel segments for processing
   * @throws IllegalStateException if the Content-Type or status code of the OSD response is not as
   *                               expected
   */
  public List<ChannelSegment<Waveform>> loadChannelSegments(UUID processingGroupId,
      Instant startTime,
      Instant endTime) {

    Objects.requireNonNull(processingGroupId,
        "Cannot invoke loadChannelSegments with null processingGroupId");
    Objects.requireNonNull(startTime, "Cannot invoke loadChannelSegments with null startTime");
    Objects.requireNonNull(endTime, "Cannot invoke loadChannelSegments with null endTime");

    if (endTime.isBefore(startTime)) {
      throw new IllegalArgumentException(
          "Cannot invoke loadChannelSegments with endTime before startTime");
    }

    try {
      ChannelProcessingGroup procGroup = loadChannelProcessingGroup(processingGroupId);
      Map<String, Object> postBody = Map.of("channel-ids", procGroup.getChannelIds(),
          "start-time", startTime,
          "end-time", endTime,
          "with-waveforms", true);

      final String acceptType = "application/msgpack";

      // json object mapper serializes the request body; response is binary MessagePack
      Unirest.setObjectMapper(ObjectSerialization.getJsonClientObjectMapper());
      HttpResponse<InputStream> response = Unirest
          .post(waveformServiceConfig.getBaseUrl() + "/channel-segment")
          .header("Accept", acceptType)
          .header("Content-Type", "application/json")
          .body(postBody)
          .asBinary();

      String responseContentType = response.getHeaders().getFirst("Content-Type");
      if (!responseContentType.equals(acceptType)) {
        throw new IllegalStateException(String.format(
            "Response from service has incompatible Content-Type. Must be %s, but was %s",
            acceptType, responseContentType));
      }

      Map<UUID, ChannelSegment<Waveform>> channelSegmentsById =
          readChannelSegmentsByIdFromResponse(response);

      logger.info("Invoke {} Channel Segments received from OSD", channelSegmentsById.size());

      return new ArrayList<>(channelSegmentsById.values());
    } catch (UnirestException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<UUID, ChannelSegment<Waveform>> readChannelSegmentsByIdFromResponse(HttpResponse<InputStream> response)
      throws IOException {
    InputStream body = response.getRawBody();

    byte[] responseBytes = body.readAllBytes();
    if (response.getStatus() != HttpStatus.OK_200) {
      throw new IllegalStateException(String.format(
          "Got error response from service looking up channel processing group; "
              + "status %d, message: %s", response.getStatus(), new String(responseBytes)));
    }

    Map<UUID, ChannelSegment<Waveform>> channelSegmentsById =
        ObjectSerialization.readChannelSegmentsMsgpack(responseBytes);

    checkState(!channelSegmentsById.isEmpty(), "Error retrieving waveforms: Insufficient Data");

    return channelSegmentsById;
  }

  /**
   * Beam Service OSD access library operation to store results of an Beam. This invokes an
   * operation in a OSD service over HTTP.
   *
   * @param channelSegments channel segment, not null
   */
  public void store(List<ChannelSegment<Waveform>> channelSegments) {
    Objects.requireNonNull(channelSegments,
        "OsdClient store requires non-null channelSegments");

    // TODO: store CreationInformation after settling CreationInformation vs. CreationInfo
    // TODO: handle private vs public StorageVisibility

    logger.info("Store {} channel segments to OSD", channelSegments.size());

    try {
      String url = waveformServiceConfig.getBaseUrl() + "/channel-segment/store";
      logger.info("Posting to {} Content-Type: {} with {} channel segments", url,
          "application/msgpack", channelSegments.size());

      HttpResponse<InputStream> response = Unirest
          .post(url)
          .header("Content-Type", "application/msgpack")
          .header("Accept", "application/json")
          .body(ObjectSerialization.writeMessagePack(channelSegments))
          .asBinary();

      logger.info("OSD responded with {} (200 = success)", response.getStatus());

    } catch (UnirestException e) {
      throw new RuntimeException(e);
    }
  }

  public ChannelProcessingGroup loadChannelProcessingGroup(UUID id) {
    try {
      HttpResponse<InputStream> response = Unirest
          .get(channelGroupServiceConfig.getBaseUrl() + "/channel-processing-group/" + id)
          .header("Accept", "application/json")
          .asBinary();
      byte[] responseBytes = response.getBody().readAllBytes();
      if (response.getStatus() != HttpStatus.OK_200) {
        throw new IllegalStateException(String.format(
            "Got error response from service looking up channel processing group; "
                + "status %d, message: %s", response.getStatus(), new String(responseBytes)));
      }
      return ObjectSerialization.readJson(
          responseBytes,
          ChannelProcessingGroup.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
