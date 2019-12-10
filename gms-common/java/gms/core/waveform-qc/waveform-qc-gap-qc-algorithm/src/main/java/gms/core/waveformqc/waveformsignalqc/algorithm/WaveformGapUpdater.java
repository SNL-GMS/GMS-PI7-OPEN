package gms.core.waveformqc.waveformsignalqc.algorithm;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Determines how gaps in waveform data affect existing {@link QcMask}.
 */
public class WaveformGapUpdater {

  /**
   * Given existing gap {@link QcMask} and newly found {@link WaveformGapQcMask} determine
   * how the gaps affect the masks.  Possibilities are:
   *
   * 1. Newly acquired data has new gaps that need to be masked
   * 2. Newly acquired data completely fills a mask
   * 3. Newly acquired data fills a portion of an existing mask
   * a. The gap mask may be shortened from either or both ends
   * b. The gap mask may be split into multiple new gap masks
   *
   * Returns new QcMasks or QcMasks updated by this processing.
   *
   * @param gaps new {@link WaveformGapQcMask}, not null
   * @param existingMasks existing gap {@link QcMask}, not null
   * @param channelSegmentId id to the {@link gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment}
   * processed to find the gaps, not null
   * @param creationInfoId id to the {@link gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation}
   * describing this QcMask processing, not null
   * @return List of QcMasks updated or newly created by this operation, not null
   * @throws NullPointerException if gaps, existingMasks, channelSegmentId, or creationInfoId is
   * null
   */
  public static List<QcMask> updateQcMasks(List<WaveformGapQcMask> gaps,
      List<QcMask> existingMasks, UUID channelSegmentId, UUID creationInfoId) {

    Objects.requireNonNull(gaps, "WaveformGapUpdater.updateQcMasks requires non-null gaps");
    Objects.requireNonNull(existingMasks,
        "WaveformGapUpdater.updateQcMasks requires non-null existing masks");
    Objects.requireNonNull(channelSegmentId,
        "WaveformGapUpdater.updateQcMasks requires non-null channelSegmentId");
    Objects.requireNonNull(creationInfoId,
        "WaveformGapUpdater.updateQcMasks requires non-null creationInfoId");

    // 1. Remove from existing masks all masks equal to gaps
    // potentiallyUpdatedMasks is left with those existingMasks not equal to any gaps
    Set<QcMask> potentiallyUpdatedMasks = existingMasks.stream()
        .filter(m -> differentTimeFrom(m, gaps))
        .collect(Collectors.toSet());

    // 2. Remove gaps that match existingMasks
    Set<WaveformGapQcMask> newGaps = gaps.stream()
        .filter(g -> differentTimeFrom(g, existingMasks))
        .collect(Collectors.toSet());

    // 3. Find which gaps fall within existing potentiallyUpdatedMasks.  This helps determine
    // which potentiallyUpdatedMasks are updated or rejected.
    Map<Optional<QcMask>, List<WaveformGapQcMask>> maskGapMap = newGaps.stream()
        .collect(Collectors.groupingBy(g -> checkWithinMask(g, potentiallyUpdatedMasks)));

    // 4. To find filled masks: start with all potentiallyUpdatedMasks and remove masks overlapped
    // by new gaps (i.e. keep masks not overlapped by new gaps).  This leaves existing masks not
    // related in any way to new gaps.  These masks were filled and need to be rejected.
    Set<QcMask> filledMasks = new HashSet<>(potentiallyUpdatedMasks);
    maskGapMap.keySet().stream().filter(Optional::isPresent).map(Optional::get)
        .forEach(filledMasks::remove);
    filledMasks.forEach(m -> m.reject("Gap QcMask rejected due to filling in from new data.",
        List.of(channelSegmentId)));

    // 4. Any gaps not associated with existing masks are new
    Set<QcMask> newMasks = new HashSet<>();
    if (maskGapMap.containsKey(Optional.empty())) {
      newMasks.addAll(
          maskGapMap.get(Optional.empty()).stream().map(g -> createQcMask(g))
              .collect(Collectors.toSet()));
    }

    // 5. Remaining gaps either modify an existing mask's time range or split an existing mask into
    // multiple new masks
    Set<QcMask> updatedMasks = maskGapMap.entrySet().stream()
        .filter(e -> e.getKey().isPresent())
        .flatMap(e -> updateQcMask(e.getKey().get(), e.getValue(), channelSegmentId
        ))
        .collect(Collectors.toSet());

    return Stream.of(filledMasks, newMasks, updatedMasks)
        .flatMap(Set::stream)
        .collect(Collectors.toList());
  }

  /**
   * Determine if the mask has a different start and end time from any of the gaps
   *
   * @param mask {@link QcMask}, not null
   * @param gaps list of {@link WaveformGapQcMask}, not null
   * @return true if the mask is different in time from each gap, false otherwise
   */
  private static boolean differentTimeFrom(QcMask mask, List<WaveformGapQcMask> gaps) {
    QcMaskVersion currentVersion = mask.getCurrentQcMaskVersion();
    return gaps.stream()
        .noneMatch(
            g -> g.getStartTime().equals(currentVersion.getStartTime().get()) && g.getEndTime()
                .equals(currentVersion.getEndTime().get()));
  }

