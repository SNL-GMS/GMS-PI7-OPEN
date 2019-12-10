package gms.core.eventlocation.plugins.pluginutils;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionDerivativeType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.utilities.geomath.EarthShape;
import gms.shared.utilities.geomath.FilteredRealVector;
import gms.shared.utilities.geomath.GeoMath;
import gms.shared.utilities.geomath.VectorUnit;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class Util {

  //TODO: make configurable or pass in?
  private final static EarthShape earthShape = EarthShape.SPHERE;

  //TODO: consolidate this with StatUtil
  private final static double EPS = Math.sqrt(Math.ulp(0.5));

  private final static double initialDamping = 0.001;
  private final static double dampingIncreaseFactor = 10.0;

  /**
   * Calculate the diagonal elements of the inverse diagonal matrix of combined errors:
   * data/observed and model/predicted errors
   *
   * @param observedErrors vector of errors (stddev) associated with observed data
   * @param predictedErrors vector of errors (stddev) associated with predicted model
   * @return diagonal elements of the inverse of the diagonal matrix of combined errors
   */
  public static RealVector calculateInverseSigma(final RealVector observedErrors,
      final RealVector predictedErrors) {
    RealVector obs = observedErrors.copy();
    RealVector prd = predictedErrors.copy();
    obs.ebeMultiply(obs);
    prd.ebeMultiply(prd);
    RealVector sum = obs.add(prd);
    sum.mapToSelf((d) -> 1.0 / Math.sqrt(d));
    return sum;
  }

  public static FilteredRealVector calculateInverseSigma(final FilteredRealVector observedErrors,
      final FilteredRealVector predictedErrors) {
    FilteredRealVector obs = observedErrors.copyFilteredRealVector();
    FilteredRealVector prd = predictedErrors.copyFilteredRealVector();
    obs.ebeMultiply(obs);
    prd.ebeMultiply(prd);
    FilteredRealVector sum = obs.add(prd);
    sum.mapToSelf((d) -> 1.0 / Math.sqrt(d));
    return sum;
  }

  /**
   * Calculate the vector of weighted residuals
   *
   * @param weights diagonal matrix of weights
   * @param observations vector of observed arrival times, event-to-station azimuths, and
   * slownesses
   * @param predictions vector of predicted arrival times, event-to-station azimuths, and
   * slownesses
   * @return vector of weighted residual values (i.e., error values)
   */
  public static RealVector calculateWeightedResiduals(RealMatrix weights, RealVector observations,
      RealVector predictions) {
    // returning weights^(-1) * (observations - predictions)
    return MatrixUtils.inverse(weights).operate(observations.subtract(predictions));
  }


  /**
   *
   */
  public static RealVector moveEvent(RealVector location, RealVector cartesianDelta) {
    double[] cartesianDeltaArray = cartesianDelta.toArray();

    double[] v = VectorUnit.move(
        earthShape.getVector(
            Math.toRadians(location.getEntry(0)),
            Math.toRadians(location.getEntry(1))),
        Math.toRadians(GeoMath.DEGREES_PER_KM *
            Math.sqrt(cartesianDeltaArray[0] * cartesianDeltaArray[0]
                + cartesianDeltaArray[1] * cartesianDeltaArray[1])),
        Math.atan2(cartesianDeltaArray[1], cartesianDeltaArray[0]));
// TODO Erik is thinking this should be atan of lat/lon, not lon/lat.  Reason this through with Jim or Austin

    return new ArrayRealVector(new double[]{
        earthShape.getLatDegrees(v),
        earthShape.getLonDegrees(v),
        location.getEntry(2) + cartesianDeltaArray[2],
        location.getEntry(3) + cartesianDeltaArray[3]});
  }


  public static double calculateDamping(RealVector currentResidual, RealVector nextResidual,
      double currentDamping) {

    if (nextResidual.getNorm() >= currentResidual.getNorm()) {
      if (currentDamping == 0) {
        return initialDamping;
      }
      return currentDamping * dampingIncreaseFactor;
    } else {
      return currentDamping / dampingIncreaseFactor;
    }
  }

  /**
   * Calculate delta-m (cartesian delta) given a residual vector, damping value, and SVD components
   * of the A derivative matrix where the signular value matrix has had "problematic" signular
   * values zeroed out.
   *
   * @param svdU U matrix of SVD decomposition
   * @param svdV V matrix of SVD decomposition
   * @param adjustedSvdW "adjusted" W matrix of SVD decomposition
   * @param residual residual vector
   * @param damping damping factor
   * @return solution to the equation: (A-tranpose * A + lambda * I) * delta-m = A-transopose * r
   */
  public static RealVector calculateCartesianDelta(RealMatrix svdU, RealMatrix svdV,
      RealMatrix adjustedSvdW, RealVector residual, double damping) {

    RealMatrix wSquared = adjustedSvdW.power(2);
    RealMatrix lambdaW = wSquared.add(MatrixUtils.createRealIdentityMatrix(wSquared
        .getColumnDimension()).scalarMultiply(damping));

    return svdV.multiply(MatrixUtils.inverse(lambdaW)).multiply(adjustedSvdW).multiply(
        svdU.transpose()).operate(residual);
  }

  static RealMatrix setSingularValuesZero(RealMatrix matrix, double tolerance) {

    Objects.requireNonNull(matrix, "Null matrix");

    if (matrix.getColumnDimension() != matrix.getRowDimension()) {
      throw new IllegalArgumentException("Cannot execute method on non-square matrix");
    }

    final int diagonalLength = matrix.getColumnDimension();
    double[] diagonal = new double[diagonalLength];

    for (int i = 0; i < diagonalLength; i++) {
      diagonal[i] = matrix.getEntry(i, i);
    }

    double condition;

    Triple<Double, Double, Integer> result = Util.findMaxMinAndMinIndex(diagonal);

    double sMax = result.getLeft();
    double sMin = result.getMiddle();
    int sMinIndex = result.getRight();

    condition = sMax / sMin;

    while (Double.compare(condition, tolerance) > 0) {

      diagonal[sMinIndex] = 0;

      result = Util.findMaxMinAndMinIndex(diagonal);

      sMax = result.getLeft();
      sMin = result.getMiddle();
      sMinIndex = result.getRight();

      condition = sMax / sMin;
    }

    double[][] newMatrix = new double[diagonalLength][diagonalLength];

    for (int i = 0; i < diagonalLength; i++) {

      newMatrix[i][i] = diagonal[i];
    }

    return new Array2DRowRealMatrix(newMatrix);
  }

  // returns < max, min, minIndex >
  private static Triple<Double, Double, Integer> findMaxMinAndMinIndex(double[] diagonal) {

    double sMax = Double.MIN_VALUE;

    double sMin = Double.MAX_VALUE;
    int sMinIndex = -1;

    for (int i = 0; i < diagonal.length; i++) {

      if (diagonal[i] < sMin && Double.compare(0.0, diagonal[i]) < 0) {

        sMin = diagonal[i];
        sMinIndex = i;
      }

      if (diagonal[i] > sMax) {

        sMax = diagonal[i];
      }
    }

    if (sMinIndex == -1) {
      throw new IllegalStateException("Could not find sMin - index is still -1");
    }

    return Triple.of(sMax, sMin, sMinIndex);
  }

  /**
   * Returns an {@code OptionalDouble} wrapping the value of a {@code FeaturmMeasurement}. Currently
   * this only supports the feature measure types arrival time, station to event azimust, and
   * slowness, since these are the only ones relevant to the event location algorithm.
   *
   * @return an OptionalDouble wrapping the value, if one of the supported types.
   */
  public static OptionalDouble getFeatureMeasurementValue(
      final FeatureMeasurement<?> featureMeasurement) {
    FeatureMeasurementType<?> featureMeasurementType =
        featureMeasurement.getFeatureMeasurementType();
    if (featureMeasurementType.equals(FeatureMeasurementTypes.ARRIVAL_TIME)) {
      // Convert instant to the number of seconds since the beginning of the epoch.
      InstantValue instantValue = (InstantValue) featureMeasurement.getMeasurementValue();
      Instant instant = instantValue.getValue();
      return OptionalDouble.of(((double) instant.getEpochSecond()) +
          ((double) instant.getNano()) / 1.0e9);
    } else if (featureMeasurementType.equals(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH)
        || featureMeasurementType.equals(FeatureMeasurementTypes.SLOWNESS)) {
      NumericMeasurementValue numericMeasurementValue =
          (NumericMeasurementValue) featureMeasurement.getMeasurementValue();
      DoubleValue doubleValue = numericMeasurementValue.getMeasurementValue();
      return OptionalDouble.of(doubleValue.getValue());
    }
    return OptionalDouble.empty();
  }

  public static double computeDeltaMNorm(RealVector cartesianDeltaM) {
    final double VELOCITY = 8.0; // estimate of the seismic velocity of the medium near the hypocenter
    double deltaLatKm = GeoMath.degToKm(cartesianDeltaM.getEntry(0));
    double deltaLonKm = GeoMath.degToKm(cartesianDeltaM.getEntry(1));
    return Math.sqrt(deltaLatKm * deltaLatKm
        + deltaLonKm * deltaLonKm * earthShape.equatorialRadius * earthShape.equatorialRadius +
        cartesianDeltaM.getEntry(2) * cartesianDeltaM.getEntry(2) +
        cartesianDeltaM.getEntry(3) * cartesianDeltaM.getEntry(3) * VELOCITY * VELOCITY);
  }

  public static double[] compute2dEllipse(RealMatrix covarianceMatrix,
      int rowOfInterest, int columnOfInterest) {
    Validate.isTrue(MatrixUtils.isSymmetric(covarianceMatrix, EPS));

    double c11 = covarianceMatrix.getEntry(rowOfInterest, rowOfInterest);
    double c22 = covarianceMatrix.getEntry(columnOfInterest, columnOfInterest);
    double c12 = covarianceMatrix.getEntry(rowOfInterest, columnOfInterest);

    double theta;
    if (c12 == 0.0) {
      theta = 0;
    } else {
      double e = (c11 - c22) / (2.0 * c12);
      theta = Math.atan(-e + Math.sqrt(e * e + 1.0));
    }
    double cosTheta = Math.cos(theta);
    double sinTheta = Math.sin(theta);

    double cosSqTheta = cosTheta * cosTheta;
    double sinSqTheta = sinTheta * sinTheta;

    double middleTerm = 2.0 * c12 * cosTheta * sinTheta;

    // Compute the lengths of the major and minor axes.
    double axis1 = 1.0/Math.sqrt(c11 * cosSqTheta + middleTerm + c22 * sinSqTheta);
    double axis2 = 1.0/Math.sqrt(c11 * sinSqTheta - middleTerm + c22 * cosSqTheta);

    double semiMajorAxisLength;
    double semiMinorAxisLength;
    double semiMajorAxisTrend;
    double semiMinorAxisTrend;
    double thetaDegrees = Math.toDegrees(theta);

    // According to the SAND Report, the formula for axis1 is the semi major axis length. But
    // we've seen cases where the semi major axis length < semi minor axis length.
    if (axis1 > axis2) {
      semiMajorAxisLength = axis1;
      semiMinorAxisLength = axis2;
      semiMajorAxisTrend = thetaDegrees;
      semiMinorAxisTrend = thetaDegrees + 90.0;
    } else {
      semiMajorAxisLength = axis2;
      semiMinorAxisLength = axis1;
      semiMajorAxisTrend = thetaDegrees + 90.0;
      semiMinorAxisTrend = thetaDegrees;
    }

    // Order major axis length, minor axis length, major axis trend, minor axis trend
    return new double[] {
        semiMajorAxisLength,
        semiMinorAxisLength,
        semiMajorAxisTrend,
        semiMinorAxisTrend
    };
  }
}
