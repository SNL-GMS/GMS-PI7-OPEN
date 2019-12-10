package gms.core.signaldetection.association.control.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.signaldetection.association.control.EventHypothesisClaimCheck;
import gms.core.signaldetection.association.control.SignalDetectionAssociationControl;
import gms.core.signaldetection.association.control.SignalDetectionAssociationParameters;
import gms.core.signaldetection.association.control.SignalDetectionAssociationResult;
import gms.core.signaldetection.association.control.SignalDetectionHypothesisClaimCheck;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.utilities.service.Request;
import gms.shared.utilities.service.RequestParsingUtils;
import gms.shared.utilities.service.RequestParsingUtils.DeserializationException;
import gms.shared.utilities.service.Response;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains functions that handle HTTP requests for the routes defined in {@link Application}.
 */
public class RequestHandlers {

  private final Logger logger = LoggerFactory.getLogger(RequestHandlers.class);

  private static final String EVENT_LOCATION = "eventLocation";
  private static final String SIG_DET_HYPOTHESES = "signalDetectionHypotheses";


  private final SignalDetectionAssociationControl control;

  /**
   * Given a {@link SignalDetectionAssociationControl}, return a new {@link RequestHandlers}
   *
   * @param control {@link SignalDetectionAssociationControl} the new {@link RequestHandlers} object
   * should use
   * @return New {@link RequestHandlers}.  Not null.
   */
  static RequestHandlers create(SignalDetectionAssociationControl control) {
    return new RequestHandlers(control);
  }

