package gms.dataacquisition.css.waveformloader;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.RawBody;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.StorageUnavailableException;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelSegmentPersister {

  private static final Logger logger = LoggerFactory.getLogger(ChannelSegmentPersister.class);

  private final String url;

  public ChannelSegmentPersister(String hostName) {
    Objects.requireNonNull(hostName, "Persister needs non-null hostname");
    if (!hostName.startsWith("http://")) {
      hostName = "http://" + hostName;
    }
    this.url = hostName + "/mechanisms/object-storage-distribution/waveforms/channel-segment/store";
  }

  public void storeSegments(List<ChannelSegment<Waveform>> segments) throws Exception {
    Objects.requireNonNull(segments, "Cannot store null segments");
    final Set<UUID> segmentIds = segments.stream().map(ChannelSegment::getId).collect(Collectors.toSet());
    handleResponse(postMsgPack(segments, this.url),
        "channel segments with ids " + segmentIds);
  }

  /**
   * Sends the data to the OSD, via an HTTP post with msgpack.
   *
   * @param obj data to be sent
   * @param url endpoint
   * @return An object containing the OSD response.
   * @throws Exception if for instance, the host cannot be reached
   */
  private static HttpResponse<String> postMsgPack(Object obj, String url) throws Exception {
    RawBody body = Unirest.post(url)
        .header("Accept", "application/json")
        .header("Content-Type", "application/msgpack")
        .header("Connection", "close")
        .body(CoiObjectMapperFactory.getMsgpackObjectMapper().writeValueAsBytes(obj));
    return body.asString();
  }

  /**
   * Handles an HTTP response, checking for error codes and throwing exceptions.
   *
   * @param response the http response to handle
   * @throws Exception if the response contains an error status code (client or server)
   */
  private static void handleResponse(HttpResponse<String> response,
      String dataDescription) throws Exception {
    int statusCode = response.getStatus();
    if (statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE) {
      throw new StorageUnavailableException();
    } else if (statusCode == HttpStatus.SC_CONFLICT) {
      logger.warn("Conflict in storing data: " + dataDescription);
    }
    // 400's and 500's are errors, except 'conflict', which is not considered an error.
    else if (statusCode >= 400 && statusCode <= 599) {
      throw new Exception(String.format("Error response from server (code %d): %s",
          statusCode, response.getBody()));
    }
  }

}
