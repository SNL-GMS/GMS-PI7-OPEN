package gms.core.eventlocation.control.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.eventlocation.control.EventHypothesisClaimCheck;
import gms.core.eventlocation.control.EventLocationControl;
import gms.core.eventlocation.control.EventLocationControlParameters;
import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.utilities.service.Request;
import gms.shared.utilities.service.RequestParsingUtils;
import gms.shared.utilities.service.RequestParsingUtils.DeserializationException;
import gms.shared.utilities.service.Response;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains functions that handle HTTP requests for the routes defined in {@link Application}.
 */
public class RequestHandlers {

  private final Logger logger = LoggerFactory.getLogger(RequestHandlers.class);

  private static final String MISSING_PARAMETER_ERROR_MSG = "Json field \"%s\" does not exist in the request body";

  private static final String EVENT_LOCATION_CONTROL_PARAMETERS = "parameters";

  private final EventLocationControl control;


  private RequestHandlers(EventLocationControl control) {

    this.control = control;
  }


  public static RequestHandlers create(EventLocationControl control) {

    return new RequestHandlers(control);
  }


  /**
   * Handles the /is-alive endpoint.
   *
   * Used to determine if the service is running.  Returns a 200 response with the current system
   * time.
   *
   * @param request {@link Request} object representing the HTTP request.
   * @param deserializer {@link ObjectMapper} to use for deserializing request body contents.
   * @return {@link Response}, representing the HTTP response.  Not Null.
   */
  public Response<String> isAlive(Request request, ObjectMapper deserializer) {

    this.logger.info("Handling request asking if the service is alive");

    return Response.success(Long.toString(System.currentTimeMillis()));
  }


  /**
   * Handles the /event/location/locate endpoint.
   *
   * Deserializes request body, calls the control class to perform event location via automated
   * processing, and returns a {@link Collection} of the resulting {@link
   * EventHypothesisClaimCheck}s
   *
   * @param request {@link Request} object representing the HTTP request.
   * @param deserializer {@link ObjectMapper} to use for deserializing request body contents.
   * @return {@link Response}, representing the HTTP response.  Not Null.
   */
  public Response<Collection<EventHypothesisClaimCheck>> locate(Request request,
      ObjectMapper deserializer) {

    this.logger.info("Handling request to locate via automated processing endpoint");

    // Define objects to deserialize from request body

    JsonNode requestJsonNode;
    Collection<EventHypothesisClaimCheck> eventHypothesisClaimChecks;
    Optional<EventLocationControlParameters> parameters;

    try {

      // Deserialize request body into JsonNode

      requestJsonNode = RequestParsingUtils.extractRequest(request, deserializer);

      // Deserialize EventHypothesisClaimChecks

      final String EVENT_HYPOTHESIS_CLAIM_CHECKS = "eventHypothesisClaimChecks";

      Optional<List<EventHypothesisClaimCheck>> optionalEventHypothesisClaimChecks = RequestParsingUtils
          .extractRequestElementList(requestJsonNode, deserializer, EVENT_HYPOTHESIS_CLAIM_CHECKS,
              EventHypothesisClaimCheck.class);

      if (optionalEventHypothesisClaimChecks.isPresent()) {

        eventHypothesisClaimChecks = optionalEventHypothesisClaimChecks.get();
      } else {

        String errorMsg = String
            .format(RequestHandlers.MISSING_PARAMETER_ERROR_MSG, EVENT_HYPOTHESIS_CLAIM_CHECKS);
        this.logger.error(errorMsg);
        return Response.clientError(errorMsg);
      }

      // Deserialize EventLocationControlParameters

      parameters = RequestParsingUtils
          .extractRequestElement(requestJsonNode, deserializer,
              RequestHandlers.EVENT_LOCATION_CONTROL_PARAMETERS,
              EventLocationControlParameters.class);
    } catch (DeserializationException e) {

      this.logger.error(e.getMessage());
      return Response.clientError(e.getMessage());
    }

    Collection<EventHypothesisClaimCheck> responseBodyContents;

    try {
      if (parameters.isPresent()) {
        responseBodyContents = this.control.locate(eventHypothesisClaimChecks, parameters.get());
      } else {
        responseBodyContents = this.control.locate(eventHypothesisClaimChecks);
      }
    } catch (TooManyRestraintsException e) {
      return Response.serverError(e.getMessage());
    }
    return Response.success(responseBodyContents);
  }