  // Private constructor called by static factory method
  private RequestHandlers(SignalDetectionAssociationControl control) {
    this.control = control;
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
  Response<String> isAlive(Request request, ObjectMapper deserializer) {
    return Response.success("" + System.currentTimeMillis());
  }

  /**
   * Handles the /event/association/associate-to-location endpoint.
   *
   * Deserializes request body, calls the control class to perform association via claim-check, and
   * returns the resulting {@link EventHypothesisClaimCheck}
   *
   * @param request {@link Request} object representing the HTTP request.
   * @param deserializer {@link ObjectMapper} to use for deserializing request body contents.
   * @return {@link Response}, representing the HTTP response.  Not Null.
   */
  Response<EventHypothesisClaimCheck> associateToLocation(Request request,
      ObjectMapper deserializer) {

    try {

      // Deserialize request body into JsonNode
      final JsonNode requestBodyJsonNode = RequestParsingUtils
          .extractRequest(request, deserializer);

      // Deserialize event location, claim checks, and processing context from the request.
      final EventLocation eventLocation = getRequiredRequestElement(requestBodyJsonNode,
          deserializer,
          RequestHandlers.EVENT_LOCATION,
          EventLocation.class);

      final String SIG_DET_HYPOTHESIS_CLAIM_CHECKS = "signalDetectionHypothesisClaimChecks";
      final List<SignalDetectionHypothesisClaimCheck> signalDetectionHypothesisClaimChecks
          = getRequiredRequestElementList(requestBodyJsonNode,
          deserializer,
          SIG_DET_HYPOTHESIS_CLAIM_CHECKS,
          SignalDetectionHypothesisClaimCheck.class);

      return Response.success(this.control
          .associateToLocation(signalDetectionHypothesisClaimChecks, eventLocation));

    } catch (DeserializationException de) {

      logger.error(de.getMessage());
      return Response.clientError(de.getMessage());

    }
  }

  /**
   * Handles the /event/association/associate-to-location/interactive endpoint.
   *
   * Deserializes request body, calls the control class to perform association, and returns the
   * resulting {@link Event}
   *
   * @param request {@link Request} object representing the HTTP request.
   * @param deserializer {@link ObjectMapper} to use for deserializing request body contents.
   * @return {@link Response}, representing the HTTP response.  Not Null.
   */
  Response<Event> associateToLocationInteractive(Request request,
      ObjectMapper deserializer) {

    try {

      // Deserialize request body into JsonNode
      final JsonNode requestBodyJsonNode = RequestParsingUtils
          .extractRequest(request, deserializer);

      // Define objects to read from request body JsonNode

      // Deserialize event location out of request body JsonNode
      final EventLocation eventLocation = getRequiredRequestElement(requestBodyJsonNode,
          deserializer,
          RequestHandlers.EVENT_LOCATION,
          EventLocation.class);

      final List<SignalDetectionHypothesis> signalDetectionHypotheses =
          getRequiredRequestElementList(requestBodyJsonNode,
              deserializer,
              SIG_DET_HYPOTHESES,
              SignalDetectionHypothesis.class);

      // Call control class with eventLocation, signalDetectionHypotheses, and processingContext

      Event createdEvent = this.control
          .associateToLocationInteractive(signalDetectionHypotheses, eventLocation);

      return Response.success(createdEvent);

    } catch (DeserializationException de) {
      logger.error(de.getMessage());
      return Response.clientError(de.getMessage());
    }
  }

  Response<EventHypothesis> associateToEventHypothesisInteractive(Request request,
      ObjectMapper deserializer) {

    try {

      // Deserialize request body into JsonNode
      final JsonNode requestBodyJsonNode = RequestParsingUtils
          .extractRequest(request, deserializer);


      // Deserialize event hypothesis out of request body JsonNode
      final String EVENT_HYPOTHESIS = "eventHypothesis";

      final EventHypothesis eventHypothesis = getRequiredRequestElement(requestBodyJsonNode,
          deserializer,
          EVENT_HYPOTHESIS,
          EventHypothesis.class);

      // Deserialize signal detection hypotheses out of request body JsonNode
      final List<SignalDetectionHypothesis> signalDetectionHypotheses =
          getRequiredRequestElementList(requestBodyJsonNode,
              deserializer,
              RequestHandlers.SIG_DET_HYPOTHESES,
              SignalDetectionHypothesis.class);

      EventHypothesis newEventHypothesis = this.control
          .associateToEventHypothesisInteractive(signalDetectionHypotheses, eventHypothesis);

      return Response.success(newEventHypothesis);

    } catch (DeserializationException de) {
      logger.error(de.getMessage());
      return Response.clientError(de.getMessage());
    }
  }

  /**
   * This will return a list of signal detections and list of corresponding events.
   * @param request
   * @param deserializer
   * @return
   */
  Response<SignalDetectionAssociationResult> associateDetections(Request request, ObjectMapper deserializer) {
    final String PARAMETERS = "parameters";
    final List<SignalDetectionHypothesisDescriptor> signalDetectionHypotheses;
    final JsonNode requestBodyJsonNode;
    final Optional<SignalDetectionAssociationParameters> params;
    final SignalDetectionAssociationResult result;

    // Parsing Required Parameters
    try {
      requestBodyJsonNode = RequestParsingUtils
          .extractRequest(request, deserializer);

      signalDetectionHypotheses =
          getRequiredRequestElementList(requestBodyJsonNode,
              deserializer,
              RequestHandlers.SIG_DET_HYPOTHESES,
              SignalDetectionHypothesisDescriptor.class);
    } catch (DeserializationException de) {
      logger.error(de.getMessage());
      return Response.clientError(de.getMessage());
    }

    try {
      params = RequestParsingUtils.extractRequestElement(
          requestBodyJsonNode,
          deserializer,
          PARAMETERS,
          SignalDetectionAssociationParameters.class);
    } catch (DeserializationException de) {
      logger.error(de.getMessage());
      return Response.clientError(de.getMessage());
    }

    try {
      if (params.isPresent()) {
        result = this.control.associate(signalDetectionHypotheses, params.get());
      } else {
        result = this.control.associate(signalDetectionHypotheses);
      }
      return Response.success(result);
    } catch(Exception e) {
      logger.error(e.getMessage());
      return Response.serverError("Association failed on data: ");
    }

  }


  // Utility for obtaining a required request element which throws an exception if it is not
  // present.
  private static <T> T getRequiredRequestElement(
      JsonNode requestNode,
      ObjectMapper deserializer,
      String key,
      Class<? extends T> clss) throws DeserializationException {

    Optional<T> opt = RequestParsingUtils.extractRequestElement(
        requestNode,
        deserializer,
        key,
        clss
    );

    if (!opt.isPresent()) {
      throw new DeserializationException("Json field \"" + key +
          "\" does not exist in the request body.");
    }

    return opt.get();
  }

  // Utility for obtaining a required request element list which throws an exception if it is not
  // present.
  private static <T> List<T> getRequiredRequestElementList(
      JsonNode requestNode,
      ObjectMapper deserializer,
      String key,
      Class<? extends T> clss) throws DeserializationException {

    Optional<List<T>> opt = RequestParsingUtils.extractRequestElementList(requestNode,
        deserializer,
        key,
        clss);

    if (!opt.isPresent()) {
      throw new DeserializationException("List field \"" + key
          + "\" does not exist in the request body.");
    }

    return opt.get();
  }
}
