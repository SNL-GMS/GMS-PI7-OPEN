package gms.core.signalenhancement.beam.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.core.signalenhancement.beam.TestFixtures;
import gms.shared.mechanisms.configuration.Configuration;
import gms.shared.mechanisms.configuration.ConfigurationOption;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import gms.shared.mechanisms.configuration.client.GlobalConfigurationReferenceResolver;
import gms.shared.mechanisms.configuration.constraints.StringConstraint;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PluginVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ProcessingGroupDescriptor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BeamConfigurationTests {

  @Mock
  private ConfigurationRepository mockConfigurationRepository;

  private static final Predicate<InvocationOnMock> isPrefixQuery = invocation ->
      BeamConfiguration.BEAM_PREFIX.equals(invocation.getArguments()[0]);

  private static final Predicate<InvocationOnMock> isGlobalReferenceQuery = invocation ->
      GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX
          .equals(invocation.getArguments()[0]);

  private final ProcessingGroupDescriptor processingGroupDescriptor = TestFixtures.getProcessingGroupDescriptor();

  private static final String channelProcessingGroupCriterion = "channel-processing-group-id";

  @Test
  void testCreate() {
    mockConfigurationRepositoryDefaults();

    final BeamConfiguration config = BeamConfiguration.create(mockConfigurationRepository);
    assertNotNull(config);

    // Validate BeamConfiguration loads all of the configurations
    Mockito.verify(mockConfigurationRepository, Mockito.times(1))
        .getKeyRange(BeamConfiguration.BEAM_PREFIX);
  }

  @Test
  void testCreateValidatesParameters() {
    final NullPointerException exception = assertThrows(NullPointerException.class,
        () -> BeamConfiguration.create(null));
    assertTrue(exception.getMessage()
        .contains("BeamConfiguration cannot be created with null ConfigurationRepository"));
  }

  @Test
  void testGetInteractivePluginRegistrationInfo() {
    final RegistrationInfo expectedRegistrationInfo = RegistrationInfo
        .from("interactive", PluginVersion.from(9, 8, 7));

    final Collection<Configuration> configurations = List.of(
        Configuration.from(BeamConfiguration.INTERACTIVE_BEAM_CONFIGURATION_KEY,
            List.of(ConfigurationOption.from("INTERACTIVE", List.of(),
                ObjectSerialization.toFieldMap(expectedRegistrationInfo)))));

    // Mock configuration for the interactive beam plugin registration info
    final Predicate<InvocationOnMock> isPrefixQuery = invocation ->
        BeamConfiguration.BEAM_PREFIX.equals(invocation.getArguments()[0]);

    Mockito.when(
        mockConfigurationRepository.getKeyRange(Mockito.anyString())).thenAnswer(invocation -> {
      if (isPrefixQuery.test(invocation)) {
        return configurations;
      } else if (isGlobalReferenceQuery.test(invocation)) {
        return List.of();
      }
      throw new InvalidUseOfMatchersException(
          String.format("Arguments %s do not match", Arrays.toString(invocation.getArguments()))
      );
    });

    final BeamConfiguration config = BeamConfiguration.create(mockConfigurationRepository);
    final RegistrationInfo actualRegistrationInfo = config.getInteractivePluginRegistrationInfo();

    assertAll(
        () -> assertNotNull(actualRegistrationInfo),
        () -> assertEquals(expectedRegistrationInfo, actualRegistrationInfo)
    );
  }

  @Test
  void testGetAutomaticPluginRegistration() {
    final RegistrationInfo expectedRegistrationInfo = RegistrationInfo
        .from("automatic1", PluginVersion.from(9, 8, 7));

    final Collection<Configuration> configurations = List.of(
        Configuration.from(BeamConfiguration.AUTOMATIC_BEAM_PLUGIN_CONFIGURATION_KEY,
            List.of(ConfigurationOption.from("AUTOMATIC", List.of(),
                ObjectSerialization.toFieldMap(expectedRegistrationInfo)))));

    // Mock configuration for the interactive beam plugin registration info
    Mockito.when(mockConfigurationRepository.getKeyRange(Mockito.anyString()))
        .thenAnswer(invocation -> {
          if (isPrefixQuery.test(invocation)) {
            return configurations;
          } else if (isGlobalReferenceQuery.test(invocation)) {
            return List.of();
          }
          throw new InvalidUseOfMatchersException(
              String.format("Arguments %s do not match", Arrays.toString(invocation.getArguments()))
          );
        });

    final BeamConfiguration config = BeamConfiguration.create(mockConfigurationRepository);
    final RegistrationInfo actualRegistrationInfo = config
        .getAutomaticPluginRegistrationInfo(processingGroupDescriptor);

    assertAll(
        () -> assertNotNull(actualRegistrationInfo),
        () -> assertEquals(expectedRegistrationInfo, actualRegistrationInfo)
    );
  }

  private void mockConfigurationRepositoryDefaults() {
    Mockito.when(mockConfigurationRepository.getKeyRange(Mockito.anyString()))
        .thenAnswer(invocation -> {
          if (isPrefixQuery.test(invocation)) {
            return List.of(Configuration.from("mockConfiguration", List.of()));
          } else if (isGlobalReferenceQuery.test(invocation)) {
            return List.of();
          }
          throw new InvalidUseOfMatchersException(
              String.format("Arguments %s do not match", Arrays.toString(invocation.getArguments()))
          );
        });
  }

  @Test
  void testGetAutomaticPluginRegistrationValidatesParameters() {
    mockConfigurationRepositoryDefaults();
    final BeamConfiguration config = BeamConfiguration.create(mockConfigurationRepository);

    assertAll(
        () -> assertTrue(assertThrows(NullPointerException.class,
            () -> config.getAutomaticPluginRegistrationInfo(null)).getMessage()
            .contains("Cannot lookup plugin RegistrationInfo for null ProcessingGroupDescriptor"))
    );
  }

  @Test
  void testGetAutomaticBeamDefinitionsValidatesParameters() {
    mockConfigurationRepositoryDefaults();
    final BeamConfiguration config = BeamConfiguration.create(mockConfigurationRepository);

    assertAll(
        () -> assertTrue(assertThrows(NullPointerException.class,
            () -> config.getAutomaticBeamDefinitions(null)).getMessage()
            .contains("Cannot lookup BeamDefinitions for null ProcessingGroupDescriptor"))
    );
  }

  @Test
  void testGetAutomaticBeamDefinitions() {

    // Sample BeamDefinitionFile returned by mocked ConfigurationConsumerUtility
    final BeamDefinitionFile beamDefinitionFile = BeamDefinitionFile.from(
        PhaseType.P,
        true,
        true,
        false,
        20.0,
        1.0,
        List.of(
            SlownessAzimuthPair.from(1, 2),
            SlownessAzimuthPair.from(3, 4),
            SlownessAzimuthPair.from(5, 6),
            SlownessAzimuthPair.from(7, 8),
            SlownessAzimuthPair.from(9, 10)
        ),
        Location.from(10, 20, 30, 40),
        Map.of(
            UUID.randomUUID(), RelativePosition.from(1, 2, 3),
            UUID.randomUUID(), RelativePosition.from(4, 5, 6),
            UUID.randomUUID(), RelativePosition.from(7, 8, 9)),
        2);

    final Collection<Configuration> configurations = List.of(
        Configuration.from(BeamConfiguration.AUTOMATIC_BEAM_DEFINITIONS_CONFIGURATION_KEY,
            List.of(ConfigurationOption.from("DEFINITIONS",
                List.of(StringConstraint.from(channelProcessingGroupCriterion,
                    Operator.from(Type.EQ, false),
                    Set.of(processingGroupDescriptor.getProcessingGroupId().toString()), 100)),
                ObjectSerialization.toFieldMap(beamDefinitionFile)))));

    // Mock configuration for the interactive beam plugin registration info
    final Predicate<InvocationOnMock> isPrefixQuery = invocation ->
        BeamConfiguration.BEAM_PREFIX.equals(invocation.getArguments()[0]);

    Mockito.when(mockConfigurationRepository.getKeyRange(Mockito.anyString()))
        .thenAnswer(invocation -> {
          if (isPrefixQuery.test(invocation)) {
            return configurations;
          } else if (isGlobalReferenceQuery.test(invocation)) {
            return List.of();
          }

          throw new InvalidUseOfMatchersException(
              String.format("Arguments %s do not match", Arrays.toString(invocation.getArguments()))
          );
        });

    final BeamConfiguration config = BeamConfiguration.create(mockConfigurationRepository);
    final List<BeamDefinition> automaticBeamDefinitions = config
        .getAutomaticBeamDefinitions(processingGroupDescriptor);

    // Make sure there are as many BeamDefinitionAndChannelIdPair as there are AzimuthSlownessPairs
    // in the BeamDefinitionFile, and
    final int expectedNumOfBeamDefinitions = beamDefinitionFile.getBeamGrid().size();
    assertAll(
        () -> assertNotNull(automaticBeamDefinitions),
        () -> assertEquals(expectedNumOfBeamDefinitions, automaticBeamDefinitions.size()),

        // Each definition (azimuth, slowness) corresponds to a recipe entry
        () -> assertTrue(automaticBeamDefinitions.stream()
            .allMatch(bd -> equalsOmitAzimuthSlowness(beamDefinitionFile, bd)))
    );
  }

  /**
   * Determines whether the {@link BeamDefinitionAndChannelIdPair} is contained in the {@link
   * BeamDefinitionFile} (the recipe contains many (azimuth, slowness) but the definition only
   * includes one).
   *
   * @param beamDefinitionFile {@link BeamDefinitionFile}, not null
   * @param beamDefinition {@link BeamDefinitionAndChannelIdPair}, not null
   * @return true if the BeamDefinitionAndChannelIdPair is an entry in the recipe
   */
  private static boolean equalsOmitAzimuthSlowness(BeamDefinitionFile beamDefinitionFile,
      BeamDefinition beamDefinition) {

    final Predicate<BeamDefinition> azSlowInRecipeInRecipe =
        bd -> beamDefinitionFile.getBeamGrid().contains(SlownessAzimuthPair.from(
            bd.getSlowness(),
            bd.getAzimuth()));

    final Predicate<BeamDefinition> definitionInRecipe = bd ->
        toFieldMap(beamDefinitionFile, List.of("beamGrid"))
            .equals(toFieldMap(bd, List.of("azimuth", "slowness")));

    return azSlowInRecipeInRecipe.and(definitionInRecipe).test(beamDefinition);
  }

  private static Map<String, Object> toFieldMap(Object object, Collection<String> keysToRemove) {
    final Map<String, Object> fieldMap = ObjectSerialization.toFieldMap(object);
    keysToRemove.forEach(fieldMap::remove);
    return fieldMap;
  }
}
