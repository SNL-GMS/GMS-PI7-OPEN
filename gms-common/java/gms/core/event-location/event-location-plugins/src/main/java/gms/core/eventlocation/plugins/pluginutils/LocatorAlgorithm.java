package gms.core.eventlocation.plugins.pluginutils;

import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

/**
 * Rperesents any algorithm that locates via a seed, observations, observations errors, and closure
 * that generates predictions in the form of basic math objects (vectors and matrices)
 *
 * @param <T> The type returned by the prediction generator. The type is restricted by
 * getErrorValueNaNProcessor and getJacobianNaNProcessor
 */
public interface LocatorAlgorithm<T extends RealMatrix> {

  /**
   * Perform location givin an initial location seed, a set of observations, and a closure
   * that returns prediction and error values and a jacobian
   *
   * @param mSeed Initial guess at location
   * @param observations Initial set of observations (first column) with their errors (second column)
   * @param predictionFunction Closure which given a location vector returns a set of predictions,
   * represented by pair of matrices - the first matrix has a column vector of values and a column
   * vector of erros; the second is the calculated Jacobian matrix
   * @return A triple containing three values:
   * <ol>
   *   <li>The final location estimate</li>
   *   <li>Covariance matrix</li>
   *   <li>Matrix containing a column for residuals and column for residual weights</li>
   * </ol>
   */
  Triple<RealVector, RealMatrix, RealMatrix> locate(
      final RealVector mSeed,
      final RealMatrix observations,
      final Function<RealVector, Pair<T, T>> predictionFunction);

  /**
   * Closure which takes a RealMatrix of type T and returns any RealMatrix where NaNs inside
   * the error/value matrix have been processed.
   *
   * @return NaN-processed matrix of value/errors
   */
  Function<RealMatrix, T> getErrorValueNaNProcessor();

  /**
   * Closure which takes a RealMatrix of type T and returns any RealMatrix where NaNs inside
   * the Jacobian matrix have been processed.
   *
   * @return NaN-processed Jacobian
   */
  Function<RealMatrix, T> getJacobianNaNProcessor();

  interface Builder<T extends RealMatrix> {
    LocatorAlgorithm<T> build() throws TooManyRestraintsException;

    default Builder<T> withLatitudeParameterConstrainedToSeededValue(
        final boolean latitudeParameterConstrainedToSeededValue) {
      return this;
    }

    default Builder<T> withLongitudeParameterConstrainedToSeededValue(
        final boolean longitudeParameterConstrainedToSeededValue) {
      return this;
    }

    default Builder<T> withDepthParameterConstrainedToSeededValue(
        final boolean depthParameterConstrainedToSeededValue) {
      return this;
    }

    default Builder<T> withTimeParameterConstrainedToSeededValue(
        final boolean timeParameterConstrainedToSeededValue) {
      return this;
    }
  }
}
