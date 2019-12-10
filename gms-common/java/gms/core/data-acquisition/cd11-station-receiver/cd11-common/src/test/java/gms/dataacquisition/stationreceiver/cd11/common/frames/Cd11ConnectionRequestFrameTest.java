package gms.dataacquisition.stationreceiver.cd11.common.frames;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilitiesTest;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import org.junit.Test;


public class Cd11ConnectionRequestFrameTest {

  private static final short MAJOR_VERSION = 1;      // [2] 0 - 1
  private static final short MINOR_VERSION = 1;      // [2] 2 - 3
  private static final String STATION = "STA12345";  // [8] 4 - 11
  private static final String STATION_TYPE = "IMS\0"; // [4] 12 - 15
  private static final String SERVICE_TYPE = "TCP\0"; // [4] 16 - 19
  private static final String IP = "127.0.0.1";      // [4] 20 - 23
  private static final int PORT = 8080;            // [2] 24 - 25
  private static final String IP2 = "192.168.0.1";   // [4] 26 - 29
  private static final int PORT2 = 8181;           // [2] 30 - 31

  private static ByteBuffer initConnReq() throws Exception {
    ByteBuffer TEST_CONN_REQ = ByteBuffer.allocate(Cd11ConnectionRequestFrame.FRAME_LENGTH);

    TEST_CONN_REQ.putShort(MAJOR_VERSION);
    TEST_CONN_REQ.putShort(MINOR_VERSION);
    TEST_CONN_REQ.put(STATION.getBytes());
    TEST_CONN_REQ.put(STATION_TYPE.getBytes());
    TEST_CONN_REQ.put(SERVICE_TYPE.getBytes());
    TEST_CONN_REQ.putInt((ByteBuffer.wrap(InetAddress.getByName(IP).getAddress())).getInt());
    TEST_CONN_REQ.putChar((char) PORT);
    TEST_CONN_REQ.putInt((ByteBuffer.wrap(InetAddress.getByName(IP2).getAddress())).getInt());
    TEST_CONN_REQ.putChar((char) PORT2);

    return TEST_CONN_REQ;
  }

  @Test
  public void testConnReqParsing() throws Exception {

    // Create header, body, and trailer.
    Cd11FrameHeader TEST_HEADER = FrameHeaderTestUtility.createHeaderForConnectionRequest(
        Cd11FrameHeaderTest.CREATOR, Cd11FrameHeaderTest.DESTINATION);

    ByteBuffer TEST_CONN_REQ = initConnReq();

    Cd11FrameTrailer TEST_TRAILER = FrameTrailerTestUtility.createTrailerWithoutAuthentication(
        TEST_HEADER, TEST_CONN_REQ.array());
    byte[] TEST_TRAILER_array = TEST_TRAILER.toBytes();

    // Place all into a CD1.1 frame.
    ByteBuffer CD11 = ByteBuffer.allocate(Cd11FrameHeader.FRAME_LENGTH +
        Cd11ConnectionRequestFrame.FRAME_LENGTH + TEST_TRAILER_array.length);
    CD11.put(TEST_HEADER.toBytes());
    CD11.put(TEST_CONN_REQ.array());
    CD11.put(TEST_TRAILER_array);

    // Convert into input stream for testing.
    DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(CD11.array()));

    // Perform tests.
    Cd11Frame cd11Frame = FrameUtilitiesTest.readNextCd11Object(inputStream);
    Cd11ConnectionRequestFrame requestFrame = (Cd11ConnectionRequestFrame) cd11Frame;

    assertEquals(MAJOR_VERSION, requestFrame.majorVersion);
    assertEquals(MINOR_VERSION, requestFrame.minorVersion);
    assertEquals(STATION, requestFrame.stationName);
    assertEquals(STATION_TYPE.trim(), requestFrame.stationType.trim());
    assertEquals(SERVICE_TYPE.trim(), requestFrame.serviceType.trim());
    assertEquals(IP, FrameUtilities.intToIpAddressString(requestFrame.ipAddress));
    assertEquals(PORT, requestFrame.port);
    assertEquals(IP2, FrameUtilities.intToIpAddressString(requestFrame.secondIpAddress));
    assertEquals(PORT2, requestFrame.secondPort);

    byte[] requestFrameBytes = requestFrame.toBytes();
    assertArrayEquals(requestFrameBytes, CD11.array());
  }

}