package gms.shared.mechanisms.configuration.constraints;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class TimeOfDayRangeConstraintTests {

  private static final LocalTime min = LocalTime.parse("23:00:00");
  private static final LocalTime max = LocalTime.parse("01:00:00");

  private TimeOfDayRangeConstraint con =
      TimeOfDayRangeConstraint.from("time_of_day",
          Operator.from(Type.IN, false),
          TimeOfDayRange.from(min, max), 1);

  @Test
  void testIN() {
    assertTrue(con.test(min));
    assertTrue(con.test(LocalTime.MIDNIGHT));
    assertTrue(con.test(LocalTime.MAX));
    assertTrue(con.test(LocalTime.MIN));

    // by default time constraint is upper bound exclusive
    assertFalse(con.test(max));
  }

  @Test
  void testIsSatisfiedValidatesArguments() {
    assertTrue(
        assertThrows(NullPointerException.class, () -> con.test(null))
            .getMessage().contains("queryVal can't be null"));
  }
}
