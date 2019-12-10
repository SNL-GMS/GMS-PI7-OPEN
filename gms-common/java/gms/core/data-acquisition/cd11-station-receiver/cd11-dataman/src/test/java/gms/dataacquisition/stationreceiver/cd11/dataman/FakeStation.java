package gms.dataacquisition.stationreceiver.cd11.dataman;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Socket;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;

/**
 * This class exists to simply connect to a socket and send 1 Cd11DataFrame. Cd11DataConsumerTest
 * spawns off this class as a separate thread so it can mimic multiple stations sending data frames
 * at the same time.
 */
public class FakeStation implements Runnable {

  private Cd11Socket cd11Socket;
  private Cd11ChannelSubframe[] sfArray;
  private final String dataConsumerWellKnownIpAddress;
  private final Integer dataConsumerAssignedPort;

  public FakeStation(
      Cd11Socket cd11Socket, Cd11ChannelSubframe[] sfArray, String ip, Integer port) {
    this.cd11Socket = cd11Socket;
    this.sfArray = sfArray;
    this.dataConsumerWellKnownIpAddress = ip;
    this.dataConsumerAssignedPort = port;
  }

  @Override
  public void run() {
    try {
      cd11Socket.connect(
          this.dataConsumerWellKnownIpAddress, this.dataConsumerAssignedPort, 500);
      cd11Socket.sendCd11DataFrame(sfArray, 1);
    } catch (Exception e) {
    } finally {
      cd11Socket.disconnect();
    }
  }
}
