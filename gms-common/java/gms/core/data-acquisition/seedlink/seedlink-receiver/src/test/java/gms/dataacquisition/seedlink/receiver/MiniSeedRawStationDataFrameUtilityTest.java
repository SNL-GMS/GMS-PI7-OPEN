package gms.dataacquisition.seedlink.receiver;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationAndChannelId;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.BeforeClass;
import org.junit.Test;


public class MiniSeedRawStationDataFrameUtilityTest {

  private static final double TOLERANCE = 1e-9;
  private static final String
      NETWORK_NAME = "II", STATION_NAME = "KDAK", CHANNEL_NAME = "BH1", LOCATION_CODE = "00",
      RECEIVED_NAME = String.join("/", NETWORK_NAME, STATION_NAME, CHANNEL_NAME, LOCATION_CODE);
  private static final UUID STATION_ID = UUID.randomUUID(), CHANNEL_ID = UUID.randomUUID();
  private static final Map<String, StationAndChannelId> NAME_TO_IDS = Map.of(RECEIVED_NAME,
      StationAndChannelId.from(STATION_ID, CHANNEL_ID));
  private static final Instant
      FRAME_START_TIME = Instant.parse("2018-11-28T15:00:00.019Z"),
      FRAME_END_TIME = Instant.parse("2018-11-28T15:00:01.919Z");
  private static byte[] rawBytes;

  @BeforeClass
  public static void setup() throws Exception {
    // read the test packet bytes once
    rawBytes = Files.readAllBytes(
        Paths.get("src/test/resources/" + STATION_NAME + ".mseed"));
  }


  /**
   * Test the 'happy path' of parsing a valid packet with a valid matching acquisition group (that
   * has station info matching the packet)
   */
  @Test
  public void testParseAcquiredStationDataPacket() throws Exception {
    final RawStationDataFrame rsdf = MiniSeedRawStationDataFrameUtility.
        parseAcquiredStationDataPacket(rawBytes, Instant.now(), NAME_TO_IDS);
    assertNotNull(rsdf);
    assertEquals(STATION_ID, rsdf.getStationId());
    assertEquals(Set.of(CHANNEL_ID), rsdf.getChannelIds());
    assertEquals(AcquisitionProtocol.SEEDLINK, rsdf.getAcquisitionProtocol());
    assertEquals(FRAME_START_TIME, rsdf.getPayloadDataStartTime());
    assertEquals(FRAME_END_TIME, rsdf.getPayloadDataEndTime());
    assertArrayEquals(rawBytes, rsdf.getRawPayload());
    assertEquals(AuthenticationStatus.NOT_APPLICABLE, rsdf.getAuthenticationStatus());
  }

  /**
   * Test parsing when the packet byte[] is bad, but name => id mapping is still good.
   */
  @Test(expected = Exception.class)
  public void testParseBadStationDataPacket() throws Exception {
    MiniSeedRawStationDataFrameUtility.parseAcquiredStationDataPacket(
        new byte[]{(byte) 0, (byte) 1}, Instant.EPOCH, NAME_TO_IDS);
  }

  /**
   * Test parsing with good packet but the acquisition group doesn't know the name in the packet so
   * an empty frame is returned.
   */
  @Test(expected = Exception.class)
  public void testGoodPacketButUnknownStationName() throws Exception {
    MiniSeedRawStationDataFrameUtility.parseAcquiredStationDataPacket(
        rawBytes, Instant.now(),
        Map.of("foo/bar", StationAndChannelId.from(UUID.randomUUID(), UUID.randomUUID())));
  }

  /**
   * Test parsing a byte[] that contains garbage content.
   */
  @Test
  public void testParseBadFrame() {
    final Optional<Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>>> result
        = MiniSeedRawStationDataFrameUtility.parseRawStationDataFrame(
        new byte[]{(byte) 0, (byte) 1}, UUID.randomUUID());
    assertEquals(Optional.empty(), result);
  }

  /**
   * Test parsing a legit frame with seedlink data in it.
   */
  @Test
  public void testParseGoodFrame() throws Exception {
    final UUID chanId = UUID.randomUUID();
    final Optional<Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>>> result
        = MiniSeedRawStationDataFrameUtility.parseRawStationDataFrame(rawBytes, chanId);
    assertNotNull(result);
    assertTrue(result.isPresent());
    final Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>> p = result.get();
    assertNotNull(p);
    final ChannelSegment<Waveform> seg = p.getLeft();
    assertNotNull(seg);
    assertEquals(chanId, seg.getChannelId());
    assertEquals(Type.ACQUIRED, seg.getType());
    assertEquals(Timeseries.Type.WAVEFORM, seg.getTimeseriesType());
    final List<Waveform> waveforms = seg.getTimeseries();
    assertEquals(1, waveforms.size());
    final Waveform onlyWaveform = waveforms.get(0);
    assertEquals(40.0, onlyWaveform.getSampleRate(), TOLERANCE);
    assertEquals(FRAME_START_TIME, onlyWaveform.getStartTime());
    assertEquals(FRAME_END_TIME, onlyWaveform.getEndTime());
    final double[] expectedWaveformsSamples = CoiObjectMapperFactory
        .getJsonObjectMapper().readValue(
            new File("src/test/resources/expectedWaveformSamples.json"), double[].class);
    assertArrayEquals(expectedWaveformsSamples, onlyWaveform.getValues(), TOLERANCE);
    final Collection<AcquiredChannelSoh> sohs = p.getRight();
    assertNotNull(sohs);
    // TODO: when SOH parsing is implemented, add testing on SOH and remove below assertion
    assertTrue(sohs.isEmpty());
  }
}
