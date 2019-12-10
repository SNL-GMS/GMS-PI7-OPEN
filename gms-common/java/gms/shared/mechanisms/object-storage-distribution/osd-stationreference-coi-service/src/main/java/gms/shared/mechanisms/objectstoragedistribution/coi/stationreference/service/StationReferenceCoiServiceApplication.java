package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.StationReferenceRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.service.configuration.ConfigurationLoader;
import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Application entry point for the Signal Detection Repository Service. Starts a {@link
 * StationReferenceCoiService} with loaded {@link Configuration} and a specific {@link
 * StationReferenceRepositoryInterface} implementation.
 */
public class StationReferenceCoiServiceApplication {

  public static void main(String[] args) {
    Runtime.getRuntime()
        .addShutdownHook(new Thread(StationReferenceCoiService::stopService));
    final Map<String, String> entityMgrProps = new HashMap<>();
    final Configuration config = ConfigurationLoader.load();
    config.getPersistenceUrl().ifPresent(s -> entityMgrProps.put("hibernate.connection.url", s));
    EntityManagerFactory entityManagerFactory = CoiEntityManagerFactory.create(entityMgrProps);
    Runtime.getRuntime().addShutdownHook(new Thread(entityManagerFactory::close));
    final StationReferenceRepositoryInterface stationRefRepo = new StationReferenceRepositoryJpa(
        entityManagerFactory);


    StationReferenceCoiService.startService(config, stationRefRepo,
        new ProcessingStationReferenceFactory(stationRefRepo));
  }
}
