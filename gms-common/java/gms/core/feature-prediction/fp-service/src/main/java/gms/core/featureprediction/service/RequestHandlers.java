package gms.core.featureprediction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.utilities.service.Request;
import gms.shared.utilities.service.RequestParsingUtils;
import gms.shared.utilities.service.RequestParsingUtils.DeserializationException;
import gms.shared.utilities.service.Response;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Contains handler functions for the service routes.
 */
public class RequestHandlers {

  private Logger logger = LoggerFactory.getLogger(RequestHandlers.class);

  private SignalFeaturePredictionUtility signalFeaturePredictionUtility;

  public RequestHandlers() {
    signalFeaturePredictionUtility = new SignalFeaturePredictionUtility();
  }

  public Response<String> isAlive(Request request, ObjectMapper deserializer) {
    return Response.success("" + System.currentTimeMillis());
  }

  /**
   * Computes and returns feature predictions based on an event location, and one or more receiver
   * locations.
   *
   * event location = (latitude, longitude, depth, and time) receiver location = (latitude,
   * longitude, depth, and elevation)
   *
   * @return feature predictions
   */
  public Response<List<FeaturePrediction<?>>> featurePredictionsForSourceAndReceiverLocations(
      Request request, ObjectMapper deserializer) {

    try {

      // Deserialize the input object.
      logger.info("Action:featurePredictionsForSourceAndReceiverLocations Step:deserializeStart");
      final StreamingFeaturePredictionsForSourceAndReceiverLocations coi =
          RequestParsingUtils.extractRequest(request, deserializer,
              StreamingFeaturePredictionsForSourceAndReceiverLocations.class);
      logger.info("Action:featurePredictionsForSourceAndReceiverLocations Step:deserializeEnd");

      logger.info("Action:featurePredictionsForSourceAndReceiverLocations Step:processingStart");
      // Call the signal-feature-prediction-utility.
      List<FeaturePrediction<?>> featurePredictions;
      try {
        featurePredictions = signalFeaturePredictionUtility.predict(
            coi.getFeatureMeasurementTypes(), coi.getSourceLocation(), new HashSet<>(coi.getReceiverLocations()),
            coi.getPhase(), coi.getModel(), coi.getCorrectionDefinitions());
      } catch (Exception e) {
        String errMsg = "One or more feature prediction plugins threw an exception: " + e.toString();
        logger.error(errMsg);

        // Return an error response.
        return Response.serverError(errMsg);
      }
      logger.info("Action:featurePredictionsForSourceAndReceiverLocations Step:processingEnd");

      // Return a success response.
      return Response.success(featurePredictions);

    } catch (DeserializationException de) {
      logger.error(de.getMessage());
      return Response.clientError(de.getMessage());
    }
  }

  /**
   * Computes FeaturePredictions based on a LocationSolution of an EventHypothesis and receiving
   * Channels. Updates and returns the LocationSolution with the calculated FeaturePredictions.
   *
   * @return the LocationSolution with the calculated FeaturePredictions
   */
  public Response<LocationSolution> featurePredictionsForLocationSolutionAndChannel(
      Request request, ObjectMapper deserializer) {
    try {

      logger.info("Action:featurePredictionsForLocationSolutionAndChannel Step:deserializeStart");
      // Deserialize the input object.
      final StreamingFeaturePredictionsForLocationSolutionAndChannel coi =
          RequestParsingUtils.extractRequest(request, deserializer,
              StreamingFeaturePredictionsForLocationSolutionAndChannel.class);
      logger.info("Action:featurePredictionsForLocationSolutionAndChannel Step:deserializeEnd");

      logger.info("Action:featurePredictionsForLocationSolutionAndChannel Step:processingStart");
      // Call the signal-feature-prediction-utility.
      LocationSolution updatedLocationSolution;
      try {
        updatedLocationSolution = signalFeaturePredictionUtility.predict(
            coi.getFeatureMeasurementTypes(), coi.getSourceLocation(), coi.getReceiverLocations(),
            coi.getPhase(), coi.getModel(), coi.getCorrectionDefinitions());
      } catch (Exception e) {
        String errMsg = "One or more feature prediction plugins threw an exception: " + e.toString();
        logger.error(errMsg);

        // Return an error response.
        return Response.serverError(errMsg);
      }
      logger.info("Action:featurePredictionsForLocationSolutionAndChannel Step:processingEnd");

      // Return a success response.
      return Response.success(updatedLocationSolution);

    } catch (DeserializationException de) {
      logger.error(de.getMessage());
      return Response.clientError(de.getMessage());
    }

  }
}
