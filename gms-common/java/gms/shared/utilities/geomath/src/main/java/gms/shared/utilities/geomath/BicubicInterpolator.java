package gms.shared.utilities.geomath;

import java.util.function.BiFunction;

public interface BicubicInterpolator {

  /**
   * Sets the interpolation data. Arrays are copied. And returns a bifunction that interpolates
   *
   * @param x The monotonically increasing array of x positions.
   * @param y The monotonically increasing array of y positions.
   * @param data The 2D values for each grid position stored as [x][y].
   * @return A function that returns interpolated data, plus the following derivatives: df/dx,
   * d2f/dx2, df/dy, d2f/dydx
   */
  BiFunction<Double, Double, double[]> getFunctionAndDerivatives(double[] x, double[] y,
      double[][] data);

}
