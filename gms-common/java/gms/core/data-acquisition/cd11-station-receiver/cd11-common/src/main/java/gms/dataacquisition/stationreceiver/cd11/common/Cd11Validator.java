package gms.dataacquisition.stationreceiver.cd11.common;

import com.google.common.net.InetAddresses;
import java.util.Set;
import org.apache.commons.lang3.Validate;

public class Cd11Validator {

  public static final Set<String>
      KNOWN_STATION_TYPES = Set
      .of("IDC", "IMS"), // TODO: Determine if there are more station types...
      KNOWN_SERVICE_TYPES = Set.of("UDP", "TCP");

  /**
   * Validates a network port number.
   *
   * @param port Port number.
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static void validPortNumber(int port) throws IllegalArgumentException {
    Validate.inclusiveBetween(0, 65535, port, "Port number is out of range.");
  }

  /**
   * Validates a network port number, and does not accept 0 as valid.
   *
   * @param port Port number.
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static void validNonZeroPortNumber(int port) throws IllegalArgumentException {
    Validate.inclusiveBetween(1, 65535, port, "Port number is out of range.");
  }

  /**
   * Validates the Frame Creator value. (CD 1.1 Frame Header)
   *
   * @param frameCreator Frame Creator value.
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static String validFrameCreator(String frameCreator)
      throws IllegalArgumentException, NullPointerException {

    Validate.notEmpty(frameCreator);
    Validate.isTrue(frameCreator.length() <= 8);
    return frameCreator;
  }

  /**
   * Validates the Frame Destination value. (CD 1.1 Frame Header)
   *
   * @param frameDestination Frame Destination value (CD 1.1 Frame Header).
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static String validFrameDestination(String frameDestination)
      throws IllegalArgumentException, NullPointerException {

    //TODO: see if we need to test this. They send us empty frame destinations in ACKNACK frames
    //Validate.notEmpty(frameDestination);
    Validate.isTrue(frameDestination.length() <= 8);
    return frameDestination;
  }

  /**
   * Validates the Station Name / Responder Name value. (CD 1.1 Connection Request / Connection
   * Response frames)
   *
   * @param name Station Name or Responder Name.
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static String validStationOrResponderName(String name)
      throws IllegalArgumentException, NullPointerException {

    return ofMaxLength(name, 8);
  }

  public static String validStationOrResponderType(String name) {
    name = ofMaxLength(name, 4);
    Validate.isTrue(KNOWN_STATION_TYPES.contains(name.trim()),
        String.format("Unknown station or responder type %s (only know %s)",
            name, KNOWN_STATION_TYPES));
    return name;
  }

  public static String ofMaxLength(String s, int length) {
    Validate.notNull(s);
    Validate.isTrue(s.length() <= length,
        String.format("String is too long; max %d but was %d",
            length, s.length()));
    return s;
  }

  /**
   * Validates the Service Type value.
   *
   * @param serviceType Service Type.
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static String validServiceType(String serviceType)
      throws IllegalArgumentException, NullPointerException {

    serviceType = ofMaxLength(serviceType, 4);
    Validate.isTrue(!serviceType.equals("UDP"), "UDP is not yet implemented.");
    Validate.isTrue(KNOWN_SERVICE_TYPES.contains(serviceType),
        "Invalid Service Type value received.");
    return serviceType;
  }

  /**
   * Validates a network IP Address.
   *
   * @param ipAddress IP Address.
   * @return Integer representation of the IP Address.
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static int validIpAddress(String ipAddress)
      throws IllegalArgumentException, NullPointerException {

    Validate.notEmpty(ipAddress, "IP Address empty or null.");

    try {
      return InetAddresses.coerceToInteger(InetAddresses.forString(ipAddress));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid IP Address received.", e);
    }
  }

  public static int validIpAddress(int ipAddress) {
    try {
      InetAddresses.fromInteger(ipAddress);
      return ipAddress;
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid IP Address received: %1$s.", e);
    }
  }

  /**
   * Validates a sequence number which should be greater than or equal to 0. (CD 1.1 Acknack frame)
   *
   * @param sequenceNumber The sequence number.
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static long validSequenceNumber(long sequenceNumber) throws IllegalArgumentException {
    Validate.isTrue(sequenceNumber >= 0,
        "Sequence number is too low (must be greater than 0).");
    return sequenceNumber;

  }

  /**
   * Validates a "frame set acked" value. (CD 1.1 Acknack frame)
   *
   * @param frameSetAcked Full name of the frame set being acknowledged (for example, "SG7:0").
   * @throws IllegalArgumentException thrown when invalid data is received.
   */
  public static String validFrameSetAcked(String frameSetAcked) throws IllegalArgumentException {
    Validate.notEmpty(frameSetAcked);
    //Validate.isTrue(frameSetAcked.length() == frameSetAcked.trim().length(),
    //    "Frame Set Acked contains whitespace.");
    Validate.isTrue(frameSetAcked.length() <= 20,
        "Frame Set Acked is too long (20-byte maximum).");
    return frameSetAcked;
  }
}
