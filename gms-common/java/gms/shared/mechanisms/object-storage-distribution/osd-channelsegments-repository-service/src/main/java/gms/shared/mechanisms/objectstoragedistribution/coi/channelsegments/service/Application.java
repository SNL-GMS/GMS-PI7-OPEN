package gms.shared.mechanisms.objectstoragedistribution.coi.channelsegments.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.channelsegments.repository.ChannelSegmentsRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.channelsegments.repository.jpa.ChannelSegmentsRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.JpaCassandraWaveformRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.configuration.CassandraConfig;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.FkSpectraRepositoryJpa;
import gms.shared.utilities.service.HttpService;
import gms.shared.utilities.service.Route;
import gms.shared.utilities.service.ServiceDefinition;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    CassandraConfig cassandraConfig = Application.loadCassandraConfig(config);

    ChannelSegmentsRepository channelSegmentsRepository = ChannelSegmentsRepositoryJpa.create(
        entityManagerFactory,
        new JpaCassandraWaveformRepository(entityManagerFactory, cassandraConfig),
        new FkSpectraRepositoryJpa(entityManagerFactory, cassandraConfig)
    );

    RequestHandlers handlers = RequestHandlers.create(channelSegmentsRepository);

    return ServiceDefinition.builder()
        .setJsonMapper(CoiObjectMapperFactory.getJsonObjectMapper())
        .setMsgpackMapper(CoiObjectMapperFactory.getMsgpackObjectMapper())
        .setRoutes(Set.of(
            Route.create(
                "/is-alive",
                handlers::isAlive),
            Route.create(
                "/coi/channel-segments/query/segment-ids",
                handlers::retrieveBySegmentIds
            ),
            Route.create(
                "/coi/channel-segments/query/channel-ids",
                handlers::retrieveByChannelIdsAndTime)
        ))
        .setPort(port)
        .build();
  }

  private static CompositeConfiguration loadConfig()
      throws ConfigurationException {

    CompositeConfiguration config = new CompositeConfiguration();
    config.addConfiguration(new EnvironmentConfiguration());
    config.addConfiguration(new PropertiesConfiguration(
        "gms/shared/mechanisms/objectstoragedistribution/coi/channelsegments/service/application.properties"));

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

  private static CassandraConfig loadCassandraConfig(Configuration configuration) {

    String cassandraUrl = configuration.getString("cassandra_url");

    CassandraConfig.Builder cassandraConfigBuilder = CassandraConfig.builder();

    if (Objects.nonNull(cassandraUrl)) {
      String[] cassandraHostAndPort = cassandraUrl.split(":");

      if (cassandraHostAndPort.length != 2) {
        throw new IllegalArgumentException(
            "\"cassandra_url\" is not in the correct format, should be: <hostname>:<port>");
      }

      String cassandraHost = cassandraHostAndPort[0];
      int cassandraPort = Integer.parseInt(cassandraHostAndPort[1]);

      cassandraConfigBuilder.setConnectPoints(cassandraHost).setPort(cassandraPort);
    }

    return cassandraConfigBuilder.build();
  }
}
