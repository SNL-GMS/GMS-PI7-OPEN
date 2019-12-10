package gms.shared.mechanisms.configuration.constraints;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StringConstraintTests {

  @Test
  void testPhaseIN() {
    final StringConstraint con =
        StringConstraint.from("phase",
            Operator.from(Type.IN, false),
            Set.of("P", "Lg", "S"),
            1);

    assertAll(
        () -> assertTrue(con.test("P")),
        () -> assertTrue(con.test("Lg")),
        () -> assertTrue(con.test("S")),
        () -> assertFalse(con.test("I"))
    );
  }

  @Test
  void testPhaseEQ() {
    final StringConstraint con =
        StringConstraint.from("phase",
            Operator.from(Type.EQ, false),
            Set.of("P"),
            1);

    assertAll(
        () -> assertTrue(con.test("P")),
        () -> assertFalse(con.test("I"))
    );
  }

  @Test
  void testPhaseNotEQ() {
    final StringConstraint con =
        StringConstraint.from("phase",
            Operator.from(Type.EQ, false),
            Set.of("P", "Lg"),
            1);

    assertFalse(con.test("P"));
  }

  @Test
  void testIsSatisfiedValidatesParameter() {
    assertTrue(
        assertThrows(NullPointerException.class,
            () -> StringConstraint
                .from("phase", Operator.from(Type.IN, false), Set.of("P", "Lg", "S"), 1)
                .test(null)).getMessage()
            .contains("queryVal can't be null"));
  }
}
