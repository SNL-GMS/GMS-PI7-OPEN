package gms.shared.utilities.javautilities.generation;

import gms.shared.utilities.javautilities.generation.GenerationException;
import java.util.Optional;

/**
 * Implementations of the {@code Generator} interface are reusable entities
 * that optionally generate instances of some class based upon a number of parameters.
 * Implementations typically have a no-arg constructor and have setters that
 * can be chained as in the builder pattern.
 * @param <T>
 */
public interface Generator<T> {

  /**
   * Generate an instance of an entity T, if possible.
   * @return return the entity wrapped by an optional, or an empty optional
   *   if an entity cannot be generated based upon the current paramters.
   */
  Optional<T> generate() throws GenerationException;

}
