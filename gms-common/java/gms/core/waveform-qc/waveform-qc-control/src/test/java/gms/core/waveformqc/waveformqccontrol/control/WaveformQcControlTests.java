package gms.core.waveformqc.waveformqccontrol.control;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gms.core.waveformqc.plugin.WaveformQcPlugin;
import gms.core.waveformqc.waveformqccontrol.coi.CoiRepository;
import gms.core.waveformqc.waveformqccontrol.configuration.QcConfiguration;
import gms.core.waveformqc.waveformqccontrol.configuration.QcParameters;
import gms.shared.frameworks.control.ControlContext;
import gms.shared.frameworks.pluginregistry.PluginRegistry;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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

/**
 * Unit tests for {@link WaveformQcControl}
 */
@ExtendWith(MockitoExtension.class)
public class WaveformQcControlTests {

  //convenience function for null assertions
  private static Function<Executable, Executable> assertThrowsNullPointer =
      e -> () -> assertThrows(NullPointerException.class, e);

  //dependency injected mocks
  @Mock
  private QcConfiguration mockConfig;
  @Mock
  private PluginRegistry mockRegistry;
  @Mock
  private CoiRepository mockRepository;

  //data mocks
  @Mock
  ChannelSegmentDescriptor mockDescriptor;
  @Mock
  WaveformQcPlugin mockPlugin;
  @Mock
  ChannelSegment<Waveform> mockWaveforms;
  @Mock
  QcParameters mockParameters;
  @Mock
  QcMask mockQcMask;

  @Captor
  private ArgumentCaptor<List<QcMask>> qcMaskCaptor;

  private WaveformQcControl qcControl;

  @BeforeEach
  public void setUp() {
    this.qcControl = WaveformQcControl.create(mockConfig, mockRegistry, mockRepository);
  }

  @AfterEach
  public void tearDown() {
    this.qcControl = null;
  }

  @Test
  void testCreateNullArguments() {
    assertThrows(NullPointerException.class, () -> WaveformQcControl.create(null));
  }

  @Test
  void testRequestProcessingNullArguments() {
    assertThrows(NullPointerException.class,
        () -> qcControl.requestQcProcessing(null));

    Executable nullDescriptor = assertThrowsNullPointer
        .apply(() -> qcControl.requestQcProcessing(null, mockParameters));
    Executable nullParameters = assertThrowsNullPointer
        .apply(() -> qcControl.requestQcProcessing(mockDescriptor, null));

    assertAll("WaveformQcControl requestProcessing null arguments:",
        nullDescriptor, nullParameters);
  }

  @Test
  void testExecuteProcessingNullArguments() {
    Executable nullPlugin = assertThrowsNullPointer
        .apply(() -> WaveformQcControl.executeQcProcessing(null,
            mockWaveforms, emptyList(), emptyList(), Map.of()));

    Executable nullWaveforms = assertThrowsNullPointer
        .apply(() -> WaveformQcControl.executeQcProcessing(mockPlugin,
            null, emptyList(), emptyList(), Map.of()));

    Executable nullSohStatuses = assertThrowsNullPointer
        .apply(() -> WaveformQcControl.executeQcProcessing(mockPlugin,
            mockWaveforms, null, emptyList(), Map.of()));

    Executable nullQcMasks = assertThrowsNullPointer
        .apply(() -> WaveformQcControl.executeQcProcessing(mockPlugin,
            mockWaveforms, emptyList(), null, Map.of()));

    Executable nullParameters = assertThrowsNullPointer
        .apply(() -> WaveformQcControl.executeQcProcessing(mockPlugin,
            mockWaveforms, emptyList(), emptyList(), null));

    assertAll("WaveformQcControl executeProcessing null arguments:",
        nullPlugin, nullWaveforms, nullSohStatuses, nullQcMasks, nullParameters);
  }

  @Test
  void testStoreNullArguments() {
    assertThrows(NullPointerException.class, () -> qcControl.storeQcMasks(null));
  }

