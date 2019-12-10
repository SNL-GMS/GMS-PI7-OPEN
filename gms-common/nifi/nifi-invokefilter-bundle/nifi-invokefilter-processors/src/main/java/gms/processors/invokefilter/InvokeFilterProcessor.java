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
package gms.processors.invokefilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.IOException;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

@Tags({"invoke, filter, filtering, signal, enhancement"})
@CapabilityDescription("Provide a description")
@ReadsAttributes({
    @ReadsAttribute(attribute = "channel-ids", description = "List of comma-separated channel ids"
        + " to run processing on. The ids should be in UUID format."),
    @ReadsAttribute(attribute = "start-time", description = "The start of the time range to run"
        + " processing on. The value should be a date string in ISO-8601 instant format."),
    @ReadsAttribute(attribute = "end-time", description = "The end of the time range to run"
        + " processing on. The value should be a date string in ISO-8601 instant format.")})
@WritesAttributes({
    @WritesAttribute(attribute = "filter-channel-ids", description = "Ordered list of channel ids corresponding to the output channels of the filtered input channels.")})
public class InvokeFilterProcessor extends AbstractProcessor {

  static final PropertyDescriptor CONTROL_URL = new PropertyDescriptor
      .Builder().name("control-url")
      .displayName("Control URL")
      .description("URL for the control service")
      .required(true)
      .addValidator(StandardValidators.URL_VALIDATOR)
      .build();

  static final Relationship SUCCESS = new Relationship.Builder()
      .name("success")
      .description("Successful execution of Filter Control Service")
      .build();

  private static final Relationship FAILURE = new Relationship.Builder()
      .name("failure")
      .description("Unsuccessful execution of Filter Control Service")
      .build();

  private ObjectMapper objectMapper;

  @Override
  protected void init(final ProcessorInitializationContext context) {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    initializeUnirest(objectMapper);
  }

  @Override
  public Set<Relationship> getRelationships() {
    final Set<Relationship> relationships = new HashSet<>();
    relationships.add(SUCCESS);
    relationships.add(FAILURE);
    return Collections.unmodifiableSet(relationships);
  }

  @Override
  public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
    final List<PropertyDescriptor> descriptors = new ArrayList<>();
    descriptors.add(CONTROL_URL);
    return Collections.unmodifiableList(descriptors);
  }

  @Override
  public void onTrigger(final ProcessContext context, final ProcessSession session) {
    FlowFile flowFile = session.get();
    if (flowFile == null) {
      return;
    }

    String controlUrl = context.getProperty(CONTROL_URL.getName()).getValue();

    UUID processingStep = UUID.fromString(getIdentifier());

    List<UUID> channelIds = Arrays.stream(flowFile.getAttribute("channel-ids").split(","))
        .map(UUID::fromString)
        .collect(Collectors.toList());
    Instant startTime = Instant.parse(flowFile.getAttribute("start-time"));
    Instant endTime = Instant.parse(flowFile.getAttribute("end-time"));

    getLogger().info("Nifi Processor: " + context.getName() + ":" + getIdentifier()
        + " invoking control service at url: " + controlUrl);

    HttpResponse<String> response = invokeControl(controlUrl, processingStep, channelIds,
        startTime,
        endTime);

    session.getProvenanceReporter().send(flowFile, controlUrl,
        response.getStatus() + ":" + response.getStatusText() + ":" + response.getBody());

    if (response.getStatus() == 200) {
      try {
        UUID[] outputIds = objectMapper.readValue(response.getBody(), UUID[].class);
        String uuidsAsString = Arrays.stream(outputIds)
            .map(UUID::toString)
            .collect(Collectors.joining(","));

        session.putAttribute(flowFile, "filter-channel-ids", uuidsAsString);
        session.transfer(flowFile, SUCCESS);
      } catch (IOException e) {
        getLogger().error("Error parsing filter control response: " + response.getBody(), e);
        session.transfer(flowFile, FAILURE);
      }
    } else {
        getLogger().error("Error status from remote process invocation: "
            + response.getStatus() + ":" + response.getStatusText() + ":" + response.getBody());
        session.transfer(flowFile, FAILURE);
      }

    session.commit();
  }

  private HttpResponse<String> invokeControl(String controlUrl, UUID channelProcessingStep,
      List<UUID> channelIds, Instant startTime, Instant endTime) {

    Map<UUID, UUID> inputOutputChannelMap = new HashMap<>();
    inputOutputChannelMap.put(UUID.fromString("3f174558-cff8-3aba-bea2-39f9b2b28b30"),
        UUID.fromString("73c4b1d0-9c03-4ae8-9627-ab02a86f4763"));
    inputOutputChannelMap.put(UUID.fromString("7233514a-946f-34d9-b004-55834dfa3e52"),
        UUID.fromString("8d7478a5-4b64-42f7-be1f-5db79466a3fd"));
    inputOutputChannelMap.put(UUID.fromString("9e5c1c14-c291-32c6-8c24-9efac6bf70cf"),
        UUID.fromString("531443af-7d91-4b7c-8ea9-29829cc0aafd"));
    inputOutputChannelMap.put(UUID.fromString("70ba9365-f5fa-3667-842f-bfa2b55e2ffd"),
        UUID.fromString("9461e4f7-91e4-4a95-b8e6-9b05e0be2e78"));
    inputOutputChannelMap.put(UUID.fromString("60c87a3c-5390-3a0c-8b05-acd217d0e014"),
        UUID.fromString("ff3867b0-97ef-4858-9d10-3f9b921056c3"));
    inputOutputChannelMap.put(UUID.fromString("3cea4a55-0025-3655-91c4-cc948b749d16"),
        UUID.fromString("9541f309-b70f-4ce4-b7c9-ae206f68cdbc"));
    inputOutputChannelMap.put(UUID.fromString("e1ae40b2-3921-3d4c-9095-938ced7833a9"),
        UUID.fromString("7bfeea91-a06e-4c2a-847f-94133ee4461c"));
    inputOutputChannelMap.put(UUID.fromString("09c79723-f5f2-30ef-b12d-571884691971"),
        UUID.fromString("ec682ff5-4806-4383-b12c-495358126a5e"));
    inputOutputChannelMap.put(UUID.fromString("c9464459-1873-3f24-90ac-e59081d7d0d8"),
        UUID.fromString("7265ac9e-ee94-4e7a-8581-782145e7878e"));

    Map<UUID, UUID> bodyInputOutputChannels = channelIds.stream()
        .filter(inputOutputChannelMap::containsKey)
        .collect(Collectors.toMap(Function.identity(), inputOutputChannelMap::get));

    ProcessingContext processingContext = new ProcessingContext(
        new ProcessingStepReference(new UUID(0, 0), new UUID(0, 0),
            channelProcessingStep));
    InvokeFilterDto body = new InvokeFilterDto(channelProcessingStep, bodyInputOutputChannels,
        startTime,
        endTime, processingContext);

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

}
