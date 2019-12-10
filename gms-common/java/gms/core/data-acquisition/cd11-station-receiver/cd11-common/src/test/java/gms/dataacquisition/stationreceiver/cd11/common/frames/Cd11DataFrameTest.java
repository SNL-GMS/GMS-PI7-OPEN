package gms.dataacquisition.stationreceiver.cd11.common.frames;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.common.primitives.Ints;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilitiesTest;
import gms.dataacquisition.stationreceiver.cd11.common.enums.CompressionFormat;
import gms.dataacquisition.stationreceiver.cd11.common.enums.DataType;
import gms.dataacquisition.stationreceiver.cd11.common.enums.SensorType;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.time.Instant;
import org.junit.Test;


public class Cd11DataFrameTest {

  private static final int DATA_FRAME_HEADER_SIZE = 44;
  private static final int NUM_CHANNELS = 1;
  private static final int TWO_CHANNELS = 2;
  private static final int FRAME_TIME_LENGTH = 10000;
  private static final Instant NOMINAL_TIME = Instant.parse("2017-12-06T17:15:00Z");
  private static final int CHANNEL_STRING_COUNT = 10;
  private static final int CHANNEL_STRING_COUNT2 = 20;
  private static final String CHANNEL_STRING = "STA12SHZ01";
  private static final String TRIMMED_CHANNEL_STRING = "STA12SHZ01";

  private static final int DATA_SUBFRAME_SIZE = 100;
  private static final int CHANNEL_LENGTH = 96;
  private static final int AUTHENTICATION_OFFSET = DATA_FRAME_HEADER_SIZE + DATA_SUBFRAME_SIZE - 16;
  private static final int AUTHENTICATION_OFFSET2 =
      DATA_FRAME_HEADER_SIZE + (DATA_SUBFRAME_SIZE * 2) - 16;
  private static final boolean CHANNEL_DESCRIPTION_AUTHENTICATION = false;
  private static final CompressionFormat CHANNEL_DESCRIPTION_TRANSFORMATION
      = CompressionFormat.CANADIAN_AFTER_SIGNATURE;
  private static final SensorType CHANNEL_DESCRIPTION_SENSOR_TYPE = SensorType.HYDROACOUSTIC;
  private static final boolean CHANNEL_DESCRIPTION_OPTION_FLAG = false;
  private static final String CHANNEL_DESCRIPTION_SITE_NAME = "STA12";
  private static final String CHANNEL_DESCRIPTION_CHANNEL_NAME = "SHZ";
  private static final String CHANNEL_DESCRIPTION_LOCATION = "01";
  private static final DataType CHANNEL_DESCRIPTION_DATA_FORMAT = DataType.s4;
  private static final int CHANNEL_DESCRIPTION_CALIB_FACTOR = 0;
  private static final int CHANNEL_DESCRIPTION_CALIB_PER = 0;
  private static final Instant TIME_STAMP = Instant.parse("2017-12-01T17:15:00.123Z");
  private static final int SUBFRAME_TIME_LENGTH = 10000;
  private static final int SAMPLES = 8;
  private static final int CHANNEL_STATUS_SIZE = 4;
  private static final int CHANNEL_STATUS = 0;
  private static final int DATA_SIZE = 8;
  private static final byte[] CHANNEL_DATA = new byte[8];
  private static final int SUBFRAME_COUNT = 0;

  private static final int AUTH_KEY = 123;
  private static final int AUTH_SIZE = 8;
  private static final long AUTH_VALUE = 1512076158000l;
  private static final long COMM_VERIFY = 1512076209000l;


