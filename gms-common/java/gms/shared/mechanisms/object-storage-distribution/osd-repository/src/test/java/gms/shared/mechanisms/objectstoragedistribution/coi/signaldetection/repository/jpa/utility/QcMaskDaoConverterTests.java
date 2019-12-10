package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskDao;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.QcMaskVersionDao;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class QcMaskDaoConverterTests {

  private final UUID channelId = UUID.randomUUID();
  private final List<QcMaskVersionDescriptor> parents = Arrays.asList(
      QcMaskVersionDescriptor.from(UUID.randomUUID(), 1),
      QcMaskVersionDescriptor.from(UUID.randomUUID(), 2));
  private final UUID channelSegmentId1 = UUID.randomUUID();
  private final List<UUID> channelSegmentIdList = Collections
      .singletonList(channelSegmentId1);
  private final QcMaskType qcMaskType1 = QcMaskType.LONG_GAP;
  private final QcMaskCategory qcMaskCategory = QcMaskCategory.WAVEFORM_QUALITY;

  private final String rationale = "Rationale";
  private final Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");
  private final Instant endTime = Instant.parse("2007-12-03T11:15:30.00Z");
  private final UUID creationInfoId = UUID.randomUUID();

  private QcMask qcMask;

  @Before
  public void setUp() {
    this.qcMask = QcMask
        .create(this.channelId, parents, this.channelSegmentIdList, this.qcMaskCategory,
            this.qcMaskType1, this.rationale, this.startTime, this.endTime);
  }

  @After
  public void tearDown() {
    this.qcMask = null;
  }

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testToDaoNullParameters() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create QcMaskDao from a null QcMask");
    QcMaskDaoConverter.toDao(null);
  }

  @Test
  public void testFromDaoNullParameters() throws Exception {
    QcMaskDao qcMaskDao = QcMaskDaoConverter.toDao(qcMask);
    QcMaskVersionDao qcMaskVersionDao = QcMaskVersionDaoConverter.toDao(qcMaskDao,
        qcMask.getCurrentQcMaskVersion());

    TestUtilities.checkStaticMethodValidatesNullArguments(QcMaskDaoConverter.class,
        "fromDao", qcMaskDao, qcMaskVersionDao);

    TestUtilities.checkStaticMethodValidatesNullArguments(QcMaskDaoConverter.class,
        "fromDao", qcMaskDao, Collections.singletonList(qcMaskVersionDao));
  }

  @Test
  public void testFromDaoEmptyQcMaskVersionDaosExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot create QcMask from an empty QcMaskVersionDaos");
    QcMaskDaoConverter.fromDao(new QcMaskDao(), Collections.emptyList());
  }

  @Test
  public void testToDao() {
    QcMaskDao dao = QcMaskDaoConverter.toDao(qcMask);

    assertNotNull(dao);
    assertEquals(qcMask.getId(), dao.getId());
    assertEquals(qcMask.getChannelId(),
        dao.getChannelId());
  }

  @Test
  public void testToDaoFromDao() {
    QcMaskDao qcMaskDao = QcMaskDaoConverter.toDao(qcMask);
    List<QcMaskVersionDao> qcMaskVersionDaos = qcMask.qcMaskVersions()
        .map(v -> QcMaskVersionDaoConverter.toDao(qcMaskDao, v))
        .collect(Collectors.toList());

    QcMask actualQcMask = QcMaskDaoConverter.fromDao(qcMaskDao, qcMaskVersionDaos);

    assertEquals(qcMask, actualQcMask);
  }
}
