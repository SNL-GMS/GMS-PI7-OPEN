package gms.core.signaldetection.signaldetectorcontrol.util;

import java.net.URL;
import java.util.Objects;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.EnvironmentConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationLoader {

  private static Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);

  /**
   * Obtains a {@link CompositeConfiguration} of environment variables, system properties, and
   * properties file properties (former properties override latter properties so environment
   * variables have precedence over other properties).  Uses the provided propertiesFileUrl for the
   * properties file properties.
   *
   * @param propertiesFileUrl URL handle to a properties file, not null
   * @return CompositeConfiguration, not null
   * @throws NullPointerException if propertiesFileUrl is null
   * @throws RuntimeException if the propertiesFileUrl cannot be loaded into a {@link Configuration}
   */
  public static CompositeConfiguration load(URL propertiesFileUrl) {

    Objects.requireNonNull(propertiesFileUrl,
        "ConfigurationLoader requires non-null propertiesFileUrl");

    // Load properties from the propertiesFileUrl.  Throws an unchecked exception
    // on failure in order to fail fast if the properties can't be found.
    FileBasedConfiguration propertiesFileConfig;
    try {
      Configurations c = new Configurations();
      propertiesFileConfig = c.fileBasedBuilder(PropertiesConfiguration.class, propertiesFileUrl)
          .getConfiguration();
    } catch (ConfigurationException e) {
      logger.error("Could not load configuration from " + propertiesFileUrl, e);
      throw new RuntimeException(
          "ConfigurationLoader could not load configuration file " + propertiesFileUrl, e);
    }

    // Configurations added first override configurations added later.
    // System properties configuration are normally set via -D command line flags and environment
    // configuration is OS environment variables.
    CompositeConfiguration config = new CompositeConfiguration();
    config.addConfiguration(new EnvironmentConfiguration());
    config.addConfiguration(new SystemConfiguration());
    config.addConfiguration(propertiesFileConfig);

    return config;
  }
}