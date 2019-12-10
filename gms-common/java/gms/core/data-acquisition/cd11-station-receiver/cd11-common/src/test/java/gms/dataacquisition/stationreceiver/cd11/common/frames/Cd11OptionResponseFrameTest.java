package gms.dataacquisition.stationreceiver.cd11.common.frames;

/* NOT YET IMPLEMENTED

public class Cd11OptionResponseFrameTest {

    private static final int OPTION_COUNT = 1;      // [4] 0 - 3
    private static final int OPTION_TYPE = 1;       // [4] 4 - 7
    private static final int OPTION_SIZE = 8;       // [4] 8 - 11
    private static final String OPTION_RESPONSE = "STA1    "; // [8] 12 - 19

    public ByteBuffer initOptionResp() throws Exception {
        ByteBuffer TEST_OPTION_REQ = ByteBuffer.allocate(
                Cd11OptionResponseFrame.FRAME_LENGTH);

        TEST_OPTION_REQ.putInt(OPTION_COUNT);
        TEST_OPTION_REQ.putInt(OPTION_TYPE);
        TEST_OPTION_REQ.putInt(OPTION_SIZE);
        TEST_OPTION_REQ.put(OPTION_RESPONSE.getBytes());

        return TEST_OPTION_REQ;
    }

    @Test
    public void testOptionResp() throws Exception {
        ByteBuffer TEST_OPTION_RESP = initOptionResp();

        TEST_OPTION_RESP.position(0);
        assertEquals(TEST_OPTION_RESP.getInt(), OPTION_COUNT);
        assertEquals(TEST_OPTION_RESP.getInt(), OPTION_TYPE);
        assertEquals(TEST_OPTION_RESP.getInt(), OPTION_SIZE);

        byte[] option_request = new byte[8];
        TEST_OPTION_RESP.get(option_request);
        String location = new String(option_request);
        assertEquals(OPTION_RESPONSE, location);
    }

    @Test
    public void testOptionRespParsing() throws Exception {

        // Create header, body, and trailer.
        Cd11FrameHeader TEST_HEADER = FrameHeaderTestUtility.createHeaderForOptionResponse(
                Cd11FrameHeaderTest.CREATOR, Cd11FrameHeaderTest.DESTINATION, Cd11FrameHeaderTest.SERIES);

        ByteBuffer TEST_OPTION_RESP = initOptionResp();
        byte[] TEST_OPTION_RESP_array = TEST_OPTION_RESP.array();

        Cd11FrameTrailer TEST_TRAILER = FrameTrailerTestUtility.createTrailerWithoutAuthentication(
                TEST_HEADER, TEST_OPTION_RESP_array);
        byte[] TEST_TRAILER_array = TEST_TRAILER.toBytes();

        // Place all into a CD1.1 frame.
        ByteBuffer CD11 = ByteBuffer.allocate(Cd11FrameHeader.FRAME_LENGTH +
                Cd11OptionResponseFrame.FRAME_LENGTH +
                TEST_TRAILER_array.length);
        CD11.put(TEST_HEADER.toBytes());
        CD11.put(TEST_OPTION_RESP_array);
        CD11.put(TEST_TRAILER_array);

        // Convert into input stream for testing.
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(CD11.array()));

        // Perform tests.
        Cd11Frame cd11Frame = FrameUtilitiesTest.readNextCd11Object(inputStream);
        Cd11OptionResponseFrame responseFrame = (Cd11OptionResponseFrame) cd11Frame;

        assertEquals(responseFrame.optionCount, OPTION_COUNT);
        assertEquals(responseFrame.optionType, OPTION_TYPE);
        assertEquals(responseFrame.optionSize, OPTION_SIZE);
        assertEquals(OPTION_RESPONSE, new String(responseFrame.optionResponse));

        byte[] requestFrameBytes = responseFrame.toBytes();
        assertArrayEquals(CD11.array(), requestFrameBytes);
    }

}
*/
