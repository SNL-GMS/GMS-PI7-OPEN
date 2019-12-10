package gms.core.waveformqc.waveformsignalqc.plugin;

import static java.util.Collections.emptyList;

import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformGapInterpreter;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformGapQcMask;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformGapUpdater;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wraps the gap algorithm logic in {@link WaveformGapInterpreter} and {@link WaveformGapUpdater} to
 * create new {@link QcMaskType#REPAIRABLE_GAP} and {@link QcMaskType#LONG_GAP} masks and update
 * existing gap masks with newly acquired waveforms.
 *
 * Wrap WaveformGapQcPlugin in a plugin component implementing the {@link
 * gms.core.waveformqc.plugin.WaveformQcPlugin} to create the actual gap plugin.
 */
public class WaveformGapQcPlugin {

  /**
   * Determines which gap {@link QcMask} exist in the provided {@link ChannelSegment} and updates as
   * necessary the existing QcMasks (i.e. rejecting because they are filled, shortening or splitting
   * due to new data acquisition). Assumes the QcMasks and ChannelSegments occur in the same time
   * intervals.  Returns new QcMasks and any updated existingQcMasks.
   *
   * @param channelSegment {@link ChannelSegment} to check for gaps, not null
   * @param existingQcMasks existing {@link QcMask} that might be updated, not null
   * @param parameterFieldMap id to the {@link gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation}
   * associated with returned {@link QcMask}
   * @return Stream of new or updated {@link QcMask}, not null
   */
  public List<QcMask> createQcMasks(ChannelSegment<Waveform> channelSegment,
      Collection<QcMask> existingQcMasks,
      Map<String, Object> parameterFieldMap) {

    Objects.requireNonNull(channelSegment,
        "WaveformGapQcPlugin createQcMasks cannot accept null channelSegment");
    Objects.requireNonNull(existingQcMasks,
        "WaveformGapQcPlugin createQcMasks cannot accept null existing QcMasks");
    Objects.requireNonNull(parameterFieldMap,
        "WaveformGapQcPlugin createQcMasks cannot accept null existing QcMasks");

    WaveformGapQcPluginParameters pluginParameters = ObjectSerialization
        .fromFieldMap(parameterFieldMap, WaveformGapQcPluginParameters.class);

    // Filter for valid gap masks then group by processing channel id
    final Map<UUID, List<QcMask>> existingGapQcMasks = existingQcMasks.stream()
        .filter(WaveformGapQcPlugin::gapType)
        .collect(Collectors.groupingBy(QcMask::getChannelId));

    return createOutputQcMasks(channelSegment,
                lookupExistingQcMasks(existingGapQcMasks, channelSegment.getChannelId()),
                pluginParameters).collect(Collectors.toList());
  }

  /**
   * Utility using the {@link WaveformGapInterpreter} and {@link WaveformGapUpdater} to create new
   * and update existing qcMasks for a single {@link ChannelSegment}
   *
   * @param channelSegment {@link ChannelSegment} to check for gaps, not null
   * @param existingQcMasks existing {@link QcMask} that might be updated, not null
   * @param pluginParameters id to the {@link gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation}
   * associated with returned {@link QcMask}
   * @return Stream of new or updated {@link QcMask}, not null
   */
  private Stream<QcMask> createOutputQcMasks(ChannelSegment<Waveform> channelSegment,
      List<QcMask> existingQcMasks, WaveformGapQcPluginParameters pluginParameters) {

    List<WaveformGapQcMask> newGaps = WaveformGapInterpreter
        .createWaveformGapQcMasks(channelSegment,
            pluginParameters.getMinLongGapLengthInSamples());

    return WaveformGapUpdater.updateQcMasks(newGaps, existingQcMasks, channelSegment.getId(),
        new UUID(0L, 0L)).stream();
  }

  /**
   * Lookup the existingByChannelId {@link QcMask} occurring on the provided {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel}.
   * Returns an empty list if there are no such masks.
   *
   * @param existingByChannelId existing {@link QcMask} organized by ProcessingChannel id, not null
   * @param uuid processingChannel id, not null
   * @return list of {@link QcMask}, not null
   */
  private List<QcMask> lookupExistingQcMasks(Map<UUID, List<QcMask>> existingByChannelId,
      UUID uuid) {
    List<QcMask> existingForId = existingByChannelId.get(uuid);
    return existingForId != null ? existingForId : emptyList();
  }

  /**
   * Determine if the provided {@link QcMask} is a gap QcMask (i.e. the mask's current {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion}
   * has {@link QcMaskVersion#getType()} of either {@link QcMaskType#REPAIRABLE_GAP} and {@link
   * QcMaskType#LONG_GAP}
   *
   * @param qcMask {@link QcMask}, not null
   * @return true if the qcMask's current version is a gap and false otherwise
   */
  private static boolean gapType(QcMask qcMask) {
    return qcMask.getCurrentQcMaskVersion().getType()
        .map(t -> QcMaskType.LONG_GAP.equals(t) || QcMaskType.REPAIRABLE_GAP.equals(t))
        .orElse(false);
  }
}