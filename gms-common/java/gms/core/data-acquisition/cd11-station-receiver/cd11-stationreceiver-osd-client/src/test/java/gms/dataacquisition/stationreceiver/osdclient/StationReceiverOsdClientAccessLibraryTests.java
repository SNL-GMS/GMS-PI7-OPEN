package gms.dataacquisition.stationreceiver.osdclient;

import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

public class StationReceiverOsdClientAccessLibraryTests {

  private static final String
      STORE_CHANNEL_SEGMENTS_URL = "/mechanisms/object-storage-distribution/waveforms/channel-segment/store",
      STORE_ANALOG_SOH_URL = "/coi/acquired-channel-sohs/analog",
      STORE_BOOLEAN_SOH_URL = "/coi/acquired-channel-sohs/boolean",
      STORE_FRAME_URL = "/coi/raw-station-data-frames",
      GET_STATION_URL = "/mechanisms/object-storage-distribution/station-reference/stations/processing/name/",
      GET_SITE_URL = "/mechanisms/object-storage-distribution/station-reference/sites/processing/name/";

  @ClassRule
  public static WireMockClassRule wireMockRule = new WireMockClassRule(
      wireMockConfig().dynamicPort());

  private static StationReceiverOsdClientInterface client;

  @Rule
  public WireMockClassRule instanceRule = wireMockRule;

  private static final ObjectMapper jsonMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private static final ObjectMapper msgpackMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

  @Before
  public void setup() {
    final HttpClientConfig clientConfig = new HttpClientConfig(
        "localhost", instanceRule.port());
    // intentionally pass same client config as both params;
    // makes wiremock testing easier, access library doesn't know the difference
    client = new StationReceiverOsdClientAccessLibrary(clientConfig, clientConfig);
  }

  @Test
  public void testStoreChannelSegments() throws Exception {
    final List<ChannelSegment<Waveform>> segments = List.of(TestFixtures.segment);
    final List<UUID> segmentIds = segments.stream().map(ChannelSegment::getId).collect(Collectors.toList());
    mockServiceMsgpackStore(STORE_CHANNEL_SEGMENTS_URL, segments, segmentIds);
    client.storeChannelSegments(segments);
    verifyPostRequestMsgpack(STORE_CHANNEL_SEGMENTS_URL, segments);
  }

  @Test
  public void testStoreChannelStatesOfHealth() throws Exception {
    mockServiceMsgpackStore(STORE_ANALOG_SOH_URL,
        List.of(TestFixtures.sohAnalog),
        List.of(TestFixtures.sohAnalog.getId()));
    mockServiceMsgpackStore(STORE_BOOLEAN_SOH_URL,
        List.of(TestFixtures.sohBoolean),
        List.of(TestFixtures.sohBoolean.getId()));
    client.storeChannelStatesOfHealth(List.of(TestFixtures.sohAnalog, TestFixtures.sohBoolean));
    verifyPostRequestMsgpack(STORE_ANALOG_SOH_URL, List.of(TestFixtures.sohAnalog));
    verifyPostRequestMsgpack(STORE_BOOLEAN_SOH_URL, List.of(TestFixtures.sohBoolean));
  }

  @Test
  public void testStoreRawStationDataFrames() throws Exception {
    final List<RawStationDataFrame> frames = List.of(TestFixtures.frame);
    final List<UUID> frameIds = frames.stream().map(RawStationDataFrame::getId).collect(Collectors.toList());
    mockServiceMsgpackStore(STORE_FRAME_URL, frames, frameIds);
    client.storeRawStationDataFrame(TestFixtures.frame);
    verifyPostRequestMsgpack(STORE_FRAME_URL, frames);
  }

  @Test
  public void testGetStationId() throws Exception {
    final Station sta = TestFixtures.station;
    mockServiceQueryGetByName(GET_STATION_URL, sta.getName(), sta);
    Optional<UUID> stationId = client.getStationId(sta.getName());
    assertNotNull(stationId);
    assertTrue(stationId.isPresent());
    assertEquals(sta.getId(), stationId.get());
    // query with bad name, expect empty
    stationId = client.getStationId("some fake name");
    assertNotNull(stationId);
    assertFalse(stationId.isPresent());
  }

  @Test
  public void testGetChannelId() throws Exception {
    final Site site = TestFixtures.site;
    final Channel chan = TestFixtures.channel;
    mockServiceQueryGetByName(GET_SITE_URL, TestFixtures.siteName, site);
    Optional<UUID> channelId = client.getChannelId(TestFixtures.siteName, TestFixtures.channelName);
    assertNotNull(channelId);
    assertTrue(channelId.isPresent());
    assertEquals(chan.getId(), channelId.get());
    // query with bad site name, expect empty
    channelId = client.getChannelId("some fake name", TestFixtures.channelName);
    assertNotNull(channelId);
    assertFalse(channelId.isPresent());
    // query with valid site name, but bad channel name; expect empty
    channelId = client.getChannelId(TestFixtures.siteName, "some fake name");
    assertNotNull(channelId);
    assertFalse(channelId.isPresent());
  }

  private static void verifyPostRequestMsgpack(String url, Object body) throws Exception {
    final byte[] binaryBody = msgpackMapper.writeValueAsBytes(body);
    verify(1, postRequestedFor(
        urlEqualTo(url))
        .withHeader("Content-Type", equalTo("application/msgpack"))
        .withHeader("Accept", equalTo("application/json"))
        .withRequestBody(binaryEqualTo(binaryBody)));
  }

  private static void mockServiceMsgpackStore(String url, Object requestBody, Object responseBody)
      throws Exception {

    final byte[] request = msgpackMapper.writeValueAsBytes(requestBody);
    final String response = jsonMapper.writeValueAsString(responseBody);
    givenThat(post(urlEqualTo(url))
        .withRequestBody(binaryEqualTo(request))
        .willReturn(ok()
            .withHeader("Content-Type", "application/json")
            .withBody(response)));
  }

  private static void mockServiceQueryGetByName(String url, String name, Object responseBody) throws Exception {
    final String responseJson = jsonMapper.writeValueAsString(responseBody);
    givenThat(get(urlEqualTo(url + name))
        .willReturn(ok()
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson)));
  }

}
