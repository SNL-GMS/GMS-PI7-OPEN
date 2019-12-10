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
package gms.processors.invokebeamforming;

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
import java.util.HashSet;
import java.util.List;
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

@Tags({"invoke, beam, beamforming"})
@CapabilityDescription("Invokes the beaming service")
@SeeAlso({})
@ReadsAttributes({
    @ReadsAttribute(attribute = "processing-group-id", description = "ID of processining group containing channels to process"),
    @ReadsAttribute(attribute = "start-time", description = "start time"),
    @ReadsAttribute(attribute = "end-time", description = "end time")
})
@WritesAttributes({
    @WritesAttribute(attribute = "derived-channel-ids", description = "comma-seperated list of channales containing the beams")})
public class InvokeBeamformingProcessor extends AbstractProcessor {

  public static final PropertyDescriptor SERVICE_URL = new PropertyDescriptor
      .Builder().name("service-url")
      .displayName("Service URL")
      .description("URL for the service")
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

  private ObjectMapper objectMapper;

  @Override
  protected void init(final ProcessorInitializationContext context) {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    initializeUnirest(objectMapper);

    final List<PropertyDescriptor> descriptors = new ArrayList<>();
    descriptors.add(SERVICE_URL);
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

    String serviceUrl = context.getProperty(SERVICE_URL.getName()).getValue();

    getLogger().info("Nifi Processor: " + context.getName() + ":" + getIdentifier()
        + " invoking service at url: " + serviceUrl);

    getLogger().info("calling service with params:" +
        "\n\tserviceUrl: " + serviceUrl +
        "\n\tprocessing-group-id: " + flowFile.getAttribute("processing-group-id") +
        "\n\tstart-time: " + flowFile.getAttribute("start-time") +
        "\n\tend-time: " + flowFile.getAttribute("end-time") +
        "\n\tchannel-processing-step: " + getIdentifier());

    try {

      HttpResponse<String> response = invokeControl(
          serviceUrl,
          UUID.fromString(flowFile.getAttribute("processing-group-id")),
          Instant.parse(flowFile.getAttribute("start-time")),
          Instant.parse(flowFile.getAttribute("end-time")),
          UUID.fromString(getIdentifier())
      );

      session.getProvenanceReporter().send(flowFile, serviceUrl,
          response.getStatus() + ":" + response.getStatusText() + ":" + response.getBody());

      if (response.getStatus() == 200) {
        UUID[] uuids = objectMapper.readValue(response.getBody(), UUID[].class);
        String uuidsAsString = Arrays.stream(uuids).map(UUID::toString)
            .collect(Collectors.joining(","));

        session.putAttribute(flowFile, "derived-channel-ids", uuidsAsString);

        session.transfer(flowFile, SUCCESS);
      } else {
        getLogger().error("Error status from remote process invocation: "
            + response.getStatus() + ":" + response.getStatusText() + ":" + response.getBody());
        session.transfer(flowFile, FAILURE);
      }

    } catch (ProcessException | IOException e) {
      getLogger().error("Error invoking beam control, processing cannot continue.", e);
      session.transfer(flowFile, FAILURE);
    }

    session.commit();

  }

  private HttpResponse<String> invokeControl(String controlUrl, UUID processingGroupId,
      Instant startTime, Instant endTime, UUID channelProcessingStep
  ) {
    ProcessingContext processingContext = new ProcessingContext(
        new ProcessingStepReference(new UUID(0, 0), new UUID(0, 0),
            channelProcessingStep));
    InvokeBeamformingDto body = new InvokeBeamformingDto(
        processingGroupId,
        startTime,
        endTime,
        processingContext
    );

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
