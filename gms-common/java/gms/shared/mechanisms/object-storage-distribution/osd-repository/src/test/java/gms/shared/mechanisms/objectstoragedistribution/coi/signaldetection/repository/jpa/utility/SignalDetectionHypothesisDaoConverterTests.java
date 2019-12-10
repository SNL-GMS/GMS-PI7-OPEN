package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.repository.jpa.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects.SignalDetectionHypothesisDao;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SignalDetectionHypothesisDaoConverterTests {

  private final UUID id = UUID.randomUUID();
  private final SignalDetection parentSignalDetection = new SignalDetectionDaoConverterTests().getTestSignalDetection();
  private boolean rejected = false;
  private Set<FeatureMeasurement<?>> featureMeasurements =
      Set.of(TestFixtures.arrivalTimeMeasurement, TestFixtures.phaseMeasurement);
  private final UUID creationInfoId = UUID.randomUUID();
  private final SignalDetectionHypothesis signalDetectionHypothesis =
      SignalDetectionHypothesis.from(id, parentSignalDetection.getId(), rejected, featureMeasurements, creationInfoId);

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testToDaoNullSignalDetectionHypothesisExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create SignalDetectionHypothesisDao from a null SignalDetectionHypothesis");
    SignalDetectionHypothesisDaoConverter.toDao(SignalDetectionDaoConverter.toDao(parentSignalDetection), null);
  }

  @Test
  public void testToDaoNullSignalDetectionDaoExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create SignalDetectionHypothesisDao from a null SignalDetectionDao");
    SignalDetectionHypothesisDaoConverter.toDao(null, signalDetectionHypothesis);
  }

  @Test
  public void testFromDaoNullSignalDetectionHypothesisExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create SignalDetectionHypothesis from a null SignalDetectionHypothesisDao");
    SignalDetectionHypothesisDaoConverter.fromDao(null);
  }

  @Test
  public void testToFromDao() {

    final SignalDetectionHypothesisDao dao = SignalDetectionHypothesisDaoConverter.toDao(
        SignalDetectionDaoConverter.toDao(parentSignalDetection), signalDetectionHypothesis);
    assertNotNull(dao);
    assertEquals(id, dao.getSignalDetectionHypothesisId());
    assertEquals(parentSignalDetection.getId(), dao.getParentSignalDetection().getSignalDetectionId());
    assertEquals(rejected, dao.isRejected());
    assertEquals(creationInfoId, dao.getCreationInfoId());

    final SignalDetectionHypothesis signalDetectionHypothesis2 = SignalDetectionHypothesisDaoConverter.fromDao(dao);
    assertNotNull(signalDetectionHypothesis2);
    assertEquals(id, signalDetectionHypothesis2.getId());
    assertEquals(parentSignalDetection.getId(), signalDetectionHypothesis2.getParentSignalDetectionId());
    assertEquals(rejected, signalDetectionHypothesis2.isRejected());
    assertEquals(featureMeasurements, new HashSet<>(signalDetectionHypothesis2.getFeatureMeasurements()));
    assertEquals(creationInfoId, signalDetectionHypothesis2.getCreationInfoId());
  }
}
