package gms.shared.mechanisms.configuration.constraints;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import org.junit.jupiter.api.Test;

class NumericRangeConstraintTests {

  private static final Double delta = 1e-15;

  @Test
  void testIN() {
    final Double min = 1.0;
    final NumericRangeConstraint con =
        NumericRangeConstraint.from("snr_range",
            Operator.from(Type.IN, false),
            DoubleRange.fromUnboundedMax(min), 1);

    assertTrue(con.test(1.0));
    assertTrue(!con.test(.5));
  }

  @Test
  void testIsSatisfiedMinimumBoundInclusive() {
    final Double min = 1.0;
    final Double max = 2.0;

    final NumericRangeConstraint con = NumericRangeConstraint
        .from("snr_range", Operator.from(Type.IN, false), DoubleRange.from(min, max), 1);

    assertAll(
        () -> assertFalse(con.test(min - delta)),
        () -> assertTrue(con.test(min)),
        () -> assertTrue(con.test(min + delta))
    );
  }

  @Test
  void testIsSatisfiedMaximumBoundInclusive() {
    final Double min = 1.0;
    final Double max = 2.0;

    final NumericRangeConstraint con = NumericRangeConstraint
        .from("snr_range", Operator.from(Type.IN, false), DoubleRange.from(min, max), 1);

    assertAll(
        () -> assertTrue(con.test(max - delta)),
        () -> assertTrue(con.test(max)),
        () -> assertFalse(con.test(max + delta))
    );
  }

  @Test
  void testFromValidatesArguments() {
    assertEquals("Operator Type: EQ is not supported",
        assertThrows(IllegalArgumentException.class, () -> NumericRangeConstraint
            .from("snr_range", Operator.from(Type.EQ, false), DoubleRange.fromUnboundedMax(3), 1))
            .getMessage());
  }

  @Test
  void testIsSatisfiedValidatesArguments() {
    final NumericRangeConstraint con =
        NumericRangeConstraint.from("snr_range",
            Operator.from(Type.IN, false),
            DoubleRange.fromUnboundedMax(3.0), 1);

    assertTrue(
        assertThrows(NullPointerException.class, () -> con.test(null))
            .getMessage().contains("queryValue can't be null"));
  }
}
