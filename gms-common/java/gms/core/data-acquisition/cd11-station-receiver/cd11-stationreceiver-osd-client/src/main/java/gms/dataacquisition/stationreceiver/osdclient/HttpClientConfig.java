package gms.dataacquisition.stationreceiver.osdclient;

public class HttpClientConfig {

  public final String hostname;
  public final int port;

  public HttpClientConfig(String hostname, int port) {
    this.hostname = hostname;
    this.port = port;
  }

  public String asUrl() {
    return "http://" + this.hostname + ":" + this.port;
  }
}
