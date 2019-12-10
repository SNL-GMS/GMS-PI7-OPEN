package gms.shared.mechanisms.objectstoragedistribution.coi.common;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests the plugin registry
 */
@ExtendWith(MockitoExtension.class)
public class PluginRegistryTest {

  @Mock
  private Plugin mockedPlugin;

  @Mock
  private Plugin alternateMockedPlugin;

  private PluginRegistry<Plugin> registry;

  @BeforeEach
  void setUp() {
    registry = new PluginRegistry<>();
  }

  @Test
  public void testLookupNullThrowsNPE() {
    assertThrows(NullPointerException.class, () -> registry.lookup(null));
  }

  @Test
  public void testLookupReturnsPlugin() {
    given(mockedPlugin.getName()).willReturn("test");
    given(mockedPlugin.getVersion()).willReturn(PluginVersion.from(1, 2, 3));
    registry.register(mockedPlugin);

    given(alternateMockedPlugin.getName()).willReturn("alternate");
    given(alternateMockedPlugin.getVersion()).willReturn(PluginVersion.from(2, 3, 4));
    registry.register(alternateMockedPlugin);

    Optional<Plugin> actual;

    actual = registry.lookup(RegistrationInfo.create("test", 1, 2, 3));
    assertTrue(actual.isPresent());
    assertEquals(mockedPlugin, actual.get());

    actual = registry.lookup(RegistrationInfo.create("alternate", 2, 3, 4));
    assertTrue(actual.isPresent());
    assertEquals(alternateMockedPlugin, actual.get());
  }

  @Test
  public void testLookupWrongName() {
    given(mockedPlugin.getName()).willReturn("test");
    given(mockedPlugin.getVersion()).willReturn(PluginVersion.from(1, 0, 0));
    registry.register(mockedPlugin);

    Optional<Plugin> plugin = registry
        .lookup(RegistrationInfo.from("othertest", PluginVersion.from(1, 0, 0)));
    assertFalse(plugin.isPresent());
  }

  @Test
  public void testLookupWrongVersion() {
    given(mockedPlugin.getName()).willReturn("test");
    given(mockedPlugin.getVersion()).willReturn(PluginVersion.from(1, 0, 0));
    registry.register(mockedPlugin);

    Optional<Plugin> plugin = registry
        .lookup(RegistrationInfo.from("test", PluginVersion.from(1, 1, 0)));
    assertFalse(plugin.isPresent());
  }

}
