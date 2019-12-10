package gms.core.signaldetection.association.control.integration;

import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.signaldetection.association.control.SignalDetectionAssociationControlOsdGateway;
import gms.core.signaldetection.association.control.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.StationReferenceRepositoryJpa;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;

class SignalDetectionAssociationControlOsdGatewayIntegrationTests {
  private StationReferenceRepositoryInterface stationReferenceRepositoryInterface;
  private SignalDetectionAssociationControlOsdGateway osdGateway;

  SignalDetectionAssociationControlOsdGatewayIntegrationTests() throws MalformedURLException {
    this.stationReferenceRepositoryInterface = new StationReferenceRepositoryJpa();
    this.osdGateway = SignalDetectionAssociationControlOsdGateway.create("localhost", 8080,
        stationReferenceRepositoryInterface);
  }

  @IntegrationTest
  void testRetrieveSignalDetectionHypotheses() throws UnirestException, IOException {

    final UUID hypothesisId1 = UUID
        .fromString("d4f55ab2-eedb-3f0a-bd76-2936a68e862e");

    List<UUID> hypothesisIds = List.of(
        hypothesisId1
    );

    List<SignalDetectionHypothesis> hypotheses = this.osdGateway
        .retrieveSignalDetectionHypotheses(hypothesisIds);

    List<UUID> retrievedHypothesisIds = hypotheses.stream().map(SignalDetectionHypothesis::getId).collect(
        Collectors.toList());

    Assertions.assertEquals(hypothesisIds, retrievedHypothesisIds);
  }

  @IntegrationTest
  void testStoreEvents() throws UnirestException, IOException {
    Event event = TestFixtures.associatedEvent;

    List<Event> eventsToStore = List.of(event);
    List<UUID> eventIdsToStore = eventsToStore.stream().map(Event::getId)
        .collect(Collectors.toList());

    List<UUID> storedEventIds = this.osdGateway.storeOrUpdateEvents(eventsToStore);

    Assertions.assertEquals(eventIdsToStore, storedEventIds);
  }

}
