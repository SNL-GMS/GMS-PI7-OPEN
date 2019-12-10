package gms.shared.mechanisms.configuration;

import gms.shared.mechanisms.configuration.constraints.DefaultConstraint;
import gms.shared.mechanisms.configuration.constraints.WildcardConstraint;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Processes {@link Configuration} to find which {@link ConfigurationOption} best matches a list of
 * {@link Selector}s.
 */
public class ConfigurationResolver {

  /**
   * Predicate testing true when the provided {@link Constraint} is not a {@link
   * WildcardConstraint}
   */
  private static final Predicate<Constraint> isNotWildcardConstraint =
      c -> !WildcardConstraint.class.isAssignableFrom(c.getClass());

  /**
   * Selector matching a {@link DefaultConstraint}
   */
  private static final Selector<String> defaultSelector = Selector
      .from(DefaultConstraint.CRITERION, "-");

  private ConfigurationResolver() {
  }

  /**
   * Processes the {@link Configuration} to find which {@link ConfigurationOption} best matches a
   * list of {@link Selector}s.  Returns that ConfigurationOption's values as a field map (a Map of
   * String to Object) Resolution checks each of the Configuration's ConfigurationOptions against
   * the provided Selectors using the {@link ConfigurationOption#getConstraints()}.  Each Selector
   * is checked against the corresponding Constraint (a Selector corresponds to a Constraint when
   * {@link Constraint#getCriterion()} matches {@link Selector#getCriterion()}).  There is a match
   * when the {@link Constraint#test(Object)} by the {@link Selector#getValue()}.  There are also
   * {@link WildcardConstraint} which trivially match any corresponding Selector.  A
   * ConfigurationOption is valid if there is a Selector corresponding to all
   * non-WildcardConstraints and the Selectors satisfy all of the corresponding Constraints.  A list
   * of Selectors will not match a ConfigurationOption if there is ​not​ a Selector for a Constraint
   * that exists in the ConfigurationOption.  However, if there is a Selector for a Constraint that
   * does not exist then the Selector is ignored, effectively treating missing Constraints like
   * WildcardConstraints.  If there are multiple valid ConfigurationOptions then tiebreakers are 1)
   * the valid ConfigurationOption with the most non-WildcardConstraints and 2) the
   * ConfigurationOption with the highest distinct {@link Constraint#getPriority()}.  If applying
   * the tiebreakers does not break the tie then the Configuration cannot be uniquely resolved for
   * the input Selectors and an IllegalStateException is thrown.  If there are no valid
   * ConfigurationOptions an IllegalStateException is thrown.
   *
   * The {@link Configuration} may also have ConfigurationOptions with a {@link
   * gms.shared.mechanisms.configuration.constraints.DefaultConstraint}.  These ConfigurationOptions
   * provide default values that are used if there are no valid ConfigurationOptions for the input
   * Selectors.
   *
   * @param configuration Configuration to resolve with the provided Selectors, not null
   * @param selectors Selectors to match against constraints in the Configuration's
   * ConfigurationOptions, not null
   * @return a field map (Map of String to Object), not null
   * @throws IllegalArgumentException if there is a repeated {@link Selector#getCriterion()} in the
   * provided Selectors
   * @throws NullPointerException if configuration or selectors are null
   * @throws IllegalStateException if the Selectors satisfy more than one ConfigurationOption; if
   * the selectors do not match any ConfigurationOptions
   */
  public static Map<String, Object> resolve(Configuration configuration, List<Selector> selectors) {
    Objects.requireNonNull(configuration, "Cannot resolve a null Configuration");
    Objects.requireNonNull(selectors, "Cannot resolve from null Selectors");

    // Validate each Selector is for a unique Criterion
    if (selectors.size() != selectors.stream().map(Selector::getCriterion).distinct().count()) {
      throw new IllegalArgumentException("Each Selector must be for a unique criterion");
    }

    // Resolve the best matching defaults for the provided selectors
    List<Selector> selectorsWithDefault = Stream
        .concat(selectors.stream(), Stream.of(defaultSelector)).collect(Collectors.toList());

    final Optional<Map<String, Object>> defaults =
        getBestMatchedParameters(configuration, selectorsWithDefault);

    // Resolve the best matching default overrides for the provided selectors
    final Optional<Map<String, Object>> overrides =
        getBestMatchedParameters(configuration, selectors);

    // Throw if there are no defaults and no default overrides matching the provided selectors
    if (!defaults.isPresent() && !overrides.isPresent()) {
      throw new IllegalStateException("Configuration did not resolve to any matching "
          + "ConfigurationOptions and also did not match any default ConfigurationOptions");
    }

    // Construct a field map containing the defaults field map with entries overwritten by
    // corresponding entries from the override field map
    final Map<String, Object> resolvedParameters = new HashMap<>(defaults.orElse(Map.of()));
    resolvedParameters.putAll(overrides.orElse(Map.of()));
    return resolvedParameters;
  }

