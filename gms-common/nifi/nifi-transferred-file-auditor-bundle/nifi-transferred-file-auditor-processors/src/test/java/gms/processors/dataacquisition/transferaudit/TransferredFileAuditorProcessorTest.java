package gms.processors.dataacquisition.transferaudit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoice;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import gms.utilities.transferauditor.TransferAuditorUtility;
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
import org.mockito.Mockito;

public class TransferredFileAuditorProcessorTest {

  private static final ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private TestRunner testRunner;

  private TransferAuditorUtility auditor
      = Mockito.mock(TransferAuditorUtility.class);

  @Before
  public void init() {
    testRunner = TestRunners.newTestRunner(new TransferredFileAuditorProcessor() {
      // return the mocked auditor instead of real
      protected TransferAuditorUtility getAuditor() {
        return auditor;
      }
    });
  }

  @Test
  public void testProcessor() throws Exception {
    final String frameFilename = "some-file-rsdf-foo.json";
    final String invoiceFilename = "some-file-inv-foo.inv";
    final RawStationDataFrame frame = createFrame();
    final String frameString = mapper.writeValueAsString(frame);
    final TransferredFileInvoice invoice = createInvoice();
    final String invoiceString = mapper.writeValueAsString(invoice);
    // enqueue file one: raw station data frame
    testRunner.enqueue(frameString, filenameAttrs(frameFilename));
    // enqueue file two: invoice
    testRunner.enqueue(invoiceString, filenameAttrs(invoiceFilename));
    final String badFlowContent = "blah";
    // enqueue file three: garbage content and good file name
    final MockFlowFile badContentGoodNameFlow = testRunner.enqueue(
        badFlowContent, filenameAttrs("frame-rsdf.json"));
    // enqueue file four: good content and unknown file name
    final MockFlowFile goodContentNoFilename = testRunner.enqueue(frameString);
    // queue file five: good content and file name that isn't recognized as any type
    final MockFlowFile goodContentUnknownFilename = testRunner.enqueue(
        frameString, filenameAttrs("foo.json"));
    // run the processor
    testRunner.run();
    // assert the frame and invoice were given to the auditor utility
    verify(auditor, times(1))
        .receive(eq(frame), eq(frameFilename), any(Instant.class));
    verify(auditor, times(1))
        .receive(eq(invoice), eq(invoiceFilename), any(Instant.class));
    // assert the frame (not the invoice) was transferred to SUCCESS
    final List<MockFlowFile> successFlows = testRunner.getFlowFilesForRelationship(
        TransferredFileAuditorProcessor.SUCCESS);
    assertNotNull(successFlows);
    assertEquals(1, successFlows.size());
    successFlows.get(0).assertContentEquals(
        // processor writes out frame arrays built up from single frames
        mapper.writeValueAsString(new RawStationDataFrame[]{frame}));
    // assert the bad flow file was transferred to FAILURE
    final List<MockFlowFile> failureFlows = testRunner.getFlowFilesForRelationship(
        TransferredFileAuditorProcessor.FAILURE);
    assertNotNull(failureFlows);
    assertEquals(3, failureFlows.size());
    assertTrue(failureFlows.contains(badContentGoodNameFlow));
    assertTrue(failureFlows.contains(goodContentNoFilename));
    assertTrue(failureFlows.contains(goodContentUnknownFilename));
    // note: invoice was removed from flow but that need not be asserted;
    // if it's not transferred or removed, the test will
    // throw an exception when the session is committed
  }

  private static RawStationDataFrame createFrame() {
    final Set<UUID> chanIds = new HashSet<>();
    chanIds.add(UUID.randomUUID());
    return RawStationDataFrame.create(UUID.randomUUID(),
        chanIds, AcquisitionProtocol.SEEDLINK, Instant.EPOCH,
        Instant.EPOCH.plusSeconds(11), Instant.now(),
        new byte[] {(byte) 1, (byte) 2},
        AuthenticationStatus.NOT_APPLICABLE, CreationInfo.DEFAULT);
  }

  private static TransferredFileInvoice createInvoice() {
    return TransferredFileInvoice.from(new Random().nextLong(), new HashSet<>());
  }

  private static Map<String, String> filenameAttrs(String filename) {
    final Map<String, String> m = new HashMap<>();
    m.put("filename", filename);
    return Collections.unmodifiableMap(m);
  }
}
