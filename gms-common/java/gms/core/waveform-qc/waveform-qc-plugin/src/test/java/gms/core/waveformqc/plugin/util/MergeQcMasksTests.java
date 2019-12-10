package gms.core.waveformqc.plugin.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh.AcquiredChannelSohType;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class MergeQcMasksTests {

  private MergeQcMasksTestData testData;
  private Duration threshold;

  @BeforeEach
  public void setUp() {
    testData = new MergeQcMasksTestData();

    final double sampleLengthSec = 1.0 / 40.0;
    threshold = Duration.ofNanos((long) (1.5 * sampleLengthSec * 1e9));
  }

  /**
   * Verifies the merge operation extends an existing mask.  Also verifies it does not return
   * existing masks unaffected by the merge.
   */
  @Test
  public void testMergeExtendExistingMask() {

    Collection<QcMask> newMasks = Collections.singleton(testData.qcMaskNewOverlap);
    Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);
    Collection<QcMask> result = MergeQcMasks
        .merge(newMasks, existingMasks, threshold);

    assertEquals(1, result.size());

    QcMask merged = result.iterator().next();
    assertEquals(testData.qcMaskChan1.getId(), merged.getId());
    assertEquals(2, merged.qcMaskVersions().count());

    QcMaskVersion mergedVersion = merged.getCurrentQcMaskVersion();
    assertEquals(testData.start1, mergedVersion.getStartTime().get());
    assertEquals(testData.endMerged, mergedVersion.getEndTime().get());
    assertEquals(QcMaskType.CALIBRATION, mergedVersion.getType().get());
    assertEquals(QcMaskUtility.getSystemRationale(AcquiredChannelSohType.CALIBRATION_UNDERWAY),
        mergedVersion.getRationale());


  }

  /**
   * Verifies the merging of a duplicate new mask with an existing one causes no modifications to
   * the existing mask nor creation of new masks. The existing mask should simply be returned.
   */
  @Test
  public void testMergeDuplicateExistingMask() {
    MergeQcMasksTestData existingTestData = new MergeQcMasksTestData();

    Collection<QcMask> newMasks = Collections.singleton(testData.qcMaskChan1);
    Collection<QcMask> existingMasks = Collections.singleton(existingTestData.qcMaskChan1);
    Collection<QcMask> result = MergeQcMasks
        .merge(newMasks, existingMasks, threshold);

    assertEquals(1, result.size());
    QcMask merged = result.iterator().next();
    assertEquals(existingTestData.qcMaskChan1, merged);
    assertEquals(1, merged.qcMaskVersions().count());
  }

  @Test
  public void testMergeTwoExistingMasks() {

    // Merged mask expected parents are the current versions of qcMaskChan1AfterGap and qcMaskChan1
    final QcMaskVersionDescriptor afterGapParent = QcMaskVersionDescriptor
        .from(testData.qcMaskChan1AfterGap.getId(),
            testData.qcMaskChan1AfterGap.getCurrentQcMaskVersion().getVersion());
    final QcMaskVersionDescriptor preGapParent = QcMaskVersionDescriptor
        .from(testData.qcMaskChan1.getId(),
            testData.qcMaskChan1.getCurrentQcMaskVersion().getVersion());

    Collection<QcMask> newMasks = Collections.singleton(testData.qcMaskNewOverlap);
    Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1AfterGap, testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);
    Collection<QcMask> result = MergeQcMasks
        .merge(newMasks, existingMasks, threshold);

    // Expect 3 returned masks: 2 rejected (qcMaskChan1AfterGap, qcMaskChan1) and the merged mask
    assertEquals(3, result.size());
    assertFalse(result.contains(testData.qcMaskNewOverlap));
    assertFalse(result.contains(testData.qcMaskChan1NoOverlap));

    // Find and verify the single new merged mask
    QcMask merged = verifySingleUnrejectedMask(result);
    QcMaskVersion mergedVersion = merged.getCurrentQcMaskVersion();
    verifyMergedQcMask(mergedVersion);

    // Verify correct parents
    final Collection<QcMaskVersionDescriptor> parents = mergedVersion.getParentQcMasks();
    assertEquals(2, parents.size());
    assertTrue(parents.contains(afterGapParent));
    assertTrue(parents.contains(preGapParent));

    // Verify the rejected masks have correct attributes
    assertRejected(testData.qcMaskChan1AfterGap.getId(), result, merged.getId());
    assertRejected(testData.qcMaskChan1.getId(), result, merged.getId());


  }

  private QcMask verifySingleUnrejectedMask(Collection<QcMask> result) {
    final Predicate<QcMask> unrejectedMaskPredicate = q -> !q.getCurrentQcMaskVersion()
        .isRejected();
    assertEquals(1, result.stream().filter(unrejectedMaskPredicate).count());

    Optional<QcMask> mergedOptional = result.stream().filter(unrejectedMaskPredicate).findAny();
    assertTrue(mergedOptional.isPresent());
    assertEquals(1, mergedOptional.get().qcMaskVersions().count());

    return mergedOptional.get();
  }

  @Test
  public void testMergeThreeNewAndNoExistingMasks() {

    Collection<QcMask> newMasks = Arrays
        .asList(testData.qcMaskChan1, testData.qcMaskChan1AfterGap, testData.qcMaskNewOverlap);
    Collection<QcMask> existingMasks = Collections.emptyList();
    Collection<QcMask> result = MergeQcMasks
        .merge(newMasks, existingMasks, threshold);

    // Expect 1 returned masks: the newly merged mask
    assertEquals(1, result.size());
    assertFalse(result.contains(testData.qcMaskChan1));
    assertFalse(result.contains(testData.qcMaskChan1AfterGap));
    assertFalse(result.contains(testData.qcMaskNewOverlap));

    // Find and verify the new merged mask
    QcMask merged = result.iterator().next();
    assertEquals(1, merged.qcMaskVersions().count());

    QcMaskVersion mergedVersion = merged.getCurrentQcMaskVersion();
    verifyMergedQcMask(mergedVersion);
    assertEquals(Collections.emptyList(), mergedVersion.getParentQcMasks());

  }

  @Test
  public void testMergeTwoMasksExistingInsideNew() {
    Instant start = Instant.ofEpochSecond(0);
    Instant end = Instant.ofEpochSecond(300);

    UUID processingChannelId = UUID.randomUUID();
    QcMask newMask = QcMask
        .create(processingChannelId, Collections.emptyList(), Collections.emptyList(),
            QcMaskCategory.STATION_SOH, QcMaskType.STATION_SECURITY, "test", start, end);

    QcMask existingMask = QcMask
        .create(processingChannelId, Collections.emptyList(), Collections.emptyList(),
            QcMaskCategory.STATION_SOH, QcMaskType.STATION_SECURITY, "test", start.plusSeconds(10),
            end.minusSeconds(10));

    Collection<QcMask> result = MergeQcMasks
        .merge(Collections.singleton(newMask), Collections.singleton(existingMask), threshold);

    assertEquals(1, result.size());
    assertTrue(result.contains(existingMask));

    QcMask merged = result.iterator().next();
    assertEquals(2, merged.qcMaskVersions().count());

    QcMaskVersion mergedVersion = merged.getCurrentQcMaskVersion();
    assertTrue(mergedVersion.getStartTime().isPresent());
    assertEquals(start, mergedVersion.getStartTime().get());
    assertTrue(mergedVersion.getEndTime().isPresent());
    assertEquals(end, mergedVersion.getEndTime().get());

  }

  @Test
  public void testMergeTwoMasksNewInsideExisting() {
    Instant start = Instant.ofEpochSecond(0);
    Instant end = Instant.ofEpochSecond(300);

    UUID processingChannelId = UUID.randomUUID();
    QcMask newMask = QcMask
        .create(processingChannelId, Collections.emptyList(), Collections.emptyList(),
            QcMaskCategory.STATION_SOH, QcMaskType.STATION_SECURITY, "test", start.plusSeconds(10),
            end.minusSeconds(10));

    QcMask existingMask = QcMask
        .create(processingChannelId, Collections.emptyList(), Collections.emptyList(),
            QcMaskCategory.STATION_SOH, QcMaskType.STATION_SECURITY, "test", start, end);

    Collection<QcMask> result = MergeQcMasks
        .merge(Collections.singleton(newMask), Collections.singleton(existingMask), threshold);

    assertEquals(1, result.size());
    assertTrue(result.contains(existingMask));

    QcMask merged = result.iterator().next();
    assertEquals(1, merged.qcMaskVersions().count());

    QcMaskVersion mergedVersion = merged.getCurrentQcMaskVersion();
    assertTrue(mergedVersion.getStartTime().isPresent());
    assertEquals(start, mergedVersion.getStartTime().get());
    assertTrue(mergedVersion.getEndTime().isPresent());
    assertEquals(end, mergedVersion.getEndTime().get());

  }

  private void verifyMergedQcMask(QcMaskVersion mergedVersion) {
    assertEquals(testData.start1, mergedVersion.getStartTime().get());
    assertEquals(testData.endMerged2, mergedVersion.getEndTime().get());
    assertEquals(QcMaskType.CALIBRATION, mergedVersion.getType().get());
    assertEquals(QcMaskUtility.getSystemRationale(AcquiredChannelSohType.CALIBRATION_UNDERWAY),
        mergedVersion.getRationale());
  }

  private void assertRejected(UUID qcMaskId, Collection<QcMask> result, UUID id) {
    Optional<QcMask> actual = result.stream().filter(q -> q.getId().equals(qcMaskId)).findAny();
    assertTrue(actual.isPresent());
    assertEquals(2, actual.get().qcMaskVersions().count());
    assertTrue(actual.get().getCurrentQcMaskVersion().isRejected());

    final String expectedRationale = "Merged to form QcMask with ID: " + id;
    assertEquals(expectedRationale, actual.get().getCurrentQcMaskVersion().getRationale());
  }

  @Test
  public void testMergeReturnsSingleNewMaskExactly() {
    Collection<QcMask> newMasks = Collections.singleton(testData.qcMaskNewOverlap);
    Collection<QcMask> result = MergeQcMasks
        .merge(newMasks, Collections.emptyList(), threshold);

    assertEquals(1, result.size());
    assertEquals(testData.qcMaskNewOverlap, result.iterator().next());
  }

  @Test
  public void testMergeThresholdNonInclusive() {
    Duration maskSeparation = Duration.ofMillis(25);

    Collection<QcMask> newMasks = Collections.singleton(testData.qcMaskNewOverlap);
    Collection<QcMask> existingMasks = Arrays
        .asList(testData.qcMaskChan1, testData.qcMaskChan1NoOverlap);
    Collection<QcMask> result = MergeQcMasks
        .merge(newMasks, existingMasks, maskSeparation);

    // Verify the two masks are actually separated by maskSeparation
    final Instant start = testData.qcMaskNewOverlap.getCurrentQcMaskVersion().getStartTime().get();
    final Instant end = testData.qcMaskChan1.getCurrentQcMaskVersion().getEndTime().get();
    assertEquals(maskSeparation, Duration.between(end, start));

    // Verify only the new mask is returned
    assertEquals(1, result.size());

    QcMask newMask = result.iterator().next();
    assertEquals(testData.qcMaskNewOverlap.getId(), newMask.getId());
    assertEquals(1, newMask.qcMaskVersions().count());
  }

  @Test
  public void testMergeNullArguments() {
    assertThrows(NullPointerException.class,
        () -> MergeQcMasks.merge(null, Collections.emptyList(), threshold),
        "Null New Masks");

    assertThrows(NullPointerException.class,
        () -> MergeQcMasks.merge(List.of(testData.qcMaskNewOverlap), null, threshold),
        "Null Existing Masks");

    assertThrows(NullPointerException.class,
        () -> MergeQcMasks.merge(List.of(testData.qcMaskNewOverlap), Collections.emptyList(), null),
        "Null Threshold");

    assertDoesNotThrow(
        () -> MergeQcMasks.merge(List.of(testData.qcMaskNewOverlap), Collections.emptyList(), threshold),
        "None Null");

  }

  @Test
  public void testMergeIllegalArguments() {
    Executable differentCategories = () -> assertThrows(IllegalArgumentException.class,
        () -> MergeQcMasks.merge(Collections.singleton(testData.analystDefinedOverlap),
            List.of(testData.systemDefinedOverlap), threshold),
        "Different Categories");

    Executable differentExistingTypes = () -> assertThrows(IllegalArgumentException.class,
        () -> MergeQcMasks.merge(Collections.singleton(testData.qcMaskNewOverlap),
            List.of(testData.qcMaskChan1, testData.qcMaskChan1DiffType), threshold),
        "Different Existing Types");

    Executable differentNewAndExistingTypes = () -> assertThrows(IllegalArgumentException.class,
        () -> MergeQcMasks.merge(Collections.singleton(testData.qcMaskNewOverlap),
            Collections.singletonList(testData.qcMaskChan1DiffType), threshold),
        "Different New and Existing Types");

    Executable differentNewTypes = () -> assertThrows(IllegalArgumentException.class,
        () -> MergeQcMasks.merge(List.of(testData.qcMaskNewOverlap, testData.qcMaskChan1DiffType),
            Collections.singletonList(testData.qcMaskChan1), threshold),
        "Different New Types");

    Executable differentExistingChannels = () -> assertThrows(IllegalArgumentException.class,
        () -> MergeQcMasks.merge(Collections.singleton(testData.qcMaskNewOverlap),
            List.of(testData.qcMaskChan1, testData.qcMaskChan2), threshold),
        "Different Existing Channels");

    Executable differentNewAndExistingChannels = () -> assertThrows(IllegalArgumentException.class,
        () -> MergeQcMasks.merge(Collections.singleton(testData.qcMaskNewOverlap),
            Collections.singletonList(testData.qcMaskChan2), threshold),
        "Different New And Existing Channels");

    Executable differentNewChannels = () -> assertThrows(IllegalArgumentException.class,
        () -> MergeQcMasks.merge(List.of(testData.qcMaskNewOverlap, testData.qcMaskChan2),
            Collections.singletonList(testData.qcMaskChan1), threshold),
        "Different New Channels");

    Executable noNewMasks = () -> assertThrows(IllegalArgumentException.class,
        () -> MergeQcMasks
            .merge(Collections.emptyList(), Collections.singletonList(testData.qcMaskChan1),
                threshold),
        "No New Masks");

    Executable rejectedMasks = () -> assertThrows(IllegalArgumentException.class,
        () -> {
          testData.qcMaskChan1.reject("Rejected", Collections.emptyList());
          MergeQcMasks.merge(Collections.singletonList(testData.qcMaskNewOverlap),
              Collections.singletonList(testData.qcMaskChan1), threshold);
        },
        "Rejected Masks");

    assertAll("MergeQcMasks.merge illegal arguments:",
        differentCategories, differentExistingTypes, differentNewAndExistingTypes,
        differentNewTypes, differentExistingChannels, differentNewAndExistingChannels,
        differentNewChannels, noNewMasks, rejectedMasks);
  }

}
