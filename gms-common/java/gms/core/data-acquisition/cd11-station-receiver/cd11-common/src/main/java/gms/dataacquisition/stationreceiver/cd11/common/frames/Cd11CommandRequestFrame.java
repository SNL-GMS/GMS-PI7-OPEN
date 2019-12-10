package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import org.apache.commons.lang3.Validate;


public class Cd11CommandRequestFrame extends Cd11Frame {

  public final String stationName;
  public final String site;
  public final String channel;
  public final String locName;
  public final Instant timestamp;
  public final String commandMessage;

  /**
   * Creates an command request object from a byte frame.
   *
   * @param cd11ByteFrame CD 1.1 frame segments.
   * @throws Exception input stream is null, has bad data, too short, etc.
   */
  public Cd11CommandRequestFrame(Cd11ByteFrame cd11ByteFrame) throws Exception {
    super(cd11ByteFrame);

    ByteBuffer body = cd11ByteFrame.getFrameBodyByteBuffer();
    this.stationName = FrameUtilities.readBytesAsString(body, 8);
    this.site = FrameUtilities.readBytesAsString(body, 5);
    this.channel = FrameUtilities.readBytesAsString(body, 3);
    this.locName = FrameUtilities.readBytesAsString(body, 2);
    body.position(body.position() + 2); // Skip two null bytes.
    this.timestamp = FrameUtilities.jdToInstant(FrameUtilities.readBytesAsString(body, 20));
    int commandMessageSize = body.getInt();
    this.commandMessage = FrameUtilities.readBytesAsString(body, commandMessageSize);

    this.validate();
  }

  /**
   * Creates an command request object with all arguments.
   *
   * @param stationName station name
   * @param site site
   * @param channel channel
   * @param locName location name
   * @param timestamp time stamp
   * @param commandMessage command message
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11CommandRequestFrame(
      String stationName, String site, String channel, String locName,
      Instant timestamp, String commandMessage)
      throws IllegalArgumentException {

    super(FrameType.COMMAND_REQUEST);

    this.stationName = stationName;
    this.site = site;
    this.channel = channel;
    this.locName = locName;
    this.timestamp = timestamp;
    this.commandMessage = commandMessage;

    this.validate();
  }

  private void validate() {
    Validate.notNull(stationName);
    Validate.isTrue(stationName.length() <= 8);

    Validate.notNull(site);
    Validate.isTrue(site.length() <= 5);

    Validate.notNull(channel);
    Validate.isTrue(channel.length() <= 3);

    Validate.notNull(locName);
    Validate.isTrue(locName.length() <= 2);

    Validate.notNull(timestamp);

    Validate.notBlank(commandMessage);
  }

  /**
   * Returns this command request frame as bytes.
   *
   * @return byte[], representing the frame in wire format
   * @throws IOException if errors occur in writing to output stream, etc.
   */
  @Override
  public byte[] getFrameBodyBytes() throws IOException {
    int frameSize = 8 + 5 + 3 + 2 + 2 + 20 + Integer.BYTES + commandMessage.length();

    ByteBuffer output = ByteBuffer.allocate(frameSize);
    output.put(FrameUtilities.padToLength(stationName, 8).getBytes());
    output.put(FrameUtilities.padToLength(site, 5).getBytes());
    output.put(FrameUtilities.padToLength(channel, 3).getBytes());
    output.put(FrameUtilities.padToLength(locName, 2).getBytes());
    output.put((byte) 0); // Null byte.
    output.put((byte) 0); // Null byte.
    output.put(FrameUtilities.instantToJd(timestamp).getBytes());
    output.putInt(commandMessage.length());
    output.put(commandMessage.getBytes());

    return output.array();
  }

  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder("Cd11CommandRequestFrame { ");
    out.append("frameType: \"").append(frameType).append("\", ");
    out.append("stationName: \"").append(stationName).append("\", ");
    out.append("site: \"").append(site).append("\", ");
    out.append("channel: \"").append(channel).append("\", ");
    out.append("locName: \"").append(locName).append("\", ");
    out.append("timestamp: \"").append(timestamp).append("\", ");
    out.append("commandMessageSize: ").append(commandMessage.length()).append(", ");
    out.append("commandMessage: \"").append(commandMessage).append("\" ");
    out.append("}");
    return out.toString();
  }
}
