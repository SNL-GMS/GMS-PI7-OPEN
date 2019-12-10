package gms.shared.utilities.geomath;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import java.util.OptionalDouble;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.special.Gamma;

/**
 * Contains static utility methods for computing various statistics.
 */
public final class StatUtil {

  // The square root of machine epsilon.
  private static final double EPS = Math.sqrt(Math.ulp(0.5));
  private static final double FPMIN = Double.MIN_VALUE / EPS;

  // Maximum number of iterations used by betacf()
  private static final int BETACF_MAXIT = 10000;

  // Maximum number of iterations used by zbrent()
  private static final int ZBRENT_MAXIT = 100;
  private static final double ZBRENT_EPS = Math.ulp(1.0);

  private static final int ZBRAC_MAXIT = 50;
  private static final double ZBRAC_FACTOR = 1.6;

  // Upper limit for the condition number used in computeUncertainties().
  private static final double MAX_CONDITION_NUMBER = 1.0e30;

  private static final double MAX_LENGTH = 1.1e60;

  // Uninstantiable
  private StatUtil() {}

  /**
   * <p>
   * Returns the logarithm of the gamma function.
   * </p>
   *
   * @param x Argument.
   * @return the value of {@code log(Gamma(x))}, {@code Double.NaN} if
   * {@code x <= 0.0}.
   */
  public static double gammln(final double x) {
    return Gamma.logGamma(x);
  }

  /**
   * <p>
   * Returns the incomplete gamma function Q(a, x) evaluated by its
   * continued fraction representation as gammcf. Q(a, x) is the
   * complement of P(a, x).
   * </p>
   * @param a
   * @param x
   * @return
   * @throws {@code org.apache.commons.math3.exception.MaxCountExceededException}
   *   if the algorithm fails to converge.
   */
  public static double gcf(final double a, final double x) {
    return 1.0 - gammp(a, x);
  }

  /**
   * <p>
   * Returns the incomplete gamma function P(a, x) evaluated by its
   * continued fraction representation as gammcf. P(a, x) is the
   * complement of Q(a, x).
   * </p>
   * @param a
   * @param x
   * @return
   * @throws {@code org.apache.commons.math3.exception.MaxCountExceededException}
   *   if the algorithm fails to converge.
   */
  public static double gammp(final double a, final double x) {
    return Gamma.regularizedGammaP(a, x);
  }

  /**
   * <p>
   * Returns the incomplete gamma function P(a,x) evaluated by its series
   * representation.
   * </p>
   * @param a
   * @param x
   * @return
   * @throws {@code org.apache.commons.math3.exception.MaxCountExceededException}
   *   if the algorithm fails to converge.
   */
  public static double gser(final double a, final double x) {
    return 1.0 - Gamma.regularizedGammaQ(a, x);
  }

  /**
   * <p>
   * Returns the complement of the incomplete gamma function
   * </p>
   * @param a
   * @param x
   * @return
   * @throws {@code org.apache.commons.math3.exception.MaxCountExceededException}
   *   if the algorithm fails to converge.
   */
  public static double gammq(final double a, final double x) {
    if (x < 0.0) {
      throw new IllegalArgumentException("x < 0: " + x);
    }
    if (a <= 0.0) {
      throw new IllegalArgumentException("a <= 0: " + a);
    }
    return x < a + 1.0 ? 1.0 - gser(a, x) : gcf(a, x);
  }

