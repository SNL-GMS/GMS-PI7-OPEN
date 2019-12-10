package gms.shared.frameworks.control;

import com.google.auto.value.AutoValue;
import gms.shared.frameworks.pluginregistry.PluginRegistry;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents initialized versions of the common external dependencies (e.g. PluginRegistry,
 * configuration, logging, etc.) required by a GMS Control. Created by a {@link Builder} which
 * provides default dependency instantiations for any dependencies the client leaves unset.
 */
@AutoValue
public abstract class ControlContext {

  /**
   * Default {@link ControlContextDefaultsFactory} used by a ControlContext when the factory is not
   * overridden in {@link ControlContext#builder(String, ControlContextDefaultsFactory)}
   */
  private static final ControlContextDefaultsFactory controlContextDefaultsFactory =
      new ControlContextDefaultsFactory() {};

  /**
   * Obtain the {@link SystemConfig} from this {@link ControlContext}
   *
   * @return {@link SystemConfig}, not null
   */
  public abstract SystemConfig getSystemConfig();

  /**
   * Obtain the {@link ConfigurationRepository} from this {@link ControlContext}
   *
   * @return {@link ConfigurationRepository}, not null
   */
  public abstract ConfigurationRepository getProcessingConfigurationRepository();

  /**
   * Obtain the {@link PluginRegistry} from this {@link ControlContext}
   *
   * @return {@link PluginRegistry}, not null
   */
  public abstract PluginRegistry getPluginRegistry();

  /**
   * Obtain an {@link ControlContext} {@link Builder} for a control with the provided name
   *
   * @param controlName control name, not null
   * @return {@link Builder}, not null
   * @throws NullPointerException if controlName is null
   */
  public static Builder builder(String controlName) {
    return builder(controlName, controlContextDefaultsFactory);
  }

  /**
   * Obtain an {@link ControlContext} {@link Builder} for a control with the provided name and
   * {@link ControlContextDefaultsFactory}
   *
   * @param controlName control name, not null
   * @param controlContextDefaultsFactory {@link ControlContextDefaultsFactory} providing control
   *     framework implementations when not overridden by client, not null
   * @return {@link Builder}, not null
   * @throws NullPointerException if controlName or controlContextDefaultsFactory are null
   */
  public static Builder builder(
      String controlName, ControlContextDefaultsFactory controlContextDefaultsFactory) {

    Objects.requireNonNull(controlName, "ControlContext.Builder requires non-null controlName");
    Objects.requireNonNull(
        controlContextDefaultsFactory,
        "ControlContext.Builder requires non-null controlContextDefaultsFactory");

    return new AutoValue_ControlContext.Builder()
        .setControlContextDefaultsFactory(controlContextDefaultsFactory)
        .systemConfig(controlContextDefaultsFactory.createSystemConfig(controlName))
        .pluginRegistry(controlContextDefaultsFactory.createPluginRegistry());
  }

  /**
   * Constructs a {@link ControlContext} from provided values or defaults from a {@link
   * ControlContextDefaultsFactory} If necessary, a framework provided to this builder will be used
   * to construct another framework (e.g. as in {@link
   * ControlContextDefaultsFactory#createProcessingConfigurationRepository(SystemConfig)} where
   * {@link SystemConfig} is used to construct the processing {@link ConfigurationRepository}). Any
   * framework provided to this builder is used as provided without alteration (e.g. if a processing
   * {@link ConfigurationRepository} framework is provided it will be used as provided without
   * attempting to inject the {@link SystemConfig} used in this builder).
   */
  @AutoValue.Builder
  public abstract static class Builder {

    private ControlContextDefaultsFactory defaultsFactory;

    /**
     * Set the {@link ControlContextDefaultsFactory} used in this Builder. package-private
     * visibility since clients provide the controlContextDefaultsFactory via {@link
     * ControlContext#builder(String, ControlContextDefaultsFactory)}
     *
     * @param controlContextDefaultsFactory {@link ControlContextDefaultsFactory}, not null
     * @return this {@link Builder}
     */
    Builder setControlContextDefaultsFactory(
        ControlContextDefaultsFactory controlContextDefaultsFactory) {
      this.defaultsFactory = controlContextDefaultsFactory;
      return this;
    }

    /**
     * Set the {@link SystemConfig} to use in the built {@link ControlContext}
     *
     * @param systemConfig {@link SystemConfig}, not null
     * @return this {@link Builder}
     */
    public abstract Builder systemConfig(SystemConfig systemConfig);

    /**
     * Obtains the {@link SystemConfig} set in this builder if one has been set. Not Optional since
     * a SystemConfig is set when the builder is instantiated.
     *
     * @return {@link SystemConfig} set in this Builder
     */
    abstract SystemConfig getSystemConfig();

    /**
     * Set the {@link PluginRegistry} to use in the built {@link ControlContext}
     *
     * <p>AutoValue requires this operation to be in the builder, but it is package-private since
     * there is currently only one way to instantiate a {@link PluginRegistry} so a client can't
     * possibly override the default.
     *
     * @param pluginRegistry {@link PluginRegistry}, not null
     * @return this {@link Builder}
     */
    abstract Builder pluginRegistry(PluginRegistry pluginRegistry);

    /**
     * Set the {@link ConfigurationRepository} to use in the built {@link ControlContext}.
     *
     * @param processingConfigurationRepository {@link ConfigurationRepository}, not null
     * @return this {@link Builder}
     */
    public abstract Builder processingConfigurationRepository(
        ConfigurationRepository processingConfigurationRepository);

    /**
     * Obtains the {@link ConfigurationRepository} set in this builder if one has been set.
     *
     * @return {@link Optional} containing the {@link ConfigurationRepository} set in this Builder
     */
    abstract Optional<ConfigurationRepository> getProcessingConfigurationRepository();

    /**
     * AutoValue generated builder. Called by {@link Builder#build()}.
     *
     * @return {@link ControlContext}, not null
     */
    abstract ControlContext autoBuild();

    /**
     * Obtain the {@link ControlContext} defined by this {@link Builder}. Uses defaults for any
     * ControlContext properties not explicitly set in this Builder.
     *
     * @return {@link ControlContext}, not null
     */
    public ControlContext build() {

      // Build with default ConfigurationRepository, if necessary. Construct with the
      // SystemConfig set in this builder
      if (!getProcessingConfigurationRepository().isPresent()) {
        processingConfigurationRepository(
            defaultsFactory.createProcessingConfigurationRepository(getSystemConfig()));
      }

      return autoBuild();
    }
  }
}
