package gms.core.eventlocation.plugins.pluginutils;

import static com.google.common.collect.Streams.zip;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Streams;
import gms.core.eventlocation.plugins.definitions.EventLocationDefinitionGeigers;
import gms.core.eventlocation.plugins.exceptions.TooManyRestraintsException;
import gms.core.eventlocation.plugins.pluginutils.seedgeneration.SeedGenerator;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ElevationCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Ellipse;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EllipticityCorrection1dDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionDerivativeType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationBehavior;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationSolution;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementTypes;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.InstantValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.NumericMeasurementValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStation;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.StationType;
import gms.shared.utilities.geomath.RowFilteredRealMatrix;
import gms.shared.utilities.geomath.StatUtil;
import gms.shared.utilities.signalfeaturepredictionutility.SignalFeaturePredictionUtility;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GeneralEventLocatorDelegateTests {

  private static int LOWEST_TIME = 1;
  private static double LOWEST_LON = 45.0;

  private static long integerSeconds = 100;
  private static long nanos = (long) (0.51 * 1_000_000_000.0);
  private static double timeSeconds =
      Instant.ofEpochSecond(integerSeconds).getEpochSecond() + nanos / 1_000_000_000.0;
  private static Instant time = Instant.ofEpochSecond(integerSeconds, nanos);

  private static RealVector returnedVector =
      new ArrayRealVector(new double[]{0.0, 0.0, 0.0,
          timeSeconds});

  private static RealMatrix returnedCovarianceMatrix = new Array2DRowRealMatrix(new double[][]{
      //When the first and second row/column is used, will generate an ellipse
      //where one axis is sqrt(3) times the length of the other.

      //Also creates an ellipse with major axis trend PI/4.
      {1.0, 0.5, 3.0, 4.0},
      {0.5, 1.0, 5.0, 6.0},
      {3.0, 5.0, 1.2, 7.0},
      {4.0, 6.0, 7.0, 1.3}
  });

  //NOTE: No correlation with returnedCovarianceMatrix
  private static RealMatrix derivativeMatrix = new Array2DRowRealMatrix(new double[][]{
      {1.1, 1.2, 1.3, 1.4},
      {1.4, 1.5, 1.6, 1.7},
      {Double.NaN, Double.NaN, Double.NaN, Double.NaN},
      {1.8, 1.9, 2.0, 2.1},
      {1.8, 1.9, 2.0, 2.1},
      {1.8, 1.9, 2.0, 2.1}
  });

  private static RealMatrix filteredDerivativeMatrix = TestFixtures.replaceWithZeroFilter
      .apply(derivativeMatrix);

  private static double confidence = 0.95;

  private static RealMatrix filteredCovarianceMatrix = filteredDerivativeMatrix.transpose()
      .multiply(filteredDerivativeMatrix);

  private static RealMatrix returnedInfoMatrix = new Array2DRowRealMatrix(new double[][]{
      {7.0, 10.0},
      {8.0, 11.0},
      {9.0, 12.0},
      {13.0, 14.0},
      {15.0, 16.0}
  });

  private static RealMatrix returnedFilteredInfoMatrix = new Array2DRowRealMatrix(new double[][]{
      {7.0, 10.0},
      {8.0, 11.0},
      {0, 0},
      {9.0, 12.0},
      {13.0, 14.0},
      {15.0, 16.0}
  });

  private static RealVector weightedResiduals =
      returnedInfoMatrix.getColumnVector(0).ebeMultiply(returnedInfoMatrix.getColumnVector(1));

  private static RealVector filteredWeightedResiduals =
      returnedFilteredInfoMatrix.getColumnVector(0)
          .ebeMultiply(returnedFilteredInfoMatrix.getColumnVector(1));

  private static double scalingFactor = StatUtil.kappas(
      ScalingFactorType.CONFIDENCE,
      0,
      weightedResiduals.getDimension(),
      returnedCovarianceMatrix.getColumnDimension(),
      weightedResiduals.dotProduct(weightedResiduals),
      confidence,
      1.0)[1];

  private static double filteredScalingFactor = StatUtil.kappas(
      ScalingFactorType.CONFIDENCE,
      0,
      filteredWeightedResiduals.getDimension(),
      filteredCovarianceMatrix.getColumnDimension(),
      filteredWeightedResiduals.dotProduct(filteredWeightedResiduals),
      confidence,
      1.0)[1];

  private static RealMatrix returnedFpValues = new Array2DRowRealMatrix(new double[][]{
      {1.0, 4.0},
      {2.0, 5.0},
      {3.0, 6.0},
      {13.0, 14.0},
      {15.0, 16.0}
  });

  private static RealVector returnedRestrainedVector =
      new ArrayRealVector(new double[]{1.0, 1.0, 43.2,
          0.0});

  private static RealMatrix returnedRestrainedCovarianceMatrix = new Array2DRowRealMatrix(
      new double[][]{
          {1.0, 0.5, 3.0},
          {0.5, 1.0, 5.0},
          {3.0, 5.0, 1.2}
      });

  private static double restrainedScalingFactor = StatUtil.kappas(
      ScalingFactorType.CONFIDENCE,
      0,
      weightedResiduals.getDimension(),
      returnedCovarianceMatrix.getColumnDimension(),
      weightedResiduals.dotProduct(weightedResiduals),
      confidence,
      1.0)[1];

  private static RealMatrix returnedRecoveredRestrainedCovarianceMatrix = new Array2DRowRealMatrix(
      new double[][]{
          {1.0, 0.5, 0.0, 3.0},
          {0.5, 1.0, 0.0, 5.0},
          {0.0, 0.0, 0.0, 0.0},
          {3.0, 5.0, 0.0, 1.2}
      }).scalarMultiply(restrainedScalingFactor);

  private static RealMatrix returnedFilteredFpValues = new Array2DRowRealMatrix(new double[][]{
      {1.0, 4.0},
      {2.0, 5.0},
      {Double.NaN, Double.NaN},
      {3.0, 6.0},
      {13.0, 14.0},
      {15.0, 16.0}
  });

  private static UUID[] signalDetectionUUIDs = {
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID()
  };

  private static double[] longitudes = {
      LOWEST_LON * 2.1,
      LOWEST_LON,
      LOWEST_LON + 20.0,
      LOWEST_LON * 3.1,
      LOWEST_LON + 6
  };

  private static InstantValue[] arrivalTimes = {
      InstantValue.from(Instant.ofEpochSecond(LOWEST_TIME * 1000), Duration.ZERO),
      InstantValue.from(Instant.ofEpochSecond(LOWEST_TIME), Duration.ZERO),
      InstantValue.from(Instant.ofEpochSecond(LOWEST_TIME * 100), Duration.ZERO),
      InstantValue.from(Instant.ofEpochSecond(LOWEST_TIME + 1), Duration.ZERO),
      InstantValue.from(Instant.ofEpochSecond(LOWEST_TIME * 101), Duration.ZERO)
  };

  private static FeatureMeasurement<?> featureMeasurements[] = {
      FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.SLOWNESS,
          NumericMeasurementValue.from(
              Instant.EPOCH,
              DoubleValue.from(0.0, 0.0, Units.UNITLESS))),
      FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.ARRIVAL_TIME,
          arrivalTimes[1]),
      FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.ARRIVAL_TIME,
          arrivalTimes[2]),
      FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.ARRIVAL_TIME,
          arrivalTimes[3]),
      FeatureMeasurement.create(UUID.randomUUID(), FeatureMeasurementTypes.ARRIVAL_TIME,
          arrivalTimes[4])
  };

  private static FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurementP = FeatureMeasurement
      .create(
          UUID.randomUUID(), FeatureMeasurementTypes.PHASE,
          PhaseTypeMeasurementValue.from(PhaseType.P, 100.0));

  private static FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurementS = FeatureMeasurement
      .create(
          UUID.randomUUID(), FeatureMeasurementTypes.PHASE,
          PhaseTypeMeasurementValue.from(PhaseType.S, 100.0));

  private static List<ReferenceStation> stations = List.of(
      ReferenceStation.create(
          "station1",
          "station1",
          StationType.SeismicArray,
          InformationSource.create(
              "station1",
              Instant.now(),
              "station1"
          ),
          "station1",
          0.0,
          longitudes[0],
          0.0,
          Instant.now(),
          Instant.now(),
          List.of()
      ),
      ReferenceStation.create(
          "station2",
          "station2",
          StationType.SeismicArray,
          InformationSource.create(
              "station2",
              Instant.now(),
              "station2"
          ),
          "station2",
          0.0,
          longitudes[1],
          0.0,
          Instant.now(),
          Instant.now(),
          List.of()
      ),
      ReferenceStation.create(
          "station3",
          "station3",
          StationType.SeismicArray,
          InformationSource.create(
              "station3",
              Instant.now(),
              "station3"
          ),
          "station3",
          0.0,
          longitudes[2],
          0.0,
          Instant.now(),
          Instant.now(),
          List.of()
      )
  );

  private static List<ReferenceStation> fileredStations = List.of(
      ReferenceStation.create(
          "station1",
          "station1",
          StationType.SeismicArray,
          InformationSource.create(
              "station1",
              Instant.now(),
              "station1"
          ),
          "station1",
          0.0,
          longitudes[0],
          0.0,
          Instant.now(),
          Instant.now(),
          List.of()
      ),
      ReferenceStation.create(
          "station2",
          "station2",
          StationType.SeismicArray,
          InformationSource.create(
              "station2",
              Instant.now(),
              "station2"
          ),
          "station2",
          0.0,
          longitudes[1],
          0.0,
          Instant.now(),
          Instant.now(),
          List.of()
      ),
      ReferenceStation.create(
          "station3",
          "station3",
          StationType.SeismicArray,
          InformationSource.create(
              "station3",
              Instant.now(),
              "station3"
          ),
          "station3",
          0.0,
          71.2,
          0.0,
          Instant.now(),
          Instant.now(),
          List.of()
      ),
      ReferenceStation.create(
          "station3",
          "station3",
          StationType.SeismicArray,
          InformationSource.create(
              "station3",
              Instant.now(),
              "station3"
          ),
          "station3",
          0.0,
          longitudes[2],
          0.0,
          Instant.now(),
          Instant.now(),
          List.of()
      ),
      ReferenceStation.create(
          "station3",
          "station3",
          StationType.SeismicArray,
          InformationSource.create(
              "station3",
              Instant.now(),
              "station3"
          ),
          "station3",
          0.0,
          longitudes[3],
          0.0,
          Instant.now(),
          Instant.now(),
          List.of()
      ),
      ReferenceStation.create(
          "station3",
          "station3",
          StationType.SeismicArray,
          InformationSource.create(
              "station3",
              Instant.now(),
              "station3"
          ),
          "station3",
          0.0,
          longitudes[4],
          0.0,
          Instant.now(),
          Instant.now(),
          List.of()
      )
  );

  private SeedGenerator mockSeedGenerator = Mockito.mock(SeedGenerator.class);

  private LocatorAlgorithm<RealMatrix> mockAlgorithm = Mockito.mock(LocatorAlgorithm.class);

  private SignalFeaturePredictionUtility mockUtility = Mockito
      .mock(SignalFeaturePredictionUtility.class);

  class TestExpectedException extends Exception {

  }

  @BeforeEach
  public void initEach() {
    Mockito.reset(mockSeedGenerator);
    Mockito.reset(mockAlgorithm);
    Mockito.reset(mockUtility);
  }

  @Test
  public void testLocatePassesCorrections() throws Exception {
    SignalDetectionHypothesis[] hypotheses = {
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[0], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[0])
            .addMeasurement(phaseMeasurementS).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[1], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[1])
            .addMeasurement(phaseMeasurementP).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[2], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[2])
            .addMeasurement(phaseMeasurementS).build()
    };

    GeneralEventLocatorDelegate<RealMatrix> delegate = new GeneralEventLocatorDelegate<>();

    delegate.initialize(
        mockSeedGenerator,
        () -> mockAlgorithm,
        mockUtility
    );

    when(mockSeedGenerator.generate(any(), any())).thenReturn(EventLocation.from(
        90.0,
        90.0,
        0.0,
        Instant.MIN));

    //TODO: Return mock ellipses / errors
    when(mockAlgorithm.locate(any(), any(), any()))
        .thenAnswer(invocation -> {
          Function<RealVector, Pair<RowFilteredRealMatrix, RowFilteredRealMatrix>> fFunction = invocation
              .getArgument(2);

          //fFunction.apply(new ArrayRealVector(new double[]{0.0, 0.0, 0.0, 0.0}));

          return Triple.of(returnedVector, returnedCovarianceMatrix,
              new RowFilteredRealMatrix(returnedInfoMatrix));
        });

    List.of(true, false).forEach(truthValue -> {

      Mockito.reset(mockUtility);

      try {
        when(mockUtility.predict(
            any(),
            (EventLocation) any(),
            (Set) any(),
            any(),
            any(),
            any()
        )).thenAnswer(invocation -> {
          List<FeaturePredictionCorrection> corrections = invocation.getArgument(5);

          if (truthValue) {
            Assertions.assertEquals(2, corrections.size());

            Assertions.assertEquals(1, corrections.stream().filter(
                featurePredictionCorrection -> featurePredictionCorrection instanceof EllipticityCorrection1dDefinition)
                .count());

            Assertions.assertEquals(1, corrections.stream().filter(
                featurePredictionCorrection -> featurePredictionCorrection instanceof ElevationCorrection1dDefinition)
                .count());
          } else {
            Assertions.assertEquals(corrections.size(), 0);
          }

          //We dont care about anything else, throw this exception and be done with it.
          throw new TestExpectedException();

        });
      } catch (Exception e) {
        //throw new RuntimeException(e);
      }

      try {
        delegate
            .locate(Arrays.asList(hypotheses), stations,
                EventLocationDefinitionGeigers.create(
                    1000,
                    0.01,
                    0.95,
                    "ak135",
                    truthValue,
                    ScalingFactorType.CONFIDENCE,
                    0,
                    0.01,
                    0,
                    2,
                    true,
                    0.01,
                    10.0,
                    0.01,
                    0.01,
                    1.0e5,
                    0.1,
                    1.0,
                    0,
                    List.of(LocationRestraint.from(
                        RestraintType.UNRESTRAINED,
                        null,
                        RestraintType.UNRESTRAINED,
                        null,
                        DepthRestraintType.UNRESTRAINED.UNRESTRAINED,
                        null,
                        RestraintType.UNRESTRAINED,
                        null))
                ));
      } catch (RuntimeException | TooManyRestraintsException e) {

        //Absorb the TestExpectedException while also ensuring it was the exception thrown.
        Assertions.assertTrue(e.getCause() instanceof TestExpectedException);
      }
    });


  }

  @Test
  public void testSeedGeneratorAndAlgorithmCalled() throws Exception {
    SignalDetectionHypothesis[] hypotheses = {
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[0], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[0])
            .addMeasurement(phaseMeasurementS).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[1], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[1])
            .addMeasurement(phaseMeasurementP).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[2], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[2])
            .addMeasurement(phaseMeasurementS).build()
    };

    GeneralEventLocatorDelegate<RealMatrix> delegate = new GeneralEventLocatorDelegate<>();

    delegate.initialize(
        mockSeedGenerator,
        () -> mockAlgorithm,
        mockUtility
    );

    when(mockSeedGenerator.generate(any(), any())).thenReturn(EventLocation.from(
        90.0,
        90.0,
        0.0,
        Instant.MIN));

    //TODO: Return mock ellipses / errors
    when(mockAlgorithm.locate(any(), any(), any()))
        .thenAnswer(invocation -> {

          RealVector seed = invocation.getArgument(0);

          //Check seed values match location restraints
          Assertions.assertEquals(1.0, seed.getEntry(0));
          Assertions.assertEquals(2.0, seed.getEntry(1));
          Assertions.assertEquals(3.0, seed.getEntry(2));
          Assertions.assertEquals(0.0, seed.getEntry(3));

          //Ignore the seed values, just want to check that they were passed correctly
          //based on the location restraint.

          return Triple.of(returnedVector, returnedCovarianceMatrix,
              new RowFilteredRealMatrix(returnedInfoMatrix));
        });

    List<LocationSolution> locations = delegate
        .locate(Arrays.asList(hypotheses), stations,
            EventLocationDefinitionGeigers.create(
                1000,
                0.01,
                0.95,
                "ak135",
                false,
                ScalingFactorType.CONFIDENCE,
                0,
                0.01,
                0,
                2,
                true,
                0.01,
                10.0,
                0.01,
                0.01,
                1.0e5,
                0.1,
                1.0,
                0,
                List.of(
                    new LocationRestraint.Builder()
                        .setLatitudeRestraint(1.0)
                        .setLongitudeRestraint(2.0)
                        .setDepthRestraint(3.0)
                        .setTimeRestraint(Instant.EPOCH)
                        .build(),
                    new LocationRestraint.Builder()
                        .setLatitudeRestraint(1.0)
                        .setLongitudeRestraint(2.0)
                        .setDepthRestraint(3.0)
                        .setTimeRestraint(Instant.EPOCH)
                        .build()
                )
            ));

    verify(mockSeedGenerator, times(2)).generate(any(), any());

    verify(mockAlgorithm, times(2)).locate(any(), any(), any());

    Assertions.assertEquals(locations.size(), 2);

    locations.forEach(locationSolution -> {
      Assertions
          .assertEquals(returnedVector.getEntry(0),
              locationSolution.getLocation().getLatitudeDegrees());

      Assertions
          .assertEquals(returnedVector.getEntry(1),
              locationSolution.getLocation().getLongitudeDegrees());

      Assertions
          .assertEquals(returnedVector.getEntry(2), locations.get(0).getLocation().getDepthKm());

      //TODO: Sort through arrival time vs travel time
      Assertions.assertEquals(time, locations.get(0).getLocation().getTime());

      //int currentRow = 0;
      List<List<Double>> resultCovarianceMatrixList = locations.get(0).getLocationUncertainty()
          .get().getCovarianceMatrix();

      for (int i = 0; i < resultCovarianceMatrixList.size(); i++) {
        for (int j = 0; j < resultCovarianceMatrixList.get(i).size(); j++) {
          Assertions.assertTrue(resultCovarianceMatrixList.get(i).get(j).equals(
              returnedCovarianceMatrix.getEntry(i, j) * scalingFactor));
        }
      }

      Ellipse ellipse = locationSolution.getLocationUncertainty().get().getEllipses().stream()
          .findFirst().get();

      double semiMajorAxis = ellipse.getMajorAxisLength(); // / 2.0;
      double semiMinorAxis = ellipse.getMinorAxisLength(); // / 2.0;

      Assertions
          .assertEquals(3.0 * semiMinorAxis * semiMinorAxis, semiMajorAxis * semiMajorAxis, 10E-6);

      Assertions
          .assertEquals(ellipse.getMajorAxisTrend(), Math.toDegrees(Math.PI / 4.0) + 90.0, 10E-16);
      Assertions
          .assertEquals(ellipse.getMinorAxisTrend(), Math.toDegrees(Math.PI / 4.0), 10E-16);

      Assertions.assertEquals(Math.sqrt(returnedCovarianceMatrix.getEntry(2, 2) * scalingFactor),
          ellipse.getDepthUncertainty());
      Assertions.assertEquals(
          Math.sqrt(returnedCovarianceMatrix.getEntry(3, 3) * scalingFactor),
          (ellipse.getTimeUncertainty().getSeconds()
              + ellipse.getTimeUncertainty().getNano() / 1_000_000_000.0)
          , 10E-9);

      Assertions.assertEquals(confidence, ellipse.getConfidenceLevel());
    });

  }

  @Test
  public void testFFunction() throws Exception {

    LocatorAlgorithm<RealMatrix> mockAlgorithmWithBehavior = getMockAlgorithmWithBehavior();

    SignalFeaturePredictionUtility mockUtilityWithBehavior = getMockUtilityWithBehavior();

    //getMockUtilityWithBehavior() is basing how it builds the set of FPs on this array.
    SignalDetectionHypothesis[] hypotheses = {
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[0], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[0])
            .addMeasurement(phaseMeasurementS).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[1], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[1])
            .addMeasurement(phaseMeasurementP).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), UUID.randomUUID(), false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[2])
            .addMeasurement(phaseMeasurementP).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[2], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[2])
            .addMeasurement(phaseMeasurementS).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[3], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[3])
            .addMeasurement(phaseMeasurementP).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[4], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[4])
            .addMeasurement(phaseMeasurementP).build(),
    };

    when(mockSeedGenerator.generate(any(), any()))
        .thenReturn(EventLocation.from(0.0, 0.0, 0.0, Instant.EPOCH));

    GeneralEventLocatorDelegate<RealMatrix> delegate = new GeneralEventLocatorDelegate<>();

    delegate.initialize(
        mockSeedGenerator,
        () -> mockAlgorithmWithBehavior,
        mockUtilityWithBehavior
    );

    List<LocationSolution> locations = delegate
        .locate(Arrays.asList(hypotheses), fileredStations,
            EventLocationDefinitionGeigers.create(
                1000,
                0.01,
                0.95,
                "ak135",
                false,
                ScalingFactorType.CONFIDENCE,
                0,
                0.01,
                4,
                2,
                true,
                0.01,
                10.0,
                0.01,
                0.01,
                1.0e5,
                0.1,
                1.0,
                0,
                List.of(LocationRestraint.from(
                    RestraintType.UNRESTRAINED,
                    null,
                    RestraintType.UNRESTRAINED,
                    null,
                    DepthRestraintType.UNRESTRAINED,
                    null,
                    RestraintType.UNRESTRAINED,
                    null))
            ));

    Assertions
        .assertEquals(returnedFpValues.getRowDimension(),
            locations.get(0).getFeaturePredictions().size());

    Arrays.stream(returnedFpValues.getColumn(0))
        .forEach(
            value -> Assertions
                .assertEquals(1, locations.get(0).getFeaturePredictions().stream().filter(
                    fp -> Math.abs(getRawMeasurmentValue(fp).getValue() - value) <= 10E-16)
                    .count()));

    Arrays.stream(returnedFpValues.getColumn(1))
        .forEach(
            value -> Assertions
                .assertEquals(1, locations.get(0).getFeaturePredictions().stream().filter(
                    fp -> Math.abs(getRawMeasurmentValue(fp).getStandardDeviation() - value)
                        <= 10E-16)
                    .count()));

    Assertions.assertTrue(
        locations.get(0).getLocationBehaviors().stream()
            .map(LocationBehavior::getFeatureMeasurementId)
            .collect(Collectors.toList())
            .containsAll(Arrays.stream(featureMeasurements)
                .map(FeatureMeasurement::getId)
                .collect(Collectors.toList())));

    Assertions.assertTrue(
        locations.get(0).getLocationBehaviors().stream()
            .map(LocationBehavior::getFeaturePredictionId)
            .collect(Collectors.toList())
            .containsAll(locations.get(0).getFeaturePredictions().stream()
                .map(FeaturePrediction::getId)
                .collect(Collectors.toList())));

    Map<UUID, FeatureMeasurementType<?>> featureMeasurementMap = new HashMap<>();
    Map<UUID, FeatureMeasurementType<?>> featurePredictionMap = new HashMap<>();

    Arrays.stream(featureMeasurements).forEach(featureMeasurement -> featureMeasurementMap
        .put(featureMeasurement.getId(), featureMeasurement.getFeatureMeasurementType()));

    locations.get(0).getFeaturePredictions().forEach(featurePrediction -> featurePredictionMap
        .put(featurePrediction.getId(), featurePrediction.getPredictionType()));

    locations.get(0).getLocationBehaviors().stream().forEach(locationBehavior ->
        Assertions
            .assertEquals(featureMeasurementMap.get(locationBehavior.getFeatureMeasurementId()),
                featurePredictionMap.get(locationBehavior.getFeaturePredictionId()))
    );

    List<List<Double>> resultCovarianceMatrixList = locations.get(0).getLocationUncertainty()
        .get().getCovarianceMatrix();

    for (int i = 0; i < resultCovarianceMatrixList.size(); i++) {
      for (int j = 0; j < resultCovarianceMatrixList.get(i).size(); j++) {
        Assertions.assertEquals(filteredCovarianceMatrix.getEntry(i, j) * filteredScalingFactor,
            resultCovarianceMatrixList.get(i).get(j), 10E-9);
      }
    }
  }

  @Test
  public void testNotEnoughMeasurementsThrowIllegalArgumentException() {
    SignalDetectionHypothesis[] hypotheses = {
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[0], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[0])
            .addMeasurement(phaseMeasurementS).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[1], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[1])
            .addMeasurement(phaseMeasurementP).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[2], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[2])
            .addMeasurement(phaseMeasurementS).build()
    };

    GeneralEventLocatorDelegate<RealMatrix> delegate = new GeneralEventLocatorDelegate<>();

    delegate.initialize(
        mockSeedGenerator,
        () -> mockAlgorithm,
        mockUtility
    );

    when(mockSeedGenerator.generate(any(), any())).thenReturn(EventLocation.from(
        90.0,
        90.0,
        0.0,
        Instant.MIN));

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      List<LocationSolution> locations = delegate
          .locate(Arrays.asList(hypotheses), stations,
              EventLocationDefinitionGeigers.create(
                  1000,
                  0.01,
                  0.95,
                  "ak135",
                  false,
                  ScalingFactorType.CONFIDENCE,
                  4,
                  0.01,
                  4,
                  2,
                  true,
                  0.01,
                  10.0,
                  0.01,
                  0.01,
                  1.0e5,
                  0.1,
                  1.0,
                  0,
                  List.of(LocationRestraint.from(
                      RestraintType.UNRESTRAINED,
                      null,
                      RestraintType.UNRESTRAINED,
                      null,
                      DepthRestraintType.UNRESTRAINED.UNRESTRAINED,
                      null,
                      RestraintType.UNRESTRAINED,
                      null))
              ));
    });
  }

  /**
   * Create a mock algorithm that calls the closure returned by getFFunction
   */
  private LocatorAlgorithm<RealMatrix> getMockAlgorithmWithBehavior() {
    LocatorAlgorithm<RealMatrix> mockGeigersAlgorithm = Mockito.mock(LocatorAlgorithm.class);

    when(mockGeigersAlgorithm.locate(any(), any(), any())).thenAnswer(
        invocation -> {
          Function<RealVector, Pair<RealMatrix, RealMatrix>> predictionFunction = invocation
              .getArgument(2);

          Pair<RealMatrix, RealMatrix> result = null;

          //Call the F function twice to check that only the final set of feature predictions is
          //counted
          for (int i = 0; i < 2; i++) {
            result = predictionFunction.apply(
                new ArrayRealVector(returnedVector)
            );
          }

          final RealMatrix fpMatrix = result.getFirst();

          //Ensure return value is some permutation of our fp values, minus NaN rows.
          //(Permutation will be different because the signal detections in testFFunction
          //are not grouped by phase)
          IntStream.range(0, returnedFilteredFpValues.getRowDimension())
              .mapToObj(i -> returnedFilteredFpValues.getRowVector(i))
              .forEach(realVector -> {
                if (!Double.isNaN(realVector.getEntry(0)) && !Double
                    .isNaN(realVector.getEntry(1))) {
                  Assertions.assertTrue(IntStream.range(0, fpMatrix.getRowDimension())
                      .mapToObj(fpMatrix::getRowVector)
                      .anyMatch(row -> row.equals(realVector)));
                }
              });

          return Triple.of(
              returnedVector,
              filteredCovarianceMatrix,
              TestFixtures.replaceWithZeroFilter.apply(returnedFilteredInfoMatrix));
        });

    when(mockGeigersAlgorithm.getErrorValueNaNProcessor())
        .thenReturn(TestFixtures.replaceWithZeroFilter);
    when(mockGeigersAlgorithm.getJacobianNaNProcessor())
        .thenReturn(TestFixtures.replaceWithZeroFilter);

    return mockGeigersAlgorithm;
  }

  private static <T, V extends FeatureMeasurementType<T>> FeaturePrediction<T> createFeaturePredictionHelper(
      V type,
      PhaseType phase,
      Optional<T> optionalValue,
      EventLocation sourceLocation,
      Location receiverLocation,
      Map<FeaturePredictionDerivativeType, DoubleValue> derivativeTypeDoubleValueMap) {
    return FeaturePrediction.create(
        phase,
        optionalValue,
        Set.of(),
        false,
        type,
        sourceLocation,
        receiverLocation,
        Optional.of(UUID.randomUUID()),
        derivativeTypeDoubleValueMap
    );
  }

  private static Duration durationFromDouble(double fractionalSeconds) {
    return Duration.ofNanos((long) (fractionalSeconds * 1_000_000_000));
  }

  private static DoubleValue getRawMeasurmentValue(FeaturePrediction<?> featurePrediction) {
    FeatureMeasurementType<?> type = featurePrediction.getPredictionType();

    if (type instanceof NumericMeasurementType) {
      return ((NumericMeasurementValue) featurePrediction.getPredictedValue()
          .orElseThrow(AssertionError::new)).getMeasurementValue();
    } else if (type instanceof InstantMeasurementType) {
      InstantValue instantValue = ((InstantValue) featurePrediction.getPredictedValue()
          .orElseThrow(AssertionError::new));
      Instant instant = instantValue.getValue();
      return DoubleValue.from(
          instant.getEpochSecond() + (double) instant.getNano() / 1_000_000_000.0,
          (double) instantValue.getStandardDeviation().toNanos() / 1_000_000_000.0,
          Units.SECONDS
      );
    } else {
      return null;
    }
  }

  /**
   * Return a version of SignalFeaturePredictionUtility that returns values based on private finals
   * Should test itself (see comment before assertion below) that returned fp types match the passed
   * in list of feature measurement types.
   */
  private SignalFeaturePredictionUtility getMockUtilityWithBehavior() {
    return new SignalFeaturePredictionUtility() {

      @Override
      public List<FeaturePrediction<?>> predict(
          List<FeatureMeasurementType<?>> types,
          EventLocation sourceLocation,
          List<Location> receiverLocations,
          PhaseType phase,
          String model,
          List<FeaturePredictionCorrection> correctionDefinitions) throws Exception {

        Assertions.assertEquals(returnedVector.getEntry(0), sourceLocation.getLatitudeDegrees());
        Assertions.assertEquals(returnedVector.getEntry(1), sourceLocation.getLongitudeDegrees());
        Assertions.assertEquals(returnedVector.getEntry(2), sourceLocation.getDepthKm());
        Assertions
            .assertEquals(returnedVector.getEntry(3), sourceLocation.getTime().getEpochSecond()
                + sourceLocation.getTime().getNano() / 1_000_000_000.0);

        final RealMatrix fpValues; // = new Array2DRowRealMatrix();
        final RealMatrix dMatrix;
        List<ReferenceStation> stations;

        //Pick out values based on phase; these match the list of signal detectionsin the testFFunction
        //test. Maybe allow the set of selected indices per phase to be passed in?
        if (phase.equals(PhaseType.S)) {
          fpValues = returnedFilteredFpValues.getSubMatrix(new int[]{0, 3}, new int[]{0, 1});
          dMatrix = derivativeMatrix.getSubMatrix(new int[]{0, 3}, new int[]{0, 1, 2, 3});
          stations = List.of(fileredStations.get(0), fileredStations.get(3));
        } else {
          fpValues = returnedFilteredFpValues.getSubMatrix(new int[]{1, 2, 4, 5}, new int[]{0, 1});
          dMatrix = derivativeMatrix.getSubMatrix(new int[]{1, 2, 4, 5}, new int[]{0, 1, 2, 3});
          stations = List.of(fileredStations.get(1), fileredStations.get(2), fileredStations.get(4),
              fileredStations.get(5));
        }

        List<FeaturePrediction<?>> featurePredictions =
            //zip(Arrays.stream(returnedFilteredFpValues.getData()), fileredStations.stream(),
            zip(IntStream.range(0, fpValues.getRowDimension()).boxed(),
                stations.stream(),
                (row, station) -> {
                  Map<FeaturePredictionDerivativeType, DoubleValue> derivativeTypeDoubleValueMap = Map
                      .of(
                          FeaturePredictionDerivativeType.D_DX,
                          DoubleValue.from(dMatrix.getRow(row)[0], 0.0, Units.UNITLESS),
                          FeaturePredictionDerivativeType.D_DY,
                          DoubleValue.from(dMatrix.getRow(row)[1], 0.0, Units.UNITLESS),
                          FeaturePredictionDerivativeType.D_DZ,
                          DoubleValue.from(dMatrix.getRow(row)[2], 0.0, Units.UNITLESS),
                          FeaturePredictionDerivativeType.D_DT,
                          DoubleValue.from(dMatrix.getRow(row)[3], 0.0, Units.UNITLESS)
                      );

                  EventLocation eventLocation = EventLocation.from(0.0, 0.0, 0.0, Instant.EPOCH);

                  Location receiverLocation = Location.from(
                      station.getLatitude(),
                      station.getLongitude(),
                      0.0,
                      station.getElevation());

                  //Return an FP with an Optional.empty value if our contrived matrix has NaNs
                  if (Double.isNaN(fpValues.getEntry(row, 0)) || Double
                      .isNaN(fpValues.getEntry(row, 1))) {
                    return createFeaturePredictionHelper(
                        FeatureMeasurementTypes.ARRIVAL_TIME,
                        phase,
                        Optional.empty(),
                        eventLocation,
                        receiverLocation,
                        derivativeTypeDoubleValueMap
                    );
                  }
                  //Return SLOWNESS if phase type is S (again based on testFFunction) and this is
                  // the first S measurement
                  else if (row == 0 && phase.equals(PhaseType.S)) {
                    return createFeaturePredictionHelper(
                        FeatureMeasurementTypes.SLOWNESS,
                        phase,
                        Optional.of(NumericMeasurementValue.from(Instant.now(),
                            DoubleValue.from(fpValues.getEntry(row, 0),
                                fpValues.getEntry(row, 1), Units.UNITLESS))),
                        eventLocation,
                        receiverLocation,
                        derivativeTypeDoubleValueMap
                    );
                  }
                  //Otherwise return an arrival time.
                  else {
                    return createFeaturePredictionHelper(
                        FeatureMeasurementTypes.ARRIVAL_TIME,
                        phase,
                        Optional.of(InstantValue.from(
                            Instant.EPOCH.plus(
                                durationFromDouble(fpValues.getEntry(row, 0))),
                            durationFromDouble(fpValues.getEntry(row, 1)))
                        ),
                        eventLocation,
                        receiverLocation,
                        derivativeTypeDoubleValueMap
                    );
                  }
                }).collect(Collectors.toList());

        //Ensure our prediction types match the list of passed in measurement types
        return Streams.zip(types.stream(), featurePredictions.stream(),
            (t, f) -> {
              Assertions.assertEquals(t, f.getPredictionType());
              return f;
            }
        ).collect(Collectors.toList());
      }
    };
  }

  @Test
  public void testRestrainedDepth() throws Exception {
    SignalDetectionHypothesis[] hypotheses = {
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[0], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[0])
            .addMeasurement(phaseMeasurementS).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[1], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[1])
            .addMeasurement(phaseMeasurementP).build(),
        SignalDetectionHypothesis
            .builder(UUID.randomUUID(), signalDetectionUUIDs[2], false, UUID.randomUUID())
            .addMeasurement(featureMeasurements[2])
            .addMeasurement(phaseMeasurementS).build()
    };

    LocatorAlgorithm<RealMatrix> mockAlgorithm = Mockito.mock(LocatorAlgorithm.class);

    when(mockAlgorithm.locate(any(), any(), any())).thenAnswer(
        invocation -> {
          RealVector newVector = new ArrayRealVector(returnedRestrainedVector);
          newVector.setEntry(0, 100.1);
          newVector.setEntry(1, 100.2);
          newVector.setEntry(3, 100.3);
          return Triple.of(newVector, returnedRestrainedCovarianceMatrix, returnedInfoMatrix);
        });

    when(mockSeedGenerator.generate(any(), any())).thenReturn(EventLocation.from(
        returnedRestrainedVector.getEntry(0),
        returnedRestrainedVector.getEntry(1),
        returnedRestrainedVector.getEntry(2),
        Instant.ofEpochMilli((int) returnedRestrainedVector.getEntry(3))));

    GeneralEventLocatorDelegate<RealMatrix> delegate = new GeneralEventLocatorDelegate<>();

    delegate.initialize(
        mockSeedGenerator,
        () -> mockAlgorithm,
        mockUtility
    );

    List<LocationSolution> locations = delegate
        .locate(Arrays.asList(hypotheses), stations,
            EventLocationDefinitionGeigers.create(
                1000,
                0.01,
                0.95,
                "ak135",
                false,
                ScalingFactorType.CONFIDENCE,
                0,
                1.0,
                0,
                2,
                true,
                0.01,
                10.0,
                0.01,
                0.01,
                1.0e5,
                0.1,
                1.0,
                0,
                List.of(LocationRestraint.from(
                    RestraintType.UNRESTRAINED,
                    null,
                    RestraintType.UNRESTRAINED,
                    null,
                    DepthRestraintType.FIXED_AT_DEPTH,
                    returnedRestrainedVector.getEntry(2),
                    RestraintType.UNRESTRAINED,
                    null
                ))
            ));

    List<List<Double>> covarianceMatrix = locations.get(0).getLocationUncertainty().get()
        .getCovarianceMatrix();

    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        Assertions.assertEquals(returnedRecoveredRestrainedCovarianceMatrix.getEntry(i, j),
            covarianceMatrix.get(j).get(i).doubleValue());
      }
    }

    Assertions.assertEquals(
        returnedRestrainedVector.getEntry(2),
        locations.get(0).getLocation().getDepthKm());

    Assertions.assertNotEquals(
        returnedRestrainedVector.getEntry(0),
        locations.get(0).getLocation().getLatitudeDegrees());

    Assertions.assertNotEquals(
        returnedRestrainedVector.getEntry(1),
        locations.get(0).getLocation().getLongitudeDegrees());

    Assertions.assertNotEquals(
        returnedRestrainedVector.getEntry(3),
        locations.get(0).getLocation().getTime().toEpochMilli());
  }

  @Test
  public void testFillInCovarianceMatrix() {
    List.of(
        Triple.of(
            new int[]{1, 2, 3},
            new Array2DRowRealMatrix(new double[][]{
                {1},
            }),
            new Array2DRowRealMatrix(new double[][]{
                {1, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
            })),

        Triple.of(
            new int[]{0, 2, 3},
            new Array2DRowRealMatrix(new double[][]{
                {1},
            }),
            new Array2DRowRealMatrix(new double[][]{
                {0, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
            })),

        Triple.of(
            new int[]{0, 1, 3},
            new Array2DRowRealMatrix(new double[][]{
                {1},
            }),
            new Array2DRowRealMatrix(new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 0}
            })),

        Triple.of(
            new int[]{1, 3},
            new Array2DRowRealMatrix(new double[][]{
                {1, 2},
                {3, 4}
            }),
            new Array2DRowRealMatrix(new double[][]{
                {1, 0, 2, 0},
                {0, 0, 0, 0},
                {3, 0, 4, 0},
                {0, 0, 0, 0}
            })),

        Triple.of(
            new int[]{0, 3},
            new Array2DRowRealMatrix(new double[][]{
                {1, 2},
                {3, 4}
            }),
            new Array2DRowRealMatrix(new double[][]{
                {0, 0, 0, 0},
                {0, 1, 2, 0},
                {0, 3, 4, 0},
                {0, 0, 0, 0}
            })),

        Triple.of(
            new int[]{1, 2},
            new Array2DRowRealMatrix(new double[][]{
                {1, 2},
                {3, 4}
            }),
            new Array2DRowRealMatrix(new double[][]{
                {1, 0, 0, 2},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {3, 0, 0, 4}
            })),

        Triple.of(
            new int[]{0},
            new Array2DRowRealMatrix(new double[][]{
                {1, 2, 5},
                {3, 4, 6},
                {7, 8, 9}
            }),
            new Array2DRowRealMatrix(new double[][]{
                {0, 0, 0, 0},
                {0, 1, 2, 5},
                {0, 3, 4, 6},
                {0, 7, 8, 9}
            })),

        Triple.of(
            new int[]{1},
            new Array2DRowRealMatrix(new double[][]{
                {1, 2, 5},
                {3, 4, 6},
                {7, 8, 9}
            }),
            new Array2DRowRealMatrix(new double[][]{
                {1, 0, 2, 5},
                {0, 0, 0, 0},
                {3, 0, 4, 6},
                {7, 0, 8, 9}
            })),

        Triple.of(
            new int[]{2},
            new Array2DRowRealMatrix(new double[][]{
                {1, 2, 5},
                {3, 4, 6},
                {7, 8, 9}
            }),
            new Array2DRowRealMatrix(new double[][]{
                {1, 2, 0, 5},
                {3, 4, 0, 6},
                {0, 0, 0, 0},
                {7, 8, 0, 9}
            })),

        Triple.of(
            new int[]{3},
            new Array2DRowRealMatrix(new double[][]{
                {1, 2, 5},
                {3, 4, 6},
                {7, 8, 9}
            }),
            new Array2DRowRealMatrix(new double[][]{
                {1, 2, 5, 0},
                {3, 4, 6, 0},
                {7, 8, 9, 0},
                {0, 0, 0, 0}
            }))
    ).forEach(triple -> {
          BitSet exclusionBits = new BitSet(4);

          for (int i = 0; i < 4; i++) {
            exclusionBits.set(i, false);
          }

          Arrays.stream(triple.getLeft()).forEach(index -> exclusionBits.set(index));

          Assertions.assertEquals(triple.getRight(),
              GeneralEventLocatorDelegate.fillCovarianceMatrix(triple.getMiddle(), exclusionBits));
        }
    );
  }
}
