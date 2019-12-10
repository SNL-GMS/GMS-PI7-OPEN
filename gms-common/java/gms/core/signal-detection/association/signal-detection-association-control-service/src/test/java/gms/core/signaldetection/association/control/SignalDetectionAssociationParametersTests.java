package gms.core.signaldetection.association.control;

import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterionDefinition;
import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationDefinition;
import gms.core.signaldetection.association.plugins.SignalDetectionAssociatorDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SignalDetectionAssociationParametersTests {

  private static int maxStationsPerGrid = 300;
  private static double sigmaSlowness = 0.01;
  private static List<PhaseType> phases = List.of(PhaseType.P, PhaseType.S);
  private static List<PhaseType> forwardTransformationPhases = List
      .of(PhaseType.P, PhaseType.PKP, PhaseType.S);
  private static double beliefThreshold = 0.02;
  private static boolean primaryPhaseRequiredForSecondary = true;
  private static double sigmaTime = 0.03;
  private static double chiLimit = 0.04;
  private static boolean freezeArrivalsAtBeamPoints = false;
  private static double primaryTimeWeight = 0.05;
  private static double secondaryTimeWeight = 0.06;
  private static double arrayAzimuthWeight = 0.07;
  private static double threeComponentAzimuthWeight = 0.08;
  private static double arraySlowWeight = 0.09;
  private static double threeComponentSlowWeight = 0.10;
  private static double weightThreshold = 0.11;
  private static double arQualAlpha = 0.12;
  private static double arQualBeta = 0.13;
  private static double arQualGamma = 0.14;
  private static double arQualThreshold = 0.15;
  private static double expectedGridCylinderRadiusDegrees = 1.0;
  private static double expectedGridCylinderDepthKm = 50.0;
  private static double expectedGridCylinderHeightKm = 100.0;
  private static double expectedMinimumMagnitude = 3.5;
  private static int numFirstSta = 5;

  private static final WeightedEventCriteriaCalculationDefinition weightedEventCriteria =
      WeightedEventCriteriaCalculationDefinition.create(
          primaryTimeWeight,
          secondaryTimeWeight,
          arrayAzimuthWeight,
          threeComponentAzimuthWeight,
          arraySlowWeight,
          threeComponentSlowWeight,
          weightThreshold
          );
  private static final ArrivalQualityEventCriterionDefinition arrivalQualityCriteria =
      ArrivalQualityEventCriterionDefinition.create(
          arQualAlpha,
          arQualBeta,
          arQualGamma,
          arQualThreshold
          );
  private static final SignalDetectionAssociatorDefinition definition = SignalDetectionAssociatorDefinition
      .create(
          maxStationsPerGrid,
          sigmaSlowness,
          phases,
          forwardTransformationPhases,
          beliefThreshold,
          primaryPhaseRequiredForSecondary,
          sigmaTime,
          chiLimit,
          freezeArrivalsAtBeamPoints,
          expectedGridCylinderRadiusDegrees,
          expectedGridCylinderDepthKm,
          expectedGridCylinderHeightKm,
          expectedMinimumMagnitude,
          weightedEventCriteria,
          arrivalQualityCriteria,
          numFirstSta);

  private static final String pluginName = "myRockinPluginName";
  private static final String pluginVersion = "myRockinPluginVersion";

  private static final PluginInfo pluginInfo = PluginInfo.from(pluginName, pluginVersion);

  @Test
  void testCreate() {
    SignalDetectionAssociationParameters parameters = SignalDetectionAssociationParameters.create(
        pluginInfo, definition);

    Assertions.assertEquals(pluginInfo, parameters.getPluginInfo().get());

    Assertions.assertEquals(definition, parameters.getSignalDetectionAssociatorDefinition().get());
  }

  @ParameterizedTest
  @MethodSource("testCreateNullParametersProvider")
  void testCreateNullParameters(PluginInfo pluginInfo,
      SignalDetectionAssociatorDefinition definition) {

    SignalDetectionAssociationParameters parameters = SignalDetectionAssociationParameters.create(
        pluginInfo, definition
    );


    Assertions.assertEquals(Optional.ofNullable(pluginInfo), parameters.getPluginInfo());
    Assertions.assertEquals(Optional.ofNullable(definition), parameters.getSignalDetectionAssociatorDefinition());
  }

  private static Stream<Arguments> testCreateNullParametersProvider() {
    return Stream.of(
        Arguments.arguments(null, definition),
        Arguments.arguments(pluginInfo, null)
    );
  }
}
