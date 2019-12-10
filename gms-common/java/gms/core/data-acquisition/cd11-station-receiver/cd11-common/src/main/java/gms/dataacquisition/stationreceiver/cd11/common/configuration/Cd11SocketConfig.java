package gms.dataacquisition.stationreceiver.cd11.common.configuration;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;
import org.apache.commons.lang3.Validate;

public class Cd11SocketConfig {

  public static final String DEFAULT_STATION_OR_RESPONDER_NAME = "H04N";
  public static final String DEFAULT_STATION_OR_RESPONDER_TYPE = "IDC";
  public static final String DEFAULT_SERVICE_TYPE = "TCP";
  public static final String DEFAULT_FRAME_CREATOR = "TEST";
  public static final String DEFAULT_FRAME_DESTINATION = "0";
  public static final short DEFAULT_PROTOCOL_MAJOR_VERSION = 1;
  public static final short DEFAULT_PROTOCOL_MINOR_VERSION = 1;
  public static final int DEFAULT_AUTHENTICATION_KEY_IDENTIFIER = 0;

  public final String stationOrResponderName;
  public final String stationOrResponderType;
  public final String serviceType;
  public final String frameCreator;
  public final String frameDestination;
  public final int authenticationKeyIdentifier;
  public final short protocolMajorVersion;
  public final short protocolMinorVersion;

  /**
   * CD 1.1 Socket used to communicate with any CD 1.1 protocol component (Connection Manager, Data
   * Provider, Data Consumer).
   *
   * @param stationOrResponderName Name of the station or responder that this Cd11Socket represents
   * (used to create CD 1.1 Connection Request/Response Frames).
   * @param stationOrResponderType IDC, IMS, etc. (CD 1.1 Connection Request/Response Frames).
   * @param serviceType TCP or UDP (CD 1.1 Connection Request/Response Frames).
   * @param frameCreator Identifier of the creator of the frame (CD 1.1 Header Frame).
   * @param frameDestination Identifier of the destination fo the frame (CD 1.1 Header Frame).
   * @param authenticationKeyIdentifier Identifier for public key used for authentication (CD 1.1
   * Footer Frame).
   * @param protocolMajorVersion Major version number of this protocol (CD 1.1 Connection
   * Request/Response Frames).
   * @param protocolMinorVersion Minor version number of this protocol (CD 1.1 Connection
   * Request/Response Frames).
   */
  private Cd11SocketConfig(
      String stationOrResponderName, String stationOrResponderType, String serviceType,
      String frameCreator, String frameDestination,
      int authenticationKeyIdentifier,
      short protocolMajorVersion, short protocolMinorVersion) {
    this.stationOrResponderName = stationOrResponderName;
    this.stationOrResponderType = stationOrResponderType;
    this.serviceType = serviceType;
    this.frameCreator = frameCreator;
    this.frameDestination = frameDestination;
    this.authenticationKeyIdentifier = authenticationKeyIdentifier;
    this.protocolMajorVersion = protocolMajorVersion;
    this.protocolMinorVersion = protocolMinorVersion;
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
   * Constructs instances of {@link Cd11SocketConfig} following the builder pattern
   */
  public static class Builder {

    private String stationOrResponderName;
    private String stationOrResponderType;
    private String serviceType;
    private String frameCreator;
    private String frameDestination;
    private int authenticationKeyIdentifier;
    private short protocolMajorVersion;
    private short protocolMinorVersion;

    private Builder() {
      this.stationOrResponderName = DEFAULT_STATION_OR_RESPONDER_NAME;
      this.stationOrResponderType = DEFAULT_STATION_OR_RESPONDER_TYPE;
      this.serviceType = DEFAULT_SERVICE_TYPE;
      this.frameCreator = DEFAULT_FRAME_CREATOR;
      this.frameDestination = DEFAULT_FRAME_DESTINATION;
      this.authenticationKeyIdentifier = DEFAULT_AUTHENTICATION_KEY_IDENTIFIER;
      this.protocolMajorVersion = DEFAULT_PROTOCOL_MAJOR_VERSION;
      this.protocolMinorVersion = DEFAULT_PROTOCOL_MINOR_VERSION;
    }

    public Cd11SocketConfig build() {
      Cd11Validator.validStationOrResponderName(stationOrResponderName);
      Cd11Validator.validStationOrResponderType(stationOrResponderType);
      Cd11Validator.validServiceType(serviceType);
      Cd11Validator.validFrameCreator(frameCreator);
      Cd11Validator.validFrameDestination(frameDestination);
      Validate.isTrue(protocolMajorVersion >= 0, "Invalid major version number.");
      Validate.isTrue(protocolMinorVersion >= 0, "Invalid minor version number.");

      return new Cd11SocketConfig(
          stationOrResponderName, stationOrResponderType, serviceType,
          frameCreator, frameDestination,
          authenticationKeyIdentifier,
          protocolMajorVersion, protocolMinorVersion);
    }

    /**
     * The name of the station or responder as specified in the CD 1.1 Protocol.
     *
     * @param value Station or responder name.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setStationOrResponderName(String value) {
      this.stationOrResponderName = value;
      return this;
    }

    /**
     * Station or responder type as specified in the CD 1.1 Protocol: IMS, IDC, etc.
     *
     * @param value Responder type.
     * @return Configuration built from this {@link Builder}, not null
     */
    public Builder setStationOrResponderType(String value) {
      this.stationOrResponderType = value;
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
  }
}
