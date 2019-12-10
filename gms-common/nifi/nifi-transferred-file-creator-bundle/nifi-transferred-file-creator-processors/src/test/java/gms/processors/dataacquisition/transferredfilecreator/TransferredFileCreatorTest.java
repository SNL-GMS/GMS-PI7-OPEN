package gms.processors.dataacquisition.transferredfilecreator;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.*;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


public class TransferredFileCreatorTest {

    private static final ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    private TestRunner testRunner;

    //This is the same test file as SequenceNumberUtilityTest.
    private static String testSeqNumFile = "src/test/resources/seqNumFile.txt";
    private static long startingSeqNum = 300; //should match whats in the file above
    private static int flowFileIntake = 10; //how many flowfiles before creating an invoice
    private static int resendInvoice = 10; //how many times to resend an invoice

    @Before
    public void init() throws Exception {
        reset();
        testRunner = TestRunners.newTestRunner(TransferredFileCreator.class);
        testRunner.setProperty(TransferredFileCreator.SEQUENCE_NUMBER_FILE, testSeqNumFile);
        testRunner.setProperty(TransferredFileCreator.FLOW_FILE_INTAKE_PROPERTY, String.valueOf(flowFileIntake));
        testRunner.setProperty(TransferredFileCreator.RESEND_INVOICE_PROPERTY, String.valueOf(resendInvoice));
    }

