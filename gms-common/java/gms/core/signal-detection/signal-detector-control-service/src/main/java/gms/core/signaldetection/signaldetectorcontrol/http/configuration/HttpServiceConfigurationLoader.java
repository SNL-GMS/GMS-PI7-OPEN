package gms.core.signaldetection.signaldetectorcontrol.http.configuration;

import gms.core.signaldetection.signaldetectorcontrol.util.ConfigurationLoader;
import java.net.URL;
import java.util.Objects;
import org.apache.commons.configuration2.Configuration;

public class HttpServiceConfigurationLoader {

  private static final String PORT_KEY = "port";
  private static final String MIN_THREADS_KEY = "min_threads";
  private static final String MAX_THREADS_KEY = "max_threads";
  private static final String IDLE_TIMEOUT_MILLIS_KEY = "idle_timeout_millis";
  private static final String BASE_URL_KEY = "base_url";

  /**
   * Obtains a {@link HttpServiceConfiguration} based on the {@link HttpServiceConfigurationLoader}
   * properties hierarchy.
   *
   * @param propertiesFileUrl {@link URL} to the properties file to load, not null
   * @return a constructed HttpServiceConfiguration, not null
   * @throws NullPointerException if propertiesFileUrl is null
   */
  public static HttpServiceConfiguration load(URL propertiesFileUrl) {
    Objects.requireNonNull(propertiesFileUrl,
        "HttpServiceConfigurationLoader requires non-null propertiesFileUrl");

    final Configuration config = ConfigurationLoader.load(propertiesFileUrl);

    return HttpServiceConfiguration.builder()
        .setBaseUrl(config.getString(BASE_URL_KEY, HttpServiceConfiguration.DEFAULT_BASE_URL))
        .setPort(config.getInt(PORT_KEY, HttpServiceConfiguration.DEFAULT_PORT))
        .setMinThreads(config.getInt(MIN_THREADS_KEY, HttpServiceConfiguration.DEFAULT_MIN_THREADS))
        .setMaxThreads(config.getInt(MAX_THREADS_KEY, HttpServiceConfiguration.DEFAULT_MAX_THREADS))
        .setIdleTimeOutMillis(config
            .getInt(IDLE_TIMEOUT_MILLIS_KEY, HttpServiceConfiguration.DEFAULT_IDLE_TIMEOUT_MILLIS))
        .build();
  }
}