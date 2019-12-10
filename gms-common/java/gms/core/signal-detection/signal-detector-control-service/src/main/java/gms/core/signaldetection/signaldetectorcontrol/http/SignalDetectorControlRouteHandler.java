package gms.core.signaldetection.signaldetectorcontrol.http;

import gms.core.signaldetection.signaldetectorcontrol.control.ExecuteStreamingCommand;
import gms.core.signaldetection.signaldetectorcontrol.control.SignalDetectorControl;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalDetectorControlRouteHandler {

  private Logger logger = LoggerFactory.getLogger(SignalDetectorControlRouteHandler.class);

  private final SignalDetectorControl signalDetectorControl;

  private SignalDetectorControlRouteHandler(SignalDetectorControl signalDetectorControl) {
    this.signalDetectorControl = signalDetectorControl;
  }

  /**
   * Obtains a new {@link SignalDetectorControlRouteHandler} that delegates calls to the {@link
   * SignalDetectorControl}
   *
   * @param signalDetectorControl SignalDetectorControl, not null
   * @return constructed {@link SignalDetectorControlRouteHandler}, not null
   */
  public static SignalDetectorControlRouteHandler create(
      SignalDetectorControl signalDetectorControl) {
    Objects.requireNonNull(signalDetectorControl,
        "SignalDetectorControlRouteHandler cannot be constructed with null SignalDetectorControl");
    return new SignalDetectorControlRouteHandler(signalDetectorControl);
  }

  /**
   * Route handler for streaming invocation of {@link gms.core.signaldetection.signaldetectorcontrol.control.SignalDetectorControl}
   * Body must be a serialized {@link StreamingDto}
   *
   * @param requestBodyType {@link ContentType} of the request body content, not null
   * @param body request body content, possibly empty or malformed, not null
   * @param responseBodyType client's desired {@link ContentType} of the response body, not null
   * @return {@link StandardResponse}, not null
   */
  public StandardResponse streaming(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {

    Objects.requireNonNull(requestBodyType,
        "SignalDetectorControlRouteHandler requires non-null requestBodyType");
    Objects.requireNonNull(body, "SignalDetectorControlRouteHandler requires non-null body");
    Objects.requireNonNull(responseBodyType,
        "SignalDetectorControlRouteHandler requires non-null responseBodyType");

    logger
        .info(
            "Invoked Signal Detector Control / Streaming with Content-Type: {}, Accept: {}",
            requestBodyType, requestBodyType);

    if (!isAcceptableStreamingRequestType(requestBodyType)) {
      final String message =
          "SignalDetectorControl streaming invocation cannot accept inputs in format "
              + requestBodyType;
      return StandardResponse
          .create(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, message, ContentType.TEXT_PLAIN);
    }

    if (!isAcceptableStreamingResponseType(responseBodyType)) {
      final String message =
          "SignalDetectorControl streaming invocation cannot provide outputs in format "
              + responseBodyType;
      return StandardResponse
          .create(HttpStatus.NOT_ACCEPTABLE_406, message, ContentType.TEXT_PLAIN);
    }

    logger.info("Content-Type and Accept types both acceptable");

    // Invoke SignalDetectorControl and construct a StandardResponse from the results
    ExecuteStreamingCommand streamingCommand = executeCommandFrom(
        getStreamingDtoDeserializer(requestBodyType).apply(body));

    logger.info("Created ExecuteStreamingCommand");

    return StandardResponse.create(HttpStatus.OK_200,
        getSerializationOp(responseBodyType).apply(signalDetectorControl.execute(streamingCommand)),
        responseBodyType);
  }

  /**
   * Return an aliveness check for the system.
   */
  public static StandardResponse alive(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {
    return StandardResponse.create(HttpStatus.OK_200,
        "Service alive",
        responseBodyType);
  }

  /**
   * Obtains an {@link ExecuteStreamingCommand} from a {@link StreamingDto}
   *
   * @param streamingDto StreamingDto, not null
   * @return ExecuteStreamingCommand, not null
   */
  private ExecuteStreamingCommand executeCommandFrom(StreamingDto streamingDto) {
    return ExecuteStreamingCommand
        .create(streamingDto.getChannelSegment(),
            streamingDto.getStartTime(),
            streamingDto.getEndTime(),
            streamingDto.getProcessingContext());
  }

  /**
   * Determines if the {@link ContentType} can be used as both input and output by the streaming
   * detector service
   *
   * @param contentType a ContentType, not null
   * @return whether contentType can be accepted and returned by the streaming streaming detector
   * service
   */
  private static boolean isAcceptableStreamingResponseType(ContentType contentType) {
    return ContentType.APPLICATION_JSON == contentType
        || ContentType.APPLICATION_MSGPACK == contentType
        || ContentType.APPLICATION_ANY == contentType;
  }

  /**
   * Determines if the {@link ContentType} can be used as both input and output by the streaming
   * detector service
   *
   * @param contentType a ContentType, not null
   * @return whether contentType can be accepted and returned by the streaming streaming detector
   * service
   */
  private static boolean isAcceptableStreamingRequestType(ContentType contentType) {
    return ContentType.APPLICATION_JSON == contentType
        || ContentType.APPLICATION_MSGPACK == contentType;
  }

  /**
   * Obtains a function to deserialize a {@link ContentType} byte array into a {@link StreamingDto}.
   * Assumes the content type has already been validated as supported.
   *
   * @param type ContentType, not null
   * @return Function to map a byte[] into a StreamingDto
   */
  private static Function<byte[], StreamingDto> getStreamingDtoDeserializer(ContentType type) {
    return getDeserializer(type, StreamingDto.class);
  }


  /**
   * Route handler for claim check invocation of {@link SignalDetectorControl} Body must be a
   * serialized {@link ChannelSegmentDescriptor}
   *
   * @param requestBodyType {@link ContentType} of the request body content, not null
   * @param body request body content, possibly empty or malformed, not null
   * @param responseBodyType client's desired {@link ContentType} of the response body, not null
   * @return {@link StandardResponse}, not null
   */
  public StandardResponse claimCheck(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {

    Objects.requireNonNull(requestBodyType,
        "SignalDetectorControlRouteHandler requires non-null requestBodyType");
    Objects.requireNonNull(body, "SignalDetectorControlRouteHandler requires non-null body");
    Objects.requireNonNull(responseBodyType,
        "SignalDetectorControlRouteHandler requires non-null responseBodyType");

    logger.info(
        "Invoked Signal Detector Control / Claim Check with Content-Type: {}, and Accept: {}",
        requestBodyType, requestBodyType);

    if (!isAcceptableClaimCheckRequestType(requestBodyType)) {
      final String message =
          "SignalDetectorControl claim check invocation cannot accept inputs in format "
              + requestBodyType;
      return StandardResponse
          .create(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, message, ContentType.TEXT_PLAIN);
    }

    if (!isAcceptableClaimCheckResponseType(responseBodyType)) {
      final String message =
          "SignalDetectorControl claim check invocation cannot provide outputs in format "
              + responseBodyType;
      return StandardResponse
          .create(HttpStatus.NOT_ACCEPTABLE_406, message, ContentType.TEXT_PLAIN);
    }

    logger.info("Content-Type and Accept types both acceptable");

    // Invoke SignalDetectorControl and construct a StandardResponse from the results
    final ChannelSegmentDescriptor channelSegmentDescriptor = getChannelSegmentDescriptorDeserializer(
        requestBodyType).apply(body);

    logger.info("Deserialized ChannelSegmentDescriptor");

    return StandardResponse.create(HttpStatus.OK_200,
        getSerializationOp(responseBodyType)
            .apply(signalDetectorControl.execute(channelSegmentDescriptor)),
        responseBodyType);
  }

  /**
   * Determines if the {@link ContentType} can be used as output by the claim check
   * SignalDetectorControl service
   *
   * @param contentType a ContentType, not null
   * @return whether contentType can be accepted and returned by the claim check
   * SignalDetectorControl service
   */
  private static boolean isAcceptableClaimCheckResponseType(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON
        || contentType == ContentType.APPLICATION_ANY;
  }

  /**
   * Determines if the {@link ContentType} can be used as output by the claim check
   * SignalDetectorControl service
   *
   * @param contentType a ContentType, not null
   * @return whether contentType can be accepted and returned by the claim check
   * SignalDetectorControl service
   */
  private static boolean isAcceptableClaimCheckRequestType(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON;
  }

  /**
   * Obtains a function to deserialize a {@link ContentType} byte array into a {@link
   * ChannelSegmentDescriptor}. Assumes the content type has already been validated as supported.
   *
   * @param type ContentType, not null
   * @return Function to map a byte[] into a ChannelSegmentDescriptor
   */
  private static Function<byte[], ChannelSegmentDescriptor> getChannelSegmentDescriptorDeserializer(
      ContentType type) {
    return getDeserializer(type, ChannelSegmentDescriptor.class);
  }

  /**
   * Obtains a function to deserialize a {@link ContentType} byte array into a T. Assumes the
   * content type has already been validated as supported.
   *
   * @param type ContentType, not null
   * @param classType Class object for type T
   * @param <T> type of the deserialized object
   * @return Function to map a byte[] into a T
   */
  private static <T> Function<byte[], T> getDeserializer(ContentType type, Class<T> classType) {
    // There is a type error when all of this is on one line.  Doubtless it involves type erasure.
    BiFunction<byte[], Class<T>, T> deserialization = getDeserializationOp(type);
    return b -> deserialization.apply(b, classType);
  }

  /**
   * Obtains the deserialization operation to invoke to deserialize a byte[] in format {@link
   * ContentType} into a T
   *
   * @param type ContentType, not null
   * @param <T> type of the deserialized object
   * @return BiFunction to map a (byte[], Class) into a T
   */
  private static <T> BiFunction<byte[], Class<T>, T> getDeserializationOp(ContentType type) {
    if (ContentType.APPLICATION_JSON == type) {
      return ObjectSerialization::readJson;
    } else if (ContentType.APPLICATION_MSGPACK == type) {
      return ObjectSerialization::readMessagePack;
    }

    throw new IllegalStateException(
        "SignalDetectorControlRouteHandler cannot instantiate a deserializer for an unsupported ContentType.");
  }

  /**
   * Obtains the serialization operation to invoke to serialize an object into a byte[] in format
   * {@link ContentType}
   *
   * @param type ContentType, not null
   * @return Function to serialize an Object into a byte[]
   */
  private static Function<Object, byte[]> getSerializationOp(ContentType type) {
    if (ContentType.APPLICATION_JSON == type || ContentType.APPLICATION_ANY == type) {
      return ObjectSerialization::writeJson;
    } else if (ContentType.APPLICATION_MSGPACK == type) {
      return ObjectSerialization::writeMessagePack;
    }

    throw new IllegalStateException(
        "SignalDetectorControlRouteHandler cannot instantiate a serializer for an unsupported ContentType.");
  }
}
