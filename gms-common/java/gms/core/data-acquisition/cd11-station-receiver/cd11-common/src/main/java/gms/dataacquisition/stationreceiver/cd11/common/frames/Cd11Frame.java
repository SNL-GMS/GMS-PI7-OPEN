package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.CRC64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.Validate;

/**
 * Base class for all CD 1.1 Frame classes.
 */
public abstract class Cd11Frame {

  public final FrameType frameType;
  private final byte[] rawNetworkBytes;
  private Cd11FrameHeader header = null;
  private Cd11FrameTrailer trailer = null;

  private static final Map<Class<?>, FrameType> frameTypeToClass = Collections
      .unmodifiableMap(Map.of(
          Cd11ConnectionRequestFrame.class, FrameType.CONNECTION_REQUEST,
          Cd11ConnectionResponseFrame.class, FrameType.CONNECTION_RESPONSE,
          // Option request/response frame omitted, no classes implemented for those
          Cd11DataFrame.class, FrameType.DATA,
          Cd11AcknackFrame.class, FrameType.ACKNACK,
          Cd11AlertFrame.class, FrameType.ALERT
          // Command request/response frame omitted, no classes implemented
      ));

  /**
   * Enumeration for each CD 1.1 frame type.
   */
  public enum FrameType {

    CONNECTION_REQUEST(1),
    CONNECTION_RESPONSE(2),
    OPTION_REQUEST(3),
    OPTION_RESPONSE(4),
    DATA(5),
    ACKNACK(6),
    ALERT(7),
    COMMAND_REQUEST(8),
    COMMAND_RESPONSE(9),
    CD_ONE_ENCAPSULATION(13),
    CUSTOM_RESET_FRAME(26);

    private final int value;

    FrameType(final int newValue) {
      value = newValue;
    }

    public int getValue() {
      return value;
    }

    public static FrameType fromInt(int value) throws IllegalArgumentException {
      switch (value) {
        case 1:
          return CONNECTION_REQUEST;
        case 2:
          return CONNECTION_RESPONSE;
        case 3:
          return OPTION_REQUEST;
        case 4:
          return OPTION_RESPONSE;
        case 5:
          return DATA;
        case 6:
          return ACKNACK;
        case 7:
          return ALERT;
        case 8:
          return COMMAND_REQUEST;
        case 9:
          return COMMAND_RESPONSE;
        case 13:
          return CD_ONE_ENCAPSULATION;
        case 26:
          return CUSTOM_RESET_FRAME;
        default:
          throw new IllegalArgumentException(String.format(
              "Integer value %1$d does not map to a Cd11FrameTypeIdentifier enumeration.", value));
      }
    }

    /**
     * Returns a string containing a comma separated list of valid enumeration values.
     *
     * @return Comma separated list of valid values.
     */
    public static String validValues() {
      StringBuilder validValues = new StringBuilder();
      for (FrameType fti : FrameType.values()) {
        if (validValues.length() > 0) {
          validValues.append(", ");
        }
        validValues.append(fti.toString());
      }
      return validValues.toString();
    }

    /**
     * Returns a string containing a comma separated list of valid integers.
     *
     * @return Comma separated list of valid integers.
     */
    public static String validIntValues() {
      StringBuilder validValues = new StringBuilder();
      for (FrameType fti : FrameType.values()) {
        if (validValues.length() > 0) {
          validValues.append(", ");
        }
        validValues.append(fti.getValue());
      }
      return validValues.toString();
    }
  }

  /**
   * Base class constructor for CD 1.1 frames constructed in-memory.
   *
   * @param frameType The frame type.
   * @throws NullPointerException Thrown when input is null.
   */
  public Cd11Frame(FrameType frameType) throws NullPointerException {
    this.frameType = Objects.requireNonNull(frameType);
    this.rawNetworkBytes = null; // Since this was constructed from memory, and not read from the network.
  }

  /**
   * Base class constructor for CD 1.1 frames constructed from a byte array.
   *
   * @throws Exception Thrown when invalid byte frame is received.
   */
  public Cd11Frame(Cd11ByteFrame byteFrame) throws Exception {
    this.frameType = byteFrame.getFrameType();
    this.rawNetworkBytes = byteFrame.getRawReceivedBytes();
    this.setFrameHeader(new Cd11FrameHeader(byteFrame.getFrameHeaderByteBuffer()));
    this.setFrameTrailer(new Cd11FrameTrailer(
        byteFrame.getFrameTrailerSegment1ByteBuffer(),
        byteFrame.getFrameTrailerSegment2ByteBuffer()));
  }

  /**
   * True if the frame header has been set, otherwise false.
   *
   * @return True if frame header exists, otherwise false.
   */
  public final boolean frameHeaderExists() {
    return (this.header != null);
  }

  /**
   * True if the frame trailer has been set, otherwise false.
   *
   * @return True if frame trailer exists, otherwise false.
   */
  public final boolean frameTrailerExists() {
    return (this.trailer != null);
  }

  /**
   * Returns the frame header, if one has been set.
   *
   * @return CD 1.1 frame header.
   * @throws IllegalStateException Thrown if header has not yet been set.
   */
  public final Cd11FrameHeader getFrameHeader() throws IllegalStateException {
    if (this.header == null) {
      throw new IllegalStateException("Frame header has not yet been set.");
    }
    return this.header;
  }

