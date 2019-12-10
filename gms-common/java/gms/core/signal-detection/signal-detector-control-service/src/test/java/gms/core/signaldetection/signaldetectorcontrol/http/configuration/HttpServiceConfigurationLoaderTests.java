package gms.core.signaldetection.signaldetectorcontrol.http.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HttpServiceConfigurationLoaderTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private URL propertiesUrl;

  @Before
  public void setUp() {
    final String propertyFile = "gms/core/signaldetection/signaldetectorcontrol/http/application.properties";
    propertiesUrl = Thread.currentThread().getContextClassLoader().getResource(propertyFile);
    TestCase.assertNotNull("Test could not load properties file " + propertyFile, propertiesUrl);
  }

  @Test
  public void testLoadConfiguration() {
    HttpServiceConfiguration config = HttpServiceConfigurationLoader.load(propertiesUrl);

    assertNotNull(config);
    assertEquals("/signal-detection/signal-detector-control-test-base/", config.getBaseUrl());

    assertEquals(8081, config.getPort());
    assertEquals(1, config.getMinThreads());
    assertEquals(13, config.getMaxThreads());
    assertEquals(1000, config.getIdleTimeOutMillis());
  }

  @Test
  public void testLoadConfigurationNullUrlExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("HttpServiceConfigurationLoader requires non-null propertiesFileUrl");
    HttpServiceConfigurationLoader.load(null);
  }
}
