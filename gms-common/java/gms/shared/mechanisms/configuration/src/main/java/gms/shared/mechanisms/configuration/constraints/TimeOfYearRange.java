package gms.shared.mechanisms.configuration.constraints;

import static java.util.concurrent.TimeUnit.DAYS;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A {@link ModuloRange} using {@link LocalDateTime} values.
 */
@AutoValue
public abstract class TimeOfYearRange extends ModuloRange<LocalDateTime> {

  // This value must be set to a leap year.  This ensures that
  // when a user passes in February 29th of a leap year to the contains() method
  // the code won't through an exception.
  static final int DEFAULT_LEAP_YEAR = 2016;

  @JsonCreator
  public static TimeOfYearRange from(
      @JsonProperty("min") LocalDateTime min,
      @JsonProperty("max") LocalDateTime max) {

    min = toMonthDayHourMin(min);
    max = toMonthDayHourMin(max);

    return new AutoValue_TimeOfYearRange(min, max);
  }

  public long getDuration(LocalDateTime min, LocalDateTime max) {
    return Duration.between(min, max).toMinutes();
  }

  TimeOfYearRange() {
    // NOTE: we are using the number of days in a leap year.  This is because
    // we always set the year of min and max time to a leap year.  The use case for this
    // is on leap years we could be processing datastream from 2/29.
    super(DAYS.toMinutes(LocalDate.ofYearDay(DEFAULT_LEAP_YEAR, 1).lengthOfYear()));
  }

  static LocalDateTime toMonthDayHourMin(LocalDateTime val) {
    //notice that we overwrite the year with a leap year.  This ensures that if a user passes in
    // yyyy-02-29, where yyyy is a leap year, the contains() method will still work.
    return LocalDateTime
        .of(DEFAULT_LEAP_YEAR, val.getMonth(), val.getDayOfMonth(), val.getHour(), val.getMinute());
  }

  public LocalDateTime toSupportedValue(LocalDateTime val) {
    return toMonthDayHourMin(val);
  }

  public abstract LocalDateTime getMin();

  public abstract LocalDateTime getMax();
}
