package gms.dataacquisition.stationreceiver.cd11.connman.configuration;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11ConnectionManagerConfig {

  private static Logger logger = LoggerFactory.getLogger(Cd11ConnectionManagerConfig.class);

  public static final String DEFAULT_RESPONDER_NAME = "TEST";
  public static final String DEFAULT_RESPONDER_TYPE = "IDC";
  public static final String DEFAULT_SERVICE_TYPE = "TCP";
  public static final String DEFAULT_FRAME_CREATOR = "TEST";
  public static final String DEFAULT_FRAME_DESTINATION = "0";

  public final String connectionManagerIpAddress;
  public final int connectionManagerPort;
  public final String dataProviderIpAddress; // TODO: In the future, this should come from the OSD, on a per-station basis!
  public final String responderName;
  public final String responderType;
  public final String serviceType;
  public final String frameCreator;
  public final String frameDestination;

  /**
   * Builds the connection manager configuration manually.
   *
   * @param connectionManagerIpAddress IP address on the local machine to bind to.
   * @param connectionManagerPort Port number on the local machine to bind to.
   * @param responderName Responder Name for this Connection Manager (CD 1.1 Connection Response
   * Frame).
   * @param responderType IDC, IMS, etc. (CD 1.1 Connection Request/Response Frames).
   * @param serviceType TCP or UDP (CD 1.1 Connection Request/Response Frames).
   * @param frameCreator Identifier of the creator of the frame (CD 1.1 Header Frame).
   * @param frameDestination Identifier of the destination fo the frame (CD 1.1 Header Frame).
   * @throws IllegalArgumentException Thrown when invalid configuration data is received.
   */
  private Cd11ConnectionManagerConfig(
      String connectionManagerIpAddress, int connectionManagerPort,
      String dataProviderIpAddress, // TODO: In the future, this should come from the OSD, on a per-station basis!
      String responderName, String responderType, String serviceType,
      String frameCreator, String frameDestination) {
    this.connectionManagerIpAddress = connectionManagerIpAddress;
    this.connectionManagerPort = connectionManagerPort;
    this.dataProviderIpAddress = dataProviderIpAddress; // TODO: In the future, this should come from the OSD, on a per-station basis!
    this.responderName = responderName;
    this.responderType = responderType;
    this.serviceType = serviceType;
    this.frameCreator = frameCreator;
    this.frameDestination = frameDestination;
  }

  /**
   * Obtains an instance of {@link Builder}
   *
   * @param connectionManagerIpAddress The "Well Known" IP Address of the Connection Manager.
   * @param connectionManagerPort The "Well Known" port number of the Connection Manager.
   * @return a Builder, not null
   */
  public static Builder builder(
      String connectionManagerIpAddress, int connectionManagerPort, String dataProviderIpAddress
      // TODO: In the future, this should come from the OSD, on a per-station basis!
  ) {
    return new Builder(connectionManagerIpAddress, connectionManagerPort,
        dataProviderIpAddress
        // TODO: In the future, this should come from the OSD, on a per-station basis!
    );
  }

  /**
   * Constructs instances of {@link Cd11ConnectionManagerConfig} following the builder pattern
   */
  public static class Builder {

    private String connectionManagerIpAddress;
    private int connectionManagerPort;
    private String dataProviderIpAddress; // TODO: In the future, this should come from the OSD, on a per-station basis!
    private String responderName;
    private String responderType;
    private String serviceType;
    private String frameCreator;
    private String frameDestination;

    /**
     * Builder.
     *
     * @param connectionManagerIpAddress The "Well Known" IP Address of the Connection Manager.
     * @param connectionManagerPort The "Well Known" port number of the Connection Manager.
     */
    private Builder(
        String connectionManagerIpAddress, int connectionManagerPort,
        String dataProviderIpAddress) {
      this.connectionManagerIpAddress = connectionManagerIpAddress;
      this.connectionManagerPort = connectionManagerPort;
      this.dataProviderIpAddress = dataProviderIpAddress; // TODO: In the future, this should come from the OSD, on a per-station basis!
      this.responderName = DEFAULT_RESPONDER_NAME;
      this.responderType = DEFAULT_RESPONDER_TYPE;
      this.serviceType = DEFAULT_SERVICE_TYPE;
      this.frameCreator = DEFAULT_FRAME_CREATOR;
      this.frameDestination = DEFAULT_FRAME_DESTINATION;
    }

    public Cd11ConnectionManagerConfig build() {
      Cd11Validator.validIpAddress(connectionManagerIpAddress);
      Cd11Validator.validNonZeroPortNumber(connectionManagerPort);
      Cd11Validator.validIpAddress(dataProviderIpAddress); // TODO: In the future, this should come from the OSD, on a per-station basis!
      Cd11Validator.validFrameCreator(responderName);
      Cd11Validator.validStationOrResponderType(responderType);
      Cd11Validator.validServiceType(serviceType);
      Cd11Validator.validFrameCreator(frameCreator);
      Cd11Validator.validFrameDestination(frameDestination);

      return new Cd11ConnectionManagerConfig(
          connectionManagerIpAddress, connectionManagerPort,
          dataProviderIpAddress,
          // TODO: In the future, this should come from the OSD, on a per-station basis!
          responderName, responderType, serviceType,
          frameCreator, frameDestination);
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
