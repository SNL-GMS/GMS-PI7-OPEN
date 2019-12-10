package gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects;

import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.EventTestFixtures;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class LocationUncertaintyTests {

  @Test
  public void testFrom() {
    final LocationUncertainty locationUncertainty = LocationUncertainty
        .from(EventTestFixtures.xx, EventTestFixtures.xy, EventTestFixtures.xz, EventTestFixtures.xt, EventTestFixtures.yy,
            EventTestFixtures.yz, EventTestFixtures.yt, EventTestFixtures.zz, EventTestFixtures.zt, EventTestFixtures.tt,
            EventTestFixtures.stDevOneObservation, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet);
    final double TOLERANCE = 0.0000000001;
    assertEquals(EventTestFixtures.xx, locationUncertainty.getXx(), TOLERANCE);
    assertEquals(EventTestFixtures.xy, locationUncertainty.getXy(), TOLERANCE);
    assertEquals(EventTestFixtures.xz, locationUncertainty.getXz(), TOLERANCE);
    assertEquals(EventTestFixtures.xt, locationUncertainty.getXt(), TOLERANCE);
    assertEquals(EventTestFixtures.yy, locationUncertainty.getYy(), TOLERANCE);
    assertEquals(EventTestFixtures.yz, locationUncertainty.getYz(), TOLERANCE);
    assertEquals(EventTestFixtures.yt, locationUncertainty.getYt(), TOLERANCE);
    assertEquals(EventTestFixtures.zz, locationUncertainty.getZz(), TOLERANCE);
    assertEquals(EventTestFixtures.tt, locationUncertainty.getTt(), TOLERANCE);
    assertEquals(EventTestFixtures.stDevOneObservation, locationUncertainty.getStDevOneObservation(),
        TOLERANCE);
    assertEquals(EventTestFixtures.ellipseSet, locationUncertainty.getEllipses());
    assertEquals(EventTestFixtures.ellipsoidSet, locationUncertainty.getEllipsoids());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testEllipsesImmutable() {
    EventTestFixtures.locationUncertainty.getEllipses().add(EventTestFixtures.ellipse);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testEllipsoidsImmutable() {
    EventTestFixtures.locationUncertainty.getEllipsoids().add(EventTestFixtures.ellipsoid);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNaNxx() {
    LocationUncertainty
        .from(Double.NaN, EventTestFixtures.xy, EventTestFixtures.xz, EventTestFixtures.xt, EventTestFixtures.yy,
            EventTestFixtures.yz, EventTestFixtures.yt, EventTestFixtures.zz, EventTestFixtures.zt, EventTestFixtures.tt,
            EventTestFixtures.stDevOneObservation, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNaNxy() {
    LocationUncertainty
        .from(EventTestFixtures.xx, Double.NaN, EventTestFixtures.xz, EventTestFixtures.xt, EventTestFixtures.yy,
            EventTestFixtures.yz, EventTestFixtures.yt, EventTestFixtures.zz, EventTestFixtures.zt, EventTestFixtures.tt,
            EventTestFixtures.stDevOneObservation, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNaNxz() {
    LocationUncertainty
        .from(EventTestFixtures.xx, EventTestFixtures.xy, Double.NaN, EventTestFixtures.xt, EventTestFixtures.yy,
            EventTestFixtures.yz, EventTestFixtures.yt, EventTestFixtures.zz, EventTestFixtures.zt, EventTestFixtures.tt,
            EventTestFixtures.stDevOneObservation, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNaNxt() {
    LocationUncertainty
        .from(EventTestFixtures.xx, EventTestFixtures.xy, EventTestFixtures.xz, Double.NaN, EventTestFixtures.yy,
            EventTestFixtures.yz, EventTestFixtures.yt, EventTestFixtures.zz, EventTestFixtures.zt, EventTestFixtures.tt,
            EventTestFixtures.stDevOneObservation, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNaNyy() {
    LocationUncertainty
        .from(EventTestFixtures.xx, EventTestFixtures.xy, EventTestFixtures.xz, EventTestFixtures.xt, Double.NaN,
            EventTestFixtures.yz, EventTestFixtures.yt, EventTestFixtures.zz, EventTestFixtures.zt, EventTestFixtures.tt,
            EventTestFixtures.stDevOneObservation, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNaNyz() {
    LocationUncertainty
        .from(EventTestFixtures.xx, EventTestFixtures.xy, EventTestFixtures.xz, EventTestFixtures.xt, EventTestFixtures.yy,
            Double.NaN, EventTestFixtures.yt, EventTestFixtures.zz, EventTestFixtures.zt, EventTestFixtures.tt,
            EventTestFixtures.stDevOneObservation, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNaNyt() {
    LocationUncertainty.from(EventTestFixtures.xx, EventTestFixtures.xy, EventTestFixtures.xz,
        EventTestFixtures.xt, EventTestFixtures.yy, EventTestFixtures.yz, Double.NaN,
        EventTestFixtures.zz, EventTestFixtures.zt, EventTestFixtures.tt,
        EventTestFixtures.stDevOneObservation, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNaNzz() {
    LocationUncertainty
        .from(EventTestFixtures.xx, EventTestFixtures.xy, EventTestFixtures.xz, EventTestFixtures.xt, EventTestFixtures.yy,
            EventTestFixtures.yz, EventTestFixtures.yt, Double.NaN, EventTestFixtures.zt, EventTestFixtures.tt,
            EventTestFixtures.stDevOneObservation, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNaNzt() {
    LocationUncertainty
        .from(EventTestFixtures.xx, EventTestFixtures.xy, EventTestFixtures.xz, EventTestFixtures.xt, EventTestFixtures.yy,
            EventTestFixtures.yz, EventTestFixtures.yt, EventTestFixtures.zz, Double.NaN, EventTestFixtures.tt,
            EventTestFixtures.stDevOneObservation, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNaNtt() {
    LocationUncertainty
        .from(EventTestFixtures.xx, EventTestFixtures.xy, EventTestFixtures.xz, EventTestFixtures.xt, EventTestFixtures.yy,
            EventTestFixtures.yz, EventTestFixtures.yt, EventTestFixtures.zz, EventTestFixtures.zt, Double.NaN,
            EventTestFixtures.stDevOneObservation, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNaNstDevOneObservation() {
    LocationUncertainty
        .from(EventTestFixtures.xx, EventTestFixtures.xy, EventTestFixtures.xz, EventTestFixtures.xt, EventTestFixtures.yy,
            EventTestFixtures.yz, EventTestFixtures.yt, EventTestFixtures.zz, EventTestFixtures.zt, EventTestFixtures.tt,
            Double.NaN, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet);
  }

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(EventTestFixtures.locationUncertainty, LocationUncertainty.class);
  }

  @Test
  public void testGetCovarianceMatrix() {

    // To ensure all 10 values are unique.
    final Set<Double> valueSet = new HashSet<>();

    // What each element represents with the indices underneath
    // xx, xy, xz, xt, yy, yz, yt, zz, zt, tt
    //  0,  1,  2,  3,  4,  5,  6,  7,  8,  9
    final double[] values = new double[10];

    final Random random = new Random(8475894L);
    for (int i=0; i<values.length; i++) {
      Double d = null;
      do {
        d = random.nextDouble() * 5.0;
      } while (valueSet.contains(d));
      values[i] = d;
    }

    final LocationUncertainty locationUncertainty = LocationUncertainty
        .from(values[0], values[1], values[2], values[3], values[4],
            values[5], values[6], values[7], values[8], values[9],
            // Rest doesn't matter for this test.
            EventTestFixtures.stDevOneObservation,
            EventTestFixtures.ellipseSet,
            EventTestFixtures.ellipsoidSet);

    final List<List<Double>> covMatrix = locationUncertainty.getCovarianceMatrix();

    // Test the diagonal is what it's supposed to be.
    final double[] expectedDiagonal = new double[] { values[0], values[4], values[7], values[9] };
    for (int i=0; i<4; i++) {
      assertEquals(expectedDiagonal[i], covMatrix.get(i).get(i), 0.0);
    }

    // Test that the values are symmetric about the diagonal, ie, Vij = Vji
    for (int i=0; i<4; i++) {
      for (int j=i+1; j<4; j++) {
        Double ij = covMatrix.get(i).get(j);
        Double ji = covMatrix.get(j).get(i);
        assertEquals(ij, ji);
      }
    }
  }

}
