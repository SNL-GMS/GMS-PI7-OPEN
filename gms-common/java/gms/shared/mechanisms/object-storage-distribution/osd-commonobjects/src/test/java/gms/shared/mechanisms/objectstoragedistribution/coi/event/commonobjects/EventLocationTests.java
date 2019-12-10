package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import org.junit.Test;

public class EventLocationTests {

  @Test
  public void testFrom() {
      EventLocation loc = EventLocation.from(EventTestFixtures.lat, EventTestFixtures.lon,
        EventTestFixtures.depth, EventTestFixtures.time);
    assertEquals(EventTestFixtures.lat, loc.getLatitudeDegrees(), 0.001);
    assertEquals(EventTestFixtures.lon, loc.getLongitudeDegrees(), 0.001);
    assertEquals(EventTestFixtures.depth, loc.getDepthKm(), 0.001);
    assertEquals(EventTestFixtures.time, loc.getTime());
  }

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(EventTestFixtures.location, EventLocation.class);
  }

}
