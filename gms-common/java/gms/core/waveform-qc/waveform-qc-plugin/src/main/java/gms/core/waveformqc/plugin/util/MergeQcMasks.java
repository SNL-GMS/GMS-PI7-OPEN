package gms.core.waveformqc.plugin.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Preconditions;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MergeQcMasks {

  /**
   * Merges a collection of newMasks with a collection of existingMasks.  Masks are merged if they
   * are the same type (QcMasks are the same type if their {@link QcMask#getCurrentQcMaskVersion()}
   * have the same {@link QcMaskVersion#getCategory()}, {@link QcMaskVersion#getType()} and {@link
   * QcMaskVersion#getRationale()}, occur on the same Processing Channel, and overlap or are
   * adjacent in time.  It is assumed all of the masks provided to this function have the same type
   * and occur on the same processing channel.
   *
   * threshold is a {@link Duration} determining when two masks overlap.  The threshold is
   * non-inclusive: if the threshold is set to 1 second and exactly 1 second separates the end of
   * one mask from the beginning of the next mask they will not be merged.
   *
   * Merges groups of overlapping masks into a single new mask in the following way: 1. A single
   * existing mask merged with any number of new masks results in a new version of the existing mask
   * with an extended start and/or end time 2. Multiple existing masks merged with any number of new
   * masks results in 1) rejecting all the existing masks 2) creating a single new mask with all of
   * the existing masks as parent references (see {@link QcMaskVersion#getParentQcMasks()}
   *
   * Does not merge groups of overlapping existing masks unless there is also at least one
   * overlapping new mask in the set of overlapping masks.  However, this does not require a new
   * mask to overlap all of the existing masks; e.g. if two existing masks overlap and one new mask
   * overlaps one of the existing masks then all three masks will be merged into a single mask.
   *
   * Returns any masks created or updated by this operation
   *
   * @param newMasks collection of newly created QcMasks, all of the same type, rationale, and on
   * the same channel, non null
   * @param existingMasks collection of existing QcMasks, all of the same type, rationale, and on
   * the same channel, non null
   * @param threshold Duration determining whether two QcMasks occur adjacent in time,
   * non-inclusive
   * @return collection of new or updated QcMasks, not null
   * @throws NullPointerException if newMasks, existingMasks, or threshold are null
   * @throws IllegalArgumentException if not all of the provided masks have the same type and
   * rationale; if any of the provided masks occur on different processing channels.
   */
  public static Collection<QcMask> merge(Collection<QcMask> newMasks,
      Collection<QcMask> existingMasks, Duration threshold) {

    Preconditions.checkNotNull(newMasks, "MergeQcMasks requires non-null newMasks");
    Preconditions.checkNotNull(existingMasks, "MergeQcMasks requires non-null existingMasks");
    Preconditions.checkNotNull(threshold, "MergeQcMasks requires non-null threshold");
    checkArgument(!newMasks.isEmpty(), "MergeQcMasks requires at least one newMask");

    final List<QcMask> allMasks = Stream.concat(newMasks.stream(), existingMasks.stream())
        .collect(toList());

    // Verify all masks have the same type, not rejected, and occur on the same processingChannel
    verifyQcMasks(allMasks);

    //Can presently ignore warnings on optional as a non-rejected qcmask version will have a start
    //time and end time
    Comparator<QcMask> startEndComparator = Comparator
        .comparing(a -> a.getCurrentQcMaskVersion().getStartTime().get());
    startEndComparator = startEndComparator
        .thenComparing(a -> a.getCurrentQcMaskVersion().getEndTime().get());

    // Create a list all of the new and existing masks, sorted by startTime
    final List<QcMask> timeOrdered = allMasks.stream()
        .sorted(startEndComparator)
        .collect(toList());

    // Group into lists of overlapping masks, remove groups only containing existing masks,
    // construct masks from the overlap groups, return merged masks
    return groupByOverlappingTime(timeOrdered, threshold).stream()
        .filter(l -> l.stream().anyMatch(newMasks::contains))
        .map(l -> mergeMasks(l, newMasks))
        .flatMap(List::stream)
        .collect(toList());
  }

  /**
   * Verifies each of the provided {@link QcMask} have the same type (both {@link QcMaskType} and
   * rationale string), occur on the same processing channel, and are not rejected.
   *
   * @param qcMasks list of QcMasks to verify, non null
   */
  private static void verifyQcMasks(List<QcMask> qcMasks) {

    List<QcMaskCategory> allCategories = qcMasks.stream()
        .map(qcMask -> qcMask.getCurrentQcMaskVersion().getCategory())
        .collect(toList());

    // Check none of the masks are rejected
    checkArgument(
        allCategories.stream().noneMatch(Predicate.isEqual(QcMaskCategory.REJECTED)),
        "Error merging QcMasks: cannot merge rejected masks");
    // Check all masks are the same category
    checkArgument(allCategories.stream().distinct().limit(2).count() <= 1,
        "Error merging QcMasks: must provide masks from the same category");

    // Check all masks are the same type
    Stream<QcMaskType> allTypes = qcMasks.stream()
        .map(qcMask -> qcMask.getCurrentQcMaskVersion().getType().orElseThrow(
            () -> new IllegalArgumentException(
                "Error merging QcMasks: must provide masks with a type")));
    checkArgument(allTypes.distinct().limit(2).count() <= 1,
        "Error merging QcMasks: must provide masks from the same type");

      // Check all of the masks have the same processingChannel id
    Stream<UUID> allChannelIds = qcMasks.stream()
        .map(QcMask::getChannelId);
    checkArgument(allChannelIds.distinct().limit(2).count() <= 1,
        "Error merging QcMasks: must provide masks from the same channel");

  }

  /**
   * Merge the overlapGroup {@link QcMask}s into a single mask.  Reject existing masks if multiple
   * existing masks get merged in this process.  There are three scenarios depending on the type of
   * masks in overlapGroup:
   *
   * 1. No existing masks: merge all the masks and return a single new mask 2. One existing mask:
   * add a new version to the existing mask which spans the overlapGroup 3. Multiple existing masks:
   * create a single new mask to span the overlapGroup.  Create rejected versions of all of the
   * existing masks.
   *
   * Assumes the overlapGroup contains at least 1 new mask.
   *
   * @param overlapGroup QcMasks to merge, contains at least one element
   * @param newMasks newly created QcMasks, not empty
   * @return list of QcMask, contains one new mask and 0 or more rejected existing masks
   */
  private static List<QcMask> mergeMasks(List<QcMask> overlapGroup, Collection<QcMask> newMasks) {
    // If the overlap group contains one mask then just return it (by precondition it is new)
    if (overlapGroup.size() == 1) {
      return overlapGroup;
    }

    // Get common parameters used when constructing the mergedQcMasks
    final UUID processingChannelId = overlapGroup.get(0).getChannelId();

    final Instant startTime = overlapGroup.stream()
        .map(q -> q.getCurrentQcMaskVersion().getStartTime())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .min(Instant::compareTo).orElseThrow(
            () -> new IllegalArgumentException(
                "Error merging QcMasks: no valid start time found."));

    final Instant endTime = overlapGroup.stream()
        .map(q -> q.getCurrentQcMaskVersion().getEndTime())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .max(Instant::compareTo).orElseThrow(
            () -> new IllegalArgumentException("Error merging QcMasks: no valid end time found."));

    final QcMaskVersion firstVersion = overlapGroup.get(0).getCurrentQcMaskVersion();
    final QcMaskCategory category = firstVersion.getCategory();
    final QcMaskType type = firstVersion.getType().orElseThrow(
        () -> new IllegalArgumentException("Error merging QcMasks: no valid type found."));
    final String rationaleString = firstVersion.getRationale();

    List<QcMask> existingQcMasks = overlapGroup.stream().filter(m -> !newMasks.contains(m))
        .collect(toList());

    List<QcMask> mergedQcMasks = new ArrayList<>();

    // If there are no existing masks then just return a single new mask
    if (existingQcMasks.isEmpty()) {
      mergedQcMasks.add(QcMask
          .create(processingChannelId, emptyList(), emptyList(), category,
              type, rationaleString, startTime, endTime));
    }

    // If there is only one existing mask then update it with the mergedQcMasks mask.
    else if (existingQcMasks.size() == 1) {
      QcMask toUpdate = existingQcMasks.get(0);

      if (!(toUpdate.getCurrentQcMaskVersion().getStartTime().map(startTime::equals)
          .orElse(Boolean.FALSE)
          && toUpdate.getCurrentQcMaskVersion().getEndTime().map(endTime::equals)
          .orElse(Boolean.FALSE))) {

        toUpdate
            .addQcMaskVersion(emptyList(), category, type, rationaleString, startTime,
                endTime);
      }

      mergedQcMasks.add(toUpdate);
    }

    // If there is more than one existing mask then reject those masks and create a new mask
    // with multiple parents.
    else {
      // Current versions of each existing mask are the parents of the merged mask
      final List<QcMaskVersionDescriptor> parents = existingQcMasks.stream()
          .map(
              q -> QcMaskVersionDescriptor.from(q.getId(), q.getCurrentQcMaskVersion().getVersion()))
          .collect(toList());

      // Create a new mask spanning the group
      final QcMask mergedQcMask = QcMask
          .create(processingChannelId, parents, emptyList(), category, type,
              rationaleString, startTime, endTime);
      mergedQcMasks.add(mergedQcMask);

      // Reject the existing masks
      existingQcMasks.forEach(q -> q
          .reject("Merged to form QcMask with ID: " + mergedQcMask.getId(), emptyList()));
      mergedQcMasks.addAll(existingQcMasks);
    }

    return mergedQcMasks;
  }

  /**
   * Group the provided {@link QcMask}s into lists ordered by time where adjacent masks in each list
   * overlap within a threshold
   *
   * @param qcMasks QcMasks to group, ordered by start time
   * @param threshold Duration to determine whether two QcMasks overlap in time
   * @return collection of lists of overlapping QcMasks
   */
  private static Collection<List<QcMask>> groupByOverlappingTime(List<QcMask> qcMasks,
      Duration threshold) {

    Collection<List<QcMask>> grouped = new ArrayList<>();

    int groupStartIndex = 0;
    for (int groupEndIndex = 1; groupEndIndex < qcMasks.size(); ++groupEndIndex) {
      if (!overlapInTime(qcMasks.get(groupEndIndex - 1), qcMasks.get(groupEndIndex), threshold)) {
        grouped.add(qcMasks.subList(groupStartIndex, groupEndIndex));
        groupStartIndex = groupEndIndex;
      }
    }

    // Add the last group
    if (groupStartIndex < qcMasks.size()) {
      grouped.add(qcMasks.subList(groupStartIndex, qcMasks.size()));
    }

    return grouped;
  }

  /**
   * Determine if two {@link QcMask} overlap in time.  The two masks must be ordered by start time
   * (a starts before b starts); the overlap is if QcMask b begins within the threshold of QcMask a
   * ending.
   *
   * @param a first mask to check for overlap, starts before b starts
   * @param b second mask to check for overlap
   * @return true if the QcMasks overlap, false otherwise
   */
  private static boolean overlapInTime(QcMask a, QcMask b, Duration threshold) {
    Instant bStart = b.getCurrentQcMaskVersion().getStartTime().get();
    Instant aEnd = a.getCurrentQcMaskVersion().getEndTime().get().plus(threshold);

    return bStart.isBefore(aEnd);
  }

}
