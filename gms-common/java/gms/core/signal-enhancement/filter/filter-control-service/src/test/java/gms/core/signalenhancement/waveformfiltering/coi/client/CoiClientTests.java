package gms.core.signalenhancement.waveformfiltering.coi.client;

import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import gms.core.signalenhancement.waveformfiltering.coi.CoiClient;
import gms.core.signalenhancement.waveformfiltering.coi.HttpClientConfiguration;
import gms.core.signalenhancement.waveformfiltering.http.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentStorageResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CoiClientTests {

  private WireMockServer wireMockServer;

  private static final String HOST = "localhost";
  private static final String BASE_WAVEFORMS_URL = "/mechanisms/object-storage-distribution/waveforms";
  private static final String BASE_CHANNELS_URL = "/coi";
  private static final String CHANNELS_QUERY_URL = BASE_CHANNELS_URL + "/channels/query/versionIds";

  private CoiClient coiClient;

  private ObjectMapper jsonMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private HttpClientConfiguration coiStationReferenceConfiguration;
  private HttpClientConfiguration coiWaveformConfiguration;

  @BeforeEach
  void setup() {
    wireMockServer = new WireMockServer(options().dynamicPort());
    wireMockServer.start();
    coiStationReferenceConfiguration = HttpClientConfiguration
        .create(HOST, wireMockServer.port(), BASE_CHANNELS_URL);
    coiWaveformConfiguration = HttpClientConfiguration
        .create(HOST, wireMockServer.port(), BASE_WAVEFORMS_URL);
    coiClient = CoiClient
        .create(coiStationReferenceConfiguration, coiWaveformConfiguration);
  }

  @AfterEach
  void teardown() {
    wireMockServer.stop();
    coiClient = null;
  }


  @Test
  void testCreate() {
    assertThrows(NullPointerException.class,
        () -> CoiClient.create(coiStationReferenceConfiguration, null));
    assertThrows(NullPointerException.class,
        () -> CoiClient.create(null, coiWaveformConfiguration));
  }

  @Test
  void testLoadChannelSegmentsNullArgumentsValidated()
      throws IllegalAccessException {
    final Instant startTime = Instant.EPOCH;
    final Instant endTime = startTime.plus(Duration.ofMillis(900));
    final UUID channelId = UUID.randomUUID();
    final ChannelSegmentDescriptor descriptor = ChannelSegmentDescriptor
        .from(channelId, startTime, endTime);
    final byte[] requestBody = ObjectSerialization.writeJson(Map.of(
        "channel-ids", List.of(descriptor.getChannelId()),
        "start-time", descriptor.getStartTime(),
        "end-time", descriptor.getEndTime(),
        "with-waveforms", true));

    final ChannelSegment<Waveform> out = createMockChannelSegment(channelId, startTime);

    // Post json requestBody to the /channel-segment endpoint; return messagepack ChannelSegments
    wireMockServer.stubFor(post(urlEqualTo(BASE_WAVEFORMS_URL + "/channel-segment"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(ok()
            .withHeader("Content-Type", "application/msgpack")
            .withBody(ObjectSerialization.writeMessagePack(Map.of(out.getChannelId(), out)))));

    TestUtilities.checkMethodValidatesNullArguments(coiClient, "getWaveforms",
        descriptor);
  }

  @Test
  void testLoadChannelSegmentsOutOfOrderTimesThrowsIllegalArgumentException() {
    Instant startTime = Instant.now();
    Instant endTime = startTime.minus(Duration.ofMillis(10));

    ChannelSegmentDescriptor descriptor = ChannelSegmentDescriptor
        .from(UUID.randomUUID(), startTime, endTime);

    assertThrows(IllegalArgumentException.class, () -> coiClient.getWaveforms(descriptor));
  }

  @Test
  void testGetChannels() throws IOException {
    List<ReferenceChannel> expectedChannels = Stream.generate(this::createMockChannel)
        .limit(10).collect(toList());

    List<UUID> channelIds = Stream.generate(UUID::randomUUID)
        .limit(10).collect(toList());

    byte[] requestBody = jsonMapper.writeValueAsBytes(channelIds);

    wireMockServer.givenThat(post(urlEqualTo(CHANNELS_QUERY_URL))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(ok()
            .withHeader("Content-Type", "application/json")
            .withBody(jsonMapper.writeValueAsBytes(expectedChannels))));

    List<ReferenceChannel> actualChannels = coiClient.getChannels(channelIds);
    assertEquals(expectedChannels, actualChannels);
  }

  @Test
  void testGetChannelSegments() throws IOException {
    final Instant startTime = Instant.EPOCH;
    final Instant endTime = startTime.plus(Duration.ofMillis(900));
    final UUID channel1 = UUID.randomUUID();
    final ChannelSegmentDescriptor descriptor = ChannelSegmentDescriptor
        .from(channel1, startTime, endTime);
    final byte[] requestBody = jsonMapper.writeValueAsBytes(Map.of(
        "channel-ids", List.of(descriptor.getChannelId()),
        "start-time", descriptor.getStartTime(),
        "end-time", descriptor.getEndTime(),
        "with-waveforms", true));

    final ChannelSegment<Waveform> out = createMockChannelSegment(channel1, startTime);

    // Post json requestBody to the /channel-segment endpoint; return messagepack ChannelSegments
    wireMockServer.givenThat(post(urlEqualTo(BASE_WAVEFORMS_URL + "/channel-segment"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(ok()
            .withHeader("Content-Type", "application/msgpack")
            .withBody(ObjectSerialization.writeMessagePack(Map.of(out.getChannelId(), out)))));

    final ChannelSegment<Waveform> channelSegment = coiClient
        .getWaveforms(descriptor);

    // Make sure the client invokes the osd gateway service
    wireMockServer.verify(1, postRequestedFor(
        urlEqualTo(BASE_WAVEFORMS_URL + "/channel-segment"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("Accept", equalTo("application/msgpack"))
        .withRequestBody(binaryEqualTo(requestBody)));

    // Make sure the correct ChannelSegments come back
    assertNotNull(channelSegment);
    assertEquals(out, channelSegment);
  }

  private ReferenceChannel createMockChannel() {
    return ReferenceChannel
        .create(String.format("test%s", Math.random()), ChannelType.BROADBAND_VERTICAL,
            ChannelDataType.SEISMIC_ARRAY, "test", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 40.0,
            Instant.EPOCH, Instant.EPOCH, InformationSource.create("test", Instant.EPOCH, "test"),
            "test", RelativePosition.from(0.0, 0.0, 0.0), emptyList());
  }

  private ChannelSegment<Waveform> createMockChannelSegment(UUID channelIdA, Instant startTime) {

    final Waveform wf = Waveform
        .withValues(startTime, 2.0, new double[]{9, 8, 7, 6, 5, 4, 3, 2, 1});

    return ChannelSegment
        .create(channelIdA, "mockSegment", ChannelSegment.Type.ACQUIRED, Set.of(wf),
            CreationInfo.DEFAULT);
  }

  @Test
  void testStoreNullArgumentsValidated()
      throws IllegalAccessException, JsonProcessingException {
    wireMockServer.givenThat(post(urlEqualTo(BASE_WAVEFORMS_URL + "/channel-segment/store"))
        .withRequestBody(binaryEqualTo(jsonMapper.writeValueAsBytes(emptyList())))
        .willReturn(ok()
            .withHeader("Content-Type", "application/json")
            .withBody(
                ObjectSerialization.writeJson(ChannelSegmentStorageResponse.builder().build()))));

    TestUtilities.checkMethodValidatesNullArguments(coiClient, "storeChannelSegments",
        List.of());
  }
}
