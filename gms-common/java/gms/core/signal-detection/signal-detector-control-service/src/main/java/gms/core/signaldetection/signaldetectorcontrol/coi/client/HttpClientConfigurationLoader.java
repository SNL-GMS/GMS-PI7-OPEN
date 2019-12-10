package gms.core.signaldetection.signaldetectorcontrol.coi.client;

import gms.core.signaldetection.signaldetectorcontrol.util.ConfigurationLoader;
import java.net.URL;
import java.util.Objects;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConversionException;

/**
 * Creates {@link HttpClientConfiguration} from a hierarchy of
 * Environment variables
 * System properties (i.e. as set with java -D command line flags)
 * application.properties file properties
 *
 * Properties in higher levels override properties from lower levels
 */
public class HttpClientConfigurationLoader {

  private static final String HOST_KEY = "host";
  private static final String PORT_KEY = "port";
  private static final String BASE_URL_KEY = "baseUrl";

  /**
   * Loads a {@link HttpClientConfiguration} using configuration from a hierarchy provided by {@link
   * ConfigurationLoader}. Uses the provided configurationPrefix deconflict configuration keys when
   * creating the HttpClientConfiguration.
   *
   * Required configuration keys and value types are:
   * host (string)
   * port (integer)
   * baseUrl (string)
   *
   * @param configurationPrefix prefix added to all configuration keys during value lookup, not
   * null
   * @param propertiesFileUrl URL for a properties file, not null
   * @return {@link HttpClientConfiguration}, not null
   * @throws NullPointerException if configurationPrefix or propertiesFileUrl are null
   * @throws IllegalStateException if required configuration keys are missing or their values are of
   * the incorrect type
   */
  public static HttpClientConfiguration load(String configurationPrefix, URL propertiesFileUrl) {
    Objects.requireNonNull(configurationPrefix,
        "HttpClientConfigurationLoader requires non-null configurationPrefix");
    Objects.requireNonNull(propertiesFileUrl,
        "HttpClientConfigurationLoader requires non-null propertiesFileUrl");

    Configuration configuration = ConfigurationLoader.load(propertiesFileUrl);

    final String host = parseString(configuration, getKey(configurationPrefix, HOST_KEY));
    final int port = parseInt(configuration, getKey(configurationPrefix, PORT_KEY));
    final String basePath = parseString(configuration, getKey(configurationPrefix, BASE_URL_KEY));

    return HttpClientConfiguration.create(host, port, basePath);
  }

  /**
   * Parses a string value associated with the key from the configuration.
   *
   * @param configuration {@link Configuration}, not null
   * @param key configuration key, not null
   * @return String configuration value, not null
   * @throws IllegalStateException if configuration does not have a value for key or if the value
   * cannot be parsed to a String
   */
  private static String parseString(Configuration configuration, String key) {
    if (!configuration.containsKey(key)) {
      throw new IllegalStateException(
          "HttpClientConfigurationLoader requires property " + key + " to be defined as a string");
    }

    String result;
    try {
      result = configuration.getString(key);
    } catch (ConversionException e) {
      throw new IllegalStateException(
          "HttpClientConfigurationLoader could not parse property " + key + " as a string");
    }

    return result;
  }

  /**
   * Parses an int value associated with the key from the configuration.
   *
   * @param configuration {@link Configuration}, not null
   * @param key configuration key, not null
   * @return integer configuration value, not null
   * @throws IllegalStateException if configuration does not have a value for key or if the value
   * cannot be parsed to an integer
   */
  private static int parseInt(Configuration configuration, String key) {
    if (!configuration.containsKey(key)) {
      throw new IllegalStateException(
          "HttpClientConfigurationLoader requires property " + key
              + " to be defined as an integer");
    }

    int result;
    try {
      result = configuration.getInt(key);
    } catch (ConversionException e) {
      throw new IllegalStateException(
          "HttpClientConfigurationLoader could not parse property " + key + " as an integer");
    }

    return result;
  }

  private static String getKey(String prefix, String key) {
    return prefix + key;
  }
}
