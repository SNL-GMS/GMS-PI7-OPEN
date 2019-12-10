package gms.shared.frameworks.pluginregistry;

/**
 * Interface common to all plugins in GMS.
 */
public interface Plugin {

  /**
   * Gets the name of the plugin, used for looking it up.
   * @return the name of the plugin
   */
  String getName();
}
