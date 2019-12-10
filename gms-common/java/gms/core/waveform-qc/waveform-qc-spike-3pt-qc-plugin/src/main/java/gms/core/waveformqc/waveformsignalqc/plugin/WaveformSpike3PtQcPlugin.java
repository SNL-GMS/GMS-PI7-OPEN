package gms.core.waveformqc.waveformsignalqc.plugin;

import static java.util.Collections.emptyList;

import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformSpike3PtInterpreter;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformSpike3PtQcMask;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
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

/**
 * Wraps the gap algorithm logic in {@link WaveformSpike3PtInterpreter} to create new {@link
 * QcMaskType#SPIKE} mask and update possible end-point spike masks with newly acquired waveforms.
 *
 * Wrap WaveformSpike3PtQcPlugin in a plugin component implementing the {@link
 * gms.core.waveformqc.plugin.WaveformQcPlugin} to create the actual spike plugin.
 */
public class WaveformSpike3PtQcPlugin {

  private WaveformSpike3PtInterpreter waveformSpike3PtInterpreter;

  private WaveformSpike3PtQcPlugin(
      WaveformSpike3PtInterpreter waveformSpike3PtInterpreter) {
    this.waveformSpike3PtInterpreter = waveformSpike3PtInterpreter;
  }

  /**
   * Obtains a new {@link WaveformSpike3PtQcPlugin} using the provided {@link
   * WaveformSpike3PtInterpreter}
   *
   * @return new {@link WaveformSpike3PtQcPlugin}, not null
   */
  public static WaveformSpike3PtQcPlugin create(
      WaveformSpike3PtInterpreter waveformSpike3PtInterpreter) {
    Objects.requireNonNull(waveformSpike3PtInterpreter,
        "WaveformSpike3PtQcPlugin create cannot accept null spike algorithm");

    return new WaveformSpike3PtQcPlugin(
        waveformSpike3PtInterpreter);
  }

  /**
   * Determines which spike {@link QcMask} exist in the provided {@link ChannelSegment} and updates
   * as necessary the existing QcMasks. Assumes the QcMasks and ChannelSegments occur in the same
   * time intervals.  Returns new QcMasks and any updated existingQcMasks.
   *
   * @param waveforms {@link ChannelSegment} to check for spikes, not null
   * @param existingQcMasks existing {@link QcMask} that might be updated, not null
   * @param parameterFieldMap id to the {@link gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation}
   * associated with returned {@link QcMask}
   * @return Stream of new or updated {@link QcMask}, not null
   */
  public List<QcMask> createQcMasks(ChannelSegment<Waveform> waveforms,
      Collection<QcMask> existingQcMasks,
      Map<String, Object> parameterFieldMap) {

    Objects.requireNonNull(waveforms,
        "WaveformSpike3PtQcPlugin createQcMasks cannot accept null waveforms");
    Objects.requireNonNull(existingQcMasks,
        "WaveformSpike3PtQcPlugin createQcMasks cannot accept null existing existingQcMasks");
    Objects.requireNonNull(parameterFieldMap,
        "WaveformSpike3PtQcPlugin createQcMasks cannot accept null parameterFieldMap");

    WaveformSpike3PtQcPluginParameters parameters = ObjectSerialization.fromFieldMap(parameterFieldMap, WaveformSpike3PtQcPluginParameters.class);

    // Filter for valid non-rejected spike masks then group by processing channel id
    final Map<UUID, List<QcMask>> existingSpikeQcMasks = existingQcMasks.stream()
        .filter(WaveformSpike3PtQcPlugin::spikeType)
        .filter(q -> !q.getCurrentQcMaskVersion().isRejected())
        .collect(Collectors.groupingBy(QcMask::getChannelId));

    return waveformSpike3PtInterpreter
        .createWaveformSpike3PtQcMasks(waveforms,
            parameters.getMinConsecutiveSampleDifferenceSpikeThreshold(),
            parameters.getRmsLeadSampleDifferences(),
            parameters.getRmsLagSampleDifferences(),
            parameters.getRmsAmplitudeRatioThreshold()).stream()
        .filter(m -> spikeDifferentFromMasks(m, existingSpikeQcMasks.get(m.getChannelId())))
        .map(WaveformSpike3PtQcPlugin::qcMaskFromSpike3PtMask).collect(Collectors.toList());
  }

  private static QcMask qcMaskFromSpike3PtMask(
      WaveformSpike3PtQcMask spike3PtQcMask) {

    final String rationale = "System created 3Pt Spike values mask";

    return QcMask.create(spike3PtQcMask.getChannelId(), emptyList(),
        List.of(spike3PtQcMask.getChannelSegmentId()), QcMaskCategory.WAVEFORM_QUALITY,
        QcMaskType.SPIKE, rationale,
        spike3PtQcMask.getStartTime(), spike3PtQcMask.getEndTime());
  }

  /**
   * Determine if the provided {@link QcMask} is a spike QcMask (i.e. the mask's current {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion}
   * has {@link QcMaskVersion#getType()} of {@link QcMaskType#SPIKE}
   *
   * @param qcMask {@link QcMask}, not null
   * @return true if the qcMask's current version is a spike and false otherwise
   */
  private static boolean spikeType(QcMask qcMask) {
    return qcMask.getCurrentQcMaskVersion().getType()
        .map(QcMaskType.SPIKE::equals)
        .orElse(false);
  }

  /**
   * Determine if the spike does not already exist in the collection of existing QcMasks
   *
   * @param spike {@link WaveformSpike3PtQcMask}, not null
   * @param qcMasks list of {@link QcMask}, not null
   * @return true if the spike is different in time from each mask, false otherwise
   */
  private static boolean spikeDifferentFromMasks(WaveformSpike3PtQcMask spike,
      List<QcMask> qcMasks) {
    return (qcMasks == null) || qcMasks.stream()
        .map(QcMask::getCurrentQcMaskVersion)
        .noneMatch(m -> spike.getStartTime().equals(m.getStartTime().get())
            && spike.getEndTime().equals(m.getEndTime().get()));
  }
}
