package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Range;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility.TimeseriesUtility;
import java.time.Instant;
import java.util.Set;
import org.junit.Test;

public class TimeseriesUtilityTests {

  private final Instant startTime = Instant.EPOCH;
  private final double sampleRate = 5.0;

  @Test
  public void createValidTest() {
    Timeseries t = Waveform.withoutValues(startTime, sampleRate, 5);
    assertEquals(5, t.getSampleCount());
    // 5 samples, one sample every 200ms,
    // end time should be 800ms later (not 1000ms because first sample is at 0)
    assertEquals(Instant.EPOCH.plusMillis(800), t.getEndTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void createNegativeSampleCountTest() {
    Waveform.withoutValues(startTime, sampleRate, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createZeroSampleCountTest() {
    Waveform.withoutValues(startTime, sampleRate, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createNegativeSampleRateTest() {
    Waveform.withoutValues(startTime, -0.005, 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createZeroSampleRateTest() {
    Waveform.withoutValues(startTime, 0, 5);
  }

  @Test
  public void testTimeForSample() {
    Instant start = Instant.EPOCH;
    Timeseries t = Waveform.withoutValues(start, sampleRate, 2000);
    assertEquals(Instant.EPOCH, t.computeSampleTime(0));
    assertEquals(Instant.EPOCH.plusSeconds(1), t.computeSampleTime(5));
    assertEquals(Instant.EPOCH.plusSeconds(20), t.computeSampleTime(100));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTimeForSampleBeyondRangeExpect() {
    Timeseries t = Waveform.withoutValues(Instant.EPOCH, sampleRate, 100);
    t.computeSampleTime(t.getSampleCount());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTimeForSampleBeforeRange() {
    Timeseries t = Waveform.withoutValues(Instant.EPOCH, sampleRate, 100);
    t.computeSampleTime(-1);
  }

  @Test
  public void testTimeRange() {
    Timeseries t = Waveform.withoutValues(startTime, sampleRate, 5);
    Range<Instant> range = t.computeTimeRange();

    Timeseries t2 = Waveform.withoutValues(startTime.plusSeconds(30), sampleRate, 5);
    Range<Instant> range2 = t2.computeTimeRange();

    assertEquals(t.getStartTime(), range.lowerEndpoint());
    assertEquals(t.getEndTime(), range.upperEndpoint());

    assertEquals(t2.getStartTime(), range2.lowerEndpoint());
    assertEquals(t2.getEndTime(), range2.upperEndpoint());
  }

  @Test
  public void testNoneOverlapped() {
    // test: none of the three overlap
    Timeseries t1 = Waveform.withoutValues(startTime, 1.0, 5);
    assertEquals(startTime.plusSeconds(4), t1.getEndTime());  // just checking my math
    Timeseries t2 = Waveform.withoutValues(t1.getEndTime().plusMillis(1), 1.0, 5);
    Timeseries t3 = Waveform.withoutValues(t2.getEndTime().plusMillis(1), 1.0, 5);

    assertTrue(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // test: t1 and t2 overlap on end.  Note the change to 'minusMillis' instead of 'plusMillis'.
    t2 = Waveform.withoutValues(t1.getEndTime().minusMillis(1), 1.0, 5);
    // passing unordered collection (set) to ensure it's not depending on order of a list or something
    assertFalse(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // same thing, but now t1.end = t2.start
    t2 = Waveform.withoutValues(t1.getEndTime(), 1.0, 5);
    assertEquals(t1.getEndTime(), t2.getStartTime());  // just to verify
    assertFalse(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // test: t2 and 53 have overlapping start times
    // below: set t2 back to what it was (doesn't overlap with t1)
    t2 = Waveform.withoutValues(t1.getEndTime().plusMillis(1), 1.0, 5);
    t3 = Waveform.withoutValues(t2.getStartTime().minusMillis(1), 1.0, 5);
    assertFalse(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // same test but now t2 and t3 have equal start times.
    // altering sample count too, otherwise waveforms are identical
    t3 = Waveform.withoutValues(t2.getStartTime(), 1.0, 10);
    assertEquals(t2.getStartTime(), t3.getStartTime()); // just to verify
    assertFalse(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // try single timeseries, should always return false.
    assertTrue(TimeseriesUtility.noneOverlapped(Set.of(t1)));
  }
}
