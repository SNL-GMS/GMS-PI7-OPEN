package gms.shared.mechanisms.configuration.client;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.configuration.Configuration;
import gms.shared.mechanisms.configuration.ConfigurationOption;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.ConfigurationTransform;
import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import gms.shared.mechanisms.configuration.Selector;
import gms.shared.mechanisms.configuration.TestUtilities;
import gms.shared.mechanisms.configuration.constraints.NumericScalarConstraint;
import gms.shared.mechanisms.configuration.constraints.WildcardConstraint;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfigurationConsumerUtilityTests {

  private static final String configurationKey = "[component-name]-configuration";

  private static final FooParameters fooParamsDefaults = FooParameters.from(100, "string100", true);
  private static final Map<String, Object> fooParamsDefaultsMap = ObjectSerialization
      .toFieldMap(fooParamsDefaults);

  private static final NumericScalarConstraint snrIs5 = NumericScalarConstraint
      .from("snr", Operator.from(Type.EQ, false), 5.0, 100);

  private static final ConfigurationOption configOptDefault = ConfigurationOption
      .from("SNR-5", List.of(WildcardConstraint.from("snr")), fooParamsDefaultsMap);

  private static final ConfigurationOption configOptSnrIs5 = ConfigurationOption
      .from("SNR-5", List.of(snrIs5), Map.of("a", 10));

  private Configuration configurationSnrIs5 = Configuration
      .from(configurationKey, List.of(configOptDefault, configOptSnrIs5));

  @Mock
  private ConfigurationRepository configurationRepository;

  /**
   * Mocks configurationRepository to return an List of Configurations containing
   * configurationSnrIs5 when queried with configurationKey.  Does not return any configurations for
   * the global configuration key prefix.
   */
  private void mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults() {
    Mockito.when(configurationRepository.getKeyRange(Mockito.anyString()))
        .thenAnswer(invocation -> {
          Object argument = invocation.getArguments()[0];
          if (argument.equals(configurationKey)) {
            return List.of(configurationSnrIs5);
          } else if (argument
              .equals(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX)) {
            return List.of();
          }
          throw new InvalidUseOfMatchersException(
              String.format("Argument %s does not match", argument)
          );
        });
  }

  private ConfigurationConsumerUtility getClientUtilMockGmsConfig() {
    mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults();
    return ConfigurationConsumerUtility.builder(configurationRepository)
        .configurationNamePrefixes(List.of(configurationKey)).build();
  }

  /**
   * Mocks configurationRepository to return an empty List when queried with configurationKey
   */
  private void mockGmsConfigurationToReturnEmptyListNoGlobalDefaults() {
    Mockito.when(configurationRepository.getKeyRange(Mockito.anyString()))
        .thenAnswer(invocation -> {
          Object argument = invocation.getArguments()[0];
          if (argument.equals(configurationKey)) {
            return List.of();
          } else if (argument
              .equals(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX)) {
            return List.of();
          }
          throw new InvalidUseOfMatchersException(
              String.format("Argument %s does not match", argument)
          );
        });
  }

  @Test
  void testBuildNoKeys() {
    final ConfigurationConsumerUtility clientUtility =
        ConfigurationConsumerUtility.builder(configurationRepository).build();

    // When no keys are provided only the globally reference configuration should be loaded
    verify(configurationRepository, Mockito.times(1)).getKeyRange(Mockito.anyString());
    verify(configurationRepository, Mockito.times(1))
        .getKeyRange(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX);

    // TODO: verify watches are setup

    assertNotNull(clientUtility);
  }

  @Test
  void testBuildNullConfigurationRepositoryExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(() -> ConfigurationConsumerUtility.builder(null),
        NullPointerException.class, "Requires non-null ConfigurationRepository");
  }

  @Test
  void testBuildLoadsKeys() {
    final ConfigurationConsumerUtility clientUtility = getClientUtilMockGmsConfig();

    verify(configurationRepository, Mockito.times(1)).getKeyRange(configurationKey);

    // TODO: verify watches are setup
    assertNotNull(clientUtility);
  }

  @Test
  void testBuildNullConfigurationKeysExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(
        () -> ConfigurationConsumerUtility.builder(configurationRepository)
            .configurationNamePrefixes(null),
        NullPointerException.class, "Requires non-null configurationNamePrefixes"
    );
  }

  @Test
  void testBuildConfigurationItemDoesNotExistExpectIllegalStateException() {
    mockGmsConfigurationToReturnEmptyListNoGlobalDefaults();

    TestUtilities.expectExceptionAndMessage(
        () -> ConfigurationConsumerUtility.builder(configurationRepository)
            .configurationNamePrefixes(List.of(configurationKey)).build(),
        IllegalStateException.class,
        "No Configuration(s) found for key prefix(es) [" + configurationKey + "]"
    );
  }

  @Test
  void testBuildNullTransformsExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(
        () -> ConfigurationConsumerUtility.builder(configurationRepository).transforms(null),
        NullPointerException.class, "Requires non-null transforms"
    );
  }

  @Test
  void testBuildLoadsGlobalDefaults() {
    mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults();

    ConfigurationConsumerUtility.builder(configurationRepository)
        .configurationNamePrefixes(List.of(configurationKey))
        .build();

    verify(configurationRepository, Mockito.times(1))
        .getKeyRange(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX);
  }

  @Test
  void testGlobalDefaultsCanBeEmpty() {
    mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults();

    assertDoesNotThrow(() ->
        ConfigurationConsumerUtility.builder(configurationRepository)
            .configurationNamePrefixes(List.of(configurationKey))
            .build());
  }

  @Test
  void testLoadConfigurations() {
    mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults();

    final ConfigurationConsumerUtility clientUtility = ConfigurationConsumerUtility
        .builder(configurationRepository)
        .build();
    assertNotNull(clientUtility);

    verify(configurationRepository, Mockito.times(0)).getKeyRange(configurationKey);

    clientUtility.loadConfigurations(List.of(configurationKey));
    verify(configurationRepository, Mockito.times(1)).getKeyRange(configurationKey);
    Mockito.verifyNoMoreInteractions(configurationRepository);

    // TODO: verify watches are setup
  }

  @Test
  void testLoadConfigurationsDoesNotReloadExistingConfigurations() {
    final ConfigurationConsumerUtility clientUtility = getClientUtilMockGmsConfig();
    clientUtility.loadConfigurations(List.of(configurationKey));
    Mockito.verifyNoMoreInteractions(configurationRepository);
  }

  @Test
  void testLoadConfigurationsItemDoesNotExistExpectIllegalStateException() {
    mockGmsConfigurationToReturnEmptyListNoGlobalDefaults();

    final ConfigurationConsumerUtility clientUtility = ConfigurationConsumerUtility
        .builder(configurationRepository)
        .build();
    TestUtilities.expectExceptionAndMessage(
        () -> clientUtility.loadConfigurations(List.of(configurationKey)),
        IllegalStateException.class,
        "No Configuration(s) found for key prefix(es) [" + configurationKey + "]"
    );
  }

  @Test
  void testLoadConfigurationsValidatesParameters() {
    final ConfigurationConsumerUtility clientUtility = getClientUtilMockGmsConfig();

    TestUtilities.expectExceptionAndMessage(
        () -> clientUtility.loadConfigurations(null),
        NullPointerException.class,
        "Requires non-null configurationNamePrefixes"
    );
  }

  private static class NamedInt {

    @JsonProperty
    private int named;

    @JsonCreator
    private NamedInt(
        @JsonProperty("named") int named) {
      this.named = named;
    }
  }

  @Test
  void testBuildAppliesTransforms() {
    mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults();

    @SuppressWarnings("unchecked") final Function<FooParameters, NamedInt> aFromFoo = mock(
        Function.class);
    Mockito.when(aFromFoo.apply(Mockito.any())).thenReturn(new NamedInt(-100));

    ConfigurationConsumerUtility.builder(configurationRepository)
        .configurationNamePrefixes(List.of(configurationKey))
        .transforms(Map.of(configurationKey,
            ConfigurationTransform.from(FooParameters.class, NamedInt.class, aFromFoo)))
        .build();

    verify(aFromFoo, Mockito.times(2)).apply(Mockito.any());
  }

  @Test
  void testLoadConfigurationsAppliesTransforms() {
    mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults();

    @SuppressWarnings("unchecked") final Function<FooParameters, NamedInt> aFromFoo = mock(
        Function.class);
    Mockito.when(aFromFoo.apply(Mockito.any())).thenReturn(new NamedInt(-100));

    final ConfigurationConsumerUtility clientUtility = ConfigurationConsumerUtility
        .builder(configurationRepository)
        .transforms(Map.of(configurationKey,
            ConfigurationTransform.from(FooParameters.class, NamedInt.class, aFromFoo)))
        .build();

    verify(aFromFoo, Mockito.times(0)).apply(Mockito.any());

    clientUtility.loadConfigurations(List.of(configurationKey));
    verify(aFromFoo, Mockito.times(2)).apply(Mockito.any());
  }

  @Test
  void testResolveProducesTransformedResult() {
    mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults();

    @SuppressWarnings("unchecked") final Function<FooParameters, NamedInt> aFromFoo = mock(
        Function.class);
    Mockito.when(aFromFoo.apply(Mockito.any())).thenReturn(new NamedInt(-100));

    final ConfigurationConsumerUtility configurationConsumerUtility = ConfigurationConsumerUtility
        .builder(configurationRepository)
        .configurationNamePrefixes(List.of(configurationKey))
        .transforms(Map.of(configurationKey,
            ConfigurationTransform.from(FooParameters.class, NamedInt.class, aFromFoo)))
        .build();

    final NamedInt resolvedParams = configurationConsumerUtility
        .resolve(configurationKey, List.of(Selector.from("snr", -5.0)), NamedInt.class);

    assertAll(
        () -> assertNotNull(resolvedParams),
        () -> assertEquals(-100, resolvedParams.named)
    );
  }

  @Test
  void testTransformFailsExpectIllegalArgumentException() {
    mockGmsConfigurationToReturnPresentConfigurationItemNoGlobalDefaults();

    @SuppressWarnings("unchecked") final Function<FooParameters, NamedInt> aFromFoo = mock(
        Function.class);
    Mockito.when(aFromFoo.apply(Mockito.any())).thenThrow(IllegalArgumentException.class);

    final ConfigurationTransform<FooParameters, NamedInt> transform = ConfigurationTransform
        .from(FooParameters.class, NamedInt.class, aFromFoo);

    final Executable createConfigurationConsumerUtility = () -> ConfigurationConsumerUtility
        .builder(configurationRepository)
        .configurationNamePrefixes(List.of(configurationKey))
        .transforms(Map.of(configurationKey, transform))
        .build();

    TestUtilities.expectExceptionAndMessage(
        createConfigurationConsumerUtility,
        IllegalArgumentException.class,
        "Could not transform Configuration '" + configurationKey + "'"
    );
  }

  @Test
  void testResolveToFieldMap() {
    final Map<String, Object> resolvedParamsFieldMap = getClientUtilMockGmsConfig()
        .resolve(configurationKey, List.of(Selector.from("snr", -5.0)));

    assertAll(
        () -> assertNotNull(resolvedParamsFieldMap),
        () -> assertEquals(fooParamsDefaultsMap, resolvedParamsFieldMap)
    );
  }

  @Test
  void testResolveUnknownConfigurationKeyExpectIllegalArgumentException() {
    final String unknownKey = "unknown-key";
    TestUtilities.expectExceptionAndMessage(
        () -> getClientUtilMockGmsConfig().resolve(unknownKey, List.of(), Number.class),
        IllegalArgumentException.class, "No Configuration named " + unknownKey + " is in this"
    );
  }

  @Test
  void testResolveToObjectFromClass() {
    final FooParameters resolvedParams = getClientUtilMockGmsConfig()
        .resolve(configurationKey, List.of(Selector.from("snr", -5.0)), FooParameters.class);

    assertAll(
        () -> assertNotNull(resolvedParams),
        () -> assertEquals(fooParamsDefaults, resolvedParams)
    );
  }

  @Test
  void testParameterClassNotCreatableExpectIllegalArgumentException() {
    TestUtilities.expectExceptionAndMessage(
        () -> getClientUtilMockGmsConfig().resolve(configurationKey, List.of(), Number.class),
        IllegalArgumentException.class,
        "Resolved Configuration is not a valid instance of " + Number.class.getCanonicalName()
    );
  }

  @Test
  void testResolveNullConfigurationNameExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(
        () -> getClientUtilMockGmsConfig().resolve(null, List.of()),
        NullPointerException.class,
        "Cannot resolve Configuration for null configurationName"
    );
  }

  @Test
  void testResolveNullSelectorsExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(
        () -> getClientUtilMockGmsConfig().resolve(configurationKey, null),
        NullPointerException.class,
        "Cannot resolve Configuration for null selectors"
    );
  }

  @Test
  void testResolveNullParametersClassExpectNullPointerException() {
    TestUtilities.expectExceptionAndMessage(
        () -> getClientUtilMockGmsConfig().resolve(configurationKey, List.of(), null),
        NullPointerException.class,
        "Cannot resolve Configuration to null parametersClass"
    );
  }
}
