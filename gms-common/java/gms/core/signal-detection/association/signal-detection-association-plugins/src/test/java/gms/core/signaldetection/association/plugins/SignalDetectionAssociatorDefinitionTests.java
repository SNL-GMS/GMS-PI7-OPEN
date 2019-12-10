package gms.core.signaldetection.association.plugins;

import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterionDefinition;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculation;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SignalDetectionAssociatorDefinitionTests {

  @Test
  void testCreate() {
    WeightedEventCriteriaCalculationDefinition weightEventCriteriaDefinition =
        WeightedEventCriteriaCalculationDefinition.create(
            1.4,
            1.5,
            1.6,
            1.7,
            1.8,
            1.9,
            2.0
            );
    ArrivalQualityEventCriterionDefinition arrivalQualityEventCriterionDefinition =
        ArrivalQualityEventCriterionDefinition.create(
            2.1,
            2.2,
            2.3,
            2.4
            );
    SignalDetectionAssociatorDefinition definition = SignalDetectionAssociatorDefinition.create(
        4,
        1.0,
        List.of(PhaseType.S, PhaseType.P),
        List.of(PhaseType.S, PhaseType.P, PhaseType.PKP),
        1.1,
        false,
        1.2,
        1.3,
        true,
        1.0,
        50.0,
        100.0,
        3.5,
        weightEventCriteriaDefinition,
        arrivalQualityEventCriterionDefinition,
        5
    );

    Assertions.assertEquals(4, definition.getMaxStationsPerGrid());
    Assertions.assertEquals(1.0, definition.getSigmaSlowness());
    Assertions.assertEquals(List.of(PhaseType.S, PhaseType.P), definition.getPhases());
    Assertions.assertEquals(List.of(PhaseType.S, PhaseType.P, PhaseType.PKP),
        definition.getForwardTransformationPhases());
    Assertions.assertEquals(1.1, definition.getBeliefThreshold());
    Assertions.assertEquals(false, definition.isPrimaryPhaseRequiredForSecondary());
    Assertions.assertEquals(1.2, definition.getSigmaTime());
    Assertions.assertEquals(1.3, definition.getChiLimit());
    Assertions.assertEquals(true, definition.isFreezeArrivalsAtBeamPoints());
    Assertions.assertEquals(1.4, definition.getWeightedEventCriteria().getPrimaryTimeWeight());
    Assertions.assertEquals(1.5, definition.getWeightedEventCriteria().getSecondaryTimeWeight());
    Assertions.assertEquals(1.6, definition.getWeightedEventCriteria().getArrayAzimuthWeight());
    Assertions.assertEquals(1.7, definition.getWeightedEventCriteria().getThreeComponentAzimuthWeight());
    Assertions.assertEquals(1.8, definition.getWeightedEventCriteria().getArraySlowWeight());
    Assertions.assertEquals(1.9, definition.getWeightedEventCriteria().getThreeComponentSlowWeight());
    Assertions.assertEquals(2.0, definition.getWeightedEventCriteria().getWeightThreshold());
    Assertions.assertEquals(2.1, definition.getArrivalQualityCriteria().getArrivalQualityAlpha());
    Assertions.assertEquals(2.2, definition.getArrivalQualityCriteria().getArrivalQualityBeta());
    Assertions.assertEquals(2.3, definition.getArrivalQualityCriteria().getArrivalQualityGamma());
    Assertions.assertEquals(2.4, definition.getArrivalQualityCriteria().getArrivalQualityThreshold());
    Assertions.assertEquals(5, definition.getNumFirstSta());
  }

  @Test
  void testEqualsAndHash() {
    WeightedEventCriteriaCalculationDefinition weightEventCriteriaDefinition =
        WeightedEventCriteriaCalculationDefinition.create(
            1.4,
            1.5,
            1.6,
            1.7,
            1.8,
            1.9,
            2.0
        );
    ArrivalQualityEventCriterionDefinition arrivalQualityEventCriterionDefinition =
        ArrivalQualityEventCriterionDefinition.create(
            2.1,
            2.2,
            2.3,
            2.4
        );
    SignalDetectionAssociatorDefinition definition1 = SignalDetectionAssociatorDefinition.create(
        4,
        1.0,
        new ArrayList<>() {{
          add(PhaseType.S);
          add(PhaseType.P);
        }},
        List.of(PhaseType.S, PhaseType.P, PhaseType.PKP),
        1.1,
        false,
        1.2,
        1.3,
        true,
        1.0,
        50.0,
        100.0,
        3.5,
        weightEventCriteriaDefinition,
        arrivalQualityEventCriterionDefinition,
        5
    );

    SignalDetectionAssociatorDefinition definition2 = SignalDetectionAssociatorDefinition.create(
        4,
        1.0,
        List.of(PhaseType.S, PhaseType.P),
        new ArrayList<>() {{
          add(PhaseType.S);
          add(PhaseType.P);
          add(PhaseType.PKP);
        }},
        1.1,
        false,
        1.2,
        1.3,
        true,
        1.0,
        50.0,
        100.0,
        3.5,
        weightEventCriteriaDefinition,
        arrivalQualityEventCriterionDefinition,
        5
    );

    Assertions.assertEquals(definition1, definition2);
    Assertions.assertEquals(definition2, definition1);

    Assertions.assertEquals(definition2.hashCode(), definition1.hashCode());
  }

  @ParameterizedTest
  @MethodSource("testNotEqualsProvider")
  void testNotEquals(
      int maxStationsPerGrid, double sigmaSlowness, List<PhaseType> phases,
      List<PhaseType> forwardTransformationPhases, double beliefThreshold,
      boolean primaryPhaseRequiredForSecondary, double sigmaTime, double chiLimit,
      boolean freezeArrivalsAtBeamPoints, double primaryTimeWeight, double secondaryTimeWeight,
      double arrayAzimuthWeight, double threeComponentAzimuthWeight, double arraySlowWeight,
      double threeComponentSlowWeight, double weightThreshold, double arQualAlpha,
      double arQualBeta, double arQualGamma, double arQualThreshold,
      double gridCylinderRadiusDegrees,
      double gridCylinderDepthKm,
      double gridCylinderHeightKm,
      double minimumMagnitude,
      int numFirstSta
  ) {
    WeightedEventCriteriaCalculationDefinition weightEventCriteriaDefinition =
        WeightedEventCriteriaCalculationDefinition.create(
            1.4,
            1.5,
            1.6,
            1.7,
            1.8,
            1.9,
            2.0
        );
    ArrivalQualityEventCriterionDefinition arrivalQualityEventCriterionDefinition =
        ArrivalQualityEventCriterionDefinition.create(
            2.1,
            2.2,
            2.3,
            2.4
        );
    WeightedEventCriteriaCalculationDefinition weightedEventCriteriaCalculationDefinition2 =
        WeightedEventCriteriaCalculationDefinition.create(
            primaryTimeWeight,
            secondaryTimeWeight,
            arrayAzimuthWeight,
            threeComponentAzimuthWeight,
            arraySlowWeight,
            threeComponentSlowWeight,
            weightThreshold
        );
    ArrivalQualityEventCriterionDefinition arrivalQualityEventCriterionDefinition2 =
        ArrivalQualityEventCriterionDefinition.create(
          arQualAlpha,
          arQualBeta,
          arQualGamma,
          arQualThreshold
        );
    SignalDetectionAssociatorDefinition definition1 = SignalDetectionAssociatorDefinition.create(
        4,
        1.0,
        List.of(PhaseType.S, PhaseType.P),
        List.of(PhaseType.S, PhaseType.P, PhaseType.PKP),
        1.1,
        false,
        1.2,
        1.3,
        true,
        gridCylinderRadiusDegrees,
        gridCylinderDepthKm,
        gridCylinderHeightKm,
        minimumMagnitude,
        weightEventCriteriaDefinition,
        arrivalQualityEventCriterionDefinition,
        5
    );

    SignalDetectionAssociatorDefinition definition2 = SignalDetectionAssociatorDefinition.create(
        maxStationsPerGrid,
        sigmaSlowness,
        phases,
        forwardTransformationPhases,
        beliefThreshold,
        primaryPhaseRequiredForSecondary,
        sigmaTime,
        chiLimit,
        freezeArrivalsAtBeamPoints,
        gridCylinderRadiusDegrees,
        gridCylinderDepthKm,
        gridCylinderHeightKm,
        minimumMagnitude,
        weightedEventCriteriaCalculationDefinition2,
        arrivalQualityEventCriterionDefinition2,
        numFirstSta
    );

    Assertions.assertNotEquals(definition1, definition2);
    Assertions.assertNotEquals(definition2, definition1);
  }

  static Stream<Arguments> testNotEqualsProvider() {
    List<PhaseType> wronglist1 = List.of(PhaseType.pPdiff, PhaseType.S);
    List<PhaseType> wronglist2 = List.of(PhaseType.pPdiff, PhaseType.S, PhaseType.PKP);

    List<PhaseType> rightlist1 = List.of(PhaseType.S, PhaseType.P);
    List<PhaseType> rightList2 = List.of(PhaseType.S, PhaseType.P, PhaseType.PKP);

    return Stream.of(
        Arguments
            .arguments(1, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.1, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, wronglist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, wronglist2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 2.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, true, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 2.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 2.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, false, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 2.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 2.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 2.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                2.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 2.8, 1.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 2.9, 2.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 1.0, 2.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 1.1, 2.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 1.2, 2.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 1.3, 2.4, 1.0, 50.0, 100.0, 3.5, 5),
        Arguments
            .arguments(4, 1.0, rightlist1, rightList2, 1.1, false, 1.2, 1.3, true, 1.4, 1.5, 1.6,
                1.7, 1.8, 1.9, 2.0, 2.1, 2.2, 2.3, 1.4, 1.0, 50.0, 100.0, 3.5, 4)
    );
  }
}
