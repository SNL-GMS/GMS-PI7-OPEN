package gms.core.signaldetection.association.control;

import gms.core.signaldetection.association.eventredundancy.plugins.ArrivalQualityEventCriterionDefinition;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ArrivalQualityCriteriaParametersTests {
  private static double arQualAlpha = 0.12;
  private static double arQualBeta = 0.13;
  private static double arQualGamma = 0.14;
  private static double arQualThreshold = 0.15;

  private static final ArrivalQualityEventCriterionDefinition definition =
      ArrivalQualityEventCriterionDefinition.create(
          arQualAlpha,
          arQualBeta,
          arQualGamma,
          arQualThreshold
      );

  static final String pluginName = "default-plugin";
  static final String pluginVersion = "1.0.0";
  static final PluginInfo pluginInfo = PluginInfo.from(pluginName, pluginVersion);

  @Test
  void testCreate() {
    ArrivalQualityCriteriaParameters parameters = ArrivalQualityCriteriaParameters.create(
        pluginInfo, definition);

    Assertions.assertEquals(pluginInfo, parameters.getPluginInfo().get());

    Assertions.assertEquals(definition, parameters.getArrivalQualityEventCriterionDefinition().get());
  }

  @ParameterizedTest
  @MethodSource("testCreateNullParametersProvider")
  void testCreateNullParameters(PluginInfo pluginInfo,
      ArrivalQualityEventCriterionDefinition definition) {

    ArrivalQualityCriteriaParameters parameters = ArrivalQualityCriteriaParameters.create(
        pluginInfo, definition
    );


    Assertions.assertEquals(Optional.ofNullable(pluginInfo), parameters.getPluginInfo());
    Assertions.assertEquals(Optional.ofNullable(definition), parameters.getArrivalQualityEventCriterionDefinition());
  }

  private static Stream<Arguments> testCreateNullParametersProvider() {
    return Stream.of(
        Arguments.arguments(null, definition),
        Arguments.arguments(pluginInfo, null)
    );
  }

}
