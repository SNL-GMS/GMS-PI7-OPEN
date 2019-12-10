package gms.shared.frameworks.systemconfig;

import gms.shared.frameworks.utilities.ServerConfig;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to general GMS system configuration. System configuration may include values such
 * as log levels, database connection parameters, service routes, thread pool sizes, etc. but does
 * not include any algorithm specific configuration values.
 *
 * <p>Retrieves configuration values from a {@link SystemConfigRepository}.
 *
 * <p>Key names have an inferred name space using a dot ('.') as a separator. The first part of the
 * name (before the dot) is the name of the control class. The more specific value with the control
 * name will override the more general value if present.
 *
 * <p>Consider the following example:
 *
 * <p>We may define a value for the key 'port' as follows:
 *
 * <pre>{@code
 * port = 8080
 * }</pre>
 *
 * While most control classes want to share this value, we may want to define a <i>different</i>
 * value specific to the 'spacemodulator' control process (perhaps as a temporary override for
 * development or testing). We can do that by prefixing the control name:
 *
 * <pre>{@code
 * spacemodulator.port = 591
 * }</pre>
 *
 * Thus the 'spacemodulator' control class can always just call {@code getValueAsInt('port')}. This
 * will first look for the value of port with our control name ('spacemodulator.port') and, failing
 * to find that, will then just return the value of 'port'.
 *
 * <p>Alternatively, the caller specify a key name with a desired prefix already included.
 *
 * <p>For instance, the 'timeinhibitor' control class may need to look up the port for the
 * 'spacemodulator'. In this case, it would call {@code getValueAsInt("spacemodulator.port")}. This
 * would resolve as if we had done a lookup of 'port' with the prefix of 'spacemodulator'
 */
public class SystemConfig {

  /**
   * The name of the configuration item indicating the client timeout.
   */
  public static final String CLIENT_TIMEOUT = "client-timeout";

  /**
   * The name of the configuration item indicating a service host identifier.
   */
  public static final String HOST = "host";

  /**
   * The name of the configuration item indicating the port at which a service is provided.
   */
  public static final String PORT = "port";

  /**
   * The name of the configuraton item indicating the duration before a request should time out.
   */
  public static final String IDLE_TIMEOUT = "idle-timeout";

  /**
   * The name of the configuration item indicating the minimum number of threads a service process
   * should instantiate to handling requests.
   */
  public static final String MIN_THREADS = "min-threads";

  /**
   * The name of the configuration item indicating the maximum number of threads a service process
   * should instantiate for handling requests.
   */
  public static final String MAX_THREADS = "max-threads";

  /**
   * The name of the configuration item indicating the location of the processing configuration for
   * a control class.
   */
  public static final String PROCESSING_CONFIGURATION_ROOT = "processing-configuration-root";

  private static final Logger logger = LoggerFactory.getLogger(SystemConfig.class);
  private final String controlName;
  private final List<SystemConfigRepository> repositories;

  /**
   * Creates a new {@link SystemConfig} for a Control with the provided controlName.
   *
   * @param controlName control name, not null
   * @throws NullPointerException if controlName is null
   */
  public static SystemConfig create(String controlName) {
    return new SystemConfig(controlName, SystemConfigRepositoryDefaultFactory.create());
  }

  /**
   * Creates a new {@link SystemConfig} for a Control with the provided controlName. Values are
   * gathered from the provided repository.
   *
   * @param controlName control name, not null
   * @param repository the repository to retrieve values from
   * @throws NullPointerException if controlName is null
   */
  public static SystemConfig create(String controlName, SystemConfigRepository repository) {
    return new SystemConfig(controlName, Collections.singletonList(repository));
  }

  /**
   * Creates a new {@link SystemConfig} for a Control with the provided controlName. Values are
   * gathered from the provided list of repositories. Values found in repositories toward the front
   * of the list will override values in repositories toward the end of the list.
   *
   * @param controlName control name, not null
   * @param repositories the repository to retrieve values from
   * @throws NullPointerException if controlName is null
   */
  public static SystemConfig create(String controlName, List<SystemConfigRepository> repositories) {
    return new SystemConfig(controlName, repositories);
  }

  private SystemConfig(String controlName, List<SystemConfigRepository> repositories) {
    Objects.requireNonNull(controlName, "SystemConfig cannot be created with a null controlName");
    Validate.notBlank(controlName, "SystemConfig cannot be created with an empty controlName");
    Objects.requireNonNull(repositories, "SystemConfig cannot be created with a null repository");
    logger.info(
        "system-configuration: creating system configuration for control named {}", controlName);
    this.controlName = controlName;
    this.repositories = Collections.unmodifiableList(repositories);
  }

  /**
   * Get the control name for this {@link SystemConfig}.
   *
   * @return control name for this {@link SystemConfig}
   */
  public String getControlName() {
    return controlName;
  }

  /**
   * Get the value of the specified key (if present) from the GMS system configuration.
   *
   * @param key key to a single configuration value, not null
   * @return value for specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key n
   */
  public String getValue(String key) {
    Objects.requireNonNull(key, "System configuration key can not be null");

    // Search through each repository for the requested key and return the first found value.
    for (SystemConfigRepository r : repositories) {
      Optional<String> value = r.search(key, controlName);
      if (value.isPresent()) {
        return value.get().trim();
      }
    }
    throw new MissingResourceException(
        "system configuration value not found for '" + key + "'", controlName, key);
  }

