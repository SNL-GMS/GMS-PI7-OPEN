package gms.core.signaldetection.association.control;

import gms.core.signaldetection.association.eventredundancy.plugins.WeightedEventCriteriaCalculationDefinition;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class WeightedEventCriteriaParametersTests {
  private static double primaryTimeWeight = 0.05;
  private static double secondaryTimeWeight = 0.06;
  private static double arrayAzimuthWeight = 0.07;
  private static double threeComponentAzimuthWeight = 0.08;
  private static double arraySlowWeight = 0.09;
  private static double threeComponentSlowWeight = 0.10;
  private static double weightThreshold = 0.11;

  private static final WeightedEventCriteriaCalculationDefinition definition =
      WeightedEventCriteriaCalculationDefinition.create(
          primaryTimeWeight,
          secondaryTimeWeight,
          arrayAzimuthWeight,
          threeComponentAzimuthWeight,
          arraySlowWeight,
          threeComponentSlowWeight,
          weightThreshold
      );

  static final String pluginName = "default-plugin";
  static final String pluginVersion = "1.0.0";
  static final PluginInfo pluginInfo = PluginInfo.from(pluginName, pluginVersion);

  @Test
  void testCreate() {
    WeightedEventCriteriaParameters parameters = WeightedEventCriteriaParameters.create(
        pluginInfo, definition);

    Assertions.assertEquals(pluginInfo, parameters.getPluginInfo().get());

    Assertions.assertEquals(definition, parameters.getWeightedEventCriteriaCalculationDefinition().get());
  }

  @ParameterizedTest
  @MethodSource("testCreateNullParametersProvider")
  void testCreateNullParameters(PluginInfo pluginInfo,
      WeightedEventCriteriaCalculationDefinition definition) {

    WeightedEventCriteriaParameters parameters = WeightedEventCriteriaParameters.create(
        pluginInfo, definition
    );


    Assertions.assertEquals(Optional.ofNullable(pluginInfo), parameters.getPluginInfo());
    Assertions.assertEquals(Optional.ofNullable(definition), parameters.getWeightedEventCriteriaCalculationDefinition());
  }

  private static Stream<Arguments> testCreateNullParametersProvider() {
    return Stream.of(
        Arguments.arguments(null, definition),
        Arguments.arguments(pluginInfo, null)
    );
  }
}
