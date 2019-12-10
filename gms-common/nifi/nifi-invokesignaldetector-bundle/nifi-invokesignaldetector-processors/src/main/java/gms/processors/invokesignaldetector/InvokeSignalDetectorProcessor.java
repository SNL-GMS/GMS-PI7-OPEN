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
package gms.processors.invokesignaldetector;

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
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

@Tags({"invoke, detection, signal"})
@CapabilityDescription("Makes calls to Signal Detection Service given information from the flowfile. Creates and updates flowfile with passport metadata.")
@SeeAlso({})
@ReadsAttributes({
    @ReadsAttribute(attribute = "start-time", description = "The start of the time range to run"
        + " processing on. The value should be a date string in ISO-8601 instant format."),
    @ReadsAttribute(attribute = "end-time", description = "The end of the time range to run")})
@WritesAttributes({
    @WritesAttribute(attribute = "sd-channel-id", description = "The single channel id sd was run on for flowfile created from the input parent."),
    @WritesAttribute(attribute = "signal-detection-hypothesis-descriptors", description = "The AIC hypotheses with their station IDs of all signal detections created for this flowfile.")
})

public class InvokeSignalDetectorProcessor extends AbstractProcessor {

  public static final PropertyDescriptor CONTROL_URL = new PropertyDescriptor
      .Builder().name("control-url")
      .displayName("Control URL")
      .description("URL for the control service")
      .required(true)
      .addValidator(StandardValidators.URL_VALIDATOR)
      .build();


  public static final PropertyDescriptor INPUT_CHANNEL_IDS = new PropertyDescriptor
      .Builder().name("input-channel-ids")
      .displayName("Input Channel IDs")
      .description(
          "List of UUIDs for input channels. Expects attribute from flowfile via Nifi Expression Language.")
      .required(true)
      .expressionLanguageSupported(ExpressionLanguageScope.FLOWFILE_ATTRIBUTES)
      .addValidator(StandardValidators.ATTRIBUTE_EXPRESSION_LANGUAGE_VALIDATOR)
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

  private ObjectMapper objectMapper;

  @Override
  protected void init(final ProcessorInitializationContext context) {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    initializeUnirest(objectMapper);

    final List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();
    propertyDescriptors.add(CONTROL_URL);
    propertyDescriptors.add(INPUT_CHANNEL_IDS);

    this.descriptors = Collections.unmodifiableList(propertyDescriptors);

    final Set<Relationship> relationshipSet = new HashSet<>();
    relationshipSet.add(SUCCESS);
    relationshipSet.add(FAILURE);
    this.relationships = Collections.unmodifiableSet(relationshipSet);
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
  public void onTrigger(final ProcessContext context, final ProcessSession session) {
    FlowFile flowFile = session.get();
    if (flowFile == null) {
      return;
    }
    String controlUrl = context.getProperty(CONTROL_URL.getName()).getValue();

    String inputChannelIds = context.getProperty(INPUT_CHANNEL_IDS.getName())
        .evaluateAttributeExpressions(flowFile).getValue();
    if (inputChannelIds.equals("")) {
      getLogger().error(
          "Error: Provided query to " + INPUT_CHANNEL_IDS.getName()
              + " returned \"\".");
      session.transfer(flowFile, FAILURE);
      return;
    }

    List<UUID> channelIds = Arrays
        .stream(inputChannelIds.split(","))
        .map(UUID::fromString).collect(Collectors.toList());

    Map<String, String> overridePropertiesMap = getOverridePropertiesMap(context);
    UUID processingStep = UUID.fromString(getIdentifier());

    Instant startTime = Instant.parse(flowFile.getAttribute("start-time"));
    Instant endTime = Instant.parse(flowFile.getAttribute("end-time"));

    getLogger().info("Nifi Processor: " + context.getName() + ":" + getIdentifier()
        + " invoking control service at url: " + controlUrl);

    for (UUID channelId : channelIds) {
      FlowFile sdFile = session.create();
      session.putAttribute(sdFile, "sd-channel-id", channelId.toString());

      Map<String, String> parentAttributes = new HashMap<>(flowFile.getAttributes());
      parentAttributes.remove("uuid");

      session.putAllAttributes(sdFile, parentAttributes);

      try {
        HttpResponse<String> response = invokeControl(controlUrl, processingStep, channelId,
            startTime, endTime, overridePropertiesMap);

        if (response.getStatus() == 200) {
          session.putAttribute(sdFile, "signal-detection-hypothesis-descriptors", response.getBody());
          session.transfer(sdFile, SUCCESS);
        } else {
          getLogger().error("Error status from remote process invocation: "
              + response.getStatus() + ":" + response.getStatusText() + ":" + response.getBody());
          session.transfer(sdFile, FAILURE);
        }
        session.getProvenanceReporter().send(sdFile, controlUrl,
            response.getStatus() + ":" + response.getStatusText() + ":" + response.getBody());

      } catch (ProcessException e) {
        getLogger().error("Error invoking detector control, processing cannot continue.", e);
        session.transfer(sdFile, FAILURE);
      }
    }

    session.remove(flowFile);
  }

  private HttpResponse<String> invokeControl(String controlUrl, UUID processingStep, UUID channelId,
      Instant startTime, Instant endTime, Map<String, String> overridePropertiesMap) {
    ProcessingContext processingContext = new ProcessingContext(
        new ProcessingStepReference(new UUID(0, 0), new UUID(0, 0), processingStep));

    InvokeSignalDetectorDto body = new InvokeSignalDetectorDto(channelId, startTime, endTime,
        processingContext, overridePropertiesMap);

    try {
      return Unirest.post(controlUrl)
          .header("Content-Type", "application/json")
          .header("Accept", "application/json")
          .body(body).asString();
    } catch (UnirestException e) {
      throw new ProcessException("Error invoking control service", e);
    }
  }

  /**
   * One-time initialization of unirest on init of the processor in order to setup the correct
   * serialization of dto objects.
   */
  private static void initializeUnirest(ObjectMapper objectMapper) {
    // Only one time
    Unirest.setObjectMapper(new JacksonObjectMapper(objectMapper));
  }

  /**
   * Returns all override properties defined in the ProcessContext.
   *
   * @param context The Process Context
   * @return All defined override properties
   */
  private Map<String, String> getOverridePropertiesMap(ProcessContext context) {

    return new HashMap<>();
  }
}
