package gms.shared.mechanisms.configuration.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import gms.shared.mechanisms.configuration.Configuration;
import gms.shared.mechanisms.configuration.TestUtilities;
import gms.shared.mechanisms.configuration.client.FooParameters;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ObjectSerializationTests {

  @Test
  void testGetObjectMapper() {
    assertNotNull(ObjectSerialization.getObjectMapper());
  }

  @Test
  public void testConvertFromMap() {
    final FooParameters deserialized = ObjectSerialization
        .fromFieldMap(Map.of("a", 100, "b", "string100", "c", true), FooParameters.class);

    assertEquals(TestUtilities.fooParamsDefaults, deserialized);
  }

  @Test
  public void testConvertToMap() {
    final FooParameters fooParameters = FooParameters.from(-200, "string-200", false);
    final Map<String, Object> map = ObjectSerialization.toFieldMap(fooParameters);

    assertEquals(-200, map.get("a"));
    assertEquals("string-200", map.get("b"));
    assertEquals(false, map.get("c"));
  }

  @Test
  public void testMapConversionRoundTrip() {
    final FooParameters fooParameters = FooParameters.from(-200, "string-200", false);
    final Map<String, Object> map = ObjectSerialization.toFieldMap(fooParameters);
    final FooParameters deserialized = ObjectSerialization
        .fromFieldMap(map, FooParameters.class);

    assertEquals(fooParameters, deserialized);
  }

  @Test
  public void testEmbeddedObjectMapConversionRoundTrip() {
    Configuration deserialized = ObjectSerialization.fromFieldMap(
        ObjectSerialization.toFieldMap(TestUtilities.configurationFilter),
        Configuration.class);

    assertEquals(TestUtilities.configurationFilter, deserialized);
  }

}