  /**
   * Sets the frame header, if it has not already been set.
   *
   * @throws IllegalStateException Thrown if header has already been set.
   */
  public final void setFrameHeader(Cd11FrameHeader cd11FrameHeader)
      throws IllegalArgumentException, IllegalStateException {

    if (this.header != null) {
      throw new IllegalStateException("Frame header has already been set.");
    }

    // Check that the frame type matches.
    if (this.frameType != cd11FrameHeader.frameType) {
      throw new IllegalArgumentException(
          "The Frame Type of the header received does not match the frame type of this object.");
    }

    this.header = cd11FrameHeader;
  }

  /**
   * Returns the frame trailer, if one has been set.
   *
   * @return CD 1.1 frame trailer.
   * @throws IllegalStateException Thrown if trailer has not yet been set.
   */
  public final Cd11FrameTrailer getFrameTrailer() throws IllegalStateException {
    if (this.trailer == null) {
      throw new IllegalStateException("Frame trailer has not yet been set.");
    }
    return this.trailer;
  }

  /**
   * Sets the frame trailer, if it has not already been set.
   *
   * @throws IllegalStateException Thrown if trailer has already been set.
   */
  public final void setFrameTrailer(Cd11FrameTrailer cd11FrameTrailer)
      throws IllegalStateException {
    if (this.trailer != null) {
      throw new IllegalStateException("Frame trailer has already been set.");
    }
    this.trailer = cd11FrameTrailer;
  }

  /**
   * Returns a byte array representing the entire CD 1.1 frame (header, body, and trailer). NOTE:
   * This method can only be called when a fully constructed frame trailer has been set.
   *
   * @return Byte array representing the full CD 1.1 frame.
   * @throws IllegalStateException Thrown when the frame header or trailer have not been set.
   * @throws IOException Thrown if the full frame cannot be serialized.
   */
  public final byte[] toBytes() throws IllegalStateException, IOException {
    if (this.header == null) {
      throw new IllegalStateException("Frame header has not been set.");
    }

    if (this.trailer == null) {
      throw new IllegalStateException("Frame trailer has not been set.");
    }

    ByteArrayOutputStream frameBytes = new ByteArrayOutputStream(Cd11FrameHeader.FRAME_LENGTH);
    frameBytes.write(this.header.toBytes());
    frameBytes.write(this.getFrameBodyBytes());
    frameBytes.write(this.trailer.toBytes());

    return frameBytes.toByteArray();
  }

  /**
   * Returns the raw bytes read from the network, when the CD 1.1 frame object is constructed from
   * network bytes. NOTE: When the CD 1.1 frame is constructed in-memory, this method returns null.
   *
   * @return Byte array of network bytes, or null.
   */
  public final byte[] getRawNetworkBytes() {
    return rawNetworkBytes;
  }

  /**
   * Calculate the CRC over the entire frame and compare with the CRC in the frame footer.
   *
   * @return TRUE if CRC is verified, otherwise FALSE.  If there is an IO error generating the bytes
   * to compute the CRC value, false is returned (i.e. this method does not throw IOException)
   */
  public boolean isValidCRC() {
    try {
      byte[] rawNetworkBytes = this.getRawNetworkBytes();
      byte[] bytes = (rawNetworkBytes == null) ? this.toBytes() : rawNetworkBytes;

      // Replace commverification bytes with all zeros before we computing the CRC.
      for (int i = (bytes.length - Long.BYTES); i < bytes.length; i++) {
        bytes[i] = 0;
      }

      // Compute the CRC value.
      return CRC64.isValidCrc(
          bytes, bytes.length,
          this.getFrameTrailer().commVerification);
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Validates this Cd11Frame as a specific type of frame, and checks that the header FrameType
   * matches expected for that class of frame.
   *
   * @param clazz the type the frame is expected to be; determines the return type
   * @param <T> the type of the return value, determined by clazz
   * @return the frame casted into the desired type
   * @throws IllegalArgumentException if the frame or clazz is null, if there is no class
   * implementation known for the type of frame, or if the FrameType in the header of the frame is
   * unknown or not as expected for the requested class.
   */
  public <T> T asFrameType(Class<T> clazz) throws IllegalArgumentException {
    Validate.notNull(clazz);
    // Get expected FrameType
    FrameType expectedFrameType = Cd11Frame.frameTypeToClass.get(clazz);
    Validate.notNull(expectedFrameType,
        "Could not find expected FrameType for class " + clazz.getName());
    // Check that the expected and actual FrameType match, otherwise throw an exception.
    if (this.frameType != expectedFrameType) {
      throw new IllegalArgumentException(String.format(
          "Wrong type of frame (expected frameType %s, received %s).",
          expectedFrameType, this.frameType));
    } else if (!clazz.isInstance(this)) {
      throw new IllegalArgumentException(String.format(
          "Frame of wrong type; expected %s but got %s",
          clazz.getName(), this.getClass().getName()));
    }
    return clazz.cast(this);
  }

  /**
   * Returns a byte array representing the frame body (must be implemented by inheriting class.
   *
   * @return Byte array representing the frame body.
   * @throws IOException Thrown if the frame body cannot be serialized.
   */
  public abstract byte[] getFrameBodyBytes() throws IOException;
}
