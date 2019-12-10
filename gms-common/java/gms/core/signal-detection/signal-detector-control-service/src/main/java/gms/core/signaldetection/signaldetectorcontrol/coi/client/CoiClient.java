package gms.core.signaldetection.signaldetectorcontrol.coi.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.signaldetection.signaldetectorcontrol.http.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoiClient implements CoiRepository {

  private static final Logger logger = LoggerFactory.getLogger(CoiClient.class);
  private final String channelSegmentsServiceUrl;
  private final String signalDetectionsServiceUrl;

  /**
   * Construct the COI client that is used to make service calls to the OSD
   */
  private CoiClient(String channelSegmentsServiceUrl, String signalDetectionsServiceUrl) {
    this.channelSegmentsServiceUrl = channelSegmentsServiceUrl;
    this.signalDetectionsServiceUrl = signalDetectionsServiceUrl;
  }

  /**
   * Obtains a new {@link CoiClient} configured to access the service at the provided {@link
   * HttpClientConfiguration}
   *
   * @param channelSegmentsClientConfiguration {@link HttpClientConfiguration} describing how to
   * access the OSD service, not null
   * @return CoiClient, not null
   * @throws NullPointerException if channelSegmentsClientConfiguration is null
   */
  public static CoiClient create(HttpClientConfiguration channelSegmentsClientConfiguration,
      HttpClientConfiguration signalDetectionsClientConfiguration) {
    Objects.requireNonNull(channelSegmentsClientConfiguration,
        "Cannot create CoiClient with null channelSegmentsClientConfiguration");
    Objects.requireNonNull(signalDetectionsClientConfiguration,
        "Cannot create CoiClient with null signalDetectionsClientConfiguration");

    return new CoiClient(channelSegmentsClientConfiguration.buildUrl(),
        signalDetectionsClientConfiguration.buildUrl());
  }

  /**
   * Performs a POST on the Signal Detector Control OSD Gateway to retrieve the data required for an
   * invoke operation in Signal Detector Control.
   *
   * @param channelIds load input data for these {@link UUID} to {@link Channel}s
   * @param startTime load input data inclusively beginning at this time, not null
   * @param endTime load input data inclusively ending at this time, not null
   * @return Set of available channel segments for processing
   * @throws IllegalStateException if the osd-gateway service responds with a failure; if the
   * osd-gateway-service responds with an unanticipated Content-Type
   */
  @Override
  public List<ChannelSegment<Waveform>> getChannelSegments(Collection<UUID> channelIds,
      Instant startTime, Instant endTime) {

    Objects.requireNonNull(channelIds,
        "Cannot invoke getChannelSegments with null channelIds");
    Objects.requireNonNull(startTime, "Cannot invoke getChannelSegments with null startTime");
    Objects.requireNonNull(endTime, "Cannot invoke getChannelSegments with null endTime");

    if (endTime.isBefore(startTime)) {
      throw new IllegalArgumentException(
          "Cannot invoke getChannelSegments with endTime before startTime");
    }

    logger.info("Requesting channel segments from {} for channels:{}, start:{}, end:{}",
        channelSegmentsServiceUrl, channelIds, startTime, endTime);

    try {
      // json object mapper serializes the request body; response is binary MessagePack
      HttpResponse<InputStream> response = Unirest
          .post(channelSegmentsServiceUrl)
          .header("Accept", "application/msgpack")
          .header("Content-Type", "application/json")
          .body(ObjectSerialization.writeJson(
              Map.of("channel-ids", channelIds, "start-time", startTime, "end-time", endTime)))
          .asBinary();

      try (InputStream body = response.getBody()) {
        // Request succeeded: parse raw response from byte[] to Set<ChannelSegment>
        if (response.getStatus() == 200) {
          Map<UUID, ChannelSegment<Waveform>> channelSegmentMap = ObjectSerialization
              .readMessagePackChannelSegments(body.readAllBytes());

          logger.info("COI Service returned with ChannelSegments");
          return new ArrayList<>(channelSegmentMap.values());
        } else {
          String errorMessage = inputStreamToString(body);
          throw new IllegalStateException(errorMessage);
        }
      } catch (IOException e) {
        logger.error("Could not deserialize channelSegments from message pack response", e);
        throw new UncheckedIOException(e);
      }
    } catch (UnirestException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Signal Detector Control OSD Gateway access library operation to storeSignalDetections {@link
   * SignalDetection}s. This invokes an operation in the OSD Gateway service over HTTP.
   *
   * @param signalDetections collection of SignalDetections, not null
   * @return wheter the storeSignalDetections was successful
   */
  @Override
  public List<UUID> storeSignalDetections(Collection<SignalDetection> signalDetections) {
    Objects.requireNonNull(signalDetections,
        "CoiClient storeSignalDetections requires non-null signalDetections");

    // TODO: storeSignalDetections CreationInformation after settling CreationInformation vs.
    //  CreationInfo
    // TODO: handle private vs public StorageVisibility

    logger.info("Storing {} signal detections to COI route:{}", signalDetections.size(),
        signalDetectionsServiceUrl);

    try {
      HttpResponse<InputStream> response = Unirest
          .post(signalDetectionsServiceUrl)
          .header("Content-Type", "application/msgpack")
          .header("Accept", "application/json")
          .body(ObjectSerialization
              .writeMessagePack(signalDetections))
          .asBinary();

      try (InputStream body = response.getBody()) {
        if (HttpStatus.OK_200 == response.getStatus()) {
          List<UUID> storedIds = ObjectSerialization.readJsonCollection(body.readAllBytes(),
              List.class, UUID.class);

          logger.info("COI Service returned success for SignalDetections:{}", storedIds);

          return storedIds;
        } else {
          String errorMessage = inputStreamToString(body);
          throw new IllegalStateException(errorMessage);
        }
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    } catch (UnirestException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Signal Detector Control OSD Gateway access library operation to storeSignalDetections {@link
   * SignalDetection}s. This invokes an operation in the OSD Gateway service over HTTP.
   *
   * @param channelSegments collection of SignalDetections, not null
   */
  @Override
  public void storeChannelSegments(Collection<ChannelSegment<Waveform>> channelSegments) {
    Objects.requireNonNull(channelSegments,
        "CoiClient storeChannelSegments requires non-null channelSegments");

    logger.info("Storing {} channel segments to COI route:{}", channelSegments.size(),
        channelSegmentsServiceUrl);

    try {
      HttpResponse<InputStream> response = Unirest
          .post(channelSegmentsServiceUrl + "/store")
          .header("Content-Type", "application/msgpack")
          .header("Accept", "application/json")
          .body(ObjectSerialization
              .writeMessagePack(channelSegments))
          .asBinary();

      try (InputStream body = response.getBody()) {
        if (HttpStatus.OK_200 == response.getStatus()) {
          logger.info("COI Service returned success for SignalDetections");
        } else {
          String errorMessage = inputStreamToString(body);
          throw new IllegalStateException(errorMessage);
        }
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    } catch (UnirestException e) {
      throw new RuntimeException(e);
    }
  }

  private static String inputStreamToString(InputStream inputStream) throws IOException {
    try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) != -1) {
        result.write(buffer, 0, length);
      }

      return result.toString(StandardCharsets.UTF_8.toString());
    }
  }
}