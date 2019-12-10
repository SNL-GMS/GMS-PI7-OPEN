package gms.shared.utilities.geomath;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class BicubicSplineInterpolatorTests {

  final private double[] xgrid1 = {39.0, 39.5, 40.0, 40.5, 41.0, 41.5, 42.0};
  final private double[] ygrid1 = {35.0, 50.0, 75.0, 100.0};
  final private double[][] values1 = {
      {442.9671, 441.4871, 439.0246, 436.5673},
      {447.1415, 445.658, 443.1895, 440.7262},
      {451.2988, 449.8117, 447.3374, 444.8681},
      {455.4391, 453.9485, 451.4683, 448.9932},
      {459.5623, 458.0683, 455.5823, 453.1014},
      {463.6686, 462.1711, 459.6794, 457.1926},
      {467.7578, 466.2568, 463.7593, 461.2666}};

  final private double[][] values1Transpose = BicubicSplineInterpolator.transposeMatrix(values1);

  final private double xintrp1 = 41.006537192482874; //40.84073276581503;
  final private double yintrp1 = 70.0;
  final private double[] results1 =
      {456.1327134593133, 8.213007695018854, -0.06992008581683005, -0.09937206689920229,
          -4.596945046841938E-4};
  //{454.769981628745, 8.224224109065798, -0.0646680840265265};
  final private double MAX_ERROR_VALUE = 1.0E-05;

  //TODO: This seems low; double check this.
  final private double MAX_ERROR_DX = 1.0E-02;

  //TODO: This seems low; double check this.
  final private double MAX_ERROR_DX2 = 1.0E-02;
  final private double MAX_ERROR_DY = 1.0E-08;
  final private double MAX_ERROR_DYDX = 1.0E-06;

  @Test
  public void interpolationTest() {
    BicubicInterpolator nbci = new BicubicSplineInterpolator();
    //nbci.set(xintrp1, yintrp1, xgrid1, ygrid1, values1);
    //nbci.interpolate();

    double[] actuals = nbci.getFunctionAndDerivatives(xgrid1, ygrid1, values1)
        .apply(xintrp1, yintrp1);

    assertTrue("Value residual exceeds maximum error",
        Math.abs(results1[0] - actuals[0]) < MAX_ERROR_VALUE);
    assertTrue("Derivative wrt x residual exceeds maximum error",
        Math.abs(results1[1] - actuals[1]) < MAX_ERROR_DX);
    assertTrue("2nd Derivative wrt x residual exceeds maximum error",
        Math.abs(results1[2] - actuals[2]) < MAX_ERROR_DX2);
    assertTrue("Derivative wrt y residual exceeds maximum error",
        Math.abs(results1[3] - actuals[3]) < MAX_ERROR_DY);

    //TODO: Why is this failing when it is dont d2/dydx correctly (apparently)?
    assertTrue("Partial derivative wrt xy residual exceeds maximum error",
        Math.abs(results1[4] - actuals[4]) < MAX_ERROR_DYDX);

  }
}
