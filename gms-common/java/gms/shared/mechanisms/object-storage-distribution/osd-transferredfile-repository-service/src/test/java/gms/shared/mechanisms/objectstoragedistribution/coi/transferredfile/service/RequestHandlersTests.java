package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFile;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.TransferredFileRepositoryInterface;
import gms.shared.utilities.service.HttpStatus.Code;
import gms.shared.utilities.service.Request;
import gms.shared.utilities.service.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class RequestHandlersTests {

  private RequestHandlers requestHandlers;
  private Request request;

  private final ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private final ObjectMapper msgPackObjectMapper = CoiObjectMapperFactory.getMsgpackObjectMapper();

  @Mock
  private TransferredFileRepositoryInterface mockTransferredFileRepository;

  @BeforeEach
  void init() {

    MockitoAnnotations.initMocks(this);

    this.requestHandlers = new RequestHandlers(mockTransferredFileRepository);

    this.request = Mockito.mock(Request.class);
  }

  /*
   * Tests for /isAlive request handler
   */

  @Test
  void testIsAlive() {

    // Call isAlive request handler
    Response<String> response = this.requestHandlers.isAlive(this.request, this.jsonObjectMapper);

    // Assert that we got 200 response
    assertEquals(Code.OK, response.getHttpStatus());

    // assert that the response body is not null
    assertNotNull(response.getBody());

    // assert that we didn't receive an error message
    assertFalse(response.getErrorMessage().isPresent());
  }

  /*
   * Tests for retrieving transferred files by various time range with valid JSON requests
   */
  @Test
  void testRetrievingAllTransferredFilesEmptyStringBodyJson() throws Exception {

    // mock that the repo will return all TransferredFiles when given no time range
    given(this.mockTransferredFileRepository.retrieveAll())
            .willReturn((TestFixtures.tfs));

    // create a request param as the empty string
    String requestParams = "";

    // mock that the request body returns request parameters as JSON
    given(this.request.getBody())
            .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));


    // call the TransferredFile request handler that retrieves TransferredFiles within a specified time range
    Response<List<TransferredFile>> response = this.requestHandlers.retrieveTransferredFilesByTimeRange(this.request,
            this.jsonObjectMapper);

    // assert that we received a 200 OK, there was no error message, the response body was not null,
    // the number of TransferredFiles received was as expected, and the content of the TransferredFiles received is as expected
    isExpectedResponseValidRequest(response, TestFixtures.tfs);
  }

  @Test
  void testRetrievingSubsetOfTransferredFilesByTimeRangeJson() throws Exception {
    // mock that the repo returns the 2 transferred files with transfer time 30 seconds ago to 5 seconds ago
    // when given that time range
    given(this.mockTransferredFileRepository.retrieveByTransferTime(TestFixtures.nowMinusThirtySeconds,
            TestFixtures.nowMinusFiveSeconds))
            .willReturn(List.of(TestFixtures.transferredRawStationDataFrame2,
                    TestFixtures.transferredRawStationDataFrame3));

    // create the request params
    Map<String, Object> requestParams = timeRangeRequestParamBuilder(TestFixtures.nowMinusThirtySeconds, TestFixtures.nowMinusFiveSeconds);

    // mock that the request body returns request parameters as JSON
    given(this.request.getBody())
            .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // call the TransferredFile request handler that retrieves TransferredFiles within a specified time range
    Response<List<TransferredFile>> response = this.requestHandlers.retrieveTransferredFilesByTimeRange(this.request,
            this.jsonObjectMapper);

    // assert that we received a 200 OK, there was no error message, the response body was not null,
    // the number of TransferredFiles received was as expected, and the content of the TransferredFiles received is as expected
    isExpectedResponseValidRequest(response, List.of(TestFixtures.transferredRawStationDataFrame2,
            TestFixtures.transferredRawStationDataFrame3));
  }


  @Test
  void testRetrieveZeroTransferredFilesWhenNoneInTimeRangeJson() throws Exception {
    // mock that the repo returns 0 TransferredFiles when none exist in that time range
    given(this.mockTransferredFileRepository.retrieveByTransferTime(TestFixtures.epochTime,
            TestFixtures.epochTimePlusOneSecond))
            .willReturn(List.of());

    // create the request params
    Map<String, Object> requestParams = timeRangeRequestParamBuilder(TestFixtures.epochTime, TestFixtures.epochTimePlusOneSecond);

    // mock that the request body returns request parameters as JSON
    given(this.request.getBody())
            .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // call the TransferredFile request handler that retrieves TransferredFiles within a specified time range
    Response<List<TransferredFile>> response = this.requestHandlers.retrieveTransferredFilesByTimeRange(this.request,
            this.jsonObjectMapper);

    // assert that we received a 200 OK, there was no error message, the response body was not null,
    // the number of TransferredFiles received was as expected, and the content of the TransferredFiles received is as expected
    isExpectedResponseValidRequest(response, List.of());
  }


  /*
   * Test retrieving all transferred files when no time range params are given in a valid msgpack request
   */
  @Test
  void testRetrievingAllTransferredFilesEmptyBodyMsgPack() throws Exception {
    // mock that the repo will return all TransferredFiles when given no time range
    given(this.mockTransferredFileRepository.retrieveAll())
            .willReturn(TestFixtures.tfs);

    // tag the request as msgpack
    given(this.request.clientSentMsgpack()).willReturn(true);

    // create a request param as the empty string
    String requestParams = "";

    // mock that the request body returns request parameters as msgpack
    given(this.request.getRawBody())
            .willReturn(this.msgPackObjectMapper.writeValueAsBytes(requestParams));

    // call the TransferredFile request handler that retrieves TransferredFiles within a specified time range
    Response<List<TransferredFile>> response = this.requestHandlers.retrieveTransferredFilesByTimeRange(this.request,
            this.msgPackObjectMapper);

    // assert that we received a 200 OK, there was no error message, the response body was not null,
    // the number of TransferredFiles received was as expected, and the content of the TransferredFiles received is as expected
    isExpectedResponseValidRequest(response, TestFixtures.tfs);
  }

  @Test
  void testRetrievingSubsetOfTransferredFilesByTimeRangeMsgpack() throws Exception {
    // mock that the repo returns only the 2 transferred files with transfer time 30 seconds ago to 5 seconds ago
    // when given that time range
    given(this.mockTransferredFileRepository.retrieveByTransferTime(TestFixtures.nowMinusThirtySeconds,
            TestFixtures.nowMinusFiveSeconds))
            .willReturn(List.of(TestFixtures.transferredRawStationDataFrame2,
                    TestFixtures.transferredRawStationDataFrame3));

    // create the request params
    Map<String, Object> requestParams = timeRangeRequestParamBuilder(TestFixtures.nowMinusThirtySeconds,
            TestFixtures.nowMinusFiveSeconds);

    // mock that the request body returns request parameters as MessagePack
    given(this.request.getRawBody())
            .willReturn(this.msgPackObjectMapper.writeValueAsBytes(requestParams));

    // tag the request as msgpack
    given(this.request.clientSentMsgpack()).willReturn(true);

    // call the TransferredFile request handler that retrieves TransferredFiles within a specified time range
    Response<List<TransferredFile>> response = this.requestHandlers.retrieveTransferredFilesByTimeRange(this.request,
            this.msgPackObjectMapper);

    // assert that we received a 200 OK, there was no error message, the response body was not null,
    // the number of TransferredFiles received was as expected, and the content of the TransferredFiles received is as expected
    isExpectedResponseValidRequest(response, List.of(TestFixtures.transferredRawStationDataFrame2,
            TestFixtures.transferredRawStationDataFrame3));

  }


  @Test
  void testRetrieveZeroTransferredFilesWhenNoneInTimeRangeMsgpack() throws Exception {
    // mock that the repo returns 0 TransferredFiles when none exist in that time range
    given(this.mockTransferredFileRepository.retrieveByTransferTime(TestFixtures.epochTime,
            TestFixtures.epochTimePlusOneSecond))
            .willReturn(List.of());

    // create the request params
    Map<String, Object> requestParams = timeRangeRequestParamBuilder(TestFixtures.epochTime, TestFixtures.epochTimePlusOneSecond);

    // mock that the request body returns request parameters as MessagePack
    given(this.request.getRawBody())
            .willReturn(this.msgPackObjectMapper.writeValueAsBytes(requestParams));

    // tag the request as msgpack
    given(this.request.clientSentMsgpack()).willReturn(true);

    // call the TransferredFile request handler that retrieves TransferredFiles within a specified time range
    Response<List<TransferredFile>> response = this.requestHandlers.retrieveTransferredFilesByTimeRange(this.request,
            this.msgPackObjectMapper);

    // assert that we received a 200 OK, there was no error message, the response body was not null,
    // the number of TransferredFiles received was as expected, and the content of the TransferredFiles received is as expected
    isExpectedResponseValidRequest(response, List.of());
  }

  /*
   * Test attempting to retrieve TransferredFiles by time range with a missing start time param
   * Expect to receive a 400 bad request
   */
  @Test()
  void testExpectClientErrorRetrievingTransferredFilesByTimeRangeMissingStartTimeParameter() throws
          Exception {
    // create the request params
    Map<String, Object> requestParams = Map.ofEntries(
            Map.entry("transferStartTime", ""),
            Map.entry("transferEndTime", TestFixtures.now)
    );

    // mock that the request body would return request parameters as JSON
    given(this.request.getBody())
            .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // provide bad params to the handler
    Response<List<TransferredFile>> response = this.requestHandlers.retrieveTransferredFilesByTimeRange(this.request,
            this.jsonObjectMapper);

    // assert that we got a 400 BAD REQUEST response
    assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // assert that we received an error message
    assertTrue(response.getErrorMessage().isPresent());
  }

  /*
   * Test attempting to retrieve TransferredFiles by time range with a missing end time param
   * Expect to receive a 400 bad request
   */

  @Test()
  void testExpectClientErrorRetrievingTransferredFilesByTimeRangeMissingEndTimeParameter() throws
          Exception {
    // create the request params
    Map<String, Object> requestParams = Map.ofEntries(
            Map.entry("transferStartTime", TestFixtures.epochTime),
            Map.entry("transferEndTime", "")
    );

    // mock that the request body would return request parameters as JSON
    given(this.request.getBody())
            .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // provide bad params to the handler
    Response<List<TransferredFile>> response = this.requestHandlers.retrieveTransferredFilesByTimeRange(this.request,
            this.jsonObjectMapper);

    // assert that we got a 400 BAD REQUEST response
    assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // assert that we received an error message
    assertTrue(response.getErrorMessage().isPresent());
  }


  /*
   * Tests for retrieving transferred files by time range with the start time param after the end time.
   * Expect to receive a 400 BAD REQUEST
   */

  @Test
  void testExpectClientErrorRetrievingTransferredFilesByTimeRangeStartTimeAfterEndTime() throws Exception {
    // create the request params
    Map<String, Object> requestParams = timeRangeRequestParamBuilder(TestFixtures.now, TestFixtures.epochTime);

    // mock that the request body would return request parameters as JSON
    given(this.request.getBody())
            .willReturn(this.jsonObjectMapper.writeValueAsString(requestParams));

    // provide an invalid time range to the handler
    Response<List<TransferredFile>> response = this.requestHandlers.retrieveTransferredFilesByTimeRange(this.request,
            this.jsonObjectMapper);

    // assert that we got a 400 BAD REQUEST response
    assertEquals(Code.BAD_REQUEST, response.getHttpStatus());

    // assert that we received an error message
    assertTrue(response.getErrorMessage().isPresent());


  }

  /*
   * Tests for retrieving transferred files by time range with a bad request body.
   * Expect to receive a 400 bad request
   */

  @Test
  void testExpectAllFilesWhenNoValidParamsArePassed() throws Exception {

    // mock that the repo will return all TransferredFiles when given no time range
    given(this.mockTransferredFileRepository.retrieveAll())
            .willReturn((TestFixtures.tfs));

    // mock a bad JSON request body
    given(this.request.getBody()).willReturn(this.jsonObjectMapper.writeValueAsString("nonsense"));

    // providing a request body with irrelevant parameters
    Response<List<TransferredFile>> response = this.requestHandlers.retrieveTransferredFilesByTimeRange(this.request,
            this.jsonObjectMapper);

    isExpectedResponseValidRequest(response, TestFixtures.tfs);

  }

  /**
   * Build a good request for a time range.
   *
   * @param startTime The start time to bound the request by
   * @param endTime   THe end time to bound the request by
   * @return a Map of query strings to provided times.
   */
  private Map<String, Object> timeRangeRequestParamBuilder(Instant startTime, Instant endTime) {
    return Map.of(
            "transferStartTime", startTime,
            "transferEndTime", endTime
    );
  }

  /**
   * Make a number of assertions about a response.
   *
   * @param response      The Response object
   * @param expectedFiles The TransferredFiles expected to be returned
   */
  private void isExpectedResponseValidRequest(Response<List<TransferredFile>> response,
                                              Collection<TransferredFile> expectedFiles) {
    // get the response body
    Optional<List<TransferredFile>> responseBody = response.getBody();
    List<TransferredFile> tfs = responseBody.orElse(null);

    assertEquals(response.getHttpStatus(), Code.OK);
    assertFalse(response.getErrorMessage().isPresent());
    assertNotNull(tfs);
    assertEquals(expectedFiles.size(), tfs.size());
    assertEquals(expectedFiles, tfs);
  }

}
