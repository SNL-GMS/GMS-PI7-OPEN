package gms.shared.mechanisms.configuration.constraints;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class DefaultConstraintTests {

  @Test
  void testFrom() {
    assertEquals(DefaultConstraint.CRITERION, DefaultConstraint.from().getCriterion());
  }

  @Test
  void testSerializationIgnoresParentProperties() {
    final Map<String, Object> fieldMap = ObjectSerialization.toFieldMap(DefaultConstraint.from());

    assertAll(
        () -> assertNotNull(fieldMap),
        () -> assertEquals(1, fieldMap.size()),
        () -> assertTrue(fieldMap.containsKey("constraintType"))
    );
  }

  @Test
  void testIsSatisfiedIsTrue() {
    final DefaultConstraint constraint = DefaultConstraint.from();

    assertAll(
        () -> assertTrue(constraint.test(true)),
        () -> assertTrue(constraint.test(false)),
        () -> assertTrue(constraint.test(1.0)),
        () -> assertTrue(constraint.test("abc")),
        () -> assertTrue(constraint.test(null))
    );
  }
}
