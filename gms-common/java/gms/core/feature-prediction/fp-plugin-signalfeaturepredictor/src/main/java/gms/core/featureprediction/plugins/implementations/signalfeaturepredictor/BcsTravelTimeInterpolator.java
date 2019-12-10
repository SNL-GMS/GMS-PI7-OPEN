package gms.core.featureprediction.plugins.implementations.signalfeaturepredictor;

import gms.core.featureprediction.plugins.DepthDistance1dModelSet;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.utilities.geomath.BicubicInterpolator;
import gms.shared.utilities.geomath.BicubicSplineInterpolator;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;


/**
 * Default algorithm travel time (arrival time) in SignalFeaturePredictor plugin
 */
public class BcsTravelTimeInterpolator {

  private static final int MAX_DIST_SAMPLES = 7;
  private static final int MAX_DEPTH_SAMPLES = 4;
  private static final int MIN_NUM_DIST_SAMPLES = 3;
  private static final double DISTANCE_PROXIMITY_TOLERANCE = 0.00001;
  private static final double DEPTH_PROXIMITY_TOLERANCE = 0.001;

  private static final double FLOAT_EPSILON = 1.0e-7;

  private final DepthDistance1dModelSet<double[], double[][]> depthDistance1dModelSet;
  private final BicubicInterpolator interpolator = new BicubicSplineInterpolator();
  private final String earthModelName;
  private final PhaseType phaseType;
  private final boolean extrapolateGridpoints;

  private boolean wasExtrapolated;

  private BcsTravelTimeInterpolator(
      DepthDistance1dModelSet<double[], double[][]> depthDistance1dModelSet,
      String earthModelName, PhaseType phaseType, boolean extrapolateGridpoints) {
    this.depthDistance1dModelSet = depthDistance1dModelSet;
    this.earthModelName = earthModelName;
    this.phaseType = phaseType;
    this.extrapolateGridpoints = extrapolateGridpoints;
  }


  /**
   * Estimate phase travel time for a given depth over an angular distance
   *
   * @param depthKm location of event in kilometers below surface of spherical earth
   * @param angleDeg angular degrees between event and receiver as measured from center of earth
   * @return phase travel time from event to receiver in seconds
   */
  public double[] getPhaseTravelTimeAndDerivatives(double depthKm, double angleDeg) {
    double depthsKm[] = depthDistance1dModelSet.getDepthsKm(earthModelName, phaseType);
    double anglesDeg[] = depthDistance1dModelSet.getDistancesDeg(earthModelName, phaseType);
    double travelTimesSec[][] = depthDistance1dModelSet
        .getValues(earthModelName, phaseType);

    wasExtrapolated = false;

    Triple<double[], double[], double[][]> miniTable = generateMinitable(
        depthKm,
        angleDeg,
        depthsKm,
        anglesDeg,
        travelTimesSec);

    double[][] newTravelTimeSec = miniTable.getRight();
    double[] newAnglesDeg = miniTable.getMiddle();
    double[] newDepthsKm = miniTable.getLeft();

    return interpolator.getFunctionAndDerivatives(newAnglesDeg, newDepthsKm,
        transposeMatrix(newTravelTimeSec)).apply(angleDeg, depthKm);
  }

  public boolean wasExtrapolated() {
    return wasExtrapolated;
  }

  private static double[][] transposeMatrix(double[][] m) {
    double[][] temp = new double[m[0].length][m.length];
    for (int i = 0; i < m.length; i++) {
      for (int j = 0; j < m[0].length; j++) {
        temp[j][i] = m[i][j];
      }
    }
    return temp;
  }

