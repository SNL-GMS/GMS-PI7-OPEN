package gms.core.waveformqc.waveformsignalqc.plugin;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.willReturn;

import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformRepeatedAmplitudeInterpreter;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformRepeatedAmplitudeQcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WaveformRepeatedAmplitudeQcPluginTests {

  @Mock
  private WaveformRepeatedAmplitudeInterpreter mockWaveformRepeatedAmplitudeInterpreter;

  private static final int minRepeats = 2;
  private static final double maxDelta = 1.0;
  private static final double maskMergeThresholdSec = 1.0;
  private static final double startAmplitude = 2.5;

  private List<ChannelSegment<Waveform>> channelSegments;

  private Map<ChannelSegment<Waveform>, List<WaveformRepeatedAmplitudeQcMask>> expectedMasksByChannelSegment;

  private List<WaveformRepeatedAmplitudeQcMask> allRepeatedAmplitudeMasks;

  private QcMask existingQcMaskToMerge;

  private Map<String, Object> getPluginConfiguration(boolean zeroThreshold) {
    double threshold = zeroThreshold ? 0.0 : maskMergeThresholdSec;
    return Map.of(
        "minSeriesLengthInSamples", minRepeats,
        "maxDeltaFromStartAmplitude", maxDelta,
        "maskMergeThresholdSeconds", threshold
    );
  }

  @BeforeEach
  public void setUp() {

    UUID processingChannelId1 = UUID.randomUUID();
    UUID processingChannelId2 = UUID.randomUUID();
    channelSegments = List.of(
        generateChannelSegment(processingChannelId1), generateChannelSegment(processingChannelId2)
    );

    expectedMasksByChannelSegment = new HashMap<>();
    channelSegments.forEach(c ->
        expectedMasksByChannelSegment.put(c, List.of(WaveformRepeatedAmplitudeQcMask
            .create(Instant.EPOCH.plusSeconds(5), Instant.EPOCH.plusSeconds(15),
                c.getChannelId(), c.getId())))
    );

    existingQcMaskToMerge = QcMask
        .create(processingChannelId1, emptyList(), List.of(UUID.randomUUID()),
            QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.REPEATED_ADJACENT_AMPLITUDE_VALUE,
            "System created repeated adjacent amplitude values mask", Instant.EPOCH,
            Instant.EPOCH.plusSeconds(5));

    allRepeatedAmplitudeMasks = expectedMasksByChannelSegment
        .values().stream().flatMap(List::stream).collect(Collectors.toList());
  }

  private ChannelSegment<Waveform> generateChannelSegment(UUID processingChannelId) {
    final Waveform wf = Waveform.withValues(Instant.EPOCH, 2, new double[]{5, 6, 7});
    return ChannelSegment.create(processingChannelId, "test", ChannelSegment.Type.RAW,
        List.of(wf), CreationInfo.DEFAULT);
  }

  private WaveformRepeatedAmplitudeQcPlugin createPluginWithMocks() {
    return WaveformRepeatedAmplitudeQcPlugin.create(mockWaveformRepeatedAmplitudeInterpreter);
  }

  @Test
  public void testCreate() {
    WaveformRepeatedAmplitudeQcPlugin plugin = createPluginWithMocks();

    assertNotNull(plugin);
  }

  @Test
  public void testCreateNullInterpreterExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> WaveformRepeatedAmplitudeQcPlugin.create(null));
  }

  @Test
  public void testCreateQcMasks() {
    WaveformRepeatedAmplitudeQcPlugin plugin = createPluginWithMocks();

    channelSegments.forEach(c ->
        willReturn(expectedMasksByChannelSegment.get(c))
            .given(mockWaveformRepeatedAmplitudeInterpreter)
            .createWaveformRepeatedAmplitudeQcMasks(c, minRepeats, maxDelta));

    List<QcMask> qcMasks = plugin
        .createQcMasks(channelSegments.get(0), emptyList(), getPluginConfiguration(false));
    List<QcMask> qcMasks1 = plugin
        .createQcMasks(channelSegments.get(1), emptyList(), getPluginConfiguration(false));
    qcMasks.addAll(qcMasks1);

    verifyContainsExpectedMasks(qcMasks.stream());
  }

  private static boolean equivalent(QcMask qcMask, WaveformRepeatedAmplitudeQcMask repeatedAmp) {
    QcMaskVersion curVer = qcMask.getCurrentQcMaskVersion();

    return qcMask.getChannelId().equals(repeatedAmp.getChannelId())
        && qcMask.getQcMaskVersions().size() == 1
        && !curVer.isRejected()
        && curVer.getStartTime().get().equals(repeatedAmp.getStartTime())
        && curVer.getEndTime().get().equals(repeatedAmp.getEndTime())
        && curVer.getChannelSegmentIds().size() == 1
        && curVer.getChannelSegmentIds().contains(repeatedAmp.getChannelSegmentId())
        && curVer.getCategory().equals(QcMaskCategory.WAVEFORM_QUALITY)
        && curVer.getType().get().equals(QcMaskType.REPEATED_ADJACENT_AMPLITUDE_VALUE)
        && curVer.getRationale().equals(
        "System created repeated adjacent amplitude values mask");
  }

  @Test
  public void testCreateQcMasksMergesExistingMasks() {
    WaveformRepeatedAmplitudeQcPlugin plugin = createPluginWithMocks();

    willReturn(expectedMasksByChannelSegment.get(channelSegments.get(0)))
        .given(mockWaveformRepeatedAmplitudeInterpreter)
        .createWaveformRepeatedAmplitudeQcMasks(channelSegments.get(0), minRepeats, maxDelta);

    List<QcMask> qcMasks = plugin
        .createQcMasks(channelSegments.get(0), List.of(existingQcMaskToMerge),
            getPluginConfiguration(false));

    assertNotNull(qcMasks);

    assertEquals(1, qcMasks.size());
    assertEquals(2, qcMasks.get(0).getQcMaskVersions().size());

    QcMaskVersion curVersion = qcMasks.get(0).getCurrentQcMaskVersion();
    assertNotNull(curVersion);
    assertFalse(curVersion.isRejected());

    assertEquals(Instant.EPOCH, curVersion.getStartTime().get());
    assertEquals(Instant.EPOCH.plusSeconds(15), curVersion.getEndTime().get());
    assertEquals(QcMaskType.REPEATED_ADJACENT_AMPLITUDE_VALUE, curVersion.getType().get());
  }

  @Test
  public void testCreateQcMasksUsesConfiguredMergeThreshold() {
    WaveformRepeatedAmplitudeQcPlugin plugin = createPluginWithMocks();

    channelSegments.forEach(c ->
        willReturn(expectedMasksByChannelSegment.get(c))
            .given(mockWaveformRepeatedAmplitudeInterpreter)
            .createWaveformRepeatedAmplitudeQcMasks(c, minRepeats, maxDelta));

    // If merge threshold is not used then the new and existing masks would merge
    // since they are 0.0 seconds apart
    List<QcMask> qcMasks = plugin
        .createQcMasks(channelSegments.get(0), List.of(existingQcMaskToMerge),
            getPluginConfiguration(true));
    List<QcMask> qcMasks1 = plugin
        .createQcMasks(channelSegments.get(1), List.of(existingQcMaskToMerge),
            getPluginConfiguration(true));
    qcMasks.addAll(qcMasks1);

    verifyContainsExpectedMasks(qcMasks.stream());
  }

  @Test
  public void testCreateQcMasksFiltersNonRepeatedAmplitudeMasks() {
    WaveformRepeatedAmplitudeQcPlugin plugin = createPluginWithMocks();

    channelSegments.forEach(c ->
        willReturn(expectedMasksByChannelSegment.get(c))
            .given(mockWaveformRepeatedAmplitudeInterpreter)
            .createWaveformRepeatedAmplitudeQcMasks(c, minRepeats, maxDelta));

    // If these were repeated adjacent amplitude masks the plugin would update them
    List<QcMask> notRepeatedAmplitudeMasks = List.of(
        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SPIKE,
            channelSegments.get(0).getChannelId(), channelSegments.get(0).getId(),
            Instant.ofEpochSecond(5), Instant.ofEpochSecond(15)),

        createQcMask(QcMaskCategory.ANALYST_DEFINED, QcMaskType.REPEATED_ADJACENT_AMPLITUDE_VALUE,
            channelSegments.get(0).getChannelId(), channelSegments.get(0).getId(),
            Instant.ofEpochSecond(5), Instant.ofEpochSecond(15))
    );

    List<QcMask> notRepeatedAmplitudeMasks1 = List.of(
        createQcMask(QcMaskCategory.STATION_SOH, QcMaskType.TIMING,
            channelSegments.get(1).getChannelId(), channelSegments.get(1).getId(),
            Instant.ofEpochSecond(0), Instant.ofEpochSecond(20))
    );

    List<QcMask> qcMasks = plugin.createQcMasks(channelSegments.get(0), notRepeatedAmplitudeMasks,
        getPluginConfiguration(false));
    List<QcMask> qcMasks1 = plugin.createQcMasks(channelSegments.get(1), notRepeatedAmplitudeMasks1,
        getPluginConfiguration(false));
    qcMasks.addAll(qcMasks1);

    verifyContainsExpectedMasks(qcMasks.stream());
  }

  private void verifyContainsExpectedMasks(Stream<QcMask> qcMasksStream) {
    assertNotNull(qcMasksStream);
    List<QcMask> qcMasksList = qcMasksStream.collect(Collectors.toList());

    assertEquals(expectedMasksByChannelSegment.values().stream().mapToInt(List::size).sum(),
        qcMasksList.size());
    assertTrue(qcMasksList.stream().allMatch(
        q -> allRepeatedAmplitudeMasks.stream().filter(r -> equivalent(q, r)).count() == 1));
  }

  private QcMask createQcMask(QcMaskCategory category, QcMaskType type, UUID processingChannelId,
      UUID channelSegmentId, Instant start, Instant end) {

    return QcMask
        .create(processingChannelId, emptyList(), List.of(channelSegmentId), category, type,
        String.format("System created: amplitudes within %s of %s", maxDelta, startAmplitude),
        start, end);
  }

  @Test
  public void testCreateQcMasksNullChannelSegmentsExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> createPluginWithMocks()
            .createQcMasks(null, emptyList(), getPluginConfiguration(false)));
  }

  @Test
  public void testCreateQcMasksNullExistingMasksExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> createPluginWithMocks()
            .createQcMasks(channelSegments.get(0), null, getPluginConfiguration(false)));
  }

  @Test
  public void testCreateQcMasksNullParameterFieldMapExpectNullPointerException() {
    assertThrows(NullPointerException.class,
        () -> createPluginWithMocks()
            .createQcMasks(channelSegments.get(0), emptyList(), null));
  }
}
