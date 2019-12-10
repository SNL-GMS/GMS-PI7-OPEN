package gms.shared.utilities.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.lang3.Validate;

/**
 * Defines an HTTP service.  Contains properties such as it's port number and routes.
 * This object should contain enough information to instantiate a running HTTP service.
 */
@AutoValue
public abstract class ServiceDefinition {

  /**
   * Gets the HTTP routes of this service definition. These define the behavior of the service
   *
   * @return the routes, immutable
   */
  public abstract Set<Route> getRoutes();

  /**
   * Gets the ObjectMapper for JSON serialization. Note: in the future, this may start having a
   * default if we can manage to get an ObjectMapper in here that handles all COI objects.
   *
   * @return the ObjectMapper for JSON
   */
  public abstract ObjectMapper getJsonMapper();

  /**
   * Gets the ObjectMapper for msgpack serialization. Note: in the future, this may start having a
   * default if we can manage to get an ObjectMapper in here that handles all COI objects.
   *
   * @return the ObjectMapper for msgpack
   */
  public abstract ObjectMapper getMsgpackMapper();

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
  public abstract int getThreadIdleTimeoutMillis();

  /**
   * Creates a builder for this class with defaults set.
   *
   * @return a builder
   */
  public static Builder builder() {
    return new AutoValue_ServiceDefinition.Builder()
        // set defaults
        .setJsonMapper(Defaults.JSON_MAPPER)
        .setMsgpackMapper(Defaults.MSGPACK_MAPPER)
        .setPort(Defaults.PORT)
        .setRoutes(Set.of())
        .setMinThreadPoolSize(Defaults.MIN_THREAD_POOL_SIZE)
        .setMaxThreadPoolSize(Defaults.MAX_THREAD_POOL_SIZE)
        .setThreadIdleTimeoutMillis(Defaults.THREAD_IDLE_TIMEOUT_MILLIS);
  }

  /**
   * Builder for this class.
   */
  @AutoValue.Builder
  public abstract static class Builder {

    // this is only in this builder (not public), used so that
    // in build the set can be made into unmodifiable set.
    // Otherwise to maintain that collection as immutable you'd need to use e.g. Guava ImmutableSet.
    abstract Set<Route> getRoutes();

    public abstract Builder setJsonMapper(ObjectMapper jsonMapper);

    public abstract Builder setMsgpackMapper(ObjectMapper msgpackMapper);

    public abstract Builder setPort(int port);

    public abstract Builder setMinThreadPoolSize(int size);

    public abstract Builder setMaxThreadPoolSize(int size);

    public abstract Builder setThreadIdleTimeoutMillis(int timeout);

    public abstract Builder setRoutes(Set<Route> routes);

    abstract ServiceDefinition autoBuild();

    public ServiceDefinition build() {
      ServiceDefinition def = setRoutes(Collections.unmodifiableSet(getRoutes())).autoBuild();
      // validate the object properties
      Validate.isTrue(def.getPort() > 0 && def.getPort() <= 65535,   // max port number
          "Port number " + def.getPort() + " is not in range [0, 65535]");
      Validate.isTrue(def.getMinThreadPoolSize() > 0,
          "min thread pool size is " + def.getMinThreadPoolSize() + ", must be > 0");
      Validate.isTrue(def.getMaxThreadPoolSize() > 0,
          "max thread pool size is " + def.getMaxThreadPoolSize() + ", must be > 0");
      Validate.isTrue(def.getMinThreadPoolSize() <= def.getMaxThreadPoolSize(),
          String.format("min thread pool size must be <= max thread pool size (min=%d, max=%d)",
              def.getMinThreadPoolSize(), def.getMaxThreadPoolSize()));
      Validate.isTrue(def.getThreadIdleTimeoutMillis() > 0,
          "thread timeout is " + def.getThreadIdleTimeoutMillis() + ", must be > 0");
      return def;
    }
  }
}