  @Test
  void testExecuteNullArguments() {
    assertThrows(NullPointerException.class, () -> qcControl.executeAutomatic(null));
  }

  @Test
  void testExecuteProcessingReturnsEmptyMasksReturnsEmptyList() {
    final WaveformQcControl spyControl = spy(qcControl);
    final List<QcMaskVersionDescriptor> descs = spyControl.executeAutomatic(mockDescriptor);
    assertEquals(List.of(), descs);
  }

  @Test
  void testExecute() {
    final WaveformQcControl spyControl = spy(qcControl);
    final List<QcMask> masks = List.of(mockQcMask);
    final QcMaskVersionDescriptor desc = mock(QcMaskVersionDescriptor.class);
    doReturn(masks).when(spyControl).requestQcProcessing(mockDescriptor);
    when(mockQcMask.getCurrentVersionAsReference()).thenReturn(desc);
    assertEquals(List.of(desc), spyControl.executeAutomatic(mockDescriptor));
  }

  @Test
  void testProcessing() throws IOException {
    ChannelSegmentDescriptor descriptor = ChannelSegmentDescriptor.from(new UUID(0, 0),
        Instant.EPOCH, Instant.MAX);
    ChannelSegmentDescriptor processingDescriptor = ChannelSegmentDescriptor.from(new UUID(0, 0),
        Instant.MIN, Instant.MAX);
    Map<String, Object> pluginParams = Map.of("TEST", "TEST");
    QcParameters parameters = QcParameters.from("TEST", pluginParams);

    //config mocking
    given(mockConfig.getPluginConfigurations()).willReturn(List.of(parameters));

    //plugin mocking
    given(mockRegistry.get(parameters.getPluginName(), WaveformQcPlugin.class))
        .willReturn(mockPlugin);
    given(mockPlugin.getProcessingDescriptor(descriptor, pluginParams))
        .willReturn(processingDescriptor);
    given(mockPlugin.generateQcMasks(mockWaveforms, emptyList(), emptyList(), pluginParams))
        .willReturn(List.of(mockQcMask));

    //coi mocking
    given(mockRepository.getWaveforms(processingDescriptor)).willReturn(mockWaveforms);
    given(mockRepository.getChannelSohStatuses(processingDescriptor, Duration.ofMillis(1000)))
        .willReturn(emptyList());
    given(mockRepository.getQcMasks(processingDescriptor)).willReturn(emptyList());

    //processing that retrieves config
    List<QcMask> actualMasks = qcControl.requestQcProcessing(descriptor);
    assertEquals(1, actualMasks.size());
    assertEquals(mockQcMask, actualMasks.get(0));

    //processing that has config and retrieves data
    actualMasks = qcControl.requestQcProcessing(descriptor, parameters);
    assertEquals(1, actualMasks.size());
    assertEquals(mockQcMask, actualMasks.get(0));

    //processing that has data and config
    actualMasks = WaveformQcControl.executeQcProcessing(mockPlugin, mockWaveforms, emptyList(),
        emptyList(), pluginParams);
    assertEquals(1, actualMasks.size());
    assertEquals(mockQcMask, actualMasks.get(0));
  }

  @Test
  void testStoreQcMasks() throws IOException {
    QcMaskVersionDescriptor maskDescriptor = QcMaskVersionDescriptor.from(new UUID(0, 0),
        1);

    given(mockQcMask.getCurrentVersionAsReference()).willReturn(maskDescriptor);
    willDoNothing().given(mockRepository).storeQcMasks(qcMaskCaptor.capture());

    List<QcMaskVersionDescriptor> actualMaskDescriptors = qcControl
        .storeQcMasks(List.of(mockQcMask));
    assertEquals(1, actualMaskDescriptors.size());
    assertEquals(maskDescriptor, actualMaskDescriptors.get(0));

    verify(mockRepository).storeQcMasks(List.of(mockQcMask));
  }
}

