package gms.core.signalenhancement.beam.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.configuration.Configuration;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.ConfigurationTransform;
import gms.shared.mechanisms.configuration.Selector;
import gms.shared.mechanisms.configuration.client.ConfigurationConsumerUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.RegistrationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ProcessingGroupDescriptor;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Utility class interacting with the Configuration mechanism via {@link
 * ConfigurationConsumerUtility} to provide parameters to {@link BeamControl}.
 * <p>
 * Which {@link gms.core.signalenhancement.beamcontrol.plugin.BeamPlugin} to invoke is separated
 * from the
 * {@link gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition}s
 * since the current implementation assumes any BeamDefinitionFile can be used with any BeamPlugin.
 */
public class BeamConfiguration {

  private static final String SEPARATOR = ".";
  static final String BEAM_PREFIX = "beam-command-executioner" + SEPARATOR;
  private static final UnaryOperator<String> buildKey = s -> BEAM_PREFIX + s;

  static final String AUTOMATIC_BEAM_PLUGIN_CONFIGURATION_KEY = buildKey
      .apply("automatic-configuration-plugin");

  static final String AUTOMATIC_BEAM_DEFINITIONS_CONFIGURATION_KEY = buildKey
      .apply("automatic-configuration-beam-definitions");
  private static final String CHANNEL_PROCESSING_GROUP_ID_CRITERION = "channel-processing-group-id";

  static final String INTERACTIVE_BEAM_CONFIGURATION_KEY = buildKey
      .apply("interactive-configuration");

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  private BeamConfiguration(ConfigurationConsumerUtility configurationConsumerUtility) {
    this.configurationConsumerUtility = configurationConsumerUtility;
  }

  /**
   * Obtain a new {@link BeamConfiguration} using the provided {@link ConfigurationRepository} to
   * provide beaming configuration.
   *
   * @param configurationRepository {@link ConfigurationRepository}, not null
   * @return {@link BeamConfiguration}, not null
   * @throws NullPointerException if configurationConsumerUtility is null
   */
  public static BeamConfiguration create(ConfigurationRepository configurationRepository) {
    Objects.requireNonNull(configurationRepository,
        "BeamConfiguration cannot be created with null ConfigurationRepository");

    // Construct a ConfigurationConsumerUtility with the provided configurationRepository and
    // the necessary ConfigurationTransforms
    final ConfigurationConsumerUtility configurationConsumerUtility = ConfigurationConsumerUtility
        .builder(configurationRepository)
        .configurationNamePrefixes(List.of(buildKey.apply("")))
        .transforms(getConfigurationTransforms())
        .build();

    return new BeamConfiguration(configurationConsumerUtility);
  }

  /**
   * Obtain the {@link ConfigurationTransform} the {@link ConfigurationConsumerUtility} should use
   * for {@link Configuration}s with each {@link Configuration#getName()}
   *
   * @return map of Configuration name to ConfigurationTransform, not null
   */
  private static Map<String, ConfigurationTransform<?, ?>> getConfigurationTransforms() {
    return Map.of(AUTOMATIC_BEAM_DEFINITIONS_CONFIGURATION_KEY,
        ConfigurationTransform
            .from(BeamDefinitionFile.class, BeamDefinitionWrapper.class,
                BeamConfiguration::toBeamDefinitionAndChannelIdPairs));
  }

  /**
   * Obtain the {@link RegistrationInfo} for the default beaming plugin used for {@link
   * BeamStreamingCommand} execution
   *
   * @return {@link RegistrationInfo}, not null
   */
  public RegistrationInfo getInteractivePluginRegistrationInfo() {
    return configurationConsumerUtility
        .resolve(INTERACTIVE_BEAM_CONFIGURATION_KEY, List.of(), RegistrationInfo.class);
  }

