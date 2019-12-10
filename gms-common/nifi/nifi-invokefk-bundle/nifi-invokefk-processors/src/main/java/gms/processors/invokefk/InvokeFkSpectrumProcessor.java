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
package gms.processors.invokefk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.lookup.LookupFailureException;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.lookup.StringLookupService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Tags({"invoke, fk, fkspectrum, spectrum"})
@CapabilityDescription("Provide a description")
@SeeAlso({})
@ReadsAttributes({
    @ReadsAttribute(attribute = "processing-group-id", description = "ID of processining group containing channels to process"),
    @ReadsAttribute(attribute = "start-time", description = "start time"),
    @ReadsAttribute(attribute = "sample-count", description = "number of spectra to calculate")
})
@WritesAttributes({
    @WritesAttribute(attribute = "derived-channel-ids", description = "comma-seperated list of channales containing the FK spectra")})
public class InvokeFkSpectrumProcessor extends AbstractProcessor {

  private static final String[] PHASE_TYPES = new String[]{"P", "S"};

  public static final PropertyDescriptor FK_SPECTRA_URL = new PropertyDescriptor
      .Builder().name("fk-spectra-url")
      .displayName("FK Spectra URL")
      .description("URL for the FK Spectra")
      .required(true)
      .addValidator(StandardValidators.URL_VALIDATOR)
      .build();

  public static final PropertyDescriptor FK_MEASUREMENT_URL = new PropertyDescriptor
      .Builder().name("fk-measurement-url")
      .displayName("FK Measurement URL")
      .description("URL for the FK Measurement")
      .required(true)
      .addValidator(StandardValidators.URL_VALIDATOR)
      .build();

  public static final PropertyDescriptor OUTPUT_CHANNEL_LOOKUP = new PropertyDescriptor
      .Builder().name("output-channel-lookup")
      .displayName("Output Channel Lookup")
      .description("Lookup Controller Service for retrieving an output channel id based"
          + " on the phase type.")
      .required(true)
      .identifiesControllerService(StringLookupService.class)
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

  @Override
  protected Collection<ValidationResult> customValidate(ValidationContext validationContext) {
    final List<ValidationResult> errors = new ArrayList<>(super.customValidate(validationContext));

    final Set<String> requiredKeys = validationContext.getProperty(OUTPUT_CHANNEL_LOOKUP)
        .asControllerService(StringLookupService.class).getRequiredKeys();
    if (requiredKeys.size() != 1) {
      errors.add(new ValidationResult.Builder()
          .subject(OUTPUT_CHANNEL_LOOKUP.getDisplayName())
          .valid(false)
          .explanation(
              "Output Channel Lookup attribute requires a key-value lookup service supporting"
                  + " exactly one required key.")
          .build());
    }

    return errors;
  }

