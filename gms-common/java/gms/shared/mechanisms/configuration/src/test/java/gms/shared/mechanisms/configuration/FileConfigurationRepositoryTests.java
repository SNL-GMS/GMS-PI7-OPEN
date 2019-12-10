package gms.shared.mechanisms.configuration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class FileConfigurationRepositoryTests {

  @Test
  void testCreate() {

    final String configDir = "gms/shared/mechanisms/configuration/configuration-base";
    final String componentAPrefix = "componentA-base.";
    final String componentBPrefix = "componentB-base.";
    final Path basePath = pathFromString(configDir);

    final FileConfigurationRepository repository = FileConfigurationRepository.create(basePath);

    assertAll(
        () -> assertNotNull(repository),

        // componentA configuration loaded with correct name
        () -> assertEquals(1, repository.getKeyRange(componentAPrefix).size()),
        () -> assertEquals(componentAPrefix + "config",
            repository.getKeyRange(componentAPrefix).stream().findFirst()
                .map(Configuration::getName).orElse("FAIL TEST")),

        // componentB configuration loaded with correct name
        () -> assertEquals(1, repository.getKeyRange(componentBPrefix).size()),
        () -> assertEquals(componentBPrefix + "configB",
            repository.getKeyRange(componentBPrefix).stream().findFirst()
                .map(Configuration::getName).orElse("FAIL TEST"))
    );
  }

  @Test
  void testCreateValidatesParameters() {
    assertEquals("configurationRoot can't be null",
        assertThrows(NullPointerException.class, () -> FileConfigurationRepository.create(null))
            .getMessage());
  }

  @Test
  void testCreateEmptyDirectory() {
    final String configDir = "gms/shared/mechanisms/configuration/configuration-base-empty";
    assertNotNull(FileConfigurationRepository.create(pathFromString(configDir)));
  }

  private Path pathFromString(String string) {
    return new File(Objects
        .requireNonNull(getClass().getClassLoader().getResource(string))
        .getFile()).toPath();
  }
}
