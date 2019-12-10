package gms.shared.mechanisms.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigurationReferenceTests {


  @Test
  void testIsConfigurationReferenceKey() {
    assertTrue(ConfigurationReference
        .isConfigurationReferenceKey(ConfigurationReference.REF_COMMAND + "global"));
    assertFalse(ConfigurationReference
        .isConfigurationReferenceKey("global" + ConfigurationReference.REF_COMMAND));
  }


  @Test
  void testSerialization() {
    ConfigurationReference gt = ConfigurationReference.from("global",
        List.of(Selector.from("criterion", "value")));

    Map<String, Object> map = ObjectSerialization.toFieldMap(
        ConfigurationReference.from("global",
            List.of(Selector.from("criterion", "value"))));

    assertTrue(map.containsKey(ConfigurationReference.REF_COMMAND + "global"));

    ConfigurationReference deserializationResult = ObjectSerialization
        .fromFieldMap(map, ConfigurationReference.class);
    assertEquals(gt, deserializationResult);
  }
}
