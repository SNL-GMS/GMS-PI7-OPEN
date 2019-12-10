package gms.processors.dataacquisition.ims2.rawframecreator;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.ims20.receiver.Ims20RawStationDataFrameUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationAndChannelId;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisition.configuration.StationDataAcquisitionGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Ims2RawFrameCreatorProcessor extends AbstractProcessor {

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
          "Mapping between IMS2.0 name to StationAndChannelId object, as JSON string")
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

      /**
       * TODO - note for future reference - delete when implemented with NiFi controller service
       * Parameter Ims20StationDataAcquisitionGroup is used to verify that the station, provider
       * address, and receiver address correspond to expectations - current guidance from archi team is
       * to hard code for now, but eventually use the NiFi controller service * (which loads config)
       * later on. If data fails this authentication, then log failure and do not produce RSDF. See
       * Station Data Acquisition doc in Feature guidance
       */
      // Note: only AcquisitionProtocol and idsByReceivedName are used here -
      // List.of(), providerIpAddress, and providerPort are all fake because they cannot be empty or null
      final Instant now = Instant.now();
      final StationDataAcquisitionGroup sdag = StationDataAcquisitionGroup.create(
          List.of("FakeStationName"), AcquisitionProtocol.IMS_WAVEFORM,
          "https://ops-msgsys.ctbto.org/nms_user_services/xmlrpc/",
          9000,
          now, now, idsByReceivedName, true, "");

      JsonNode jsonRoot = objectMapper.readTree(flowFileStr);
      String imsWid2String = jsonRoot.get("ims2-response").asText();
      String receptionDateTimeString = jsonRoot.get("reception-time").asText();
      // reception-time is received as "yyyy-mm-dd xx:xx:xx.xxxxx"
      // need to separate date and time, and then create a UTC string with them
      String[] splitStr = receptionDateTimeString.trim().split("\\s+");
      String receptionDateString = splitStr[0];
      String receptionTimeString = splitStr[1];
      String receptionDateTimeStringUtc = receptionDateString + "T" + receptionTimeString + "Z";
      Instant receptionTime = Instant.parse(receptionDateTimeStringUtc);
      byte[] imsWid2Bytes = imsWid2String.getBytes();

      try {
        final RawStationDataFrame rsdf = Ims20RawStationDataFrameUtility
            .parseAcquiredStationDataPacket(
                imsWid2Bytes, receptionTime, sdag);
        getFlowedM8(session.create(inputFlow), session, SUCCESS, rsdf,
            String.format("rsdf-sta-%s-id-%s.json", rsdf.getStationId(), rsdf.getId()));
        getLogger().info("Successful conversion to RawStationDataFrame");
      } catch (Exception e) {
        getLogger().error("Conversion to RawStationDataFrame failed: ", e);
        getFlowedM8(session.create(inputFlow), session, FAILURE, imsWid2Bytes, "");
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
}