  /**
   * <p>
   * Computes the continued fraction used by the incomplete beta function,
   * betai.
   * </p>
   * @param a
   * @param b
   * @param x
   * @return
   */
  public static double betacf(final double a, final double b, final double x) {

    // Will be used in factors that occur in the coefficients.
    final double qab = a + b;
    final double qap = a + 1.0;
    final double qam = a - 1.0;

    double del;

    double c = 1.0;
    double d = 1.0 - qab * x / qap;

    if (Math.abs(d) < FPMIN) {
      d = FPMIN;
    }

    d = 1.0 / d;
    double h = d;

    boolean converged = false;

    for (int m = 1; m <= BETACF_MAXIT && !converged; m++) {
      int m2 = 2 * m;
      double aa = m * (b - m) * x / ( (qam + m2) * (a + m2));
      d = 1.0 + aa * d;
      if (Math.abs(d) < FPMIN) {
        d = FPMIN;
      }
      c = 1.0 + aa / c;
      if (Math.abs(c) < FPMIN) {
        c = FPMIN;
      }
      d = 1.0 / d;
      h *= d * c;
      aa = - (a + m) * (qab + m) * x / ( (a + m2) * (qap + m2));
      d = 1.0 + aa * d;
      if (Math.abs(d) < FPMIN) {
        d = FPMIN;
      }
      c = 1.0 + aa / c;
      if (Math.abs(c) < FPMIN) {
        c = FPMIN;
      }
      d = 1.0 / d;
      del = d * c;
      h *= del;
      if (Math.abs(del - 1.0) <= EPS) {
        converged = true;
      }
    }

    if (!converged) {
      throw new MaxCountExceededException(BETACF_MAXIT);
    }

    return h;
  }

  /**
   * Implementation of the incomplete beta function
   * @param a
   * @param b
   * @param x
   * @return a value in the range [0 - 1].
   */
  public static double betai(final double a, final double b, final double x) {

    if (x < 0.0 || x > 1.0) {
      throw new IllegalArgumentException(
          "x not in [0 - 1]: " + x
      );
    }

    double bt = 0.0;

    if (x > 0.0 && x < 1.0) {
      bt = Math.exp(gammln(a + b) - gammln(a) - gammln(b) + a * Math.log(x) +
          b * Math.log(1.0 - x));
    }

    if (x < (a + 1.0) / (a + b + 2.0)) {
      return bt * betacf(a, b, x) / a;
    } else {
      return 1.0 - bt * betacf(b, a, 1.0 - x) / b;
    }
  }

  private static double probability(
      final double f,
      final double fn,
      final double fm,
      final double fk) {

    return fk >= 0 ?
        betai(fn * 0.5, fm * 0.5, (fn / (fn + fm * f))) :
        gammq(fm * 0.5, f * 0.5);
  }

  private static double probability(
      final int m, final long n, final int k,
      final double chi_sqr) {

    if (m < 1 || chi_sqr < 0.0) {
      return -1.0;
    }

    if (k < 0) {
      return 1. - gammq(m * 0.5, chi_sqr * 0.5);
    }

    double f_n = n + k;

    if (f_n <= 0) {
      return 1.0;
    }

    return 1.0 - betai(f_n * 0.5, m * 0.5, (f_n / (f_n + chi_sqr)));
  }

  public static double fStatistic(final int m, final int n, final int k,
      final double p) {

    double fn = n + k;
    double fm = m;
    double fk = k;
    double fp = 1.0 - p;

    double[] x = new double[] {0., 1.4};

    if (m < 0 || p >= 1.0) {
      return -1.;
    }

    UnivariateFunction func = d -> probability(d, fn, fm, fk) - fp;

    if (k >= 0) {

      if (fn == 0) {
        return 1e100;
      }

      // ZBrac begins with x1 == 0.0, x2 == 1.4, but they're modified by the algorithm.
      // You cannot pass in the original values to zbrent.
      ZBrac zBrac = ZBrac.zbrac(0.0, 1.4, func);

      if (zBrac.isBracketed()) {
          return fm * new BrentSolver().solve(ZBRAC_MAXIT, func,
              zBrac.getX1(), zBrac.getX2(), zBrac.getMidpoint());
      }

    } else {

      ZBrac zBrac = ZBrac.zbrac(0, 1.4, func);
      if (zBrac.isBracketed()) {
        return new BrentSolver().solve(ZBRAC_MAXIT, func,
            zBrac.getX1(), zBrac.getX2(), zBrac.getMidpoint());
      }
    }

    return -1.0;
  }