  /**
   * Obtains an Optional field map containing the {@link ConfigurationOption#getParameters()} from
   * the {@link ConfigurationOption} best matched by the provided {@link Selector}s.
   *
   * @param configuration {@link Configuration}, not null
   * @param selectors {@link List} of {@link Selector}s, not null
   * @return Optional string to object map, not null
   */
  private static Optional<Map<String, Object>> getBestMatchedParameters(Configuration configuration,
      List<Selector> selectors) {

    // Find all matching ConfigurationOptions
    final List<ConfigurationOption> matches = configuration.getConfigurationOptions().stream()
        .filter(o -> match(o.getConstraints(), selectors))
        .collect(Collectors.toList());

    // Find the best matching ConfigurationOption
    return Optional.ofNullable(matches.isEmpty() ? null : findBestMatch(matches).getParameters());
  }

  /**
   * Determines whether the {@link Selector}s match the {@link Constraint}s.  Finds Selectors
   * corresponding to each Constraint using {@link Constraint#getCriterion()} and {@link
   * Selector#getCriterion()}.  A Selector trivially matches a {@link WildcardConstraint} but
   * otherwise {@link Constraint#test(Object)} must evaluate true for {@link Selector#getValue()}.
   * If Constraint is not a WildcardConstraint and there is no corresponding Selector then there is
   * no match.  The Constraints match with the Selectors if all Constraints are satisfied.
   *
   * @param constraints {@link List} of {@link Constraint}s, not null
   * @param selectors {@link List} of {@link Selector}s, not null
   * @return true if the Constraints match the Selectors and false otherwise
   */
  @SuppressWarnings("unchecked")
  private static boolean match(List<Constraint> constraints, List<Selector> selectors) {

    // Remove WildcardConstraints since they have no effect, then sort by Constraint criterion
    final List<Constraint> sortedConstraints = constraints.stream()
        .filter(isNotWildcardConstraint)
        .sorted(Comparator.comparing(Constraint::getCriterion))
        .collect(Collectors.toList());

    // Keep default Selectors and Selectors with criteria matching a Constraint criteria
    // (i.e. ignore Selectors that do not have corresponding Constraints, effectively treating
    // those constraints as WildcardConstraints that match the Selector), then sort by
    // Selector criterion
    final Set<String> constraintCriteria = sortedConstraints.stream()
        .map(Constraint::getCriterion)
        .collect(Collectors.toSet());

    final List<Selector> sortedSelectors = selectors.stream()
        .filter(s -> (defaultSelector.equals(s) || constraintCriteria.contains(s.getCriterion())))
        .sorted(Comparator.comparing(Selector::getCriterion))
        .collect(Collectors.toList());

    // After removing WildcardConstraints there can only be a match if each Constraint has a
    // Selector with a matching criterion
    if (sortedSelectors.size() != sortedConstraints.size()) {
      return false;
    }

    // Match if there is a Constraint match for each Selector
    return IntStream.range(0, sortedConstraints.size())
        .allMatch(
            i -> ConstraintEvaluator.evaluate(sortedConstraints.get(i), sortedSelectors.get(i)));
  }

  /**
   * Provided one or more {@link ConfigurationOption}s determine which ConfigurationOption wins in a
   * tiebreaking scenario. The rules are: 1) if there is a single ConfigurationOption then it wins
   * 2) the valid ConfigurationOption with the most non-WildcardConstraints and 3) the
   * ConfigurationOption with the highest distinct {@link Constraint#getPriority()}.
   *
   * @param matches apply tiebreakers to these {@link Collection} of {@link ConfigurationOption},
   * not null
   * @return ConfigurationOption winning the tiebreakers, not null
   */
  private static ConfigurationOption findBestMatch(Collection<ConfigurationOption> matches) {

    // Only 1 match - return it
    if (matches.size() == 1) {
      return matches.iterator().next();
    }

    // Multiple matches - apply tie breakers
    final Collection<ConfigurationOption> mostExactMatches = extractMostExactConstraintMatches(
        matches);

    // First tiebreaker is the number of exact Constraint matches.
    // If there is a winning ConfigurationOption then return it.
    if (mostExactMatches.size() == 1) {
      return mostExactMatches.iterator().next();
    }

    // Second tiebreaker is the priority of the exact matching Constraints.
    return tieBreakByConstraintPriority(mostExactMatches);
  }

