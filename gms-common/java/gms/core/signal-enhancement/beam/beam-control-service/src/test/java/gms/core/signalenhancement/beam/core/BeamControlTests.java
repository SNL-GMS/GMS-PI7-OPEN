package gms.core.signalenhancement.beam.core;

import gms.core.signalenhancement.beam.TestFixtures;
import gms.core.signalenhancement.beam.osd.client.OsdClient;
import gms.core.signalenhancement.beamcontrol.plugin.BeamPlugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginRegistry;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ProcessingGroupDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BeamControlTests {

  private BeamControl beamControl;
  private BeamControl beamControlUnInitialized;
  private RegistrationInfo mockInfo;
  private List<BeamDefinition> beamDefinitions;

  @Mock
  private OsdClient mockOsdClient;

  @Mock
  private PluginRegistry<BeamPlugin> mockBeamPluginRegistry;

  @Mock
  private BeamConfiguration mockBeamConfiguration;

  @Mock
  private BeamPlugin mockPlugin;

  @BeforeEach
  public void setup() {
    this.beamControl = BeamControl
        .create(mockBeamPluginRegistry, mockOsdClient, mockBeamConfiguration);
    this.beamControlUnInitialized = BeamControl
        .create(mockBeamPluginRegistry, mockOsdClient, mockBeamConfiguration);
    this.mockInfo = RegistrationInfo.from("BeamPlugin", PluginVersion.from(1, 0, 0));

    this.beamDefinitions = List.of(TestFixtures.getBeamDefinition());

    preTestInitialize();
  }

  private static Stream<Arguments> handlerNullArguments() {
    return Stream.of(
        arguments(null, mock(OsdClient.class), mock(BeamConfiguration.class)),
        arguments(mock(PluginRegistry.class), null, mock(BeamConfiguration.class)),
        arguments(mock(PluginRegistry.class), mock(OsdClient.class), null)
    );
  }

  //TODO: Add more tests as executioner is developed

  @ParameterizedTest
  @MethodSource("handlerNullArguments")
  void testCreateNullArguments(PluginRegistry<BeamPlugin> pluginRegistry,
      OsdClient osdClient, BeamConfiguration beamConfiguration) {
    assertThrows(NullPointerException.class,
        () -> BeamControl.create(pluginRegistry, osdClient, beamConfiguration));
  }

  @Test
  void testCreate() {
    assertNotNull(BeamControl
        .create(mockBeamPluginRegistry, mock(OsdClient.class),
            mock(BeamConfiguration.class)));
  }

  @Test
  void testExecuteStreamingCallsStore() {

    BeamStreamingCommand command = TestFixtures.getBeamStreamingCommand();
    Waveform test = TestFixtures.waveform;

    given(mockBeamConfiguration.getInteractivePluginRegistrationInfo()).willReturn(mockInfo);
    given(mockPlugin.beam(anyCollection(), any(BeamDefinition.class))).willReturn(List.of(test));
    given(mockBeamPluginRegistry.lookup(any(RegistrationInfo.class)))
        .willReturn(Optional.of(mockPlugin));

    List<ChannelSegment<Waveform>> waveforms = beamControl.executeStreaming(command);
    double[] expectedValues = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

    assertEquals(command.getOutputChannelId(), waveforms.get(0).getChannelId());
    assertArrayEquals(expectedValues, test.getValues(), 0.0000000001);
  }

  @Test
  void testExecuteClaimCheckNullArguments() {
    assertThrows(NullPointerException.class,
        () -> beamControl.executeClaimCheck(null));
  }

  @Test
  void testExecuteClaimCheckBeforeInitializeExpectIllegalStateException() {
    assertThrows(IllegalStateException.class,
        () -> beamControlUnInitialized.executeClaimCheck(mock(ProcessingGroupDescriptor.class)));
  }

  @Test
  void testExecuteClaimCheckUndefinedParameters() {

    final RegistrationInfo mockInfo = RegistrationInfo.from("mock", PluginVersion.from(1, 0, 0));
    final ProcessingGroupDescriptor descriptor = TestFixtures.getProcessingGroupDescriptor();

    //load mock configuration
    // TODO: remove this OSD operation
    //given(mockOsdClient.loadConfiguration()).willReturn(mockBeamConfiguration);
    given(mockOsdClient.loadPluginConfiguration(any(RegistrationInfo.class)))
        .willReturn(Map.of());

    given(mockBeamConfiguration.getAutomaticBeamDefinitions(descriptor)).willReturn(List.of());
    beamControl.initialize();

    assertThrows(IllegalStateException.class, () -> beamControl.executeClaimCheck(descriptor));
  }

  @Test
  void testExecuteClaimCheckMissingPluginsExpectIllegalStateException() {
    final Instant startTime = Instant.EPOCH;
    final Instant endTime = startTime.plusMillis(1000);
    final UUID processingGroupId = UUID.randomUUID();

    RegistrationInfo mockInfo = RegistrationInfo.from("BeamPlugin",
        PluginVersion.from(1, 0, 0));

    //load mock configuration
    given(mockBeamPluginRegistry.entrySet()).willReturn(Set.of());
    given(mockBeamPluginRegistry.lookup(mockInfo)).willReturn(Optional.empty());

    final ProcessingGroupDescriptor descriptor = ProcessingGroupDescriptor
        .create(processingGroupId, startTime, endTime);

    given(mockBeamConfiguration.getAutomaticBeamDefinitions(descriptor))
        .willReturn(beamDefinitions);
    given(mockBeamConfiguration.getAutomaticPluginRegistrationInfo(descriptor))
        .willReturn(mockInfo);

    // don't define mockBeamPluginRegistry.lookup(mockInfo)) and plugin will show up as
    // missing

    beamControlUnInitialized.initialize();
    assertThrows(IllegalStateException.class,
        () -> beamControlUnInitialized.executeClaimCheck(descriptor));
  }

  @Test
  void testExecuteClaimCheck() {
    final UUID processingGroupId = UUID.randomUUID();

    final RegistrationInfo registrationInfo1 = RegistrationInfo.from("BeamPlugin",
        PluginVersion.from(1, 0, 0));

    final Instant startTime = Instant.EPOCH;
    final Instant endTime = startTime.plusSeconds(5);

    willReturn(List.of(TestFixtures.randomWaveformChannelSegment(),
        TestFixtures.randomWaveformChannelSegment())).given(mockOsdClient)
        .loadChannelSegments(processingGroupId, startTime, endTime);

    // Mock configuration
    final ProcessingGroupDescriptor claimCheckCommand = ProcessingGroupDescriptor
        .create(processingGroupId, startTime, endTime);
    given(mockBeamConfiguration.getAutomaticPluginRegistrationInfo(claimCheckCommand))
        .willReturn(registrationInfo1);
    given(mockBeamConfiguration.getAutomaticBeamDefinitions(claimCheckCommand))
        .willReturn(beamDefinitions);

    //registry returns the right plugin for each parameter
    given(mockBeamPluginRegistry.lookup(registrationInfo1)).willReturn(Optional.of(mockPlugin));

    given(mockPlugin.beam(anyList(), any(BeamDefinition.class))).willReturn(List.of(TestFixtures.waveform));

    given(mockOsdClient.loadChannelProcessingGroup(processingGroupId))
        .willReturn(TestFixtures.getChannelProcessingGroup());

    // Execute claim check command
    beamControlUnInitialized.initialize();

    List<ChannelSegment<Waveform>> beams = beamControlUnInitialized
        .executeClaimCheck(claimCheckCommand);

    verify(mockOsdClient, times(1))
        .loadChannelSegments(processingGroupId, startTime, endTime);

    // Validate one ChannelSegment is returned with its channel id set to outputChannelId
    assertEquals(1, beams.size());

    String expectedName = String.format("%s/%s-%s", TestFixtures.getChannelProcessingGroup().getComment(),
        beamDefinitions.get(0).getAzimuth(), beamDefinitions.get(0).getSlowness());
    assertEquals(UUID.nameUUIDFromBytes(expectedName.getBytes()), beams.get(0).getChannelId());
  }

  @Test
  void testExecuteStreamingNullStreamingExpectNullPointerException() {
    beamControl.initialize();
    assertThrows(NullPointerException.class,
        () -> beamControl.executeStreaming((BeamStreamingCommand) null));
  }

  @Test
  void testExecuteStreamingBeforeInitializeExpectIllegalStateException() {
    assertThrows(IllegalStateException.class,
        () -> beamControlUnInitialized.executeStreaming(mock(BeamStreamingCommand.class)));
  }

  private void preTestInitialize() {
    given(mockBeamPluginRegistry.entrySet())
        .willReturn(Set.of(PluginRegistry.Entry.create(mockInfo, mockPlugin)));
    given(mockOsdClient.loadPluginConfiguration(any(RegistrationInfo.class)))
        .willReturn(Map.of());

    beamControl.initialize();

    verify(mockPlugin, times(1)).initialize(ArgumentMatchers.any());
  }

}
