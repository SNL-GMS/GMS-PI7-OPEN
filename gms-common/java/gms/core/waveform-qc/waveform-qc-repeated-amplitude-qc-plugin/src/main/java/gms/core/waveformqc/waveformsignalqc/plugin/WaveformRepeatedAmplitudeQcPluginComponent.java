package gms.core.waveformqc.waveformsignalqc.plugin;

import com.google.auto.service.AutoService;
import gms.core.waveformqc.plugin.WaveformQcPlugin;
import gms.core.waveformqc.plugin.objects.ChannelSohStatusSegment;
import gms.core.waveformqc.waveformsignalqc.algorithm.WaveformRepeatedAmplitudeInterpreter;
import gms.shared.frameworks.pluginregistry.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A component wrapping a {@link WaveformRepeatedAmplitudeQcPlugin} by implementing the {@link
 * WaveformQcPlugin} interface. Provides a name and version number but defers logic to the
 * WaveformGapQcPlugin.
 */
@AutoService(Plugin.class)
public class WaveformRepeatedAmplitudeQcPluginComponent implements WaveformQcPlugin {

  private static final String PLUGIN_NAME = "waveformRepeatedAmplitudeQcPlugin";
  private WaveformRepeatedAmplitudeQcPlugin plugin;

  public WaveformRepeatedAmplitudeQcPluginComponent() {
    plugin = WaveformRepeatedAmplitudeQcPlugin.create(new WaveformRepeatedAmplitudeInterpreter());
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
   * Uses the wrapped {@link WaveformRepeatedAmplitudeQcPlugin} to create gap {@link QcMask}s.
   *
   * @param waveforms {@link ChannelSegment}s to check for repeated adjacent amplitudes, not null
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

    Objects.requireNonNull(waveforms,
        "WaveformRepeatedAmplitudeQcPluginComponent cannot generateQcMasks with null channelSegments");
    Objects.requireNonNull(sohStatusChanges,
        "WaveformRepeatedAmplitudeQcPluginComponent cannot generateQcMasks with null channelSohStatusChanges");
    Objects.requireNonNull(existingQcMasks,
        "WaveformRepeatedAmplitudeQcPluginComponent cannot generateQcMasks with null existingQcMasks");
    Objects.requireNonNull(parameterFieldMap,
        "WaveformRepeatedAmplitudeQcPluginComponent cannot generateQcMasks with null parameterFieldMap");

    return plugin.createQcMasks(waveforms, existingQcMasks, parameterFieldMap);
  }
}
