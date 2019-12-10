/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * work for additional information regarding copyright ownership.
 * The ASF licenses file to You under the Apache License, Version 2.0
 * (the "License"); you may not use file except in compliance with
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
package gms.processors.invokeinterval;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;


public class InvokeIntervalProcessorTest {

  private TestRunner testRunner;

  //  @Rule
  //  public final ExpectedException exception = ExpectedException.none();

  @ClassRule
  public static WireMockClassRule wireMockRule = new WireMockClassRule(
      wireMockConfig().dynamicPort());

  @Rule
  public WireMockClassRule instanceRule = wireMockRule;

  /*
    Some valid testing constants.
   */
  private static final String TEST_STATION_NAME = "alderaan";
  private static final String TEST_STATION_TYPE = "SeismicArray";

  private static final String HOST = "localhost";
  private static final String BASE_URI = "/mechanisms/object-storage-distribution/station-reference/stations/processing/name";

  private static final Duration TEST_INTERVAL_LENGTH = Duration.parse("PT10S");
  private static final Duration TEST_INTERVAL_DELAY = Duration.parse("PT30S");

  /*
    Some invalid testing constants
   */
  private static final String BAD_TEST_INTERVAL_LENGTH = "PT30";
  private static final String BAD_TEST_INTERVAL_DELAY = "P";

  private static ObjectMapper objectMapper;

  private String testURL;
  private String testAPIEndpoint;
  private String testAPIResponse;
  private String testAPIResponseNoSites;

  @Before
  public void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Before
  public void init() {
    testRunner = TestRunners.newTestRunner(InvokeIntervalProcessor.class);
    testURL = "http://" + HOST + ":" + instanceRule.port() + BASE_URI;
    testAPIEndpoint = testURL + "/" + TEST_STATION_NAME;

    testRunner.getLogger().debug("testURL = " + testURL);
    testRunner.getLogger().debug("testAPIEndpoint = " + testAPIEndpoint);

    testAPIResponse = "{'id':'830f4ac8-24ee-4e49-a18d-c346a67383e5','name':'ALDR','stationType':'SeismicArray','sites':[{'channels':[{'id':'eacad8b4-8ddd-4db3-81e6-7341c2a67771','name':'XWING'},{'id':'29ac768f-f2ba-418c-a03d-3455dbf02b5d','name':'BWING'}],'id':'1f5b95a0-779b-40dd-81dc-0b7ff05f2ec3','name':'ALDR01'},{'channels':[{'id':'572ef85b-661d-4d53-932e-2e9f9f3e69d8','name':'YT1300'},{'id':'61134547-26a0-4570-a0cf-8d06cfb58ecb','name':'YWING'}],'id':'d9d5e7bf-4eeb-48bf-9aec-f379af6ff5bc','name':'ALDR02'},{'channels':[{'id':'399e9ad2-d3e4-496b-b596-311c21193077','name':'DEATHSTAR'},{'id':'126fa66e-606e-4936-87cf-46d38cc6cc62','name':'TIEFIGHTER'},{'id':'d66e1c8c-2315-47c9-83dc-c5422c939fdb','name':'AWING'}],'id':'b852bc50-cea4-4387-847d-124ae9efb4dc','name':'ALDR03'}]}";
    testAPIResponseNoSites = "{'id':'830f4ac8-24ee-4e49-a18d-c346a67383e5','name':'ALDR','sites':[]}";

    testRunner.getLogger().debug("testAPIResponse = " + testAPIResponse);
  }

  @Test
  public void testProcessorInvalidIntervalLength() {
    /*
      Initialize processor with test values.
     */
    testRunner.setProperty(InvokeIntervalProcessor.OSD_GATEWAY_URL, testURL);
    testRunner.setProperty(InvokeIntervalProcessor.STATION_NAME, TEST_STATION_NAME);

    testRunner.setProperty(InvokeIntervalProcessor.INTERVAL_DELAY, TEST_INTERVAL_DELAY.toString());
    testRunner
        .setProperty(InvokeIntervalProcessor.INTERVAL_LENGTH, BAD_TEST_INTERVAL_LENGTH);

    testRunner.assertNotValid();
  }

  @Test
  public void testProcessorInvalidIntervalDelay() {
    /*
      Initialize processor with test values.
     */
    testRunner.setProperty(InvokeIntervalProcessor.OSD_GATEWAY_URL, testURL);
    testRunner.setProperty(InvokeIntervalProcessor.STATION_NAME, TEST_STATION_NAME);

    testRunner.setProperty(InvokeIntervalProcessor.INTERVAL_DELAY, BAD_TEST_INTERVAL_DELAY);
    testRunner
        .setProperty(InvokeIntervalProcessor.INTERVAL_LENGTH, TEST_INTERVAL_LENGTH.toString());

    testRunner.assertNotValid();
  }

