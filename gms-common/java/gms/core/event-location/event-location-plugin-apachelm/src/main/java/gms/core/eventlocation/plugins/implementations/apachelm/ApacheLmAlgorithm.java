package gms.core.eventlocation.plugins.implementations.apachelm;

import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import gms.core.eventlocation.plugins.pluginutils.LocatorAlgorithm;
import gms.core.eventlocation.plugins.pluginutils.Util;
import gms.shared.utilities.geomath.FilteredRealVector;
import java.time.Duration;
import java.time.Instant;
import java.util.BitSet;
import java.util.function.Function;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresFactory;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.util.Incrementor;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import org.apache.commons.lang3.tuple.Pair;


public class ApacheLmAlgorithm implements LocatorAlgorithm<RealMatrix> {

  private final int maximumIterationCount;
  private final double residualConvergenceThreshold;
  private boolean latitudeParameterConstrainedToSeededValue;
  private boolean longitudeParameterConstrainedToSeededValue;
  private boolean depthParameterConstrainedToSeededValue;
  private boolean timeParameterConstrainedToSeededValue;
  private static Logger logger = LogManager.getLogger(ApacheLmAlgorithm.class);


  private static RealMatrixChangingVisitor VALUE_JACOBIAN_NAN_VISITOR = new RealMatrixChangingVisitor() {
    @Override
    public void start(int rows, int columns, int startRow, int endRow, int startColumn,
        int endColumn) {

    }

    @Override
    public double visit(int row, int column, double value) {
      return Double.isNaN(value) ? 0 : value;
    }

    @Override
    public double end() {
      return 0;
    }
  };

  public ApacheLmAlgorithm(int maximumIterationCount, double residualConvergenceThreshold,
      boolean latitudeParameterConstrainedToSeededValue,
      boolean longitudeParameterConstrainedToSeededValue,
      boolean depthParameterConstrainedToSeededValue,
      boolean timeParameterConstrainedToSeededValue) {
    this.maximumIterationCount = maximumIterationCount;
    this.residualConvergenceThreshold = residualConvergenceThreshold;
    this.latitudeParameterConstrainedToSeededValue = latitudeParameterConstrainedToSeededValue;
    this.longitudeParameterConstrainedToSeededValue = longitudeParameterConstrainedToSeededValue;
    this.depthParameterConstrainedToSeededValue = depthParameterConstrainedToSeededValue;
    this.timeParameterConstrainedToSeededValue = timeParameterConstrainedToSeededValue;
  }

