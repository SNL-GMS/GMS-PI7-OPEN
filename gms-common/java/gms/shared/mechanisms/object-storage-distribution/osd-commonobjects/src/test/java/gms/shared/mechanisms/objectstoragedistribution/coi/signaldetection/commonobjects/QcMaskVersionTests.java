package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.SignalDetectionTestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion.Builder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests {@link QcMaskVersion} factory creation
 *
 * Created by jrhipp on 9/13/17.
 */
class QcMaskVersionTests {

  private final long qcMaskVersionId = 6L;
  private final UUID qcMaskVersionParentId = UUID
      .fromString("b38ae749-2833-4197-a8cb-4609ddd4342f");
  private final long qcMaskVersionParentVersion = 5L;

  private final UUID channelSegmentId1 = UUID.randomUUID();
  private final UUID channelSegmentId2 = UUID.randomUUID();

  private final QcMaskType qcMaskType = QcMaskType.LONG_GAP;
  private final QcMaskCategory qcMaskCategory = QcMaskCategory.WAVEFORM_QUALITY;
  private final String rationale = "Rationale";
  private final Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");
  private final Instant endTime = Instant.parse("2007-12-03T11:15:30.00Z");

  private QcMaskVersion qcMaskVersion;

  @BeforeEach
  void setUp() {
    qcMaskVersion = QcMaskVersion.builder()
        .setVersion(qcMaskVersionId)
        .addParentQcMask(qcMaskVersionParentId, qcMaskVersionParentVersion)
        .addChannelSegmentId(channelSegmentId1)
        .addChannelSegmentId(channelSegmentId2)
        .setType(qcMaskType)
        .setCategory(qcMaskCategory)
        .setRationale(rationale)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .build();
  }

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.qcMaskVersion,
        QcMaskVersion.class);
  }

  @Test
  void testBuildRejectedOptionalsValidated() {
    Supplier<Builder> rejectedVersionSupplier = () -> QcMaskVersion.builder()
        .setVersion(6L)
        .addParentQcMask(qcMaskVersionParentId, qcMaskVersionParentVersion)
        .addChannelSegmentId(channelSegmentId1)
        .addChannelSegmentId(channelSegmentId2)
        .setCategory(QcMaskCategory.REJECTED)
        .setRationale(rationale);

    //permute the cases, only when none of the three are set should we successfully validate
    assertDoesNotThrow(rejectedVersionSupplier.get()::build);

    assertThrows(IllegalStateException.class,
        () -> rejectedVersionSupplier.get().setType(qcMaskType).build());

    assertThrows(IllegalStateException.class,
        () -> rejectedVersionSupplier.get().setStartTime(startTime).build());

    assertThrows(IllegalStateException.class,
        () -> rejectedVersionSupplier.get().setEndTime(endTime).build());

    assertThrows(IllegalStateException.class,
        () -> rejectedVersionSupplier.get().setType(qcMaskType)
            .setStartTime(startTime).build());

    assertThrows(IllegalStateException.class,
        () -> rejectedVersionSupplier.get().setStartTime(startTime)
            .setEndTime(endTime).build());

    assertThrows(IllegalStateException.class,
        () -> rejectedVersionSupplier.get().setType(qcMaskType).setEndTime(endTime)
            .build());
  }

  @Test
  void testBuildNotRejectedCheckOptionalsValidated() {

    Supplier<QcMaskVersion.Builder> notRejectedVersionSupplier =
        () -> QcMaskVersion.builder()
        .setVersion(6L)
        .addParentQcMask(qcMaskVersionParentId, qcMaskVersionParentVersion)
        .addChannelSegmentId(channelSegmentId1)
        .addChannelSegmentId(channelSegmentId2)
        .setCategory(qcMaskCategory)
        .setRationale(rationale);

    //permute the cases, only when all three are set should we successfully validate
    assertThrows(IllegalStateException.class,
        notRejectedVersionSupplier.get()::build);

    assertThrows(IllegalStateException.class,
        () -> notRejectedVersionSupplier.get().setType(qcMaskType).build());

    assertThrows(IllegalStateException.class,
        () -> notRejectedVersionSupplier.get().setStartTime(startTime).build());

    assertThrows(IllegalStateException.class,
        () -> notRejectedVersionSupplier.get().setEndTime(endTime).build());

    assertThrows(IllegalStateException.class,
        () -> notRejectedVersionSupplier.get().setType(qcMaskType)
            .setStartTime(startTime).build());

    assertThrows(IllegalStateException.class,
        () -> notRejectedVersionSupplier.get().setStartTime(startTime)
            .setEndTime(endTime).build());

    assertThrows(IllegalStateException.class,
        () -> notRejectedVersionSupplier.get().setType(qcMaskType).setEndTime(endTime)
            .build());

    assertDoesNotThrow(() -> notRejectedVersionSupplier.get().setType(qcMaskType)
        .setStartTime(startTime).setEndTime(endTime).build());

  }

  @Test
  void testBuildStartTimeAfterEndTimeExpectIllegalStateException() {
    assertThrows(IllegalStateException.class,
        () -> qcMaskVersion.toBuilder().setStartTime(endTime.plusSeconds(10)).build());
  }

  @Test
  void testBuildDuplicateParentQcMasksExpectIllegalStateException() {
    assertThrows(IllegalStateException.class,
        () -> qcMaskVersion.toBuilder()
            .addParentQcMask(qcMaskVersionParentId, qcMaskVersionParentVersion).build());
  }

  @Test
  void testHasParent() {
    assertTrue(qcMaskVersion.hasParent());
    assertFalse(qcMaskVersion.toBuilder().setParentQcMasks(List.of()).build().hasParent());
  }

  @Test
  void testTypeCategoryValidationExpectIllegalStateException() {
    // throws error ... SENSOR_PROBLEM type is not a WAVEFORM_QUALITY category.
    assertThrows(IllegalStateException.class,
        () -> qcMaskVersion.toBuilder().setType(QcMaskType.SENSOR_PROBLEM).build());
  }

}
