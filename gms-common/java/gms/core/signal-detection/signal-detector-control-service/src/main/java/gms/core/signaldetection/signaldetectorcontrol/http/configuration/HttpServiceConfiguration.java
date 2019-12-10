package gms.core.signaldetection.signaldetectorcontrol.http.configuration;

import java.util.Objects;
import java.util.function.BiPredicate;

public class HttpServiceConfiguration {

  static final int DEFAULT_PORT = 8080;
  static final int DEFAULT_MIN_THREADS = 2;
  static final int DEFAULT_MAX_THREADS = 13;
  static final int DEFAULT_IDLE_TIMEOUT_MILLIS = 30000;
  static final String DEFAULT_BASE_URL = "/signal-detection/signal-detector/";

  private final String baseUrl;
  private final int port;
  private final int minThreads;
  private final int maxThreads;
  private final int idleTimeOutMillis;

  private HttpServiceConfiguration(String baseUrl, int port, int minThreads,
      int maxThreads, int idleTimeOutMillis) {

    this.baseUrl = baseUrl;
    this.port = port;
    this.minThreads = minThreads;
    this.maxThreads = maxThreads;
    this.idleTimeOutMillis = idleTimeOutMillis;
  }

  public String getBaseUrl() {
    return this.baseUrl;
  }

  public int getPort() {
    return port;
  }

  public int getMinThreads() {
    return minThreads;
  }

  public int getMaxThreads() {
    return maxThreads;
  }

  public int getIdleTimeOutMillis() {
    return idleTimeOutMillis;
  }

  /**
   * Obtains an instance of {@link Builder}
   *
   * @return a Builder, not null
   */
  public static Builder builder() {
    return new Builder();
  }

  private static <A, B> void validateParameter(BiPredicate<A, B> test, A a, B b, String message) {
    if (test.test(a, b)) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Constructs instances of {@link HttpServiceConfiguration} following the builder pattern
   */
  public static class Builder {

    private String baseUrl;
    private int port;
    private int minThreads;
    private int maxThreads;
    private int idleTimeOutMillis;

    private Builder() {
      this.baseUrl = DEFAULT_BASE_URL;
      this.port = DEFAULT_PORT;
      this.minThreads = DEFAULT_MIN_THREADS;
      this.maxThreads = DEFAULT_MAX_THREADS;
      this.idleTimeOutMillis = DEFAULT_IDLE_TIMEOUT_MILLIS;
    }

    /**
     * Construct the {@link HttpServiceConfiguration}
     *
     * @return HttpServiceConfiguration built from this {@link Builder}, not null
     * @throws IllegalArgumentException if minThreads, maxThreads, idleTimeOutMillis, or port are
     * negative; if minThreads is greater than maxThreads; if port is beyond the valid range
     */
    public HttpServiceConfiguration build() {

      // Passes if a is strictly less than b
      final BiPredicate<Integer, Integer> lessThan = (a, b) -> Integer.compare(a, b) < 0;

      validateParameter(lessThan, port, 0,
          "HttpServiceConfiguration must use a port between 0 and 65535");
      validateParameter(lessThan, 65536, port,
          "HttpServiceConfiguration must use a port between 0 and 65535");
      validateParameter(lessThan, minThreads, 1,
          "HttpServiceConfiguration minThreads must be >= 1");
      validateParameter(lessThan, maxThreads, 1,
          "HttpServiceConfiguration maxThreads must be >= 1");
      validateParameter(lessThan, idleTimeOutMillis, 1,
          "HttpServiceConfiguration idleTimeOutMillis must be >= 1");
      validateParameter(lessThan, maxThreads, minThreads,
          "HttpServiceConfiguration cannot have maxThreads less than minThreads");

      return new HttpServiceConfiguration(baseUrl, port,
          minThreads, maxThreads, idleTimeOutMillis);
    }

    /**
     * Set the service's base URL
     *
     * @param baseUrl service's base URL, not null
     * @return this {@link Builder}
     * @throws NullPointerException if the baseUrl parameter is null
     */
    public Builder setBaseUrl(String baseUrl) {
      this.baseUrl = Objects
          .requireNonNull(baseUrl, "HttpServiceConfiguration cannot have null baseUrl");
      return this;
    }

    /**
     * Set the service's port
     *
     * @param port service's port
     * @return this {@link Builder}
     */
    public Builder setPort(int port) {
      this.port = port;
      return this;
    }

    /**
     * Set the service's minimum number of threads
     *
     * @param minThreads service's minimum number of threads
     * @return this {@link Builder}
     */
    public Builder setMinThreads(int minThreads) {
      this.minThreads = minThreads;
      return this;
    }

    /**
     * Set the service's maximum number of threads
     *
     * @param maxThreads service's maximum number of threads
     * @return this {@link Builder}
     */
    public Builder setMaxThreads(int maxThreads) {
      this.maxThreads = maxThreads;
      return this;
    }

    /**
     * Set the service's idle timeout in milliseconds
     *
     * @param idleTimeOutMillis service's idle timeout in milliseconds
     * @return this {@link Builder}
     */
    public Builder setIdleTimeOutMillis(int idleTimeOutMillis) {
      this.idleTimeOutMillis = idleTimeOutMillis;
      return this;
    }
  }

}
