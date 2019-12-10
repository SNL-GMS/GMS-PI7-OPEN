package gms.core.featureprediction.plugins;

import gms.shared.mechanisms.pluginregistry.Plugin;

/**
 * Base interface for 1d distance models that represent Slowness, and that are also plugins.
 */
public interface SlownessUncertaintyPlugin extends Plugin, Distance1dModelSet {

}