  @Override
  public Triple<RealVector, RealMatrix, RealMatrix> locate(
      final RealVector seedWithRestrainedValues,
      final RealMatrix finalObservations,
      final Function<RealVector, Pair<RealMatrix, RealMatrix>> predictionFunction) {

    class FinalErrorValuesWrapper {

      RealMatrix observations;
      RealMatrix predictions;

      void setErrorsAndValues(RealMatrix observationErrors, RealMatrix predictionErrors) {
        this.observations = observationErrors;
        this.predictions = predictionErrors;
      }
    }

    Instant start = Instant.now();

    final FinalErrorValuesWrapper errorValuesWrapper = new FinalErrorValuesWrapper();

    FilteredRealVector mSeed = getRestrainedSeed(seedWithRestrainedValues);

    LeastSquaresProblem problem = LeastSquaresFactory.create(
        point -> {

          Pair<RealMatrix, RealMatrix> valuesWithErrors = predictionFunction.apply(
              fixPointRestrainedValues(point, mSeed));

          errorValuesWrapper.setErrorsAndValues(finalObservations, valuesWithErrors.getKey());

          return Pair
              .create(valuesWithErrors.getKey().getColumnVector(0), valuesWithErrors.getValue());
        },
        finalObservations.getColumnVector(0),
        mSeed,
        (iteration, previous, current) -> {
          double residualRatio = Math.abs(previous.getRMS() / current.getRMS() - 1.0);

          return residualRatio < residualConvergenceThreshold
              || iteration >= maximumIterationCount;
        },
        10000,
        maximumIterationCount);

    LeastSquaresProblem dynamicProblem = new LeastSquaresProblem() {
      @Override
      public RealVector getStart() {
        return problem.getStart();
      }

      @Override
      public int getObservationSize() {
        return problem.getObservationSize();
      }

      @Override
      public int getParameterSize() {
        return problem.getParameterSize();
      }

      @Override
      public Evaluation evaluate(RealVector point) {
        Evaluation preliminaryEvaluation = problem.evaluate(point);

        return new Evaluation() {
          private RealMatrix observations = errorValuesWrapper.observations;
          private RealMatrix predictions = errorValuesWrapper.predictions;

          @Override
          public RealMatrix getCovariances(double threshold) {
            return preliminaryEvaluation.getCovariances(threshold);
          }

          @Override
          public RealVector getSigma(double covarianceSingularityThreshold) {
            return preliminaryEvaluation.getSigma(covarianceSingularityThreshold);
          }

          @Override
          public double getRMS() {
            RealVector residuals = getResiduals();

            return Math.sqrt(residuals.dotProduct(residuals)) / residuals.getDimension();
          }

          @Override
          public RealMatrix getJacobian() {
            return preliminaryEvaluation.getJacobian();
          }

          @Override
          public double getCost() {
            RealVector residuals = getResiduals();

            return residuals.dotProduct(residuals);
          }

          @Override
          public RealVector getResiduals() {
            RealVector inverseSigma = Util.calculateInverseSigma(observations.getColumnVector(1),
                predictions.getColumnVector(1));

            // compute next residual vector, rCurr.  i.e., r_(i+1)
            RealVector rNext = observations.getColumnVector(0)
                .subtract(predictions.getColumnVector(0));

            rNext.ebeMultiply(inverseSigma);

            return rNext;
          }

          @Override
          public RealVector getPoint() {
            return preliminaryEvaluation.getPoint();
          }
        };

      }

      @Override
      public Incrementor getEvaluationCounter() {
        return problem.getEvaluationCounter();
      }

      @Override
      public Incrementor getIterationCounter() {
        return problem.getIterationCounter();
      }

      @Override
      public ConvergenceChecker<Evaluation> getConvergenceChecker() {
        return problem.getConvergenceChecker();
      }
    };

    LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer()
        .withOrthoTolerance(0)
        //.withCostRelativeTolerance(00000)
        //.withInitialStepBoundFactor(0.01)
        ;

    Optimum optimum = optimizer.optimize(dynamicProblem);

    RealMatrix residualWeightMatrix = new Array2DRowRealMatrix(
        optimum.getResiduals().getDimension(), 2);
    residualWeightMatrix.setColumnVector(0, optimum.getResiduals());
    residualWeightMatrix.setColumnVector(1,
        Util.calculateInverseSigma(errorValuesWrapper.observations.getColumnVector(1),
            errorValuesWrapper.predictions.getColumnVector(1)));

    Instant finish = Instant.now();
    logger.info(String.format("locate() execution time: %d ms", Duration.between(start, finish).toMillis()));
    return Triple
        .of(fixPointRestrainedValues(optimum.getPoint(), mSeed), optimum.getCovariances(0.0),
            residualWeightMatrix);
  }

  @Override
  public Function<RealMatrix, RealMatrix> getErrorValueNaNProcessor() {
    return valueErrorMatrix -> {
      valueErrorMatrix.walkInOptimizedOrder(VALUE_JACOBIAN_NAN_VISITOR);
      return valueErrorMatrix;
    };
  }

  @Override
  public Function<RealMatrix, RealMatrix> getJacobianNaNProcessor() {
    return jacobianMatrix -> {
      jacobianMatrix.walkInOptimizedOrder(VALUE_JACOBIAN_NAN_VISITOR);
      return jacobianMatrix;
    };
  }



  private FilteredRealVector getRestrainedSeed(RealVector seed) {
    return new FilteredRealVector(seed, new BitSet(4) {{
      set(0, latitudeParameterConstrainedToSeededValue);
      set(1, longitudeParameterConstrainedToSeededValue);
      set(2, depthParameterConstrainedToSeededValue);
      set(3, timeParameterConstrainedToSeededValue);
    }});
  }

  private static RealVector fixPointRestrainedValues(RealVector point,
      FilteredRealVector restrainedSeed) {
    BitSet bits = (BitSet) restrainedSeed.getInclusionBits().clone();

    int[] includedIndices = bits.stream().toArray();
    bits.flip(0, 4);
    int[] excludedIndices = bits.stream().toArray();

    RealVector newVector = new ArrayRealVector(4);

    for (int i = 0; i < includedIndices.length; i++) {
      newVector.setEntry(includedIndices[i], point.getEntry(i));
    }

    for (int i = 0; i < excludedIndices.length; i++) {
      newVector.setEntry(excludedIndices[i],
          restrainedSeed.getInnerVector().getEntry(excludedIndices[i]));
    }

    return newVector;
  }

