package gms.shared.frameworks.control;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.frameworks.systemconfig.FileSystemConfigRepository;
import gms.shared.mechanisms.configuration.ConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ControlContextTests {

  private static final String CONTROL_NAME = "control-name";

  private static final String mockSystemConfigFilename =
      ControlContextTests.class
          .getClassLoader()
          .getResource("system-configuration.properties")
          .getPath();

  @Spy
  final ControlContextDefaultsFactory mockDefaultsFactory =
      new ControlContextDefaultsFactory() {
        @Override
        public SystemConfig createSystemConfig(String applicationName) {
          final FileSystemConfigRepository repository =
              FileSystemConfigRepository.builder().setFilename(mockSystemConfigFilename).build();
          return SystemConfig.create(applicationName, repository);
        }
      };

  @Test
  void testBuilder() {

    final ControlContext.Builder builder =
        ControlContext.builder(CONTROL_NAME, mockDefaultsFactory);
    assertNotNull(builder);

    final ControlContext controlContext = builder.build();

    assertAll(
        () -> assertNotNull(controlContext),
        () -> assertNotNull(controlContext.getSystemConfig()),
        () -> assertNotNull(controlContext.getProcessingConfigurationRepository()),
        () -> assertNotNull(controlContext.getPluginRegistry()));
  }

  @Test
  void testBuilderWithDefaultsFactoryOverride() {
    final ControlContext.Builder builder =
        ControlContext.builder(CONTROL_NAME, mockDefaultsFactory);

    assertNotNull(builder);

    builder.build();
    verify(mockDefaultsFactory, times(1)).createSystemConfig(Mockito.anyString());
    verify(mockDefaultsFactory, times(1)).createProcessingConfigurationRepository(Mockito.any());
    verify(mockDefaultsFactory, times(1)).createPluginRegistry();
  }

  @Test
  void testBuilderUsesProvidedControlName() {
    ControlContext.builder(CONTROL_NAME, mockDefaultsFactory).build();
    verify(mockDefaultsFactory, times(1)).createSystemConfig(CONTROL_NAME);
  }

  @Test
  void testBuilderValidatesParameters() {
    assertEquals(
        "ControlContext.Builder requires non-null controlName",
        assertThrows(NullPointerException.class, () -> ControlContext.builder(null)).getMessage());

    assertEquals(
        "ControlContext.Builder requires non-null controlContextDefaultsFactory",
        assertThrows(NullPointerException.class, () -> ControlContext.builder("", null))
            .getMessage());

    assertEquals(
        "ControlContext.Builder requires non-null controlName",
        assertThrows(
                NullPointerException.class,
                () -> ControlContext.builder(null, mock(ControlContextDefaultsFactory.class)))
            .getMessage());
  }

  @Test
  void testBuilderWithSystemConfig() {

    final SystemConfig systemConfig =
        SystemConfig.create(
            "different-" + CONTROL_NAME,
            FileSystemConfigRepository.builder().setFilename(mockSystemConfigFilename).build());

    final ControlContext controlContext =
        ControlContext.builder(CONTROL_NAME, mockDefaultsFactory)
            .systemConfig(systemConfig)
            .build();

    final SystemConfig defaultSystemConfig =
        ControlContext.builder(CONTROL_NAME, mockDefaultsFactory).build().getSystemConfig();

    assertAll(
        () -> assertEquals(systemConfig, controlContext.getSystemConfig()),
        () ->
            assertNotEquals(
                defaultSystemConfig.getProcessingConfigurationRoot(),
                controlContext.getSystemConfig().getProcessingConfigurationRoot()));
  }

  @Test
  void testBuilderWithConfigurationRepository() {
    final ConfigurationRepository configurationRepository = new ConfigurationRepository() {};

    final ControlContext controlContext =
        ControlContext.builder(CONTROL_NAME)
            .processingConfigurationRepository(configurationRepository)
            .build();

    final ConfigurationRepository defaultConfigurationRepository =
        ControlContext.builder(CONTROL_NAME, mockDefaultsFactory)
            .build()
            .getProcessingConfigurationRepository();

    assertAll(
        () ->
            assertEquals(
                configurationRepository, controlContext.getProcessingConfigurationRepository()),
        () ->
            assertNotEquals(
                defaultConfigurationRepository.getClass(),
                controlContext.getProcessingConfigurationRepository().getClass()));
  }

  @Test
  void testBuilderCreatesConfigurationRepositoryWithSystemConfiguration() {
    final String systemConfigName = "different-" + CONTROL_NAME;
    final SystemConfig systemConfigOverride =
        SystemConfig.create(
            systemConfigName,
            FileSystemConfigRepository.builder().setFilename(mockSystemConfigFilename).build());

    ControlContext.builder(CONTROL_NAME, mockDefaultsFactory)
        .systemConfig(systemConfigOverride)
        .build();

    verify(mockDefaultsFactory).createProcessingConfigurationRepository(systemConfigOverride);
  }
}
