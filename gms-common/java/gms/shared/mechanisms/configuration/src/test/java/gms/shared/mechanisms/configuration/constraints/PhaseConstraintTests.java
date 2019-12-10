package gms.shared.mechanisms.configuration.constraints;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PhaseConstraintTests {

  @Test
  void testPhaseIN() {
    final PhaseConstraint con =
        PhaseConstraint.from("phase",
            Operator.from(Type.IN, false),
            Set.of(PhaseType.P, PhaseType.Lg, PhaseType.S),
            1);

    assertAll(
        () -> assertTrue(con.test(PhaseType.P)),
        () -> assertTrue(con.test(PhaseType.Lg)),
        () -> assertTrue(con.test(PhaseType.S)),
        () -> assertFalse(con.test(PhaseType.I))
    );
  }

  @Test
  void testPhaseEQ() {
    final PhaseConstraint con =
        PhaseConstraint.from("phase",
            Operator.from(Type.EQ, false),
            Set.of(PhaseType.P),
            1);

    assertAll(
        () -> assertTrue(con.test(PhaseType.P)),
        () -> assertFalse(con.test(PhaseType.I))
    );
  }

  @Test
  void testPhaseNotEQ() {
    final PhaseConstraint con =
        PhaseConstraint.from("phase",
            Operator.from(Type.EQ, false),
            Set.of(PhaseType.P, PhaseType.Lg),
            1);

    assertFalse(con.test(PhaseType.P));
  }

  @Test
  void testIsSatisfiedValidatesParameter() {
    assertTrue(
        assertThrows(NullPointerException.class,
            () -> PhaseConstraint.from("phase", Operator.from(Type.IN, false),
                Set.of(PhaseType.P, PhaseType.Lg, PhaseType.S), 1).test(null)).getMessage()
            .contains("queryVal can't be null"));
  }
}
