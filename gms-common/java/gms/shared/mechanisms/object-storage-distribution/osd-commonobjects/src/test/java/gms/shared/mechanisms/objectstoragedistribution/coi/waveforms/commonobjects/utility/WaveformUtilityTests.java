package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class WaveformUtilityTests {

  private static final Waveform waveform1;

  private static final Waveform waveform2InterpolatedGap2;
  private static final Waveform waveform2InterpolatedGap4;
  private static final Waveform waveform2InterpolatedGap1_5;

  private static final Waveform waveform2MergedGap1;
  private static final Waveform waveform2MergedGap2;

  private static final Waveform waveform3;

  private static final List<Waveform> gap1Waveforms;
  private static final List<Waveform> gap2Waveforms;
  private static final List<Waveform> differentSampleRateWaveforms;
  private static final List<Waveform> interpolatedGap1Waveforms;
  private static final List<Waveform> interpolatedGap2Waveforms;
  private static final List<Waveform> interpolatedGap4Waveforms;
  private static final List<Waveform> interpolatedGap1_5Waveforms;

  static {
    Instant startTime = Instant.EPOCH;

    double[] samples = getDoubleArray(41, 1.0);
    waveform1 = Waveform.withValues(startTime, 40.0, samples);
    samples = getDoubleArray(41, 3.0);
    waveform3 = Waveform.withValues(startTime.plusSeconds(2), 40.0, samples);

    samples = getDoubleArray(21, 1.0);
    Waveform waveform2HalfSampleRate = Waveform.withValues(
        startTime.plusSeconds(1).plusMillis(25), 20.0, samples);

    differentSampleRateWaveforms = List.of(waveform1, waveform2HalfSampleRate, waveform3);

    // Used to test no interpolated waveforms when gaps less than 1.5 sample time difference exist
    samples = getDoubleArray(39, 2.0);
    Waveform waveform2InterpolatedGap1 = Waveform
        .withValues(waveform1.getEndTime().plusMillis(25),
            40.0, samples);

    interpolatedGap1Waveforms = List.of(waveform1, waveform2InterpolatedGap1, waveform3);

    // Used to test production of two interpolated waveforms with 1 point each (limiting)
    samples = getDoubleArray(37, 2.0);
    waveform2InterpolatedGap2 = Waveform.withValues(waveform1.getEndTime().plusMillis(50),
        40.0, samples);

    interpolatedGap2Waveforms = List.of(waveform1, waveform2InterpolatedGap2, waveform3);

    // Used to test production of two interpolated waveforms with 3 points each
    samples = getDoubleArray(33, 2.0);
    waveform2InterpolatedGap4 = Waveform.withValues(waveform1.getEndTime().plusMillis(100),
        40.0, samples);

    interpolatedGap4Waveforms = List.of(waveform1, waveform2InterpolatedGap4, waveform3);

    // Used to test sensitivity to the 1.5 sample time gap difference
    samples = getDoubleArray(38, 2.0);
    Instant startTime1_5 = waveform1.getEndTime().plusMillis(37);
    Instant endTime1_5 = startTime.plusSeconds(2).minusMillis(38);
    double sampleRate1_5 =
        37.0 / ((double) Duration.between(startTime1_5, endTime1_5).toNanos() / 1000000000);
    waveform2InterpolatedGap1_5 = Waveform.withValues(startTime1_5, sampleRate1_5, samples);

    interpolatedGap1_5Waveforms = List.of(waveform1, waveform2InterpolatedGap1_5, waveform3);

    // Create a merged gap waveform set with a different middle waveform so that the gaps produced
    // are slightly less than (by 2 nanoseconds) than a single sample period between the waveforms.
    samples = getDoubleArray(39, 2.0);
    Instant startTimeMergedGap1 = startTime.plusSeconds(1).plusMillis(25).minusNanos(2);
    Instant endTimeMergedGap1 = startTime.plusSeconds(2).minusMillis(25).plusNanos(2);
    double sampleRateMergedGap1 =
        38.0 / ((double) Duration.between(startTimeMergedGap1, endTimeMergedGap1).toNanos()
            / 1000000000);
    waveform2MergedGap1 = Waveform.withValues(startTimeMergedGap1, sampleRateMergedGap1, samples);
    gap1Waveforms = List.of(waveform1, waveform2MergedGap1, waveform3);

    // Create a merged gap waveform set such that the gap between this waveform and the first
    // waveform is the same as in waveform2MergedGap1 but the gap between this waveform and the
    // third waveform is 2 samples wide. This gap will fail to be merged because it will be larger
    // than the 1.5 sample period allowed for forming merged waveforms.
    samples = getDoubleArray(38, 2.0);
    Instant startTimeMergedGap2 = startTime.plusSeconds(1).plusMillis(25).minusNanos(2);
    Instant endTimeMergedGap2 = startTime.plusSeconds(2).minusMillis(50).plusNanos(2);
    double sampleRateMergedGap2 =
        37.0 / ((double) Duration.between(startTimeMergedGap2, endTimeMergedGap2).toNanos()
            / 1000000000);
    waveform2MergedGap2 = Waveform.withValues(startTimeMergedGap2, sampleRateMergedGap2, samples);
    gap2Waveforms = List.of(waveform1, waveform2MergedGap2, waveform3);
  }


  /**
   * Test merged waveforms where gaps are a couple nanoseconds less than the minimumGapLimit (1.0)
   * so no merged waveforms are created.
   */
  @Test
  public void testMergedWaveformMinLimitGap() {
    List<Waveform> mergedWaveforms = WaveformUtility.mergeWaveforms(
        gap1Waveforms, .01, 1.1);
    assertEquals(gap1Waveforms, mergedWaveforms);
  }

  /**
   * Test merged waveforms where sample rates are different (1st and 3rd waveforms have sample rates
   * of 40 samples per second, while the middle waveform is half of that). No merged waveforms
   * should be created
   */
  @Test
  public void testMergedWaveformDifferentSampleRates() {
    List<Waveform> mergedWaveforms = WaveformUtility.mergeWaveforms(
        differentSampleRateWaveforms, 2.0, 1.0);
    assertEquals(differentSampleRateWaveforms, mergedWaveforms);
  }

  /**
   * Test merged waveforms where gaps are a couple nanoseconds less than 1.0 with a minimumGapLimit
   * of .9. Both gaps should be merged and a single waveform should be returned.
   */
  @Test
  public void testMergedWaveformGap1() {
    List<Waveform> mergedGap1 = WaveformUtility.mergeWaveforms(
        gap1Waveforms, 1.25, 0.9);

    assertEquals(1, mergedGap1.size());

    Waveform waveform = mergedGap1.get(0);
    assertEquals(waveform.getSampleCount(), waveform1.getSampleCount() +
        waveform2MergedGap1.getSampleCount() + waveform3.getSampleCount());
    assertEquals(waveform.getStartTime(), waveform1.getStartTime());
    assertEquals(waveform.getEndTime(), waveform3.getEndTime());

    for (int i = 0; i < waveform1.getValues().length; ++i) {
      assertEquals(waveform.getValues()[i], waveform1.getValues()[i], 0.0);
    }
    for (int i = 0; i < waveform2MergedGap1.getValues().length; ++i) {
      assertEquals(waveform.getValues()[i + (int) waveform1.getSampleCount()],
          waveform2MergedGap1.getValues()[i], 0.0);
    }
    for (int i = 0; i < waveform3.getValues().length; ++i) {
      assertEquals(waveform.getValues()[i + (int) (waveform1.getSampleCount() +
              waveform2MergedGap1.getSampleCount())],
          waveform3.getValues()[i], 0.0);
    }

    assertEquals(waveform.getSampleRate(), (double) (waveform.getSampleCount() - 1) /
            ((double) Duration.between(waveform.getStartTime(), waveform.getEndTime()).toNanos()
                / 1000000000),
        0.0);
  }

  /**
   * Test merged waveforms where gaps are a couple nanoseconds less than 1.0 for the gap between the
   * first and second waveforms and the gap between the second and third waveforms is larger than
   * 1.5 (2.0) and thus is not produced. The result is that the first two waveforms are merged and
   * the third waveform is returned without modification.
   */
  @Test
  public void testMergedWaveformGap2() {
    List<Waveform> mergedGap2 = WaveformUtility.mergeWaveforms(
        gap2Waveforms, 1.25, 0.9);

    assertEquals(2, mergedGap2.size());

    // get the first waveform and validate it is a merged waveforms formed by merging the first and
    // the second waveform
    Waveform waveform = mergedGap2.get(0);
    assertEquals(waveform.getSampleCount(), waveform1.getSampleCount() +
        waveform2MergedGap2.getSampleCount());
    assertEquals(waveform.getStartTime(), waveform1.getStartTime());
    assertEquals(waveform.getEndTime().getNano(), waveform2MergedGap2.getEndTime().getNano(), 2);

    for (int i = 0; i < waveform1.getValues().length; ++i) {
      assertEquals(waveform.getValues()[i], waveform1.getValues()[i], 0.0);
    }
    for (int i = 0; i < waveform2MergedGap2.getValues().length; ++i) {
      assertEquals(waveform.getValues()[i + (int) waveform1.getSampleCount()],
          waveform2MergedGap2.getValues()[i], 0.0);
    }

    assertEquals(waveform.getSampleRate(), (double) (waveform.getSampleCount() - 1) /
            ((double) Duration.between(waveform.getStartTime(), waveform.getEndTime()).toNanos()
                / 1000000000),
        1e-7);

    // verify last waveform is waveform3
    assertEquals(mergedGap2.get(1), waveform3);
  }

  /**
   * Test that no waveforms are interpolated if the gaps exceed the maximumGapLimit setting. Using
   * interpolatedGap2Waveforms where the gap sample periods are 2.5 and setting the limit to 2.0.
   */
  @Test
  public void testInterpolatedWaveformMaxLimitGap() {
    List<Waveform> waveforms = WaveformUtility.interpolateWaveformGap(
        interpolatedGap2Waveforms, .01, 2.0);
    assertEquals(interpolatedGap2Waveforms, waveforms);
  }

  /**
   * Test interpolated waveforms where sample rates are different (1st and 3rd waveforms have sample
   * rates of 40 samples per second, while the middle waveform is half of that). No interpolated
   * waveforms should be created
   */
  @Test
  public void testInterpolatedWaveformDifferentSampleRates() {
    List<Waveform> mergedWaveforms = WaveformUtility.interpolateWaveformGap(
        differentSampleRateWaveforms, 2.0, 1.0);
    assertEquals(differentSampleRateWaveforms, mergedWaveforms);
  }

  /**
   * Test where the gaps are small so no interpolation is performed. Output waveforms are the same
   * as the original waveforms.
   */
  @Test
  public void testInterpolateWaveformGap1() {
    List<Waveform> waveforms = WaveformUtility.interpolateWaveformGap(
        interpolatedGap1Waveforms, .01, 4.0);
    assertEquals(interpolatedGap1Waveforms, waveforms);
  }

  /**
   * Test where two gaps exist between three consecutive waveforms that are separated by 2 data
   * samples each. In this case two new waveforms are inserted between the 3 existing waveforms.
   * This test verifies the interpolation of samples in the new interpolated waveforms each
   * containing 1 sample point.
   */
  @Test
  public void testInterpolateWaveformGap2() {
    List<Waveform> waveforms = WaveformUtility.interpolateWaveformGap(
        interpolatedGap2Waveforms, .01, 4.0);

    // Original 3 waveforms and 2 new interpolated waveforms
    assertEquals(5, waveforms.size());

    // iterate across the original ('before') and produced ('after') waveforms to verify their
    // correctness
    Iterator<Waveform> beforeIterator = interpolatedGap2Waveforms.iterator();
    Iterator<Waveform> afterIterator = waveforms.iterator();

    // first waveforms are the same (waveform1)
    Waveform beforeWaveform = beforeIterator.next();
    Waveform afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform1, afterWaveform);

    // third waveform of 'after' is the same as second waveform of 'before' (waveform2InterpolatedGap2)
    // second waveform of 'after' is first interpolated waveform
    beforeWaveform = beforeIterator.next();
    Waveform waveformInterp1 = afterIterator.next();
    ;
    afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform2InterpolatedGap2, afterWaveform);

    // last waveform of 'before' and 'after' are the same (waveform3)
    // second to last waveform of 'after' is second interpolated waveform
    beforeWaveform = beforeIterator.next();
    Waveform waveformInterp2 = afterIterator.next();
    ;
    afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform3, afterWaveform);

    // validate first interpolated waveform (1 point)
    assertEquals(1, waveformInterp1.getSampleCount());
    assertEquals(
        waveform1.getEndTime().plusNanos((long) (1000000000 / waveformInterp1.getSampleRate())),
        waveformInterp1.getStartTime());
    assertEquals(waveform2InterpolatedGap2.getStartTime()
            .minusNanos((long) (1000000000 / waveformInterp1.getSampleRate())),
        waveformInterp1.getEndTime());
    assertEquals(waveform2InterpolatedGap2.getSampleRate(), waveformInterp1.getSampleRate(),
        1.0e-9);
    assertEquals(0.5 * (waveform2InterpolatedGap2.getFirstSample() + waveform1.getLastSample()),
        waveformInterp1.getValues()[0], 1.0e-9);

    // validate second interpolated waveform (1 point)
    assertEquals(1, waveformInterp2.getSampleCount());
    assertEquals(waveform2InterpolatedGap2.getEndTime()
            .plusNanos((long) (1000000000 / waveformInterp2.getSampleRate())),
        waveformInterp2.getStartTime());
    assertEquals(
        waveform3.getStartTime().minusNanos((long) (1000000000 / waveformInterp2.getSampleRate())),
        waveformInterp2.getEndTime());
    assertEquals(waveform2InterpolatedGap2.getSampleRate(), waveformInterp2.getSampleRate(),
        1.0e-9);
    assertEquals(0.5 * (waveform2InterpolatedGap2.getLastSample() + waveform3.getFirstSample()),
        waveformInterp2.getValues()[0], 1.0e-9);
  }

  /**
   * Test where two gaps exist between three consecutive waveforms that are separated by 4 data
   * samples each. In this case two new waveforms are inserted between the 3 existing waveforms.
   * This test verifies the interpolation of samples in the new interpolated waveforms each
   * containing 3 points.
   */
  @Test
  public void testInterpolateWaveformGap4() {
    List<Waveform> waveforms = WaveformUtility.interpolateWaveformGap(
        interpolatedGap4Waveforms, .01, 5.0);

    // Original 3 waveforms and 2 new interpolated waveforms
    assertEquals(5, waveforms.size());

    // iterate across the original ('before') and produced ('after') waveforms to verify their
    // correctness
    Iterator<Waveform> beforeIterator = interpolatedGap4Waveforms.iterator();
    Iterator<Waveform> afterIterator = waveforms.iterator();

    // first waveforms are the same (waveform1)
    Waveform beforeWaveform = beforeIterator.next();
    Waveform afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform1, afterWaveform);

    // third waveform of 'after' is the same as second waveform of 'before' (waveform2InterpolatedGap4)
    // second waveform of 'after' is first interpolated waveform
    beforeWaveform = beforeIterator.next();
    Waveform waveformInterp1 = afterIterator.next();
    ;
    afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform2InterpolatedGap4, afterWaveform);

    // last waveform of 'before' and 'after' are the same (waveform3)
    // second to last waveform of 'after' is second interpolated waveform
    beforeWaveform = beforeIterator.next();
    Waveform waveformInterp2 = afterIterator.next();
    ;
    afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform3, afterWaveform);

    // validate first interpolated waveform (3 points)
    assertEquals(3, waveformInterp1.getSampleCount());
    assertEquals(
        waveform1.getEndTime().plusNanos((long) (1000000000 / waveformInterp1.getSampleRate())),
        waveformInterp1.getStartTime());
    assertEquals(waveform2InterpolatedGap4.getStartTime()
            .minusNanos((long) (1000000000 / waveformInterp1.getSampleRate())),
        waveformInterp1.getEndTime());
    assertEquals(waveform2InterpolatedGap4.getSampleRate(), waveformInterp1.getSampleRate(),
        1.0e-9);

    double delta = (waveform2InterpolatedGap4.getFirstSample() - waveform1.getLastSample()) /
        (waveformInterp1.getSampleCount() + 1);
    for (int i = 0; i < waveformInterp1.getSampleCount(); ++i) {
      assertEquals(delta * (i + 1) + waveform1.getLastSample(),
          waveformInterp1.getValues()[i], 1.0e-9);
    }

    // validate second interpolated waveform (3 points)
    assertEquals(3, waveformInterp2.getSampleCount());
    assertEquals(waveform2InterpolatedGap4.getEndTime()
            .plusNanos((long) (1000000000 / waveformInterp2.getSampleRate())),
        waveformInterp2.getStartTime());
    assertEquals(
        waveform3.getStartTime().minusNanos((long) (1000000000 / waveformInterp2.getSampleRate())),
        waveformInterp2.getEndTime());
    assertEquals(waveform2InterpolatedGap4.getSampleRate(), waveformInterp2.getSampleRate(),
        1.0e-9);

    delta = (waveform3.getFirstSample() - waveform2InterpolatedGap4.getLastSample()) /
        (waveformInterp2.getSampleCount() + 1);
    for (int i = 0; i < waveformInterp2.getSampleCount(); ++i) {
      assertEquals(delta * (i + 1) + waveform2InterpolatedGap4.getLastSample(),
          waveformInterp2.getValues()[i], 1.0e-9);
    }
  }

  /**
   * Tests that interpolated waveforms are inserted when the the sample time difference (using a
   * mean sample rate between the adjacent waveforms) between two consecutive waveforms are just
   * smaller than and just larger than 1.5. In this case the first and second waveforms are
   * separated by a 1.48 sample period and the second and third waveforms are separated by 1.52 a
   * 1.52 sample period. Proper evaluation inserts a single new interpolated waveform between the
   * second and third waveforms, but not between the first and second waveforms since it's sample
   * time difference is less than 1.5.
   */
  @Test
  public void testInterpolateWaveformGap1_5() {
    List<Waveform> waveforms = WaveformUtility.interpolateWaveformGap(
        interpolatedGap1_5Waveforms, .01, 4.0);

    assertEquals(4, waveforms.size());

    // iterate across the original ('before') and produced ('after') waveforms to verify their
    // correctness
    Iterator<Waveform> beforeIterator = interpolatedGap1_5Waveforms.iterator();
    Iterator<Waveform> afterIterator = waveforms.iterator();

    // first waveforms are the same (waveform1)
    Waveform beforeWaveform = beforeIterator.next();
    Waveform afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform1, afterWaveform);

    // second waveforms are the same (waveform2Gap1_5)
    beforeWaveform = beforeIterator.next();
    afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform2InterpolatedGap1_5, afterWaveform);

    // last waveform of 'before' and 'after' are the same (waveform3)
    // second to last waveform of 'after' is second interpolated waveform
    beforeWaveform = beforeIterator.next();
    Waveform waveformInterp2 = afterIterator.next();
    ;
    afterWaveform = afterIterator.next();
    assertEquals(beforeWaveform, afterWaveform);
    assertEquals(waveform3, afterWaveform);

    // validate interpolated waveform (1 point)
    assertEquals(1, waveformInterp2.getSampleCount());
    assertEquals(waveform2InterpolatedGap1_5.getEndTime()
            .plusNanos((long) (1000000000 / waveformInterp2.getSampleRate())),
        waveformInterp2.getStartTime());
    assertEquals(
        waveform3.getStartTime().minusNanos((long) (1000000000 / waveformInterp2.getSampleRate())),
        waveformInterp2.getEndTime());

    double gapTimeWidth = (double) Duration.between(waveform2InterpolatedGap1_5.getEndTime(),
        waveform3.getStartTime()).toNanos() / 1000000000;
    double sampleRate = (double) (waveformInterp2.getSampleCount() + 1) / gapTimeWidth;
    assertEquals(sampleRate, waveformInterp2.getSampleRate(), 1.0e-9);

    assertEquals(0.5 * (waveform2InterpolatedGap1_5.getLastSample() + waveform3.getFirstSample()),
        waveformInterp2.getValues()[0], 1.0e-9);
  }

  @Test
  public void testMergeWaveformsNoValues() {
    Waveform waveform1 = Waveform.withoutValues(Instant.ofEpochSecond(0),
        60.0, 600);
    Waveform waveform2 = Waveform.withoutValues(Instant.ofEpochSecond(0).plusSeconds(10),
        60.0, 600);

    List<Waveform> actualWaveforms = WaveformUtility.mergeWaveforms(List.of(waveform1, waveform2),
        0.0, 0.0);

    assertNotNull(actualWaveforms);
    assertEquals(1, actualWaveforms.size());

    Waveform actualWaveform = actualWaveforms.get(0);

    assertEquals(Instant.ofEpochSecond(0), actualWaveform.getStartTime());
    assertEquals(1200, actualWaveform.getSampleCount());
    assertEquals(60.0, actualWaveform.getSampleRate(), 1E-5);
    assertEquals(0, actualWaveform.getValues().length);
  }

  private static double[] getDoubleArray(int n, double value) {
    double[] a = new double[n];
    Arrays.fill(a, value);
    return a;
  }

}
