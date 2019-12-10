package gms.dataacquisition.stationreceiver.cd11.dataframeparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.stationreceiver.osdclient.StationReceiverOsdClientInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.*;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Cd11RawStationDataFrameReaderTest {

  private static Pair<List<ChannelSegment<Waveform>>, List<AcquiredChannelSoh>> parsedFrameData_cc;
  private static StationReceiverOsdClientInterface osdClient;

  @BeforeClass
  public static void setup() throws Exception {
    //Create RawStationDataFrame from s4 format file
    String contents = new String(Files.readAllBytes(
        new File("src/test/resources/seismic-cc-dataframe.json").toPath()));
    final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    RawStationDataFrame frame_cc = objMapper.readValue(contents, RawStationDataFrame.class);

    //Ensure frame deserialized correctly
    osdClient = mock(StationReceiverOsdClientInterface.class);
    when(osdClient.getChannelId(any(), any())).thenReturn(Optional.of(UUID.randomUUID()));
    assertEquals("217e3354-fb0d-40df-84ac-77387ead6d8f", frame_cc.getId().toString());
    assertEquals("[00000000-0000-0000-0000-000000000000]", frame_cc.getChannelIds().toString());
    assertEquals("10144621-aeb4-439a-bb6e-16002d30dfe6", frame_cc.getStationId().toString());
    assertEquals("2018-04-02T05:50:00Z", frame_cc.getPayloadDataStartTime().toString());
    assertEquals("2018-04-02T05:50:10Z", frame_cc.getPayloadDataEndTime().toString());
    assertEquals("2018-04-03T17:30:35.537290Z", frame_cc.getReceptionTime().toString());
    parsedFrameData_cc = Cd11RawStationDataFrameReader.read(frame_cc, osdClient);
  }

  @Test
  public void testRead() throws Exception {
    //Test Channel Segment Parsing
    List<ChannelSegment<Waveform>> segments = parsedFrameData_cc.getLeft();
    assertEquals(14, segments.size());
    ChannelSegment<Waveform> channelSegment = segments.get(0);
    assertEquals(800, channelSegment.getTimeseries().get(0).getSampleCount());
    assertEquals(80.0, channelSegment.getTimeseries().get(0).getSampleRate(), 0);
    assertEquals("2018-04-02T05:50:00Z",
            channelSegment.getTimeseries().get(0).getStartTime().toString());
    assertEquals("2018-04-02T05:50:09.987500Z",
            channelSegment.getTimeseries().get(0).getEndTime().toString());

    Optional<ChannelSegment<Waveform>> gec2a_segment = segments.stream()
        .filter(s -> StringUtils.containsIgnoreCase(s.getName(), "GEC2A/HHZ ACQUIRED"))
        .findFirst();
    assertTrue(gec2a_segment.isPresent());
    double[] firstTenSamples = Arrays.copyOfRange(
        gec2a_segment.get().getTimeseries().get(0).getValues(), 0, 10);
    double[] expected = new double[] {-37618.0, -37639.0, -37657.0,
            -37716.0, -37709.0, -37717.0, -37744.0, -37740.0,
            -37768.0, -37774.0};
    assertArrayEquals(expected, firstTenSamples, 0.00000001);
    // test SOH
    List<AcquiredChannelSoh> acquiredChannelSohs = parsedFrameData_cc.getRight();

    //Only asserting the first channel values, since they are different between channels
    for (int i=0;i <17;i++) {
      AcquiredChannelSoh item = acquiredChannelSohs.get(i);
      //Checking clock differential, only analog SOH
      if (item instanceof AcquiredChannelSohAnalog) {
        assertEquals(2.0, ((AcquiredChannelSohAnalog) item).getStatus(), 0.01);
      } else {
        AcquiredChannelSohBoolean boolSOH = (AcquiredChannelSohBoolean) item;
        AcquiredChannelSohType type = boolSOH.getType();
        // for this frame, only expected 'true' flag is GPS receiver unlocked.
        if (type.equals(AcquiredChannelSohType.VAULT_DOOR_OPENED)) {
          assertTrue(boolSOH.getStatus());
        }
        else {
          assertFalse(boolSOH.getStatus());
        }
      }
    }
  }

}