    //needed to set the file back to default original starting value
    @AfterClass
    public static void reset() throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(testSeqNumFile));
        writer.write(String.valueOf(startingSeqNum));
        writer.close();
    }

    @Test
    public void testBadInput() throws Exception{
        final String badFlowContent = "blah";
        // enqueue file one: garbage content and good file name
        final MockFlowFile badContentGoodNameFlow = testRunner.enqueue(
                badFlowContent, filenameAttrs("rsdf-sta-1-id-1.json"));

        // enqueue file two: good content and unknown file name
        final RawStationDataFrame frame = createFrame();
        final String frameString = mapper.writeValueAsString(frame);
        final MockFlowFile goodContentNoFilename = testRunner.enqueue(frameString);

        // run the processor
        testRunner.run();

        //Nothing should succeed
        final List<MockFlowFile> successFlows = testRunner.getFlowFilesForRelationship(
                TransferredFileCreator.SUCCESS);
        assertEquals(0, successFlows.size());

        //Both should fail
        final List<MockFlowFile> failureFlows = testRunner.getFlowFilesForRelationship(
                TransferredFileCreator.FAILURE);
        assertEquals(2, failureFlows.size());
        assertTrue(failureFlows.containsAll(List.of(badContentGoodNameFlow, goodContentNoFilename)));
    }

    //Tests processor behavior. A flow file containing a RawStationDataFrame goes in, once it has 10, write an invoice
    //The invoice should have all 10 of those filenames
    @Test
    public void testProcessor() throws Exception {
        // Enqueue 10 flowfiles containing RSDFs to trigger invoice creation
        // Also keep a list of rsdf names
        List<String> rsdfNames = new ArrayList<>();
        for(int i=0; i<flowFileIntake; i++){
            RawStationDataFrame rsdf = createFrame();
            String name = String.format("rsdf-sta-%s-id-%s.json",rsdf.getStationId(), rsdf.getId());
            testRunner.enqueue(mapper.writeValueAsString(rsdf), filenameAttrs(name));
            rsdfNames.add(name);
        }

        //run the processor
        testRunner.run();

        //assert an invoice was written out to SUCCESS
        final List<MockFlowFile> successFlows = testRunner.getFlowFilesForRelationship(
                TransferredFileCreator.SUCCESS);
        assertEquals(1, successFlows.size());

        //assert nothing failed
        final List<MockFlowFile> failureFlows = testRunner.getFlowFilesForRelationship(
                TransferredFileCreator.FAILURE);
        assertEquals(0, failureFlows.size());

        //grab the contents
        TransferredFileInvoice outputInvoice = mapper.readValue(
                new String(successFlows.get(0).toByteArray(), StandardCharsets.UTF_8),
                TransferredFileInvoice.class);

        // +1 because of the invoice itself
        Set<TransferredFile> transferredFiles = outputInvoice.getTransferredFiles();
        assertEquals(rsdfNames.size() + 1, transferredFiles.size());

        //see if every rsdf is present
        Set<String> transferredFileNames = transferredFiles.stream().
                map(TransferredFile::getFileName).collect(Collectors.toSet());
        for (String rsdfName : rsdfNames) {
            assertTrue(transferredFileNames.contains(rsdfName));
        }

        //test if it contains the invoice itself
        assertTrue(transferredFileNames.contains("inv-" + (startingSeqNum +1) + ".inv"));
    }

    //Tests invoice behavior. Should include any invoices sent less than 10 times but nothing more than 10 times.
    @Test
    public void testMultipleInvoices() throws Exception{
        int invoicesToList = 15;
        for(int i=0; i<flowFileIntake*invoicesToList; i++){
            RawStationDataFrame rsdf = createFrame();
            String name = String.format("rsdf-sta-%s-id-%s.json",rsdf.getStationId(), rsdf.getId());
            testRunner.enqueue(mapper.writeValueAsString(rsdf), filenameAttrs(name));
        }
        testRunner.run(invoicesToList);

        //assert the correct number of invoices was written out to SUCCESS
        final List<MockFlowFile> successFlows = testRunner.getFlowFilesForRelationship(
                TransferredFileCreator.SUCCESS);
        assertEquals(invoicesToList, successFlows.size());

        //assert nothing failed
        final List<MockFlowFile> failureFlows = testRunner.getFlowFilesForRelationship(
                TransferredFileCreator.FAILURE);
        assertEquals(0, failureFlows.size());

        //assert that the transferred file lists contains the invoices we expect
        for(int i=0; i<successFlows.size();i++){
            TransferredFileInvoice invoice = mapper.readValue(
                    new String(successFlows.get(i).toByteArray(), StandardCharsets.UTF_8),
                    TransferredFileInvoice.class);
            Set<TransferredFile> transferredFiles = invoice.getTransferredFiles();
            if(i<resendInvoice){
                //+1 for itself
                assertEquals(flowFileIntake + i +1, transferredFiles.size());
                assertInvoicesAsExpected(resendInvoice, invoice.getSequenceNumber(), transferredFiles);
            }
            //invoices that have already been sent 10 times should not be present
            else{
                assertEquals(flowFileIntake + resendInvoice, transferredFiles.size());
                assertInvoicesAsExpected(resendInvoice, invoice.getSequenceNumber(), transferredFiles);
            }
        }
    }

    private static void assertInvoicesAsExpected(int invoiceRepeatAmount, long seqNum, Collection<TransferredFile> files) {
        //because the first invoice is +1 the starting sequence number
        final List<Long> seqNums = LongStream.rangeClosed(seqNum-invoiceRepeatAmount +1, seqNum)
                .boxed()
                .filter(l -> l >= startingSeqNum + 1) //because the first invoice is +1 the starting sequence number
                .collect(Collectors.toList());
        assertInvoicesInOnce(seqNums, files);
    }

    private static void assertInvoicesInOnce(List<Long> sequenceNums, Collection<TransferredFile> files) {
        final List<TransferredFile<TransferredFileInvoiceMetadata>> invoices = files.stream()
                .filter(f -> f.getMetadataType().equals(TransferredFileMetadataType.TRANSFERRED_FILE_INVOICE))
                .map(f -> (TransferredFile<TransferredFileInvoiceMetadata>) f)
                .collect(Collectors.toList());
        final List<Long> actualSequenceNums = invoices.stream()
                .map(i -> i.getMetadata().getSequenceNumber()).collect(Collectors.toList());
        Collections.sort(sequenceNums);
        Collections.sort(actualSequenceNums);
        assertEquals("Expected to find all of the sequence numbers in the files",
                sequenceNums, actualSequenceNums);
    }

    private static RawStationDataFrame createFrame() {
        final Set<UUID> chanIds = new HashSet<>();
        chanIds.add(UUID.randomUUID());
        return RawStationDataFrame.create(UUID.randomUUID(),
                chanIds, AcquisitionProtocol.SEEDLINK, Instant.EPOCH,
                Instant.EPOCH.plusSeconds(11), Instant.now(),
                new byte[] {(byte) 1, (byte) 2},
                RawStationDataFrame.AuthenticationStatus.NOT_APPLICABLE, CreationInfo.DEFAULT);
    }

    private static Map<String, String> filenameAttrs(String filename) {
        final Map<String, String> m = new HashMap<>();
        m.put("filename", filename);
        return Collections.unmodifiableMap(m);
    }
}
