package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import org.junit.Test;

public class BcsTravelTimeInterpolatorTests {

  @Test
  public void testGridPointsNoHoles() {
    double[] distances = TestFixtures.modelNoHoles
        .getDistancesDeg("ignored", PhaseType.P);
    double[] depths = TestFixtures.modelNoHoles.getDepthsKm("ignored", PhaseType.P);
    double[][] values = TestFixtures.modelNoHoles
        .getValues("ignored", PhaseType.P);

    BcsTravelTimeInterpolator interpolator = new BcsTravelTimeInterpolator.Builder()
        .withEarthModelName("ak135")
        .withEarthModelsPlugin(TestFixtures.modelNoHoles)
        .withPhaseType(PhaseType.P)
        .withExtrapolation(false)
        .build();

    boolean wasExtrapolated = false;

    for (int i = 0; i < distances.length; i++) {
      for (int j = 0; j < depths.length; j++) {
        assertEquals(values[j][i],
            interpolator.getPhaseTravelTimeAndDerivatives(depths[j], distances[i])[0], 0.0);
        wasExtrapolated = wasExtrapolated || interpolator.wasExtrapolated();
      }
    }

    assertTrue(!wasExtrapolated);
  }

  @Test
  public void testGridPointsNoHolesDoesNotExtrapolate() {
    double[] distances = TestFixtures.modelNoHoles
        .getDistancesDeg("ignored", PhaseType.P);
    double[] depths = TestFixtures.modelNoHoles.getDepthsKm("ignored", PhaseType.P);
    double[][] values = TestFixtures.modelNoHoles
        .getValues("ignored", PhaseType.P);

    BcsTravelTimeInterpolator interpolator = new BcsTravelTimeInterpolator.Builder()
        .withEarthModelName("ak135")
        .withEarthModelsPlugin(TestFixtures.modelNoHoles)
        .withPhaseType(PhaseType.P)
        .withExtrapolation(true)
        .build();

    boolean wasExtrapolated = false;

    for (int i = 0; i < distances.length; i++) {
      for (int j = 0; j < depths.length; j++) {
        assertEquals(values[j][i],
            interpolator.getPhaseTravelTimeAndDerivatives(depths[j], distances[i])[0], 0.0);
        wasExtrapolated = wasExtrapolated || interpolator.wasExtrapolated();
      }
    }

    assertTrue(!wasExtrapolated);
  }

  @Test
  public void testSomeValuesNoHoles() {
    BcsTravelTimeInterpolator interpolator = new BcsTravelTimeInterpolator.Builder()
        .withEarthModelName("ak135")
        .withEarthModelsPlugin(TestFixtures.modelNoHoles)
        .withPhaseType(PhaseType.P)
        .build();

    //TODO: may want to find more datapoints here
    double value = interpolator.getPhaseTravelTimeAndDerivatives(70, 40.84073276581503)[0];
    assertEquals(454.769981628745, value, 10e-9);
  }

  @Test
  public void testGridPointsHoles() {
    double[] distances = TestFixtures.modelInternalHoles
        .getDistancesDeg("ignored", PhaseType.P);
    double[] depths = TestFixtures.modelInternalHoles
        .getDepthsKm("ignored", PhaseType.P);
    double[][] values = TestFixtures.tableNoHoles;
    BcsTravelTimeInterpolator interpolator = new BcsTravelTimeInterpolator.Builder()
        .withEarthModelName("ak135")
        .withEarthModelsPlugin(TestFixtures.modelInternalHoles)
        .withPhaseType(PhaseType.P)
        .withExtrapolation(true)
        .build();

    boolean wasExtrapolated = false;

    for (int i = 0; i < distances.length; i++) {
      for (int j = 0; j < depths.length; j++) {
        assertEquals(values[j][i],
            interpolator.getPhaseTravelTimeAndDerivatives(depths[j], distances[i])[0], 0.001);
        wasExtrapolated = wasExtrapolated || interpolator.wasExtrapolated();
      }
    }

    assertTrue(wasExtrapolated);

  }

  @Test
  public void testOutsideDepthBounds() {
    double[] distances = TestFixtures.modelNoHoles
        .getDistancesDeg("ignored", PhaseType.P);
    double[] values = TestFixtures.depthExtrapolatedTravelTimes;

    BcsTravelTimeInterpolator interpolator = new BcsTravelTimeInterpolator.Builder()
        .withEarthModelName("ak135")
        .withEarthModelsPlugin(TestFixtures.modelNoHoles)
        .withPhaseType(PhaseType.P)
        .withExtrapolation(true)
        .build();

    boolean wasExtrapolated = false;

    for (int i = 0; i < distances.length; i++) {
      assertEquals(values[i],
          interpolator
              .getPhaseTravelTimeAndDerivatives(TestFixtures.extrapolatedDepth, distances[i])[0],
          0.1);
      wasExtrapolated = wasExtrapolated || interpolator.wasExtrapolated();
    }

    assertTrue(wasExtrapolated);
  }

  @Test
  public void testOutsideDistanceBounds() {
    double[] depths = TestFixtures.modelNoHoles
        .getDepthsKm("ignored", PhaseType.P);
    double[] values = TestFixtures.distanceExtrapolatedTravelTimes;

    BcsTravelTimeInterpolator interpolator = new BcsTravelTimeInterpolator.Builder()
        .withEarthModelName("ak135")
        .withEarthModelsPlugin(TestFixtures.modelNoHoles)
        .withPhaseType(PhaseType.P)
        .withExtrapolation(true)
        .build();

    boolean wasExtrapolated = false;

    for (int i = 0; i < depths.length; i++) {
      assertEquals(values[i],
          interpolator
              .getPhaseTravelTimeAndDerivatives(depths[i], TestFixtures.extrapolatedDistance)[0],
              0.1);
      wasExtrapolated = wasExtrapolated || interpolator.wasExtrapolated();
    }

    assertTrue(wasExtrapolated);
  }
}