  /**
   * Obtain the {@link RegistrationInfo}s for the
   * {@link gms.core.signalenhancement.beamcontrol.plugin.BeamPlugin}
   * to use when executing a particular {@link ProcessingGroupDescriptor}
   *
   * @param processingGroupDescriptor {@link ProcessingGroupDescriptor} providing contextual
   *                                                                   information
   *                                  about the processing request, not null
   * @return {@link RegistrationInfo}, not null
   * @throws NullPointerException if processingGroupDescriptor is null
   */
  public RegistrationInfo getAutomaticPluginRegistrationInfo(
      ProcessingGroupDescriptor processingGroupDescriptor) {
    Objects.requireNonNull(processingGroupDescriptor,
        "Cannot lookup plugin RegistrationInfo for null ProcessingGroupDescriptor");

    // TODO: add additional selectors based on the data time interval, etc.

    return configurationConsumerUtility.resolve(
        AUTOMATIC_BEAM_PLUGIN_CONFIGURATION_KEY,
        List.of(toChannelProcessingGroupSelector.apply(processingGroupDescriptor)),
        RegistrationInfo.class);
  }

  /**
   * Obtain the {@link BeamDefinitionAndChannelIdPair}s to use when executing a particular {@link
   * ProcessingGroupDescriptor}
   *
   * @param processingGroupDescriptor {@link ProcessingGroupDescriptor} providing contextual
   *                                                                   information
   *                                  about the processing request, not null
   * @return List of {@link BeamDefinitionAndChannelIdPair}, not null
   * @throws NullPointerException if processingGroupDescriptor is null
   */
  public List<BeamDefinition> getAutomaticBeamDefinitions(
      ProcessingGroupDescriptor processingGroupDescriptor) {

    Objects.requireNonNull(processingGroupDescriptor,
        "Cannot lookup BeamDefinitions for null ProcessingGroupDescriptor");

    // TODO: add additional selectors based on the data time interval, etc.
    return configurationConsumerUtility.resolve(
        AUTOMATIC_BEAM_DEFINITIONS_CONFIGURATION_KEY,
        List.of(toChannelProcessingGroupSelector.apply(processingGroupDescriptor)),
        BeamDefinitionWrapper.class
    ).wrapped;
  }

  /**
   * Obtain a {@link Selector} for a ChannelProcessingGroup id from a
   * {@link ProcessingGroupDescriptor}
   */
  private static final Function<ProcessingGroupDescriptor, Selector<String>>
      toChannelProcessingGroupSelector = c ->
      Selector.from(CHANNEL_PROCESSING_GROUP_ID_CRITERION, c.getProcessingGroupId().toString());

  /**
   * Obtains a list of {@link BeamDefinitionAndChannelIdPair} from a {@link BeamDefinitionFile}.
   * There is a separate definition for each of the points in
   * {@link BeamDefinitionFile#getBeamGrid()}
   *
   * @param beamDefinitionFile {@link BeamDefinitionFile}, not null
   * @return list of {@link BeamDefinitionAndChannelIdPair}, not null
   */
  private static BeamDefinitionWrapper toBeamDefinitionAndChannelIdPairs(
      BeamDefinitionFile beamDefinitionFile) {

    return new BeamDefinitionWrapper(
        beamDefinitionFile.getBeamGrid().stream().map(point ->
            BeamDefinition.from(
                beamDefinitionFile.getPhaseType(),
                point.getAzimuth(),
                point.getSlowness(),
                beamDefinitionFile.isCoherent(),
                beamDefinitionFile.isSnappedSampling(),
                beamDefinitionFile.isTwoDimensional(),
                beamDefinitionFile.getNominalWaveformSampleRate(),
                beamDefinitionFile.getWaveformSampleRateTolerance(),
                beamDefinitionFile.getBeamPoint(),
                beamDefinitionFile.getRelativePositionsByChannelId(),
                beamDefinitionFile.getMinimumWaveformsForBeam())
        ).collect(Collectors.toList())
    );
  }

  /**
   * Utility class wrapping a List of {@link BeamDefinitionAndChannelIdPair} since the
   * deserialization in {@link ConfigurationConsumerUtility#resolve(String, List, Class)} requires
   * all fields to be named but a BeamDefinitionAndChannelIdPair[] is anonymous unless it is wrapped
   * in another class.
   */
  private static class BeamDefinitionWrapper {

    @JsonProperty
    private List<BeamDefinition> wrapped;

    @JsonCreator
    private BeamDefinitionWrapper(
        @JsonProperty("wrapped") List<BeamDefinition> wrapped) {
      this.wrapped = Objects.requireNonNull(wrapped);
    }
  }
}
