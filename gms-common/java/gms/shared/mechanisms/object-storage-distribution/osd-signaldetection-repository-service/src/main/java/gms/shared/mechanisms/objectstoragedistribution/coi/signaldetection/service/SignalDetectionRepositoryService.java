package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.EventRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ChannelProcessingGroupRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.QcMaskRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.factory.ProcessingStationReferenceFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.configuration.Configuration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers.ChannelProcessingGroupRouteHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers.EventRouteHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers.ExceptionHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers.HttpErrorHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers.QcMaskRouteHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers.SignalDetectionRouteHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers.StationProcessingRouteHandlers;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.FkSpectraRepository;
import java.util.Objects;
import spark.Spark;

/**
 * Service implementation of the signaldetection-repository. <p> Provides a minimal wrapper around a
 * singleton {@link Spark} service.
 */
public class SignalDetectionRepositoryService {

  private SignalDetectionRepositoryService() {
  }

  /**
   * Start the service using the provided {@link Configuration} to determine service properties.
   * Routes repository calls to the provided {@link QcMaskRepository}.
   *
   * @param configuration Configuration for this service, not null
   * @param qcMaskRepository route repository calls to this QcMaskRepository
   */
  public static void startService(Configuration configuration,
      QcMaskRepository qcMaskRepository,
      SignalDetectionRepository signalDetectionRepository,
      FkSpectraRepository fkRepository,
      ChannelProcessingGroupRepository channelProcessingGroupRepository,
      EventRepository eventRepository,
      ProcessingStationReferenceFactory processingStationReferenceFactory) {
    Objects.requireNonNull(configuration,
        "Cannot create SignalDetectionRepositoryService with null configuration");
    Objects.requireNonNull(qcMaskRepository,
        "Cannot create SignalDetectionRepositoryService with null qcMaskRepository");
    Objects.requireNonNull(signalDetectionRepository,
        "Cannot create SignalDetectionRepositoryService with null signalDetectionRepository");
    Objects.requireNonNull(fkRepository,
        "Cannot create FkSpectraRepository with null fkRepository");
    Objects.requireNonNull(channelProcessingGroupRepository,
        "Cannot create SignalDetectionRepositoryService with null channelProcessingGroupRepository");
    Objects.requireNonNull(eventRepository,
        "Cannot create EventRepositoryService with null eventRepository");
    Objects.requireNonNull(processingStationReferenceFactory,
        "Cannot create SignalDetectionRepositoryService with null processingStationReferenceFactory");

    configureHttpServer(configuration);
    configureRoutesAndFilters(configuration, qcMaskRepository, signalDetectionRepository,
        fkRepository, channelProcessingGroupRepository,
        eventRepository, processingStationReferenceFactory);
    Spark.awaitInitialization();
  }

  /**
   * Configures the HTTP server before the it is started. NOTE: This method must be called before
   * routes and filters are declared, because Spark Java automatically starts the HTTP server when
   * routes and filters are declared.
   */
  private static void configureHttpServer(Configuration configuration) {
    // Set the listening port.
    Spark.port(configuration.getPort());

    // Set the min/max number of threads, and the idle timeout.
    Spark.threadPool(configuration.getMaxThreads(),
        configuration.getMinThreads(),
        configuration.getIdleTimeOutMillis());
  }

