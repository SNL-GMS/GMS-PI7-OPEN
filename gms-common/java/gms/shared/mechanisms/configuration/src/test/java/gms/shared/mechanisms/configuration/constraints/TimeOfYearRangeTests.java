package gms.shared.mechanisms.configuration.constraints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TimeOfYearRangeTests {

  @Test
  void testMonthDayYearMinIsALeapYear() {
    LocalDateTime nonLeapYear = LocalDateTime.of(1970, 2, 28, 15, 38);
    LocalDateTime leapYear = TimeOfYearRange.toMonthDayHourMin(nonLeapYear);
    assertTrue(LocalDate.of(leapYear.getYear(), leapYear.getMonth(), leapYear.getDayOfMonth())
        .isLeapYear());
  }

  @Test
  void testMonthDayYearMin() {
    // 2012 is a leap year
    assertEquals(LocalDateTime.of(TimeOfYearRange.DEFAULT_LEAP_YEAR, 1, 7, 15, 38),
        TimeOfYearRange.toMonthDayHourMin(LocalDateTime.of(2019, 1, 7, 15, 38, 45, 10))
    );
  }

  @Test
  void testTimeRangeSpansYear() {
    TimeOfYearRange tr = TimeOfYearRange
        .from(LocalDateTime.parse("1970-12-31T00:00:00"),
            LocalDateTime.parse("1970-01-02T00:00:00"));
    assertTrue(tr.contains(LocalDateTime.MAX));
    assertTrue(tr.contains(LocalDateTime.MIN));

    assertTrue(tr.contains(LocalDateTime.MAX, false, false));
    assertTrue(tr.contains(LocalDateTime.MIN, false, false));

  }

  @Test
  void testBoundsExclusive() {
    TimeOfYearRange tr = TimeOfYearRange
        .from(LocalDateTime.parse("1970-10-10T23:00:00"), LocalDateTime.MAX);
    assertFalse(tr.contains(LocalDateTime.parse("1970-10-10T23:00:00"), false, true));
    assertFalse(tr.contains(LocalDateTime.MAX, true, false));
  }

  @Test
  void testBoundsInclusive() {
    TimeOfYearRange tr = TimeOfYearRange
        .from(LocalDateTime.parse("1970-10-10T23:00:00"), LocalDateTime.MAX);
    assertTrue(tr.contains(LocalDateTime.parse("1970-10-10T23:00:00"), true, false));
    assertTrue(tr.contains(LocalDateTime.MAX, false, true));
  }

  @Test
  void testDefaultBounds() {
    TimeOfYearRange tr = TimeOfYearRange
        .from(LocalDateTime.parse("1970-10-10T23:00:00"), LocalDateTime.MAX);

    // defaults to lower bound inclusive
    assertTrue(tr.contains(LocalDateTime.parse("1970-10-10T23:00:00")));

    // defaults to upper bound exclusive
    assertFalse(tr.contains(LocalDateTime.MAX));
  }
}
