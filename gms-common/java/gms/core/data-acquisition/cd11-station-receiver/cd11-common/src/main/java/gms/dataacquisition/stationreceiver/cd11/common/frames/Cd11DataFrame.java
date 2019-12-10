package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The CD-1.1 Data Frame contains both a description of its data and the actual data values
 * themselves. Thus, the frame combines the fields of both the Data and Data Format Frames of the
 * CD-1 protocol. In addition, the status field of the Channel Subframe is allowed to be of variable
 * size, and its definition may vary among different implementations of CD-1.1. As with all frames,
 * the standard frame header and frame trailer surround the payload. In normal operation, Data
 * Frames comprise the bulk of the transmission. Each Data Frame consists of the standard Frame
 * Header, a header for the Channel Subframes, one or more Channel Subframes, and a standard Frame
 * Trailer. When uncompressed, data must be provided in network byte order; no translation is
 * provided by transport processing.
 */
public class Cd11DataFrame extends Cd11Frame {

  public final Cd11ChannelSubframeHeader chanSubframeHeader;
  public Cd11ChannelSubframe[] channelSubframes;

  private static Logger logger = LoggerFactory.getLogger(Cd11DataFrame.class);

  /**
   * Creates a data object from a byte frame.
   *
   * @param cd11ByteFrame CD 1.1 frame segments.
   * @throws Exception input stream is null, has bad data, too short, etc.
   */
  public Cd11DataFrame(Cd11ByteFrame cd11ByteFrame) throws Exception {
    super(cd11ByteFrame);

    //Get body (Subframe header + subframes)
    ByteBuffer body = cd11ByteFrame.getFrameBodyByteBuffer();
    //Passes the entire body, not just the subframe header, but only uses the bytes it needs
    this.chanSubframeHeader = new Cd11ChannelSubframeHeader(body);

    //number of subframes = chanSubframeHeader.channelString.length / 10
    //there are 10 bytes for each channel subframe in the channel string
    int numOfSubFrames = chanSubframeHeader.channelString.length() / 10;

    //Grab the remaining bytes from the body, these are just the subframes
    //Create a temporary arraylist of the subframes, throw away any that
    //do not have a valid timestamp.  All valid subframes are then added
    //to this frame object.
    ByteBuffer subframes = body.slice();
    int subframeCnt = 0;
    ArrayList<Cd11ChannelSubframe> tempSubFrames = new ArrayList<Cd11ChannelSubframe>();
    while (subframeCnt++ < numOfSubFrames) {
      try {
        Cd11ChannelSubframe singleSubframe = new Cd11ChannelSubframe(subframes);
        tempSubFrames.add(singleSubframe);
      } catch (RuntimeException ex) {
        logger.info("Bad subframe " + subframeCnt + 1 + " so skipping it");
      }
    }
    if (numOfSubFrames == tempSubFrames.size()) {
      this.channelSubframes =
          new Cd11ChannelSubframe[tempSubFrames.size()];
      for (int i = 0; i < numOfSubFrames; i++) {
        channelSubframes[i] = tempSubFrames.get(i);
      }

      validate();

      int bytesParsed = body.position() + subframes.position() + Cd11FrameHeader.FRAME_LENGTH;
      int bytesRead = getFrameHeader().trailerOffset;

      if (bytesRead != bytesParsed) {
        logger.error(
            "Not all bytes parsed:  Bytes read=" + bytesRead + " bytes parsed=" + bytesParsed);
      }
    }
  }

  /**
   * Creates a Data frame with all arguments.
   *
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11DataFrame(
      Cd11ChannelSubframeHeader channelSubframeHeader, Cd11ChannelSubframe[] channelSubframes)
      throws IllegalArgumentException {

    super(FrameType.DATA);

    this.chanSubframeHeader = channelSubframeHeader;
    this.channelSubframes = channelSubframes;
    validate();
  }

  /**
   * Create a Cd11DataFrame given it's channel subframes. The header data is inferred from the
   * content of the subframes.
   *
   * @param channelSubframes the subframes
   * @throws IllegalArgumentException if the subframes array contains null elements, or is empty
   */
  public Cd11DataFrame(Cd11ChannelSubframe[] channelSubframes) throws IllegalArgumentException {

    super(FrameType.DATA);

    Validate.notEmpty(channelSubframes);
    Validate.noNullElements(channelSubframes);
    this.channelSubframes = channelSubframes;

    Cd11ChannelSubframe firstSubframe = channelSubframes[0];
    int numChannels = channelSubframes.length;
    // TODO: possibly calculate frameTimeLength = latest finishing frametime - earliestNomTime starting frame time
    int frameTimeLength = firstSubframe.subframeTimeLength;

    // nominal time is the earliestNomTime timestamp of all frames, not specifically the first one
    Instant earliestNomTime = firstSubframe.timeStamp;
    for (int i = 0; i < channelSubframes.length; i++) {
      if (channelSubframes[i].timeStamp.isBefore(earliestNomTime)) {
        //the new timestamp is earlier than the previous earliest, set it
        earliestNomTime = channelSubframes[i].timeStamp;
      }
    }
    int channelStringCount = 10 * numChannels;  // defined in CD11 spec this way...
    this.chanSubframeHeader = new Cd11ChannelSubframeHeader(
        numChannels, frameTimeLength, earliestNomTime, channelStringCount,
        channelsString(this.channelSubframes));

    validate();
  }

  /**
   * Validates this object. Throws an exception if there are any problems with it's fields.
   */
  private void validate() throws IllegalArgumentException, NullPointerException {
    Validate.notEmpty(channelSubframes);
    Validate.noNullElements(channelSubframes);
    Validate.notNull(this.chanSubframeHeader);
  }

  /**
   * Turns this data frame into a byte[]
   *
   * @return a byte[] representation of this data frame
   * @throws IOException if cannot write to byte array output stream, etc.
   */
  @Override
  public byte[] getFrameBodyBytes() throws IOException {
    int size = chanSubframeHeader.getSize();
    for (Cd11ChannelSubframe subframe : channelSubframes) {
      size += subframe.getSize();
    }

    ByteBuffer output = ByteBuffer.allocate(size);
    output.put(chanSubframeHeader.toBytes());
    for (Cd11ChannelSubframe subframe : channelSubframes) {
      output.put(subframe.toBytes());
    }

    return output.array();
  }

  public static String channelsString(Cd11ChannelSubframe[] subframes) {
    Validate.notNull(subframes);
    String s = Arrays.stream(subframes).map(f -> f.channelString())
        .collect(Collectors.joining());
    int requiredSize = s.length() + FrameUtilities.calculateUnpaddedLength(s.length(), 4);
    return FrameUtilities.padToLength(s, requiredSize);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Cd11DataFrame that = (Cd11DataFrame) o;

    if (chanSubframeHeader != null ? !chanSubframeHeader.equals(that.chanSubframeHeader)
        : that.chanSubframeHeader != null) {
      return false;
    }
    // Probably incorrect - comparing Object[] arrays with Arrays.equals
    return Arrays.equals(channelSubframes, that.channelSubframes);
  }

  @Override
  public int hashCode() {
    int result = chanSubframeHeader != null ? chanSubframeHeader.hashCode() : 0;
    result = 31 * result + Arrays.hashCode(channelSubframes);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder("Cd11DataFrame { ");
    out.append("frameType: \"").append(frameType).append("\", ");
    out.append("chanSubframeHeader: \"").append(chanSubframeHeader).append("\", ");
    out.append("channelSubframes: \"").append(Arrays.toString(channelSubframes)).append("\", ");
    out.append("}");
    return out.toString();
  }
}
