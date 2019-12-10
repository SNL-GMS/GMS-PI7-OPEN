package gms.processors.dataacquisition.seedlink.rawframecreator;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.seedlink.receiver.MiniSeedRawStationDataFrameUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.ReceivedStationDataPacket;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationAndChannelId;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
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
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

public class SeedlinkRawFrameCreatorProcessor extends AbstractProcessor {

  public static final Relationship SUCCESS = new Relationship.Builder()
      .name("success")
      .description("Successful creation of RawStationDataFrame")
      .build();
  public static final Relationship FAILURE = new Relationship.Builder()
      .name("failure")
      .description("Creation of RawStationDataFrame failed")
      .build();

  public static final PropertyDescriptor IDS_BY_RECEIVED_NAME_PROPERTY = new PropertyDescriptor
      .Builder().name("ids-by-received-name")
      .displayName("Received names to ID's")
      .description(
          "Mapping between Seedlink name (re-arrangement of SNCL to N/S/C/L) to StationAndChannelId object, as JSON string")
      .required(true)
      .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
      .build();

  private final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private final JavaType IDS_BY_RECEIVED_NAME_JAVATYPE = objectMapper.getTypeFactory()
      .constructMapType(HashMap.class, String.class, StationAndChannelId.class);

  private Set<Relationship> relationships;

  private List<PropertyDescriptor> descriptors;

  private Map<String, StationAndChannelId> idsByReceivedName;

  @Override
  protected void init(final ProcessorInitializationContext context) {
    getLogger().info("init");
    final Set<Relationship> relationships = new HashSet<>();
    relationships.add(SUCCESS);
    relationships.add(FAILURE);
    this.relationships = Collections.unmodifiableSet(relationships);

    final List<PropertyDescriptor> descriptors = new ArrayList<>();
    descriptors.add(IDS_BY_RECEIVED_NAME_PROPERTY);
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
    this.idsByReceivedName = readIdsByReceivedName(context);
  }

  @OnStopped
  public void onStopped() {
    getLogger().info("onStopped");
  }

  @Override
  public List<ValidationResult> customValidate(ValidationContext context) {
    final List<ValidationResult> result = new ArrayList<>();
    try {
      readIdsByReceivedName(context.getProperties().get(IDS_BY_RECEIVED_NAME_PROPERTY));
    } catch (IOException e) {
      result.add(new ValidationResult.Builder().explanation(e.getMessage()).valid(false).build());
    }
    return result;
  }

  @Override
  public void onTrigger(final ProcessContext context, final ProcessSession session)
      throws ProcessException {
    getLogger().info("onTrigger");

    try {
      getLogger().info("Getting flow file");
      final FlowFile inputFlow = session.get();
      if (inputFlow == null) {
        return;
      }
      getLogger().info("Got flow file");
      getLogger().info("Attempting to deserialize flowfile contents.");
      final InputStream inputStream = session.read(inputFlow);
      final String flowFileStr = new BufferedReader(new InputStreamReader(inputStream))
          .lines().collect(Collectors.joining());
      inputStream.close();
      final ReceivedStationDataPacket[] packets = objectMapper.
          readValue(flowFileStr, ReceivedStationDataPacket[].class);
      getLogger().info("Deserialized flow file into array of " + packets.length + " packets");

      //Build up a list of successfully parsed frames, failed packets
      for (ReceivedStationDataPacket packet : packets) {
        try {
          final RawStationDataFrame rsdf = getPacketParser().parse(
                  packet.getPacket(),
                  packet.getReceptionTime(),
                  idsByReceivedName);
          getFlowedM8(session.create(inputFlow), session, SUCCESS, rsdf,
                  String.format("rsdf-sta-%s-id-%s.json",rsdf.getStationId(), rsdf.getId()));
          getLogger().info("Successful conversion to RawStationDataFrame");
        } catch (Exception e) {
          getLogger().error("Conversion to RawStationDataFrame failed: ", e);
          getFlowedM8(session.create(inputFlow), session, FAILURE, packet, "");
        }
      }
      session.remove(inputFlow);
      session.commit();
      getLogger().info("session committed");
    } catch (Exception e) {
      getLogger().error("Error in onTrigger: ", e);
      session.rollback();
    }
  }

  /*
      Writes a list to a flow file and sends it along
   */
  private void getFlowedM8(FlowFile flow, ProcessSession session, Relationship relationship,
      Object value, String filename) {
    getLogger().info("Writing flowfile");
    session.putAttribute(flow, "filename", filename);
    session.write(flow, outputStream -> objectMapper.writeValue(outputStream, value));
    getLogger().info("Flowfile written");
    session.transfer(flow, relationship);
    getLogger().info("Flowfile transferred");
  }

  private Map<String, StationAndChannelId> readIdsByReceivedName(final ProcessContext context)
      throws IOException {
    return readIdsByReceivedName(context.getProperty(IDS_BY_RECEIVED_NAME_PROPERTY).getValue());
  }

  private Map<String, StationAndChannelId> readIdsByReceivedName(String s) throws IOException {
    return validateIdsByReceivedName(this.objectMapper.readValue(s, IDS_BY_RECEIVED_NAME_JAVATYPE));
  }

  private Map<String, StationAndChannelId> validateIdsByReceivedName(
      Map<String, StationAndChannelId> m) {
    Validate.notEmpty(m, "idsByReceivedName cannot be null or empty");
    return m;
  }

  @FunctionalInterface
  protected interface PacketParser {
    RawStationDataFrame parse(byte[] rawPacket, Instant receptionTime,
        Map<String, StationAndChannelId> idsByReceivedName) throws Exception;
  }

  protected PacketParser getPacketParser() {
    return MiniSeedRawStationDataFrameUtility::parseAcquiredStationDataPacket;
  }
}