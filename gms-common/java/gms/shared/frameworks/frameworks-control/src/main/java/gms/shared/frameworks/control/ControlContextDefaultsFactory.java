package gms.shared.frameworks.control;

import gms.shared.frameworks.pluginregistry.PluginRegistry;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.FileConfigurationRepository;

import java.nio.file.Path;
import java.util.MissingResourceException;
import java.util.Objects;

/**
 * Factory operations creating default instances of the dependencies provided by {@link
 * ControlContext}.
 */
interface ControlContextDefaultsFactory {

  /**
   * Create a default {@link SystemConfig} for an application with the provided name
   *
   * @param applicationName String containing the application name, not null
   * @return {@link SystemConfig}, not null
   */
  default SystemConfig createSystemConfig(String applicationName) {
    return SystemConfig.create(applicationName);
  }

  /**
   * Create a default {@link ConfigurationRepository} for an application. Use the provided {@link
   * SystemConfig} to initialize the ProcessingConfigurationRepository.
   *
   * @param systemConfig {@link SystemConfig} providing system configuration necessary
   *     to create the ProcessingConfigurationRepository, not null
   * @return {@link ConfigurationRepository}, not null
   * @throws NullPointerException if systemConfigurationClient is null
   * @throws IllegalStateException if processing-configuration-root is not defined
   */
  default ConfigurationRepository createProcessingConfigurationRepository(
      SystemConfig systemConfig) {

    Objects.requireNonNull(
        systemConfig,
        "Processing ConfigurationRepository cannot be created with a null SystemConfig");

    Path processingConfigurationRoot;
    try {
      processingConfigurationRoot = systemConfig.getProcessingConfigurationRoot();
    } catch (MissingResourceException e) {
      throw new IllegalStateException(
          "processing-configuration-root not defined for control " + systemConfig.getControlName(), e);
    }

    return FileConfigurationRepository.create(processingConfigurationRoot);
  }

  /**
   * Create a default {@link PluginRegistry} for an application.
   *
   * @return {@link PluginRegistry}, not null
   */
  default PluginRegistry createPluginRegistry() {
    return PluginRegistry.create();
  }
}
