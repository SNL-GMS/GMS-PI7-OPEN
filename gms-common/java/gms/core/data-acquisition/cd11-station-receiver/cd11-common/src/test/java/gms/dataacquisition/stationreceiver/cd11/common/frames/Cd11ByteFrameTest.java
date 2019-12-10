package gms.dataacquisition.stationreceiver.cd11.common.frames;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import org.junit.BeforeClass;
import org.junit.Test;

public class Cd11ByteFrameTest {

  private static final int FRAME_TYPE = 10;                     // [4] 0 - 3
  private static final int TRAILER_OFFSET = 68;                 // [4] 4 - 7
  public static final String CREATOR = "XDCXDC_1";             // [8] 8 - 13
  public static final String DESTINATION = "XDCXDC_2";         // [8] 14 - 21
  private static final long SEQUENCE_NUMBER = 1512074377000l;  // [8] 22 - 29
  public static final int SERIES = 123;// [4] 30 - 33
  private static ByteBuffer TEST_BAD_HEADER;

  @BeforeClass
  public static void initBadHeader() throws Exception {
    TEST_BAD_HEADER = ByteBuffer.allocate(Cd11FrameHeader.FRAME_LENGTH - Integer.BYTES);

    TEST_BAD_HEADER.putInt(FRAME_TYPE);
    TEST_BAD_HEADER.putInt(TRAILER_OFFSET);
    TEST_BAD_HEADER.put(CREATOR.getBytes());
    TEST_BAD_HEADER.put(DESTINATION.getBytes());
    TEST_BAD_HEADER.putLong(SEQUENCE_NUMBER);
    //TEST_BAD_HEADER.putInt(SERIES);   // omitted so the header is invalid
  }

  @Test(expected = Exception.class)
  public void testBadHeader() throws Exception {
    long endTimeNs = System.nanoTime() + (5000 * 1000 * 1000);

    DataInputStream inputStream = new DataInputStream(
        new ByteArrayInputStream(TEST_BAD_HEADER.array()));
    new Cd11ByteFrame(inputStream, () -> (System.nanoTime() >= endTimeNs));
  }
}
