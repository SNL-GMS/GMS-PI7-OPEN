package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Channel Subframe Header describes the Channel Subframes to follow. The nominal time field is
 * the Data Frame’s time signature; times for each Channel Subframe are given therein. Other fields
 * in the Channel Subframe Header list the number of channels and their site/channel/location
 * information.
 */
public class Cd11ChannelSubframeHeader {

  // See constructor javadoc for description of the fields.
  public final int numOfChannels;
  public final int frameTimeLength;
  public final Instant nominalTime;     // Defined in CD11 spec as 20 byte string
  public final int channelStringCount; // Must be numOfChannels * 10 + padding for div by 4
  public final String channelString;
  public int paddedChannelStringLength;

  public final int NOM_TIME_LENGTH = 20;

  private static Logger logger = LoggerFactory.getLogger(Cd11ChannelSubframeHeader.class);

  /**
   * The minimum byte array length of a subframe header. This value does not include the
   * channelString value which is dynamic.
   */
  public static final int MINIMUM_FRAME_LENGTH = (Integer.BYTES * 3) + 20;


  public Cd11ChannelSubframeHeader(ByteBuffer bodyByteBuffer) {
    if (bodyByteBuffer.remaining() < MINIMUM_FRAME_LENGTH) {
      String error = "ChannelSubframeHeader minimum size is " + MINIMUM_FRAME_LENGTH +
          " but byte buffer only contains " + bodyByteBuffer.remaining() + " bytes.";
      logger.error(error);
      throw new BufferUnderflowException();
    }

    //In the case of the Data Frame, the Channel Subframe Header
    //immediately follows the Frame Header (i.e it is the first
    //thing in the body.
    this.numOfChannels = bodyByteBuffer.getInt();
    this.frameTimeLength = bodyByteBuffer.getInt();

    String nominalTimeString = FrameUtilities.readBytesAsString(bodyByteBuffer, NOM_TIME_LENGTH);
    if (FrameUtilities.validJulianDate(nominalTimeString)) {
      this.nominalTime = FrameUtilities.jdToInstant(nominalTimeString);

      this.channelStringCount = bodyByteBuffer.getInt();

      int padding = FrameUtilities.calculateUnpaddedLength(channelStringCount, Integer.BYTES);
      this.paddedChannelStringLength = this.channelStringCount + padding;

//      this.paddedChannelStringLength = FrameUtilities.calculatePaddedLength(channelStringCount, Integer.BYTES);
//      this.channelString = FrameUtilities.readRawBytes(bodyByteBuffer, this.paddedChannelStringLength);
      byte[] channelStringAsBytes = FrameUtilities
          .readRawBytes(bodyByteBuffer, this.channelStringCount);
      this.channelString = new String(channelStringAsBytes, Charset.forName("UTF-8"));
      int offset = 0;
      for (int i = 0; i < this.numOfChannels; ++i) {
        String subChannelString = this.channelString.substring(offset, offset + 10);
        offset += 10;
        if (!FrameUtilities.validChannelString(subChannelString)) {
          logger.info("Invalid channelString for subchannel string " + i + 1);
        }
      }
      // Check if the station indeed padded the channel string to be word (4 bytes) aligned
      // or if the padding is actually the upper half word of the following field (the channel length
      // field).  If the number of channels is odd, the channel string should be padded with 2 bytes so
      // the padding plus the upper half of the channel length field should be zero (assuming
      // the following channel subframe is <= 65,536 bytes).  If the value is not zero reset the byte
      // position to immediately after the channel string, otherwise reset it to after the padding.
      // Resume parsing the channel subframe data.
      if (padding > 0) {
        int temp = bodyByteBuffer.getInt();
        if (temp != 0) {
          logger.info("Channel string di not end on a 4 byte boundary.");
          bodyByteBuffer.position(bodyByteBuffer.position() - Integer.BYTES);
        } else {
          bodyByteBuffer.position(bodyByteBuffer.position() - padding);
        }

      }

      validate();
    } else {
      logger.info("Invalid nominalTime field received: " + nominalTimeString);
      this.nominalTime = Instant.MIN;
      this.channelStringCount = 0;
      this.paddedChannelStringLength = 0;
      this.channelString = null;
      throw new RuntimeException("Bad formatted nominalTime string");
    }
  }

