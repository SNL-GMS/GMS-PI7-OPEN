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

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class GetAvailabilityProcessorTest {

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
    testRunner = TestRunners.newTestRunner(GetAvailabilityProcessor.class);
    testUrl = "http://" + HOST + ":" + instanceRule.port() + BASE_URI;
  }

  @Test
  public void testProcessorSingleValueReturnsAvailable() throws JsonProcessingException {
    Instant startTime = Instant.ofEpochSecond(0);
    Instant endTime = startTime.plus(5, ChronoUnit.MINUTES);
    Map<UUID, Double> availabilityMap = new HashMap<>();
    availabilityMap.put(UUID.randomUUID(), 0.5);
    String rawChannelIds = availabilityMap.keySet().stream()
        .map(UUID::toString)
        .collect(Collectors.joining(","));

    configureAvailabilityRoute(BASE_URI, startTime, endTime, availabilityMap);

    testRunner.setProperty(GetAvailabilityProcessor.AVAILABILITY_URL, testUrl);
    testRunner.setProperty(GetAvailabilityProcessor.DATA_AVAILABILITY_THRESHOLD, Double.toString(0.5));
    testRunner.setProperty(GetAvailabilityProcessor.TOTAL_AVAILABILITY_THRESHOLD, Double.toString(1.0));

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put("channel-ids", rawChannelIds);
    attributesMap.put("start-time", startTime.toString());
    attributesMap.put("end-time", endTime.toString());

    testRunner.enqueue(new byte[0], attributesMap);
    testRunner.run();

    testRunner.assertAllFlowFilesTransferred(GetAvailabilityProcessor.AVAILABLE);
  }

  @Test
  public void testProcessorMultipleValuesAllAvailableReturnsAvailable()
      throws JsonProcessingException {
    Instant startTime = Instant.ofEpochSecond(0);
    Instant endTime = startTime.plus(5, ChronoUnit.MINUTES);
    Map<UUID, Double> availabilityMap = new HashMap<>();
    availabilityMap.put(UUID.randomUUID(), 0.5);
    availabilityMap.put(UUID.randomUUID(), 0.6);
    String rawChannelIds = availabilityMap.keySet().stream()
        .map(UUID::toString)
        .collect(Collectors.joining(","));

    configureAvailabilityRoute(BASE_URI, startTime, endTime, availabilityMap);

    testRunner.setProperty(GetAvailabilityProcessor.AVAILABILITY_URL, testUrl);
    testRunner.setProperty(GetAvailabilityProcessor.DATA_AVAILABILITY_THRESHOLD, Double.toString(0.5));
    testRunner
        .setProperty(GetAvailabilityProcessor.TOTAL_AVAILABILITY_THRESHOLD, Double.toString(1.0));

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put("channel-ids", rawChannelIds);
    attributesMap.put("start-time", startTime.toString());
    attributesMap.put("end-time", endTime.toString());

    testRunner.enqueue(new byte[0], attributesMap);
    testRunner.run();

    testRunner.assertAllFlowFilesTransferred(GetAvailabilityProcessor.AVAILABLE);
  }

  @Test
  public void testProcessorMultipleValuesSomeAvailableReturnsAvailable()
      throws JsonProcessingException {
    Instant startTime = Instant.ofEpochSecond(0);
    Instant endTime = startTime.plus(5, ChronoUnit.MINUTES);
    Map<UUID, Double> availabilityMap = new HashMap<>();
    availabilityMap.put(UUID.randomUUID(), 0.5);
    availabilityMap.put(UUID.randomUUID(), 0.6);
    String rawChannelIds = availabilityMap.keySet().stream()
        .map(UUID::toString)
        .collect(Collectors.joining(","));

    configureAvailabilityRoute(BASE_URI, startTime, endTime, availabilityMap);

    testRunner.setProperty(GetAvailabilityProcessor.AVAILABILITY_URL, testUrl);
    testRunner.setProperty(GetAvailabilityProcessor.DATA_AVAILABILITY_THRESHOLD, Double.toString(0.6));
    testRunner
        .setProperty(GetAvailabilityProcessor.TOTAL_AVAILABILITY_THRESHOLD, Double.toString(0.5));

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put("channel-ids", rawChannelIds);
    attributesMap.put("start-time", startTime.toString());
    attributesMap.put("end-time", endTime.toString());

    testRunner.enqueue(new byte[0], attributesMap);
    testRunner.run();

    testRunner.assertAllFlowFilesTransferred(GetAvailabilityProcessor.AVAILABLE);
  }

  @Test
  public void testProcessorMultipleValuesSomeAvailableReturnsUnavailable()
      throws JsonProcessingException {
    Instant startTime = Instant.ofEpochSecond(0);
    Instant endTime = startTime.plus(5, ChronoUnit.MINUTES);
    Map<UUID, Double> availabilityMap = new HashMap<>();
    availabilityMap.put(UUID.randomUUID(), 0.5);
    availabilityMap.put(UUID.randomUUID(), 0.6);
    String rawChannelIds = availabilityMap.keySet().stream()
        .map(UUID::toString)
        .collect(Collectors.joining(","));

    configureAvailabilityRoute(BASE_URI, startTime, endTime, availabilityMap);

    testRunner.setProperty(GetAvailabilityProcessor.AVAILABILITY_URL, testUrl);
    testRunner.setProperty(GetAvailabilityProcessor.DATA_AVAILABILITY_THRESHOLD, Double.toString(0.6));
    testRunner
        .setProperty(GetAvailabilityProcessor.TOTAL_AVAILABILITY_THRESHOLD, Double.toString(1.0));

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put("channel-ids", rawChannelIds);
    attributesMap.put("start-time", startTime.toString());
    attributesMap.put("end-time", endTime.toString());

    testRunner.enqueue(new byte[0], attributesMap);
    testRunner.run();

    testRunner.assertAllFlowFilesTransferred(GetAvailabilityProcessor.UNAVAILABLE);
  }

  @Test
  public void testProcessorSingleValueReturnsUnavailable() throws JsonProcessingException {
    Instant startTime = Instant.ofEpochSecond(0);
    Instant endTime = startTime.plus(5, ChronoUnit.MINUTES);
    Map<UUID, Double> availabilityMap = new HashMap<>();
    availabilityMap.put(UUID.randomUUID(), 0.5);
    String rawChannelIds = availabilityMap.keySet().stream()
        .map(UUID::toString)
        .collect(Collectors.joining(","));

    configureAvailabilityRoute(BASE_URI, startTime, endTime, availabilityMap);

    testRunner.setProperty(GetAvailabilityProcessor.AVAILABILITY_URL, testUrl);
    testRunner.setProperty(GetAvailabilityProcessor.DATA_AVAILABILITY_THRESHOLD, Double.toString(0.6));
    testRunner.setProperty(GetAvailabilityProcessor.TOTAL_AVAILABILITY_THRESHOLD, Double.toString(0.6));

    Map<String, String> attributesMap = new HashMap<>();
    attributesMap.put("channel-ids", rawChannelIds);
    attributesMap.put("start-time", startTime.toString());
    attributesMap.put("end-time", endTime.toString());

    testRunner.enqueue(new byte[0], attributesMap);
    testRunner.run();

    testRunner.assertAllFlowFilesTransferred(GetAvailabilityProcessor.UNAVAILABLE);
  }

  private static void configureAvailabilityRoute(String uri, Instant startTime, Instant endTime,
      Map<UUID, Double> availabilityMap) throws JsonProcessingException {
    String requestBody = buildAvailabilityRequestBody(startTime, endTime, availabilityMap.keySet());
    String responseBody = objectMapper.writeValueAsString(availabilityMap);
    givenThat(post(urlPathEqualTo(uri))
        .withRequestBody(equalToJson(requestBody))
        .willReturn(ok()
            .withBody(responseBody)));
  }

  private static String buildAvailabilityRequestBody(Instant startTime, Instant endTime,
      Collection<UUID> channelIds) throws JsonProcessingException {
    Map<String, Object> body = new HashMap<>();
    body.put("start-time", startTime);
    body.put("end-time", endTime);
    body.put("channel-ids", channelIds);

    return objectMapper.writeValueAsString(body);
  }

}
