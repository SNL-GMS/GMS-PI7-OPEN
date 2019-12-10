package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import org.junit.Test;

public class EllipsoidTests {

  @Test
  public void testFrom() {
    //kWeight = 0.0 is an acceptable value here
    //kWeight doesn't matter when scalingFactorType is set to CONFIDENCE
    //confidenceLevel does
    final Ellipsoid ellipsoid = Ellipsoid.from(
        EventTestFixtures.scalingFactorType, EventTestFixtures.kWeight,
        EventTestFixtures.confidenceLevel,
        EventTestFixtures.majorAxisLength, EventTestFixtures.majorAxisTrend,
        EventTestFixtures.majorAxisPlunge, EventTestFixtures.intermediateAxisLength,
        EventTestFixtures.intermediateAxisTrend, EventTestFixtures.intermediateAxisPlunge,
        EventTestFixtures.minorAxisLength, EventTestFixtures.minorAxisTrend,
        EventTestFixtures.minorAxisPlunge, EventTestFixtures.timeUncertainty);
    assertNotNull(ellipsoid);
    assertEquals(EventTestFixtures.scalingFactorType, ellipsoid.getScalingFactorType());
    assertEquals(EventTestFixtures.timeUncertainty, ellipsoid.getTimeUncertainty());
    final double tolerance = 0.0000000001;
    assertEquals(EventTestFixtures.kWeight, ellipsoid.getkWeight(), tolerance);
    assertEquals(EventTestFixtures.confidenceLevel, ellipsoid.getConfidenceLevel(), tolerance);
    assertEquals(EventTestFixtures.majorAxisLength, ellipsoid.getMajorAxisLength(), tolerance);
    assertEquals(EventTestFixtures.majorAxisTrend, ellipsoid.getMajorAxisTrend(), tolerance);
    assertEquals(EventTestFixtures.majorAxisPlunge, ellipsoid.getMajorAxisPlunge(), tolerance);
    assertEquals(EventTestFixtures.intermediateAxisLength, ellipsoid.getIntermediateAxisLength(), tolerance);
    assertEquals(EventTestFixtures.intermediateAxisTrend, ellipsoid.getIntermediateAxisTrend(), tolerance);
    assertEquals(EventTestFixtures.intermediateAxisPlunge, ellipsoid.getIntermediateAxisPlunge(), tolerance);
    assertEquals(EventTestFixtures.minorAxisLength, ellipsoid.getMinorAxisLength(), tolerance);
    assertEquals(EventTestFixtures.minorAxisTrend, ellipsoid.getMinorAxisTrend(), tolerance);
    assertEquals(EventTestFixtures.minorAxisPlunge, ellipsoid.getMinorAxisPlunge(), tolerance);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadConfidenceLevelWithConfidenceScalingFactor() {
    Ellipsoid.from(ScalingFactorType.CONFIDENCE, EventTestFixtures.kWeight,
        0.0,
        EventTestFixtures.majorAxisLength, EventTestFixtures.majorAxisTrend,
        EventTestFixtures.majorAxisPlunge, EventTestFixtures.intermediateAxisLength,
        EventTestFixtures.intermediateAxisTrend, EventTestFixtures.intermediateAxisPlunge,
        EventTestFixtures.minorAxisLength, EventTestFixtures.minorAxisTrend,
        EventTestFixtures.minorAxisPlunge, EventTestFixtures.timeUncertainty);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonInfiniteKWeightWithCoverageScalingFactor() {
    Ellipsoid.from(ScalingFactorType.COVERAGE, 1.0,
        EventTestFixtures.confidenceLevel,
        EventTestFixtures.majorAxisLength, EventTestFixtures.majorAxisTrend,
        EventTestFixtures.majorAxisPlunge, EventTestFixtures.intermediateAxisLength,
        EventTestFixtures.intermediateAxisTrend, EventTestFixtures.intermediateAxisPlunge,
        EventTestFixtures.minorAxisLength, EventTestFixtures.minorAxisTrend,
        EventTestFixtures.minorAxisPlunge, EventTestFixtures.timeUncertainty);
  }

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(EventTestFixtures.ellipsoid, Ellipsoid.class);
  }
}
