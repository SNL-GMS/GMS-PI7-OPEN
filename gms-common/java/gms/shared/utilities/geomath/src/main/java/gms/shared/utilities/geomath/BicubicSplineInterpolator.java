package gms.shared.utilities.geomath;

import java.util.Arrays;
import java.util.function.BiFunction;

public class BicubicSplineInterpolator implements BicubicInterpolator {

  private static class Bracket {

    public int khi = 0;
    public int klo = 0;
    public double a = 0.0;
    public double b = 0.0;
    public double h = 0.0;

    /**
     * Standard bisection method to bracket the input interpolation location between 2 entries of a
     * monotonically increasing vector (xGrid). On exit the values a, b, and h are assigned which
     * are used to perform the actual value, derivative, and 2nd derivative interpolation.
     *
     * @param x Interpolation location.
     * @param xGrid Monotonically increasing grid vector.
     */
    public Bracket(double x, double[] xGrid) {
      int k = 0;
      klo = 0;
      khi = xGrid.length - 1;
      while (khi - klo > 1) {
        k = (khi + klo) >> 1;
        if (xGrid[k] > x) {
          khi = k;
        } else {
          klo = k;
        }
      }

      // klo and khi now bracket the input value of x in xGrid
      // get h, a, and b
      h = xGrid[khi] - xGrid[klo];
      a = (xGrid[khi] - x) / h;
      b = (x - xGrid[klo]) / h;
    }
  }

  @Override
  public BiFunction<Double, Double, double[]> getFunctionAndDerivatives(double[] x, double[] y,
      double[][] data) {
    double[] xGrid = Arrays.copyOf(x, x.length);
    double[] yGrid = Arrays.copyOf(y, y.length);
    double[][] values = new double[data.length][];
    for (int i = 0; i < values.length; ++i) {
      values[i] = Arrays.copyOf(data[i], data[i].length);
    }

    BiFunction<Double, Double, double[]> transposeInterpolator = (yintrp, xintrp) ->
        interpolate(yGrid, xGrid, transposeMatrix(values), new Bracket(yintrp, y),
            new Bracket(xintrp, x));

    return (xintrp, yintrp) -> {
      double[] derivativeSet1 = interpolate(xGrid, yGrid, values, new Bracket(xintrp, x),
          new Bracket(yintrp, y));
      double[] derivativeSet2 = transposeInterpolator.apply(yintrp, xintrp);
      //double crossDerivative = interpolatedCrossDerivative(transposeInterpolator, yintrp, xintrp);

      return new double[]{
          derivativeSet1[0], // f
          derivativeSet1[1], // df/dx
          derivativeSet1[2], // df2/d2x
          derivativeSet2[1], // df/dy
          interpolatedCrossDerivative(xGrid, yGrid, values, xintrp, yintrp) // d2f/dydx
      };
    };
    //interpolate(xGrid, yGrid, values, new Bracket(xintrp, x), new Bracket(yintrp, y));
  }

  /**
   * \brief Function to Perform Cubic Spline Interpolation at a Point Bracketed by 1-D Cubic
   * Splines. On Return the interpolated value, derivative, and 2nd derivative are saved, which can
   * be retreived using the appropriate getters.
   *
   * Based On the function "splin2": Press, W.H. et al., 1988, "Numerical Recipes", 94-110.
   *
   * @param xGrid The x grid locations. A monotonically increasing array. The x interpolation
   * location must lie within the limits of this vector.
   * @param yGrid The y grid locations. A monotonically increasing array. The y interpolation
   * location must lie within the limits of this vector.
   * @param values The 2d array of values to be interpolated. These are stored as [x][y].
   * @param xBracket A Bracket object that contains the interpolation spacing information for the x
   * grid.
   * @param yBracket A Bracket object that contains the interpolation spacing information for the y
   * grid.
   * @return An array with three elements: the interpolated value, the interpolated 1st derivative,
   * the interpolated 2nd derivative
   */
  private static double[] interpolate(double[] xGrid, double[] yGrid, double[][] values,
      Bracket xBracket, Bracket yBracket) {
    // calculate the 2nd derivatives at each of the tables values for splines
    // running in the y directions (d2v_dy2)

    double[][] d2v_dy2 = new double[xGrid.length][yGrid.length];
    for (int j = 0; j < xGrid.length; j++) {
      naturalSpline(yGrid, values[j], d2v_dy2[j]);
    }
    //spline(yGrid, values[j], 1.0e30, 1.0e30, d2v_dy2[j]);

    // calculate xgrid interpolated values interpolated at the y interpolation
    // location (valuesOnXAtYinterp)

    double[] valuesOnXAtYinterp = new double[xGrid.length];
    for (int j = 0; j < xGrid.length; j++) {
      valuesOnXAtYinterp[j] = interpolatedValue(yBracket, values[j], d2v_dy2[j]);
    }

    // calculate xgrid second derivative entries along the y interpolation
    // location and save in d2v_dx2

    double[] d2v_dx2 = new double[xGrid.length];
    naturalSpline(xGrid, valuesOnXAtYinterp, d2v_dx2);
    //spline(xGrid, valuesOnXAtYinterp, 1.0e30, 1.0e30, d2v_dx2);

    // save the interpolate value, derivative, and 2nd derivative

    return new double[]{
        interpolatedValue(xBracket, valuesOnXAtYinterp, d2v_dx2),
        interpolatedDerivative(xBracket, valuesOnXAtYinterp, d2v_dx2),
        interpolated2ndDerivative(xBracket, d2v_dx2)
    };
  }

