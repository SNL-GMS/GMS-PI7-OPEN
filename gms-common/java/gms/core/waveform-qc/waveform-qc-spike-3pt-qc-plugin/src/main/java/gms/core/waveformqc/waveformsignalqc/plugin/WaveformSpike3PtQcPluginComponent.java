package gms.core.waveformqc.waveformsignalqc.plugin;

import com.google.auto.service.AutoService;
import gms.core.waveformqc.plugin.WaveformQcPlugin;
import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegment;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformSpike3PtInterpreter;
import gms.shared.frameworks.pluginregistry.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component wrapping a {@link WaveformSpike3PtQcPlugin} by implementing the {@link
 * WaveformSpike3PtQcPlugin} interface. Provides a name and version number but defers logic to the
 * WaveformGapQcPlugin.
 */
@AutoService(Plugin.class)
public class WaveformSpike3PtQcPluginComponent implements WaveformQcPlugin {

  private static final String PLUGIN_NAME = "waveformSpike3PtQcPlugin";
  private WaveformSpike3PtQcPlugin plugin;

  private static final Logger logger = LoggerFactory
      .getLogger(WaveformSpike3PtQcPluginComponent.class);

  public WaveformSpike3PtQcPluginComponent() {
    plugin = WaveformSpike3PtQcPlugin.create(new WaveformSpike3PtInterpreter());
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
   * Uses the wrapped {@link WaveformSpike3PtQcPlugin} to create spike {@link QcMask}s.
   *
   * @param waveforms {@link ChannelSegment}s to check for spikes, not null
   * @param sohStatusChanges {@link ChannelSohStatusSegment}, not null
   * @param existingQcMasks Previously created {@link QcMask}s that can affect processing, not null
   * @param parameterFieldMap A field map of plugin configuration parameters, not null
   * @return Stream of new or updated QcMasks, not null
   * @throws NullPointerException if channelSegments, channelSohStatusChanges, existingQcMasks, or
   * creationInfoId are null
   */
  @Override
  public List<QcMask> generateQcMasks(ChannelSegment<Waveform> waveforms,
      Collection<ChannelSohStatusSegment> sohStatusChanges,
      Collection<QcMask> existingQcMasks, Map<String, Object> parameterFieldMap) {

    Objects.requireNonNull(waveforms);
    Objects.requireNonNull(sohStatusChanges);
    Objects.requireNonNull(existingQcMasks);
    Objects.requireNonNull(parameterFieldMap);

    logger.info("WaveformSpike3PtQcPluginComponent generateQcMasks invoked with Channel Segment {},"
            + "{} Soh statuses, {} QcMasks", waveforms.getId(), sohStatusChanges.size(),
        existingQcMasks.size());

    return plugin.createQcMasks(waveforms, existingQcMasks, parameterFieldMap);
  }

}
