package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.SignalDetectionDao;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SignalDetectionDaoConverterTests {

  private static final UUID id = UUID.randomUUID();
  private static String monitoringOrganization = "CTBTO";
  private static UUID stationId = UUID.randomUUID();
  private List<SignalDetectionHypothesis> signalDetectionHypotheses = Collections.emptyList();
  private static final UUID creationInfoId = UUID.randomUUID();
  private final SignalDetection signalDetection = SignalDetection.from(id, monitoringOrganization,
      stationId, signalDetectionHypotheses, creationInfoId);

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testToDaoNullSignalDetectionExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create SignalDetectionDao from a null SignalDetection");
    SignalDetectionDaoConverter.toDao(null);
  }

  @Test
  public void testFromDaoNullSignalDetectionExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create SignalDetection from a null SignalDetectionDao");
    SignalDetectionDaoConverter.fromDao(null, signalDetectionHypotheses);
  }

  @Test
  public void testFromDaoNullSignalDetectionHypothesisExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create SignalDetection from a null signalDetectionHypotheses");

    SignalDetectionDao dao = SignalDetectionDaoConverter.toDao(signalDetection);
    SignalDetectionDaoConverter.fromDao(dao, null);
  }

  @Test
  public void testToFromDao() {

    final SignalDetectionDao dao = SignalDetectionDaoConverter.toDao(signalDetection);
    assertNotNull(dao);
    assertEquals(id, dao.getSignalDetectionId());
    assertEquals(monitoringOrganization, dao.getMonitoringOrganization());
    assertEquals(stationId, dao.getStationId());
    assertEquals(creationInfoId, dao.getCreationInfoId());

    final SignalDetection signalDetection2 = SignalDetectionDaoConverter.fromDao(dao, signalDetectionHypotheses);
    assertNotNull(signalDetection2);
    assertEquals(id, signalDetection2.getId());
    assertEquals(monitoringOrganization, signalDetection2.getMonitoringOrganization());
    assertEquals(stationId, signalDetection2.getStationId());
    assertArrayEquals(signalDetectionHypotheses.toArray(), signalDetection2.getSignalDetectionHypotheses().toArray());
    assertEquals(creationInfoId, signalDetection2.getCreationInfoId());
  }

  public SignalDetection getTestSignalDetection() { return signalDetection; }
}
