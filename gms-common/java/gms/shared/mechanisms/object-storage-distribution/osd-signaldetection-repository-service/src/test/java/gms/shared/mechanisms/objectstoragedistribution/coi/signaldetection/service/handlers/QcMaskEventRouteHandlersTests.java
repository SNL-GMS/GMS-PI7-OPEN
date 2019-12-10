package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipse;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipsoid;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationUncertainty;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredEventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.EventRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ChannelProcessingGroupRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.SignalDetectionRepositoryService;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.testUtilities.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.testUtilities.TestUtilityMethods;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.testUtilities.UnirestTestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.StoreEventResponseDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.FkSpectraRepository;
import java.net.ServerSocket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

//TODO:  This class combines both the QC Mask routes as well as the FeatureMeasurement routes.
//TODO:  These had to be combined because we would get sporadic random test failures because of
//TODO:  port binding when we tried spinning up separate QC and FM mock servers.  So, they
//TODO:  were combined.  We should find a fix for this at some point....
public class QcMaskEventRouteHandlersTests {

  /**
   * Serializes and deserializes signal detection common objects
   */
  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private static final ObjectMapper messagePackMapper = CoiObjectMapperFactory
      .getMsgpackObjectMapper();
  ;
  private static final JavaType eventListJT = objectMapper.getTypeFactory()
      .constructCollectionType(List.class, Event.class);
  private static final String channelId = "ba8a2aa5-ae09-46ea-a15c-87f222980572";
  private static final String startTime = "2010-05-20T00:59:59.108Z";
  private static final String endTime = "2010-05-20T01:00:01.991Z";
  private static final Instant startTimeInstant = Instant.parse(startTime);
  private static final Instant endTimeInstant = Instant.parse(endTime);

  private static String queryEventIdsUrl;
  private static String queryEventTimeUrl;
  private static String storeEventUrl;
  private static final UUID
      EventID1 = UUID.fromString("ba8a2aa5-ae09-46ea-a15c-87f222980572");
  //EventID2 = UUID.fromString("af694ce7-474f-ced0-812e-96a135792468");
  private static final List<UUID> eventIds = new ArrayList<>(); // = List.of(EventID1, EventID2);

  private static Event eventFromId, eventFromTimeLocation, eventToBeUpdated, eventWrongOrg,
      eventRightOrg;
  private static List<Event> eventList;
  private Map<String, Object> timeAndLocationPostBody;

  private static String RETRIEVE_QC_MASK_URL;
  private static String STORE_QC_MASK_URL;

  private static ArgumentCaptor<QcMask> maskCaptor
      = ArgumentCaptor.forClass(QcMask.class);
  @Captor
  private ArgumentCaptor<Collection<Event>> newEventCaptor;

  @Captor
  private ArgumentCaptor<Collection<Event>> updatedEventCaptor;

  private static final QcMaskRepository mockQcMaskRepository =
      Mockito.mock(QcMaskRepository.class);
  private static final SignalDetectionRepository mockSignalDetectionRepository =
      Mockito.mock(SignalDetectionRepository.class);
  private static final FkSpectraRepository mockFkRepository = Mockito
      .mock(FkSpectraRepository.class);
  private static final ProcessingStationReferenceFactory mockStationReferenceFactory =
      Mockito.mock(ProcessingStationReferenceFactory.class);
  private static final EventRepository mockEventRepository = Mockito
      .mock(EventRepository.class);
  private static final ChannelProcessingGroupRepository mockChannelProcessingGroupRepository = Mockito
      .mock(ChannelProcessingGroupRepository.class);


