package gms.core.signaldetection.signaldetectorcontrol.http;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import gms.core.signaldetection.signaldetectorcontrol.TestFixtures;
import gms.core.signaldetection.signaldetectorcontrol.control.ExecuteStreamingCommand;
import gms.core.signaldetection.signaldetectorcontrol.control.SignalDetectorControl;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SignalDetectorControlRouteHandlerTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private SignalDetectorControl mockSignalDetectorControl;
  private SignalDetectorControlRouteHandler routeHandlerMockControl;
  private List<SignalDetection> outputSignalDetections;
  private List<SignalDetectionHypothesisDescriptor> signalDetectionHypothesisDescriptors;

  @Before
  public void setUp() {

    mockSignalDetectorControl = mock(SignalDetectorControl.class);
    routeHandlerMockControl = SignalDetectorControlRouteHandler.create(mockSignalDetectorControl);

    outputSignalDetections = List.of(TestFixtures.randomSignalDetection());

    signalDetectionHypothesisDescriptors = outputSignalDetections.get(0)
        .getSignalDetectionHypotheses().stream().map(sdh -> SignalDetectionHypothesisDescriptor
            .from(sdh, outputSignalDetections.get(0).getStationId())).collect(Collectors.toList());

    given(mockSignalDetectorControl.execute((ExecuteStreamingCommand) notNull()))
        .willReturn(outputSignalDetections);

    given(mockSignalDetectorControl.execute((ChannelSegmentDescriptor) notNull()))
        .willReturn(signalDetectionHypothesisDescriptors);
  }

  /**
   * @return {@link StreamingDto} serialized in JSON
   */
  private byte[] getJsonStreamingDto() {
    return ObjectSerialization.writeJson(TestFixtures.getStreamingDto());
  }

  /**
   * @return {@link StreamingDto} serialized in MessagePack
   */
  private byte[] getMessagePackStreamingDto() {
    return ObjectSerialization.writeMessagePack(TestFixtures.getStreamingDto());
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
   * SignalDetectorControlRouteHandler#streaming(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return whether the contentType is unacceptable to the streaming interface
   */
  private static boolean negativeStreamingResponseTypeFilter(ContentType contentType) {
    return !streamingResponseTypeFilter(contentType);
  }

  /**
   * Determines whether the {@link ContentType} is acceptable to {@link
   * SignalDetectorControlRouteHandler#streaming(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return wheter the contentType is acceptable to the streaming interface
   */
  private static boolean streamingRequestTypeFilter(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON
        || contentType == ContentType.APPLICATION_MSGPACK;
  }

  /**
   * Determines whether the {@link ContentType} is unacceptable to {@link
   * SignalDetectorControlRouteHandler#streaming(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return whether the contentType is unacceptable to the streaming interface
   */
  private static boolean negativeStreamingRequestTypeFilter(ContentType contentType) {
    return !streamingRequestTypeFilter(contentType);
  }

  /**
   * Determines whether the {@link ContentType} is acceptable to {@link
   * SignalDetectorControlRouteHandler#streaming(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return wheter the contentType is acceptable to the streaming interface
   */
  private static boolean streamingResponseTypeFilter(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON
        || contentType == ContentType.APPLICATION_MSGPACK
        || contentType == ContentType.APPLICATION_ANY;
  }

  @Test
  public void testCreate() {
    SignalDetectorControlRouteHandler handler = SignalDetectorControlRouteHandler
        .create(mock(SignalDetectorControl.class));
    assertNotNull(handler);
  }

  @Test
  public void testCreateNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage(
            "SignalDetectorControlRouteHandler cannot be constructed with null SignalDetectorControl");
    SignalDetectorControlRouteHandler.create(null);
  }

  @Test
  public void testStreamingRequestContentTypeJson() {
    //TODO: Pass proper streaming object

    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_JSON, getJsonStreamingDto(),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testStreamingRequestContentTypeMessagePack() {
    //TODO: Pass proper streaming object

    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_MSGPACK, getMessagePackStreamingDto(),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testStreamingRequestContentTypeAny() {
    //TODO: Pass proper streaming object

    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_ANY, getMessagePackStreamingDto(),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, response.getHttpStatus());
  }

  @Test
  public void testStreamingAcceptTypeJson() {
    //TODO: Pass proper streaming object

    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_JSON, getJsonStreamingDto(),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testStreamingAcceptTypeMessagePack() {
    //TODO: Pass proper streaming object

    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_JSON, getJsonStreamingDto(),
            ContentType.APPLICATION_MSGPACK);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testStreamingAcceptTypeAny() {
    //TODO: Pass proper streaming object

    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_JSON, getJsonStreamingDto(),
            ContentType.APPLICATION_ANY);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testStreamingRequestContentTypeUnsupportedExpect415() {
    getContentTypeStream(SignalDetectorControlRouteHandlerTests::negativeStreamingRequestTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, routeHandlerMockControl
                .streaming(c, new byte[0], ContentType.APPLICATION_JSON).getHttpStatus())
        );
  }

  @Test
  public void testStreamingAcceptTypeUnsupportedExpect406() {
    getContentTypeStream(
        SignalDetectorControlRouteHandlerTests::negativeStreamingResponseTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.NOT_ACCEPTABLE_406, routeHandlerMockControl
                .streaming(ContentType.APPLICATION_JSON, new byte[0], c).getHttpStatus())
        );
  }

  @Test
  public void testStreamingProvidesJsonResponse() {
    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_MSGPACK, getMessagePackStreamingDto(),
            ContentType.APPLICATION_JSON);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertArrayEquals(ObjectSerialization.writeJson(outputSignalDetections),
        (byte[]) response.getResponseBody());
  }

  @Test
  public void testStreamingProvidesJsonResponseForResponseTypeAny() {
    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_MSGPACK, getMessagePackStreamingDto(),
            ContentType.APPLICATION_ANY);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertArrayEquals(ObjectSerialization.writeJson(outputSignalDetections),
        (byte[]) response.getResponseBody());
  }

  @Test
  public void testStreamingProvidesMsgPackResponse() {
    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_JSON, getJsonStreamingDto(),
            ContentType.APPLICATION_MSGPACK);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertArrayEquals(ObjectSerialization.writeMessagePack(outputSignalDetections),
        (byte[]) response.getResponseBody());
  }

  @Test
  public void testStreamingNullBodyTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SignalDetectorControlRouteHandler requires non-null requestBodyType");
    routeHandlerMockControl.streaming(null, new byte[0], ContentType.APPLICATION_JSON);
  }

  @Test
  public void testStreamingNullBodyExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SignalDetectorControlRouteHandler requires non-null body");
    routeHandlerMockControl
        .streaming(ContentType.APPLICATION_JSON, null, ContentType.APPLICATION_MSGPACK);
  }

  @Test
  public void testStreamingNullResponseTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SignalDetectorControlRouteHandler requires non-null responseBodyType");
    routeHandlerMockControl.streaming(ContentType.APPLICATION_JSON, new byte[0], null);
  }

  @Test
  public void testStreamingInvokesExecute() {
    final byte[] requestBody = getMessagePackStreamingDto();
    routeHandlerMockControl
        .streaming(ContentType.APPLICATION_MSGPACK, requestBody, ContentType.APPLICATION_JSON);

    then(mockSignalDetectorControl).should()
        .execute(getExecuteStreamingCommandFromDto(requestBody));
  }

  private ExecuteStreamingCommand getExecuteStreamingCommandFromDto(byte[] requestBody) {
    StreamingDto dto = ObjectSerialization.readMessagePack(requestBody, StreamingDto.class);
    return ExecuteStreamingCommand
        .create(dto.getChannelSegment(),
            dto.getStartTime(),
            dto.getEndTime(),
            //TODO: to be included when SignalDetectorParameters is implemented
            //dto.getSignalDetectorParameters(),
            dto.getProcessingContext());
  }

  private static boolean claimCheckResponseTypeFilter(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON
        || contentType == ContentType.APPLICATION_ANY;
  }

  private static boolean negativeClaimCheckResponseTypeFilter(ContentType contentType) {
    return !claimCheckResponseTypeFilter(contentType);
  }

  private static boolean claimCheckRequestTypeFilter(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON;
  }

  private static boolean negativeClaimCheckRequestTypeFilter(ContentType contentType) {
    return !claimCheckRequestTypeFilter(contentType);
  }

  @Test
  public void testClaimCheckRequestContentTypeUnsupportedExpect415() {
    getContentTypeStream(
        SignalDetectorControlRouteHandlerTests::negativeClaimCheckRequestTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, routeHandlerMockControl
                .claimCheck(c, new byte[0], ContentType.APPLICATION_JSON).getHttpStatus())
        );
  }

  @Test
  public void testClaimCheckAcceptTypeUnsupportedExpect406() {
    getContentTypeStream(
        SignalDetectorControlRouteHandlerTests::negativeClaimCheckResponseTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.NOT_ACCEPTABLE_406, routeHandlerMockControl
                .claimCheck(ContentType.APPLICATION_JSON, new byte[0], c).getHttpStatus())
        );
  }

  @Test
  public void testClaimCheckProvidesJsonResponse() {
    final StandardResponse response = routeHandlerMockControl
        .claimCheck(ContentType.APPLICATION_JSON, getChannelSegmentDescriptor(),
            ContentType.APPLICATION_JSON);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertArrayEquals(ObjectSerialization.writeJson(signalDetectionHypothesisDescriptors),
        (byte[]) response.getResponseBody());
  }

  @Test
  public void testClaimCheckProvidesJsonResponseForResponseTypeAny() {
    final StandardResponse response = routeHandlerMockControl
        .claimCheck(ContentType.APPLICATION_JSON, getChannelSegmentDescriptor(),
            ContentType.APPLICATION_ANY);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertArrayEquals(ObjectSerialization.writeJson(signalDetectionHypothesisDescriptors),
        (byte[]) response.getResponseBody());
  }

  @Test
  public void testClaimCheckNullBodyTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SignalDetectorControlRouteHandler requires non-null requestBodyType");
    routeHandlerMockControl.claimCheck(null, new byte[0], ContentType.APPLICATION_JSON);
  }

  @Test
  public void testClaimCheckNullBodyExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SignalDetectorControlRouteHandler requires non-null body");
    routeHandlerMockControl
        .claimCheck(ContentType.APPLICATION_JSON, null, ContentType.APPLICATION_MSGPACK);
  }

  @Test
  public void testClaimCheckNullResponseTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("SignalDetectorControlRouteHandler requires non-null responseBodyType");
    routeHandlerMockControl.claimCheck(ContentType.APPLICATION_JSON, new byte[0], null);
  }

  @Test
  public void testClaimCheckAcceptTypeJson() {
    //TODO: Pass proper claim check object
    given(mockSignalDetectorControl.execute(any(ChannelSegmentDescriptor.class)))
        .willReturn(List.of());

    final StandardResponse response = routeHandlerMockControl
        .claimCheck(ContentType.APPLICATION_JSON, getChannelSegmentDescriptor(),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testClaimCheckAcceptTypeAny() {
    //TODO: Pass proper claim check object
    given(mockSignalDetectorControl.execute(any(ChannelSegmentDescriptor.class)))
        .willReturn(List.of());

    final StandardResponse response = routeHandlerMockControl
        .claimCheck(ContentType.APPLICATION_JSON, getChannelSegmentDescriptor(),
            ContentType.APPLICATION_ANY);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testClaimCheckRequestContentTypeJson() {
    //TODO: Pass proper claim check object
    given(mockSignalDetectorControl.execute(any(ChannelSegmentDescriptor.class)))
        .willReturn(List.of());

    final StandardResponse response = routeHandlerMockControl
        .claimCheck(ContentType.APPLICATION_JSON, getChannelSegmentDescriptor(),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testClaimCheckInvokesExecute() {
    //TODO: Test that the route handler calls getChannelSegmentDescriptorFromBytes on the control object

    final byte[] requestBody = getChannelSegmentDescriptor();
    routeHandlerMockControl
        .claimCheck(ContentType.APPLICATION_JSON, requestBody, ContentType.APPLICATION_JSON);

    then(mockSignalDetectorControl).should()
        .execute(ObjectSerialization.readJson(requestBody, ChannelSegmentDescriptor.class));
  }

  private byte[] getChannelSegmentDescriptor() {
    return ObjectSerialization.writeJson(TestFixtures.randomChannelSegmentDescriptor());
  }

}
