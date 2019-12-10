package gms.core.eventlocation.control;

import gms.core.eventlocation.plugins.EventLocationDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.LocationRestraint;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.RestraintType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.ScalingFactorType;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EventLocationControlParametersTests {

  private static final int maximumIterations = 100;
  private static final double convergenceTolerance = 0.01;
  private static final double uncertaintyProbabilityPercentile = 12.34;
  private static final String earthMocel = "TESTMODEL";
  private static final boolean applyCorrections = false;
  private static final ScalingFactorType scalingFactorType = ScalingFactorType.CONFIDENCE;
  private static final int kWeight = 4;
  private static final double aprioriVariance = 0.3;
  private static final int minimumNumberOfObservations = 4;

  private static final EventLocationDefinition defaultEventLocationDefinition = EventLocationDefinition
      .create(
          EventLocationControlParametersTests.maximumIterations,
          EventLocationControlParametersTests.convergenceTolerance,
          EventLocationControlParametersTests.uncertaintyProbabilityPercentile,
          EventLocationControlParametersTests.earthMocel,
          EventLocationControlParametersTests.applyCorrections,
          EventLocationControlParametersTests.scalingFactorType,
          EventLocationControlParametersTests.kWeight,
          EventLocationControlParametersTests.aprioriVariance,
          EventLocationControlParametersTests.minimumNumberOfObservations,
          List.of(LocationRestraint.from(
              RestraintType.UNRESTRAINED,
              null,
              RestraintType.UNRESTRAINED,
              null,
              DepthRestraintType.UNRESTRAINED,
              null,
              RestraintType.UNRESTRAINED,
              null)));

  private static final String pluginName = "myRockinPluginName";
  private static final String pluginVersion = "myRockinPluginVersion";

  private static final PluginInfo pluginInfo = PluginInfo.from(pluginName, pluginVersion);


  // Tests successful case of EventLocationControlParameters.create()
  @Test
  void testCreate() {

    EventLocationControlParameters parameters = EventLocationControlParameters.create(
        PluginInfo.from(
            EventLocationControlParametersTests.pluginName,
            EventLocationControlParametersTests.pluginVersion
        ),
        EventLocationControlParametersTests.defaultEventLocationDefinition
    );

    Assertions
        .assertEquals(EventLocationControlParametersTests.pluginName,
            parameters.getPluginInfo().get().getName());
    Assertions.assertEquals(EventLocationControlParametersTests.pluginVersion,
        parameters.getPluginInfo().get().getVersion());
    Assertions.assertEquals(EventLocationControlParametersTests.defaultEventLocationDefinition,
        parameters.getEventLocationDefinition().get());
  }

  @ParameterizedTest
  @MethodSource("testCreateNullParametersProvider")
  void testCreateNullParameters(PluginInfo pluginInfo,
      EventLocationDefinition eventLocationDefinition) {

    EventLocationControlParameters parameters = EventLocationControlParameters
        .create(pluginInfo, eventLocationDefinition);

    Assertions.assertEquals(Optional.ofNullable(pluginInfo), parameters.getPluginInfo());
    Assertions.assertEquals(Optional.ofNullable(eventLocationDefinition),
        parameters.getEventLocationDefinition());
  }


  // Parameter provider for parameterized test method: testCreateNullParameters()
  private static Stream<Arguments> testCreateNullParametersProvider() {

    return Stream.of(
        Arguments.arguments(
            null,
            EventLocationControlParametersTests.defaultEventLocationDefinition
        ),
        Arguments.arguments(
            EventLocationControlParametersTests.pluginInfo,
            null
        )
    );
  }
}