  /**
   * Registers all routes and filters. Sets exceptions for endpoints to specific pre implemented
   * responses.
   */
  private static void configureRoutesAndFilters(Configuration configuration,
      QcMaskRepository qcMaskRepository,
      SignalDetectionRepository signalDetectionRepository,
      FkSpectraRepository fkRepository,
      ChannelProcessingGroupRepository channelProcessingGroupRepository,
      EventRepository eventRepository,
      ProcessingStationReferenceFactory processingStationReferenceFactory) {

    // requests normally return JSON
    Spark.before((request, response) -> response.type("application/json"));

    // Exception handlers.
    Spark.exception(Exception.class, ExceptionHandlers::ExceptionHandler);

    // HTTP custom error handlers.
    Spark.notFound(HttpErrorHandlers::Http404);
    Spark.internalServerError(HttpErrorHandlers::Http500);

    // QC mask routes

    Spark.get(configuration.getBaseUrl() + "qc-mask",
        ((request, response) -> QcMaskRouteHandlers
            .findCurrentByChannelIdAndTimeRange(request, response, qcMaskRepository)));

    Spark.get("coi/qc-mask",
        ((request, response) -> QcMaskRouteHandlers
            .findCurrentByChannelIdAndTimeRange(request, response, qcMaskRepository)));

    Spark.post(configuration.getBaseUrl() + "qc-mask",
        ((request, response) -> QcMaskRouteHandlers
            .retrieveQcMasksByChannelIdsAndTimeRange(request, response, qcMaskRepository)));

    Spark.post("coi/qc-mask",
        ((request, response) -> QcMaskRouteHandlers
            .retrieveQcMasksByChannelIdsAndTimeRange(request, response, qcMaskRepository)));

    Spark.post("coi/qc-masks/query/channels-and-time",
        ((request, response) -> QcMaskRouteHandlers
            .retrieveQcMasksByChannelIdsAndTimeRange(request,
                response,
                qcMaskRepository)));

    Spark.post("coi/qc-masks",
        ((request, response) -> QcMaskRouteHandlers
            .storeQcMasks(request, response, qcMaskRepository)));

    // signal detections
    SignalDetectionRouteHandlers signalDetectionRoutes = SignalDetectionRouteHandlers
        .create(signalDetectionRepository, fkRepository);

    Spark.get(configuration.getBaseUrl() + "signal-detections/:id",
        signalDetectionRoutes::getSignalDetection);

    Spark.post("/coi/signal-detections/query/ids",
        signalDetectionRoutes::getSignalDetectionsByIds);

    Spark.post("/coi/signal-detections/hypotheses/query/ids",
        signalDetectionRoutes::getSignalDetectionsHypothesesByIds);

    Spark.get(configuration.getBaseUrl() + "signal-detections",
        signalDetectionRoutes::getSignalDetectionsByTimeRange);

    Spark.post("/coi/signal-detections/query/stations-and-time",
        signalDetectionRoutes::getSignalDetectionsByStationAndTimeRange);

    Spark.post("/coi/signal-detections",
        signalDetectionRoutes::storeSignalDetections);

    Spark.post("/coi/channel-segments/fks/query/channel-ids",
        signalDetectionRoutes::retrieveFkChannelSegments);

    Spark.post("/coi/channel-segments/fks/query/segment-ids",
        signalDetectionRoutes::retrieveFkChannelSegmentsBySegmentIds);

    Spark.post("/coi/channel-segments/fks",
        signalDetectionRoutes::storeFkSpectra);

    Spark.post(configuration.getBaseUrl() + "beam-creation-info/store",
        signalDetectionRoutes::storeBeamCreationInfoFromArray);

    //processing objects
    StationProcessingRouteHandlers processingRoutes = StationProcessingRouteHandlers
        .create(processingStationReferenceFactory);

    Spark.get(configuration.getBaseUrl() + "network", processingRoutes::getNetwork);

    //channel processing groups
    ChannelProcessingGroupRouteHandlers channelProcessingGroupRoutes = ChannelProcessingGroupRouteHandlers
        .create(channelProcessingGroupRepository);

    Spark.get(configuration.getBaseUrl() + "channel-processing-group/:id",
        channelProcessingGroupRoutes::getChannelProcessingGroup);

    Spark.post(configuration.getBaseUrl() + "channel-processing-group",
        channelProcessingGroupRoutes::storeChannelProcessingGroups);

    Spark.get(configuration.getBaseUrl() + "alive", signalDetectionRoutes::alive);

    //events
    EventRouteHandlers eventRoutes = EventRouteHandlers
        .create(eventRepository);

    Spark.post("/coi/events/query/ids", eventRoutes::retrieveEventsByIds);

    Spark.post("/coi/events/query/time-lat-lon", eventRoutes::retrieveEventsByTimeAndLocation);

    Spark.post("/coi/events", eventRoutes::storeEvents);
  }

  /**
   * Stops the REST service.
   */
  public static void stopService() {
    Spark.stop();
  }
}
