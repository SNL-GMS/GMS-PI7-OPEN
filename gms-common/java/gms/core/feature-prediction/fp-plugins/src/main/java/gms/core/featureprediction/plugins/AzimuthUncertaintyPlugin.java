package gms.core.featureprediction.plugins;

import gms.shared.mechanisms.pluginregistry.Plugin;

/**
 * Base interface for 1d distance models that represent Azimuth, and that are also plugins.
 */
public interface AzimuthUncertaintyPlugin extends Plugin, Distance1dModelSet {

}
