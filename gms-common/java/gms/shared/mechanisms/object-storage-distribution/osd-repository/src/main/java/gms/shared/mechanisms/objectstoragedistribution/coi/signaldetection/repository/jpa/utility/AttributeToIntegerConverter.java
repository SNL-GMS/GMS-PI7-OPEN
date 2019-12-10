package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.persistence.AttributeConverter;

/**
 * Base class for {@link AttributeConverter} implementations which map instances of T to an integer
 * (e.g. enumerations)
 *
 * @param <T> class type this converter maps to and from integers
 */
class AttributeToIntegerConverter<T> implements AttributeConverter<T, Integer> {

  private final Map<T, Integer> toColumn;
  private final Map<Integer, T> fromColumn;

  /**
   * Obtains a new {@link AttributeToIntegerConverter} using the provided forward mapping of T
   * instances to integers
   *
   * @param forwardMapping mapping of each T to an integer, not null
   */
  AttributeToIntegerConverter(Map<T, Integer> forwardMapping) {

    // Normally don't throw exceptions from constructors but this class is meant to be overridden
    // by concrete implementations and can't use a static factory to construct the instances.
    Objects.requireNonNull(forwardMapping,
        "Cannot create AttributeToIntegerConverter with a null forwardMapping");

    toColumn = new HashMap<>(forwardMapping);

    fromColumn = new HashMap<>();
    toColumn.forEach((k, v) -> fromColumn.put(v, k));
  }

  @Override
  public Integer convertToDatabaseColumn(T filterType) {
    return toColumn.get(filterType);
  }

  @Override
  public T convertToEntityAttribute(Integer integer) {
    return fromColumn.get(integer);
  }
}
