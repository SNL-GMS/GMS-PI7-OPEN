package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.BeamRepositoryCassandraJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.JpaCassandraWaveformRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.configuration.CassandraConfig;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.FkSpectraRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.RawStationDataFrameRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.StationSohRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.configuration.ConfigurationLoader;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

/**
 * Application entry point for the Waveforms Repository Service. Starts a {@link
 * WaveformRepositoryService} with loaded {@link Configuration} and a specific {@link
 * WaveformRepository}, {@link StationSohRepositoryInterface},
 * and {@link RawStationDataFrameRepositoryInterface} implementation.
 */
public class WaveformRepositoryServiceApplication {

  private static CassandraConfig buildCassandraConfig(Configuration config){
    CassandraConfig.Builder builder = CassandraConfig.builder().setConnectPoints(config.cassandra_connect_points)
        .setPort(config.cassandraPort)
        .setUser(config.cassandraUser)
        .setPass(config.cassandraPass)
        .setClusterName(config.cassandraClusterName)
        .setWaveformTable(config.waveformTable);
    return builder.build();
  }

  public static void main(String[] args) throws Exception {
    final Configuration config = ConfigurationLoader.load();

    final Map<String, String> entityMgrProps = new HashMap<>();
    config.persistenceUrl.ifPresent(s -> entityMgrProps.put("hibernate.connection.url", s));
    final EntityManagerFactory emFactory = CoiEntityManagerFactory.create(entityMgrProps);

    final CassandraConfig cassandraConfig = buildCassandraConfig(config);

    Runtime.getRuntime()
        .addShutdownHook(new Thread(emFactory::close));
    Runtime.getRuntime()
        .addShutdownHook(new Thread(WaveformRepositoryService::stopService));

    WaveformRepositoryService.startService(
        config,
        new JpaCassandraWaveformRepository(emFactory, cassandraConfig),
        new StationSohRepositoryJpa(emFactory),
        new RawStationDataFrameRepositoryJpa(emFactory),
        new BeamRepositoryCassandraJpa(emFactory, cassandraConfig),
        new FkSpectraRepositoryJpa(emFactory, cassandraConfig));
  }
}
