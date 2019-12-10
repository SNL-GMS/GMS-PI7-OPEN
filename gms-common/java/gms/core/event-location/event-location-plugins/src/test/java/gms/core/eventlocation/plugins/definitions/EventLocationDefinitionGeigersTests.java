package gms.core.eventlocation.plugins.definitions;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EventLocationDefinitionGeigersTests {

  private static final String EARTH_MODEL = "ak135";

  @Test
  void testCreate() {

    List<LocationRestraint> restraintIn = List.of(LocationRestraint.from(
        RestraintType.UNRESTRAINED,
        null,
        RestraintType.UNRESTRAINED,
        null,
        DepthRestraintType.UNRESTRAINED.UNRESTRAINED,
        null,
        RestraintType.UNRESTRAINED,
        null));

    List<LocationRestraint> restraintReference = List.of(LocationRestraint.from(
        RestraintType.UNRESTRAINED,
        null,
        RestraintType.UNRESTRAINED,
        null,
        DepthRestraintType.UNRESTRAINED.UNRESTRAINED,
        null,
        RestraintType.UNRESTRAINED,
        null));

    EventLocationDefinitionGeigers definition = EventLocationDefinitionGeigers.create(
        1,
        2.3,
        6.7,
        EARTH_MODEL,
        true,
        ScalingFactorType.CONFIDENCE,
        6,
        1.0,
        4,
        2,
        true,
        0.1,
        10.0,
        0.0001,
        0.01,
        1.0e5,
        4.5,
        0.1,
        8,
        restraintIn
    );

    Assertions.assertAll(
        () -> Assertions.assertEquals(1, definition.getMaximumIterationCount()),
        () -> Assertions.assertEquals(2.3, definition.getConvergenceThreshold()),
        () -> Assertions.assertEquals(6.7, definition.getUncertaintyProbabilityPercentile()),
        () -> Assertions.assertEquals(EARTH_MODEL, definition.getEarthModel()),
        () -> Assertions.assertTrue(definition.isApplyTravelTimeCorrections()),
        () -> Assertions
            .assertEquals(ScalingFactorType.CONFIDENCE, definition.getScalingFactorType()),
        () -> Assertions.assertEquals(6, definition.getkWeight()),
        () -> Assertions.assertEquals(1.0, definition.getAprioriVariance()),
        () -> Assertions.assertEquals(4, definition.getMinimumNumberOfObservations()),
        () -> Assertions.assertEquals(2, definition.getConvergenceCount()),
        () -> Assertions.assertTrue(definition.isLevenbergMarquardtEnabled()),
        () -> Assertions.assertEquals(0.1, definition.getLambda0()),
        () -> Assertions.assertEquals(10.0, definition.getLambdaX()),
        () -> Assertions.assertEquals(0.0001, definition.getDeltaNormThreshold()),
        () -> Assertions.assertEquals(0.01, definition.getSingularValueWFactor()),
        () -> Assertions.assertEquals(1.0e5, definition.getMaximumWeightedPartialDerivative()),
        () -> Assertions.assertEquals(4.5, definition.getDampingFactorStep()),
        () -> Assertions.assertEquals(0.1, definition.getDeltamThreshold()),
        () -> Assertions.assertEquals(8, definition.getDepthFixedIterationCount()),
        () -> Assertions.assertEquals(restraintReference, definition.getLocationRestraints())
    );
  }

  @Test
  void testCreateNullEarthModel() {

    Throwable exception = Assertions.assertThrows(NullPointerException.class, () ->
        EventLocationDefinitionGeigers.create(
            1,
            0.01,
            0.95,
            null,
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
        )
    );

    Assertions.assertEquals("Null earthModel", exception.getMessage());
  }

  @Test
  void testEquals() {

    EventLocationDefinitionGeigers definition = EventLocationDefinitionGeigers.create(
        1,
        2.3,
        6.7,
        EARTH_MODEL,
        true,
        ScalingFactorType.CONFIDENCE,
        6,
        1.0,
        4,
        2,
        true,
        0.1,
        10.0,
        0.0001,
        0.01,
        1.0e5,
        4.5,
        0.1,
        8,
        List.of(LocationRestraint.from(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.UNRESTRAINED.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null))
    );

    EventLocationDefinitionGeigers definition2 = EventLocationDefinitionGeigers.create(
        1,
        2.3,
        6.7,
        EARTH_MODEL,
        true,
        ScalingFactorType.CONFIDENCE,
        6,
        1.0,
        4,
        2,
        true,
        0.1,
        10.0,
        0.0001,
        0.01,
        1.0e5,
        4.5,
        0.1,
        8,
        List.of(LocationRestraint.from(
            RestraintType.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null,
            DepthRestraintType.UNRESTRAINED.UNRESTRAINED,
            null,
            RestraintType.UNRESTRAINED,
            null))
    );

    Assertions.assertEquals(definition, definition2);
    Assertions.assertEquals(definition2, definition);
  }

  @ParameterizedTest
  @MethodSource("testEqualsProvider")
  void testNotEquals(
      int maximumIterations,
      double convergenceThreshold,
      double uncertaintyProbabilityPercentile,
      String earthModel,
      boolean applyTravelTimeCorrections,
      ScalingFactorType scalingFactorType,
      int kWeight,
      double aprioriVariance,
      int minimumNumberOfObservations,
      int convergenceCount,
      boolean levenbergMarquardtEnabled,
      double lambda0,
      double lambdaX,
      double deltaNormThreshold,
      double singularValueWThreshold,
      double maximumWeightedPartialDerivative,
      double dampingFactorStep,
      double deltamThreshold,
      int depthFixedIterationCount,
      List<LocationRestraint> locationRestraints
  ) {

    EventLocationDefinitionGeigers definition = EventLocationDefinitionGeigers.create(
        1,
        2.3,
        6.7,
        EARTH_MODEL,
        true,
        ScalingFactorType.CONFIDENCE,
        6,
        1.0,
        4,
        2,
        true,
        0.1,
        10.0,
        0.0001,
        0.01,
        1.0e5,
        4.5,
        0.1,
        8,
        List.of(LocationRestraint.from(
            RestraintType.UNRESTRAINED,
            0.0,
            RestraintType.UNRESTRAINED,
            0.0,
            DepthRestraintType.UNRESTRAINED.UNRESTRAINED,
            0.0,
            RestraintType.UNRESTRAINED,
            Instant.EPOCH))
    );

    EventLocationDefinitionGeigers definition2 = EventLocationDefinitionGeigers.create(
        maximumIterations,
        convergenceThreshold,
        uncertaintyProbabilityPercentile,
        earthModel,
        applyTravelTimeCorrections,
        scalingFactorType,
        kWeight,
        aprioriVariance,
        minimumNumberOfObservations,
        convergenceCount,
        levenbergMarquardtEnabled,
        lambda0,
        lambdaX,
        deltaNormThreshold,
        singularValueWThreshold,
        maximumWeightedPartialDerivative,
        dampingFactorStep,
        deltamThreshold,
        depthFixedIterationCount,
        locationRestraints
    );

    Assertions.assertNotEquals(definition, definition2);
    Assertions.assertNotEquals(definition2, definition);
  }


  // Argument provider for parameterized test method: testNotEquals()
  static Stream<Arguments> testEqualsProvider() {

    List<LocationRestraint> correctLocationRestraint = List.of(LocationRestraint.from(
        RestraintType.UNRESTRAINED,
        0.0,
        RestraintType.UNRESTRAINED,
        0.0,
        DepthRestraintType.UNRESTRAINED.UNRESTRAINED,
        0.0,
        RestraintType.UNRESTRAINED,
        Instant.EPOCH));

    List<LocationRestraint> wrongLocationRestraint = List.of(LocationRestraint.from(
        RestraintType.FIXED,
        0.0,
        RestraintType.FIXED,
        0.0,
        DepthRestraintType.FIXED_AT_DEPTH,
        0.0,
        RestraintType.UNRESTRAINED,
        Instant.MAX));

    return Stream.of(
        Arguments
            .arguments(0, 2.3, 6.7, EARTH_MODEL, true, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.2, 6.7, EARTH_MODEL, true, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.8, EARTH_MODEL, true, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, "WRONG    ", true, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, true, ScalingFactorType.COVERAGE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 7, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.5, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.0, 8, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 3,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                false, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.2, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 15.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0002, 0.01, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.03, 1.0e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.1e5, 4.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 5.5, 0.1, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.2, 8, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, false, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 9, correctLocationRestraint),
        Arguments
            .arguments(1, 2.3, 6.7, EARTH_MODEL, true, ScalingFactorType.CONFIDENCE, 6, 1.0, 4, 2,
                true, 0.1, 10.0, 0.0001, 0.01, 1.0e5, 4.5, 0.1, 9, wrongLocationRestraint)
    );
  }
}