  @Override
  protected void init(final ProcessorInitializationContext context) {
    initializeUnirest();

    final List<PropertyDescriptor> descriptors = new ArrayList<>();
    descriptors.add(FK_SPECTRA_URL);
    descriptors.add(FK_MEASUREMENT_URL);
    descriptors.add(OUTPUT_CHANNEL_LOOKUP);
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

    String fkSpectraUrl = context.getProperty(FK_SPECTRA_URL.getName()).getValue();

    StringLookupService outputChannelLookup = context.getProperty(OUTPUT_CHANNEL_LOOKUP.getName())
        .asControllerService(StringLookupService.class);
    String coordinateKey = getCoordinateKey(outputChannelLookup);

    getLogger().info("Nifi Processor: " + context.getName() + ":" + getIdentifier()
        + " invoking service at url: " + fkSpectraUrl);

    try {
      Map<String, UUID> phaseOutputputChannels = new HashMap<>();

      Arrays.stream(PHASE_TYPES).forEach(pt -> {
        try {
          outputChannelLookup.lookup(Collections.singletonMap(coordinateKey, pt))
              .ifPresent(uuidString -> phaseOutputputChannels.put(pt, UUID.fromString(uuidString)));
        } catch (LookupFailureException e) {
          throw new ProcessException("Error looking up output channel", e);
        }
      });

      HttpResponse<UUID[]> fkSpectraResponse = invokeControl(
          fkSpectraUrl,
          UUID.fromString(flowFile.getAttribute("processing-group-id")),
          phaseOutputputChannels,
          Instant.parse(flowFile.getAttribute("start-time")),
          Integer.parseInt(flowFile.getAttribute("sample-count")),
          UUID.fromString(getIdentifier())
      );

      session.getProvenanceReporter().send(flowFile, fkSpectraUrl,
          fkSpectraResponse.getStatus() + ":" + fkSpectraResponse.getStatusText() + ":"
              + fkSpectraResponse.getBody());

      if (fkSpectraResponse.getStatus() == 200) {
        getLogger().info("getting uuids as string");
        String uuidsAsString = Arrays.stream(fkSpectraResponse.getBody()).map(UUID::toString)
            .reduce((s1, s2) -> s1 + "," + s2).orElse("");
        getLogger().info("uuidsAsString: " + uuidsAsString);

        session.putAttribute(flowFile, "derived-channel-ids", uuidsAsString);
      } else {
        getLogger().error("Error status from remote process invocation: "
            + fkSpectraResponse.getStatus() + ":" + fkSpectraResponse.getStatusText() + ":"
            + fkSpectraResponse.getBody());
        session.transfer(flowFile, FAILURE);
      }

      String fkMeasurementUrl = context.getProperty(FK_MEASUREMENT_URL.getName()).getValue();

      getLogger().info("Nifi Processor: " + context.getName() + ":" + getIdentifier()
          + " invoking service at url: " + fkMeasurementUrl);

      HttpResponse<UUID[]> fkMeasurementResponse = invokeControl(
          fkMeasurementUrl,
          UUID.fromString(flowFile.getAttribute("fk-power-spectrum-channel-id")),
          Instant.parse(flowFile.getAttribute("start-time")),
          Instant.parse(flowFile.getAttribute("end-time")),
          UUID.fromString(getIdentifier())
      );

      session.getProvenanceReporter().send(flowFile, fkMeasurementUrl,
          fkMeasurementResponse.getStatus() + ":" + fkMeasurementResponse.getStatusText() + ":"
              + fkMeasurementResponse.getBody());

      if (fkMeasurementResponse.getStatus() == 200) {
        getLogger().info("getting uuids as string");
        String uuidsAsString = Arrays.stream(fkMeasurementResponse.getBody()).map(UUID::toString)
            .reduce((s1, s2) -> s1 + "," + s2).orElse("");
        getLogger().info("uuidsAsString: " + uuidsAsString);

        session.putAttribute(flowFile, "fk-measurement-ids", uuidsAsString);

        session.transfer(flowFile, SUCCESS);
      } else {
        getLogger().error("Error status from remote process invocation: "
            + fkSpectraResponse.getStatus() + ":" + fkSpectraResponse.getStatusText() + ":"
            + fkSpectraResponse.getBody());
        session.transfer(flowFile, FAILURE);
      }

    } catch (ProcessException e) {
      getLogger().error("Error invoking filter control, processing cannot continue.", e);
      session.transfer(flowFile, FAILURE);
    }

    session.commit();

  }

  private HttpResponse<UUID[]> invokeControl(String controlUrl, UUID processingGroup,
      Map<String, UUID> phaseOutputChannelIds,
      Instant startTime, int sampleCount, UUID channelProcessingStep) {
    ProcessingContext processingContext = new ProcessingContext(
        new ProcessingStepReference(new UUID(0, 0), new UUID(0, 0),
            channelProcessingStep));
    InvokeFkSpectrumDto body = new InvokeFkSpectrumDto(
        processingGroup,
        phaseOutputChannelIds,
        startTime,
        sampleCount,
        processingContext
    );

    try {
      return Unirest.post(controlUrl)
          .header("Content-Type", "application/json")
          .header("Accept", "application/json")
          .body(body).asObject(UUID[].class);
    } catch (UnirestException e) {
      throw new ProcessException("Error invoking control service", e);
    }
  }

  private HttpResponse<UUID[]> invokeControl(String controlUrl, UUID fkPowerSpectrumChannelId,
      Instant startTime, Instant endTime, UUID channelProcessingStep) {
    ProcessingContext processingContext = new ProcessingContext(
        new ProcessingStepReference(new UUID(0, 0), new UUID(0, 0),
            channelProcessingStep));
    InvokeFkMeasurementDto body = new InvokeFkMeasurementDto(
        fkPowerSpectrumChannelId,
        startTime,
        endTime,
        processingContext
    );

    try {
      return Unirest.post(controlUrl)
          .header("Content-Type", "application/json")
          .header("Accept", "application/json")
          .body(body).asObject(UUID[].class);
    } catch (UnirestException e) {
      throw new ProcessException("Error invoking control service", e);
    }
  }


  private static String getCoordinateKey(StringLookupService lookupService) {
    final Set<String> requiredKeys = lookupService.getRequiredKeys();
    if (requiredKeys.size() != 1) {
      throw new ProcessException(
          "Output Channel Lookup attribute requires a key-value lookup service supporting exactly"
              + " one required key, was: " + String.valueOf(requiredKeys.size()));
    }

    return requiredKeys.iterator().next();
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
