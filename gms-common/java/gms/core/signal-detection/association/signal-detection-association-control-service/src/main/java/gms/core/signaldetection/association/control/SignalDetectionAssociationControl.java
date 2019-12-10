package gms.core.signaldetection.association.control;

import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.signaldetection.association.control.gacache.GACache;
import gms.core.signaldetection.association.control.service.ArrivalQualityCriteriaConfigurationParameters;
import gms.core.signaldetection.association.control.service.SignalDetectionAssociationControlConfiguration;
import gms.core.signaldetection.association.control.service.SignalDetectionAssociatorConfigurationParameters;
import gms.core.signaldetection.association.control.service.WeightedEventCriteriaConfigurationParameters;
import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterionDefinition;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationDefinition;
import gms.core.signaldetection.association.plugins.SdhStationAssociation;
import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorDefinition;
import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalDetectionAssociationControl {

  private static final Logger logger = LoggerFactory
      .getLogger(SignalDetectionAssociationControl.class);

  private final SignalDetectionAssociationControlOsdGateway osdGateway;
  private final SignalDetectionAssociationControlConfiguration config;
  private final PluginRegistry pluginRegistry;
  private SignalDetectionAssociationParameters parameters;
  private SignalDetectionAssociatorPlugin plugin;

  private GACache gaCache;

  private SignalDetectionAssociationControl(
      SignalDetectionAssociationControlOsdGateway osdGateway,
      SignalDetectionAssociationControlConfiguration configuration,
      PluginRegistry pluginRegistry,
      SignalDetectionAssociatorPlugin plugin,
      SignalDetectionAssociationParameters parameters,
      GACache gaCache) {
    this.osdGateway = osdGateway;
    this.config = configuration;
    this.pluginRegistry = pluginRegistry;
    this.plugin = plugin;
    this.parameters = parameters;
    this.gaCache = gaCache;
  }

  public static SignalDetectionAssociationControl create(
      PluginRegistry pluginRegistry,
      SignalDetectionAssociationControlOsdGateway osdGateway,
      SignalDetectionAssociationControlConfiguration configuration,
      GACache gaCache) {

    pluginRegistry.loadAndRegister();
    SignalDetectionAssociationParameters parameters = SignalDetectionAssociationControl
        .loadDefaultSignalDetectionAssociationParameters(configuration);

    PluginInfo defaultPluginInfo = parameters.getPluginInfo().orElseThrow(
        () -> new IllegalStateException(String
            .format("Default %s does not contain %s",
                SignalDetectionAssociatorDefinition.class.getSimpleName(),
                PluginInfo.class.getSimpleName())));

    SignalDetectionAssociatorPlugin plugin = pluginRegistry
        .lookup(defaultPluginInfo, SignalDetectionAssociatorPlugin.class).orElseThrow(
            () -> {
              String errMsg = String
                  .format("Could not load plugin class \"%s\" with name \"%s\" and version \"%s\".",
                      SignalDetectionAssociatorPlugin.class, defaultPluginInfo.getName(),
                      defaultPluginInfo.getVersion());

              SignalDetectionAssociationControl.logger.error(errMsg);

              return new IllegalStateException(errMsg);
            }
        );


    Optional<Set<ReferenceStation>> stations = osdGateway.retrieveStations();
    List<SignalFeaturePredictionUtility> predictionUtilities =
        List.of(
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility(),
            new SignalFeaturePredictionUtility()
        );

    if(stations.isPresent()) {
      plugin.initialize(configuration.getParams().getGridModelFileName(), stations.get(),
          predictionUtilities, parameters.getSignalDetectionAssociatorDefinition().get());
    }
    
    return new SignalDetectionAssociationControl(osdGateway, configuration, pluginRegistry, plugin, parameters, gaCache);
  }

  private static SignalDetectionAssociationParameters loadDefaultSignalDetectionAssociationParameters(
      SignalDetectionAssociationControlConfiguration configuration) {
    PluginInfo pluginInfo = PluginInfo.from(configuration.getParams().getPluginName(),
        configuration.getParams().getPluginVersion());
    SignalDetectionAssociatorConfigurationParameters params = configuration.getParams();
    WeightedEventCriteriaConfigurationParameters weightedEventPluginParams = configuration
        .getWeightedEventPluginParams();
    ArrivalQualityCriteriaConfigurationParameters arrivalQualityCriteriaConfigurationParameters = configuration
        .getArrivalQualityPluginParams();
    List<PhaseType> phases = new ArrayList<>();
    List<PhaseType> fwdPhases = new ArrayList<>();

    // TODO: Need to pass in Quality Metric Plugin Information into plugin for initialization
    WeightedEventCriteriaCalculationDefinition weightedEventCriteriaCalculationDefinition =
        WeightedEventCriteriaCalculationDefinition.create(
            weightedEventPluginParams.getPrimaryTimeWeight(),
            weightedEventPluginParams.getSecondaryTimeWeight(),
            weightedEventPluginParams.getArrayAzimuthWeight(),
            weightedEventPluginParams.getThreeComponentAzimuthWeight(),
            weightedEventPluginParams.getArraySlowWeight(),
            weightedEventPluginParams.getThreeComponentSlowWeight(),
            weightedEventPluginParams.getWeightThreshold()
        );

    ArrivalQualityEventCriterionDefinition arrivalQualityEventCriterionDefinition =
        ArrivalQualityEventCriterionDefinition.create(
            arrivalQualityCriteriaConfigurationParameters.getArrivalQualityAlpha(),
            arrivalQualityCriteriaConfigurationParameters.getArrivalQualityBeta(),
            arrivalQualityCriteriaConfigurationParameters.getArrivalQualityGamma(),
            arrivalQualityCriteriaConfigurationParameters.getArrivalQualityThreshold()
        );

    for (String phase : params.getPhases()) {
      phases.add(PhaseType.valueOf(phase));
    }

    for (String phase : params.getForwardTransformationPhases()) {
      fwdPhases.add(PhaseType.valueOf(phase));
    }

    return SignalDetectionAssociationParameters.create(pluginInfo,
        SignalDetectionAssociatorDefinition.create(
            params.getMaxStationsPerGrid(),
            params.getSigmaSlowness(),
            phases,
            fwdPhases,
            params.getBeliefThreshold(),
            params.getPrimaryPhaseRequiredForSecondary(),
            params.getSigmaTime(),
            params.getChiLimit(),
            params.getFreezeArrivalsAtBeamPoints(),
            params.getGridCylinderRadiusDegrees(),
            params.getGridCylinderDepthKm(),
            params.getGridCylinderHeightKm(),
            params.getMinimumMagnitude(),
            weightedEventCriteriaCalculationDefinition,
            arrivalQualityEventCriterionDefinition,
            params.getNumFirstSta()));
  }

  /**
   * Creates a new {@link Event} with a single {@link EventHypothesis} at the provided {@link
   * EventLocation}.  The provided {@link Collection} of {@link SignalDetectionHypothesis} objects
   * are associated to this {@link EventHypothesis} via {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation}
   * objects.
   *
   * @param detectionHypotheses {@link Collection} of {@link SignalDetectionHypothesis} objects to
   * associate to the single {@link EventHypothesis} contained in the {@link Event} created and
   * returned by this method
   * @param eventLocation {@link EventLocation} at which to create the {@link EventHypothesis} to
   * associate the provided {@link Collection} of {@link SignalDetectionHypothesis} objects
   */
  public Event associateToLocationInteractive(
      Collection<SignalDetectionHypothesis> detectionHypotheses,
      EventLocation eventLocation
  ) {

    Set<LocationBehavior> locationBehaviors = new HashSet<>();

    detectionHypotheses.forEach(dh ->
        dh.getFeatureMeasurements().forEach(fm ->
            locationBehaviors.add(
                LocationBehavior.from(
                    0.0,
                    1.0,
                    true,
                    UUID.fromString("00000000-0000-0000-0000-000000000000"),
                    fm.getId()
                )
            )
        )
    );

    LocationSolution locationSolution = LocationSolution.create(
        eventLocation,
        // Not dealing with LocationRestraint for now, setting everything to UNRESTRAINED
        LocationRestraint.from(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null
        ),
        null,  // Optional parameter, not dealing with this yet
        locationBehaviors,
        new HashSet<>()  // Optional parameter, not dealing with this yet
    );

    PreferredLocationSolution preferredLocationSolution = PreferredLocationSolution
        .from(locationSolution);

    return Event.create(
        new HashSet<>(),
        detectionHypotheses.stream().map(SignalDetectionHypothesis::getId)
            .collect(Collectors.toSet()),
        Set.of(locationSolution),
        preferredLocationSolution,
        "MyFakeMonitoringOrganization",
        UUID.randomUUID()
    );
  }

  /**
   * Creates and stores new {@link Event} with a single {@link EventHypothesis} at the provided
   * {@link EventLocation}.  The {@link SignalDetectionHypothesis} objects referenced by the
   * provided {@link Collection} of {@link SignalDetectionHypothesisClaimCheck}s are associated to
   * this {@link EventHypothesis} via {@link gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation}
   * objects.
   *
   * @param signalDetectionHypothesisClaimChecks {@link SignalDetectionHypothesisClaimCheck}s
   * referencing the {@link SignalDetectionHypothesis} objects to associate to the single {@link
   * EventHypothesis} in the {@link Event} created by this method
   * @param eventLocation {@link EventLocation} at which to create the {@link EventHypothesis} that
   * the provided {@link Collection} of {@link SignalDetectionHypothesis} objects will be associated
   * to
   * @return new claim check referencing new event, stored in osd, that associates the given signal
   * detection hypotheses and location
   */
  public EventHypothesisClaimCheck associateToLocation(
      Collection<SignalDetectionHypothesisClaimCheck> signalDetectionHypothesisClaimChecks,
      EventLocation eventLocation
  ) {

    List<SignalDetectionHypothesis> signalDetectionHypotheses;
    try {
      signalDetectionHypotheses = osdGateway.retrieveSignalDetectionHypotheses(
          signalDetectionHypothesisClaimChecks.stream().map(
              SignalDetectionHypothesisClaimCheck::getSignalDetectionId).collect(
              Collectors.toList()));
    } catch (UnirestException | IOException e) {
      this.logger.error("Exception while retrieving signal detection hypotheses", e);
      throw new IllegalStateException(e);
    }

    Event event = this
        .associateToLocationInteractive(signalDetectionHypotheses, eventLocation);

    List<UUID> successfullyStoredEventIds;
    try {
      successfullyStoredEventIds = this.osdGateway.storeOrUpdateEvents(List.of(event));
      if (successfullyStoredEventIds.isEmpty()) {
        throw new IllegalStateException(
            "Control class failed to store new Event with id: " + event.getId());
      }

      this.logger.info("Successfully stored new Event with ID: {}", successfullyStoredEventIds);
    } catch (UnirestException | IOException e) {
      this.logger.info("Failed to store Event", e);
      throw new IllegalStateException(e);
    }

    return EventHypothesisClaimCheck
        .from(event.getOverallPreferred().getId(), event.getId());
  }

  /**
   * Associates a Set of SignalDetectionHypothesis objects with an EventHypothesis via {@link
   * AssociatorUtility}.  Does not modify the provided EventHypothesis.  Creates a new
   * EventHypothesis with the new associations applied.
   *
   * @param detectionHypotheses {@link Set} of {@link SignalDetectionHypothesis} to associate with
   * the provided {@link EventHypothesis}
   * @param eventHypothesis {@link EventHypothesis} with which to associate the provided {@link Set}
   * of {@link SignalDetectionHypothesis}
   * @return New {@link EventHypothesis} with the requested associations applied
   */
  public EventHypothesis associateToEventHypothesisInteractive(
      Collection<SignalDetectionHypothesis> detectionHypotheses, EventHypothesis eventHypothesis) {

    Objects.requireNonNull(detectionHypotheses,
        "SignalDetectionAssociationControl::associateToEventHypothesis() requires non-null \"detectionHypotheses\" parameter");
    Objects.requireNonNull(eventHypothesis,
        "SignalDetectionAssociationControl::associateToEventHypothesis() requires non-null \"eventHypothesis\" parameter");

    return AssociatorUtility.associateToEvent(detectionHypotheses, eventHypothesis);
  }

  // TODO: Probably need to pass in the actual ProcessingContext object to the overloaded parameters
  public SignalDetectionAssociationResult associate(
      Collection<SignalDetectionHypothesisDescriptor> signalDetectionHypothesesDescriptors,
      SignalDetectionAssociationParameters params) throws Exception {

    Objects.requireNonNull(signalDetectionHypothesesDescriptors,
        "SignalDetectionAssociationControl::associate() requires non-null \"signalDetectionHypotheses\" parameter");
    Objects.requireNonNull(params,
        "SignalDetectionAssociationControl::associate() requires non-null \"parameters\" parameter");

    // TODO: When cache is implemented, need to update this in order to manually manage retrieval
    // TODO: update of cached objects.

    return getSignalDetectionAssociationResult(signalDetectionHypothesesDescriptors, params);
  }

  public SignalDetectionAssociationResult associate(
      Collection<SignalDetectionHypothesisDescriptor> signalDetectionHypotheses)
      throws Exception {
    Objects.requireNonNull(signalDetectionHypotheses,
        "SignalDetectionAssociationControl::associate() requires non-null \"signalDetectionHypotheses\" parameter");
    return getSignalDetectionAssociationResult(signalDetectionHypotheses, this.parameters);
  }

  private SignalDetectionAssociationResult getSignalDetectionAssociationResult(
      Collection<SignalDetectionHypothesisDescriptor> signalDetectionHypothesesDescriptors,
      SignalDetectionAssociationParameters params) throws Exception {
    List<EventHypothesis> events = new ArrayList<>();
    List<UUID> stationIds = signalDetectionHypothesesDescriptors.stream()
        .map((obj) -> obj.getStationId()).collect(Collectors.toList());

    // Grab Set of Reference Stations
    try {
      Set<ReferenceStation> referenceStations = this.osdGateway.retrieveStations(stationIds);
      logger.info("stationIds: " + stationIds);
      logger.info("Stations found: " + referenceStations);
      List<SdhStationAssociation> sdhStationAssociations = new ArrayList<>();
      for (SignalDetectionHypothesisDescriptor desc : signalDetectionHypothesesDescriptors) {
        for(ReferenceStation station : referenceStations) {
          if(station.getVersionId().equals(desc.getStationId())) {
            sdhStationAssociations
                .add(SdhStationAssociation.from(desc.getSignalDetectionHypothesis(),
                    station));
          }
        }
      }

      // Store provided sdhStationAssociations in the cache
      sdhStationAssociations.forEach(this.gaCache::cacheSdhStationAssociation);

      Pair<Set<SignalDetectionHypothesis>, Set<EventHypothesis>> results = this.plugin
          .associate(events, sdhStationAssociations,
              params.getSignalDetectionAssociatorDefinition().get());

      // Store resulting EventHypotheses in the cache
      results.getRight().forEach(this.gaCache::cacheEventHypothesis);

      return SignalDetectionAssociationResult.from(results.getKey(), results.getValue());
    } catch (Exception e) {
      logger.error("Association Failed", e);
      throw e;
    }
  }
}
