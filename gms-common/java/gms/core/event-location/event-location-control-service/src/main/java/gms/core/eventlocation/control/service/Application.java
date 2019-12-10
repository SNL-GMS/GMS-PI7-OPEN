package gms.core.eventlocation.control.service;

import gms.core.eventlocation.control.EventLocationControl;
import gms.core.eventlocation.control.EventLocationControlOsdGateway;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.StationReferenceRepositoryJpa;
import gms.shared.utilities.service.HttpService;
import gms.shared.utilities.service.Route;
import gms.shared.utilities.service.ServiceDefinition;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

  // Port the application will run on
  private static final int PORT = 8080;

  /**
   * Starts the application with default parameters.
   *
   * @param args N/A
   */
  public static void main(String[] args) {
    new HttpService(getHttpServiceDefinition()).start();
  }

  /**
   * Return the service definition.
   *
   * @return service definition
   */
  private static ServiceDefinition getHttpServiceDefinition() {

    RequestHandlers handlers = RequestHandlers
        .create(EventLocationControl.create(Application.createOsdGateway()));

    return ServiceDefinition.builder()
        .setJsonMapper(CoiObjectMapperFactory.getJsonObjectMapper())
        .setMsgpackMapper(CoiObjectMapperFactory.getMsgpackObjectMapper())
        .setRoutes(Set.of(
            Route.create(
                "/is-alive",
                handlers::isAlive),
            Route.create(
                "/event/location/locate", handlers::locate),
            Route.create(
                "/event/location/locate/interactive", handlers::locateInteractive),
            Route.create(
                "/event/location/locate/validation", handlers::locateValidation
            )
        ))
        .setPort(Application.PORT)
        .build();
  }

  // Creates a new EventLocationControlOsdGateway set to connect to the COI service
  //   using properties COI_SERVICE_HOST and COI_SERVICE_PORT.  First checks environment variables,
  //   and if they are not set, checks application.properties.
  //
  // Throws a RuntimeException if host/port values cannot be read from the environment or application.properties.
  private static EventLocationControlOsdGateway createOsdGateway() {

    Logger logger = LoggerFactory.getLogger(Application.class);

    Configuration config;
    try {
      config = Application.loadConfig();
    } catch (ConfigurationException e) {
      logger.error("Cannot start application - failed to load configuration: {}", e.getMessage());
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

      throw new IllegalStateException(errMsg, e);
    }

    if (!Objects.isNull(coiServiceHost)) {
      try {
        logger.info(
            "EventLocationControlOsdGateway is set to connect to the COI service at http://{}:{}.",
            coiServiceHost, coiServicePort);
        return EventLocationControlOsdGateway.create(
            coiServiceHost,
            coiServicePort,
            new StationReferenceRepositoryJpa(
                CoiEntityManagerFactory.create(Application.loadHibernateConfig(config)))
        );
      } catch (MalformedURLException e) {
        logger.error("Malformed COI service URL", e);
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
        "gms/core/eventlocation/control/service/application.properties"));

    return config;
  }

  private static Map<String, String> loadHibernateConfig(Configuration configuration) {
    String coiServiceHost = configuration.getString("persistence_url");

    if (Objects.nonNull(coiServiceHost)) {
      return Map.ofEntries(Map.entry("hibernate.connection.url", coiServiceHost));
    } else {
      return Map.of();
    }
  }
}
