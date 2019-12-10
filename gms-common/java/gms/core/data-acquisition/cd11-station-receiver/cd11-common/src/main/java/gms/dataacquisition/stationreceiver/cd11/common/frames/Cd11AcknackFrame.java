package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.apache.commons.lang3.Validate;


/**
 * The Acknack Frame is provided for error control. It also delivers a heartbeat (to prevent
 * timeout) if there are no Data Frames or other frames to send. Status fields include the name of
 * the frame set being acknowledged, the range of valid sequence numbers, and a list of gaps in the
 * sequence. A frame set is a container for the frames that will or have been exchanged with a
 * protocol peer. Each frame set contains frames from a unique source. The gaps are presented in
 * increasing order and must be non-overlapping intervals within the range of lowest seq no through
 * highest seq no. When an Acknack Frame is sent for heartbeat/keep-alive purposes, its content is
 * the same as at any other time, in other words, the list of sequence number gaps. Acknack Frames
 * provide heartbeat signals. The provisional policy is that Acknack frames will be sent at least
 * once a minute. Acknack Frames employ the standard frame header. However, the header sequence
 * number is undefined in the Acknack Frame. Further, Acknack Frames do not report on the delivery
 * of other Acknack Frames. This would not be possible because Acknack frames have no sequence
 * number.
 */
public class Cd11AcknackFrame extends Cd11Frame {

  // See constructor javadoc for description of the fields.
  public final String framesetAcked;  // Defined in CD11 spec as 20 bytes
  public final long lowestSeqNum;
  public final long highestSeqNum;
  public final int gapCount;
  public final long[] gapRanges;

  /**
   * The minimum byte array length of an acknack frame. This value does not include the size of gaps
   * which is dynamic.
   */
  public static final int MINIMUM_FRAME_LENGTH = (Long.BYTES * 2) + Integer.BYTES + 20;
  // The size required in the gaps byte[] buffer for each gap is long * 2 (for start and end time of gap). */
  public static final int SIZE_PER_GAP = Long.BYTES * 2;

  /**
   * Creates an acknack object from a byte frame.
   *
   * @param cd11ByteFrame CD 1.1 frame segments.
   * @throws Exception input stream is null, has bad data, too short, etc.
   */
  public Cd11AcknackFrame(Cd11ByteFrame cd11ByteFrame) throws Exception {
    super(cd11ByteFrame);

    ByteBuffer body = cd11ByteFrame.getFrameBodyByteBuffer();
    this.framesetAcked = FrameUtilities.readBytesAsString(body, 20);
    this.lowestSeqNum = body.getLong();
    this.highestSeqNum = body.getLong();
    this.gapCount = body.getInt();
    // parse the gaps
    long[] gaps = new long[gapCount * 2];
    // *2 is because there are two gap numbers for each gap
    for (int i = 0; i < gapCount * 2; i++) {
      gaps[i] = body.getLong();
    }
    this.gapRanges = gaps;

    validate();
  }

  /**
   * Creates an acknack frame with all arguments.
   *
   * @param framesetAcked full name of the frame set being acknowledged (for example, “SG7:0”)
   * @param lowestSeqNum lowest valid sequence number sent considered during the current connection
   * for the set (0 until a frame set is no longer empty)
   * @param highestSeqNum highest valid sequence number considered during the current connection for
   * the set (–1 until a frame set is no longer empty)
   * @param gapRanges each gap contains two long entries for start time and end time
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11AcknackFrame(
      String framesetAcked, long lowestSeqNum, long highestSeqNum, long[] gapRanges)
      throws IllegalArgumentException {

    // Initialize the base class.
    super(FrameType.ACKNACK);

    // Initialize properties
    this.framesetAcked = framesetAcked;
    this.lowestSeqNum = lowestSeqNum;
    this.highestSeqNum = highestSeqNum;
    this.gapCount = gapRanges.length / 2;
    this.gapRanges = gapRanges;

    validate();
  }

  /**
   * Validates this object. Throws an exception if there are any problems with it's fields.
   */
  private void validate() throws IllegalArgumentException, NullPointerException {
    Cd11Validator.validFrameSetAcked(this.framesetAcked);
    Validate.notNull(this.gapRanges);
    Validate.isTrue(this.gapRanges.length % 2 == 0,
        "Must have an even number of gap ranges");
    Validate.isTrue(this.gapCount * 2 == gapRanges.length,
        "Gap count must be twice as long as the length of gap ranges");
  }

  /**
   * Returns this acknack frame as bytes.
   *
   * @return byte[], representing the frame in wire format
   * @throws IOException if errors occur in writing to output stream, etc.
   */
  @Override
  public byte[] getFrameBodyBytes() throws IOException {
    ByteBuffer output = ByteBuffer.allocate(Cd11AcknackFrame.MINIMUM_FRAME_LENGTH +
        (gapRanges.length * Long.BYTES));
    output.put(FrameUtilities.padToLength(this.framesetAcked, 20).getBytes());
    output.putLong(this.lowestSeqNum);
    output.putLong(this.highestSeqNum);
    output.putInt(this.gapCount);
    for (long l : this.gapRanges) {
      output.putLong(l);
    }

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

    Cd11AcknackFrame that = (Cd11AcknackFrame) o;

    if (lowestSeqNum != that.lowestSeqNum) {
      return false;
    }
    if (highestSeqNum != that.highestSeqNum) {
      return false;
    }
    if (gapCount != that.gapCount) {
      return false;
    }
    if (framesetAcked != null ? !framesetAcked.equals(that.framesetAcked)
        : that.framesetAcked != null) {
      return false;
    }
    return Arrays.equals(gapRanges, that.gapRanges);
  }

  @Override
  public int hashCode() {
    int result = framesetAcked != null ? framesetAcked.hashCode() : 0;
    result = 31 * result + (int) (lowestSeqNum ^ (lowestSeqNum >>> 32));
    result = 31 * result + (int) (highestSeqNum ^ (highestSeqNum >>> 32));
    result = 31 * result + gapCount;
    result = 31 * result + Arrays.hashCode(gapRanges);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder("Cd11AcknackFrame { ");
    out.append("frameType: \"").append(frameType).append("\", ");
    out.append("framesetAcked: \"").append(framesetAcked).append("\", ");
    out.append("lowestSeqNum: ").append(lowestSeqNum).append(", ");
    out.append("highestSeqNum: ").append(highestSeqNum).append(", ");
    out.append("gapCount: ").append(gapCount).append(", ");
    out.append("gaps: \"").append(Arrays.toString(gapRanges)).append("\" ");
    out.append("}");
    return out.toString();
  }
}
