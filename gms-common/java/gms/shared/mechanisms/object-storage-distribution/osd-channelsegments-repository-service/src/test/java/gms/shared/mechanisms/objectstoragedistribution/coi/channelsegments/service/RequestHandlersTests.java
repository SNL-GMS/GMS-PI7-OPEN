package gms.shared.mechanisms.objectstoragedistribution.coi.channelsegments.service;

import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.channelsegments.repository.ChannelSegmentsRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.channelsegments.repository.jpa.ChannelSegmentsRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import gms.shared.utilities.service.ContentType;
import gms.shared.utilities.service.HttpStatus.Code;
import gms.shared.utilities.service.Request;
import gms.shared.utilities.service.Response;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RequestHandlersTests {

  private RequestHandlers requestHandlers;
  private Request request;

  private ObjectMapper jsonObjectMapper;
  private ObjectMapper msgPackObjectMapper;

  private ChannelSegmentsRepository mockChannelSegmentsRepository;

  @BeforeEach
  void init() {

    this.mockChannelSegmentsRepository = Mockito.mock(ChannelSegmentsRepositoryJpa.class);

    this.requestHandlers = RequestHandlers.create(this.mockChannelSegmentsRepository);

    this.request = Mockito.mock(Request.class);
    this.jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    this.msgPackObjectMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();
  }

  /*
   * Tests for /isAlive request handler
   */

  @Test
  void testIsAlive() {

    // Call isAlive request handler
    Response<String> response = this.requestHandlers.isAlive(this.request, this.jsonObjectMapper);

    // Assert that we got 200 response
    Assertions.assertEquals(Code.OK, response.getHttpStatus());
  }

  /*
   * Tests for /coi/channel-segments/query/segment-ids request handler
   */

  @Test
  void testRetrieveBySegmentIdsJson() throws Exception {

    Collection<UUID> channelSegmentIds = TestFixtures.channelSegments.stream()
        .map(ChannelSegment::getId).collect(
            Collectors.toList());

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("ids", channelSegmentIds)
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Mock that the repository returns channel segments
    given(this.mockChannelSegmentsRepository.retrieveChannelSegmentsByIds(channelSegmentIds, true))
        .willReturn(TestFixtures.channelSegments);

    // Call retrieveBySegmentIds request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveBySegmentIds(this.request, this.jsonObjectMapper);

    // Assert that we got 200 response
    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert that we got no error message
    Assertions.assertEquals(Boolean.FALSE, response.getErrorMessage().isPresent());

    // Assert that we got a response object
    Assertions.assertTrue(response.getBody().isPresent());

    // Assert that we got the correct response object
    Assertions.assertEquals(TestFixtures.channelSegments, response.getBody().get());
  }

  @Test
  void testRetrieveBySegmentIdsMsgPack() throws Exception {

    Collection<UUID> channelSegmentIds = TestFixtures.channelSegments.stream()
        .map(ChannelSegment::getId).collect(
            Collectors.toList());

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("ids", channelSegmentIds)
    );

    // Mock that the request body returns MesssagePack
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));

    // Mock that the repository returns channel segments
    given(this.mockChannelSegmentsRepository.retrieveChannelSegmentsByIds(channelSegmentIds, true))
        .willReturn(TestFixtures.channelSegments);

    // Mock that the request body returns request parameters as MessagePack
    given(this.request.getRawBody())
        .willReturn(this.msgPackObjectMapper.writeValueAsBytes(requestParams));

    // Call retrieveBySegmentIds request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveBySegmentIds(this.request, this.msgPackObjectMapper);

    // Assert that we got 200 response
    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert that we got no error message
    Assertions.assertEquals(Boolean.FALSE, response.getErrorMessage().isPresent());

    // Assert that we got a response object
    Assertions.assertTrue(response.getBody().isPresent());

    // Assert that we got the correct response object
    Assertions.assertEquals(TestFixtures.channelSegments, response.getBody().get());
  }

  @Test
  void testRetrieveBySegmentIdsWithTimeseriesTrue() throws Exception {

    Collection<UUID> channelSegmentIds = TestFixtures.channelSegments.stream()
        .map(ChannelSegment::getId).collect(
            Collectors.toList());

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("ids", channelSegmentIds),
        Map.entry("withTimeseries", Boolean.TRUE)
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Mock that the repository returns channel segments
    given(this.mockChannelSegmentsRepository.retrieveChannelSegmentsByIds(channelSegmentIds, true))
        .willReturn(TestFixtures.channelSegments);

    // Call retrieveBySegmentIds request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveBySegmentIds(this.request, this.jsonObjectMapper);

    // Assert that we got 200 response
    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert that we got no error message
    Assertions.assertEquals(Boolean.FALSE, response.getErrorMessage().isPresent());

    // Assert that we got a response object
    Assertions.assertTrue(response.getBody().isPresent());

    // Assert that we got the correct response object
    Assertions.assertEquals(TestFixtures.channelSegments, response.getBody().get());
  }

  @Test
  void testRetrieveBySegmentIdsWithTimeseriesFalse() throws Exception {

    Collection<UUID> channelSegmentIds = TestFixtures.channelSegments.stream()
        .map(ChannelSegment::getId).collect(
            Collectors.toList());

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("ids", channelSegmentIds),
        Map.entry("withTimeseries", Boolean.FALSE)
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Mock that the repository returns channel segments - we don't care if we mock return a channel
    // segment with values, we only care that the method got called with withTimeseries = false
    given(this.mockChannelSegmentsRepository.retrieveChannelSegmentsByIds(channelSegmentIds, false))
        .willReturn(TestFixtures.channelSegments);

    // Call retrieveBySegmentIds request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveBySegmentIds(this.request, this.jsonObjectMapper);

    // Assert that we got 200 response
    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert that we got no error message
    Assertions.assertEquals(Boolean.FALSE, response.getErrorMessage().isPresent());

    // Assert that we got a response object
    Assertions.assertTrue(response.getBody().isPresent());

    // Assert that we got the correct response object
    Assertions.assertEquals(TestFixtures.channelSegments, response.getBody().get());
  }

  @Test
  void testRetrieveBySegmentIdsBadWithTimeseriesParemeter() throws IOException {

    Collection<UUID> channelSegmentIds = TestFixtures.channelSegments.stream()
        .map(ChannelSegment::getId).collect(
            Collectors.toList());

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("ids", channelSegmentIds),
        Map.entry("withTimeseries", Instant.now())
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Call retrieveBySegmentIds request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveBySegmentIds(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got correct error message
    Assertions
        .assertEquals("Could not deserialize JSON field \"withTimeseries\" into " + Boolean.class,
            response.getErrorMessage().get());
  }

  @Test
  void testRetrieveBySegmentIdsMissingSegmentIdsParameter() throws IOException {

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("withTimeseries", Instant.now())
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Call retrieveBySegmentIds request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveBySegmentIds(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got correct error message
    Assertions.assertEquals("JSON field \"ids\" does not exist in the request body",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveBySegmentIdsBadSegmentIdsParameter() throws IOException {

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("ids", List.of(Instant.now()))
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Call retrieveBySegmentIds request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveBySegmentIds(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got correct error message
    TypeReference<List<UUID>> listOfUuidType = new TypeReference<List<UUID>>() {
    };
    Assertions
        .assertEquals("Could not deserialize JSON field \"ids\" into " + listOfUuidType.getType(),
            response.getErrorMessage().get());
  }

  @Test
  void testRetrieveBySegmentIdsEmptyRequestBodyJson() throws IOException {

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody()).willReturn("");

    // Call retrieveBySegmentIds request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveBySegmentIds(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got correct error message
    Assertions.assertEquals(
        "Could not deserialize request body into JsonNode: request body is empty",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveBySegmentIdsEmptyRequestBodyMsgPack() throws IOException {

    // Mock that the request body returns MessagePack
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));

    // Mock that the request body returns request parameters as MessagePack
    given(this.request.getRawBody()).willReturn(new byte[]{});

    // Call retrieveBySegmentIds request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveBySegmentIds(this.request, this.msgPackObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got correct error message
    Assertions.assertEquals(
        "Could not deserialize request body into JsonNode: request body is empty",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveBySegmentIdsBadRequestBody() throws IOException {

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody()).willReturn("asdf");

    // Call retrieveBySegmentIds request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveBySegmentIds(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got correct error message
    Assertions.assertEquals(
        "Could not deserialize request body into JsonNode: malformed request body",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveBySegmentIdsNullRequestBodyJson() throws IOException {

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody()).willReturn(null);

    // Call retrieveBySegmentIds request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveBySegmentIds(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got correct error message
    Assertions.assertEquals(
        "Could not deserialize request body into JsonNode: request body is null",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveBySegmentIdsNullRequestBodyMsgPack() throws IOException {

    // Mock that the request body returns MessagePack
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));

    // Mock that the request body returns request parameters as MessagePack
    given(this.request.getRawBody()).willReturn(null);

    // Call retrieveBySegmentIds request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveBySegmentIds(this.request, this.msgPackObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got correct error message
    Assertions.assertEquals(
        "Could not deserialize request body into JsonNode: request body is null",
        response.getErrorMessage().get());
  }

  /*
   * Tests for /coi/channel-segments/query/channel-ids request handler
   */

  @Test
  void testRetrieveByChannelIdsAndTimeJson() throws Exception {

    Collection<UUID> channelIds = TestFixtures.channelSegments.stream().map(ChannelSegment::getChannelId).collect(
        Collectors.toList());

    Instant startTime = Instant.EPOCH;

    Instant endTime = Instant.EPOCH.plusSeconds(1);

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("channelIds", channelIds),
        Map.entry("startTime", startTime),
        Map.entry("endTime", endTime)
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Mock that the repository returns channel segments
    given(this.mockChannelSegmentsRepository.retrieveChannelSegmentsByChannelIds(channelIds, startTime, endTime))
        .willReturn(TestFixtures.channelSegments);

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.jsonObjectMapper);

    // Assert that we got 200 response
    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert that we got no error message
    Assertions.assertEquals(Boolean.FALSE, response.getErrorMessage().isPresent());

    // Assert that we got a response object
    Assertions.assertTrue(response.getBody().isPresent());

    // Assert that we got the correct response object
    Assertions.assertEquals(TestFixtures.channelSegments, response.getBody().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeMsgPack() throws Exception {

    Collection<UUID> channelIds = TestFixtures.channelSegments.stream().map(ChannelSegment::getChannelId).collect(
        Collectors.toList());

    Instant startTime = Instant.EPOCH;

    Instant endTime = Instant.EPOCH.plusSeconds(1);

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("channelIds", channelIds),
        Map.entry("startTime", startTime),
        Map.entry("endTime", endTime)
    );

    // Mock that the request body returns MessagePack
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));

    // Mock that the request body returns request parameters as MessagePack
    given(this.request.getRawBody())
        .willReturn(this.msgPackObjectMapper.writeValueAsBytes(requestParams));

    // Mock that the repository returns channel segments
    given(this.mockChannelSegmentsRepository.retrieveChannelSegmentsByChannelIds(channelIds, startTime, endTime))
        .willReturn(TestFixtures.channelSegments);

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.msgPackObjectMapper);

    // Assert that we got 200 response
    Assertions.assertEquals(Code.OK, response.getHttpStatus());

    // Assert that we got no error message
    Assertions.assertEquals(Boolean.FALSE, response.getErrorMessage().isPresent());

    // Assert that we got a response object
    Assertions.assertTrue(response.getBody().isPresent());

    // Assert that we got the correct response object
    Assertions.assertEquals(TestFixtures.channelSegments, response.getBody().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeStartTimeAfterEndTime() throws IOException {

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("channelIds", List.of(UUID.randomUUID())),
        Map.entry("startTime", Instant.now()),
        Map.entry("endTime", Instant.now().minus(Duration.ofDays(1)))
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got the request error message
    TypeReference<List<UUID>> listOfUuidType = new TypeReference<List<UUID>>() {
    };
    Assertions.assertEquals(
        "\"startTime\" parameter cannot be after \"endTime\" parameter",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeBadChannelIdsParameter() throws IOException {

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("channelIds", List.of(Instant.now())),
        Map.entry("startTime", Instant.now().minus(Duration.ofDays(1))),
        Map.entry("endTime", Instant.now())
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got the request error message
    TypeReference<List<UUID>> listOfUuidType = new TypeReference<List<UUID>>() {
    };
    Assertions.assertEquals(
        "Could not deserialize JSON field \"channelIds\" into " + listOfUuidType.getType(),
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeMissingChannelIdsParameter() throws IOException {

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("startTime", Instant.now().minus(Duration.ofDays(1))),
        Map.entry("endTime", Instant.now())
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got the request error message
    Assertions.assertEquals(
        "JSON field \"channelIds\" does not exist in the request body",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeBadStartTimeParameter() throws IOException {

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("channelIds", List.of(UUID.randomUUID())),
        Map.entry("startTime", "asdf"),
        Map.entry("endTime", Instant.now())
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got the request error message
    Assertions.assertEquals(
        "Could not deserialize JSON field \"startTime\" into " + Instant.class,
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeMissingStartTimeParameter() throws IOException {

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("channelIds", List.of(UUID.randomUUID())),
        Map.entry("endTime", Instant.now())
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got the request error message
    Assertions.assertEquals(
        "JSON field \"startTime\" does not exist in the request body",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeBadEndTimeParameter() throws IOException {

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("channelIds", List.of(UUID.randomUUID())),
        Map.entry("startTime", Instant.now().minus(Duration.ofDays(1))),
        Map.entry("endTime", "asdf")
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got the request error message
    Assertions.assertEquals(
        "Could not deserialize JSON field \"endTime\" into " + Instant.class,
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeMissingEndTimeParameter() throws IOException {

    // Create request parameters
    Map<String, Object> requestParams = Map.ofEntries(
        Map.entry("channelIds", List.of(UUID.randomUUID())),
        Map.entry("startTime", Instant.now().minus(Duration.ofDays(1)))
    );

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody())
        .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got the request error message
    Assertions.assertEquals(
        "JSON field \"endTime\" does not exist in the request body",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeEmptyRequestBodyJson() {

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody()).willReturn("");

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got the request error message
    Assertions.assertEquals(
        "Could not deserialize request body into JsonNode: request body is empty",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeEmptyRequestBodyMessagePack() {

    // Mock that the request body returns MessagePack
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));

    // Mock that the request body returns request parameters as MessagePack
    given(this.request.getRawBody()).willReturn(new byte[]{});

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.msgPackObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got the request error message
    Assertions.assertEquals(
        "Could not deserialize request body into JsonNode: request body is empty",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeBadRequestBody() {

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody()).willReturn("asdf");

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got the request error message
    Assertions.assertEquals(
        "Could not deserialize request body into JsonNode: malformed request body",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeNullRequestBodyJson() throws IOException {

    // Mock that the request body returns request parameters as JSON
    given(this.request.getBody()).willReturn(null);

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.jsonObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got the request error message
    Assertions.assertEquals(
        "Could not deserialize request body into JsonNode: request body is null",
        response.getErrorMessage().get());
  }

  @Test
  void testRetrieveByChannelIdsAndTimeNullRequestBodyMsgPack() throws IOException {

    // Mock that the request body returns MessagePack
    given(this.request.getContentType()).willReturn(Optional.of(ContentType.APPLICATION_MSGPACK));

    // Mock that the request body returns request parameters as MessagePack
    given(this.request.getRawBody()).willReturn(null);

    // Call retrieveByChannelIdsAndTime request handler
    Response<Collection<ChannelSegment<? extends Timeseries>>> response = this.requestHandlers
        .retrieveByChannelIdsAndTime(this.request, this.msgPackObjectMapper);

    // Assert that we got 400 response
    Assertions.assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // Assert that we got an error message
    Assertions.assertEquals(Boolean.TRUE, response.getErrorMessage().isPresent());

    // Assert that we got the request error message
    Assertions.assertEquals(
        "Could not deserialize request body into JsonNode: request body is null",
        response.getErrorMessage().get());
  }
}
