package gms.core.featureprediction.plugins;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * Base interface for 1 dimensional model sets (such as slowness or azimuth uncertainty) based
 * on distance
 */
public interface Distance1dModelSet {

  void initialize(Set<String> earthModelNames) throws IOException;

  Set<String> getEarthModelNames();

  Set<PhaseType> getPhaseTypes(String earthModelName);

  double[] getDistancesDeg(String earthModelName, PhaseType phase);

  double[] getValues(String earthModelName, PhaseType phase);

  Optional<double[]> getDistanceModelingErrors(String earthModelName, PhaseType phase);

  Optional<double[]> getValueModelingErrors(String earthModelName, PhaseType phase);
}