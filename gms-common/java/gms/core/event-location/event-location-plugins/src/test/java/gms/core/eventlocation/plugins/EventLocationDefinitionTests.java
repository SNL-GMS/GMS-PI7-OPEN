package gms.core.eventlocation.plugins;

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

class EventLocationDefinitionTests {

  @Test
  void testCreate() {

    List<LocationRestraint> correctLocationRestraint = List.of(LocationRestraint.from(
        RestraintType.UNRESTRAINED,
        0.0,
        RestraintType.UNRESTRAINED,
        0.0,
        DepthRestraintType.UNRESTRAINED,
        0.0,
        RestraintType.UNRESTRAINED,
        Instant.EPOCH));

    EventLocationDefinition eventLocationDefinition = EventLocationDefinition.create(
        100,
        0.01,
        12.34,
        "TESTMODEL",
        false,
        ScalingFactorType.CONFIDENCE,
        4,
        0.3,
        4,
        correctLocationRestraint);

    Assertions.assertEquals(100, eventLocationDefinition.getMaximumIterationCount());
    Assertions.assertEquals(0.01, eventLocationDefinition.getConvergenceThreshold());
    Assertions.assertEquals(12.34, eventLocationDefinition.getUncertaintyProbabilityPercentile());
    Assertions.assertEquals("TESTMODEL", eventLocationDefinition.getEarthModel());
    Assertions.assertEquals(false, eventLocationDefinition.isApplyTravelTimeCorrections());
    Assertions
        .assertEquals(ScalingFactorType.CONFIDENCE, eventLocationDefinition.getScalingFactorType());
    Assertions.assertEquals(4, eventLocationDefinition.getkWeight());
    Assertions.assertEquals(0.3, eventLocationDefinition.getAprioriVariance());
    Assertions.assertEquals(4, eventLocationDefinition.getMinimumNumberOfObservations());
    Assertions
        .assertEquals(correctLocationRestraint, eventLocationDefinition.getLocationRestraints());
  }

  @Test
  void testEquals() {

    EventLocationDefinition eventLocationDefinition = EventLocationDefinition.create(
        100,
        0.01,
        12.34,
        "TESTMODEL",
        false,
        ScalingFactorType.CONFIDENCE,
        4,
        0.3,
        4,
        List.of(LocationRestraint.from(
            RestraintType.UNRESTRAINED,
            0.0,
            RestraintType.UNRESTRAINED,
            0.0,
            DepthRestraintType.UNRESTRAINED,
            0.0,
            RestraintType.UNRESTRAINED,
            Instant.EPOCH)));

    EventLocationDefinition eventLocationDefinition2 = EventLocationDefinition.create(
        100,
        0.01,
        12.34,
        "TESTMODEL",
        false,
        ScalingFactorType.CONFIDENCE,
        4,
        0.3,
        4,
        List.of(LocationRestraint.from(
            RestraintType.UNRESTRAINED,
            0.0,
            RestraintType.UNRESTRAINED,
            0.0,
            DepthRestraintType.UNRESTRAINED,
            0.0,
            RestraintType.UNRESTRAINED,
            Instant.EPOCH)));

    Assertions.assertEquals(eventLocationDefinition, eventLocationDefinition2);
    Assertions.assertEquals(eventLocationDefinition2, eventLocationDefinition);
  }

  // This is parameterized because there will likely be more than one field in EventLocationDefinition
  //  in the future
  @ParameterizedTest
  @MethodSource("testNotEqualsProvider")
  void testNotEquals(
      int maximumIterations,
      double convergenceTolorance,
      double uncertaintyProbabilityPercentile,
      String earthModel,
      boolean applyTravelTimeCorrections,
      ScalingFactorType scalingFactorType,
      int kWeight,
      double aprioriVariance,
      int minimumNumberOfObservations,
      List<LocationRestraint> locationRestraint) {

    EventLocationDefinition eventLocationDefinition = EventLocationDefinition.create(
        100,
        0.01,
        12.34,
        "TESTMODEL",
        false,
        ScalingFactorType.CONFIDENCE,
        4,
        0.3,
        4,
        List.of(LocationRestraint.from(
            RestraintType.UNRESTRAINED,
            0.0,
            RestraintType.UNRESTRAINED,
            0.0,
            DepthRestraintType.UNRESTRAINED,
            0.0,
            RestraintType.UNRESTRAINED,
            Instant.EPOCH)));

    EventLocationDefinition eventLocationDefinition2 = EventLocationDefinition.create(
        maximumIterations,
        convergenceTolorance,
        uncertaintyProbabilityPercentile,
        earthModel,
        applyTravelTimeCorrections,
        scalingFactorType,
        kWeight,
        aprioriVariance,
        minimumNumberOfObservations,
        locationRestraint);

    Assertions.assertNotEquals(eventLocationDefinition, eventLocationDefinition2);
    Assertions.assertNotEquals(eventLocationDefinition2, eventLocationDefinition);
  }


  // Argument provider for parameterized test method: testNotEquals()
  static Stream<Arguments> testNotEqualsProvider() {

    List<LocationRestraint> correctLocationRestraint = List.of(LocationRestraint.from(
        RestraintType.UNRESTRAINED,
        0.0,
        RestraintType.UNRESTRAINED,
        0.0,
        DepthRestraintType.UNRESTRAINED,
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
        Instant.MAX
    ));

    return Stream.of(
        Arguments
            .arguments(100, 0.01, 43.21, "TESTMODEL", false, ScalingFactorType.CONFIDENCE, 4, 0.3,
                4, correctLocationRestraint),
        Arguments
            .arguments(100, 0.01, 12.34, "TESTMODEX", false, ScalingFactorType.CONFIDENCE, 4, 0.3,
                4, correctLocationRestraint),
        Arguments
            .arguments(100, 0.01, 12.34, "TESTMODEL", true, ScalingFactorType.CONFIDENCE, 4, 0.3, 4,
                correctLocationRestraint),
        Arguments
            .arguments(100, 0.01, 12.34, "TESTMODEL", false, ScalingFactorType.COVERAGE, 4, 0.3, 4,
                correctLocationRestraint),
        Arguments
            .arguments(100, 0.01, 12.34, "TESTMODEL", false, ScalingFactorType.CONFIDENCE, 5, 0.3,
                4, correctLocationRestraint),
        Arguments
            .arguments(100, 0.01, 12.34, "TESTMODEL", false, ScalingFactorType.CONFIDENCE, 4, 0.4,
                4, correctLocationRestraint),
        Arguments
            .arguments(100, 0.01, 12.34, "TESTMODEL", false, ScalingFactorType.CONFIDENCE, 4, 0.3,
                5, correctLocationRestraint),

        Arguments
            .arguments(100, 0.01, 12.34, "TESTMODEL", false, ScalingFactorType.CONFIDENCE, 4, 0.3,
                4, wrongLocationRestraint),

        Arguments
            .arguments(101, 0.01, 12.34, "TESTMODEL", false, ScalingFactorType.CONFIDENCE, 4, 0.3,
                4, correctLocationRestraint),
        Arguments
            .arguments(100, 0.02, 12.34, "TESTMODEL", false, ScalingFactorType.CONFIDENCE, 4, 0.3,
                4, correctLocationRestraint)
    );
  }
}
