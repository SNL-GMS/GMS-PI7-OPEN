package gms.core.eventlocation.plugins.implementations.apachelm;

import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.apache.commons.lang3.tuple.Pair;

public class ApacheLmAlgorithmTests {

  /*
   * seed vector
   */

  static final double ORIGIN_LAT = 10.0;  // South China Sea (between Vietnam and Brunei)
  static final double ORIGIN_LON = 110.0;
  static final double ORIGIN_DEPTH = 70.0;
  static final double EVENT_TIME = 1546300800.0;  // 01-JAN-2019 00:00:00 GMT
  static final Logger logger = LoggerFactory.getLogger(ApacheLmAlgorithmTests.class);

  static final RealVector mSeed = new ArrayRealVector(
      new double[]{ORIGIN_LAT + 10.0, ORIGIN_LON + 15.0, ORIGIN_DEPTH, EVENT_TIME});
  // TODO: find out why the following commented-out test fails
//    new double[]{ORIGIN_LAT + 10.0, ORIGIN_LON + 10.0, ORIGIN_DEPTH, EVENT_TIME});

  /*
   * pcalc data
   */

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
  static final double[] AS10 = new double[]{-23.695526, 133.915193, 0.624, 10.0, 110.0, 70.0,
      41.011643,
      456.567567, 1.160000, 0.097215, 0.295708, 8.212651, 2.500000, 322.527671, 10.000000};
  static final double[] AS31 = new double[]{-23.665134, 133.905261, 0.6273, 10.0, 110.0, 70.0,
      40.981989,
      456.324740, 1.160000, 0.097722, 0.295949, 8.215254, 2.500000, 322.518690, 10.000000};
  static final double[] WB10 = new double[]{-19.7671, 134.3928, 0.3621, 10.0, 110.0, 70.0,
      38.249498,
      433.616740, 1.160000, 0.056121, 0.329138, 8.399207, 2.500000, 318.931528, 10.000000};
  static final double[] WR7 = new double[]{-19.9552, 134.476, 0.3548, 10.0, 110.0, 70.0, 38.442749,
      435.236839, 1.160000, 0.055008, 0.328383, 8.387003, 2.500000, 318.984783, 10.000000};
  static final double[] AS06 = new double[]{-23.646206, 133.972511, 0.6813, 10.0, 110.0, 70.0,
      41.004503,
      456.518592, 1.160000, 0.106140, 0.296444, 8.213150, 2.500000, 322.422230, 10.000000};
  static final double[] LBTBB = new double[]{-25.015124, 25.596598, 1.1483, 10.0, 110.0, 70.0,
      89.220487,
      768.885873, 1.199971, 0.191977, 0.596213, 4.686213, 2.500000, 78.580567, 10.000000};
  static final double[] KUR08 = new double[]{50.56317, 78.5108, 0.1986, 10.0, 110.0, 70.0,
      48.120499,
      512.785516, 1.160000, 0.031352, -0.027926, 7.708201, 2.500000, 136.299409, 10.000000};
  static final double[] MK07 = new double[]{46.753431, 82.315664, 0.6398, 10.0, 110.0, 70.0,
      43.616330,
      477.456071, 1.160000, 0.100164, 0.023972, 8.031469, 2.500000, 138.450168, 10.000000};



  /*
   * observations vectors
   */

  static final double[][] matrix = {
      {EVENT_TIME + AS10[7], AS10[8]},
      {EVENT_TIME + KUR08[7], KUR08[8]},
      {EVENT_TIME + WB10[7], WB10[8]},
      {EVENT_TIME + WR7[7], WR7[8]},
      {MK07[13], MK07[14]},
      {MK07[11], MK07[12]},
      {LBTBB[13], LBTBB[14]},
      {LBTBB[11], LBTBB[12]}
  };
  static final RealMatrix observations = new Array2DRowRealMatrix(matrix);

