package gms.core.signaldetection.plugin;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;

public interface SignalDetectorPlugin extends Plugin {


  // TODO: Signal Detection is configurable by: region, station, channel, time of day, time of year.

  /**
   * Detects signal arrival times on the input {@link ChannelSegment}.  May use metadata from
   * the {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel}
   * to influence processing.
   * @param channelSegment detect signal arrivals in this ChannelSegment, not null
   * @param pluginParams
   * @return Collection of {@link Instant} signal arrival times, not null
   */
  Collection<Instant> detectSignals(ChannelSegment<Waveform> channelSegment,
      Map<String, Object> pluginParams);

  @Override
  default PluginVersion getVersion() {
    //TODO: delete from base interface
    return PluginVersion.from(1,0,0);
  }

  @Override
  default void initialize(Map<String, Object> parameterFieldMap) {
    //TODO: delete from base interface
  }
}
