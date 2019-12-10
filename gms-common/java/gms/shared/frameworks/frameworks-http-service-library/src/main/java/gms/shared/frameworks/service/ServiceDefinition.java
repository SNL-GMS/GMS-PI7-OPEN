package gms.shared.frameworks.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.utilities.ServerConfig;
import gms.shared.frameworks.utilities.Validation;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.lang3.Validate;

/**
 * Defines an HTTP service.  Contains properties such as it's port number and routes. This object
 * should contain enough information to instantiate a running HTTP service.
 */
@AutoValue
public abstract class ServiceDefinition {

  /**
   * Path reserved for basic health check routes.
   */
  static final String HEALTHCHECK_PATH = "/alive";

  /**
   * Gets the HTTP {@link Route}s of this service definition. These define the behavior of the
   * service.  Each Route has a unique {@link Route#getPath()} and none of the {@link
   * Route#getPath()} are equal to {@link ServiceDefinition#HEALTHCHECK_PATH}
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
   * Gets the config for the server
   * @return the server config
   */
  public abstract ServerConfig getServerConfig();
  /**
   * Creates a builder for this class with defaults set.
   *
   * @return a builder
   */
  public static Builder builder(ServerConfig serverConfig) {
    return new AutoValue_ServiceDefinition.Builder()
        .setServerConfig(serverConfig)
        .setJsonMapper(CoiObjectMapperFactory.getJsonObjectMapper())
        .setMsgpackMapper(CoiObjectMapperFactory.getMsgpackObjectMapper())
        .setRoutes(Set.of());
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

    public abstract Builder setServerConfig(ServerConfig serverConfig);

    public abstract Builder setRoutes(Set<Route> routes);

    abstract ServiceDefinition autoBuild();

    /**
     * Obtains a new {@link ServiceDefinition} from the contents of this builder.  Validates the
     * ServiceDefinition's parameters and throws an IllegalArgumentException if any of them are
     * invalid. The validations are:
     *  - Each {@link Route#getPath()} in {@link ServiceDefinition#getRoutes()} must be unique
     *  - None of the {@link Route#getPath()} can equal {@link ServiceDefinition#HEALTHCHECK_PATH}
     *
     * @return new {@link ServiceDefinition}, not null
     * @throws IllegalArgumentException if any of the ServiceDefinition attributes are out of range
     */
    public ServiceDefinition build() {
      ServiceDefinition def = setRoutes(Collections.unmodifiableSet(getRoutes())).autoBuild();
      // validate the object properties
      Validation.throwForNonUnique(def.getRoutes(), Route::getPath,
          "Each route must have a unique path but paths but the following paths are duplicated");
      Validate.isTrue(
          def.getRoutes().stream().map(Route::getPath).noneMatch(HEALTHCHECK_PATH::equals),
          "Endpoint " + HEALTHCHECK_PATH + " is reserved and cannot be provided as a Route's path");
      return def;
    }
  }
}
