package gms.core.eventlocation.control;

import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.eventlocation.control.service.LocateValidationInput;
import gms.core.eventlocation.plugins.EventLocationDefinition;
import gms.core.eventlocation.plugins.EventLocatorPlugin;
import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredEventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.PreferredLocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventLocationControl {

  private static final Logger logger = LoggerFactory.getLogger(EventLocationControl.class);

  private final PluginRegistry pluginRegistry;

  private final EventLocatorPlugin eventLocatorPlugin;

  private final EventLocationControlParameters eventLocationControlParameters;

  private final EventLocationConfiguration eventLocationConfiguration;

  private final EventLocationControlOsdGateway osdGateway;


  private EventLocationControl(
      PluginRegistry pluginRegistry,
      EventLocatorPlugin eventLocatorPlugin,
      EventLocationControlParameters eventLocationControlParameters,
      EventLocationConfiguration eventLocationConfiguration,
      EventLocationControlOsdGateway osdGateway
  ) {

    this.pluginRegistry = pluginRegistry;

    this.eventLocatorPlugin = eventLocatorPlugin;

    this.eventLocationControlParameters = eventLocationControlParameters;

    this.eventLocationConfiguration = eventLocationConfiguration;

    this.osdGateway = osdGateway;
  }


  public static EventLocationControl create(EventLocationControlOsdGateway osdGateway) {

    Objects.requireNonNull(osdGateway, "Null osdGateway");

    PluginRegistry pluginRegistry = PluginRegistry.getRegistry();
    pluginRegistry.loadAndRegister();

    EventLocationConfiguration eventLocationConfiguration = EventLocationConfiguration.create();

    EventLocationControlParameters parameters = EventLocationControl
        .loadDefaultEventLocationControlParameters(eventLocationConfiguration);

    PluginInfo defaultPluginInfo = parameters.getPluginInfo().orElseThrow(
        () -> new IllegalStateException(String
            .format("Default %s does not contain %s", EventLocationDefinition.class.getSimpleName(),
                PluginInfo.class.getSimpleName())));

    EventLocatorPlugin eventLocatorPlugin = pluginRegistry
        .lookup(defaultPluginInfo, EventLocatorPlugin.class).orElseThrow(() -> {
          String errMsg = String
              .format("Could not load plugin class \"%s\" with name \"%s\" and version \"%s\".",
                  EventLocatorPlugin.class, defaultPluginInfo.getName(),
                  defaultPluginInfo.getVersion());

          EventLocationControl.logger.error(errMsg);

          return new IllegalStateException(errMsg);
        });

    EventLocationControl.logger.info("Loaded locator plugin for {}", defaultPluginInfo);

    EventLocationControl.logger.info("Initializing eventLocatorPlugin...");

    eventLocatorPlugin.initialize();

    EventLocationControl.logger.info("Successfully initialized eventLocatorPlugin");

    return new EventLocationControl(
        pluginRegistry,
        eventLocatorPlugin,
        parameters,
        eventLocationConfiguration,
        osdGateway
    );
  }


  public Collection<EventHypothesisClaimCheck> locate(
      Collection<EventHypothesisClaimCheck> eventHypothesisClaimChecks
  ) throws TooManyRestraintsException {

    EventLocationControl.logger.info(
        "Locating with EventHypothesisClaimChecks and default EventLocationControlParameters...\nEventHypothesisClaimChecks: {}",
        eventHypothesisClaimChecks);

    // Retrieve parent events.  Currently no way to retrieve EventHypotheses without retrieving parent Event.
    Set<Event> events;
    try {
      events = this.osdGateway.retrieveEvents(
          eventHypothesisClaimChecks.stream().map(EventHypothesisClaimCheck::getEventId)
              .collect(Collectors.toSet()));
    } catch (UnirestException | IOException e) {

      EventLocationControl.logger.error(e.getMessage());
      throw new IllegalStateException(e);
    }

    // Events may have other EventHypotheses we do not want to locate for, so collect the UUIDs of EventHypotheses
    //  we DO want to locate for.
    Set<UUID> eventHypothesisIds = eventHypothesisClaimChecks.stream()
        .map(EventHypothesisClaimCheck::getEventHypothesisId).collect(
            Collectors.toSet());

    if (!this.eventLocationControlParameters.getEventLocationDefinition().isPresent()) {

      throw new IllegalStateException(
          "Default EventLocationParameters has empty EventLocationDefinition");
    }

    return this.locateForEventHypotheses(this.eventLocatorPlugin,
        this.eventLocationControlParameters.getEventLocationDefinition().get(), events,
        eventHypothesisIds);
  }


  public Collection<EventHypothesisClaimCheck> locate(
      Collection<EventHypothesisClaimCheck> eventHypothesisClaimChecks,
      EventLocationControlParameters eventLocationControlParameters
  ) throws TooManyRestraintsException {

    EventLocationControl.logger.info(
        "Locating with EventHypothesisClaimChecks and overridden EventLocationControlParameters...\nEventHypothesisClaimChecks: {}\nEventLocationControlParameters: {}",
        eventHypothesisClaimChecks, eventLocationControlParameters);

    // Retrieve Events to extract EventHypotheses
    Set<Event> events;
    try {
      events = this.osdGateway.retrieveEvents(
          eventHypothesisClaimChecks.stream().map(EventHypothesisClaimCheck::getEventId)
              .collect(Collectors.toSet()));
    } catch (UnirestException | IOException e) {

      EventLocationControl.logger.error(e.getMessage());
      throw new IllegalStateException(e);
    }

    // Events may have other EventHypotheses we do not want to locate for, so collect the UUIDs of EventHypotheses
    //  we DO want to locate for.
    Set<UUID> eventHypothesisIds = eventHypothesisClaimChecks.stream()
        .map(EventHypothesisClaimCheck::getEventHypothesisId).collect(
            Collectors.toSet());

    LocatorPluginAndDefinition locatorPluginAndDefinition = this
        .resolveLocatorPluginAndDefinition(eventLocationControlParameters);

    EventLocatorPlugin locatorPlugin = locatorPluginAndDefinition.getLocatorPlugin();

    EventLocationDefinition locationDefinition = locatorPluginAndDefinition.getLocationDefinition();

    return this
        .locateForEventHypotheses(locatorPlugin, locationDefinition, events, eventHypothesisIds);
  }


  public Map<UUID, Set<LocationSolution>> locateInteractive(
      Collection<EventHypothesis> eventHypotheses,
      Collection<SignalDetection> signalDetections
  ) throws TooManyRestraintsException {

    EventLocationControl.logger.info(
        "Locating with EventHypotheses, SignalDetections and default EventLocationControlParameters...\n"
            + "EventHypotheses: {}\nSignalDetections: {}",
        eventHypotheses, signalDetections);

    // Maps eventHypothesis UUID to collection of location solutions produced by the locator
    Map<UUID, Set<LocationSolution>> locationSolutionsMap = new HashMap<>();

    for (EventHypothesis eh : eventHypotheses) {

//      // Get UUIDs of all associated SignalDetectionHypotheses
//      Set<UUID> associatedSignalDetectionHypothesesIds = eh.getAssociations()
//          .stream()
//          .map(SignalDetectionEventAssociation::getSignalDetectionHypothesisId)
//          .collect(Collectors.toSet());

      // Get reference station version UUID from those SignalDetections that contain an associated
      // SignalDetectionHypothesis
//      Set<UUID> referenceStationVersionIds = signalDetections.stream()
//          .filter(sd -> {
//                Set<UUID> hypothesisIdsInDetection = sd.getSignalDetectionHypotheses()
//                    .stream()
//                    .map(SignalDetectionHypothesis::getId)
//                    .collect(Collectors.toSet());
//
//                return hypothesisIdsInDetection.contains(sd.getId());
//              }
//          )
//          .map(SignalDetection::getStationId)
//          .collect(Collectors.toSet());

      Set<UUID> referenceStationVersionIds = signalDetections.stream()
          .map(SignalDetection::getStationId).collect(
              Collectors.toSet());

      Set<ReferenceStation> referenceStationsForProvidedDetections = new HashSet<>();

      referenceStationVersionIds.forEach(id -> {
            Optional<ReferenceStation> stationOptional = this.osdGateway.retrieveStation(id);
            if (stationOptional.isPresent()) {
              referenceStationsForProvidedDetections.add(stationOptional.get());
            }
          }
      );

      Validate.isTrue(
          referenceStationsForProvidedDetections.size() == referenceStationVersionIds.size(),
          "Unable to retrieve ReferenceStations for all ReferenceStation version UUIDs in the provided SignalDetections");

      LocateValidationInput locateValidationInput = LocateValidationInput.create(
          eh,
          new ArrayList<>(signalDetections),
          referenceStationsForProvidedDetections
      );

      EventLocationControl.logger.info(
          "Calling locator plugin with...\nSIGNAL_DETECTION_HYPOTHESES: {}\n STATIONS: {}\n EVENT_LOCATION_DEFINITION: {}\n",
          locateValidationInput.getSignalDetectionHypotheses(),
          referenceStationsForProvidedDetections,
          this.eventLocationControlParameters.getEventLocationDefinition());

      if (!this.eventLocationControlParameters.getEventLocationDefinition().isPresent()) {

        throw new IllegalStateException(
            "Default EventLocationParameters has empty EventLocationDefinition");
      }

      Set<LocationSolution> locationSolutions = new HashSet<>(
          this.eventLocatorPlugin.locate(
              locateValidationInput.getEventHypothesis().getPreferredLocationSolution(),
              locateValidationInput.getSignalDetectionHypotheses(),
              locateValidationInput.getReferenceStations(),
              this.eventLocationControlParameters.getEventLocationDefinition().get()
          )
      );

      EventLocationControl.logger.info("Resulting LocationSolutions: {}\n", locationSolutions);
      locationSolutionsMap.put(eh.getId(), locationSolutions);
    }

    return locationSolutionsMap;
  }

  public Map<UUID, Set<LocationSolution>> locateInteractive(
      Collection<EventHypothesis> eventHypotheses,
      Collection<SignalDetection> signalDetections,
      EventLocationControlParameters overrideEventLocationControlParameters
  ) throws TooManyRestraintsException {

    EventLocationControl.logger.info(
        "Locating with EventHypotheses and overridden EventLocationControlParameters...\nEventHypotheses: {}\n Parameters: {}",
        eventHypotheses, overrideEventLocationControlParameters);

    LocatorPluginAndDefinition locatorPluginAndDefinition = this
        .resolveLocatorPluginAndDefinition(overrideEventLocationControlParameters);

    EventLocatorPlugin locatorPlugin = locatorPluginAndDefinition.getLocatorPlugin();
    EventLocationDefinition locationDefinition = locatorPluginAndDefinition.getLocationDefinition();

    // Maps eventHypothesis UUID to collection of location solutions produced by the locator
    Map<UUID, Set<LocationSolution>> locationSolutionsMap = new HashMap<>();

    for (EventHypothesis eh : eventHypotheses) {

      Set<UUID> referenceStationVersionIds = signalDetections.stream()
          .map(SignalDetection::getStationId).collect(
              Collectors.toSet());

      Set<ReferenceStation> referenceStationsForProvidedDetections = new HashSet<>();

      referenceStationVersionIds.forEach(id -> {
            Optional<ReferenceStation> stationOptional = this.osdGateway.retrieveStation(id);
            if (stationOptional.isPresent()) {
              referenceStationsForProvidedDetections.add(stationOptional.get());
            }
          }
      );

      Validate.isTrue(
          referenceStationsForProvidedDetections.size() == referenceStationVersionIds.size(),
          "Unable to retrieve ReferenceStations for all ReferenceStation version UUIDs in the provided SignalDetections");

      LocateValidationInput locateValidationInput = LocateValidationInput.create(
          eh,
          new ArrayList<>(signalDetections),
          referenceStationsForProvidedDetections
      );

      EventLocationControl.logger.info(
          "Calling locator plugin with...\nSIGNAL_DETECTION_HYPOTHESES: {}\n STATIONS: {}\n EVENT_LOCATION_DEFINITION: {}\n",
          locateValidationInput.getSignalDetectionHypotheses(),
          referenceStationsForProvidedDetections,
          this.eventLocationControlParameters.getEventLocationDefinition());

      if (!this.eventLocationControlParameters.getEventLocationDefinition().isPresent()) {

        throw new IllegalStateException(
            "Default EventLocationParameters has empty EventLocationDefinition");
      }

      Set<LocationSolution> locationSolutions = new HashSet<>(
          locatorPlugin.locate(
              locateValidationInput.getEventHypothesis().getPreferredLocationSolution(),
              locateValidationInput.getSignalDetectionHypotheses(),
              locateValidationInput.getReferenceStations(),
              locationDefinition
          )
      );

      EventLocationControl.logger.info("Resulting LocationSolution: {}\n", locationSolutions);
      locationSolutionsMap.put(eh.getId(), locationSolutions);
    }

    return locationSolutionsMap;
  }

  public Map<UUID, Set<LocationSolution>> locateValidation(
      LocateValidationInput locateValidationInput
  ) throws TooManyRestraintsException {

    EventLocationControl.logger.info(
        "Locating with validation input and default EventLocationControlParameters...\nValidationInput: {}\nEventLocationControlParameters: {}",
        locateValidationInput, this.eventLocationControlParameters);

    // Maps eventHypothesis UUID to collection of location solutions produced by the locator
    Map<UUID, Set<LocationSolution>> locationSolutionsMap = new HashMap<>();

    // Locate for input object
    EventLocationDefinition definition = this.eventLocationControlParameters
        .getEventLocationDefinition()
        .orElseThrow(() -> new IllegalStateException(
            "Default EventLocationDefinition not present in default EventLocationControlParameters"));

    EventLocationControl.logger.info(
        "Calling locator plugin with...\nSIGNAL_DETECTION_HYPOTHESES: {}\n STATIONS: {}\n EVENT_LOCATION_DEFINITION: {}\n",
        locateValidationInput.getSignalDetectionHypotheses(),
        locateValidationInput.getReferenceStations(),
        definition);

    locationSolutionsMap.put(
        locateValidationInput.getEventHypothesis().getId(),
        new HashSet<>(this.eventLocatorPlugin.locate(
            locateValidationInput.getEventHypothesis().getPreferredLocationSolution(),
            locateValidationInput.getSignalDetectionHypotheses(),
            locateValidationInput.getReferenceStations(),
            definition
        ))
    );

    return locationSolutionsMap;
  }

  public Map<UUID, Set<LocationSolution>> locateValidation(
      LocateValidationInput locateValidationInput,
      EventLocationControlParameters overrideEventLocationControlParameters
  ) throws TooManyRestraintsException {

    // Resolve overridden plugin and/or definition
    LocatorPluginAndDefinition locatorPluginAndDefinition = this
        .resolveLocatorPluginAndDefinition(overrideEventLocationControlParameters);

    // Maps eventHypothesis UUID to collection of location solutions produced by the locator
    Map<UUID, Set<LocationSolution>> locationSolutionsMap = new HashMap<>();

    // Locate for input object
    EventLocationControl.logger.info(
        "Calling locator plugin with...\nSIGNAL_DETECTION_HYPOTHESES: {}\n STATIONS: {}\n EVENT_LOCATION_DEFINITION: {}\n",
        locateValidationInput.getSignalDetectionHypotheses(),
        locateValidationInput.getReferenceStations(),
        locatorPluginAndDefinition.getLocationDefinition());

    locationSolutionsMap.put(
        locateValidationInput.getEventHypothesis().getId(),
        new HashSet<>(locatorPluginAndDefinition.getLocatorPlugin().locate(
            locateValidationInput.getEventHypothesis().getPreferredLocationSolution(),
            locateValidationInput.getSignalDetectionHypotheses(),
            locateValidationInput.getReferenceStations(),
            locatorPluginAndDefinition.getLocationDefinition()
        ))

    );

    return locationSolutionsMap;
  }


  // Value class for holding non-optional references to the locator plugin and location definition
  private class LocatorPluginAndDefinition {

    private final EventLocatorPlugin locatorPlugin;
    private final EventLocationDefinition locationDefinition;

    LocatorPluginAndDefinition(EventLocatorPlugin locatorPlugin,
        EventLocationDefinition locationDefinition) {

      Objects.requireNonNull(locatorPlugin, "Null locatorPlugin");
      Objects.requireNonNull(locationDefinition, "Null locationDefinition");

      this.locatorPlugin = locatorPlugin;
      this.locationDefinition = locationDefinition;
    }

    EventLocatorPlugin getLocatorPlugin() {
      return locatorPlugin;
    }

    EventLocationDefinition getLocationDefinition() {
      return locationDefinition;
    }

  }


  private LocatorPluginAndDefinition resolveLocatorPluginAndDefinition(
      EventLocationControlParameters eventLocationControlParameters) {

    Optional<PluginInfo> optionalPluginInfo = eventLocationControlParameters.getPluginInfo();
    Optional<EventLocationDefinition> optionalEventLocationDefinition = eventLocationControlParameters
        .getEventLocationDefinition();

    EventLocatorPlugin locatorPlugin;
    EventLocationDefinition locationDefinition;

    String noDefinitionErrorMsg = "EventLocationDefinition was not provided, and the default EventLocationDefinition for the provided PluginInfo could not be loaded";

    // If user overrides PluginInfo AND EventLocationDefinition, use both
    // If user overrides PluginInfo but NOT EventLocationDefinition, load specified plugin and default EventLocationDefinition
    // If user overrides EventLocationDefinition but NOT PluginInfo, use specified definition with default EventLocationPlugin
    // If user does NOT override EventLocationDefinition and a default one cannot be loaded, throw IllegalStateException
    if (optionalPluginInfo.isPresent()) {

      // User overrode the locator plugin - use it
      locatorPlugin = this.loadUserPlugin(optionalPluginInfo.get());
      locatorPlugin.initialize();

      if (optionalEventLocationDefinition.isPresent()) {

        // User overrode the default definition for the overridden plugin - use it
        locationDefinition = optionalEventLocationDefinition.get();
      } else {

        // User did not override the default definition for the overridden plugin - use the
        // definition from configuration.  If no definition from configuration is found, throw
        // an IllegalStateException.
        EventLocationControl.logger
            .info("User did not override EventLocationDefinition - using default");
        locationDefinition = this.eventLocationConfiguration
            .getParametersForPlugin(optionalPluginInfo.get()).getEventLocationDefinition()
            .orElseThrow(() -> new IllegalStateException(noDefinitionErrorMsg));
      }
    } else {

      // User did not override locator plugin - use default plugin
      locatorPlugin = this.eventLocatorPlugin;

      // If user overrode default definition, use it.  Else, use the default definition.  If no
      // definition can be obtained, throw an IllegalStateException.
      locationDefinition = optionalEventLocationDefinition.orElse(
          this.eventLocationControlParameters.getEventLocationDefinition()
              .orElseThrow(() -> new IllegalStateException(noDefinitionErrorMsg))
      );
    }

    return new LocatorPluginAndDefinition(locatorPlugin, locationDefinition);
  }


  // Utility method for calling the locator plugin.  Prepares the input for the locator plugin, constructs updated
  // Events/EventHypotheses from the locator plugin output, and stores the updated Events
  private Set<EventHypothesisClaimCheck> locateForEventHypotheses(EventLocatorPlugin
      locatorPlugin,
      EventLocationDefinition locatorDefinition, Collection<Event> events,
      Collection<UUID> eventHypothesisIds) throws TooManyRestraintsException {

    // Holds updated Events - Events are immutable so we must create brand new Events from the original Events.
    Collection<Event> newEvents = new ArrayList<>();

    // Holds new EventHypothesisClaimChecks
    Set<EventHypothesisClaimCheck> newClaimChecks = new HashSet<>();

    for (Event e : events) {

      // Holds preferred event hypothesis history, needed when creating the new event to store.
      // Every location generates a new event hypothesis - and each event hypothesis gets added
      // to the preferred event hypothesis history
      List<PreferredEventHypothesis> preferredEventHypothesisHistory = new ArrayList<>(
          e.getPreferredEventHypothesisHistory());

      // Filter for only the EventHypotheses we want to locate on
      Set<EventHypothesis> hypotheses = e.getHypotheses().stream()
          .filter(eh -> eventHypothesisIds.contains(eh.getId())).collect(Collectors.toSet());

      // Copy original list of hypotheses so we can add our new hypothesis to this
      Set<EventHypothesis> newHypotheses = new HashSet<>(e.getHypotheses());

      for (EventHypothesis eh : hypotheses) {

        // Build input for locator plugin
        Pair<List<SignalDetectionHypothesis>, List<ReferenceStation>> locatorInput = this
            .buildLocatorInput(eh);
        List<SignalDetectionHypothesis> signalDetectionHypotheses = locatorInput.getLeft();
        List<ReferenceStation> referenceStations = locatorInput.getRight();

        // Call locator plugin
        List<LocationSolution> locationSolutions = locatorPlugin
            .locate(eh.getPreferredLocationSolution(), signalDetectionHypotheses, referenceStations,
                locatorDefinition);

        // Builds and add new hypothesis to list of new Hypotheses so that we can add them to the updated Event
        EventHypothesis newHypothesis = this.buildUpdatedEventHypothesis(eh, locationSolutions);
        newHypotheses.add(newHypothesis);

        // Create and add EventHypothesisClaimCheck for the new EventHypothesis to the list of new
        //  EventHypothesisClaimChecks
        newClaimChecks.add(
            EventHypothesisClaimCheck.from(
                newHypothesis.getId(),
                e.getId()
            )
        );

        preferredEventHypothesisHistory.add(
            PreferredEventHypothesis.from(
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                newHypothesis
            )
        );
      }

      // Add updated event to collection of updated events to store
      newEvents.add(
          Event.from(
              e.getId(),
              e.getRejectedSignalDetectionAssociations(),
              e.getMonitoringOrganization(),
              newHypotheses,
              e.getFinalEventHypothesisHistory(),
              preferredEventHypothesisHistory
          )
      );
    }

    // Store updated events
    Collection<UUID> storedEventIds;
    try {

      storedEventIds = this.osdGateway.storeOrUpdateEvents(newEvents);
    } catch (UnirestException | IOException e) {

      EventLocationControl.logger.error(e.getMessage());
      throw new IllegalStateException(e);
    }

    // Verify the new Event storage was successful

    Set<UUID> newEventIds = newEvents.stream().map(Event::getId)
        .collect(Collectors.toSet());

    Set<UUID> failedIds = newEventIds.stream().filter(id -> !storedEventIds.contains(id))
        .collect(Collectors.toSet());

    if (!failedIds.isEmpty()) {

      String errMsg = String.format("Failed to store updated Events with UUIDs: %s", failedIds);
      EventLocationControl.logger.error(errMsg);
      throw new IllegalStateException(errMsg);
    }

    // Storage was successful, return new claim checks
    return newClaimChecks;
  }

  // Utility method to create a new EventHypothesis given a new LocationSolution and the EventHypothesis
  // the new LocationSolution was generated from
  private EventHypothesis buildUpdatedEventHypothesis(EventHypothesis oldHypothesis,
      List<LocationSolution> locationSolutions) {

    Set<UUID> parentEventHypotheses = new HashSet<>(oldHypothesis.getParentEventHypotheses());
    parentEventHypotheses.add(oldHypothesis.getId());

    return EventHypothesis.from(
        UUID.randomUUID(),
        oldHypothesis.getEventId(),
        parentEventHypotheses,
        false,
        new HashSet<>(locationSolutions),
        getPreferredLocationSolutionFromRestraints(locationSolutions),
        oldHypothesis.getAssociations()
    );
  }

  private static PreferredLocationSolution getPreferredLocationSolutionFromRestraints(
      List<LocationSolution> locationSolutions) {
    //unrestrained > fixed at surface > fixed at depth.

    return PreferredLocationSolution.from(
        locationSolutions.stream().reduce((locationSolution1, locationSolution2) -> {
          DepthRestraintType depthRestraintType1 = locationSolution1.getLocationRestraint()
              .getDepthRestraintType();
          DepthRestraintType depthRestraintType2 = locationSolution2.getLocationRestraint()
              .getDepthRestraintType();

          if (depthRestraintType1.equals(DepthRestraintType.UNRESTRAINED)) {
            return locationSolution1;
          } else if (depthRestraintType2.equals(DepthRestraintType.UNRESTRAINED)) {
            return locationSolution2;
          } else if (depthRestraintType1.equals(DepthRestraintType.FIXED_AT_SURFACE)) {
            return locationSolution1;
          } else if (depthRestraintType2.equals(DepthRestraintType.FIXED_AT_SURFACE)) {
            return locationSolution2;
          }
          return locationSolution1;
        }).orElse(locationSolutions.get(0)));
  }

  // Utility method for loading the user-requested plugin
  private EventLocatorPlugin loadUserPlugin(PluginInfo pluginInfo) {
    Optional<EventLocatorPlugin> optionalUserOverridePlugin = this.pluginRegistry.lookup(
        pluginInfo,
        EventLocatorPlugin.class
    );

    if (!optionalUserOverridePlugin.isPresent()) {
      throw new IllegalStateException(String.format("Could not load user requested plugin: %s",
          pluginInfo.toString()));
    }

    return optionalUserOverridePlugin.get();
  }

  // Utility method for loading default EventLocationControlParameters and EventLocationDefinition from config
  private static EventLocationControlParameters loadDefaultEventLocationControlParameters(
      EventLocationConfiguration eventLocationConfiguration) {

    Configuration config;
    try {

      config = new PropertiesConfiguration(
          "gms/core/eventlocation/control/EventLocationControl.properties");
    } catch (ConfigurationException e) {

      String errorMsg = "Could not create EventLocationControl - could not load default EventLocationControlParameters";
      EventLocationControl.logger.error(errorMsg, e);
      throw new IllegalStateException(errorMsg, e);
    }

    // Load plugin name from config
    String pluginName = config.getString("pluginName");
    Objects.requireNonNull(pluginName,
        "No \"pluginName\" property in EventLocationControl.properties");

    // Load plugin version from config
    String pluginVersion = config.getString("pluginVersion");
    Objects.requireNonNull(pluginVersion,
        "No \"pluginName\" property in EventLocationControl.properties");

    EventLocationControlParameters controlParameters = eventLocationConfiguration
        .getParametersForPlugin(PluginInfo.from(pluginName, pluginVersion));

    if (!controlParameters.getEventLocationDefinition().isPresent()) {

      throw new IllegalStateException(String.format(
          "Default EventLocationParameters has empty EventLocationDefinition.  PluginInfo: %s",
          PluginInfo.from(pluginName, pluginVersion)));
    }

    return controlParameters;
  }

  // Utility method for loading data objects to pass to plugin
  private Pair<List<SignalDetectionHypothesis>, List<ReferenceStation>> buildLocatorInput(
      EventHypothesis eventHypothesis) {

    // Extract SignalDetectionHypothesis UUIDs to retrieve from OSD

    List<UUID> hypothesisIds = eventHypothesis.getAssociations().stream()
        .map(SignalDetectionEventAssociation::getSignalDetectionHypothesisId)
        .collect(Collectors.toList());

    List<SignalDetectionHypothesis> hypotheses;

    try {

      hypotheses = this.osdGateway
          .retrieveSignalDetectionHypotheses(hypothesisIds);
    } catch (UnirestException | IOException e) {

      logger.error(e.getMessage());
      throw new IllegalStateException(e);
    }

    // Extract SignalDetection UUIDs to retrieve from OSD

    List<UUID> detectionIds = hypotheses.stream()
        .map(SignalDetectionHypothesis::getParentSignalDetectionId).collect(Collectors.toList());

    // Retrieve SignalDetections from OSD

    List<SignalDetection> detections;

    try {

      detections = this.osdGateway
          .retrieveSignalDetections(detectionIds);
    } catch (UnirestException | IOException e) {

      logger.error(e.getMessage());
      throw new IllegalStateException(e);
    }

    // Extract ReferenceStation UUIDs to retrieve from OSD

    List<UUID> stationIds = detections.stream().map(SignalDetection::getStationId).collect(
        Collectors.toList());

    // Retrieve ReferenceStations

    List<ReferenceStation> stations = new ArrayList<>();

    stationIds.forEach(id -> {
          Optional<ReferenceStation> stationOptional = this.osdGateway.retrieveStation(id);
          if (stationOptional.isPresent()) {
            stations.add(stationOptional.get());
          }
        }
    );

    if (stations.size() != detections.size()) {
      throw new IllegalStateException("Could not retrieve stations for all hypotheses");
    }

    return Pair.of(hypotheses, stations);
  }
}