  /**
   * Determine if the gaps has a different start and end time from any of the masks
   *
   * @param gap {@link WaveformGapQcMask}, not null
   * @param masks list of {@link QcMask}, not null
   * @return true if the gap is different in time from each mask, false otherwise
   */
  private static boolean differentTimeFrom(WaveformGapQcMask gap, Collection<QcMask> masks) {
    return masks.stream()
        .map(QcMask::getCurrentQcMaskVersion)
        .noneMatch(m -> gap.getStartTime().equals(m.getStartTime().get())
            && gap.getEndTime().equals(m.getEndTime().get()));
  }

  /**
   * Determine which of the masks a gap overlaps.  The gap can only overlap with a maximum of one
   * mask since processing newly acquired data can not extend a gap mask.
   *
   * @param gap a {@link WaveformGapQcMask}, not null
   * @param masks list of {@link QcMask}, not null
   * @return Optional QcMask overlapped by the gap
   */
  private static Optional<QcMask> checkWithinMask(WaveformGapQcMask gap, Collection<QcMask> masks) {

    //find first works as we can assume no new gap overlaps between previous existing gaps
    // (reprocessing data does not result in longer gaps)
    return masks.stream()
        .filter(m ->
            !gap.getStartTime().isBefore(m.getCurrentQcMaskVersion().getStartTime().get()) &&
                !gap.getEndTime().isAfter(m.getCurrentQcMaskVersion().getEndTime().get()))
        .findFirst();
  }

  /**
   * Obtains a new {@link QcMask} from the provided {@link WaveformGapQcMask} and associated with
   * the creationInfoId.  The mask has no parent masks.
   *
   * @param waveformGapQcMask {@link WaveformGapQcMask}, not null
   * @return new QcMask, not null
   */
  private static QcMask createQcMask(WaveformGapQcMask waveformGapQcMask) {
    return QcMask.create(waveformGapQcMask.getChannelId(), Collections.emptyList(),
        List.of(waveformGapQcMask.getChannelSegmentId()),
        QcMaskCategory.WAVEFORM_QUALITY, waveformGapQcMask.getQcMaskType(),
        "System created gap mask", waveformGapQcMask.getStartTime(),
        waveformGapQcMask.getEndTime());
  }

  /**
   * Updates the existing {@link QcMask} using the provided {@link WaveformGapQcMask}.  The gaps may
   * either update the existingMask by shortening it from either or both ends or split the mask.
   * Returns a stream of the created and/or updated QcMasks.
   *
   * @param existingMask {@link QcMask}, not null
   * @param gaps list of {@link WaveformGapQcMask}, not null
   * @param channelSegmentId id to the {@link gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment}
   * processed to find the gaps, not null
   */
  private static Stream<QcMask> updateQcMask(QcMask existingMask,
      Collection<WaveformGapQcMask> gaps, UUID channelSegmentId) {

    // Update the range on the existing mask
    if (gaps.size() == 1) {
      WaveformGapQcMask newGap = gaps.iterator().next();
      return Stream.of(updateExistingMask(existingMask, newGap));
    }

    // Reject the existing mask and create new masks for each of the gaps
    existingMask.reject("Gap QcMask rejected due to being split from new data.",
        List.of(channelSegmentId));

    QcMaskVersionDescriptor parent = QcMaskVersionDescriptor.from(existingMask.getId(),
        existingMask.getCurrentQcMaskVersion().getVersion());

    return Stream.concat(Stream.of(existingMask), gaps.stream()
        .map(g -> createQcMask(g, parent)));
  }

  /**
   * Obtains a new {@link QcMask} from the provided {@link WaveformGapQcMask} and associated with
   * the creationInfoId.  The mask has the provided parent.
   *
   * @param waveformGapQcMask {@link WaveformGapQcMask}, not null
   * @param parent {@link QcMaskVersionDescriptor} to the new qcMask's parent qcMask
   * @return new QcMask, not null
   */
  private static QcMask createQcMask(WaveformGapQcMask waveformGapQcMask,
      QcMaskVersionDescriptor parent) {

    return QcMask.create(waveformGapQcMask.getChannelId(), List.of(parent),
        List.of(waveformGapQcMask.getChannelSegmentId()), QcMaskCategory.WAVEFORM_QUALITY,
        waveformGapQcMask.getQcMaskType(), "Gap QcMask created from existing"
            + "mask due to new data", waveformGapQcMask.getStartTime(),
        waveformGapQcMask.getEndTime());
  }

  /**
   * Update the {@link QcMask} with a new version based on information from the provided {@link
   * WaveformGapQcMask} and associated with the creationInfoId.
   *
   * @param qcMask {@link QcMask} to update, not null
   * @param gap {@link WaveformGapQcMask}, not null
   * @return the input qcMask
   */
  private static QcMask updateExistingMask(QcMask qcMask, WaveformGapQcMask gap) {
    qcMask.addQcMaskVersion(List.of(gap.getChannelSegmentId()), QcMaskCategory.WAVEFORM_QUALITY,
        gap.getQcMaskType(), "Gap QcMask time range altered due to new data",
        gap.getStartTime(), gap.getEndTime());

    return qcMask;
  }
}