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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class InvokeSignalDetectorProcessorTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @ClassRule
  public static final WireMockClassRule wireMockRule = new WireMockClassRule(
      wireMockConfig().dynamicPort());

  private static final String START_TIME = "start-time";
  private static final String END_TIME = "end-time";

  @Rule
  public WireMockClassRule instanceRule = wireMockRule;

  private TestRunner testRunner;

  private static final String HOST = "localhost";
  private static final String BASE_URI = "/test";

  private static ObjectMapper objectMapper;

  private static String expectedHypothesisDescrpitorsAsString;

  private String testUrl;

  @BeforeClass
  public static void setUp() throws JsonProcessingException {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    ArrayNode expectedHypothesisDescriptorsNode = JsonNodeFactory.instance.arrayNode()
        .add(JsonNodeFactory.instance.objectNode()
            .put("stationId", UUID.randomUUID().toString())
            .set("signalDetectionHypothesis", JsonNodeFactory.instance.objectNode()
                .put("id", UUID.randomUUID().toString())
                .put("parentSignalDetectionId", UUID.randomUUID().toString())
                .put("rejected", false)
                .put("creationInfoId", UUID.randomUUID().toString())
                .set("featureMeasurements", JsonNodeFactory.instance.arrayNode()
                    .add(JsonNodeFactory.instance.objectNode()
                        .put("id", UUID.randomUUID().toString())
                        .put("channelSegmentId", UUID.randomUUID().toString())
                        .put("featureMeasurementType", "ARRIVAL_TIME")
                        .set("measurementValue", JsonNodeFactory.instance.objectNode()
                            .put("value", Instant.EPOCH.toString())
                            .put("standardDeviation", Duration.ofSeconds(1).toString())))
                    .add(JsonNodeFactory.instance.objectNode()
                        .put("id", UUID.randomUUID().toString())
                        .put("channelSegmentId", UUID.randomUUID().toString())
                        .put("featureMeasurementType", "PHASE")
                        .set("measurementValue", JsonNodeFactory.instance.objectNode()
                            .put("value", "P")
                            .put("confidence", 1.0))))));
    expectedHypothesisDescrpitorsAsString = objectMapper
        .writeValueAsString(expectedHypothesisDescriptorsNode);
  }

  @Before
  public void init() {
    testRunner = TestRunners.newTestRunner(InvokeSignalDetectorProcessor.class);
    testUrl = "http://" + HOST + ":" + instanceRule.port() + BASE_URI;
  }

  @Test
  public void testProcessor()
      throws IOException {
    testRunner.setProperty(InvokeSignalDetectorProcessor.CONTROL_URL, testUrl);
    testRunner.setProperty(InvokeSignalDetectorProcessor.INPUT_CHANNEL_IDS, "${filter-ids}");

    UUID processingStep = UUID.fromString(testRunner.getProcessor().getIdentifier());
    List<UUID> channelIds = Arrays.asList(UUID.fromString("0364c595-a44b-3952-8baa-4629deb35ea5"),
        UUID.fromString("0364c595-a44b-3952-8baa-4629deb35ea6"),
        UUID.fromString("0364c595-a44b-3952-8baa-4629deb35ea7"));
    String channelIdsString = channelIds.stream().map(UUID::toString)
        .collect(Collectors.joining(","));

    Instant startTime = Instant.ofEpochSecond(0);
    Instant endTime = Instant.ofEpochSecond(300);

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put("filter-ids", channelIdsString);
    attributesMap.put(START_TIME, startTime.toString());
    attributesMap.put(END_TIME, endTime.toString());

    testRunner.enqueue(new byte[0], attributesMap);

    List<InvokeSignalDetectorDto> dtos = channelIds.stream()
        .map(id -> createInvokeSignalDetectorDto(id, startTime, endTime, processingStep))
        .collect(Collectors.toList());

    for (InvokeSignalDetectorDto dto : dtos) {
      givenThat(post(urlEqualTo(BASE_URI))
          .withRequestBody(equalTo(objectMapper.writeValueAsString(dto)))
          .willReturn(ok(expectedHypothesisDescrpitorsAsString)));
    }

    testRunner.run();

    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(InvokeSignalDetectorProcessor.SUCCESS);
    assertFlowFilesValid(testRunner, channelIds);
    assertProvenanceEventsValid(testRunner, channelIds, testUrl);

    for (InvokeSignalDetectorDto dto : dtos) {
      verify(1, postRequestedFor(urlEqualTo(BASE_URI))
          .withRequestBody(equalTo(objectMapper.writeValueAsString(dto))));
    }
  }

  @Test
  public void testProcessorChannelIdAsProperty()
      throws IOException {
    testRunner.setProperty(InvokeSignalDetectorProcessor.CONTROL_URL, testUrl);

    String channelIdsString = "0364c595-a44b-3952-8baa-4629deb35ea5";
    UUID channelId = UUID.fromString(channelIdsString);
    testRunner.setProperty(InvokeSignalDetectorProcessor.INPUT_CHANNEL_IDS, channelIdsString);

    UUID processingStep = UUID.fromString(testRunner.getProcessor().getIdentifier());

    Instant startTime = Instant.ofEpochSecond(0);
    Instant endTime = Instant.ofEpochSecond(300);

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put(START_TIME, startTime.toString());
    attributesMap.put(END_TIME, endTime.toString());

    testRunner.enqueue(new byte[0], attributesMap);

    InvokeSignalDetectorDto dto = createInvokeSignalDetectorDto(channelId, startTime, endTime,
        processingStep);

    givenThat(post(urlEqualTo(BASE_URI))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(dto)))
        .willReturn(ok(expectedHypothesisDescrpitorsAsString)));

    testRunner.run();

    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(InvokeSignalDetectorProcessor.SUCCESS);
    assertFlowFilesValid(testRunner, Collections.singletonList(channelId));
    assertProvenanceEventsValid(testRunner, Collections.singletonList(channelId), testUrl);

    verify(1, postRequestedFor(urlEqualTo(BASE_URI))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(dto))));
  }

  @Test
  public void testProcessorChannelIdCsvAsProperty()
      throws IOException {
    testRunner.setProperty(InvokeSignalDetectorProcessor.CONTROL_URL, testUrl);

    List<UUID> channelIds = Arrays.asList(UUID.fromString("0364c595-a44b-3952-8baa-4629deb35ea5"),
        UUID.fromString("0364c595-a44b-3952-8baa-4629deb35ea6"),
        UUID.fromString("0364c595-a44b-3952-8baa-4629deb35ea7"));
    String channelIdsString = channelIds.stream().map(UUID::toString)
        .collect(Collectors.joining(","));
    testRunner.setProperty(InvokeSignalDetectorProcessor.INPUT_CHANNEL_IDS, channelIdsString);

    UUID processingStep = UUID.fromString(testRunner.getProcessor().getIdentifier());

    Instant startTime = Instant.ofEpochSecond(0);
    Instant endTime = Instant.ofEpochSecond(300);

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put(START_TIME, startTime.toString());
    attributesMap.put(END_TIME, endTime.toString());

    testRunner.enqueue(new byte[0], attributesMap);

    List<InvokeSignalDetectorDto> dtos = channelIds.stream()
        .map(id -> createInvokeSignalDetectorDto(id, startTime, endTime, processingStep))
        .collect(Collectors.toList());

    for (InvokeSignalDetectorDto dto : dtos) {
      givenThat(post(urlEqualTo(BASE_URI))
          .withRequestBody(equalTo(objectMapper.writeValueAsString(dto)))
          .willReturn(ok(expectedHypothesisDescrpitorsAsString)));
    }

    testRunner.run();

    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(InvokeSignalDetectorProcessor.SUCCESS);

    assertFlowFilesValid(testRunner, channelIds);
    assertProvenanceEventsValid(testRunner, channelIds, testUrl);

    for (InvokeSignalDetectorDto dto : dtos) {
      verify(1, postRequestedFor(urlEqualTo(BASE_URI))
          .withRequestBody(equalTo(objectMapper.writeValueAsString(dto))));
    }
  }

  @Test
  public void testInputChannelIdsBadAttribute()
      throws JsonProcessingException {
    testRunner.setProperty(InvokeSignalDetectorProcessor.CONTROL_URL, testUrl);
    testRunner.setProperty(InvokeSignalDetectorProcessor.INPUT_CHANNEL_IDS, "${not-an-attribute}");

    UUID processingStep = UUID.fromString(testRunner.getProcessor().getIdentifier());
    List<UUID> channelIds = Arrays.asList(UUID.fromString("0364c595-a44b-3952-8baa-4629deb35ea5"),
        UUID.fromString("0364c595-a44b-3952-8baa-4629deb35ea6"),
        UUID.fromString("0364c595-a44b-3952-8baa-4629deb35ea7"));
    String channelIdsString = channelIds.stream().map(UUID::toString)
        .collect(Collectors.joining(","));

    Instant startTime = Instant.ofEpochSecond(0);
    Instant endTime = Instant.ofEpochSecond(300);

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put("filter-ids", channelIdsString);
    attributesMap.put(START_TIME, startTime.toString());
    attributesMap.put(END_TIME, endTime.toString());

    testRunner.enqueue(new byte[0], attributesMap);

    List<InvokeSignalDetectorDto> dtos = channelIds.stream()
        .map(id -> createInvokeSignalDetectorDto(id, startTime, endTime, processingStep))
        .collect(Collectors.toList());

    for (InvokeSignalDetectorDto dto : dtos) {
      givenThat(post(urlEqualTo(BASE_URI))
          .withRequestBody(equalTo(objectMapper.writeValueAsString(dto)))
          .willReturn(ok()));
    }

    testRunner.run();

    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(InvokeSignalDetectorProcessor.FAILURE);
  }

  private static void assertFlowFilesValid(TestRunner testRunner,
      List<UUID> channelIds) {
    List<MockFlowFile> sdFiles = testRunner
        .getFlowFilesForRelationship(InvokeSignalDetectorProcessor.SUCCESS);

    assertThat(sdFiles.size(), is(channelIds.size()));

    for (MockFlowFile sdFile : sdFiles) {
      assertThat(channelIds, hasItem(UUID.fromString(sdFile.getAttribute("sd-channel-id"))));
      assertThat(sdFile.getAttribute(START_TIME), both(notNullValue()).and(not("")));
      assertThat(sdFile.getAttribute(END_TIME), both(notNullValue()).and(not("")));

      String hypothesesAsString = sdFile.getAttribute("signal-detection-hypothesis-descriptors");
      assertThat(hypothesesAsString, both(notNullValue()).and(not("")));

      assertEquals(expectedHypothesisDescrpitorsAsString, hypothesesAsString);
    }
  }

  private static void assertProvenanceEventsValid(TestRunner testRunner, List<UUID> channelIds,
      String testUrl) {
    List<ProvenanceEventRecord> provenanceEvents = testRunner.getProvenanceEvents().stream()
        .filter(pe -> ProvenanceEventType.SEND.equals(pe.getEventType()))
        .collect(Collectors.toList());
    assertThat(provenanceEvents.size(), is(channelIds.size()));
    for (ProvenanceEventRecord provenanceEvent : provenanceEvents) {
      assertThat(provenanceEvent.getEventType(), is(ProvenanceEventType.SEND));
      assertThat(provenanceEvent.getTransitUri(), is(testUrl));
      assertThat(provenanceEvent.getDetails(), containsString(Integer.toString(200)));
    }
  }

  private static InvokeSignalDetectorDto createInvokeSignalDetectorDto(UUID channelId,
      Instant startTime, Instant endTime, UUID processingStep) {
    // build the dto with partial subset of override properties.
    return new InvokeSignalDetectorDto(channelId,
        startTime, endTime, new ProcessingContext(
        new ProcessingStepReference(new UUID(0, 0),
            new UUID(0, 0), processingStep)), Collections.emptyMap());
  }
}