  /**
   * Similar to calculateUncertainties() in SolverLSQ.java of the locoo3d project,
   * this method returns a 4 by 4 uncertainty matrix and a 4 element length vector wrapped
   * as an instance of {@code Pair<RealMatrix, RealVector}. The original locoo3d version
   * returns the result in a 5 by 4 double array with with the first 4 rows
   * holding the uncertainty matrix and the 5th row holding the lengths. This version returns
   * the uncertainties in the matrix and the lengths in the vector.
   *
   * The columns of the uncertainty matrix are orthonormal unit VectorMods that describe the
   * principal axes of the 4D uncertainty hyperellipse. The lengths of the VectorMods, returned
   * in the 4 element vector, correspond to the distance from the center of the hyperellipse to
   * its perimeter. The perimeter corresponds to the contour where chi-square = 1.0.
   *
   * For location parameters that were fixed, the length of the corresponding VectorMod
   * will be zero, indicating perfect confidence in that parameter.  Parameters
   * with a singular value < singularValueCutoff, will have infinite length VectorMods,
   * indicating 0 confidence in those parameters.
   *
   * In all of SolverLSQ, the units of LAT, LON have been radians, and the units
   * of DEPTH have been km.  The units of the derivatives of tt, az, sh wrt to
   * LAT, LON and DEPTH have been xxx/km.  Components of the
   * uncertainty matrix are converted to km so that the 3 spatial components have
   * the same units.  In routine LocatorResults, where the uncertainty matrix is
   * soon to be sent, the units of all 3 spatial components is assumed to be km.
   *
   * @param aMatrix an n by m matrix filtered by column. The inner matrix must have 4 columns,
   *   therefore, m <= 4. The only reason columns are filtered is when elements of the location
   *   solution are held fixed.
   *
   * @param singularValueCutoff the singular value cutoff value. This
   *   is typically a small value such as 1e-6.
   *
   * @return
   */
  public static Pair<RealMatrix, RealVector> computeUncertainties(
      final ColumnFilteredRealMatrix aMatrix,
      final double singularValueCutoff) {

    Validate.notNull(aMatrix, "aMatrix must not be null");

    if (aMatrix.getInnerColumnDimension() != 4) {
      throw new IllegalArgumentException("aMatrix.getInnerColumnDimension() != 4: " +
          aMatrix.getInnerColumnDimension());
    }

    final int n = aMatrix.getRowDimension();
    final int m = aMatrix.getColumnDimension();

    if (m > n) {
      throw new IllegalArgumentException(String.format("m is greater than n: %d > %d",
          m, n));
    }

    final int[] includedDims = aMatrix.includedInnerColumns();

    final SingularValueDecomposition svd = new SingularValueDecomposition(aMatrix);

    // n by m orthogonal
    final RealMatrix uMatrix = svd.getU();

    // m singular values sorted in descending order.
    final double[] singularValues = svd.getSingularValues();

    // m by m orthogonal
    final RealMatrix vMatrix = svd.getV();

    // The max singular value divided by the min singular value, but limited to MAX_CONDITION_NUMBER
    final double conditionNumber = Math.min(MAX_CONDITION_NUMBER, svd.getConditionNumber());

    final double[][] uncertainty = new double[4][4];
    final double[] lengths = new double[4];

    // multiply each component of each principal axis by its length
    for (int col = 0; col < m; col++) {

      int innerCol = includedDims[col];

      double length = (singularValues[col] > singularValueCutoff) ?
          1.0 / singularValues[col] : MAX_LENGTH;

      for (int row = 0; row < m; row++) {
        int innerRow = includedDims[row];
        uncertainty[innerRow][innerCol] = vMatrix.getEntry(row, col) * length;
      }
    }

    for (int col=0; col<4; col++) {
      double sumSq = 0.0;
      for (int row=0; row<4; row++) {
        double v = uncertainty[row][col];
        sumSq += v*v;
      }
      double length = Math.sqrt(sumSq);
      lengths[col] = length;
      if (length > 0.0) {
        for (int row=0; row<4; row++) {
          uncertainty[row][col] /= length;
        }
      }
    }

    RealMatrix uncertaintyMatrix = MatrixUtils.createRealMatrix(uncertainty);
    RealVector lengthVector = MatrixUtils.createRealVector(lengths);

    return Pair.of(uncertaintyMatrix, lengthVector);
  }

