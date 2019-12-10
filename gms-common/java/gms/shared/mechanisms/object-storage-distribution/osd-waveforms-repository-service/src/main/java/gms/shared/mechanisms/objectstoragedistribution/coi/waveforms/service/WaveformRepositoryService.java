package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.BeamRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.FkSpectraRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.RawStationDataFrameRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.StationSohRepositoryInterface;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers.SparkChannelSohCoiRouteHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers.ExceptionHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers.HttpErrorHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers.SparkCommonRouteHandler;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers.SparkRawDataFrameCoiRouteHandler;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.handlers.SparkWaveformCoiRouteHandler;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.service.responsetransformers.JsonTransformer;
import java.util.Objects;
import spark.Spark;

/**
 * Service implementation of the waveform-repository.
 *
 * Provides a minimal wrapper around a singleton {@link Spark} service.
 */
public class WaveformRepositoryService {

  private WaveformRepositoryService() {
  }

  /**
   * Starts the service with the provided {@link Configuration} to determine service properties.
   * Routes repository calls to the provided {@link WaveformRepository}, {@link
   * StationSohRepositoryInterface}.
   *
   * @param configuration the configuration of the service
   * @param waveformRepository the Waveform Repository Interface
   * @param stationSohRepository the Station Soh Repository Interface
   */
  public static void startService(Configuration configuration,
      WaveformRepository waveformRepository,
      StationSohRepositoryInterface stationSohRepository,
      RawStationDataFrameRepositoryInterface frameRepository,
      BeamRepositoryInterface beamRepository,
      FkSpectraRepository fkRepository) {
    Objects.requireNonNull(configuration,
        "Cannot create WaveformRepositoryService with null configuration");
    Objects.requireNonNull(waveformRepository,
        "Cannot create WaveformRepositoryService with null waveformRepository");
    Objects.requireNonNull(stationSohRepository,
        "Cannot create WaveformRepositoryService with null stationSohRepository");
    Objects.requireNonNull(frameRepository,
        "Cannot create WaveformRepositoryService with null frameRepository");
    Objects.requireNonNull(beamRepository,
        "Cannot create WaveformRepositoryService with null beamRepository");
    Objects.requireNonNull(fkRepository,
        "Cannot create WaveformRepositoryService with null fkRepository");

    configureHttpServer(configuration);
    configureRoutesAndFilters(configuration, waveformRepository, stationSohRepository,
        frameRepository, beamRepository, fkRepository);
    Spark.awaitInitialization();

  }

  /**
   * Stops the REST service.
   */
  public static void stopService() {
    Spark.stop();
  }

  /**
   * Configures the HTTP server before the it is started. NOTE: This method must be called before
   * routes and filters are declared, because Spark Java automatically starts the HTTP server when
   * routes and filters are declared.
   *
   * @param configuration the configuration to be used
   */
  private static void configureHttpServer(Configuration configuration) {
    // Set the listening port.
    Spark.port(configuration.port);

    // Set the min/max number of threads, and the idle timeout.
    Spark.threadPool(configuration.maxThreads,
        configuration.minThreads,
        configuration.idleTimeOutMillis);
  }

