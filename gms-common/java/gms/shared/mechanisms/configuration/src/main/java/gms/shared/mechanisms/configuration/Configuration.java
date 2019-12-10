package gms.shared.mechanisms.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import gms.shared.mechanisms.configuration.constraints.WildcardConstraint;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Configuration that can be resolved into parameters by a {@link ConfigurationResolver}. Groups
 * related {@link ConfigurationOption}s which provide the parameters that should be used when their
 * {@link ConfigurationOption#getConstraints()} are satisfied during resolution.
 */
@AutoValue
@JsonPropertyOrder({"name", "default", "configurationOptions"})
public abstract class Configuration {

  // TODO: add UUID?

  /**
   * Obtains a new {@link Configuration} with the provided name and {@link ConfigurationOption}s
   * with changeTime of the current time. When the same type of {@link Constraint} (based on {@link
   * Constraint#getCriterion()}) appears in multiple ConfigurationOptions, all of the non-{@link
   * WildcardConstraint}s must have the same {@link Constraint#getPriority()} in each
   * ConfigurationOption.
   *
   * @param name configuration name, not null
   * @param configurationOptions ConfigurationOptions in the new Configuration, not null
   * @return {@link Configuration}, not null
   * @throws NullPointerException if name or configurationOptions are null
   * @throws IllegalArgumentException if Constraints for the same criterion do not have the same
   * priority in all ConfigurationOptions
   */
  public static Configuration from(String name,
      Collection<ConfigurationOption> configurationOptions) {
    return Configuration.from(name, configurationOptions, Instant.now());
  }

  /**
   * Obtains a new {@link Configuration} with the provided name, {@link ConfigurationOption}s, and
   * changeTime. When the same type of {@link Constraint} (based on {@link
   * Constraint#getCriterion()}) appears in multiple ConfigurationOptions, all of the non-{@link
   * WildcardConstraint}s must have the same {@link Constraint#getPriority()} in each
   * ConfigurationOption.
   *
   * @param name configuration name, not null
   * @param configurationOptions ConfigurationOptions in the new Configuration, not null
   * @param changeTime {@link Instant} when the Configuration was changed
   * @return {@link Configuration}, not null
   * @throws NullPointerException if name or configurationOptions are null
   * @throws IllegalArgumentException if Constraints for the same criterion do not have the same
   * priority in all ConfigurationOptions
   */
  @JsonCreator
  public static Configuration from(
      @JsonProperty("name") String name,
      @JsonProperty("configurationOptions") Collection<ConfigurationOption> configurationOptions,
      @JsonProperty("changeTime") Instant changeTime) {

    Objects.requireNonNull(configurationOptions, "configurationOptions can't be null");
    assertConstraintsHaveConsistentPriorities(configurationOptions);

    return new AutoValue_Configuration(name, ImmutableList.copyOf(configurationOptions),
        changeTime);
  }

  /**
   * Validates when the same type of {@link Constraint} (based on {@link Constraint#getCriterion()})
   * appears in multiple ConfigurationOptions all of the non-{@link WildcardConstraint}s must have
   * the same {@link Constraint#getPriority()} in each ConfigurationOption.
   *
   * @param configurationOptions check Constraint priorities for these {@link ConfigurationOption}s,
   * not null
   * @throws IllegalArgumentException if the ConfigurationOptions fail the assertion
   */
  private static void assertConstraintsHaveConsistentPriorities(
      Collection<ConfigurationOption> configurationOptions) {
    final BinaryOperator<Set<Long>> concat = (l, r) -> Stream.of(l, r)
        .collect(HashSet::new, Set::addAll, Set::addAll);

    // Get the Constraints from each ConfigurationOption, remove the WildcardConstraints, and
    // collect them into a Criterion -> Set<Priorities> map.  Any criterion mapped to a set with
    // more than 1 element fails the assertion that non-WildcardConstraints for the same criterion
    // have consistent priorities.
    final String[] constraintsWithInconsistentPriorities = configurationOptions.stream()
        .flatMap(co -> co.getConstraints().stream())
        .filter(c -> !ConstraintType.WILDCARD.equals(c.getConstraintType()))
        .collect(Collectors.toMap(Constraint::getCriterion, c -> Set.of(c.getPriority()), concat))
        .entrySet().stream()
        .filter(e -> e.getValue().size() > 1)
        .map(Entry::getKey)
        .toArray(String[]::new);

    if (constraintsWithInconsistentPriorities.length > 0) {
      throw new IllegalArgumentException("Constraints for the same criterion must have the "
          + "same priority in all ConfigurationOptions, but this is not true for the following "
          + "criteria: " + Arrays.toString(constraintsWithInconsistentPriorities));
    }
  }

  public abstract String getName();

  public abstract ImmutableCollection<ConfigurationOption> getConfigurationOptions();

  public abstract Instant getChangeTime();
}
