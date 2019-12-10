/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
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
package gms.processors.getavailability;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.MapType;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
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

@Tags({"example"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({
    @ReadsAttribute(attribute = "channel-ids", description = "CSV List of Channel UUIDs"),
    @ReadsAttribute(attribute = "start-time", description = "Start of interval"),
    @ReadsAttribute(attribute = "end-time", description = "End of interval")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})
public class GetAvailabilityProcessor extends AbstractProcessor {

  public static final PropertyDescriptor AVAILABILITY_URL = new PropertyDescriptor
      .Builder().name("AVAILABILITY_URL")
      .displayName("Availability URL")
      .description("URL for querying data availability")
      .required(true)
      .addValidator(StandardValidators.URL_VALIDATOR)
      .build();

  public static final PropertyDescriptor DATA_AVAILABILITY_THRESHOLD = new PropertyDescriptor
      .Builder().name("DATA_AVAILABILITY_THRESHOLD")
      .displayName("Data Availability Threshold")
      .description("Percentage Allowable Threshold for Availability of Data")
      .required(true)
      .addValidator(new DoublePercentageValidator())
      .build();

  public static final PropertyDescriptor TOTAL_AVAILABILITY_THRESHOLD = new PropertyDescriptor
      .Builder().name("TOTAL_AVAILABILITY_THRESHOLD")
      .displayName("Total Availability Threshold")
      .description(
          "Percentage Allowable Threshold for total Available Channels meeting Data Availability Threshold")
      .required(true)
      .addValidator(new DoublePercentageValidator())
      .build();

  public static final Relationship AVAILABLE = new Relationship.Builder()
      .name("AVAILABLE")
      .description("Data is available")
      .build();

  public static final Relationship UNAVAILABLE = new Relationship.Builder()
      .name("UNAVAILABLE")
      .description("Data is unavailable")
      .build();

  public static final Relationship FAILURE = new Relationship.Builder()
      .name("FAILURE")
      .description("Failure in retrieving availability")
      .build();

  private List<PropertyDescriptor> descriptors;
  private Set<Relationship> relationships;

  private ObjectMapper objectMapper;

  private MapType responseBodyType;

  @Override
  protected void init(final ProcessorInitializationContext context) {
    this.objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    this.responseBodyType = objectMapper.getTypeFactory().constructMapType(HashMap.class,
        UUID.class, Double.class);

    final List<PropertyDescriptor> descriptors = new ArrayList<>();
    descriptors.add(AVAILABILITY_URL);
    descriptors.add(DATA_AVAILABILITY_THRESHOLD);
    descriptors.add(TOTAL_AVAILABILITY_THRESHOLD);
    this.descriptors = Collections.unmodifiableList(descriptors);

    final Set<Relationship> relationships = new HashSet<>();
    relationships.add(AVAILABLE);
    relationships.add(UNAVAILABLE);
    relationships.add(FAILURE);
    this.relationships = Collections.unmodifiableSet(relationships);
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

  }

  @Override
  public void onTrigger(final ProcessContext context, final ProcessSession session)
      throws ProcessException {
    FlowFile flowFile = session.get();

    if (flowFile == null) {
      return;
    }

    //Retrieve configured properties
    String availabilitylUrl = context.getProperty(AVAILABILITY_URL).getValue();
    Double availabilityThreshold = context.getProperty(DATA_AVAILABILITY_THRESHOLD).asDouble();
    Double totalAvailabilityThreshold = context.getProperty(TOTAL_AVAILABILITY_THRESHOLD)
        .asDouble();

    //Retrieve flowfile attributes
    Set<UUID> channelIds = Arrays.stream(flowFile.getAttribute("channel-ids").split(","))
        .map(UUID::fromString)
        .collect(Collectors.toSet());
    Instant startTime = Instant.parse(flowFile.getAttribute("start-time"));
    Instant endTime = Instant.parse(flowFile.getAttribute("end-time"));

    getLogger().info("Nifi Processor: " + context.getName() + ":" + getIdentifier()
        + " retrieving availability at: " + availabilitylUrl);

    try {
      //make availability request, and log result in provenance
      HttpResponse<JsonNode> response = getAvailability(availabilitylUrl, channelIds, startTime,
          endTime);
      session.getProvenanceReporter().send(flowFile, availabilitylUrl,
          response.getStatus() + ":" + response.getStatusText() + ":" + response.getBody());

      if (response.getStatus() == 200) {
        //parse response into availability map and determine overall availability
        String body = response.getBody().toString();
        final Map<UUID, Double> availability;
        try {
          availability = objectMapper.readValue(body, responseBodyType);
          session.transfer(flowFile,
              isAvailable(availability, availabilityThreshold, totalAvailabilityThreshold)
                  ? AVAILABLE : UNAVAILABLE);
        } catch (IOException e) {
          getLogger().error("Error parsing response: " + body, e);
          session.transfer(flowFile, FAILURE);
        }
      } else {
        getLogger().error("Error status from remote process invocation: "
            + response.getStatus() + ":" + response.getStatusText() + ":" + response.getBody());
        session.transfer(flowFile, FAILURE);
      }
    } catch (ProcessException e) {
      getLogger().error("Error retrieving availability, processing cannot continue.", e);
      session.transfer(flowFile, FAILURE);
    }

    session.commit();
  }

  /**
   * Processes whether or not enough data is available, given the configured thresholds.
   * @param availability Map of channel id to availability precentage.
   * @param availabilityThreshold Threshold used to declare if enough data for a channel is available.
   * @param totalAvailabilityThreshold Threshold used to declare if enough channels are available.
   * @return Whether or not there is enough total data available.
   */
  private boolean isAvailable(Map<UUID, Double> availability, Double availabilityThreshold,
      Double totalAvailabilityThreshold) {

    double totalAvailable = availability.values().stream().mapToDouble(Double::doubleValue)
        .filter(a -> a >= availabilityThreshold)
        .count();

    return totalAvailable / availability.size() >= totalAvailabilityThreshold;
  }

  /**
   * Retrieves availability percentages for data from channels
   *
   * @param availabilityUrl Availability service url to get the availability information from.
   * @param channelIds Set of UUIDs representing channels to retrieve availability for.
   * @param startTime Start of time range to retrieve availability.
   * @param endTime End of time range to retrieve availability.
   * @return The response from post request, to be parsed into the availability map after status checks.
   */
  private HttpResponse<JsonNode> getAvailability(String availabilityUrl,
      Collection<UUID> channelIds,
      Instant startTime, Instant endTime) {

    Map<String, Object> body = new HashMap<>();
    body.put("start-time", startTime);
    body.put("end-time", endTime);
    body.put("channel-ids", channelIds);

    try {
      return Unirest.post(availabilityUrl)
          .header("Content-Type", "application/json")
          .header("Accepts", "application/json")
          .body(objectMapper.writeValueAsString(body))
          .asJson();
    } catch (UnirestException | JsonProcessingException e) {
      throw new ProcessException("Error invoking control service", e);
    }
  }
}
