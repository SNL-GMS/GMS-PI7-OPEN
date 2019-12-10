package gms.core.signalenhancement.waveformfiltering.plugin;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegmentDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.util.Collection;
import java.util.Map;

public interface FilterPlugin extends Plugin {

  @Override
  default PluginVersion getVersion() {
    return PluginVersion.from(1, 0, 0);
  }

  @Override
  default void initialize(Map<String, Object> parameterFieldMap) {
    // no initialization required by default
  }

  /**
   * Generates a sequence of filtered waveforms
   *
   * @param channelSegment {@link ChannelSegment} containing waveforms to filter, not null
   * @param pluginParams a field map containing plugin-specific parameters for filtering, not null
   * @return New filtered waveforms
   */
  Collection<Waveform> filter(ChannelSegment<Waveform> channelSegment,
      Map<String, Object> pluginParams);

  default ChannelSegmentDescriptor getProcessingDescriptor(ChannelSegmentDescriptor descriptor,
      Map<String, Object> pluginParams) {
    return descriptor;
  }
}
