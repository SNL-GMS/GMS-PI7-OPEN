package gms.dataacquisition.stationreceiver.osdclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RawBody;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.StorageUnavailableException;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.*;
import org.apache.commons.lang3.Validate;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;


public class StationReceiverOsdClientAccessLibrary implements StationReceiverOsdClientInterface {

  private static final HttpClientConfig DEFAULT_STATION_REF_SERVICE_CONFIG
      = new HttpClientConfig("osd-stationreference-coi-service", 8080);
  private static final HttpClientConfig DEFAULT_WAVEFORMS_SERVICE_CONFIG
      = new HttpClientConfig("osd-waveforms-repository-service", 8080);

  private static final ObjectMapper jsonMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private static final ObjectMapper msgpackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

  private final String
      GET_STATION_BY_NAME_URL,
      GET_SITE_BY_NAME_URL,
      STORE_ANALOG_SOHS_URL,
      STORE_BOOLEAN_SOHS_URL,
      STORE_CHANNEL_SEGMENTS_URL,
      STORE_RAW_STATION_DATA_FRAME_URL;

  private static final Logger logger =
      LoggerFactory.getLogger(StationReceiverOsdClientAccessLibrary.class);

  private final String fsOutputDirectory;

  private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
  private final RandomStringGenerator generator = new RandomStringGenerator.Builder()
      .withinRange('0', 'z')
      .filteredBy(LETTERS, DIGITS)
      .build();

  private final Map<ChannelInfo, UUID> channelIdCache = new HashMap<>();
  private final Map<String, UUID> stationIdCache = new HashMap<>();
  private final Set<ChannelInfo> unknownChannels = new HashSet<>();
  private final Set<String> unknownStations = new HashSet<>();

