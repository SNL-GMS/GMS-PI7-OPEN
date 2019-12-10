package gms.core.signalenhancement.fk.control.configuration;

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

public class FkConfiguration {

  private static final String SEPARATOR = ".";
  private static final String FK_PREFIX = "fk-control" + SEPARATOR;
  private static final UnaryOperator<String> KEY_BUILDER = s -> FK_PREFIX + s;

  private static final String STATION_ID_CRITERION = "station-id";

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  private FkConfiguration(ConfigurationConsumerUtility configurationConsumerUtility) {
    this.configurationConsumerUtility = configurationConsumerUtility;
  }

  public static FkConfiguration create(ConfigurationRepository configurationRepository) {
    Objects.requireNonNull(configurationRepository,
        "FkConfiguration cannot be created with null ConfigurationRepository");

    ConfigurationConsumerUtility configurationConsumerUtility = ConfigurationConsumerUtility
        .builder(configurationRepository)
        .configurationNamePrefixes(List.of(KEY_BUILDER.apply("")))
        .transforms(getConfigurationTransforms())
        .build();

    return new FkConfiguration(configurationConsumerUtility);
  }

  private static Map<String, ConfigurationTransform<?, ?>> getConfigurationTransforms() {
    return Collections.emptyMap();
  }

  public FkSpectraParameters getFkSpectraParameters(UUID stationId) {
    return configurationConsumerUtility
        .resolve(KEY_BUILDER.apply("fk-spectra-parameters"),
            List.of(Selector.from(STATION_ID_CRITERION, stationId.toString())),
            FkSpectraParameters.class);
  }

  public List<FkAttributesParameters> getFkAttributesParameters(UUID stationId) {
    return configurationConsumerUtility
        .resolve(KEY_BUILDER.apply("fk-attributes-parameters"),
            List.of(Selector.from(STATION_ID_CRITERION, stationId.toString())),
            FkAttributesParametersFile.class)
        .getFkAttributesParameters();
  }

}
