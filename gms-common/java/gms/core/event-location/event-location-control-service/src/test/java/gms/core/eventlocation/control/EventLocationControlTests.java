package gms.core.eventlocation.control;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.eventlocation.control.service.LocateValidationInput;
import gms.core.eventlocation.plugins.EventLocationDefinition;
import gms.core.eventlocation.plugins.EventLocatorPlugin;
import gms.core.eventlocation.plugins.definitions.EventLocationDefinitionGeigers;
import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;

class EventLocationControlTests {

  private EventLocationControlOsdGateway mockOsdGateway = Mockito
      .mock(EventLocationControlOsdGateway.class);

  private EventLocatorPlugin mockEventLocatorPlugin = Mockito.mock(EventLocatorPlugin.class);

  private List<EventHypothesisClaimCheck> eventHypothesisClaimChecks = List.of(
      EventHypothesisClaimCheck.from(
          TestFixtures.event.getHypotheses().iterator().next().getId(),
          TestFixtures.event.getId()
      )
  );

  private EventLocationControlParameters parameters = EventLocationControlParameters.create(
      PluginInfo.from("eventLocationGeigersPlugin", "1.0.0"),
      EventLocationDefinitionGeigers.create(
          100,
          0.01,
          0.95,
          "ak135",
          true,
          ScalingFactorType.CONFIDENCE,
          4,
          0.01,
          4,
          2,
          true,
          0.01,
          10.0,
          0.01,
          0.01,
          1.0e5,
          0.1,
          1.0,
          4,
          List.of(LocationRestraint.from(
              RestraintType.UNRESTRAINED,
              null,
              RestraintType.UNRESTRAINED,
              null,
              DepthRestraintType.UNRESTRAINED,
              null,
              RestraintType.UNRESTRAINED,
              null))
      )
  );

  @BeforeEach
  void initEach() {
    Mockito.reset(this.mockEventLocatorPlugin, this.mockOsdGateway);
  }

  @Test
  void testCreate() {

    Assertions.assertNotNull(EventLocationControl.create(this.mockOsdGateway));
  }

  @Test
  void testCreateNullOsdGateway() {

    Throwable exception = Assertions.assertThrows(NullPointerException.class, () ->
        EventLocationControl.create(null)
    );

    Assertions.assertEquals("Null osdGateway", exception.getMessage());
  }

  // * * * * * * *
  // test locate()
  // * * * * * * *

  @Test
  void testLocate()
      throws UnirestException, IOException, NoSuchFieldException, TooManyRestraintsException {

    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    Set<UUID> eventUuids = this.eventHypothesisClaimChecks.stream()
        .map(EventHypothesisClaimCheck::getEventId).collect(
            Collectors.toSet());

    given(this.mockOsdGateway.retrieveEvents(eventUuids)).willReturn(Set.of(TestFixtures.event));

    given(this.mockOsdGateway.storeOrUpdateEvents(Mockito.anyCollection()))
        .willReturn(Set.of(TestFixtures.event.getId()));

    given(this.mockOsdGateway.retrieveSignalDetectionHypotheses(anyList()))
        .willReturn(TestFixtures.signalDetectionHypotheses);

    given(this.mockOsdGateway.retrieveSignalDetections(anyList()))
        .willReturn(TestFixtures.signalDetections);

    given(this.mockOsdGateway.retrieveStation(any(UUID.class)))
        .willReturn(Optional.of(TestFixtures.stations.get(0)));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocationControlParameters"),
        this.parameters);