  // Required for JSON serialization.
  static {

    Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {

      public <T> T readValue(String s, Class<T> aClass) {
        try {
          return jsonMapper.readValue(s, aClass);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

      public String writeValue(Object o) {
        try {
          return jsonMapper.writeValueAsString(o);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
    // Set socket data timeout to 5 minutes, for large waveform uploads.
    Unirest.setTimeouts(DEFAULT_CONNECTION_TIMEOUT, 300000);
  }

  public StationReceiverOsdClientAccessLibrary(String fsOutputDirectory) {
    this(DEFAULT_STATION_REF_SERVICE_CONFIG, DEFAULT_WAVEFORMS_SERVICE_CONFIG, fsOutputDirectory);
  }

  public StationReceiverOsdClientAccessLibrary() {
    this(DEFAULT_STATION_REF_SERVICE_CONFIG, DEFAULT_WAVEFORMS_SERVICE_CONFIG, null);
  }

  /**
   * Access library used to query and send data to the OSD.
   */
  public StationReceiverOsdClientAccessLibrary(HttpClientConfig stationRefServiceConfig,
      HttpClientConfig waveformsServiceConfig) {
    this(stationRefServiceConfig, waveformsServiceConfig, null);
  }

  /**
   * Access library used to query and send data to the OSD, and optionally write raw station data
   * frames to disk.
   *
   * @param fsOutputDirectory Optional path to write raw station data frames to, as flat JSON
   * files.
   */
  public StationReceiverOsdClientAccessLibrary(HttpClientConfig stationRefServiceConfig,
      HttpClientConfig waveformsServiceConfig, String fsOutputDirectory) {
    this.fsOutputDirectory = fsOutputDirectory;
    Objects.requireNonNull(stationRefServiceConfig);
    Objects.requireNonNull(waveformsServiceConfig);
    this.GET_STATION_BY_NAME_URL = stationRefServiceConfig.asUrl()
        + "/mechanisms/object-storage-distribution/station-reference/stations/processing/name/{name}";
    this.GET_SITE_BY_NAME_URL = stationRefServiceConfig.asUrl()
        + "/mechanisms/object-storage-distribution/station-reference/sites/processing/name/{name}";
    this.STORE_ANALOG_SOHS_URL =
        waveformsServiceConfig.asUrl() + "/coi/acquired-channel-sohs/analog";
    this.STORE_BOOLEAN_SOHS_URL =
        waveformsServiceConfig.asUrl() + "/coi/acquired-channel-sohs/boolean";
    this.STORE_CHANNEL_SEGMENTS_URL =
        waveformsServiceConfig.asUrl() + "/mechanisms/object-storage-distribution/waveforms/channel-segment/store";
    this.STORE_RAW_STATION_DATA_FRAME_URL =
        waveformsServiceConfig.asUrl() + "/coi/raw-station-data-frames";
  }

  /**
   * Sends a channel segment batch to the OSD.
   *
   * @param channelSegmentBatch data to be sent
   */
  @Override
  public void storeChannelSegments(Collection<ChannelSegment<Waveform>> channelSegmentBatch)
      throws Exception {

    Validate.notNull(channelSegmentBatch);
    final long startTime = System.currentTimeMillis();
    if (channelSegmentBatch.isEmpty()) {
      return; // nothing to do
    }
    List<String> segmentNames = channelSegmentBatch.stream()
        .map(ChannelSegment::getName).collect(Collectors.toList());
    handleResponse(postMsgPack(channelSegmentBatch, STORE_CHANNEL_SEGMENTS_URL),
        segmentNames.toString());
    final long endTime = System.currentTimeMillis();
    logger.info(String.format("Stored %d channel segments in %d ms: %s",
        channelSegmentBatch.size(), endTime - startTime, segmentNames));
  }

  /**
   * Stores a set of SOH objects which may contain both boolean and analog types.
   *
   * @param sohs the soh's to store
   */
  @Override
  public void storeChannelStatesOfHealth(Collection<AcquiredChannelSoh> sohs)
      throws Exception {

    Validate.notNull(sohs);
    if (sohs.isEmpty()) {
      return; // nothing to do
    }
    Set<AcquiredChannelSohAnalog> analogSohs = sohs.stream()
        .filter(soh -> soh instanceof AcquiredChannelSohAnalog)
        .map(soh -> (AcquiredChannelSohAnalog) soh)
        .collect(Collectors.toSet());
    Set<AcquiredChannelSohBoolean> booleanSohs = sohs.stream()
        .filter(soh -> soh instanceof AcquiredChannelSohBoolean)
        .map(soh -> (AcquiredChannelSohBoolean) soh)
        .collect(Collectors.toSet());

    final long startTime = System.currentTimeMillis();
    handleResponse(postMsgPack(analogSohs, STORE_ANALOG_SOHS_URL), analogSohs.toString());
    handleResponse(postMsgPack(booleanSohs, STORE_BOOLEAN_SOHS_URL), booleanSohs.toString());
    final long endTime = System.currentTimeMillis();
    logger.info(String.format("Stored %d SOH's in %d ms",
        sohs.size(), endTime - startTime));
  }

  /**
   * Sends a RawStationDataFrame to the OSD.
   *
   * @param frame the frame to store
   */
  @Override
  public void storeRawStationDataFrame(RawStationDataFrame frame) throws Exception {
    Validate.notNull(frame);
    FileWriter fileWriter = null;
    try {
      if (this.fsOutputDirectory != null) {
        // Write to disk.

        String json = jsonMapper.writeValueAsString(frame);
        String fileName = frame.getStationId() + "-" + System.nanoTime() + ".json";
        File file = new File(this.fsOutputDirectory + fileName);
        fileWriter = new FileWriter(file);
        fileWriter.write(json);
        fileWriter.close();
        fileWriter = null;

        //Now change permissions so we can rsync them
        file.setReadable(true, false);
        file.setWritable(true, false);

        //Generate name for manifest file, to avoid overwriting
        String manifestName =
            this.fsOutputDirectory + System.nanoTime() + "-" + generator.generate(5)
                + "-manifest.inv";
        //Add json to manifest file
        File manifest = new File(manifestName);
        manifest.setWritable(false, false);
        boolean successfulManifestCreation = false;
        successfulManifestCreation = manifest.exists() || manifest.createNewFile();

        if (successfulManifestCreation) {
          FileOutputStream manifestOutputStream = new FileOutputStream(manifest, true);
          manifestOutputStream.write((fileName + '\n').getBytes());
          manifest.setReadable(true, false);
          manifest.setWritable(true, false);
          manifestOutputStream.close();
        } else {
          logger.error("Could not find or create manifest.inv");
        }
      }
    } catch (Exception e) {
      logger.error("Raw station data frame could not be written to the output directory.", e);
    } finally {
      if (fileWriter != null) {
        try {
          fileWriter.close();
        } catch (IOException ioex) {
        }
      }
    }

    final long startTime = System.currentTimeMillis();
    handleResponse(postMsgPack(List.of(frame), STORE_RAW_STATION_DATA_FRAME_URL),
            "frame for station " + frame.getStationId()
            + " starting at " + frame.getPayloadDataStartTime());
    final long endTime = System.currentTimeMillis();
    logger.info(String.format("Stored raw station data frame in %d ms",
        endTime - startTime));
  }

  /**
   * Retrieves a Station by it's name.
   *
   * @param stationName the name of the station
   * @return the Station, or null if it cannot be found.
   */
  @Override
  public Optional<UUID> getStationId(String stationName) throws Exception {
    Validate.notEmpty(stationName);
    // look for the station ID in cache.
    final UUID cachedId = this.stationIdCache.get(stationName);
    if (cachedId != null) {
      logger.info("Returned cached id " + cachedId + " for station " + stationName);
      return Optional.of(cachedId);
    }
    // see if this station has already been queried for and not found; don't query for it again if so.
    if (this.unknownStations.contains(stationName)) {
      logger.info("station " + stationName + " is unknown, not querying for again");
      return Optional.empty();
    }
    final Station sta = getByName(GET_STATION_BY_NAME_URL, stationName, Station.class);
    if (sta == null) {
      logger.info("Queried for station " + stationName + ", not found; will not query again");
      this.unknownStations.add(stationName);
      return Optional.empty();
    }
    final UUID id = sta.getId();
    logger.info("Found station " + stationName + " to have id " + id);
    this.stationIdCache.put(stationName, id);
    return Optional.of(id);
  }

  /**
   * Retrieve a Channel ID from the OSD.
   *
   * @param siteName name of site
   * @param channelName name of channel
   * @return Channel ID as UUID, or null if it cannot be found.
   */
  @Override
  public Optional<UUID> getChannelId(String siteName, String channelName) throws Exception {
    Validate.notEmpty(siteName);
    Validate.notEmpty(channelName);
    // look for the channel ID in cache.
    final ChannelInfo ci = new ChannelInfo(siteName, channelName);
    final UUID cachedId = this.channelIdCache.get(ci);
    if (cachedId != null) {
      logger.info(String.format(
          "Returning cached id %s for query of channel %s/%s",
          cachedId, ci.siteName, ci.channelName));
      return Optional.of(cachedId);
    }
    // see if the channel has already been queried for and not found.
    if (this.unknownChannels.contains(ci)) {
      logger.info(String.format("channel %s/%s is unknown, not querying for again",
          ci.siteName, ci.channelName));
      return Optional.empty();
    }
    final Site site = getByName(GET_SITE_BY_NAME_URL, siteName, Site.class);
    if (site == null) {
      logger.info(
          "Queried for site " + siteName + "; not found; will not query again");
      this.unknownChannels.add(ci);
      return Optional.empty();
    }
    final Optional<Channel> chan = site.getChannels().stream()
        .filter(c -> c.getName().equals(siteName + "/" + channelName))
        .findFirst();
    if (!chan.isPresent()) {
      logger.info("Site does not have a channel named " + channelName + ": " + site);
      return Optional.empty();
    }
    final UUID id = chan.get().getId();
    logger.info(String.format(
        "Queried for channel %s/%s, got back id %s",
        ci.siteName, ci.channelName, id));
    this.channelIdCache.put(ci, id);
    return Optional.of(id);
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
        .body(msgpackMapper.writeValueAsBytes(obj));
    return body.asString();
  }

  private static <T> T getByName(String url,
      String name, Class<T> responseType) throws UnirestException, IOException {

    HttpResponse<String> response = Unirest.get(url)
        .header("Accept", "application/json")
        .routeParam("name", name)
        .asString();
    final String responseBody = response.getBody();
    if (response.getStatus() != 200) {
      logger.error("Error response from COI service url " + url
          + " with name; response from server: " + responseBody);
      return null;
    }
    return jsonMapper.readValue(responseBody, responseType);
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

  private static class ChannelInfo {

    public final String siteName, channelName;

    public ChannelInfo(String siteName, String channelName) {
      this.siteName = siteName;
      this.channelName = channelName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ChannelInfo that = (ChannelInfo) o;

      if (siteName != null ? !siteName.equals(that.siteName) : that.siteName != null) {
        return false;
      }
      return channelName != null ? channelName.equals(that.channelName)
          : that.channelName == null;
    }

    @Override
    public int hashCode() {
      int result = siteName != null ? siteName.hashCode() : 0;
      result = 31 * result + (channelName != null ? channelName.hashCode() : 0);
      return result;
    }
  }
}
