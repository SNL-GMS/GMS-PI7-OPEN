package gms.core.signalenhancement.fk.service.fkspectra;

import static gms.core.signalenhancement.fk.service.ContentType.isAny;
import static gms.core.signalenhancement.fk.service.ContentType.isJson;
import static gms.core.signalenhancement.fk.service.ContentType.isMsgPack;

import com.google.common.base.Preconditions;
import gms.core.signalenhancement.fk.control.FkControl;
import gms.core.signalenhancement.fk.control.FkSpectraCommand;
import gms.core.signalenhancement.fk.service.ContentType;
import gms.core.signalenhancement.fk.service.StandardResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.StationProcessingInterval;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles HTTP invocation to {@link FkControl}
 */
public class FkRouteHandler {

  private static Logger logger = LoggerFactory.getLogger(FkRouteHandler.class);
  private static final Predicate<ContentType> invalidRequest = isJson.or(isMsgPack).negate();
  private static final Predicate<ContentType> invalidResponse = isJson.or(isMsgPack).or(isAny)
      .negate();

  private final FkControl fkControl;

  private FkRouteHandler(FkControl fkControl) {
    this.fkControl = fkControl;
  }

  /**
   * Obtains a new {@link FkRouteHandler} that delegates calls to the {@link FkControl}
   *
   * @param fkControl fkControl, not null
   * @return constructed {@link FkRouteHandler}, not null
   */
  public static FkRouteHandler create(
      FkControl fkControl) {
    Objects.requireNonNull(fkControl,
        "FkRouteHandler cannot be constructed with null fkControl");
    return new FkRouteHandler(fkControl);
  }

  /**
   * Route handler for claim check invocation of {@link FkControl} Body must be a serialized {@link
   * StationProcessingInterval}
   *
   * @param requestBodyType {@link ContentType} of the request body content, not null
   * @param body request body content, possibly empty or malformed, not null
   * @param responseBodyType client's desired {@link ContentType} of the response body, not null
   * @return {@link StandardResponse}, not null
   */
  public StandardResponse featureMeasurements(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {
    Preconditions.checkNotNull(requestBodyType);
    Preconditions.checkNotNull(body);
    Preconditions.checkNotNull(responseBodyType);

    logger.info(
        "Invoked Fk Analysis with Content-Type: {}, Accept: {}, and body length: {}",
        requestBodyType, requestBodyType, body.length);

    if (invalidRequest.test(requestBodyType)) {
      final String message =
          "fkControl claim check invocation cannot accept inputs in format "
              + requestBodyType;
      return StandardResponse
          .create(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, message, ContentType.TEXT_PLAIN);
    }

    if (invalidResponse.test(responseBodyType)) {
      final String message =
          "fkControl claim check invocation cannot provide outputs in format "
              + responseBodyType;
      return StandardResponse
          .create(HttpStatus.NOT_ACCEPTABLE_406, message, ContentType.TEXT_PLAIN);
    }

    logger.info("Content-Type and Accept types both acceptable");

    // Invoke fkControl and construct a StandardResponse from the results
    final SignalDetectionHypothesisDescriptor[] sdhDescriptors = requestBodyType
        .getDeserializer(SignalDetectionHypothesisDescriptor[].class).apply(body);

    logger.info("Created FkAnalysisCommand: {}", sdhDescriptors);

    return StandardResponse.create(HttpStatus.OK_200,
        responseBodyType.getSerializer().apply(fkControl.measureFkFeatures(Arrays.asList(sdhDescriptors))),
        responseBodyType);
  }

  /**
   * Route handler for spectra invocation of {@link FkControl} Body must be a serialized {@link
   * FkSpectraCommand}
   *
   * @param requestBodyType {@link ContentType} of the request body content, not null
   * @param body request body content, possibly empty or malformed, not null
   * @param responseBodyType client's desired {@link ContentType} of the response body, not null
   * @return {@link StandardResponse}, not null
   */
  public StandardResponse interactiveSpectra(ContentType requestBodyType, byte[] body,
      ContentType responseBodyType) {
    Preconditions.checkNotNull(requestBodyType);
    Preconditions.checkNotNull(body);
    Preconditions.checkNotNull(responseBodyType);

    logger.info(
        "Invoked Fk Spectra with Content-Type: {}, Accept: {}, and body length: {}",
        requestBodyType, responseBodyType, body.length);

    if (invalidRequest.test(requestBodyType)) {
      final String message =
          "fkControl spectra invocation cannot accept inputs in format "
              + requestBodyType;
      return StandardResponse
          .create(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, message, ContentType.TEXT_PLAIN);
    }

    if (invalidResponse.test(responseBodyType)) {
      final String message =
          "fkControl spectra invocation cannot provide outputs in format "
              + responseBodyType;
      return StandardResponse
          .create(HttpStatus.NOT_ACCEPTABLE_406, message, ContentType.TEXT_PLAIN);
    }

    logger.info("Content-Type and Accept types both acceptable");

    logger.info("Action:generateFkSpectra Step:deserializeStart");
    FkSpectraCommand fkSpectraCommand = requestBodyType.getDeserializer(FkSpectraCommand.class)
        .apply(body);
    logger.info("Action:generateFkSpectra Step:deserializeEnd");

    logger.info("Action:generateFkSpectra Step:processingStart");
    List<ChannelSegment<FkSpectra>> fkSpectraSegments = fkControl.generateFkSpectra(fkSpectraCommand);
    logger.info("Action:generateFkSpectra Step:processingEnd");

    logger.info("Action:generateFkSpectra Step:serializeStart");
    Object responseBody = responseBodyType.getSerializer().apply(fkSpectraSegments);
    logger.info("Action:generateFkSpectra Step:serializeEnd");

    return StandardResponse.create(HttpStatus.OK_200,
        responseBody,
        responseBodyType);
  }

}
