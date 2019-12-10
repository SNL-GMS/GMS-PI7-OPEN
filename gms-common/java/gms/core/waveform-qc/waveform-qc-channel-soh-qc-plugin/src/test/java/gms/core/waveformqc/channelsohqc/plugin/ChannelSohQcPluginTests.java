package gms.core.waveformqc.channelsohqc.plugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChannelSohQcPluginTests {

  private TestData testData;
  private ChannelSohQcPlugin plugin;

  @BeforeEach
  void setUp() {
    testData = new TestData();
    plugin = new ChannelSohQcPlugin();
  }


  private Map<String, Object> getDefaultPluginConfiguration() {
    return Map.of("mergeThreshold", "PT0.037S",
        "excludedTypes", Collections.emptyList());
  }


  @Test
  void testGenerateQcMasksNullArguments() {
    assertThrows(NullPointerException.class,
        () -> plugin
            .generateQcMasks(null, testData.overlapChannelSohStatusChanges, Collections.emptyList(),
                getDefaultPluginConfiguration()));

    assertThrows(NullPointerException.class,
        () -> plugin
            .generateQcMasks(testData.testWaveform, null, Collections.emptyList(),
                getDefaultPluginConfiguration()));

    assertThrows(NullPointerException.class,
        () -> plugin
            .generateQcMasks(testData.testWaveform, testData.overlapChannelSohStatusChanges, null,
                getDefaultPluginConfiguration()));

    assertThrows(NullPointerException.class,
        () -> plugin
            .generateQcMasks(testData.testWaveform, testData.overlapChannelSohStatusChanges,
                Collections.emptyList(),
                null));

    assertDoesNotThrow(
        () -> plugin
            .generateQcMasks(testData.testWaveform, testData.overlapChannelSohStatusChanges,
                Collections.emptyList(),
                getDefaultPluginConfiguration()));
  }

  /**
   * Tests {@link ChannelSohQcPlugin#generateQcMasks(ChannelSegment, Collection, Collection, Map)}
   * correctly merges new and existing masks
   */
  @Test
  void testGenerateQcMasksWithExisting() {
    final Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);

    List<QcMask> qcMasks = plugin
        .generateQcMasks(testData.testWaveform, testData.overlapChannelSohStatusChanges,
            existingMasks,
            getDefaultPluginConfiguration());

    testData.verifyPluginMergeQcMasks(qcMasks);
  }

  /**
   * Tests {@link ChannelSohQcPlugin#generateQcMasks(ChannelSegment, Collection, Collection, Map)}
   * does not merge with rejected existing masks
   */
  @Test
  void testGenerateQcMasksRejectedExisting() {

    testData.qcMaskChan1AfterGap.reject("Rejected", Collections.emptyList());
    final Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);

    List<QcMask> qcMasks = plugin
        .generateQcMasks(testData.testWaveform, testData.overlapChannelSohStatusChanges,
            existingMasks,
            getDefaultPluginConfiguration());

    // Expect 1 returned mask: qcMaskChan1 extended by the new mask
    assertEquals(1, qcMasks.size());
    assertTrue(qcMasks.contains(testData.qcMaskChan1));
    assertEquals(2, qcMasks.get(0).getQcMaskVersions().size());
    assertEquals(testData.start1, qcMasks.get(0).getCurrentQcMaskVersion().getStartTime().get());
    assertEquals(testData.endMerged, qcMasks.get(0).getCurrentQcMaskVersion().getEndTime().get());
  }

  /**
   * Tests {@link ChannelSohQcPlugin#generateQcMasks(ChannelSegment, Collection, Collection, Map)}
   * does not merge with analyst defined existing masks
   */
  @Test
  void testGenerateQcMasksAnalystDefinedExisting() {

    QcMaskVersion analystVersion = testData.qcMaskChan1AfterGap.getCurrentQcMaskVersion()
        .toBuilder().setCategory(QcMaskCategory.ANALYST_DEFINED).build();

    testData.qcMaskChan1AfterGap
        .addQcMaskVersion(analystVersion.getChannelSegmentIds(), analystVersion.getCategory(),
            analystVersion.getType().get(), analystVersion.getRationale(),
            analystVersion.getStartTime().get(), analystVersion.getEndTime().get());

    final Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);

    List<QcMask> qcMasks = plugin
        .generateQcMasks(testData.testWaveform, testData.overlapChannelSohStatusChanges,
            existingMasks,
            getDefaultPluginConfiguration());

    // Expect 1 returned mask: qcMaskChan1 extended by the new mask
    assertEquals(1, qcMasks.size());
    assertTrue(qcMasks.contains(testData.qcMaskChan1));
    assertEquals(2, qcMasks.get(0).getQcMaskVersions().size());
    assertEquals(testData.start1, qcMasks.get(0).getCurrentQcMaskVersion().getStartTime().get());
    assertEquals(testData.endMerged, qcMasks.get(0).getCurrentQcMaskVersion().getEndTime().get());
  }

  /**
   * Creates a {@link ChannelSohQcPlugin} configured to not create QcMasks for any {@link
   * AcquiredChannelSohType}, then verifies the plugin does not create QcMasks
   */
  @Test
  void testGenerateQcMasksExcludesTypeFromConfig() {
    List<QcMask> qcMasks = plugin
        .generateQcMasks(testData.testWaveform, testData.overlapChannelSohStatusChanges,
            Collections.emptyList(),
            Map.of("mergeThreshold", Duration.ofMillis(37),
                "excludedTypes",
                List.of(testData.overlapChannelSohStatusChanges.iterator().next().getType())));
    assertEquals(0, qcMasks.size());
  }

  /**
   * Creates a ChannelSohQcPlugin with configured mergeThreshold smaller than the duration between
   * the {@link TestData) new and existing masks, then verifies the plugin does not merge the new
   * mask with the existing masks.
   */
  @Test
  void testGenerateQcMasksUsesMergeThresholdConfiguration() {

    // TestData masks are separated by 25ms so should not be merged
    ChannelSohQcPlugin plugin = new ChannelSohQcPlugin();

    final Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);

    List<QcMask> qcMasks = plugin
        .generateQcMasks(testData.testWaveform, testData.overlapChannelSohStatusChanges,
            existingMasks,
            Map.of("mergeThreshold", Duration.ofMillis(5),
                "excludedTypes", Collections.emptyList()));

    // Expect a single new mask.  Verify none of the existing masks exist in the plugin's output
    assertEquals(1, qcMasks.size());
    final QcMask newMask = qcMasks.get(0);
    assertTrue(existingMasks.stream().map(QcMask::getId).noneMatch(newMask.getId()::equals));
    assertEquals(1, newMask.getQcMaskVersions().size());

    // New QcMask should match the testData.qcMaskNewOverlap mask
    TestData.verifySingleMask(qcMasks, testData.qcMaskNewOverlap, existingMasks);
  }

}
