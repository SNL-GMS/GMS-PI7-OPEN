package gms.core.eventlocation.plugins.implementations.geigers;

import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.utilities.geomath.BicubicInterpolator;
import gms.shared.utilities.geomath.BicubicSplineInterpolator;
import gms.shared.utilities.geomath.GeoMath;
import gms.shared.utilities.geomath.RowFilteredRealMatrix;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;


public class GeigersAlgorithmTests {

  private RealVector mTrue;
  private RealVector mSeed;
  private RealMatrix observations;
  private Map<PhaseType, List<Pair<Location, FeatureMeasurementType<?>>>> phaseLocationMap;
  private Function<RealVector, Pair<RowFilteredRealMatrix, RowFilteredRealMatrix>> predictionFunction;

  @Before
  public void setup() {
    /*
     * seed vector
     */

    final double ORIGIN_LAT = 10.0;  // South China Sea (between Vietnam and Brunei)
    final double ORIGIN_LON = 110.0;
    final double ORIGIN_DEPTH = 70.0;
    final double EVENT_TIME = 1546300800.0;  // 01-JAN-2019 00:00:00 GMT

    mTrue = new ArrayRealVector(new double[]{ORIGIN_LAT, ORIGIN_LON, ORIGIN_DEPTH, EVENT_TIME});
    mSeed = new ArrayRealVector(
//    new double[]{ORIGIN_LAT, ORIGIN_LON, ORIGIN_DEPTH, EVENT_TIME});
        new double[]{ORIGIN_LAT - 1.0, ORIGIN_LON - 1.0, ORIGIN_DEPTH, EVENT_TIME});
//    new double[]{ORIGIN_LAT + 5.0, ORIGIN_LON + 5.0, ORIGIN_DEPTH, EVENT_TIME});
//    new double[]{ORIGIN_LAT + 10.0, ORIGIN_LON + 10.0, ORIGIN_DEPTH, EVENT_TIME});


    /*
     * observations matrix
     */

    //
    // pcalc data
    //
    //  0: site_lat
    //  1: site_lon
    //  2: site_elev
    //  3: origin_lat
    //  4: origin_lon
    //  5: origin_depth
    //  6: distance_degrees
    //  7: travel_time
    //  8: tt_model_uncertainty
    //  9: tt_elevation_correction
    // 10: tt_ellipticity_correction
    // 11: slowness_degrees
    // 12: slowness_model_uncertainty_degrees
    // 13: azimuth_degrees
    // 14: azimuth_model_uncertainty_degrees
    final double[] AS10 = new double[]{-23.695526, 133.915193, 0.624, 10.0, 110.0, 70.0,
        41.011643, 456.567567, 1.160000, 0.097215, 0.295708, 8.212651, 2.500000, 322.527671,
        10.000000};
    final double[] AS31 = new double[]{-23.665134, 133.905261, 0.6273, 10.0, 110.0, 70.0,
        40.981989, 456.324740, 1.160000, 0.097722, 0.295949, 8.215254, 2.500000, 322.518690,
        10.000000};
    final double[] WB10 = new double[]{-19.7671, 134.3928, 0.3621, 10.0, 110.0, 70.0,
        38.249498, 433.616740, 1.160000, 0.056121, 0.329138, 8.399207, 2.500000, 318.931528,
        10.000000};
    final double[] WR7 = new double[]{-19.9552, 134.476, 0.3548, 10.0, 110.0, 70.0,
        38.442749,
        435.236839, 1.160000, 0.055008, 0.328383, 8.387003, 2.500000, 318.984783, 10.000000};
    final double[] AS06 = new double[]{-23.646206, 133.972511, 0.6813, 10.0, 110.0, 70.0,
        41.004503, 456.518592, 1.160000, 0.106140, 0.296444, 8.213150, 2.500000, 322.422230,
        10.000000};
    final double[] LBTBB = new double[]{-25.015124, 25.596598, 1.1483, 10.0, 110.0, 70.0,
        89.220487, 768.885873, 1.199971, 0.191977, 0.596213, 4.686213, 2.500000, 78.580567,
        10.000000};
    final double[] KUR08 = new double[]{50.56317, 78.5108, 0.1986, 10.0, 110.0, 70.0,
        48.120499, 512.785516, 1.160000, 0.031352, -0.027926, 7.708201, 2.500000, 136.299409,
        10.000000};
    final double[] MK07 = new double[]{46.753431, 82.315664, 0.6398, 10.0, 110.0, 70.0,
        43.616330, 477.456071, 1.160000, 0.100164, 0.023972, 8.031469, 2.500000, 138.450168,
        10.000000};

    double[][] matrix = {
        {EVENT_TIME + AS10[7], AS10[8]},
        {EVENT_TIME + KUR08[7], KUR08[8]},
        {EVENT_TIME + WB10[7], WB10[8]},
        {EVENT_TIME + WR7[7], WR7[8]},
        {MK07[13], MK07[14]},
        {MK07[11], MK07[12]},
        {LBTBB[13], LBTBB[14]},
        {LBTBB[11], LBTBB[12]}
    };
    observations = new Array2DRowRealMatrix(matrix);

    /*
     * prediction function
     */

    List<FeatureMeasurementType<?>> featureMeasurementTypes = new ArrayList<>();
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);
    featureMeasurementTypes.add(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);