  /**
   * Creates data channel subframe header with all arguments.
   *
   * @param numOfChannels number of channels in this frame
   * @param frameTimeLength time in milliseconds this frame encompasses
   * @param nominalTime 20-byte ASCII nominal UTC start time of all channels in frame;
   * @param chanStringCount unpadded length in bytes of the channel string; must be ten times the
   * number of channels field
   * @param chanString channel string listing the Channel Subframes to follow, 10 bytes per subframe
   * The entire channel string is null-padded to a multiple of four bytes. Each 10-byte tring is
   * formatted as follows: five bytes for the site name left justified and padded with ASCII null
   * (if necessary), three bytes for the channel name left justified and padded with ASCII null (if
   * necessary), and two bytes for the location name left justified and padded with ASCII null (if
   * necessary). For example, the site name ‘KCC’ would be followed by two null characters before
   * the specification of a channel name. Note that if only one channel of data is provided, then
   * the channel description field must be padded with 2 null bytes (at the end) to satisfy the
   * requirement of being evenly divisible by 4.
   */
  public Cd11ChannelSubframeHeader(
      int numOfChannels, int frameTimeLength, Instant nominalTime,
      int chanStringCount, String chanString) {
    this.numOfChannels = numOfChannels;
    this.frameTimeLength = frameTimeLength;
    this.nominalTime = nominalTime;
    this.channelStringCount = chanStringCount;
    this.paddedChannelStringLength = FrameUtilities
        .calculatePaddedLength(channelStringCount, Integer.BYTES);
    this.channelString = chanString;

    validate();
  }

  /**
   * The size of the channel subframe header can be dynamic because the channel_string field is
   * dependent upon the number of subframes.
   *
   * @return The size in bytes of the channel subframe header
   */
  public int getSize() {
    return MINIMUM_FRAME_LENGTH + this.paddedChannelStringLength;
  }

  /**
   * Turns this data subframe header into a byte[]
   *
   * @return a byte[] representation of this data subframe header
   * @throws IOException if cannot write to byte array output stream, etc.
   */
  public byte[] toBytes() throws IOException {

    ByteBuffer output = ByteBuffer.allocate(getSize());
    output.putInt(numOfChannels);
    output.putInt(frameTimeLength);
    output.put(FrameUtilities.instantToJd(nominalTime).getBytes());
    output.putInt(channelStringCount);
    output.put(FrameUtilities.padToLength(channelString, this.paddedChannelStringLength)
        .getBytes());

    return output.array();
  }

  /**
   * Validates this object. Throws an exception if there are any problems with it's fields.
   */
  private void validate() throws IllegalArgumentException, NullPointerException {
    Validate.isTrue(this.numOfChannels > 0,
        "ChannelSubframeHeader.NumOfChannels must be > 0, "
            + "but value is: " + this.numOfChannels);

    Validate.isTrue(this.frameTimeLength > 0,
        "ChannelSubframeHeader.FrameTimeLength must be > 0, "
            + "but value is: " + this.frameTimeLength);

    Validate.notNull(this.nominalTime);

    Validate.isTrue(this.channelStringCount >= 0,
        "ChannelSubframeHeader.ChannelStringCount must be >= 0, "
            + "but value is: " + this.channelStringCount);

    Validate.notEmpty(this.channelString);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Cd11ChannelSubframeHeader that = (Cd11ChannelSubframeHeader) o;

    if (numOfChannels != that.numOfChannels) {
      return false;
    }
    if (frameTimeLength != that.frameTimeLength) {
      return false;
    }
    if (channelStringCount != that.channelStringCount) {
      return false;
    }
    if (NOM_TIME_LENGTH != that.NOM_TIME_LENGTH) {
      return false;
    }
    if (nominalTime != null ? !nominalTime.equals(that.nominalTime)
        : that.nominalTime != null) {
      return false;
    }
    return channelString != null ? channelString.equals(that.channelString)
        : that.channelString == null;
  }

  @Override
  public int hashCode() {
    int result = numOfChannels;
    result = 31 * result + frameTimeLength;
    result = 31 * result + (nominalTime != null ? nominalTime.hashCode() : 0);
    result = 31 * result + channelStringCount;
    result = 31 * result + (channelString != null ? channelString.hashCode() : 0);
    result = 31 * result + NOM_TIME_LENGTH;
    return result;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder("Cd11ChannelSubframeHeader { ");
    out.append("numOfChannels: ").append(numOfChannels).append(", ");
    out.append("frameTimeLength: ").append(frameTimeLength).append(", ");
    out.append("nominalTime: \"").append(nominalTime).append("\", ");
    out.append("channelStringCount: ").append(channelStringCount).append(", ");
    out.append("channelString: \"").append(channelString).append("\", ");
    out.append("NOM_TIME_LENGTH: ").append(NOM_TIME_LENGTH).append(" ");
    out.append("}");
    return out.toString();
  }
}
