package gms.shared.mechanisms.configuration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.configuration.Operator.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class OperatorTests {

  @Test
  void testOperatorTruth() {
    Operator op = Operator.from(Operator.Type.EQ, false);

    assertTrue(op.truth(true));
    assertTrue(!op.truth(false));

    op = Operator.from(Operator.Type.EQ, true);

    assertTrue(!op.truth(true));
    assertTrue(op.truth(false));
  }

  @Test
  void testAssertInValidOperator() {
    final Operator operator = Operator.from(Type.IN, false);
    final Type type = Type.EQ;

    assertAll(
        () -> assertThrows(IllegalArgumentException.class, () -> Operator.assertValidOperatorType(operator, type)),
        () -> assertThrows(NullPointerException.class, () -> Operator.assertValidOperatorType(null, type)),
        () -> assertThrows(NullPointerException.class, () -> Operator.assertValidOperatorType(operator,
            (Type) null))
        );
  }

  @Test
  void testAssertValidOperator() {
    Executable assertSuccessTest = () -> Operator
        .assertValidOperatorType(Operator.from(Type.IN, false), Type.EQ, Type.IN);
    assertDoesNotThrow(assertSuccessTest);
  }
}
