package gms.core.signalenhancement.fk.control;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.Ordering;
import gms.core.signalenhancement.fk.FkTestUtility;
import gms.core.signalenhancement.fk.coi.client.CoiRepository;
import gms.core.signalenhancement.fk.control.configuration.FileBasedFkConfiguration;
import gms.core.signalenhancement.fk.control.configuration.FkAttributesParameters;
import gms.core.signalenhancement.fk.control.configuration.FkConfiguration;
import gms.core.signalenhancement.fk.control.configuration.FkSpectraParameters;
import gms.core.signalenhancement.fk.plugin.fkattributes.FkAttributesPlugin;
import gms.core.signalenhancement.fk.plugin.fkspectra.FkSpectraPlugin;
import gms.core.signalenhancement.fk.plugin.util.FkSpectraInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginRegistry;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginRegistry.Entry;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.ProcessingResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.StationProcessingInterval;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.UpdateStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Timeseries;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FkControlTests {

  private FkControl fkControl;

  @Mock
  private CoiRepository mockCoiRepository;

  @Mock
  private PluginRegistry<FkSpectraPlugin> mockFkSpectraPluginRegistry;

  @Mock
  private PluginRegistry<FkAttributesPlugin> mockFkAttributesPluginRegistry;

  @Mock
  private FileBasedFkConfiguration mockFileBasedFkConfiguration;

  @Mock
  private FkConfiguration mockFkConfiguration;

  @Captor
  private ArgumentCaptor<Collection<ChannelSegment<FkSpectra>>> fkSpectraChannelSegmentCaptor;

  @Captor
  private ArgumentCaptor<Collection<SignalDetectionHypothesisDescriptor>> signalDetectionHypothesisDescriptorCaptor;

  @BeforeEach
  void setup() {
    this.fkControl = FkControl.create(mockFkSpectraPluginRegistry,
        mockFkAttributesPluginRegistry, mockCoiRepository, mockFkConfiguration);
  }

  @Test
  void testCreate() {
    assertAll("Testing create",
        () -> assertThrows(NullPointerException.class,
            () -> FkControl.create(null, mockFkAttributesPluginRegistry, mockCoiRepository,
                mockFkConfiguration)),
        () -> assertThrows(NullPointerException.class,
            () -> FkControl.create(mockFkSpectraPluginRegistry, null, mockCoiRepository,
                mockFkConfiguration)),
        () -> assertThrows(NullPointerException.class,
            () -> FkControl
                .create(mockFkSpectraPluginRegistry, mockFkAttributesPluginRegistry, null,
                    mockFkConfiguration)),
        () -> assertThrows(NullPointerException.class,
            () -> FkControl.create(mockFkSpectraPluginRegistry, mockFkAttributesPluginRegistry,
                mockCoiRepository, null)),
        () -> assertNotNull(FkControl
            .create(mockFkSpectraPluginRegistry, mockFkAttributesPluginRegistry,
                mockCoiRepository, mockFkConfiguration)));
  }

  @Test
  void testInitialize() {
    assertAll("Testing execution when not initialized",
        () -> assertThrows(IllegalStateException.class,
            () -> fkControl.measureFkFeatures(FkTestUtility.defaultInputDescriptors())),
        () -> assertThrows(IllegalStateException.class,
            () -> fkControl.generateFkSpectra(FkTestUtility.defaultSpectraCommand())));
  }

  @Test
  void testExecuteFkAnalysisNullCommand() {
    fkControl.initialize();

    assertThrows(NullPointerException.class,
        () -> fkControl.generateFkSpectra(null));
  }

  @Test
  void testMeasureFkFeatures() {
    //mocking plugins for initialization
    RegistrationInfo spectraPluginInfo = RegistrationInfo.create("mock1", 1, 0, 0);
    Map<RegistrationInfo, FkSpectraPlugin> spectraPluginMap = givenServiceIsConfigured(
        FkSpectraPlugin.class, mockFkSpectraPluginRegistry, spectraPluginInfo);

    RegistrationInfo attributesPluginInfo = RegistrationInfo.create("mockFkAttributesPlugin",
        1, 0, 0);
    Map<RegistrationInfo, FkAttributesPlugin> attributesPluginMap = givenServiceIsConfigured(
        FkAttributesPlugin.class, mockFkAttributesPluginRegistry, attributesPluginInfo);

    given(mockCoiRepository.getConfiguration()).willReturn(mockFileBasedFkConfiguration);

    fkControl.initialize();

    //mocking signal detections and waveforms returned from the coi repository
    UUID stationId = UUID.randomUUID();
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(30);
    List<UUID> processingIds = Stream.generate(UUID::randomUUID)
        .limit(10)
        .sorted()
        .collect(Collectors.toList());

    List<SignalDetectionHypothesis> hypotheses = processingIds.stream()
        .map(FkTestUtility::randomSignalDetectionHypothesis)
        .collect(Collectors.toList());

    Set<UUID> channelSegmentIds = hypotheses.stream()
        .flatMap(hypothesis -> hypothesis.getFeatureMeasurements().stream())
        .map(FeatureMeasurement::getChannelSegmentId)
        .collect(Collectors.toSet());

    List<ChannelSegment<Waveform>> channelSegments = givenWaveformsAreAvailable(mockCoiRepository,
        startTime,
        endTime,
        channelSegmentIds);

    List<UUID> channelIds = channelSegments.stream()
        .map(ChannelSegment::getChannelId)
        .collect(Collectors.toList());

    //mocking configuration
    FkSpectraDefinition definition = FkTestUtility.defaultSpectraDefinition(Duration.ofSeconds(10),
        Duration.ofSeconds(30),
        channelIds);

    UUID outputChannelId = UUID.randomUUID();
    FkSpectraParameters parameters = FkSpectraParameters.from(spectraPluginInfo.getName(),
        definition,
        outputChannelId);

    given(mockFkConfiguration.getFkSpectraParameters(eq(stationId)))
        .willReturn(parameters);

    List<FkAttributesParameters> fkAttributesParameters = FkTestUtility
        .fkAttributesParameters(definition);

    given(mockFkConfiguration.getFkAttributesParameters(eq(stationId)))
        .willReturn(fkAttributesParameters);

    //mocking plugin responses
    // TODO: does this need to be the window start?
    FkSpectra defaultSpectra = FkTestUtility.defaultSpectra(FkTestUtility.ARRIVAL_TIME, 1.0);
    FkSpectrum defaultSpectrum = defaultSpectra.getValues().get(0);
    FkSpectraInfo defaultInfo = FkTestUtility.spectraInfo(definition);

    given(spectraPluginMap.get(spectraPluginInfo).generateFk(channelSegments, definition))
        .willReturn(List.of(defaultSpectra));

    given(attributesPluginMap.get(attributesPluginInfo)
        .generateFkAttributes(defaultInfo, defaultSpectrum))
        .willReturn(FkTestUtility.defaultAttributes());

    List<SignalDetectionHypothesisDescriptor> descriptors = hypotheses.stream()
        .map(sdh -> SignalDetectionHypothesisDescriptor.from(sdh, stationId)).collect(
            Collectors.toList());

    //mocking coi storage
    given(mockCoiRepository.storeSignalDetectionHypotheses(any())).willReturn(
        descriptors.stream()
            .collect(Collectors.toMap(Function.identity(), id -> UpdateStatus.UPDATED)));

    //when FkControl executes
    ProcessingResponse<SignalDetectionHypothesisDescriptor> actual = fkControl
        .measureFkFeatures(descriptors);

    then(mockCoiRepository).should(times(10))
        .storeFkSpectras(fkSpectraChannelSegmentCaptor.capture(), any());
    then(mockCoiRepository).should()
        .storeSignalDetectionHypotheses(signalDetectionHypothesisDescriptorCaptor.capture());

    //channel segment/fk spectra assertions
    Collection<ChannelSegment<FkSpectra>> actualChannelSegments = fkSpectraChannelSegmentCaptor
        .getAllValues().stream().flatMap(Collection::stream).collect(Collectors.toList());

    //one segment per hypothesis
    assertEquals(processingIds.size(), actualChannelSegments.size());

    //one spectra per segment
    actualChannelSegments.forEach(cs -> assertEquals(1, cs.getTimeseries().size()));

    //plugin generates same segment each time
    actualChannelSegments.forEach(cs -> assertEquals(defaultSpectra, cs.getTimeseries().get(0)));

    //hypothesis assertions
    Collection<SignalDetectionHypothesisDescriptor> actualDescriptors = signalDetectionHypothesisDescriptorCaptor
        .getValue();

    //one hypothesis per descriptor
    assertEquals(descriptors.size(), actualDescriptors.size());

    //same hypothesis (id)
    assertThat(descriptors.stream().map(d -> d.getSignalDetectionHypothesis().getId()).collect(
        Collectors.toList()),
        hasItems(actual.getUpdated().stream().map(d -> d.getSignalDetectionHypothesis().getId()).toArray()));

    //azimuth assertions
    List<NumericMeasurementValue> azimuthValues = actualDescriptors.stream()
        .map(descriptor -> descriptor.getSignalDetectionHypothesis()
            .getFeatureMeasurement(FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH))
        .flatMap(Optional::stream)
        .map(FeatureMeasurement::getMeasurementValue)
        .collect(Collectors.toList());

    //all have azimuths
    assertEquals(descriptors.size(), azimuthValues.size());

    //all have correct azimuth values (channel segment id definitely different)/the default value
    azimuthValues.forEach(
        av -> assertEquals(FkTestUtility.defaultAzimuthMeasurement().getMeasurementValue(), av));

    //slowness assertions
    List<NumericMeasurementValue> slownessValues = actualDescriptors.stream()
        .map(descriptor -> descriptor.getSignalDetectionHypothesis()
            .getFeatureMeasurement(FeatureMeasurementTypes.SLOWNESS))
        .flatMap(Optional::stream)
        .map(FeatureMeasurement::getMeasurementValue)
        .collect(Collectors.toList());

    //all have slownesses
    assertEquals(descriptors.size(), slownessValues.size());

    //all have correct slowness values (channel segment id definitely different)/the default value
    slownessValues.forEach(
        sv -> assertEquals(FkTestUtility.defaultSlownessMeasurement().getMeasurementValue(), sv));
  }

  @Test
  void testGenerateFkSpectra() {
    //mocking plugins for initialization
    RegistrationInfo spectraPluginInfo = RegistrationInfo.create("mock1", 1, 0, 0);
    Map<RegistrationInfo, FkSpectraPlugin> spectraPluginMap = givenServiceIsConfigured(
        FkSpectraPlugin.class, mockFkSpectraPluginRegistry, spectraPluginInfo);

    RegistrationInfo attributesPluginInfo = RegistrationInfo.create("mockFkAttributesPlugin",
        1, 0, 0);
    Map<RegistrationInfo, FkAttributesPlugin> attributesPluginMap = givenServiceIsConfigured(
        FkAttributesPlugin.class, mockFkAttributesPluginRegistry, attributesPluginInfo);

    given(mockCoiRepository.getConfiguration()).willReturn(mockFileBasedFkConfiguration);

    fkControl.initialize();

    //mocking for execution
    Duration windowLead = Duration.ofSeconds(10);
    Duration windowLength = Duration.ofSeconds(30);

    Instant start = Instant.EPOCH;

    Set<UUID> channelIds = Stream.generate(UUID::randomUUID).limit(10)
        .collect(Collectors.toSet());

    //mocking available waveforms given time and channels
    List<ChannelSegment<Waveform>> waveforms = givenWaveformsAreAvailable(
        mockCoiRepository,
        start.minus(windowLead),
        start.plus(windowLength.minus(windowLead)),
        channelIds);

    FkSpectraDefinition defaultDefinition = FkTestUtility
        .defaultSpectraDefinition(windowLead, windowLength,
            channelIds);

    UUID outputChannelId = new UUID(0, 0);
    FkSpectraParameters parameters = FkSpectraParameters.from(spectraPluginInfo.getName(),
        defaultDefinition,
        outputChannelId);

    FkSpectraCommand defaultCommand = FkTestUtility
        .defaultSpectraCommand(start, channelIds, windowLead, windowLength);

    //mocking return of fk parameters
    given(mockFileBasedFkConfiguration
        .createFkSpectraParameters(defaultCommand, FkControl.getModalSampleRate(waveforms)))
        .willReturn(parameters);

    given(mockFileBasedFkConfiguration
        .createFkAttributesParameters(parameters.getDefinition()))
        .willReturn(FkTestUtility.fkAttributesParameters(parameters.getDefinition()));

    FkSpectra defaultSpectra = FkTestUtility.defaultSpectra(start, 1.0);
    FkSpectrum defaultSpectrum = defaultSpectra.getValues().get(0);
    FkSpectraInfo defaultInfo = FkTestUtility.spectraInfo(defaultCommand,
        defaultDefinition);

    //mocking generating of fkspectra and fk attributes from the mock plugins
    given(spectraPluginMap.get(spectraPluginInfo).generateFk(waveforms, defaultDefinition))
        .willReturn(List.of(defaultSpectra));

    given(attributesPluginMap.get(attributesPluginInfo)
        .generateFkAttributes(defaultInfo, defaultSpectrum))
        .willReturn(FkTestUtility.defaultAttributes());

    //execute spectra command after all mocking
    List<ChannelSegment<FkSpectra>> actualSegments = fkControl.generateFkSpectra(defaultCommand);
    assertEquals(1, actualSegments.size());

    ChannelSegment<FkSpectra> actualSegment = actualSegments.get(0);
    assertEquals(start, actualSegment.getStartTime());
    assertEquals(Timeseries.Type.FK_SPECTRA, actualSegment.getTimeseriesType());
    assertEquals(ChannelSegment.Type.FK_SPECTRA, actualSegment.getType());
    assertEquals(1, actualSegment.getTimeseries().size());

    FkSpectra actualSpectra = actualSegments.get(0).getTimeseries().get(0);
    assertEquals(defaultSpectra, actualSpectra);

    //spectra command should not store the created spectras, only return them
    then(mockCoiRepository).should(never()).storeFkSpectras(anyCollection(), any());
  }

  /**
   * Helper method for creating mock plugins and configuring the plugin registry
   *
   * @param pluginClass       Plugin class to mock
   * @param pluginRegistry    Mock registry to supplement with "given" statements
   * @param registrationInfos Registration information to create plugins for
   * @param <T>               Type of plugin
   * @return Map of registration information to mocked plugins
   */
  private <T extends Plugin> Map<RegistrationInfo, T> givenServiceIsConfigured(
      Class<T> pluginClass,
      PluginRegistry<T> pluginRegistry,
      RegistrationInfo... registrationInfos) {
    Map<RegistrationInfo, T> pluginMap = Arrays.stream(registrationInfos)
        .collect(Collectors.toMap(Function.identity(), ri -> mock(pluginClass)));

    pluginMap.forEach((r, p) -> given(p.getName()).willReturn(r.getName()));
    pluginMap.forEach((r, p) -> given(p.getVersion()).willReturn(r.getVersion()));

    given(pluginRegistry.entrySet()).willReturn(
        pluginMap.entrySet().stream()
            .map(e -> Entry.create(e.getKey(), e.getValue()))
            .collect(Collectors.toSet()));

    pluginMap.forEach((r, p) -> given(pluginRegistry.lookup(r))
        .willReturn(Optional.of(p)));

    return pluginMap;
  }

  private List<ChannelSegment<Waveform>> givenWaveformsAreAvailable(
      CoiRepository coiRepository,
      Instant start, Instant end, Set<UUID> channelIds) {

    List<ChannelSegment<Waveform>> channelSegments = channelIds.stream()
        .map(c -> FkTestUtility.randomWaveform(c, start, end))
        .collect(Collectors.toList());

    given(coiRepository
        .findWaveformsByChannelsAndTime(channelIds, start, end))
        .willReturn(channelSegments);

    return channelSegments;
  }

}
