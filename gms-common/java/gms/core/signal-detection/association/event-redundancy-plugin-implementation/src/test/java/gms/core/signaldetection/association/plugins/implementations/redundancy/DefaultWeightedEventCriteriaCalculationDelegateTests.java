package gms.core.signaldetection.association.plugins.implementations.redundancy;

import gms.core.signaldetection.association.CandidateEvent;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationDefinition;
import gms.core.signaldetection.association.plugins.implementations.redundancy.utility.TestData.WeightedCriterionData;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.DoubleValue;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.Units;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetectionHypothesis;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

public class DefaultWeightedEventCriteriaCalculationDelegateTests {

  //TODO: Three component and secondary phase constants will be added in the future.
  //   (NOTE: Ensure they are normalized also)

  private static final double TIME_WEIGHT_BASE = 0.5;

  private static final double AZIMUTH_WEIGHT_BASE = 0.26;

  private static final double SLOWNESS_WEIGHT_BASE = 0.24;

  @BeforeAll
  static void initializeAndInitialSanityCheck() {

    //SANITY CHECK ON TEST
    // We want the above constants to add to 1.0 for a consistent value to check against any
    // combination of measurement types. The values in the definition will be normalized to always
    // add to 1.0.
    Assertions.assertEquals(1.0, TIME_WEIGHT_BASE + AZIMUTH_WEIGHT_BASE + SLOWNESS_WEIGHT_BASE);

  }

  //
  // Test the delegate with specific counts of specific types of feature measurements
  //   counts[0] -> ARRIVAL TIME
  //   counts[1] -> AZIMUTH
  //   counts[2] -> SLOWNESSS
  //
  // The set of signal detection hypotheses is passed in instead of constructed because the
  // measurements can be distributed amongst the SDH is different ways; it is up to the caller
  // to determine that distribution based on the goal of the test.
  //
  @ParameterizedTest
  @MethodSource("delegateTestsProvider")
  void testDelegateWithMeasurementCounts(
      int[] counts,
      Set<SignalDetectionHypothesis> signalDetectionHypotheses) {

    CandidateEvent candidateEvent = Mockito.mock(CandidateEvent.class);

    Mockito.when(candidateEvent.getCorroboratingSet()).thenReturn(signalDetectionHypotheses);

    DefaultWeightedEventCriteriaCalculationDelegate delegate = new DefaultWeightedEventCriteriaCalculationDelegate();

    DoubleValue saneValue = delegate.calculate(candidateEvent,
        WeightedEventCriteriaCalculationDefinition.create(
            TIME_WEIGHT_BASE / counts[0],
            Double.NaN,
            AZIMUTH_WEIGHT_BASE / counts[1],
            Double.NaN,
            SLOWNESS_WEIGHT_BASE / counts[2],
            Double.NaN,
            Double.NaN
        ));

    Assertions.assertEquals(1.0, saneValue.getValue());

    Assertions.assertEquals(saneValue.getStandardDeviation(), 0.0);
    Assertions.assertEquals(saneValue.getUnits(), Units.UNITLESS);
  }

  //
  // Sanity check.
  //
  @ParameterizedTest
  @MethodSource("delegateTestsProvider")
  void testDelegateWithMeasurementCountsSanityCheckNotOne(
      int[] counts,
      Set<SignalDetectionHypothesis> signalDetectionHypotheses) {

    CandidateEvent candidateEvent = Mockito.mock(CandidateEvent.class);

    Mockito.when(candidateEvent.getCorroboratingSet()).thenReturn(signalDetectionHypotheses);

    DefaultWeightedEventCriteriaCalculationDelegate delegate = new DefaultWeightedEventCriteriaCalculationDelegate();

    //SANITY CHECK 1
    DoubleValue notSaneValue = delegate.calculate(candidateEvent,
        WeightedEventCriteriaCalculationDefinition.create(
            1 + TIME_WEIGHT_BASE / counts[0],
            Double.NaN,
            1 + AZIMUTH_WEIGHT_BASE / counts[1],
            Double.NaN,
            1 + SLOWNESS_WEIGHT_BASE / counts[2],
            Double.NaN,
            Double.NaN
        ));

    Assertions.assertNotEquals(1.0, notSaneValue.getValue());
  }

  //
  // Sanity check. Designed to fail if the delegate isnt actually doing anything.
  //
  @ParameterizedTest
  @MethodSource("delegateTestsProvider")
  void testDelegateWithMeasurementCountsSanityCheckNaN(
      int[] counts,
      Set<SignalDetectionHypothesis> signalDetectionHypotheses) {

    CandidateEvent candidateEvent = Mockito.mock(CandidateEvent.class);

    Mockito.when(candidateEvent.getCorroboratingSet()).thenReturn(signalDetectionHypotheses);

    DefaultWeightedEventCriteriaCalculationDelegate delegate = new DefaultWeightedEventCriteriaCalculationDelegate();

    //SANITY CHECK 2
    DoubleValue notSaneValue = delegate.calculate(candidateEvent,
        WeightedEventCriteriaCalculationDefinition.create(
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            Double.NaN
        ));

    Assertions.assertTrue(Double.isNaN(notSaneValue.getValue()));
  }

  static Stream<Arguments> delegateTestsProvider() {
    return Stream.of(
        Arguments.arguments(new int[]{1, 1, 1},
            WeightedCriterionData.multipleSdhsWithSingleMeasurment),

        Arguments.arguments(new int[]{1, 1, 1},
            WeightedCriterionData.twoSdhsThreeMeasurements),

        Arguments.arguments(new int[]{1, 2, 1},
            WeightedCriterionData.twoSdhsFourMeasurements),

        Arguments.arguments(new int[]{1, 2, 1},
            WeightedCriterionData.twoSdhsFourRelevantMeasurementsOneIrrelevant)
    );
  }
}
