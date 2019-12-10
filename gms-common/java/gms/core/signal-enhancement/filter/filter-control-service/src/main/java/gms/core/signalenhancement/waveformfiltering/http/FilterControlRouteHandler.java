package gms.core.signalenhancement.waveformfiltering.http;

import gms.core.signalenhancement.waveformfiltering.control.FilterControl;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentProcessingResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentStorageResponse;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles HTTP invocation to {@link FilterControl}
 */
public class FilterControlRouteHandler {

  private static Logger logger = LoggerFactory.getLogger(FilterControlRouteHandler.class);

  private final FilterControl filterControl;

  private FilterControlRouteHandler(FilterControl filterControl) {
    this.filterControl = filterControl;
  }

  /**
   * Obtains a new {@link FilterControlRouteHandler} that delegates calls to the {@link
   * FilterControl}
   *
   * @param filterControl FilterControl, not null
   * @return constructed {@link FilterControlRouteHandler}, not null
   */
  public static FilterControlRouteHandler create(FilterControl filterControl) {
    Objects.requireNonNull(filterControl,
        "FilterControlRouteHandler cannot be constructed with null FilterControl");
    return new FilterControlRouteHandler(filterControl);
  }

  /**
   * Route handler for streaming invocation of {@link gms.core.signalenhancement.waveformfiltering.control.FilterControl}
   * Body must be a serialized {@link StreamingRequest}
   *
   * @param requestBodyType {@link ContentType} of the request body content, not null
   * @param body request body content, possibly empty or malformed, not null
   * @param responseBodyType client's desired {@link ContentType} of the response body, not null
   * @return {@link StandardResponse}, not null
   */
  public StandardResponse streaming(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {
    Objects.requireNonNull(requestBodyType,
        "FilterControlRouteHandler requires non-null requestBodyType");
    Objects.requireNonNull(body, "FilterControlRouteHandler requires non-null body");
    Objects.requireNonNull(responseBodyType,
        "FilterControlRouteHandler requires non-null responseBodyType");

    logger.info(
        "Request received to execute streaming filter processing with Content-Type: {}, Accept: {}, body length: {}",
        requestBodyType, responseBodyType, body.length);

    if (isNotAcceptableStreamingType(requestBodyType)) {
      final String message =
          "FilterControl streaming invocation cannot accept inputs in format " + requestBodyType;
      return StandardResponse
          .create(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, message, ContentType.TEXT_PLAIN);
    }

    if (isNotAcceptableStreamingType(responseBodyType)) {
      final String message =
          "FilterControl streaming invocation cannot provide outputs in format " + responseBodyType;
      return StandardResponse
          .create(HttpStatus.NOT_ACCEPTABLE_406, message, ContentType.TEXT_PLAIN);
    }

    logger.info("Content-Type and Accept types both acceptable");

    // Invoke FilterControl and construct a StandardResponse from the results

    logger.info("Action:filterChannelSegments Step:deserializeStart");
    StreamingRequest request = getStreamingDtoDeserializer(requestBodyType).apply(body);
    logger.info("Action:filterChannelSegments Step:deserializeEnd");

    logger.info("Action:filterChannelSegments Step:processingStart");
    List<ChannelSegment<Waveform>> filteredChannelSegments = filterControl
        .executeProcessing(request.getChannelSegments(),
            request.getInputToOutputChannelIds(), request.getPluginParams());
    logger.info("Action:filterChannelSegments Step:processingEnd");

    logger.info("Processing executed for request, returning with {} filtered channel segments",
        filteredChannelSegments.size());

    logger.info("Action:filterChannelSegments Step:serializeStart");
    Object responseBody = getSerializationOp(responseBodyType).apply(filteredChannelSegments);
    logger.info("Action:filterChannelSegments Step:serializeEnd");

    return StandardResponse.create(HttpStatus.OK_200,
        responseBody,
        responseBodyType);
  }

  /**
   * Route handler for claim check invocation of {@link gms.core.signalenhancement.waveformfiltering.control.FilterControl}
   * Body must be a serialized {@link ChannelSegmentDescriptor}
   *
   * @param requestBodyType {@link ContentType} of the request body content, not null
   * @param body request body content, possibly empty or malformed, not null
   * @param responseBodyType client's desired {@link ContentType} of the response body, not null
   * @return {@link StandardResponse}, not null
   */
  public StandardResponse claimCheck(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {

    Objects.requireNonNull(requestBodyType,
        "FilterControlRouteHandler requires non-null requestBodyType");
    Objects.requireNonNull(body, "FilterControlRouteHandler requires non-null body");
    Objects.requireNonNull(responseBodyType,
        "FilterControlRouteHandler requires non-null responseBodyType");

    if (isNotAcceptableClaimCheckType(requestBodyType)) {
      final String message =
          "FilterControl control invocation cannot accept inputs in format " + requestBodyType;
      return StandardResponse
          .create(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, message, ContentType.TEXT_PLAIN);
    }

    if (isNotAcceptableClaimCheckType(responseBodyType)) {
      final String message =
          "FilterControl claim check invocation cannot provide outputs in format "
              + responseBodyType;
      return StandardResponse
          .create(HttpStatus.NOT_ACCEPTABLE_406, message, ContentType.TEXT_PLAIN);
    }

    ChannelSegmentDescriptor descriptor = getChannelSegmentDescriptorDeserializer(requestBodyType)
        .apply(body);

    logger.info("Claim check request received: {}", descriptor);

    ChannelSegmentProcessingResponse.Builder responseBuilder = ChannelSegmentProcessingResponse
        .builder();

    List<ChannelSegment<Waveform>> filteredSegments = filterControl.requestProcessing(descriptor);

    if(!filteredSegments.isEmpty()){
      logger.info("Storing {} filtered ChannelSegments", filteredSegments.size());
      ChannelSegmentStorageResponse storageResponse = filterControl
          .storeWaveforms(filteredSegments);
      responseBuilder.addAllStored(storageResponse.getStored())
          .addAllFailed(storageResponse.getFailed());
    }

    return StandardResponse.create(HttpStatus.OK_200,
        getSerializationOp(responseBodyType).apply(responseBuilder.build()),
        ContentType.APPLICATION_JSON);
  }

  /**
   * Determines if the {@link ContentType} can be used as both input and output by the streaming
   * filter service
   *
   * @param contentType a ContentType, not null
   * @return whether contentType can be accepted and returned by the streaming filter service
   */
  private static boolean isNotAcceptableStreamingType(ContentType contentType) {
    return ContentType.APPLICATION_JSON != contentType
        && ContentType.APPLICATION_MSGPACK != contentType;
  }

  /**
   * Obtains a function to deserialize a {@link ContentType} byte array into a {@link StreamingRequest}.
   * Assumes the content type has already been validated as supported.
   *
   * @param type ContentType, not null
   * @return Function to map a byte[] into a StreamingRequest
   */
  private static Function<byte[], StreamingRequest> getStreamingDtoDeserializer(ContentType type) {
    return getDeserializer(type, StreamingRequest.class);
  }

  /**
   * Obtains a function to deserialize a {@link ContentType} byte array into a {@link StreamingRequest}.
   * Assumes the content type has already been validated as supported.
   *
   * @param type ContentType, not null
   * @return Function to map a byte[] into a StreamingRequest
   */
  private static Function<byte[], ChannelSegmentDescriptor> getChannelSegmentDescriptorDeserializer(
      ContentType type) {
    return getDeserializer(type, ChannelSegmentDescriptor.class);
  }

  /**
   * Determines if the {@link ContentType} can be used as both input and output by the claim check
   * filter service
   *
   * @param contentType a ContentType, not null
   * @return whether contentType can be accepted and returned by the claim check filter service
   */
  private static boolean isNotAcceptableClaimCheckType(ContentType contentType) {
    return contentType != ContentType.APPLICATION_JSON;
  }

  /**
   * Route handler for alive endpoint.  Ignores any request headers.  Responds with {@link
   * ContentType#TEXT_PLAIN} body containing a simple aliveness message.
   *
   * @return {@link StandardResponse}, not null
   */
  public StandardResponse alive() {
    final String aliveAt = "Filter Control Service alive at " + Instant.now();
    return StandardResponse.create(HttpStatus.OK_200, aliveAt, ContentType.TEXT_PLAIN);
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
        "FilterControlRouteHandler cannot instantiate a deserializer for an unsupported ContentType.");
  }

  /**
   * Obtains the serialization operation to invoke to serialize an object into a byte[] in format
   * {@link ContentType}
   *
   * @param type ContentType, not null
   * @return Function to serialize an Object into a byte[]
   */
  private static Function<Object, byte[]> getSerializationOp(ContentType type) {
    if (ContentType.APPLICATION_JSON == type) {
      return ObjectSerialization::writeJson;
    } else if (ContentType.APPLICATION_MSGPACK == type) {
      return ObjectSerialization::writeMessagePack;
    }

    throw new IllegalStateException(
        "FilterControlRouteHandler cannot instantiate a serializer for an unsupported ContentType.");
  }
}
