package gms.core.featureprediction.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.utilities.service.HttpService;
import gms.shared.utilities.service.Route;
import gms.shared.utilities.service.ServiceDefinition;
import java.util.Set;


public class Application {

  /**
   * Starts the application with default parameters.
   *
   * @param args N/A
   */
  public static void main(String[] args) {
    new HttpService(getHttpServiceDefinition(8080)).start();
  }

  /**
   * Return the service definition.
   *
   * @param port port number
   * @return service definition
   */
  public static ServiceDefinition getHttpServiceDefinition(int port) {
    RequestHandlers handlers = new RequestHandlers();

    return ServiceDefinition.builder()
        .setJsonMapper(CoiObjectMapperFactory.getJsonObjectMapper())
        .setMsgpackMapper(CoiObjectMapperFactory.getMsgpackObjectMapper())
        .setRoutes(Set.of(
            Route.create(
                "/feature-measurement/prediction/for-source-and-receiver-locations",
                handlers::featurePredictionsForSourceAndReceiverLocations),
            Route.create(
                "/feature-measurement/prediction/for-location-solution-and-channel",
                handlers::featurePredictionsForLocationSolutionAndChannel),
            Route.create(
                "/is-alive",
                handlers::isAlive)))
        .setPort(port)
        .build();
  }

}
