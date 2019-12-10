package gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration;

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
 * Utility to load a {@link Cd11DataProviderConfig} based on a hierarchy of: Environment variables
 * System properties (i.e. as set with java -D command line flags) application.properties file
 * properties
 *
 * Properties in higher levels override properties from lower levels
 */
public class Cd11DataProviderConfigurationLoader {

  private static final Logger logger =
      LoggerFactory.getLogger(Cd11DataProviderConfigurationLoader.class);

  /*
   * Statically load properties since the ConfigurationManager can only be initialized once
   */
  static {

    // Load properties from the application.properties file. Throws an unchecked exception
    // on failure in order to fail fast if the properties can't be found.
    final String propFile = "gms/dataacquisition/stationreceiver/cd11/dataprovider/application.properties";

    ConcurrentMapConfiguration propertiesFileConfig;
    try {
      propertiesFileConfig = new ConcurrentMapConfiguration(new PropertiesConfiguration(propFile));
    } catch (ConfigurationException e) {
      String msg = String.format("Could not load configuration from %s: ", propFile);
      logger.error(msg, e);
      throw new RuntimeException(msg, e);
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
   * Obtains a {@link Cd11DataProviderConfig} based on the properties hierarchy.
   *
   * @return a constructed Configuration, not null
   */
  public static Cd11DataProviderConfig load() {
    return Cd11DataProviderConfig.builder()
        .setConnectionManagerIpAddress(getString("connectionManagerIpAddress",
            Cd11DataProviderConfig.DEFAULT_CONNECTION_MANAGER_IP_ADDRESS))
        .setConnectionManagerPort(
            getInt("connectionManagerPort", Cd11DataProviderConfig.DEFAULT_CONNECTION_MANAGER_PORT))
        .setStationName(getString("stationName", Cd11DataProviderConfig.DEFAULT_STATION_NAME))
        .build();
  }

  private static String getString(String key, String defaultValue) {
    return DynamicPropertyFactory.getInstance().getStringProperty(key, defaultValue).get();
  }

  private static int getInt(String key, int defaultValue) {
    return DynamicPropertyFactory.getInstance().getIntProperty(key, defaultValue).get();
  }
}
