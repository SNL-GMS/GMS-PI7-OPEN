package gms.dataacquisition.stationreceiver.cd11.connman;

import java.time.LocalDateTime;

public class ConnectionLog {

  public final LocalDateTime timestamp;
  public final String connectingIpAddress;
  public final int connectingPort;
  public final String connectingStationName;
  public final boolean isValidConnectionRequest;

  public ConnectionLog(
      String connectingIpAddress,
      int connectingPort,
      String connectingStationName,
      boolean isValidConnectionRequest) {
    this.timestamp = LocalDateTime.now();
    this.connectingIpAddress = connectingIpAddress;
    this.connectingPort = connectingPort;
    this.connectingStationName = connectingStationName;
    this.isValidConnectionRequest = isValidConnectionRequest;
  }
}
