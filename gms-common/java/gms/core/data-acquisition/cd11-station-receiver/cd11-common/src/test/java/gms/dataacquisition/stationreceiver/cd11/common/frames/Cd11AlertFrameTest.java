package gms.dataacquisition.stationreceiver.cd11.common.frames;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilitiesTest;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import org.junit.Test;


public class Cd11AlertFrameTest {

  public static final int SIZE = 8;                 // [4] 0 - 3
  public static final String MESSAGE = "TERM1234";  // [8] 4 - 11

  private static ByteBuffer initAlert() throws Exception {
    ByteBuffer TEST_ALERT = ByteBuffer.allocate(Cd11AlertFrame.MINIMUM_FRAME_LENGTH + 8);

    TEST_ALERT.putInt(SIZE);
    TEST_ALERT.put(MESSAGE.getBytes());

    return TEST_ALERT;
  }

  @Test
  public void testAlertParsing() throws Exception {

    // Create header, body, and trailer.
    Cd11FrameHeader TEST_HEADER = FrameHeaderTestUtility.createHeaderForAlert(
        Cd11FrameHeaderTest.CREATOR, Cd11FrameHeaderTest.DESTINATION, 8);

    ByteBuffer TEST_ALERT = initAlert();
    byte[] TEST_ALERT_array = TEST_ALERT.array();

    Cd11FrameTrailer TEST_TRAILER = FrameTrailerTestUtility.createTrailerWithoutAuthentication(
        TEST_HEADER, TEST_ALERT_array);
    byte[] TEST_TRAILER_array = TEST_TRAILER.toBytes();

    // Place all into a CD1.1 frame.
    ByteBuffer CD11 = ByteBuffer.allocate(Cd11FrameHeader.FRAME_LENGTH +
        TEST_ALERT_array.length +
        TEST_TRAILER_array.length);
    CD11.put(TEST_HEADER.toBytes());
    CD11.put(TEST_ALERT_array);
    CD11.put(TEST_TRAILER_array);

    // Convert into input stream for testing.
    DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(CD11.array()));

    // Perform tests.
    Cd11Frame cd11Frame = FrameUtilitiesTest.readNextCd11Object(inputStream);
    Cd11AlertFrame alertFrame = (Cd11AlertFrame) cd11Frame;

    assertEquals(SIZE, alertFrame.size);
    assertEquals(MESSAGE, alertFrame.message);

    byte[] alertFrameBytes = alertFrame.toBytes();
    assertArrayEquals(CD11.array(), alertFrameBytes);
  }
}
