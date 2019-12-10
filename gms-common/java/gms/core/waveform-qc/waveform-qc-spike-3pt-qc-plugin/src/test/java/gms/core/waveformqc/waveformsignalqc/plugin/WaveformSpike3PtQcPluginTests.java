package gms.core.waveformqc.waveformsignalqc.plugin;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.willReturn;

import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformSpike3PtInterpreter;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformSpike3PtQcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class WaveformSpike3PtQcPluginTests {

  private static ChannelSegment<Waveform> channelSegment;

  @Mock
  private WaveformSpike3PtInterpreter mockWaveformSpike3PtInterpreter;

  public static Waveform createWaveform(Instant start, double samplesPerSec, int numSamples) {
    double[] values = new double[numSamples];
    Arrays.fill(values, 1.0);

    return Waveform.withValues(start, samplesPerSec, values);
  }

  private Map<String, Object> getDefaultParameters() {
    return Map.of(
        "minConsecutiveSampleDifferenceSpikeThreshold", 0.5,
        "rmsAmplitudeRatioThreshold", 4.0,
        "rmsLeadSampleDifferences", 9,
        "rmsLagSampleDifferences", 9);
  }

  @BeforeEach
  public void setUp() {
    final Instant start = Instant.EPOCH;
    final Instant end = start.plusSeconds(1000);

    final Waveform waveform = createWaveform(start, 40.0, 8000);

    /* Include spikes in the first waveform */
    waveform.getValues()[15] = -1.0;

    final Waveform waveform1 = createWaveform(start.plusSeconds(350), 40.0, 2000);

    /* Include spikes in the second waveform */
    waveform1.getValues()[15] = -1.0;

    final Waveform waveform2 = createWaveform(start.plusSeconds(400), 40.0, 8000);
    final Waveform waveform3 = createWaveform(start.plusSeconds(610), 40.0, 7600);
    final Waveform waveform4 = createWaveform(start.plusSeconds(800), 40.0, 8000);

    channelSegment = ChannelSegment.create(
        UUID.randomUUID(),
        "segmentName",
        ChannelSegment.Type.RAW,
        List.of(waveform, waveform1, waveform2, waveform3, waveform4),
        new CreationInfo("test", Instant.now(), new SoftwareComponentInfo("test", "test")));
  }

  private void setupMockInterpreter() {
    List<WaveformSpike3PtQcMask> discoveredQcMasks = List.of(
        WaveformSpike3PtQcMask.create(channelSegment.getChannelId(),
            channelSegment.getId(), Instant.ofEpochSecond(0).plusNanos(25000000L)),
        WaveformSpike3PtQcMask.create(channelSegment.getChannelId(),
            channelSegment.getId(),
            Instant.ofEpochSecond(0).plusSeconds(350).plusNanos(25000000L)));

    willReturn(discoveredQcMasks)
        .given(mockWaveformSpike3PtInterpreter)
        .createWaveformSpike3PtQcMasks(channelSegment, 0.5, 9, 9, 4.0);

  }

  @Test
  public void testCreateNullInterpreterExpectNullPointerException() {
    assertThrows(NullPointerException.class, () -> WaveformSpike3PtQcPlugin.create(null));
  }

  /**
   * Make sure the plugin creates masks.  Don't check full details of the mask since that is done in
   * algorithm testing.
   */
  @Test
  public void testQcMaskCreation() {
    setupMockInterpreter();

    WaveformSpike3PtQcPlugin plugin = WaveformSpike3PtQcPlugin.create(
        mockWaveformSpike3PtInterpreter);

    List<QcMask> qcMasks = plugin
        .createQcMasks(channelSegment, emptyList(), getDefaultParameters());

    assertEquals(2, qcMasks.size());
  }

  /**
   * The plugin takes into account passed in existing QcMasks. In the test below, the plugin removes
   * ALL non-Spike QcMasks. The plugin removes ALL non-Spike, rejected, and duplicated Spike
   * QcMasks. Don't check full details since that is covered in algorithm testing.
   */
  @Test
  public void testCreateQcMasksWithExistingNonSpikeQcMasks() {
    setupMockInterpreter();

    // Create other non-SPIKE masks and provide to plugin
    List<QcMask> existingQcMasks = List.of(
        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.LONG_GAP,
            channelSegment.getChannelId(), channelSegment.getId(),
            Instant.ofEpochSecond(100), Instant.ofEpochSecond(200)),

        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.REPAIRABLE_GAP,
            channelSegment.getChannelId(), channelSegment.getId(),
            Instant.ofEpochSecond(400), Instant.ofEpochSecond(600))
    );

    WaveformSpike3PtQcPlugin plugin = WaveformSpike3PtQcPlugin.create(
        mockWaveformSpike3PtInterpreter);

    List<QcMask> spikeMasks = plugin
        .createQcMasks(channelSegment, existingQcMasks, getDefaultParameters());

    // Expect two new, passed in two existing non-spike QcMasks
    assertEquals(2, spikeMasks.size());
  }

  /**
   * The plugin takes into account passed in existing QcMasks. In the test below, the plugin removes
   * ALL rejected QcMasks. The plugin removes ALL non-Spike, rejected, and duplicated Spike QcMasks.
   * Don't check full details since that is covered in algorithm testing.
   */
  @Test
  public void testCreateQcMasksWithExistingRejectedQcMasks() {
    setupMockInterpreter();

    // Create rejected QcMasks and provide to plugin
    QcMask rejectQcMask1 = createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.LONG_GAP,
        channelSegment.getChannelId(), channelSegment.getId(),
        Instant.ofEpochSecond(100), Instant.ofEpochSecond(200));

    rejectQcMask1.reject("Test Reject Mask", List.of(channelSegment.getChannelId()));

    QcMask rejectQcMask2 = createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SPIKE,
        channelSegment.getChannelId(), channelSegment.getId(),
        Instant.ofEpochSecond(400), Instant.ofEpochSecond(400));

    rejectQcMask2.reject("Test Reject Mask", List.of(channelSegment.getChannelId()));

    List<QcMask> existingQcMasks = List.of(rejectQcMask1, rejectQcMask2);

    WaveformSpike3PtQcPlugin plugin = WaveformSpike3PtQcPlugin.create(
        mockWaveformSpike3PtInterpreter);

    List<QcMask> spikeMasks = plugin
        .createQcMasks(channelSegment, existingQcMasks, getDefaultParameters());

    // Expect two new, passed in two existing rejected QcMasks
    assertEquals(2, spikeMasks.size());
  }

  /**
   * The plugin takes into account passed in existing QcMasks. In the test below, the plugin
   * contains a duplicate QcMask and removes it. Don't check full details since that is covered in
   * algorithm testing.
   */
  @Test
  public void testCreateQcMasksWithExistingDuplicatedQcMask() {
    setupMockInterpreter();

    // Create duplicated QcMasks and provide to plugin
    List<QcMask> existingQcMasks = List.of(
        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SPIKE,
            channelSegment.getChannelId(), channelSegment.getId(),
            Instant.ofEpochSecond(0).plusNanos(25000000L),
            Instant.ofEpochSecond(0).plusNanos(25000000L))
    );

    WaveformSpike3PtQcPlugin plugin = WaveformSpike3PtQcPlugin.create(
        mockWaveformSpike3PtInterpreter);

    List<QcMask> spikeMasks = plugin
        .createQcMasks(channelSegment, existingQcMasks, getDefaultParameters());

    // Expect one new, passed in one duplicated QcMask
    assertEquals(1, spikeMasks.size());
  }


  private QcMask createQcMask(QcMaskCategory category, QcMaskType type, UUID processingChannelId,
      UUID channelSegmentId,
      Instant start, Instant end) {

    return QcMask
        .create(processingChannelId, emptyList(), List.of(channelSegmentId), category, type,
            "Test Mask", start, end);
  }

}
