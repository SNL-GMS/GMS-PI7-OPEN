package gms.processors.dataacquisition.seedlink;


import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.seedlink.receiver.SeedlinkConnectionUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.ReceivedStationDataPacket;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationDataAcquisitionGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.annotation.lifecycle.OnUnscheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

public class SeedlinkConnectionManagerProcessor extends AbstractProcessor {

  public static final Relationship SUCCESS = new Relationship.Builder()
      .name("success")
      .description("Successful receipt of data")
      .build();

  // configures how many hours back the request should be for.  To go into effect,
  // the processor will close the connection and re-open it.
  public static final PropertyDescriptor REQUEST_HOURS_BACK_PROPERTY = new PropertyDescriptor
      .Builder().name("request-hours-back")
      .displayName("Request hours back")
      .description("Number of hours to request data going back for")
      .required(false)
      .defaultValue("0")
      .addValidator(StandardValidators.NON_NEGATIVE_INTEGER_VALIDATOR)
      .build();

  private int requestHoursBack = 0;

  // hostname of the seedlink data provider to connect to
  public static final PropertyDescriptor PROVIDER_HOSTNAME_PROPERTY = new PropertyDescriptor
      .Builder().name("provider-hostname")
      .displayName("Seedlink data provider hostname")
      .description("Hostname of the Seedlink data provider")
      .required(false)
      .defaultValue("rtserve.iris.washington.edu")
      .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
      .build();

  private String providerHostname;

  // port of the seedlink data provider to connect to
  public static final PropertyDescriptor PROVIDER_PORT_PROPERTY = new PropertyDescriptor
      .Builder().name("provider-port")
      .displayName("Seedlink data provider network port")
      .description("Port number of the Seedlink data provider")
      .required(false)
      .defaultValue("18000")
      .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
      .build();

  private int providerPort;