    List<Location> receiverLocations = new ArrayList<>() {{
      add(Location.from(AS10[0], AS10[1], 0.0, AS10[2]));
      add(Location.from(KUR08[0], KUR08[1], 0.0, KUR08[2]));
      add(Location.from(WB10[0], WB10[1], 0.0, WB10[2]));
      add(Location.from(WR7[0], WR7[1], 0.0, WR7[2]));
      add(Location.from(MK07[0], MK07[1], 0.0, MK07[2]));
      add(Location.from(MK07[0], MK07[1], 0.0, MK07[2]));
      add(Location.from(LBTBB[0], LBTBB[1], 0.0, LBTBB[2]));
      add(Location.from(LBTBB[0], LBTBB[1], 0.0, LBTBB[2]));
    }};

    phaseLocationMap = new HashMap<>();
    List<Pair<Location, FeatureMeasurementType<?>>> pairs = new LinkedList<>();

    for (int i = 0; i < featureMeasurementTypes.size(); i++) {
      pairs.add(Pair.create(receiverLocations.get(i), featureMeasurementTypes.get(i)));
    }

    phaseLocationMap.put(PhaseType.P, pairs);

    predictionFunction =
        new SignalFeaturePredictionUtility().getFFunction(
            "ak135",
            phaseLocationMap,
            List.of(PhaseType.P),
            List.of(),
            featurePredictions -> {
            },
            valueErrorMatrix -> {
              return RowFilteredRealMatrix
                  .filterRowsByValue(valueErrorMatrix, (v) -> Double.isNaN(v), false);
            },
            jacobianMatrix -> {
              return RowFilteredRealMatrix
                  .filterRowsByValue(jacobianMatrix, (v) -> Double.isNaN(v), false);
            },
            new LocationRestraint.Builder().build()
        );
  }


  @Test
  @Ignore
  //TODO: Remove this TODO when problems being addressed by this test are fixed in other branch
  public void testLocateMethod2() throws Exception {
    mSeed = new ArrayRealVector(
        new double[]{-2.0675, 126.78543, 52.965, 1274387588.384});

    observations = new Array2DRowRealMatrix(new double[][]{
        {342.93, 2.17},
        {11.88, 0.45},
        {1274388064.35, 0.12},
        {161.93, 14.88},
        {9.05, 2.34},
        {1274388318.66, 0.82},
        {107.88, 54.64},
        {4.7, 4.31},
        {1274388406.63, 1.042999999},
        {178.81, 6.13},
        {10.56, 1.13},
        {1274388212.3, 0.782},
        {156.11, 13.67},
        {11.35, 2.7},
        {1274388447.962, 0.847},
        {118.73, 14.19},
        {6.33, 1.56},
        {1274388386.025, 0.12},
        {199.44, 6.67},
        {7.3, 4.29},
        {1274388268.47, 0.779},
        {348.26, 1.51},
        {21.13, 0.56},
        {1274388240.354, 0.552},
        {33.22, 180.0},
        {0.77, 2.75},
        {1274388903.722, 1.07}
    });

    List<FeatureMeasurementType<?>> featureMeasurementTypes = new ArrayList<>();
    featureMeasurementTypes.add(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);
    featureMeasurementTypes.add(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH);
    featureMeasurementTypes.add(FeatureMeasurementTypes.SLOWNESS);
    featureMeasurementTypes.add(FeatureMeasurementTypes.ARRIVAL_TIME);

    List<Location> receiverLocations = new ArrayList<>() {
      {
        add(Location.from(-23.66513, 133.90526, 0.0, 0.627));
        add(Location.from(-23.66513, 133.90526, 0.0, 0.627));
        add(Location.from(-23.66513, 133.90526, 0.0, 0.627));
        add(Location.from(47.83469, 106.39499, 0.0, 1.416));
        add(Location.from(47.83469, 106.39499, 0.0, 1.416));
        add(Location.from(47.83469, 106.39499, 0.0, 1.416));
        add(Location.from(53.94805, 84.81878, 0.0, 0.229));
        add(Location.from(53.94805, 84.81878, 0.0, 0.229));
        add(Location.from(53.94805, 84.81878, 0.0, 0.229));
        add(Location.from(37.44209, 127.88452, 0.0, 0.138));
        add(Location.from(37.44209, 127.88452, 0.0, 0.138));
        add(Location.from(37.44209, 127.88452, 0.0, 0.138));
        add(Location.from(53.02492, 70.38853, 0.0, 0.42));
        add(Location.from(53.02492, 70.38853, 0.0, 0.42));
        add(Location.from(53.02492, 70.38853, 0.0, 0.42));
        add(Location.from(46.79368, 82.29057, 0.0, 0.618));
        add(Location.from(46.79368, 82.29057, 0.0, 0.618));
        add(Location.from(46.79368, 82.29057, 0.0, 0.618));
        add(Location.from(44.1998, 131.9888, 0.0, 0.17));
        add(Location.from(44.1998, 131.9888, 0.0, 0.17));
        add(Location.from(44.1998, 131.9888, 0.0, 0.17));
        add(Location.from(-19.9426, 134.3395, 0.0, 0.389));
        add(Location.from(-19.9426, 134.3395, 0.0, 0.389));
        add(Location.from(-19.9426, 134.3395, 0.0, 0.389));
        add(Location.from(13.14771, 1.69471, 0.0, 0.214));
        add(Location.from(13.14771, 1.69471, 0.0, 0.214));
        add(Location.from(13.14771, 1.69471, 0.0, 0.214));
      }
    };

    List<Pair<Location, FeatureMeasurementType<?>>> pairsP = new LinkedList<>();
    for (int i = 0; i < 21; i++) {
      pairsP.add(Pair.create(receiverLocations.get(i), featureMeasurementTypes.get(i)));
    }
    List<Pair<Location, FeatureMeasurementType<?>>> pairsS = new LinkedList<>();
    for (int i = 21; i < 24; i++) {
      pairsS.add(Pair.create(receiverLocations.get(i), featureMeasurementTypes.get(i)));
    }
    List<Pair<Location, FeatureMeasurementType<?>>> pairsPKP = new LinkedList<>();
    for (int i = 24; i < 27; i++) {
      pairsPKP.add(Pair.create(receiverLocations.get(i), featureMeasurementTypes.get(i)));
    }

    phaseLocationMap = new HashMap<>();
    phaseLocationMap.put(PhaseType.P, pairsP);
    phaseLocationMap.put(PhaseType.S, pairsS);
    phaseLocationMap.put(PhaseType.PKP, pairsPKP);

    GeigersAlgorithm algorithm = new GeigersAlgorithm.Builder()
        .withMaximumIterationCount(100)
        .withConvergenceThreshold(0.01)
        .withConvergenceCount(100)
        .withLevenbergMarquardtEnabled(true)
        .withLambda0(0.01)
        .withLambdaX(10.0)
        .withDeltaNormThreshold(0.01)
        .withSingularValueWFactor(0.001)
        .withMaximumWeightedPartialDerivative(10.0e12)
        .withLatitudeParameterConstrainedToSeededValue(false)
        .withLongitudeParameterConstrainedToSeededValue(false)
        .withDepthParameterConstrainedToSeededValue(false)
        .withTimeParameterConstrainedToSeededValue(true)
        .build();

    predictionFunction = new SignalFeaturePredictionUtility().getFFunction(
        "ak135",
        phaseLocationMap,
        List.of(PhaseType.P, PhaseType.S, PhaseType.PKP),
        List.of(),
        featurePredictions -> {
        },
        algorithm.getErrorValueNaNProcessor(),
        algorithm.getJacobianNaNProcessor(),
        new LocationRestraint.Builder().setTimeRestraint(Instant.ofEpochSecond(1274387588L)).build()
    );

    Triple<RealVector, RealMatrix, RealMatrix> result = algorithm
        .locate(mSeed, observations, predictionFunction);

    RealVector locationVector = result.getLeft();
//    Assert.assertArrayEquals(mTrue.toArray(), locationVector.toArray(), 2.0);
  }

  @Test
  @Ignore
  public void printTestValuesObs0_Time() {

    double delta = GeoMath.greatCircleAngularSeparation(9.0, 109.0, -23.695526, 133.915193);
    double alpha = GeoMath.azimuth(9.0, 109.0, -23.695526, 133.915193);
    double cosAlpha = Math.cos(Math.toRadians(alpha));
    double sinAlpha = Math.sin(Math.toRadians(alpha));

    final double[] delta_stops = {39.0, 39.5, 40.0, 40.5, 41.0, 41.5, 42.0};
    final double[] depth_stops = {35.0, 50.0, 75.0, 100.0};
    final double[][] table = {
        {442.9671, 441.4871, 439.0246, 436.5673},
        {447.1415, 445.658,  443.1895, 440.7262},
        {451.2988, 449.8117, 447.3374, 444.8681},
        {455.4391, 453.9485, 451.4683, 448.9932},
        {459.5623, 458.0683, 455.5823, 453.1014},
        {463.6686, 462.1711, 459.6794, 457.1926},
        {467.7578, 466.2568, 463.7593, 461.2666}};
    BicubicInterpolator nbci = new BicubicSplineInterpolator();
    double[] derivatives = nbci.getFunctionAndDerivatives(delta_stops, depth_stops, table).apply(delta, 70.0);

    double dTdDelta = derivatives[1];
    double dTdx = -dTdDelta * sinAlpha;
    double dTdy = -dTdDelta * cosAlpha;
    double dTdz = derivatives[3];

    System.err.print("Travel Time: observation 0\n" +
        "\t      delta = " + delta + "\n" +
        "\t      alpha = " + alpha + "degrees\n" +
        "\t sin(alpha) = " + sinAlpha + "\n" +
        "\t cos(alpha) = " + cosAlpha + "\n" +
        "\tdT / dDelta = " + dTdDelta + "\n" +
        "\t    dT / dx = " + dTdx + "\n" +
        "\t    dT / dy = " + dTdy + "\n" +
        "\t    dT / dz = " + dTdz + "\n");
  }

  @Test
  @Ignore
  public void printTestValuesObs4_Azimuth() {

    double delta = GeoMath.greatCircleAngularSeparation(9.0, 109.0, 46.753431, 82.315664);
    double alpha = GeoMath.azimuth(9.0, 109.0, 46.753431, 82.315664);
    double cosAlpha = Math.cos(Math.toRadians(alpha));
    double sinAlpha = Math.sin(Math.toRadians(alpha));
    double sinDelta = Math.sin(Math.toRadians(delta));

    double dTdx = -cosAlpha/sinDelta;
    double dTdy = sinAlpha/sinDelta;

    System.err.print("Travel Time: observation 4\n" +
        "\t      delta = " + delta + "\n" +
        "\t      alpha = " + alpha + "degrees\n" +
        "\t sin(alpha) = " + sinAlpha + "\n" +
        "\t cos(alpha) = " + cosAlpha + "\n" +
        "\t sin(delta) = " + sinDelta + "\n" +
        "\t    dT / dx = " + dTdx + "\n" +
        "\t    dT / dy = " + dTdy + "\n");
  }

  @Test
  @Ignore
  public void printTestValuesObs7_Slowness() {

    double delta = GeoMath.greatCircleAngularSeparation(9.0, 109.0, -25.015124, 25.596598);
    double alpha = GeoMath.azimuth(9.0, 109.0, -25.015124, 25.596598);
    double cosAlpha = Math.cos(Math.toRadians(alpha));
    double sinAlpha = Math.sin(Math.toRadians(alpha));

    final double[] delta_stops = {39.0, 39.5, 40.0, 40.5, 41.0, 41.5, 42.0};
    final double[] depth_stops = {35.0, 50.0, 75.0, 100.0};
    final double[][] table = {
        {442.9671, 441.4871, 439.0246, 436.5673},
        {447.1415, 445.658,  443.1895, 440.7262},
        {451.2988, 449.8117, 447.3374, 444.8681},
        {455.4391, 453.9485, 451.4683, 448.9932},
        {459.5623, 458.0683, 455.5823, 453.1014},
        {463.6686, 462.1711, 459.6794, 457.1926},
        {467.7578, 466.2568, 463.7593, 461.2666}};
    BicubicInterpolator nbci = new BicubicSplineInterpolator();
    double[] derivatives = nbci.getFunctionAndDerivatives(delta_stops, depth_stops, table).apply(delta, 70.0);

    double d2TdDelta2 = derivatives[2];
    double d2TdZdDelta = derivatives[4];
    double dTdx = -d2TdDelta2 * sinAlpha;
    double dTdy = -d2TdDelta2 * cosAlpha;
    double dTdz = d2TdZdDelta;

    System.err.print("Travel Time: observation 7\n" +
        "\t       delta = " + delta + "\n" +
        "\t       alpha = " + alpha + "degrees\n" +
        "\t  sin(alpha) = " + sinAlpha + "\n" +
        "\t  cos(alpha) = " + cosAlpha + "\n" +
        "\t d2T/dDelta2 = " + d2TdDelta2 + "\n" +
        "\td2T/dZdDelta = " + d2TdZdDelta + "\n" +
        "\t     dT / dx = " + dTdx + "\n" +
        "\t     dT / dy = " + dTdy + "\n" +
        "\t     dT / dz = " + dTdz + "\n");
  }

  @Test
  public void testLocateMethod() throws Exception {
    GeigersAlgorithm algorithm = new GeigersAlgorithm.Builder()
        .withMaximumIterationCount(400)
        .withConvergenceThreshold(0.001)
        .withConvergenceCount(2)
        .withLevenbergMarquardtEnabled(true)
        .withLambda0(0.001)
        .withLambdaX(10.0)
        .withDeltaNormThreshold(0.01)
        .withSingularValueWFactor(1.0e-6)
        .withMaximumWeightedPartialDerivative(1.0e12)
        .withLatitudeParameterConstrainedToSeededValue(false)
        .withLongitudeParameterConstrainedToSeededValue(false)
        .withDepthParameterConstrainedToSeededValue(false)
        .withTimeParameterConstrainedToSeededValue(false)
        .build();

    Triple<RealVector, RealMatrix, RealMatrix> result = algorithm
        .locate(mSeed, observations, predictionFunction);

    RealVector locationVector = result.getLeft();
//    Assert.assertArrayEquals(mTrue.toArray(), locationVector.toArray(), 2.0);
  }

  @Test
  public void testLocateWithOneConstraint() throws Exception {
    List.of(
        Triple.of(
            new LocationRestraint.Builder()
                .setLatitudeRestraint(mSeed.getEntry(0))
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withLatitudeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{0}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setLongitudeRestraint(mSeed.getEntry(1))
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withLongitudeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{1}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setDepthRestraint(mSeed.getEntry(2))
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withDepthParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{2}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setTimeRestraint(Instant.MIN)
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withTimeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{3}
        )
    ).forEach(triple -> {
          GeigersAlgorithm algorithm = triple.getMiddle();
          Function<RealVector, Pair<RowFilteredRealMatrix, RowFilteredRealMatrix>> predictionFunction =
              new SignalFeaturePredictionUtility().getFFunction(
                  "ak135",
                  phaseLocationMap,
                  List.of(PhaseType.P),
                  List.of(),
                  featurePredictions -> {
                  },
                  valueErrorMatrix -> {
                    return RowFilteredRealMatrix
                        .filterRowsByValue(valueErrorMatrix, (v) -> Double.isNaN(v), false);
                  },
                  jacobianMatrix -> {
                    return RowFilteredRealMatrix
                        .filterRowsByValue(jacobianMatrix, (v) -> Double.isNaN(v), false);
                  },
                  triple.getLeft()
              );
          Triple<RealVector, RealMatrix, RealMatrix> result = algorithm
              .locate(mSeed, observations, predictionFunction);
          RealVector locationVector = result.getLeft();

          Arrays.stream(triple.getRight()).forEach(
              index -> Assertions
                  .assertEquals(mSeed.getEntry(index), locationVector.getEntry(index)));
        }
    );
  }

  @Test
  public void testLocateWithTwoConstraints() throws Exception {
    List.of(
        Triple.of(
            new LocationRestraint.Builder()
                .setLatitudeRestraint(mSeed.getEntry(0))
                .setLongitudeRestraint(mSeed.getEntry(1))
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withLatitudeParameterConstrainedToSeededValue(true)
                .withLongitudeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{0, 1}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setLatitudeRestraint(mSeed.getEntry(0))
                .setDepthRestraint(mSeed.getEntry(2))
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withLatitudeParameterConstrainedToSeededValue(true)
                .withDepthParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{0, 2}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setLatitudeRestraint(mSeed.getEntry(0))
                .setTimeRestraint(Instant.MIN)
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withLatitudeParameterConstrainedToSeededValue(true)
                .withTimeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{0, 3}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setLongitudeRestraint(mSeed.getEntry(1))
                .setDepthRestraint(mSeed.getEntry(2))
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withLongitudeParameterConstrainedToSeededValue(true)
                .withDepthParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{1, 2}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setLongitudeRestraint(mSeed.getEntry(1))
                .setTimeRestraint(Instant.MIN)
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withLongitudeParameterConstrainedToSeededValue(true)
                .withTimeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{1, 3}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setDepthRestraint(mSeed.getEntry(2))
                .setTimeRestraint(Instant.MIN)
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withDepthParameterConstrainedToSeededValue(true)
                .withTimeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{2, 3}
        )
    ).forEach(triple -> {
          GeigersAlgorithm algorithm = triple.getMiddle();
          Function<RealVector, Pair<RowFilteredRealMatrix, RowFilteredRealMatrix>> predictionFunction =
              new SignalFeaturePredictionUtility().getFFunction(
                  "ak135",
                  phaseLocationMap,
                  List.of(PhaseType.P),
                  List.of(),
                  featurePredictions -> {
                  },
                  valueErrorMatrix -> {
                    return RowFilteredRealMatrix
                        .filterRowsByValue(valueErrorMatrix, (v) -> Double.isNaN(v), false);
                  },
                  jacobianMatrix -> {
                    return RowFilteredRealMatrix
                        .filterRowsByValue(jacobianMatrix, (v) -> Double.isNaN(v), false);
                  },
                  triple.getLeft()
              );
          Triple<RealVector, RealMatrix, RealMatrix> result = algorithm
              .locate(mSeed, observations, predictionFunction);
          RealVector locationVector = result.getLeft();

          Arrays.stream(triple.getRight()).forEach(
              index -> Assertions
                  .assertEquals(mSeed.getEntry(index), locationVector.getEntry(index)));
        }
    );
  }

  @Test
  public void testLocateWithThreeConstraints() throws Exception {
    List.of(
        Triple.of(
            new LocationRestraint.Builder()
                .setLatitudeRestraint(mSeed.getEntry(0))
                .setLongitudeRestraint(mSeed.getEntry(1))
                .setDepthRestraint(mSeed.getEntry(2))
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withLatitudeParameterConstrainedToSeededValue(true)
                .withLongitudeParameterConstrainedToSeededValue(true)
                .withDepthParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{0, 1, 2}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setLatitudeRestraint(mSeed.getEntry(0))
                .setLongitudeRestraint(mSeed.getEntry(1))
                .setTimeRestraint(Instant.MIN)
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withLatitudeParameterConstrainedToSeededValue(true)
                .withLongitudeParameterConstrainedToSeededValue(true)
                .withTimeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{0, 1, 3}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setLatitudeRestraint(mSeed.getEntry(0))
                .setDepthRestraint(mSeed.getEntry(2))
                .setTimeRestraint(Instant.MIN)
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withLatitudeParameterConstrainedToSeededValue(true)
                .withDepthParameterConstrainedToSeededValue(true)
                .withTimeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{0, 2, 3}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setLongitudeRestraint(mSeed.getEntry(1))
                .setDepthRestraint(mSeed.getEntry(2))
                .setTimeRestraint(Instant.MIN)
                .build(),
            new GeigersAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withConvergenceThreshold(0.001)
                .withConvergenceCount(2)
                .withLevenbergMarquardtEnabled(true)
                .withLambda0(0.001)
                .withLambdaX(10.0)
                .withDeltaNormThreshold(0.01)
                .withSingularValueWFactor(1.0e-6)
                .withMaximumWeightedPartialDerivative(1.0e12)
                .withLongitudeParameterConstrainedToSeededValue(true)
                .withDepthParameterConstrainedToSeededValue(true)
                .withTimeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{1, 2, 3}
        )
    ).forEach(triple -> {
          GeigersAlgorithm algorithm = triple.getMiddle();
          Function<RealVector, Pair<RowFilteredRealMatrix, RowFilteredRealMatrix>> predictionFunction =
              new SignalFeaturePredictionUtility().getFFunction(
                  "ak135",
                  phaseLocationMap,
                  List.of(PhaseType.P),
                  List.of(),
                  featurePredictions -> {
                  },
                  valueErrorMatrix -> {
                    return RowFilteredRealMatrix
                        .filterRowsByValue(valueErrorMatrix, (v) -> Double.isNaN(v), false);
                  },
                  jacobianMatrix -> {
                    return RowFilteredRealMatrix
                        .filterRowsByValue(jacobianMatrix, (v) -> Double.isNaN(v), false);
                  },
                  triple.getLeft()
              );
          Triple<RealVector, RealMatrix, RealMatrix> result = algorithm
              .locate(mSeed, observations, predictionFunction);
          RealVector locationVector = result.getLeft();

          Arrays.stream(triple.getRight()).forEach(
              index -> Assertions
                  .assertEquals(mSeed.getEntry(index), locationVector.getEntry(index)));
        }
    );
  }

  @Test
  public void testBuilderThrowsTooManyRestraintsException() {
    Assertions.assertThrows(TooManyRestraintsException.class, () ->
        new GeigersAlgorithm.Builder()
            .withMaximumIterationCount(1000)
            //Four constraints
            .withLatitudeParameterConstrainedToSeededValue(true)
            .withLongitudeParameterConstrainedToSeededValue(true)
            .withDepthParameterConstrainedToSeededValue(true)
            .withTimeParameterConstrainedToSeededValue(true)
            .build());
  }
}
