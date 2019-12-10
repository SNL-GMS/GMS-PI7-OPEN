package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.SignalDetectionTestFixtures;
import java.util.UUID;
import org.junit.Test;


/**
 * Created by dvanwes on 9/29/17.
 */
public class CalibrationTests {

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.calibration,
        Calibration.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(Calibration.class);
  }

  @Test
  public void createOperationValidationTest() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
            Calibration.class, "create",
        1.0, 2.0, 3.0, 4.0);
  }

  @Test
  public void fromOperationValidationTest() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
            Calibration.class, "from",
        UUID.randomUUID(), 1.0, 2.0, 3.0, 4.0);
  }

}
