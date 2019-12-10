package gms.processors.dataacquisition.transferaudit;


import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationAndChannelId;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFileInvoice;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.TransferredFileRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.jpa.TransferredFileRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.utilities.transferauditor.TransferAuditorUtility;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

public class TransferredFileAuditorProcessor extends AbstractProcessor {

  public static final Relationship SUCCESS = new Relationship.Builder()
      .name("success")
      .description("Successful parsing and processing of the file")
      .build();
  public static final Relationship FAILURE = new Relationship.Builder()
      .name("failure")
      .description("Failed to parse or process the file")
      .build();

  // Sets the number of flow files to read per trigger
  public static final PropertyDescriptor FLOW_FILE_INTAKE_PROPERTY = new PropertyDescriptor
      .Builder().name("flow-file-intake")
      .displayName("Flow File Intake")
      .description("Sets the number of flow files to get per each onTrigger")
      .required(false)
      .defaultValue("10")
      .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
      .build();

  private int flowFileIntake = 10;

  public static final PropertyDescriptor PERSISTENCE_URL_PROPERTY = new PropertyDescriptor
      .Builder().name("persistence-url")
      .displayName("Persistence URL (to database)")
      .description("Sets the URL of the database")
      .required(false)
      .defaultValue("jdbc:postgresql://postgresql-stationreceiver:5432/xmp_metadata")
      .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
      .build();

  private String persistenceUrl = "jdbc:postgresql://postgresql-stationreceiver:5432/xmp_metadata";

  private final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private TransferAuditorUtility auditor;

  private TransferredFileRepositoryInterface tfRepo;

  private boolean auditorInitialized = false;

  private Set<Relationship> relationships;

  private List<PropertyDescriptor> descriptors;

  @Override
  protected void init(final ProcessorInitializationContext context) {
    getLogger().info("init");
    final Set<Relationship> relationships = new HashSet<>();
    relationships.add(SUCCESS);
    relationships.add(FAILURE);
    this.relationships = Collections.unmodifiableSet(relationships);

    final List<PropertyDescriptor> descriptors = new ArrayList<>();
    descriptors.add(FLOW_FILE_INTAKE_PROPERTY);
    descriptors.add(PERSISTENCE_URL_PROPERTY);
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
  public void onScheduled(final ProcessContext context) {
    getLogger().info("onScheduled");
    readConfigurationProperties(context);
  }

  @OnStopped
  public void onStopped() {
    getLogger().info("onStopped");
    closeAuditor();
  }

  @Override
  public void onTrigger(final ProcessContext context, final ProcessSession session)
      throws ProcessException {

    if (!this.auditorInitialized) {
      this.auditor = getAuditor();
      this.auditorInitialized = true;
    }

    // read some flow files
    final List<FlowFile> flowFiles = session.get(flowFileIntake);
    if (flowFiles == null || flowFiles.isEmpty()) {
      getLogger().info("Flowfiles empty or null: " + flowFiles);
      return;
    }
    final Set<RawStationDataFrame> frames = new HashSet<>();
    for (FlowFile flow : flowFiles) {
        // try and determine the filename
        final String filename = flow.getAttribute("filename");
        if (filename == null || filename.length() == 0) {
          getLogger().error("Filename null or empty " + filename);
          session.transfer(flow, FAILURE);
        } else {
          final Instant receptionTime = Instant.now();
          try {
            // check for each kind of known file content type
            // frame: give to auditor, add to frames to flow out, remove from input flow
            if (filename.toLowerCase().trim().contains("rsdf")) {
              final RawStationDataFrame frame = readFlowAs(
                  session, flow, RawStationDataFrame.class);
              if (frame == null) {
                fileDeserializedAsNull(session, flow, filename);
              } else {
                this.auditor.receive(frame, filename, receptionTime);
                frames.add(frame);
                session.remove(flow);
              }
              // invoice: give to auditor, remove from input flow
            } else if (filename.toLowerCase().trim().contains("inv")) {
              final TransferredFileInvoice invoice = readFlowAs(
                  session, flow, TransferredFileInvoice.class);
              if (invoice == null) {
                fileDeserializedAsNull(session, flow, filename);
              } else {
                this.auditor.receive(invoice, filename, receptionTime);
                session.remove(flow);  // do not pass along when successful
              }
              // throw exception on unknown type, this is caught and flow sent to FAILURE
            } else {
              throw new RuntimeException("Could not determine file type from name: " + filename);
            }
          } catch(Exception e) {
            getLogger().error("Error processing file " + filename, e);
            session.transfer(flow, FAILURE);
          }
        }
    }
    // if frames were read, flow them out to SUCCESS
    if (!frames.isEmpty()) {
      final FlowFile outputFlow = session.create();
      session.write(outputFlow, outputStream -> objectMapper.writeValue(outputStream, frames));
      session.transfer(outputFlow, SUCCESS);
    }
    session.commit();
  }

  // helper method for reading a flowfile into a specific type
  private <T> T readFlowAs(ProcessSession session, FlowFile flow, Class<T> type) throws IOException {
    final InputStream input = session.read(flow);
    final T t = objectMapper.readValue(input, type);
    input.close();
    return t;
  }

  // helper method for logic when contents of a file come back as null object
  private void fileDeserializedAsNull(ProcessSession session, FlowFile flow, String filename) {
    getLogger().error("Deserialized file " + filename + " as null object");
    session.transfer(flow, FAILURE);
  }

  // read config for this processor
  private void readConfigurationProperties(final ProcessContext context) {
    final PropertyValue flowIntakeProp = context.getProperty(FLOW_FILE_INTAKE_PROPERTY);
    if (flowIntakeProp != null) {
      this.flowFileIntake = flowIntakeProp.asInteger();
    }
    final PropertyValue persistenceUrlProp = context.getProperty(PERSISTENCE_URL_PROPERTY);
    if (persistenceUrlProp != null) {
      this.persistenceUrl = persistenceUrlProp.getValue();
    }
  }

  protected TransferAuditorUtility getAuditor() {
    final Map<String, String> hibernateProps = new HashMap<>();
    hibernateProps.put("hibernate.connection.url", this.persistenceUrl);
    this.tfRepo = new TransferredFileRepositoryJpa(
        CoiEntityManagerFactory.create(hibernateProps));
    Runtime.getRuntime().addShutdownHook(new Thread(() -> this.tfRepo.close()));
    return new TransferAuditorUtility(this.tfRepo);
  }

  private void closeAuditor() {
    if (this.auditorInitialized) {
      if (this.tfRepo != null) {
        this.tfRepo.close();
        this.tfRepo = null;
      }
      this.auditor = null;
      this.auditorInitialized = false;
    }
  }
}