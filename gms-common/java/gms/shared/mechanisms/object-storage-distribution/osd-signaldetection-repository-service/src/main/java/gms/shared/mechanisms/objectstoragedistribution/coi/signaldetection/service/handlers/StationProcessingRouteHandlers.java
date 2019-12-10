package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.RequestUtil;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.ResponseUtil;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

/**
 * {@link spark.Route} handler methods for resolving http requests.
 */
public class StationProcessingRouteHandlers {

  private static final Logger logger = LoggerFactory.getLogger(StationProcessingRouteHandlers.class);

  private final ProcessingStationReferenceFactory processingFactory;

  private StationProcessingRouteHandlers(ProcessingStationReferenceFactory processingFactory) {
    this.processingFactory = processingFactory;
  }


  /**
   * Factory method for creating a new {@link StationProcessingRouteHandlers} object.
   *
   * @param factory Underlying factory used to create station processing objects
   * @return The {@link StationProcessingRouteHandlers}
   */
  public static StationProcessingRouteHandlers create(ProcessingStationReferenceFactory factory) {
    return new StationProcessingRouteHandlers(Objects.requireNonNull(factory));
  }

  /**
   * Retrieve a {@link Network} given a 'name' and 'time' query parameter from the {@link Request}
   *
   * @param request The http request
   * @param response The http response
   * @return A network given the provided parameters, or an empty Optional if no such network exists.
   */
  public String getNetwork(Request request, Response response) {
    Objects.requireNonNull(request);
    Objects.requireNonNull(response);

    logger.info("Received request to retrieve network:{}", request.url());

    String name = Optional.ofNullable(request.queryParams("name")).orElseThrow(
        () -> new IllegalArgumentException(
            "Error retrieving network, must provide name query param."));

    String timeParam = Optional.ofNullable(request.queryParams("time")).orElseThrow(
        () -> new IllegalArgumentException(
            "Error retrieving network, must provide time query param."));

    Instant time = Instant.parse(timeParam);

    if (RequestUtil.clientAcceptsJson(request)) {
      response.type("application/json");
      logger.info("Retrieving network: {} active at time {}", name, time);
      Optional<Network> network = processingFactory.networkFromName(name, time, time);
      logger.info("Resulting network: {}", network);
      return ObjectSerialization.writeValue(network);
    } else {
      return ResponseUtil.notAcceptable(request, response);
    }

  }

}