  private static ByteBuffer initDataFrame() throws Exception {
    ByteBuffer TEST_DATA_FRAME = ByteBuffer.allocate(DATA_FRAME_HEADER_SIZE + DATA_SUBFRAME_SIZE);

    for (int i = 0; i < CHANNEL_DATA.length; i++) {
      CHANNEL_DATA[i] = (byte) i;
    }

    // Data frame header
    TEST_DATA_FRAME.putInt(NUM_CHANNELS);
    TEST_DATA_FRAME.putInt(FRAME_TIME_LENGTH);
    TEST_DATA_FRAME.put(FrameUtilities.instantToJd(NOMINAL_TIME).getBytes());
    TEST_DATA_FRAME.putInt(CHANNEL_STRING_COUNT);
    TEST_DATA_FRAME.put(CHANNEL_STRING.getBytes());
    TEST_DATA_FRAME.putShort((short) 0); // align to 4 byte boundary

//    byte[] tempChannelString = CHANNEL_STRING.getBytes();
//    int pos = tempChannelString.length;
//    tempChannelString[pos - 2] = 0;
//    tempChannelString[pos - 1] = 0;
//    TEST_DATA_FRAME.put(tempChannelString);

    //  Channel Subframe 1
    TEST_DATA_FRAME.putInt(CHANNEL_LENGTH);
    TEST_DATA_FRAME.putInt(AUTHENTICATION_OFFSET);
    TEST_DATA_FRAME.put(toByte(CHANNEL_DESCRIPTION_AUTHENTICATION));
    TEST_DATA_FRAME.put(CHANNEL_DESCRIPTION_TRANSFORMATION.code);
    TEST_DATA_FRAME.put(CHANNEL_DESCRIPTION_SENSOR_TYPE.code);
    TEST_DATA_FRAME.put(toByte(CHANNEL_DESCRIPTION_OPTION_FLAG));
    TEST_DATA_FRAME.put(CHANNEL_DESCRIPTION_SITE_NAME.getBytes());
    TEST_DATA_FRAME.put(CHANNEL_DESCRIPTION_CHANNEL_NAME.getBytes());
    TEST_DATA_FRAME.put(CHANNEL_DESCRIPTION_LOCATION.getBytes());
    TEST_DATA_FRAME.put(CHANNEL_DESCRIPTION_DATA_FORMAT.name().getBytes());
    TEST_DATA_FRAME.putInt(CHANNEL_DESCRIPTION_CALIB_FACTOR);
    TEST_DATA_FRAME.putInt(CHANNEL_DESCRIPTION_CALIB_PER);
    TEST_DATA_FRAME.put(FrameUtilities.instantToJd(TIME_STAMP).getBytes());
    TEST_DATA_FRAME.putInt(SUBFRAME_TIME_LENGTH);
    TEST_DATA_FRAME.putInt(SAMPLES);
    TEST_DATA_FRAME.putInt(CHANNEL_STATUS_SIZE);
    TEST_DATA_FRAME.putInt(CHANNEL_STATUS);
    TEST_DATA_FRAME.putInt(DATA_SIZE);
    TEST_DATA_FRAME.put(CHANNEL_DATA);
    TEST_DATA_FRAME.putInt(SUBFRAME_COUNT);
    TEST_DATA_FRAME.putInt(AUTH_KEY);
    TEST_DATA_FRAME.putInt(AUTH_SIZE);
    TEST_DATA_FRAME.putLong(AUTH_VALUE);

    return TEST_DATA_FRAME;
  }