  static final List<FeatureMeasurementType<?>> featureMeasurementTypes = new ArrayList<>() {{
    add(FeatureMeasurementTypes.ARRIVAL_TIME);
    add(FeatureMeasurementTypes.ARRIVAL_TIME);
    add(FeatureMeasurementTypes.ARRIVAL_TIME);
    add(FeatureMeasurementTypes.ARRIVAL_TIME);
    add(FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH);
    add(FeatureMeasurementTypes.SLOWNESS);
    add(FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH);
    add(FeatureMeasurementTypes.SLOWNESS);
  }};

  static final List<Location> receiverLocations = new ArrayList<>() {{
    add(Location.from(AS10[0], AS10[1], 0.0, AS10[2]));
    add(Location.from(KUR08[0], KUR08[1], 0.0, KUR08[2]));
    add(Location.from(WB10[0], WB10[1], 0.0, WB10[2]));
    add(Location.from(WR7[0], WR7[1], 0.0, WR7[2]));
    add(Location.from(MK07[0], MK07[1], 0.0, MK07[2]));
    add(Location.from(MK07[0], MK07[1], 0.0, MK07[2]));
    add(Location.from(LBTBB[0], LBTBB[1], 0.0, LBTBB[2]));
    add(Location.from(LBTBB[0], LBTBB[1], 0.0, LBTBB[2]));
  }};

  static final Map<PhaseType, List<Pair<Location, FeatureMeasurementType<?>>>> phaseLocationMap = new HashMap<>();
  static final List<Pair<Location, FeatureMeasurementType<?>>> pairs = new LinkedList<>();

  static {
    for (int i = 0; i < featureMeasurementTypes.size(); i++) {
      pairs.add(Pair.create(receiverLocations.get(i), featureMeasurementTypes.get(i)));
    }

    phaseLocationMap.put(PhaseType.P, pairs);
  }


  @Test
  public void testLocateMethod() throws Exception {

    logger.info("beginning test");
    ApacheLmAlgorithm algorithm = new ApacheLmAlgorithm.Builder()
        .withMaximumIterationCount(100)
        .withResidualConvergenceThreshold(0.001)
        .build();

    Function<RealVector, Pair<RealMatrix, RealMatrix>> predictionFunction =
        new SignalFeaturePredictionUtility().getFFunction(
            "ak135",
            phaseLocationMap,
            List.of(PhaseType.P),
            List.of(),
            featurePredictions -> {
            },
            TestFixtures.replaceWithZeroFilter,
            TestFixtures.replaceWithZeroFilter,
            new LocationRestraint.Builder().build()
        );

    Triple<RealVector, RealMatrix, RealMatrix> result = algorithm
        .locate(mSeed, observations, predictionFunction);

    RealVector locationVector = result.getLeft();
//    Assert.assertArrayEquals(mTrue.toArray(), locationVector.toArray(), 0.3);
    logger.info("ending test");
  }


  @Test
  public void testLocateWithOneConstraint() throws TooManyRestraintsException {
    logger.info("beginning test");
    List.of(
        Triple.of(
            new LocationRestraint.Builder()
                .setLatitudeRestraint(mSeed.getEntry(0))
                .build(),
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
                .withLatitudeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{0}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setLongitudeRestraint(mSeed.getEntry(1))
                .build(),
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
                .withLongitudeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{1}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setDepthRestraint(mSeed.getEntry(2))
                .build(),
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
                .withDepthParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{2}
        ),
        Triple.of(
            new LocationRestraint.Builder()
                .setTimeRestraint(Instant.MIN)
                .build(),
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
                .withTimeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{3}
        )
    ).forEach(triple -> {
      ApacheLmAlgorithm algorithm = triple.getMiddle();

      Function<RealVector, Pair<RealMatrix, RealMatrix>> predictionFunction =
          new SignalFeaturePredictionUtility().getFFunction(
              "ak135",
              phaseLocationMap,
              List.of(PhaseType.P),
              List.of(),
              featurePredictions -> {
              },
              TestFixtures.replaceWithZeroFilter,
              TestFixtures.replaceWithZeroFilter,
              triple.getLeft()
          );

      Triple<RealVector, RealMatrix, RealMatrix> result = algorithm
          .locate(mSeed, observations, predictionFunction);

      RealVector locationVector = result.getLeft();

      Arrays.stream(triple.getRight()).forEach(
          index -> Assertions.assertEquals(mSeed.getEntry(index), locationVector.getEntry(index)));
    });
    logger.info("ending test");
  }


