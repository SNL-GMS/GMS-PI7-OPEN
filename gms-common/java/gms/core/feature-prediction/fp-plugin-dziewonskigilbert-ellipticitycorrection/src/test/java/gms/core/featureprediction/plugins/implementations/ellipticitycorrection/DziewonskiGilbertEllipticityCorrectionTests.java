package gms.core.featureprediction.plugins.implementations.ellipticitycorrection;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * This test class only contains tests on code in {@link StandardAsciiDgEllipticityCorrectionPlugin} that can't easily
 * be covered by {@link DziewonskiGilbertEllipticityCorrectionsTests}, such as null-checking and parameter
 * validation.
 */
public class DziewonskiGilbertEllipticityCorrectionTests {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  //TODO: Why is this ignored
  @Ignore
  @Test
  public void testFromNullUrlThrowsNullPointerException() throws NullPointerException {
    exception.expect(NullPointerException.class);
    exception.expectMessage("StandardAsciiDgEllipticityCorrectionPlugin::from() requires non-null url parameter");
  }
}