  private static double sigNum(double a, double b) {
    return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
  }

  /**
   * Computes the sigma
   *
   * @param scalingFactorType must be COVERAGE, CONFIDENCE, or K_WEIGHTED
   * @param k only relevant if scalingFactorType is K_WEIGHTED. In that case it must be >= 1
   * @param definingObservationCount the number of defining observations used
   *   in the location algorithm
   * @param m the number of free parameters, which must be in [1 - 4]
   * @param weightedResidualSumSquares sum of squares of the weighted
   *   residuals for defining observations
   * @param aprioriVariance the apriori variance
   *
   * @return the sigma value, which is >= 0.0
   */
  public static double sigma(
      final ScalingFactorType scalingFactorType,
      final int k,
      final int definingObservationCount,
      final int m,
      final double weightedResidualSumSquares,
      final double aprioriVariance
  ) {
    Validate.notNull(scalingFactorType);
    if (m < 1 || m > 4) {
      throw new IllegalArgumentException("m not in range [1-4]: " + m);
    }
    int K = k;
    if (scalingFactorType == ScalingFactorType.COVERAGE) {
      // Just return the sqrt.
      return Math.sqrt(aprioriVariance);
    } else if (scalingFactorType == ScalingFactorType.CONFIDENCE) {
      K = 0;
    } else if (scalingFactorType == ScalingFactorType.K_WEIGHTED) {
      if (k <= 0) {
        throw new IllegalArgumentException(
            "for k-weighted, k must be > 0: " + k
        );
      }
    }
    double sigma = 0.0;
    int denominator = K + definingObservationCount - m;
    if (denominator > 0) {
      sigma = Math.sqrt((K * aprioriVariance + weightedResidualSumSquares)/denominator);
    }
    return sigma;
  }

  /**
   * Computes 4 kappa values. The 2nd element can be used as a scaling factor for
   * the axes of the uncertainty ellipse. The 3rd element can be used as the
   * scaling factor for the uncertainty ellipsoid.
   *
   * @param scalingFactorType must be COVERAGE, CONFIDENCE, or K_WEIGHTED
   * @param k only relevant if scalingFactorType is K_WEIGHTED. In that case it must be >= 1
   * @param definingObservationCount the number of defining observations used
   *   in the location algorithm
   * @param m the number of free parameters, which must be in [1 - 4]
   * @param weightedResidualSumSquares sum of squares of the weighted
   *   residuals for defining observations
   * @param confidence a confidence level in the range [0 - 1]
   * @param aprioriVariance the apriori variance
   *
   * @return the sigma value, which is >= 0.0
   */
  public static double[] kappas(
      final ScalingFactorType scalingFactorType,
      final int k,
      final int definingObservationCount,
      final int m,
      final double weightedResidualSumSquares,
      final double confidence,
      final double aprioriVariance) {

    if (confidence < 0.0 || confidence > 1.0) {
      throw new IllegalArgumentException("confidence must be in [0 - 1]: " +
          confidence);
    }

    // This will do the rest of the parameter checking for us.
    double sigma = sigma(
        scalingFactorType,
        k,
        definingObservationCount,
        m,
        weightedResidualSumSquares,
        aprioriVariance
    );

    int K = k;
    if (scalingFactorType == ScalingFactorType.COVERAGE) {
      K = -1;
    } else if (scalingFactorType == ScalingFactorType.CONFIDENCE) {
      K = 0;
    }

    double[] kappas = new double[4];

    if (definingObservationCount - m >= 0 && sigma > 0.0) {
      for (int i=0; i<4; i++) {
        kappas[i] = sigma * Math.sqrt(
            fStatistic(
                i + 1,
                definingObservationCount - m,
                K,
                confidence
            )
        );
      }
    }

    return kappas;
  }

