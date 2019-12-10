package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.cassandra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;

public class CassandraDbUtiltyTest {

  @Test
  public void getDaysTest() {
    final LocalDate epochDate = LocalDate.parse("1970-01-01");
    List<LocalDate> days;
    // range of [EPOCH, EPOCH + 1 min].  Should only find one day, EPOCH.
    days = CassandraDbUtility.getDays(Instant.EPOCH,
        Instant.EPOCH.plusSeconds(60));
    assertNotNull(days);
    assertEquals(1, days.size());
    LocalDate onlyDay = days.iterator().next();
    assertNotNull(onlyDay);
    assertEquals(epochDate, onlyDay);
    // range of [EPOCH, EPOCH + numDays].
    // Should find [EPOCH, EPOCH + 1 day, EPOCH + 2 day, ... EPOCH + n-1 days].
    final int numDays = 3;
    days = CassandraDbUtility.getDays(Instant.EPOCH,
        Instant.EPOCH.plus(numDays, ChronoUnit.DAYS));
    assertNotNull(days);
    assertEquals(numDays + 1, days.size());
    final List<LocalDate> expectedDays = IntStream.range(0, numDays+1)
        .mapToObj(epochDate::plusDays)
        .collect(Collectors.toList());
    assertEquals(expectedDays, days);
  }

  @Test
  public void testGetDaysSpansDayBoundaryButTimeRangeLessThanADay() {
    // two dates
    final String startDateStr = "1970-01-07", endDateStr = "1970-01-08";
    // two Instant's around a date boundary
    final Instant start = Instant.parse(startDateStr + "T23:59:59Z");
    final Instant end = Instant.parse(endDateStr + "T00:00:04.975Z");
    final List<LocalDate> days = CassandraDbUtility.getDays(start, end);
    assertNotNull(days);
    assertEquals(2, days.size());
    assertEquals(LocalDate.parse(startDateStr), days.get(0));
    assertEquals(LocalDate.parse(endDateStr), days.get(1));
  }

  @Test
  public void getTimeTest() {
    long t0 = System.currentTimeMillis() - 5;
    double t = CassandraDbUtility.getTime(t0);
    assertTrue(t > 0.0);
  }

}
