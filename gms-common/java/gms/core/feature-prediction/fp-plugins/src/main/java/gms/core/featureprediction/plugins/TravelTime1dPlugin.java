package gms.core.featureprediction.plugins;

import gms.shared.mechanisms.pluginregistry.Plugin;

/**
 * Represents a travel time model that is also a plugin. Each grid point is a scaler.
 */
public interface TravelTime1dPlugin extends Plugin, DepthDistance1dModelSet<double[], double[][]> {

}
