package gms.core.signalenhancement.beam.osd.client;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.notNull;

import java.net.URL;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class HttpClientConfigurationLoaderTests {

  private static final String basePropertiesPath = "gms/core/signalenhancement/beam/osd/client/";

  //convenience function for null assertions
  private static Function<Executable, Executable> assertThrowsNullPointer =
      e -> () -> assertThrows(NullPointerException.class, e);

  private URL propertiesUrl;

  @BeforeEach
  public void setUp() {
    final String filePath = basePropertiesPath + "osdClient.properties";
    propertiesUrl = Thread.currentThread().getContextClassLoader().getResource(filePath);
    assertNotNull(propertiesUrl);
  }

  @Test
  void testLoad() {
    HttpClientConfiguration signalDetectionsConfig = HttpClientConfigurationLoader
        .load("signalDetections_", propertiesUrl);
    assertNotNull(signalDetectionsConfig);
    assertEquals("osd-signaldetection-repository-service", signalDetectionsConfig.getHost());
    assertEquals(8080, signalDetectionsConfig.getPort());
    assertEquals("/coi",
        signalDetectionsConfig.getBasePath());
  }

  @Test
  void testLoadNullArguments() {

    Executable nullDescriptor = assertThrowsNullPointer
        .apply(() -> HttpClientConfigurationLoader.load(null, new URL("file:/test")));
    Executable nullParameters = assertThrowsNullPointer
        .apply(() -> HttpClientConfigurationLoader.load("test", null));

    assertAll("HttpClientConfigurationLoader load null arguments:",
        nullDescriptor, nullParameters);
  }
}