  /**
   * Cubic Spline Construction Function.
   *
   * Function. Given arrays x[0..n-1] and y[0..n-1] containing a tabulated function, i.e., y[i] =
   * f(x[i]), with x[0] < x[1] < x[n-1], and given values yp1 and ypn for the first derivative of
   * the interpolating function at points 0 and n-1, respectively, this routine returns an array
   * y2[0..n-1] that contains the 2nd i derivatives of the interpolating function at the tabulated
   * points x[i].
   *
   * If yp1 and/or ypn are equal to 1.0e30 or larger, the routine is signaled to set the
   * corresponding boundary condition for a natural spline, with zero second derivative on that
   * boundary.
   *
   * NOTE: This routine only needs to be called once to process the entire tabulated function in x
   * and y arrays.
   *
   * Based On the function "spline": Press, W.H. et al., 1988, "Numerical Recipes", 94-110.
   *
   * @param x - An input vector of independent values of a cubic spline.
   * @param y - An input vector of dependent values of the cubic spline defined on the values xa.
   * @param yp1 - Value of dy/dx evaluated at x[0].
   * @param ypn - Value of dy/dx evaluated at x[x.length-1].
   * @param y2 - A Vector of second derivatives defined on xa.
   */
  private static void spline(double[] x, double[] y, double yp1, double ypn,
      double[] y2) {
    int i, k;
    double p, qn, sig, un;
    double[] u = new double[x.length];

    // calculate temporary u vector
    if (yp1 > 0.99e30) {
      y2[0] = u[0] = 0.0;
    } else {
      y2[0] = -0.5;
      u[0] = ((3.0 / (x[1] - x[0])) * ((y[1] - y[0]) / (x[1] - x[0]) - yp1));
    }

    // Decomposition loop for tri-diagonal algorithm

    int xlm1 = x.length - 1;
    int xlm2 = x.length - 2;
    for (i = 1; i < xlm1; i++) {
      sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
      p = sig * y2[i - 1] + 2.0;
      y2[i] = (sig - 1.0) / p;
      u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1])
          / (x[i] - x[i - 1]);
      u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
    }
    if (ypn > 0.99e30) {
      qn = un = 0.0;
    } else {
      qn = 0.5;
      un = (3.0 / (x[xlm1] - x[xlm2]))
          * (ypn - (y[xlm1] - y[xlm2]) / (x[xlm1] - x[xlm2]));
    }

    // Back substitution loop of tri-diagonal algorithm

    y2[xlm1] = (un - qn * u[xlm2]) / (qn * y2[xlm2] + 1.0);
    for (k = xlm2; k >= 0; k--) {
      y2[k] = y2[k] * y2[k + 1] + u[k];
    }
  }

  /**
   * Cubic Spline Construction Function.
   *
   * Function. Given arrays x[0..n-1] and y[0..n-1] containing a tabulated function, i.e., y[i] =
   * f(x[i]), with x[0] < x[1] < x[n-1], and given values yp1 and ypn for the first derivative of
   * the interpolating function at points 0 and n-1, respectively, this routine returns an array
   * y2[0..n-1] that contains the 2nd i derivatives of the interpolating function at the tabulated
   * points x[i].
   *
   * If yp1 and/or ypn are equal to 1.0e30 or larger, the routine is signaled to set the
   * corresponding boundary condition for a natural spline, with zero second derivative on that
   * boundary.
   *
   * NOTE: This routine only needs to be called once to process the entire tabulated function in x
   * and y arrays.
   *
   * Based On the function "spline": Press, W.H. et al., 1988, "Numerical Recipes", 94-110.
   *
   * @param x - An input vector of independent values of a cubic spline.
   * @param y - An input vector of dependent values of the cubic spline defined on the values xa.
   * @param y2 - A Vector of second derivatives defined on xa.
   */
  private static void naturalSpline(double[] x, double[] y, double[] y2) {
    int i, k;
    double p, sig;
    double[] u = new double[x.length];

    // calculate temporary u vector
    y2[0] = u[0] = 0.0;

    // Decomposition loop for tri-diagonal algorithm

    int xlm1 = x.length - 1;
    int xlm2 = x.length - 2;
    for (i = 1; i < xlm1; i++) {
      sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
      p = sig * y2[i - 1] + 2.0;
      y2[i] = (sig - 1.0) / p;
      u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1])
          / (x[i] - x[i - 1]);
      u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
    }

    // Back substitution loop of tri-diagonal algorithm

    y2[xlm1] = 0.0;
    for (k = xlm2; k >= 0; k--) {
      y2[k] = y2[k] * y2[k + 1] + u[k];
    }
  }

  /**
   * Returns the natural cubic spline interpolated value at the requested interpolation location.
   *
   * @param bracket The bracket object providing the x or y grid interpolation spacing information.
   * @param values The set of values to be interpolated.
   * @param d2v The second derivative in the direction of the monotonically increasing grid (for
   * which bracket was defined).
   * @return The interpolated value.
   */
  private static double interpolatedValue(Bracket bracket, double[] values, double[] d2v) {
    return bracket.a * values[bracket.klo] + bracket.b * values[bracket.khi]
        + ((bracket.a * bracket.a * bracket.a - bracket.a) * d2v[bracket.klo] +
        (bracket.b * bracket.b * bracket.b - bracket.b) * d2v[bracket.khi]) *
        (bracket.h * bracket.h) / 6.0;
  }

  /**
   * Returns the natural cubic spline interpolated 1st derivative at the requested interpolation
   * location.
   *
   * @param bracket The bracket object providing the x or y grid interpolation spacing information.
   * @param values The set of values to be interpolated for the 1st derivative.
   * @param d2v The second derivative in the direction of the monotonically increasing grid (for
   * which bracket was defined).
   * @return The interpolated first derivative.
   */
  private static double interpolatedDerivative(Bracket bracket, double[] values, double[] d2v) {
    return ((values[bracket.khi] - values[bracket.klo]) / bracket.h)
        - (((3.0 * bracket.a * bracket.a - 1.0) *
        bracket.h * d2v[bracket.klo]) / 6.0)
        + (((3.0 * bracket.b * bracket.b - 1.0) *
        bracket.h * d2v[bracket.khi]) / 6.0);
  }

  /**
   * Returns the natural cubic spline interpolated 2nd derivative at the requested interpolation
   * location.
   *
   * @param bracket The bracket object providing the x or y grid interpolation spacing information.
   * @param d2v The second derivative in the direction of the monotonically increasing grid (for
   * which bracket was defined).
   * @return The interpolated second derivative.
   */
  private static double interpolated2ndDerivative(Bracket bracket, double[] d2v) {
    return bracket.a * d2v[bracket.klo] + bracket.b * d2v[bracket.khi];
  }

  private static double interpolatedCrossDerivative(
      double[] xGrid, double[] yGrid, double[][] values,
      double x, double y) {

    final double EPSILON = 1e-7;

    double upperXderivative = interpolate(xGrid, yGrid, values, new Bracket(x, xGrid),
        new Bracket(y + EPSILON, yGrid))[1];
    double lowerXderivative = interpolate(xGrid, yGrid, values, new Bracket(x, xGrid),
        new Bracket(y - EPSILON, yGrid))[1];

    return (upperXderivative - lowerXderivative) / (2 * EPSILON);
  }

  protected static double[][] transposeMatrix(double[][] m) {
    double[][] temp = new double[m[0].length][m.length];
    for (int i = 0; i < m.length; i++) {
      for (int j = 0; j < m[0].length; j++) {
        temp[j][i] = m[i][j];
      }
    }
    return temp;
  }
}