  @Test
  public void testProcessorInvalidChannelName() {
    testRunner.setProperty(InvokeIntervalProcessor.OSD_GATEWAY_URL, testURL);
    testRunner.setProperty(InvokeIntervalProcessor.STATION_NAME, TEST_STATION_NAME);
    testRunner.setProperty(InvokeIntervalProcessor.CHANNEL_NAME, "[");

    testRunner.setProperty(InvokeIntervalProcessor.INTERVAL_DELAY, TEST_INTERVAL_DELAY.toString());
    testRunner
        .setProperty(InvokeIntervalProcessor.INTERVAL_LENGTH, TEST_INTERVAL_LENGTH.toString());

    testRunner.assertNotValid();
  }

  @Test
  public void testProcessorValidProperties() {
    /*
      Initialize processor with test values.
     */
    testRunner.setProperty(InvokeIntervalProcessor.OSD_GATEWAY_URL, testURL);
    testRunner.setProperty(InvokeIntervalProcessor.STATION_NAME, TEST_STATION_NAME);

    testRunner.setProperty(InvokeIntervalProcessor.INTERVAL_DELAY, TEST_INTERVAL_DELAY.toString());
    testRunner
        .setProperty(InvokeIntervalProcessor.INTERVAL_LENGTH, TEST_INTERVAL_LENGTH.toString());

    testRunner.assertValid();
  }

  @Test
  public void test404NoFlowFileGeneration() {
    /*
      Initialize processor with test values.
     */
    testRunner.getLogger().debug("Adding properties to processor");
    testRunner.setProperty(InvokeIntervalProcessor.OSD_GATEWAY_URL, testURL);
    testRunner.setProperty(InvokeIntervalProcessor.STATION_NAME, TEST_STATION_NAME);

    testRunner.setProperty(InvokeIntervalProcessor.INTERVAL_DELAY, TEST_INTERVAL_DELAY.toString());
    testRunner
        .setProperty(InvokeIntervalProcessor.INTERVAL_LENGTH, TEST_INTERVAL_LENGTH.toString());

    /*
      Stub out the API response for the testing station.
     */
    testRunner.getLogger()
        .debug("Stubbing response for " + BASE_URI + "/" + TEST_STATION_NAME);
    givenThat(get(urlEqualTo(BASE_URI + "/" + TEST_STATION_NAME))
        .willReturn(notFound()
            .withHeader("Content-Type", "application/json")
            .withBody("")));

    /*
      Actually run the processor.
     */
    testRunner.getLogger().debug("Processor under test: " + testRunner.getProcessor().toString());
    testRunner.run();
    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(InvokeIntervalProcessor.SUCCESS);
    assertThat(testRunner.getProvenanceEvents().size(), is(0));
    assertThat(testRunner.getFlowFilesForRelationship(InvokeIntervalProcessor.SUCCESS).size(),
        is(0));
  }

  @Test
  public void testNoSitesNoFlowFileGeneration() {
    /*
      Initialize processor with test values.
     */
    testRunner.getLogger().debug("Adding properties to processor");
    testRunner.setProperty(InvokeIntervalProcessor.OSD_GATEWAY_URL, testURL);
    testRunner.setProperty(InvokeIntervalProcessor.STATION_NAME, TEST_STATION_NAME);
    testRunner.setProperty(InvokeIntervalProcessor.INTERVAL_DELAY, TEST_INTERVAL_DELAY.toString());
    testRunner
            .setProperty(InvokeIntervalProcessor.INTERVAL_LENGTH, TEST_INTERVAL_LENGTH.toString());

    /*
      Stub out the API response for the testing station.
     */
    testRunner.getLogger()
            .debug("Stubbing response for " + BASE_URI + "/" + TEST_STATION_NAME);
    givenThat(get(urlEqualTo(BASE_URI + "/" + TEST_STATION_NAME))
            .willReturn(notFound()
                    .withHeader("Content-Type", "application/json")
                    .withBody(testAPIResponseNoSites)));

    /*
      Actually run the processor.
     */
    testRunner.getLogger().debug("Processor under test: " + testRunner.getProcessor().toString());
    testRunner.run();
    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(InvokeIntervalProcessor.SUCCESS);
    assertThat(testRunner.getProvenanceEvents().size(), is(0));
    assertThat(testRunner.getFlowFilesForRelationship(InvokeIntervalProcessor.SUCCESS).size(),
            is(0));
  }

