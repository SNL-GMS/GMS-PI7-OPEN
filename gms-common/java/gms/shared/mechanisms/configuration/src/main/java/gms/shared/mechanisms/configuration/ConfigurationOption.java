package gms.shared.mechanisms.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ConfgurationOption is parameter values that can be resolved from a {@link Configuration} when a
 * list of {@link Constraint}s are satisfied.  ConfigurationOptions represent either default
 * parameters (both overall defaults or defaults to apply when other Constraints are satisfied) or
 * override parameters.  {@link ConfigurationResolver} performs the resolution.
 */
@AutoValue
public abstract class ConfigurationOption {

  /**
   * Obtains a new {@link ConfigurationOption} with provided name, {@link Constraint}s, and
   * parameters field map.  The Constraints must have unique {@link Constraint#getCriterion()}s.
   *
   * @param name String name, not null
   * @param constraints list of {@link Constraint}, not null
   * @param parameters field map, not null
   * @return {@link ConfigurationOption}, not nukk
   * @throws IllegalArgumentException if the Constraints do not have unique criterions
   */
  @JsonCreator
  public static ConfigurationOption from(
      @JsonProperty("name") String name,
      @JsonProperty("constraints") List<Constraint> constraints,
      @JsonProperty("parameters") Map<String, Object> parameters) {

    Objects.requireNonNull(constraints, "constraints can't be null");
    assertUniqueConstraintCriterions(constraints);

    return new AutoValue_ConfigurationOption(name, ImmutableList.copyOf(constraints),
        ImmutableMap.copyOf(parameters));
  }

  /**
   * Determines if any of the {@link Constraint}s have duplicated {@link Constraint#getCriterion()}
   * and raises an exception if there are any duplicates.
   *
   * @param constraints list of {@link Constraint}, not null
   * @throws IllegalArgumentException if the constraints list has any non-unique criterions
   */
  private static void assertUniqueConstraintCriterions(List<Constraint> constraints) {
    // Extract the criterion from each constraint, group in a (criterion -> #occurrences) map,
    // remove criterions with a single occurrence, and throw an exception if necessary.
    final String[] duplicateConstraints = constraints.stream()
        .map(Constraint::getCriterion)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
        .entrySet()
        .stream()
        .filter(e -> e.getValue() > 1)
        .map(Entry::getKey)
        .toArray(String[]::new);

    if (duplicateConstraints.length > 0) {
      throw new IllegalArgumentException("ConfigurationOption's Constraints must all have unique "
          + "criterions but these criterions appear in multiple Constraints: "
          + Arrays.toString(duplicateConstraints));
    }
  }

  /**
   * Obtain this ConfigurationOption's name
   * @return String, not null
   */
  public abstract String getName();

  /**
   * Obtain an immutable list of this ConfigurationOption's {@link Constraint}
   * @return immutable list of {@link Constraint}, not null
   */
  public abstract List<Constraint> getConstraints();

  /**
   * Obtain an immutable map containing this ConfigurationOption's parameters
   * @return immutable string to object map, not null
   */
  public abstract Map<String, Object> getParameters();
}
