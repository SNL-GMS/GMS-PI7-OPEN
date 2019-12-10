package gms.core.signalenhancement.waveformfiltering.configuration;

import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.ConfigurationTransform;
import gms.shared.mechanisms.configuration.Selector;
import gms.shared.mechanisms.configuration.client.ConfigurationConsumerUtility;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class FilterConfiguration {

  private static final String SEPARATOR = ".";
  public static final String FILTER_PREFIX = "filter-control" + SEPARATOR;
  private static final UnaryOperator<String> KEY_BUILDER = s -> FILTER_PREFIX + s;

  public static final String SAMPLE_RATE_CRITERION = "sampleRate";

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  private FilterConfiguration(ConfigurationConsumerUtility configurationConsumerUtility) {
    this.configurationConsumerUtility = configurationConsumerUtility;
  }

  /**
   * Obtain a new {@link FilterConfiguration} using the provided {@link ConfigurationRepository} to
   * provide QC configuration.
   *
   * @param configurationRepository {@link ConfigurationRepository}, not null
   * @return {@link FilterConfiguration}, not null
   * @throws NullPointerException if configurationConsumerUtility is null
   */
  public static FilterConfiguration create(ConfigurationRepository configurationRepository) {
    Objects.requireNonNull(configurationRepository,
        "FilterConfiguration cannot be created with null ConfigurationRepository");

    // Construct a ConfigurationConsumerUtility with the provided configurationRepository and
    // the necessary ConfigurationTransforms
    final ConfigurationConsumerUtility configurationConsumerUtility = ConfigurationConsumerUtility
        .builder(configurationRepository)
        .configurationNamePrefixes(List.of(KEY_BUILDER.apply("")))
        .transforms(getConfigurationTransforms())
        .build();

    return new FilterConfiguration(configurationConsumerUtility);
  }

  public List<FilterParameters> getFilterParameters(double sampleRate) {
    return configurationConsumerUtility.resolve(KEY_BUILDER.apply("processing-parameters"),
        List.of(Selector.from(SAMPLE_RATE_CRITERION, sampleRate)), FilterParametersFile.class)
        .getFilterParameters();
  }

  private static Map<String, ConfigurationTransform<?, ?>> getConfigurationTransforms() {
    return Collections.emptyMap();
  }
}
