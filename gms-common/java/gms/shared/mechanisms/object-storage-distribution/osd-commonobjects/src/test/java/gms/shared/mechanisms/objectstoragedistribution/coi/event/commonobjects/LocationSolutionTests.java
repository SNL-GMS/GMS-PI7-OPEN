package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;


import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import java.util.UUID;
import org.junit.Test;

public class LocationSolutionTests {

  @Test
  public void testWithLocationAndRestraintOnly() {
    final LocationSolution loc = LocationSolution.withLocationAndRestraintOnly(
        EventTestFixtures.location, EventTestFixtures.locationRestraint);
    assertNotNull(loc.getId());
    assertEquals(EventTestFixtures.location, loc.getLocation());
    assertEquals(EventTestFixtures.locationRestraint, loc.getLocationRestraint());
    assertFalse(loc.getLocationUncertainty().isPresent());
    assertTrue(loc.getFeaturePredictions().isEmpty());
    assertTrue(loc.getLocationBehaviors().isEmpty());
  }

  @Test
  public void testFrom() {
    final UUID id = UUID.randomUUID();
    final LocationSolution loc = LocationSolution.from(id, EventTestFixtures.location, EventTestFixtures.locationRestraint,
        EventTestFixtures.locationUncertainty, EventTestFixtures.locationBehaviors, EventTestFixtures.featurePredictions);
    assertEquals(id, loc.getId());
    assertEquals(EventTestFixtures.location, loc.getLocation());
    assertEquals(EventTestFixtures.locationRestraint, loc.getLocationRestraint());
    assertTrue(loc.getLocationUncertainty().isPresent());
    assertEquals(EventTestFixtures.arrayLen, loc.getFeaturePredictions().size());
    assertEquals(EventTestFixtures.arrayLen, loc.getLocationBehaviors().size());
    assertEquals(EventTestFixtures.locationUncertainty, loc.getLocationUncertainty().get());
    assertTrue(loc.getFeaturePredictions().contains(EventTestFixtures.featurePrediction));
    assertTrue(loc.getLocationBehaviors().contains(EventTestFixtures.locationBehavior));
  }

  @Test
  public void testCreate() {
    LocationSolution loc = LocationSolution.create(EventTestFixtures.location, EventTestFixtures.locationRestraint,
        EventTestFixtures.locationUncertainty, EventTestFixtures.locationBehaviors, EventTestFixtures.featurePredictions);
    assertEquals(EventTestFixtures.location, loc.getLocation());
    assertEquals(EventTestFixtures.locationRestraint, loc.getLocationRestraint());
    assertTrue(loc.getLocationUncertainty().isPresent());
    assertEquals(EventTestFixtures.arrayLen, loc.getFeaturePredictions().size());
    assertEquals(EventTestFixtures.arrayLen, loc.getLocationBehaviors().size());
    assertEquals(EventTestFixtures.locationUncertainty, loc.getLocationUncertainty().get());
    assertTrue(loc.getFeaturePredictions().contains(EventTestFixtures.featurePrediction));
    assertTrue(loc.getLocationBehaviors().contains(EventTestFixtures.locationBehavior));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFeaturePredictionsUnmodifiable() {
    LocationSolution.withLocationAndRestraintOnly(
        EventTestFixtures.location, EventTestFixtures.locationRestraint).getFeaturePredictions()
        .add(EventTestFixtures.featurePrediction);
  }

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(EventTestFixtures.locationSolution, LocationSolution.class);
  }

}
