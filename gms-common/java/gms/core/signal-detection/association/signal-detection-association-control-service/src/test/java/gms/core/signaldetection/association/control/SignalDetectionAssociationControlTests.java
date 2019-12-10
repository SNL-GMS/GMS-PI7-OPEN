package gms.core.signaldetection.association.control;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import gms.core.signaldetection.association.control.gacache.GACache;
import gms.core.signaldetection.association.control.gacache.GACacheJCache;
import gms.core.signaldetection.association.control.service.ArrivalQualityCriteriaConfigurationParameters;
import gms.core.signaldetection.association.control.service.SignalDetectionAssociationControlConfiguration;
import gms.core.signaldetection.association.control.service.SignalDetectionAssociatorConfigurationParameters;
import gms.core.signaldetection.association.control.service.WeightedEventCriteriaConfigurationParameters;
import gms.core.signaldetection.association.plugins.SdhStationAssociation;
import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorDefinition;
import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorPlugin;
import gms.core.signaldetection.association.plugins.implementations.globalgrid.GlobalGridSignalDetectionAssociatorPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.SignalDetectionEventAssociation;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import gms.shared.mechanisms.pluginregistry.PluginRegistry;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SignalDetectionAssociationControlTests {

  private SignalDetectionAssociationControlOsdGateway osdGateway;
  private SignalDetectionAssociationControl signalDetectionAssociationControl;
  private SignalDetectionAssociationControlConfiguration config;
  private PluginRegistry pluginRegistry;
  private GlobalGridSignalDetectionAssociatorPlugin plugin;
  private SignalDetectionAssociatorConfigurationParameters configParameters;
  private SignalDetectionAssociationParameters mockParams;
  private final SignalDetectionAssociatorDefinition mockDefinition = SignalDetectionAssociatorDefinition
      .create(
          5,
          0.65,
          List.of(PhaseType.P),
          List.of(PhaseType.P),
          0.65,
          true,
          0.65,
          0.65,
          true,
      1.0,
      50.0,
      100.0,
      3.5,
          TestFixtures.weightedEventCriteriaCalculationDefinition,
          TestFixtures.arrivalQualityEventCriterionDefinition,
          5);
  private WeightedEventCriteriaConfigurationParameters weightedEventPluginParameters;
  private ArrivalQualityCriteriaConfigurationParameters arrivalQualityCriteriaConfigurationParameters;

  // Manages the GACache used by SignalDetectionAssociationControl
  private CacheManager gaCacheManager;

  private static GACache instantiateGaCache(CacheManager gaCacheManager) {

    // Create configuration for SdhStationAssociation cache
    MutableConfiguration<UUID, SdhStationAssociation> sdhStationAssociationCacheConfig = new MutableConfiguration<>();
    sdhStationAssociationCacheConfig.setStoreByValue(false);

    // Create configuration for EventHypothesis cache
    MutableConfiguration<UUID, EventHypothesis> eventHypothesisCacheConfig = new MutableConfiguration<>();
    eventHypothesisCacheConfig.setStoreByValue(false);

    // Create configuration for ReferenceStation cache
    MutableConfiguration<UUID, ReferenceStation> referenceStationCacheConfig = new MutableConfiguration<>();
    referenceStationCacheConfig.setStoreByValue(false);

    return GACacheJCache.create(
        gaCacheManager,
        sdhStationAssociationCacheConfig,
        eventHypothesisCacheConfig,
        referenceStationCacheConfig
    );
  }

  @BeforeEach
  void setUp() {
    this.osdGateway = mock(SignalDetectionAssociationControlOsdGateway.class);
    this.config = mock(SignalDetectionAssociationControlConfiguration.class);
    this.configParameters = mock(SignalDetectionAssociatorConfigurationParameters.class);
    this.weightedEventPluginParameters = mock(WeightedEventCriteriaConfigurationParameters.class);
    this.arrivalQualityCriteriaConfigurationParameters = mock(
        ArrivalQualityCriteriaConfigurationParameters.class);
    this.plugin = mock(GlobalGridSignalDetectionAssociatorPlugin.class);
    this.pluginRegistry = mock(PluginRegistry.class);
    this.mockParams = SignalDetectionAssociationParameters
        .create(mockDefinition);

    given(this.config.getParams()).willReturn(this.configParameters);
    given(this.config.getWeightedEventPluginParams())
        .willReturn(this.weightedEventPluginParameters);
    given(this.config.getArrivalQualityPluginParams())
        .willReturn(this.arrivalQualityCriteriaConfigurationParameters);
    given(this.configParameters.getPluginName()).willReturn("default-plugin");
    given(this.configParameters.getPluginVersion()).willReturn("1.0.0");

    Mockito.doNothing().when(this.plugin).initialize(any(), any(), any(), any());
    Path mockPath = Paths.get("mockPath");
    Mockito.doReturn(mockPath).when(this.config).getGridModelFilePath();
    given(this.pluginRegistry
        .lookup(PluginInfo.from("default-plugin", "1.0.0"),
            SignalDetectionAssociatorPlugin.class)).willReturn(Optional.of(this.plugin));

    // Get cache manager to use to create GACache in SignalDetectionAssociationControl
    CachingProvider cachingProvider = Caching.getCachingProvider();
    this.gaCacheManager = cachingProvider.getCacheManager();

    this.signalDetectionAssociationControl =
        SignalDetectionAssociationControl
            .create(pluginRegistry, osdGateway, config, instantiateGaCache(this.gaCacheManager));
  }

  @AfterEach
  void teardown() {

    // close cache
    this.gaCacheManager.close();
  }

  @Test
  void testAssociateToLocationInteractive() {

    List<SignalDetectionHypothesis> signalDetectionHypotheses = TestFixtures.signalDetectionHypotheses;
    EventLocation eventLocation = TestFixtures.eventLocation;

    Event event = this.signalDetectionAssociationControl
        .associateToLocationInteractive(signalDetectionHypotheses, eventLocation);

    // Collect original signal detection hypothesis ids
    Set<UUID> signalDetectionHypothesisIds = signalDetectionHypotheses.stream()
        .map(SignalDetectionHypothesis::getId).collect(
            Collectors.toSet());

    // Collect the signal detection hypothesis ids we made associations to
    Set<UUID> associatedSignalDetectionHypothesisIds = event.getHypotheses().stream()
        .flatMap(h -> h.getAssociations().stream())
        .map(SignalDetectionEventAssociation::getSignalDetectionHypothesisId)
        .collect(
            Collectors.toSet());

    // Assert that the signal detection hypotheses we requested to be associated are the signal
    //   detection hypotheses that actually got associated
    Assertions.assertEquals(signalDetectionHypothesisIds, associatedSignalDetectionHypothesisIds);

    // Assert that the associations were made to the correct event hypothesis id
    event.getHypotheses().forEach(h ->
        h.getAssociations().forEach(a ->
            Assertions.assertEquals(h.getId(), a.getEventHypothesisId())
        )
    );

    // Collect original feature measurement ids
    Set<UUID> featureMeasurementIds = signalDetectionHypotheses
        .stream()
        .flatMap(sdh -> sdh.getFeatureMeasurements().stream())
        .map(FeatureMeasurement::getId)
        .collect(Collectors.toSet());

    // Collect feature measurement ids from location behaviors created by the association operation
    Set<UUID> locationBehaviorFeatureMeasurementIds = event.getHypotheses()
        .stream()
        .flatMap(e -> e.getLocationSolutions().stream())
        .flatMap(ls -> ls.getLocationBehaviors().stream())
        .map(LocationBehavior::getFeatureMeasurementId)
        .collect(Collectors.toSet());

    // Assert that the original feature measurement ids were the ids present in the location behaviors
    //   created by the association operation
    Assertions.assertEquals(featureMeasurementIds, locationBehaviorFeatureMeasurementIds);
  }

  @Test
  void testAssociateToLocation() throws IOException, UnirestException {
    EventLocation eventLocation = TestFixtures.eventLocation;
    List<SignalDetectionHypothesisClaimCheck> signalDetectionHypothesisClaimChecks
        = TestFixtures.signalDetectionHypothesisClaimChecks;

    given(this.osdGateway
        .retrieveSignalDetectionHypotheses(signalDetectionHypothesisClaimChecks.stream()
            .map(SignalDetectionHypothesisClaimCheck::getSignalDetectionId).collect(
                Collectors.toList())))
        .willReturn(TestFixtures.signalDetectionHypotheses);

    given(this.osdGateway.storeOrUpdateEvents(anyList()))
        .willReturn(List.of(TestFixtures.associatedEvent.getId()));

    EventHypothesisClaimCheck eventHypothesisClaimCheck = signalDetectionAssociationControl
        .associateToLocation(
            signalDetectionHypothesisClaimChecks,
            eventLocation);

    verify(this.osdGateway).retrieveSignalDetectionHypotheses(
        signalDetectionHypothesisClaimChecks.stream()
            .map(SignalDetectionHypothesisClaimCheck::getSignalDetectionId).collect(
            Collectors.toList()));

    verify(this.osdGateway).storeOrUpdateEvents(anyList());

    Assertions.assertNotNull(eventHypothesisClaimCheck.getEventHypothesisId());
    Assertions.assertNotNull(eventHypothesisClaimCheck.getEventId());
  }

  @Test
  void testAssociateToEventHypothesisInteractive() throws IOException {
    EventHypothesis eventHypothesis = TestFixtures.unassociatedEventHypothesis;
    List<SignalDetectionHypothesis> signalDetectionHypotheses = TestFixtures.signalDetectionHypotheses;

    EventHypothesis newEventHypothesis = this.signalDetectionAssociationControl
        .associateToEventHypothesisInteractive(signalDetectionHypotheses, eventHypothesis);

    Set<SignalDetectionEventAssociation> signalDetectionEventAssociations = newEventHypothesis
        .getAssociations();

    Set<UUID> associatedSignalDetectionHypothesisIds = signalDetectionEventAssociations.stream()
        .map(SignalDetectionEventAssociation::getSignalDetectionHypothesisId).collect(
            Collectors.toSet());

    Set<UUID> signalDetectionHypothesisIds = signalDetectionHypotheses.stream()
        .map(SignalDetectionHypothesis::getId).collect(
            Collectors.toSet());

    Assertions.assertEquals(signalDetectionHypothesisIds, associatedSignalDetectionHypothesisIds);

    Set<UUID> locationBehaviorMeasurementIds = new HashSet<>();
    signalDetectionHypotheses.forEach(sdh ->
        sdh.getFeatureMeasurements().forEach(fm ->
            locationBehaviorMeasurementIds.add(fm.getId())));

    eventHypothesis.getLocationSolutions().forEach(ls ->
        ls.getLocationBehaviors().forEach(lb ->
            locationBehaviorMeasurementIds.add(lb.getFeatureMeasurementId())
        )
    );

    Set<UUID> associatedLocationBehaviorMeasurementIds = new HashSet<>();
    newEventHypothesis.getLocationSolutions().forEach(ls ->
        ls.getLocationBehaviors().forEach(lb ->
            associatedLocationBehaviorMeasurementIds.add(lb.getFeatureMeasurementId())
        )
    );

    Assertions
        .assertEquals(locationBehaviorMeasurementIds, associatedLocationBehaviorMeasurementIds);
  }

  @Test
  void testAssociateToEventHypothesisNullSignalDetectionHypotheses() throws IOException {
    ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    EventHypothesis eventHypothesis = TestFixtures.unassociatedEventHypothesis;

    Assertions.assertThrows(NullPointerException.class, () ->
        this.signalDetectionAssociationControl
            .associateToEventHypothesisInteractive(null, eventHypothesis)
    );
  }

  @Test
  void testAssociateToEventHypothesisNullEventHypothesis() throws IOException {
    ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    List<SignalDetectionHypothesis> signalDetectionHypotheses = TestFixtures.signalDetectionHypotheses;

    Assertions.assertThrows(NullPointerException.class, () ->
        this.signalDetectionAssociationControl
            .associateToEventHypothesisInteractive(signalDetectionHypotheses, null)
    );
  }

  @Test
  void testAssociateNullSignalDetectionHypothesisListAndUserParametersThrowsError()
      throws IOException {
    NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
        () -> this.signalDetectionAssociationControl
            .associate(null, mockParams));
    Assertions.assertEquals(
        "SignalDetectionAssociationControl::associate() requires non-null \"signalDetectionHypotheses\" parameter",
        exception.getMessage());
  }

  @Test
  void testAssociateSignalDetectionHypothesisListAndNullUserParametersThrowsError()
      throws IOException {
    NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
        () -> this.signalDetectionAssociationControl
            .associate(TestFixtures.signalDetectionHypothesesDescriptors, null));
    Assertions.assertEquals(
        "SignalDetectionAssociationControl::associate() requires non-null \"parameters\" parameter",
        exception.getMessage());
  }

  @Test
  void testAssociateSignalDetectionHypothesisListAndUserParametersDoesNotThrowError()
      throws IOException {
    UUID stationUUID = UUID.randomUUID();
    ReferenceStation mockReferenceStation = Mockito.mock(ReferenceStation.class);
    SignalDetectionHypothesisDescriptor mockSdhDescriptor = Mockito
        .mock(SignalDetectionHypothesisDescriptor.class);
    SignalDetectionHypothesis mockSdh = Mockito.mock(SignalDetectionHypothesis.class);

    UUID mockSdhId = UUID.randomUUID();
    Mockito.when(mockSdh.getId()).thenReturn(mockSdhId);

    Set<ReferenceStation> testStations = Set.of(
        mockReferenceStation
    );
    List<SignalDetectionHypothesisDescriptor> mockSdhAssociationList = List.of(
        mockSdhDescriptor
    );

    given(mockReferenceStation.getVersionId()).willReturn(stationUUID);
    given(mockSdhDescriptor.getSignalDetectionHypothesis()).willReturn(mockSdh);
    given(mockSdhDescriptor.getStationId()).willReturn(stationUUID);

    SignalDetectionAssociationParameters mockParams = SignalDetectionAssociationParameters
        .create(mockDefinition);
    Pair<Set<SignalDetectionHypothesis>, Set<EventHypothesis>> mockResults = Pair
        .of(Set.of(), Set.of());
    Path mockPath = Paths.get("mockPath");
    Mockito.doReturn(mockPath).when(this.config).getGridModelFilePath();
    try {
      given(this.osdGateway.retrieveStations(any())).willReturn(testStations);
    } catch (Exception e) {

    }
    Mockito.doNothing().when(this.plugin).initialize(any(), any(), any(), any());
    Mockito.doReturn(mockResults).when(this.plugin).associate(anyList(), any(), any());
    Assertions.assertDoesNotThrow(
        () -> this.signalDetectionAssociationControl
            .associate(mockSdhAssociationList, mockParams));

    Cache<UUID, SdhStationAssociation> sdhStationAssociationCache = this.gaCacheManager
        .getCache("sdhStationAssociationCache");
    SdhStationAssociation cachedSdhStationAssociation = sdhStationAssociationCache.get(mockSdhId);

    Assertions.assertEquals(mockSdhId,
        cachedSdhStationAssociation.getSignalDetectionHypothesis().getId());
  }

  @Test
  void testAssociateNullSignalDetectionHypothesisListThrowsError() throws IOException {
    NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
        () -> this.signalDetectionAssociationControl
            .associate(null));
    Assertions.assertEquals(
        "SignalDetectionAssociationControl::associate() requires non-null \"signalDetectionHypotheses\" parameter",
        exception.getMessage());
  }
}
