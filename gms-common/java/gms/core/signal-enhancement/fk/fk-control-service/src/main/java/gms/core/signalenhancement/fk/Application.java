package gms.core.signalenhancement.fk;

import gms.core.signalenhancement.fk.coi.client.CoiRepository;
import gms.core.signalenhancement.fk.coi.client.config.CoiClientConfiguration;
import gms.core.signalenhancement.fk.coi.client.config.CoiClientConfigurationLoader;
import gms.core.signalenhancement.fk.control.FkControl;
import gms.core.signalenhancement.fk.control.configuration.FkConfiguration;
import gms.core.signalenhancement.fk.plugin.fkattributes.FkAttributesPlugin;
import gms.core.signalenhancement.fk.plugin.fkspectra.FkSpectraPlugin;
import gms.core.signalenhancement.fk.service.ContentType;
import gms.core.signalenhancement.fk.service.ErrorHandler;
import gms.core.signalenhancement.fk.service.FkCommonRouteHandler;
import gms.core.signalenhancement.fk.service.StandardResponse;
import gms.core.signalenhancement.fk.service.configuration.HttpServiceConfiguration;
import gms.core.signalenhancement.fk.service.configuration.HttpServiceConfigurationLoader;
import gms.core.signalenhancement.fk.service.fkspectra.FkRouteHandler;
import gms.core.signalenhancement.fk.util.UrlUtility;
import gms.shared.mechanisms.configuration.FileConfigurationRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginRegistry;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.repository.CoiEntityManagerFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.SignalDetectionRepositoryJpa;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra.JpaCassandraWaveformRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.FkSpectraRepositoryJpa;
import java.io.File;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import javax.persistence.EntityManagerFactory;

public class Application {

  private static Logger logger = LoggerFactory.getLogger(Application.class);

  private static final String HIBERNATE_CONNECTION_URL = "hibernate.connection.url";

  /**
   * Path suffix for FK Control Service alive endpoint
   * {@link HttpServiceConfiguration} provides the prefix.
   */
  private static final String FK_ALIVE_PATH = "/alive";

  /**
   * Path suffix for spectra invocation of {@link FkControl}.
   * {@link HttpServiceConfiguration} provides the prefix.
   */
  private static final String INTERACTIVE_SPECTRA_PATH = "/spectra/interactive";

  /**
   * Path suffix for claim check invocation of {@link FkControl}.
   * {@link HttpServiceConfiguration} provides the prefix.
   */
  private static final String FEATURE_MEASUREMENTS_PATH = "/feature-measurements";

  public static void main(String[] args) {
    Runtime.getRuntime().addShutdownHook(new Thread(Application::stop));
    Application.start();
  }

  private Application() {
  }

  /**
   * Starts the Spark HTTP server and registers routes
   */
  public static void start() {
    logger.info("Initializing fkspectra-control-service ...");
    final String propertiesFile = "gms/core/signalenhancement/fk/service/application.properties";
    HttpServiceConfiguration config = HttpServiceConfigurationLoader
        .load(UrlUtility.getUrlToResourceFile(propertiesFile));

    logger.info("Initializing embedded server...");
    configureServer(config);
    logger.info("Embedded server initialized.");

    logger.info("Initializing fkspectra-control-service routes...");
    configureRoutes(config);
    logger.info("fkspectra-control-service routes initialized.");

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
    Spark.exception(Exception.class, exception(ErrorHandler::handle500));
    Spark.exception(IllegalArgumentException.class, exception(ErrorHandler::handle500));
    Spark.exception(NullPointerException.class, exception(ErrorHandler::handle500));
    Spark.exception(UncheckedIOException.class, exception(ErrorHandler::handle400));

    // Error handlers
    Spark.notFound(route(r -> ErrorHandler.handle404(r.url())));
    Spark.internalServerError(route(r -> ErrorHandler.handle500("")));
  }

  /**
   * Obtains an {@link ExceptionHandler} using exceptionToString to extract a String from an {@link
   * Exception} and the exceptionHandler to create a {@link StandardResponse} from the String.
   *
   * @param exceptionHandler function creating a StandardResponse from a String, not null
   * @return ExceptionHandler, not null
   */
  private static <U extends Exception> ExceptionHandler<U> exception(
      Function<String, StandardResponse> exceptionHandler) {

    // Fills in a Response from a StandardResponse
    final BiConsumer<StandardResponse, Response> fillResponseFromStdRes = (stdRes, res) -> {
      res.status(stdRes.getHttpStatus());
      res.type(stdRes.getContentType().toString());
      res.body(stdRes.getResponseBody().toString());
    };

    // Creates a StandardResponse from an Exception
    final Function<Exception, StandardResponse> exceptionToStdRes = e ->
        exceptionHandler.apply(Application.messageOrEmpty(e));

    // Create the ExceptionHandler
    return (e, req, res) -> fillResponseFromStdRes.accept(exceptionToStdRes.apply(e), res);
  }

