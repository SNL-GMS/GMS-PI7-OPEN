package gms.dataacquisition.stationreceiver.cd11.common.frames;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.enums.DataType;
import gms.dataacquisition.stationreceiver.cd11.common.enums.SensorType;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.time.Instant;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Perform some tests on the Cd11ChannelSubframe class.  These tests do not provide complete
 * coverage.
 */
public class Cd11ChannelSubframeTest {

  private static ByteBuffer buffer1, buffer2, buffer3;
  private static int size1 = 208; // size of frame
  private static int size2 = 212; // size of frame
  private static int size3 = 216; // size of frame

  private static int authOffset = 80;
  private static String authString = "01afc\0\0\0";
  private static Instant timestamp = Instant.parse("2017-12-07T12:13:14.000Z");
  private static SensorType sensorType = SensorType.HYDROACOUSTIC;
  private static String siteName = "TST01";
  private static String siteName3 = "TEST01";
  private static String chanName = "C01";
  private static String locName = "00";
  private static DataType dataFormat = DataType.i2;
  private static int authSize = 5;
  private static int timeLen = 2000; // milliseconds
  private static int sampleCnt = 100; // samples
  private static int statusCnt = 20;
  private static float calFactor = 12.3f;
  private static float calPeriod = 1.23f;
  private static byte[] statusBytes = {0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1};
  private static int dataCnt = 100;
  private static byte[] dataBytes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
      0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

  @BeforeClass
  public static void setup() {
    buffer1 = ByteBuffer.allocateDirect(size1);
    buffer1.putInt(size1 - 4); // size of subframe minus this field
    buffer1.putInt(authOffset);
    buffer1.put((byte) 0);  // authentication
    buffer1.put((byte) 0);  // transformation
    buffer1.put(sensorType.code);  // sensor type
    buffer1.put((byte) 0);  // option flag
    buffer1.put(siteName.getBytes());
    buffer1.put(chanName.getBytes());
    buffer1.put(locName.getBytes());
    buffer1.put(dataFormat.name().getBytes());
    buffer1.putFloat(calFactor);
    buffer1.putFloat(calPeriod);
    buffer1.put(FrameUtilities.instantToJd(timestamp).getBytes());
    buffer1.putInt(timeLen);
    buffer1.putInt(sampleCnt);
    buffer1.putInt(statusCnt);
    buffer1.put(statusBytes);
    buffer1.putInt(dataCnt);
    buffer1.put(dataBytes);
    buffer1.putInt(0);
    buffer1.putInt(0);
    buffer1.putInt(authSize);
    buffer1.put(authString.getBytes());

    buffer2 = ByteBuffer.allocateDirect(size2);
    buffer2.putInt(size2); // <-- this is an invalid length
    buffer2.putInt(authOffset);
    buffer2.put((byte) 0);  // authentication
    buffer2.put((byte) 0);  // transformation
    buffer2.put(sensorType.code);  // sensor type
    buffer2.put((byte) 0);  // option flag
    buffer2.put(siteName.getBytes());
    buffer2.put(chanName.getBytes());
    buffer2.put(locName.getBytes());
    buffer2.put(dataFormat.name().getBytes());
    buffer2.putFloat(calFactor);
    buffer2.putFloat(calPeriod);
    buffer2.put(FrameUtilities.instantToJd(timestamp).getBytes());
    buffer2.putInt(timeLen);
    buffer2.putInt(sampleCnt);
    buffer2.putInt(statusCnt);
    buffer2.put(statusBytes);
    buffer2.putInt(dataCnt);
    buffer2.put(dataBytes);
    buffer2.putInt(0);
    buffer2.putInt(0);
    buffer2.putInt(authSize);
    buffer2.put(authString.getBytes());

    buffer3 = ByteBuffer.allocateDirect(size3);
    buffer3.putInt(size3 - Integer.BYTES); // size of subframe minus this field
    buffer3.putInt(authOffset);
    buffer3.put((byte) 0);  // authentication
    buffer3.put((byte) 0);  // transformation
    buffer3.put(sensorType.code);  // sensor type
    buffer3.put((byte) 0);  // option flag
    buffer3.put(siteName3.getBytes());  // <-- this should throw a clinker in the parsing
    buffer3.put(chanName.getBytes());
    buffer3.put(locName.getBytes());
    buffer3.put(dataFormat.name().getBytes());
    buffer3.putFloat(calFactor);
    buffer3.putFloat(calPeriod);
    buffer3.put(FrameUtilities.instantToJd(timestamp).getBytes());
    buffer3.putInt(timeLen);
    buffer3.putInt(sampleCnt);
    buffer3.putInt(statusCnt);
    buffer3.put(statusBytes);
    buffer3.putInt(dataCnt);
    buffer3.put(dataBytes);
    buffer3.putInt(0);
    buffer3.putInt(0);
    buffer3.putInt(authSize);
    buffer3.put(authString.getBytes());
  }

  @AfterClass
  public static void tearDown() {

  }

  /**
   * Perform some tests to verify that fields were parsed correctly.  We only test some fields.
   */
  @Test
  public void testParsing() {
    buffer1.rewind();
    Cd11ChannelSubframe csf = new Cd11ChannelSubframe(buffer1);
    assertNotNull(csf);

    assertEquals(csf.authSize, authSize);
    assertEquals(siteName, csf.siteName);
    assertEquals(FrameUtilities.stripString(locName), csf.locationName);
    assertEquals(calFactor, csf.calibrationFactor, 0.00000001);
    assertEquals(timestamp, csf.timeStamp);
    assertArrayEquals(statusBytes, csf.channelStatusData);
    assertArrayEquals(dataBytes, csf.channelData);
    assertEquals(sampleCnt, csf.samples);
    assertEquals(sensorType, csf.sensorType);
    assertArrayEquals(authString.getBytes(), csf.authValue);
  }


  /**
   * Test when the data alignment is off.
   */
  @Test(expected = BufferUnderflowException.class)
  public void testBufferUnderflow() {
    new Cd11ChannelSubframe(buffer3);
  }


  /**
   * Test when the frame size and the declared size do not match.
   */
  @Ignore
  public void testFrameSize() throws IllegalArgumentException {
    buffer2.rewind();
    new Cd11ChannelSubframe(buffer2);
  }

  /**
   * Test the toBytes method.
   */
  @Test
  public void testToBytes() throws IOException {
    buffer1.rewind();
    Cd11ChannelSubframe csf = new Cd11ChannelSubframe(buffer1);
    assertNotNull(csf);
    buffer1.rewind();
    byte[] bytes1 = csf.toBytes();
    byte[] bytes2 = new byte[buffer1.remaining()];
    buffer1.get(bytes2);

    assertArrayEquals(bytes1, bytes2);
  }
}
