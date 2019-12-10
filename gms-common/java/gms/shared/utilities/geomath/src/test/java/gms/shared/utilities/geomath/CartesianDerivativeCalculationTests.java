package gms.shared.utilities.geomath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

//TODO: Create tests for some independent values
public class CartesianDerivativeCalculationTests {

  final private double[] distanceGrid = {39.0, 39.5, 40.0, 40.5, 41.0, 41.5, 42.0};
  final private double[] depthGrid = {35.0, 50.0, 75.0, 100.0};
  final private double[][] travelTimes = {
      {442.9671, 441.4871, 439.0246, 436.5673},
      {447.1415, 445.658, 443.1895, 440.7262},
      {451.2988, 449.8117, 447.3374, 444.8681},
      {455.4391, 453.9485, 451.4683, 448.9932},
      {459.5623, 458.0683, 455.5823, 453.1014},
      {463.6686, 462.1711, 459.6794, 457.1926},
      {467.7578, 466.2568, 463.7593, 461.2666}};
  final private double distance = 41.006537192482874; //40.84073276581503;
  final private double depth = 70.0;
  //final private double[] results1 = {454.769981628745, 8.224224109065798, -0.0646680840265265};
  final private double azimuth = 31.4;
  final private double[] rawDerivatives =
      {    8.213007695018854,      //dDistance
          -0.06992008581683005,   //d2Distance2
          -0.09937206689920229,   //dDepth
          -4.596945046841938E-4}; //d2DepthDistance
  final private double[] ttDerivatives = {
      -rawDerivatives[0] * Math.sin(Math.toRadians(azimuth)),
      -rawDerivatives[0] * Math.cos(Math.toRadians(azimuth)),
      rawDerivatives[2],
      1.0
  };
  final private double[] azDerivatives = {
      -Math.cos(Math.toRadians(azimuth)) / Math.sin(Math.toRadians(distance)),
       Math.sin(Math.toRadians(azimuth)) / Math.sin(Math.toRadians(distance)),
      0.0,
      0.0
  };
  final private double[] shDerivatives = {
      -rawDerivatives[1] * Math.sin(Math.toRadians(azimuth)),
      -rawDerivatives[1] * Math.cos(Math.toRadians(azimuth)),
      rawDerivatives[3],
      0.0
  };
  final private double MAX_TT_ERROR = 1.0E-03;
  final private double MAX_AZ_ERROR = 1.0E-03;
  final private double MAX_SH_ERROR = 1.0E-02;

  private BicubicInterpolator interpolator = new BicubicSplineInterpolator();

  @Test
  public void testTravelTime() {
    double[] oldDerivatives = interpolator.getFunctionAndDerivatives(distanceGrid, depthGrid, travelTimes)
        .apply(distance, depth);

    double[] result = CartesianDerivativeCalculation.OF_TRAVEL_TIME
        .calculate(oldDerivatives, distance, depth, azimuth);

    for (int i = 0; i < ttDerivatives.length; i++) {
      assertEquals(ttDerivatives[i], result[i], MAX_TT_ERROR);
    }
  }

  @Test
  public void testAzimuth() {

    double[] oldDerivatives = interpolator.getFunctionAndDerivatives(distanceGrid, depthGrid, travelTimes)
        .apply(distance, depth);

    double[] result = CartesianDerivativeCalculation.OF_AZIMUTH
        .calculate(oldDerivatives, distance, depth, azimuth);

    for (int i = 0; i < azDerivatives.length; i++) {
      assertEquals(azDerivatives[i], result[i], MAX_AZ_ERROR);
    }
  }

  @Test
  public void testSlowness() {

    double[] oldDerivatives = interpolator.getFunctionAndDerivatives(distanceGrid, depthGrid, travelTimes)
        .apply(distance, depth);

    double[] result = CartesianDerivativeCalculation.OF_SLOWNESS
        .calculate(oldDerivatives, distance, depth, azimuth);

    for (int i = 0; i < shDerivatives.length; i++) {
      assertEquals(shDerivatives[i], result[i], MAX_SH_ERROR);
    }
  }
}
