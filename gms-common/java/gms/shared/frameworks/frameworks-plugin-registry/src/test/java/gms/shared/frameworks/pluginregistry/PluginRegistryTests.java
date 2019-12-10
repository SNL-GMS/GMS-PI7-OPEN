package gms.shared.frameworks.pluginregistry;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.frameworks.pluginregistry.fixtures.BarImplOne;
import gms.shared.frameworks.pluginregistry.fixtures.BarImplTwo;
import gms.shared.frameworks.pluginregistry.fixtures.FooImplOne;
import gms.shared.frameworks.pluginregistry.fixtures.FooImplTwo;
import gms.shared.frameworks.pluginregistry.fixtures.IBar;
import gms.shared.frameworks.pluginregistry.fixtures.IFoo;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

public class PluginRegistryTests {

  private static final Map<String, Long> BAR_NAME_TO_VAL
      = Map.of(BarImplOne.NAME, BarImplOne.VALUE, BarImplTwo.NAME, BarImplTwo.VALUE);

  private static final Map<String, Long> FOO_NAME_T0_VAL
      = Map.of(FooImplOne.NAME, FooImplOne.VALUE, FooImplTwo.NAME, FooImplTwo.VALUE);

  private static final PluginRegistry registry = PluginRegistry.create();

  @Test
  public void testGetOneKnownNameWithValidClassBarFindsPlugin() {
    BAR_NAME_TO_VAL.forEach((name, val) -> lookupAndCheckPlugin(
        IBar.class, name, IBar::getBarValue, val));
  }

  @Test
  public void testGetOneKnownNameValidClassFooFindsPlugin() {
    FOO_NAME_T0_VAL.forEach((name, val) -> lookupAndCheckPlugin(
        IFoo.class, name, IFoo::getFooValue, val));
  }

  @Test
  public void testGetOneKnownNameInvalidClassThrows() {
    assertAll("Retrieve plugin by known name but incorrect class throws exception",
        () -> assertThrows(IllegalArgumentException.class, () -> registry.get(BarImplOne.NAME, IFoo.class)),
        () -> assertThrows(IllegalArgumentException.class, () -> registry.get(FooImplOne.NAME, IBar.class)));
  }

  @Test
  public void testGetOneUnknownNameValidClassThrows() {
    assertAll("Retrieve plugin by known name but incorrect class throws exception",
        () -> assertThrows(IllegalArgumentException.class, () -> registry.get("fake", IBar.class)),
        () -> assertThrows(IllegalArgumentException.class, () -> registry.get("fake", IFoo.class)));
  }

  @Test
  public void testGetMultipleKnownNamesValidClassFooFindsPlugins() {
    lookupAndCheckPlugins(IFoo.class, FOO_NAME_T0_VAL, IFoo::getFooValue);
  }

  @Test
  public void testGetMultipleKnownNamesValidClassBarFindsPlugins() {
    lookupAndCheckPlugins(IBar.class, BAR_NAME_TO_VAL, IBar::getBarValue);
  }

  @Test
  public void testGetMultipleKnownNamesInvalidClassThrows() {
    assertThrows(IllegalArgumentException.class,
        () -> registry.get(Set.of(FooImplOne.NAME, FooImplTwo.NAME), IBar.class));
  }

  @Test
  public void testGetMultipleNamesAtLeastOneUnknownValidClassFooThrows() {
    assertAll("Get by multiple names at least one unknown but invalid class throws",
        () -> assertThrows(IllegalArgumentException.class,
          () -> registry.get(Set.of(FooImplOne.NAME, "fake"), IFoo.class)),
        () -> assertThrows(IllegalArgumentException.class,
            () -> registry.get(Set.of(BarImplOne.NAME, "fake"), IBar.class)));
  }

  private static <P extends Plugin> void lookupAndCheckPlugin(
      Class<P> pluginClass, String expectedName,
      Function<P, Long> valExtractor, long expectedVal) {

    checkPlugin(registry.get(expectedName, pluginClass),
        expectedName, valExtractor, expectedVal);
  }

  private static <P extends Plugin> void lookupAndCheckPlugins(
      Class<P> pluginClass, Map<String, Long> nameToVal,
      Function<P, Long> valExtractor) {

    checkPlugins(registry.get(nameToVal.keySet(), pluginClass),
        nameToVal, valExtractor);
  }

  private static <P extends Plugin> void checkPlugins(
      Collection<P> plugins, Map<String, Long> nameToVal,
      Function<P, Long> valExtractor) {

    assertNotNull(plugins, "Got null plugin collection back from registry");
    assertEquals( 2, plugins.size(), "Asked for two plugins, expect exactly 2");
    nameToVal.forEach((key, val) -> {
      final Optional<P> p = plugins.stream()
          .filter(f -> f.getName().equals(key))
          .findFirst();
      assertNotNull(p);
      assertTrue(p.isPresent());
      checkPlugin(p.get(), key, valExtractor, val);
    });
  }

  private static <P extends Plugin> void checkPlugin(
      P plugin, String expectedName,
      Function<P, Long> valExtractor, long expectedVal) {

    assertNotNull(plugin, "Expected to find non-null plugin");
    // need cast otherwise call is ambiguous to different JUnit methods
    assertEquals(expectedVal, (long) valExtractor.apply(plugin));
    assertEquals(expectedName, plugin.getName());
  }
}