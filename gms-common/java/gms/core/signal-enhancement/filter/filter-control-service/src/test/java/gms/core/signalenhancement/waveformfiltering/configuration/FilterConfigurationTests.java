package gms.core.signalenhancement.waveformfiltering.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.configuration.ConfigurationRepository;
import gms.shared.mechanisms.configuration.FileConfigurationRepository;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FilterConfigurationTests {

  private FilterConfiguration filterConfiguration;

  @BeforeEach
  void setUp() throws URISyntaxException {
    URL testUrl = Thread.currentThread().getContextClassLoader()
        .getResource("gms/core/signalenhancement/waveformfiltering/configuration-base/");
    Path testPath = Paths.get(testUrl.toURI());

    ConfigurationRepository configurationRepository = FileConfigurationRepository.create(testPath);
    filterConfiguration = FilterConfiguration.create(configurationRepository);
  }

  @Test
  void testGetFilterParametersResolvesSampleRate() {
    assertDoesNotThrow(() -> filterConfiguration.getFilterParameters(20.0));
  }

  @Test
  void testGetFilterParametersDoesNotResolveSampleRate() {
    assertThrows(IllegalStateException.class,
        () -> filterConfiguration.getFilterParameters(999.0));
  }
}
