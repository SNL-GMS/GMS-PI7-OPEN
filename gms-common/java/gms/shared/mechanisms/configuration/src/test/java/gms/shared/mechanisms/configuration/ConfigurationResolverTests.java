package gms.shared.mechanisms.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gms.shared.mechanisms.configuration.Operator.Type;
import gms.shared.mechanisms.configuration.client.FooParameters;
import gms.shared.mechanisms.configuration.constraints.DefaultConstraint;
import gms.shared.mechanisms.configuration.constraints.NumericScalarConstraint;
import gms.shared.mechanisms.configuration.constraints.WildcardConstraint;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfigurationResolverTests {

  private NumericScalarConstraint snrIs5;
  private NumericScalarConstraint bazIs10;
  private WildcardConstraint snrIsWildcard;

  private ConfigurationOption configOptDefault;
  private ConfigurationOption configOptSnrIs5BazIs10;
  private ConfigurationOption configOptSnrIs5BazIsWildcard;

  private FooParameters fooParamsDefaults;
  private Map<String, Object> snrIs5ParametersFieldMap;
  private Map<String, Object> snrIs5WIthSnr5DefaultParametersFieldMap;
  private Map<String, Object> fooParamsSnrIs5BazIs10FieldMap;
  private Map<String, Object> fooParamsSnrIs5BazIsWildcardFieldMap;
  private Map<String, Object> fooParamsDefaultsFieldMap;

  private Configuration configurationSnrIs5;
  private Configuration configurationSnrIs5BazIs10;
  private Configuration configurationSnrIs5BazIsWildcard;
  private Configuration configurationSnrIs5WithSnr5Default;
  private Configuration configurationSnrIs5NoDefaults;

  @BeforeEach
  void setUp() {
    snrIs5 = NumericScalarConstraint.from("snr", Operator.from(Type.EQ, false), 5.0, 100);
    bazIs10 = NumericScalarConstraint.from("baz", Operator.from(Type.EQ, false), 10.0, 1000);
    snrIsWildcard = WildcardConstraint.from("snr");
    final WildcardConstraint bazIsWildcard = WildcardConstraint.from("baz");

    fooParamsDefaults = FooParameters.from(100, "string100", true);
    final FooParameters fooParamsSnr5Defaults = fooParamsDefaults.toBuilder().c(false).build();
    fooParamsDefaultsFieldMap = ObjectSerialization.toFieldMap(fooParamsDefaults);
    configOptDefault = ConfigurationOption
        .from("GLOBAL-DEFAULT", List.of(DefaultConstraint.from(), snrIsWildcard, bazIsWildcard),
            fooParamsDefaultsFieldMap);
    final ConfigurationOption configOptSnrIs5Default = ConfigurationOption
        .from("GLOBAL-DEFAULT", List.of(DefaultConstraint.from(), snrIs5, bazIsWildcard),
            ObjectSerialization.toFieldMap(fooParamsSnr5Defaults));

    final ConfigurationOption configOptSnrIs5 = ConfigurationOption
        .from("SNR-5", List.of(snrIs5), Map.of("a", 10));
    configOptSnrIs5BazIs10 = ConfigurationOption
        .from("(SNR-5)/(BAZ-10)", List.of(snrIs5, bazIs10), Map.of("a", 20));
    configOptSnrIs5BazIsWildcard = ConfigurationOption
        .from("(SNR-5)/(BAZ-WILDCARD)", List.of(snrIs5, bazIsWildcard), Map.of("a", 30));

    snrIs5ParametersFieldMap = ObjectSerialization.toFieldMap(
        fooParamsDefaults.toBuilder().a(10).build());
    snrIs5WIthSnr5DefaultParametersFieldMap = ObjectSerialization.toFieldMap(
        fooParamsSnr5Defaults.toBuilder().a(10).build());
    fooParamsSnrIs5BazIs10FieldMap = ObjectSerialization.toFieldMap(
        fooParamsDefaults.toBuilder().a(20).build());
    fooParamsSnrIs5BazIsWildcardFieldMap = ObjectSerialization.toFieldMap(
        fooParamsDefaults.toBuilder().a(30).build());

    configurationSnrIs5 = Configuration
        .from("TEST-CONFIG", List.of(configOptDefault, configOptSnrIs5));
    configurationSnrIs5WithSnr5Default = Configuration
        .from("TEST-CONFIG", List.of(configOptDefault, configOptSnrIs5Default, configOptSnrIs5));
    configurationSnrIs5BazIs10 = Configuration
        .from("TEST-CONFIG", List.of(configOptDefault, configOptSnrIs5BazIs10));
    configurationSnrIs5BazIsWildcard = Configuration
        .from("TEST-CONFIG", List.of(configOptDefault, configOptSnrIs5BazIsWildcard));
    configurationSnrIs5NoDefaults = Configuration.from("TEST-CONFIG", List.of(configOptSnrIs5));
  }

  /**
   * Verifies ConfigurationResolver returns the Configuration default when there are no selectors
   */
  @Test
  void testResolvesConfigurationDefaultWhenNoSelectors() {
    assertEquals(fooParamsDefaultsFieldMap,
        ConfigurationResolver.resolve(configurationSnrIs5, List.of()));
  }

  @Test
  void testResolvesAmongMultipleDefaults() {
    assertEquals(snrIs5WIthSnr5DefaultParametersFieldMap, ConfigurationResolver
        .resolve(configurationSnrIs5WithSnr5Default, List.of(Selector.from("snr", 5.0))));
  }

  @Test
  void testResolvesConfigurationOptionWhenSelectorMatchesSingleConstraint() {
    assertEquals(snrIs5ParametersFieldMap, ConfigurationResolver
        .resolve(configurationSnrIs5, List.of(Selector.from("snr", 5.0))));
  }

  @Test
  void testResolvesDefaultWhenSingleConstraintDoesNotMatch() {
    assertEquals(fooParamsDefaultsFieldMap, ConfigurationResolver
        .resolve(configurationSnrIs5, List.of(Selector.from("snr", -5.0))));
  }

  @Test
  void testResolvesMultipleSelectors() {
    assertEquals(fooParamsSnrIs5BazIs10FieldMap,
        ConfigurationResolver.resolve(configurationSnrIs5BazIs10,
            List.of(Selector.from("snr", 5.0), Selector.from("baz", 10.0))));
  }

  @Test
  void testResolvesConfigurationOptionWithSelectorsOutOfOrderWithConstraints() {
    assertEquals(fooParamsSnrIs5BazIs10FieldMap,
        ConfigurationResolver.resolve(configurationSnrIs5BazIs10,
            List.of(Selector.from("baz", 10.0), Selector.from("snr", 5.0))));
  }

  @Test
  void testResolveIgnoresSelectorsNotMatchingAnyConstraintCriterions() {
    assertEquals(fooParamsSnrIs5BazIs10FieldMap,
        ConfigurationResolver.resolve(configurationSnrIs5BazIs10,
            List.of(Selector.from("bar", -20.0), Selector.from("baz", 10.0),
                Selector.from("snr", 5.0))));
  }

  @Test
  void testResolveMatchesWildcards() {
    assertEquals(fooParamsSnrIs5BazIsWildcardFieldMap, ConfigurationResolver
        .resolve(configurationSnrIs5BazIsWildcard,
            List.of(Selector.from("baz", 10.0), Selector.from("snr", 5.0))));
  }

  @Test
  void testResolveNoSelectorWithCriterionForWildcardConstraintExpectConfigurationOptionMatch() {
    assertEquals(fooParamsSnrIs5BazIsWildcardFieldMap, ConfigurationResolver
        .resolve(configurationSnrIs5BazIsWildcard, List.of(Selector.from("snr", 5.0))));
  }

  @Test
  void testResolveNoSelectorWithCriterionForNonWildcardConstraintExpectDefaultParameters() {
    assertEquals(fooParamsDefaultsFieldMap, ConfigurationResolver
        .resolve(configurationSnrIs5BazIsWildcard, List.of(Selector.from("baz", 5.0))));
  }

  /**
   * Ties occur when the Selectors match multiple ConfigurationOptions.  The first tie breaker is
   * the number of exact matches (e.g. non-WildcardConstraint matches).  In this example the
   * Selectors match both ConfigurationOptions but one of them has 1 exact match and one wildcard
   * while the other has 2 exact matches.
   */
  @Test
  void testResolveUsesNumberOfExactMatchesWhenSelectorsMatchMultipleConfigurationOptions() {
    final Configuration configuration = Configuration.from("TEST-CONFIG",
        List.of(configOptDefault, configOptSnrIs5BazIs10, configOptSnrIs5BazIsWildcard));

    assertEquals(fooParamsSnrIs5BazIs10FieldMap, ConfigurationResolver.resolve(configuration,
        List.of(Selector.from("baz", 10.0), Selector.from("snr", 5.0))));
  }

  /**
   * Ties occur when the Selectors match multiple ConfigurationOptions.  The first tie breaker is
   * the number of exact matches (e.g. non-WildcardConstraint matches).  The second tie breaker is
   * the priority of the exact matching Constraints.  In this example the Selectors match both
   * ConfigurationOptions with 1 exact match and 1 wildcard match.
   */
  @Test
  void testResolveUsesPriorityToBreakTies() {
    final ConfigurationOption configOptSnrIsWildcardBazIs10 = ConfigurationOption
        .from("(SNR-WILDCARD)/(BAZ-10)", List.of(snrIsWildcard, bazIs10), Map.of("a", 40));

    final Map<String, Object> fooParamsSnrIsWildcardBazIs10 = ObjectSerialization
        .toFieldMap(fooParamsDefaults.toBuilder().a(40).build());

    final Configuration configuration = Configuration.from("TEST-CONFIG",
        List.of(configOptDefault, configOptSnrIs5BazIsWildcard, configOptSnrIsWildcardBazIs10));

    assertEquals(fooParamsSnrIsWildcardBazIs10, ConfigurationResolver.resolve(configuration,
        List.of(Selector.from("baz", 10.0), Selector.from("snr", 5.0))));
  }

  /**
   * Tie where multiple ConfigurationOptions have the same number of exact matches and the exact
   * matches have the same priorities.  This means the Configuration did not resolve to a single
   * ConfigurationOption.
   */
  @Test
  void testResolveHasTieExpectIllegalStateException() throws Exception {

    // Priority of barIs20 matches snrIs5 so the resolution will match both ConfigurationOptions
    // and neither tie breaker will resolve the tie.
    final Constraint barIs20 = NumericScalarConstraint
        .from("bar", Operator.from(Type.EQ, false), 20.0, snrIs5.getPriority());
    final Constraint barIsWildcard = WildcardConstraint.from("bar");

    final ConfigurationOption configOptSnrIsWildcardBarIs20 = ConfigurationOption
        .from("(SNR-WILDCARD)/(BAR-20)", List.of(snrIsWildcard, barIs20), Map.of("a", 40));

    final ConfigurationOption configOptSnrIs5BarIsWildcard = ConfigurationOption
        .from("(SNR-5)/(BAR-WILDCARD)", List.of(snrIs5, barIsWildcard), Map.of("a", 30));

    final Configuration configuration = Configuration.from("TEST-CONFIG",
        List.of(configOptDefault, configOptSnrIs5BarIsWildcard, configOptSnrIsWildcardBarIs20));

    TestUtilities.expectExceptionAndMessage(
        () -> ConfigurationResolver
            .resolve(configuration,
                List.of(Selector.from("snr", 5.0), Selector.from("bar", 20.0))),
        IllegalStateException.class,
        "Configuration resolved to multiple ConfigurationOptions"
    );
  }

  @Test
  void testResolveDuplicateSelectorCriteriaExpectIllegalArgumentException() {
    TestUtilities.expectExceptionAndMessage(
        () -> ConfigurationResolver
            .resolve(configurationSnrIs5,
                List.of(Selector.from("A", 1.0), Selector.from("A", 1.0))),
        IllegalArgumentException.class,
        "Each Selector must be for a unique criterion");
  }

  @Test
  void testResolveNullConfigurationExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(
        () -> ConfigurationResolver.resolve(null, List.of()),
        NullPointerException.class,
        "Cannot resolve a null Configuration"
    );
  }

  @Test
  void testResolveNullSelectorsExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(
        () -> ConfigurationResolver.resolve(configurationSnrIs5, null),
        NullPointerException.class,
        "Cannot resolve from null Selectors"
    );
  }

  @Test
  void testResolveFindsNoMatchesExpectIllegalArgumentException() {
    TestUtilities.expectExceptionAndMessage(
        () -> ConfigurationResolver
            .resolve(configurationSnrIs5NoDefaults, List.of(Selector.from("snr", -5.0))),
        IllegalStateException.class,
        "Configuration did not resolve to any matching ConfigurationOptions and also did "
            + "not match any default ConfigurationOptions");
  }
}