  @Before
  public void setupObjectsAndMocks() {
    MockitoAnnotations.initMocks(this);
    //location/eventFromId creation
    final EventLocation location = EventLocation.from(0, 0, 0, Instant.EPOCH);
    final LocationRestraint locationRestraint = LocationRestraint.from(
        RestraintType.UNRESTRAINED,
        0.0,
        RestraintType.UNRESTRAINED,
        0.0,
        DepthRestraintType.UNRESTRAINED,
        0.0,
        RestraintType.UNRESTRAINED,
        Instant.EPOCH);
    final Ellipse ellipse = Ellipse
        .from(ScalingFactorType.CONFIDENCE, 0.0, 0.5, 0.0, 0.0,
            0.0, 0.0, 0.0, Duration.ofMinutes(1));
    final Ellipsoid ellipsoid = Ellipsoid
        .from(ScalingFactorType.CONFIDENCE, 0.0, 0.5, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, Duration.ofMinutes(1));
    final LocationUncertainty locationUncertainty = LocationUncertainty.from(0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Set.of(ellipse), Set.of(ellipsoid));
    final LocationBehavior locationBehavior = LocationBehavior.from(0.0, 0.0, true,
        UUID.randomUUID(), UUID.randomUUID());
    final LocationSolution locationSolution = LocationSolution
        .create(location, locationRestraint, locationUncertainty,
            Set.of(locationBehavior), Set.of());
    final PreferredLocationSolution preferredLocationSolution = PreferredLocationSolution
        .from(locationSolution);

    eventFromId = Event.create(Set.of(UUID.randomUUID()),
        Set.of(UUID.randomUUID()),
        Set.of(locationSolution),
        preferredLocationSolution,
        "theMonitoringOrg",
        UUID.randomUUID());

    eventRightOrg = Event.create(Set.of(UUID.randomUUID()),
        Set.of(UUID.randomUUID()),
        Set.of(locationSolution),
        preferredLocationSolution,
        "monitoringOrg 6",
        UUID.randomUUID());

    eventWrongOrg = Event.from(
        eventRightOrg.getId(),
        Set.of(),
        "monitoringOrg 6oops",
        eventRightOrg.getHypotheses(),
        List.of(),
        List.of(PreferredEventHypothesis
            .from(UUID.randomUUID(), eventRightOrg.getHypotheses().iterator().next()))
    );

    eventToBeUpdated = Event.create(Set.of(UUID.randomUUID()),
        Set.of(UUID.randomUUID()),
        Set.of(locationSolution),
        preferredLocationSolution,
        "theMonitoringOrg",
        UUID.randomUUID());

    eventFromTimeLocation = Event.create(Set.of(UUID.randomUUID()),
        Set.of(UUID.randomUUID()),
        Set.of(locationSolution),
        preferredLocationSolution,
        "IDC",
        UUID.randomUUID());

    eventList = List.of(eventFromId, eventToBeUpdated, eventWrongOrg);
    eventIds.add(eventFromId.getId());
    eventIds.add(eventToBeUpdated.getId());
    eventIds.add(eventWrongOrg.getId());

    reset(mockQcMaskRepository);
    reset(mockSignalDetectionRepository);
    reset(mockFkRepository);
    reset(mockStationReferenceFactory);
    reset(mockEventRepository);
    reset(mockChannelProcessingGroupRepository);

    given(mockQcMaskRepository
        .findCurrentByChannelIdAndTimeRange(
            UUID.fromString(channelId),
            Instant.parse(startTime),
            Instant.parse(endTime)))
        .willReturn(List.of(TestFixtures.qcMask));
    given(mockEventRepository.findEventsByIds(anyCollection()))
        .willAnswer(invocation -> {
          Collection<UUID> uuids = (Collection<UUID>) invocation.getArgument(0);
          if (eventIds.containsAll(uuids)) {
            return List.of(eventToBeUpdated, eventRightOrg);
          }
          return List.of();
        });
    given(mockEventRepository.findEventsByTimeAndLocation(startTimeInstant, endTimeInstant, -20, 20,
        -30, 30))
        .willReturn(List.of(eventFromTimeLocation));
    //For request bodies without the optional min/max lat/long params
    given(mockEventRepository.findEventsByTimeAndLocation(startTimeInstant, endTimeInstant, -90, 90,
        -180, 180))
        .willReturn(List.of(eventFromTimeLocation));

    doNothing().when(mockQcMaskRepository).store(maskCaptor.capture());
    //doNothing().when(mockEventRepository)
    doNothing().when(mockEventRepository).storeEvents(newEventCaptor.capture(),
        anyCollection());
    doAnswer(invocation -> {
      Collection<Event> updatedEvents = invocation.getArgument(0);
      Collection<Event> errorEventList = invocation.getArgument(1);
      if (updatedEvents.contains(eventWrongOrg)) {
        errorEventList.add(eventWrongOrg);
      }
      return null;
    })
        .when(mockEventRepository).updateEvents(updatedEventCaptor.capture(),
        ArgumentMatchers.eq(new ArrayList<>()));
  }

