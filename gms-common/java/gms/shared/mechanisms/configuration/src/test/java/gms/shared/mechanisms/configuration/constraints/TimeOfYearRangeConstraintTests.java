package gms.shared.mechanisms.configuration.constraints;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TimeOfYearRangeConstraintTests {

  @Test
  void testFromValidatesParameters() {

    final TimeOfYearRange timeOfYearRange = TimeOfYearRange.from(
        LocalDateTime.parse("1970-12-31T00:00:00"), LocalDateTime.parse("1970-01-02T00:00:00"));

    assertEquals("Operator Type: EQ is not supported", assertThrows(IllegalArgumentException.class,
        () -> TimeOfYearRangeConstraint.from("A", Operator.from(
            Type.EQ, false), timeOfYearRange, 1)).getMessage());
  }

  @Test
  void testIsSatisfied() {
    final LocalDateTime min = LocalDateTime.parse("1970-01-02T00:00:00");
    final LocalDateTime max = LocalDateTime.parse("1970-12-31T00:00:00");
    final TimeOfYearRange timeOfYearRange = TimeOfYearRange.from(min, max);

    final TimeOfYearRangeConstraint con = TimeOfYearRangeConstraint.from("A", Operator.from(
        Type.IN, false), timeOfYearRange, 1);

    assertAll(
        () -> assertTrue(con.test(min)),
        () -> assertFalse(con.test(min.minus(Duration.ofNanos(1)))),
        () -> assertTrue(con.test(max.minus(Duration.ofNanos(1)))),
        () -> assertFalse(con.test(max))
    );
  }

  @Test
  void testIsSatisfiedValidatesArguments() {
    final TimeOfYearRange timeOfYearRange = TimeOfYearRange.from(
        LocalDateTime.parse("1970-12-31T00:00:00"), LocalDateTime.parse("1970-01-02T00:00:00"));

    final TimeOfYearRangeConstraint con = TimeOfYearRangeConstraint.from("A", Operator.from(
        Type.IN, false), timeOfYearRange, 1);

    assertTrue(
        assertThrows(NullPointerException.class, () -> con.test(null))
            .getMessage().contains("queryVal can't be null"));
  }
}
