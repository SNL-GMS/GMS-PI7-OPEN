package gms.core.signalenhancement.waveformfiltering;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class LinearWaveformFilterPluginTests {

  private LinearWaveformFilterPlugin plugin = new LinearWaveformFilterPlugin();

  @Test
  void testFilterNullArguments() {
    assertThrows(NullPointerException.class,
        () -> plugin.filter(FilterTestData.CHANNEL_SEGMENT, null));

    assertThrows(NullPointerException.class,
        () -> plugin.filter(null, FilterTestData.FIR_FILTER_DEF_FIELD_MAP));
  }

  @Test
  void testFirFilter() {

    Collection<Waveform> waveforms =
        plugin.filter(FilterTestData.CHANNEL_SEGMENT, FilterTestData.FIR_FILTER_DEF_FIELD_MAP);

    assertNotNull(waveforms);
    assertEquals(1, waveforms.size());

    Waveform inputWaveform = FilterTestData.CHANNEL_SEGMENT.getTimeseries().get(0);
    Waveform outputWaveform = waveforms.iterator().next();

    assertEquals(inputWaveform.getStartTime(), outputWaveform.getStartTime());
    assertEquals(inputWaveform.getEndTime(), outputWaveform.getEndTime());
    assertEquals(inputWaveform.getSampleRate(), outputWaveform.getSampleRate(), Double.MIN_NORMAL);
    assertEquals(inputWaveform.getSampleCount(), outputWaveform.getSampleCount());
    assertArrayEquals(FilterTestData.FORWARD_COEFFS, outputWaveform.getValues());
  }

  @Test
  void testFirFilterMerges() {

    Collection<Waveform> filteredWaveforms =
        plugin.filter(FilterTestData.CHANNEL_SEGMENT2, FilterTestData.FIR_FILTER_DEF_FIELD_MAP);

    assertNotNull(filteredWaveforms);
    assertEquals(1, filteredWaveforms.size());

    Waveform inputWaveform1 = FilterTestData.CHANNEL_SEGMENT2.getTimeseries().get(0);
    Waveform inputWaveform2 = FilterTestData.CHANNEL_SEGMENT2.getTimeseries()
        .get(FilterTestData.CHANNEL_SEGMENT2.getTimeseries().size() - 1);
    Waveform outputWaveform = filteredWaveforms.iterator().next();

    assertEquals(inputWaveform1.getStartTime(), outputWaveform.getStartTime());
    assertEquals(inputWaveform2.getEndTime(), outputWaveform.getEndTime());
    assertEquals(inputWaveform1.getSampleRate(), outputWaveform.getSampleRate(), Double.MIN_NORMAL);
    assertEquals(inputWaveform1.getSampleCount() + inputWaveform2.getSampleCount(),
        outputWaveform.getSampleCount());

    // Expected output: FORWARD_COEFFS repeated twice
    final double[] expectedOutput = new double[FilterTestData.FORWARD_COEFFS.length * 2];
    System.arraycopy(FilterTestData.FORWARD_COEFFS, 0, expectedOutput, 0,
        FilterTestData.FORWARD_COEFFS.length);
    System.arraycopy(FilterTestData.FORWARD_COEFFS, 0, expectedOutput,
        FilterTestData.FORWARD_COEFFS.length, FilterTestData.FORWARD_COEFFS.length);

    assertArrayEquals(expectedOutput, outputWaveform.getValues());
  }

  @Test
  void testMultipleWaveforms() {

    final int numCoeffs = FilterTestData.FORWARD_COEFFS.length;
    final double[] low = IntStream.range(0, numCoeffs / 2).mapToDouble(i -> 0.0).toArray();
    final double[] high = IntStream.range(0, numCoeffs * 2).mapToDouble(i -> 1.0).toArray();

    final Duration period = Duration.ofMillis(1000 / 20);
    final Waveform wf1 = Waveform.withValues(Instant.EPOCH, 20.0, low);
    final Waveform wf2 = Waveform.withValues(wf1.getEndTime().plus(period), 20.0, high);
    final Waveform wf3 = Waveform.withValues(wf2.getEndTime().plus(period), 20.0, high);
    final Waveform wf4 = Waveform.withValues(wf3.getEndTime().plus(period), 20.0, high);

    final SortedSet<Waveform> step = new TreeSet<>(List.of(wf1, wf2, wf3, wf4));
    final ChannelSegment<Waveform> segment = ChannelSegment
        .create(UUID.randomUUID(), "TEST", ChannelSegment.Type.FILTER, step, CreationInfo.DEFAULT);

    Collection<Waveform> filteredWaveforms = plugin.filter(segment, FilterTestData.FIR_FILTER_DEF_FIELD_MAP);

    assertNotNull(filteredWaveforms);
    assertEquals(1, filteredWaveforms.size());

    double[] samples = filteredWaveforms.stream().findFirst().get().getValues();

    // Expected results:
    //   Output[0-2]: 0
    //   Output[3]: first filter coefficient
    //   Output[4]: sum of first two filter coefficients
    //   ...
    //   Output[7] sum of first 5 filter coefficients
    //   Output[8-end]: sum of filter coefficients
    // Reach steady state at sample 8, then all values equal to sum of coefficients
    // Ramp time to steady state crosses the boundary between wf1 and wf2

    // Check the initial samples are 0.0
    assertTrue(Arrays.stream(samples, 0, numCoeffs / 2)
        .allMatch(d -> Math.abs(d) < 1e-10));

    // Compute sums of prefixes of the filter coefficients
    final double[] coeffSums = IntStream.range(0, numCoeffs).mapToDouble(
        i -> IntStream.rangeClosed(0, i).mapToDouble(j -> FilterTestData.FORWARD_COEFFS[j]).sum()
    ).toArray();

    // Check the ramp up samples
    final int firstFullSum = numCoeffs + (numCoeffs / 2) - 1;
    assertTrue(IntStream.rangeClosed(3, firstFullSum)
        .mapToDouble(i -> Math.abs(samples[i] - coeffSums[i - 3]))
        .allMatch(d -> d < 1e-10));

    // Check the steady state samples
    assertTrue(Arrays.stream(samples, firstFullSum, samples.length)
        .allMatch(d -> Math.abs(d - coeffSums[coeffSums.length - 1]) < 1e-10));
  }
}
