package gms.core.eventlocation.plugins.pluginutils;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.utilities.geomath.FilteredRealVector;
import java.time.Duration;
import java.time.Instant;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UtilTests {

  // [2.4327666748312735, 120.09582, 2.901724160278718, 1.29464852388681E9]
  private static final RealVector eventToMove = new ArrayRealVector(new double[]{
      2.4327666748312735, 120.09582, 2.901724160278718, 1294648523.886810000});

  private static final RealVector moveByVector = new ArrayRealVector(new double[]{
      4.011839368439597, -4.033131664449292, -2.32, 0.01676670245145531
  });

  @Test
  public void testCalculateInverseSigma() {
    RealVector combined = new ArrayRealVector(new double[]{0.2, 0.2, 0.2, 0.2});
    FilteredRealVector dataErrs = new FilteredRealVector(
        new ArrayRealVector(new double[]{3.0, 3.0, 3.0, 3.0}));
    FilteredRealVector modelErrs = new FilteredRealVector(
        new ArrayRealVector(new double[]{4.0, 4.0, 4.0, 4.0}));
    FilteredRealVector result = Util.calculateInverseSigma(dataErrs, modelErrs);
    Assertions.assertEquals(combined, result);
  }

  @Test
  public void testWeightedResiduals() {
    RealMatrix weights = new Array2DRowRealMatrix(new double[][]{
        {5.0, 0.0, 0.0, 0.0, 0.0},
        {0.0, 4.0, 0.0, 0.0, 0.0},
        {0.0, 0.0, 3.0, 0.0, 0.0},
        {0.0, 0.0, 0.0, 2.0, 0.0},
        {0.0, 0.0, 0.0, 0.0, 1.0}
    });
    RealVector observations = new ArrayRealVector(new double[]{6.0, 5.0, 4.0, 3.0, 2.0});
    RealVector predictions = new ArrayRealVector(new double[]{1.0, 1.0, 1.0, 1.0, 1.0});
    RealVector result = Util.calculateWeightedResiduals(weights, observations, predictions);
    Assertions.assertEquals(predictions, result);
  }

  @Test
  public void testMoveEvent() {
    final double MAX_ERROR = 10E-5;

    RealVector newLocation = Util.moveEvent(eventToMove, moveByVector);

    //[2.4688619625592523, 120.05949893661469, 0.5817241602790091, 1.2946485239035769E9]

    Assertions.assertEquals(2.4688619625592523, newLocation.getEntry(0), MAX_ERROR);
    Assertions.assertEquals(120.05949893661469, newLocation.getEntry(1), MAX_ERROR);
    Assertions.assertEquals(0.5817241602790091, newLocation.getEntry(2), MAX_ERROR);
    Assertions.assertEquals(1.2946485239035769E9, newLocation.getEntry(3), MAX_ERROR);
  }

  @Test
  public void testCalculateDamping() {
    RealVector current = new ArrayRealVector(new double[]{
        1.0, 1.0, 1.0, 1.0, 1.0, 1.0
    });

    RealVector next = new ArrayRealVector(new double[]{
        1.0, 1.0, 10.0, 1.0, 1.0, 1.0
    });

    double damping = Util.calculateDamping(current, next, 0.0);

    Assertions.assertEquals(0.001, damping);

    damping = Util.calculateDamping(current, next, damping);

    Assertions.assertEquals(0.01, damping);

    next = new ArrayRealVector(new double[]{
        1.0, 1.0, 0.0, 1.0, 1.0, 1.0
    });

    damping = Util.calculateDamping(current, next, damping);

    Assertions.assertEquals(0.001, damping);
  }

  @Test
  public void testCalculateCartesianDelta() {
    RealMatrix A = new Array2DRowRealMatrix(new double[][]{
        {67, 67},
        {5, 50}});

    RealVector residual = new ArrayRealVector(new double[]{11, 17});

    double lambda = 0.01;

    RealMatrix AtA = A.transpose().multiply(A);
    RealMatrix multiplier = MatrixUtils.inverse(AtA.add(MatrixUtils.createRealIdentityMatrix(
        AtA.getColumnDimension()).scalarMultiply(lambda)));
    RealVector expectedValue = multiplier.multiply(A.transpose()).operate(residual);

    SingularValueDecomposition svd = new SingularValueDecomposition(A);
    RealVector actualValue1 = Util.calculateCartesianDelta(
        svd.getU(),
        svd.getV(),
        svd.getS(),
        residual,
        lambda);

    double[] expectedValueArray = expectedValue.toArray();
    double[] actualValueArray = actualValue1.toArray();

    for (int i = 0; i < expectedValueArray.length; i++) {
      Assertions.assertEquals(expectedValueArray[i], actualValueArray[i], 10e-16);
    }
  }

  @ParameterizedTest
  @MethodSource("testSetSingularValuesZeroProvider")
  public void testSetSingularValuesZero(double tolerance, RealMatrix input,
      RealMatrix expectedOutput) {

    RealMatrix output = Util.setSingularValuesZero(input, tolerance);

    Assertions.assertEquals(expectedOutput, output);
  }

  static Stream<Arguments> testSetSingularValuesZeroProvider() {

    return Stream.of(
        Arguments.arguments(
            Math.pow(10, 6),
            new Array2DRowRealMatrix(
                new double[][]{
                    {1.0, 0.0},
                    {0.0, 10000000.0}
                }
            ),
            new Array2DRowRealMatrix(
                new double[][]{
                    {0.0, 0.0},
                    {0.0, 10000000.0}
                }
            )
        ),
        Arguments.arguments(
            Math.pow(10, 7),
            new Array2DRowRealMatrix(
                new double[][]{
                    {10000000.0, 0.0, 0.0, 0.0},
                    {0.0, 1.0, 0.0, 0.0},
                    {0.0, 0.0, 100000.0, 0.0},
                    {0.0, 0.0, 0.0, 100000000.0}
                }
            ),
            new Array2DRowRealMatrix(
                new double[][]{
                    {10000000.0, 0.0, 0.0, 0.0},
                    {0.0, 0.0, 0.0, 0.0},
                    {0.0, 0.0, 100000.0, 0.0},
                    {0.0, 0.0, 0.0, 100000000.0}
                }
            )
        )
    );
  }

  @Test
  public void testSetSingularValuesZeroNullMatrix() {

    Throwable exception = Assertions.assertThrows(NullPointerException.class, () ->
        Util.setSingularValuesZero(null, 100.0)
    );

    Assertions.assertEquals("Null matrix", exception.getMessage());
  }

  @Test
  public void testSetSingularValuesNonSquareMatrix() {

    Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () ->
        Util.setSingularValuesZero(new Array2DRowRealMatrix(new double[1][2]), 100.0)
    );

    Assertions.assertEquals("Cannot execute method on non-square matrix", exception.getMessage());
  }

  @Test
  public void testGetFeatureMeasurementValue() {

    final Instant instant1 = Instant.parse("2019-01-21T14:17:19.272921Z");
    final InstantValue instantValue = InstantValue.from(instant1, Duration.ZERO);
    final FeatureMeasurement<InstantValue> instantFeatureMeasurement =
        FeatureMeasurement.create(
            UUID.randomUUID(),
            FeatureMeasurementTypes.ARRIVAL_TIME,
            instantValue);

    OptionalDouble optDouble = Util.getFeatureMeasurementValue(instantFeatureMeasurement);

    Assertions.assertTrue(optDouble.isPresent());

    final long epochMs = Math.round(optDouble.getAsDouble() * 1e3);

    final Duration duration = Duration.between(instant1, Instant.ofEpochMilli(epochMs));

    // Verify that it's less than a millisecond. There will be some round off error. If
    // using toNanos(), it will be 60 or so.
    Assertions.assertTrue(duration.toMillis() == 0L);

    final double azimuth = 30.0;

    final FeatureMeasurement<?> azimuthMeasurement = FeatureMeasurement.create(
        UUID.randomUUID(),
        FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH,
        NumericMeasurementValue.from(Instant.EPOCH, DoubleValue.from(azimuth,
            0.0, Units.DEGREES)));

    optDouble = Util.getFeatureMeasurementValue(azimuthMeasurement);

    Assertions.assertTrue(optDouble.isPresent());
    Assertions.assertEquals(azimuth, optDouble.getAsDouble(), 1e-9);

    final double slowness = 5.2;

    final FeatureMeasurement<?> slownessMeasurement = FeatureMeasurement.create(
        UUID.randomUUID(),
        FeatureMeasurementTypes.SLOWNESS,
        NumericMeasurementValue.from(Instant.EPOCH, DoubleValue.from(slowness,
            0.0, Units.SECONDS_PER_DEGREE)));

    optDouble = Util.getFeatureMeasurementValue(slownessMeasurement);

    Assertions.assertTrue(optDouble.isPresent());
    Assertions.assertEquals(slowness, optDouble.getAsDouble(), 1e-9);

    final FeatureMeasurement<?> wrongAzimuthMeasurement = FeatureMeasurement.create(
        UUID.randomUUID(),
        FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH,
        NumericMeasurementValue.from(Instant.EPOCH, DoubleValue.from(10.4,
            0.0, Units.DEGREES)));

    optDouble = Util.getFeatureMeasurementValue(wrongAzimuthMeasurement);

    Assertions.assertFalse(optDouble.isPresent());
  }

  @Test
  public void testCompute2dEllipse() {
    RealMatrix covarianceMatrix = new Array2DRowRealMatrix(new double[][]{
        //When the first and second row/column is used, will generate an ellipse
        //where one axis is sqrt(3) times the length of the other.
        {1.0, 0.5, 3.0, 4.0},
        {0.5, 1.0, 5.0, 6.0},
        {3.0, 5.0, 1.2, 7.0},
        {4.0, 6.0, 7.0, 1.3}
    });

    double[] axes = Util.compute2dEllipse(covarianceMatrix, 0, 1);

    Assertions.assertTrue(axes[0] >= axes[1]);

    // Obviously axes[1] has to be less than axes[0] for this to pass.
    Assertions.assertEquals(
        3.0 * (axes[1] * axes[1]), (axes[0] * axes[0]), 10E-16);
  }

  @Test
  public void testCompute2dEllipseZeroOffDiagonal() {
    final double axis0 = 2.0;
    final double axis1 = 1.0;

    RealMatrix covarianceMatrix = new Array2DRowRealMatrix(new double[][]{
        //When the first and second row/column is used, will generate an ellipse
        //where one axis is 1/sqrt(c11) and the other is 1/sqrt(c22)
        {axis0, 0.0, 3.0, 4.0},
        {0.0, axis1, 5.0, 6.0},
        {3.0, 5.0, 1.2, 7.0},
        {4.0, 6.0, 7.0, 1.3}
    });

    double[] axes = Util.compute2dEllipse(covarianceMatrix, 0, 1);

    Assertions.assertTrue(axes[0] >= axes[1]);

    Assertions.assertEquals(Math.sqrt(1.0 / axis0), axes[1], 10e-16);
    Assertions.assertEquals(Math.sqrt(1.0 / axis1), axes[0], 10e-16);
  }
}
