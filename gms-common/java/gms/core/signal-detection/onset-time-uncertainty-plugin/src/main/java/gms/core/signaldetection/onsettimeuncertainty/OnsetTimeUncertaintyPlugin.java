package gms.core.signaldetection.onsettimeuncertainty;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public interface OnsetTimeUncertaintyPlugin extends Plugin {

  /**
   * Calculates the onset time uncertainty for the provided waveform and onset time (pick)
   *
   * @param waveform The waveform for which the onsetx time uncertainty will be calculated
   * @param pick The Instant of the onset time to calculate uncertainty for
   * @param pluginParams
   * @return
   */
  Duration calculateOnsetTimeUncertainty(Waveform waveform, Instant pick,
      Map<String, Object> pluginParams);

  @Override
  default PluginVersion getVersion() {
    //TODO: remove from base plugin interface
    return PluginVersion.from(1,0,0);
  }

  @Override
  default void initialize(Map<String, Object> parameterFieldMap) {
    //TODO: remove from base plugin interface
  }
}
