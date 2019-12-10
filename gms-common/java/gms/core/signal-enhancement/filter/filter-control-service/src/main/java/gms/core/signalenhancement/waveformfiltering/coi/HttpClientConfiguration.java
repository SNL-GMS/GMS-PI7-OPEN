package gms.core.signalenhancement.waveformfiltering.coi;

import java.util.Objects;

/**
 * Contains configuration used by a client to access an HTTP services.
 */
public class HttpClientConfiguration {

  private final String host;
  private final int port;
  private final String basePath;

  private HttpClientConfiguration(String host, int port, String basePath) {
    this.host = host;
    this.port = port;
    this.basePath = basePath;
  }

  /**
   * Obtains a new {@link HttpClientConfiguration} from the provided service address (host, port,
   * and basePath).  basePath must start with a / and must not end with a / (e.g. /base/path is
   * valid but base/path and base/path/ are not)
   *
   * @param host server hostname, not null
   * @param port server port, must be greater than or equal to 0 and less than or equal to 65535
   * @param basePath service base path, not null
   * @return HttpClientConfiguration, not null
   * @throws NullPointerException if host or basePath are null
   * @throws IllegalArgumentException if port is outside the valid range [0, 65535]
   */
  public static HttpClientConfiguration create(String host, int port, String basePath) {
    Objects.requireNonNull(host, "HttpClientConfiguration requires non-null host");
    Objects.requireNonNull(basePath, "HttpClientConfiguration requires non-null basePath");

    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException(
          "HttpClientConfiguration must use a port between 0 and 65535");
    }

    if (!basePath.startsWith("/") || basePath.endsWith("/")) {
      throw new IllegalArgumentException(
          "HttpClientConfiguration must have a basePath beginning with a / and not ending with a /");
    }

    return new HttpClientConfiguration(host, port, basePath);
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getBasePath() {
    return basePath;
  }
}