  /**
   * Get the integer value of the specified key (if present) from the GMS system configuration.
   *
   * @param key key to a single configuration value, not null
   * @return integer value for specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key
   * @throws NumberFormatException if value can not be parsed as an integer
   */
  public int getValueAsInt(String key) {
    return Integer.parseInt(getValue(key));
  }

  /**
   * Get the integer value of the specified key (if present) from the GMS system configuration.
   *
   * @param key key to a single configuration value, not null
   * @return long value for specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key
   * @throws NumberFormatException if value can not be parsed as an long
   */
  public long getValueAsLong(String key) {
    return Long.parseLong(getValue(key));
  }

  /**
   * Get the double value of the specified key (if present) from the GMS system configuration.
   *
   * @param key key to a single configuration value, not null
   * @return double value for specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key
   * @throws NumberFormatException if value can not be parsed as an double
   */
  public double getValueAsDouble(String key) {
    return Double.parseDouble(getValue(key));
  }

  /**
   * Get the boolean value of the specified key (if present) from the GMS system configuration.
   *
   * @param key key to a single configuration value, not null
   * @return boolean value for specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key
   * @throws IllegalArgumentException if value can not be parsed as an boolean
   */
  public boolean getValueAsBoolean(String key) {
    // Note: not using Boolean.parseBoolean so we can support 1/0 and yes/no values in addition to
    // true/false.
    String value = getValue(key);
    switch (value.toLowerCase()) {
      case "true":
      case "yes":
      case "1":
        return true;
      case "false":
      case "no":
      case "0":
        return false;
      default:
        throw new IllegalArgumentException(
            value + "does not represent a boolean value of either \"true\" or \"false\"");
    }
  }

  /**
   * Get the {@link Path} represented by the specified key (if present) from the GMS system
   * configuration.
   *
   * @param key key to a single configuration value, not null
   * @return Path object represented by the specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key
   * @throws IllegalArgumentException if value can not be parsed as an boolean
   */
  public Path getValueAsPath(String key) {
    return Paths.get(getValue(key));
  }

  /**
   * Get the {@link Duration} represented by the specified key (if present) from the GMS system
   * configuration.
   *
   * @param key key to a single configuration value, not null
   * @return {@link Duration} object represented by the specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key
   * @throws IllegalArgumentException if value can not be parsed as a {@link Duration}
   */
  public Duration getValueAsDuration(String key) {
    return Duration.parse(getValue(key));
  }

  /**
   * Get the {@link URL} for the specified component from the GMS system configuration.
   *
   * @param componentName the name of the component
   * @return URL for the component
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if host or port cannot be found for the component
   * @throws IllegalArgumentException if retrieved host or port from config are invalid
   */
  public URL getUrlOfComponent(String componentName) {
    return makeUrl(getValue(createKey(componentName, SystemConfig.HOST)),
        getValueAsInt(createKey(componentName, SystemConfig.PORT)));
  }

  /**
   * Get the {@link URL} for this component from the GMS system configuration.
   *
   * @return URL for the component this configuration is for
   * @throws MissingResourceException if host or port cannot be found
   * @throws IllegalArgumentException if retrieved host or port from config are invalid
   */
  public URL getUrl() {
    return makeUrl(getValue(SystemConfig.HOST), getValueAsInt(SystemConfig.PORT));
  }

  private static URL makeUrl(String host, int port) {
    final String urlStr = String.format("http://%s:%d", host, port);
    try {
      return new URL(urlStr);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Malformed URL: " + urlStr, e);
    }
  }

  /**
   * Gets a {@link ServerConfig} object for this configuration.
   *
   * @return a {@link ServerConfig}, not null
   */
  public ServerConfig getServerConfig() {
    return ServerConfig.from(
        getValueAsInt(SystemConfig.PORT),
        getValueAsInt(SystemConfig.MIN_THREADS),
        getValueAsInt(SystemConfig.MAX_THREADS),
        getValueAsDuration(SystemConfig.IDLE_TIMEOUT));
  }

  /**
   * Get the {@link Path} to the root directory of the processing configuration files for this
   * configuration's Control.
   *
   * <p>The configuration value for [$controlName].processing-configuration-root specifies the path
   * to the root directory.
   *
   * <p>This first looks in the resources of the context class for the current thread, and if not
   * found there, then looks on the file system.
   *
   * @return {@link Path}, not null
   * @throws MissingResourceException if the processing-configuration-root key can not be found for
   * this Control.
   * @throws MissingResourceException if file specified by processing-config-root does not exist.
   * @throws NullPointerException if the processing-configuration-root key is not defined.
   */
  public Path getProcessingConfigurationRoot() {
    final String processingConfigRoot = getValue(PROCESSING_CONFIGURATION_ROOT);

    // First, look for this the resources for our current thread class context
    final URL resourceUrl =
        Thread.currentThread().getContextClassLoader().getResource(processingConfigRoot);
    if (null != resourceUrl) {
      return new File(resourceUrl.getPath()).toPath();
    }

    // Next, look on the local file system
    final File processingConfigFile = new File(processingConfigRoot);

    if (!processingConfigFile.exists()) {
      logger.error("system-configuration: Can't find processing configuration root in resources or on file system: {}",
          processingConfigRoot);
      throw new MissingResourceException(
          "processing configuration root not found in resources or on file system",
          controlName,
          processingConfigRoot);
    }
    return processingConfigFile.toPath();
  }

  static String createKey(String prefix, String key) {
    return prefix + SystemConfigConstants.SEPARATOR + key;
  }
}
