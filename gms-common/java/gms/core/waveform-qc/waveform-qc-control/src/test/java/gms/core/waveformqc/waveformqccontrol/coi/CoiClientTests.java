package gms.core.waveformqc.waveformqccontrol.coi;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.mashape.unirest.http.Unirest;
import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegment;
import gms.core.waveformqc.waveformqccontrol.coi.CoiClient;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests the {@link CoiClient} using {@link @RestClientTest} to mock the OSD COI service.
 */
public class CoiClientTests {


  private WireMockServer wireMockServer;

  private static final String BASE_URL = "http://localhost";

  private static final ObjectMapper jsonMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  //TODO: Update these URIs when the new "/coi"-based route is implemented.
  private static final String WAVEFORM_COI_URI = "/mechanisms/object-storage-distribution/waveforms";
  private static final String QC_MASK_COI_URI = "/mechanisms/object-storage-distribution/signal-detection";

  private CoiClient coiClient;

  @BeforeAll
  public static void initialize() {
    Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {

      @Override
      public <T> T readValue(String value, Class<T> valueType) {
        try {
          return jsonMapper.readValue(value, valueType);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public String writeValue(Object value) {
        try {
          return jsonMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  @AfterAll
  public static void shutdown() {
    Unirest.setObjectMapper(null);
  }

  @BeforeEach
  public void setUp() {
    wireMockServer = new WireMockServer(options().dynamicPort());
    wireMockServer.start();
    coiClient = new CoiClient(
        BASE_URL + ":" + wireMockServer.port() + WAVEFORM_COI_URI,
        BASE_URL + ":" + wireMockServer.port() + QC_MASK_COI_URI);
  }

  @AfterEach
  public void tearDown() {
    wireMockServer.stop();
    coiClient = null;
  }

  @Test
  void testNullArguments() {
    assertThrows(NullPointerException.class, () -> coiClient.getWaveforms(null));
    assertThrows(NullPointerException.class, () -> coiClient.getChannelSoh(null));
    assertThrows(NullPointerException.class,
        () -> coiClient.getChannelSohStatuses(null, Duration.ofMillis(1000)));
    assertThrows(NullPointerException.class, () -> coiClient.getChannelSohStatuses(
        ChannelSegmentDescriptor.from(UUID.randomUUID(), Instant.MIN, Instant.MAX), null));
    assertThrows(NullPointerException.class, () -> coiClient.getQcMasks(null));
    assertThrows(NullPointerException.class, () -> coiClient.storeQcMasks(null));

  }

  @Test
  void testGetWaveforms() throws IOException {
    UUID processingChannelId = UUID.randomUUID();

    wireMockServer.givenThat(post(urlEqualTo(
        WAVEFORM_COI_URI + CoiClient.BASE_URL + "/channel-segment"))
        .withRequestBody(equalTo(jsonMapper.writeValueAsString(
            Map.of("channel-ids", Set.of(processingChannelId), "start-time",
                Instant.MIN,
                "end-time", Instant.MAX, "with-waveforms", true))))
        .willReturn(ok()
            .withHeader("Content-Type", "application/json")
            .withBody(jsonMapper.writeValueAsString(new HashMap<>() {{
              put(processingChannelId, getMockChannelSegment(processingChannelId));
            }}))));

    ChannelSegmentDescriptor descriptor = ChannelSegmentDescriptor
        .from(processingChannelId, Instant.MIN,
            Instant.MAX);

    assertNotNull(coiClient.getWaveforms(descriptor));
  }

  @Test
  void testGetChannelSohStatuses() throws IOException {
    UUID processingChannelId = UUID.randomUUID();

    ChannelSohStatusSegment sohStatus1 = sohStatus(processingChannelId,
        getMockSohBool(processingChannelId));

    wireMockServer
        .givenThat(get(urlPathMatching(
            WAVEFORM_COI_URI + CoiClient.BASE_URL + "/acquired-channel-soh/boolean(.*)"))
            .withQueryParam("channel-id", equalTo(processingChannelId.toString()))
            .withQueryParam("start-time", equalTo(Instant.MIN.toString()))
            .withQueryParam("end-time", equalTo(Instant.MAX.toString()))
            .willReturn(ok()
                .withHeader("Content-Type", "application/json")
                .withBody(
                    jsonMapper.writeValueAsString(List.of(getMockSohBool(processingChannelId))))));

    ChannelSegmentDescriptor descriptor = ChannelSegmentDescriptor
        .from(processingChannelId, Instant.MIN,
            Instant.MAX);

    List<ChannelSohStatusSegment> channelSohStatuses = coiClient
        .getChannelSohStatuses(descriptor, Duration.ofMillis(1000));
    assertNotNull(channelSohStatuses);
    assertThat(channelSohStatuses,
        is(List.of(sohStatus1)));
  }

  @Test
  void testGetQcMasks() throws IOException {
    UUID processingChannelId = UUID.randomUUID();

    wireMockServer.givenThat(post(urlEqualTo(
        QC_MASK_COI_URI + CoiClient.BASE_URL + "/qc-mask"))
        .withRequestBody(equalTo(jsonMapper.writeValueAsString(
            Map.of("channel-ids", Set.of(processingChannelId), "start-time",
                Instant.MIN,
                "end-time", Instant.MAX))))
        .willReturn(ok()
            .withHeader("Content-Type", "application/json")
            .withBody(jsonMapper.writeValueAsString(new HashMap<>() {{
              put(processingChannelId, List.of(getMockQcMask(processingChannelId)));
            }}))));

    ChannelSegmentDescriptor descriptor = ChannelSegmentDescriptor
        .from(processingChannelId, Instant.MIN,
            Instant.MAX);

    assertNotNull(coiClient.getQcMasks(descriptor));
  }

  private static ChannelSegment<Waveform> getMockChannelSegment(UUID processingChannelId) {
    return ChannelSegment.create(processingChannelId,
        "mockChannel" + processingChannelId.toString(),
        ChannelSegment.Type.ACQUIRED, Set.of(Waveform.withoutValues(Instant.MIN, 1, 1)),
        CreationInfo.DEFAULT);
  }

  private static QcMask getMockQcMask(UUID processingChannelId) {
    return QcMask.create(
        processingChannelId, Set.of(), emptyList(), QcMaskCategory.ANALYST_DEFINED,
        QcMaskType.LONG_GAP, "", Instant.MIN, Instant.MAX);
  }

  private static AcquiredChannelSohBoolean getMockSohBool(UUID processingChannelId) {
    return AcquiredChannelSohBoolean.create(
        processingChannelId, AcquiredChannelSohType.ZEROED_DATA, Instant.MIN, Instant.MAX, true,
        CreationInfo.DEFAULT);
  }

  private static ChannelSohStatusSegment sohStatus(UUID processingChannelId,
      AcquiredChannelSohBoolean mockSohBool) {
    return ChannelSohStatusSegment.builder().setChannelId(processingChannelId)
        .setType(mockSohBool.getType())
        .addStatusSegment(Instant.MIN, Instant.MAX, mockSohBool.getStatus()).build();
  }

  @Test
  void testStoreQcMasksEmptyList() throws IOException {
    final String url = QC_MASK_COI_URI + CoiClient.BASE_URL + "/qc-masks";
    wireMockServer.givenThat(post(urlEqualTo(url))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalTo(jsonMapper.writeValueAsString(emptyList())))
        .willReturn(ok()));

    coiClient.storeQcMasks(emptyList());
    wireMockServer.verify(0, postRequestedFor(urlEqualTo(url)));
  }
}
