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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class InvokeQcProcessorTest {


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
    testRunner = TestRunners.newTestRunner(InvokeQcProcessor.class);
    testUrl = "http://" + HOST + ":" + instanceRule.port() + BASE_URI;
  }

  @Test
  public void testProcessor() throws JsonProcessingException {
    testRunner.setProperty(InvokeQcProcessor.CONTROL_URL, testUrl);
    testRunner.setProperty("TEST_DYNAMIC", "TEST_VALUE");
    testRunner.setProperty("TEST_DYNAMIC2", "TEST_VALUE2");

    UUID processingStep = UUID.fromString(testRunner.getProcessor().getIdentifier());

    Set<UUID> channelIds = Stream.generate(UUID::randomUUID)
        .limit(10)
        .collect(Collectors.toSet());
    String channelIdsRaw = channelIds.stream()
        .map(UUID::toString)
        .collect(Collectors.joining(","));
    Instant startTime = Instant.ofEpochSecond(0);
    Instant endTime = Instant.ofEpochSecond(300);

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put("channel-ids", channelIdsRaw);
    attributesMap.put("start-time", startTime.toString());
    attributesMap.put("end-time", endTime.toString());

    testRunner.enqueue(new byte[0], attributesMap);

    ProcessingContext processingContext = new ProcessingContext(
        new ProcessingStepReference(new UUID(0, 0), new UUID(0, 0), processingStep));
    Map<String, String> parameterOverrides = new HashMap<>();
    parameterOverrides.put("TEST_DYNAMIC", "TEST_VALUE");
    parameterOverrides.put("TEST_DYNAMIC2", "TEST_VALUE2");

    InvokeControlDto invokeQcDto = new InvokeControlDto(channelIds, startTime, endTime,
        processingContext, parameterOverrides);

    givenThat(post(urlEqualTo(BASE_URI))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(invokeQcDto)))
        .willReturn(ok()));

    testRunner.run();

    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(InvokeQcProcessor.SUCCESS);

    assertTrue(testRunner.getLogger().getDebugMessages().size() > 0);

    List<ProvenanceEventRecord> provenanceEvents = testRunner.getProvenanceEvents();
    assertThat(provenanceEvents.size(), is(1));
    ProvenanceEventRecord provenanceEvent = provenanceEvents.get(0);
    assertThat(provenanceEvent.getEventType(), is(ProvenanceEventType.SEND));
    assertThat(provenanceEvent.getTransitUri(), is(testUrl));
    assertThat(provenanceEvent.getDetails(), containsString(Integer.toString(200)));

    verify(1, postRequestedFor(urlEqualTo(BASE_URI))
        .withRequestBody(equalTo(objectMapper.writeValueAsString(invokeQcDto))));
  }

}
