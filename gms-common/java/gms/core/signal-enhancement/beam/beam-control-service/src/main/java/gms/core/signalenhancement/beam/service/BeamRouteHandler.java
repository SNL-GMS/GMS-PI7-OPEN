package gms.core.signalenhancement.beam.service;

import static gms.core.signalenhancement.beam.service.ContentType.isAny;
import static gms.core.signalenhancement.beam.service.ContentType.isJson;
import static gms.core.signalenhancement.beam.service.ContentType.isMsgPack;
import static java.util.stream.Collectors.toList;

import gms.core.signalenhancement.beam.core.BeamControl;
import gms.core.signalenhancement.beam.core.BeamStreamingCommand;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ProcessingGroupDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles HTTP invocation to {@link BeamControl}
 */
public class BeamRouteHandler {

  private Logger logger = LoggerFactory.getLogger(BeamRouteHandler.class);

  private final BeamControl beamControl;

  private BeamRouteHandler(BeamControl beamControl) {
    this.beamControl = beamControl;
  }

  /**
   * Obtains a new {@link BeamRouteHandler} that delegates calls to the {@link BeamControl}
   *
   * @param beamControl BeamControl, not null
   * @return constructed {@link BeamRouteHandler}, not null
   */
  public static BeamRouteHandler create(
      BeamControl beamControl) {
    Objects.requireNonNull(beamControl,
        "BeamRouteHandler cannot be constructed with null beamControl");
    return new BeamRouteHandler(beamControl);
  }

