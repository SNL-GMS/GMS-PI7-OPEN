package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.TransferredFileRepositoryJpa;
import gms.shared.utilities.service.HttpService;
import gms.shared.utilities.service.Route;
import gms.shared.utilities.service.ServiceDefinition;
import org.apache.commons.configuration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Application {
  /**
   * Starts the application with default parameters.
   *
   * @param args N/A
   */
  public static void main(String[] args) {
    new HttpService(getHttpServiceDefinition(8080)).start();
  }

  /**
   * Return the service definition.
   *
   * @param port port number
   * @return service definition
   */
  private static ServiceDefinition getHttpServiceDefinition(int port) {

    Logger logger = LoggerFactory.getLogger(Application.class);

    Configuration config;
    try {
      config = Application.loadConfig();
    } catch (ConfigurationException e) {
      logger.error(String.format("Cannot start application - failed to load configuration: %s", e.getMessage()));
      throw new IllegalStateException(e);
    }

    EntityManagerFactory entityManagerFactory = CoiEntityManagerFactory.create(loadHibernateConfig(config));

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        logger.info("Shutting down EntityManageFactory");
        entityManagerFactory.close();
    }));

    TransferredFileRepositoryJpa transferredFileRepository = new TransferredFileRepositoryJpa(entityManagerFactory);

    RequestHandlers handlers = new RequestHandlers(transferredFileRepository);

    return ServiceDefinition.builder()
            .setJsonMapper(CoiObjectMapperFactory.getJsonObjectMapper())
            .setMsgpackMapper(CoiObjectMapperFactory.getMsgpackObjectMapper())
            .setRoutes(Set.of(
                    Route.create(
                            "/is-alive",
                            handlers::isAlive),
                    Route.create(
                            "/data-acquisition/status/transferred-file/by-transfer-time",
                            handlers::retrieveTransferredFilesByTimeRange
                    )
            ))
            .setPort(port)
            .build();
  }

  private static CompositeConfiguration loadConfig()
          throws ConfigurationException {

    CompositeConfiguration config = new CompositeConfiguration();
    config.addConfiguration(new EnvironmentConfiguration());
    config.addConfiguration(new PropertiesConfiguration(
            "gms/shared/mechanisms/objectstoragedistribution/coi/transferredfile/service/application.properties"));

    return config;
  }

  private static Map<String, String> loadHibernateConfig(Configuration configuration) {
    String coiServiceHost = configuration.getString("persistence_url");

    if (Objects.nonNull(coiServiceHost)) {
      return Map.of("hibernate.connection.url", coiServiceHost);
    } else {
      return Map.of();
    }
  }

}
