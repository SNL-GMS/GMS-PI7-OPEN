package gms.dataacquisition.stationreceiver.cd11.common;

import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionRequestFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionResponseFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11FrameHeader;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11FrameTrailer;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import org.apache.commons.lang3.Validate;


/**
 * Collection of useful functions for dealing with CD-1.1 frames.
 */
public class FrameUtilities {

  // Expected length of timestamps in CD-1.1.
  public static final int TIMESTAMP_LEN = 20;

  /**
   * Pads the string to the specified length with whitespace.
   *
   * @param s the string to pad
   * @param length the desired length of the string
   * @param paddingCharacter the character to use as padding
   * @return the original string s, padded to the right with paddingCharacter up to length
   * @throws IllegalArgumentException if the length of the input string is greater than the
   * requested length
   */
  public static String padToLength(String s, int length, char paddingCharacter)
      throws IllegalArgumentException {
    if (s.length() > length) {
      throw new IllegalArgumentException("String too large already; length = "
          + s.length() + ", padded request was to " + length);
    }
    while (s.length() < length) {
      s += paddingCharacter;
    }
    return s;
  }

  /**
   * Pads string to length using ASCII null character ('\0')
   *
   * @param s the string to pad
   * @param length the desired length of the string
   * @return the original string s, padded to the right with ASCII null up to length
   * @throws IllegalArgumentException if the length of the input string is greater than the
   * requested length
   */
  public static String padToLength(String s, int length) {
    return padToLength(s, length, '\0');
  }

  /**
   * Calculates the padded length of a field so the correct amount of bytes can be extracted from a
   * byte buffer.
   *
   * @param unpaddedLength the unpadded length
   * @param divisibleBy what the padded length should be divisible by
   * @return The padded length of a field, which includes padding.
   */
  public static int calculatePaddedLength(int unpaddedLength, int divisibleBy) {
    int padded = unpaddedLength;
    //Pad the auth value size so it's divisible by supplied value (which is typically 4)
    if (unpaddedLength % divisibleBy != 0) {
      padded += (divisibleBy - (unpaddedLength % divisibleBy));
    }
    return padded;
  }

  /**
   * Calculates the padding needed to make the field length divisible by some value.
   *
   * @param size The uppadded length of the value
   * @param divisibleBy The value the field should be divisible by (typically 4) in CD1.1
   * @return The number of bytes needed for padding.
   */
  public static int calculateUnpaddedLength(int size, int divisibleBy) {
    int modSize = size % divisibleBy;
    return (modSize == 0) ? 0 : divisibleBy - modSize;
  }

  /**
   * Parses a fixed number of bytes from a byte buffer, converts them to a string, and removes nulls
   * and white space.
   *
   * @param frameBytesBuffer the byte buffer to parse from.
   * @param length the number of bytes to parse.
   * @return String
   * @throws IllegalArgumentException if byte buffer is null or doesn't have at least 'length'
   * bytes
   */
  public static String readBytesAsString(ByteBuffer frameBytesBuffer, int length)
      throws IllegalArgumentException {

    Validate.notNull(frameBytesBuffer);
    Validate.isTrue(frameBytesBuffer.remaining() >= length,
        String.format("Not enough bytes in this bytebuff to read; need %d but only %d",
            length, frameBytesBuffer.remaining()));

    byte[] stringBytes = new byte[length];
    frameBytesBuffer.get(stringBytes);
    // In case strings are null-terminated (c style), replace ASCII '0' with empty.
    return stripString(new String(stringBytes));
  }

  /**
   * Parses a fixed number of bytes from a byte buffer, converts them to a string, and removes nulls
   * and white space.
   *
   * @param frameBytesBuffer the byte buffer to parse from.
   * @param length the number of bytes to parse.
   * @return String
   * @throws IllegalArgumentException if byte buffer is null or doesn't have at least 'length'
   * bytes
   */
  public static byte[] readRawBytes(ByteBuffer frameBytesBuffer, int length)
      throws IllegalArgumentException {

    Validate.notNull(frameBytesBuffer);
    Validate.isTrue(frameBytesBuffer.remaining() >= length,
        String.format("Not enough bytes in this bytebuff to read; need %d but only %d",
            length, frameBytesBuffer.remaining()));

    byte[] stringBytes = new byte[length];
    frameBytesBuffer.get(stringBytes);

    return stringBytes;
  }