  /**
   * Route handler for streaming invocation of {@link BeamControl} Body must be a serialized {@link
   * BeamStreamingCommand}
   *
   * @param requestBodyType {@link ContentType} of the request body content, not null
   * @param body request body content, possibly empty or malformed, not null
   * @param responseBodyType client's desired {@link ContentType} of the response body, not null
   * @return {@link StandardResponse}, not null
   */
  public StandardResponse streaming(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {

    Objects.requireNonNull(requestBodyType,
        "BeamRouteHandler requires non-null requestBodyType");
    Objects.requireNonNull(body, "BeamRouteHandler requires non-null body");
    Objects.requireNonNull(responseBodyType,
        "BeamRouteHandler requires non-null responseBodyType");

    logger
        .info(
            "Invoked Beam Service / Streaming with Content-Type: {}, Accept: {}, and body length: {}",
            requestBodyType, requestBodyType, body.length);

    if (!isAcceptableStreamingType(requestBodyType, true)) {
      final String message =
          "BeamControl streaming invocation cannot accept inputs in format "
              + requestBodyType;
      return StandardResponse
          .create(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, message, ContentType.TEXT_PLAIN);
    }

    if (!isAcceptableStreamingType(responseBodyType, false)) {
      final String message =
          "BeamControl streaming invocation cannot provide outputs in format "
              + responseBodyType;
      return StandardResponse
          .create(HttpStatus.NOT_ACCEPTABLE_406, message, ContentType.TEXT_PLAIN);
    }

    logger.info("Content-Type and Accept types both acceptable");

    logger.info("Action:beamForming Step:deserializeStart");
    BeamStreamingCommand beamStreamingCommand = getStreamingDeserializer(
        requestBodyType).apply(body);
    logger.info("Action:beamForming Step:deserializeEnd");

    logger.info("Created BeamStreamingCommand: outpuChannelId: {}, waveforms: {}, beamDefinition: {}",
        beamStreamingCommand.getOutputChannelId(),
        beamStreamingCommand.getWaveforms().size(),
        beamStreamingCommand.getBeamDefinition());

    logger.info("Action:beamForming Step:processingStart");
    List<ChannelSegment<Waveform>> beamChannelSegments = beamControl
        .executeStreaming(beamStreamingCommand);
    logger.info("Action:beamForming Step:processingEnd");

    logger.info("Action:beamForming Step:serializeStart");
    Object responseBody = responseBodyType.getSerializer().apply(beamChannelSegments);
    logger.info("Action:beamForming Step:serializeEnd");

    return StandardResponse.create(HttpStatus.OK_200,
        responseBody,
        responseBodyType);
  }

  /**
   * Determines if the {@link ContentType} can be used by the streaming Beam service
   *
   * @param contentType a ContentType, not null
   * @param isRequestType whether the provided ContentType should be validated for use as a request
   * body type or response body type
   * @return whether contentType can be accepted and returned by the streaming Beam service
   */
  private static boolean isAcceptableStreamingType(ContentType contentType, boolean isRequestType) {
    return isRequestType ? isJson.or(isMsgPack).test(contentType)
        : isJson.or(isMsgPack).or(isAny).test(contentType);
  }

  /**
   * Obtains a function to deserialize a {@link ContentType} byte array into a {@link
   * BeamStreamingCommand}. Assumes the content type has already been validated as supported.
   *
   * @param type ContentType, not null
   * @return Function to map a byte[] into a BeamStreamingCommand
   */
  private static Function<byte[], BeamStreamingCommand> getStreamingDeserializer(
      ContentType type) {
    return type.getDeserializer(BeamStreamingCommand.class);
  }

  /**
   * Route handler for claim check invocation of {@link BeamControl} Body must be a serialized
   * {@link ProcessingGroupDescriptor}
   *
   * @param requestBodyType {@link ContentType} of the request body content, not null
   * @param body request body content, possibly empty or malformed, not null
   * @param responseBodyType client's desired {@link ContentType} of the response body, not null
   * @return {@link StandardResponse}, not null
   */
  public StandardResponse claimCheck(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {

    Objects.requireNonNull(requestBodyType,
        "BeamRouteHandler requires non-null requestBodyType");
    Objects.requireNonNull(body, "BeamRouteHandler requires non-null body");
    Objects.requireNonNull(responseBodyType,
        "BeamRouteHandler requires non-null responseBodyType");

    logger.info(
        "Invoked Beam Service / Claim Check with Content-Type: {}, Accept: {}, and body length: {}",
        requestBodyType, requestBodyType, body.length);

    if (!isAcceptableClaimCheckType(requestBodyType, true)) {
      final String message =
          "BeamControl claim check invocation cannot accept inputs in format "
              + requestBodyType;
      return StandardResponse
          .create(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, message, ContentType.TEXT_PLAIN);
    }

    if (!isAcceptableClaimCheckType(responseBodyType, false)) {
      final String message =
          "BeamControl claim check invocation cannot provide outputs in format "
              + responseBodyType;
      return StandardResponse
          .create(HttpStatus.NOT_ACCEPTABLE_406, message, ContentType.TEXT_PLAIN);
    }

    logger.info("Content-Type and Accept types both acceptable");

    final ProcessingGroupDescriptor processingGroupDescriptor = getProcessingGroupDescriptorDeserializer(
        requestBodyType).apply(body);
    logger.info("Created ProcessingGroupDescriptor: {}", processingGroupDescriptor);

    List<ChannelSegment<Waveform>> beams = beamControl.executeClaimCheck(processingGroupDescriptor);

    if (!beams.isEmpty()) {
      logger.info("Storing {} beams", beams.size());
      beamControl.storeWaveforms(beams);
    }

    List<ChannelSegmentDescriptor> beamDescriptors = beams.stream()
        .map(ChannelSegmentDescriptor::from)
        .collect(toList());

    return StandardResponse.create(HttpStatus.OK_200,
        responseBodyType.getSerializer().apply(beamDescriptors),
        responseBodyType);
  }

  /**
   * Determines if the {@link ContentType} can be used by the claim check Beam service
   *
   * @param contentType a ContentType, not null
   * @param isRequest whether the provided ContentType should be validated for use as a request body
   * type or response body type
   * @return whether contentType can be accepted and returned by the claim check Beam service
   */
  private static boolean isAcceptableClaimCheckType(ContentType contentType, boolean isRequest) {
    return isRequest ? isJson.test(contentType) : isJson.or(isAny).test(contentType);
  }

  /**
   * Obtains a function to deserialize a {@link ContentType} byte array into a {@link
   * ProcessingGroupDescriptor}. Assumes the content type has already been validated as supported.
   *
   * @param type ContentType, not null
   * @return Function to map a byte[] into a ProcessingGroupDescriptor
   */
  private static Function<byte[], ProcessingGroupDescriptor> getProcessingGroupDescriptorDeserializer(
      ContentType type) {
    return type.getDeserializer(ProcessingGroupDescriptor.class);
  }
}
