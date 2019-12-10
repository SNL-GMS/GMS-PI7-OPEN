package gms.shared.frameworks.utilities;

import com.google.auto.value.AutoValue;
import java.time.Duration;
import org.apache.commons.lang3.Validate;

@AutoValue
public abstract class ServerConfig {

  /**
   * Gets the port number the service runs on.
   *
   * @return the port number
   */
  public abstract int getPort();

  /**
   * Gets the minimum number of HTTP worker threads to be in the thread pool.
   *
   * @return the minimum size of the thread pool
   */
  public abstract int getMinThreadPoolSize();

  /**
   * Gets the maximum number of HTTP worker threads to be in the thread pool.
   *
   * @return the maximum size of the thread pool
   */
  public abstract int getMaxThreadPoolSize();

  /**
   * Gets the timeout in milliseconds of any HTTP worker thread from being idle.
   *
   * @return the timeout of each worker thread in millis
   */
  public abstract Duration getThreadIdleTimeout();

  /**
   * Creates a {@link ServerConfig} from all args.
   * @param port the port number
   * @param minThreads the minimum number of threads
   * @param maxThreads the maximum number of threads
   * @param idleTimeout the timeout for idle threads
   * @return a {@link ServerConfig}, not null
   * @throws IllegalArgumentException if any of these are not satisfied:
   *  - port must be >= 0 and <= 65535
   *  - minThreads must be > 0 and <= maxThreads
   *  - maxThreads must be > 0
   *  - idleTimeout must be > 0
   */
  public static ServerConfig from(
      int port, int minThreads, int maxThreads, Duration idleTimeout) {
    Validate.isTrue(port > 0 && port <= 65535,   // max port number
        "Port number " + port + " is not in range [0, 65535]");
    Validate.isTrue(minThreads > 0,
        "min thread pool size is " + minThreads + ", must be > 0");
    Validate.isTrue(maxThreads > 0,
        "max thread pool size is " + maxThreads + ", must be > 0");
    Validate.isTrue(minThreads <= maxThreads,
        String.format("min thread pool size must be <= max thread pool size (min=%d, max=%d)",
            minThreads, maxThreads));
    Validate.isTrue(!idleTimeout.isNegative(),
        "thread timeout is " + idleTimeout + ", must not be negative");
    return new AutoValue_ServerConfig(port, minThreads, maxThreads, idleTimeout);
  }
}
