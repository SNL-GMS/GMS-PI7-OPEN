package gms.dataacquisition.stationreceiver.cd11.common.frames;


public class FrameTrailerTestUtility {

  public static Cd11FrameTrailer createTrailerWithoutAuthentication(Cd11FrameHeader header,
      byte[] payload)
      throws Exception {

    return new Cd11FrameTrailer(0, new byte[0]);
  }
}
