package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import java.util.UUID;
import org.junit.Test;

public class LocationBehaviorTests {

  @Test
  public void testFrom() {
    final UUID fpId = UUID.randomUUID(), fmId = UUID.randomUUID();
    LocationBehavior loc = LocationBehavior.from(EventTestFixtures.residual, EventTestFixtures.weight,
        EventTestFixtures.isDefining, fpId, fmId);
    assertEquals(EventTestFixtures.residual, loc.getResidual(), 0.001);
    assertEquals(EventTestFixtures.weight, loc.getWeight(), 0.001);
    assertEquals(EventTestFixtures.isDefining, loc.isDefining());
    assertEquals(fpId, loc.getFeaturePredictionId());
    assertEquals(fmId, loc.getFeatureMeasurementId());
  }

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(EventTestFixtures.locationBehavior, LocationBehavior.class);
  }
}
