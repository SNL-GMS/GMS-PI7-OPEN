package gms.core.signaldetection.signaldetectorcontrol.control;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Map.entry;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

import gms.core.signaldetection.onsettimerefinement.OnsetTimeRefinementPlugin;
import gms.core.signaldetection.onsettimeuncertainty.OnsetTimeUncertaintyPlugin;
import gms.core.signaldetection.plugin.SignalDetectorPlugin;
import gms.core.signaldetection.signaldetectorcontrol.TestFixtures;
import gms.core.signaldetection.signaldetectorcontrol.coi.client.CoiClient;
import gms.core.signaldetection.signaldetectorcontrol.configuration.OnsetTimeRefinementParameters;
import gms.core.signaldetection.signaldetectorcontrol.configuration.OnsetTimeUncertaintyParameters;
import gms.core.signaldetection.signaldetectorcontrol.configuration.SignalDetectionParameters;
import gms.core.signaldetection.signaldetectorcontrol.configuration.SignalDetectorConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginRegistry;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SignalDetectorControlTests {

  private static SignalDetectorControl signalDetectorControl;

  @Mock
  private CoiClient mockCoiClient;
  @Mock
  private PluginRegistry<SignalDetectorPlugin> mockSignalDetectorControlPluginRegistry;
  @Mock
  private PluginRegistry<OnsetTimeUncertaintyPlugin> mockOnsetTimeUncertaintyPluginRegistry;
  @Mock
  private PluginRegistry<OnsetTimeRefinementPlugin> mockOnsetTimeRefinementPluginRegistry;
  @Mock
  private SignalDetectorConfiguration mockConfiguration;

  @Captor
  ArgumentCaptor<Collection<SignalDetection>> signalDetectionsCaptor;

  @Captor
  ArgumentCaptor<Collection<ChannelSegment<Waveform>>> channelSegmentCaptor;

  @BeforeEach
  public void setup() {
    signalDetectorControl = SignalDetectorControl
        .create(mockConfiguration,
            mockSignalDetectorControlPluginRegistry,
            mockOnsetTimeUncertaintyPluginRegistry,
            mockOnsetTimeRefinementPluginRegistry,
            mockCoiClient);
  }

  @Test
  void testCreateNullRegistryExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> SignalDetectorControl.create(mockConfiguration,
            null,
            mockOnsetTimeUncertaintyPluginRegistry,
            mockOnsetTimeRefinementPluginRegistry,
            mock(CoiClient.class)));
  }

  @Test
  void testCreateNullOsdGatewayAccessLibraryExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> SignalDetectorControl.create(mockConfiguration,
            new PluginRegistry<>(),
            mockOnsetTimeUncertaintyPluginRegistry,
            mockOnsetTimeRefinementPluginRegistry,
            null));
  }

  @Test
  void testCreateNullConfigurationExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> SignalDetectorControl.create(null,
            new PluginRegistry<>(),
            mockOnsetTimeUncertaintyPluginRegistry,
            mockOnsetTimeRefinementPluginRegistry,
            mockCoiClient));
  }

  @Test
  void testCreate() {
    assertNotNull(
        SignalDetectorControl
            .create(mockConfiguration, new PluginRegistry<>(), new PluginRegistry<>(),
                new PluginRegistry<>(),
                mock(CoiClient.class)));
  }

  @Test
  void testGivenNullClaimCheckCommandThenExecuteShouldThrowNullPointerException() {
    signalDetectorControl.initialize();
    assertThrows(NullPointerException.class,
        () -> signalDetectorControl.execute((ChannelSegmentDescriptor) null));
  }

  @Test
  void testGivenNullStreamingCommandThenExecuteShouldThrowNullPointerException() {
    signalDetectorControl.initialize();
    assertThrows(NullPointerException.class,
        () -> signalDetectorControl.execute((ExecuteStreamingCommand) null));
  }

  @Test
  void testGivenNoInitializationThenExecuteClaimCheckShouldThrowIllegalStateException() {
    assertThrows(IllegalStateException.class,
        () -> signalDetectorControl.execute(mock(ChannelSegmentDescriptor.class)));
  }

  @Test
  void testGivenNoInitializationThenExecuteStreamingShouldThrowIllegalStateException() {
    assertThrows(IllegalStateException.class,
        () -> signalDetectorControl.execute(mock(ExecuteStreamingCommand.class)));
  }

  @Test
  void testGivenNoDataStoredThenExecuteClaimCheckShouldThrowIllegalStateException() {
    Instant start = Instant.EPOCH;
    Instant end = start.plusSeconds(300);
    UUID channelId = UUID.fromString("19253def-7a3b-4e0c-bfec-35905ed76999");

    given(mockCoiClient
        .getChannelSegments(List.of(channelId), start, end))
        .willReturn(emptyList());

    signalDetectorControl.initialize();

    ChannelSegmentDescriptor descriptor = ChannelSegmentDescriptor.from(channelId, start, end);
    assertThrows(IllegalStateException.class,
        () -> signalDetectorControl.execute(descriptor));
  }

  @Test
  void testExecuteClaimCheck() {
    Instant start = Instant.EPOCH;
    Instant end = start.plusSeconds(7864);
    Duration uncertainty1 = Duration.ofNanos(3);
    Duration uncertainty2 = Duration.ofNanos(6);
    UUID channelId = UUID.fromString("4872e829-7a5a-310a-8c59-91ecb3a04376");

    ChannelSegmentDescriptor channelSegmentDescriptor = ChannelSegmentDescriptor.from(
        channelId, start, end);

    Map<RegistrationInfo, SignalDetectorPlugin> signalDetectorPluginMap = givenServiceIsConfigured(
        SignalDetectorPlugin.class,
        mockSignalDetectorControlPluginRegistry,
        RegistrationInfo
            .create("mockDetector1", 1, 0, 0));

    RegistrationInfo onsetTimeUncertaintyRegistrationInfo =
        RegistrationInfo.create("mockUncertaintyPlugin1", 1, 0, 0);
    OnsetTimeUncertaintyPlugin onsetTimeUncertaintyPlugin = mock(OnsetTimeUncertaintyPlugin.class);

    RegistrationInfo onsetTimeRefinementRegistrationInfo =
        RegistrationInfo.create("mockRefinementPlugin1", 1, 0, 0);
    OnsetTimeRefinementPlugin onsetTimeRefinementPlugin = mock(OnsetTimeRefinementPlugin.class);

    given(onsetTimeUncertaintyPlugin.getName())
        .willReturn(onsetTimeUncertaintyRegistrationInfo.getName());
    given(onsetTimeUncertaintyPlugin.getVersion())
        .willReturn(onsetTimeUncertaintyRegistrationInfo.getVersion());

    given(onsetTimeRefinementPlugin.getName())
        .willReturn(onsetTimeRefinementRegistrationInfo.getName());
    given(onsetTimeRefinementPlugin.getVersion())
        .willReturn(onsetTimeRefinementRegistrationInfo.getVersion());

    given(mockOnsetTimeUncertaintyPluginRegistry.lookup(onsetTimeUncertaintyRegistrationInfo))
        .willReturn(Optional.of(onsetTimeUncertaintyPlugin));

    given(mockOnsetTimeRefinementPluginRegistry.lookup(onsetTimeRefinementRegistrationInfo))
        .willReturn(Optional.of(onsetTimeRefinementPlugin));

    ChannelSegment<Waveform> channelSegment = givenDataIsAvailable(start, end, channelId).get(0);

    //mock parameter creation
    given(mockConfiguration
        .getSignalDetectionParameters(UUID.fromString("565ca127-6d78-32ba-bdc9-ce05fc3b8ddf")))
        .willReturn(
            List.of(SignalDetectionParameters.from("mockDetector1", emptyMap())));

    given(mockConfiguration.getOnsetTimeUncertaintyParameters())
        .willReturn(
            OnsetTimeUncertaintyParameters.from("mockUncertaintyPlugin1", emptyMap()));

    given(mockConfiguration.getOnsetTimeRefinementParameters())
        .willReturn(
            OnsetTimeRefinementParameters.from("mockRefinementPlugin1", emptyMap()));

    //mock plugin execution
    signalDetectorPluginMap.values().forEach(p -> given(p.detectSignals(channelSegment, emptyMap()))
        .willReturn(List.of(TestFixtures.ARRIVAL_TIME1, TestFixtures.ARRIVAL_TIME2)));

    willReturn(uncertainty1)
        .given(onsetTimeUncertaintyPlugin)
        .calculateOnsetTimeUncertainty(TestFixtures.WAVEFORM, TestFixtures.ARRIVAL_TIME1,
            emptyMap());
    willReturn(uncertainty2)
        .given(onsetTimeUncertaintyPlugin)
        .calculateOnsetTimeUncertainty(TestFixtures.WAVEFORM, TestFixtures.ARRIVAL_TIME2,
            emptyMap());

    willReturn(uncertainty1)
        .given(onsetTimeUncertaintyPlugin)
        .calculateOnsetTimeUncertainty(TestFixtures.WAVEFORM, TestFixtures.REFINED_ARRIVAL_TIME1,
            emptyMap());
    willReturn(uncertainty2)
        .given(onsetTimeUncertaintyPlugin)
        .calculateOnsetTimeUncertainty(TestFixtures.WAVEFORM, TestFixtures.REFINED_ARRIVAL_TIME2,
            emptyMap());

    willReturn(TestFixtures.REFINED_ARRIVAL_TIME1)
        .given(onsetTimeRefinementPlugin)
        .refineOnsetTime(TestFixtures.WAVEFORM, TestFixtures.ARRIVAL_TIME1, emptyMap());
    willReturn(TestFixtures.REFINED_ARRIVAL_TIME2)
        .given(onsetTimeRefinementPlugin)
        .refineOnsetTime(TestFixtures.WAVEFORM, TestFixtures.ARRIVAL_TIME2, emptyMap());

    signalDetectorControl.initialize();

    Collection<SignalDetectionHypothesisDescriptor> expectedRefinedSdhDescriptors = signalDetectorControl
        .execute(channelSegmentDescriptor);

    then(mockCoiClient).should().storeSignalDetections(signalDetectionsCaptor.capture());
    then(mockCoiClient).should().storeChannelSegments(channelSegmentCaptor.capture());

    Map<Instant, Duration> expectedArrivalTimes = Map
        .ofEntries(entry(TestFixtures.ARRIVAL_TIME1, uncertainty1),
            entry(TestFixtures.ARRIVAL_TIME2, uncertainty2),
            entry(TestFixtures.REFINED_ARRIVAL_TIME1, uncertainty1),
            entry(TestFixtures.REFINED_ARRIVAL_TIME2, uncertainty2));

    validateSignalDetections(signalDetectionsCaptor.getValue(), expectedArrivalTimes, 2);
    assertEquals(channelSegmentCaptor.getValue(), List.of(channelSegment));

    //all arrival feature measurements made on the input channel segment
    List<UUID> channelSegmentIds = signalDetectionsCaptor.getValue().stream()
        .flatMap(sd -> sd.getSignalDetectionHypotheses().stream())
        .flatMap(sdh -> sdh.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).stream())
        .map(FeatureMeasurement::getChannelSegmentId)
        .distinct()
        .collect(Collectors.toList());
    assertEquals(1, channelSegmentIds.size());
    assertTrue(channelSegmentIds.contains(channelSegment.getId()));

    //the uuids we returned should be the refined signal detection hypothesis ids
    assertEquals(2, expectedRefinedSdhDescriptors.size());
    List<SignalDetectionHypothesisDescriptor> actualRefinedSdhDescriptors = signalDetectionsCaptor
        .getValue().stream()
        .map(sd -> SignalDetectionHypothesisDescriptor.from(sd.getSignalDetectionHypotheses()
            .get(sd.getSignalDetectionHypotheses().size() - 1), sd.getStationId()))
        .collect(Collectors.toList());
    assertEquals(expectedRefinedSdhDescriptors,
        actualRefinedSdhDescriptors);
  }

  @Test
  void testExecuteStreaming() {
    Instant start = Instant.EPOCH;
    Instant end = start.plusSeconds(300);

    Duration uncertainty1 = Duration.ofNanos(6);
    Duration uncertainty2 = Duration.ofNanos(10);

    ChannelSegment<Waveform> channelSegment = TestFixtures.randomChannelSegment();

    Map<RegistrationInfo, SignalDetectorPlugin> signalDetectorPluginMap = givenServiceIsConfigured(
        SignalDetectorPlugin.class,
        mockSignalDetectorControlPluginRegistry,
        RegistrationInfo.create("mockDetector1", 1, 0, 0),
        RegistrationInfo.create("mockDetector2", 1, 0, 0));

    OnsetTimeUncertaintyPlugin onsetTimeUncertaintyPlugin = mock(OnsetTimeUncertaintyPlugin.class);
    RegistrationInfo onsetTimeUncertaintyRegistrationInfo =
        RegistrationInfo.create("mockOnsetTimeUncertaintyPlugin", 1, 0, 0);

    RegistrationInfo onsetTimeRefinementRegistrationInfo =
        RegistrationInfo.create("mockOnsetTimeRefinementPlugin", 1, 0, 0);
    OnsetTimeRefinementPlugin onsetTimeRefinementPlugin = mock(OnsetTimeRefinementPlugin.class);

    given(onsetTimeUncertaintyPlugin.getName())
        .willReturn(onsetTimeUncertaintyRegistrationInfo.getName());
    given(onsetTimeUncertaintyPlugin.getVersion())
        .willReturn(onsetTimeUncertaintyRegistrationInfo.getVersion());

    given(onsetTimeRefinementPlugin.getName())
        .willReturn(onsetTimeRefinementRegistrationInfo.getName());
    given(onsetTimeRefinementPlugin.getVersion())
        .willReturn(onsetTimeRefinementRegistrationInfo.getVersion());

    given(mockOnsetTimeUncertaintyPluginRegistry.lookup(onsetTimeUncertaintyRegistrationInfo))
        .willReturn(Optional.of(onsetTimeUncertaintyPlugin));

    given(mockOnsetTimeRefinementPluginRegistry.lookup(onsetTimeRefinementRegistrationInfo))
        .willReturn(Optional.of(onsetTimeRefinementPlugin));

    //mock parameter creation
    given(mockConfiguration.getSignalDetectionParameters(channelSegment.getChannelId()))
        .willReturn(
            List.of(
                SignalDetectionParameters.from("mockDetector1", emptyMap()),
                SignalDetectionParameters.from("mockDetector2", emptyMap())));

    given(mockConfiguration.getOnsetTimeUncertaintyParameters())
        .willReturn(
            OnsetTimeUncertaintyParameters.from("mockOnsetTimeUncertaintyPlugin", emptyMap()));

    given(mockConfiguration.getOnsetTimeRefinementParameters())
        .willReturn(
            OnsetTimeRefinementParameters.from("mockOnsetTimeRefinementPlugin", emptyMap()));

    //mock plugin execution
    Iterator<SignalDetectorPlugin> pluginIterator = signalDetectorPluginMap.values().iterator();
    given(pluginIterator.next().detectSignals(channelSegment, emptyMap()))
        .willReturn(List.of(TestFixtures.ARRIVAL_TIME1));
    willReturn(List.of(TestFixtures.ARRIVAL_TIME2))
        .given(pluginIterator.next()).detectSignals(channelSegment, emptyMap());

    given(onsetTimeUncertaintyPlugin
        .calculateOnsetTimeUncertainty(TestFixtures.WAVEFORM, TestFixtures.ARRIVAL_TIME1,
            emptyMap()))
        .willReturn(uncertainty1);
    willReturn(uncertainty2)
        .given(onsetTimeUncertaintyPlugin)
        .calculateOnsetTimeUncertainty(TestFixtures.WAVEFORM, TestFixtures.ARRIVAL_TIME2,
            emptyMap());

    willReturn(uncertainty1)
        .given(onsetTimeUncertaintyPlugin)
        .calculateOnsetTimeUncertainty(TestFixtures.WAVEFORM, TestFixtures.REFINED_ARRIVAL_TIME1,
            emptyMap());
    willReturn(uncertainty2)
        .given(onsetTimeUncertaintyPlugin)
        .calculateOnsetTimeUncertainty(TestFixtures.WAVEFORM, TestFixtures.REFINED_ARRIVAL_TIME2,
            emptyMap());

    given(onsetTimeRefinementPlugin
        .refineOnsetTime(TestFixtures.WAVEFORM, TestFixtures.ARRIVAL_TIME1, emptyMap()))
        .willReturn(TestFixtures.REFINED_ARRIVAL_TIME1);
    willReturn(TestFixtures.REFINED_ARRIVAL_TIME2)
        .given(onsetTimeRefinementPlugin)
        .refineOnsetTime(TestFixtures.WAVEFORM, TestFixtures.ARRIVAL_TIME2, emptyMap());

    ExecuteStreamingCommand command = ExecuteStreamingCommand.create(
        channelSegment, start, end,
        //TODO: Fix SignalDetectorParameters create() once SignalDetectorParameters is implemented
        //SignalDetectorParameters.create(XXX),
        ProcessingContext.createInteractive(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            StorageVisibility.PUBLIC));

    signalDetectorControl.initialize();

    Collection<SignalDetection> signalDetections = signalDetectorControl
        .execute(command);

    then(mockCoiClient).should().storeSignalDetections(signalDetectionsCaptor.capture());
    then(mockCoiClient).should().storeChannelSegments(channelSegmentCaptor.capture());

    Map<Instant, Duration> expectedArrivalTimes = Map
        .ofEntries(entry(TestFixtures.ARRIVAL_TIME1, uncertainty1),
            entry(TestFixtures.ARRIVAL_TIME2, uncertainty2),
            entry(TestFixtures.REFINED_ARRIVAL_TIME1, uncertainty1),
            entry(TestFixtures.REFINED_ARRIVAL_TIME2, uncertainty2));

    validateSignalDetections(signalDetectionsCaptor.getValue(), expectedArrivalTimes, 2);
    assertThat(channelSegmentCaptor.getValue(), is(List.of(channelSegment)));

    //check all arrival times set appropriate channelsegment id
    List<UUID> actualChannelSegmentIds = signalDetectionsCaptor.getValue().stream()
        .flatMap(sd -> sd.getSignalDetectionHypotheses().stream())
        .flatMap(sdh -> sdh.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).stream())
        .map(FeatureMeasurement::getChannelSegmentId)
        .distinct()
        .collect(Collectors.toList());

    assertThat(actualChannelSegmentIds.size(), is(1));
    assertThat(actualChannelSegmentIds, hasItems(channelSegment.getId()));

    //check that the signal detections returned are the ones that were stored
    assertThat(signalDetections.size(), is(signalDetectionsCaptor.getValue().size()));
    assertThat(signalDetections,
        hasItems(signalDetectionsCaptor.getValue().toArray(new SignalDetection[0])));
  }

  private <T extends Plugin> Map<RegistrationInfo, T> givenServiceIsConfigured(Class<T> pluginClass,
      PluginRegistry<T> pluginRegistry,
      RegistrationInfo... registrationInfos) {
    Map<RegistrationInfo, T> pluginMap = Arrays.stream(registrationInfos)
        .collect(Collectors.toMap(Function.identity(), ri -> mock(pluginClass)));

    pluginMap.forEach((r, p) -> willReturn(Optional.of(p)).given(pluginRegistry).lookup(r));

    return pluginMap;
  }

  private List<ChannelSegment<Waveform>> givenDataIsAvailable(Instant start,
      Instant end, UUID... channelIds) {

    List<ChannelSegment<Waveform>> channelSegments = Arrays.stream(channelIds)
        .map(c -> TestFixtures.randomChannelSegment(c, start))
        .collect(Collectors.toList());

    given(mockCoiClient
        .getChannelSegments(Arrays.asList(channelIds), start, end))
        .willReturn(channelSegments);

    return channelSegments;
  }

  private static void validateSignalDetections(Collection<SignalDetection> actualSignalDetections,
      Map<Instant, Duration> expectedArrivalTimes, int expectedSDHsPerDetection) {

    //we should get back a new signal detection hypothesis per arrival time
    assertThat(
        actualSignalDetections.stream().mapToInt(sd -> sd.getSignalDetectionHypotheses().size())
            .sum(), is(expectedArrivalTimes.entrySet().size()));

    //each signal detection should have exactly as many SignalDetectionHypotheses as expected
    // e.g. 1 for no refinement, 2 for refinement
    actualSignalDetections
        .forEach(sd -> assertThat(sd.getSignalDetectionHypotheses().size(),
            is(expectedSDHsPerDetection)));

    List<SignalDetectionHypothesis> actualHypotheses = actualSignalDetections.stream()
        .flatMap(sd -> sd.getSignalDetectionHypotheses().stream())
        .collect(Collectors.toList());

    //each sdh should have an arrival time
    actualHypotheses.forEach(sdh -> assertThat(
        sdh.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).isPresent(), is(true)));

    List<Instant> actualArrivalTimes = actualHypotheses.stream()
        .flatMap(sdh -> sdh.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).stream())
        .map(f -> f.getMeasurementValue().getValue())
        .collect(Collectors.toList());

    List<Duration> actualUncertainties = actualHypotheses.stream()
        .flatMap(sdh -> sdh.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).stream())
        .map(f -> f.getMeasurementValue().getStandardDeviation())
        .collect(Collectors.toList());

    Instant[] expectedArrivalTimeInstants = expectedArrivalTimes.keySet()
        .toArray(new Instant[0]);
    Duration[] expectedUncertainties = expectedArrivalTimes.values()
        .toArray(new Duration[0]);

    //we should have both arrival times returned from the plugin
    assertThat(actualArrivalTimes, hasItems(expectedArrivalTimeInstants));
    assertThat(actualUncertainties, hasItems(expectedUncertainties));
  }
}
