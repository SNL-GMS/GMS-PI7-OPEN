package gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11DataProviderConfig {

  private static Logger logger = LoggerFactory.getLogger(Cd11DataProviderConfig.class);

  public static final String DEFAULT_LOCAL_IP_ADDRESS = null;
  public static final int DEFAULT_LOCAL_PORT = 0;
  public static final String DEFAULT_CONNECTION_MANAGER_IP_ADDRESS = "127.0.0.1";
  public static final int DEFAULT_CONNECTION_MANAGER_PORT = 8041;
  public static final String DEFAULT_STATION_NAME = "H04N";
  public static final String DEFAULT_STATION_TYPE = "IDC";
  public static final String DEFAULT_SERVICE_TYPE = "TCP";
  public static final String DEFAULT_FRAME_CREATOR = "TEST";
  public static final String DEFAULT_FRAME_DESTINATION = "0";
  public static final short DEFAULT_PROTOCOL_MAJOR_VERSION = 1;
  public static final short DEFAULT_PROTOCOL_MINOR_VERSION = 1;
  public static final int DEFAULT_AUTHENTICATION_KEY_IDENTIFIER = 0;
  public static final long DEFAULT_MAX_SOCKET_CONNECTION_WAIT_TIME_MS = 30000; // 30 seconds.
  public static final long DEFAULT_CONNECTION_EXPIRED_TIME_LIMIT_SEC = 120; // 2 minutes.
  public static final long DEFAULT_DATA_FRAME_SENDING_INTERVAL_MS = 500; // 500 milliseconds.
  public static final String DEFAULT_CANNED_FRAME_PATH = null;


  public final String localIpAddress;
  public final int localPort;
  public final String connectionManagerIpAddress;
  public final int connectionManagerPort;
  public final String stationName;
  public final String stationType;
  public final String serviceType;
  public final String frameCreator;
  public final String frameDestination;
  public final short protocolMajorVersion;
  public final short protocolMinorVersion;
  public final int authenticationKeyIdentifier;
  public final long maxSocketConnectWaitTimeMs;
  public final long connectionExpiredTimeLimitSec;
  public final long dataFrameSendingIntervalMs;
  public final String cannedFramePath;

  /**
   * Constructor.
   *
   * @param localIpAddress IP address on the local machine to bind to.
   * @param localPort Port number on the local machine to bind to.
   * @param connectionManagerIpAddress Well Known IP Address of the remote connection manager.
   * @param connectionManagerPort Well Known port number of the remote connection manager.
   * @param stationName Name of the station that this machine represents (CD 1.1 Connection
   * Request/Response Frames).
   * @param stationType IDC, IMS, etc. (CD 1.1 Connection Request/Response Frames).
   * @param serviceType TCP or UDP (CD 1.1 Connection Request/Response Frames).
   * @param frameCreator Identifier of the creator of the frame (CD 1.1 Header Frame).
   * @param frameDestination Identifier of the destination fo the frame (CD 1.1 Header Frame).
   * @param protocolMajorVersion Major version number of this protocol (CD 1.1 Connection
   * Request/Response Frames).
   * @param protocolMinorVersion Minor version number of this protocol (CD 1.1 Connection
   * Request/Response Frames).
   * @param authenticationKeyIdentifier Identifier for public key used for authentication (CD 1.1
   * Footer Frame).
   *  @param authenticationKeyIdentifier Identifier for public key use
   */
  private Cd11DataProviderConfig(
      String localIpAddress, int localPort,
      String connectionManagerIpAddress, int connectionManagerPort,
      String stationName, String stationType, String serviceType,
      String frameCreator, String frameDestination,
      short protocolMajorVersion, short protocolMinorVersion,
      int authenticationKeyIdentifier,
      long maxSocketConnectWaitTimeMs,
      long connectionExpiredTimeLimitSec,
      long dataFrameSendingIntervalMs,
      String cannedFramePath)
      throws IllegalArgumentException {
    this.localIpAddress = localIpAddress;
    this.localPort = localPort;
    this.connectionManagerIpAddress = connectionManagerIpAddress;
    this.connectionManagerPort = connectionManagerPort;
    this.stationName = stationName;
    this.stationType = stationType;
    this.serviceType = serviceType;
    this.frameCreator = frameCreator;
    this.frameDestination = frameDestination;
    this.protocolMajorVersion = protocolMajorVersion;
    this.protocolMinorVersion = protocolMinorVersion;
    this.authenticationKeyIdentifier = authenticationKeyIdentifier;
    this.maxSocketConnectWaitTimeMs = maxSocketConnectWaitTimeMs;
    this.connectionExpiredTimeLimitSec = connectionExpiredTimeLimitSec;
    this.dataFrameSendingIntervalMs = dataFrameSendingIntervalMs;
    this.cannedFramePath = cannedFramePath;
  }

  /**
   * Obtains an instance of {@link Builder}
   *
   * @return a Builder, not null
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Constructs instances of {@link Cd11DataProviderConfig} following the builder pattern
   */
  public static class Builder {

    private String localIpAddress;
    private int localPort;
    private String connectionManagerIpAddress;
    private int connectionManagerPort;
    private String stationName;
    private String stationType;
    private String serviceType;
    private String frameCreator;
    private String frameDestination;
    private short protocolMajorVersion;
    private short protocolMinorVersion;
    private int authenticationKeyIdentifier;
    private long maxSocketConnectWaitTimeMs;
    private long connectionExpiredTimeLimitSec;
    private long dataFrameSendingIntervalMs;
    private String cannedFramePath;

    private Builder() {
      this.localIpAddress = DEFAULT_LOCAL_IP_ADDRESS;
      this.localPort = DEFAULT_LOCAL_PORT;
      this.connectionManagerIpAddress = DEFAULT_CONNECTION_MANAGER_IP_ADDRESS;
      this.connectionManagerPort = DEFAULT_CONNECTION_MANAGER_PORT;
      this.stationName = DEFAULT_STATION_NAME;
      this.stationType = DEFAULT_STATION_TYPE;
      this.serviceType = DEFAULT_SERVICE_TYPE;
      this.frameCreator = DEFAULT_FRAME_CREATOR;
      this.frameDestination = DEFAULT_FRAME_DESTINATION;
      this.protocolMajorVersion = DEFAULT_PROTOCOL_MAJOR_VERSION;
      this.protocolMinorVersion = DEFAULT_PROTOCOL_MINOR_VERSION;
      this.authenticationKeyIdentifier = DEFAULT_AUTHENTICATION_KEY_IDENTIFIER;
      this.maxSocketConnectWaitTimeMs = DEFAULT_MAX_SOCKET_CONNECTION_WAIT_TIME_MS;
      this.connectionExpiredTimeLimitSec = DEFAULT_CONNECTION_EXPIRED_TIME_LIMIT_SEC;
      this.dataFrameSendingIntervalMs = DEFAULT_DATA_FRAME_SENDING_INTERVAL_MS;
      this.cannedFramePath = DEFAULT_CANNED_FRAME_PATH;
    }

    /**
     * Construct the {@link Cd11DataProviderConfig}
     *
     * @return Configuration built from this {@link Builder}, not null
     * @throws IllegalArgumentException if invalid argument values are specified
     */
    public Cd11DataProviderConfig build() {
      // Parse and validate: localIpAddress
      if (localIpAddress != null) {
        Cd11Validator.validIpAddress(localIpAddress);
      }

      // Parse and validate: dataConsumerPort
      Cd11Validator.validPortNumber(localPort);

      // Parse and validate: connectionManagerIpAddress
      Validate.notBlank(connectionManagerIpAddress,
          "Required command-line argument -connectionManagerIpAddress is empty.");

      // Parse and validate: connectionManagerPort
      Cd11Validator.validNonZeroPortNumber(connectionManagerPort);

      // Parse and validate: stationName
      Validate.notBlank(stationName, "The stationName value is empty.");
      Validate.isTrue(
          stationName.length() <= 8,
          "Station name is too long (8 character maximum).");

      // Parse and validate: stationType
      Cd11Validator.validStationOrResponderType(stationType);

      // Parse and validate: serviceType
      Cd11Validator.validServiceType(serviceType);

      // Parse and validate: frameCreator
      Validate.notBlank(frameCreator);
      Validate.isTrue(
          frameCreator.length() <= 8,
          "Frame creator is too long (8 character maximum).");

      // Parse and validate: frameDestination
      Validate.notBlank(frameDestination);
      Validate.isTrue(
          frameDestination.length() <= 8,
          "Frame destination is too long (8 character maximum).");

      // Parse and validate: protocolMajorVersion
      Validate.isTrue(
          protocolMajorVersion >= 0,
          "Invalid value assigned to protocolMajorVersion.");

      // Parse and validate: protocolMinorVersion
      Validate.isTrue(
          protocolMinorVersion >= 0,
          "Invalid value assigned to protocolMinorVersion.");

      // Parse and validate: maxSocketConnectWaitTimeMs
      Validate.isTrue(
          maxSocketConnectWaitTimeMs >= 1,
          "Invalid value assigned to maxSocketConnectWaitTimeMs.");

      // Parse and validate: connectionExpiredTimeLimitSec
      Validate.isTrue(
          connectionExpiredTimeLimitSec >= 1,
          "Invalid value assigned to connectionExpiredTimeLimitSec.");

      // Parse and validate: dataFrameSendingIntervalMs
      Validate.isTrue(
          dataFrameSendingIntervalMs >= 1,
          "Invalid value assigned to dataFrameSendingIntervalMs.");

      return new Cd11DataProviderConfig(
          localIpAddress, localPort,
          connectionManagerIpAddress, connectionManagerPort,
          stationName, stationType, serviceType,
          frameCreator, frameDestination,
          protocolMajorVersion, protocolMinorVersion,
          authenticationKeyIdentifier,
          maxSocketConnectWaitTimeMs,
          connectionExpiredTimeLimitSec,
          dataFrameSendingIntervalMs,
          cannedFramePath);
    }

    /**
     * The IP Address of the Data Provider, or null for anyLocal address (default: null).
     *
     * @param value IP Address, or null for anyLocal address.
     * @return this {@link Builder}
     */
    public Builder setLocalIpAddress(String value) {
      this.localIpAddress = value;
      return this;
    }

    /**
     * The port number of the Data Provider, or 0 if using an ephemeral port (default: 0).
     *
     * @param value Port number, or 0 for ephemeral port.
     * @return this {@link Builder}
     */
    public Builder setLocalPort(int value) {
      this.localPort = value;
      return this;
    }

    /**
     * The "well known" IP Address of the Connection Manager.
     *
     * @param value IP Address.
     * @return this {@link Builder}
     */
    public Builder setConnectionManagerIpAddress(String value) {
      this.connectionManagerIpAddress = value;
      return this;
    }

    /**
     * The "well known" port number of the Connection Manager.
     *
     * @param value Port number (non-zero).
     * @return this {@link Builder}
     */
    public Builder setConnectionManagerPort(int value) {
      this.connectionManagerPort = value;
      return this;
    }

    /**
     * The name of the station (as specified in the CD 1.1 Protocol).
     *
     * @param value Station name.
     * @return this {@link Builder}
     */
    public Builder setStationName(String value) {
      this.stationName = value;
      return this;
    }

    /**
     * Station type as specified in the CD 1.1 Protocol: IMS, IDC, etc.
     *
     * @param value Station type.
     * @return this {@link Builder}
     */
    public Builder setStationType(String value) {
      this.stationType = value;
      return this;
    }

    /**
     * TCP or UDP (default: TCP).
     *
     * @param value Service type.
     * @return this {@link Builder}
     */
    public Builder setServiceType(String value) {
      this.serviceType = value;
      return this;
    }

    /**
     * Name of the frame creator (as specified in the CD 1.1 Protocol).
     *
     * @param value Frame creator.
     * @return this {@link Builder}
     */
    public Builder setFrameCreator(String value) {
      this.frameCreator = value;
      return this;
    }

    /**
     * IMS, IDC, 0, etc (default: 0).
     *
     * @param value Frame destination.
     * @return this {@link Builder}
     */
    public Builder setFrameDestination(String value) {
      this.frameDestination = value;
      return this;
    }

    /**
     * The major version number of the CD protocol (default: 1).
     *
     * @param value Protocol major version number.
     * @return this {@link Builder}
     */
    public Builder setProtocolMajorVersion(short value) {
      this.protocolMajorVersion = value;
      return this;
    }

    /**
     * The minor version number of the CD protocol (default: 1).
     *
     * @param value Protocol minor version number.
     * @return this {@link Builder}
     */
    public Builder setProtocolMinorVersion(short value) {
      this.protocolMinorVersion = value;
      return this;
    }

    /**
     * Auth key identifier for the CD 1.1 frame trailers.
     *
     * @param value Authentication key identifier.
     * @return this {@link Builder}
     */
    public Builder setAuthenticationKeyIdentifier(int value) {
      this.authenticationKeyIdentifier = value;
      return this;
    }

    /**
     * Maximum amount of time to wait for a socket to connect, in milliseconds (default: 30000).
     *
     * @param value Wait time in milliseconds.
     * @return this {@link Builder}
     */
    public Builder setMaxSocketConnectWaitTimeMs(long value) {
      this.maxSocketConnectWaitTimeMs = value;
      return this;
    }

    /**
     * Maximum amount of time to wait for a CD 1.1 frame to arrive before giving up, in seconds
     * (default: 120).
     *
     * @param value Wait time in seconds.
     * @return this {@link Builder}
     */
    public Builder setConnectionExpiredTimeLimitSec(long value) {
      this.connectionExpiredTimeLimitSec = value;
      return this;
    }

    /**
     * The amount of time to pause before sending one data frame after another, in milliseconds
     * (default: 500).
     *
     * @param value Pause time in milliseconds.
     * @return this {@link Builder}
     */
    public Builder setDataFrameSendingIntervalMs(long value) {
      this.dataFrameSendingIntervalMs = value;
      return this;
    }

    /**
     * The path to stored data frames
     * (default: null).
     *
     * @param value Path to stored data frames.
     * @return this {@link Builder}
     */
    public Builder setCannedFramePath (String value) {
      this.cannedFramePath = value;
      return this;
    }
  }
}
