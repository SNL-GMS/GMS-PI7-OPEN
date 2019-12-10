package gms.processors.dataacquisition.transferredfilecreator;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFile;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoice;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoiceMetadata;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileRawStationDataFrameMetadata;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import org.apache.commons.lang3.Validate;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class TransferredFileCreator extends AbstractProcessor {
    public static final Relationship SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Successfully wrote TransferredFileInvoice")
            .build();
    public static final Relationship FAILURE = new Relationship.Builder()
            .name("failure")
            .description("Failed to write TransferredFileInvoice")
            .build();

    private final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    private Set<Relationship> relationships;
    private List<String> requestStrings;
    private List<PropertyDescriptor> descriptors;

    //Sets the number of flow files to get before creating an invoice
    //One flowfile contains exactly one RawStationDataFrame
    public static final PropertyDescriptor FLOW_FILE_INTAKE_PROPERTY = new PropertyDescriptor
            .Builder().name("flow-file-intake")
            .displayName("Flow File Intake")
            .description("Sets the number of flow files to get before creating an invoice." +
                    "One flowfile contains exactly one RawStationDataFrame.")
            .required(false)
            .defaultValue("10")
            .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
            .build();

    private int flowFileIntake = 10;


    //Sets the number of time to resend an invoice. Invoices are resent this
    //amount of times to hopefully guarantee cross domain transfer.
    public static final PropertyDescriptor RESEND_INVOICE_PROPERTY = new PropertyDescriptor
            .Builder().name("resend-invoice")
            .displayName("Resend Invoice")
            .description("Sets the number of time to resend an invoice. Invoices are resent this" +
                    "amount of times to hopefully guarantee cross domain transfer.")
            .required(false)
            .defaultValue("10")
            .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
            .build();

    private int resendInvoice = 10;

    //Sets where to look for the sequenceNumberFile
    public static final PropertyDescriptor SEQUENCE_NUMBER_FILE = new PropertyDescriptor
            .Builder().name("sequence-number-file")
            .displayName("Sequence Number File")
            .description("Sets the file which this processor reads to get the next sequence number for Invoices")
            .required(false)
            .defaultValue("/grid/persistence/invoice-sequence-number.txt")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .build();

    private String seqNumFileName = "/grid/persistence/transferred-files/invoice-sequence-number.txt";

    //mapping each TransferredFileInvoice to how many times it has been sent;
    private Map<TransferredFile<TransferredFileInvoiceMetadata>, Integer> invoiceSendCountMap = new HashMap<>();

    @Override
    protected void init(final ProcessorInitializationContext context) {
        getLogger().info("init");
        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(SUCCESS);
        relationships.add(FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);

        final List<PropertyDescriptor> descriptors = new ArrayList<>();
        descriptors.add(FLOW_FILE_INTAKE_PROPERTY);
        descriptors.add(RESEND_INVOICE_PROPERTY);
        descriptors.add(SEQUENCE_NUMBER_FILE);
        this.descriptors = Collections.unmodifiableList(descriptors);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) throws Exception {
        getLogger().info("onScheduled");
        readConfigurationProperties(context);
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        getLogger().info("onTrigger");

        try {
            getLogger().info("Getting flow file");
            final List<FlowFile> flowFiles = session.get(flowFileIntake);
            if (flowFiles == null || flowFiles.isEmpty()) {
                getLogger().info("Flowfiles empty or null: " + flowFiles);
                return;
            }
            getLogger().info("Got flow files");
            getLogger().info("Attempting to deserialize flowfile contents.");

            //One RSDF per flow file, iterate through flow files and add RSDFs to set
            Set<TransferredFile> transferredFiles = new HashSet<>();
            for (FlowFile flow : flowFiles) {
                final InputStream inputStream = session.read(flow);
                final String flowFileStr = new BufferedReader(new InputStreamReader(inputStream))
                        .lines().collect(Collectors.joining());
                inputStream.close();
                try {
                    final String name = Validate.notNull(flow.getAttribute("filename"),
                            "Empty flow file name, routing to Failure.");
                    Validate.isTrue(name.startsWith("rsdf") && name.endsWith(".json"),
                            "Unrecognized filename: " + name + ". Must start with rsdf- and end with .json");
                    final RawStationDataFrame rsdf = objectMapper.readValue(flowFileStr, RawStationDataFrame.class);
                    Validate.notNull(rsdf, "RawStationDataFrame deserialized to null");
                    TransferredFile<TransferredFileRawStationDataFrameMetadata> transferredFile = TransferredFile.createSent(
                            name,
                            "TREMENDOUS",
                            Instant.now(),
                            TransferredFileRawStationDataFrameMetadata
                                    .from(rsdf.getPayloadDataStartTime(),
                                            rsdf.getPayloadDataEndTime(),
                                            rsdf.getStationId(),
                                            rsdf.getChannelIds()));

                    transferredFiles.add(transferredFile);
                    getLogger().info("Added " + name + " to transferredFile set.");
                    session.remove(flow);
                } catch (Exception e) {
                    getLogger().error("Error creating transferred files: ", e);
                    getFlowedM8(flow, session, FAILURE, flowFileStr);
                }
            }

            if (!transferredFiles.isEmpty()) {
                //Create metadata for itself, to add to transferred file invoice
                long seqNum = SequenceNumberUtility.getAndUpdateSequenceNumber(seqNumFileName);
                String invoiceFileName = "inv-" + seqNum + ".inv";
                TransferredFile<TransferredFileInvoiceMetadata> invoiceMetaData = TransferredFile.createSent(
                        invoiceFileName,
                        "COLOSSAL",
                        Instant.now(),
                        TransferredFileInvoiceMetadata.from(seqNum));
                invoiceSendCountMap.put(invoiceMetaData, 0);
                transferredFiles.addAll(getInvoicesToSend());

                //Create the Invoice
                TransferredFileInvoice invoice = TransferredFileInvoice.from(seqNum, transferredFiles);

                //write the flowfile
                FlowFile flow = session.create();
                session.putAttribute(flow, "filename", invoiceFileName);
                getFlowedM8(flow, session, SUCCESS, invoice);
            }
            session.commit();
            getLogger().info("session committed");
        } catch (Exception e) {
            getLogger().error("Error in onTrigger: ", e);
        }
    }

    /*
        Writes a list to a flow file and sends it along
     */
    private void getFlowedM8(FlowFile flow, ProcessSession session, Relationship relationship, Object value) {
        getLogger().info("Writing flowfile");
        session.write(flow, outputStream -> objectMapper.writeValue(outputStream, value));
        getLogger().info("Flowfile written");
        session.transfer(flow, relationship);
        getLogger().info("Flowfile transferred");
    }

    //Manage the map of how many times each invoice was sent, return a set of invoices to send
    private Set<TransferredFile<TransferredFileInvoiceMetadata>> getInvoicesToSend(){
        Set<TransferredFile<TransferredFileInvoiceMetadata>> invoicesToSend = new HashSet<>();
        final Set<TransferredFile<TransferredFileInvoiceMetadata>> keysToRemove = new HashSet<>();
        for(Map.Entry<TransferredFile<TransferredFileInvoiceMetadata>, Integer> entry : invoiceSendCountMap.entrySet()){
            if(entry.getValue() < resendInvoice){
                invoicesToSend.add(entry.getKey());
                //increase send count
                entry.setValue(entry.getValue()+1);
            }
            else{
                keysToRemove.add(entry.getKey());
            }
        }
        keysToRemove.forEach(invoiceSendCountMap::remove);
        return invoicesToSend;
    }

    private void readConfigurationProperties(final ProcessContext context) throws IOException {
        this.flowFileIntake = Objects.requireNonNull(
                context.getProperty(FLOW_FILE_INTAKE_PROPERTY),
                "Flowfile intake cannot be null")
                .asInteger();
        this.resendInvoice = Objects.requireNonNull(
                context.getProperty(RESEND_INVOICE_PROPERTY),
                "Resend invoice property cannot be null")
                .asInteger();
        this.seqNumFileName = Objects.requireNonNull(
                context.getProperty(SEQUENCE_NUMBER_FILE),
                "Sequence Number File property cannot be null")
                .toString();
    }
}
