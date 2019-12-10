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


public class Cd11ConnectionResponseFrameTest {

  private static final short MAJOR_VERSION = 1;         // [2] 0 - 1
  private static final short MINOR_VERSION = 1;         // [2] 2 - 3
  private static final String RESPONDER = "STA12345";   // [8] 4 - 11
  private static final String RESPONDER_TYPE = "IMS\0";  // [4] 12 - 15
  private static final String SERVICE_TYPE = "TCP\0";    // [4] 16 - 19
  private static final String IP = "127.0.0.1";      // [4] 20 - 23
  private static final int PORT = 8080;            // [2] 24 - 25
  private static final String IP2 = "192.168.0.1";   // [4] 26 - 29
  private static final int PORT2 = 8181;           // [2] 30 - 31

  private static ByteBuffer initConnResp() throws Exception {
    ByteBuffer TEST_CONN_RESP = ByteBuffer.allocate(Cd11ConnectionResponseFrame.FRAME_LENGTH);

    TEST_CONN_RESP.putShort(MAJOR_VERSION);
    TEST_CONN_RESP.putShort(MINOR_VERSION);
    TEST_CONN_RESP.put(RESPONDER.getBytes());
    TEST_CONN_RESP.put(RESPONDER_TYPE.getBytes());
    TEST_CONN_RESP.put(SERVICE_TYPE.getBytes());
    TEST_CONN_RESP.putInt((ByteBuffer.wrap(InetAddress.getByName(IP).getAddress())).getInt());
    TEST_CONN_RESP.putChar((char) PORT);
    TEST_CONN_RESP.putInt((ByteBuffer.wrap(InetAddress.getByName(IP2).getAddress())).getInt());
    TEST_CONN_RESP.putChar((char) PORT2);

    return TEST_CONN_RESP;
  }

  @Test
  public void testConnRespParsing() throws Exception {

    // Create header, body, and trailer.
    Cd11FrameHeader TEST_HEADER = FrameHeaderTestUtility.createHeaderForConnectionResponse(
        Cd11FrameHeaderTest.CREATOR, Cd11FrameHeaderTest.DESTINATION);

    ByteBuffer TEST_CONN_RESP = initConnResp();

    Cd11FrameTrailer TEST_TRAILER = FrameTrailerTestUtility.createTrailerWithoutAuthentication(
        TEST_HEADER, TEST_CONN_RESP.array());
    byte[] TEST_TRAILER_array = TEST_TRAILER.toBytes();

    // Place all into a CD1.1 frame.
    ByteBuffer CD11 = ByteBuffer.allocate(Cd11FrameHeader.FRAME_LENGTH +
        Cd11ConnectionResponseFrame.FRAME_LENGTH + TEST_TRAILER_array.length);
    CD11.put(TEST_HEADER.toBytes());
    CD11.put(TEST_CONN_RESP.array());
    CD11.put(TEST_TRAILER_array);

    // Convert into input stream for testing.
    DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(CD11.array()));

    // Perform tests.
    Cd11Frame cd11Frame = FrameUtilitiesTest.readNextCd11Object(inputStream);
    Cd11ConnectionResponseFrame responseFrame = (Cd11ConnectionResponseFrame) cd11Frame;

    assertEquals(MAJOR_VERSION, responseFrame.majorVersion);
    assertEquals(MINOR_VERSION, responseFrame.minorVersion);
    assertEquals(RESPONDER, responseFrame.responderName);
    assertEquals(RESPONDER_TYPE.trim(), responseFrame.responderType.trim());
    assertEquals(SERVICE_TYPE.trim(), responseFrame.serviceType.trim());
    assertEquals(IP, FrameUtilities.intToIpAddressString(responseFrame.ipAddress));
    assertEquals(PORT, responseFrame.port);
    assertEquals(IP2, FrameUtilities.intToIpAddressString(responseFrame.secondIpAddress));
    assertEquals(PORT2, responseFrame.secondPort);

    byte[] responseFrameBytes = responseFrame.toBytes();
    assertArrayEquals(responseFrameBytes, CD11.array());
  }
}
