package gms.shared.frameworks.systemconfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.frameworks.utilities.ServerConfig;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SystemConfigTests {

  private static String testConfigDir =
      SystemConfigTests.class.getClassLoader().getResource("test-config/").getPath();

  private static String basicConfigFilename = testConfigDir + "basic.properties";
  private static String controlConfigFilename = testConfigDir + "control.properties";
  private static String overrideConfigFilename = testConfigDir + "override.properties";

  private static SystemConfig basicConfig;

  @BeforeAll
  static void testSetup() {
    basicConfig = createConfig("control-name", basicConfigFilename);
  }

  @Test
  void testGetValue() {
    assertEquals("jabberwocky", basicConfig.getValue("string-parameter1"));
    assertEquals("twas brillig and the slithy toves", basicConfig.getValue("string-parameter2"));
    assertEquals("42", basicConfig.getValue("integer-parameter1"));
  }

  @Test
  void testMissingValue() {
    assertThrows(MissingResourceException.class, () -> basicConfig.getValue("string-parameter3"));
  }

  @Test
  void testGetValueAsInt() {
    assertEquals(42, basicConfig.getValueAsInt("integer-parameter1"));
    assertEquals(-19, basicConfig.getValueAsInt("integer-parameter2"));
  }

  @Test()
  void testGetNonIntValueAsInt() {
    assertThrows(NumberFormatException.class, () -> basicConfig.getValueAsInt("string-parameter1"));
  }

  @Test()
  void testGetLongValueAsInt() {
    assertThrows(NumberFormatException.class, () -> basicConfig.getValueAsInt("long-parameter"));
  }

  @Test
  void testGetValueAsLong() {
    assertEquals(2147483648L, basicConfig.getValueAsLong("long-parameter"));
  }

  @Test()
  void testGetNonLongValueAsLong() {
    assertThrows(
        NumberFormatException.class, () -> basicConfig.getValueAsLong("string-parameter1"));
  }

  @Test
  void testGetValueAsDouble() {
    assertEquals(3.1415, basicConfig.getValueAsDouble("double-parameter"));
  }

  @Test()
  void testGetNonDoubleValueAsDouble() {
    assertThrows(
        NumberFormatException.class, () -> basicConfig.getValueAsDouble("string-parameter1"));
  }

  @Test
  void testGetValueAsBoolean() {
    assertTrue(basicConfig.getValueAsBoolean("boolean-true-parameter1"));
    assertTrue(basicConfig.getValueAsBoolean("boolean-true-parameter2"));
    assertTrue(basicConfig.getValueAsBoolean("boolean-true-parameter3"));
    assertTrue(basicConfig.getValueAsBoolean("boolean-true-parameter4"));
    assertTrue(basicConfig.getValueAsBoolean("boolean-true-parameter5"));

    assertFalse(basicConfig.getValueAsBoolean("boolean-false-parameter1"));
    assertFalse(basicConfig.getValueAsBoolean("boolean-false-parameter2"));
    assertFalse(basicConfig.getValueAsBoolean("boolean-false-parameter3"));
    assertFalse(basicConfig.getValueAsBoolean("boolean-false-parameter4"));
    assertFalse(basicConfig.getValueAsBoolean("boolean-false-parameter5"));
  }

  @Test()
  void testGetNonBooleanValueAsDouble() {
    assertThrows(
        IllegalArgumentException.class, () -> basicConfig.getValueAsBoolean("string-parameter1"));
  }

  @Test
  void testGetValueAsDuration() {
    assertEquals(Duration.ofSeconds(90), basicConfig.getValueAsDuration("duration-parameter"));
  }

  @Test
  void testGetUrlOfComponent() {
    final SystemConfig config = createConfig("control-2", controlConfigFilename);
    final URL url = config.getUrlOfComponent("control3");
    assertNotNull(url);
    assertEquals(config.getValue(
        SystemConfig.createKey("control3", SystemConfig.HOST)), url.getHost());
    assertEquals(config.getValueAsInt(
        SystemConfig.createKey("control3", SystemConfig.PORT)), url.getPort());
    assertEquals("", url.getPath());
  }

  @Test
  void testGetUrl() {
    final String controlName = "control3";
    final SystemConfig config = createConfig(controlName, controlConfigFilename);
    final URL url = config.getUrl();
    assertNotNull(url);
    assertEquals(config.getValue(
        SystemConfig.createKey(controlName, SystemConfig.HOST)), url.getHost());
    assertEquals(config.getValueAsInt(
        SystemConfig.createKey(controlName, SystemConfig.PORT)), url.getPort());
    assertEquals("", url.getPath());
  }

  @Test
  void testGetServerConfig() {
    final String controlName = "control1";
    final SystemConfig config = createConfig(controlName, controlConfigFilename);
    final ServerConfig serverConfig = config.getServerConfig();
    assertNotNull(serverConfig);
    final BiConsumer<String, Integer> checker = (key, expectedVal) ->
    assertEquals(config.getValueAsInt(SystemConfig.createKey(controlName, key)),
        (int) expectedVal);
    checker.accept(SystemConfig.PORT, serverConfig.getPort());
    checker.accept(SystemConfig.MIN_THREADS, serverConfig.getMinThreadPoolSize());
    checker.accept(SystemConfig.MAX_THREADS, serverConfig.getMaxThreadPoolSize());
    assertEquals(config.getValueAsDuration(
        SystemConfig.createKey(controlName, SystemConfig.IDLE_TIMEOUT)),
        serverConfig.getThreadIdleTimeout());
  }

  @Test()
  void testControlOverride() {
    final SystemConfig config1 = createConfig("control1", controlConfigFilename);
    final SystemConfig config2 = createConfig("control2", controlConfigFilename);

    assertEquals("control1", config1.getValue("host"));
    assertEquals(8080, config1.getValueAsInt("port"));
    assertEquals("control2", config1.getValue("control2.host"));
    assertEquals(80, config1.getValueAsInt("control2.port"));

    assertEquals("control2", config2.getValue("host"));
    assertEquals(80, config2.getValueAsInt("port"));
    assertEquals("control1", config2.getValue("control1.host"));
    assertEquals(8080, config2.getValueAsInt("control1.port"));
  }

  @Test()
  void testFileOverride() {
    final SystemConfig config1 = createConfig(
        "control1", overrideConfigFilename, controlConfigFilename);
    final SystemConfig config2 = createConfig(
        "control2", overrideConfigFilename, controlConfigFilename);

    assertEquals("control1", config1.getValue("host"));
    assertEquals(9000, config1.getValueAsInt("port"));
    assertEquals("test-control2", config1.getValue("control2.host"));
    assertEquals(9000, config1.getValueAsInt("control2.port"));

    assertEquals("test-control2", config2.getValue("host"));
    assertEquals(9000, config2.getValueAsInt("port"));
    assertEquals("control1", config2.getValue("control1.host"));
    assertEquals(9000, config2.getValueAsInt("control1.port"));
  }

  private static SystemConfig createConfig(String controlName, String... fileNames) {
    final List<SystemConfigRepository> repos = Arrays.stream(fileNames)
        .map(SystemConfigTests::configRepository)
        .collect(Collectors.toList());
    return SystemConfig.create(controlName, repos);
  }

  private static FileSystemConfigRepository configRepository(String fileName) {
    return FileSystemConfigRepository.builder().setFilename(fileName).build();
  }
}