  // maximum batch size of packets written to each flow file.  Defaults to 10
  public static final PropertyDescriptor PACKET_BATCH_SIZE_PROPERTY = new PropertyDescriptor
      .Builder().name("packet-batch-size")
      .displayName("Packet batch size")
      .description("Maximum number of packets to include in a batch written to each flow file")
      .required(false)
      .defaultValue("10")
      .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR)
      .build();

  private int packetBatchSize = 10;

  public static final PropertyDescriptor REQUEST_STRINGS_PROPERTY = new PropertyDescriptor
      .Builder().name("request-strings")
      .displayName("Seedlink request strings")
      .description("Seedlink commands to use in the initial handshake")
      .required(false)
      .defaultValue(
          "[\"STATION ULN IU\",\"SELECT ?????.D\",\"STATION PMG IU\",\"SELECT ?????.D\"]\n")
      .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
      .build();

  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private List<String> requestStrings;

  private Set<Relationship> relationships;

  private List<PropertyDescriptor> descriptors;

  private final BlockingQueue<ReceivedStationDataPacket> packetQueue = new LinkedBlockingQueue<>();

  private SeedlinkConnectionUtility seedlinkConnection;

  private boolean connected = false;

  @Override
  protected void init(final ProcessorInitializationContext context) {
    getLogger().info("init");
    final Set<Relationship> relationships = new HashSet<>();
    relationships.add(SUCCESS);
    this.relationships = Collections.unmodifiableSet(relationships);

    final List<PropertyDescriptor> descriptors = new ArrayList<>();
    descriptors.add(REQUEST_HOURS_BACK_PROPERTY);
    descriptors.add(PROVIDER_HOSTNAME_PROPERTY);
    descriptors.add(PROVIDER_PORT_PROPERTY);
    descriptors.add(PACKET_BATCH_SIZE_PROPERTY);
    descriptors.add(REQUEST_STRINGS_PROPERTY);
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

  @OnUnscheduled
  public void onUnscheduled() {
    getLogger().info("onUnscheduled");
    closeConnection();
  }

  @OnStopped
  public void onStopped() {
    getLogger().info("onStopped");
    closeConnection();
  }

  @OnScheduled
  public void onScheduled(final ProcessContext context) throws Exception {
    getLogger().info("onScheduled");
    readConfigurationProperties(context);
  }

  @Override
  public void onTrigger(final ProcessContext context, final ProcessSession session)
      throws ProcessException {

    openConnection();

    final Collection<ReceivedStationDataPacket> packets = new ArrayList<>(packetBatchSize);
    try {
      getLogger().info("Draining some packets from queue");
      packetQueue.drainTo(packets, packetBatchSize);
    } catch (Exception e) {
      getLogger().error("Error polling for packet", e);
    }

    if (packets.isEmpty()) {
      getLogger().info("Got no packets; queue size = " + packetQueue.size());
      context.yield();
      return;
    }

    getLogger().info("Creating flow file of " + packets.size() + " packets");
    final FlowFile flow = session.create();
    getLogger().info("Created flow file");
    session.write(flow, outputStream -> objectMapper.writeValue(outputStream, packets));
    getLogger().info("Wrote a packet to flow file, committing session");
    session.transfer(flow, SUCCESS);
    getLogger().info("Flow file transferred");
    session.commit();
    getLogger().info("Session committed");
  }

  private void handlePacket(ReceivedStationDataPacket p) {
    getLogger().info("Got packet with sequence number: " + p.getSequenceNumber());
    try {
      this.packetQueue.offer(p, 500, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      getLogger().error("Could not offer packet to queue", e);
    }
  }

  private void closeConnection() {
    if (this.seedlinkConnection != null) {
      getLogger().info("closeConnection: interrupt seedlink listener thread");
      this.seedlinkConnection.stop();
      this.seedlinkConnection.waitUntilThreadStops();
      this.seedlinkConnection = null;
    }
    this.connected = false;
  }

  private void openConnection() {
    if (!this.connected) {
      try {
        closeConnection();
        final StationDataAcquisitionGroup sdag = StationDataAcquisitionGroup.create(
            this.requestStrings, AcquisitionProtocol.SEEDLINK,
            this.providerHostname, this.providerPort,
            Instant.EPOCH, Instant.EPOCH, new HashMap<>(), true, "");
        getLogger().info("Initializing seedlink connection using acquisition group " + sdag);
        // must wrap in Thread because need to set as daemon
        this.seedlinkConnection = new SeedlinkConnectionUtility(sdag,
            Instant.now().minus(this.requestHoursBack, ChronoUnit.HOURS),
            this::handlePacket);
        this.seedlinkConnection.start();
        this.connected = true;
        getLogger().info("Started seedlink connection utility");
      } catch (Exception e) {
        getLogger().error("Error starting seedlink connection thread", e);
        this.closeConnection();
      }
    }
  }

  private void readConfigurationProperties(final ProcessContext context) throws IOException {
    this.requestHoursBack = Objects.requireNonNull(
        context.getProperty(REQUEST_HOURS_BACK_PROPERTY),
        "Request hours back cannot be null")
        .asInteger();
    this.providerHostname = Objects.requireNonNull(
        context.getProperty(PROVIDER_HOSTNAME_PROPERTY),
        "Provider hostname cannot be null")
        .getValue();
    this.providerPort = Objects.requireNonNull(
        context.getProperty(PROVIDER_PORT_PROPERTY),
        "Provider port cannot be null")
        .asInteger();
    final String requestStringsValue = Objects.requireNonNull(
        context.getProperty(REQUEST_STRINGS_PROPERTY),
        "Request strings cannot be null")
        .getValue();
    this.requestStrings = objectMapper.readValue(requestStringsValue,
        objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class));
    this.packetBatchSize = Objects.requireNonNull(
        context.getProperty(PACKET_BATCH_SIZE_PROPERTY),
        "Packet batch size cannot be null")
        .asInteger();
  }
}
