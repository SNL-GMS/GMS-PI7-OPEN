package gms.dataacquisition.stationreceiver.cd11.common.frames;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilitiesTest;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import org.junit.Test;


public class Cd11AcknackFrameTest {

  private static final String FRAMESET_ACKED = "STA12345678901234567";  // [20] 0 - 19
  private static final long LOWEST_SEQ_NUM = 1512074377000l;            // [8] 20 - 27
  private static final long HIGHEST_SEQ_NUM = 1512076209000l;           // [8] 28 - 35
  private static final int GAP_COUNT = 2;                               // [4] 36 - 39
  private static final long[] GAPS = new long[]{1, 2, 3, 4};           // [32] 40 - 71

  private static ByteBuffer initAcknack() throws Exception {
    ByteBuffer TEST_ACKNACK = ByteBuffer.allocate(Cd11AcknackFrame.MINIMUM_FRAME_LENGTH + 32);

    TEST_ACKNACK.put(FRAMESET_ACKED.getBytes());
    TEST_ACKNACK.putLong(LOWEST_SEQ_NUM);
    TEST_ACKNACK.putLong(HIGHEST_SEQ_NUM);
    TEST_ACKNACK.putInt(GAP_COUNT);
    for (long l : GAPS) {
      TEST_ACKNACK.putLong(l);
    }

    return TEST_ACKNACK;
  }

  @Test
  public void testAcknackParsing() throws Exception {

    // Create header, body, and trailer.
    Cd11FrameHeader TEST_HEADER = FrameHeaderTestUtility.createHeaderForAcknack(
        Cd11FrameHeaderTest.CREATOR, Cd11FrameHeaderTest.DESTINATION, GAP_COUNT);

    ByteBuffer TEST_ACKNACK = initAcknack();
    byte[] TEST_ACKNACK_array = TEST_ACKNACK.array();

    Cd11FrameTrailer TEST_TRAILER = FrameTrailerTestUtility.createTrailerWithoutAuthentication(
        TEST_HEADER, TEST_ACKNACK_array);
    byte[] TEST_TRAILER_array = TEST_TRAILER.toBytes();

    // Place all into a CD1.1 frame.
    ByteBuffer CD11 = ByteBuffer.allocate(Cd11FrameHeader.FRAME_LENGTH +
        TEST_ACKNACK_array.length +
        TEST_TRAILER_array.length);
    CD11.put(TEST_HEADER.toBytes());
    CD11.put(TEST_ACKNACK_array);
    CD11.put(TEST_TRAILER_array);

    // Convert into input stream for testing.
    DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(CD11.array()));

    // Perform tests.
    Cd11Frame cd11Frame = FrameUtilitiesTest.readNextCd11Object(inputStream);
    Cd11AcknackFrame acknackFrame = (Cd11AcknackFrame) cd11Frame;

    assertEquals(FRAMESET_ACKED, acknackFrame.framesetAcked);
    assertEquals(acknackFrame.highestSeqNum, HIGHEST_SEQ_NUM);
    assertEquals(acknackFrame.lowestSeqNum, LOWEST_SEQ_NUM);
    assertEquals(acknackFrame.gapCount, GAP_COUNT);
    assertArrayEquals(acknackFrame.gapRanges, GAPS);

    byte[] acknackFrameBytes = acknackFrame.toBytes();
    assertArrayEquals(CD11.array(), acknackFrameBytes);
  }
}
