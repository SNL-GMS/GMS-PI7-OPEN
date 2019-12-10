package gms.core.waveformqc.waveformsignalqc.plugin;

import com.google.auto.service.AutoService;
import gms.core.waveformqc.plugin.WaveformQcPlugin;
import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegment;
import gms.shared.frameworks.pluginregistry.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component wrapping a {@link WaveformGapQcPlugin} by implementing the {@link WaveformQcPlugin}
 * interface. Provides a name and version number but defers logic to the WaveformGapQcPlugin.
 */
@AutoService(Plugin.class)
public class WaveformGapQcPluginComponent implements WaveformQcPlugin {

  private static final String PLUGIN_NAME = "waveformGapQcPlugin";
  private WaveformGapQcPlugin plugin;

  private static final Logger logger = LoggerFactory.getLogger(WaveformGapQcPluginComponent.class);

  public WaveformGapQcPluginComponent() {
    plugin = new WaveformGapQcPlugin();
  }

  /**
   * Obtains this plugin component's name
   *
   * @return String, not null
   */
  @Override
  public String getName() {
    return PLUGIN_NAME;
  }

  /**
   * Uses the wrapped {@link WaveformGapQcPlugin} to create gap {@link QcMask}s.
   *
   * @param waveforms {@link ChannelSegment}s to check for gaps, not null
   * @param sohStatusChanges {@link ChannelSohStatusSegment}, not null
   * @param existingQcMasks Previously created {@link QcMask}s that can affect processing, not null
   * @param parameterFieldMap {@link UUID} to a {@link gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo}
   * to associate with new QcMasks, not null
   * @return Stream of new or updated QcMasks, not null
   * @throws NullPointerException if channelSegments, channelSohStatusChanges, existingQcMasks, or
   * creationInfoId are null
   */
  @Override
  public List<QcMask> generateQcMasks(ChannelSegment<Waveform> waveforms,
      Collection<ChannelSohStatusSegment> sohStatusChanges,
      Collection<QcMask> existingQcMasks, Map<String, Object> parameterFieldMap) {

    Objects.requireNonNull(waveforms,
        "WaveformGapQcPluginComponent cannot generateQcMasks with null channelSegments");
    Objects.requireNonNull(sohStatusChanges,
        "WaveformGapQcPluginComponent cannot generateQcMasks with null channelSohStatusChanges");
    Objects.requireNonNull(existingQcMasks,
        "WaveformGapQcPluginComponent cannot generateQcMasks with null existingQcMasks");
    Objects.requireNonNull(parameterFieldMap,
        "WaveformGapQcPluginComponent cannot generateQcMasks with null creationInfoId");

    logger.info("WaveformGapQcPluginComponent generateQcMasks invoked with Channel Segment {},"
            + "{} Soh statuses, {} QcMasks", waveforms.getId(), sohStatusChanges.size(),
        existingQcMasks.size());

    if (null == plugin) {
      throw new IllegalStateException(
          "WaveformGapQcPluginComponent cannot be used before it is initialized");
    }

    return plugin.createQcMasks(waveforms, existingQcMasks, parameterFieldMap);
  }
}
