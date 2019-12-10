package gms.core.eventlocation.control;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.jpa.StationReferenceRepositoryJpa;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class EventLocationControlOsdGatewayTests {

  // Used to create the testing EntityManagerFactory
  private static final String UNIT_NAME = CoiEntityManagerFactory.UNIT_NAME;

  // Used to create the testing EntityManagerFactory
  private static final Map<String, String> h2Properties = Map.of(
      "hibernate.connection.driver_class", "org.h2.Driver",
      "hibernate.connection.url", "jdbc:h2:mem:test",
      "hibernate.dialect", "org.hibernate.dialect.H2Dialect",
      "hibernate.hbm2ddl.auto", "create-drop",
      "hibernate.flushMode", "FLUSH_AUTO");

  // Used to create the testing EntityManagerFactory - connects to local h2 database
  static EntityManagerFactory createTestingEntityManagerFactory() {
    try {
      return Persistence.createEntityManagerFactory(UNIT_NAME, h2Properties);
    } catch (PersistenceException e) {
      throw new IllegalArgumentException("Could not create persistence unit " + UNIT_NAME, e);
    }
  }

  private ObjectMapper jsonObjectMapper;

  private WireMockServer wireMockServer;

  private EntityManagerFactory entityManagerFactory;

  private static final String HOSTNAME = "localhost";
  private static final String STORE_EVENTS_URL_PATH = "/coi/events";
  private static final String QUERY_EVENTS_URL_PATH = "/coi/events/query/ids";

  private static final String STORED_EVENTS_KEY = "storedEvents";
  private static final String UPDATED_EVENTS_KEY = "updatedEvents";
  private static final String ERROR_EVENTS_KEY = "errorEvents";


  @BeforeEach
  void init() {

    this.jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    this.entityManagerFactory = EventLocationControlOsdGatewayTests
        .createTestingEntityManagerFactory();

    this.wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
    this.wireMockServer.start();
  }

  @AfterEach
  void teardown() {

    this.wireMockServer.stop();
  }

  /**
   * Tests success case of retrieving Events
   */
  @Test
  public void testRetrieveEvents() throws UnirestException, IOException {

    Collection<UUID> eventIds = List.of(TestFixtures.event.getId());

    // Create mock HTTP response body
    String responseBody = this.jsonObjectMapper
        .writeValueAsString(List.of(TestFixtures.event));

    // Mock that HTTP POST returns the mock response body
    this.wireMockServer
        .stubFor(post(urlEqualTo(EventLocationControlOsdGatewayTests.QUERY_EVENTS_URL_PATH))
            .withRequestBody(equalTo(this.jsonObjectMapper.writeValueAsString(eventIds)))
            .willReturn(ok().withBody(responseBody)));

    // Create osdGateway using wiremock's dynamic port
    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME, this.wireMockServer.port(),
            new StationReferenceRepositoryJpa(this.entityManagerFactory));

    // Call retrieval method
    Collection<Event> retrievedEvents = osdGateway.retrieveEvents(eventIds);

    // this.wireMockServer.verify that we sent the correct HTTP request to the wiremock server
    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(EventLocationControlOsdGatewayTests.QUERY_EVENTS_URL_PATH))
        .withRequestBody(equalTo(this.jsonObjectMapper.writeValueAsString(eventIds))));

    // Assert the collection of input events' UUIDs is equal to the collection of the successfully stored/updated events' UUIDs
    Assertions.assertEquals(eventIds,
        retrievedEvents.stream().map(Event::getId).collect(Collectors.toCollection(
            ArrayList::new)));
  }

  @Test
  public void testRetrieveEventsNot200() throws UnirestException, IOException {

    Collection<UUID> eventIds = List.of(TestFixtures.event.getId());

    // Create mock HTTP response body
    this.wireMockServer
        .stubFor(post(urlEqualTo(EventLocationControlOsdGatewayTests.QUERY_EVENTS_URL_PATH))
            .withRequestBody(equalTo(this.jsonObjectMapper.writeValueAsString(eventIds)))
            .willReturn(badRequest().withBody("Server Error")));

    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME, this.wireMockServer.port(),
            new StationReferenceRepositoryJpa(this.entityManagerFactory));

    Assertions.assertThrows(IllegalStateException.class, () ->
        osdGateway.retrieveEvents(eventIds)
    );

    this.wireMockServer.verify(
        postRequestedFor(urlEqualTo(EventLocationControlOsdGatewayTests.QUERY_EVENTS_URL_PATH))
            .withRequestBody(equalTo(this.jsonObjectMapper.writeValueAsString(eventIds))));
  }

  /**
   * Tests success case of storing new events with EventLocationControlOsdGateway::storeOrUpdateEvents()
   */
  @Test
  public void testStoreEvents() throws UnirestException, IOException {

    Collection<Event> events = List.of(TestFixtures.event);

    // Create mock HTTP response body that indicates events were STORED
    ObjectNode jsonNode = this.jsonObjectMapper.createObjectNode()
        .putPOJO(EventLocationControlOsdGatewayTests.STORED_EVENTS_KEY,
            this.jsonObjectMapper.writeValueAsString(List.of(events.iterator().next().getId())))
        .putPOJO(EventLocationControlOsdGatewayTests.UPDATED_EVENTS_KEY,
            this.jsonObjectMapper.writeValueAsString(List.of()))
        .putPOJO(EventLocationControlOsdGatewayTests.ERROR_EVENTS_KEY,
            this.jsonObjectMapper.writeValueAsString(List.of()));

    // Mock that HTTP POST returns the mock response body
    this.wireMockServer
        .stubFor(post(urlEqualTo(EventLocationControlOsdGatewayTests.STORE_EVENTS_URL_PATH))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(events)))
            .willReturn(ok().withBody(jsonNode.toString())));

    // Create osdGateway using wiremock's dynamic port
    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME, this.wireMockServer.port(),
            new StationReferenceRepositoryJpa(this.entityManagerFactory));

    // Call store/update osd gateway operation
    Collection<UUID> storedOrUpdatedEventIds = osdGateway.storeOrUpdateEvents(events);

    // this.wireMockServer.verify that we sent the correct HTTP request to the wiremock server
    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(EventLocationControlOsdGatewayTests.STORE_EVENTS_URL_PATH))
        .withRequestBody(equalTo(this.jsonObjectMapper.writeValueAsString(events))));

    // Assert the collection of input events' UUIDs is equal to the collection of the successfully stored/updated events' UUIDs
    Assertions.assertEquals(events.stream().map(Event::getId).collect(Collectors.toList()),
        storedOrUpdatedEventIds);
  }

  /**
   * Tests success case of updating events with EventLocationControlOsdGateway::storeOrUpdateEvents()
   */
  @Test
  public void testUpdateEvents() throws UnirestException, IOException {

    Collection<Event> events = List.of(TestFixtures.event);

    // Create mock HTTP response body that indicates events were UPDATED
    ObjectNode jsonNode = this.jsonObjectMapper.createObjectNode()
        .putPOJO(EventLocationControlOsdGatewayTests.STORED_EVENTS_KEY,
            this.jsonObjectMapper.writeValueAsString(List.of()))
        .putPOJO(EventLocationControlOsdGatewayTests.UPDATED_EVENTS_KEY,
            this.jsonObjectMapper.writeValueAsString(List.of(events.iterator().next().getId())))
        .putPOJO(EventLocationControlOsdGatewayTests.ERROR_EVENTS_KEY,
            this.jsonObjectMapper.writeValueAsString(List.of()));

    // Mock that HTTP POST returns the mock response body
    this.wireMockServer
        .stubFor(post(urlEqualTo(EventLocationControlOsdGatewayTests.STORE_EVENTS_URL_PATH))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(events)))
            .willReturn(ok().withBody(jsonNode.toString())));

    // Create osdGateway using wiremock's dynamic port
    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME, this.wireMockServer.port(),
            new StationReferenceRepositoryJpa(this.entityManagerFactory));

    // Call store/update osd gateway operation
    Collection<UUID> storedOrUpdatedEventIds = osdGateway.storeOrUpdateEvents(events);

    // this.wireMockServer.verify that we sent the correct HTTP request to the wiremock server
    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(EventLocationControlOsdGatewayTests.STORE_EVENTS_URL_PATH))
        .withRequestBody(equalTo(this.jsonObjectMapper.writeValueAsString(events))));

    // Assert the list of input events' UUIDs is equal to the list of the successfully stored/updated events' UUIDs
    Assertions.assertEquals(events.stream().map(Event::getId).collect(Collectors.toList()),
        storedOrUpdatedEventIds);
  }

  /**
   * Tests failure case of attempting to store or update events with EventLocationControlOsdGateway::storeOrUpdateEvents()
   */
  @Test
  public void testStoreOrUpdateEventsError() throws UnirestException, IOException {

    Collection<Event> events = List.of(TestFixtures.event);

    // Create mock HTTP response body that indicates events were UPDATED
    ObjectNode jsonNode = this.jsonObjectMapper.createObjectNode()
        .putPOJO(EventLocationControlOsdGatewayTests.STORED_EVENTS_KEY,
            this.jsonObjectMapper.writeValueAsString(List.of()))
        .putPOJO(EventLocationControlOsdGatewayTests.UPDATED_EVENTS_KEY,
            this.jsonObjectMapper.writeValueAsString(List.of()))
        .putPOJO(EventLocationControlOsdGatewayTests.ERROR_EVENTS_KEY,
            this.jsonObjectMapper.writeValueAsString(List.of(events.iterator().next().getId())));

    // Mock that HTTP POST returns the mock response body
    this.wireMockServer
        .stubFor(post(urlEqualTo(EventLocationControlOsdGatewayTests.STORE_EVENTS_URL_PATH))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(events)))
            .willReturn(ok().withBody(jsonNode.toString())));

    // Create osdGateway using wiremock's dynamic port
    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME, this.wireMockServer.port(),
            new StationReferenceRepositoryJpa(this.entityManagerFactory));

    // Call store/update osd gateway operation
    Collection<UUID> storedOrUpdatedEventIds = osdGateway.storeOrUpdateEvents(events);

    // this.wireMockServer.verify that we sent the correct HTTP request to the wiremock server
    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(EventLocationControlOsdGatewayTests.STORE_EVENTS_URL_PATH))
        .withRequestBody(equalTo(this.jsonObjectMapper.writeValueAsString(events))));

    // Assert the list of the successfully stored/updated events' UUIDs is empty
    Assertions.assertEquals(List.of(), storedOrUpdatedEventIds);
  }

  /**
   * Tests server returns 500 response
   */
  @Test
  public void testStoreOrUpdateEvents500Response() throws IOException {

    Collection<Event> events = List.of(TestFixtures.event);

    this.wireMockServer
        .stubFor(post(urlEqualTo(EventLocationControlOsdGatewayTests.STORE_EVENTS_URL_PATH))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(events)))
            .willReturn(WireMock.aResponse().withStatus(500)));

    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME, this.wireMockServer.port(),
            new StationReferenceRepositoryJpa(this.entityManagerFactory));
    Assertions.assertThrows(RuntimeException.class, () -> osdGateway.storeOrUpdateEvents(events));

    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(EventLocationControlOsdGatewayTests.STORE_EVENTS_URL_PATH))
        .withRequestBody(equalTo(
            this.jsonObjectMapper.writeValueAsString(events))));
  }

  /**
   * Tests server returns 409 conflict response
   */
  @Test
  public void testStoreOrUpdateEvents409Conflict() throws IOException {
    Collection<Event> events = List.of(TestFixtures.event);

    this.wireMockServer
        .stubFor(post(urlEqualTo(EventLocationControlOsdGatewayTests.STORE_EVENTS_URL_PATH))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(events)))
            .willReturn(WireMock.aResponse().withStatus(409)));

    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME, this.wireMockServer.port(),
            new StationReferenceRepositoryJpa(this.entityManagerFactory));
    Assertions
        .assertThrows(IllegalArgumentException.class, () -> osdGateway.storeOrUpdateEvents(events));

    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(EventLocationControlOsdGatewayTests.STORE_EVENTS_URL_PATH))
        .withRequestBody(equalTo(
            this.jsonObjectMapper.writeValueAsString(events))));
  }

  /**
   * Tests server returns 400 response
   */
  @Test
  public void testStoreOrUpdateEvents400Response() throws IOException {
    Collection<Event> events = List.of(TestFixtures.event);

    this.wireMockServer
        .stubFor(post(urlEqualTo(EventLocationControlOsdGatewayTests.STORE_EVENTS_URL_PATH))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(events)))
            .willReturn(WireMock.aResponse().withStatus(400)));

    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME, this.wireMockServer.port(),
            new StationReferenceRepositoryJpa(this.entityManagerFactory));
    Assertions.assertThrows(RuntimeException.class, () -> osdGateway.storeOrUpdateEvents(events));

    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(EventLocationControlOsdGatewayTests.STORE_EVENTS_URL_PATH))
        .withRequestBody(equalTo(
            this.jsonObjectMapper.writeValueAsString(events))));
  }


  @Test
  public void testCreateNullHost() {
    Assertions.assertThrows(NullPointerException.class, () ->
        EventLocationControlOsdGateway.create(
            null,
            8080,
            new StationReferenceRepositoryJpa(this.entityManagerFactory)
        )
    );
  }

  @Test
  public void testCreateNullPort() {
    Assertions.assertThrows(NullPointerException.class, () ->
        EventLocationControlOsdGateway.create(
            EventLocationControlOsdGatewayTests.HOSTNAME,
            null,
            new StationReferenceRepositoryJpa(this.entityManagerFactory)
        )
    );
  }

  @Test
  public void teststoreOrUpdateEventsNullEvents() throws MalformedURLException {
    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME, 8080,
            new StationReferenceRepositoryJpa(this.entityManagerFactory));
    Assertions.assertThrows(NullPointerException.class, () ->
        osdGateway.storeOrUpdateEvents(null)
    );
  }

  /**
   * Tests success case of SignalDetectionAssociationOsdGateway::retrieveSignalDetections()
   */
  @Test
  void testRetrieveSignalDetections()
      throws UnirestException, IOException {

    List<SignalDetection> signalDetections = TestFixtures.signalDetections;
    List<UUID> signalDetectionIds = signalDetections.stream().map(SignalDetection::getId).collect(
        Collectors.toList());

    Map<String, Object> requestBodyParams = Map.ofEntries(
        Map.entry("ids", signalDetectionIds)
    );

    this.wireMockServer.stubFor(post(urlEqualTo("/coi/signal-detections/query/ids"))
        .withRequestBody(equalTo(this.jsonObjectMapper.writeValueAsString(requestBodyParams)))
        .willReturn(
            ok().withBody(this.jsonObjectMapper.writeValueAsString(signalDetections))));

    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME,
            this.wireMockServer.port(),
            new StationReferenceRepositoryJpa(this.entityManagerFactory));

    List<SignalDetection> retrievedSignalDetectionHypotheses = osdGateway
        .retrieveSignalDetections(signalDetectionIds);

    Assertions.assertEquals(signalDetections, retrievedSignalDetectionHypotheses);

    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo("/coi/signal-detections/query/ids"))
        .withRequestBody(
            equalTo(this.jsonObjectMapper.writeValueAsString(requestBodyParams))));
  }

  @Test
  void testRetrieveSignalDetectionsNullUuids() throws MalformedURLException {
    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME, 8080,
            new StationReferenceRepositoryJpa(this.entityManagerFactory));
    Assertions.assertThrows(NullPointerException.class, () ->
        osdGateway.retrieveSignalDetections(null)
    );
  }

  /**
   * Tests success case of SignalDetectionAssociationOsdGateway::retrieveSignalDetectionHypotheses()
   */
  @Test
  void testRetrieveSignalDetectionHypotheses()
      throws UnirestException, IOException {

    List<SignalDetectionHypothesis> signalDetectionHypotheses = TestFixtures.signalDetectionHypotheses;
    List<UUID> signalDetectionHypothesisIds = signalDetectionHypotheses.stream()
        .map(SignalDetectionHypothesis::getId).collect(
            Collectors.toList());

    ObjectNode requestBodyJsonNode = this.jsonObjectMapper.createObjectNode()
        .putPOJO("ids", this.jsonObjectMapper.writeValueAsString(signalDetectionHypothesisIds));

    this.wireMockServer.stubFor(post(urlEqualTo("/coi/signal-detections/hypotheses/query/ids"))
        .withRequestBody(equalTo(requestBodyJsonNode.toString()))
        .willReturn(
            ok().withBody(this.jsonObjectMapper.writeValueAsString(signalDetectionHypotheses))));

    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME,
            this.wireMockServer.port(),
            new StationReferenceRepositoryJpa(this.entityManagerFactory));

    List<SignalDetectionHypothesis> retrievedSignalDetectionHypotheses = osdGateway
        .retrieveSignalDetectionHypotheses(signalDetectionHypothesisIds);

    Assertions.assertEquals(signalDetectionHypotheses, retrievedSignalDetectionHypotheses);

    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo("/coi/signal-detections/hypotheses/query/ids"))
        .withRequestBody(equalTo(requestBodyJsonNode.toString())));
  }

  @Test
  void testRetrieveSignalDetectionHypothesesNullUuids() throws MalformedURLException {
    EventLocationControlOsdGateway osdGateway = EventLocationControlOsdGateway
        .create(EventLocationControlOsdGatewayTests.HOSTNAME, 8080,
            new StationReferenceRepositoryJpa(this.entityManagerFactory));
    Assertions.assertThrows(NullPointerException.class, () ->
        osdGateway.retrieveSignalDetectionHypotheses(null)
    );
  }
}