  /**
   * Registers all routes and filters. Sets exceptions for endpoints to specific pre implemented
   * responses.
   *
   * @param configuration the configuration to be used
   * @param waveformRepository the Waveform Repository Interface
   * @param stationSohRepository the Station Soh Repository Interface
   * @param frameRepository the frame repository interface
   */
  private static void configureRoutesAndFilters(Configuration configuration,
      WaveformRepository waveformRepository,
      StationSohRepositoryInterface stationSohRepository,
      RawStationDataFrameRepositoryInterface frameRepository,
      BeamRepositoryInterface beamRepository,
      FkSpectraRepository fkRepository) {

    // exception handler.
    Spark.exception(Exception.class, ExceptionHandlers::ExceptionHandler);

    /////////////////////////////////////////////////////////////////////////////////////////
    // HTTP custom error handlers.
    Spark.notFound(HttpErrorHandlers::Http404);
    Spark.internalServerError(HttpErrorHandlers::Http500);
    /////////////////////////////////////////////////////////////////////////////////////////
    // Filters
    //     requests normally return JSON
    Spark.before((request, response) -> response.type("application/json"));
    /////////////////////////////////////////////////////////////////////////////////////////
    // Routes
    Spark.post("/mechanisms/object-storage-distribution/waveforms/channel-segment/store",
        (request, response) -> SparkWaveformCoiRouteHandler
            .storeChannelSegments(request, response, waveformRepository));

    Spark.post("/coi/channel-segment/store",
        (request, response) -> SparkWaveformCoiRouteHandler
            .storeChannelSegments(request, response, waveformRepository));

    Spark.post("/mechanisms/object-storage-distribution/waveforms/beam-result/store",
        (request, response) -> SparkWaveformCoiRouteHandler.storeBeamResult(
            request, response, waveformRepository, beamRepository));

    Spark.get("/mechanisms/object-storage-distribution/waveforms/channel-segment",
        ((request, response) -> SparkWaveformCoiRouteHandler
            .retrieveChannelSegment(request, response, waveformRepository)));

    Spark.get("/coi/channel-segment",
        ((request, response) -> SparkWaveformCoiRouteHandler
            .retrieveChannelSegment(request, response, waveformRepository)));

    Spark.post("/mechanisms/object-storage-distribution/waveforms/channel-segment",
        (request, response) -> SparkWaveformCoiRouteHandler
            .retrieveChannelSegmentsByChannelIdsOrChannelSegmentIds(request, response,
                waveformRepository));

    Spark.post("/coi/channel-segment",
        (request, response) -> SparkWaveformCoiRouteHandler
            .retrieveChannelSegmentsByChannelIdsOrChannelSegmentIds(request, response,
                waveformRepository));

    Spark.post("/mechanisms/object-storage-distribution/waveforms/channel-segment/fk",
        (request, response) -> SparkWaveformCoiRouteHandler
            .retrieveFkChannelSegmentsByChannelIds(request, response, fkRepository));

    Spark.post("/coi/channel-segment/fk",
        (request, response) -> SparkWaveformCoiRouteHandler
            .retrieveFkChannelSegmentsByChannelIds(request, response, fkRepository));

    Spark.get("/mechanisms/object-storage-distribution/waveforms/acquired-channel-soh/analog/:id",
        ((request, response) -> SparkChannelSohCoiRouteHandlers
            .getAcquiredChannelSohAnalog(request, response, stationSohRepository)));

    Spark.get("/coi/acquired-channel-soh/analog/:id",
        ((request, response) -> SparkChannelSohCoiRouteHandlers
            .getAcquiredChannelSohAnalog(request, response, stationSohRepository)));

    Spark.get("/mechanisms/object-storage-distribution/waveforms/acquired-channel-soh/analog",
        ((request, response) -> SparkChannelSohCoiRouteHandlers
            .getAcquiredChannelSohAnalogTimeRange(request, response, stationSohRepository)));

    Spark.get("/coi/acquired-channel-soh/analog",
        ((request, response) -> SparkChannelSohCoiRouteHandlers
            .getAcquiredChannelSohAnalogTimeRange(request, response, stationSohRepository)));

    Spark.post("/coi/acquired-channel-sohs/analog",
        (request, response) -> SparkChannelSohCoiRouteHandlers
            .storeAcquiredChannelSohsAnalog(request, response, stationSohRepository),
        new JsonTransformer());

    Spark.post("/coi/acquired-channel-sohs/analog/store",
        (request, response) -> SparkChannelSohCoiRouteHandlers
            .storeAcquiredChannelSohsAnalog(request, response, stationSohRepository),
        new JsonTransformer());

    Spark.get("/mechanisms/object-storage-distribution/waveforms/acquired-channel-soh/boolean/:id",
        ((request, response) -> SparkChannelSohCoiRouteHandlers
            .getAcquiredChannelSohBoolean(request, response, stationSohRepository)));

    Spark.get("/coi/acquired-channel-soh/boolean/:id",
        ((request, response) -> SparkChannelSohCoiRouteHandlers
            .getAcquiredChannelSohBoolean(request, response, stationSohRepository)));

    Spark.get("/mechanisms/object-storage-distribution/waveforms/acquired-channel-soh/boolean",
        ((request, response) -> SparkChannelSohCoiRouteHandlers
            .getAcquiredChannelSohBooleanTimeRange(request, response, stationSohRepository)));

    Spark.get("/coi/acquired-channel-soh/boolean",
        ((request, response) -> SparkChannelSohCoiRouteHandlers
            .getAcquiredChannelSohBooleanTimeRange(request, response, stationSohRepository)));

    Spark.post("/coi/acquired-channel-sohs/boolean",
        (request, response) -> SparkChannelSohCoiRouteHandlers
            .storeAcquiredChannelSohsBoolean(request, response, stationSohRepository),
        new JsonTransformer());

    Spark.post("/mechanisms/object-storage-distribution/waveforms/channel-availability",
        ((request, response) -> SparkWaveformCoiRouteHandler
            .batchCalculateChannelAvailability(request, response, waveformRepository)));

    Spark.get("/mechanisms/object-storage-distribution/waveforms/frames",
        (request, response) -> SparkRawDataFrameCoiRouteHandler
            .getRawStationDataFrames(request, response, frameRepository));

    Spark.post("/coi/raw-station-data-frames",
        (request, response) -> SparkRawDataFrameCoiRouteHandler
            .storeRawStationDataFrames(request, response, frameRepository),
        new JsonTransformer());

    Spark.get("/mechanisms/object-storage-distribution/waveforms/alive",
        (SparkCommonRouteHandler::alive));
  }

  @FunctionalInterface
  private interface TriConsumer<A, B, C> {

    void accept(A a, B b, C c) throws Exception;
  }

  // used to ease creation of route handlers that have no return value under normal operation.
  private static spark.Route voidRoute(
      TriConsumer<spark.Request, spark.Response, WaveformRepository> consumer,
      WaveformRepository waveformRepository) {
    return (req, res) -> {
      consumer.accept(req, res, waveformRepository);
      return "";
    };
  }
}
