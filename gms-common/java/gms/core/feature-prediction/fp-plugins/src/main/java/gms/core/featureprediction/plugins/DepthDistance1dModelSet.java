package gms.core.featureprediction.plugins;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * Base interface for models based on depth and distance, such as travel time models and error
 * correction models. Depth/distance models can return differnt types of data, so this interface
 * is parameterized.
 *
 * @param <A> Axis type - the type (such as double[]) used to represent axis values
 * @param <T> Value type - the type (such as double[][]) used to represent two-dimensional grid
 * values. "Two dimensional" here means that there are two axis; grid values themeselves can have
 * any dimension.
 */
public interface DepthDistance1dModelSet<A, T> {

  void initialize(Set<String> earthModelNames) throws IOException;

  Set<String> getEarthModelNames();

  Set<PhaseType> getPhaseTypes(String earthModelName);

  A getDepthsKm(String earthModelName, PhaseType phase);

  A getDistancesDeg(String earthModelName, PhaseType phase);

  T getValues(String earthModelName, PhaseType phase);

  Optional<A> getDepthModelingErrors(String earthModelName, PhaseType phase);

  Optional<A> getDistanceModelingErrors(String earthModelName, PhaseType phase);

  Optional<T> getValueModelingErrors(String earthModelName, PhaseType phase);
}
