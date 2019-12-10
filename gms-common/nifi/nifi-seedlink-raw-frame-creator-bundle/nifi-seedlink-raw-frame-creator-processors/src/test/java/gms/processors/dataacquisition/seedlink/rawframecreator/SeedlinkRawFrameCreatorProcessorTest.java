package gms.processors.dataacquisition.seedlink.rawframecreator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.processors.dataacquisition.seedlink.rawframecreator.SeedlinkRawFrameCreatorProcessor.PacketParser;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.ReceivedStationDataPacket;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationAndChannelId;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

public class SeedlinkRawFrameCreatorProcessorTest {

  private static final ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private static final Map<String, StationAndChannelId> idsToReceivedName;

  private PacketParser mockParser = mock(PacketParser.class);

  private TestRunner testRunner;

  static {
    final Map<String, StationAndChannelId> m = new HashMap<>();
    m.put("someNet/someSta/someChan/loc",
        StationAndChannelId.from(UUID.randomUUID(), UUID.randomUUID()));
    idsToReceivedName = Collections.unmodifiableMap(m);
  }

  @Before
  public void init() throws Exception {
    reset(mockParser);
    testRunner = TestRunners.newTestRunner(new SeedlinkRawFrameCreatorProcessor() {
      // override the packet parser to return the mock
      protected PacketParser getPacketParser() {
        return mockParser;
      }
    });
    testRunner.setProperty(SeedlinkRawFrameCreatorProcessor.IDS_BY_RECEIVED_NAME_PROPERTY,
        mapper.writeValueAsString(idsToReceivedName));
  }

  @Test
  public void testProcessorWithBadContent() throws Exception {
    final String flowContent = "blah";
    testRunner.enqueue(flowContent);
    testRunner.run();
    verify(mockParser, never()).parse(any(), any(), any());  // verify parser is not called
    testRunner.assertAllFlowFilesTransferred(SeedlinkRawFrameCreatorProcessor.FAILURE);
  }

  @Test
  public void testProcessor() throws Exception {
    final ReceivedStationDataPacket goodPacket = createPacket();
    final ReceivedStationDataPacket badPacket = createPacket();
    final String flowContent = mapper.writeValueAsString(
        new ReceivedStationDataPacket[]{goodPacket, badPacket});
    final RawStationDataFrame parsedFrame = createFrame();
    when(mockParser.parse(goodPacket.getPacket(), goodPacket.getReceptionTime(), idsToReceivedName))
        .thenReturn(parsedFrame);
    when(mockParser.parse(badPacket.getPacket(), badPacket.getReceptionTime(), idsToReceivedName))
        .thenThrow(new NullPointerException("psyche!"));
    testRunner.enqueue(flowContent);
    testRunner.run();
    verify(mockParser, times(1)).parse(
        goodPacket.getPacket(), goodPacket.getReceptionTime(), idsToReceivedName);
    verify(mockParser, times(1)).parse(
        badPacket.getPacket(), badPacket.getReceptionTime(), idsToReceivedName);
    final List<MockFlowFile> successFlows = testRunner.getFlowFilesForRelationship(
        SeedlinkRawFrameCreatorProcessor.SUCCESS);
    assertEquals(1, successFlows.size());
    final MockFlowFile onlySuccessFlow = successFlows.get(0);
    onlySuccessFlow.assertContentEquals(mapper.writeValueAsString(parsedFrame));
    onlySuccessFlow.assertAttributeExists("filename");  // written for use by PutFile
    assertTrue(onlySuccessFlow.getAttribute("filename").contains("rsdf"));
    final List<MockFlowFile> failureFlows = testRunner.getFlowFilesForRelationship(
        SeedlinkRawFrameCreatorProcessor.FAILURE);
    assertEquals(1, failureFlows.size());
    failureFlows.get(0).assertContentEquals(mapper.writeValueAsString(badPacket));
  }

  private static ReceivedStationDataPacket createPacket() {
    final Random rand = new Random();
    return ReceivedStationDataPacket.from(
        new byte[]{(byte) rand.nextInt(255), (byte) rand.nextInt(255)},
        Instant.EPOCH.plusMillis(rand.nextInt()),
        rand.nextLong(), "station");
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
}