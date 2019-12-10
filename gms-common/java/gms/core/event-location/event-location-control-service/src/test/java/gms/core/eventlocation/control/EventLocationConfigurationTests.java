package gms.core.eventlocation.control;

import gms.core.eventlocation.plugins.EventLocationDefinition;
import gms.core.eventlocation.plugins.definitions.EventLocationDefinitionApacheLm;
import gms.core.eventlocation.plugins.definitions.EventLocationDefinitionGeigers;
import gms.shared.mechanisms.pluginregistry.PluginInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EventLocationConfigurationTests {


  @Test
  void testCreateGeigers() {

    EventLocationConfiguration configuration = EventLocationConfiguration.create();

    EventLocationControlParameters parameters = configuration
        .getParametersForPlugin(PluginInfo.from("eventLocationGeigersPlugin", "1.0.0"));

    EventLocationDefinition definition = parameters.getEventLocationDefinition().orElseThrow(IllegalStateException::new);

    Assertions.assertTrue(definition instanceof EventLocationDefinitionGeigers);

    PluginInfo pluginInfo = parameters.getPluginInfo().orElseThrow(IllegalStateException::new);

    Assertions.assertAll(
        () -> Assertions.assertEquals("eventLocationGeigersPlugin", pluginInfo.getName()),
        () -> Assertions.assertEquals("1.0.0", pluginInfo.getVersion()),
        () -> Assertions.assertEquals(0.9, definition.getUncertaintyProbabilityPercentile())
    );
  }

  @Test
  void testCreateApacheLm() {

    EventLocationConfiguration configuration = EventLocationConfiguration.create();

    EventLocationControlParameters parameters = configuration
        .getParametersForPlugin(PluginInfo.from("eventLocationApacheLmPlugin", "1.0.0"));

    EventLocationDefinition definition = parameters.getEventLocationDefinition().orElseThrow(IllegalStateException::new);

    Assertions.assertTrue(definition instanceof EventLocationDefinitionApacheLm);

    PluginInfo pluginInfo = parameters.getPluginInfo().orElseThrow(IllegalStateException::new);

    Assertions.assertAll(
        () -> Assertions.assertEquals("eventLocationApacheLmPlugin", pluginInfo.getName()),
        () -> Assertions.assertEquals("1.0.0", pluginInfo.getVersion()),
        () -> Assertions.assertEquals(0.9, definition.getUncertaintyProbabilityPercentile())
    );
  }

  @Test
  void testGetParametersForPluginDoesntExist() {

    EventLocationConfiguration configuration = EventLocationConfiguration.create();

    PluginInfo pluginInfo = PluginInfo.from("dingoDanPlugin", "dingoDanVersion");

    Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () ->

        configuration.getParametersForPlugin(pluginInfo)
    );

    String errMsg = String.format(
        "EventLocationConfiguration does not contain EventLocationControlParameters for the specified PluginInfo: %s",
        pluginInfo.toString());

    Assertions.assertEquals(errMsg, exception.getMessage());
  }
}
