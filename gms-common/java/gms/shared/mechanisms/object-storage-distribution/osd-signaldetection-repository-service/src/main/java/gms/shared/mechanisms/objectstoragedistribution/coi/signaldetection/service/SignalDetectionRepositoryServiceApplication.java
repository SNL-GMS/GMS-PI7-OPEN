package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.EventRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.ChannelProcessingGroupRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.QcMaskRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.SignalDetectionRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.configuration.ConfigurationLoader;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.StationReferenceRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.FkSpectraRepositoryJpa;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

/**
 * Application entry point for the Signal Detection Repository Service. Starts a {@link
 * SignalDetectionRepositoryService} with loaded {@link Configuration} and a specific {@link
 * QcMaskRepository} implementation.
 */
public class SignalDetectionRepositoryServiceApplication {
  public static void main(String[] args) {
    Runtime.getRuntime()
        .addShutdownHook(new Thread(SignalDetectionRepositoryService::stopService));
    final Configuration config = ConfigurationLoader.load();
    final Map<String, String> entityManagerProps = new HashMap<>();
    config.getPersistenceUrl().ifPresent(s ->
        entityManagerProps.put("hibernate.connection.url", s));
    final EntityManagerFactory emFactory = CoiEntityManagerFactory.create(entityManagerProps);
    Runtime.getRuntime()
        .addShutdownHook(new Thread(emFactory::close));
    SignalDetectionRepositoryService.startService(
        config,
        QcMaskRepositoryJpa.create(emFactory),
        SignalDetectionRepositoryJpa.create(emFactory),
        new FkSpectraRepositoryJpa(emFactory),
        ChannelProcessingGroupRepositoryJpa.create(emFactory),
        new EventRepositoryJpa(emFactory),
        new ProcessingStationReferenceFactory(new StationReferenceRepositoryJpa(emFactory)));
  }
}
