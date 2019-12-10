package gms.core.signaldetection.association.control.service;

import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.client.ConfigurationConsumerUtility;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * This is the Utility Configuration Object for the Signal Detection Associator Class.
 */
public class SignalDetectionAssociationControlConfiguration {

  static final String SEPARATOR = ".";
  static final String SIGNAL_DETECTION_ASSOCIATION_PLUGIN_PREFIX = "signal-detection-association-control" + SEPARATOR;
  static final String SIGNAL_DETECTION_ASSOCIATION_QUALITY_METRIC_PREFIX = SIGNAL_DETECTION_ASSOCIATION_PLUGIN_PREFIX +
      "quality-metrics" + SEPARATOR;
  public static final String GMS_SIGNALDETECTION_ASSOCIATION_EARTH_MODEL_DIR = "gms/core/signaldetection/association/control/service/tesseract-models";
  private static UnaryOperator<String> buildKey = s -> SIGNAL_DETECTION_ASSOCIATION_PLUGIN_PREFIX + s;
  private static UnaryOperator<String> buildQualityMetricKey = s -> SIGNAL_DETECTION_ASSOCIATION_QUALITY_METRIC_PREFIX + s;
  private ConfigurationConsumerUtility configurationConsumerUtility;


  static final String SIGNAL_DETECTION_DEFAULT_PLUGIN_PREFIX = buildKey.apply("default");
  static final String SIGNAL_DETECTION_ARRIVAL_QUALITY_DEFAULT_PLUGIN_PREFIX = buildQualityMetricKey.apply("arrival-quality");
  static final String SIGNAL_DETECTION_WEIGHTED_EVENT_DEFAULT_PLUGIN_PREFIX = buildQualityMetricKey.apply("weighted-event");

  private SignalDetectionAssociationControlConfiguration(ConfigurationConsumerUtility consumerUtility) {
    this.configurationConsumerUtility = consumerUtility;
  }

  /**
   *
   * Factory method for generating SignalDetectionAssociatorConfiguration Objects
   *
   * @param configurationRepository - Repository object to get composite configurations from.
   * @return new {@link SignalDetectionAssociationControlConfiguration} object
   * @throws NullPointerException if configurationRepository is null
   */
  public static SignalDetectionAssociationControlConfiguration create(
      ConfigurationRepository configurationRepository) {
    Objects.requireNonNull(configurationRepository,
        "SignalDetectionAssociatorConfiguration can not be instantiated with a null ConfigurationRepository object.");

    final ConfigurationConsumerUtility configurationConsumerUtility = ConfigurationConsumerUtility
        .builder(configurationRepository)
        .configurationNamePrefixes(List.of(buildKey.apply("")))
        .build();

    return new SignalDetectionAssociationControlConfiguration(configurationConsumerUtility);
  }


  /**
   * Provides the appropriate file path to load into the Associator
   * @return actual file path to the model file.
   */
  public Path getGridModelFilePath() {
    final String basePath = Thread.currentThread().getContextClassLoader().getResource(
        GMS_SIGNALDETECTION_ASSOCIATION_EARTH_MODEL_DIR).getPath();
    SignalDetectionAssociatorConfigurationParameters params =  configurationConsumerUtility.resolve(
        SIGNAL_DETECTION_DEFAULT_PLUGIN_PREFIX, List.of(), SignalDetectionAssociatorConfigurationParameters.class);
    final String fileName = basePath + "/" + params.getGridModelFileName();
    return new File(fileName).toPath();
  }

  /**
   * Return the SignalDetectionAssociatorConfigurationParameters object from the default plugin
   *
   * @return the SignalDetectionAssociatorConfigurationParameters object retrieved from the configuration utility.
   */
  public SignalDetectionAssociatorConfigurationParameters getParams() {
    return configurationConsumerUtility.resolve(
        SIGNAL_DETECTION_DEFAULT_PLUGIN_PREFIX, List.of(), SignalDetectionAssociatorConfigurationParameters.class);
  }

  public WeightedEventCriteriaConfigurationParameters getWeightedEventPluginParams() {
    return configurationConsumerUtility.resolve(
        SIGNAL_DETECTION_WEIGHTED_EVENT_DEFAULT_PLUGIN_PREFIX,
        List.of(),
        WeightedEventCriteriaConfigurationParameters.class
    );
  }

  public ArrivalQualityCriteriaConfigurationParameters getArrivalQualityPluginParams() {
    return configurationConsumerUtility.resolve(
        SIGNAL_DETECTION_ARRIVAL_QUALITY_DEFAULT_PLUGIN_PREFIX,
        List.of(),
        ArrivalQualityCriteriaConfigurationParameters.class
    );
  }
}
