package gms.core.signaldetection.association.plugins.implementations.globalgrid;

import gms.core.signaldetection.association.CandidateEvent;
import gms.core.signaldetection.association.MissingFeatureMeasurementException;
import gms.core.signaldetection.association.MissingNodeStationException;
import gms.core.signaldetection.association.UnexpectedPhasesException;
import gms.core.signaldetection.association.eventredundancy.plugins.EventRedundancyRemoval;
import gms.core.signaldetection.association.eventredundancy.plugins.EventRedundancyRemovalDefinition;
import gms.core.signaldetection.association.plugins.SdhStationAssociation;
import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint.Builder;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import gms.shared.utilities.javautilities.generation.GenerationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalGridSignalDetectionAssociatorDelegate {

  static Logger logger = LoggerFactory.getLogger(GlobalGridSignalDetectionAssociatorDelegate.class);
  private EventRedundancyRemoval redundancyRemovalPlugin;

  private GlobalGridSignalDetectionAssociatorDelegate(EventRedundancyRemoval plugin) {
    this.redundancyRemovalPlugin = plugin;
    this.redundancyRemovalPlugin.initialize();
  }

  public static GlobalGridSignalDetectionAssociatorDelegate create(
      PluginRegistry pluginRegistry,
      PluginInfo redundancyPluginInfo) {
    pluginRegistry.loadAndRegister();
    Optional<EventRedundancyRemoval> plugin = pluginRegistry
        .lookup(redundancyPluginInfo, EventRedundancyRemoval.class);
    return new GlobalGridSignalDetectionAssociatorDelegate(
        plugin.orElseThrow(() -> new IllegalStateException("Redundancy Plugin not found")));
  }

  public Pair<Set<SignalDetectionHypothesis>, Set<EventHypothesis>> associate(
      Collection<EventHypothesis> eventHypotheses,
      Collection<SdhStationAssociation> sdhStationAssociations,
      SignalDetectionAssociatorDefinition definition,
      TesseractModelGA model) {

    // TODO: Need to write implementation of basic associator here.

    //TODO: Uncomment after figuring out how the generate the associations list.
    //  and ensuring that model.getFirstArrivalMap() returns a non-empty map.
    //  Calling sdhStationAssocations() with an empty list or
    //  gridNodeMap() with an empty map generates IllegalArgumentExceptions.

    model.initializeFirstArrivalMap(definition.getNumFirstSta());
    CandidateEventGenerator eventGenerator = new CandidateEventGenerator()
        .definition(definition)
        .sigmaTime(definition.getSigmaTime())
        .gridNodeMap(model.getFirstArrivalMap())
        .sdhStationAssociations(new ArrayList<>(sdhStationAssociations));

    // generate a set of candidate events
    try {
      Set<SignalDetectionHypothesis> signalDetectionHypotheses = sdhStationAssociations.stream()
          .map((obj) -> obj.getSignalDetectionHypothesis())
          .collect(Collectors.toSet());
      Optional<Set<CandidateEvent>> optionalCandidateEvents = eventGenerator.generate();
      if (optionalCandidateEvents.isPresent()) {
        // run the associator to populate corroborators for each SDH station association
        associateCorroborators(optionalCandidateEvents.get(), sdhStationAssociations, definition);

        // call the redundancy plugin
        EventRedundancyRemovalDefinition redundancyRemovalDefinition = EventRedundancyRemovalDefinition
            .create(definition.getWeightedEventCriteria(), definition.getArrivalQualityCriteria());
        Set<CandidateEvent> events = this.redundancyRemovalPlugin
            .reduce(optionalCandidateEvents.get(), redundancyRemovalDefinition);
        Set<EventHypothesis> eventHypothesisSet = events.stream().map((obj) -> {
          final LocationSolution newLocationSolution = LocationSolution.create(
              EventLocation.from(obj.getGridNode().getCenterLatitudeDegrees(),
                  obj.getGridNode().getCenterLongitudeDegrees(),
                  obj.getGridNode().getCenterDepthKm(),
                  obj.getOriginTime()),
              new Builder().build(),
              null,
              Set.of(),
              Set.of()
          );
          final UUID eventHypothesisId = UUID.randomUUID();
          obj.getCorroboratingSet().add(obj.getDriverSdh());
          Set<SignalDetectionEventAssociation> eventAssociations =
              obj
                  .getCorroboratingSet()
                  .stream()
                  .map((sdh) -> SignalDetectionEventAssociation.from(
                      UUID.randomUUID(), eventHypothesisId, sdh.getId(), false))
                  .collect(Collectors.toSet());
          return EventHypothesis.from(
              UUID.randomUUID(),
              eventHypothesisId,
              Set.of(),
              false,
              Set.of(
                  newLocationSolution
              ),
              PreferredLocationSolution.from(newLocationSolution),
              eventAssociations
          );
        }).collect(Collectors.toSet());

        events.stream().forEach((obj) -> signalDetectionHypotheses.add(obj.getDriverSdh()));
        return Pair.of(signalDetectionHypotheses, eventHypothesisSet);
      }
    } catch (GenerationException ex) {
      String message = ex.getMessage();
      if (ex.getCause() != null) {
        message += ": " + ex.getCause().getMessage();
      }
      logger.error(message, ex);
    }

    return Pair.of(Set.of(), Set.of());
  }

  /**
   *
   */
  private void associateCorroborators(Set<CandidateEvent> candidates,
      Collection<SdhStationAssociation> sdhStationAssociations,
      SignalDetectionAssociatorDefinition definition) {

    Set<SignalDetectionHypothesis> signalDetectionHypotheses = sdhStationAssociations.stream()
        .map((obj) -> obj.getSignalDetectionHypothesis()).collect(Collectors.toSet());
    //
    // Go through the list of candidate events and filter those signal detection hypotheses
    // whose arrival times are before the candidate event's origin time and whose phase type
    // is either unknown or is not in the user-specified list.
    //
    for (CandidateEvent candidateEvent : candidates) {
      Set<SignalDetectionHypothesis> corroborators = sdhStationAssociations
          .stream()
          // first filter those sdh station association whose signal detection hypothesis
          // arrival time is before arrival time
          // of candidate event.
          .filter(sdhStationAssociation -> sdhStationAssociation.getSignalDetectionHypothesis()
              .getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME)
              .orElseThrow(AssertionError::new)
              .getMeasurementValue()
              .getValue()
              .isAfter(candidateEvent.getDriverSdh()
                  .getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME)
                  .orElseThrow(AssertionError::new)
                  .getMeasurementValue()
                  .getValue()))
          // next, filter out those sdh station association whose SDH phase type is either not
          // in list of user-specified phases and is not an UNKNOWN phase.
          .filter(sdhStationAssociation -> definition.getPhases().contains(
              sdhStationAssociation.getSignalDetectionHypothesis()
                  .getFeatureMeasurement(FeatureMeasurementTypes.PHASE)
                  .orElseThrow(AssertionError::new)
                  .getMeasurementValue()
                  .getValue()) ||
              sdhStationAssociation.getSignalDetectionHypothesis()
                  .getFeatureMeasurement(FeatureMeasurementTypes.PHASE)
                  .orElseThrow(AssertionError::new).getMeasurementValue().getValue()
                  .equals(PhaseType.UNKNOWN))
          //
          .filter(sdhStationAssociation -> {
            try {
              return candidateEvent.passesTravelTimeConstraintCheck(sdhStationAssociation)
                  && CandidateEvent
                  .passesSlownessConstraintCheck(sdhStationAssociation.getReferenceStation(),
                      sdhStationAssociation.getSignalDetectionHypothesis(),
                      definition.getSigmaSlowness(),
                      candidateEvent.getGridNode());
            } catch (MissingFeatureMeasurementException e) {
              e.printStackTrace();
            } catch (MissingNodeStationException e) {
              e.printStackTrace();
            } catch (UnexpectedPhasesException e) {
              e.printStackTrace();
            }
            return false;
          }).map((obj) -> obj.getSignalDetectionHypothesis()).collect(Collectors.toSet());

      candidateEvent.getCorroboratingSet().addAll(corroborators);
    }
  }

}
