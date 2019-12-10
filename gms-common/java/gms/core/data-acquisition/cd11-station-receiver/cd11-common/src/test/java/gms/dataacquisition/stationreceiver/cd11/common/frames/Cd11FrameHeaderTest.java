package gms.dataacquisition.stationreceiver.cd11.common.frames;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame.FrameType;
import java.nio.ByteBuffer;
import org.junit.Test;


public class Cd11FrameHeaderTest {

  private static final FrameType FRAME_TYPE = FrameType.CONNECTION_REQUEST;                     // [4] 0 - 3
  private static final int TRAILER_OFFSET = 68;                 // [4] 4 - 7
  public static final String CREATOR = "XDCXDC_1";             // [8] 8 - 13
  public static final String DESTINATION = "XDCXDC_2";         // [8] 14 - 21
  private static final long SEQUENCE_NUMBER = 1512074377000l;  // [8] 22 - 29
  public static final int SERIES = 123;                        // [4] 30 - 33

  private static ByteBuffer initHeader() throws Exception {
    ByteBuffer TEST_HEADER = ByteBuffer.allocate(Cd11FrameHeader.FRAME_LENGTH);

    TEST_HEADER.putInt(FRAME_TYPE.getValue());
    TEST_HEADER.putInt(TRAILER_OFFSET);
    TEST_HEADER.put(CREATOR.getBytes());
    TEST_HEADER.put(DESTINATION.getBytes());
    TEST_HEADER.putLong(SEQUENCE_NUMBER);
    TEST_HEADER.putInt(SERIES);

    return TEST_HEADER;
  }

  @Test
  public void testHeaderParsing() throws Exception {
    ByteBuffer TEST_HEADER = initHeader();

    Cd11FrameHeader frameHeader = new Cd11FrameHeader(TEST_HEADER);

    assertEquals(FRAME_TYPE, frameHeader.frameType);
    assertEquals(TRAILER_OFFSET, frameHeader.trailerOffset);
    assertEquals(CREATOR, frameHeader.frameCreator);
    assertEquals(DESTINATION, frameHeader.frameDestination);
    assertEquals(SEQUENCE_NUMBER, frameHeader.sequenceNumber);
    assertEquals(SERIES, frameHeader.series);

    byte[] frameHeaderBytes = frameHeader.toBytes();
    assertArrayEquals(frameHeaderBytes, TEST_HEADER.array());
  }
}