  /**
   * Remove nul characters and whitespace from a string. Assumes the string is left justified and
   * padded on the right with nul characters.
   *
   * @param str The input string.
   * @return String
   */
  public static String stripString(String str) {
    //int idx = str.indexOf("\0");

    // Remove all null characters from the string.
    str = str.replace("\0", "");

    // Trim whitespace from the ends of the string.
    str = str.trim();

    return str;
  }

  /**
   * Julian Dates in CD11 are in the form yyyyddd hh:mm:ss.mmm This will create an instance with the
   * correct time and year on Jan 1st then add the days, to easily handle the 'ddd' to 'mmdd'
   * conversion:
   *
   * - Example Julian Date Input: 2017346 23:20:00.142
   *
   * - Example Instance Output: 2017-12-13T23:20:00.142Z
   *
   * @param jd timestamp from CD11 in the form yyyyddd hh:mm:ss.mmm
   * @return Instant object with UTC format
   * @throws IllegalArgumentException Thrown when input string is not the correct length.
   * @throws DateTimeParseException Thrown when date cannot be parsed from the input string.
   */
  public static Instant jdToInstant(String jd)
      throws DateTimeParseException, IllegalArgumentException {
    if (jd.length() == TIMESTAMP_LEN) {
      String year = jd.substring(0, 4);
      // minus one because nothing to add when days = 001
      int days = Integer.parseInt(jd.substring(4, 7)) - 1;
      String hmsm = jd.substring(8, TIMESTAMP_LEN).trim();
      String utc = year + "-01-01T" + hmsm + "Z";
      Instant jan1 = Instant.parse(utc);
      return jan1.plus(days, ChronoUnit.DAYS);
    } else {
      throw new IllegalArgumentException("Julian Date not length " + TIMESTAMP_LEN);
    }
  }

  /**
   * Creates a 'julian date' string (jd), given an instant.
   *
   * @param i the instant
   * @return Example Instant input '2017-12-13T23:20:00.142Z' will provide this result: '2017346 23:20:00.142'.
   */
  public static String instantToJd(Instant i) {
    LocalDateTime date = LocalDateTime.ofInstant(i, ZoneOffset.UTC);
    String dateString = String.valueOf(date.getYear()) +
        String.valueOf(date.getDayOfYear()) + " " +
        String.format("%02d:%02d:%02d.%03d",
            date.getHour(),
            date.getMinute(),
            date.getSecond(),
            date.get(ChronoField.MILLI_OF_SECOND));

    return padToLength(dateString, TIMESTAMP_LEN);
  }

  /**
   * Determine if the input parameter is of the form yyyyddd hh:mm:ss.ttt.
   *
   * @param jd The Julian data string to check
   * @return true if valid Julian date false otherwise
   */
  public static boolean validJulianDate(String jd) {
    return (jd.matches("\\d{4}\\d{3} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}")) ? true : false;
  }

  public static boolean validChannelString(String channelString) {
    return (channelString.matches("[\\w|\\x00]{10}")) ? true : false;
  }