  /**
   * Handles the /event/location/locate/interactive endpoint.
   *
   * Deserializes request body, calls the control class to perform event location via interactive
   * processing, and returns a {@link Map} from {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis}
   * {@link UUID} to a {@link Collection} of {@link LocationSolution}
   *
   * @param request {@link Request} object representing the HTTP request.
   * @param deserializer {@link ObjectMapper} to use for deserializing request body contents.
   * @return {@link Response}, representing the HTTP response.  Not Null.
   */
  public Response<Map<UUID, Set<LocationSolution>>> locateInteractive(Request request,
      ObjectMapper deserializer) {

    this.logger.info("Handling request to locate via interactive endpoint");

    // Define objects to deserialize from request body

    JsonNode requestJsonNode;
    List<EventHypothesis> eventHypotheses;
    List<SignalDetection> signalDetections;
    Optional<EventLocationControlParameters> parameters;

    try {

      // Deserialize request body into JsonNode

      requestJsonNode = RequestParsingUtils.extractRequest(request, deserializer);

      // Deserialize EventHypotheses

      final String EVENT_HYPOTHESES = "eventHypotheses";

      Optional<List<EventHypothesis>> optionalEventHypotheses = RequestParsingUtils
          .extractRequestElementList(requestJsonNode, deserializer, EVENT_HYPOTHESES,
              EventHypothesis.class);

      if (optionalEventHypotheses.isPresent()) {

        eventHypotheses = optionalEventHypotheses.get();
      } else {

        String errorMsg = String
            .format(RequestHandlers.MISSING_PARAMETER_ERROR_MSG, EVENT_HYPOTHESES);
        this.logger.error(errorMsg);
        return Response.clientError(errorMsg);
      }

      // Deserialize SignalDetections

      final String SIGNAL_DETECTIONS = "signalDetections";

      Optional<List<SignalDetection>> optionalSignalDetections = RequestParsingUtils
          .extractRequestElementList(requestJsonNode, deserializer, SIGNAL_DETECTIONS,
              SignalDetection.class);

      if (optionalSignalDetections.isPresent()) {

        signalDetections = optionalSignalDetections.get();
      } else {

        String errorMsg = String
            .format(RequestHandlers.MISSING_PARAMETER_ERROR_MSG, SIGNAL_DETECTIONS);
        this.logger.error(errorMsg);
        return Response.clientError(errorMsg);
      }

      // Deserialize EventLocationControlParameters

      parameters = RequestParsingUtils.extractRequestElement(requestJsonNode, deserializer,
          RequestHandlers.EVENT_LOCATION_CONTROL_PARAMETERS, EventLocationControlParameters.class);

    } catch (DeserializationException e) {

      this.logger.error(e.getMessage());
      return Response.clientError(e.getMessage());
    }

    Map<UUID, Set<LocationSolution>> responseBodyContents;

    try {
      if (parameters.isPresent()) {
        responseBodyContents = this.control
            .locateInteractive(eventHypotheses, signalDetections, parameters.get());
      } else {
        responseBodyContents = this.control.locateInteractive(eventHypotheses, signalDetections);
      }
    } catch (TooManyRestraintsException e) {
      return Response.serverError(e.getMessage());
    }

    return Response.success(responseBodyContents);
  }


