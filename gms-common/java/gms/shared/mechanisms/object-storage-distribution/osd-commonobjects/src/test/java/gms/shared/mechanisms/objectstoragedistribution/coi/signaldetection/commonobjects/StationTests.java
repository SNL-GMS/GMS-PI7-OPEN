package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.SignalDetectionTestFixtures;
import org.junit.Test;


/**
 *
 */
public class StationTests {

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.station,
            Station.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(Station.class);
  }

  @Test
  public void fromOperationValidationTest() throws Exception {

    TestUtilities.checkStaticMethodValidatesNullArguments(
            Station.class, "from",
            SignalDetectionTestFixtures.stationID, SignalDetectionTestFixtures.stationName, SignalDetectionTestFixtures.description, SignalDetectionTestFixtures.stationType,
            SignalDetectionTestFixtures.lat, SignalDetectionTestFixtures.lon, SignalDetectionTestFixtures.elev,
            SignalDetectionTestFixtures.sites);
  }

  @Test
  public void createOperationValidationTest() throws Exception {

    TestUtilities.checkStaticMethodValidatesNullArguments(
            Station.class, "create",
            SignalDetectionTestFixtures.stationName, SignalDetectionTestFixtures.description, SignalDetectionTestFixtures.stationType,
            SignalDetectionTestFixtures.lat, SignalDetectionTestFixtures.lon, SignalDetectionTestFixtures.elev,
            SignalDetectionTestFixtures.sites);
  }

  @Test(expected = Exception.class)
  public void testEmptyName() {
    Station.create("", SignalDetectionTestFixtures.description, SignalDetectionTestFixtures.stationType,

            SignalDetectionTestFixtures.lat, SignalDetectionTestFixtures.lon, SignalDetectionTestFixtures.elev,
            SignalDetectionTestFixtures.sites);
  }
}
