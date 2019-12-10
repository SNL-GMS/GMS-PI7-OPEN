package gms.processors.dataacquisition.ims2.requestgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

public class Ims2RequestGenerator extends AbstractProcessor {

  public static final Relationship SUCCESS = new Relationship.Builder()
      .name("success")
      .description("Successful creation of Request File")
      .build();
  public static final Relationship FAILURE = new Relationship.Builder()
      .name("failure")
      .description("Creation of Request File failed")
      .build();

  // configures the station codes to populate a request with
  public static final PropertyDescriptor STATIONS_TO_REQUEST_PROPERTY = new PropertyDescriptor
      .Builder().name("stations-to-request-name")
      .displayName("Stations to Request")
      .description(
          "A comma delimited list of station codes to request. All channels are included.")
      .required(true)
      .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
      .build();

  private List<String> stationsToRequest;

  //Sets the start time of each generated request to this many minutes from the current time.
  public static final PropertyDescriptor REQUEST_FROM_MINUTES_AGO = new PropertyDescriptor
      .Builder().name("request-from-minutes-ago")
      .displayName("Request From Minutes Ago")
      .description(
          "Sets the start time of each generated request to this many minutes from the current time, rounded down.")
      .required(false)
      .defaultValue("6")
      .addValidator(StandardValidators.NON_NEGATIVE_INTEGER_VALIDATOR)
      .build();

  private int requestFromMinutesAgo = 6;

  //Sets the end time of each generated request to this many minutes from the current time, rounded down.
  public static final PropertyDescriptor REQUEST_TO_MINUTES_AGO = new PropertyDescriptor
      .Builder().name("request-to-minutes-ago")
      .displayName("Request To Minutes Ago")
      .description(
          "Sets the end time of each generated request to this many minutes from the current time, rounded down."
              + " Set to 0 for present minute but be aware that the full minute worth or data may not yet be available.")
      .required(false)
      .defaultValue("1")
      .addValidator(StandardValidators.NON_NEGATIVE_INTEGER_VALIDATOR)
      .build();

  private int requestToMinutesAgo = 1;

  private final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

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
    descriptors.add(STATIONS_TO_REQUEST_PROPERTY);
    descriptors.add(REQUEST_FROM_MINUTES_AGO);
    descriptors.add(REQUEST_TO_MINUTES_AGO);
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

  @OnStopped
  public void onStopped() {
    getLogger().info("onStopped");
  }

  //Gets triggered by the Run Schedule property in the Scheduling tab of the NiFi UI
  @Override
  public void onTrigger(final ProcessContext context, final ProcessSession session)
      throws ProcessException {
    getLogger().info("onTrigger");

    //Truncate to the minute (round down)
    Instant base = Instant.now();
    Instant startTime = base.minusSeconds(60 * requestFromMinutesAgo)
        .truncatedTo(ChronoUnit.MINUTES);
    Instant endTime = base.minusSeconds(60 * requestToMinutesAgo).truncatedTo(ChronoUnit.MINUTES);

    //Generate and flow an ImsRequestInfo object, each containing subset of all stations requested
    for (String station : stationsToRequest) {
      ImsRequestInfo requestInfo = new ImsRequestInfo(station,
          startTime, endTime);
      getLogger().info(String.format("Flowing ImsRequestInfo for station: %s, time: $s to %s",
          requestInfo.getStation(), requestInfo.getTimeRange()));
      getFlowedM8(session, SUCCESS, requestInfo);
    }

    session.commit();
    getLogger().info("Session committed");
  }

  /*
      Writes a list to a flow file and sends it along
   */
  private void getFlowedM8(ProcessSession session, Relationship relationship, Object value) {
    getLogger().info("Creating flow file request");
    final FlowFile flow = session.create();
    getLogger().info("Created flow file");
    session.write(flow, outputStream -> objectMapper.writeValue(outputStream, value));
    getLogger().info("Wrote a packet to flow file, committing session");
    session.transfer(flow, relationship);
    getLogger().info("Flow file transferred");

  }

  private void readConfigurationProperties(final ProcessContext context) throws IOException {
    String stationsToRequestString = Objects.requireNonNull(
        context.getProperty(STATIONS_TO_REQUEST_PROPERTY),
        "Stations to request can not be empty.")
        .toString();
    this.stationsToRequest = Arrays.asList(stationsToRequestString.split("\\s*,\\s*"));

    //Get the request times
    int requestFromMinutesAgoFromProps = Objects.requireNonNull(
        context.getProperty(REQUEST_FROM_MINUTES_AGO),
        "Request hours back cannot be null")
        .asInteger();
    int requestToMinutesAgoFromProps = Objects.requireNonNull(
        context.getProperty(REQUEST_TO_MINUTES_AGO),
        "Request hours back cannot be null")
        .asInteger();
    //Make sure start time is before end time
    if (requestToMinutesAgoFromProps > requestFromMinutesAgoFromProps) {
      throw new IllegalArgumentException("Request to minutes ago property value must be less "
          + "than Request from minutes ago property value.");
    } else {
      this.requestFromMinutesAgo = requestFromMinutesAgoFromProps;
      this.requestToMinutesAgo = requestToMinutesAgoFromProps;
    }
  }
}