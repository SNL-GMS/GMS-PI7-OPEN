package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskVersionDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskVersionDescriptorDao;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class QcMaskVersionDaoConverterTests {

  private final UUID channelId = UUID.randomUUID();
  private final List<QcMaskVersionDescriptor> parents = Arrays.asList(
      QcMaskVersionDescriptor.from(UUID.randomUUID(), 1),
      QcMaskVersionDescriptor.from(UUID.randomUUID(), 2));
  private final UUID channelSegmentId1 = UUID.randomUUID();
  private final UUID channelSegmentId2 = UUID.randomUUID();
  private final List<UUID> channelSegmentIdList = Arrays
      .asList(channelSegmentId1, channelSegmentId2);
  private final QcMaskType qcMaskType1 = QcMaskType.LONG_GAP;
  private final QcMaskCategory qcMaskCategory = QcMaskCategory.WAVEFORM_QUALITY;

  private final String rationale = "Rationale";
  private final Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");
  private final Instant endTime = Instant.parse("2007-12-03T11:15:30.00Z");

  private QcMask qcMask;
  private QcMask qcMaskRejected;

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Before
  public void setUp() {
    qcMask = QcMask
        .create(this.channelId, parents, this.channelSegmentIdList, this.qcMaskCategory,
            this.qcMaskType1,
            this.rationale, this.startTime, this.endTime);
    qcMaskRejected = QcMask
        .create(this.channelId, parents, this.channelSegmentIdList, this.qcMaskCategory,
            this.qcMaskType1,
            this.rationale, this.startTime, this.endTime);
    qcMaskRejected.reject("Rejected", this.channelSegmentIdList);
  }

  @Test
  public void testToDaoNullVersionExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create QcMaskVersionDao from a null QcMaskVersion");
    QcMaskVersionDaoConverter.toDao(QcMaskDaoConverter.toDao(qcMask), null);
  }

  @Test
  public void testToDaoNullMaskExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create QcMaskVersionDao from a null QcMask");
    QcMaskVersionDaoConverter.toDao(null, qcMask.getCurrentQcMaskVersion());
  }

  @Test
  public void testFromDaoNullQcMaskExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create QcMaskVersion from a null QcMaskVersionDao");
    QcMaskVersionDaoConverter.fromDao(null);
  }

  @Test
  public void testToDao() {
    final QcMaskVersion qcMaskVersion = qcMask.getCurrentQcMaskVersion();
    final QcMaskDao qcMaskDao = QcMaskDaoConverter.toDao(qcMask);
    QcMaskVersionDao dao = QcMaskVersionDaoConverter.toDao(qcMaskDao, qcMaskVersion);

    assertNotNull(dao);
    verifyVersionDao(qcMaskDao, qcMaskVersion, dao);
  }

  @Test
  public void testToDaoRejected() {
    final QcMaskVersion qcMaskVersion = qcMaskRejected.getCurrentQcMaskVersion();
    final QcMaskDao qcMaskDao = QcMaskDaoConverter.toDao(qcMaskRejected);
    QcMaskVersionDao dao = QcMaskVersionDaoConverter.toDao(qcMaskDao, qcMaskVersion);

    assertNotNull(dao);
    verifyVersionDao(qcMaskDao, qcMaskVersion, dao);
  }

  @Test
  public void testToDaoFromDao() {
    final QcMaskVersion expectedVersion = qcMask.getCurrentQcMaskVersion();
    final QcMaskDao qcMaskDao = QcMaskDaoConverter.toDao(qcMask);

    QcMaskVersionDao dao = QcMaskVersionDaoConverter.toDao(qcMaskDao, expectedVersion);

    QcMaskVersion actualVersion = QcMaskVersionDaoConverter.fromDao(dao);

    assertEquals(expectedVersion, actualVersion);
  }

  @Test
  public void testToDaoFromDaoRejected() {
    final QcMaskVersion expectedVersion = qcMaskRejected.getCurrentQcMaskVersion();
    final QcMaskDao qcMaskDao = QcMaskDaoConverter.toDao(qcMaskRejected);

    QcMaskVersionDao dao = QcMaskVersionDaoConverter.toDao(qcMaskDao, expectedVersion);

    QcMaskVersion actualVersion = QcMaskVersionDaoConverter.fromDao(dao);

    assertEquals(expectedVersion, actualVersion);
  }

  private static void verifyVersionDao(QcMaskDao qcMaskDao, QcMaskVersion version,
      QcMaskVersionDao versionDao) {
    assertEquals(qcMaskDao, versionDao.getOwnerQcMask());
    assertEquals(version.getVersion(),
        versionDao.getVersion());

    List<QcMaskVersionDescriptorDao> qcMaskVersionDescriptorDaos = version.getParentQcMasks().stream()
        .map(QcMaskVersionDescriptorDaoConverter::toDao)
        .collect(Collectors.toList());

    assertEquals(qcMaskVersionDescriptorDaos, versionDao.getParentQcMasks());

    final List<UUID> versionChanSegIds = version.getChannelSegmentIds();
    final List<UUID> versionDaoChanSegIds = versionDao
        .getChannelSegmentIds();
    assertEquals(versionChanSegIds.size(), versionDaoChanSegIds.size());

    // Verify the QcMaskVersionDao contains QcMaskVersionToChannelSegmentIdentityDao with the correct
    // ChannelSegment identifiers
    final List<UUID> versionChanSegIdDaos = version.getChannelSegmentIds();

    versionDaoChanSegIds.forEach(d -> assertTrue(versionChanSegIdDaos.contains(d)));

    assertEquals(version.getCategory(), versionDao.getCategory());
    assertEquals(version.getRationale(), versionDao.getRationale());
    if (!version.getCategory().equals(QcMaskCategory.REJECTED)) {
      assertEquals(version.getType().get(), versionDao.getType());
      assertEquals(version.getStartTime().get(), versionDao.getStartTime());
      assertEquals(version.getEndTime().get(), versionDao.getEndTime());
    } else {
      assertTrue(!version.getType().isPresent());
      Assert.assertNull(versionDao.getType());
      assertTrue(!version.getStartTime().isPresent());
      Assert.assertNull(versionDao.getStartTime());
      assertTrue(!version.getEndTime().isPresent());
      Assert.assertNull(versionDao.getEndTime());
    }
  }
}
