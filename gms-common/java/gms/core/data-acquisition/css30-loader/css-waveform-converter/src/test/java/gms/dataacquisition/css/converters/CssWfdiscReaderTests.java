package gms.dataacquisition.css.converters;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import gms.dataacquisition.css.converters.data.SegmentAndSohBatch;
import gms.dataacquisition.css.converters.data.WfdiscSampleReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.utilities.standardtestdataset.ReferenceChannelFileReader;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the CssWfdiscReader.
 */
public class CssWfdiscReaderTests {

  private final ReferenceChannelFileReader channelReader = mock(ReferenceChannelFileReader.class);
  private static final String siteName = "DAVOX", channelName = "HHN";
  private static final UUID DAVOX_HHN_ID = UUID.randomUUID();

  @Before
  public void setup() {
    reset(channelReader);
    // mock the channel reader
    when(channelReader.findChannelIdByNameAndTime(anyString(), anyString(), any(Instant.class)))
        .thenReturn(Optional.empty());
    final List<Instant> maskStartTimes = List.of(
        // these times correspond to the test wfdisc file times for segments for DAVOX/HHN
        Instant.parse("2010-05-20T00:00:00Z"),
        Instant.parse("2010-05-20T00:59:59.924Z"),
        Instant.parse("2010-05-20T02:55:32.724Z"));
    for (Instant t : maskStartTimes) {
      when(channelReader.findChannelIdByNameAndTime(siteName, channelName, t))
          .thenReturn(Optional.of(DAVOX_HHN_ID));
    }
  }

  @Test
  public void testReader() throws Exception {
    final CssWfdiscReader reader = new CssWfdiscReader(
        "src/test/resources/css/WFS4/wfdisc_gms_s4.txt", channelReader,
        1, List.of(siteName), List.of(channelName), null, null, false);
    final List<SegmentAndSohBatch> allBatches = reader.readAllBatches();
    final Set<ChannelSegment<Waveform>> segments = new HashSet<>();
    final Set<AcquiredChannelSohBoolean> sohs = new HashSet<>();
    final Map<UUID, WfdiscSampleReference> idToWs = new HashMap<>();
    for (SegmentAndSohBatch batch : allBatches) {
      segments.addAll(batch.getSegments());
      sohs.addAll(batch.getSohs());
      idToWs.putAll(batch.getIdToW());
    }
    assertNotNull(segments);
    assertEquals(3, segments.size());
    assertNotNull(sohs);
    assertEquals(1, sohs.size());

    // assert properties of the loaded segments
    for (ChannelSegment<Waveform> cs : segments) {
      assertEquals(
          "Expected channel id of each segment to match mock from channel reader",
          DAVOX_HHN_ID, cs.getChannelId());
      assertEquals(siteName + "/" + channelName + " ACQUIRED", cs.getName());
      assertEquals(ChannelSegment.Type.ACQUIRED, cs.getType());
    }
    // assert properties of the loaded soh
    Optional<AcquiredChannelSohBoolean> onlySohOptional = sohs.stream().findFirst();
    assertTrue(onlySohOptional.isPresent());
    AcquiredChannelSohBoolean onlySoh = onlySohOptional.get();
    assertNotNull(onlySoh);
    assertEquals("Expected channel id of each segment to match mock from channel reader",
        DAVOX_HHN_ID, onlySoh.getChannelId());
    assertEquals(AcquiredChannelSohType.CLIPPED, onlySoh.getType());
    assertEquals(true, onlySoh.getStatus());
    assertEquals(Instant.parse("2010-05-20T00:00:00Z"), onlySoh.getStartTime());
    assertEquals(Instant.parse("2010-05-20T00:02:12.991Z"), onlySoh.getEndTime());

    // assert properties of WfdiscSampleReferences
    assertEquals(3, idToWs.size());
    WfdiscSampleReference wfdiscSampleReference = idToWs.get(UUID.nameUUIDFromBytes("64583333".getBytes()));
    assertNotNull(wfdiscSampleReference);
    assertEquals("DAVOX0.w", wfdiscSampleReference.getWaveformFile());
    assertEquals(15960, wfdiscSampleReference.getSampleCount());
    assertEquals(3897184, wfdiscSampleReference.getfOff());
  }
}
