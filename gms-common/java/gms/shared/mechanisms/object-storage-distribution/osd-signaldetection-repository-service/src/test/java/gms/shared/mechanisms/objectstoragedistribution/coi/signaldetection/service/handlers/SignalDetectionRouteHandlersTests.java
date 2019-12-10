package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.when;
import static org.mockito.BDDMockito.willDoNothing;

import com.fasterxml.jackson.databind.JavaType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.testUtilities.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.StoreFkChannelSegmentsDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.FkSpectraRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import spark.Request;
import spark.Response;

@RunWith(MockitoJUnitRunner.class)
public class SignalDetectionRouteHandlersTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private SignalDetectionRouteHandlers handlers;

  @Mock
  private SignalDetectionRepository repository;

  @Mock
  private FkSpectraRepository fkRepository;

  @Mock
  private Request request;

  @Mock
  private Response response;

  @Captor
  private ArgumentCaptor<SignalDetection> sigDetArgumentCaptor;

  @Captor
  private ArgumentCaptor<BeamCreationInfo> beamCreationInfoArgumentCaptor;

  @Captor
  private ArgumentCaptor<StoreFkChannelSegmentsDto> fkArgumentCaptor;

  private static JavaType sigDetsListType;
  private static JavaType mapUuidToSigDetsJavaType;

  @BeforeClass
  public static void setupHandlers() {
    JavaType uuidType = TestFixtures.objectMapper.constructType(UUID.class);
    sigDetsListType = TestFixtures.objectMapper.getTypeFactory().constructCollectionType(
        List.class, SignalDetection.class);
    mapUuidToSigDetsJavaType = TestFixtures.objectMapper.getTypeFactory().constructMapType(
        HashMap.class, uuidType, sigDetsListType);
  }

  @Before
  public void setUp() {
    handlers = SignalDetectionRouteHandlers.create(repository, fkRepository);
  }

  @Test
  public void testCreateNullParameterThrowsNullPointerException() {
    exception.expect(NullPointerException.class);
    SignalDetectionRouteHandlers.create(null, null);
  }

  @Test
  public void testGetSignalDetectionValidatesNullValues() throws IllegalAccessException {
    TestUtilities.checkMethodValidatesNullArguments(handlers, "getSignalDetection",
        request, response);
  }

  @Test
  public void testGetSignalDetectionBadAcceptReturnsError406() {
    given(request.params(":id"))
        .willReturn(UUID.randomUUID().toString());

    String responseBody = handlers.getSignalDetection(request, response);
    assertThat(responseBody, containsString("406"));

    verify(response, times(1)).status(406);
  }

  @Test
  public void testGetSignalDetectionNoIdParameterReturnsAllSignalDetections() {
    UUID testId = UUID.randomUUID();
    List<SignalDetection> expected = List.of(randomSignalDetection(testId));

    given(request.headers("Accept")).willReturn("application/json");
    given(request.params(":id"))
        .willReturn(null);
    given(repository.retrieveAll()).willReturn(expected);

    String actual = handlers.getSignalDetection(request, response);

    assertThat(actual,
        is(both(notNullValue()).and(equalTo(ObjectSerialization.writeValue(expected)))));

    verify(repository, times(1)).retrieveAll();
    verify(repository, never()).findSignalDetectionById(any());
  }

  @Test
  public void testGetSignalDetectionWithIdNotFoundReturnsNullString() {
    given(request.headers("Accept")).willReturn("application/json");
    given(request.params(":id"))
        .willReturn(UUID.randomUUID().toString());
    given(repository.findSignalDetectionById(any()))
        .willReturn(Optional.empty());

    String actual = handlers.getSignalDetection(request, response);

    //Jackson serialization converts an empty optional to "null"
    assertThat(actual, is(both(notNullValue()).and(equalTo("null"))));
  }

  @Test
  public void testGetSignalDetectionWithIdReturnsSingleSignalDetection() {
    UUID testId = UUID.randomUUID();
    SignalDetection expected = randomSignalDetection(testId);

    given(repository.findSignalDetectionById(testId))
        .willReturn(Optional.of(expected));

    given(request.headers("Accept")).willReturn("application/json");

    given(request.params(":id"))
        .willReturn(testId.toString());

    String actual = handlers.getSignalDetection(request, response);

    assertThat(actual,
        is(both(notNullValue()).and(equalTo(ObjectSerialization.writeValue(expected)))));

    verify(repository, times(1)).findSignalDetectionById(testId);
    verify(repository, never()).retrieveAll();
  }

  @Test
  public void testGetSignalDetectionsWithIdsJson() throws Exception {
    UUID testId = UUID.randomUUID();
    SignalDetection signalDetection = randomSignalDetection(testId);

    List<UUID> ids = List.of(testId);
    List<SignalDetection> expected = List.of(signalDetection);

    given(repository.findSignalDetectionsByIds(ids))
        .willReturn(expected);

    given(request.headers("Accept")).willReturn("application/json");

    Map<String, Object> params = Map.ofEntries(
        Map.entry("ids", ids)
    );
    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(params));

    Object actual = handlers.getSignalDetectionsByIds(request, response);

    assertThat(actual,
        is(both(notNullValue()).and(equalTo(ObjectSerialization.writeValue(expected)))));

    verify(repository, times(1)).findSignalDetectionsByIds(ids);
    verify(repository, never()).retrieveAll();
  }

  @Test
  public void testGetSignalDetectionsWithIdsMessagePack() throws Exception {
    UUID testId = UUID.randomUUID();
    SignalDetection signalDetection = randomSignalDetection(testId);

    List<UUID> ids = List.of(testId);
    List<SignalDetection> expected = List.of(signalDetection);

    given(request.headers("Accept")).willReturn("application/msgpack");
    given(request.contentType()).willReturn("application/msgpack");

    Map<String, Object> params = Map.ofEntries(
        Map.entry("ids", ids)
    );
    given(request.bodyAsBytes()).willReturn(TestFixtures.msgPackMapper.writeValueAsBytes(params));

    // mock the repository to return results
    given(repository
        .findSignalDetectionsByIds(ids))
        .willReturn(expected);

    byte[] body = (byte[]) handlers.getSignalDetectionsByIds(request, response);

    List<SignalDetection> retrievedSignalDetections = TestFixtures.msgPackMapper
        .readValue(body, TestFixtures.listSigDet);

    assertEquals(expected, retrievedSignalDetections);
  }

  @Test
  public void testGetSignalDetectionHypothesesWithIds() throws Exception {
    final SignalDetection signalDetection = randomSignalDetection(UUID.randomUUID());
    final SignalDetection signalDetection2 = randomSignalDetection(UUID.randomUUID());
    final List<SignalDetectionHypothesis> allHyps = List.of(signalDetection, signalDetection2).stream()
        .map(SignalDetection::getSignalDetectionHypotheses)
        .flatMap(List::stream)
        .collect(Collectors.toList());
    final List<UUID> hypIds = allHyps.stream()
        .map(SignalDetectionHypothesis::getId).collect(Collectors.toList());

    given(repository.findSignalDetectionHypothesesByIds(hypIds))
        .willReturn(allHyps);

    given(request.headers("Accept")).willReturn("application/json");

    final Map<String, Object> params = Map.of("ids", hypIds);
    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(params));

    final Object actual = handlers.getSignalDetectionsHypothesesByIds(request, response);

    assertThat(actual,
        is(both(notNullValue()).and(equalTo(ObjectSerialization.writeValue(allHyps)))));

    verify(repository, times(1)).findSignalDetectionHypothesesByIds(hypIds);
    verify(repository, never()).retrieveAll();
  }

  /**
   * Tests storing each of the BeamCreationInfo included in the provided BeamCreationInfos[]. Expect
   * a response code of HttpStatus.OK_200
   */
  @Test
  public void testStoreBeamCreationInfoFromArray() throws Exception {
    List<BeamCreationInfo> beamCreationInfos = List.of(TestFixtures.beamCreationInfo);
    doNothing().when(repository).store(beamCreationInfoArgumentCaptor.capture());
    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(
        List.of(TestFixtures.beamCreationInfo)));
    when(response.status()).thenReturn(HttpStatus.OK_200);
    String serviceResponse = handlers.storeBeamCreationInfoFromArray(
        request, response);
    assertEquals("", serviceResponse);
    assertEquals(beamCreationInfos, beamCreationInfoArgumentCaptor.getAllValues());
    assertEquals(HttpStatus.OK_200, response.status());
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testNullParametersForStoreBeamCreationInfoFromArrayStore() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot store null beam creation infos");
    given(request.body()).willReturn(null);
    handlers.storeBeamCreationInfoFromArray(
        request, response);
  }

  /**
   * Tests storing each of the SignalDetections included in the provided SignalDetections[]. Expect
   * a response code of HttpStatus.OK_200 Provides and input body of json bytes and expects back the
   * same type
   */
  @Test
  public void testStoreSignalDetectionsAcceptJsonContentJson() throws Exception {
    List<SignalDetection> detections = List
        .of(TestFixtures.signalDetection, TestFixtures.signalDetection2);
    List<UUID> detectionIds = List.of(TestFixtures.signalDetection.getId(),
        TestFixtures.signalDetection2.getId());

    willDoNothing().given(repository).store(sigDetArgumentCaptor.capture());
    given(request.headers("Content-Type")).willReturn("application/json");
    given(request.headers("Accept")).willReturn("application/json");
    given(request.bodyAsBytes())
        .willReturn(TestFixtures.objectMapper.writeValueAsBytes(detections));

    Object serviceResponse = handlers.storeSignalDetections(
        request, response);

    then(response)
        .should()
        .status(200);

    then(response)
        .should()
        .type("application/json");

    assertEquals(detections, sigDetArgumentCaptor.getAllValues());
    assertThat(serviceResponse, instanceOf(byte[].class));
    UUID[] responseIds = TestFixtures.objectMapper
        .readValue((byte[]) serviceResponse, UUID[].class);
    assertEquals(detectionIds, Arrays.asList(responseIds));
  }

  /**
   * Tests storing each of the SignalDetections included in the provided SignalDetections[]. Expect
   * a response code of HttpStatus.OK_200 Provides and input body of msgpack bytes and expects back
   * the same type
   */
  @Test
  public void testStoreSignalDetectionsAcceptMsgPackContentMsgPack() throws Exception {
    List<SignalDetection> detections = List
        .of(TestFixtures.signalDetection, TestFixtures.signalDetection2);
    List<UUID> detectionIds = List.of(TestFixtures.signalDetection.getId(),
        TestFixtures.signalDetection2.getId());

    willDoNothing().given(repository).store(sigDetArgumentCaptor.capture());
    given(request.headers("Content-Type")).willReturn("application/msgpack");
    given(request.headers("Accept")).willReturn("application/msgpack");
    given(request.bodyAsBytes())
        .willReturn(TestFixtures.msgPackMapper.writeValueAsBytes(detections));

    Object serviceResponse = handlers.storeSignalDetections(
        request, response);

    then(response)
        .should()
        .status(200);

    then(response)
        .should()
        .type("application/msgpack");

    assertEquals(detections, sigDetArgumentCaptor.getAllValues());
    assertThat(serviceResponse, instanceOf(byte[].class));
    UUID[] responseIds = TestFixtures.msgPackMapper
        .readValue((byte[]) serviceResponse, UUID[].class);
    assertEquals(detectionIds, Arrays.asList(responseIds));
  }

  /**
   * Tests that posting a null request to all specified endpoints, which should return a 'bad
   * request'.
   */
  @Test
  public void testStoreSignalDetectionsNullBodyThrowsNullPointerException() throws Exception {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot store null signal detections");

    given(request.bodyAsBytes()).willReturn(null);
    handlers.storeSignalDetections(request, response);
  }

  @Test
  public void testFindSignalDetectionsByTimeRange() throws Exception {
    List<SignalDetection> expectedResponse = List.of(
        TestFixtures.signalDetection, TestFixtures.signalDetection2);
    // mock the repository to return results
    given(repository
        .findSignalDetections(TestFixtures.startTime, TestFixtures.endTime))
        .willReturn(expectedResponse);
    // mock query params
    given(request.queryParams("start-time")).willReturn(TestFixtures.startTime.toString());
    given(request.queryParams("end-time")).willReturn(TestFixtures.endTime.toString());
    // JSON test
    given(request.headers("Accept")).willReturn(""); // blank defaults to JSON
    Object serviceResponse = handlers.getSignalDetectionsByTimeRange(
        request, response);
    assertNotNull(serviceResponse);
    List<SignalDetection> responseDets = TestFixtures.objectMapper.readValue(
        serviceResponse.toString(), sigDetsListType);
    assertEquals(expectedResponse, responseDets);
    // msgpack test
    given(request.headers("Accept")).willReturn("application/msgpack");
    serviceResponse = handlers.getSignalDetectionsByTimeRange(
        request, response);
    assertNotNull(serviceResponse);
    responseDets = TestFixtures.msgPackMapper.readValue(
        (byte[]) serviceResponse, sigDetsListType);
    assertEquals(expectedResponse, responseDets);
  }

  @Test
  public void testFindSignalDetectionsByStationAndTimeRangeMessagePack() throws Exception {
    final UUID stationId = TestFixtures.signalDetection.getStationId(),
        stationId2 = TestFixtures.signalDetection2.getStationId();
    final List<UUID> stationIds = List.of(stationId, stationId2);
    Map<String, Object> params = Map.ofEntries(
        Map.entry("stationIds", stationIds),
        Map.entry("startTime", TestFixtures.startTime),
        Map.entry("endTime", TestFixtures.endTime)
    );

    given(request.headers("Accept")).willReturn("application/msgpack");
    given(request.contentType()).willReturn("application/msgpack");

    given(request.bodyAsBytes()).willReturn(TestFixtures.msgPackMapper.writeValueAsBytes(params));

    // mock the repository to return results
    Map<UUID, List<SignalDetection>> expectedResponse = Map.of(
        stationId, List.of(TestFixtures.signalDetection),
        stationId2, List.of(TestFixtures.signalDetection2));
    given(repository
        .findSignalDetectionsByStationIds(stationIds, TestFixtures.startTime, TestFixtures.endTime))
        .willReturn(expectedResponse);

    byte[] body = (byte[]) handlers.getSignalDetectionsByStationAndTimeRange(request, response);

    Map<UUID, List<SignalDetection>> retrievedSignalDetections = TestFixtures.msgPackMapper
        .readValue(body, TestFixtures.mapUuidListSigDet);

    assertEquals(expectedResponse, retrievedSignalDetections);
  }

  @Test
  public void testGetSignalDetectionsByStationAndTimeRange() throws Exception {
    final UUID stationId = TestFixtures.signalDetection.getStationId(),
        stationId2 = TestFixtures.signalDetection2.getStationId();
    final List<UUID> stationIds = List.of(stationId, stationId2);
    Map<UUID, List<SignalDetection>> expectedResponse = Map.of(
        stationId, List.of(TestFixtures.signalDetection),
        stationId2, List.of(TestFixtures.signalDetection2));
    // mock the repository to return results
    given(repository
        .findSignalDetectionsByStationIds(stationIds, TestFixtures.startTime, TestFixtures.endTime))
        .willReturn(expectedResponse);
    // mock the request
    given(request.body())
        .willReturn(TestFixtures.objectMapper.writeValueAsString(
            Map.of(
                "stationIds", stationIds,
                "startTime", TestFixtures.startTime,
                "endTime", TestFixtures.endTime)));
    // test get JSON response
    given(request.headers("Accept")).willReturn(""); // blank defaults to JSON
    Object serviceResponse = handlers.getSignalDetectionsByStationAndTimeRange(
        request, response);
    assertNotNull(serviceResponse);
    Map<UUID, List<SignalDetection>> responseMap = TestFixtures.objectMapper.readValue(
        serviceResponse.toString(), mapUuidToSigDetsJavaType);
    assertEquals(expectedResponse, responseMap);
    // test get msgpack response
    given(request.headers("Accept")).willReturn("application/msgpack");
    serviceResponse = handlers.getSignalDetectionsByStationAndTimeRange(
        request, response);
    assertNotNull(serviceResponse);
    responseMap = TestFixtures.msgPackMapper.readValue(
        (byte[]) serviceResponse, mapUuidToSigDetsJavaType);
    assertEquals(expectedResponse, responseMap);
  }

  @Test
  public void testRetrieveFkChannelSegmentsRequestMessagePackResponseMessagePack()
      throws Exception {
    UUID channelId = TestFixtures.fkChannelID;
    final Instant startTime = Instant.now().minusSeconds(5);
    final Instant endTime = Instant.now();
    final boolean includeSpectrum = true;

    Optional<ChannelSegment<FkSpectra>> fkSpectras = Optional
        .of(TestFixtures.BuildUniqueFkInput(startTime));

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelIds", List.of(channelId)),
        Map.entry("startTime", startTime),
        Map.entry("endTime", endTime),
        Map.entry("includeSpectrum", includeSpectrum)
    );

    given(request.headers("Accept")).willReturn("application/msgpack");
    given(request.contentType()).willReturn("application/msgpack");

    given(request.bodyAsBytes()).willReturn(TestFixtures.msgPackMapper.writeValueAsBytes(params));

    given(fkRepository.retrieveFkSpectraByTime(channelId, startTime, endTime, true))
        .willReturn(fkSpectras);

    byte[] body = (byte[]) handlers.retrieveFkChannelSegments(request, response);

    List<ChannelSegment<FkSpectra>> retrievedFkSpectras = TestFixtures.msgPackMapper
        .readValue(body, TestFixtures.listChanSegFk);

    assertEquals(List.of(fkSpectras.get()), retrievedFkSpectras);
  }

  @Test
  public void testRetrieveFkChannelSegmentsRequestMessagePackResponseJson() throws Exception {
    UUID channelId = TestFixtures.fkChannelID;
    final Instant startTime = Instant.now().minusSeconds(5);
    final Instant endTime = Instant.now();

    Optional<ChannelSegment<FkSpectra>> fkSpectras = Optional
        .of(TestFixtures.BuildUniqueFkInput(startTime));

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelIds", List.of(channelId)),
        Map.entry("startTime", startTime),
        Map.entry("endTime", endTime)
    );

    given(request.contentType()).willReturn("application/msgpack");

    given(request.bodyAsBytes()).willReturn(TestFixtures.msgPackMapper.writeValueAsBytes(params));
    given(fkRepository.retrieveFkSpectraByTime(channelId, startTime, endTime, true))
        .willReturn(fkSpectras);

    String body = handlers.retrieveFkChannelSegments(request, response).toString();

    List<ChannelSegment<FkSpectra>> retrievedFkSpectras = TestFixtures.objectMapper
        .readValue(body, TestFixtures.listChanSegFk);

    assertEquals(List.of(fkSpectras.get()), retrievedFkSpectras);
  }

  @Test
  public void testRetrieveFkChannelSegmentsRequestJsonResponseMessagePack() throws Exception {
    UUID channelId = TestFixtures.fkChannelID;
    final Instant startTime = Instant.now().minusSeconds(5);
    final Instant endTime = Instant.now();
    final boolean includeSpectrum = true;

    Optional<ChannelSegment<FkSpectra>> fkSpectras = Optional
        .of(TestFixtures.BuildUniqueFkInput(startTime));

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelIds", List.of(channelId)),
        Map.entry("startTime", startTime),
        Map.entry("endTime", endTime)
    );

    given(request.headers("Accept")).willReturn("application/msgpack");
    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(params));
    given(fkRepository.retrieveFkSpectraByTime(channelId, startTime, endTime, true))
        .willReturn(fkSpectras);

    byte[] body = (byte[]) handlers.retrieveFkChannelSegments(request, response);

    List<ChannelSegment<FkSpectra>> retrievedFkSpectras = TestFixtures.msgPackMapper
        .readValue(body, TestFixtures.listChanSegFk);

    assertEquals(List.of(fkSpectras.get()), retrievedFkSpectras);
  }

  @Test
  public void testRetrieveFkChannelSegmentsRequestJsonResponseJson() throws Exception {
    UUID channelId = TestFixtures.fkChannelID;
    final Instant startTime = Instant.now().minusSeconds(5);
    final Instant endTime = Instant.now();

    Optional<ChannelSegment<FkSpectra>> fkSpectras = Optional
        .of(TestFixtures.BuildUniqueFkInput(startTime));

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelIds", List.of(channelId)),
        Map.entry("startTime", startTime),
        Map.entry("endTime", endTime)
    );

    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(params));
    given(fkRepository.retrieveFkSpectraByTime(channelId, startTime, endTime, true))
        .willReturn(fkSpectras);

    String body = handlers.retrieveFkChannelSegments(request, response).toString();

    List<ChannelSegment<FkSpectra>> retrievedFkSpectras = TestFixtures.objectMapper
        .readValue(body, TestFixtures.listChanSegFk);

    assertEquals(List.of(fkSpectras.get()), retrievedFkSpectras);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRetrieveFkChannelSegmentsEndTimeBeforeStartTimeIllegalArgumentException()
      throws Exception {
    UUID channelId = TestFixtures.fkChannelID;
    final Instant endTime = Instant.now().minusSeconds(5);
    final Instant startTime = Instant.now();

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelIds", List.of(channelId)),
        Map.entry("startTime", startTime),
        Map.entry("endTime", endTime)
    );

    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(params));

    handlers.retrieveFkChannelSegments(request, response);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRetrieveFkChannelSegmentsMissingChannelIdParameterIllegalArgumentException()
      throws Exception {
    final Instant startTime = Instant.now().minusSeconds(5);
    final Instant endTime = Instant.now();

    Map<String, Object> params = Map.ofEntries(
        Map.entry("startTime", startTime),
        Map.entry("endTime", endTime)
    );

    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(params));

    handlers.retrieveFkChannelSegments(request, response);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRetrieveFkChannelSegmentsMissingStartTimeParameterIllegalArgumentException()
      throws Exception {
    UUID channelId = TestFixtures.fkChannelID;
    final Instant startTime = Instant.now().minusSeconds(5);
    final Instant endTime = Instant.now();

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelIds", List.of(channelId)),
        Map.entry("endTime", endTime)
    );

    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(params));

    handlers.retrieveFkChannelSegments(request, response);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRetrieveFkChannelSegmentsMissingEndTimeParameterIllegalArgumentException()
      throws Exception {
    UUID channelId = TestFixtures.fkChannelID;
    final Instant startTime = Instant.now().minusSeconds(5);
    final Instant endTime = Instant.now();

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelIds", List.of(channelId)),
        Map.entry("startTime", startTime)
    );

    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(params));

    handlers.retrieveFkChannelSegments(request, response);
  }

  @Test
  public void testRetrieveFkChannelSegmentByChannelSegmentIdsRequestMessagePackResponseMessagePack()
      throws Exception {
    final Instant startTime = Instant.parse("1996-01-02T03:04:04Z");

    ChannelSegment<FkSpectra> fkChannelSegment = TestFixtures
        .BuildUniqueFkInput(startTime);
    List<ChannelSegment<FkSpectra>> fkChannelSegments = List.of(fkChannelSegment);

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelSegmentIds", List.of(fkChannelSegment.getId())),
        Map.entry("withFkSpectra", true)
    );

    given(request.contentType()).willReturn("application/msgpack");
    given(request.headers("Accept")).willReturn("application/msgpack");
    given(request.bodyAsBytes()).willReturn(TestFixtures.msgPackMapper.writeValueAsBytes(params));
    given(fkRepository.retrieveFkChannelSegment(fkChannelSegment.getId(), true))
        .willReturn(Optional.of(fkChannelSegment));

    byte[] body = (byte[]) handlers.retrieveFkChannelSegmentsBySegmentIds(request, response);

    List<ChannelSegment<FkSpectra>> retrievedFkChannelSegments = TestFixtures.msgPackMapper
        .readValue(body, TestFixtures.listChanSegFk);

    assertEquals(fkChannelSegments, retrievedFkChannelSegments);
  }

  @Test
  public void testRetrieveFkChannelSegmentByChannelSegmentIdsRequestMessagePackResponseJson()
      throws Exception {
    final Instant startTime = Instant.parse("1996-01-02T03:04:04Z");

    ChannelSegment<FkSpectra> fkChannelSegment = TestFixtures
        .BuildUniqueFkInput(startTime);
    List<ChannelSegment<FkSpectra>> fkChannelSegments = List.of(fkChannelSegment);

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelSegmentIds", List.of(fkChannelSegment.getId())),
        Map.entry("withFkSpectra", true)
    );

    given(request.contentType()).willReturn("application/msgpack");
    given(request.bodyAsBytes()).willReturn(TestFixtures.msgPackMapper.writeValueAsBytes(params));
    given(fkRepository.retrieveFkChannelSegment(fkChannelSegment.getId(), true))
        .willReturn(Optional.of(fkChannelSegment));

    String body = handlers.retrieveFkChannelSegmentsBySegmentIds(request, response).toString();

    List<ChannelSegment<FkSpectra>> retrievedFkChannelSegments = TestFixtures.objectMapper
        .readValue(body, TestFixtures.listChanSegFk);

    assertEquals(fkChannelSegments, retrievedFkChannelSegments);
  }

  @Test
  public void testRetrieveFkChannelSegmentByChannelSegmentIdsRequestJsonResponseMessagePack()
      throws Exception {
    final Instant startTime = Instant.parse("1996-01-02T03:04:04Z");

    ChannelSegment<FkSpectra> fkChannelSegment = TestFixtures
        .BuildUniqueFkInput(startTime);
    List<ChannelSegment<FkSpectra>> fkChannelSegments = List.of(fkChannelSegment);

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelSegmentIds", List.of(fkChannelSegment.getId())),
        Map.entry("withFkSpectra", true)
    );

    given(request.headers("Accept")).willReturn("application/msgpack");
    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(params));
    given(fkRepository.retrieveFkChannelSegment(fkChannelSegment.getId(), true))
        .willReturn(Optional.of(fkChannelSegment));

    byte[] body = (byte[]) handlers.retrieveFkChannelSegmentsBySegmentIds(request, response);

    List<ChannelSegment<FkSpectra>> retrievedFkChannelSegments = TestFixtures.msgPackMapper
        .readValue(body, TestFixtures.listChanSegFk);

    assertEquals(fkChannelSegments, retrievedFkChannelSegments);
  }

  @Test
  public void testRetrieveFkChannelSegmentByChannelSegmentIdsRequestJsonResponseJson()
      throws Exception {
    final Instant startTime = Instant.parse("1996-01-02T03:04:04Z");

    ChannelSegment<FkSpectra> fkChannelSegment = TestFixtures
        .BuildUniqueFkInput(startTime);
    List<ChannelSegment<FkSpectra>> fkChannelSegments = List.of(fkChannelSegment);

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelSegmentIds", List.of(fkChannelSegment.getId())),
        Map.entry("withFkSpectra", true)
    );

    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(params));
    given(fkRepository.retrieveFkChannelSegment(fkChannelSegment.getId(), true))
        .willReturn(Optional.of(fkChannelSegment));
    String body = handlers.retrieveFkChannelSegmentsBySegmentIds(request, response).toString();

    List<ChannelSegment<FkSpectra>> retrievedFkChannelSegments = TestFixtures.objectMapper
        .readValue(body, TestFixtures.listChanSegFk);

    assertEquals(fkChannelSegments, retrievedFkChannelSegments);
  }

  @Test
  public void testRetrieveFkChannelSegmentByChannelSegmentIdsWithoutFkSpectra() throws Exception {
    final Instant startTime = Instant.parse("1996-01-02T03:04:04Z");

    ChannelSegment<FkSpectra> fkChannelSegment = TestFixtures
        .BuildUniqueFkInput(startTime);

    List<FkSpectra> timeseries = List.of(fkChannelSegment.getTimeseries().get(0)
        .toBuilder().withoutValues(fkChannelSegment.getTimeseries().get(0).getSampleCount())
        .build());

    ChannelSegment<FkSpectra> fkChannelSegmentWithoutSpectra = ChannelSegment.from(fkChannelSegment.getId(),
        fkChannelSegment.getChannelId(), fkChannelSegment.getName(), ChannelSegment.Type.FK_SPECTRA,
        timeseries, fkChannelSegment.getCreationInfo());

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelSegmentIds", List.of(fkChannelSegment.getId())),
        Map.entry("withFkSpectra", false)
    );

    System.out.println(fkChannelSegmentWithoutSpectra.getTimeseries());

    given(request.contentType()).willReturn("application/msgpack");
    given(request.headers("Accept")).willReturn("application/msgpack");
    given(request.bodyAsBytes()).willReturn(TestFixtures.msgPackMapper.writeValueAsBytes(params));
    given(fkRepository.retrieveFkChannelSegment(fkChannelSegment.getId(), (boolean) params.get("withFkSpectra")))
        .willReturn(Optional.of(fkChannelSegmentWithoutSpectra));

    byte[] body = (byte[]) handlers.retrieveFkChannelSegmentsBySegmentIds(request, response);

    assertArrayEquals(TestFixtures.msgPackMapper.writeValueAsBytes(List.of(fkChannelSegmentWithoutSpectra)), body);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRetrieveFkChannelSegmentByChannelSegmentIdsMissingChannelSegmentIdParameter()
      throws Exception {
    final Instant startTime = Instant.parse("1996-01-02T03:04:04Z");

    ChannelSegment<FkSpectra> fkChannelSegment = TestFixtures
        .BuildUniqueFkInput(startTime);

    Map<String, Object> params = Map.ofEntries(
        Map.entry("withFkSpectra", true)
    );

    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(params));

    handlers.retrieveFkChannelSegmentsBySegmentIds(request, response).toString();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRetrieveFkChannelSegmentByChannelSegmentIdsMissingWithFkSpectraParameter()
      throws Exception {
    final Instant startTime = Instant.parse("1996-01-02T03:04:04Z");

    ChannelSegment<FkSpectra> fkChannelSegment = TestFixtures
        .BuildUniqueFkInput(startTime);

    Map<String, Object> params = Map.ofEntries(
        Map.entry("channelSegmentIds", List.of(fkChannelSegment.getId()))
    );

    given(request.body()).willReturn(TestFixtures.objectMapper.writeValueAsString(params));

    handlers.retrieveFkChannelSegmentsBySegmentIds(request, response).toString();
  }

  @Test
  public void testStoreFkSpectraChannelSegmentsRequestMessagePackResponseMessagePack()
      throws Exception {
    final Instant startTime = Instant.parse("1996-01-02T03:04:04Z");

    StoreFkChannelSegmentsDto fkDto = new StoreFkChannelSegmentsDto(List.of(TestFixtures
        .BuildUniqueFkInput(startTime)));

    given(request.contentType()).willReturn("application/msgpack");
    given(request.bodyAsBytes()).willReturn(ObjectSerialization.writeMessagePack(fkDto));
    given(request.headers("Accept")).willReturn("application/msgpack");

    byte[] body = (byte[]) handlers.storeFkSpectra(request, response);

    List<UUID> storedChannelSegmentIds = TestFixtures.msgPackMapper
        .readValue(body, TestFixtures.listUuid);

    assertTrue(fkDto.getChannelSegments().stream().map(c -> c.getId()).collect(Collectors.toList())
        .equals(storedChannelSegmentIds));
  }

  @Test
  public void testStoreFkSpectraChannelSegmentsRequestMessagePackResponseJson() throws Exception {
    final Instant startTime = Instant.parse("1996-01-02T03:04:04Z");

    StoreFkChannelSegmentsDto fkDto = new StoreFkChannelSegmentsDto(List.of(TestFixtures
        .BuildUniqueFkInput(startTime)));

    given(request.contentType()).willReturn("application/msgpack");
    given(request.bodyAsBytes()).willReturn(ObjectSerialization.writeMessagePack(fkDto));

    String body = handlers.storeFkSpectra(request, response).toString();

    List<UUID> storedChannelSegmentIds = TestFixtures.objectMapper
        .readValue(body, TestFixtures.listUuid);

    assertTrue(fkDto.getChannelSegments().stream().map(c -> c.getId()).collect(Collectors.toList())
        .equals(storedChannelSegmentIds));
  }

  @Test
  public void testStoreFkSpectraChannelSegmentsRequestJsonResponseMessagePack() throws Exception {
    final Instant startTime = Instant.parse("1996-01-02T03:04:04Z");

    StoreFkChannelSegmentsDto fkDto = new StoreFkChannelSegmentsDto(List.of(TestFixtures
        .BuildUniqueFkInput(startTime)));

    String jsonBody = ObjectSerialization.writeValue(fkDto);
    given(request.body()).willReturn(jsonBody);
    given(request.headers("Accept")).willReturn("application/msgpack");

    byte[] body = (byte[]) handlers.storeFkSpectra(request, response);

    List<UUID> storedChannelSegmentIds = TestFixtures.msgPackMapper
        .readValue(body, TestFixtures.listUuid);

    assertTrue(fkDto.getChannelSegments().stream().map(c -> c.getId()).collect(Collectors.toList())
        .equals(storedChannelSegmentIds));
  }

  @Test
  public void testStoreFkSpectraChannelSegmentsRequestJsonResponseJson() throws Exception {
    final Instant startTime = Instant.parse("1996-01-02T03:04:04Z");

    StoreFkChannelSegmentsDto fkDto = new StoreFkChannelSegmentsDto(List.of(TestFixtures
        .BuildUniqueFkInput(startTime)));

    given(request.body()).willReturn(ObjectSerialization.writeValue(fkDto));

    String body = handlers.storeFkSpectra(request, response).toString();

    List<UUID> storedChannelSegmentIds = TestFixtures.objectMapper
        .readValue(body, TestFixtures.listUuid);

    assertTrue(fkDto.getChannelSegments().stream().map(c -> c.getId()).collect(Collectors.toList())
        .equals(storedChannelSegmentIds));
  }

  private static FeatureMeasurement<InstantValue> randomArrivalTimeMeasurement() {
    final Instant t = Instant.ofEpochMilli(1);
    return FeatureMeasurement.create(UUID.randomUUID(),
        FeatureMeasurementTypes.ARRIVAL_TIME,
        InstantValue.from(t, Duration.ofMillis(500)));
  }

  private static FeatureMeasurement<PhaseTypeMeasurementValue> randomPhaseMeasurement() {
    return FeatureMeasurement.create(UUID.randomUUID(), 
        FeatureMeasurementTypes.PHASE,
        PhaseTypeMeasurementValue.from(
            PhaseType.UNKNOWN, 1.0));
  }

  private static SignalDetection randomSignalDetection(UUID stationId) {
    return SignalDetection.create("monitoringOrganization", stationId,
        List.of(randomArrivalTimeMeasurement(), randomPhaseMeasurement()), UUID.randomUUID());
  }
}