  private static ByteBuffer initDataFrameTwoChannels() throws Exception {
    //ByteBuffer dataFrameBuff = ByteBuffer.allocate(DATA_FRAME_HEADER_SIZE + DATA_SUBFRAME_SIZE*2);
    ByteBuffer dataFrameBuff = ByteBuffer.allocate(88 + DATA_SUBFRAME_SIZE * 2);

    for (int i = 0; i < CHANNEL_DATA.length; i++) {
      CHANNEL_DATA[i] = (byte) i;
    }

    // Data frame header
    dataFrameBuff.putInt(TWO_CHANNELS);
    dataFrameBuff.putInt(FRAME_TIME_LENGTH);
    dataFrameBuff.put(FrameUtilities.instantToJd(NOMINAL_TIME).getBytes());
    dataFrameBuff.putInt(CHANNEL_STRING_COUNT2);
    dataFrameBuff.put(TRIMMED_CHANNEL_STRING.getBytes());
    dataFrameBuff.put(TRIMMED_CHANNEL_STRING.getBytes());

    // Channel Subframe 1
    dataFrameBuff.putInt(CHANNEL_LENGTH);
    dataFrameBuff.putInt(AUTHENTICATION_OFFSET2);
    dataFrameBuff.put(toByte(CHANNEL_DESCRIPTION_AUTHENTICATION));
    dataFrameBuff.put(CHANNEL_DESCRIPTION_TRANSFORMATION.code);
    dataFrameBuff.put(CHANNEL_DESCRIPTION_SENSOR_TYPE.code);
    dataFrameBuff.put(toByte(CHANNEL_DESCRIPTION_OPTION_FLAG));
    dataFrameBuff.put(CHANNEL_DESCRIPTION_SITE_NAME.getBytes());
    dataFrameBuff.put(CHANNEL_DESCRIPTION_CHANNEL_NAME.getBytes());
    dataFrameBuff.put(CHANNEL_DESCRIPTION_LOCATION.getBytes());
    dataFrameBuff.put(CHANNEL_DESCRIPTION_DATA_FORMAT.name().getBytes());
    dataFrameBuff.putInt(CHANNEL_DESCRIPTION_CALIB_FACTOR);
    dataFrameBuff.putInt(CHANNEL_DESCRIPTION_CALIB_PER);
    dataFrameBuff.put(FrameUtilities.instantToJd(TIME_STAMP).getBytes());
    dataFrameBuff.putInt(SUBFRAME_TIME_LENGTH);
    dataFrameBuff.putInt(SAMPLES);
    dataFrameBuff.putInt(CHANNEL_STATUS_SIZE);
    dataFrameBuff.putInt(CHANNEL_STATUS);
    dataFrameBuff.putInt(DATA_SIZE);
    dataFrameBuff.put(CHANNEL_DATA);
    dataFrameBuff.putInt(SUBFRAME_COUNT);
    dataFrameBuff.putInt(AUTH_KEY);
    dataFrameBuff.putInt(AUTH_SIZE);
    dataFrameBuff.putLong(AUTH_VALUE);

    // Channel Subframe 2
    dataFrameBuff.putInt(CHANNEL_LENGTH);
    dataFrameBuff.putInt(AUTHENTICATION_OFFSET);
    dataFrameBuff.put(toByte(CHANNEL_DESCRIPTION_AUTHENTICATION));
    dataFrameBuff.put(CHANNEL_DESCRIPTION_TRANSFORMATION.code);
    dataFrameBuff.put(CHANNEL_DESCRIPTION_SENSOR_TYPE.code);
    dataFrameBuff.put(toByte(CHANNEL_DESCRIPTION_OPTION_FLAG));
    dataFrameBuff.put(CHANNEL_DESCRIPTION_SITE_NAME.getBytes());
    dataFrameBuff.put(CHANNEL_DESCRIPTION_CHANNEL_NAME.getBytes());
    dataFrameBuff.put(CHANNEL_DESCRIPTION_LOCATION.getBytes());
    dataFrameBuff.put(CHANNEL_DESCRIPTION_DATA_FORMAT.name().getBytes());
    dataFrameBuff.putInt(CHANNEL_DESCRIPTION_CALIB_FACTOR);
    dataFrameBuff.putInt(CHANNEL_DESCRIPTION_CALIB_PER);
    dataFrameBuff.put(FrameUtilities.instantToJd(TIME_STAMP).getBytes());
    dataFrameBuff.putInt(SUBFRAME_TIME_LENGTH);
    dataFrameBuff.putInt(SAMPLES);
    dataFrameBuff.putInt(CHANNEL_STATUS_SIZE);
    dataFrameBuff.putInt(CHANNEL_STATUS);
    dataFrameBuff.putInt(DATA_SIZE);
    dataFrameBuff.put(CHANNEL_DATA);
    dataFrameBuff.putInt(SUBFRAME_COUNT);
    dataFrameBuff.putInt(AUTH_KEY);
    dataFrameBuff.putInt(AUTH_SIZE);
    dataFrameBuff.putLong(AUTH_VALUE);

    return dataFrameBuff;
  }

