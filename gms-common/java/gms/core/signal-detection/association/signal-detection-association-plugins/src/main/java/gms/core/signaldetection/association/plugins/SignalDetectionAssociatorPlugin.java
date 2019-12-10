package gms.core.signaldetection.association.plugins;

import gms.shared.mechanisms.pluginregistry.Name;
import gms.shared.mechanisms.pluginregistry.Plugin;
import gms.shared.mechanisms.pluginregistry.Version;

/**
 *
 * Interface for turning core functionality of associator into plugin
 *
 */
public interface SignalDetectionAssociatorPlugin extends SignalDetectionAssociator, Plugin {

  //TODO: This will contain inhereited methods to replace annotations,
  //TODO:    once the plugin registry is updated.

}
