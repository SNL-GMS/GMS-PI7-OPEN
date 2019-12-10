package gms.shared.frameworks.systemconfig;

import com.google.protobuf.ByteString;
import com.ibm.etcd.api.RangeResponse;

import java.util.Optional;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class providing retrieval of system configuration values from one or more etcd servers.
 *
 * <p>If an invalid endpoint is specified, this object will be still be valid but will return no
 * configuration values. A warning will be logged for this case.
 */
public class EtcdSystemConfigRepository implements SystemConfigRepository {

  private static final Logger logger = LoggerFactory.getLogger(EtcdSystemConfigRepository.class);

  private final EtcdClientManager etcdClientManager;

  /** Instantiate a EtcdSystemConfigRepository */
  private EtcdSystemConfigRepository(String endpoints, String username, String password) {
    this.etcdClientManager = new EtcdClientManager(endpoints, username, password);
  }

  /**
   * Etcd-specific implementation of get.
   *
   * @param key key name to return the value for from this repository
   * @return value of key if present, null if not found.
   */
  @Override
  public Optional<String> get(String key) {
    try {
      RangeResponse response =
          this.etcdClientManager.getEtcdClient().get(ByteString.copyFromUtf8(key)).sync();
      long responseCount = response.getCount();
      if (responseCount > 0) {
        if (responseCount > 1) {
          logger.warn(
              "etcd returned {} possible values for key {}. Returning first one.",
              responseCount,
              key);
        }
        return Optional.of(response.getKvs(0).getValue().toStringUtf8());
      }
    } catch (Exception e) {
      String message =
          String.format(
              "etcd unavailable: failed to read key %s from etcd '%s'",
              key, this.etcdClientManager);
      logger.warn(message);
    }
    return Optional.empty();
  }

  /**
   * Get the name of this system configuration repository as a string.
   *
   * @return name of the form "etcd:endpoints"
   */
  @Override
  public String toString() {
    return this.etcdClientManager.toString();
  }

  /** Return a builder for an EtcdSystemConfigurationRepository. */
  public static Builder builder() {
    return new EtcdSystemConfigRepository.Builder();
  }

  /** Builder for an EtcdSystemConfigurationRepository connected to one or more etcd servers. */
  public static class Builder {
    private static final String DEFAULT_ETCD_ENDPOINTS = "etcd:2379";
    private static final String DEFAULT_ETCD_USERNAME  = "gms";
    private static final String DEFAULT_ETCD_PASSWORD  = "gmsdb:gms@etcd=prevent-important-guest";
    private String endpoints =
        Optional.ofNullable(System.getenv("GMS_ETCD_ENDPOINTS")).orElse(DEFAULT_ETCD_ENDPOINTS);
    private String username =
        Optional.ofNullable(System.getenv("GMS_ETCD_USERNAME")).orElse(DEFAULT_ETCD_USERNAME);
    // Etcd requires a password, but this is for read-only access as the gms user
    private String password = 
        Optional.ofNullable(System.getenv("GMS_ETCD_PASSWORD")).orElse(DEFAULT_ETCD_PASSWORD); 

    /**
     * Set the endpoints for the EtcdSystemConfigurationRepository under construction.
     *
     * @param endpoints one or more etcd server:port endpoints (comma-separated)
     */
    public Builder setEndpoints(String endpoints) {
      this.endpoints = endpoints;
      return this;
    }

    /**
     * Set the access credentials for the EtcdSystemConfigurationRepository under construction.
     *
     * @param username username to use when connecting to etcd servers
     * @param password password to use when connecting to etcd servers
     */
    public Builder setCredentials(String username, String password) {
      this.username = username;
      this.password = password;
      return this;
    }

    /**
     * Finish construction of a new EtcdSystemConfigRepository
     *
     * @return newly constructed EtcdSystemConfigRepository
     */
    public EtcdSystemConfigRepository build() {
      return new EtcdSystemConfigRepository(Validate.notBlank(endpoints), username, password);
    }
  }
}
