/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * work for additional information regarding copyright ownership.
 * The ASF licenses file to You under the Apache License, Version 2.0
 * (the "License"); you may not use file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gms.processors.invokeinterval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

/*
  Expected attributes in output flow file
*/
@WritesAttributes({
    @WritesAttribute(attribute = "station-name", description = "Name of the station to use in the invoke."),
    @WritesAttribute(attribute = "station-type", description = "Type of the station to use in the invoke."),
    @WritesAttribute(attribute = "station-id", description = "UUID of the station used in the invoke."),
    @WritesAttribute(attribute = "channel-ids", description = "List of comma-separated channel ids to run processing on. The ids should be in UUID format."),
    @WritesAttribute(attribute = "start-time", description = "The start of the time range to run processing on. The value should be a date string in ISO-8601 instant format."),
    @WritesAttribute(attribute = "end-time", description = "The end of the time range to run processing on. The value should be a date string in ISO-8601 instant format."),
    @WritesAttribute(attribute = "initialization-time", description = "The time the interval was initialized. The value should be a date string in ISO-8601 instant format.")})
@Tags({"invoke", "interval"})
@CapabilityDescription("Provide a description")
public class InvokeIntervalProcessor extends AbstractProcessor {

  /*
    STATION_NAME will be used to retrieve the UUID for the station and the list
    of UUIDs for the channels provided by the station.
   */
  public static final PropertyDescriptor STATION_NAME = new PropertyDescriptor
      .Builder().name("STATION_NAME")
      .displayName("Station Name")
      .description("Name of the station to initialize an interval for.")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  /*
    CHANNEL_NAME will be REGEX expression used to check if the input station type is to be
    processed.
   */
  public static final PropertyDescriptor CHANNEL_NAME = new PropertyDescriptor
      .Builder().name("CHANNEL_NAME")
      .displayName("Channel Name")
      .description("Name of channels to filter down to in the FlowFile's channel ids. Accepts regular expressions. Defaults to everything (i.e. '.*').")
      .required(true)
      .defaultValue(".*")
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .addValidator(StandardValidators.REGULAR_EXPRESSION_VALIDATOR)
      .build();

  /*
    OSD_GATEWAY_URL will be used to retrieve data from the OSD API.
   */
  public static final PropertyDescriptor OSD_GATEWAY_URL = new PropertyDescriptor
      .Builder().name("OSD_GATEWAY_URL")
      .displayName("OSD Gateway URL")
      .description("URL of the gateway to the object storage distribution service.")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .build();

  /*
    INTERVAL_DELAY will be used in calculating the interval length in the
    processing sequence. This is the time to wait between interval
    initializations.
   */
  public static final PropertyDescriptor INTERVAL_DELAY = new PropertyDescriptor
      .Builder().name("INTERVAL_DELAY")
      .displayName("Internal delay interval")
      .description(
          "Time interval in seconds to wait before triggering, in ISO 8601 duration format")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      .addValidator(new ISO8601Validator())
      .build();

  /*
    INTERVAL_LENGTH will be used in calculatinf the interval length in the
    processing sequence. This is the amount of time to request from the start
    time.
   */
  public static final PropertyDescriptor INTERVAL_LENGTH = new PropertyDescriptor
      .Builder().name("INTERVAL_LENGTH")
      .displayName("Length of retrieval interval")
      .description(
          "Time interval in seconds to wait before triggering, in ISO 8601 duration format")
      .required(true)
      .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
      //      .addValidator(ISO_8601_VALIDATOR)
      .addValidator(new ISO8601Validator())
      .build();

  public static final Relationship SUCCESS = new Relationship.Builder()
      .name("success")
      .description("Invoke interval succeeded")
      .build();

  private List<PropertyDescriptor> descriptors;

  private Set<Relationship> relationships;

  private ObjectMapper objectMapper;

  private String sourceUrl;
  private String stationName;
  private String stationType;
  private String stationId;
  private String channelIds;

  @Override
  protected void init(final ProcessorInitializationContext context) {
    objectMapper = new ObjectMapper();

    // Set up descriptors (configuration parameters)
    final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
    descriptors.add(STATION_NAME);
    descriptors.add(CHANNEL_NAME);
    descriptors.add(OSD_GATEWAY_URL);
    descriptors.add(INTERVAL_DELAY);
    descriptors.add(INTERVAL_LENGTH);
    this.descriptors = Collections.unmodifiableList(descriptors);

    // Set up relationships (transitions)
    final Set<Relationship> relationships = new HashSet<Relationship>();
    relationships.add(SUCCESS);

    this.relationships = Collections.unmodifiableSet(relationships);
  }

  @Override
  public Set<Relationship> getRelationships() {
    return relationships;
  }

