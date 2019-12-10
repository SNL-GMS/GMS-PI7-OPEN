package gms.core.signaldetection.signaldetectorcontrol.configuration;

import static java.util.Collections.emptyList;

import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.ConfigurationTransform;
import gms.shared.mechanisms.configuration.Selector;
import gms.shared.mechanisms.configuration.client.ConfigurationConsumerUtility;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class SignalDetectorConfiguration {

  private static final String SEPARATOR = ".";
  private static final String SIGNAL_DETECTION_PREFIX = "signal-detector-control" + SEPARATOR;
  private static final UnaryOperator<String> KEY_BUILDER = s -> SIGNAL_DETECTION_PREFIX + s;

  private static final String STATION_ID_CRITERION = "stationId";

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  private SignalDetectorConfiguration(ConfigurationConsumerUtility configurationConsumerUtility) {
    this.configurationConsumerUtility = configurationConsumerUtility;
  }

  public static SignalDetectorConfiguration create(
      ConfigurationRepository configurationRepository) {
    Objects.requireNonNull(configurationRepository,
        "FilterConfiguration cannot be created with null ConfigurationRepository");

    // Construct a ConfigurationConsumerUtility with the provided configurationRepository and
    // the necessary ConfigurationTransforms
    final ConfigurationConsumerUtility configurationConsumerUtility = ConfigurationConsumerUtility
        .builder(configurationRepository)
        .configurationNamePrefixes(List.of(KEY_BUILDER.apply("")))
        .transforms(getConfigurationTransforms())
        .build();

    return new SignalDetectorConfiguration(configurationConsumerUtility);
  }

  private static Map<String, ConfigurationTransform<?, ?>> getConfigurationTransforms() {
    return Collections.emptyMap();
  }

  public List<SignalDetectionParameters> getSignalDetectionParameters(UUID stationId) {
    return configurationConsumerUtility
        .resolve(KEY_BUILDER.apply("signal-detection-parameters"), List.of(
            Selector.from(STATION_ID_CRITERION, stationId)), SignalDetectionParametersFile.class)
        .getSignalDetectionParameters();
  }

  public OnsetTimeUncertaintyParameters getOnsetTimeUncertaintyParameters() {
    return configurationConsumerUtility
        .resolve(KEY_BUILDER.apply("onset-time-uncertainty-parameters"), emptyList(),
            OnsetTimeUncertaintyParameters.class);
  }

  public OnsetTimeRefinementParameters getOnsetTimeRefinementParameters() {
    return configurationConsumerUtility
        .resolve(KEY_BUILDER.apply("onset-time-refinement-parameters"), emptyList(),
            OnsetTimeRefinementParameters.class);
  }
}
