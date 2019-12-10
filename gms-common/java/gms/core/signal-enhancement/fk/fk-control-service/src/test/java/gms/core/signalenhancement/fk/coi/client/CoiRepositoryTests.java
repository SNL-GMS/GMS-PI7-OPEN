package gms.core.signalenhancement.fk.coi.client;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import gms.core.signalenhancement.fk.FkTestUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesisDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.SignalDetectionRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.UpdateStatus;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.FkSpectraRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.WaveformRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CoiRepositoryTests {

  @Mock
  private SignalDetectionRepository mockSignalDetectionRepository;

  @Mock
  private WaveformRepository mockWaveformRepository;

  @Mock
  private FkSpectraRepository mockFkSpectraRepository;

  @Captor
  ArgumentCaptor<ChannelSegment<FkSpectra>> fkSpectraArgumentCaptor;

  @Captor
  ArgumentCaptor<Collection<SignalDetectionHypothesisDescriptor>> signalDetectionHypothesesDescriptorCaptor;

  private CoiRepository coiRepository;

  @BeforeEach
  public void setUp() {
    coiRepository = CoiRepository
        .from(mockSignalDetectionRepository, mockWaveformRepository, mockFkSpectraRepository);
  }

  @AfterEach
  public void tearDown() {
    coiRepository = null;
  }

  @Test
  void testCreateValidation() {
    assertAll("Create Validation",
        () -> assertThrows(NullPointerException.class,
            () -> CoiRepository.from(null, mockWaveformRepository, mockFkSpectraRepository)),
        () -> assertThrows(NullPointerException.class,
            () -> CoiRepository
                .from(mockSignalDetectionRepository, null, mockFkSpectraRepository)),
        () -> assertThrows(NullPointerException.class,
            () -> CoiRepository
                .from(mockSignalDetectionRepository, mockWaveformRepository, null)),
        () -> assertNotNull(assertDoesNotThrow(
            () -> CoiRepository.from(mockSignalDetectionRepository, mockWaveformRepository,
                mockFkSpectraRepository))));
  }

  @Test
  void testFindWaveformsByChannelsAndTimeValidation() throws Exception {
    given(
        mockWaveformRepository.retrieveChannelSegments(anyCollection(), any(), any(), anyBoolean()))
        .willReturn(Map.of());

    assertAll("Find Waveforms Validation",
        () -> assertThrows(NullPointerException.class,
            () -> coiRepository.findWaveformsByChannelsAndTime(null, Instant.MIN, Instant.MAX)),
        () -> assertThrows(NullPointerException.class,
            () -> coiRepository
                .findWaveformsByChannelsAndTime(List.of(UUID.randomUUID()), null, Instant.MAX)),
        () -> assertThrows(NullPointerException.class,
            () -> coiRepository
                .findWaveformsByChannelsAndTime(List.of(UUID.randomUUID()), Instant.MIN, null)),
        () -> assertThrows(IllegalArgumentException.class,
            () -> coiRepository.findWaveformsByChannelsAndTime(List.of(),
                Instant.MIN, Instant.MAX), "Empty channel ids should throw IllegalArgument"),
        () -> assertThrows(IllegalArgumentException.class,
            () -> coiRepository.findWaveformsByChannelsAndTime(List.of(UUID.randomUUID()),
                Instant.MAX, Instant.MIN),
            "Start time after end time should throw IllegalArgument"),
        () -> assertNotNull(assertDoesNotThrow(
            () -> coiRepository.findWaveformsByChannelsAndTime(List.of(UUID.randomUUID()),
                Instant.MIN, Instant.MAX), "Valid input should not throw exception")));
  }

  @Test
  void testFindWaveformsByChannelsAndTime() throws Exception {
    Instant start = Instant.EPOCH;
    Instant end = start.plusSeconds(60);

    final UUID channelId = new UUID(0, 0);

    ChannelSegment<Waveform> expectedWaveform = FkTestUtility.randomWaveform(channelId, start, end);

    given(mockWaveformRepository.retrieveChannelSegments(List.of(channelId),
        start, end, true)).willReturn(Map.of(channelId, expectedWaveform));
    Collection<ChannelSegment<Waveform>> actualWaveforms = coiRepository
        .findWaveformsByChannelsAndTime(List.of(channelId), start, end);

    assertEquals(1, actualWaveforms.size());
    assertEquals(expectedWaveform, actualWaveforms.iterator().next());
  }

  @Test
  void testStoreFkSpectrasValidation() {
    ChannelSegment<FkSpectra> expected = FkTestUtility
        .defaultSpectraSegment(UUID.randomUUID(), Instant.EPOCH, 30.0);
    assertAll("Store FkSpectras Validation",
        () -> assertThrows(NullPointerException.class,
            () -> coiRepository.storeFkSpectras(null, StorageVisibility.PUBLIC)),
        () -> assertThrows(NullPointerException.class,
            () -> coiRepository.storeFkSpectras(List.of(expected), null)),
        () -> assertDoesNotThrow(
            () -> coiRepository.storeFkSpectras(List.of(expected), StorageVisibility.PUBLIC)));
  }

  @Test
  void testStoreFkSpectras() throws Exception {
    ChannelSegment<FkSpectra> expectedSpectra = FkTestUtility
        .defaultSpectraSegment(new UUID(0, 0), Instant.EPOCH, 1.0);
    doNothing().when(mockFkSpectraRepository).storeFkSpectra(fkSpectraArgumentCaptor.capture());
    coiRepository
        .storeFkSpectras(List.of(expectedSpectra), StorageVisibility.PUBLIC);
    then(mockFkSpectraRepository).should().storeFkSpectra(expectedSpectra);
    assertEquals(expectedSpectra, fkSpectraArgumentCaptor.getValue());
  }

  @Test
  void testStoreSignalDetectionHypothesesValidation() {
    SignalDetectionHypothesis expected = FkTestUtility
        .defaultSignalDetectionHypothesis(UUID.randomUUID(), Instant.EPOCH);
    SignalDetectionHypothesisDescriptor expectedDescriptor = SignalDetectionHypothesisDescriptor.from(expected, UUID.randomUUID());
    assertAll("Store FkSpectras Validation",
        () -> assertThrows(NullPointerException.class,
            () -> coiRepository.storeSignalDetectionHypotheses(null)),
        () -> assertDoesNotThrow(
            () -> coiRepository.storeSignalDetectionHypotheses(List.of(expectedDescriptor))));
  }

  @Test
  void testStoreSignalDetectionHypotheses() {
    SignalDetectionHypothesis expected = FkTestUtility
        .defaultSignalDetectionHypothesis(UUID.randomUUID(), Instant.EPOCH);
    SignalDetectionHypothesisDescriptor expectedDescriptor = SignalDetectionHypothesisDescriptor.from(expected, UUID.randomUUID());

    doReturn(Map.of(UpdateStatus.UPDATED, expectedDescriptor)).when(mockSignalDetectionRepository)
        .store(signalDetectionHypothesesDescriptorCaptor.capture());

    coiRepository
        .storeSignalDetectionHypotheses(List.of(expectedDescriptor));
    then(mockSignalDetectionRepository).should().store(List.of(expectedDescriptor));

    Collection<SignalDetectionHypothesisDescriptor> actual = signalDetectionHypothesesDescriptorCaptor.getValue();
    assertEquals(1, actual.size());
    assertEquals(expected, actual.iterator().next().getSignalDetectionHypothesis());
  }
}
