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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class InvokeBeamformingProcessorTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @ClassRule
  public static WireMockClassRule wireMockRule = new WireMockClassRule(
      wireMockConfig().dynamicPort());

  @Rule
  public WireMockClassRule instanceRule = wireMockRule;

  private TestRunner testRunner;

  private static final String HOST = "localhost";
  private static final String BASE_URI = "/test";

  private static ObjectMapper objectMapper;

  private String testUrl;

  @BeforeClass
  public static void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Before
  public void init() {
    testRunner = TestRunners.newTestRunner(InvokeBeamformingProcessor.class);
    testUrl = "http://" + HOST + ":" + instanceRule.port() + BASE_URI;
    testRunner.setProperty(InvokeBeamformingProcessor.SERVICE_URL, testUrl);
  }

  @Test
  public void testProcessorEmptyServiceResponse()
      throws JsonProcessingException, InitializationException {

    UUID processingStep = UUID.fromString(testRunner.getProcessor().getIdentifier());

    Instant startTime = Instant.EPOCH;
    Instant endTime = Instant.EPOCH.plusSeconds(10);
    UUID processingGroupId = UUID.randomUUID();

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put("processing-group-id", processingGroupId.toString());
    attributesMap.put("start-time", startTime.toString());
    attributesMap.put("end-time", endTime.toString());

    testRunner.enqueue(new byte[0], attributesMap);

    InvokeBeamformingDto dto = new InvokeBeamformingDto(
        processingGroupId,
        startTime,
        endTime,
        new ProcessingContext(
            new ProcessingStepReference(new UUID(0, 0),
                new UUID(0, 0), processingStep))
    );

    givenThat(post(urlEqualTo(BASE_URI))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(dto)))
        .willReturn(WireMock.ok(objectMapper.writeValueAsString(new ArrayList<String>()))));

    testRunner.run();

    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(InvokeBeamformingProcessor.SUCCESS);
    testRunner.assertAllFlowFiles(flowFile ->
      assertEquals("",flowFile.getAttribute("derived-channel-ids"))
    );
  }

  @Test
  public void testProcessor() throws JsonProcessingException, InitializationException {
    UUID processingStep = UUID.fromString(testRunner.getProcessor().getIdentifier());

    Instant startTime = Instant.EPOCH;
    Instant endTime = Instant.EPOCH.plusSeconds(10);
    UUID processingGroupId = UUID.randomUUID();

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put("processing-group-id", processingGroupId.toString());
    attributesMap.put("start-time", startTime.toString());
    attributesMap.put("end-time", endTime.toString());

    testRunner.enqueue(new byte[0], attributesMap);

    InvokeBeamformingDto dto = new InvokeBeamformingDto(
        processingGroupId,
        startTime,
        endTime,
        new ProcessingContext(
            new ProcessingStepReference(new UUID(0, 0),
                new UUID(0, 0), processingStep))
    );

    givenThat(post(urlEqualTo(BASE_URI))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(dto)))
        .willReturn(WireMock.ok(objectMapper.writeValueAsString(
            Collections.singletonList(UUID.randomUUID())))));

    testRunner.run();

    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(InvokeBeamformingProcessor.SUCCESS);
    testRunner.assertAllFlowFiles(flowFile -> {
      List<UUID> ids = Arrays.stream(flowFile.getAttribute("derived-channel-ids").split(","))
          .map(UUID::fromString).collect(Collectors.toList());

      assertThat(ids.size(), is(1));
    });

    List<ProvenanceEventRecord> provenanceEvents = testRunner.getProvenanceEvents();
    assertThat(provenanceEvents.size(), is(1));
    ProvenanceEventRecord provenanceEvent = provenanceEvents.get(0);
    assertThat(provenanceEvent.getEventType(), is(ProvenanceEventType.SEND));
    assertThat(provenanceEvent.getTransitUri(), is(testUrl));
    assertThat(provenanceEvent.getDetails(), containsString(Integer.toString(200)));

    verify(1, postRequestedFor(urlEqualTo(BASE_URI))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(dto))));
  }

}