  @Test
  public void testDataFrameParsing() throws Exception {

    // Create header, body, and trailer.
    Cd11FrameHeader TEST_HEADER = FrameHeaderTestUtility.createHeaderForData(
        Cd11FrameHeader.FRAME_LENGTH + DATA_FRAME_HEADER_SIZE + DATA_SUBFRAME_SIZE,
        Cd11FrameHeaderTest.CREATOR, Cd11FrameHeaderTest.DESTINATION, 1512074377000l);

    ByteBuffer TEST_DATA_FRAME = initDataFrame();

    Cd11FrameTrailer TEST_TRAILER = FrameTrailerTestUtility.createTrailerWithoutAuthentication(
        TEST_HEADER, TEST_DATA_FRAME.array());
    byte[] TEST_TRAILER_array = TEST_TRAILER.toBytes();

    // Place all into a CD1.1 frame.
    ByteBuffer CD11 = ByteBuffer.allocate(Cd11FrameHeader.FRAME_LENGTH +
        Cd11ChannelSubframeHeader.MINIMUM_FRAME_LENGTH + 12 +
        Cd11ChannelSubframe.MINIMUM_FRAME_LENGTH + 4 + 8 + 8 +
        TEST_TRAILER_array.length);
    CD11.put(TEST_HEADER.toBytes());
    CD11.put(TEST_DATA_FRAME.array());
    CD11.put(TEST_TRAILER_array);

    // Convert into input stream for testing.
    DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(CD11.array()));

    // Perform tests.
    Cd11Frame cd11Frame = FrameUtilitiesTest.readNextCd11Object(inputStream);
    Cd11DataFrame requestFrame = (Cd11DataFrame) cd11Frame;

    // Test header
    assertEquals(NUM_CHANNELS, requestFrame.chanSubframeHeader.numOfChannels);
    assertEquals(FRAME_TIME_LENGTH, requestFrame.chanSubframeHeader.frameTimeLength);
    assertEquals(NOMINAL_TIME, requestFrame.chanSubframeHeader.nominalTime);
    assertEquals(CHANNEL_STRING_COUNT, requestFrame.chanSubframeHeader.channelStringCount);
    assertEquals(CHANNEL_STRING, requestFrame.chanSubframeHeader.channelString);

    // Test subframe
    Cd11ChannelSubframe sf = requestFrame.channelSubframes[0];
    assertEquals(CHANNEL_LENGTH, sf.channelLength);
    assertEquals(AUTHENTICATION_OFFSET, sf.authOffset);
    assertEquals(CHANNEL_DESCRIPTION_AUTHENTICATION, sf.authenticationOn);
    assertEquals(CHANNEL_DESCRIPTION_TRANSFORMATION, sf.compressionFormat);
    assertEquals(CHANNEL_DESCRIPTION_SENSOR_TYPE, sf.sensorType);
    assertEquals(CHANNEL_DESCRIPTION_OPTION_FLAG, sf.isCalib);
    assertEquals(CHANNEL_DESCRIPTION_SITE_NAME, sf.siteName);
    assertEquals(CHANNEL_DESCRIPTION_CHANNEL_NAME, sf.channelName);
    assertEquals(CHANNEL_DESCRIPTION_LOCATION, sf.locationName);
    assertEquals(CHANNEL_DESCRIPTION_DATA_FORMAT, sf.dataType);
    assertEquals(CHANNEL_DESCRIPTION_CALIB_FACTOR, sf.calibrationFactor, 0.00000001);
    assertEquals(CHANNEL_DESCRIPTION_CALIB_PER, sf.calibrationPeriod, 0.00000001);
    assertEquals(TIME_STAMP, sf.timeStamp);
    assertEquals(SUBFRAME_TIME_LENGTH, sf.subframeTimeLength);
    assertEquals(SAMPLES, sf.samples);
    assertEquals(CHANNEL_STATUS_SIZE, sf.channelStatusSize);
    assertArrayEquals(Ints.toByteArray(CHANNEL_STATUS), sf.channelStatusData);
    assertEquals(DATA_SIZE, sf.dataSize);
    assertArrayEquals(CHANNEL_DATA, sf.channelData);
    assertEquals(SUBFRAME_COUNT, sf.subframeCount);
    assertEquals(AUTH_KEY, sf.authKeyIdentifier);
    assertEquals(AUTH_SIZE, sf.authSize);
    assertArrayEquals(ByteBuffer.allocate(Long.BYTES).putLong(AUTH_VALUE).array(),
        sf.authValue);