  /**
   * Rational Polynomial Interpolation / Extrapolation Function
   *
   * Given arrays xa[0..n-1] and ya[0..n-1], and given a value, x, this routine returns a value of y
   * and an accuracy estimate, dy. The value returned is that of the diagonal rational function,
   * evaluated at x, which passes through the n points (xa[i], ya[i]), i = 0..n-1.)
   *
   * @param xa - Pointer to a vector of x values.
   * @param ya - Pointer to a vector of y values.
   * @param x - Value at which to interpolate / extrapolate.
   * @return Interpolated value.
   */
  private static double ratint(double[] xa, double[] ya, double x) {
    double dd, h, hh, t, w;
    int ns = 0, n = xa.length;
    double[] c = new double[n];
    double[] d = new double[n];

    double y;

    hh = Math.abs(x - xa[0]);

    for (int i = 0; i < n; i++) {
      h = Math.abs(x - xa[i]);
      if (h == 0.0) {
        return ya[i];
      } else if (h < hh) {
        ns = i;
        hh = h;
      }
      c[i] = ya[i];
      d[i] = ya[i] + FLOAT_EPSILON; /* Needed to prevent a rare */
    }                                 /* zero-over-zero condition */

    y = ya[ns--];

    for (int m = 0; m < n - 1; m++) {
      for (int i = 0; i < n - 1 - m; i++) {
        w = c[i + 1] - d[i];
        h = xa[i + m + 1] - x;
        t = (xa[i] - x) * d[i] / h;
        dd = t - c[i + 1];

        // Interpolating function has a pole at the requested value of x.
        // Return error
        if (Math.abs(dd) < 1e-15 ) {
          return Double.NaN;
        }

        dd = w / dd;
        d[i] = c[i + 1] * dd;
        c[i] = t * dd;
      }
      y += 2 * (ns + 1) < (n - m - 1) ? c[ns + 1] : d[ns--];
    }
    return y;
  }