  /**
   * A mutable builder for a {@link ApacheLmAlgorithm}.  The builder has two phases. At inception,
   * it is in the build phase in which it can be modified.  Once the build() method is called, the
   * {@link Builder} transitions to the built phase, to create the {@link ApacheLmAlgorithm}.  Once
   * the build() method is called, the {@link Builder} can no longer be used.
   */
  public static final class Builder implements LocatorAlgorithm.Builder<RealMatrix> {
    private int maximumIterationCount;
    private double residualConvergenceThreshold; // = 0.001;
    private boolean latitudeParameterConstrainedToSeededValue;
    private boolean longitudeParameterConstrainedToSeededValue;
    private boolean depthParameterConstrainedToSeededValue;
    private boolean timeParameterConstrainedToSeededValue;


    /**
     * Sets the maximum number of iterations permitted in {@link ApacheLmAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * ApacheLmAlgorithm}
     */
    public Builder withMaximumIterationCount(int maximumIterationCount) {
      this.maximumIterationCount = maximumIterationCount;
      return this;
    }

    /**
     * Sets the maximum number of iterations permitted in {@link ApacheLmAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * ApacheLmAlgorithm}
     */
    public Builder withResidualConvergenceThreshold(double residualConvergenceThreshold) {
      this.residualConvergenceThreshold = residualConvergenceThreshold;
      return this;
    }

    /**
     * Set to {@code true} if latitude parameter is to be constrained to the seeded value in {@link
     * ApacheLmAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * ApacheLmAlgorithm}
     */
    @Override
    public Builder withLatitudeParameterConstrainedToSeededValue(
        final boolean latitudeParameterConstrainedToSeededValue) {
      this.latitudeParameterConstrainedToSeededValue = latitudeParameterConstrainedToSeededValue;
      return this;
    }

    /**
     * Set to {@code true} if longitude parameter is to be constrained to the seeded value in {@link
     * ApacheLmAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * ApacheLmAlgorithm}
     */
    @Override
    public Builder withLongitudeParameterConstrainedToSeededValue(
        final boolean longitudeParameterConstrainedToSeededValue) {
      this.longitudeParameterConstrainedToSeededValue = longitudeParameterConstrainedToSeededValue;
      return this;
    }

    /**
     * Set to {@code true} if depth parameter is to be constrained to the seeded value in {@link
     * ApacheLmAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * ApacheLmAlgorithm}
     */
    @Override
    public Builder withDepthParameterConstrainedToSeededValue(
        final boolean depthParameterConstrainedToSeededValue) {
      this.depthParameterConstrainedToSeededValue = depthParameterConstrainedToSeededValue;
      return this;
    }

    /**
     * Set to {@code true} if time parameter is to be constrained to the seeded value in {@link
     * ApacheLmAlgorithm}
     *
     * @return this builder
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * ApacheLmAlgorithm}
     */
    @Override
    public Builder withTimeParameterConstrainedToSeededValue(
        final boolean timeParameterConstrainedToSeededValue) {
      this.timeParameterConstrainedToSeededValue = timeParameterConstrainedToSeededValue;
      return this;
    }

    /**
     * Builds the {@link ApacheLmAlgorithm} from the parameters defined during the build phase.
     *
     * @return a new {@link ApacheLmAlgorithm}
     * @throws IllegalStateException if the Builder has already been used to create a {@link
     * ApacheLmAlgorithm}
     */
    @Override
    public ApacheLmAlgorithm build() throws TooManyRestraintsException {
      if (latitudeParameterConstrainedToSeededValue
          && longitudeParameterConstrainedToSeededValue
          && depthParameterConstrainedToSeededValue
          && timeParameterConstrainedToSeededValue) {
        throw new TooManyRestraintsException(4);
      }

      Validate.isTrue(1 < maximumIterationCount,
          "ApacheLmAlgorithm must execute more than one iteration");

      return new ApacheLmAlgorithm(maximumIterationCount, residualConvergenceThreshold,
          latitudeParameterConstrainedToSeededValue, longitudeParameterConstrainedToSeededValue,
          depthParameterConstrainedToSeededValue, timeParameterConstrainedToSeededValue);
    }
  }

}
