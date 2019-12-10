package gms.core.eventlocation.plugins.implementations.geigers;

import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import gms.core.eventlocation.plugins.pluginutils.LocatorAlgorithm;
import gms.core.eventlocation.plugins.pluginutils.Util;
import gms.shared.utilities.geomath.EarthShape;
import gms.shared.utilities.geomath.GeoMath;
import gms.shared.utilities.geomath.RowFilteredRealMatrix;
import gms.shared.utilities.geomath.VectorUnit;
import java.time.Duration;
import java.time.Instant;
import java.util.BitSet;
import java.util.function.Function;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GeigersAlgorithm implements LocatorAlgorithm<RowFilteredRealMatrix> {

  static final Logger logger = LoggerFactory.getLogger(GeigersAlgorithm.class);

  private final int MAXIMUM_ITERATION_COUNT;
  private final double CONVERGENCE_THRESHOLD;
  private final int CONVERGENCE_COUNT;
  private final boolean LEVENBERG_MARQUARDT_ENABLED;
  private final double LAMBDA_0;
  private final double LAMBDA_X;
  private final double DELTA_NORM_THRESHOLD;
  private final double SINGULAR_VALUE_W_FACTOR;
  private final double MAXIMUM_WEIGHTED_PARTIAL_DERIVATIVE;
  private final boolean CONSTRAIN_LATITUDE_PARAMETER;
  private final boolean CONSTRAIN_LONGITUDE_PARAMETER;
  private final boolean CONSTRAIN_DEPTH_PARAMETER;
  private final boolean CONSTRAIN_TIME_PARAMETER;

  private GeigersAlgorithm(
      final int maximumIterationCount,
      final double convergenceThreshold,
      final int convergenceCount,
      final boolean levenbergMarquardtEnabled,
      final double lambda0,
      final double lambdaX,
      final double deltaNormThreshold,
      final double singularValueWFactor,
      final double maximumWeightedPartialDerivative,
      final boolean constrainLatitudeParameter,
      final boolean constrainLongitudeParameter,
      final boolean constrainDepthParameter,
      final boolean constrainTimeParameter) {
    this.MAXIMUM_ITERATION_COUNT = maximumIterationCount;
    this.CONVERGENCE_THRESHOLD = convergenceThreshold;
    this.CONVERGENCE_COUNT = convergenceCount;
    this.LEVENBERG_MARQUARDT_ENABLED = levenbergMarquardtEnabled;
    this.LAMBDA_0 = lambda0;
    this.LAMBDA_X = lambdaX;
    this.DELTA_NORM_THRESHOLD = deltaNormThreshold;
    this.SINGULAR_VALUE_W_FACTOR = singularValueWFactor;
    this.MAXIMUM_WEIGHTED_PARTIAL_DERIVATIVE = maximumWeightedPartialDerivative;
    this.CONSTRAIN_LATITUDE_PARAMETER = constrainLatitudeParameter;
    this.CONSTRAIN_LONGITUDE_PARAMETER = constrainLongitudeParameter;
    this.CONSTRAIN_DEPTH_PARAMETER = constrainDepthParameter;
    this.CONSTRAIN_TIME_PARAMETER = constrainTimeParameter;
  }


  /**
   * Ballard, S.  August 2004. Seismic Event Location Using Levenberg-Marquardt Least Squares
   * Inversion, 2nd Edition.
   */
  @Override
  public Triple<RealVector, RealMatrix, RealMatrix> locate(
      final RealVector mSeed,
      final RealMatrix observations,
      final Function<RealVector, Pair<RowFilteredRealMatrix, RowFilteredRealMatrix>> predictionFunction) {
    Instant start = Instant.now();
    try {
      if (!GeoMath.isNormalizedLatLon(mSeed.getEntry(0), mSeed.getEntry(1))) {
        throw new IllegalStateException("Encountered non-normalized lat/lon");
      }

      LocationEstimate newLoc = new LocationEstimate(
          mSeed,
          observations,
          predictionFunction);

      double lambda = LEVENBERG_MARQUARDT_ENABLED ? LAMBDA_0 : 0.0;
      int convergenceCount = 0;
      int iterationCount = 0;

      do {
        ++iterationCount;
        LocationEstimate oldLoc = (LocationEstimate) newLoc.clone();
        newLoc.update(lambda);
        newLoc.acceptLastComputedLocation();
        if (newLoc.definingObservationListChanged()) {
          convergenceCount = 0;
          lambda = LAMBDA_0;
        }

        if (LEVENBERG_MARQUARDT_ENABLED) {
          while (newLoc.getResidualSumOfSquares() > oldLoc.getResidualSumOfSquares() && newLoc.getDeltaNorm() > DELTA_NORM_THRESHOLD) {
            lambda *= LAMBDA_X;
             newLoc.update(lambda);
            if (newLoc.definingObservationListChanged()) {
              convergenceCount = 0;
              lambda = LAMBDA_0;
            }
          }
          if (newLoc.getDeltaNorm() < DELTA_NORM_THRESHOLD) {
            newLoc = (LocationEstimate) oldLoc.clone();
            break;
          }
          if (lambda >= LAMBDA_0) {
            lambda /= LAMBDA_X;
          }
        }

        if (Math.abs(newLoc.getResidualSumOfSquares() / oldLoc.getResidualSumOfSquares() - 1.0) < CONVERGENCE_THRESHOLD) {
          ++convergenceCount;
        } else {
          convergenceCount = 0;
        }

      } while ((convergenceCount < CONVERGENCE_COUNT || iterationCount == 1) && (iterationCount < MAXIMUM_ITERATION_COUNT));

      newLoc.acceptLastComputedLocation();

      RealMatrix matrix = new Array2DRowRealMatrix(new double[newLoc.getDefiningObservationCount()][2]);
      matrix.setColumn(0, newLoc.getWeightedResidualArray());
      matrix.setColumn(1, newLoc.getInverseSigmaArray());

      Instant finish = Instant.now();
      logger.info(String.format("locate() execution time: %d ms", Duration.between(start, finish).toMillis()));
      return Triple.of(newLoc.getLocation(), newLoc.getSVDCovarianceMatrix(), matrix);
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("Failed to clone GeigersAlgorithm.LocationEstimate.");
    }
  }

  @Override
  public Function<RealMatrix, RowFilteredRealMatrix> getErrorValueNaNProcessor() {
    return valueErrorMatrix -> RowFilteredRealMatrix
        .includeRowsByValue(valueErrorMatrix, x -> !Double.isNaN(x), true);
  }

  @Override
  public Function<RealMatrix, RowFilteredRealMatrix> getJacobianNaNProcessor() {
    return jacobianMatrix -> RowFilteredRealMatrix
        .includeRowsByValue(jacobianMatrix, x -> !Double.isNaN(x), true);
  }

  /*
   * This class stores current location estimate.  Allows you to test out different values of lambda
   * before accepting it as a new location estimate.
   */
  private class LocationEstimate implements Cloneable {

    private RealVector location;
    private final RealMatrix OBSERVATIONS;
    private final Function<RealVector, Pair<RowFilteredRealMatrix, RowFilteredRealMatrix>> PREDICTION_FUNCTION;
    private RowFilteredRealMatrix predictions;
    private boolean definingObservationsListChanged = false;
    private BitSet lastRowExclusionBitSet = null;
    private RealVector inverseSigma;
    private RealVector weightedResiduals;
    private RealMatrix weightedAMatrix;
    private double residualSumOfSquares;
    private SVD svd;
    private double deltaNorm;
    private RealVector lastComputedLocation = null;

    LocationEstimate(final RealVector location, final RealMatrix observations, final Function<RealVector, Pair<RowFilteredRealMatrix, RowFilteredRealMatrix>> predictionFunction) {
      this.location = location.copy();
      this.OBSERVATIONS = observations.copy();
      this.PREDICTION_FUNCTION = predictionFunction;
      //newLocationUpdate();

      Pair<RowFilteredRealMatrix, RowFilteredRealMatrix> pair = PREDICTION_FUNCTION.apply(location);
      predictions = pair.getFirst();
      RealMatrix aMatrix = pair.getSecond();

      definingObservationsListChanged = !predictions.getExclusionBitSet().equals(lastRowExclusionBitSet);
      lastRowExclusionBitSet = predictions.getExclusionBitSet();

      RowFilteredRealMatrix filteredObservations = new RowFilteredRealMatrix(OBSERVATIONS, predictions.getExclusionBitSet());

      inverseSigma = Util.calculateInverseSigma(filteredObservations.getColumnVector(1), predictions.getColumnVector(1));

      weightedResiduals = filteredObservations.getColumnVector(0).subtract(predictions.getColumnVector(0));
      weightedResiduals.ebeMultiply(inverseSigma);

      DiagonalMatrix inverseSigmaMatrix = new DiagonalMatrix(inverseSigma.toArray());
      weightedAMatrix = inverseSigmaMatrix.multiply(aMatrix);

      svd = new SVD(weightedAMatrix);

      residualSumOfSquares = weightedResiduals.dotProduct(weightedResiduals);

      // check that there are more than zero degrees of freedom
      if (weightedAMatrix.getRowDimension() < weightedAMatrix.getColumnDimension()) {
        throw new IllegalStateException("Number of observations is less than number of parameters estimated.");
      }

      // check for weighted partial derivatives exceeding permitted threshold
      for (int i = 0; i < weightedAMatrix.getRowDimension(); ++i) {
        for (int j = 0; j < weightedAMatrix.getColumnDimension(); ++j) {
          if (weightedAMatrix.getEntry(i, j) > MAXIMUM_WEIGHTED_PARTIAL_DERIVATIVE) {
            throw new IllegalStateException("Weighted partial derivative matrix has values exceeding limit, " + MAXIMUM_WEIGHTED_PARTIAL_DERIVATIVE);
          }
        }
      }
    }

    private void update(final double dampening) {
      // TODO - getS() should be replaced with the modified diagonal W matrix (singularities set to zero)
      RealVector deltaLocation = calculateCartesianDelta(svd.getU(), svd.getV(), svd.getS(), weightedResiduals, dampening);
      //TODO these two adjustments are not exactly right.  need to look into radius param more

      final double NOMINAL_SEISMIC_VELOCITY = 8.0; // km/sec
      deltaNorm = Math.sqrt((CONSTRAIN_LATITUDE_PARAMETER ? 0.0 : deltaLocation.getEntry(0) * deltaLocation.getEntry(0)) +
          (CONSTRAIN_LONGITUDE_PARAMETER ? 0.0 : deltaLocation.getEntry(1) * deltaLocation.getEntry(1)) +
          (CONSTRAIN_DEPTH_PARAMETER ? 0.0 : deltaLocation.getEntry(2) * deltaLocation.getEntry(2)) +
          (CONSTRAIN_TIME_PARAMETER ? 0.0 : deltaLocation.getEntry(3) * NOMINAL_SEISMIC_VELOCITY * deltaLocation.getEntry(3) * NOMINAL_SEISMIC_VELOCITY)
      );

      // convert from (km,km,km,sec) to (rad,rad,km,sec)
      deltaLocation.setEntry(0, deltaLocation.getEntry(0) / GeoMath.RADIUS_KM);
      deltaLocation.setEntry(1, deltaLocation.getEntry(1) / GeoMath.RADIUS_KM);

      lastComputedLocation = applyDeltaToLocation(location, deltaLocation);

      Pair<RowFilteredRealMatrix, RowFilteredRealMatrix> pair = PREDICTION_FUNCTION.apply(lastComputedLocation);
      predictions = pair.getFirst();

      definingObservationsListChanged = !predictions.getExclusionBitSet().equals(lastRowExclusionBitSet);
      lastRowExclusionBitSet = predictions.getExclusionBitSet();

      RowFilteredRealMatrix filteredObservations = new RowFilteredRealMatrix(OBSERVATIONS, predictions.getExclusionBitSet());
      inverseSigma = Util.calculateInverseSigma(filteredObservations.getColumnVector(1), predictions.getColumnVector(1));
      weightedResiduals = filteredObservations.getColumnVector(0).subtract(predictions.getColumnVector(0));
      weightedResiduals.ebeMultiply(inverseSigma);

      residualSumOfSquares = weightedResiduals.dotProduct(weightedResiduals);

      // check that there are more than zero degrees of freedom
      if (weightedAMatrix.getRowDimension() < weightedAMatrix.getColumnDimension()) {
        throw new IllegalStateException(
            "Number of observations is less than number of parameters estimated.");
      }

      // check for weighted partial derivatives exceeding permitted threshold
      for (int i = 0; i < weightedAMatrix.getRowDimension(); ++i) {
        for (int j = 0; j < weightedAMatrix.getColumnDimension(); ++j) {
          if (weightedAMatrix.getEntry(i, j) > MAXIMUM_WEIGHTED_PARTIAL_DERIVATIVE) {
            throw new IllegalStateException(
                "Weighted partial derivative matrix has values exceeding limit, "
                    + MAXIMUM_WEIGHTED_PARTIAL_DERIVATIVE);
          }
        }
      }
    }

    private void acceptLastComputedLocation() {
      if (lastComputedLocation != null) {
        location = lastComputedLocation;
        lastComputedLocation = null;
      }
    }

//    private void newLocationUpdate() {
//      Pair<RowFilteredRealMatrix, RowFilteredRealMatrix> pair = PREDICTION_FUNCTION.apply(location);
//      predictions = pair.getFirst();
//      RealMatrix aMatrix = pair.getSecond();
//
//      definingObservationsListChanged = !predictions.getExclusionBitSet().equals(lastRowExclusionBitSet);
//      lastRowExclusionBitSet = predictions.getExclusionBitSet();
//
//      RowFilteredRealMatrix filteredObservations = new RowFilteredRealMatrix(OBSERVATIONS, predictions.getExclusionBitSet());
//
//      inverseSigma = Util.calculateInverseSigma(filteredObservations.getColumnVector(1), predictions.getColumnVector(1));
//
//      weightedResiduals = filteredObservations.getColumnVector(0).subtract(predictions.getColumnVector(0));
//      weightedResiduals.ebeMultiply(inverseSigma);
//
//      DiagonalMatrix inverseSigmaMatrix = new DiagonalMatrix(inverseSigma.toArray());
//      weightedAMatrix = inverseSigmaMatrix.multiply(aMatrix);
//
//      residualSumOfSquares = weightedResiduals.dotProduct(weightedResiduals);
//
//      // check that there are more than zero degrees of freedom
//      if (weightedAMatrix.getRowDimension() < weightedAMatrix.getColumnDimension()) {
//        throw new IllegalStateException(
//            "Number of observations is less than number of parameters estimated.");
//      }
//
//      // check for weighted partial derivatives exceeding permitted threshold
//      for (int i = 0; i < weightedAMatrix.getRowDimension(); ++i) {
//        for (int j = 0; j < weightedAMatrix.getColumnDimension(); ++j) {
//          if (weightedAMatrix.getEntry(i, j) > MAXIMUM_WEIGHTED_PARTIAL_DERIVATIVE) {
//            throw new IllegalStateException(
//                "Weighted partial derivative matrix has values exceeding limit, "
//                    + MAXIMUM_WEIGHTED_PARTIAL_DERIVATIVE);
//          }
//        }
//      }
//    }

//    private void computeSVD() {
//      svd = new SVD(weightedAMatrix);
//
//      // TODO check if we should be zeroing singular values here.  if so, note that svd.getSingularValues() does not return values in the order you would expect
////      double[] singularValues = svd.getSingularValues();
////      for (int i = 0; i < singularValues.length; ++i) {
////        if (singularValues[i] < singularValueThreshold) {
////          singularValues[i] = 0.0;
////        }
////      }
//
//      //TODO find out where condition number is used
//
////      double[] singularValues = svd.getSingularValues();
////      double wMin = singularValues[0];
////      double wMax = singularValues[last];
//
////      if (wMin * 1.0e30 < wMax) {
////        conditionNumber = 1.0e30;
////      } else {
////        conditionNumber = wMax/wMin;
////      }
//    }

//    private void moveLocation(final RealVector location, final double lambda) {
//      RealVector deltaLocation = calculateCartesianDelta(
//          svd.getU(), svd.getV(), svd.getS(), weightedResiduals,
//          lambda);  // make sure we are using lsq_singular_value_cutoff * wMax as gnem does
//      this.location = applyDeltaToLocation(location, deltaLocation);
//
//      //TODO these two adjustments are not exactly right.  need to look into radius param more
//      deltaLocation.setEntry(0, deltaLocation.getEntry(0) / GeoMath.RADIUS_KM);
//      deltaLocation.setEntry(1, deltaLocation.getEntry(1) / GeoMath.RADIUS_KM);
//
//      deltaNorm = Math.sqrt(deltaLocation.getEntry(0) * deltaLocation.getEntry(0) +
//          deltaLocation.getEntry(1) * deltaLocation.getEntry(1) +
//          deltaLocation.getEntry(2) * deltaLocation.getEntry(2) +
//          deltaLocation.getEntry(3) * 8 * deltaLocation.getEntry(3) * 8);
//
//      this.newLocationUpdate();
//    }

    private RealVector calculateCartesianDelta(RealMatrix svdU, RealMatrix svdV, RealMatrix adjustedSvdW, RealVector residual, double dampening) {
      RealMatrix wSquared = adjustedSvdW.power(2);
      RealMatrix lambdaW = wSquared.add(MatrixUtils.createRealIdentityMatrix(wSquared.getColumnDimension()).scalarMultiply(dampening));

      RealVector delta = svdV.multiply(MatrixUtils.inverse(lambdaW)).multiply(adjustedSvdW).multiply(svdU.transpose()).operate(residual);
      // TODO ask Jim if this is the right way to do this.  constrain the delta?  or just constrain when delta is applied to M vector.
      int i = 0;
      return new ArrayRealVector(new double[]{
          CONSTRAIN_LATITUDE_PARAMETER ? 0.0 : delta.getEntry(i++),
          CONSTRAIN_LONGITUDE_PARAMETER ? 0.0 : delta.getEntry(i++),
          CONSTRAIN_DEPTH_PARAMETER ? 0.0 : delta.getEntry(i++),
          CONSTRAIN_TIME_PARAMETER ? 0.0 : delta.getEntry(i)});
    }


//    private RealVector applyDeltaToLocation_simple(RealVector location, RealVector delta) {
//      double distance = Math.sqrt(delta.getEntry(0) * delta.getEntry(0) + delta.getEntry(1) * delta.getEntry(1));
//      double azimuth = Math.atan2(delta.getEntry(1), delta.getEntry(0));
//    }

    private RealVector applyDeltaToLocation(RealVector location, RealVector cartesianDelta) {
      double[] cartesianDeltaArray = cartesianDelta.toArray();

      double[] v = VectorUnit.move(
          EarthShape.SPHERE.getVector(
              Math.toRadians(location.getEntry(0)),
              Math.toRadians(location.getEntry(1))),
          Math.toRadians(GeoMath.DEGREES_PER_KM *
              Math.sqrt(cartesianDeltaArray[0] * cartesianDeltaArray[0]
                  + cartesianDeltaArray[1] * cartesianDeltaArray[1])),
          Math.atan2(cartesianDeltaArray[1], cartesianDeltaArray[0]));
// TODO Erik is thinking this should be atan of lat/lon, not lon/lat.  Reason this through with Jim or Austin

      return new ArrayRealVector(new double[]{
          CONSTRAIN_LATITUDE_PARAMETER ? location.getEntry(0) : EarthShape.SPHERE.getLatDegrees(v),
          CONSTRAIN_LONGITUDE_PARAMETER ? location.getEntry(1) : EarthShape.SPHERE.getLonDegrees(v),
          CONSTRAIN_DEPTH_PARAMETER ? location.getEntry(2) : location.getEntry(2) + cartesianDeltaArray[2],
          CONSTRAIN_TIME_PARAMETER ? location.getEntry(3) : location.getEntry(3) + cartesianDeltaArray[3]});
    }

    private int getDefiningObservationCount() {
      return predictions.getRowDimension();
    }

//    private boolean svdIsNull() {
//      return this.svd == null;
//    }

    private RealMatrix getSVDCovarianceMatrix() {
      return svd.getCovariance();
    }

    private RealVector getLocation() {
      return location;
    }

    private boolean definingObservationListChanged() {
      return definingObservationsListChanged;
    }

    private double getResidualSumOfSquares() {
      return residualSumOfSquares;
    }

    private double getDeltaNorm() {
      return deltaNorm;
    }

    private double[] getInverseSigmaArray() {
      return inverseSigma.toArray();
    }

    private double[] getWeightedResidualArray() {
      return weightedResiduals.toArray();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
      LocationEstimate locationEstimate = (LocationEstimate) super.clone();
      locationEstimate.location = location.copy();
      locationEstimate.predictions = new RowFilteredRealMatrix(predictions.getInnerMatrix(),
          predictions.getExclusionBitSet());
      locationEstimate.lastRowExclusionBitSet = (BitSet) lastRowExclusionBitSet.clone();
      locationEstimate.inverseSigma = inverseSigma.copy();
      locationEstimate.weightedResiduals = weightedResiduals.copy();
      locationEstimate.weightedAMatrix = weightedAMatrix.copy();
      if (svd != null) {
        locationEstimate.svd = (SVD) svd.clone();
      }
      return locationEstimate;
    }
  }


  private class SVD implements Cloneable {

    private SingularValueDecomposition svd;
    private RealMatrix U;
    private RealMatrix V;
    private RealMatrix S;
    private RealMatrix covariance;

    public SVD(RealMatrix m) {
      svd = new SingularValueDecomposition(m);
      U = svd.getU();
      V = svd.getV();
      S = svd.getS();
      covariance = svd.getCovariance(-1.0);
    }

    public RealMatrix getU() {
      return U;
    }

    public RealMatrix getV() {
      return V;
    }

    public RealMatrix getS() {
      return S;
    }

    public RealMatrix getCovariance() {
      return covariance;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
      SVD clone = (SVD) super.clone();
      clone.U = U.copy();
      clone.V = V.copy();
      clone.S = S.copy();
      clone.covariance = covariance.copy();
      return clone;
    }
  }


  /**
   * A mutable builder for a {@link GeigersAlgorithm}.  The builder has two phases. At inception, it
   * is in the build phase in which it can be modified.  Once the build() method is called, the
   * {@link Builder} transitions to the built phase, to create the {@link GeigersAlgorithm}.  Once
   * the build() method is called, the {@link Builder} can no longer be used.
   */
  public static final class Builder implements LocatorAlgorithm.Builder<RowFilteredRealMatrix> {
    private int maximumIterationCount;
    private double convergenceThreshold;
    private int convergenceCount;
    private boolean levenbergMarquardtEnabled;
    private double lambda0;
    private double lambdaX;
    private double deltaNormThreshold;
    private double singularValueWFactor;
    private double maximumWeightedPartialDerivative;
    private boolean latitudeParameterConstrainedToSeededValue;
    private boolean longitudeParameterConstrainedToSeededValue;
    private boolean depthParameterConstrainedToSeededValue;
    private boolean timeParameterConstrainedToSeededValue;

    /**
     * Sets the maximum number of iterations permitted in {@link GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    public Builder withMaximumIterationCount(final int maximumIterationCount) {
      this.maximumIterationCount = maximumIterationCount;
      return this;
    }

    /**
     * Sets the maximum value to satisfy the convergence criterion in {@link GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    public Builder withConvergenceThreshold(final double convergenceThreshold) {
      this.convergenceThreshold = convergenceThreshold;
      return this;
    }

    /**
     * Sets the minimum number of iterations that the convergence criterion must be satisfied before
     * terminating the algorithm {@link GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    public Builder withConvergenceCount(final int convergenceCount) {
      this.convergenceCount = convergenceCount;
      return this;
    }

    /**
     * Set to {@code true} if Levenberg-Marquardt dampening is to be employed in {@link
     * GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    public Builder withLevenbergMarquardtEnabled(final boolean levenbergMarquadtEnabled) {
      this.levenbergMarquardtEnabled = levenbergMarquadtEnabled;
      return this;
    }

    /**
     * Sets the initial value of the Levenberg-Marquardt dampening factor in {@link
     * GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    public Builder withLambda0(final double lambda0) {
      this.lambda0 = lambda0;
      return this;
    }

    /**
     * Sets the factor by which the Levenberg-Marquardt dampening factor is adjusted in {@link
     * GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    public Builder withLambdaX(final double lambdaX) {
      this.lambdaX = lambdaX;
      return this;
    }

    /**
     * Sets the value of the norm of delta-m, below which the Levenberg-Marquardt dampening
     * adjustment loop is terminated in {@link GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    public Builder withDeltaNormThreshold(final double deltaNormThreshold) {
      this.deltaNormThreshold = deltaNormThreshold;
      return this;
    }

    /**
     * Sets the value when multiplied by the max SVD W-matrix value determines the value below which
     * W-matrix values are ignored in {@link GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    public Builder withSingularValueWFactor(final double singularValueWFactor) {
      this.singularValueWFactor = singularValueWFactor;
      return this;
    }

    /**
     * Sets the maximum value of a weighted partial derivative before an exception is thrown in
     * {@link GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    public Builder withMaximumWeightedPartialDerivative(
        final double maximumWeightedPartialDerivative) {
      this.maximumWeightedPartialDerivative = maximumWeightedPartialDerivative;
      return this;
    }

    /**
     * Set to {@code true} if latitude parameter is to be constrained to the seeded value in {@link
     * GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    @Override
    public Builder withLatitudeParameterConstrainedToSeededValue(
        final boolean latitudeParameterConstrainedToSeededValue) {
      this.latitudeParameterConstrainedToSeededValue = latitudeParameterConstrainedToSeededValue;
      return this;
    }

    /**
     * Set to {@code true} if longitude parameter is to be constrained to the seeded value in {@link
     * GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    @Override
    public Builder withLongitudeParameterConstrainedToSeededValue(
        final boolean longitudeParameterConstrainedToSeededValue) {
      this.longitudeParameterConstrainedToSeededValue = longitudeParameterConstrainedToSeededValue;
      return this;
    }

    /**
     * Set to {@code true} if depth parameter is to be constrained to the seeded value in {@link
     * GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    @Override
    public Builder withDepthParameterConstrainedToSeededValue(
        final boolean depthParameterConstrainedToSeededValue) {
      this.depthParameterConstrainedToSeededValue = depthParameterConstrainedToSeededValue;
      return this;
    }

    /**
     * Set to {@code true} if time parameter is to be constrained to the seeded value in {@link
     * GeigersAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    @Override
    public Builder withTimeParameterConstrainedToSeededValue(
        final boolean timeParameterConstrainedToSeededValue) {
      this.timeParameterConstrainedToSeededValue = timeParameterConstrainedToSeededValue;
      return this;
    }

    /**
     * Builds the {@link GeigersAlgorithm} from the parameters defined during the build phase.
     *
     * @return a new {@link GeigersAlgorithm}
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * GeigersAlgorithm}
     */
    @Override
    public GeigersAlgorithm build() throws TooManyRestraintsException {
      if (latitudeParameterConstrainedToSeededValue
          && longitudeParameterConstrainedToSeededValue
          && depthParameterConstrainedToSeededValue
          && timeParameterConstrainedToSeededValue) {
        throw new TooManyRestraintsException(4);
      }


      Validate.isTrue(1 < maximumIterationCount,
          "GeigersAlgorithm must execute more than one iteration");

      return new GeigersAlgorithm(
          maximumIterationCount,
          convergenceThreshold,
          convergenceCount,
          levenbergMarquardtEnabled,
          lambda0,
          lambdaX,
          deltaNormThreshold,
          singularValueWFactor,
          maximumWeightedPartialDerivative,
          latitudeParameterConstrainedToSeededValue,
          longitudeParameterConstrainedToSeededValue,
          depthParameterConstrainedToSeededValue,
          timeParameterConstrainedToSeededValue);
    }
  }
}
