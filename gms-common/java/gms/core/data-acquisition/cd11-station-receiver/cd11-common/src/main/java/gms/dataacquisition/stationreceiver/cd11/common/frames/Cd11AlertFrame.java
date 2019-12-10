package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

/**
 * Alert Frames are sent by either the data provider or consumer to notify the other party that the
 * connection is going to be terminated. Because of their very nature an Alert Frame will not be
 * addressed by an Acknack Frame. For example, if an Alert Frame were to be re-sent, the receiving
 * protocol peer would terminate the connection.
 */
public class Cd11AlertFrame extends Cd11Frame {

  // See constructor javadoc for description of the fields.
  public final int size;
  public final String message;

  /**
   * The minimum byte array length of an alert frame. This value does not include the size the
   * message which is dynamic.
   */
  public static final int MINIMUM_FRAME_LENGTH = Integer.BYTES;

  /**
   * Creates an alert object from a byte frame.
   *
   * @param cd11ByteFrame CD 1.1 frame segments.
   * @throws Exception input stream is null, has bad data, too short, etc.
   */
  public Cd11AlertFrame(Cd11ByteFrame cd11ByteFrame) throws Exception {
    super(cd11ByteFrame);

    ByteBuffer body = cd11ByteFrame.getFrameBodyByteBuffer();
    this.size = body.getInt();
    this.message = FrameUtilities
        .readBytesAsString(body, FrameUtilities.calculatePaddedLength(this.size, Integer.BYTES));

    validate();
  }

  /**
   * Creates an alert frame with all arguments.
   *
   * @param message Alert message.
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11AlertFrame(String message) throws IllegalArgumentException {

    super(FrameType.ALERT);

    this.size = FrameUtilities.calculatePaddedLength(message.length(), 4);
    this.message = message;

    validate();
  }

  /**
   * Validates this object. Throws an exception if there are any problems with it's fields.
   */
  private void validate() throws IllegalArgumentException, NullPointerException {
    Validate.isTrue(this.size >= 0, "Size must be >= 0");
    Validate.notEmpty(this.message);
  }

  /**
   * Returns this alert frame as bytes.
   *
   * @return byte[], representing the frame in wire format
   * @throws IOException if errors occur in writing to output stream, etc.
   */
  @Override
  public byte[] getFrameBodyBytes() throws IOException {
    int paddingSize = FrameUtilities.calculatePaddedLength(this.size, Integer.BYTES);

    ByteBuffer output = ByteBuffer.allocate(Cd11AlertFrame.MINIMUM_FRAME_LENGTH + paddingSize);
    output.putInt(size);
    output.put(FrameUtilities.padToLength(this.message, paddingSize).getBytes());

    return output.array();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Cd11AlertFrame that = (Cd11AlertFrame) o;

    if (size != that.size) {
      return false;
    }
    return message != null ? message.equals(that.message) : that.message == null;
  }

  @Override
  public int hashCode() {
    int result = size;
    result = 31 * result + (message != null ? message.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder("Cd11AlertFrame { ");
    out.append("frameType: \"").append(frameType).append("\", ");
    out.append("size: ").append(size).append(", ");
    out.append("message: \"").append(message).append("\" ");
    out.append("}");
    return out.toString();
  }
}
