package gms.core.signalenhancement.waveformfiltering.control;

import static gms.core.signalenhancement.waveformfiltering.TestFixtures.randomWaveform;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import gms.core.signalenhancement.waveformfiltering.coi.CoiRepository;
import gms.core.signalenhancement.waveformfiltering.configuration.FilterConfiguration;
import gms.core.signalenhancement.waveformfiltering.configuration.FilterParameters;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginRegistry;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentStorageResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilterControlTests {

  //convenience function for null assertions
  private static Function<Executable, Executable> assertThrowsNullPointer =
      e -> () -> assertThrows(NullPointerException.class, e);

  //dependency injected mocks
  @Mock
  private FilterConfiguration mockConfig;
  @Mock
  private PluginRegistry<FilterPlugin> mockRegistry;
  @Mock
  private CoiRepository mockRepository;

  //data mocks
  @Mock
  ChannelSegmentDescriptor mockDescriptor;
  @Mock
  FilterPlugin mockPlugin;
  @Mock
  ReferenceChannel mockChannel;
  @Mock
  ChannelSegment<Waveform> mockWaveforms;
  @Mock
  FilterParameters mockParameters;

  @Mock
  ChannelSegment<Waveform> mockFilteredChannelSegment;

  @Captor
  private ArgumentCaptor<List<ChannelSegment<Waveform>>> filteredWaveformsCaptor;

  private FilterControl filterControl;

  @BeforeEach
  public void setUp() {
    this.filterControl = FilterControl.create(mockConfig, mockRegistry, mockRepository);
  }

  @AfterEach
  public void tearDown() {
    this.filterControl = null;
  }

  @Test
  void testConstructNullArguments() {
    Executable nullConfig = assertThrowsNullPointer
        .apply(() -> FilterControl.create(null, mockRegistry, mockRepository));
    Executable nullRegistry = assertThrowsNullPointer
        .apply(() -> FilterControl.create(mockConfig, null, mockRepository));
    Executable nullRepository = assertThrowsNullPointer
        .apply(() -> FilterControl.create(mockConfig, mockRegistry, null));

    assertAll("FilterControl constructor null arguments:",
        nullConfig, nullRegistry, nullRepository);
  }

  @Test
  void testRequestProcessingNullArguments() {
    assertThrows(NullPointerException.class,
        () -> filterControl.requestProcessing(null));

    Executable nullDescriptor = assertThrowsNullPointer
        .apply(() -> filterControl.requestProcessing(null, mockParameters));
    Executable nullParameters = assertThrowsNullPointer
        .apply(() -> filterControl.requestProcessing(mockDescriptor, null));

    assertAll("FilterControl requestProcessing null arguments:",
        nullDescriptor, nullParameters);
  }

  @Test
  void testExecuteProcessingNullArguments() {
    Executable nullWaveforms = assertThrowsNullPointer
        .apply(() -> FilterControl.executeProcessing(null,
            mockPlugin, Map.of(), randomUUID()));

    Executable nullPlugin = assertThrowsNullPointer
        .apply(() -> FilterControl.executeProcessing(mockWaveforms,
            null, Map.of(), randomUUID()));

    Executable nullParameters = assertThrowsNullPointer
        .apply(() -> FilterControl.executeProcessing(mockWaveforms,
            mockPlugin, null, randomUUID()));

    Executable nullOutputId = assertThrowsNullPointer
        .apply(() -> FilterControl.executeProcessing(mockWaveforms,
            mockPlugin, Map.of(), null));

    assertAll("FilterControl executeProcessing null arguments:",
        nullPlugin, nullWaveforms, nullParameters, nullOutputId);
  }

  @Test
  void testStoreNullArguments() {
    assertThrows(NullPointerException.class, () -> filterControl.storeWaveforms(null));
  }

  @Test
  void testProcessing() throws IOException {
    ChannelSegmentDescriptor descriptor = ChannelSegmentDescriptor.from(new UUID(0, 0),
        Instant.EPOCH, Instant.MAX);
    ChannelSegmentDescriptor processingDescriptor = ChannelSegmentDescriptor.from(new UUID(0, 0),
        Instant.MIN, Instant.MAX);
    Map<String, Object> pluginParams = Map.of("TEST", "TEST");
    UUID outputId = randomUUID();
    FilterParameters parameters = FilterParameters.from("TEST", pluginParams);
    Waveform filteredWaveform = randomWaveform();
    double sampleRate = 40.0;

    //data mocking
    given(mockChannel.getVersionId()).willReturn(descriptor.getChannelId());
    given(mockChannel.getNominalSampleRate()).willReturn(sampleRate);

    //config mocking
    given(mockConfig.getFilterParameters(sampleRate))
        .willReturn(List.of(parameters));

    //plugin mocking
    given(mockRegistry.lookup(RegistrationInfo.create(parameters.getPluginName(), 1, 0, 0)))
        .willReturn(Optional.of(mockPlugin));
    given(mockPlugin.getProcessingDescriptor(descriptor, pluginParams))
        .willReturn(processingDescriptor);
    given(mockPlugin.filter(mockWaveforms, pluginParams))
        .willReturn(List.of(filteredWaveform));

    //coi mocking
    given(mockRepository.getChannels(List.of(descriptor.getChannelId())))
        .willReturn(List.of(mockChannel));
    given(mockRepository.getWaveforms(processingDescriptor)).willReturn(mockWaveforms);

    //processing that retrieves config
    List<ChannelSegment<Waveform>> actualFilteredSegments;
    ChannelSegment<Waveform> actualFilteredSegment;

    actualFilteredSegments = filterControl.requestProcessing(descriptor);
    assertEquals(1, actualFilteredSegments.size());
    actualFilteredSegment = actualFilteredSegments.get(0);

    assertEquals(List.of(filteredWaveform), actualFilteredSegment.getTimeseries());

    //processing that has config and retrieves data
    actualFilteredSegment = filterControl
        .requestProcessing(descriptor, parameters);
    assertEquals(List.of(filteredWaveform), actualFilteredSegment.getTimeseries());

    //processing that has data and config
    actualFilteredSegment = FilterControl
        .executeProcessing(mockWaveforms, mockPlugin, pluginParams, outputId);
    assertEquals(List.of(filteredWaveform), actualFilteredSegment.getTimeseries());
  }

  @Test
  void testStoreFilteredWaveforms() throws IOException {
    ChannelSegmentStorageResponse expectedResponse = ChannelSegmentStorageResponse.builder()
        .addStored(ChannelSegmentDescriptor.from(randomUUID(), Instant.EPOCH, Instant.MAX))
        .build();

    given(mockRepository.storeChannelSegments(List.of(mockWaveforms)))
        .willReturn(expectedResponse);

    ChannelSegmentStorageResponse storageResponse = filterControl
        .storeWaveforms(List.of(mockFilteredChannelSegment));
    assertEquals(expectedResponse, storageResponse);

    verify(mockRepository).storeChannelSegments(List.of(mockFilteredChannelSegment));
  }
}
