package gms.core.waveformqc.waveformsignalqc.plugin;

import static java.util.Collections.emptyList;

import gms.core.waveformqc.plugin.util.MergeQcMasks;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformRepeatedAmplitudeInterpreter;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformRepeatedAmplitudeQcMask;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wraps algorithm logic from {@link WaveformRepeatedAmplitudeInterpreter} to create {@link
 * QcMaskType#REPEATED_ADJACENT_AMPLITUDE_VALUE} {@link QcMask}s.
 *
 * Uses {@link MergeQcMasks} to merge new repeated adjacent amplitude masks with existing masks.
 * Merging is based on mask type only so repeated amplitude masks will be merged even if the
 * repeated amplitude in each mask is different.
 */
public class WaveformRepeatedAmplitudeQcPlugin {

  private static final String RATIONALE = "System created repeated adjacent amplitude values mask";

  private WaveformRepeatedAmplitudeQcPluginParameters parameters;

  private final WaveformRepeatedAmplitudeInterpreter waveformRepeatedAmplitudeInterpreter;

  private WaveformRepeatedAmplitudeQcPlugin(
      WaveformRepeatedAmplitudeInterpreter waveformRepeatedAmplitudeInterpreter) {
    this.waveformRepeatedAmplitudeInterpreter = waveformRepeatedAmplitudeInterpreter;
  }

  /**
   * Construct a new {@link WaveformRepeatedAmplitudeQcPlugin} using the provided configuration and
   * deferring logic to the waveformRepeatedAmplitudeInterpreter
   *
   * @param waveformRepeatedAmplitudeInterpreter {@link WaveformRepeatedAmplitudeInterpreter}, not
   * null
   * @return {@link WaveformRepeatedAmplitudeQcPlugin}, not null
   */
  public static WaveformRepeatedAmplitudeQcPlugin create(
      WaveformRepeatedAmplitudeInterpreter waveformRepeatedAmplitudeInterpreter) {

    Objects.requireNonNull(waveformRepeatedAmplitudeInterpreter,
        "WaveformRepeatedAmplitudeQcPlugin create cannot accept null waveformRepeatedAmplitudeInterpreter");

    return new WaveformRepeatedAmplitudeQcPlugin(waveformRepeatedAmplitudeInterpreter);
  }

  /**
   * Finds {@link QcMaskType#REPEATED_ADJACENT_AMPLITUDE_VALUE} {@link QcMask}s in the provided
   * {@link ChannelSegment}s.  Updates or rejects the provided existingQcMasks when necessary.
   * existingQcMasks must contain existing qcMasks overlapping the same time interval as the
   * channelSegments but can have any {@link QcMaskVersion#getType()}.  Associates new masks with
   * the creationInfoId.
   *
   * @param channelSegment {@link ChannelSegment} to search for repeated adjacent amplitude values,
   * not null
   * @param existingQcMasks potential {@link QcMask} to update, not null
   * @return List of new or updated qcMasks, not null
   */
  public List<QcMask> createQcMasks(ChannelSegment<Waveform> channelSegment,
      Collection<QcMask> existingQcMasks, Map<String, Object> parameterFieldMap) {

    Objects.requireNonNull(channelSegment,
        "WaveformRepeatedAmplitudeQcPlugin createQcMasks cannot accept null channelSegments");
    Objects.requireNonNull(existingQcMasks,
        "WaveformRepeatedAmplitudeQcPlugin createQcMasks cannot accept null existingQcMasks");
    Objects.requireNonNull(parameterFieldMap,
        "WaveformRepeatedAmplitudeQcPlugin createQcMasks cannot accept null parameterFieldMap");

    this.parameters = ObjectSerialization
        .fromFieldMap(parameterFieldMap, WaveformRepeatedAmplitudeQcPluginParameters.class);

    // Filter existing masks that are rejected or of the wrong type; group by Channel identity
    final Map<UUID, List<QcMask>> existingByChannelId = existingQcMasks.stream()
        .filter(q -> !q.getCurrentQcMaskVersion().isRejected())
        .filter(WaveformRepeatedAmplitudeQcPlugin::repeatedAmplitudeQcMaskPredicate)
        .collect(Collectors.groupingBy(QcMask::getChannelId));

    // Create new repeated adjacent amplitude masks on each ChannelSegment
    List<QcMask> newMasks = repeatsInSegment(channelSegment)
        .collect(Collectors.toList());

    if (newMasks.isEmpty()) {
      return newMasks;
    }

    // Merge new and existing masks on each Channel
    final Duration mergeThreshold = Duration
        .ofNanos((long) (parameters.getMaskMergeThresholdSeconds() * 1.0e9));

    Stream.Builder<QcMask> outputBuilder = Stream.builder();
    MergeQcMasks
        .merge(newMasks,
            existingByChannelId.getOrDefault(channelSegment.getChannelId(), emptyList()),
            mergeThreshold)
        .forEach(outputBuilder::add);

    return outputBuilder.build().collect(Collectors.toList());
  }

  /**
   * Determines if the {@link QcMask} is a system created repeated adjacent amplitude qcMask.  To
   * pass, a qcMask must have {@link QcMask#getCurrentQcMaskVersion()} with {@link
   * QcMaskVersion#getCategory()} of {@link QcMaskCategory#WAVEFORM_QUALITY}, {@link
   * QcMaskVersion#getType()} of {@link QcMaskType#REPEATED_ADJACENT_AMPLITUDE_VALUE}, and {@link
   * QcMaskVersion#getRationale()} of {@link WaveformRepeatedAmplitudeQcPlugin#RATIONALE}
   *
   * @param qcMask a {@link QcMask}, not null
   * @return true if qcMask is a system created repeated adjacent amplitude qcMask
   */
  private static boolean repeatedAmplitudeQcMaskPredicate(QcMask qcMask) {
    Predicate<QcMaskVersion> category = v -> QcMaskCategory.WAVEFORM_QUALITY
        .equals(v.getCategory());
    Predicate<QcMaskVersion> type = v -> v.getType()
        .map(QcMaskType.REPEATED_ADJACENT_AMPLITUDE_VALUE::equals).orElse(Boolean.FALSE);
    Predicate<QcMaskVersion> rationale = v -> WaveformRepeatedAmplitudeQcPlugin.RATIONALE
        .equals(v.getRationale());

    return category.and(type).and(rationale).test(qcMask.getCurrentQcMaskVersion());
  }

  /**
   * Uses {@link WaveformRepeatedAmplitudeQcPlugin#waveformRepeatedAmplitudeInterpreter} to create
   * repeated adjacent amplitude  {@link QcMask}s in a {@link ChannelSegment}.
   *
   * @param channelSegment {@link ChannelSegment} to search for repeated adjacent amplitudes, not
   * null
   * @return Stream of repeated amplitude {@link QcMask}, not null
   */
  private Stream<QcMask> repeatsInSegment(ChannelSegment<Waveform> channelSegment) {
    return waveformRepeatedAmplitudeInterpreter
        .createWaveformRepeatedAmplitudeQcMasks(channelSegment,
            parameters.getMinSeriesLengthInSamples(),
            parameters.getMaxDeltaFromStartAmplitude()).stream()
        .map(WaveformRepeatedAmplitudeQcPlugin::qcMaskFromRepeatedAmplitudeMask);
  }

  /**
   * Obtains a {@link QcMask} from a {@link WaveformRepeatedAmplitudeQcMask} and associates the new
   * mask to the creationInfoId
   *
   * @param repeatedAmplitudeQcMask {@link WaveformRepeatedAmplitudeQcMask}, not null
   * @return new {@link QcMask}, not null
   */
  private static QcMask qcMaskFromRepeatedAmplitudeMask(
      WaveformRepeatedAmplitudeQcMask repeatedAmplitudeQcMask) {

    return QcMask.create(repeatedAmplitudeQcMask.getChannelId(), emptyList(),
        List.of(repeatedAmplitudeQcMask.getChannelSegmentId()), QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.REPEATED_ADJACENT_AMPLITUDE_VALUE, RATIONALE,
        repeatedAmplitudeQcMask.getStartTime(), repeatedAmplitudeQcMask.getEndTime());
  }
}
