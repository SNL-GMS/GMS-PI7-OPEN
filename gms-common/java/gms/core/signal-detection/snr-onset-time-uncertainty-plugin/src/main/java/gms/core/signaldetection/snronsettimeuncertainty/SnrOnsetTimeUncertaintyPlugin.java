package gms.core.signaldetection.snronsettimeuncertainty;

import gms.core.signaldetection.onsettimeuncertainty.OnsetTimeUncertaintyPlugin;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public class SnrOnsetTimeUncertaintyPlugin implements OnsetTimeUncertaintyPlugin {

  private static final String PLUGIN_NAME = "snrOnsetTimeUncertaintyPlugin";

  public SnrOnsetTimeUncertaintyPlugin() {
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
   * Calculates the onset time uncertainty for the provided waveform and onset time (pick)
   *
   * @param waveform The waveform for which the onset time uncertainty will be calculated
   * @param pick The Instant of the onset time to calculate uncertainty for
   * @param pluginParams serialized {@link SnrOnsetTimeUncertaintyParameters}
   */
  @Override
  public Duration calculateOnsetTimeUncertainty(Waveform waveform, Instant pick,
      Map<String, Object> pluginParams) {

    Objects.requireNonNull(waveform,
        "SnrOnsetTimeUncertaintyPlugin calculateOnsetTimeUncertainty requires non-null waveform");
    Objects.requireNonNull(pick,
        "SnrOnsetTimeUncertaintyPlugin calculateOnsetTimeUncertainty requires non-null pick");
    Objects.requireNonNull(pluginParams,
        "SnrOnsetTimeUncertaintyPlugin calculateOnsetTimeUncertainty requires non-null pluginParams");

    double value = SnrOnsetTimeUncertaintyAlgorithm.calculateUncertainty(waveform, pick,
        ObjectSerialization.fromFieldMap(pluginParams, SnrOnsetTimeUncertaintyParameters.class));

    return Duration.ofNanos((long) (value * 1E9));
  }
}
