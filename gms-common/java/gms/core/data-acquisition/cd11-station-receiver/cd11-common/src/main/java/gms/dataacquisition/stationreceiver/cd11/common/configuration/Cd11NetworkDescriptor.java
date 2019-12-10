package gms.dataacquisition.stationreceiver.cd11.common.configuration;

import java.util.UUID;

public class Cd11NetworkDescriptor {

  public final UUID osdStationId;
  public final String stationName;
  //public final InetAddress dataConsumerIpAddress; // TODO: In the future, this should come from the OSD (for now, it comes from DataManConfig and Cd11ConnectionConfig).
  public final int dataConsumerPort;
  //public final InetAddress dataProviderIpAddress; // TODO: In the future, this should come from the OSD (for now, it comes from DataManConfig and Cd11ConnectionConfig).

  public Cd11NetworkDescriptor(
      UUID osdStationId, String stationName,
      //InetAddress dataConsumerIpAddress,
      int dataConsumerPort
      //InetAddress ipAddress
  ) {
    this.osdStationId = osdStationId;
    this.stationName = stationName;
    //this.dataConsumerIpAddress = dataConsumerIpAddress;
    this.dataConsumerPort = dataConsumerPort;
    //this.dataProviderIpAddress = ipAddress;
  }
}