  /**
   * Creates a copy of a CD 1.1 Connection Request Frame, with a modified frame type. <p> NOTE: For
   * testing purposes only!!!
   *
   * @param frame Frame used to copy.
   * @param newStationName New station name.
   * @param newIpAddress New IP address.
   * @param newPort New port number.
   * @return Modified frame.
   * @throws IOException thrown if error occurs.
   */
  public static Cd11ConnectionRequestFrame cloneAndModifyFrame(
      Cd11ConnectionRequestFrame frame,
      String newStationName, String newIpAddress, Integer newPort)
      throws IOException {

    // Create the frame body.
    String ipAddress;
    Cd11ConnectionRequestFrame newFrame = new Cd11ConnectionRequestFrame(
        frame.majorVersion, frame.minorVersion,
        (newStationName == null) ? frame.stationName : newStationName,
        frame.stationType, frame.serviceType,
        (newIpAddress == null) ? intToIpAddressString(frame.ipAddress) : newIpAddress,
        (newPort == null) ? frame.port : newPort,
        (frame.secondIpAddress == 0) ? null : intToIpAddressString(frame.secondIpAddress),
        (frame.secondPort == 0) ? null : frame.secondPort);

    // Generate the frame body byte array.
    byte[] frameBodyBytes = newFrame.getFrameBodyBytes();

    // Use the frame body byte array to generate the frame header.
    Cd11FrameHeader frameHeader = new Cd11FrameHeader(
        frame.frameType,
        Cd11FrameHeader.FRAME_LENGTH + Cd11ConnectionRequestFrame.FRAME_LENGTH,
        frame.getFrameHeader().frameCreator,
        frame.getFrameHeader().frameDestination,
        0);

    // Generate the frame header and body byte arrays.
    ByteBuffer frameHeaderAndBodyByteBuffer = ByteBuffer.allocate(
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length);
    frameHeaderAndBodyByteBuffer.put(frameHeader.toBytes());
    frameHeaderAndBodyByteBuffer.put(frameBodyBytes);

    // Generate the frame trailer.
    Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(
        frame.getFrameTrailer().authenticationKeyIdentifier,
        frameHeaderAndBodyByteBuffer.array());

    // Add the frame header and trailer.
    newFrame.setFrameHeader(frameHeader);
    newFrame.setFrameTrailer(frameTrailer);

    return newFrame;
  }

  /**
   * Creates a copy of a CD 1.1 Connection Response Frame, with a modified frame type. <p> NOTE: For
   * testing purposes only!!!
   *
   * @param frame Frame used to copy.
   * @param newResponderName New responder name.
   * @param newIpAddress New IP address.
   * @param newPort New port number.
   * @return Modified frame.
   * @throws IOException thrown if error occurs.
   */
  public static Cd11ConnectionResponseFrame cloneAndModifyFrame(
      Cd11ConnectionResponseFrame frame,
      String newResponderName, String newIpAddress, Integer newPort)
      throws IOException {

    // Create the frame body.
    String ipAddress;
    Cd11ConnectionResponseFrame newFrame = new Cd11ConnectionResponseFrame(
        frame.majorVersion, frame.minorVersion,
        (newResponderName == null) ? frame.responderName : newResponderName,
        frame.responderType,
        frame.serviceType,
        (newIpAddress == null) ? intToIpAddressString(frame.ipAddress) : newIpAddress,
        (newPort == null) ? frame.port : newPort,
        (frame.secondIpAddress == 0) ? null : intToIpAddressString(frame.secondIpAddress),
        (frame.secondPort == 0) ? null : frame.secondPort);

    // Generate the frame body byte array.
    byte[] frameBodyBytes = newFrame.getFrameBodyBytes();

    // Use the frame body byte array to generate the frame header.
    Cd11FrameHeader frameHeader = new Cd11FrameHeader(
        frame.frameType,
        Cd11FrameHeader.FRAME_LENGTH + Cd11ConnectionResponseFrame.FRAME_LENGTH,
        frame.getFrameHeader().frameCreator,
        frame.getFrameHeader().frameDestination,
        0);

    // Generate the frame header and body byte arrays.
    ByteBuffer frameHeaderAndBodyByteBuffer = ByteBuffer.allocate(
        Cd11FrameHeader.FRAME_LENGTH + frameBodyBytes.length);
    frameHeaderAndBodyByteBuffer.put(frameHeader.toBytes());
    frameHeaderAndBodyByteBuffer.put(frameBodyBytes);

    // Generate the frame trailer.
    Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(
        frame.getFrameTrailer().authenticationKeyIdentifier,
        frameHeaderAndBodyByteBuffer.array());

    // Add the frame header and trailer.
    newFrame.setFrameHeader(frameHeader);
    newFrame.setFrameTrailer(frameTrailer);

    return newFrame;
  }

  public static String intToIpAddressString(int ipAddress) throws UnknownHostException {
    final InetAddress inetAddress = InetAddress.getByAddress(BigInteger.valueOf(ipAddress).toByteArray());
    // Using 0 for the port since we just need to get the host string and then this object goes
    // out of scope (need to use InetSocketAddress since InetAddress's getHostAddress gets flagged
    // by Fortify
    return new InetSocketAddress(inetAddress, 0).getHostString();
  }
}
