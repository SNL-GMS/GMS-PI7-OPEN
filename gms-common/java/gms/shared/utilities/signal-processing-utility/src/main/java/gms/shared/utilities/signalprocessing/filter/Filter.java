package gms.shared.utilities.signalprocessing.filter;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Utility operations to filter {@link Waveform}s
 */
public class Filter {

  /**
   * Filters a {@link Waveform} using a {@link FilterDefinition}.  Only applies the filter if the
   * {@link Waveform#getSampleRate()} is in the inclusive range of {@link
   * FilterDefinition#getSampleRate()} +/- {@link FilterDefinition#getSampleRateTolerance()}
   *
   * @param waveform Waveform to filter, not null
   * @param filterDefinition FilterDefinition to apply, not null
   * @return filtered Waveform, not null
   * @throws NullPointerException if waveform or filterDefinition are null
   * @throws IllegalArgumentException if filterDefinition is not an FIR filter
   * @throws IllegalArgumentException if waveform sampleRate is not within tolerance of the filter's
   * sampleRate
   */
  public static Waveform filter(Waveform waveform, FilterDefinition filterDefinition) {

    Objects.requireNonNull(waveform, "Filter requires non-null waveform");
    Objects.requireNonNull(filterDefinition, "Filter requires non-null filterDefinition");

    // Determine filter type - currently only support FIR
    final Collection<FilterType> firFilters = List.of(FilterType.FIR_HAMMING);
    if (!firFilters.contains(filterDefinition.getFilterType())) {
      throw new IllegalArgumentException("Only FIR filtering implemented");
    }

    // Verify filter sample rate matches waveform sample rate
    assertSampleRateWithinTolerance(waveform, filterDefinition);

    // Create a new waveform with the same metadata but different samples than the input waveform
    final Function<double[], Waveform> createWaveform = s -> Waveform
        .withValues(waveform.getStartTime(), waveform.getSampleRate(), s);

    // Apply an FIR filter to the input waveform
    final Function<Waveform, double[]> fir = w -> Fir
        .filter(w.getValues(), filterDefinition.getbCoefficients());

    return createWaveform.apply(fir.apply(waveform));
  }

  /**
   * Determines if the {@link Waveform#getSampleRate()} is in the inclusive range of {@link
   * FilterDefinition#getSampleRate()} +/- {@link FilterDefinition#getSampleRateTolerance()}
   *
   * @param waveform {@link Waveform}, not null
   * @param filter {@link FilterDefinition}, not null
   * @throws IllegalArgumentException if waveform sampleRate is not within tolerance of the filter's
   * sampleRate
   */
  private static void assertSampleRateWithinTolerance(Waveform waveform, FilterDefinition filter) {
    final double minSampleRate = filter.getSampleRate() - filter.getSampleRateTolerance();
    final double maxSampleRate = filter.getSampleRate() + filter.getSampleRateTolerance();

    if (waveform.getSampleRate() < minSampleRate || waveform.getSampleRate() > maxSampleRate) {
      throw new IllegalArgumentException(
          "Filter requires input waveform with sampleRate in [" + minSampleRate + ", "
              + maxSampleRate + "]");
    }
  }
}
