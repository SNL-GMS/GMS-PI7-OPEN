package gms.core.signalenhancement.beam.service;

import static gms.core.signalenhancement.beam.service.ContentType.APPLICATION_ANY;
import static gms.core.signalenhancement.beam.service.ContentType.APPLICATION_JSON;
import static gms.core.signalenhancement.beam.service.ContentType.APPLICATION_MSGPACK;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gms.core.signalenhancement.beam.TestFixtures;
import gms.core.signalenhancement.beam.core.BeamControl;
import gms.core.signalenhancement.beam.core.BeamStreamingCommand;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ProcessingGroupDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class BeamRouteHandlerTests {

  private BeamRouteHandler routeHandler;
  private BeamControl mockBeamControl;

  private List<ChannelSegment<Waveform>> outputWaveforms;
  private List<ChannelSegmentDescriptor> outputDescriptors;

  @BeforeEach
  public void setUp() {

    mockBeamControl = mock(BeamControl.class);
    routeHandler = BeamRouteHandler.create(mockBeamControl);

    outputWaveforms = List.of(TestFixtures.waveformChannelSegment());
    outputDescriptors = outputWaveforms.stream()
        .map(ChannelSegmentDescriptor::from)
        .collect(toList());
    given(mockBeamControl.executeStreaming(notNull()))
        .willReturn(outputWaveforms);

    given(mockBeamControl.executeClaimCheck(notNull()))
        .willReturn(outputWaveforms);
  }

  /**
   * @return {@link BeamStreamingCommand} serialized in JSON
   */
  private byte[] getJsonStreamingDto() {
    return ObjectSerialization.writeJson(TestFixtures.getBeamStreamingCommand());
  }

  /**
   * @return {@link BeamStreamingCommand} serialized in MessagePack
   */
  private byte[] getMessagePackStreamingDto() {
    return ObjectSerialization.writeMessagePack(TestFixtures.getBeamStreamingCommand());
  }

  /**
   * Obtain the {@link ContentType}s passing the provided filter
   *
   * @param filter {@link ContentType} predicate
   * @return stream of ContentType matching the filter
   */
  private Stream<ContentType> getContentTypeStream(Predicate<ContentType> filter) {
    return Arrays.stream(ContentType.values()).filter(filter);
  }

  /**
   * Determines whether the {@link ContentType} is unacceptable to {@link
   * BeamRouteHandler#streaming(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return whether the contentType is unacceptable to the streaming interface
   */
  private static boolean negativeStreamingResponseTypeFilter(ContentType contentType) {
    return !streamingResponseTypeFilter(contentType);
  }

  /**
   * Determines whether the {@link ContentType} is acceptable to {@link
   * BeamRouteHandler#streaming(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return wheter the contentType is acceptable to the streaming interface
   */
  private static boolean streamingRequestTypeFilter(ContentType contentType) {
    return contentType == APPLICATION_JSON
        || contentType == APPLICATION_MSGPACK;
  }

  /**
   * Determines whether the {@link ContentType} is unacceptable to {@link
   * BeamRouteHandler#streaming(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return whether the contentType is unacceptable to the streaming interface
   */
  private static boolean negativeStreamingRequestTypeFilter(ContentType contentType) {
    return !streamingRequestTypeFilter(contentType);
  }

  /**
   * Determines whether the {@link ContentType} is acceptable to {@link
   * BeamRouteHandler#streaming(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return wheter the contentType is acceptable to the streaming interface
   */
  private static boolean streamingResponseTypeFilter(ContentType contentType) {
    return contentType == APPLICATION_JSON
        || contentType == APPLICATION_MSGPACK
        || contentType == APPLICATION_ANY;
  }

  private static Stream<Arguments> handlerNullArguments() {
    return Stream.of(
        arguments(null, new byte[0], APPLICATION_JSON),
        arguments(APPLICATION_JSON, null, APPLICATION_MSGPACK),
        arguments(APPLICATION_JSON, new byte[0], null)
    );
  }

  @Test
  void testCreate() {
    BeamRouteHandler handler = BeamRouteHandler
        .create(mock(BeamControl.class));
    assertNotNull(handler);
  }

  @Test
  void testCreateNullExpectNullPointerException() {
    assertThrows(NullPointerException.class, () -> BeamRouteHandler.create(null));
  }

  @Test
  void testStreamingRequestContentTypeJson() {
    final StandardResponse response = routeHandler
        .streaming(APPLICATION_JSON, getJsonStreamingDto(),
            APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  void testStreamingRequestContentTypeMessagePack() {
    final StandardResponse response = routeHandler
        .streaming(APPLICATION_MSGPACK, getMessagePackStreamingDto(),
            APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  void testStreamingRequestContentTypeAny() {
    final StandardResponse response = routeHandler
        .streaming(APPLICATION_ANY, getMessagePackStreamingDto(),
            APPLICATION_JSON);

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, response.getHttpStatus());
  }

  @Test
  void testStreamingAcceptTypeJson() {
    final StandardResponse response = routeHandler
        .streaming(APPLICATION_JSON, getJsonStreamingDto(),
            APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  void testStreamingAcceptTypeMessagePack() {
    final StandardResponse response = routeHandler
        .streaming(APPLICATION_JSON, getJsonStreamingDto(),
            APPLICATION_MSGPACK);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  void testStreamingAcceptTypeAny() {
    final StandardResponse response = routeHandler
        .streaming(APPLICATION_JSON, getJsonStreamingDto(),
            APPLICATION_ANY);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  void testStreamingRequestContentTypeUnsupportedExpect415() {
    getContentTypeStream(BeamRouteHandlerTests::negativeStreamingRequestTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, routeHandler
                .streaming(c, new byte[0], APPLICATION_JSON).getHttpStatus())
        );
  }

  @Test
  void testStreamingAcceptTypeUnsupportedExpect406() {
    getContentTypeStream(
        BeamRouteHandlerTests::negativeStreamingResponseTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.NOT_ACCEPTABLE_406, routeHandler
                .streaming(APPLICATION_JSON, new byte[0], c).getHttpStatus())
        );
  }

  @Test
  void testStreamingProvidesJsonResponse() {
    final StandardResponse response = routeHandler
        .streaming(APPLICATION_MSGPACK, getMessagePackStreamingDto(),
            APPLICATION_JSON);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertArrayEquals(ObjectSerialization.writeJson(outputWaveforms),
        (byte[]) response.getResponseBody());
  }

  @Test
  void testStreamingProvidesJsonResponseForResponseTypeAny() {
    final StandardResponse response = routeHandler
        .streaming(APPLICATION_MSGPACK, getMessagePackStreamingDto(),
            APPLICATION_ANY);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertArrayEquals(ObjectSerialization.writeJson(outputWaveforms),
        (byte[]) response.getResponseBody());
  }

  @Test
  void testStreamingProvidesMsgPackResponse() {
    final StandardResponse response = routeHandler
        .streaming(APPLICATION_JSON, getJsonStreamingDto(),
            APPLICATION_MSGPACK);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertArrayEquals(ObjectSerialization.writeMessagePack(outputWaveforms),
        (byte[]) response.getResponseBody());
  }

  @ParameterizedTest
  @MethodSource("handlerNullArguments")
  void testStreamingNullArguments(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {
    assertThrows(NullPointerException.class,
        () -> routeHandler.claimCheck(requestBodyType, body, responseBodyType));
  }

  @Test
  void testStreamingInvokesExecute() {
    final byte[] requestBody = getMessagePackStreamingDto();
    routeHandler
        .streaming(APPLICATION_MSGPACK, requestBody, APPLICATION_JSON);

    verify(mockBeamControl, times(1))
        .executeStreaming(ObjectSerialization
            .readMessagePack(requestBody, BeamStreamingCommand.class));
  }

  private static boolean claimCheckResponseTypeFilter(ContentType contentType) {
    return contentType == APPLICATION_JSON
        || contentType == APPLICATION_ANY;
  }

  private static boolean negativeClaimCheckResponseTypeFilter(ContentType contentType) {
    return !claimCheckResponseTypeFilter(contentType);
  }

  private static boolean claimCheckRequestTypeFilter(ContentType contentType) {
    return contentType == APPLICATION_JSON;
  }

  private static boolean negativeClaimCheckRequestTypeFilter(ContentType contentType) {
    return !claimCheckRequestTypeFilter(contentType);
  }

  @Test
  void testClaimCheckRequestContentTypeUnsupportedExpect415() {
    getContentTypeStream(
        BeamRouteHandlerTests::negativeClaimCheckRequestTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, routeHandler
                .claimCheck(c, new byte[0], APPLICATION_JSON).getHttpStatus())
        );
  }

  @Test
  void testClaimCheckAcceptTypeUnsupportedExpect406() {
    getContentTypeStream(
        BeamRouteHandlerTests::negativeClaimCheckResponseTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.NOT_ACCEPTABLE_406, routeHandler
                .claimCheck(APPLICATION_JSON, new byte[0], c).getHttpStatus())
        );
  }

  @Test
  void testClaimCheckProvidesJsonResponse() {
    final StandardResponse response = routeHandler
        .claimCheck(APPLICATION_JSON, getProcessingGroupDescriptor(),
            APPLICATION_JSON);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertArrayEquals(ObjectSerialization.writeJson(outputDescriptors),
        (byte[]) response.getResponseBody());
  }

  private byte[] getProcessingGroupDescriptor() {
    return ObjectSerialization.writeJson(TestFixtures.getProcessingGroupDescriptor());
  }

  @Test
  void testClaimCheckProvidesJsonResponseForResponseTypeAny() {
    final StandardResponse response = routeHandler
        .claimCheck(APPLICATION_JSON, getProcessingGroupDescriptor(),
            APPLICATION_ANY);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertArrayEquals(ObjectSerialization.writeJson(outputDescriptors),
        (byte[]) response.getResponseBody());
  }

  @ParameterizedTest
  @MethodSource("handlerNullArguments")
  void testClaimCheckNullArguments(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {
    assertThrows(NullPointerException.class,
        () -> routeHandler.claimCheck(requestBodyType, body, responseBodyType));
  }

  @Test
  void testClaimCheckAcceptTypeJson() {
    given(mockBeamControl.executeClaimCheck(any(ProcessingGroupDescriptor.class)))
        .willReturn(List.of());

    final StandardResponse response = routeHandler
        .claimCheck(APPLICATION_JSON, getProcessingGroupDescriptor(),
            APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  void testClaimCheckAcceptTypeAny() {
    given(mockBeamControl.executeClaimCheck(any(ProcessingGroupDescriptor.class)))
        .willReturn(List.of());

    final StandardResponse response = routeHandler
        .claimCheck(APPLICATION_JSON, getProcessingGroupDescriptor(),
            APPLICATION_ANY);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  void testClaimCheckRequestContentTypeJson() {
    given(mockBeamControl.executeClaimCheck(any(ProcessingGroupDescriptor.class)))
        .willReturn(List.of());

    final StandardResponse response = routeHandler
        .claimCheck(APPLICATION_JSON, getProcessingGroupDescriptor(),
            APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  void testClaimCheckInvokesExecute() {
    final byte[] requestBody = getProcessingGroupDescriptor();
    routeHandler
        .claimCheck(APPLICATION_JSON, requestBody, APPLICATION_JSON);

    verify(mockBeamControl, times(1))
        .executeClaimCheck(ObjectSerialization.readJson(requestBody, ProcessingGroupDescriptor.class));
  }
}
