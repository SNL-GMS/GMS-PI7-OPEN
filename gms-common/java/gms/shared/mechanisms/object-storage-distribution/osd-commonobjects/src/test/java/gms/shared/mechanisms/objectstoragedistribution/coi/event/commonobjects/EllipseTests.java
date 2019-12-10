package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import org.junit.Test;


public class EllipseTests {

  @Test
  public void testFrom() {
    //kWeight = 0.0 is an acceptable value here
    //kWeight doesn't matter when scalingFactorType is set to CONFIDENCE
    //confidenceLevel does
    Ellipse ellipse = Ellipse.from(
        EventTestFixtures.scalingFactorType, EventTestFixtures.kWeight,
        EventTestFixtures.confidenceLevel,
        EventTestFixtures.majorAxisLength, EventTestFixtures.majorAxisTrend,
        EventTestFixtures.minorAxisLength, EventTestFixtures.minorAxisTrend,
        EventTestFixtures.depthUncertainty, EventTestFixtures.timeUncertainty);
    assertEquals(EventTestFixtures.scalingFactorType, ellipse.getScalingFactorType());
    assertEquals(EventTestFixtures.timeUncertainty, ellipse.getTimeUncertainty());
    final double tolerance = 0.0000000001;
    assertEquals(EventTestFixtures.kWeight, ellipse.getkWeight(), tolerance);
    assertEquals(EventTestFixtures.confidenceLevel, ellipse.getConfidenceLevel(), tolerance);
    assertEquals(EventTestFixtures.majorAxisLength, ellipse.getMajorAxisLength(), tolerance);
    assertEquals(EventTestFixtures.majorAxisTrend, ellipse.getMajorAxisTrend(), tolerance);
    assertEquals(EventTestFixtures.minorAxisLength, ellipse.getMinorAxisLength(), tolerance);
    assertEquals(EventTestFixtures.minorAxisTrend, ellipse.getMinorAxisTrend(), tolerance);
    assertEquals(EventTestFixtures.depthUncertainty, ellipse.getDepthUncertainty(), tolerance);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadConfidenceLevelWithConfidenceScalingFactor() {
    Ellipse.from(EventTestFixtures.scalingFactorType, EventTestFixtures.kWeight, 0.0,
            EventTestFixtures.majorAxisLength, EventTestFixtures.majorAxisTrend,
            EventTestFixtures.minorAxisLength, EventTestFixtures.minorAxisTrend,
            EventTestFixtures.depthUncertainty, EventTestFixtures.timeUncertainty);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNonInfiniteKWeightWithCoverageScalingFactor() {
    Ellipse.from(EventTestFixtures.scalingFactorType2, EventTestFixtures.kWeight,
            EventTestFixtures.confidenceLevel,
            EventTestFixtures.majorAxisLength, EventTestFixtures.majorAxisTrend,
            EventTestFixtures.minorAxisLength, EventTestFixtures.minorAxisTrend,
            EventTestFixtures.depthUncertainty, EventTestFixtures.timeUncertainty);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowKWeightWithKWeightedScalingFactor() {
    Ellipse.from(EventTestFixtures.scalingFactorType2, EventTestFixtures.kWeight,
            EventTestFixtures.confidenceLevel,
            EventTestFixtures.majorAxisLength, EventTestFixtures.majorAxisTrend,
            EventTestFixtures.minorAxisLength, EventTestFixtures.minorAxisTrend,
            EventTestFixtures.depthUncertainty, EventTestFixtures.timeUncertainty);
  }

  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(EventTestFixtures.ellipse, Ellipse.class);
  }
}
