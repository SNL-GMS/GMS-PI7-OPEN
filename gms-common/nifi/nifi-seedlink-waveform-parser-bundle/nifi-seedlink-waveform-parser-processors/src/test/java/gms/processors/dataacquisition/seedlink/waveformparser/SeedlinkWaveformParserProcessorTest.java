package gms.processors.dataacquisition.seedlink.waveformparser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.processors.dataacquisition.seedlink.waveformparser.SeedlinkWaveformParserProcessor.FrameParser;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

public class SeedlinkWaveformParserProcessorTest {

  private static final ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private FrameParser mockParser = mock(FrameParser.class);

  private TestRunner testRunner;

  @Before
  public void init() {
    reset(mockParser);
    testRunner = TestRunners.newTestRunner(new SeedlinkWaveformParserProcessor() {
      // override the frame parser to return the mock
      protected FrameParser getFrameParser() {
        return mockParser;
      }
    });
  }

  @Test
  public void testProcessorWithBadContent() {
    final String flowContent = "blah";
    testRunner.enqueue(flowContent);
    testRunner.run();
    verify(mockParser, never()).parse(any(), any());  // verify parser is not called
    testRunner.assertAllFlowFilesTransferred(SeedlinkWaveformParserProcessor.FAILURE);
  }

  @Test
  public void testProcessor() throws Exception {
    final RawStationDataFrame goodFrame = createFrame();
    final RawStationDataFrame badFrame = createFrame();
    final ChannelSegment<Waveform> segment = createSegment();
    final List<ChannelSegment<Waveform>> segments = new ArrayList<>();
    segments.add(segment);
    final Collection<AcquiredChannelSoh> sohs = createSohs();
    // when goodFrame is parsed, return a result.
    when(mockParser.parse(goodFrame.getRawPayload(), goodFrame.getChannelIds().iterator().next()))
        .thenReturn(Optional.of(Pair.of(segment, sohs)));
    // when badFrame is parsed, return empty.
    when(mockParser.parse(badFrame.getRawPayload(), badFrame.getChannelIds().iterator().next()))
        .thenReturn(Optional.empty());

    // Test the goodFrame path
    final String goodFrameFlowContent = mapper.writeValueAsString(goodFrame);
    testRunner.enqueue(goodFrameFlowContent);
    testRunner.run();
    verify(mockParser, times(1)).parse(
        goodFrame.getRawPayload(), goodFrame.getChannelIds().iterator().next());
    // verify the parsed data from the good frame went to the SUCCESS relationships
    assertOneFlowWithContent(SeedlinkWaveformParserProcessor.WAVEFORM_SUCCESS, segments);
    assertOneFlowWithContent(SeedlinkWaveformParserProcessor.SOH_SUCCESS, sohs);

    // Test the badFrame path
    final String badFrameFlowContent = mapper.writeValueAsString(badFrame);
    testRunner.enqueue(badFrameFlowContent);
    testRunner.run();
    verify(mockParser, times(1)).parse(
        badFrame.getRawPayload(), badFrame.getChannelIds().iterator().next());
    // verify the bad frame went to the FAILURE relationship
    assertOneFlowWithContent(SeedlinkWaveformParserProcessor.FAILURE,
        new RawStationDataFrame[]{badFrame});
  }

  private void assertOneFlowWithContent(Relationship relationship, Object content)
      throws Exception {
    final List<MockFlowFile> flows = testRunner.getFlowFilesForRelationship(relationship);
    assertEquals(1, flows.size());
    flows.get(0).assertContentEquals(mapper.writeValueAsString(content));
  }

  private static RawStationDataFrame createFrame() {
    final Random rand = new Random();
    final Set<UUID> chanIds = new HashSet<>();
    chanIds.add(UUID.randomUUID());
    return RawStationDataFrame.create(UUID.randomUUID(),
        chanIds, AcquisitionProtocol.SEEDLINK, Instant.EPOCH,
        Instant.EPOCH.plusSeconds(11), Instant.now(),
        new byte[]{(byte) rand.nextInt(255), (byte) rand.nextInt(255)},
        AuthenticationStatus.NOT_APPLICABLE, CreationInfo.DEFAULT);
  }

  private static ChannelSegment<Waveform> createSegment() {
    final Collection<Waveform> wfs = new ArrayList<>();
    wfs.add(Waveform.withoutValues(Instant.EPOCH, 40, 10));
    return ChannelSegment.create(UUID.randomUUID(), "name",
        Type.RAW, wfs, CreationInfo.DEFAULT);
  }

  private static Collection<AcquiredChannelSoh> createSohs() {
    final Collection<AcquiredChannelSoh> sohs = new ArrayList<>();
    sohs.add(AcquiredChannelSohAnalog.create(UUID.randomUUID(),
        AcquiredChannelSohType.STATION_POWER_VOLTAGE, Instant.EPOCH,
        Instant.EPOCH.plusSeconds(5), 1.0, CreationInfo.DEFAULT));
    sohs.add(AcquiredChannelSohBoolean.create(UUID.randomUUID(),
        AcquiredChannelSohType.BACKUP_POWER_UNSTABLE, Instant.EPOCH,
        Instant.EPOCH.plusSeconds(1), true, CreationInfo.DEFAULT));
    return Collections.unmodifiableCollection(sohs);
  }

}