  /**
   * Utility class to contain the results of zbracketing. Instances are immutable.
   */
  private static class ZBrac {

    private final double x1;
    private final double x2;
    private final boolean bracketed;

    /**
     * Constructor.
     * @param x1 the lower bound of the bracket.
     * @param x2 the upper bound of the bracket.
     * @param bracketed true, if a root exists within [x1, x2] for the function used
     *   in the bracketing. false, if no root was found.
     */
    private ZBrac(double x1, double x2, boolean bracketed) {
      this.x1 = Math.min(x1, x2);
      this.x2 = Math.max(x1, x2);
      this.bracketed = bracketed;
    }

    /**
     * Returns the midpoint of the interval.
     * @return
     */
    public double getMidpoint() {
      return (x1 + x2)/2.0;
    }

    /**
     * Performs the zbracketing algorithm and returns a ZBrac instance with the results.
     * @param x1 the starting lower bound for the bracket
     * @param x2 the starting upper bound for the bracket
     * @param func the function which should have a root within the bracket
     * @return a ZBrac instance. If this instance returns isBracket() == true, a root of the
     *   function exists between the instance's x1 and x2. These may not be the same as the x1, x2
     *   passed to this method.
     * @throws IllegalArgumentException if x1 equals x2.
     */
    public static ZBrac zbrac(double x1, double x2, UnivariateFunction func) {

      if (x1 == x2) {
        throw new IllegalArgumentException("x1 == x2: " + x1);
      }

      double a = x1;
      double b = x2;
      double f1 = func.value(a);
      double f2 = func.value(b);

      OptionalDouble highestNegative = OptionalDouble.empty();
      OptionalDouble lowestPositive = OptionalDouble.empty();

      boolean bracketed = false;

      // Look for a root between a and b
      for (int i = 0; i<ZBRAC_MAXIT; i++) {

        // Keep track of which values correspond to the highest negative function value and
        // the lowest positive function value. This makes the final interval smaller.
        highestNegative = updateHighestNegative(highestNegative, a, f1);
        highestNegative = updateHighestNegative(highestNegative, b, f2);
        lowestPositive = updateLowestPositive(lowestPositive, a, f1);
        lowestPositive = updateLowestPositive(lowestPositive, b, f2);

        // If one is positive and one is negative, the function has a root within the interval.
        if (f1 * f2 < 0.0) {
          bracketed = true;
          // Update to highest for which function was negative and lowest for which
          // function was positive.
          a = highestNegative.getAsDouble();
          b = lowestPositive.getAsDouble();
          break;
        }
        // If no root, expand the interval.
        if (Math.abs(f1) < Math.abs(f2)) {
          // Bump a down
          f1 = func.value(a += ZBRAC_FACTOR * (a - b));
        } else {
          // Bump b up.
          f2 = func.value(b += ZBRAC_FACTOR * (b - a));
        }
      }

      return new ZBrac(a, b, bracketed);
    }

    private static OptionalDouble updateHighestNegative(
        OptionalDouble highestNegative, double x, double fx) {

      OptionalDouble result = highestNegative;

      if (fx < 0.0) {
        if (!result.isPresent() || result.getAsDouble() < x) {
          result = OptionalDouble.of(x);
        }
      }

      return result;
    }

    private static OptionalDouble updateLowestPositive(
        OptionalDouble lowestPositive, double x, double fx) {

      OptionalDouble result = lowestPositive;

      if (fx > 0.0) {
        if (!result.isPresent() || result.getAsDouble() > x) {
          result = OptionalDouble.of(x);
        }
      }

      return result;
    }

    /**
     * Returns the lower bound of the interval.
     * @return
     */
    public double getX1() {
      return x1;
    }

    /**
     * Returns the upper bound of the interval.
     * @return
     */
    public double getX2() {
      return x2;
    }

    /**
     * Returns whether or not a root was found in the interval.
     * @return
     */
    public boolean isBracketed() {
      return bracketed;
    }
  }
}
