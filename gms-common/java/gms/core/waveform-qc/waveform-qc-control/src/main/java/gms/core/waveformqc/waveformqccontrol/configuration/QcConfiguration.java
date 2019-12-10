package gms.core.waveformqc.waveformqccontrol.configuration;

import gms.core.waveformqc.plugin.WaveformQcPlugin;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.ConfigurationTransform;
import gms.shared.mechanisms.configuration.client.ConfigurationConsumerUtility;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class QcConfiguration {

  private static final String SEPARATOR = ".";
  public static final String QC_PREFIX = "waveform-qc-control" + SEPARATOR;
  private static final UnaryOperator<String> KEY_BUILDER = s -> QC_PREFIX + s;

  public static final String DEFAULT_PLUGIN_CONFIGURATION = KEY_BUILDER
      .apply("default-plugin-configuration");

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  private QcConfiguration(ConfigurationConsumerUtility configurationConsumerUtility) {
    this.configurationConsumerUtility = configurationConsumerUtility;
  }

  /**
   * Obtain a new {@link QcConfiguration} using the provided {@link ConfigurationRepository} to
   * provide QC configuration.
   *
   * @param configurationRepository {@link ConfigurationRepository}, not null
   * @return {@link QcConfiguration}, not null
   * @throws NullPointerException if configurationConsumerUtility is null
   */
  public static QcConfiguration create(ConfigurationRepository configurationRepository) {
    Objects.requireNonNull(configurationRepository,
        "QcConfiguration cannot be created with null ConfigurationRepository");

    // Construct a ConfigurationConsumerUtility with the provided configurationRepository and
    // the necessary ConfigurationTransforms
    final ConfigurationConsumerUtility configurationConsumerUtility = ConfigurationConsumerUtility
        .builder(configurationRepository)
        .configurationNamePrefixes(List.of(KEY_BUILDER.apply("")))
        .transforms(getConfigurationTransforms())
        .build();

    return new QcConfiguration(configurationConsumerUtility);
  }

  private static Map<String, ConfigurationTransform<?, ?>> getConfigurationTransforms() {
    return Collections.emptyMap();
  }

  /**
   * Obtain the list of {@link QcParameters}s for the {@link WaveformQcPlugin}s to use by default
   *
   * @return List of {@link QcParameters}, not null
   */
  public List<QcParameters> getPluginConfigurations() {

    return configurationConsumerUtility.resolve(
        DEFAULT_PLUGIN_CONFIGURATION,
        Collections.emptyList(),
        QcParametersFile.class).getQcParameters();
  }
}