  /**
   * Obtains the {@link Exception#getMessage()} for exception or the empty string if it does not
   * have a message
   *
   * @return String, possibly empty, not null
   */
  private static String messageOrEmpty(Exception exception) {
    logger.error("Service error:", exception);
    return Optional.ofNullable(exception.getMessage()).orElse("");
  }

  /**
   * Configures the fkspectra-control-service HTTP routes
   */
  private static void configureRoutes(HttpServiceConfiguration config) {
    // Delegate calls to this route handler
    FkRouteHandler fkRouteHandler = getFkSpectrumCommandExecutionerRouteHandler();

    // Configure the alive route handler
    Function<Request, StandardResponse> fkAliveClosure = r -> FkCommonRouteHandler.alive();
    Spark.get(config.getBaseUrl() + FK_ALIVE_PATH, route(fkAliveClosure));

    // Configure the spectra route handler
    Function<Request, StandardResponse> interactiveSpectraClosure = r -> fkRouteHandler
        .interactiveSpectra(parseContentType(r), r.bodyAsBytes(), parseAcceptType(r));
    Spark.post(config.getBaseUrl() + INTERACTIVE_SPECTRA_PATH, route(interactiveSpectraClosure));

    // Configure the claim check route handler
    Function<Request, StandardResponse> featureMeasurementsClosure = r -> fkRouteHandler
        .featureMeasurements(parseContentType(r), r.bodyAsBytes(), parseAcceptType(r));
    Spark.post(config.getBaseUrl() + FEATURE_MEASUREMENTS_PATH, route(featureMeasurementsClosure));
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

  private static FkConfiguration getFkConfiguration() {
    final String configurationBase = getUrlToResourceFile(
        "gms/core/signalenhancement/fkcontrol/configuration-base")
        .getPath();

    return FkConfiguration
        .create(FileConfigurationRepository.create(new File(configurationBase).toPath()));
  }

  /**
   * Obtains a fully constructed instance of {@link FkRouteHandler}
   *
   * @return FkRouteHandler, not null
   */
  private static FkRouteHandler getFkSpectrumCommandExecutionerRouteHandler() {
    FkControl fkControl = FkControl
        .create(getFkSpectraPluginRegistry(),
            getFkAttributesPluginRegistry(),
            coiRepository(),
            getFkConfiguration());

    fkControl.initialize();

    return FkRouteHandler
        .create(fkControl);
  }

  private static URL getUrlToResourceFile(String path) {
    final URL propFileUrl = Thread.currentThread().getContextClassLoader().getResource(path);
    if (null == propFileUrl) {
      final String message =
          "signal-detector-control application can't find file in resources: " + path;
      logger.error(message);
      throw new MissingResourceException(message, Application.class.getName(), path);
    }
    return propFileUrl;
  }

  /**
   * Obtains a {@link PluginRegistry} populated with available {@link
   * FkSpectraPlugin}s
   *
   * @return FkSpectraPluginRegistry, not null
   */
  private static PluginRegistry<FkSpectraPlugin> getFkSpectraPluginRegistry() {
    PluginRegistry<FkSpectraPlugin> registry = new PluginRegistry<>();

    logger.info("Registering {} plugins",
        ServiceLoader.load(FkSpectraPlugin.class).stream().count());
    ServiceLoader.load(FkSpectraPlugin.class).stream()
        .map(Provider::get)
        .forEach(registry::register);
    return registry;
  }

  /**
   * Obtains a {@link PluginRegistry} populated with available {@link
   * FkAttributesPlugin}s
   *
   * @return FkAttributesPluginRegistry, not null
   */
  private static PluginRegistry<FkAttributesPlugin> getFkAttributesPluginRegistry() {
    PluginRegistry<FkAttributesPlugin> registry = new PluginRegistry<>();

    logger.info("Registering {} plugins",
        ServiceLoader.load(FkAttributesPlugin.class).stream().count());
    ServiceLoader.load(FkAttributesPlugin.class).stream()
        .map(Provider::get)
        .forEach(registry::register);
    return registry;
  }

  /**
   * Obtains a fully constructed instance of {@link CoiRepository}
   *
   * @return CoiRepository, not null
   */
  private static CoiRepository coiRepository() {
    CoiClientConfiguration config = CoiClientConfigurationLoader.load();
    EntityManagerFactory entityManagerFactory = CoiEntityManagerFactory.create(Map.of(HIBERNATE_CONNECTION_URL, config.getPersistenceUrl()));
    Runtime.getRuntime().addShutdownHook(new Thread(entityManagerFactory::close));
    // Construct the CoiRepository with OSD Gatway service access configuration
    return CoiRepository.from(
        SignalDetectionRepositoryJpa.create(entityManagerFactory),
        new JpaCassandraWaveformRepository(entityManagerFactory),
        new FkSpectraRepositoryJpa(entityManagerFactory));
  }
}
