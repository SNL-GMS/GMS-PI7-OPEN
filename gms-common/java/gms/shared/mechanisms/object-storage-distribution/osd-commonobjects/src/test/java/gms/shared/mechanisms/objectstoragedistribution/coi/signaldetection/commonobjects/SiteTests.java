package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.SignalDetectionTestFixtures;
import org.junit.Test;


/**
 *
 */
public class SiteTests {

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.site,
        Site.class);
  }

  @Test
  public void equalsAndHashcodeTest() {
    TestUtilities.checkClassEqualsAndHashcode(Site.class);
  }

  @Test
  public void fromOperationValidationTest() throws Exception {

    TestUtilities.checkStaticMethodValidatesNullArguments(
        Site.class, "from",
        SignalDetectionTestFixtures.siteID, SignalDetectionTestFixtures.siteName,
        SignalDetectionTestFixtures.lat, SignalDetectionTestFixtures.lon, SignalDetectionTestFixtures.elev,
        SignalDetectionTestFixtures.channels);
  }

  @Test
  public void createOperationValidationTest() throws Exception {

    TestUtilities.checkStaticMethodValidatesNullArguments(
        Site.class, "create",
        SignalDetectionTestFixtures.siteName,
        SignalDetectionTestFixtures.lat, SignalDetectionTestFixtures.lon, SignalDetectionTestFixtures.elev,
        SignalDetectionTestFixtures.channels);
  }

  @Test(expected = Exception.class)
  public void testEmptyName() {
    Site.create("",
        SignalDetectionTestFixtures.lat, SignalDetectionTestFixtures.lon, SignalDetectionTestFixtures.elev,
        SignalDetectionTestFixtures.channels);
  }
}


