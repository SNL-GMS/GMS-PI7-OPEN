package gms.core.signaldetection.association.control;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.repository.StationReferenceRepositoryInterface;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Null;

class SignalDetectionAssociationControlOsdGatewayTests {

  private ObjectMapper jsonObjectMapper;

  private WireMockServer wireMockServer;
  private StationReferenceRepositoryInterface mockStationRepo;

  private static final String HOSTNAME = "localhost";

  private static final String STORED_EVENTS_KEY = "storedEvents";
  private static final String UPDATED_EVENTS_KEY = "updatedEvents";
  private static final String ERROR_EVENTS_KEY = "errorEvents";

  private static final String STORE_EVENTS_PATH = "/coi/events";
  private static final String STATIONS_BASE_URL = "/coi/stations";
  private static final String STATIONS_RETRIEVAL_URL = STATIONS_BASE_URL + "/query/versionIds";
  private static final String EVENTS_RETRIEVAL_URL = STORE_EVENTS_PATH + "/query/time-lat-lon";

  @BeforeEach
  void init() {

    this.jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    this.wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
    this.wireMockServer.start();
    this.mockStationRepo = Mockito.mock(StationReferenceRepositoryInterface.class);
  }

  @AfterEach
  void teardown() {

    this.wireMockServer.stop();
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

    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME,
            this.wireMockServer.port(),
            this.mockStationRepo);

    List<SignalDetectionHypothesis> retrievedSignalDetectionHypotheses = osdGateway
        .retrieveSignalDetectionHypotheses(signalDetectionHypothesisIds);

