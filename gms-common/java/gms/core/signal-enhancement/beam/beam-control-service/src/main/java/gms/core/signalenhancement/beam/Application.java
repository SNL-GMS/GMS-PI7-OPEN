package gms.core.signalenhancement.beam;

import gms.core.signalenhancement.beam.core.BeamConfiguration;
import gms.core.signalenhancement.beam.core.BeamControl;
import gms.core.signalenhancement.beam.osd.client.HttpClientConfiguration;
import gms.core.signalenhancement.beam.osd.client.HttpClientConfigurationLoader;
import gms.core.signalenhancement.beam.osd.client.OsdClient;
import gms.core.signalenhancement.beam.service.BeamCommonRouteHandler;
import gms.core.signalenhancement.beam.service.BeamRouteHandler;
import gms.core.signalenhancement.beam.service.ContentType;
import gms.core.signalenhancement.beam.service.ErrorHandler;
import gms.core.signalenhancement.beam.service.StandardResponse;
import gms.core.signalenhancement.beam.service.configuration.HttpServiceConfiguration;
import gms.core.signalenhancement.beam.service.configuration.HttpServiceConfigurationLoader;
import gms.core.signalenhancement.beamcontrol.plugin.BeamPlugin;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.FileConfigurationRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import java.io.File;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Application {

  private static Logger logger = LoggerFactory.getLogger(Application.class);

  private static final HttpClientConfiguration procGroupServiceConfig = HttpClientConfigurationLoader
      .load("signalDetections_",
          getUrlToResourceFile("gms/core/signalenhancement/beam/osd/client/osdClient.properties"));

  private static final HttpClientConfiguration waveformServiceConfig = HttpClientConfigurationLoader
      .load("waveforms_",
          getUrlToResourceFile("gms/core/signalenhancement/beam/osd/client/osdClient.properties"));

  /**
   * Path suffix for Beam Control Service alive endpoint {@link HttpServiceConfiguration} provides
   * the prefix.
   */
  private static final String BEAM_ALIVE_PATH = "/alive";

  /**
   * Path suffix for streaming invocation of {@link BeamControl}. {@link
   * HttpServiceConfiguration} provides the prefix.
   */
  private static final String BEAM_STREAMING_PATH = "/streaming";

  /**
   * Path suffix for claim check invocation of {@link BeamControl}. {@link
   * HttpServiceConfiguration} provides the prefix.
   */
  private static final String BEAM_CLAIM_CHECK_PATH = "/claim-check";

  public static void main(String[] args) {
    Runtime.getRuntime().addShutdownHook(new Thread(Application::stop));
    Application.start();
  }

  private Application() {
  }

  /**
   * Starts the Spark HTTP server and registers routes
   */
  private static void start() {
    logger.info("Initializing beam-service ...");
    final String propertiesFile = "gms/core/signalenhancement/beam/service/application.properties";
    HttpServiceConfiguration config = HttpServiceConfigurationLoader
        .load(getUrlToResourceFile(propertiesFile));

    logger.info("Initializing embedded server...");
    configureServer(config);
    logger.info("Embedded server initialized.");

    logger.info("Initializing beam-service routes...");
    configureRoutes(config);
    logger.info("beam-service routes initialized.");

    Spark.awaitInitialization();
    logger.info("Service initialized, ready to accept requests");
  }

  /**
   * Stops the Spark HTTP server
   */
  private static void stop() {
    Spark.stop();
  }

  /**
   * Configures the Spark HTTP server (ports, thread pool, exception handler)
   */
  private static void configureServer(HttpServiceConfiguration config) {
    Spark.port(config.getPort());
    Spark.threadPool(config.getMaxThreads(), config.getMinThreads(), config.getIdleTimeOutMillis());

    // Exception handlers.  Replicate calls to exception to avoid unchecked assignment warnings
    Spark.exception(Exception.class, ErrorHandler::ExceptionHandler);
  }

  /**
   * Configures the beam-service HTTP routes
   */
  private static void configureRoutes(HttpServiceConfiguration config) {
    // Delegate calls to this route handler
    BeamRouteHandler beamRouteHandler = getBeamCommandExecutionerRouteHandler();

    // Configure the alive route handler
    Function<Request, StandardResponse> beamAliveClosure = r -> BeamCommonRouteHandler.alive();
    Spark.get(config.getBaseUrl() + BEAM_ALIVE_PATH, route(beamAliveClosure));

    // Configure the streaming route handler
    Function<Request, StandardResponse> beamSpectrumStreamingClosure = r -> beamRouteHandler
        .streaming(parseContentType(r), r.bodyAsBytes(), parseAcceptType(r));
    Spark.post(config.getBaseUrl() + BEAM_STREAMING_PATH, route(beamSpectrumStreamingClosure));

    // Configure the claim check route handler
    Function<Request, StandardResponse> beamSpectrumClaimCheckClosure = r -> beamRouteHandler
        .claimCheck(parseContentType(r), r.bodyAsBytes(), parseAcceptType(r));
    Spark.post(config.getBaseUrl() + BEAM_CLAIM_CHECK_PATH, route(beamSpectrumClaimCheckClosure));
  }

  /**
   * Obtain a {@link Route} from the routeOp function mapping a {@link Request} to a {@link
   * StandardResponse}.
   *
   * @param routeOp function mapping a {@link Request} to a {@link StandardResponse}, not null
   * @return Route, not null
   */
  private static Route route(Function<Request, StandardResponse> routeOp) {

    // Fills in a Response from a StandardResponse
    final BiFunction<Response, StandardResponse, Object> responseHandler = (res, stdRes) -> {
      res.status(stdRes.getHttpStatus());
      res.type(stdRes.getContentType().toString());
      return stdRes.getResponseBody();
    };

    // Create a Route from a function mapping a Request to a StandardResponse
    final Function<Function<Request, StandardResponse>, Route> routeClosure =
        f -> (req, res) -> responseHandler.apply(res, f.apply(req));

    return routeClosure.apply(routeOp);
  }

  /**
   * Obtains the content type of the {@link Request}'s body
   *
   * @param request Request, not null
   * @return {@link ContentType}, not null
   */
  private static ContentType parseContentType(Request request) {
    return ContentType.parse(request.headers("Content-Type"));
  }

  /**
   * Obtains the clients expected body type
   *
   * @param request Request, not null
   * @return {@link ContentType}, not null
   */
  private static ContentType parseAcceptType(Request request) {
    return ContentType.parse(request.headers("Accept"));
  }

  /**
   * Obtains a fully constructed instance of {@link BeamRouteHandler}
   *
   * @return BeamRouteHandler, not null
   */
  private static BeamRouteHandler getBeamCommandExecutionerRouteHandler() {
    BeamControl beamControl = BeamControl
        .create(getBeamPluginRegistry(), getOsdClient(), getBeamConfiguration());

    beamControl.initialize();

    return BeamRouteHandler
        .create(beamControl);
  }

  /**
   * Obtains a {@link PluginRegistry} populated with available {@link BeamPlugin}s
   *
   * @return PluginRegistry, not null
   */
  private static PluginRegistry<BeamPlugin> getBeamPluginRegistry() {
    PluginRegistry<BeamPlugin> registry = new PluginRegistry<>();

    // TODO: use module system for plugin discovery?
    logger.info("Registering {} plugins",
        ServiceLoader.load(BeamPlugin.class).stream().count());
    ServiceLoader.load(BeamPlugin.class).stream()
        .map(Provider::get)
        .forEach(registry::register);
    return registry;
  }

  /**
   * Obtains a fully constructed instance of {@link OsdClient}
   *
   * @return OsdClient, not null
   */
  private static OsdClient getOsdClient() {
    // Construct the OsdClient with OSD Gatway service access configuration
    return OsdClient.create(procGroupServiceConfig, waveformServiceConfig);
  }

  /**
   * Obtains an instance of {@link BeamConfiguration}
   *
   * @return {@link BeamConfiguration}, not null
   */
  private static BeamConfiguration getBeamConfiguration() {
    return BeamConfiguration.create(getConfigurationRepository());
  }

  /**
   * Obtains a {@link ConfigurationRepository} providing beam configuration values
   *
   * @return {@link ConfigurationRepository}, not null
   */
  private static ConfigurationRepository getConfigurationRepository() {
    final String configurationBase = getUrlToResourceFile(
        "gms/core/signalenhancement/beam/configuration-base/").getPath();

    logger.info("Configuration base directory: {} ", configurationBase);

    return FileConfigurationRepository.create(new File(configurationBase).toPath());
  }

  /**
   * Obtains a {@link URL} to the file at the provided path within this Jar's resources directory
   *
   * @param path String file path relative to this Jar's root, not null
   * @return URL to the resources file at the provided path
   */
  private static URL getUrlToResourceFile(String path) {
    final URL fileUrl = Thread.currentThread().getContextClassLoader().getResource(path);
    if (null == fileUrl) {
      final String message = "beam application can't find file in resources: " + path;
      logger.error(message);
      throw new MissingResourceException(message, Application.class.getName(), path);
    }
    return fileUrl;
  }
}
