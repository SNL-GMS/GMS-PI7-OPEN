package gms.core.signalenhancement.waveformfiltering.http;

import static gms.core.signalenhancement.waveformfiltering.http.ContentType.APPLICATION_JSON;
import static gms.core.signalenhancement.waveformfiltering.http.ContentType.APPLICATION_MSGPACK;
import static gms.core.signalenhancement.waveformfiltering.http.ContentType.TEXT_PLAIN;
import static gms.core.signalenhancement.waveformfiltering.http.ContentType.UNKNOWN;
import static gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory.getJsonObjectMapper;
import static gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory.getMsgpackObjectMapper;
import static org.eclipse.jetty.http.HttpStatus.NOT_ACCEPTABLE_406;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.eclipse.jetty.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE_415;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.core.signalenhancement.waveformfiltering.TestFixtures;
import gms.core.signalenhancement.waveformfiltering.control.FilterControl;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentProcessingResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentStorageResponse;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link FilterControlRouteHandler}
 */
@ExtendWith(MockitoExtension.class)
class FilterControlRouteHandlerTests {

  private static StreamingRequest streamingRequest = TestFixtures.getStreamingRequest();
  private static ChannelSegmentDescriptor claimCheckRequest = requestDescriptor();
  private static List<ChannelSegment<Waveform>> outputWaveforms = outputWaveforms();
  private static ChannelSegmentStorageResponse storageResponse = storageResponse(outputWaveforms);
  private static ChannelSegmentProcessingResponse processingResponse = processingResponse(
      storageResponse);

  @Mock
  private FilterControl mockFilterControl;

  private FilterControlRouteHandler routeHandler;

  @BeforeEach
  void setUp() {
    routeHandler = FilterControlRouteHandler.create(mockFilterControl);
  }

  private static List<ChannelSegment<Waveform>> outputWaveforms() {
    return List
        .of(TestFixtures.randomChannelSegment(), TestFixtures.randomChannelSegment());
  }

  private static ChannelSegmentStorageResponse storageResponse(
      Collection<ChannelSegment<Waveform>> storedWaveforms) {
    ChannelSegmentStorageResponse.Builder builder = ChannelSegmentStorageResponse.builder();

    for (ChannelSegment<Waveform> storedWaveform : storedWaveforms) {
      builder.addStored(ChannelSegmentDescriptor.from(storedWaveform));
    }
    return builder.build();
  }

  private static ChannelSegmentDescriptor requestDescriptor() {
    return ChannelSegmentDescriptor
        .from(UUID.randomUUID(), Instant.EPOCH, Instant.MAX);
  }

  private static ChannelSegmentProcessingResponse processingResponse(
      ChannelSegmentStorageResponse storageResponse) {
    return ChannelSegmentProcessingResponse.builder()
        .addAllStored(storageResponse.getStored())
        .addAllFailed(storageResponse.getFailed())
        .build();
  }


  @Test
  void testCreateNullArguments() {
    assertThrows(NullPointerException.class, () -> FilterControlRouteHandler.create(null));
    assertDoesNotThrow(() -> FilterControlRouteHandler.create(mockFilterControl));
  }

