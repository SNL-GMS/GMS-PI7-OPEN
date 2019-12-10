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
package gms.processors.invokeqc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.nifi.annotation.behavior.DynamicProperties;
import org.apache.nifi.annotation.behavior.DynamicProperty;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
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

@Tags({"invoke", "quality", "control", "qc"})
@CapabilityDescription("POSTS to a Control Service with the expected JSON body in order to"
    + "execute processing for a given time range and channel ids.")
@ReadsAttributes({
    @ReadsAttribute(attribute = "channel-ids", description = "List of comma-separated channel ids"
        + " to run processing on. The ids should be in UUID format."),
    @ReadsAttribute(attribute = "start-time", description = "The start of the time range to run"
        + " processing on. The value should be a date string in ISO-8601 instant format."),
    @ReadsAttribute(attribute = "end-time", description = "The end of the time range to run"
        + " processing on. The value should be a date string in ISO-8601 instant format.")})
@DynamicProperties({
    @DynamicProperty(name = "waveformQcPlugins", value = "(<String>/<semantic version number>)+",
        description = "Comma-separated list of plugin/versions"),
    @DynamicProperty(name = "minConsecutiveSampleDifferenceSpikeThreshold", value = "Scalar Double",
        description = "Minimum consecutive sample difference spike threshold"),
    @DynamicProperty(name = "rmsAmplitudeRatioThreshold", value = "Scalar Double",
        description = "RMS amplitude ratio threshold"),
    @DynamicProperty(name = "rmsLeadSampleDifferences", value = "Scalar Integer",
        description = "RMS lead sample differences"),
    @DynamicProperty(name = "rmsLagSampleDifferences", value = "Scalar Integer",
        description = "RMS lag sample differences")
})
public class InvokeQcProcessor extends AbstractProcessor {

  public static final PropertyDescriptor CONTROL_URL = new PropertyDescriptor
      .Builder().name("control-url")
      .displayName("Control URL")
      .description("URL for the control service")
      .required(true)
      .addValidator(StandardValidators.URL_VALIDATOR)
      .build();

  public static final Relationship SUCCESS = new Relationship.Builder()
      .name("success")
      .description("Successful execution of QC Control Service")
      .build();

  public static final Relationship FAILURE = new Relationship.Builder()
      .name("failure")
      .description("Unsuccessful execution of QC Control Service")
      .build();

  private List<PropertyDescriptor> descriptors;

  private Set<Relationship> relationships;

  /**
   * Cache of dynamic properties set during {@link #onScheduled(ProcessContext)} for quick access in
   * {@link #onTrigger(ProcessContext, ProcessSession)}
   */
  private volatile Map<String, String> propertyMap = new HashMap<>();

  @Override
  protected void init(final ProcessorInitializationContext context) {
    initializeUnirest();

    final List<PropertyDescriptor> descriptors = new ArrayList<>();
    descriptors.add(CONTROL_URL);
    this.descriptors = Collections.unmodifiableList(descriptors);

    final Set<Relationship> relationships = new HashSet<>();
    relationships.add(SUCCESS);
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

  @Override
  protected PropertyDescriptor getSupportedDynamicPropertyDescriptor(
      String propertyDescriptorName) {
    return new PropertyDescriptor
        .Builder().name(propertyDescriptorName)
        .displayName(propertyDescriptorName)
        .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
        .dynamic(true)
        .build();
  }

  @OnScheduled
  public void onScheduled(final ProcessContext context) {
    this.propertyMap = context.getProperties().entrySet().stream()
        .filter(e -> e.getKey().isDynamic())
        .collect(Collectors.toMap(e -> e.getKey().getName(), Map.Entry::getValue));

    getLogger().debug("Created new override map:{}", new Object[]{propertyMap});
  }

  @Override
  public void onTrigger(final ProcessContext context, final ProcessSession session)
      throws ProcessException {
    FlowFile flowFile = session.get();

    if (flowFile == null) {
      return;
    }

    String controlUrl = context.getProperty(CONTROL_URL.getName()).getValue();
    UUID processingStep = UUID.fromString(getIdentifier());

    Set<UUID> channelIds = Arrays.stream(flowFile.getAttribute("channel-ids").split(","))
        .map(UUID::fromString)
        .collect(Collectors.toSet());
    Instant startTime = Instant.parse(flowFile.getAttribute("start-time"));
    Instant endTime = Instant.parse(flowFile.getAttribute("end-time"));

    getLogger().info("Nifi Processor: " + context.getName() + ":" + getIdentifier()
        + " invoking control service at url: " + controlUrl);

    try {
      HttpResponse<String> response = invokeQc(controlUrl, channelIds, startTime, endTime,
          processingStep);
      session.getProvenanceReporter().send(flowFile, controlUrl,
          response.getStatus() + ":" + response.getStatusText() + ":" + response.getBody());

      if (response.getStatus() == 200) {
        session.transfer(flowFile, SUCCESS);
      } else {
        getLogger().error("Error status from remote process invocation: "
            + response.getStatus() + ":" + response.getStatusText() + ":" + response.getBody());
        session.transfer(flowFile, FAILURE);
      }
    } catch (ProcessException e) {
      getLogger().error("Error invoking qc control, processing cannot continue.", e);
      session.transfer(flowFile, FAILURE);
    }

    session.commit();
  }

  /**
   * Generates an invoke control json dto and POSTS it to the input url.
   *
   * @param controlUrl Control service url to post the invoke request to.
   * @param channelIds Set of UUIDs representing channels to invoke processing on.
   * @param startTime Start of time range to invoke processing on.
   * @param endTime End of time range to invoke processing on.
   * @return The success of the invoke request, in order to route the flow file to SUCCESS or
   * FAILURE.
   */
  private HttpResponse<String> invokeQc(String controlUrl, Set<UUID> channelIds, Instant startTime,
      Instant endTime, UUID channelProcessingStep) {
    ProcessingContext processingContext = new ProcessingContext(
        new ProcessingStepReference(new UUID(0, 0),
            new UUID(0, 0), channelProcessingStep));
    InvokeControlDto body = new InvokeControlDto(channelIds, startTime, endTime, processingContext,
        propertyMap);

    try {
      return Unirest.post(controlUrl)
          .header("Content-Type", "application/json")
          .body(body).asString();
    } catch (UnirestException e) {
      throw new ProcessException("Error invoking control service", e);
    }
  }

  /**
   * One-time initialization of unirest on init of the processor in order to setup the correct
   * serialization of dto objects.
   */
  private static void initializeUnirest() {
    // Only one time
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    Unirest.setObjectMapper(new JacksonObjectMapper(objectMapper));
  }

}
