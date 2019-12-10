package gms.dataacquisition.stationreceiver.cd11.connman.configuration;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;
import gms.dataacquisition.stationreceiver.cd11.connman.Cd11Connection;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11ConnectionConfig {

  private static Logger logger = LoggerFactory.getLogger(Cd11ConnectionConfig.class);

  public static final long DEFAULT_SOCKET_READ_TIMEOUT_MS = 10000; // 10 seconds
  public static final String DEFAULT_THREAD_NAME = Cd11Connection.class.getName();
  public static final String DEFAULT_RESPONDER_NAME = "TEST";
  public static final String DEFAULT_RESPONDER_TYPE = "IDC";
  public static final String DEFAULT_SERVICE_TYPE = "TCP";
  public static final String DEFAULT_FRAME_CREATOR = "TEST";
  public static final String DEFAULT_FRAME_DESTINATION = "0";

  public final long socketReadTimeoutMs;
  public final String threadName;
  public final String responderName;
  public final String responderType;
  public final String serviceType;
  public final String frameCreator;
  public final String frameDestination;

  /**
   * Builds the connection manager configuration manually.
   *
   * @param responderName Name of the station that this machine represents (CD 1.1 Connection
   * Request/Response Frames).
   * @param responderType IDC, IMS, etc. (CD 1.1 Connection Request/Response Frames).
   * @param serviceType TCP or UDP (CD 1.1 Connection Request/Response Frames).
   * @param frameCreator Identifier of the creator of the frame (CD 1.1 Header Frame).
   * @param frameDestination Identifier of the destination fo the frame (CD 1.1 Header Frame).
   * @throws IllegalArgumentException Thrown when invalid configuration data is received.
   */
  private Cd11ConnectionConfig(
      long socketReadTimeoutMs, String threadName,
      String responderName, String responderType, String serviceType,
      String frameCreator, String frameDestination) {
    this.socketReadTimeoutMs = socketReadTimeoutMs;
    this.threadName = threadName;
    this.responderName = responderName;
    this.responderType = responderType;
    this.serviceType = serviceType;
    this.frameCreator = frameCreator;
    this.frameDestination = frameDestination;
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
   * Constructs instances of {@link Cd11ConnectionConfig} following the builder pattern
   */
  public static class Builder {

    private long socketReadTimeoutMs;
    private String threadName;
    private String responderName;
    private String responderType;
    private String serviceType;
    private String frameCreator;
    private String frameDestination;

    private Builder() {
      this.socketReadTimeoutMs = DEFAULT_SOCKET_READ_TIMEOUT_MS;
      this.threadName = DEFAULT_THREAD_NAME;
      this.responderName = DEFAULT_RESPONDER_NAME;
      this.responderType = DEFAULT_RESPONDER_TYPE;
      this.serviceType = DEFAULT_SERVICE_TYPE;
      this.frameCreator = DEFAULT_FRAME_CREATOR;
      this.frameDestination = DEFAULT_FRAME_DESTINATION;
    }

    public Cd11ConnectionConfig build() {
      Validate.isTrue(socketReadTimeoutMs > 0);
      Validate.notBlank(threadName);

      Cd11Validator.validStationOrResponderName(responderName);
      Cd11Validator.validStationOrResponderType(responderType);
      Cd11Validator.validServiceType(serviceType);
      Cd11Validator.validFrameCreator(frameCreator);
      Cd11Validator.validFrameDestination(frameDestination);

      return new Cd11ConnectionConfig(
          socketReadTimeoutMs, threadName,
          responderName, responderType, serviceType,
          frameCreator, frameDestination);
    }

    /**
     * The maximum amount of time that the Connection Manager should wait for a Data Provider to
     * send its Connection Request frame.
     *
     * @param value Milliseconds
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setSocketReadTimeoutMs(long value) {
      this.socketReadTimeoutMs = value;
      return this;
    }

    /**
     * The name of the connection handler thread.
     *
     * @param value Thread name.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setThreadName(String value) {
      this.threadName = value;
      return this;
    }

    /**
     * The name of the responder as specified in the CD 1.1 Protocol.
     *
     * @param value Responder name.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setResponderName(String value) {
      this.responderName = value;
      return this;
    }

    /**
     * Responder type as specified in the CD 1.1 Protocol: IMS, IDC, etc.
     *
     * @param value Responder type.
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
     * Name of the frame creator as specified in the CD 1.1 Protocol.
     *
     * @param value Frame creator.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setFrameCreator(String value) {
      this.frameCreator = value;
      return this;
    }

    /**
     * IMS, IDC, 0, etc (default: 0).
     *
     * @param value Frame destination.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setFrameDestination(String value) {
      this.frameDestination = value;
      return this;
    }
  }
}
