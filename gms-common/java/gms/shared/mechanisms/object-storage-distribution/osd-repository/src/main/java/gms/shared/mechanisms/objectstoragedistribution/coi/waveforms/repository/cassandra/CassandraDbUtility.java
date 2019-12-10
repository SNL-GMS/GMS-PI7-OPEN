package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;

public class CassandraDbUtility {

  /**
   * Get a list of the days (as LocalDate objects) between to Instant times.  Within Cassandra we use
   * the date (yyyy-mm-dd) as a partition key, so we need to issue a query for each day within the
   * range.
   *
   * @param start Starting time
   * @param end Ending time
   * @return A list of dates returned as LocalDate objects.
   * @throws NullPointerException if any parameter is null or if start is after end.
   */
  public static List<LocalDate> getDays(Instant start, Instant end) {
    Objects.requireNonNull(start);
    Objects.requireNonNull(end);
    Validate.isTrue(!start.isAfter(end), "Start must be <= end");

    final List<LocalDate> days = new ArrayList<>();
    // calculate number of days between the two times;
    // this counts a day whenever a date boundary is crossed.
    final int numDays = Period.between(
        LocalDate.ofInstant(start, ZoneOffset.UTC),
        LocalDate.ofInstant(end, ZoneOffset.UTC))
        .getDays() + 1;
    // add each day between start and including end;
    for (int i = 0; i < numDays; i++) {
      days.add(LocalDate.ofInstant(start.plus(i, ChronoUnit.DAYS), ZoneOffset.UTC));
    }
    return days;
  }

  /**
   * Gets days between two instants as Cassandra LocalDate's (which are not java.time.LocalDate)
   * @param start starting time
   * @param end ending time
   * @return list of Cassandra LocalDate's that span the time range
   */
  public static List<com.datastax.driver.core.LocalDate> getCassandraDays(Instant start, Instant end) {
    return getDays(start, end).stream()
        .map(CassandraDbUtility::toCassandraLocalDate)
        .collect(Collectors.toList());
  }

  /**
   * Converts a java.time.LocalDate into a com.datastax.driver.core.LocalDate for Cassandra.
   * @param date the Java LocalDate
   * @return a Cassandra LocalDate
   */
  public static com.datastax.driver.core.LocalDate toCassandraLocalDate(LocalDate date) {
    Objects.requireNonNull(date);
    return com.datastax.driver.core.LocalDate.fromYearMonthDay(
        date.getYear(), date.getMonthValue(), date.getDayOfMonth());
  }

  /**
   * Calculate the number of seconds that have elapsed.
   *
   * @param startTime The number of milliseconds since the Epoch.
   * @return A double that represents the number of seconds.
   */
  public static double getTime(long startTime) {
    return (System.currentTimeMillis() - startTime) / 1000.0;
  }

  /**
   * Converts an Instant to a long containing the number of nanoseconds since the Unix Epoch.
   *
   * @param i Instant object
   * @return number of nanoseconds since the Unix Epoch
   */
  public static long toEpochNano(Instant i) {
    return i.getEpochSecond() * 1_000_000_000 + i.getNano();
  }

  /**
   * Converts an long containing the number of nanoseconds since the Unix Epoch to an Instant.
   *
   * @param nanos nanoseconds since Unix Epoch
   * @return Instant object
   */
  public static Instant fromEpochNano(long nanos) {
    return Instant.EPOCH.plusNanos(nanos);
  }
}
