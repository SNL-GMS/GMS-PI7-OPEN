package gms.core.signalenhancement.beam.osd.client;

import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import gms.core.signalenhancement.beam.TestFixtures;
import gms.core.signalenhancement.beam.service.ContentType;
import gms.core.signalenhancement.beam.service.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroupType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class OsdClientTests {

  private WireMockServer wireMockServer;

  private static final String HOST = "localhost";
  private final String BASE_URL = "/signal-enhancement/beam/osd-gateway";
  private HttpClientConfiguration procGroupServiceConfig;
  private HttpClientConfiguration waveformServiceConfig;

  private OsdClient osdClient;

  //convenience function for null assertions
  private static Function<Executable, Executable> assertThrowsNullPointer =
      e -> () -> assertThrows(NullPointerException.class, e);

  @BeforeEach
  public void setUp() {
    wireMockServer = new WireMockServer(options().dynamicPort());
    wireMockServer.start();
    procGroupServiceConfig = HttpClientConfiguration.create(HOST,
        wireMockServer.port(), BASE_URL);
    waveformServiceConfig = HttpClientConfiguration.create(HOST,
        wireMockServer.port(), BASE_URL);
    osdClient = OsdClient.create(procGroupServiceConfig, waveformServiceConfig);
  }

  @AfterEach
  public void tearDown() {
    osdClient = null;
  }

  @Test
  void testCreate() {
    HttpClientConfiguration config1 = mock(HttpClientConfiguration.class);
    given(config1.getBasePath()).willReturn(BASE_URL);
    given(config1.getHost()).willReturn(HOST);
    given(config1.getPort()).willReturn(wireMockServer.port());
    HttpClientConfiguration config2 = mock(HttpClientConfiguration.class);
    given(config2.getBasePath()).willReturn(BASE_URL);
    given(config2.getHost()).willReturn(HOST);
    given(config2.getPort()).willReturn(wireMockServer.port());

    OsdClient client = OsdClient.create(config1, config2);
    assertNotNull(client);
  }

  private static Stream<Arguments> loadChannelSegmentsHandlerNullArguments() {
    return Stream.of(
        arguments(null, Instant.now(), Instant.now()),
        arguments(UUID.randomUUID(), null, Instant.now()),
        arguments(UUID.randomUUID(), Instant.now(), null)
    );
  }

  @Test
  void testCreateNullProcessingGroupServiceConfigExpectNullPointerException() {
    Executable nullChannelGroupServiceConfig = assertThrowsNullPointer
        .apply(() -> OsdClient.create(null, waveformServiceConfig));
    Executable nullWaveformServiceConfig = assertThrowsNullPointer
        .apply(() -> OsdClient.create(procGroupServiceConfig, null));

    assertAll("WaveformQcControl constructor null arguments:",
        nullChannelGroupServiceConfig, nullWaveformServiceConfig);
  }

  @ParameterizedTest
  @MethodSource("loadChannelSegmentsHandlerNullArguments")
  void testLoadChannelSegmentsNullArguments(UUID processingGroupId,
      Instant startTime,
      Instant endTime) {
    assertThrows(NullPointerException.class,
        () -> osdClient.loadChannelSegments(processingGroupId, startTime, endTime));
  }

  @Test
  void testLoadChannelSegmentsOutOfOrderTimesThrowsIllegalArgumentException() {
    Instant startTime = Instant.now();
    Instant endTime = startTime.minus(Duration.ofMillis(10));

    assertThrows(IllegalArgumentException.class,
        () -> osdClient.loadChannelSegments(UUID.randomUUID(), startTime, endTime));
  }

  @Test
  void testLoadChannelSegments() {
    final Instant startTime = Instant.EPOCH;
    final Instant endTime = startTime.plus(Duration.ofSeconds(10));
    final UUID chanId = UUID.randomUUID();
    final Set<UUID> channelIds = Set.of(chanId);
    final ChannelProcessingGroup procGroup = ChannelProcessingGroup.create(
        ChannelProcessingGroupType.BEAM,
        channelIds, Instant.now(), Instant.now(), "status", "comment");
    final byte[] requestBody = ObjectSerialization.writeJson(Map.of("channel-ids", channelIds,
        "start-time", startTime,
        "end-time", endTime,
        "with-waveforms", true));

    final ChannelSegment<Waveform> segment = createMockChannelSegment(chanId, startTime,
        endTime);

    // Post json requestBody to the /channel-segment endpoint; return messagepack ChannelSegments
    wireMockServer.givenThat(post(urlEqualTo(BASE_URL + "/channel-segment"))
        .withRequestBody(equalToJson(new String(requestBody)))
        .willReturn(ok()
            .withHeader("Content-Type", "application/msgpack")
            .withBody(ObjectSerialization.writeMessagePack(
                Map.of(chanId, segment)))));
    // mock endpoint response for loading ChannelProcessingGroup
    wireMockServer
        .givenThat(get(urlEqualTo(BASE_URL + "/channel-processing-group/" + procGroup.getId()))
            .willReturn(ok()
                .withBody(ObjectSerialization.writeJson(procGroup))));

    final Collection<ChannelSegment<Waveform>> actualChannelSegments = osdClient
        .loadChannelSegments(procGroup.getId(), startTime, endTime);

    // Make sure the client invokes the service
    wireMockServer.verify(1, postRequestedFor(
        urlEqualTo(BASE_URL + "/channel-segment"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("Accept", equalTo("application/msgpack"))
        .withRequestBody(binaryEqualTo(requestBody)));

    // Make sure the correct ChannelSegments come back
    assertEquals(1, actualChannelSegments.size());
    assertEquals(segment, actualChannelSegments.iterator().next());
  }

  @Test
  void testLoadChannelSegmentsWrongResponseTypeExpectIllegalStateException()
      throws JsonProcessingException {

    final Instant startTime = Instant.EPOCH;
    final Instant endTime = startTime.plus(Duration.ofMillis(900));
    ObjectMapper jsonMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    ObjectMapper msgPackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();
    UUID channelSegmentId = UUID.randomUUID();
    final String requestBody = jsonMapper
        .writeValueAsString(Map.of(
            "channel-ids", Set.of(channelSegmentId),
            "start-time", startTime,
            "end-time", endTime,
            "with-waveforms", true));
    final ChannelProcessingGroup procGroup = ChannelProcessingGroup.create(
        ChannelProcessingGroupType.BEAM,
        Set.of(channelSegmentId), Instant.now(), Instant.now(), "status", "comment");

    // Post json requestBody to the /invoke-input-data endpoint; return server error
    final ChannelSegment out1 = createMockChannelSegment(channelSegmentId, startTime, endTime);
    wireMockServer.givenThat(post(urlEqualTo(BASE_URL + "/channel-segment"))
        .withRequestBody(equalToJson(requestBody))
        .willReturn(ok()
            .withHeader("Content-Type", ContentType.TEXT_PLAIN.toString())
            .withBody(msgPackMapper.writeValueAsBytes(Map.of(out1.getChannelId(), out1)))));
    // mock endpoint response for loading ChannelProcessingGroup
    wireMockServer
        .givenThat(get(urlEqualTo(BASE_URL + "/channel-processing-group/" + procGroup.getId()))
            .willReturn(ok()
                .withBody(ObjectSerialization.writeJson(procGroup))));

    assertThrows(IllegalStateException.class,
        () -> osdClient.loadChannelSegments(procGroup.getId(), startTime, endTime));
  }

  @Test
  void testLoadChannelSegmentsFailureExpectIllegalStateException() {

    final Instant startTime = Instant.EPOCH;
    final Instant endTime = startTime.plus(Duration.ofMillis(900));
    Set<UUID> channelSegmentSet = Set.of(UUID.randomUUID());
    final byte[] requestBody = ObjectSerialization
        .writeJson(Map.of(
            "channel-ids", channelSegmentSet,
            "start-time", startTime,
            "end-time", endTime,
            "with-waveforms", true));
    final ChannelProcessingGroup procGroup = ChannelProcessingGroup.create(
        ChannelProcessingGroupType.BEAM,
        channelSegmentSet, Instant.now(), Instant.now(), "status", "comment");

    // Post json requestBody to the /invoke-input-data endpoint; return server error
    final String errorMsg = "beam osd client could not complete loadChannelSegments request";
    wireMockServer.givenThat(post(urlEqualTo(BASE_URL + "/channel-segment"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(serverError()
            .withHeader("Content-Type", ContentType.TEXT_PLAIN.toString())
            .withBody(errorMsg)));
    // mock endpoint response for loading ChannelProcessingGroup
    wireMockServer
        .givenThat(get(urlEqualTo(BASE_URL + "/channel-processing-group/" + procGroup.getId()))
            .willReturn(ok()
                .withBody(ObjectSerialization.writeJson(procGroup))));

    assertThrows(IllegalStateException.class,
        () -> osdClient.loadChannelSegments(procGroup.getId(), startTime, endTime));
  }

  private ChannelSegment<Waveform> createMockChannelSegment(UUID channelIdA, Instant startTime,
      Instant endTime) {

    final int durationSecs = (int) Duration.between(startTime, endTime).toSeconds();
    final Waveform wf1 = Waveform.withValues(startTime, 1, randoms(durationSecs + 1));

    return ChannelSegment
        .create(channelIdA, "mockSegment", ChannelSegment.Type.ACQUIRED, Set.of(wf1),
            CreationInfo.DEFAULT);
  }

  private static double[] randoms(int length) {
    double[] random = new double[length];
    IntStream.range(0, length).forEach(i -> random[i] = Math.random());
    return random;
  }

  @Test
  void testLoadDefaultBeamPluginConfiguration() {
    osdClient.loadPluginConfiguration(RegistrationInfo.from("BeamPlugin",
        PluginVersion.from(1, 0, 0)));
  }

  @Test
  void testLoadPluginConfigurationNullRegInfoExpectNullPointerException() {
    assertThrows(NullPointerException.class, () -> osdClient.loadPluginConfiguration(null));
  }

  @Test
  void testStore() {

    final List<ChannelSegment<Waveform>> waveforms = List
        .of(TestFixtures.waveformChannelSegment());

    final byte[] requestBody = ObjectSerialization.writeMessagePack(waveforms);

    // Post MessagePack requestBody to the /store endpoint; return OK - 200
    wireMockServer.givenThat(post(urlEqualTo(BASE_URL + "/channel-segment/store"))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(ok()));

    osdClient.store(waveforms);

    // Make sure the client invokes the osd service
    wireMockServer.verify(1, postRequestedFor(
        urlEqualTo(BASE_URL + "/channel-segment/store"))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withHeader("Accept", equalTo("application/json"))
        .withRequestBody(binaryEqualTo(requestBody)));
  }

  @Test
  void testStoreNullSignalDetectionsExpectNullPointerException() {
    assertThrows(NullPointerException.class, () -> osdClient.store(null));
  }
}
