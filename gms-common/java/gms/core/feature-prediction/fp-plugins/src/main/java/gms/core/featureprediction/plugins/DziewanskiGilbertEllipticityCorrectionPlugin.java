package gms.core.featureprediction.plugins;

import gms.core.featureprediction.plugins.DepthDistance1dModelSet;
import gms.shared.mechanisms.pluginregistry.Plugin;
import org.apache.commons.lang3.tuple.Triple;

/**
 * Represents Dziewanski-Gilbert ellipticity correction model that is also a plugin. Each grid
 * point is a triple of values used to calculate an overall correction.
 */
public interface DziewanskiGilbertEllipticityCorrectionPlugin extends Plugin,
    DepthDistance1dModelSet<double[], Triple<double[][], double[][], double[][]>> {

}
