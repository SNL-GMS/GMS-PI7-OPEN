package gms.core.signaldetection.signaldetectorcontrol.coi.client;

import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.mashape.unirest.http.Unirest;
import gms.core.signaldetection.signaldetectorcontrol.TestFixtures;
import gms.core.signaldetection.signaldetectorcontrol.http.ContentType;
import gms.core.signaldetection.signaldetectorcontrol.http.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CoiClientTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @ClassRule
  public static WireMockClassRule wireMockRule = new WireMockClassRule(
      wireMockConfig().dynamicPort());

  @Rule
  public WireMockClassRule instanceRule = wireMockRule;

  private static final String HOST = "localhost";
  private static final String BASE_CHANNELSEGMENTS_URL = "/coi/channel-segments";
  private static final String BASE_SIGNALDETECTIONS_URL = "/coi/signal-detections";

  private CoiClient gatewayClient;

  @Before
  public void setUp() {
    int port = instanceRule.port();

    gatewayClient = CoiClient.create(HttpClientConfiguration.create(HOST,
        port, BASE_CHANNELSEGMENTS_URL),
        HttpClientConfiguration.create(HOST, port, BASE_SIGNALDETECTIONS_URL));
    Unirest.setObjectMapper(ObjectSerialization.getJsonClientObjectMapper());
  }

  @After
  public void tearDown() {
    gatewayClient = null;
    Unirest.setObjectMapper(null);
  }

  @Test
  public void testCreate() {
    HttpClientConfiguration channelSegmentsConfig = mock(HttpClientConfiguration.class);
    HttpClientConfiguration signalDetectionsConfig = mock(HttpClientConfiguration.class);

    CoiClient client = CoiClient.create(channelSegmentsConfig, signalDetectionsConfig);
    assertNotNull(client);
  }

  @Test
  public void testCreateNullChannelSegmentsConfigExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create CoiClient with null channelSegmentsClientConfiguration");

    HttpClientConfiguration signalDetectionsConfig = mock(HttpClientConfiguration.class);
    CoiClient.create(null, signalDetectionsConfig);
  }

  @Test
  public void testCreateNullSignalDetectionsConfigExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage("Cannot create CoiClient with null signalDetectionsClientConfiguration");

    HttpClientConfiguration channelSegmentsConfig = mock(HttpClientConfiguration.class);
    CoiClient.create(channelSegmentsConfig, null);
  }

  @Test
  public void testGetChannelSegmentsNullChannelSegmentsExpectNullPointerException() {

    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot invoke getChannelSegments with null channelIds");
    gatewayClient.getChannelSegments(null, Instant.now(), Instant.now());
  }

  @Test
  public void testGetChannelSegmentsNullStartTimeExpectNullPointerException() {

    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot invoke getChannelSegments with null startTime");
    gatewayClient.getChannelSegments(List.of(), null, Instant.now());
  }

  @Test
  public void testGetChannelSegmentsNullEndTimeExpectNullPointerException() {

    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot invoke getChannelSegments with null endTime");
    gatewayClient.getChannelSegments(List.of(), Instant.now(), null);
  }

  @Test
  public void testGetChannelSegmentsOutOfOrderTimesThrowsIllegalArgumentException() {
    Instant startTime = Instant.now();
    Instant endTime = startTime.minus(Duration.ofMillis(10));

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot invoke getChannelSegments with endTime before startTime");
    gatewayClient.getChannelSegments(List.of(UUID.randomUUID()), startTime, endTime);
  }

  @Test
  public void testGetChannelSegments() {
    final Instant startTime = Instant.EPOCH;
    final UUID channel1 = UUID.randomUUID();
    final UUID channel2 = UUID.randomUUID();
    final Collection<UUID> channelUuids = List.of(channel1, channel2);
    final ChannelSegment out1 = TestFixtures.randomChannelSegment(channel1, startTime);
    final ChannelSegment out2 = TestFixtures.randomChannelSegment(channel2, startTime);
    final byte[] requestBody = ObjectSerialization
        .writeJson(Map.of("channel-ids", channelUuids, "start-time", startTime, "end-time",
            out1.getEndTime()));

    // Post json requestBody to the /invoke-input-data endpoint; return messagepack ChannelSegments
    givenThat(post(urlEqualTo(BASE_CHANNELSEGMENTS_URL))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("Accept", equalTo("application/msgpack"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(ok()
            .withHeader("Content-Type", "application/msgpack")
            .withBody(
                ObjectSerialization.writeMessagePack(Map.of(channel1, out1, channel2, out2)))));

    final Collection<ChannelSegment<Waveform>> actualChannelSegments = gatewayClient
        .getChannelSegments(channelUuids, startTime, out1.getEndTime());

    // Make sure the client invokes the osd gateway service
    verify(1, postRequestedFor(
        urlEqualTo(BASE_CHANNELSEGMENTS_URL))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("Accept", equalTo("application/msgpack"))
        .withRequestBody(binaryEqualTo(requestBody)));

    // Make sure the correct ChannelSegments come back
    assertEquals(2, actualChannelSegments.size());
    assertTrue(actualChannelSegments.containsAll(List.of(out1, out2)));
  }

  @Test
  public void testGetChannelSegmentsFailureExpectIllegalStateException() {
    final Instant startTime = Instant.EPOCH;
    final Instant endTime = startTime.plus(Duration.ofMillis(900));
    final UUID channel1 = UUID.randomUUID();
    final List<UUID> channelUuids = List.of(channel1);
    final byte[] requestBody = ObjectSerialization.writeJson(
        Map.of("channel-ids", channelUuids, "start-time", startTime, "end-time", endTime));

    // Post json requestBody to the /invoke-input-data endpoint; return server error
    final String errorMsg = "signal-detection-repository-service could not service channel " +
        "segments request";
    givenThat(post(urlEqualTo(BASE_CHANNELSEGMENTS_URL))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("Accept", equalTo("application/msgpack"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(serverError()
            .withHeader("Content-Type", ContentType.TEXT_PLAIN.toString())
            .withBody(errorMsg)));

    exception.expect(IllegalStateException.class);
    exception.expectMessage(errorMsg);
    gatewayClient.getChannelSegments(channelUuids, startTime, endTime);
  }

  @Test
  public void testStoreSignalDetections() {
    final SignalDetection signalDetection1 = TestFixtures.randomSignalDetection();
    final SignalDetection signalDetection2 = TestFixtures.randomSignalDetection();

    final UUID[] signalDetectionIds = new UUID[] {signalDetection1.getId(),
        signalDetection2.getId()};

    final List<SignalDetection> signalDetections = List.of(signalDetection1, signalDetection2);

    final byte[] requestBody = ObjectSerialization
        .writeMessagePack(signalDetections);

    // Post MessagePack requestBody to the /storeSignalDetections endpoint; return OK - 200
    givenThat(post(urlEqualTo(BASE_SIGNALDETECTIONS_URL))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withHeader("Accept", equalTo("application/json"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(ok()
            .withHeader("Content-Type", "application/json")
            .withBody(ObjectSerialization.writeJson(signalDetectionIds))));

    List<UUID> response = gatewayClient.storeSignalDetections(signalDetections);
    assertThat(response.size(), is(2));
    assertThat(response, hasItems(signalDetectionIds));

    // Make sure the client invokes the osd gateway service
    verify(1, postRequestedFor(
        urlEqualTo(BASE_SIGNALDETECTIONS_URL))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withHeader("Accept", equalTo("application/json"))
        .withRequestBody(binaryEqualTo(requestBody)));
  }

  @Test
  public void testStoreSignalDetectionsFailureThrowsException() {
    final List<SignalDetection> signalDetections = List.of(
        TestFixtures.randomSignalDetection(), TestFixtures.randomSignalDetection());

    final byte[] requestBody = ObjectSerialization
        .writeMessagePack(signalDetections);

    // Post MessagePack requestBody to the /storeSignalDetections endpoint; return OK - 200
    givenThat(post(urlEqualTo(BASE_SIGNALDETECTIONS_URL))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withHeader("Accept", equalTo("application/json"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(serverError()
            .withHeader("Content-Type", ContentType.TEXT_PLAIN.toString())
            .withBody("Could not storeSignalDetections SignalDetection"))
    );

    exception.expect(IllegalStateException.class);
    exception.expectMessage("Could not storeSignalDetections SignalDetection");
    gatewayClient.storeSignalDetections(signalDetections);
  }

  @Test
  public void testStoreSignalDetectionsNullSignalDetectionsExpectNullPointerException() {

    exception.expect(NullPointerException.class);
    exception.expectMessage("CoiClient storeSignalDetections requires non-null signalDetections");
    gatewayClient.storeSignalDetections(null);
  }

  @Test
  public void testStoreChannelSegments() {
    final ChannelSegment<Waveform> channelSegment1 = TestFixtures.randomChannelSegment();
    final ChannelSegment<Waveform> channelSegment2 = TestFixtures.randomChannelSegment();

    final UUID[] channelSegmentIds = new UUID[] {channelSegment1.getId(),
        channelSegment2.getId()};

    final List<ChannelSegment<Waveform>> channelSegments = List
        .of(channelSegment1, channelSegment2);

    final byte[] requestBody = ObjectSerialization
        .writeMessagePack(channelSegments);

    // Post MessagePack requestBody to the /storeChannelSegments endpoint; return OK - 200
    givenThat(post(urlEqualTo(BASE_CHANNELSEGMENTS_URL + "/store"))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withHeader("Accept", equalTo("application/json"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(ok()
            .withHeader("Content-Type", "application/json")
            .withBody(ObjectSerialization.writeJson(channelSegmentIds))));

    gatewayClient.storeChannelSegments(channelSegments);

    // Make sure the client invokes the osd gateway service
    verify(1, postRequestedFor(
        urlEqualTo(BASE_CHANNELSEGMENTS_URL + "/store"))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withHeader("Accept", equalTo("application/json"))
        .withRequestBody(binaryEqualTo(requestBody)));
  }

  @Test
  public void testStoreChannelSegmentsFailureThrowsException() {
    final List<ChannelSegment<Waveform>> channelSegments = List.of(
        TestFixtures.randomChannelSegment(), TestFixtures.randomChannelSegment());

    final byte[] requestBody = ObjectSerialization
        .writeMessagePack(channelSegments);

    // Post MessagePack requestBody to the /storeChannelSegments endpoint; return OK - 200
    givenThat(post(urlEqualTo(BASE_CHANNELSEGMENTS_URL + "/store"))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withHeader("Accept", equalTo("application/json"))
        .withRequestBody(binaryEqualTo(requestBody))
        .willReturn(serverError()
            .withHeader("Content-Type", ContentType.TEXT_PLAIN.toString())
            .withBody("Could not storeChannelSegments ChannelSegment"))
    );

    exception.expect(IllegalStateException.class);
    exception.expectMessage("Could not storeChannelSegments ChannelSegment");
    gatewayClient.storeChannelSegments(channelSegments);
  }

  @Test
  public void testStoreChannelSegmentsNullChannelSegmentsExpectNullPointerException() {

    exception.expect(NullPointerException.class);
    exception.expectMessage("CoiClient storeChannelSegments requires non-null channelSegments");
    gatewayClient.storeChannelSegments(null);
  }

}
