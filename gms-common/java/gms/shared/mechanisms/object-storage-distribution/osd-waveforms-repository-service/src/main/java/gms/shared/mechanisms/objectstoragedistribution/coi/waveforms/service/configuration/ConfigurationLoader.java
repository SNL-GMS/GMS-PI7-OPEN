package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.configuration;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConcurrentMapConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to load a {@link Configuration} based on a hierarchy of: Environment variables System
 * properties (i.e. as set with java -D command line flags) application.properties file properties
 *
 * Properties in higher levels override properties from lower levels
 */
public class ConfigurationLoader {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

  private static final String PORT_KEY = "port";
  private static final String MIN_THREADS_KEY = "min_threads";
  private static final String MAX_THREADS_KEY = "max_threads";
  private static final String IDLE_TIMEOUT_MILLIS_KEY = "idle_timeout_millis";
  private static final String PERSISTENCE_URL_KEY = "persistence_url";
  private static final String CASS_CONNECT_POINTS_KEY = "cassandra_connect_points";
  private static final String CASS_PORT_KEY = "cassandraPort";
  private static final String CASS_USER_KEY = "cassandraUser";
  private static final String CASS_PASS_KEY = "cassandraPass";
  private static final String CASS_CLUSTER_KEY = "cassandraClusterName";
  private static final String WAVEFORM_TABLE_KEY = "waveformTable";
  private static final String FKSPECTRA_TABLE_KEY = "fkSpectraTable";

  /*
   * Statically load properties since the ConfigurationManager can only be initialized once
   */
  static {

    // Load properties from the application.properties file.  Throws an unchecked exception
    // on failure in order to fail fast if the properties can't be found.
    final String propFile = "gms/shared/mechanisms/objectstoragedistribution/coi/waveforms/service/application.properties";

    ConcurrentMapConfiguration propertiesFileConfig;
    try {
      propertiesFileConfig = new ConcurrentMapConfiguration(new PropertiesConfiguration(propFile));
      logger.info("Loaded config from " + propFile);
    } catch (ConfigurationException e) {
      logger.error("Could not load configuration from " + propFile, e);
      throw new RuntimeException(
          "ConfigurationLoader could not load configuration file " + propFile, e);
    }

    // System properties configuration (e.g. as set via -D command line flags)
    ConcurrentMapConfiguration systemPropertiesConfig = new ConcurrentMapConfiguration(
        new SystemConfiguration());

    // Environment variable configuration
    ConcurrentMapConfiguration environmentVariablesConfig = new ConcurrentMapConfiguration(
        new EnvironmentConfiguration());

    // System Config overrides fileConfig
    ConcurrentCompositeConfiguration config = new ConcurrentCompositeConfiguration();
    config.addConfiguration(environmentVariablesConfig, "environmentConfig");
    config.addConfiguration(systemPropertiesConfig, "systemConfig");
    config.addConfiguration(propertiesFileConfig, "fileConfig");

    ConfigurationManager.install(config);
  }

  /**
   * Obtains a {@link Configuration} based on the {@link ConfigurationLoader} properties hierarchy.
   *
   * @return a constructed Configuration, not null
   */
  public static Configuration load() {
    return Configuration.builder()
        .setPersistenceUrl(
            emptyToNull(getString(PERSISTENCE_URL_KEY, Configuration.DEFAULT_PERSISTENCE_URL)))
        .setPort(getInt(PORT_KEY, Configuration.DEFAULT_PORT))
        .setMinThreads(getInt(MIN_THREADS_KEY, Configuration.DEFAULT_MIN_THREADS))
        .setMaxThreads(getInt(MAX_THREADS_KEY, Configuration.DEFAULT_MAX_THREADS))
        .setIdleTimeOutMillis(
            getInt(IDLE_TIMEOUT_MILLIS_KEY, Configuration.DEFAULT_IDLE_TIMEOUT_MILLIS))
        .setCassandraConnectPoints(getString(CASS_CONNECT_POINTS_KEY, Configuration.DEFAULT_CASS_CONNECT_POINTS))
        .setCassandraPort(getInt(CASS_PORT_KEY, Configuration.DEFAULT_CASS_PORT))
        .setCassandraUser(getString(CASS_USER_KEY, Configuration.DEFAULT_CASS_USER))
        .setCassandraPass(getString(CASS_PASS_KEY, Configuration.DEFAULT_CASS_PASS))
        .setCassandraClusterName(getString(CASS_CLUSTER_KEY, Configuration.DEFAULT_CASS_CLUSTER_NAME))
        .setWaveformTable(getString(WAVEFORM_TABLE_KEY, Configuration.DEFAULT_WAVEFORMS_TABLE))
        .setFkSpectraTable(getString(FKSPECTRA_TABLE_KEY, Configuration.DEFAULT_FKSPECTRA_TABLE))
        .build();
  }

  private static String getString(String key, String defaultValue) {
    return DynamicPropertyFactory.getInstance().getStringProperty(key, defaultValue).get();
  }

  private static int getInt(String key, int defaultValue) {
    return DynamicPropertyFactory.getInstance().getIntProperty(key, defaultValue).get();
  }

  private static String emptyToNull(String s) {
    return s.trim().isEmpty() ? null : s;
  }
}