  /**
   * Constructs a minitable around the given depth and distance from the given table of values.
   * Holes in the table are extrapolated.
   */
  private Triple<double[], double[], double[][]> generateMinitable(double depth,
      double distance, double[] depths, double[] distances, double[][] values) {

    int xleft, zleft, nx_req, nz_req;
    int xlow, xhigh, ztop, zbottom;
    //int num_samp = 0;
    int idist = 0, idepth = 0;

    double xshift, zshift, diff;

    // Required # of samples in x-direction
    nx_req = Math.min(MAX_DIST_SAMPLES, distances.length);

    // Required # of samples in z-direction
    nz_req = Math.min(MAX_DEPTH_SAMPLES, depths.length);

    double[] ttcsInHoleDist = new double[]{181., -1., 0.};

    boolean ok_so_far = true;
    for (int i = 1; i < distances.length; i++) {
      if (!Double.isNaN(values[0][i - 1]) && Double.isNaN(values[0][i])) {
        ttcsInHoleDist[0] = distances[i - 1];
        ok_so_far = false;
      } else if (!ok_so_far && !Double.isNaN(values[0][i])) {
        ttcsInHoleDist[1] = distances[i];
        break;
      }
    }
    boolean in_hole = distance > ttcsInHoleDist[0]
        && distance < ttcsInHoleDist[1];

    // ====================================================================
    // Set Depth Range
    // ====================================================================
    if (depths.length == 1) {
      // ------------------------------------------------------------------
      // CASE A: Only 1 depth sample available
      // ------------------------------------------------------------------
      ztop = 0;
      zbottom = 0;
      //TODO: tolerance?
      if (depth != depths[0])
      //TODO: handle this
      {
        return null;
      }
    } else {

      // ------------------------------------------------------------------
      // CASE B: Table contains at least 2 depth samples
      // ------------------------------------------------------------------
      zleft = hunt(depths, depth);

      if (zleft < 0) // depth < min. table depth
      {
        // Check if exactly equal
        if (Math.abs(depth - depths[0]) >= DEPTH_PROXIMITY_TOLERANCE) {
          idepth--;
        }
        ztop = 0;
        zbottom = nz_req - 1;
      } else if (zleft >= depths.length - 1) // depth > max. table depth
      {
        idepth++;
        ztop = depths.length - nz_req;
        zbottom = depths.length - 1;
      } else
      // requested depth within valid range
      {
        zbottom = Math.min(zleft + (nz_req / 2), depths.length - 1);
        ztop = Math.max(zbottom - nz_req + 1, 0);
        nz_req = zbottom - ztop + 1;
      }
    }

    // ====================================================================
    // Set Distance Range
    // ====================================================================
    // --------------------------------------------------------------------
    // Preliminary Bracketing
    // --------------------------------------------------------------------
    xleft = hunt(distances, distance);

    if (xleft < 0) {
      // Case 1: distance < minimum table distance
      // Check if exactly equal
      if (Math.abs(distance - distances[0]) >= DISTANCE_PROXIMITY_TOLERANCE) {
        idist--;
      }
      xlow = 0;
      xhigh = nx_req - 1;
    } else if (xleft >= distances.length - 1) {
      // Case 2: distance > maximum table distance
      idist++;
      xlow = distances.length - nx_req;
      xhigh = distances.length - 1;
    } else {
      // Case 3: distance within valid table region

      // Distance is within a valid table region, but may not have a
      // valid value. Interogate table in order to obtain as many
      // valid values as possible for either direct interpolation or
      // eventual extrapolation. This is determined by the xlow and
      // xhigh settings.

      // Make sure that high and low end requested does not run us
      // off one side of the distance curve or the other. We need
      // to do this even before we check the actual values contained
      // in the 2-D (x-z) array.

      xhigh = Math.min(xleft + (nx_req / 2), distances.length - 1);
      xlow = Math.max(xhigh - nx_req + 1, 0);
      if (xlow == 0) {
        xhigh = nx_req - 1;
      }
    }

    // --------------------------------------------------------------------
    // Final Adjustment of Distance Range Bounds
    //
    // If requested distance sample is within table bounds, then we
    // need to find as many valid samples as possible. If none exists
    // shift xlow and xhigh closest to a valid curve. On the other
    // hand, if the requested distance sample is located clearly
    // outside the valid table region, create an artificial mini-table
    // surrounding the requested sample distance value.
    // --------------------------------------------------------------------
    if (in_hole) {
      // ------------------------------------------------------------------
      // Case 1: Requested Distance is in a Hole
      // ------------------------------------------------------------------

      // Check outer distance value
      if (invalid(values[ztop][xhigh])) {
        // Case A: Outer distance sample is also in the hole
        wasExtrapolated = true;

        for (int i = 0; i < (nx_req - 1) / 2; i++) {
          // Shift upper distance bound downward until a valid table
          // entry is found
          --xhigh;
          if (valid(values[ztop][xhigh])) {
            break;
          }
        }
        // Reset lower distance bound to reflect the new upper bound
        xlow = xhigh - nx_req + 1;
      } else {
        // Case B: Outer distance sample is valid

        //**** added distance shift up, similar to distance shift down above.
        //**** Not sure why this was'nt done properly here.
        //****  (jrh 12-19-2018)
        for (int i = 0; i < (nx_req - 1) / 2; i++) {
          // Shift upper distance bound downward until a valid table
          // entry is found
          ++xlow;
          if (valid(values[ztop][xlow])) {
            break;
          }
        }
        // Reset higher distance bound to reflect the new lower bound
        xhigh = xlow + nx_req - 1;

        // Use a "safe" value from the distances before the hole
        //TODO: just use next valid value in table?
        // xhigh = 109;
        // xlow = xhigh - nx_req + 1;
      }
    } else if (idist == 0) {
      // ------------------------------------------------------------------
      // Case 2: Distance w/in Table, but not in a hole
      // ------------------------------------------------------------------

      // Check to see if the upper distance bound is in a bad sample region
      // Check outer distance value
      if (valid(values[ztop][0])
          && invalid(values[ztop][xhigh])) {
        wasExtrapolated = true;
        idist = 1;
        for (int i = 0; i < (nx_req - 1) / 2; i++) {
          // Shift upper distance bound downward until a valid table
          // entry is found
          --xhigh;
          if (valid(values[ztop][xhigh])) {
            idist = 0;
            break;
          }
        }
        // Reset lower distance bound to reflect the new upper bound
        xlow = xhigh - nx_req + 1;
      }
      // Check to see if the lower distance bound is in a bad sample
      // region
      else if (invalid(values[ztop][xlow])) {
        wasExtrapolated = true;

        idist = -1;
        for (int i = 0; i < (nx_req - 1) / 2; i++) {
          // Shift lower distance bound upward until a valid table
          // entry is found
          ++xlow;
          if (valid(values[ztop][xlow])) {
            idist = 0;
            break;
          }
        }
        // Reset upper distance bound to reflect the new upper bound
        xhigh = xlow + nx_req - 1;
      }
    }

    // ====================================================================
    // Construct Mini Table
    //
    // Up to now we have only inspected the 1st depth component on the
    // distance vector. Now we will build a complete mini-table which
    // will be used for actual inter/extrapolation using rational
    // function and bi-cubic spline interpolation routines.
    // ====================================================================

    double[][] mini_table = new double[nz_req][];
    double[][] deriv_2nd = new double[nz_req][nx_req];

    double[] mini_dist = extract(distances, xlow, nx_req);
    double[] mini_depth = extract(depths, ztop, nz_req);

    if (!extrapolateGridpoints) {
      for (int k = 0, kk = ztop; k < nz_req; k++, kk++) {
        mini_table[k] = extract(values[kk], xlow, nx_req);
      }
      return Triple.of(mini_depth, mini_dist, mini_table);
    }

    // --------------------------------------------------------------------
    // First, construct mini-table assuming no depth extrapolation is
    // needed. All distance extrapolation will be handled in this master
    // "for loop".
    // --------------------------------------------------------------------
    for (int k = 0, kk = ztop; k < nz_req; k++, kk++) {
      // First fill mini_table assuming all values[][] values are valid
      mini_table[k] = extract(values[kk], xlow, nx_req);
      double[] min_table_row = mini_table[k].clone();

      // ------------------------------------------------------------------
      // Check the distance value with respect to range of distances
      // ------------------------------------------------------------------
      if (in_hole || idist > 0) {
        int ii = -1;
        // ----------------------------------------------------------------
        // Case 1: Off high end of distance curve -OR-
        // in a hole
        // ----------------------------------------------------------------
        diff = distance - distances[xhigh];
        if (idist > 0 && diff > 1e-9) {
          // Case A: Off the high end of the distance curve
          //
          // Shift the distances associated with the mini table out to
          // a region centered about the requested distance
          xshift = distance - distances[xhigh - ((nx_req - 1) / 2)];
          for (int j = 0; j < nx_req; j++) {
            if (k < 1) {
              mini_dist[j] = mini_dist[j] + xshift;
            }
            mini_table[k][j] = Double.NaN;
          }
          ii = xlow;
        } else {
          // Case B: In a hole in the distance curve

          //**** Added check for xhigh or xlow having an invalid value. It is
          //**** possible when the interpolation point is in a hole, but not
          //**** shifted left or right, to have either end with invalid points.
          //**** (jrh 12-18-2018)
          if (valid(values[kk][xhigh])) {
            for (int i = xlow; i < distances.length; i++) {
              // Look for the first good value scanning upward from
              // the
              // lower distance bound
              if (valid(values[kk][i])) {
                ii = i;
                break;
              }
            }
          } else {
            // Scanning downward in distance, look for valid values in
            // the table to use for extrapolation fill-in of the mini table
            for (int i = xhigh; i >= 0; i--) {
              // Look for the first good value scanning downward from
              // the upper distance bound
              if (valid(values[kk][i])) {
                ii = i;
                break;
              }
            }
            ii = ii - nx_req + 1;
          }
        }

        //**** added two rows below to get valid rational spline interpolant
        //****  (jrh 12-18-2018)
        double[] min_table_row_ex = extract(values[kk], ii, nx_req);
        double[] mini_dist_ex = extract(distances, ii, nx_req);

        // At this depth (k) in the mini-table, extrapolate any missing
        // values along the distance direction
        for (int j = 0; j < nx_req; j++) {
          if (invalid(mini_table[k][j]))
          //**** replaced arguments mini_dist and min_table_row with their
          //**** properly shifted versions mini_dist_ex and min_table_row_ex
          //**** (jrh 12-18-2018)
          {
            mini_table[k][j] = ratint(mini_dist_ex, min_table_row_ex, mini_dist[j]);
          }
        }

      } // End if (in_hole || idist > 0)

      else if (idist < 0) {
        // ----------------------------------------------------------------
        // Case 2: Off low end of distance curve
        // ----------------------------------------------------------------
        if (distance < distances[xlow]) {
          // Shift the distances associated with the mini table down
          // to
          // a region centered about the requested distance
          xshift = distance - distances[xlow + ((nx_req - 1) / 2)];
          for (int j = 0; j < nx_req; j++) {
            if (k < 1) {
              mini_dist[j] = mini_dist[j] + xshift;
            }
            mini_table[k][j] = Double.NaN;
          }
        }

        // At this depth (k) in the mini-table, interpolate any missing
        // values along the distance direction
        for (int j = 0; j < nx_req; j++) {
          if (invalid(mini_table[k][j])) {
            wasExtrapolated = true;
            mini_table[k][j] = ratint(mini_dist, min_table_row, mini_dist[j]);
          }
        }
      } else {
        // ----------------------------------------------------------------
        // Case 3: Distance is at a valid range in the distance vector
        //
        // Make sure there are no single BAD_SAMPLE entries. If so,
        // extrapolate as necessary.
        // ----------------------------------------------------------------
        for (int j = 0; j < nx_req; j++) // Scan distances (j) in the mini
        { // table at this depth (k)
          if (invalid(mini_table[k][j])) {
            wasExtrapolated = true;
            if (j > 0) {
              // Go back and get as many valid samples for this
              // depth as is possible for a good sample space.

              int num_extrap = nx_req - j;
              int i = xlow - num_extrap;
              int num_samp = nx_req;
              while (i < 0 || invalid(values[kk][i])) {
                ++i;
                --num_samp;

                // check for minimum sample number

                if (num_samp < MIN_NUM_DIST_SAMPLES)
                //TODO: handle this (original: return WRN_INSUFFICIENT_DATA)
                {
                  return null;
                }
              }

              // Extrapolate a valid traveltime for the mini table
              for (int n = 0; n < num_extrap; n++) {
                int m = j + n;
                if (invalid(mini_table[k][m])) {
                  wasExtrapolated = true;
                  mini_table[k][m] = ratint(
                      extract(distances, i, num_samp),
                      extract(values[kk], i, num_samp),
                      mini_dist[m]);
                }
              }
            } else {
              // Advance in distance and get as many valid samples
              // for
              // this depth as is possible for a good sample
              // space.
              int num_extrap = 0;
              int num_samp = 0;
              int i = xlow;
              for (int n = 0; i < xhigh; i++, n++) {
                if (valid(values[kk][i])) {
                  xlow = i;
                  num_extrap = n;
                  for (int nn = 0; nn < nx_req; nn++) {
                    if (valid(values[kk][xlow + nn])) {
                      ++num_samp;
                    }
                  }

                  // check for minimum sample number

                  if (num_samp < MIN_NUM_DIST_SAMPLES)
                  //TODO: handle this (original: return WRN_INSUFFICIENT_DATA)
                  {
                    return null;
                  }
                  break;
                }
              }

              // Check for at least 1 sample

              if (i == xhigh)
              //TODO: handle this (original: return WRN_INSUFFICIENT_DATA)
              {
                return null;
              }

              // create mini table

              for (int n = 0; n < num_extrap; n++) {
                if (invalid(mini_table[k][n])) {
                  wasExtrapolated = true;
                  mini_table[k][n] = ratint(
                      extract(distances, i, num_samp),
                      extract(values[kk], i, num_samp),
                      mini_dist[n]);
                }
              }
            }
            break;
          }
        }
      }
    }

    // Now that the distance component of the mini-table is secure,
    // perform any necessary extrapolation for the depth component by
    // re-constructing the mini-table. Also, build transposed mini-
    // table, mini_table_trans[][], to obtain distance derivatives
    // from spline routines below.

    double[][] mini_table_trans = new double[nx_req][nz_req];

    for (int j = 0; j < nx_req; j++) {
      // Fill mini_table_trans[][] assuming all values from array,
      // mini_table[][], are valid

      for (int i = 0; i < nz_req; i++) {
        mini_table_trans[j][i] = mini_table[i][j];
      }

      // Are we below the lowest depth component in the curve

      if (idepth > 0) {
        wasExtrapolated = true;
        // Case 1: Off the deep end of the depth range
        zshift = depth - depths[zbottom - ((nz_req - 1) / 2)];
        if (j < 1) {
          for (int i = 0; i < nz_req; i++) {
            mini_depth[i] = mini_depth[i] + zshift;
          }
        }
        // Extrapolate a new set of depths bracketing the requested
        // depth
        for (int i = 0; i < nz_req; i++) {
          mini_table[i][j] = ratint(extract(depths, ztop, nz_req),
              extract(mini_table_trans[j], 0, nz_req),
              mini_depth[i]);
        }
      } else if (idepth < 0) {
        wasExtrapolated = true;
        // Case 2: Off the shallow end of the depth range
        zshift = depth - depths[ztop + ((nz_req - 1) / 2)];
        if (j < 1) {
          for (int i = 0; i < nz_req; i++) {
            mini_depth[i] = mini_depth[i] + zshift;
          }
        }
        // Extrapolate a new set of depths bracketing the requested
        // depth
        for (int i = 0; i < nz_req; i++) {
          mini_table[i][j] = ratint(extract(depths, ztop, nz_req),
              extract(mini_table_trans[j], 0, nz_req),
              mini_depth[i]);
        }
      }
    }

    wasExtrapolated = wasExtrapolated || in_hole;

    return Triple.of(mini_depth, mini_dist, mini_table);
  }

