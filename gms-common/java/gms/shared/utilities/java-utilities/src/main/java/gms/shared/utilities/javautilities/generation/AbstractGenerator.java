package gms.shared.utilities.javautilities.generation;

import java.util.Optional;

/**
 * An abstract base class for instances of {@code Generator<T>}
 * @param <T>
 */
public abstract class AbstractGenerator<T> implements Generator<T> {

  private volatile boolean checked;

  @Override
  public final Optional<T> generate() throws GenerationException {
    checkParametersIfNecessary();
    return Optional.ofNullable(doGenerate());
  }

  /**
   * Checks that all the required parameters have been set, but only if
   * the checked flag is true. If, after the check, all the parameters have
   * been set, checked is set to true to avoid work in the future. The
   * setters prevent unsetting parameters or setting them in invalid values.
   */
  protected void checkParametersIfNecessary() {
    if (!checked) {
      String[] unsetParameters = missingParameterNames();
      if (unsetParameters.length > 0) {
        throw new IllegalStateException("required parameters not set: " +
            String.join(", ", unsetParameters));
      } else {
        // It passed, so we never have to check again with this GridNodeGenerator.
        checked = true;
      }
    }
  }

  /**
   * Returns an array containing the names of parameters that have not been set.
   * @return an array, possibly of 0 length, but never null.
   */
  public abstract String[] missingParameterNames();

  /**
   * Do the actual generation of the entity, returning null if one cannot be
   * generated.
   * @return an instance of T, or null if one cannot be generated.
   */
  protected abstract T doGenerate() throws GenerationException;

}
