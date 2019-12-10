package gms.shared.mechanisms.configuration;

import com.google.auto.value.AutoValue;
import java.util.function.Function;

/**
 * Defines a transformation from an instance of type T to an instance of type R
 *
 * @param <T> initial class type
 * @param <R> transformed class type
 */
@AutoValue
public abstract class ConfigurationTransform<T, R> {

  /**
   * Obtains the initial class type
   *
   * @return initial {@link Class} type, not null
   */
  public abstract Class<T> getInputType();

  /**
   * Obtains the transformed class type
   *
   * @return transformed {@link Class} type, not null
   */
  public abstract Class<R> getOutputType();

  /**
   * Obtains the {@link Function} transforming from {@link ConfigurationTransform#getInputType()} to
   * {@link ConfigurationTransform#getOutputType()}
   *
   * @return {@link Function}, not null
   */
  public abstract Function<T, R> getTransform();

  /**
   * Obtains a {@link ConfigurationTransform} transforming instances of the provided input type to
   * instances of the provided output type
   *
   * @param inputType initial {@link Class} type, not null
   * @param outputType transformed {@link Class} type, not null
   * @param transform {@link Function} transforming from {@link ConfigurationTransform#getInputType()}
   * to {@link ConfigurationTransform#getOutputType()}, not null
   * @param <T> initial class type
   * @param <R> transformed class type
   * @return {@link ConfigurationTransform}, not null
   */
  public static <T, R> ConfigurationTransform<T, R> from(Class<T> inputType, Class<R> outputType,
      Function<T, R> transform) {

    return new AutoValue_ConfigurationTransform<>(inputType, outputType, transform);
  }
}