  /**
   * Extract a new array from x that has size elements starting at index first. No range checking is
   * performed!
   *
   * @param x original array
   * @param first index of first element
   * @param size number of elements to extract
   */
  private static double[] extract(double[] x, int first, int size) {
    double[] xx = new double[size];
    for (int i = 0; i < size; ++i) {
      xx[i] = x[first + i];
    }
    return xx;
  }

  private static boolean valid(double value) {
    return !invalid(value);
  }

  private static boolean invalid(double value) {
    return Double.isNaN(value);
  }

  private static int hunt(double[] values, double x) {
    if (x == values[values.length - 1]) {
      return values.length - 2;
    }

    int i;
    int bot = -1;
    int top = values.length;
    while (top - bot > 1) {
      i = (top + bot) / 2;
      if (x >= values[i]) {
        bot = i;
      } else {
        top = i;
      }
    }
    return bot;
  }


  /**
   * A mutable builder for a {@link BcsTravelTimeInterpolator}.  The builder has two phases. At
   * inception, it is in the build phase in which it can be modified. Once the build() method is
   * called, the {@link Builder} transitions to the built phase, to create the {@link
   * BcsTravelTimeInterpolator}.  Once the build() method is called, the {@link Builder} can no
   * longer be used.
   */
  public static final class Builder {