  @BeforeClass
  public static void setup() throws Exception {
    final int servicePort = getAvailablePort();
    Configuration config = Configuration.builder().setPort(servicePort).build();
    STORE_QC_MASK_URL = "http://localhost:" + config.getPort() + "/coi/qc-masks";
    RETRIEVE_QC_MASK_URL = "http://localhost:" + servicePort + config.getBaseUrl() + "qc-mask";
    queryEventIdsUrl = "http://localhost:" + config.getPort() + "/coi/events/query/ids";
    queryEventTimeUrl = "http://localhost:" + config.getPort() + "/coi/events/query/time-lat-lon";
    storeEventUrl =
        "http://localhost:" + config.getPort() + "/coi/events";

    SignalDetectionRepositoryService
        .startService(config, mockQcMaskRepository, mockSignalDetectionRepository, mockFkRepository,
            mockChannelProcessingGroupRepository, mockEventRepository, mockStationReferenceFactory);
  }

  //Resets the maps we use to create the post body for the query by time and location tests, saves lines of code
  @Before
  public void restMaps() {
    timeAndLocationPostBody = new HashMap<>();
    timeAndLocationPostBody.put("startTime", startTime);
    timeAndLocationPostBody.put("endTime", endTime);
    timeAndLocationPostBody.put("minLatitude", -20);
    timeAndLocationPostBody.put("maxLatitude", 20);
    timeAndLocationPostBody.put("minLongitude", -30);
    timeAndLocationPostBody.put("maxLongitude", 30);
  }

