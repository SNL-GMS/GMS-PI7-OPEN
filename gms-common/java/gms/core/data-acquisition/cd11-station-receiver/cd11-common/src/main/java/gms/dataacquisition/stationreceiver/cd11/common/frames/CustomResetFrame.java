package gms.dataacquisition.stationreceiver.cd11.common.frames;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * This custom frame signals to the Data Consumer that a "reset" has occurred, and that the
 * Data Consumer needs to clear its gap list, shutdown, and listen for a new Data Provider
 * connection.
 *
 * NOTE: This frame is **NOT** described in the CD 1.1 protocol.
 */
public class CustomResetFrame extends Cd11Frame {

  public final byte[] frameBody;

  /**
   * Creates a custom "reset" object from a byte frame.
   *
   * @param cd11ByteFrame CD 1.1 frame segments.
   * @throws Exception input stream is null, has bad data, too short, etc.
   */
  public CustomResetFrame(Cd11ByteFrame cd11ByteFrame) throws Exception {
    super(cd11ByteFrame);

    ByteBuffer body = cd11ByteFrame.getFrameBodyByteBuffer();
    this.frameBody = body.array();
  }

  /**
   * Returns this custom "reset" frame as bytes.
   *
   * @return byte[], representing the frame in wire format
   * @throws IOException if errors occur in writing to output stream, etc.
   */
  @Override
  public byte[] getFrameBodyBytes() throws IOException {
    return this.frameBody;
  }

  @Override
  public boolean equals(Object o) {
    return false;
  }

  @Override
  public int hashCode() {
    return 31 * Arrays.hashCode(frameBody);
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder("CustomResetFrame { ");
    out.append("frameBodyLength: ").append(frameBody.length).append(" ");
    out.append("}");
    return out.toString();
  }
}
