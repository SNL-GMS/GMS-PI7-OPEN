package gms.core.waveformqc.waveformsignalqc.plugin;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WaveformGapQcPluginTest {

  private static ChannelSegment<Waveform> channelSegment;
  private static WaveformGapQcPlugin plugin;

  public static Waveform createWaveform(Instant start, double samplesPerSec, int numSamples) {
    double[] values = new double[numSamples];
    Arrays.fill(values, 1.0);

    return Waveform.withValues(start, samplesPerSec, values);
  }

  @BeforeEach
  public void setUp() {
    final Instant start = Instant.EPOCH;
    final Instant end = start.plusSeconds(1000);

    final Waveform waveform = createWaveform(start, 40.0, 8000);
    final Waveform waveform1 = createWaveform(start.plusSeconds(350), 40.0, 2000);
    final Waveform waveform2 = createWaveform(start.plusSeconds(400), 40.0, 8000);
    final Waveform waveform3 = createWaveform(start.plusSeconds(610), 40.0, 7600);
    final Waveform waveform4 = createWaveform(start.plusSeconds(800), 40.0, 8000);

    channelSegment = ChannelSegment.create(
        UUID.randomUUID(),
        "segmentName",
        ChannelSegment.Type.RAW,
        List.of(waveform, waveform1, waveform2, waveform3, waveform4),
        new CreationInfo("test", Instant.now(), new SoftwareComponentInfo("test", "test")));

    plugin = new WaveformGapQcPlugin();
  }

  private static Map<String, Object> getDefaultPluginConfiguration() {
    return Map.of("minLongGapLengthInSamples", 2);
  }

  /**
   * Make sure the plugin creates masks.  Don't check full details of the mask since that is done in
   * algorithm testing.
   */
  @Test
  public void testGapMaskCreation() {
    List<QcMask> gapMasks = plugin
        .createQcMasks(channelSegment, emptyList(), getDefaultPluginConfiguration());

    assertEquals(2, gapMasks.size());
  }

  /**
   * Make sure the plugin updates existing QC Masks.  Don't check full details since that is covered
   * in algorithm testing.
   */
  @Test
  public void testCreateQcMasksUpdatesExisting() {

    // Both will be rejected due to being filled
    List<QcMask> gaps = List.of(
        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.REPAIRABLE_GAP,
            channelSegment.getChannelId(), channelSegment.getId(),
            Instant.ofEpochSecond(100), Instant.ofEpochSecond(200)),

        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.LONG_GAP,
            channelSegment.getChannelId(), channelSegment.getId(),
            Instant.ofEpochSecond(400), Instant.ofEpochSecond(600))
    );

    List<QcMask> gapMasks = plugin
        .createQcMasks(channelSegment, gaps, getDefaultPluginConfiguration());

    // Two new, two rejected
    assertEquals(4, gapMasks.size());

    // Input masks should be returned since they are rejected
    List<QcMask> updated = gapMasks.stream().filter(gaps::contains).collect(Collectors.toList());
    assertEquals(2, updated.size());
    assertTrue(
        updated.stream().map(QcMask::getCurrentQcMaskVersion).allMatch(QcMaskVersion::isRejected));
  }

  /**
   * Make sure the plugin only updates gap masks.  Don't check full details since that is covered in
   * algorithm testing.
   */
  @Test
  public void testCreateQcMasksFiltersNonGapMasks() {

    // If these were gaps they would be rejected
    List<QcMask> notGaps = List.of(
        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SPIKE,
            channelSegment.getChannelId(), channelSegment.getId(),
            Instant.ofEpochSecond(100), Instant.ofEpochSecond(200)),

        createQcMask(QcMaskCategory.STATION_SOH, QcMaskType.TIMING,
            channelSegment.getChannelId(), channelSegment.getId(),
            Instant.ofEpochSecond(400), Instant.ofEpochSecond(600))
    );

    List<QcMask> gapMasks = plugin
        .createQcMasks(channelSegment, notGaps, getDefaultPluginConfiguration());

    assertEquals(2, gapMasks.size());
  }

  /**
   * Make sure the plugin only updates existing gap masks on the correct processing channel. Don't
   * check full details since that is covered in algorithm testing.
   */
  @Test
  public void testCreateQcMasksFiltersMasksByChannel() {

    UUID channelSegmentUuid = generateDifferentUuid(channelSegment.getId());
    UUID channelUuid = generateDifferentUuid(channelSegment.getChannelId());

    List<QcMask> notGaps = List.of(
        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.REPAIRABLE_GAP, channelUuid,
            channelSegmentUuid,
            Instant.ofEpochSecond(100), Instant.ofEpochSecond(200)),

        createQcMask(QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.LONG_GAP, channelUuid,
            channelSegmentUuid,
            Instant.ofEpochSecond(400), Instant.ofEpochSecond(600))
    );

    List<QcMask> gapMasks = plugin
        .createQcMasks(channelSegment, notGaps, getDefaultPluginConfiguration());

    assertEquals(2, gapMasks.size());
  }

  private static UUID generateDifferentUuid(UUID uuid) {
    UUID other;
    do {
      other = UUID.randomUUID();
    } while (other.equals(uuid));

    return other;
  }

  private QcMask createQcMask(QcMaskCategory category, QcMaskType type, UUID processingChannelId,
      UUID channelSegmentId,
      Instant start, Instant end) {

    return QcMask.create(processingChannelId, emptyList(), List.of(channelSegmentId),
        category, type, "Test Mask", start, end);
  }
}
