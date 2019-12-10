package gms.core.signalenhancement.fk.plugin.fkattributes;

import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Functions to identify the location of the max and the associated slowness and azimuth from a
 * given FK spectrum
 */
public class DefaultFkMeasurementsAlgorithms {
  public static final double GEO_POLAR_ROTATION = 90;
  /**
   * This conversion term is calculated using the radius of the earth in
   * kilometers (6371 km).
   */
  public static final double KM_TO_DEGREES = 6371 * 2 * Math.PI / 360;
  /**
   * Epsilon constant used for mitigating measurement error.
   */
  public static final double EPSILON = 0.001;

  /**
   * The values in the fk spectrum parameter have been calculated with the FK stat
   * function, measuring the power of the beam at a provided slowness.
   *
   * @param fk the two-dimensional array of FK values
   * @return the (x, y) coordinates of the maximum value
   * @throws IllegalArgumentException if the fk spectrum is empty or has empty rows
   */
  public static Pair<Double, Double> indexOfFkMax(double[][] fk) {
    Objects.requireNonNull(fk, "FK spectrum cannot be null");

    if (fk.length == 0) {
      throw new IllegalArgumentException("FK spectrum must be non-empty");
    }
    if (fk[0].length == 0) {
      throw new IllegalArgumentException("FK spectrum must have non-empty rows");
    }

    double xMax = 0;
    double yMax = 0;
    double fkMax = -Double.MAX_VALUE;

    for (int y = 0; y < fk.length; y++) {
      for (int x = 0; x < fk[y].length; x++) {
        if (fk[y][x] > fkMax) {
          fkMax = fk[y][x];
          xMax = x;
          yMax = y;
        }
      }
    }

    return Pair.of(xMax, yMax);
  }

  /**
   * Calculate the slowness at the coordinates of the given index.
   *
   * @param xSlowStart  the x coordinate for the start of the viewing window in the slowness space
   * @param xSlowDelta  the change in the x coordinate for each array index
   * @param ySlowStart  the y coordinate for the start of the viewing window in the slowness space
   * @param ySlowDelta  the change in the y coordinate for each array index
   * @param xCoordinate x coordinate of the maximum FK stat value
   * @param yCoordinate y coordinate of the maximum FK stat value
   * @return the slowness measurement for the given index (Units: sec/deg)
   */
  public static double slownessOfIndex(double xSlowStart, double xSlowDelta, double ySlowStart,
      double ySlowDelta, double xCoordinate, double yCoordinate) {
    double xComponent = xSlowStart + xSlowDelta * xCoordinate;
    double yComponent = ySlowStart + ySlowDelta * yCoordinate;

    return Math.sqrt(Math.pow(xComponent, 2) + Math.pow(yComponent, 2)) * KM_TO_DEGREES;
  }

  /**
   * Calculate the slowness at the coordinates of the given index.
   *
   * @param xSlowStart  the x coordinate for the start of the viewing window in the slowness space
   * @param xSlowDelta  the change in the x coordinate for each array index
   * @param xCoordinate x coordinate of the maximum FK stat value
   * @return the x component of the slowness vector for the given index
   */
  public static double slownessXComponent(double xSlowStart, double xSlowDelta,
      double xCoordinate) {
    double xComponent = xSlowStart + xSlowDelta * xCoordinate;

    return xComponent;
  }

  /**
   * Calculate the slowness at the coordinates of the given index.
   *
   * @param ySlowStart  the y coordinate for the start of the viewing window in the slowness space
   * @param ySlowDelta  the change in the y coordinate for each array index
   * @param yCoordinate y coordinate of the maximum FK stat value
   * @return the y component of the slowness vector for the given index
   */
  public static double slownessYComponent(double ySlowStart, double ySlowDelta,
      double yCoordinate) {
    double yComponent = ySlowStart + ySlowDelta * yCoordinate;

    return yComponent;
  }