  @Test
  public void testFlowFileGeneration() {

    /*
      Initialize processor with test values.
     */
    testRunner.getLogger().debug("Adding properties to processor");
    testRunner.setProperty(InvokeIntervalProcessor.OSD_GATEWAY_URL, testURL);
    testRunner.setProperty(InvokeIntervalProcessor.STATION_NAME, TEST_STATION_NAME);

    testRunner.setProperty(InvokeIntervalProcessor.INTERVAL_DELAY, TEST_INTERVAL_DELAY.toString());
    testRunner
        .setProperty(InvokeIntervalProcessor.INTERVAL_LENGTH, TEST_INTERVAL_LENGTH.toString());

    /*
      Stub out the API response for the testing station.
     */
    testRunner.getLogger()
        .debug("Stubbing response for " + BASE_URI + "/" + TEST_STATION_NAME);

    givenThat(get(urlEqualTo(BASE_URI + "/" + TEST_STATION_NAME))
        .willReturn(ok()
            .withHeader("Content-Type", "application/json")
            .withBody(testAPIResponse)));

    /*
      Actually run the processor.
     */
    testRunner.getLogger().debug("Processor under test: " + testRunner.getProcessor().toString());
    testRunner.run();

    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(InvokeIntervalProcessor.SUCCESS);

    List<ProvenanceEventRecord> provenanceEvents = testRunner.getProvenanceEvents();
    assertThat(provenanceEvents.size(), is(1));
    assertThat(provenanceEvents.get(0).getEventType(), is(ProvenanceEventType.CREATE));

    List<MockFlowFile> flowFiles = testRunner
        .getFlowFilesForRelationship(InvokeIntervalProcessor.SUCCESS);
    assertThat(flowFiles.size(), is(1));
    MockFlowFile flowFile = flowFiles.get(0);

    assertThat(flowFile.getAttribute("station-name"),
        is(both(notNullValue()).and(equalTo(TEST_STATION_NAME))));

    assertThat(flowFile.getAttribute("station-type"),
        is(both(notNullValue()).and(equalTo(TEST_STATION_TYPE))));

    assertThat(flowFile.getAttribute("station-id"),
        is(both(notNullValue()).and(equalTo("830f4ac8-24ee-4e49-a18d-c346a67383e5"))));

    assertThat(flowFile.getAttribute("start-time"), is(notNullValue()));
    assertThat(flowFile.getAttribute("end-time"), is(notNullValue()));

    Instant actualStart = Instant.parse(flowFile.getAttribute("start-time"));
    Instant actualEnd = Instant.parse(flowFile.getAttribute("end-time"));

    assertThat(Duration.between(actualStart, actualEnd), is(TEST_INTERVAL_LENGTH));

    assertThat(flowFile.getAttribute("channel-ids"), is(notNullValue()));
    assertThat(flowFile.getAttribute("channel-ids").split(",").length, is(7));

    assertThat(flowFile.getAttribute("initialization-time"), is(notNullValue()));

  }

  @Test
  public void testFlowFileGenerationFilterChannels() {
    /*
      Initialize processor with test values.
     */
    testRunner.getLogger().debug("Adding properties to processor");
    testRunner.setProperty(InvokeIntervalProcessor.OSD_GATEWAY_URL, testURL);
    testRunner.setProperty(InvokeIntervalProcessor.STATION_NAME, TEST_STATION_NAME);

    testRunner.setProperty(InvokeIntervalProcessor.INTERVAL_DELAY, TEST_INTERVAL_DELAY.toString());
    testRunner
        .setProperty(InvokeIntervalProcessor.INTERVAL_LENGTH, TEST_INTERVAL_LENGTH.toString());
    testRunner.setProperty(InvokeIntervalProcessor.CHANNEL_NAME, ".WING");

    /*
      Stub out the API response for the testing station.
     */
    testRunner.getLogger()
        .debug("Stubbing response for " + BASE_URI + "/" + TEST_STATION_NAME);

    givenThat(get(urlEqualTo(BASE_URI + "/" + TEST_STATION_NAME))
        .willReturn(ok()
            .withHeader("Content-Type", "application/json")
            .withBody(testAPIResponse)));

    /*
      Actually run the processor.
     */
    testRunner.getLogger().debug("Processor under test: " + testRunner.getProcessor().toString());
    testRunner.run();

    testRunner.assertQueueEmpty();
    testRunner.assertAllFlowFilesTransferred(InvokeIntervalProcessor.SUCCESS);

    List<ProvenanceEventRecord> provenanceEvents = testRunner.getProvenanceEvents();
    assertThat(provenanceEvents.size(), is(1));
    assertThat(provenanceEvents.get(0).getEventType(), is(ProvenanceEventType.CREATE));

    List<MockFlowFile> flowFiles = testRunner
        .getFlowFilesForRelationship(InvokeIntervalProcessor.SUCCESS);
    assertThat(flowFiles.size(), is(1));
    MockFlowFile flowFile = flowFiles.get(0);

    assertThat(flowFile.getAttribute("station-name"),
        is(both(notNullValue()).and(equalTo(TEST_STATION_NAME))));

    assertThat(flowFile.getAttribute("station-type"),
        is(both(notNullValue()).and(equalTo(TEST_STATION_TYPE))));

    assertThat(flowFile.getAttribute("station-id"),
        is(both(notNullValue()).and(equalTo("830f4ac8-24ee-4e49-a18d-c346a67383e5"))));

    assertThat(flowFile.getAttribute("start-time"), is(notNullValue()));
    assertThat(flowFile.getAttribute("end-time"), is(notNullValue()));

    Instant actualStart = Instant.parse(flowFile.getAttribute("start-time"));
    Instant actualEnd = Instant.parse(flowFile.getAttribute("end-time"));

    assertThat(Duration.between(actualStart, actualEnd), is(TEST_INTERVAL_LENGTH));

    assertThat(flowFile.getAttribute("channel-ids"), is(notNullValue()));
    assertThat(flowFile.getAttribute("channel-ids").split(",").length, is(4));

    assertThat(flowFile.getAttribute("initialization-time"), is(notNullValue()));
  }

}
