package gms.shared.frameworks.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gms.shared.frameworks.pluginregistry.PluginRegistry;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ControlContextDefaultsFactoryTests {

  private ControlContextDefaultsFactory defaultsFactory;

  @BeforeEach
  void setUp() {
    defaultsFactory = new ControlContextDefaultsFactory() {};
  }

  @Test
  void testCreateSystemConfig() {
    final String appName = "app-name";
    final SystemConfig systemConfigurationClient = defaultsFactory.createSystemConfig(appName);

    assertNotNull(systemConfigurationClient);
  }

  @Test
  void testCreateProcessingConfigurationRepository() throws URISyntaxException {
    final SystemConfig systemConfigurationClient = mock(SystemConfig.class);

    final String configDir = "gms/shared/frameworks/control/processing-configuration-root";
    final Path configPath =
        Paths.get(
            Objects.requireNonNull(
                    Thread.currentThread().getContextClassLoader().getResource(configDir))
                .toURI());

    when(systemConfigurationClient.getProcessingConfigurationRoot()).thenReturn(configPath);

    final ConfigurationRepository configurationRepository =
        defaultsFactory.createProcessingConfigurationRepository(systemConfigurationClient);

    assertNotNull(configurationRepository);

    verify(systemConfigurationClient, times(1)).getProcessingConfigurationRoot();
  }

  @Test
  void testCreateProcessingConfigurationRepositoryValidatesArguments() {
    assertEquals(
        "Processing ConfigurationRepository cannot be created with a null SystemConfig",
        assertThrows(
                NullPointerException.class,
                () -> defaultsFactory.createProcessingConfigurationRepository(null))
            .getMessage());
  }

  @Test
  void testCreatePluginRegistry() {
    final PluginRegistry pluginRegistry = defaultsFactory.createPluginRegistry();
    assertNotNull(pluginRegistry);
  }
}