  @ParameterizedTest
  @MethodSource("handlerNullArguments")
  void testStreamingNullArguments(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {
    assertThrows(NullPointerException.class,
        () -> routeHandler.streaming(requestBodyType, body, responseBodyType));
  }

  @ParameterizedTest
  @MethodSource("handlerNullArguments")
  void testClaimCheckNullArguments(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {
    assertThrows(NullPointerException.class,
        () -> routeHandler.claimCheck(requestBodyType, body, responseBodyType));
  }

  private static Stream<Arguments> handlerNullArguments() {
    return Stream.of(
        arguments(null, new byte[0], APPLICATION_JSON),
        arguments(APPLICATION_JSON, null, APPLICATION_MSGPACK),
        arguments(APPLICATION_JSON, new byte[0], null)
    );
  }

  @ParameterizedTest
  @MethodSource("validStreamingArguments")
  void testValidStreaming(ContentType contentType, byte[] requestBody, ContentType acceptType,
      int expectedStatus, ContentType expectedType, byte[] expectedBody) {
    given(mockFilterControl.executeProcessing(streamingRequest.getChannelSegments(),
        streamingRequest.getInputToOutputChannelIds(), streamingRequest.getPluginParams()))
        .willReturn(outputWaveforms);

    StandardResponse response = routeHandler
        .streaming(contentType, requestBody, acceptType);

    assertEquals(expectedStatus, response.getHttpStatus());
    assertEquals(expectedType, response.getContentType());

    Object responseBody = response.getResponseBody();
    assertTrue(responseBody instanceof byte[]);
    assertArrayEquals(expectedBody, (byte[]) responseBody);
  }

  @ParameterizedTest
  @MethodSource("invalidStreamingArguments")
  void testInvalidStreaming(ContentType contentType, byte[] requestBody, ContentType acceptType,
      int expectedStatus, ContentType expectedType, String expectedBody) {
    StandardResponse response = routeHandler
        .streaming(contentType, requestBody, acceptType);

    assertEquals(expectedStatus, response.getHttpStatus());
    assertEquals(expectedType, response.getContentType());
    assertEquals(expectedBody, response.getResponseBody());
  }


  @ParameterizedTest
  @MethodSource("validClaimCheckArguments")
  void testValidClaimCheck(ContentType contentType, byte[] requestBody, ContentType acceptType,
      int expectedStatus, ContentType expectedType, byte[] expectedBody) {
    given(mockFilterControl.requestProcessing(claimCheckRequest))
        .willReturn(outputWaveforms);
    given(mockFilterControl.storeWaveforms(outputWaveforms))
        .willReturn(storageResponse);

    StandardResponse response = routeHandler.claimCheck(contentType, requestBody, acceptType);
    assertEquals(expectedStatus, response.getHttpStatus());
    assertEquals(expectedType, response.getContentType());

    Object responseBody = response.getResponseBody();
    assertTrue(responseBody instanceof byte[]);
    assertArrayEquals(expectedBody, (byte[]) responseBody);
  }

  @ParameterizedTest
  @MethodSource("invalidClaimCheckArguments")
  void testInvalidClaimCheck(ContentType contentType, byte[] requestBody, ContentType acceptType,
      int expectedStatus, ContentType expectedType, String expectedBody) {
    StandardResponse response = routeHandler.claimCheck(contentType, requestBody, acceptType);
    assertEquals(expectedStatus, response.getHttpStatus());
    assertEquals(expectedType, response.getContentType());
    assertEquals(expectedBody, response.getResponseBody());
  }

  private static Stream<Arguments> validStreamingArguments() throws JsonProcessingException {
    byte[] jsonRequest = getJsonObjectMapper().writeValueAsBytes(streamingRequest);
    byte[] jsonResponse = getJsonObjectMapper().writeValueAsBytes(outputWaveforms);
    byte[] msgpackRequest = getMsgpackObjectMapper().writeValueAsBytes(streamingRequest);
    byte[] msgpackResponse = getMsgpackObjectMapper().writeValueAsBytes(outputWaveforms);
    return Stream.of(
        getValidArguments(APPLICATION_JSON, jsonRequest, APPLICATION_JSON, jsonResponse),
        getValidArguments(APPLICATION_MSGPACK, msgpackRequest, APPLICATION_JSON, jsonResponse),
        getValidArguments(APPLICATION_JSON, jsonRequest, APPLICATION_MSGPACK, msgpackResponse),
        getValidArguments(APPLICATION_MSGPACK, msgpackRequest, APPLICATION_MSGPACK, msgpackResponse)
    );
  }

  private static Stream<Arguments> invalidStreamingArguments() {
    return Stream.of(
        getInvalidArguments(TEXT_PLAIN, APPLICATION_JSON, UNSUPPORTED_MEDIA_TYPE_415,
            "FilterControl streaming invocation cannot accept inputs in format text/plain"),
        getInvalidArguments(UNKNOWN, APPLICATION_JSON, UNSUPPORTED_MEDIA_TYPE_415,
            "FilterControl streaming invocation cannot accept inputs in format unknown"),
        getInvalidArguments(APPLICATION_JSON, TEXT_PLAIN, NOT_ACCEPTABLE_406,
            "FilterControl streaming invocation cannot provide outputs in format text/plain"),
        getInvalidArguments(APPLICATION_JSON, UNKNOWN, NOT_ACCEPTABLE_406,
            "FilterControl streaming invocation cannot provide outputs in format unknown")
    );
  }

  private static Stream<Arguments> validClaimCheckArguments() throws JsonProcessingException {
    byte[] jsonRequest = getJsonObjectMapper().writeValueAsBytes(claimCheckRequest);
    byte[] jsonResponse = getJsonObjectMapper().writeValueAsBytes(processingResponse);
    return Stream.of(
        getValidArguments(APPLICATION_JSON, jsonRequest, APPLICATION_JSON, jsonResponse)
    );
  }

  private static Stream<Arguments> invalidClaimCheckArguments() {
    return Stream.of(
        getInvalidArguments(APPLICATION_MSGPACK, APPLICATION_JSON, UNSUPPORTED_MEDIA_TYPE_415,
            "FilterControl control invocation cannot accept inputs in format application/msgpack"),
        getInvalidArguments(TEXT_PLAIN, APPLICATION_JSON, UNSUPPORTED_MEDIA_TYPE_415,
            "FilterControl control invocation cannot accept inputs in format text/plain"),
        getInvalidArguments(UNKNOWN, APPLICATION_JSON, UNSUPPORTED_MEDIA_TYPE_415,
            "FilterControl control invocation cannot accept inputs in format unknown"),
        getInvalidArguments(APPLICATION_JSON, APPLICATION_MSGPACK, NOT_ACCEPTABLE_406,
            "FilterControl claim check invocation cannot provide outputs in format application/msgpack"),
        getInvalidArguments(APPLICATION_JSON, TEXT_PLAIN, NOT_ACCEPTABLE_406,
            "FilterControl claim check invocation cannot provide outputs in format text/plain"),
        getInvalidArguments(APPLICATION_JSON, UNKNOWN, NOT_ACCEPTABLE_406,
            "FilterControl claim check invocation cannot provide outputs in format unknown")
    );
  }

  private static Arguments getValidArguments(ContentType contentType, byte[] serializedRequest,
      ContentType acceptType,
      byte[] serializedResponse) {
    return arguments(contentType, serializedRequest, acceptType, OK_200, acceptType,
        serializedResponse);
  }

  private static Arguments getInvalidArguments(ContentType contentType, ContentType acceptType,
      int responseStatus, String responseMessage) {
    return arguments(contentType, new byte[0], acceptType, responseStatus, TEXT_PLAIN,
        responseMessage);
  }

}
