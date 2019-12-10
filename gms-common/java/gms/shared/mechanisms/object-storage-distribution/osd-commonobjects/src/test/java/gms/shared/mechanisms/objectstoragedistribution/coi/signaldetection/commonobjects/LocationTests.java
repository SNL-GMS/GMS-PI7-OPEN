package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;

import static junit.framework.TestCase.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.SignalDetectionTestFixtures;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class LocationTests {
  private final double latitude = -1.2;
  private final double longitude = 3.4;
  private final double elevation = 5678.9;
  private final double depth = 123.4;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.location,
        Location.class);
  }

  @Test
  public void testFrom() {
    Location location = Location.from(latitude, longitude, depth, elevation);
    assertEquals(latitude, location.getLatitudeDegrees());
    assertEquals(longitude, location.getLongitudeDegrees());
    assertEquals(elevation, location.getElevationKm());
    assertEquals(depth, location.getDepthKm());
  }
}
