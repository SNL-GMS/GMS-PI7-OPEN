package gms.core.signaldetection.association.control.service;

import gms.core.signaldetection.association.control.SignalDetectionAssociationControl;
import gms.core.signaldetection.association.control.SignalDetectionAssociationControlOsdGateway;
import gms.core.signaldetection.association.control.gacache.GACache;
import gms.core.signaldetection.association.control.gacache.GACacheJCache;
import gms.core.signaldetection.association.plugins.SdhStationAssociation;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.FileConfigurationRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.StationReferenceRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import gms.shared.utilities.service.HttpService;
import gms.shared.utilities.service.Route;
import gms.shared.utilities.service.ServiceDefinition;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

  private static final String SIGNAL_DETECTION_ASSOCIATION_BASECONFIG =
      "gms/core/signaldetection/association/control/service/baseconfig";

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  /**
   * Starts the application with default parameters.
   *
   * @param args N/A
   */
  public static void main(String[] args) {

    CachingProvider cachingProvider = Caching.getCachingProvider();
    CacheManager cacheManager = cachingProvider.getCacheManager();

    Runtime runtime = Runtime.getRuntime();

    runtime.addShutdownHook(
        new Thread(() -> {

          Application.logger.info("Closing cache...");

          cacheManager.close();
        })
    );

    new HttpService(getHttpServiceDefinition(cacheManager)).start();


  }

  /**
   * Return the service definition.
   *
   * @return service definition
   */
  private static ServiceDefinition getHttpServiceDefinition(CacheManager cacheManager) {

    Objects.requireNonNull(cacheManager, "Null cacheManager");

    Application.logger.info("Instantiating GACache...");

    GACache gaCache = instantiateGaCache(cacheManager);

    Application.logger.info("Finished instantiating GACache");

    RequestHandlers handlers = RequestHandlers.create(
        SignalDetectionAssociationControl.create(
            PluginRegistry.getRegistry(),
            createOsdGateway(),
            grabConfiguration(),
            gaCache
        )
    );

    return ServiceDefinition.builder()
        .setJsonMapper(CoiObjectMapperFactory.getJsonObjectMapper())
        .setMsgpackMapper(CoiObjectMapperFactory.getMsgpackObjectMapper())
        .setRoutes(Set.of(
            Route.create(
                "/is-alive",
                handlers::isAlive),
            Route.create(
                "/event/association/associate-to-location/interactive",
                handlers::associateToLocationInteractive),
            Route.create(
                "/event/association/associate-to-location",
                handlers::associateToLocation),
            Route.create("/event/association/associate-to-event/interactive",
                handlers::associateToEventHypothesisInteractive),
            Route.create("/event/association/associate-detections",
                handlers::associateDetections)
        ))
        .setPort(8080)
        .build();
  }

  /**
   * Grabs the Configuration Object associated with the Signal Detection Association Control Service
   * if one can be constructed successively.
   *
   * @return instance of {@link SignalDetectionAssociationControlConfiguration}
   */
  private static SignalDetectionAssociationControlConfiguration grabConfiguration() {
    // First we grab a ConfigurationRepository (Default is FileConfigurationRepository)
    ConfigurationRepository configurationRepository = getConfigurationRepository();
    SignalDetectionAssociationControlConfiguration config = SignalDetectionAssociationControlConfiguration
        .create(configurationRepository);
    return config;
  }

  // Creates a new SignalDetectionAssociationControlOsdGateway set to connect to the COI service
  //   using properties COI_SERVICE_HOST and COI_SERVICE_PORT.  First checks environment variables,
  //   and if they are not set, checks application.properties.
  //
  // Throws a RuntimeException if host/port values cannot be read from the environment or application.properties.
  private static SignalDetectionAssociationControlOsdGateway createOsdGateway() {

    Logger logger = LoggerFactory.getLogger(Application.class);

    Configuration config;
    try {
      config = Application.loadConfig();
    } catch (ConfigurationException e) {
      logger.error("Cannot start application - failed to load configuration", e);
      throw new IllegalStateException(e);
    }

    String coiServiceHost = config.getString("COI_SERVICE_HOST"); // might be null

    int coiServicePort;

    String portNotSetErrMsg = "\"COI_SERVICE_PORT\" property is not set in either the environment or application.properties";
    String hostNotSetErrMsg = "\"COI_SERVICE_HOST\" property is not set in either the environment or application.properties";
    String appStartFailureMsg = "Cannot start application - cannot create SignalDetectionAssociationOsdGateway";
    String errMsgFormat = "%s. %s.";

    try {

      coiServicePort = config.getInt("COI_SERVICE_PORT");   // throws exception if key not found
    } catch (NoSuchElementException e) {

      String errMsg = String.format(errMsgFormat, portNotSetErrMsg, appStartFailureMsg);

      logger.error(errMsg);

      throw new IllegalStateException(errMsg);
    }

    if (!Objects.isNull(coiServiceHost)) {
      try {
        logger.info(
            "SignalDetectionAssociationControlOsdGateway is set to connect to the COI service at http://{}:{}.",
            coiServiceHost, coiServicePort);
        return SignalDetectionAssociationControlOsdGateway.create(
            coiServiceHost,
            coiServicePort,
            new StationReferenceRepositoryJpa(
                CoiEntityManagerFactory.create(Application.loadHibernateConfig(config)))
        );
      } catch (MalformedURLException e) {
        logger.error("Invalid URL for signal detection COI service", e);
        throw new IllegalStateException(e);
      }
    } else {

      String errMsg = String.format(errMsgFormat, hostNotSetErrMsg, appStartFailureMsg);

      logger.error(errMsg);

      throw new IllegalStateException(errMsg);
    }
  }

  private static CompositeConfiguration loadConfig()
      throws ConfigurationException {

    CompositeConfiguration config = new CompositeConfiguration();
    config.addConfiguration(new EnvironmentConfiguration());
    config.addConfiguration(new PropertiesConfiguration(
        "gms/core/signaldetection/association/control/service/application.properties"));

    return config;
  }

  // TODO: Follow the same approach as Beam Service does. May want to make these two methods as utilities so other services creating configurations can follow suit.

  /**
   * Grab the ConfigurationRepository object associated with this application or return an Optional
   * of an empty object.
   *
   * @return {@link ConfigurationRepository} object or null
   */
  private static ConfigurationRepository getConfigurationRepository() {
    String configurationBaseDir = getResourcePath(SIGNAL_DETECTION_ASSOCIATION_BASECONFIG);
    Logger logger = LoggerFactory.getLogger(Application.class);
    logger.info("Configuration Base Directory: {}", configurationBaseDir);
    return FileConfigurationRepository.create((new File(configurationBaseDir)).toPath());
  }

  /**
   * Retrieves Relative Path w.r.t. the classpath of a given resource.
   *
   * @return String representing the relative path.
   */
  private static String getResourcePath(String path) {
    URL fileUrl = Thread.currentThread()
        .getContextClassLoader()
        .getResource(path);
    Logger logger = LoggerFactory.getLogger(Application.class);

    if (null == fileUrl) {
      String errorMessage = "Signal Detection Association Control can't find resource path"
          + SIGNAL_DETECTION_ASSOCIATION_BASECONFIG;
      logger.error(errorMessage);
      throw new MissingResourceException(errorMessage, Application.class.getName(),
          SIGNAL_DETECTION_ASSOCIATION_BASECONFIG);
    }
    return fileUrl.getPath();
  }

  private static Map<String, String> loadHibernateConfig(Configuration configuration) {
    String coiServiceHost = configuration.getString("persistence_url");

    if (Objects.nonNull(coiServiceHost)) {
      return Map.ofEntries(Map.entry("hibernate.connection.url", coiServiceHost));
    } else {
      return Map.of();
    }
  }

  /**
   * Creates and returns a new {@link GACache} instantiated using the provided {@link CacheManager}.
   * This application's shutdown hook will use the provided {@link CacheManager} to close the caches
   * it manages.
   *
   * @param cacheManager {@link CacheManager} used to instantiate the returned {@link GACache}. This
   * {@link CacheManager} will manage the caches created and accessed by {@link GACache}.
   * @return New {@link GACache}, not null.
   */
  private static GACache instantiateGaCache(CacheManager cacheManager) {

    Objects.requireNonNull(cacheManager, "Null cacheManager");

    // Create configuration for SdhStationAssociation cache
    MutableConfiguration<UUID, SdhStationAssociation> sdhStationAssociationCacheConfig = new MutableConfiguration<>();
    sdhStationAssociationCacheConfig
        .setStoreByValue(false)
        .setExpiryPolicyFactory(
            CreatedExpiryPolicy.factoryOf(
                new Duration(
                    TimeUnit.HOURS,
                    4
                )
            )
        );

    // Create configuration for EventHypothesis cache
    MutableConfiguration<UUID, EventHypothesis> eventHypothesisCacheConfig = new MutableConfiguration<>();
    eventHypothesisCacheConfig
        .setStoreByValue(false)
        .setExpiryPolicyFactory(
            CreatedExpiryPolicy.factoryOf(
                new Duration(
                    TimeUnit.HOURS,
                    4
                )
            )
        );

    // Create configuration for ReferenceStation cache
    MutableConfiguration<UUID, ReferenceStation> referenceStationCacheConfig = new MutableConfiguration<>();
    referenceStationCacheConfig
        .setStoreByValue(false)
        .setExpiryPolicyFactory(
            CreatedExpiryPolicy.factoryOf(
                new Duration(
                    TimeUnit.HOURS,
                    4
                )
            )
        );

    return GACacheJCache.create(
        cacheManager,
        sdhStationAssociationCacheConfig,
        eventHypothesisCacheConfig,
        referenceStationCacheConfig
    );
  }
}