  /**
   * Handles the /event/location/locate/validation endpoint.
   *
   * Deserializes request body, calls the control class to perform event location via validation
   * processing, and returns a {@link Map} from {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis}
   * {@link UUID} to a {@link Collection} of {@link LocationSolution}
   *
   * @param request {@link Request} object representing the HTTP request.
   * @param deserializer {@link ObjectMapper} to use for deserializing request body contents.
   * @return {@link Response}, representing the HTTP response.  Not Null.
   */
  public Response<Map<UUID, Set<LocationSolution>>> locateValidation(Request request,
      ObjectMapper deserializer) {

    this.logger.info("Handling request to locate via validation endpoint");

    JsonNode requestJsonNode;

    EventHypothesis eventHypothesis;
    List<SignalDetection> signalDetections;
    Set<ReferenceStation> referenceStations;
    Optional<EventLocationControlParameters> optionalEventLocationControlParameters;

    try {

      // Deserialize request body into JsonNode

      requestJsonNode = RequestParsingUtils.extractRequest(request, deserializer);

      // Deserialize EventHypothesis

      final String EVENT_HYPOTHESIS = "eventHypothesis";

      Optional<EventHypothesis> optionalEventHypothesis = RequestParsingUtils
          .extractRequestElement(requestJsonNode, deserializer, EVENT_HYPOTHESIS,
              EventHypothesis.class);

      if (optionalEventHypothesis.isPresent()) {

        eventHypothesis = optionalEventHypothesis.get();
      } else {

        String errorMsg = String
            .format(RequestHandlers.MISSING_PARAMETER_ERROR_MSG, EVENT_HYPOTHESIS);
        this.logger.error(errorMsg);
        return Response.clientError(errorMsg);
      }

      // Deserialize SignalDetections

      final String SIGNAL_DETECTIONS = "signalDetections";

      Optional<List<SignalDetection>> optionalSignalDetections = RequestParsingUtils
          .extractRequestElementList(requestJsonNode, deserializer, SIGNAL_DETECTIONS,
              SignalDetection.class);

      if (optionalSignalDetections.isPresent()) {

        signalDetections = optionalSignalDetections.get();
      } else {

        String errorMsg = String
            .format(RequestHandlers.MISSING_PARAMETER_ERROR_MSG, SIGNAL_DETECTIONS);
        this.logger.error(errorMsg);
        return Response.clientError(errorMsg);
      }

      // Deserialize reference stations

      final String REFERENCE_STATIONS = "referenceStations";

      // Currently no mechanism in RequestParsingUtils to deserialize Sets, so must serialize to list then
      // convert to Set manually
      Optional<List<ReferenceStation>> optionalReferenceStationList = RequestParsingUtils
          .extractRequestElementList(requestJsonNode, deserializer, REFERENCE_STATIONS,
              ReferenceStation.class);

      if (optionalReferenceStationList.isPresent()) {

        referenceStations = new HashSet<>(optionalReferenceStationList.get());
      } else {

        String errorMsg = String
            .format(RequestHandlers.MISSING_PARAMETER_ERROR_MSG, REFERENCE_STATIONS);
        this.logger.error(errorMsg);
        return Response.clientError(errorMsg);
      }

      // Deserialize EventLocationControlParameters

      optionalEventLocationControlParameters = RequestParsingUtils
          .extractRequestElement(requestJsonNode, deserializer,
              RequestHandlers.EVENT_LOCATION_CONTROL_PARAMETERS,
              EventLocationControlParameters.class);

    } catch (DeserializationException e) {

      this.logger.error("Error deserializing request to locate via validation endpoint", e);
      return Response.clientError(
          "Error deserializing request to locate via validation endpoint: " + e.getMessage());
    }

    Map<UUID, Set<LocationSolution>> responseBodyContents;

    LocateValidationInput locateValidationInput;

    try {
      locateValidationInput = LocateValidationInput.create(
          eventHypothesis,
          signalDetections,
          referenceStations
      );
    } catch (IllegalArgumentException e) {

      logger.info("Error while validating request", e);
      return Response
          .clientError(String.format("Error while validating request: %s", e.getMessage()));
    }
    try {
      if (optionalEventLocationControlParameters.isPresent()) {
        responseBodyContents = this.control
            .locateValidation(locateValidationInput, optionalEventLocationControlParameters.get());
      } else {
        responseBodyContents = this.control.locateValidation(locateValidationInput);
      }
    } catch (TooManyRestraintsException e) {
      return Response.serverError(e.getMessage());
    }

    return Response.success(responseBodyContents);
  }
}
