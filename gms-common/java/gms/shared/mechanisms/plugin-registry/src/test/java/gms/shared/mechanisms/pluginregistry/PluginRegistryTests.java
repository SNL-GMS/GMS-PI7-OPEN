package gms.shared.mechanisms.pluginregistry;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.pluginregistry.exceptions.UnspecifiedNameOrVersionException;
import gms.shared.mechanisms.pluginregistry.testing.DummyInterface1;
import gms.shared.mechanisms.pluginregistry.testing.DummyInterface2;
import gms.shared.mechanisms.pluginregistry.testing.DummyPluginClass1;
import gms.shared.mechanisms.pluginregistry.testing.DummyPluginClass2;
import gms.shared.mechanisms.pluginregistry.testing.DummyPluginClass3;
import gms.shared.mechanisms.pluginregistry.testing.PluginNoName;
import gms.shared.mechanisms.pluginregistry.testing.PluginNoNameOrVersion;
import gms.shared.mechanisms.pluginregistry.testing.PluginNoVersion;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

public class PluginRegistryTests {

  private static PluginRegistry pluginRegistry = PluginRegistry.getRegistry();

  static {
    pluginRegistry.loadAndRegister();
  }

  @Test
  public void testRegisterAndLookup() {

    Set<PluginInfo> expectedPlugins = Set.of(
        PluginInfo.from("dummy1", "1.0.0"),
        PluginInfo.from("dummy2", "1.0.0"),
        PluginInfo.from("muhDummyClass3", "1.0.0")
    );

    Set<PluginInfo> actualPlugins = pluginRegistry.getAllPlugins();

    assertTrue(
        expectedPlugins.containsAll(actualPlugins) && actualPlugins.containsAll(expectedPlugins));

    expectedPlugins = Set.of(
        PluginInfo.from("dummy1", "1.0.0"),
        PluginInfo.from("dummy2", "1.0.0")
    );

    actualPlugins = pluginRegistry.getPluginsByInterface(
        DummyInterface1.class);

    assertTrue(
        expectedPlugins.containsAll(actualPlugins) && actualPlugins.containsAll(expectedPlugins));

    expectedPlugins = Set.of(
        PluginInfo.from("muhDummyClass3", "1.0.0")
    );

    actualPlugins = pluginRegistry.getPluginsByInterface(
        DummyInterface2.class);

    assertTrue(
        expectedPlugins.containsAll(actualPlugins) && actualPlugins.containsAll(expectedPlugins));

  }

  @Test
  public void testPluginsCalledProperly() {
    DummyInterface1 dummyObjectI1_1 = new DummyPluginClass1();
    DummyInterface1 dummyObjectI1_2 = new DummyPluginClass2();
    DummyInterface2 dummyObjectI2_3 = new DummyPluginClass3();

    DummyInterface1 plugin11 = pluginRegistry
        .lookup(PluginInfo.from("dummy1", "1.0.0"), DummyInterface1.class).get();
    assertEquals(dummyObjectI1_1.getDummyValue1(), plugin11.getDummyValue1());

    DummyInterface1 plugin12 = pluginRegistry
        .lookup(PluginInfo.from("dummy2", "1.0.0"), DummyInterface1.class).get();
    assertEquals(dummyObjectI1_2.getDummyValue1(), plugin12.getDummyValue1());

    DummyInterface2 plugin23 = pluginRegistry
        .lookup(PluginInfo.from("muhDummyClass3", "1.0.0"), DummyInterface2.class).get();
    assertEquals(dummyObjectI2_3.getDummyValue2(), plugin23.getDummyValue2());
  }

  @Test
  public void testUnAnnotatedClassesFailed() {
    assertEquals(3, pluginRegistry.getLoadingExceptions().entrySet().size());

    pluginRegistry.getLoadingExceptions().values()
        .forEach(exception -> assertTrue(exception instanceof UnspecifiedNameOrVersionException));

    assertTrue(pluginRegistry.getLoadingExceptions().keySet().containsAll(
        Set.of(PluginNoName.class, PluginNoNameOrVersion.class, PluginNoVersion.class)));

    assertEquals(3, pluginRegistry.getAllPlugins().size());
  }

  @Test
  @Disabled
  //TODO: implement this
  public void testGetPluginsByParameterizedType() {

    //Once enabled this test will fail until filled in!
    assertTrue(false);
  }

  @Test
  public void testLookupMissingPluginReturnsOptionalNull() {
    Optional<DummyInterface1> optionalPlugin = pluginRegistry
        .lookup(PluginInfo.from("dummy100", "1.0.0"),
            DummyInterface1.class);

    assertEquals(Optional.empty(), optionalPlugin);
  }
}
