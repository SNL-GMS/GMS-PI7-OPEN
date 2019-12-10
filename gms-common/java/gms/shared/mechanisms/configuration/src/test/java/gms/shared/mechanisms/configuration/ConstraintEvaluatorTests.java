package gms.shared.mechanisms.configuration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.configuration.Operator.Type;
import gms.shared.mechanisms.configuration.constraints.NumericScalarConstraint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConstraintEvaluatorTests {

  private NumericScalarConstraint constraint;

  @BeforeEach
  void setUp() throws Exception {
    constraint = NumericScalarConstraint
        .from("criterion", Operator.from(Type.EQ, false), 10.0, 10);
  }

  @Test
  public void testEvaluateExpectTrue() {
    assertTrue(ConstraintEvaluator.evaluate(constraint, Selector.from("criterion", 10.0)));
  }

  @Test
  public void testEvaluateDifferentCriterionExpectFalse() {
    assertFalse(
        ConstraintEvaluator.evaluate(constraint, Selector.from("different criterion", 10.0)));
  }

  @Test
  public void testEvaluateDifferentValueExpectFalse() {
    assertFalse(
        ConstraintEvaluator.evaluate(constraint, Selector.from("criterion", -10.0)));
  }
}
