package gms.dataacquisition.stationreceiver.cd11.connman;

import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11AcknackFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11AlertFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionRequestFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionResponseFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11FrameHeader;

/**
 * Helper methods to create CD1.1 frame headers for different frame types. Created by trsault on
 * 11/30/17.
 */
public class FrameHeaderTestUtility {

  private static final String
      DEFAULT_FRAME_CREATOR = "creator ", // 8 character
      DEFAULT_FRAME_DESTINATION = "destnatn";  // 8 character

  public static Cd11FrameHeader createHeaderForConnectionRequest() throws Exception {
    return createHeaderForConnectionRequest(DEFAULT_FRAME_CREATOR, DEFAULT_FRAME_DESTINATION);
  }

  /**
   * Create a ConnectionRequest frame header with default sequence number.
   */
  public static Cd11FrameHeader createHeaderForConnectionRequest(String frameCreator,
      String frameDestination)
      throws Exception {

    FrameType frameType = FrameType.CONNECTION_REQUEST;
    int sequenceNumber = 0;  // ConnectionRequest frames do not have a sequence number.
    int trailerOffset = Cd11FrameHeader.FRAME_LENGTH + Cd11ConnectionRequestFrame.FRAME_LENGTH;

    return new Cd11FrameHeader(frameType, trailerOffset, frameCreator, frameDestination,
        sequenceNumber);
  }

  public static Cd11FrameHeader createHeaderForConnectionResponse() throws Exception {
    return createHeaderForConnectionResponse(DEFAULT_FRAME_CREATOR, DEFAULT_FRAME_DESTINATION);
  }

  /**
   * Create a CconnectionResponse frame header with default sequence number.
   */
  public static Cd11FrameHeader createHeaderForConnectionResponse(String frameCreator,
      String frameDestination) throws Exception {

    FrameType frameType = FrameType.CONNECTION_RESPONSE;
    int sequenceNumber = 0;  // ConnectionResponse frames do not have a sequence number.
    int trailerOffset = Cd11FrameHeader.FRAME_LENGTH + Cd11ConnectionResponseFrame.FRAME_LENGTH;

    return new Cd11FrameHeader(frameType, trailerOffset, frameCreator, frameDestination,
        sequenceNumber);
  }

  // NOT YET IMPLEMENTED // /**
  // NOT YET IMPLEMENTED //  * Create an OptionRequest frame header with default sequence number.
  // NOT YET IMPLEMENTED //  */
  // NOT YET IMPLEMENTED // public static Cd11FrameHeader createHeaderForOptionRequest(String frameCreator, String frameDestination, int series) throws Exception {
  // NOT YET IMPLEMENTED //   int frameType = FrameType.OPTION_REQUEST.getValue();
  // NOT YET IMPLEMENTED //   int sequenceNumber = 0;  // OptionRequest frames do not have a sequence number.
  // NOT YET IMPLEMENTED //   int trailerOffset = Cd11FrameHeader.FRAME_LENGTH + Cd11OptionRequestFrame.FRAME_LENGTH;
  // NOT YET IMPLEMENTED //   return new Cd11FrameHeader(frameType, trailerOffset, frameCreator, frameDestination, sequenceNumber);
  // NOT YET IMPLEMENTED // }

  // NOT YET IMPLEMENTED // /**
  // NOT YET IMPLEMENTED //  * Create an OptionResponse frame header with default sequence number.
  // NOT YET IMPLEMENTED //  */
  // NOT YET IMPLEMENTED // public static Cd11FrameHeader createHeaderForOptionResponse(String frameCreator, String frameDestination, int series) throws Exception {
  // NOT YET IMPLEMENTED //   int frameType = FrameType.OPTION_RESPONSE.getValue();
  // NOT YET IMPLEMENTED //   int sequenceNumber = 0;  // OptionResponse frames do not have a sequence number.
  // NOT YET IMPLEMENTED //   int trailerOffset = Cd11FrameHeader.FRAME_LENGTH + Cd11OptionResponseFrame.FRAME_LENGTH;
  // NOT YET IMPLEMENTED //   return new Cd11FrameHeader(frameType, trailerOffset, frameCreator, frameDestination, sequenceNumber);
  // NOT YET IMPLEMENTED // }

  public static Cd11FrameHeader createHeaderForAcknack() throws Exception {
    return createHeaderForAcknack(DEFAULT_FRAME_CREATOR, DEFAULT_FRAME_DESTINATION, 0);
  }

  /**
   * Create an Acknack frame header with default sequence number using number of gaps to calculate
   * trailer offset.
   */
  public static Cd11FrameHeader createHeaderForAcknack(String frameCreator, String frameDestination,
      int gapCount)
      throws Exception {

    FrameType frameType = FrameType.ACKNACK;
    int sequenceNumber = 0;  // Acknack frames do not have a sequence number.
    int trailerOffset = Cd11FrameHeader.FRAME_LENGTH +
        Cd11AcknackFrame.MINIMUM_FRAME_LENGTH +
        (gapCount * Cd11AcknackFrame.SIZE_PER_GAP);

    return new Cd11FrameHeader(frameType, trailerOffset, frameCreator, frameDestination,
        sequenceNumber);
  }

  public static Cd11FrameHeader createHeaderForData(int trailerOffset) throws Exception {
    return createHeaderForData(trailerOffset, DEFAULT_FRAME_CREATOR,
        DEFAULT_FRAME_DESTINATION, 0);
  }

  public static Cd11FrameHeader createHeaderForData(int trailerOffset, String frameCreator,
      String frameDestination, long sequenceNumber) throws Exception {

    FrameType frameType = FrameType.DATA;
    return new Cd11FrameHeader(frameType, trailerOffset, frameCreator, frameDestination,
        sequenceNumber);
  }

  public static Cd11FrameHeader createHeaderForAlert(int paddedMessageLength) throws Exception {
    return createHeaderForAlert(DEFAULT_FRAME_CREATOR, DEFAULT_FRAME_DESTINATION,
        paddedMessageLength);
  }

  public static Cd11FrameHeader createHeaderForAlert(String frameCreator, String frameDestination,
      int paddedMessageLength) throws Exception {

    FrameType frameType = FrameType.ALERT;
    int sequenceNumber = 0;  // Protocol doesn't say, but assuming Alert frames do not have a sequence number.
    int trailerOffset = Cd11FrameHeader.FRAME_LENGTH +
        Cd11AlertFrame.MINIMUM_FRAME_LENGTH +
        paddedMessageLength;

    return new Cd11FrameHeader(frameType, trailerOffset, frameCreator, frameDestination,
        sequenceNumber);
  }
}
