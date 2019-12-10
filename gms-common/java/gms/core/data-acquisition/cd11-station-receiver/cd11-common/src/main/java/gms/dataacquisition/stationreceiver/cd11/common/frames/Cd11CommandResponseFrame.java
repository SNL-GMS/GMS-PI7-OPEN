package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import org.apache.commons.lang3.Validate;


public class Cd11CommandResponseFrame extends Cd11Frame {

  public final String responderStation;
  public final String site;
  public final String channel;
  public final String locName;
  public final Instant timestamp;
  public final String commandRequestMessage;
  public final String responseMessage;

  /**
   * Creates a command response object from a byte frame.
   *
   * @param cd11ByteFrame CD 1.1 frame segments.
   * @throws Exception input stream is null, has bad data, too short, etc.
   */
  public Cd11CommandResponseFrame(Cd11ByteFrame cd11ByteFrame) throws Exception {
    super(cd11ByteFrame);

    ByteBuffer body = cd11ByteFrame.getFrameBodyByteBuffer();
    this.responderStation = FrameUtilities.readBytesAsString(body, 8);
    this.site = FrameUtilities.readBytesAsString(body, 5);
    this.channel = FrameUtilities.readBytesAsString(body, 3);
    this.locName = FrameUtilities.readBytesAsString(body, 2);
    body.position(body.position() + 2); // Skip two null bytes.
    this.timestamp = FrameUtilities.jdToInstant(FrameUtilities.readBytesAsString(body, 20));
    int commandRequestMessageSize = body.getInt();
    this.commandRequestMessage = FrameUtilities.readBytesAsString(body, commandRequestMessageSize);
    int responseMessageSize = body.getInt();
    this.responseMessage = FrameUtilities.readBytesAsString(body, responseMessageSize);

    this.validate();
  }

  /**
   * Creates a command response object with all arguments.
   *
   * @param responderStation station name
   * @param site site
   * @param channel channel
   * @param locName location name
   * @param timestamp time stamp
   * @param commandRequestMessage original command request sent from the Data Consumer
   * @param responseMessage response message
   * @throws IllegalArgumentException Thrown on invalid input.
   */
  public Cd11CommandResponseFrame(
      String responderStation, String site, String channel, String locName,
      Instant timestamp, String commandRequestMessage, String responseMessage)
      throws IllegalArgumentException, NullPointerException {

    super(FrameType.COMMAND_RESPONSE);

    this.responderStation = responderStation;
    this.site = site;
    this.channel = channel;
    this.locName = locName;
    this.timestamp = timestamp;
    this.commandRequestMessage = commandRequestMessage;
    this.responseMessage = responseMessage;

    this.validate();
  }

  private void validate() {
    Validate.notNull(responderStation);
    Validate.isTrue(responderStation.length() <= 8);

    Validate.notNull(site);
    Validate.isTrue(site.length() <= 5);

    Validate.notNull(channel);
    Validate.isTrue(channel.length() <= 3);

    Validate.notNull(locName);
    Validate.isTrue(locName.length() <= 2);

    Validate.notNull(timestamp);

    Validate.notBlank(commandRequestMessage);

    Validate.notBlank(responseMessage);
  }

  /**
   * Returns this connection request frame as bytes.
   *
   * @return byte[], representing the frame in wire format
   * @throws IOException if errors occur in writing to output stream, etc.
   */
  @Override
  public byte[] getFrameBodyBytes() throws IOException {
    int frameSize = 8 + 5 + 3 + 2 + 2 + 20 +
        Integer.BYTES + commandRequestMessage.length() +
        Integer.BYTES + responseMessage.length();

    ByteBuffer output = ByteBuffer.allocate(frameSize);
    output.put(FrameUtilities.padToLength(responderStation, 8).getBytes());
    output.put(FrameUtilities.padToLength(site, 5).getBytes());
    output.put(FrameUtilities.padToLength(channel, 3).getBytes());
    output.put(FrameUtilities.padToLength(locName, 2).getBytes());
    output.put((byte) 0); // Null byte.
    output.put((byte) 0); // Null byte.
    output.put(FrameUtilities.instantToJd(timestamp).getBytes());
    output.putInt(commandRequestMessage.length());
    output.put(commandRequestMessage.getBytes());
    output.putInt(responseMessage.length());
    output.put(responseMessage.getBytes());

    return output.array();
  }

  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder("Cd11CommandResponseFrame { ");
    out.append("frameType: \"").append(frameType).append("\", ");
    out.append("responderStation: \"").append(responderStation).append("\", ");
    out.append("site: \"").append(site).append("\", ");
    out.append("channel: \"").append(channel).append("\", ");
    out.append("locName: \"").append(locName).append("\", ");
    out.append("timestamp: \"").append(timestamp).append("\", ");
    out.append("commandRequestMessageSize: ").append(commandRequestMessage.length()).append(", ");
    out.append("commandRequestMessage: \"").append(commandRequestMessage).append("\", ");
    out.append("responseMessageSize: ").append(responseMessage.length()).append(", ");
    out.append("responseMessage: \"").append(responseMessage).append("\" ");
    out.append("}");
    return out.toString();
  }
}