  /**
   * Provided one or more {@link ConfigurationOption}s extract the ConfigurationOptions that have
   * the most non-{@link WildcardConstraint}s.  All of the returned ConfigurationOptions have the
   * same number of non-WildcardConstraints.
   *
   * @param matches {@link Collection} of {@link ConfigurationOption}, not empty, not null
   * @return Collection with a subset of the input ConfigurationOptions having the most
   * non-WildcardConstraints, not empty, not null.
   */
  private static Collection<ConfigurationOption> extractMostExactConstraintMatches(
      Collection<ConfigurationOption> matches) {

    // Determine the number of exact matches to each ConfigurationOption.  Since the Selectors
    // match all of the ConfigurationOptions this is the number of non-WildcardConstraints in
    // each ConfigurationOption
    final Map<Long, List<ConfigurationOption>> matchesByNumExactMatches = matches.stream().collect(
        Collectors
            .groupingBy(o -> o.getConstraints().stream().filter(isNotWildcardConstraint).count()));

    // Determine which ConfigurationOptions have the most exact matches
    final List<Long> exactMatchCountsDescending = matchesByNumExactMatches.keySet().stream()
        .sorted(Comparator.reverseOrder()).collect(Collectors.toList());

    return matchesByNumExactMatches.get(exactMatchCountsDescending.get(0));
  }

  /**
   * Provided a {@link Collection} of {@link ConfigurationOption}, determines which wins a
   * tiebreaker based on the {@link Constraint#getPriority()}.  This operation assumes there can be
   * ties in Constraint priority so it ignores Constraints that have duplicate priorities.  The
   * ConfigurationOption with the Constraint with the highest remaining priority wins the
   * tiebreaker.  If there are no remaining priorities then the tiebreaker is ambiguous and an
   * IllegalStateException is thrown.
   *
   * @param matches {@link Collection} of {@link ConfigurationOption}, not empty, not null
   * @return ConfigurationOption winning the constraint priority tiebreaker, not null
   * @throws IllegalStateException if the tiebreaker does not result in a unique
   * ConfigurationOption
   */
  private static ConfigurationOption tieBreakByConstraintPriority(
      Collection<ConfigurationOption> matches) {

    // Find the priorities of the non-WildcardConstraints in each ConfigurationOption.
    final Map<ConfigurationOption, SortedSet<Long>> prioritiesPerMatch = new HashMap<>();

    final Supplier<SortedSet<Long>> descendingSetSupplier = () -> new TreeSet<>(
        Comparator.reverseOrder());

    for (ConfigurationOption configurationOption : matches) {
      final SortedSet<Long> exactMatchPriorities = configurationOption.getConstraints().stream()
          .filter(isNotWildcardConstraint)
          .map(Constraint::getPriority)
          .collect(Collectors.toCollection(descendingSetSupplier));

      prioritiesPerMatch.put(configurationOption, exactMatchPriorities);
    }

    // Find the duplicate priorities
    final List<Long> duplicatePriorities = prioritiesPerMatch.values().stream()
        .flatMap(Set::stream)
        .collect(Collectors.groupingBy(Function.identity()))
        .entrySet()
        .stream()
        .filter(e -> e.getValue().size() > 1)
        .map(Entry::getKey)
        .collect(Collectors.toList());

    // Remove the duplicate priorities from each match
    prioritiesPerMatch.forEach((k, v) -> v.removeAll(duplicatePriorities));

    // Return the match with the highest remaining priority
    Optional<ConfigurationOption> bestMatch = prioritiesPerMatch.entrySet().stream()
        .filter(m -> !m.getValue().isEmpty())
        .max(Comparator.comparingLong(m -> m.getValue().first()))
        .map(Entry::getKey);

    // Since all the Selectors exactly match all of these Constraints, the winner has the Constraint
    // with the highest unique priority.  If there is no bestMatch then there is still have a tie
    // which means the Configuration does not resolve to a unique ConfigurationOption for the
    // provided Selectors.
    // TODO: tiebreak with match quality (e.g. small time interval is better than a large interval)?
    return bestMatch.orElseThrow(
        () -> new IllegalStateException("Configuration resolved to multiple ConfigurationOptions"));
  }
}