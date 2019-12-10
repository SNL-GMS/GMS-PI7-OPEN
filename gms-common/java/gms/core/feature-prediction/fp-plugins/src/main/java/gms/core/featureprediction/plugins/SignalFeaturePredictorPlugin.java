package gms.core.featureprediction.plugins;

import gms.core.featureprediction.common.objects.PluginConfiguration;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePrediction;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionCorrection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import java.io.IOException;


/**
 * Interface defining signal feature prediction
 */
public interface SignalFeaturePredictorPlugin {

  /*
   * returns the name of the plugin, which combined with the version gives us identification for
   * this plugin.
   *
   * @return The name of the plugin.
   */
  /*String getName();*/

  /*
   * returns the version of the plugin, which combined with the name gives us identification for
   * this plugin.
   *
   * @return The version of the plugin.
   */
  /*String getVersion();*/

  /**
   * Initialize this plugin using the provided {@link PluginConfiguration}. Used to configure how
   * the plugin should run.
   *
   * @param configuration Plugin configuration.
   */
  SignalFeaturePredictorPlugin initialize(PluginConfiguration configuration) throws IOException;

  /**
   * Returns a map of the input receiver locations onto their associated {@link FeaturePrediction}s
   *
   * @param earthModel 1D Earth Model
   * @param type which feature is being requested
   * @param sourceLocation location of waveform source
   * @param receiverLocation location of waveform receiver
   * @param phase waveform phase
   * @param corrections corrections for sensor elevation
   * @return predicted value of feature
   * @throws IOException if 1D Earth Models fail to load
   */
  FeaturePrediction<?> predict(String earthModel,
      FeatureMeasurementType<?> type,
      EventLocation sourceLocation,
      Location receiverLocation,
      PhaseType phase,
      FeaturePredictionCorrection[] corrections) throws IOException;
}