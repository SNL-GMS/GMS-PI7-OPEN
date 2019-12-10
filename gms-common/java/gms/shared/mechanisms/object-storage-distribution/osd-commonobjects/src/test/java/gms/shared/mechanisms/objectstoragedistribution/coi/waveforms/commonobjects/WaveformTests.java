package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.TestFixtures;
import java.time.Instant;
import org.junit.jupiter.api.Test;


/**
 * Tests {@link Waveform} creation and usage semantics Created by trsault on 8/25/17.
 */
public class WaveformTests {

  private final Instant startTime = Instant.EPOCH;
  private final double sampleRate = 5.0;
  private final double[] values = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
      16, 17, 18, 19, 20};
  private final Instant endTime = startTime
      .plusMillis(3800); // 20 samples, 200ms apart, starting at 0.
  private final long sampleCount = values.length;
  private final double TOLERANCE = 1e-9;
  private static final Waveform testWf = TestFixtures.waveform1;

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.waveform1, Waveform.class);
  }

  @Test
  public void testWithValues() {
    Instant t = endTime;
    Waveform wf = Waveform.withValues(startTime, sampleRate, values);
    assertEquals(endTime, wf.getEndTime());
  }

  @Test
  public void testWithoutValues() {
    Waveform wf = Waveform.withoutValues(startTime, sampleRate, values.length);
    assertEquals(endTime, wf.getEndTime());
    assertEquals(values.length, wf.getSampleCount());
    assertArrayEquals(new double[]{}, wf.getValues(), 0.000000001);
  }

  @Test
  public void testFrom() {
    Waveform wf = Waveform.from(startTime, sampleRate, values.length, values);
    assertEquals(endTime, wf.getEndTime());
    // check that passing in empty values but non-zero sample count works.
    wf = Waveform.from(startTime, sampleRate, values.length, new double[]{});
    assertEquals(endTime, wf.getEndTime());
  }

  @Test()
  public void testFromWithValuesWrongSampleCount() {
    assertThrows(IllegalArgumentException.class,
        () -> Waveform.from(startTime, sampleRate, values.length + 1, values));
  }

  @Test
  public void testEqualsAndHashcode() {
    TestUtilities.checkClassEqualsAndHashcode(Waveform.class, false);
  }

  @Test
  public void testwithValuesValidatesNullArgs() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        Waveform.class, "withValues",
        startTime, sampleRate, values);
  }

  @Test
  public void testWithoutValuesValidatesNullArgs() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        Waveform.class, "withoutValues",
        startTime, sampleRate, sampleCount);
  }

  @Test
  public void testFromValidatesNullArgs() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
        Waveform.class, "from",
        startTime, sampleRate, sampleCount, values);
  }

  @Test
  public void testWindowWithoutValues() {
    // sample points are [EPOCH, EPOCH + 200ms, EPOCH + 400ms, ... (EPOCH + 3800ms).
    final Waveform wf = Waveform.withoutValues(startTime, sampleRate, 20);
    assertEquals(startTime.plusMillis(3800), wf.getEndTime());  // checking the setup
    final Waveform windowedToSameLength = wf.window(wf.getStartTime(), wf.getEndTime());
    assertEquals(wf, windowedToSameLength);
    assertEquals(wf.getSampleCount(), windowedToSameLength.getSampleCount());
    // window from [start + 700ms, end]; waveform should have the specified start/end
    // and should have 4 less samples - removed samples at times [0, 200ms, 400ms, 600ms].
    Instant originalEnd = wf.getEndTime();
    Waveform windowed = wf.window(wf.getStartTime().plusMillis(700), wf.getEndTime());
    assertNotEquals(windowed, wf);
    assertEquals(wf.getSampleCount() - 4, windowed.getSampleCount());
    // waveform now starts at start + 800ms, which is time of the first sample within the new start bound.
    assertEquals(startTime.plusMillis(800), windowed.getStartTime());
    // end time has not changed
    assertEquals(originalEnd, windowed.getEndTime());
    // now window the end time of the waveform, but leave the start time alone.
    // window the last 2 samples off the waveform by taking off 401 ms (200ms per sample).
    windowed = wf.window(wf.getStartTime(), wf.getEndTime().minusMillis(201));
    assertNotEquals(wf, windowed);
    assertEquals(wf.getStartTime(), windowed.getStartTime());
    // new end time is 600ms previous (excludes samples at 3800ms, 3600ms, leaving 3400ms and 3800ms - 3400ms = 400ms
    assertEquals(wf.getEndTime().minusMillis(400), windowed.getEndTime());
    assertEquals(wf.getSampleCount() - 2, windowed.getSampleCount());
    // now window the start and end time of the waveform.
    // remove 4 samples from left, 2 samples from the right, removing 6 samples total.
    windowed = wf.window(wf.getStartTime().plusMillis(700), wf.getEndTime().minusMillis(201));
    assertNotEquals(wf, windowed);
    assertEquals(startTime.plusMillis(800), windowed.getStartTime());
    // new end time is 600ms previous (excludes samples at 3800ms, 3600ms, leaving 3400ms and 3800ms - 3400ms = 400ms
    assertEquals(wf.getEndTime().minusMillis(400), windowed.getEndTime());
    assertEquals(wf.getSampleCount() - 6, windowed.getSampleCount());
  }

  @Test
  public void testWindowWithValues() {
    // sample points are [EPOCH, EPOCH + 200ms, EPOCH + 400ms, ... (EPOCH + 3800ms).
    final Waveform wf = Waveform.withValues(startTime, sampleRate, values);
    assertEquals(startTime.plusMillis(3800), wf.getEndTime());  // checking the setup
    final Waveform windowedToSameLength = wf.window(wf.getStartTime(), wf.getEndTime());
    assertEquals(wf, windowedToSameLength);
    assertEquals(wf.getSampleCount(), windowedToSameLength.getSampleCount());
    assertArrayEquals(values, wf.getValues(), TOLERANCE);
    // window from [start + 700ms, end]; waveform should have the specified start/end
    // and should have 4 less samples - removed samples at times [0, 200ms, 400ms, 600ms].
    Instant originalEnd = wf.getEndTime();
    Waveform windowed = wf.window(wf.getStartTime().plusMillis(700), wf.getEndTime());
    assertNotEquals(windowed, wf);
    assertEquals(wf.getSampleCount() - 4, windowed.getSampleCount());
    // waveform now starts at start + 800ms, which is time of the first sample within the new start bound.
    assertEquals(startTime.plusMillis(800), windowed.getStartTime());
    // end time has not changed
    assertEquals(originalEnd, windowed.getEndTime());
    // first 4 samples are gone from the waveform, remaining 16 are in place.
    double[] expectedValues = new double[]{5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20};
    assertArrayEquals(expectedValues, windowed.getValues(), TOLERANCE);
    // now window the end time of the waveform, but leave the start time alone.
    // window the last 2 samples off the waveform by taking off 401 ms (200ms per sample).
    windowed = wf.window(wf.getStartTime(), wf.getEndTime().minusMillis(201));
    assertNotEquals(wf, windowed);
    assertEquals(wf.getStartTime(), windowed.getStartTime());
    // new end time is 600ms previous (excludes samples at 3800ms, 3600ms, leaving 3400ms and 3800ms - 3400ms = 400ms
    assertEquals(wf.getEndTime().minusMillis(400), windowed.getEndTime());
    assertEquals(wf.getSampleCount() - 2, windowed.getSampleCount());
    expectedValues = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
    assertArrayEquals(expectedValues, windowed.getValues(), TOLERANCE);
    // now window the start and end time of the waveform.
    // remove 4 samples from left, 2 samples from the right, removing 6 samples total.
    windowed = wf.window(wf.getStartTime().plusMillis(700), wf.getEndTime().minusMillis(201));
    assertNotEquals(wf, windowed);
    assertEquals(startTime.plusMillis(800), windowed.getStartTime());
    // new end time is 600ms previous (excludes samples at 3800ms, 3600ms, leaving 3400ms and 3800ms - 3400ms = 400ms
    assertEquals(wf.getEndTime().minusMillis(400), windowed.getEndTime());
    assertEquals(wf.getSampleCount() - 6, windowed.getSampleCount());
    expectedValues = new double[]{5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
    assertArrayEquals(expectedValues, windowed.getValues(), TOLERANCE);
  }

  @Test()
  public void testWindowStartOutOfRange() {
    // minus 1 seconds makes the requested start time out of range
    assertThrows(IllegalArgumentException.class,
        () -> testWf.window(testWf.getStartTime().minusSeconds(1), testWf.getEndTime()));
  }

  @Test()
  public void testWindowEndOutOfRange() {
    // plus 1 seconds makes the requested end time out of range
    assertThrows(IllegalArgumentException.class,
        () -> testWf.window(testWf.getStartTime(), testWf.getEndTime().plusSeconds(1)));
  }

  @Test()
  public void testWindowStartAfterEnd() {
    // reversing args makes start after end
    assertThrows(IllegalArgumentException.class,
        () -> testWf.window(testWf.getEndTime(), testWf.getStartTime()));
  }

  @Test()
  public void testTrimRangeOutsideBefore() {
    assertThrows(IllegalArgumentException.class, () -> testWf
        .trim(testWf.getStartTime().minusSeconds(5), testWf.getStartTime().minusSeconds(1)));
  }

  @Test()
  public void testTrimRangeOutsideAfter() {
    assertThrows(IllegalArgumentException.class,
        () -> testWf.trim(testWf.getEndTime().plusSeconds(1), testWf.getEndTime().plusSeconds(5)));
  }

  @Test
  public void testTrim() {
    // trim from the left.
    final long samplePeriodNs = (long) (testWf.getSamplePeriod() * 1e9);
    Instant newStart = testWf.getStartTime().plusNanos(samplePeriodNs);
    Waveform trimmedLeft = testWf.trim(newStart, testWf.getEndTime());
    // start time has changed
    assertEquals(newStart, trimmedLeft.getStartTime());
    // end time has not changed
    assertEquals(testWf.getEndTime(), trimmedLeft.getEndTime());
    // one sample has been removed
    assertEquals(testWf.getSampleCount() - 1, trimmedLeft.getSampleCount());
    // what was the first sample is now the 2nd.
    assertEquals(testWf.getValues()[1], trimmedLeft.getValues()[0], TOLERANCE);
    // sample rate has not changed
    assertEquals(testWf.getSampleRate(), trimmedLeft.getSampleRate(), TOLERANCE);
    //////////////////////////////////////////////////////////////////////////////////////
    // trim from the right, similar test.
    Instant newEnd = testWf.getEndTime().minusNanos(samplePeriodNs);
    Waveform trimmedRight = testWf.trim(testWf.getStartTime(), newEnd);
    // start time has not changed
    assertEquals(testWf.getStartTime(), trimmedRight.getStartTime());
    // end time has changed
    assertEquals(newEnd, trimmedRight.getEndTime());
    // one sample has been removed
    assertEquals(testWf.getSampleCount() - 1, trimmedRight.getSampleCount());
    // what was the last sample is now the second-to-last.
    assertEquals(
        testWf.getValues()[testWf.getValues().length - 2],
        trimmedRight.getValues()[trimmedRight.getValues().length - 1],
        TOLERANCE);
    // sample rate has not changed
    assertEquals(testWf.getSampleRate(), trimmedRight.getSampleRate(), TOLERANCE);
    //////////////////////////////////////////////////////////////////////////////////////
    // trim from both sides.
    Waveform trimmedBoth = testWf.trim(newStart, newEnd);
    // start time has changed
    assertEquals(newStart, trimmedBoth.getStartTime());
    // end time has changed
    assertEquals(newEnd, trimmedBoth.getEndTime());
    // two samples have been removed (one from each side)
    assertEquals(testWf.getSampleCount() - 2, trimmedBoth.getSampleCount());
    // what was the first sample is now the 2nd.
    assertEquals(testWf.getValues()[1], trimmedBoth.getValues()[0], TOLERANCE);
    // what was the last sample is now the second-to-last.
    assertEquals(
        testWf.getValues()[testWf.getValues().length - 2],
        trimmedBoth.getValues()[trimmedBoth.getValues().length - 1],
        TOLERANCE);
    // sample rate has not changed
    assertEquals(testWf.getSampleRate(), trimmedBoth.getSampleRate(), TOLERANCE);
    // trim to outside range of waveform on both sides, assert nothing changes.
    Waveform trimmedBothOutsideRange = testWf.trim(
        testWf.getStartTime().minusSeconds(1), testWf.getEndTime().plusSeconds(1));
    assertEquals(testWf, trimmedBothOutsideRange);
  }
}
