package gms.dataacquisition.stationreceiver.cd11.dataman.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11GapList;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;
import gms.dataacquisition.stationreceiver.cd11.common.GapList;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11DataConsumerConfig {

  private static Logger logger = LoggerFactory.getLogger(Cd11DataConsumerConfig.class);

  public static final String DEFAULT_DATA_CONSUMER_IP_ADDRESS = "0.0.0.0";
  public static final String DEFAULT_EXPECTED_DATA_PROVIDER_IP_ADDRESS = "127.0.0.1";

  public static final String DEFAULT_FS_OUTPUT_DIRECTORY = null;
  public static final String DEFAULT_THREAD_NAME = "CD 1.1 Data Consumer";
  public static final String DEFAULT_RESPONDER_NAME = "DC";
  public static final String DEFAULT_RESPONDER_TYPE = "IDC";
  public static final String DEFAULT_SERVICE_TYPE = "TCP";
  public static final String DEFAULT_FRAME_CREATOR = "TEST";
  public static final String DEFAULT_FRAME_DESTINATION = "0";
  public static final short DEFAULT_PROTOCOL_MAJOR_VERSION = 1;
  public static final short DEFAULT_PROTOCOL_MINOR_VERSION = 1;
  public static final int DEFAULT_AUTHENTICATION_KEY_IDENTIFIER = 0;
  public static final long DEFAULT_CONNECTION_EXPIRED_TIME_LIMIT_SEC = 120; // 2 minutes.
  public static final long DEFAULT_DATA_FRAME_SENDING_INTERVAL_MS = 500; // 500 milliseconds.
  public static final long DEFAULT_STORE_GAP_STATE_INTERVAL_MINUTES = 5; // 5 minutes.
  public static final int DEFAULT_GAP_EXPIRATION_IN_DAYS = -1; // Never expire.

  public final String dataConsumerIpAddress;
  public final int dataConsumerPort;
  public final String expectedDataProviderIpAddress;
  public final String dataProviderStationName;
  public final UUID osdStationId;

  public final String fsOutputDirectory;
  public final String threadName;
  public final String responderName;
  public final String responderType;
  public final String serviceType;
  public final String frameCreator;
  public final String frameDestination;
  public final short protocolMajorVersion;
  public final short protocolMinorVersion;
  public final int authenticationKeyIdentifier;
  public final long connectionExpiredTimeLimitSec;
  public final long dataFrameSendingIntervalMs;
  public final long storeGapStateIntervalMinutes;
  public final int gapExpirationInDays;

  private static final String gapStoragePath = "shared-volume/gaps/";
  private static final ObjectMapper objectMapper;
  
  static {
    // Ensure that the fake gap storage path exists.
    File gapsDir = new File(gapStoragePath);
    if (!gapsDir.exists()) {
      gapsDir.mkdirs();
    }
    
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  /**
   * This method is meant to be called when constructing the configuration object manually.
   *
   * @param dataConsumerIpAddress IP address on the local machine to bind to.
   * @param dataConsumerPort Port number on the local machine to bind to.
   * @param expectedDataProviderIpAddress The IP Address of the Data Provider that is expected to
   * connect to this Data Consumer.
   * @param responderName Name of the responder that this machine represents (CD 1.1 Connection
   * Request/Response Frames).
   * @param responderType IDC, IMS, etc. (CD 1.1 Connection Request/Response Frames).
   * @param serviceType TCP or UDP (CD 1.1 Connection Request/Response Frames).
   * @param frameCreator Identifier of the creator of the frame (CD 1.1 Header Frame).
   * @param frameDestination Identifier of the destination fo the frame (CD 1.1 Header Frame).
   * @param protocolMajorVersion Major version number of this protocol (CD 1.1 Connection
   * Request/Response Frames).
   * @param protocolMinorVersion Minor version number of this protocol (CD 1.1 Connection
   * Request/Response Frames).
   * @param authenticationKeyIdentifier Identifier for public key used for authentication (CD 1.1
   * Footer Frame).
   * @param storeGapStateIntervalMinutes Frequency of storing the gap state to the OSD (in minutes).
   * @param gapExpirationInDays The number of days before a gap expires.
   */
  private Cd11DataConsumerConfig(
      String dataConsumerIpAddress, int dataConsumerPort,
      String expectedDataProviderIpAddress, String dataProviderStationName,
      UUID osdStationId, String fsOutputDirectory, String threadName,
      String responderName, String responderType, String serviceType,
      String frameCreator, String frameDestination,
      short protocolMajorVersion, short protocolMinorVersion,
      int authenticationKeyIdentifier,
      long connectionExpiredTimeLimitSec,
      long dataFrameSendingIntervalMs,
      long storeGapStateIntervalMinutes,
      int gapExpirationInDays) {
    // Set the properties.
    this.dataConsumerIpAddress = dataConsumerIpAddress;
    this.dataConsumerPort = dataConsumerPort;
    this.expectedDataProviderIpAddress = expectedDataProviderIpAddress;
    this.dataProviderStationName = dataProviderStationName;
    this.osdStationId = osdStationId;
    this.fsOutputDirectory = fsOutputDirectory;
    this.threadName = threadName;
    this.responderName = responderName;
    this.responderType = responderType;
    this.serviceType = serviceType;
    this.frameCreator = frameCreator;
    this.frameDestination = frameDestination;
    this.protocolMajorVersion = protocolMajorVersion;
    this.protocolMinorVersion = protocolMinorVersion;
    this.authenticationKeyIdentifier = authenticationKeyIdentifier;
    this.connectionExpiredTimeLimitSec = connectionExpiredTimeLimitSec;
    this.dataFrameSendingIntervalMs = dataFrameSendingIntervalMs;
    this.storeGapStateIntervalMinutes = storeGapStateIntervalMinutes;
    this.gapExpirationInDays = gapExpirationInDays;
  }

  /**
   * Obtains an instance of {@link Builder}
   *
   * @return a Builder, not null
   */
  public static Builder builder(int localPort, UUID stationId, String dataProviderStationName) {
    return new Builder(localPort, stationId, dataProviderStationName);
  }


  /**
   * Constructs instances of {@link Cd11DataConsumerConfig} following the builder pattern
   */
  public static class Builder {

    private String dataConsumerIpAddress;
    private int dataConsumerPort;
    private UUID osdStationId;
    private String expectedDataProviderIpAddress;
    private String dataProviderStationName;

    private String fsOutputDirectory;
    private String threadName;
    private String responderName;
    private String responderType;
    private String serviceType;
    private String frameCreator;
    private String frameDestination;
    private short protocolMajorVersion;
    private short protocolMinorVersion;
    private int authenticationKeyIdentifier;
    private long connectionExpiredTimeLimitSec;
    private long dataFrameSendingIntervalMs;
    private long storeGapStateIntervalMinutes;
    private int gapExpirationInDays;

    private Builder(int dataConsumerPort, UUID osdStationId, String dataProviderStationName) {
      // Set default values.
      this.dataConsumerIpAddress = DEFAULT_DATA_CONSUMER_IP_ADDRESS;
      this.dataConsumerPort = dataConsumerPort;
      this.osdStationId = osdStationId;
      this.expectedDataProviderIpAddress = DEFAULT_EXPECTED_DATA_PROVIDER_IP_ADDRESS;
      this.dataProviderStationName = dataProviderStationName;

      this.fsOutputDirectory = DEFAULT_FS_OUTPUT_DIRECTORY;
      this.threadName = DEFAULT_THREAD_NAME;
      this.responderName = DEFAULT_RESPONDER_NAME;
      this.responderType = DEFAULT_RESPONDER_TYPE;
      this.serviceType = DEFAULT_SERVICE_TYPE;
      this.frameCreator = DEFAULT_FRAME_CREATOR;
      this.frameDestination = DEFAULT_FRAME_DESTINATION;
      this.protocolMajorVersion = DEFAULT_PROTOCOL_MAJOR_VERSION;
      this.protocolMinorVersion = DEFAULT_PROTOCOL_MINOR_VERSION;
      this.authenticationKeyIdentifier = DEFAULT_AUTHENTICATION_KEY_IDENTIFIER;
      this.connectionExpiredTimeLimitSec = DEFAULT_CONNECTION_EXPIRED_TIME_LIMIT_SEC;
      this.dataFrameSendingIntervalMs = DEFAULT_DATA_FRAME_SENDING_INTERVAL_MS;
      this.storeGapStateIntervalMinutes = DEFAULT_STORE_GAP_STATE_INTERVAL_MINUTES;
      this.gapExpirationInDays = DEFAULT_GAP_EXPIRATION_IN_DAYS;
    }

    /**
     * Construct the {@link Cd11DataConsumerConfig}
     *
     * @return Configuration built from this {@link Builder}, not null
     * @throws IllegalArgumentException if parameter values are invalid
     */
    public Cd11DataConsumerConfig build() {
      // Validate dataConsumerIpAddress
      Cd11Validator.validIpAddress(dataConsumerIpAddress);

      // Validate dataConsumerPort
      Cd11Validator.validNonZeroPortNumber(dataConsumerPort);

      // Validate expectedDataProviderIpAddress
      Cd11Validator.validIpAddress(expectedDataProviderIpAddress);

      // Validate dataProviderStationName
      Validate.notBlank(dataProviderStationName);
      Validate.isTrue(dataProviderStationName.length() <= 8);

      // Validate osdStationID
      Validate.notNull(osdStationId);

      // Validate fsOutputDirectory
      if (fsOutputDirectory != null) {
        Validate.notBlank(fsOutputDirectory);

        // Check that the directory exists.
        File f = new File(fsOutputDirectory);
        Validate.isTrue(f.exists(), "Directory path provided does not exist.");
        Validate.isTrue(f.isDirectory(), "Path provided does not map to a directory.");

        // Ensure that the directory path ends with a slash.
        if (!fsOutputDirectory.endsWith("/")) {
          fsOutputDirectory += "/";
        }
      }

      // Validate threadName
      Validate.notBlank(threadName);

      // Validate stationName
      Cd11Validator.validStationOrResponderName(responderName);

      // Validate stationType
      Cd11Validator.validStationOrResponderType(responderType);

      // Validate serviceType
      Cd11Validator.validServiceType(serviceType);

      // Validate frameCreator
      Cd11Validator.validFrameCreator(frameCreator);

      // Validate frameDestination
      Cd11Validator.validFrameDestination(frameDestination);

      // Validate protocolMajorVersion
      Validate.isTrue(
          protocolMajorVersion >= 0,
          "Invalid value assigned to protocolMajorVersion.");

      // Validate protocolMajorVersion
      Validate.isTrue(
          protocolMinorVersion >= 0,
          "Invalid value assigned to protocolMinorVersion.");

      // Validate connectionExpiredTimeLimitSec
      Validate.isTrue(
          connectionExpiredTimeLimitSec > 0,
          "Value of connectionExpiredTimeLimitSec must be greater than 1.");

      // Validate dataFrameSendingIntervalMs
      Validate.isTrue(
          dataFrameSendingIntervalMs >= 0,
          "Invalid value assigned to dataFrameSendingIntervalMs.");

      // Validate the storeGapStateIntervalMinutes
      Validate.isTrue(storeGapStateIntervalMinutes > 0);

      return new Cd11DataConsumerConfig(
          dataConsumerIpAddress, dataConsumerPort,
          expectedDataProviderIpAddress, dataProviderStationName,
          osdStationId, fsOutputDirectory, threadName,
          responderName, responderType, serviceType,
          frameCreator, frameDestination,
          protocolMajorVersion, protocolMinorVersion,
          authenticationKeyIdentifier,
          connectionExpiredTimeLimitSec,
          dataFrameSendingIntervalMs,
          storeGapStateIntervalMinutes,
          gapExpirationInDays);
    }

    /**
     * The IP Address of the Data Consumer (default: 0.0.0.0).
     *
     * @param value IP address.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setDataConsumerIpAddress(String value) {
      this.dataConsumerIpAddress = value;
      return this;
    }

    /**
     * The IP Address of the Data Provider that is expected to connect to this Data Consumer (used
     * for connection verification only); (default: 127.0.0.1).
     *
     * @param value IP address.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setExpectedDataProviderIpAddress(String value) {
      this.expectedDataProviderIpAddress = value;
      return this;
    }

    /**
     * The directory path to write Raw Station Data Frame JSON flat-files.
     *
     * @param value Directory path.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setFsOutputDirectory(String value) {
      this.fsOutputDirectory = value;
      return this;
    }

    /**
     * A name for the CD 1.1 Data Consumer thread (default: "CD 1.1 Data Consumer").
     *
     * @param value Thread name.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setThreadName(String value) {
      this.threadName = value;
      return this;
    }

    /**
     * The name of this responder (as specified by the CD 1.1 Protocol).
     *
     * @param value Responder name.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setResponderName(String value) {
      this.responderName = value;
      return this;
    }

    /**
     * IMS, IDC, etc.
     *
     * @param value The responder type (as specified by the CD 1.1 Protocol).
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setResponderType(String value) {
      this.responderType = value;
      return this;
    }

    /**
     * TCP or UDP (default: TCP).
     *
     * @param value TCP or UDP.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setServiceType(String value) {
      this.serviceType = value;
      return this;
    }

    /**
     * Name of the frame creator (as specified by the CD 1.1 Protocol).
     *
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setFrameCreator(String value) {
      this.frameCreator = value;
      return this;
    }

    /**
     * IMS, IDC, 0, etc (default: 0). (See the CD 1.1 Protocol for details)
     *
     * @param value Frame destination.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setFrameDestination(String value) {
      this.frameDestination = value;
      return this;
    }

    /**
     * The major version number of the CD protocol (default: 1).
     *
     * @param value Major version number.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setProtocolMajorVersion(short value) {
      this.protocolMajorVersion = value;
      return this;
    }

    /**
     * The minor version number of the CD protocol (default: 1).
     *
     * @param value Minor version number.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setProtocolMinorVersion(short value) {
      this.protocolMinorVersion = value;
      return this;
    }

    /**
     * Auth key identifier for the CD 1.1 frame trailers.
     *
     * @param value Authentication key identifier.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setAuthenticationKeyIdentifier(int value) {
      this.authenticationKeyIdentifier = value;
      return this;
    }

    /**
     * Maximum amount of time to wait for a CD 1.1 frame to arrive before giving up, in seconds
     * (default: 120).
     *
     * @param value Seconds.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setConnectionExpiredTimeLimitSec(long value) {
      this.connectionExpiredTimeLimitSec = value;
      return this;
    }

    /**
     * The amount of time to pause before sending one data frame after another, in milliseconds
     * (default: 500).
     *
     * @param value Milliseconds.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setDataFrameSendingIntervalMs(long value) {
      this.dataFrameSendingIntervalMs = value;
      return this;
    }

    public Builder setStoreGapStateIntervalMinutes(long value) {
      this.storeGapStateIntervalMinutes = value;
      return this;
    }

    public Builder setGapExpirationInDays(int value) {
      this.gapExpirationInDays = value;
      return this;
    }
  }
  
  //GapList loaders/writers
  public static Cd11GapList loadGapState(String stationName) {
    Path path = Paths.get(gapStoragePath + stationName + ".json");
    if (Files.exists(path)) {
      try {
        String contents = new String(Files.readAllBytes(path));
        return new Cd11GapList(objectMapper.readValue(contents, GapList.class));
      } catch (IOException e) {
        logger.error("Error deserializing GapList", e);
        return new Cd11GapList();
      }
    } else {
      return new Cd11GapList();
    }
  }

  public static void persistGapState(String stationName, GapList gapList)
      throws IOException {
    String path = gapStoragePath + stationName + ".json";
    try (PrintWriter out = new PrintWriter(path)) {
      // Set file permissions.
      File file = new File(path);
      file.setReadable(true, false);
      file.setWritable(true, false);
      file.setExecutable(false, false);

      objectMapper.writeValue(file, gapList);
    }
  }

  public static void clearGapState(String stationName) throws IOException {
    String path = gapStoragePath + stationName + ".json";
    File file = new File(path);
    if (!file.delete()) {
      logger.error("Gap State file could not be deleted: " + path);
    }
  }
}
