package gms.dataacquisition.stationreceiver.cd11.common.frames;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import org.junit.Test;


public class Cd11FrameTrailerTest {

  private static final int AUTH_KEY = 123;                  // [4] 0 - 3
  private static final int AUTH_SIZE = 8;                   // [4] 4 - 7
  private static final long AUTH_VALUE = 1512076158000l;    // [N] <dependent upon authSize>
  private static final long COMM_VERIFY = 1512076209000l;   // [8] <dependent upon authValue>

  private static ByteBuffer initTrailerSegment1() throws Exception {
    ByteBuffer TEST_TRAILER_SEGMENT1 = ByteBuffer.allocate(8);

    TEST_TRAILER_SEGMENT1.putInt(AUTH_KEY);
    TEST_TRAILER_SEGMENT1.putInt(AUTH_SIZE);

    return TEST_TRAILER_SEGMENT1;
  }

  private static ByteBuffer initTrailerSegment2() throws Exception {
    ByteBuffer TEST_TRAILER_SEGMENT2 = ByteBuffer.allocate(16);

    TEST_TRAILER_SEGMENT2.putLong(AUTH_VALUE);
    TEST_TRAILER_SEGMENT2.putLong(COMM_VERIFY);

    return TEST_TRAILER_SEGMENT2;
  }

  @Test
  public void testTrailerParsing() throws Exception {
    ByteBuffer TEST_TRAILER_SEGMENT1 = initTrailerSegment1();
    ByteBuffer TEST_TRAILER_SEGMENT2 = initTrailerSegment2();

    Cd11FrameTrailer frameTrailer = new Cd11FrameTrailer(TEST_TRAILER_SEGMENT1,
        TEST_TRAILER_SEGMENT2);

    assertEquals(AUTH_KEY, frameTrailer.authenticationKeyIdentifier);
    assertEquals(AUTH_SIZE, frameTrailer.authenticationSize);
    assertEquals(AUTH_VALUE, ByteBuffer.wrap(frameTrailer.authenticationValue).getLong());
    assertEquals(COMM_VERIFY, frameTrailer.commVerification);

    // Build a single byte buffer containing the full trailer.
    byte[] frameTrailerBytes = frameTrailer.toBytes();
    ByteBuffer TEST_TRAILER = ByteBuffer.allocate(Cd11FrameTrailer.MINIMUM_FRAME_LENGTH + 8);
    TEST_TRAILER.put(TEST_TRAILER_SEGMENT1.array());
    TEST_TRAILER.put(TEST_TRAILER_SEGMENT2.array());
    byte[] testTrailerBytes = TEST_TRAILER.array();

    assertArrayEquals(frameTrailerBytes, testTrailerBytes);
  }
}
