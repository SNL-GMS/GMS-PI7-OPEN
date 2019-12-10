package gms.core.signaldetection.onsettimerefinement;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.Plugin;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Map;

public interface OnsetTimeRefinementPlugin extends Plugin {

  /**
   * Calculates the refined onset time for a given waveform and its current onset (arrival) time
   *  @param waveform the waveform from which to calculated the refined onset time
   * @param arrivalTime time determined by the system as the current onset time
   * @param pluginParams
   */
  Instant refineOnsetTime(Waveform waveform, Instant arrivalTime,
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