    Assertions.assertEquals(signalDetectionHypotheses, retrievedSignalDetectionHypotheses);

    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo("/coi/signal-detections/hypotheses/query/ids"))
        .withRequestBody(equalTo(requestBodyJsonNode.toString())));
  }

  /**
   * Tests success case of storing new events with SignalDetectionAssociationOsdGateway::storeOrUpdateEvents()
   */
  @Test
  void testStoreEvents() throws UnirestException, IOException {

    List<Event> events = List.of(TestFixtures.associatedEvent);

    // Create mock HTTP response body that indicates events were STORED
    Map<String, Object> requestBodyParams = Map.ofEntries(
        Map.entry(SignalDetectionAssociationControlOsdGatewayTests.STORED_EVENTS_KEY,
            List.of(events.get(0).getId())),
        Map.entry(SignalDetectionAssociationControlOsdGatewayTests.UPDATED_EVENTS_KEY, List.of()),
        Map.entry(SignalDetectionAssociationControlOsdGatewayTests.ERROR_EVENTS_KEY, List.of())
    );

    System.out.println(this.jsonObjectMapper.writeValueAsString(requestBodyParams));

    // Mock that HTTP POST returns the mock response body
    this.wireMockServer.stubFor(
        post(urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STORE_EVENTS_PATH))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(events)))
            .willReturn(
                ok().withBody(this.jsonObjectMapper.writeValueAsString(requestBodyParams))));

    // Create osdGateway using wiremock's dynamic port
    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME,
            this.wireMockServer.port(),
            this.mockStationRepo);

    // Call store/update osd gateway operation
    List<UUID> storedOrUpdatedEventIds = osdGateway.storeOrUpdateEvents(events);

    // this.wireMockServer.verify that we sent the correct HTTP request to the wiremock server
    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STORE_EVENTS_PATH))
        .withRequestBody(equalTo(this.jsonObjectMapper.writeValueAsString(events))));

    // Assert the list of input events' UUIDs is equal to the list of the successfully stored/updated events' UUIDs
    Assertions.assertEquals(events.stream().map(Event::getId).collect(Collectors.toList()),
        storedOrUpdatedEventIds);
  }

  /**
   * Tests success case of updating events with SignalDetectionAssociationOsdGateway::storeOrUpdateEvents()
   */
  @Test
  void testUpdateEvents() throws UnirestException, IOException {

    List<Event> events = List.of(TestFixtures.associatedEvent);

    // Create mock HTTP response body that indicates events were UPDATED
    Map<String, Object> requestBodyParams = Map.ofEntries(
        Map.entry(SignalDetectionAssociationControlOsdGatewayTests.STORED_EVENTS_KEY, List.of()),
        Map.entry(SignalDetectionAssociationControlOsdGatewayTests.UPDATED_EVENTS_KEY,
            List.of(events.get(0).getId())),
        Map.entry(SignalDetectionAssociationControlOsdGatewayTests.ERROR_EVENTS_KEY, List.of())
    );

    // Mock that HTTP POST returns the mock response body
    this.wireMockServer.stubFor(
        post(urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STORE_EVENTS_PATH))
            .withRequestBody(equalTo(this.jsonObjectMapper.writeValueAsString(events)))
            .willReturn(
                ok().withBody(this.jsonObjectMapper.writeValueAsString(requestBodyParams))));

    // Create osdGateway using wiremock's dynamic port
    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME,
            this.wireMockServer.port(),
            this.mockStationRepo);

    // Call store/update osd gateway operation
    List<UUID> storedOrUpdatedEventIds = osdGateway.storeOrUpdateEvents(events);

    // this.wireMockServer.verify that we sent the correct HTTP request to the wiremock server
    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STORE_EVENTS_PATH))
        .withRequestBody(equalTo(this.jsonObjectMapper.writeValueAsString(events))));

    // Assert the list of input events' UUIDs is equal to the list of the successfully stored/updated events' UUIDs
    Assertions.assertEquals(events.stream().map(Event::getId).collect(Collectors.toList()),
        storedOrUpdatedEventIds);
  }

  /**
   * Tests failure case of attempting to store or update events with SignalDetectionAssociationOsdGateway::storeOrUpdateEvents()
   */
  @Test
  void testStoreOrUpdateEventsError() throws UnirestException, IOException {

    List<Event> events = List.of(TestFixtures.associatedEvent);

    // Create mock HTTP response body that indicates events were UPDATED
    Map<String, Object> requestBodyParams = Map.ofEntries(
        Map.entry(SignalDetectionAssociationControlOsdGatewayTests.STORED_EVENTS_KEY, List.of()),
        Map.entry(SignalDetectionAssociationControlOsdGatewayTests.UPDATED_EVENTS_KEY, List.of()),
        Map.entry(SignalDetectionAssociationControlOsdGatewayTests.ERROR_EVENTS_KEY,
            List.of(events.get(0).getId()))
    );

    // Mock that HTTP POST returns the mock response body
    this.wireMockServer.stubFor(
        post(urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STORE_EVENTS_PATH))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(events)))
            .willReturn(
                ok().withBody(this.jsonObjectMapper.writeValueAsString(requestBodyParams))));

    // Create osdGateway using wiremock's dynamic port
    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME,
            this.wireMockServer.port(),
            this.mockStationRepo);

    // Call store/update osd gateway operation
    List<UUID> storedOrUpdatedEventIds = osdGateway.storeOrUpdateEvents(events);

    // this.wireMockServer.verify that we sent the correct HTTP request to the wiremock server
    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STORE_EVENTS_PATH))
        .withRequestBody(equalTo(this.jsonObjectMapper.writeValueAsString(events))));

    // Assert the list of the successfully stored/updated events' UUIDs is empty
    Assertions.assertEquals(List.of(), storedOrUpdatedEventIds);
  }

  /**
   * Tests server returns 500 response
   */
  @Test
  void testStoreOrUpdateEvents500Response() throws IOException {

    List<Event> events = List.of(TestFixtures.associatedEvent);

    this.wireMockServer.stubFor(
        post(urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STORE_EVENTS_PATH))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(events)))
            .willReturn(WireMock.aResponse().withStatus(500)));

    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME,
            this.wireMockServer.port(),
            this.mockStationRepo);
    Assertions.assertThrows(RuntimeException.class, () -> osdGateway.storeOrUpdateEvents(events));

    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STORE_EVENTS_PATH))
        .withRequestBody(equalTo(
            this.jsonObjectMapper.writeValueAsString(events))));
  }

  /**
   * Tests server returns 409 conflict response
   */
  @Test
  void testStoreOrUpdateEvents409Conflict() throws IOException {
    List<Event> events = List.of(TestFixtures.associatedEvent);

    this.wireMockServer.stubFor(
        post(urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STORE_EVENTS_PATH))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(events)))
            .willReturn(WireMock.aResponse().withStatus(409)));

    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME,
            this.wireMockServer.port(),
            this.mockStationRepo);
    Assertions
        .assertThrows(IllegalArgumentException.class, () -> osdGateway.storeOrUpdateEvents(events));

    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STORE_EVENTS_PATH))
        .withRequestBody(equalTo(
            this.jsonObjectMapper.writeValueAsString(events))));
  }

  /**
   * Tests server returns 400 response
   */
  @Test
  void testStoreOrUpdateEvents400Response() throws IOException {
    List<Event> events = List.of(TestFixtures.associatedEvent);

    this.wireMockServer.stubFor(
        post(urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STORE_EVENTS_PATH))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(events)))
            .willReturn(WireMock.aResponse().withStatus(400)));

    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME,
            this.wireMockServer.port(),
            this.mockStationRepo);
    Assertions.assertThrows(RuntimeException.class, () -> osdGateway.storeOrUpdateEvents(events));

    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STORE_EVENTS_PATH))
        .withRequestBody(equalTo(
            this.jsonObjectMapper.writeValueAsString(events))));
  }


  @Test
  void testCreateNullHost() {
    Assertions.assertThrows(NullPointerException.class, () ->
        SignalDetectionAssociationControlOsdGateway.create(
            null,
            8080,
            this.mockStationRepo
        )
    );
  }

  @Test
  void testCreateNullPort() {
    Assertions.assertThrows(NullPointerException.class, () ->
        SignalDetectionAssociationControlOsdGateway.create(
            SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME,
            null,
            this.mockStationRepo
        )
    );
  }

  @Test
  void testRetrieveSignalDetectionHypothesesNullUuids() throws MalformedURLException {
    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME, 8080,
            this.mockStationRepo);
    Assertions.assertThrows(NullPointerException.class, () ->
        osdGateway.retrieveSignalDetectionHypotheses(null)
    );
  }

  @Test
  void teststoreOrUpdateEventsNullEvents() throws MalformedURLException {
    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME, 8080,
            this.mockStationRepo);
    Assertions.assertThrows(NullPointerException.class, () ->
        osdGateway.storeOrUpdateEvents(null)
    );
  }

  @Test
  void testretrieveStationsNullStationIds() throws MalformedURLException {
    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME, 8080,
            this.mockStationRepo);
    NullPointerException exception = Assertions.assertThrows(NullPointerException.class, () ->
        osdGateway.retrieveStations(null));
    Assertions.assertEquals("stationIds must be non-null", exception.getMessage());
  }

  @Test
  void testRetrieveStations() throws IOException, UnirestException {
    ReferenceStation mockReferenceStation = Mockito.mock(ReferenceStation.class);
    given(mockReferenceStation.getVersionId()).willReturn(TestFixtures.referenceStation.getVersionId());
    given(mockReferenceStation.getStationType()).willReturn(StationType.Seismic3Component);
    List<UUID> stationIds = List.of(TestFixtures.referenceStation.getVersionId());

    this.wireMockServer.stubFor(
        post(urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.STATIONS_RETRIEVAL_URL))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(stationIds)))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(
                        this.jsonObjectMapper
                            .writeValueAsString(List.of(TestFixtures.referenceStation)))));

    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME,
            this.wireMockServer.port(),
            this.mockStationRepo);

    try {
      given(mockStationRepo.retrieveStations()).willReturn(List.of(mockReferenceStation));
      Set<ReferenceStation> stations = osdGateway.retrieveStations(stationIds);
      Assertions.assertTrue(!stations.isEmpty());
    } catch (Exception e) {

    }
  }

  @ParameterizedTest
  @MethodSource("parameterProvider")
  void testNullParameterVariationsThrowNullPointerException(Instant startTime,
      Instant endTime) throws IOException {
    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME,
            this.wireMockServer.port(),
            this.mockStationRepo);

    Assertions.assertThrows(NullPointerException.class,
        () -> osdGateway.retrieveEventHypotheses(startTime, endTime));

  }

  @Test
  void testRetrieveEventHypotheses() throws IOException, UnirestException {
    Instant startTime = Instant.now();
    Instant endTime = Instant.now();
    Map<String, Object> testData = Map.of(
        "startTime", startTime,
        "endTime", endTime
    );
    List<Event> listOfEvents = List.of(TestFixtures.associatedEvent);
    this.wireMockServer.stubFor(
        post(urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.EVENTS_RETRIEVAL_URL))
            .withRequestBody(
                equalTo(this.jsonObjectMapper
                    .writeValueAsString(testData)))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(
                        this.jsonObjectMapper
                            .writeValueAsString(listOfEvents))));

    SignalDetectionAssociationControlOsdGateway osdGateway = SignalDetectionAssociationControlOsdGateway
        .create(SignalDetectionAssociationControlOsdGatewayTests.HOSTNAME,
            this.wireMockServer.port(),
            this.mockStationRepo);

    List<EventHypothesis> eventHypotheses = osdGateway.retrieveEventHypotheses(startTime, endTime);

    Assertions.assertTrue(eventHypotheses.size() == TestFixtures.associatedEvent.getHypotheses().size());
    Assertions.assertArrayEquals(eventHypotheses.toArray(), TestFixtures.associatedEvent.getHypotheses().toArray());
    this.wireMockServer.verify(postRequestedFor(
        urlEqualTo(SignalDetectionAssociationControlOsdGatewayTests.EVENTS_RETRIEVAL_URL))
        .withRequestBody(equalTo(
            this.jsonObjectMapper.writeValueAsString(testData))));
  }

  static Stream<Arguments> parameterProvider() {
    return Stream.of(
        Arguments.arguments(null, null),
        Arguments.arguments(null, Instant.now()),
        Arguments.arguments(Instant.now(), null)
    );
  }
}
