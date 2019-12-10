package gms.shared.mechanisms.configuration.constraints;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BooleanConstraintTests {

  @Test
  void testFrom() {

    final String criterion = "criterion";
    final boolean value = true;
    final long priority = 100;

    final BooleanConstraint constraint = BooleanConstraint.from(criterion, value, priority);
    assertAll(
        () -> assertNotNull(constraint),
        () -> assertEquals(criterion, constraint.getCriterion()),
        () -> assertEquals(value, constraint.getValue()),
        () -> assertEquals(priority, constraint.getPriority())
    );
  }

  @Test
  void testIsSatisfiedTrueConstraint() {
    final BooleanConstraint trueConstraint = BooleanConstraint.from("A", true, 1);
    assertAll(
        () -> assertTrue(trueConstraint.test(true)),
        () -> assertFalse(trueConstraint.test(false)),
        () -> assertFalse(trueConstraint.test(null))
    );
  }

  @Test
  void testIsSatisfiedFalseConstraint() {
    final BooleanConstraint falseConstraint = BooleanConstraint.from("A", false, 1);
    assertAll(
        () -> assertFalse(falseConstraint.test(true)),
        () -> assertTrue(falseConstraint.test(false)),
        () -> assertFalse(falseConstraint.test(null))
    );
  }

  @Test
  void testSerializationIgnoresParentProperties() {
    final Map<String, Object> fieldMap = ObjectSerialization
        .toFieldMap(BooleanConstraint.from("A", true, 3));

    assertAll(
        () -> assertNotNull(fieldMap),
        () -> assertEquals(4, fieldMap.size()),
        () -> Assertions.assertTrue(fieldMap.containsKey("constraintType")),
        () -> Assertions.assertTrue(fieldMap.containsKey("criterion")),
        () -> Assertions.assertTrue(fieldMap.containsKey("value")),
        () -> Assertions.assertTrue(fieldMap.containsKey("priority"))
    );
  }
}