  @Test
  public void testLocateWithTwoConstraints() throws TooManyRestraintsException {
    logger.info("beginning test");
    List.of(
        Triple.of(
            new LocationRestraint.Builder()
                .setLatitudeRestraint(mSeed.getEntry(0))
                .setLongitudeRestraint(mSeed.getEntry(1))
                .build(),
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
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
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
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
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
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
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
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
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
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
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
                .withDepthParameterConstrainedToSeededValue(true)
                .withTimeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{2, 3}
        )
    ).forEach(triple -> {
      ApacheLmAlgorithm algorithm = triple.getMiddle();

      Function<RealVector, Pair<RealMatrix, RealMatrix>> predictionFunction =
          new SignalFeaturePredictionUtility().getFFunction(
              "ak135",
              phaseLocationMap,
              List.of(PhaseType.P),
              List.of(),
              featurePredictions -> {
              },
              TestFixtures.replaceWithZeroFilter,
              TestFixtures.replaceWithZeroFilter,
              triple.getLeft()
          );

      Triple<RealVector, RealMatrix, RealMatrix> result = algorithm
          .locate(mSeed, observations, predictionFunction);

      RealVector locationVector = result.getLeft();

      Arrays.stream(triple.getRight()).forEach(
          index -> Assertions.assertEquals(mSeed.getEntry(index), locationVector.getEntry(index)));
    });
    logger.info("ending test");
  }


  @Test
  public void testLocateWithThreeConstraints() throws TooManyRestraintsException {
    logger.info("beginning test");
    List.of(
        Triple.of(
            new LocationRestraint.Builder()
                .setLatitudeRestraint(mSeed.getEntry(0))
                .setLongitudeRestraint(mSeed.getEntry(1))
                .setDepthRestraint(mSeed.getEntry(2))
                .build(),
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
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
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
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
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
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
            new ApacheLmAlgorithm.Builder()
                .withMaximumIterationCount(100)
                .withResidualConvergenceThreshold(0.001)
                .withLongitudeParameterConstrainedToSeededValue(true)
                .withDepthParameterConstrainedToSeededValue(true)
                .withTimeParameterConstrainedToSeededValue(true)
                .build(),
            new int[]{1, 2, 3}
        )
    ).forEach(triple -> {
      ApacheLmAlgorithm algorithm = triple.getMiddle();

      Function<RealVector, Pair<RealMatrix, RealMatrix>> predictionFunction =
          new SignalFeaturePredictionUtility().getFFunction(
              "ak135",
              phaseLocationMap,
              List.of(PhaseType.P),
              List.of(),
              featurePredictions -> {
              },
              TestFixtures.replaceWithZeroFilter,
              TestFixtures.replaceWithZeroFilter,
              triple.getLeft()
          );

      Triple<RealVector, RealMatrix, RealMatrix> result = algorithm
          .locate(mSeed, observations, predictionFunction);

      RealVector locationVector = result.getLeft();

      Arrays.stream(triple.getRight()).forEach(
          index -> Assertions.assertEquals(mSeed.getEntry(index), locationVector.getEntry(index)));
    });
    logger.info("ending test");
  }

  @Test
  public void testBuilderThrowsTooManyRestraintsException() {
    logger.info("beginning test");
    Assertions.assertThrows(TooManyRestraintsException.class, () ->
        new ApacheLmAlgorithm.Builder()
            .withMaximumIterationCount(1000)
            //Four constraints
            .withLatitudeParameterConstrainedToSeededValue(true)
            .withLongitudeParameterConstrainedToSeededValue(true)
            .withDepthParameterConstrainedToSeededValue(true)
            .withTimeParameterConstrainedToSeededValue(true)
            .build());
    logger.info("ending test");
  }
}
