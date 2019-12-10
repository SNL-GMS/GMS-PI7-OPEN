package gms.dataacquisition.stationreceiver.cd11.connman;


class Cd11Station {
  public final String expectedDataProviderIpAddress;
  public final String dataConsumerIpAddress;
  public final int dataConsumerPort;

  Cd11Station(
      String expectedDataProviderIpAddress,
      String dataConsumerIpAddress,
      int dataConsumerPort) {
    this.expectedDataProviderIpAddress = expectedDataProviderIpAddress;
    this.dataConsumerIpAddress = dataConsumerIpAddress;
    this.dataConsumerPort = dataConsumerPort;
  }
}
