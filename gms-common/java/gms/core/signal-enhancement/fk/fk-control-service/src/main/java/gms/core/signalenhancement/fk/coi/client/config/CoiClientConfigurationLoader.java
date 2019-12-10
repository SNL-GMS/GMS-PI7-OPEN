package gms.core.signalenhancement.fk.coi.client.config;

import gms.core.signalenhancement.fk.util.ConfigurationLoader;
import gms.core.signalenhancement.fk.util.UrlUtility;
import java.net.URL;
import java.util.MissingResourceException;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoiClientConfigurationLoader {

  private static final Logger logger = LoggerFactory.getLogger(CoiClientConfigurationLoader.class);

  private static final String PERSISTENCE_URL = "persistence_url";

  /**
   * Obtains a {@link CoiClientConfiguration} based on the {@link CoiClientConfigurationLoader}
   * properties hierarchy.
   *
   * @return a constructed HttpServiceConfiguration, not null
   */
  public static CoiClientConfiguration load() {

    // Get URL to the application.properties file
    String path = "gms/core/signalenhancement/fk/service/application.properties";
    URL propFileUrl = UrlUtility.getUrlToResourceFile(path);

    if (null == propFileUrl) {
      String message =
          "HttpServiceConfigurationLoader can't load properties file from resources: " + path;
      logger.error(message);
      throw new MissingResourceException(message, "HttpServiceConfigurationLoader", path);
    }

    // Load configuration and use to create the HttpServiceConfiguration
    Configuration config = ConfigurationLoader.load(propFileUrl);

    return CoiClientConfiguration.create(config.getString(PERSISTENCE_URL));
  }
}
