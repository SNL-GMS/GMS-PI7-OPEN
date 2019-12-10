package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame.FrameType;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;


/**
 * Represents the header fields of Cd-1.1 frames (Connection request/response).
 */
public class Cd11FrameHeader {

  // See constructor javadoc for description of the fields.
  public final FrameType frameType;
  public final int trailerOffset;
  public final String frameCreator;       // Defined in CD11 spec as 8 bytes
  public final String frameDestination;   // Defined in CD11 spec as 8 bytes
  public final long sequenceNumber;
  public final int series;

  /**
   * The byte array length of a CD1.1 frame header.
   */
  public static final int FRAME_LENGTH = (Integer.BYTES * 3) + Long.BYTES + 8 + 8;

  /**
   * Users should instantiate this class via the Cd11FrameHeaderFactory.
   */
  public Cd11FrameHeader(ByteBuffer input) throws Exception {
    input.rewind();
    this.frameType = FrameType.fromInt(input.getInt());
    this.trailerOffset = input.getInt();
    this.frameCreator = FrameUtilities.readBytesAsString(input, 8);
    this.frameDestination = FrameUtilities.readBytesAsString(input, 8);
    this.sequenceNumber = input.getLong();
    this.series = input.getInt();

    validate();
  }

  /**
   * Users should instantiate this class via the Cd11FrameHeaderFactory.
   *
   * @param frameType numeric identifier of this frame type
   * @param trailerOffset byte offset from first byte of the frame to the beginning of the trailer
   * @param frameCreator 8-byte ASCII assigned identifier of the creator of the frame
   * @param frameDestination 8-byte ASCII identifier of the destination of the frame
   * @param sequenceNumber sequence number assigned by the frame creator
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11FrameHeader(
      FrameType frameType, int trailerOffset, String frameCreator, String frameDestination,
      long sequenceNumber) throws IllegalArgumentException {

    // Validate input and assign properties
    this.frameType = frameType;
    this.trailerOffset = trailerOffset;
    this.frameCreator = frameCreator;
    this.frameDestination = frameDestination;
    this.sequenceNumber = sequenceNumber; // NOTE: Not all CD 1.1 frame types need a valid sequence number!
    this.series = 0;

    validate();
  }


  /**
   * Validates this object. Throws an exception if there are any problems with it's fields.
   */
  private void validate() throws IllegalArgumentException, NullPointerException {
    Validate.isTrue(this.trailerOffset > 0, "trailerOffset must be positive");
    Cd11Validator.validFrameCreator(this.frameCreator);
    Cd11Validator.validFrameDestination(this.frameDestination);
  }

  public byte[] toBytes() throws IOException {
    ByteBuffer output = ByteBuffer.allocate(FRAME_LENGTH);
    output.putInt(this.frameType.getValue());
    output.putInt(this.trailerOffset);
    output.put(FrameUtilities.padToLength(this.frameCreator, 8).getBytes());
    output.put(FrameUtilities.padToLength(this.frameDestination, 8).getBytes());
    output.putLong(this.sequenceNumber);
    output.putInt(this.series);
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

    Cd11FrameHeader that = (Cd11FrameHeader) o;

    if (frameType != that.frameType) {
      return false;
    }
    if (trailerOffset != that.trailerOffset) {
      return false;
    }
    if (sequenceNumber != that.sequenceNumber) {
      return false;
    }
    if (series != that.series) {
      return false;
    }
    if (frameCreator != null ? !frameCreator.equals(that.frameCreator)
        : that.frameCreator != null) {
      return false;
    }
    return frameDestination != null ? frameDestination.equals(that.frameDestination)
        : that.frameDestination == null;
  }

  @Override
  public int hashCode() {
    int result = frameType.hashCode();
    result = 31 * result + trailerOffset;
    result = 31 * result + (frameCreator != null ? frameCreator.hashCode() : 0);
    result = 31 * result + (frameDestination != null ? frameDestination.hashCode() : 0);
    result = 31 * result + (int) (sequenceNumber ^ (sequenceNumber >>> 32));
    result = 31 * result + series;
    return result;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder("Cd11FrameHeader { ");
    out.append("frameType: \"").append(frameType).append("\", ");
    out.append("trailerOffset: ").append(trailerOffset).append(", ");
    out.append("frameCreator: \"").append(frameCreator).append("\", ");
    out.append("frameDestination: \"").append(frameDestination).append("\", ");
    out.append("sequenceNumber: ").append(sequenceNumber).append(", ");
    out.append("series: ").append(series).append(" ");
    out.append("}");
    return out.toString();
  }
}