    // Mock locate retuns a LocationSolution
    given(this.mockEventLocatorPlugin
        .locate(any(), eq(TestFixtures.signalDetectionHypotheses), eq(TestFixtures.stations),
            eq(this.parameters.getEventLocationDefinition().get())))
        .willReturn(List.of(TestFixtures.locationSolution));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocatorPlugin"),
        this.mockEventLocatorPlugin);

    Collection<EventHypothesisClaimCheck> newEventHypothesisClaimChecks = eventLocationControl
        .locate(this.eventHypothesisClaimChecks);

    verify(this.mockOsdGateway).retrieveEvents(eventUuids);

    Assertions.assertEquals(this.eventHypothesisClaimChecks.iterator().next().getEventId(),
        newEventHypothesisClaimChecks.iterator().next().getEventId());

    Assertions
        .assertNotEquals(this.eventHypothesisClaimChecks.iterator().next().getEventHypothesisId(),
            newEventHypothesisClaimChecks.iterator().next().getEventHypothesisId());
  }

  @ParameterizedTest
  @MethodSource("locationRestraintPermutationSupplier")
  void testLocatePrioritizesLocationSolutions(
      List<LocationSolution> returnedLocationSolutions,
      LocationSolution preferredLocationSolution,

      //extra super duper thorough
      boolean sanityCheck
  )
      throws UnirestException, IOException, NoSuchFieldException, TooManyRestraintsException {
    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    Set<UUID> eventUuids = this.eventHypothesisClaimChecks.stream()
        .map(EventHypothesisClaimCheck::getEventId).collect(
            Collectors.toSet());

    given(this.mockOsdGateway.retrieveEvents(eventUuids)).willReturn(Set.of(TestFixtures.event));

    given(this.mockOsdGateway.storeOrUpdateEvents(Mockito.anyCollection()))
        .willAnswer(invocation -> {

          //Check that the event hypothesis being stored has the correct prefered location solution.

          EventHypothesis newHypothesis = ((Collection<Event>) invocation.getArgument(0))
              .stream().findFirst().get()
              .getHypotheses().stream()
              .filter(eventHypothesis -> !eventHypothesis.getId()
                  .equals(TestFixtures.event.getHypotheses().stream().findFirst().get().getId()))
              .findFirst().get();

          if (!preferredLocationSolution.equals(
              newHypothesis.getPreferredLocationSolution().get().getLocationSolution())) {
            Assertions.assertTrue(sanityCheck);
          } else {
            Assertions.assertFalse(sanityCheck);
          }

          return Set.of(TestFixtures.event.getId());
        });

    given(this.mockOsdGateway.retrieveSignalDetectionHypotheses(anyList()))
        .willReturn(TestFixtures.signalDetectionHypotheses);

    given(this.mockOsdGateway.retrieveSignalDetections(anyList()))
        .willReturn(TestFixtures.signalDetections);

    given(this.mockOsdGateway.retrieveStation(any(UUID.class)))
        .willReturn(Optional.of(TestFixtures.stations.get(0)));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocationControlParameters"),
        this.parameters);

    // Mock locate retuns a LocationSolution
    given(this.mockEventLocatorPlugin
        .locate(any(), eq(TestFixtures.signalDetectionHypotheses), eq(TestFixtures.stations),
            //Control class itself does not care about the list of location restraints. Only
            //the plugin does. But we are using a mock plugin which doesnt care about them. The
            //real plugin is being tested against the list of restraints in its own test.
            eq(this.parameters.getEventLocationDefinition().get())))
        .willReturn(returnedLocationSolutions);

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocatorPlugin"),
        this.mockEventLocatorPlugin);

    Collection<EventHypothesisClaimCheck> newEventHypothesisClaimChecks = eventLocationControl
        .locate(this.eventHypothesisClaimChecks);

    verify(this.mockOsdGateway).retrieveEvents(eventUuids);

    Assertions.assertEquals(this.eventHypothesisClaimChecks.iterator().next().getEventId(),
        newEventHypothesisClaimChecks.iterator().next().getEventId());

    Assertions
        .assertNotEquals(this.eventHypothesisClaimChecks.iterator().next().getEventHypothesisId(),
            newEventHypothesisClaimChecks.iterator().next().getEventHypothesisId());
  }

  private static Stream<Arguments> locationRestraintPermutationSupplier() {
    return Stream.of(

        //Three location restraints
        Arguments.arguments(
            List.of(TestFixtures.locationSolution, TestFixtures.locationSolutionFixedAtDepth,
                TestFixtures.locationSolutionFixedAtSurface),
            TestFixtures.locationSolution, false),

        Arguments.arguments(
            List.of(TestFixtures.locationSolution, TestFixtures.locationSolutionFixedAtSurface,
                TestFixtures.locationSolutionFixedAtDepth),
            TestFixtures.locationSolution, false),

        Arguments.arguments(
            List.of(TestFixtures.locationSolutionFixedAtDepth, TestFixtures.locationSolution,
                TestFixtures.locationSolutionFixedAtSurface),
            TestFixtures.locationSolution, false),

        Arguments.arguments(
            List.of(TestFixtures.locationSolutionFixedAtSurface, TestFixtures.locationSolution,
                TestFixtures.locationSolutionFixedAtDepth),
            TestFixtures.locationSolution, false),

        Arguments.arguments(
            List.of(TestFixtures.locationSolutionFixedAtDepth,
                TestFixtures.locationSolutionFixedAtSurface, TestFixtures.locationSolution),
            TestFixtures.locationSolution, false),

        Arguments.arguments(
            List.of(TestFixtures.locationSolutionFixedAtSurface,
                TestFixtures.locationSolutionFixedAtDepth, TestFixtures.locationSolution),
            TestFixtures.locationSolution, false),

        //UNRESTRAINED and FIXED_AT_SURFACE
        Arguments.arguments(
            List.of(TestFixtures.locationSolutionFixedAtSurface, TestFixtures.locationSolution),
            TestFixtures.locationSolution, false),

        Arguments.arguments(
            List.of(TestFixtures.locationSolution, TestFixtures.locationSolutionFixedAtSurface),
            TestFixtures.locationSolution, false),

        //UNRESTRAINED and FIXED_AT_DEPTH
        Arguments.arguments(
            List.of(TestFixtures.locationSolutionFixedAtDepth, TestFixtures.locationSolution),
            TestFixtures.locationSolution, false),

        Arguments.arguments(
            List.of(TestFixtures.locationSolution, TestFixtures.locationSolutionFixedAtDepth),
            TestFixtures.locationSolution, false),

        //FIXED_AT_SURFACE and FIXED_AT_DEPTH
        Arguments.arguments(
            List.of(TestFixtures.locationSolutionFixedAtSurface,
                TestFixtures.locationSolutionFixedAtDepth),
            TestFixtures.locationSolutionFixedAtSurface, false),

        Arguments.arguments(
            List.of(TestFixtures.locationSolutionFixedAtDepth,
                TestFixtures.locationSolutionFixedAtSurface),
            TestFixtures.locationSolutionFixedAtSurface, false),

        //SANITY CHECK
        Arguments.arguments(
            List.of(TestFixtures.locationSolutionFixedAtDepth,
                TestFixtures.locationSolutionFixedAtSurface),
            TestFixtures.locationSolutionFixedAtDepth, true),

        Arguments.arguments(
            List.of(TestFixtures.locationSolutionFixedAtDepth,
                TestFixtures.locationSolutionFixedAtSurface),
            TestFixtures.locationSolution, true)
    );
  }

  @Test
  void testLocateOsdGatewayThrowsUnirestException() throws UnirestException, IOException {

    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    Set<UUID> eventUuids = this.eventHypothesisClaimChecks.stream()
        .map(EventHypothesisClaimCheck::getEventId).collect(
            Collectors.toSet());

    given(mockOsdGateway.retrieveEvents(eventUuids)).willThrow(UnirestException.class);

    Assertions.assertThrows(IllegalStateException.class, () ->
        eventLocationControl.locate(this.eventHypothesisClaimChecks)
    );

    verify(this.mockOsdGateway).retrieveEvents(eventUuids);
  }

  @Test
  void testLocateOsdGatewayThrowsIOException() throws UnirestException, IOException {

    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    Set<UUID> eventUuids = this.eventHypothesisClaimChecks.stream()
        .map(EventHypothesisClaimCheck::getEventId).collect(
            Collectors.toSet());

    given(mockOsdGateway.retrieveEvents(eventUuids)).willThrow(IOException.class);

    Assertions.assertThrows(IllegalStateException.class, () ->
        eventLocationControl.locate(this.eventHypothesisClaimChecks)
    );

    verify(this.mockOsdGateway).retrieveEvents(eventUuids);
  }

  @Test
  void testLocateOverrideParameters()
      throws UnirestException, IOException, NoSuchFieldException, TooManyRestraintsException {

    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    Set<UUID> eventUuids = this.eventHypothesisClaimChecks.stream()
        .map(EventHypothesisClaimCheck::getEventId).collect(
            Collectors.toSet());

    // Create EventLocationControlParameters to override default parameters
    EventLocationControlParameters overrideParameters = EventLocationControlParameters.create(
        PluginInfo.from("FAKE FAKE FAKE", "ONE DOT ZERO"),
        EventLocationDefinitionGeigers.create(
            100,
            0.01,
            0.95,
            "ak135",
            true,
            ScalingFactorType.CONFIDENCE,
            4,
            0.01,
            4,
            2,
            true,
            0.01,
            10.0,
            0.01,
            0.01,
            1.0e5,
            0.1,
            1.0,
            4,
            List.of(LocationRestraint.from(
                RestraintType.UNRESTRAINED,
                null,
                RestraintType.UNRESTRAINED,
                null,
                DepthRestraintType.UNRESTRAINED,
                null,
                RestraintType.UNRESTRAINED,
                null))
        )

    );

    given(this.mockOsdGateway.retrieveEvents(eventUuids)).willReturn(Set.of(TestFixtures.event));

    given(this.mockOsdGateway.storeOrUpdateEvents(Mockito.anyCollection()))
        .willReturn(Set.of(TestFixtures.event.getId()));

    given(this.mockOsdGateway.retrieveSignalDetectionHypotheses(anyList()))
        .willReturn(TestFixtures.signalDetectionHypotheses);

    given(this.mockOsdGateway.retrieveSignalDetections(anyList()))
        .willReturn(TestFixtures.signalDetections);

    given(this.mockOsdGateway.retrieveStation(any(UUID.class)))
        .willReturn(Optional.of(TestFixtures.stations.get(0)));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocationControlParameters"),
        this.parameters);

    // Mock locate retuns a LocationSolution
    given(this.mockEventLocatorPlugin
        .locate(any(), eq(TestFixtures.signalDetectionHypotheses), eq(TestFixtures.stations),
            eq(this.parameters.getEventLocationDefinition().get())))
        .willReturn(List.of(TestFixtures.locationSolution));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocatorPlugin"),
        this.mockEventLocatorPlugin);

    // Mock that plugin registry returns the overridden plugin

    PluginRegistry mockPluginRegistry = Mockito.mock(PluginRegistry.class);

    given(mockPluginRegistry
        .lookup(overrideParameters.getPluginInfo().get(), EventLocatorPlugin.class))
        .willReturn(Optional.of(this.mockEventLocatorPlugin));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("pluginRegistry"), mockPluginRegistry);

    Collection<EventHypothesisClaimCheck> newEventHypothesisClaimChecks = eventLocationControl
        .locate(this.eventHypothesisClaimChecks, overrideParameters);

    verify(this.mockOsdGateway).retrieveEvents(eventUuids);

    Assertions.assertEquals(this.eventHypothesisClaimChecks.iterator().next().getEventId(),
        newEventHypothesisClaimChecks.iterator().next().getEventId());

    Assertions
        .assertNotEquals(this.eventHypothesisClaimChecks.iterator().next().getEventHypothesisId(),
            newEventHypothesisClaimChecks.iterator().next().getEventHypothesisId());
  }

  @Test
  void testLocateOverrideParametersNoDefinition()
      throws UnirestException, IOException, NoSuchFieldException, TooManyRestraintsException {

    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    Set<UUID> eventUuids = this.eventHypothesisClaimChecks.stream()
        .map(EventHypothesisClaimCheck::getEventId).collect(
            Collectors.toSet());

    // Create EventLocationControlParameters to override default parameters
    EventLocationControlParameters overrideParametersNoDefinition = EventLocationControlParameters
        .create(
            PluginInfo.from("FAKE FAKE FAKE", "ONE DOT ZERO")
        );

    EventLocationControlParameters overrideParameters = EventLocationControlParameters.create(
        PluginInfo.from("FAKE FAKE FAKE", "ONE DOT ZERO"),
        EventLocationDefinitionGeigers.create(
            100,
            0.01,
            0.95,
            "ak135",
            true,
            ScalingFactorType.CONFIDENCE,
            4,
            0.01,
            4,
            2,
            true,
            0.01,
            10.0,
            0.01,
            0.01,
            1.0e5,
            0.1,
            1.0,
            4,
            List.of(LocationRestraint.from(
                RestraintType.UNRESTRAINED,
                null,
                RestraintType.UNRESTRAINED,
                null,
                DepthRestraintType.UNRESTRAINED,
                null,
                RestraintType.UNRESTRAINED,
                null))
        )
    );

    EventLocationConfiguration mockEventLocationConfiguration = Mockito
        .mock(EventLocationConfiguration.class);

    given(mockEventLocationConfiguration
        .getParametersForPlugin(overrideParametersNoDefinition.getPluginInfo().get()))
        .willReturn(overrideParameters);

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocationConfiguration"),
        mockEventLocationConfiguration);

    given(this.mockOsdGateway.retrieveEvents(eventUuids)).willReturn(Set.of(TestFixtures.event));

    given(this.mockOsdGateway.storeOrUpdateEvents(Mockito.anyCollection()))
        .willReturn(Set.of(TestFixtures.event.getId()));

    given(this.mockOsdGateway.retrieveSignalDetectionHypotheses(anyList()))
        .willReturn(TestFixtures.signalDetectionHypotheses);

    given(this.mockOsdGateway.retrieveSignalDetections(anyList()))
        .willReturn(TestFixtures.signalDetections);

    given(this.mockOsdGateway.retrieveStation(any(UUID.class)))
        .willReturn(Optional.of(TestFixtures.stations.get(0)));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocationControlParameters"),
        this.parameters);

    // Mock locate retuns a LocationSolution
    given(this.mockEventLocatorPlugin
        .locate(any(), eq(TestFixtures.signalDetectionHypotheses), eq(TestFixtures.stations),
            eq(this.parameters.getEventLocationDefinition().get())))
        .willReturn(List.of(TestFixtures.locationSolution));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocatorPlugin"),
        this.mockEventLocatorPlugin);

    // Mock that plugin registry returns the overridden plugin

    PluginRegistry mockPluginRegistry = Mockito.mock(PluginRegistry.class);

    given(mockPluginRegistry
        .lookup(overrideParametersNoDefinition.getPluginInfo().get(), EventLocatorPlugin.class))
        .willReturn(Optional.of(this.mockEventLocatorPlugin));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("pluginRegistry"), mockPluginRegistry);

    Collection<EventHypothesisClaimCheck> newEventHypothesisClaimChecks = eventLocationControl
        .locate(this.eventHypothesisClaimChecks, overrideParametersNoDefinition);

    verify(this.mockOsdGateway).retrieveEvents(eventUuids);

    Assertions.assertEquals(this.eventHypothesisClaimChecks.iterator().next().getEventId(),
        newEventHypothesisClaimChecks.iterator().next().getEventId());

    Assertions
        .assertNotEquals(this.eventHypothesisClaimChecks.iterator().next().getEventHypothesisId(),
            newEventHypothesisClaimChecks.iterator().next().getEventHypothesisId());
  }

  @Test
  void testLocateOverrideParametersOsdGatewayThrowsUnirestException()
      throws UnirestException, IOException {

    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    Set<UUID> eventUuids = this.eventHypothesisClaimChecks.stream()
        .map(EventHypothesisClaimCheck::getEventId).collect(
            Collectors.toSet());

    given(mockOsdGateway.retrieveEvents(eventUuids)).willThrow(UnirestException.class);

    Assertions.assertThrows(IllegalStateException.class, () ->
        eventLocationControl.locate(this.eventHypothesisClaimChecks, this.parameters)
    );

    verify(this.mockOsdGateway).retrieveEvents(eventUuids);
  }

  @Test
  void testLocateOverrideParametersOsdGatewayThrowsIOException()
      throws UnirestException, IOException {

    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    Set<UUID> eventUuids = this.eventHypothesisClaimChecks.stream()
        .map(EventHypothesisClaimCheck::getEventId).collect(
            Collectors.toSet());

    given(mockOsdGateway.retrieveEvents(eventUuids)).willThrow(IOException.class);

    Assertions.assertThrows(IllegalStateException.class, () ->
        eventLocationControl.locate(this.eventHypothesisClaimChecks, this.parameters)
    );

    verify(this.mockOsdGateway).retrieveEvents(eventUuids);
  }

  // * * * * * * * * * * * * *
  // test locateInteractive()
  // * * * * * * * * * * * * *

  @Test
  void testLocateInteractive()
      throws UnirestException, IOException, NoSuchFieldException, TooManyRestraintsException {

    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    given(this.mockOsdGateway.retrieveSignalDetectionHypotheses(anyList()))
        .willReturn(TestFixtures.signalDetectionHypotheses);

    given(this.mockOsdGateway.retrieveSignalDetections(anyList()))
        .willReturn(TestFixtures.signalDetections);

    given(this.mockOsdGateway.retrieveStation(any(UUID.class)))
        .willReturn(Optional.of(TestFixtures.stations.get(0)));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocationControlParameters"),
        this.parameters);

    // Mock locate retuns a LocationSolution
    given(this.mockEventLocatorPlugin
        .locate(any(), eq(TestFixtures.signalDetections.stream()
                .flatMap(sd -> sd.getSignalDetectionHypotheses().stream()).collect(
                Collectors.toList())), eq(TestFixtures.stations),
            eq(this.parameters.getEventLocationDefinition().get())))
        .willReturn(List.of(TestFixtures.locationSolution));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocatorPlugin"),
        this.mockEventLocatorPlugin);

    Map<UUID, Set<LocationSolution>> locationSolutionsMap = eventLocationControl
        .locateInteractive(List.of(TestFixtures.event.getHypotheses().iterator().next()),
            TestFixtures.signalDetections);

    Map<UUID, Collection<LocationSolution>> expectedLocationSolutionsMap = Map.ofEntries(
        Map.entry(TestFixtures.event.getHypotheses().iterator().next().getId(),
            Set.of(TestFixtures.locationSolution))
    );

    Assertions.assertEquals(expectedLocationSolutionsMap, locationSolutionsMap);
  }

  @Test
  void testLocateInteractiveOverrideParameters()
      throws UnirestException, IOException, NoSuchFieldException, TooManyRestraintsException {

    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    PluginInfo pluginInfo = PluginInfo.from("FAKE FAKE FAKE", "ONE DOT ZERO");
    EventLocationDefinition eventLocationDefinition = EventLocationDefinitionGeigers.create(
        100,
        0.01,
        0.95,
        "ak135",
        true,
        ScalingFactorType.CONFIDENCE,
        4,
        0.01,
        4,
        2,
        true,
        0.01,
        10.0,
        0.01,
        0.01,
        1.0e5,
        0.1,
        1.0,
        4,
        List.of(LocationRestraint.from(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null)));

    // Create EventLocationControlParameters to override default parameters
    EventLocationControlParameters overrideParameters = EventLocationControlParameters.create(
        pluginInfo,
        eventLocationDefinition
    );

    // Mock that the osd gateway client gives us the right results

    given(this.mockOsdGateway.retrieveSignalDetectionHypotheses(anyList()))
        .willReturn(TestFixtures.signalDetectionHypotheses);

    given(this.mockOsdGateway.retrieveSignalDetections(anyList()))
        .willReturn(TestFixtures.signalDetections);

    given(this.mockOsdGateway.retrieveStation(any(UUID.class)))
        .willReturn(Optional.of(TestFixtures.stations.get(0)));

    // Mock that the locator plugin returns a LocationSolution

    given(this.mockEventLocatorPlugin
        .locate(any(), eq(TestFixtures.signalDetections.stream()
                .flatMap(sd -> sd.getSignalDetectionHypotheses().stream()).collect(
                Collectors.toList())), eq(TestFixtures.stations),
            eq(eventLocationDefinition)))
        .willReturn(List.of(TestFixtures.locationSolution));

    // Mock that plugin registry returns the overridden plugin

    PluginRegistry mockPluginRegistry = Mockito.mock(PluginRegistry.class);

    given(mockPluginRegistry
        .lookup(pluginInfo, EventLocatorPlugin.class))
        .willReturn(Optional.of(this.mockEventLocatorPlugin));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("pluginRegistry"), mockPluginRegistry);

    // Extract results

    Map<UUID, Set<LocationSolution>> locationSolutionsMap = eventLocationControl
        .locateInteractive(List.of(TestFixtures.event.getHypotheses().iterator().next()),
            TestFixtures.signalDetections, overrideParameters);

    // Create expected results

    Map<UUID, Collection<LocationSolution>> expectedLocationSolutionsMap = Map.ofEntries(
        Map.entry(TestFixtures.event.getHypotheses().iterator().next().getId(),
            Set.of(TestFixtures.locationSolution))
    );

    verify(mockPluginRegistry).lookup(pluginInfo, EventLocatorPlugin.class);

    Assertions.assertEquals(expectedLocationSolutionsMap, locationSolutionsMap);
  }

  @Test
  void testLocateInteractiveOverrideParametersNoDefinition()
      throws UnirestException, IOException, NoSuchFieldException, TooManyRestraintsException {

    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    // Create EventLocationControlParameters to override default parameters
    EventLocationControlParameters overrideParametersNoDefinition = EventLocationControlParameters
        .create(
            PluginInfo.from("FAKE FAKE FAKE", "ONE DOT ZERO")
        );

    EventLocationControlParameters overrideParameters = EventLocationControlParameters.create(
        PluginInfo.from("FAKE FAKE FAKE", "ONE DOT ZERO"),
        EventLocationDefinitionGeigers.create(
            100,
            0.01,
            0.95,
            "ak135",
            true,
            ScalingFactorType.CONFIDENCE,
            4,
            0.01,
            4,
            2,
            true,
            0.01,
            10.0,
            0.01,
            0.01,
            1.0e5,
            0.1,
            1.0,
            4,
            List.of(LocationRestraint.from(
                RestraintType.UNRESTRAINED,
                null,
                RestraintType.UNRESTRAINED,
                null,
                DepthRestraintType.UNRESTRAINED,
                null,
                RestraintType.UNRESTRAINED,
                null))
        )
    );

    EventLocationConfiguration mockEventLocationConfiguration = Mockito
        .mock(EventLocationConfiguration.class);

    given(mockEventLocationConfiguration
        .getParametersForPlugin(overrideParametersNoDefinition.getPluginInfo().get()))
        .willReturn(overrideParameters);

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocationConfiguration"),
        mockEventLocationConfiguration);

    // Mock that the osd gateway client gives us the right results

    given(this.mockOsdGateway.retrieveSignalDetectionHypotheses(anyList()))
        .willReturn(TestFixtures.signalDetectionHypotheses);

    given(this.mockOsdGateway.retrieveSignalDetections(anyList()))
        .willReturn(TestFixtures.signalDetections);

    given(this.mockOsdGateway.retrieveStation(any(UUID.class)))
        .willReturn(Optional.of(TestFixtures.stations.get(0)));

    // Mock that the locator plugin returns a LocationSolution

    given(this.mockEventLocatorPlugin
        .locate(any(), eq(TestFixtures.signalDetections.stream()
                .flatMap(sd -> sd.getSignalDetectionHypotheses().stream()).collect(
                Collectors.toList())), eq(TestFixtures.stations),
            eq(overrideParameters.getEventLocationDefinition().get())))
        .willReturn(List.of(TestFixtures.locationSolution));

    // Mock that plugin registry returns the overridden plugin

    PluginRegistry mockPluginRegistry = Mockito.mock(PluginRegistry.class);

    given(mockPluginRegistry
        .lookup(overrideParameters.getPluginInfo().get(), EventLocatorPlugin.class))
        .willReturn(Optional.of(this.mockEventLocatorPlugin));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("pluginRegistry"), mockPluginRegistry);

    // Extract results

    Map<UUID, Set<LocationSolution>> locationSolutionsMap = eventLocationControl
        .locateInteractive(List.of(TestFixtures.event.getHypotheses().iterator().next()),
            TestFixtures.signalDetections, overrideParameters);

    // Create expected results

    Map<UUID, Collection<LocationSolution>> expectedLocationSolutionsMap = Map.ofEntries(
        Map.entry(TestFixtures.event.getHypotheses().iterator().next().getId(),
            Set.of(TestFixtures.locationSolution))
    );

    Assertions.assertEquals(expectedLocationSolutionsMap, locationSolutionsMap);
  }

  // * * * * * * * * * * * * *
  // test locateValidation()
  // * * * * * * * * * * * * *

  @Test
  void testLocateValidation()
      throws UnirestException, IOException, NoSuchFieldException, TooManyRestraintsException {

    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    // Mock locate retuns a LocationSolution
    given(this.mockEventLocatorPlugin
        .locate(
            TestFixtures.event.getHypotheses().iterator().next().getPreferredLocationSolution(),
            TestFixtures.signalDetectionHypotheses,
            TestFixtures.stations,
            this.parameters.getEventLocationDefinition().orElseThrow(IllegalStateException::new)
        )
    ).willReturn(List.of(TestFixtures.locationSolution));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocationControlParameters"),
        this.parameters);

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocatorPlugin"),
        this.mockEventLocatorPlugin);

    LocateValidationInput validationInput = Mockito.mock(LocateValidationInput.class);

    given(validationInput.getEventHypothesis())
        .willReturn(TestFixtures.event.getHypotheses().iterator().next());

    given(validationInput.getReferenceStations()).willReturn(TestFixtures.stations);

    given(validationInput.getSignalDetectionHypotheses())
        .willReturn(TestFixtures.signalDetectionHypotheses);

    Map<UUID, Set<LocationSolution>> locationSolutionsMap = eventLocationControl
        .locateValidation(validationInput);

    Map<UUID, Collection<LocationSolution>> expectedLocationSolutionsMap = Map.ofEntries(
        Map.entry(TestFixtures.event.getHypotheses().iterator().next().getId(),
            Set.of(TestFixtures.locationSolution))
    );

    Assertions.assertEquals(expectedLocationSolutionsMap, locationSolutionsMap);
  }

  @Test
  void testLocateValidationOverrideParameters()
      throws UnirestException, IOException, NoSuchFieldException, TooManyRestraintsException {

    EventLocationControl eventLocationControl = EventLocationControl.create(this.mockOsdGateway);

    // Mock locate retuns a LocationSolution
    given(this.mockEventLocatorPlugin
        .locate(
            TestFixtures.event.getHypotheses().iterator().next().getPreferredLocationSolution(),
            TestFixtures.signalDetectionHypotheses,
            TestFixtures.stations,
            this.parameters.getEventLocationDefinition().orElseThrow(IllegalStateException::new)
        )
    ).willReturn(List.of(TestFixtures.locationSolution));

    PluginInfo pluginInfo = PluginInfo.from("FAKE FAKE FAKE", "ONE DOT ZERO");
    EventLocationDefinition eventLocationDefinition = EventLocationDefinitionGeigers.create(
        100,
        0.01,
        0.95,
        "ak135",
        true,
        ScalingFactorType.CONFIDENCE,
        4,
        0.01,
        4,
        2,
        true,
        0.01,
        10.0,
        0.01,
        0.01,
        1.0e5,
        0.1,
        1.0,
        4,
        List.of(LocationRestraint.from(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null))
    );

    // Create EventLocationControlParameters to override default parameters
    EventLocationControlParameters overrideParameters = EventLocationControlParameters.create(
        pluginInfo,
        eventLocationDefinition
    );

    // Mock that plugin registry returns the overridden plugin

    PluginRegistry mockPluginRegistry = Mockito.mock(PluginRegistry.class);

    given(mockPluginRegistry
        .lookup(pluginInfo, EventLocatorPlugin.class))
        .willReturn(Optional.of(this.mockEventLocatorPlugin));

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("pluginRegistry"), mockPluginRegistry);

    FieldSetter.setField(eventLocationControl,
        eventLocationControl.getClass().getDeclaredField("eventLocatorPlugin"),
        this.mockEventLocatorPlugin);

    LocateValidationInput validationInput = Mockito.mock(LocateValidationInput.class);

    given(validationInput.getEventHypothesis())
        .willReturn(TestFixtures.event.getHypotheses().iterator().next());

    given(validationInput.getReferenceStations()).willReturn(TestFixtures.stations);

    given(validationInput.getSignalDetectionHypotheses())
        .willReturn(TestFixtures.signalDetectionHypotheses);

    Map<UUID, Set<LocationSolution>> locationSolutionsMap = eventLocationControl
        .locateValidation(validationInput, overrideParameters);

    Map<UUID, Set<LocationSolution>> expectedLocationSolutionsMap = Map.ofEntries(
        Map.entry(TestFixtures.event.getHypotheses().iterator().next().getId(),
            Set.of(TestFixtures.locationSolution))
    );

    Assertions.assertEquals(expectedLocationSolutionsMap, locationSolutionsMap);
  }
}
