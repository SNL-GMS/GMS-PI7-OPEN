package gms.shared.mechanisms.configuration.constraints;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WildcardConstraintTests {

  @Test
  void testFrom() {
    assertEquals("FOO", WildcardConstraint.from("FOO").getCriterion());
  }

  @Test
  void testIsSatisfiedIsTrue() {
    final WildcardConstraint constraint = WildcardConstraint.from("FOO");

    assertAll(
        () -> assertTrue(constraint.test("")),
        () -> assertTrue(constraint.test(-1)),
        () -> assertTrue(constraint.test(100.0))
    );
  }

  @Test
  void testSerializationIgnoresParentProperties() {
    final Map<String, Object> fieldMap = ObjectSerialization.toFieldMap(WildcardConstraint.from("A"));

    assertAll(
        () -> assertNotNull(fieldMap),
        () -> assertEquals(2, fieldMap.size()),
        () -> assertTrue(fieldMap.containsKey("constraintType")),
        () -> assertTrue(fieldMap.containsKey("criterion"))
    );
  }
}
