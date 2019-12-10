package gms.core.featureprediction.plugins;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.EventLocation;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.FeaturePredictionComponent;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.pluginregistry.Plugin;
import java.net.URL;
import java.util.Map;
import java.util.Set;

public interface EllipticityCorrectionPlugin extends Plugin {

  /**
   * returns the name of the plugin, which combined with the version gives us identification for
   * this plugin.
   *
   * @return The name of the plugin.
   */
  /*String getName();*/

  /**
   * returns the version of the plugin, which combined with the name gives us identification for
   * this plugin.
   *
   * @return The version of the plugin.
   */
  /*String getVersion();*/

  /**
   * Initializes the plugin via the provided {@link Map} representing the configuration values for
   * this plugin.  This function should be called before the plugin is used.
   *
   * @param earthModelNames Associates {@link String}s representing names of earth models with
   * {@link URL}s pointing to ellipticity correction files containing ellipticity correction values
   * for that earth model.
   */
  void initialize(Set<String> earthModelNames);

  /**
   * Calculates an ellipticity correction value
   *
   * @param modelName the name of the earth model for which to calculate an ellipticity correction
   * @param sourceLocation the {@link EventLocation} of the event
   * @param receiverLocation the {@link Location} of the receiver
   * @param phaseType the {@link PhaseType} for which to calculate an ellipticity correction
   * @return the ellipticity correction value
   */
  FeaturePredictionComponent correct(String modelName, EventLocation sourceLocation,
      Location receiverLocation, PhaseType phaseType);
}