  @Override
  public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
    return descriptors;
  }

  @OnScheduled
  public void onScheduled(final ProcessContext context) {
    /*
      Connect to OSD and retrieve needed values based on station name.

      Values being retrieved here:
      - station-id
      - channel-ids

      I expect the flow will be roughly:
      for each parameter:
        response , errors := perform GET request to OSD gateway
        if errors:
          log failure
          pass to failed state
        parameter := extract from response
        errors := validate parameter
        if errors:
          log failure
          pass to failed state
        self.parameter := parameter
    */
    getLogger().debug("Processor enabled, retrieving station data from OSD");

    getLogger().debug("Clearing cached values");
    stationName = null;
    stationType = null;
    sourceUrl = null;
    stationId = null;
    channelIds = null;

    /*
      Depending on the configuration, might need to include the actual API
      endpoint for the OSD gateway, which is:
      '/mechanisms/object-storage-distribution/station-reference'
     */

    stationName = context.getProperty(STATION_NAME).getValue();
    final Predicate<String> channelNameMatches = Pattern
        .compile(context.getProperty(CHANNEL_NAME).getValue()).asPredicate();
    final Predicate<JsonNode> channelNodeMatches = cn -> channelNameMatches
        .test(cn.get("name").textValue());

    sourceUrl = context.getProperty(OSD_GATEWAY_URL).getValue()
        + "/" + stationName;

    getLogger().debug("Built endpoint URL: " + sourceUrl);

    try {
      HttpResponse<com.mashape.unirest.http.JsonNode> response = Unirest.get(sourceUrl).asJson();
      if (response.getStatus() == 200) {
        // Parse into a JsonNode to retrieve the fields we're interested in
        JsonNode node = objectMapper.readTree(response.getBody().toString());

        getLogger().debug("Got back from OSD: " + node.toString());

        stationId = node.get("id").textValue();
        getLogger().debug("stationId: " + stationId);

        stationType = node.get("stationType").textValue();
        getLogger().debug("stationType: " + stationType);

        channelIds = StreamSupport.stream(node.get("sites").spliterator(), false)
            .flatMap(
                siteNode -> StreamSupport.stream(siteNode.get("channels").spliterator(), false))
            .filter(channelNodeMatches)
            .map(channelNode -> channelNode.get("id").textValue())
            .collect(Collectors.joining(","));

        getLogger().debug("Collected channel IDs: " + channelIds);
      } else {
        getLogger()
            .error("Error retrieving station data with response: " + response.getStatusText());
      }
    } catch (UnirestException e) {
      getLogger().error("Failed to retrieve station data with UnirestException.", e);
    } catch (IOException e) {
      getLogger().error("Failed to retrieve station data with IOException.", e);
    }
  }

  @Override
  public void onTrigger(final ProcessContext context, final ProcessSession session)
      throws ProcessException {
    getLogger().debug("Processor triggered.");
    getLogger().debug("Loading interval parameters from PropertyDescriptors.");

    if (Stream.of(sourceUrl, stationId, stationType, channelIds).anyMatch(Objects::isNull)) {
      getLogger().error("Error in initializing cache data, yielding processing...");
      context.yield();
      return;
    }

    if (channelIds.isEmpty()) {
      getLogger().error("No channel IDs found for station " + stationId + ", yielding processing...");
      context.yield();
      return;
    }

    // Get delay interval from parameters
    Duration intervalDelay = Duration
        .parse(context.getProperty(INTERVAL_DELAY.getName()).getValue());
    Duration intervalLength = Duration
        .parse(context.getProperty(INTERVAL_LENGTH.getName()).getValue());

    getLogger().debug("Calculating start and end times for interval.");
    // Go back intervalDelay into the past
    Instant startTime = Instant.now().minus(intervalDelay);

    // Calculate the requested interval from startTime
    Instant endTime = startTime.plus(intervalLength);

    // Create HashMap of parameters for the FlowFile and insert them
    HashMap<String, String> flowFileAttributes = new HashMap<>();
    flowFileAttributes.put("station-name", stationName);
    flowFileAttributes.put("station-id", stationId);
    flowFileAttributes.put("station-type", stationType);
    flowFileAttributes.put("channel-ids", channelIds);
    flowFileAttributes.put("start-time", startTime.toString());
    flowFileAttributes.put("end-time", endTime.toString());
    flowFileAttributes.put("initialization-time", Instant.now().toString());

    FlowFile flowFile = session.putAllAttributes(session.create(), flowFileAttributes);
    getLogger()
            .debug("Created new FlowFile with attributes: \n" + flowFile.getAttributes().toString());

      /*
        Try to transfer the newly created FlowFile to the SUCCESS relationship,
        and transition to the FAILRE relationship if that doesn't go so well.
      */
    getLogger().debug("Transferring FlowFile to SUCCESS relationship");
    session.transfer(flowFile, SUCCESS);

    getLogger().debug("Generating Provenance Event");
    session.getProvenanceReporter().create(flowFile);
    getLogger().debug("Committing session");
    session.commit();
  }

  @OnEnabled
  public void onEnabled(final ProcessContext context) {
  }
}
