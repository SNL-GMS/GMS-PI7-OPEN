package gms.core.signaldetection.onsettimerefinement;

import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public class AicOnsetTimeRefinementPlugin implements OnsetTimeRefinementPlugin {

  private static final String PLUGIN_NAME = "aicOnsetTimeRefinementPlugin";
  private static final Logger logger = LoggerFactory.getLogger(AicOnsetTimeRefinementPlugin.class);

  @Override
  public String getName() {
    return PLUGIN_NAME;
  }

  @Override
  public Instant refineOnsetTime(Waveform waveform, Instant arrivalTime,
      Map<String, Object> pluginParams) {

    Objects.requireNonNull(waveform, "(Waveform) Input waveform cannot be null.");
    Objects.requireNonNull(arrivalTime, "(Instant) Arrival time cannot be null.");

    AicOnsetTimeRefinementParameters parameters = ObjectSerialization
        .fromFieldMap(pluginParams, AicOnsetTimeRefinementParameters.class);


    return AicOnsetTimeRefinementAlgorithm.refineOnsetTime(waveform, arrivalTime, parameters);
  }
}
