package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.SignalDetectionTestFixtures;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link QcMask) creation and usage semantics
 */
public class QcMaskTests {

  private final UUID channelId = UUID
      .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
  private final UUID channelSegmentId1 = UUID.randomUUID();
  private final UUID channelSegmentId2 = UUID.randomUUID();
  private final List<UUID> channelSegmentIdList = Arrays
      .asList(channelSegmentId1, channelSegmentId2);
  private final QcMaskType qcMaskType1 = QcMaskType.LONG_GAP;
  private final QcMaskType qcMaskType2 = QcMaskType.SPIKE;
  private final QcMaskCategory qcMaskCategory = QcMaskCategory.WAVEFORM_QUALITY;
  private final String rationale = "Rationale";
  private final Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");
  private final Instant endTime = Instant.parse("2007-12-03T11:15:30.00Z");
  private final QcMaskVersionDescriptor parent1 = QcMaskVersionDescriptor
      .from(UUID.randomUUID(), 3);
  private final QcMaskVersionDescriptor parent2 = QcMaskVersionDescriptor
      .from(UUID.randomUUID(), 1);
  private final List<QcMaskVersionDescriptor> parents = Arrays.asList(parent1, parent2);

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.qcMask,
        QcMask.class);
  }

  @Test
  public void testFromNullParameters() throws Exception {
    QcMaskVersion qcMaskVersion = QcMaskVersion.from(0, Collections.emptyList(),
        channelSegmentIdList, qcMaskCategory, qcMaskType1, rationale, startTime, endTime);

    TestUtilities.checkStaticMethodValidatesNullArguments(QcMask.class, "from",
        new UUID(0L, 0L), channelId, Collections.singletonList(qcMaskVersion));
  }

  @Test
  public void testFrom() {
    UUID qcMaskId = UUID.randomUUID();
    QcMaskVersion qcMaskVersion = QcMaskVersion.from(0L, Collections.emptyList(),
            channelSegmentIdList, qcMaskCategory, qcMaskType1, rationale, startTime,
        endTime);

    QcMask qcMask = QcMask
        .from(qcMaskId, channelId, Collections.singletonList(qcMaskVersion));

    assertEquals(qcMaskId, qcMask.getId());
    assertEquals(channelId, qcMask.getChannelId());
    assertEquals(1, qcMask.qcMaskVersions().count());

    QcMaskVersion currentVersion = qcMask.getCurrentQcMaskVersion();
    assertEquals(0L, currentVersion.getVersion());
    assertEquals(channelSegmentIdList.get(0),
        currentVersion.getChannelSegmentIds().get(0));
    assertEquals(channelSegmentIdList.get(1),
        currentVersion.getChannelSegmentIds().get(1));
    assertTrue(currentVersion.getType().isPresent());
    assertEquals(qcMaskType1, currentVersion.getType().get());
    assertEquals(qcMaskCategory, currentVersion.getCategory());
    assertEquals(rationale, currentVersion.getRationale());
    assertTrue(currentVersion.getStartTime().isPresent());
    assertEquals(startTime, currentVersion.getStartTime().get());
    assertTrue(currentVersion.getEndTime().isPresent());
    assertEquals(endTime, currentVersion.getEndTime().get());
    assertTrue(currentVersion.getParentQcMasks().isEmpty());
  }

  @Test
  public void testCreateNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(QcMask.class, "create",
        channelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
        rationale, startTime, endTime);
  }

  @Test
  public void testCreateRejectedCategoryExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot create QcMask with REJECTED QcMaskCategory");

    QcMask.create(channelId, parents, channelSegmentIdList, QcMaskCategory.REJECTED,
        qcMaskType1, rationale, startTime, endTime);
  }

  @Test
  public void testCreate() {
    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
            rationale, startTime, endTime);

    assertEquals(UUID.class, qcMask.getId().getClass());
    assertEquals(channelId, qcMask.getChannelId());
    assertEquals(1, qcMask.qcMaskVersions().count());

    QcMaskVersion currentVersion = qcMask.getCurrentQcMaskVersion();
    assertEquals(0L, currentVersion.getVersion());
    assertEquals(2, currentVersion.getParentQcMasks().size());
    assertTrue(currentVersion.getParentQcMasks().contains(parent1));
    assertTrue(currentVersion.getParentQcMasks().contains(parent2));
    assertEquals(channelSegmentIdList.get(0),
        currentVersion.getChannelSegmentIds().get(0));
    assertEquals(channelSegmentIdList.get(1),
        currentVersion.getChannelSegmentIds().get(1));
    assertTrue(currentVersion.getType().isPresent());
    assertEquals(qcMaskType1, currentVersion.getType().get());
    assertEquals(qcMaskCategory, currentVersion.getCategory());
    assertEquals(rationale, currentVersion.getRationale());
    assertTrue(currentVersion.getStartTime().isPresent());
    assertEquals(startTime, currentVersion.getStartTime().get());
    assertTrue(currentVersion.getEndTime().isPresent());
    assertEquals(endTime, currentVersion.getEndTime().get());
  }

  @Test
  public void testAddQcMaskVersionNullParameters() throws Exception {
    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
            rationale, startTime, endTime);

    TestUtilities.checkMethodValidatesNullArguments(qcMask, "addQcMaskVersion",
        Collections.emptyList(), QcMaskCategory.WAVEFORM_QUALITY, QcMaskType.SPIKE,
        "Rationale SPIKE", Instant.parse("2007-12-03T10:35:30.00Z"),
        Instant.parse("2007-12-03T11:45:30.00Z"));
  }

  @Test
  public void testAddQcMaskVersionRejectedCategoryExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot add QcMaskVersion with REJECTED QcMaskCategory");

    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
            rationale, startTime, endTime);

    List<UUID> newVersionChannelSegmentIdList = Arrays
        .asList(UUID.randomUUID(), UUID.randomUUID());

    Instant newStartTime = Instant.parse("2007-12-03T10:35:30.00Z");
    Instant newEndTime = Instant.parse("2007-12-03T10:45:30.00Z");

    qcMask.addQcMaskVersion(newVersionChannelSegmentIdList, QcMaskCategory.REJECTED,
        QcMaskType.SPIKE, "Rationale SPIKE", newStartTime, newEndTime);
  }

  @Test
  public void testAddQcMaskVersion() {
    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
            rationale, startTime, endTime);

    List<UUID> newVersionChannelSegmentIdList = Arrays
        .asList(UUID.randomUUID(), UUID.randomUUID());

    Instant newStartTime = Instant.parse("2007-12-03T10:35:30.00Z");
    Instant newEndTime = Instant.parse("2007-12-03T10:45:30.00Z");
    UUID newCreationInfoId = UUID.randomUUID();

    qcMask.addQcMaskVersion(newVersionChannelSegmentIdList, QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.SPIKE, "Rationale SPIKE", newStartTime, newEndTime);

    assertEquals(2, qcMask.qcMaskVersions().count());

    QcMaskVersion currentVersion = qcMask.getCurrentQcMaskVersion();
    assertEquals(1L, currentVersion.getVersion());
    assertEquals(newVersionChannelSegmentIdList.get(0),
        currentVersion.getChannelSegmentIds().get(0));
    assertEquals(newVersionChannelSegmentIdList.get(1),
        currentVersion.getChannelSegmentIds().get(1));
    assertEquals(QcMaskType.SPIKE, currentVersion.getType().get());
    assertEquals(QcMaskCategory.WAVEFORM_QUALITY, currentVersion.getCategory());
    assertEquals("Rationale SPIKE", currentVersion.getRationale());
    assertEquals(newStartTime, currentVersion.getStartTime().get());
    assertEquals(newEndTime, currentVersion.getEndTime().get());

    assertEquals(1, currentVersion.getParentQcMasks().size());

    QcMaskVersionDescriptor parentQcMask = currentVersion.getParentQcMasks().iterator().next();

    assertEquals(qcMask.getId(), parentQcMask.getQcMaskId());
    assertEquals(0, parentQcMask.getQcMaskVersionId());
  }

  @Test
  public void testRejectNullParameters() throws Exception {
    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
            rationale, startTime, endTime);

    TestUtilities
        .checkMethodValidatesNullArguments(qcMask, "reject", rationale, channelSegmentIdList);
  }

  @Test
  public void testReject() {
    QcMask qcMask = QcMask
        .create(channelId, parents, channelSegmentIdList, qcMaskCategory, qcMaskType1,
            rationale, startTime, endTime);

    final QcMaskVersionDescriptor expectedParentReference = QcMaskVersionDescriptor
        .from(qcMask.getId(), qcMask.getCurrentQcMaskVersion().getVersion());

    qcMask.reject("Rejected Rationale", Collections.emptyList());
    assertEquals(2, qcMask.qcMaskVersions().count());

    QcMaskVersion currentVersion = qcMask.getCurrentQcMaskVersion();
    assertEquals(1L, currentVersion.getVersion());
    assertEquals(QcMaskCategory.REJECTED, currentVersion.getCategory());
    assertEquals("Rejected Rationale", currentVersion.getRationale());
    assertEquals(true, currentVersion.isRejected());
    assertEquals(Collections.singletonList(expectedParentReference),
        currentVersion.getParentQcMasks());
    assertEquals(Collections.emptyList(), currentVersion.getChannelSegmentIds());
    assertFalse(currentVersion.getType().isPresent());
    assertFalse(currentVersion.getStartTime().isPresent());
    assertFalse(currentVersion.getEndTime().isPresent());
  }

  @Test
  public void testEqualsHashCode() {
    final QcMask a = QcMask.create(channelId, parents, channelSegmentIdList,
        qcMaskCategory, qcMaskType1,
        rationale, startTime, endTime);

    final QcMask b = QcMask
        .from(a.getId(), channelId, a.qcMaskVersions().collect(Collectors.toList()));

    assertEquals(a, b);
    assertEquals(b, a);
    assertEquals(a.hashCode(), b.hashCode());
  }

  @Test
  public void testEqualsExpectInequality() {
    final QcMask a = QcMask.create(channelId, parents, channelSegmentIdList,
        qcMaskCategory, qcMaskType1,
        rationale, startTime, endTime);

    // Different ID
    QcMask b = QcMask.create(channelId, parents, channelSegmentIdList, qcMaskCategory,
        qcMaskType1,
        rationale, startTime, endTime);
    assertNotEquals(a, b);

    // Different processing channel id
    b = QcMask.from(a.getId(), UUID.fromString("f66fbfc7-98a1-4e11-826b-968d80ef36eb"),
        a.qcMaskVersions().collect(Collectors.toList()));
    assertNotEquals(a, b);

    // Different versions
    b = QcMask.from(a.getId(), UUID.fromString("f66fbfc7-98a1-4e11-826b-968d80ef36eb"),
        Collections.emptyList());
    assertNotEquals(a, b);
  }
}
