package gms.shared.frameworks.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.utilities.ServerConfig;
import java.time.Instant;
import java.util.Objects;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for controlling a service. The main entry point of this library.
 */
public class HttpService {

  private static final Logger logger = LoggerFactory.getLogger(HttpService.class);

  private boolean isRunning = false;

  private final ServiceDefinition definition;

  private final spark.Service sparkService;

  public HttpService(ServiceDefinition def) {
    this.definition = Objects.requireNonNull(def, "Cannot create HttpService will null definition");
    this.sparkService = spark.Service.ignite();
    // Register a handler that stops the service if the JVM is shutting down
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
  }

  /**
   * Indicates whether the service is currently running.
   *
   * @return true if the service is running, false otherwise.
   */
  public boolean isRunning() {
    return this.isRunning;
  }

  /**
   * Returns the definition of this service.
   * @return the definition
   */
  public ServiceDefinition getDefinition() {
    return this.definition;
  }

  /**
   * Starts the service.  If the service is already running (e.g. this
   * method has been called before), this call throws an exception.  This method
   * configures the HTTP server (e.g. sets port), registers service routes and exception handlers,
   * and launches the service.
   */
  public void start() {
    // if service is running, throw an exception.
    if (isRunning) {
      throw new IllegalStateException("Service is already running");
    }
    logger.info("Starting service...");
    configureServer();
    logger.info("Registering healthcheck route at {}", ServiceDefinition.HEALTHCHECK_PATH);
    this.sparkService.get(ServiceDefinition.HEALTHCHECK_PATH, (req, res) -> "alive at " + Instant.now());
    // register routes
    logger.info("Registering {} routes", this.definition.getRoutes().size());
    for (Route r : this.definition.getRoutes()) {
      // define a Spark route (wrap the ServiceDefinition Route with Spark specifics)
      final spark.Route sparkRoute = sparkRoute(r.getHandler(),
          this.definition.getJsonMapper(), this.definition.getMsgpackMapper());
      // register the route
      logger.info("Registering route with path {}", r.getPath());
      this.sparkService.post(r.getPath(), sparkRoute);
    }
    // start the service
    logger.info("Starting the service...");
    this.sparkService.awaitInitialization();
    isRunning = true;
    logger.info("Service is now running on port {}",
        this.definition.getServerConfig().getPort());
  }

  /**
   * Stops the service.  If the service was not running, this call does nothing.
   */
  public void stop() {
    logger.info("Stopping the service...");
    this.sparkService.stop();
    logger.info("Awaiting the service to be stopped...");
    this.sparkService.awaitStop();
    isRunning = false;
    logger.info("Service is stopped");
  }

  /**
   * Configures the HTTP server
   *
   */
  private void configureServer() {
    final ServerConfig config = this.definition.getServerConfig();
    // Set the listening port.
    logger.info("Setting port of server to {}", config.getPort());
    this.sparkService.port(config.getPort());
    logger.info(String.format("Configuring thread pool of server; "
            + "min = %d, max = %d, idle timeout = %s",
        config.getMinThreadPoolSize(),
        config.getMaxThreadPoolSize(),
        config.getThreadIdleTimeout()));
    this.sparkService.threadPool(config.getMaxThreadPoolSize(),
        config.getMinThreadPoolSize(),
        Math.toIntExact(config.getThreadIdleTimeout().toMillis()));
    // Exception handler.
    this.sparkService.exception(Exception.class,
        (ex, req, resp) -> handleServiceException(ex, resp));
    // HTTP custom error handlers.
    this.sparkService.notFound(HttpErrorHandlers::handleNotFound);
    this.sparkService.internalServerError(
        (req, resp) -> HttpErrorHandlers.handleInternalServerError(resp));
    this.sparkService.after((request, response) -> response.header(
        "access-control-allow-origin", "*"));

  }

  /**
   * Creates a Spark Route given a Route and serialization objects to use.
   *
   * @param handler the request handler
   * @param jsonMapper a serialization object for JSON
   * @param msgpackMapper a serialization object for msgpack
   * @return a Spark Route function that uses the provided RequestHandler and serialization objects
   */
  private static spark.Route sparkRoute(RequestHandler handler,
      ObjectMapper jsonMapper, ObjectMapper msgpackMapper) {

    return (sparkRequest, sparkResponse) -> {
      // wrap the Request
      final Request request = new SparkRequest(sparkRequest);
      // get the proper deserializer for the Request
      final ObjectMapper deserializer = request.clientSentMsgpack() ? msgpackMapper : jsonMapper;
      // invoke the route handler
      final Response routeResponse = invokeHandler(handler, request, deserializer);
      // appropriately set the attributes of the spark Response object
      sparkResponse.status(routeResponse.getHttpStatus().getStatusCode());
      // check if error message is set; return the error message as plain text if so.
      if (routeResponse.getErrorMessage().isPresent()) {
        sparkResponse.type("text/plain");
        return routeResponse.getErrorMessage().get();
      } else {
        Validate.isTrue(routeResponse.getBody().isPresent(),
            "Expected body to be present since error message was not present");
        // serialize based on what client accepts and return the response
        if (request.clientAcceptsMsgpack()) {
          sparkResponse.type("application/msgpack");
          return msgpackMapper.writeValueAsBytes(routeResponse.getBody().get());
        } else {
          sparkResponse.type("application/json");
          return jsonMapper.writeValueAsString(routeResponse.getBody().get());
        }
      }
    };
  }

  /**
   * Convenience function for calling a request handler on a request safely, returning either the
   * Response from the handler or a server error if the handler throws an exception
   *
   * @param handler the request handler
   * @param request the request
   * @param deserializer the deserializer
   * @return a Response; if the handler runs without throwing an exception this is just the result
   * of handler.handle(request, deserializer)... if it throws an exception, a server error Response
   * is returned.
   */
  private static Response invokeHandler(RequestHandler handler,
      Request request, ObjectMapper deserializer) {
    try {
      return handler.handle(request, deserializer);
    } catch (Exception ex) {
      logger.error("Route handler threw exception", ex);
      return Response.serverError(ex.getMessage());
    }
  }

  /**
   * Catch-all exception handler.  If this library is written correctly,
   * this should never be invoked.
   *
   * @param e the exception that was caught.
   * @param response the response that was being built when the exception was thrown
   */
  private static void handleServiceException(
      Exception e, spark.Response response) {
    logger.error("Unhandled exception from service request, returning code 500 "
        + "(is the http-service-library working correctly?)", e);
    response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
    response.type("application/text");
    response.body(e.getMessage() == null ? "" : e.getMessage());
  }
}
