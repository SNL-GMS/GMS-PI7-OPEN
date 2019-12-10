package gms.dataacquisition.stationreceiver.cd11.common.frames;

/* NOT YET IMPLEMENTED

public class Cd11OptionRequestFrameTest {

    private static final int OPTION_COUNT = 1;      // [4] 0 - 3
    private static final int OPTION_TYPE = 1;       // [4] 4 - 7
    private static final int OPTION_SIZE = 8;       // [4] 8 - 11
    private static final String OPTION_REQUEST = "STA1    "; // [8] 12 - 19

    public ByteBuffer initOptionReq() throws Exception {
        ByteBuffer TEST_OPTION_REQ = ByteBuffer.allocate(
                Cd11OptionResponseFrame.FRAME_LENGTH);

        TEST_OPTION_REQ.putInt(OPTION_COUNT);
        TEST_OPTION_REQ.putInt(OPTION_TYPE);
        TEST_OPTION_REQ.putInt(OPTION_SIZE);
        TEST_OPTION_REQ.put(OPTION_REQUEST.getBytes());

        return TEST_OPTION_REQ;
    }

    @Test
    public void testOptionReq() throws Exception {
        ByteBuffer TEST_OPTION_REQ = initOptionReq();

        TEST_OPTION_REQ.position(0);
        assertEquals(TEST_OPTION_REQ.getInt(), OPTION_COUNT);
        assertEquals(TEST_OPTION_REQ.getInt(), OPTION_TYPE);
        assertEquals(TEST_OPTION_REQ.getInt(), OPTION_SIZE);

        byte[] option_request = new byte[8];
        TEST_OPTION_REQ.get(option_request);
        String location = new String(option_request);
        assertEquals(OPTION_REQUEST, location);
    }

    @Test
    public void testOptionReqParsing() throws Exception {

        // Create header, body, and trailer.
        Cd11FrameHeader TEST_HEADER = FrameHeaderTestUtility.createHeaderForOptionRequest(
                Cd11FrameHeaderTest.CREATOR, Cd11FrameHeaderTest.DESTINATION, Cd11FrameHeaderTest.SERIES);

        ByteBuffer TEST_OPTION_REQ = initOptionReq();
        byte[] TEST_OPTION_REQ_array = TEST_OPTION_REQ.array();

        Cd11FrameTrailer TEST_TRAILER = FrameTrailerTestUtility.createTrailerWithoutAuthentication(
                TEST_HEADER, TEST_OPTION_REQ_array);
        byte[] TEST_TRAILER_array = TEST_TRAILER.toBytes();

        // Place all into a CD1.1 frame.
        ByteBuffer CD11 = ByteBuffer.allocate(Cd11FrameHeader.FRAME_LENGTH +
                Cd11OptionRequestFrame.FRAME_LENGTH +
                TEST_TRAILER_array.length);
        CD11.put(TEST_HEADER.toBytes());
        CD11.put(TEST_OPTION_REQ_array);
        CD11.put(TEST_TRAILER_array);

        // Convert into input stream for testing.
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(CD11.array()));

        // Perform tests.
        Cd11Frame cd11Frame = FrameUtilitiesTest.readNextCd11Object(inputStream);
        Cd11OptionRequestFrame requestFrame = (Cd11OptionRequestFrame) cd11Frame;

        assertEquals(requestFrame.optionCount, OPTION_COUNT);
        assertEquals(requestFrame.optionType, OPTION_TYPE);
        assertEquals(requestFrame.optionSize, OPTION_SIZE);
        assertEquals(OPTION_REQUEST, new String(requestFrame.optionRequest));

        byte[] requestFrameBytes = requestFrame.toBytes();
        assertArrayEquals(CD11.array(), requestFrameBytes);
    }
}
*/
