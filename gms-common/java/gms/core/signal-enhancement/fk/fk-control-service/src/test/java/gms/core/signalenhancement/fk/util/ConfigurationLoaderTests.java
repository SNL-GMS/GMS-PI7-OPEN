package gms.core.signalenhancement.fk.util;

public class ConfigurationLoaderTests {

  // TODO: Run with proper configuration file

  /*@Rule
  public final ExpectedException exception = ExpectedException.none();

  private URL propertiesUrl;

  @Before
  public void setUp() {
    final String propertyFile = "gms/core/signaldetection/signaldetectorcontrol/util/application.properties";
    propertiesUrl = Thread.currentThread().getContextClassLoader().getResource(propertyFile);
    assertNotNull("Test could not load properties file " + propertyFile, propertiesUrl);
  }

  @Test
  public void testLoadConfiguration() {
    CompositeConfiguration configuration = ConfigurationLoader.load(propertiesUrl);

    assertNotNull(configuration);
    assertTrue(configuration.getNumberOfConfigurations() >= 3);
    assertTrue(EnvironmentConfiguration.class.isInstance(configuration.getConfiguration(0)));
    assertTrue(SystemConfiguration.class.isInstance(configuration.getConfiguration(1)));
    assertTrue(FileBasedConfiguration.class.isInstance(configuration.getConfiguration(2)));

    assertEquals(1000, configuration.getInt("prop_int"));
    assertEquals("a string property", configuration.getString("prop_string"));
  }

  @Test
  public void testLoadNullFileExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("ConfigurationLoader requires non-null propertiesFileUrl");
    ConfigurationLoader.load(null);
  }*/
}
