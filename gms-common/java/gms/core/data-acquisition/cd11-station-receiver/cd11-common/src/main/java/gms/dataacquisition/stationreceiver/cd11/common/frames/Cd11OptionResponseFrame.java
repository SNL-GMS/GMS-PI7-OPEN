package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * The Option Response Frame is used to echo option requests. If the echoed value is different from
 * the requested value, further pairs of Option frames are exchanged until agreement is reached.
 * This frame and its companion, the Option Response Frame, are only exchanged as part of the
 * connection establishment process. However, future developments may support using these frames to
 * designate desired parameters such as a start time or list of channels, where multiple Option
 * Request Frames may be sent if necessary.  Therefore, only a single option is implemented at this
 * time. This class will require an update to make it more dynamic should new options be
 * implemented. NOTE: This class implements the "Connection establishment" option only!  This is the
 * only option defined by the current protocol specification.
 */
public class Cd11OptionResponseFrame extends Cd11Frame {

  // See constructor javadoc for description of the fields.
  public final int optionType;
  public final int optionSize;
  public final String optionResponse; // Defined in CD11 spec as 8 bytes for a "Connection establishment" option

  /**
   * The byte array length of an option response frame.
   */
  public static final int FRAME_LENGTH = (Integer.BYTES * 3) + 8;

  /**
   * Creates an option object from a byte frame.
   *
   * @param cd11ByteFrame CD 1.1 frame segments.
   * @throws Exception input stream is null, has bad data, too short, etc.
   */
  public Cd11OptionResponseFrame(Cd11ByteFrame cd11ByteFrame) throws Exception {
    super(cd11ByteFrame);

    ByteBuffer body = cd11ByteFrame.getFrameBodyByteBuffer();
    int optionCount = body.getInt(); // TODO: Must be 1, for now.
    this.optionType = body.getInt();
    this.optionSize = body.getInt();
    String unpaddedOR = FrameUtilities.readBytesAsString(body, optionSize);
    this.optionResponse = FrameUtilities.padToLength(unpaddedOR, 8);

    this.validate();
  }

  /**
   * Creates an option response frame with all arguments.
   *
   * @param optionType numeric identifier of option requested
   * @param optionResponse value of option, padded to be divisible by 4
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11OptionResponseFrame(int optionType,
      String optionResponse) // TODO: Accept a list of values.
      throws IllegalArgumentException, NullPointerException {
    super(FrameType.OPTION_RESPONSE);

    this.optionType = optionType;
    this.optionSize = optionResponse.length();
    this.optionResponse = FrameUtilities.padToLength(optionResponse, 8);

    this.validate();
  }

  private void validate() {
    // Validate option type.
    if (this.optionType != 1) {
      throw new IllegalArgumentException("Only OptionType 1 is currently accepted.");
    }

    // Validate option response.
    if (this.optionResponse == null) {
      throw new NullPointerException("Option response value is null.");
    } else if (this.optionResponse.length() < 1 || optionResponse.length() > 8) {
      throw new IllegalArgumentException(String.format(
          "Option response length must be between 1 - 8 characters (received [%1$s]).",
          optionResponse));
    }
  }

  /**
   * Returns this connection request frame as bytes.
   *
   * @return byte[], representing the frame in wire format
   * @throws IOException if errors occur in writing to output stream, etc.
   */
  @Override
  public byte[] getFrameBodyBytes() throws IOException {
    int optionResponsePaddedSize = FrameUtilities
        .calculatePaddedLength(this.optionResponse.length(), 4);

    ByteBuffer output = ByteBuffer.allocate(
        Integer.BYTES +       // Option count.
            (2 * Integer.BYTES) + // Option type + Option size.
            optionResponsePaddedSize // Option response
    );

    output.putInt(1);
    output.putInt(this.optionType);
    output.putInt(this.optionResponse.length()); // Unpadded length of the option response.
    output
        .put(FrameUtilities.padToLength(this.optionResponse, optionResponsePaddedSize).getBytes());

    return output.array();
  }

  // TODO: Define equals method.
  //@Override
  //public boolean equals(Object o) {
  //}

  // TODO: Define hashCode method.
  //@Override
  //public int hashCode() {
  //}


  @Override
  public String toString() {
    return "Cd11OptionResponseFrame{" +
        "optionType=" + optionType +
        ", optionSize=" + optionSize +
        ", optionResponse='" + optionResponse + '\'' +
        '}';
  }
}