    private boolean built = false;

    //private EarthModel1dPlugin depthDistance1dModelSet;

    private DepthDistance1dModelSet<double[], double[][]> depthDistance1dModelSet;
    private String earthModelName;
    private PhaseType phase;
    private boolean extrapolate;
    private String ALREADY_BUILT_ERR_MSG = "DefaultTravelTimeEstimation has already been built";

    /**
     * Sets the earth model 1D plugin for {@link BcsTravelTimeInterpolator}
     *
     * @param depthDistance1dModelSet 1D plugin for travel time estimation
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * SlownessSignalFeaturePredictorAlgorithm}
     */
    public Builder withEarthModelsPlugin(
        DepthDistance1dModelSet<double[], double[][]> depthDistance1dModelSet) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_ERR_MSG);
      }

      this.depthDistance1dModelSet = depthDistance1dModelSet;
      return this;
    }

    /**
     * Sets the name of the earth model for {@link BcsTravelTimeInterpolator}
     *
     * @param earthModelName for travel time estimation
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * BcsTravelTimeInterpolator}
     */
    public Builder withEarthModelName(String earthModelName) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_ERR_MSG);
      }

      this.earthModelName = earthModelName;
      return this;
    }

    /**
     * Sets the phase type for {@link BcsTravelTimeInterpolator}
     *
     * @param phase phase of earth model
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * BcsTravelTimeInterpolator}
     */
    public Builder withPhaseType(PhaseType phase) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_ERR_MSG);
      }

      this.phase = phase;
      return this;
    }

    public Builder withExtrapolation(boolean extrapolate) {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_ERR_MSG);
      }

      this.extrapolate = extrapolate;
      return this;
    }

    /**
     * Builds the {@link BcsTravelTimeInterpolator} from the parameters defined during the build
     * phase.
     *
     * @return a new {@link BcsTravelTimeInterpolator}
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * BcsTravelTimeInterpolator}, or if any parameters are set to illegal values.
     */
    public BcsTravelTimeInterpolator build() {
      if (built) {
        throw new IllegalStateException(ALREADY_BUILT_ERR_MSG);
      }

      built = true;

      // validate algorithm parameters
      Validate.notNull(depthDistance1dModelSet, "depthDistance1dModelSet is null");
      Validate.notEmpty(earthModelName, "Earth model name is empty");
      Validate.notNull(phase, "PhaseType is null");
      Validate.isTrue(depthDistance1dModelSet.getEarthModelNames().contains(earthModelName),
          "Earth model, " + earthModelName + ", not in earth model 1D plugin set.");
      Validate
          .isTrue(depthDistance1dModelSet.getPhaseTypes(earthModelName).contains(phase),
              "Phase type, " + phase + ", does not exist in earth model, " + earthModelName
                  + ".");

      return new BcsTravelTimeInterpolator(depthDistance1dModelSet,
          earthModelName, phase, extrapolate);
    }
  }

}