    byte[] requestFrameBytes = requestFrame.toBytes();
    assertArrayEquals(requestFrameBytes, CD11.array());
  }

  private static byte toByte(boolean b) {
    return b ? (byte) 1 : (byte) 0;
  }

  @Test
  public void testBadSubframeTimeStamp() throws Exception {
    Cd11FrameHeader headerBuf = FrameHeaderTestUtility.createHeaderForData(
        Cd11FrameHeader.FRAME_LENGTH + /*DATA_FRAME_HEADER_SIZE*/52 + DATA_SUBFRAME_SIZE * 2,
        Cd11FrameHeaderTest.CREATOR, Cd11FrameHeaderTest.DESTINATION, 1512074377000l);

    ByteBuffer dataFrameBuf = initDataFrameTwoChannels();

    byte dataFrameBytes[] = dataFrameBuf.array();
    int offset = 184;
    byte[] julianDate = new byte[20];
    for (int i = 0; i < 20; ++i) {
      julianDate[i] = dataFrameBytes[offset + i];
    }
    String s = new String(julianDate);
    assertEquals(s, "2017335 17:15:00.123");

    Cd11FrameTrailer trailerBuf = FrameTrailerTestUtility.createTrailerWithoutAuthentication(
        headerBuf, dataFrameBuf.array());
    byte[] trailer_array = trailerBuf.toBytes();

    // Place all into a CD1.1 frame.
    ByteBuffer CD11 = ByteBuffer.allocate(
        Cd11FrameHeader.FRAME_LENGTH +
            Cd11ChannelSubframeHeader.MINIMUM_FRAME_LENGTH + 20 +
            dataFrameBuf.capacity() +
            trailer_array.length);

    CD11.put(headerBuf.toBytes());
    CD11.put(dataFrameBuf.array());
    CD11.put(trailer_array);

    // Convert into input stream for testing.
    DataInputStream inputStream = null;
    inputStream = new DataInputStream(new ByteArrayInputStream(CD11.array()));

    // Valid 2 channel test.
    try {
      Cd11Frame cd11Frame = FrameUtilitiesTest.readNextCd11Object(inputStream);
      Cd11DataFrame requestFrame = (Cd11DataFrame) cd11Frame;
      assertEquals(TWO_CHANNELS, requestFrame.chanSubframeHeader.numOfChannels);
    } catch (RuntimeException e) {
      fail("exception occured.");
    }

    // Corrupt the julian date of the second subframe, should throw away the entire frame
    CD11.rewind();
    inputStream.reset();

    // corrupt the second digit of the minute field
    dataFrameBytes[offset + 12] = 107; //'k'

    dataFrameBuf.clear();
    dataFrameBuf.put(dataFrameBytes);

    // Validate that the data is corrupted
    byte dataFrameBytes2[] = dataFrameBuf.array();
    byte[] badJulianDate = new byte[20];
    for (int i = 0; i < 20; ++i) {
      badJulianDate[i] = dataFrameBytes2[offset + i];
    }
    String badString = new String(badJulianDate);
    assertEquals(badString, "2017335 17:1k:00.123");

    CD11.put(headerBuf.toBytes());
    CD11.put(dataFrameBuf.array());
    CD11.put(trailer_array);

    inputStream = new DataInputStream(new ByteArrayInputStream(CD11.array()));
    try {
      Cd11Frame cd11Frame = FrameUtilitiesTest.readNextCd11Object(inputStream);
      Cd11DataFrame requestFrame = (Cd11DataFrame) cd11Frame;
      assertEquals(TWO_CHANNELS, requestFrame.chanSubframeHeader.numOfChannels);
    } catch (RuntimeException e) {
      fail("exception occured.");
    }
  }
}