  @AfterClass
  public static void teardown() {
    SignalDetectionRepositoryService.stopService();
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testNullParametersForQcMaskRead() throws Exception {
    HttpResponse<String> response = UnirestTestUtilities.getJson(RETRIEVE_QC_MASK_URL);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testNullParametersForQcMaskStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(null, STORE_QC_MASK_URL, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Tests that posting bad data request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testBadParameterForQcMaskStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(1, STORE_QC_MASK_URL, String.class);
    assertNotNull(response);
    assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST_400);
  }

  /**
   * Test QcMask endpoint, checking query parameters are parsed correctly. Mocking a call to the
   * QcMaskRepository to return a QcMask. Testing endpoint response matches expected status code,
   * and json body.
   */
  @Test
  public void testQueryParametersEndpointQcMask() throws Exception {
    String queryUrl = RETRIEVE_QC_MASK_URL + "?" +
        "channel-id=" + channelId + "&" +
        "start-time=" + startTime + "&" +
        "end-time=" + endTime;

    HttpResponse<String> response = UnirestTestUtilities.getJson(queryUrl);
    assertTrue(checkOKStatus(response, "json"));

    assertEquals(TestFixtures.qcMaskJson, response.getBody());
  }

  /**
   * Tests QcMask endpoint, checking endpoint with bad channel-id. Expected a response code of
   * HttpStatus.BAD_REQUEST_400
   */
  @Test
  public void testBadChannelIdQueryParameterQcMaskEndpoint() throws Exception {
    String qcMaskWithQueryParameters = RETRIEVE_QC_MASK_URL + "?" +
        "channel-id=ba@8a2aa5-ae09-46ea-a15c-87f222980572" + "&" +
        "start-time=" + startTime + "&" +
        "end-time=" + endTime;

    HttpResponse<String> response = UnirestTestUtilities.getJson(qcMaskWithQueryParameters);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  /**
   * Tests QcMask endpoint, checking endpoint with bad start time. Expected a response code of
   * HttpStatus.BAD_REQUEST_400
   */
  @Test
  public void testBadStartTimeQueryParameterQcMaskEndpoint() throws Exception {
    String qcMaskWithQueryParameters = RETRIEVE_QC_MASK_URL + "?" +
        "channel-id=" + channelId + "&" +
        "start-time=2010-05@-20T00:59:59.108Z" + "&" +
        "end-time=" + endTime;

    HttpResponse<String> response = UnirestTestUtilities.getJson(qcMaskWithQueryParameters);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  /**
   * Tests QcMask endpoint, checking endpoint with bad end time. Expected a response code of
   * HttpStatus.BAD_REQUEST_400
   */
  @Test
  public void testBadEndTimeQueryParameterQcMaskEndpoint() throws Exception {
    String qcMaskWithQueryParameters = RETRIEVE_QC_MASK_URL + "?" +
        "channel-id=" + channelId + "&" +
        "start-time=" + startTime + "&" +
        "end-time=2010-05-2@0T01:00:01.991Z";

    HttpResponse<String> response = UnirestTestUtilities.getJson(qcMaskWithQueryParameters);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  /**
   * Tests retrieving Map<Channel UUID, List<QcMask>>. Expect a response code of HttpStatus.OK_200
   */
  @Test
  public void testQueryQcMasksByChannelIds() throws Exception {
    UUID chanId = TestFixtures.PROCESSING_CHANNEL_1_ID;
    given(mockQcMaskRepository
        .findCurrentByChannelIdAndTimeRange(chanId, TestFixtures.startTime,
            TestFixtures.endTime))
        .willReturn(TestFixtures.qcMaskList);

    Map<String, Object> postBody = Map.of(
        "channel-ids", List.of(chanId),
        "start-time", startTime,
        "end-time", endTime);

    // test json request
    HttpResponse<String> response = TestUtilityMethods
        .postResponseFromEndPoint(postBody, RETRIEVE_QC_MASK_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    String expectedJson = TestFixtures.objectMapper.writeValueAsString(Map.of(
        chanId, List.of(TestFixtures.qcMask)));
    assertEquals(response.getBody(), expectedJson);
    assertTrue(checkOKStatus(response, "json"));

    // test msgpack request
    response = TestUtilityMethods
        .postResponseFromEndPointMsgpack(postBody, RETRIEVE_QC_MASK_URL);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    byte[] expectedMsgpack = TestFixtures.msgPackMapper.writeValueAsBytes(Map.of(
        chanId, List.of(TestFixtures.qcMask)));
    assertArrayEquals(response.getRawBody().readAllBytes(), expectedMsgpack);
    assertTrue(checkOKStatus(response, "msgpack"));
  }

  /**
   * Tests storing each of the QcMasks included in the provided QcMask[]. Expect a response code of
   * HttpStatus.OK_200
   */
  @Test
  public void testStoreQcMasksJson() throws Exception {
    List<QcMask> postBody = List.of(TestFixtures.qcMask2);

    // test json request
    HttpResponse<String> responseJson = TestUtilityMethods
        .postResponseFromEndPoint(postBody, STORE_QC_MASK_URL);
    assertNotNull(responseJson);
    assertEquals(HttpStatus.OK_200, responseJson.getStatus());

    List<UUID> uuidsJson = new ArrayList<>();
    for (QcMask mask : postBody) {
      uuidsJson.add(mask.getId());
    }
    String expectedJson = TestFixtures.objectMapper.writeValueAsString(uuidsJson);
    assertEquals(responseJson.getBody(), expectedJson);
    assertTrue(checkOKStatus(responseJson, "json"));

    List<QcMask> storedMasksJson = maskCaptor.getAllValues();
    assertEquals(postBody, storedMasksJson);
  }

  @Test
  public void testStoreQcMasksMsgPack() throws Exception {
    List<QcMask> postBody = List.of(TestFixtures.qcMask2);
    HttpResponse<String> responseMsgPack = TestUtilityMethods
        .postResponseFromEndPointMsgpack(postBody, STORE_QC_MASK_URL);
    assertNotNull(responseMsgPack);
    assertEquals(HttpStatus.OK_200, responseMsgPack.getStatus());

    List<UUID> uuidsMsgPack = new ArrayList<>();
    for (QcMask mask : postBody) {
      uuidsMsgPack.add(mask.getId());
    }

    TypeReference<List<UUID>> tr = new TypeReference<>() {
    };
    List<UUID> uuidsMsgPackActual = TestFixtures.msgPackMapper
        .readValue(responseMsgPack.getRawBody(), tr);

    assertTrue(checkOKStatus(responseMsgPack, "msgpack"));
    assertEquals(uuidsMsgPack.size(), uuidsMsgPackActual.size());
    for (UUID uuid : uuidsMsgPack) {
      assertTrue(uuidsMsgPackActual.contains(uuid));
    }
  }

  /**
   * Tests providing an incomplete/bad URL to the service Expect a response code of
   * HttpStatus.BAD_REQUEST_400
   */
  @Test
  public void testRequestParamsForChannelSegmentQueryNoChannelId() throws Exception {
    String url = RETRIEVE_QC_MASK_URL +
        "?start-time=2010-05-20T00:59:59.108Z" +
        "&end-time=2010-05-20T01:00:01.991Z";

    HttpResponse<String> response = TestUtilityMethods.getResponseFromEndPoint(url);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testNullParametersForEventQuery() throws Exception {
    HttpResponse<String> response = UnirestTestUtilities
        .postJson(null, queryEventIdsUrl, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }


  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testNullParametersForEventQueryByTime() throws Exception {
    HttpResponse<String> response = UnirestTestUtilities
        .postJson(null, queryEventTimeUrl, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }


  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testNullParameterForEventStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(null, storeEventUrl, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  /**
   * Tests that posting bad data request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testBadParameterForEventStore() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(1, storeEventUrl, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
  }

  /**
   * Tests eventFromId query by ID posting/accepting JSON', should return 404.
   */
  @Test
  public void testQueryEventsByIdJSON404() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(List.of(UUID.randomUUID()), queryEventIdsUrl, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
  }

  /**
   * Tests eventFromId query by ID posting/accepting MessagePack, should return 404'.
   */
  @Test
  public void testQueryEventsByIdMsgPack404() throws Exception {
    HttpResponse<String> response = TestUtilityMethods
        .postResponseFromEndPointMsgpack(List.of(UUID.randomUUID()), queryEventIdsUrl);
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
  }

  /**
   * Tests eventFromId query by ID posting/accepting JSON'.
   */
  @Test
  public void testQueryEventsByIdJSON() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(eventIds, queryEventIdsUrl, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    Event[] eventArray = objectMapper.readValue(response.getBody(), Event[].class);
    assertEquals(2, eventArray.length);
    assertEquals(eventArray[0], eventToBeUpdated);
    assertTrue(checkOKStatus(response, "json"));
  }

  /**
   * Tests eventFromId query by ID posting/accepting MessagePack'.
   */
  @Test
  public void testQueryEventsByIdMsgPack() throws Exception {
    HttpResponse<String> response = TestUtilityMethods
        .postResponseFromEndPointMsgpack(eventIds, queryEventIdsUrl);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    Event[] eventArray = messagePackMapper
        .readValue(response.getRawBody().readAllBytes(), Event[].class);
    assertEquals(2, eventArray.length);
    assertEquals(eventArray[0], eventToBeUpdated);
    assertTrue(checkOKStatus(response, "msgpack"));
  }

  /**
   * Tests eventFromId query by time and location posting/accepting JSON.
   */
  @Test
  public void testQueryEventsByTimeAndLocationJSON() throws Exception {
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(timeAndLocationPostBody, queryEventTimeUrl, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    Event[] eventArray = objectMapper.readValue(response.getBody(), Event[].class);
    assertEquals(eventArray[0], eventFromTimeLocation);
    assertTrue(checkOKStatus(response, "json"));

    //Don't include optional parameters
    Map<String, Object> postBodyNoOptionals = Map.of(
        "startTime", startTime,
        "endTime", endTime);
    response = UnirestTestUtilities.postJson(postBodyNoOptionals, queryEventTimeUrl, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    Event[] eventArrayNoOptionals = objectMapper.readValue(response.getBody(), Event[].class);
    assertEquals(eventArrayNoOptionals[0], eventFromTimeLocation);
    assertTrue(checkOKStatus(response, "json"));
  }

  /**
   * Tests eventFromId query by time and location posting/accepting MessagePack.
   */
  @Test
  public void testQueryEventsByTimeAndLocationMsgPack() throws Exception {
    HttpResponse<String> response = TestUtilityMethods.
        postResponseFromEndPointMsgpack(timeAndLocationPostBody, queryEventTimeUrl);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    Event[] eventArray = messagePackMapper
        .readValue(response.getRawBody().readAllBytes(), Event[].class);
    assertEquals(eventArray[0], eventFromTimeLocation);
    assertTrue(checkOKStatus(response, "msgpack"));

    //Leave out optional params
    Map<String, Object> postBodyNoOptionals = Map.of(
        "startTime", startTime,
        "endTime", endTime);
    response = TestUtilityMethods.
        postResponseFromEndPointMsgpack(postBodyNoOptionals, queryEventTimeUrl);
    assertTrue(checkOKStatus(response, "msgpack"));
    Event[] eventArrayNoOptionals = messagePackMapper
        .readValue(response.getRawBody().readAllBytes(), Event[].class);
    assertEquals(eventArrayNoOptionals[0], eventFromTimeLocation);
  }

  /**
   * Tests event query by time and location posting/accepting JSON with options that shouldn't find
   * anything
   */
  @Test
  public void testQueryEventsByTimeAndLocationJSONNoResults() throws Exception {
    timeAndLocationPostBody.replace("startTime", startTimeInstant.plusSeconds(1).toString());
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(timeAndLocationPostBody, queryEventTimeUrl, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    Event[] eventArray = objectMapper.readValue(response.getBody(), Event[].class);
    assertEquals(0, eventArray.length);
  }

  /**
   * Tests event query by time and location posting/accepting MessagePack with options that
   * shouldn't find anything
   */
  @Test
  public void testQueryEventsByTimeAndLocationMsgPackNoResults() throws Exception {
    timeAndLocationPostBody.replace("startTime", startTimeInstant.plusSeconds(1).toString());
    HttpResponse<String> response =
        UnirestTestUtilities.postJson(timeAndLocationPostBody, queryEventTimeUrl, String.class);
    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getStatus());
    Event[] eventArray = objectMapper.readValue(response.getBody(), Event[].class);
    assertEquals(0, eventArray.length);
  }

  /**
   * Tests storing JSON
   */
  @Test
  public void testStoreAndUpdateEventsJSON() throws Exception {
    HttpResponse<String> responseJson = TestUtilityMethods
        .postResponseFromEndPoint(eventList, storeEventUrl);
    assertTrue(checkOKStatus(responseJson, "json"));

    //Check to see if the events passed to the store method are as expected
    Collection<Event> eventsPassedToStore = newEventCaptor.getValue();
    assertEquals(List.of(eventFromId), eventsPassedToStore);

    Collection<Event> eventsPassedToUpdate = updatedEventCaptor.getValue();
    assertEquals(List.of(eventToBeUpdated, eventWrongOrg), eventsPassedToUpdate);

    //Now check to see if we got returned the IDs we posted
    List<UUID> postedEventIds = eventList.stream().map(Event::getId).collect(Collectors.toList());
    StoreEventResponseDto response = objectMapper
        .readValue(responseJson.getBody(), StoreEventResponseDto.class);
    assertEquals(List.of(eventFromId.getId()), response.getStoredEvents());
    assertEquals(List.of(eventToBeUpdated.getId()), response.getUpdatedEvents());
    assertEquals(List.of(eventWrongOrg.getId()), response.getErrorEvents());
  }

  /**
   * Tests storing MessagePack
   */
  @Test
  public void testStoreEventsMsgPack() throws Exception {
    HttpResponse<String> responseMsgPack = TestUtilityMethods
        .postResponseFromEndPointMsgpack(eventList, storeEventUrl);
    assertTrue(checkOKStatus(responseMsgPack, "msgpack"));

    //Check to see if the events passed to the store method are as expected
    Collection<Event> eventsPassedToStore = newEventCaptor.getValue();
    assertEquals(List.of(eventFromId), eventsPassedToStore);

    Collection<Event> eventsPassedToUpdate = updatedEventCaptor.getValue();
    assertEquals(List.of(eventToBeUpdated, eventWrongOrg), eventsPassedToUpdate);

    //Now check to see if we got returned the IDs we posted
    List<UUID> postedEventIds = eventList.stream().map(Event::getId).collect(Collectors.toList());
    StoreEventResponseDto response = messagePackMapper
        .readValue(responseMsgPack.getRawBody().readAllBytes(), StoreEventResponseDto.class);
    assertEquals(List.of(eventFromId.getId()), response.getStoredEvents());
    assertEquals(List.of(eventToBeUpdated.getId()), response.getUpdatedEvents());
    assertEquals(List.of(eventWrongOrg.getId()), response.getErrorEvents());
  }

  private static int getAvailablePort() throws Exception {
    ServerSocket ephemeralSocket = new ServerSocket(0);
    final int port = ephemeralSocket.getLocalPort();
    ephemeralSocket.close();
    return port;
  }

  private static boolean checkOKStatus(HttpResponse<String> httpResponse, String contentType) {
    try {
      assertNotNull(httpResponse);
      assertEquals(HttpStatus.OK_200, httpResponse.getStatus());
      assertTrue(httpResponse.getHeaders().keySet().contains("Content-Type"));
      assertEquals(httpResponse.getHeaders().get("Content-Type").size(), 1);
      assertEquals(httpResponse.getHeaders().get("Content-Type").get(0),
          "application/" + contentType);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