  /**
   * Calculate the azumith of the given (x, y) coordinates in the geographic
   * polar system.
   *
   * @param xSlowStart  the x coordinate for the start of the viewing window in the slowness space
   * @param xSlowDelta  the change in the x coordinate for each array index
   * @param ySlowStart  the y coordinate for the start of the viewing window in the slowness space
   * @param ySlowDelta  the change in the y coordinate for each array index
   * @param xCoordinate x coordinate of the maximum FK stat value
   * @param yCoordinate y coordinate of the maximum FK stat value
   * @return the slowness measurement for the given index (Units: degrees)
   */
  public static double azimuthOfIndex(double xSlowStart, double xSlowDelta, double ySlowStart,
      double ySlowDelta, double xCoordinate, double yCoordinate) {
    double xComponent = xSlowStart + xSlowDelta * xCoordinate;
    double yComponent = ySlowStart + ySlowDelta * yCoordinate;

    /**
     * Handle the special case where the azimuth of the (0,0) vector is requested
     */
    if (xComponent == 0 && yComponent == 0) {
      return 0;
    }
    /**
     * Otherwise, return the azimuth in the geographic polar
     * coordinate system.
     */
    else {
      double azimuth = Math.atan2(yComponent, xComponent);

      if (azimuth < 0) {
        azimuth += 2 * Math.PI;
      }

      return polarToGeoPolar(Math.toDegrees(azimuth));
    }
  }

  /**
   * Convert a given polar coordinate measured counterclockwise from the
   * horizontal axis to the geographical polar coordinate system measured
   * clockwise from the vertical axis.
   * <p>
   * Note that this method does not make use of any modulus or remainder
   * operations, as those require integers.
   *
   * @param angle the angle in the polar coordinate system (Units: degrees)
   * @return the angle in the geographical polar coordinate system (Units: degrees)
   */
  private static double polarToGeoPolar(double angle) {
    double angleGP = GEO_POLAR_ROTATION - angle;

    if (angleGP < 0) {
      angleGP += 360;
    }

    return angleGP;
  }

  /**
   * Calculates the uncertainty associated with the
   * slowness vector of fk max.
   *
   * @param highPass     the high passband frequency used to calculate FkSpectra (Units: Hz)
   * @param lowPass      the low passband frequency used to calculate FkSpectra (Units: Hz)
   * @param fstat        the fstat associated with the maximum slowness value (Unitless)
   * @param dbDownRadius maybe the radius of a circle? (see gitlab
   *                     /byoung/gms/blob/master/fk/fk/ipynb, cell 13, and
   *                     wiki/display/gmswiki/FK+and+Fstat+Calculations)
   * @return the uncertainty associated with the slowness vector of the fk max (Units: sec/deg)
   */
  public static double slownessUncertainty(double highPass, double lowPass, double fstat,
      double dbDownRadius) {
    double centerFrequency = lowPass + (highPass - lowPass) / 2;

    // Calculate the change in slowness
    double delSlow =
        Math.sqrt(Math.pow(EPSILON, 2) + Math.pow(dbDownRadius / centerFrequency, 2) / fstat);

    // dk is in 1/km, so we need to convert to seconds/degree
    return delSlow * KM_TO_DEGREES;
  }

  /**
   * Calculates the uncertainty associated with the azimuth of the maximum
   * slowness vector from the fk spectrum.
   *
   * @param slowness            calculated slowness of the maximum of the fk spectrum (Units:
   *                            sec/deg)
   * @param slownessUncertainty calculated uncertainty of that slowness (Units: sec/deg)
   * @return the uncertainty associated with the azimuth of the fk max (Units: degrees)
   */
  public static double azimuthUncertainty(double slowness, double slownessUncertainty) {
    double uncertainty = 2 * Math.asin(0.5 * slownessUncertainty / slowness);

    //Based on SME guidance, if the azimuth uncertainty resolves to a NaN, we should set it to 180.0
    if (Double.isNaN(uncertainty)) {
      uncertainty = Math.toRadians(180.0);
    }

    return Math.toDegrees(uncertainty);
  }
}
