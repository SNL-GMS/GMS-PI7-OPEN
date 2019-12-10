package gms.core.waveformqc.channelsohqc.plugin;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import gms.core.waveformqc.channelsohqc.algorithm.ChannelSohQcMask;
import gms.core.waveformqc.channelsohqc.algorithm.ChannelSohStatusParser;
import gms.core.waveformqc.plugin.WaveformQcPlugin;
import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegment;
import gms.core.waveformqc.plugin.util.MergeQcMasks;
import gms.core.waveformqc.plugin.util.QcMaskUtility;
import gms.shared.frameworks.pluginregistry.Plugin;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Channel SOH QC Plugin component with a name and version number. This plugin is used create QCMask
 * from channel SOH data.
 */
@AutoService(Plugin.class)
public class ChannelSohQcPlugin implements WaveformQcPlugin {

  private static final Logger logger = LoggerFactory.getLogger(ChannelSohQcPlugin.class);
  private static final String PLUGIN_NAME = "channelSohQcPlugin";

  /**
   * Method that gets the PLUGIN_NAME of the plugin
   *
   * @return Plugin PLUGIN_NAME
   */
  @Override
  public String getName() {
    return PLUGIN_NAME;
  }

  /**
   * Uses {@link ChannelSohStatusParser} to create new {@link QcMask}s from the {@link
   * ChannelSohStatusSegment}, merges the new masks with the existingQcMasks, and returns any
   * created or updated QcMasks.
   *
   * @param sohStatusChanges collection of ChannelSohStatusSegment to process for creating new
   * QcMasks, not null
   * @param existingQcMasks existing QcMasks that might be affected by any newly created masks, not
   * null
   * @return stream of created or updated QcMasks, not null
   */
  @Override
  public List<QcMask> generateQcMasks(ChannelSegment<Waveform> waveforms,
      Collection<ChannelSohStatusSegment> sohStatusChanges,
      Collection<QcMask> existingQcMasks,
      Map<String, Object> parameterFieldMap) {
    Objects.requireNonNull(waveforms,
        "ChannelSohQcPlugin generateQcMasks cannot accept null channelSegments");
    Objects.requireNonNull(sohStatusChanges,
        "ChannelSohQcPlugin generateQcMasks cannot accept null sohStatusChanges");
    Objects.requireNonNull(existingQcMasks,
        "ChannelSohQcPlugin generateQcMasks cannot accept null existing QcMasks");
    Objects.requireNonNull(parameterFieldMap,
        "ChannelSohQcPlugin generateQcMasks cannot accept null parameterFieldMap");

    //Validate channel id matches across inputs
    UUID waveformsChannelId = waveforms.getChannelId();
    List<UUID> sohChannelIds = sohStatusChanges.stream().map(ChannelSohStatusSegment::getChannelId)
        .collect(toList());
    List<UUID> qcMaskChannelIds = existingQcMasks.stream().map(QcMask::getChannelId)
        .collect(toList());

    Stream<UUID> allChannelIds = Stream
        .of(List.of(waveformsChannelId), sohChannelIds, qcMaskChannelIds)
        .flatMap(List::stream);

    Preconditions.checkArgument(
        allChannelIds.distinct().limit(2).count() <= 1,
        "Error generating QC Masks: Channel Ids must match across input waveforms, sohs, and qc masks");

    logger.info(
        "ChannelSohQcPlugin generateQcMasks invoked with {} waveforms,"
            + "{} Soh status changes, {} QcMasks", waveforms.getTimeseries().size(),
        sohStatusChanges.size(), existingQcMasks.size());

    ChannelSohPluginParameters pluginParameters = ObjectSerialization
        .fromFieldMap(parameterFieldMap, ChannelSohPluginParameters.class);

    // Filter out rejected and analyst defined qc masks, then group by QcMaskType
    Map<QcMaskType, List<QcMask>> existingByType = existingQcMasks.stream()
        .filter(qcMask -> !qcMask.getCurrentQcMaskVersion().isRejected())
        .filter(qcMask -> !QcMaskCategory.ANALYST_DEFINED.equals(qcMask.getCurrentQcMaskVersion().getCategory()))
        .collect(groupingBy(qcMask -> qcMask.getCurrentQcMaskVersion().getType().orElseThrow(
            NoSuchElementException::new)));

    return sohStatusChanges.stream()
        .filter(soh -> !pluginParameters.getExcludedTypes().contains(soh.getType()))
        .flatMap(
            soh -> createOutputQcMasks(soh,
                existingByType.getOrDefault(QcMaskUtility.getQcMaskType(soh.getType()), Collections.emptyList()),
                pluginParameters))
        .collect(toList());
  }

  /**
   * Creates the combination of new, existing, and merged {@link QcMask}s for the given {@link
   * ChannelSohStatusSegment}
   *
   * @param sohStatus Waveform state of health status information
   * @param existingMasks Collection of already created masks related to newly created masks (same Type and channel id)
   * @param sohPluginParameters Configuration parameters used to determine merge threshold
   * @return A Stream of output QcMasks, collected by another function.
   */
  private static Stream<QcMask> createOutputQcMasks(ChannelSohStatusSegment sohStatus,
      List<QcMask> existingMasks,
      ChannelSohPluginParameters sohPluginParameters) {

    List<QcMask> newQcMasks = createNewQcMasks(sohStatus);

    return newQcMasks.isEmpty() ? Stream.empty() :
        MergeQcMasks.merge(newQcMasks, existingMasks, sohPluginParameters.getMergeThreshold())
            .stream();
  }

  /**
   * Creates new {@link ChannelSohQcMask}s and converts them into COI {@link QcMask}s.
   *
   * @param sohStatus Waveform state of health status information
   * @return New QcMasks created by the plugin algorithm.
   */
  private static List<QcMask> createNewQcMasks(ChannelSohStatusSegment sohStatus) {

    List<ChannelSohQcMask> sohQcMasks = ChannelSohStatusParser
        .parseStatuses(sohStatus);

    return sohQcMasks.stream()
        .map(m -> createQcMask(sohStatus.getChannelId(), m))
        .collect(toList());
  }

  /**
   * Creates a {@link QcMask} from the info in {@link ChannelSohQcMask}
   *
   * @param channelSohQcMask the info needed to make the mask, generated from algorithm.
   * @return {@link QcMask}
   */
  private static QcMask createQcMask(UUID processingChannelId,
      ChannelSohQcMask channelSohQcMask) {

    return QcMask.create(processingChannelId, Collections.emptyList(), Collections.emptyList(),
        QcMaskCategory.STATION_SOH,
        QcMaskUtility.getQcMaskType(channelSohQcMask.getType()),
        QcMaskUtility.getSystemRationale(channelSohQcMask.getType()),
        channelSohQcMask.getStartTime(),
        channelSohQcMask.getEndTime());
  }

}
