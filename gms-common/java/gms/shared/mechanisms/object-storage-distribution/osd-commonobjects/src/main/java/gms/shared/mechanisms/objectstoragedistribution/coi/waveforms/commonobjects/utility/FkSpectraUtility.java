package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.utility;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectra;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.FkSpectrum;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for manipulating FkSpectra objects
 */
public class FkSpectraUtility {

  public static FkSpectra.Metadata createMetadataFromDefinition(FkSpectraDefinition definition) {
    return FkSpectra.Metadata.builder()
        .setPhaseType(definition.getPhaseType())
        .setSlowStartX(definition.getSlowStartXSecPerKm())
        .setSlowDeltaX(definition.getSlowDeltaXSecPerKm())
        .setSlowStartY(definition.getSlowStartYSecPerKm())
        .setSlowDeltaY(definition.getSlowDeltaYSecPerKm())
        .build();
  }

  /**
   * Merge channel segments of FK spectra
   *
   * @param segments Channel segments containing spectra to be merged
   * @return new single channel segment containg all merged spectras
   */
  public static ChannelSegment<FkSpectra> mergeChannelSegments(
      List<ChannelSegment<FkSpectra>> segments
  ) {
    List<FkSpectra> fullSpectraList = new ArrayList<>();

    // Create pairs of channel segment UUID and fk spectra so that the
    // third property listed above can be met (preserve channel ids of singular fk spectras)
    segments.forEach(cs -> {
      cs.getTimeseries().forEach(fkSpectra -> {
        if (fullSpectraList.size() > 0) {
          FkSpectra top = fullSpectraList
              .get(fullSpectraList.size() - 1);
          if (!top.getMetadata().equals(fkSpectra.getMetadata())) {
            throw new IllegalStateException(
                "Fk spectra merge candidates do not have same metadata");
          }
        }
        fullSpectraList.add(fkSpectra);
      });
    });

    return ChannelSegment.create(
        segments.get(0).getChannelId(),
        segments.get(0).getName(),
        segments.get(0).getType(),
        mergeFkSpectras(sortAndFilterSpectra(fullSpectraList)),
        CreationInfo.DEFAULT
    );
  }

  /**
   * Perform merge operation on a list of fk spectra.
   *
   * @param fkSpectras list of fkSpectras containing fk spectra to merge.
   * @return New list of fkSpectras, where fk spectra have been merged
   */
  private static List<FkSpectra> mergeFkSpectras(
      List<FkSpectra> fkSpectras) {
    if (fkSpectras.size() <= 1) {
      return fkSpectras;
    }

    List<FkSpectra> newFkSpectraList = new ArrayList<>();

    Iterator<FkSpectra> it = fkSpectras.iterator();
    FkSpectra first = it.next();

    while (it.hasNext()) {
      FkSpectra second = it.next();

      if (first.getValues().size() == 0 && second.getValues().size() != 0) {
        throw new IllegalStateException(
            "Trying to merge a no-values spectra with a spectra that has values");
      }

      List<FkSpectrum> firstValues = first.getValues();
      List<FkSpectrum> secondValues = second.getValues();

      //Note: time series enclosed by other time series are handled in sortAndFilterSpectra()
      if (getDurationSeconds(first.getEndTime(), second.getStartTime())
          == first.getSamplePeriod()) {
        //
        // First  FK: *****
        // Second FK:      *****
        //
        FkSpectra newFkSpectra;
        if (first.getValues().size() > 0) {
          List<FkSpectrum> newValues = new ArrayList<>(firstValues);
          newValues.addAll(secondValues);
          newFkSpectra = first.toBuilder()
              .withValues(newValues).build();
        } else {
          newFkSpectra = first.toBuilder()
              .withoutValues(first.getSampleCount() + second.getSampleCount()).build();
        }
        first = newFkSpectra;
      } else if (first.getStartTime().isBefore(second.getStartTime())
          && first.getEndTime().isBefore(second.getEndTime())
          && first.getEndTime().isAfter(second.getStartTime())
          && first.getStartTime().isBefore(second.getStartTime())) {
        //
        // First  FK: ********
        // Second FK:      ********
        //
        List<FkSpectrum> newValues = new ArrayList<>(firstValues);
        Duration overlap = Duration
            .between(second.getStartTime(), first.getEndTime());
        int overlappedSamples =
            1 + (int) ((overlap.toNanos() / 1_000_000_000.0) * first.getSampleRate());
        FkSpectra newFkSpectra;
        if (first.getValues().size() > 0) {
          newValues.addAll(secondValues.subList(
              secondValues.size() - 1 - overlappedSamples, secondValues.size() - 1));
          newFkSpectra = first.toBuilder()
              .withValues(newValues).build();
        } else {
          newFkSpectra = first.toBuilder()
              .withoutValues(first.getSampleCount() + second.getSampleCount() - overlappedSamples)
              .build();
        }
        first = newFkSpectra;
      } else {
        //
        // First  FK: *****
        // Second FK:           *****
        //
        newFkSpectraList.add(first);
        first = second;
      }
    }
    newFkSpectraList.add(first);
    return newFkSpectraList;
  }

  /**
   * Sorting Timeseries' start time then end time means the smallest potentially enclosed FkSpectra
   * always appear before the enclosing FkSpectra in the list. By reversing this, we can check when
   * larger timeseries overlap smaller time series later in the list, and filter out all
   * enclosed time series.
   *
   * @param spectras A non-empty List of FkSpectra
   * @return The list of FkSpectra sorted, with values contained by others in time filtered out.
   */
  private static List<FkSpectra> sortAndFilterSpectra(
      List<FkSpectra> spectras) {
    Preconditions
        .checkArgument(!spectras.isEmpty(), "Cannot sort and filter empty list of FkSpectras");

    Comparator<FkSpectra> smallerStartLargerEnd = Comparator.comparing(FkSpectra::getStartTime)
        .thenComparing(Comparator.comparing(FkSpectra::getEndTime).reversed());

    Iterator<FkSpectra> iterator = spectras.stream()
        .sorted(smallerStartLargerEnd).iterator();

    //initialize loop
    List<FkSpectra> sortedFilteredSpectras = new ArrayList<>();
    FkSpectra current = iterator.next();
    Range<Instant> currentRange = current.computeTimeRange();

    FkSpectra next;
    Range<Instant> nextRange;
    while (iterator.hasNext()) {
      next = iterator.next();
      nextRange = next.computeTimeRange();

      if (!currentRange.encloses(nextRange)) {
        sortedFilteredSpectras.add(current);
        current = next;
        currentRange = nextRange;
      }
    }

    //always add the last enclosing time series
    sortedFilteredSpectras.add(current);

    return sortedFilteredSpectras;
  }

  /**
   * Returns the time difference of two instances
   *
   * @param startTime The start time of the gap.
   * @param endTime The end time of the gap.
   * @return The gap time difference in seconds.
   */
  private static double getDurationSeconds(Instant startTime, Instant endTime) {
    return getDurationSeconds(Duration.between(startTime, endTime));
  }

  /**
   * Converts the input Duration to (double) seconds and returns the result.
   *
   * @param duration The input Duration to be converted to (double) seconds.
   * @return The input Duration in (double) seconds.
   */
  private static double getDurationSeconds(Duration duration) {
    return (double) duration.getNano() / 1000000000 + duration.getSeconds();
  }
}
