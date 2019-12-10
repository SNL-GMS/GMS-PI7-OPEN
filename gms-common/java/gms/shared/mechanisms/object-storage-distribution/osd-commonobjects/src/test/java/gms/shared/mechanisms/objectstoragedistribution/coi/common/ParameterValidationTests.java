package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import java.util.function.Predicate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link ParameterValidation} utility operations.
 */
public class ParameterValidationTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testRequireTrue() {
    ParameterValidation.requireTrue(Predicate.isEqual("test"), "test", "valid test");
    ParameterValidation.requireTrue(String::equals, "test", "test", "valid test");
  }

  @Test
  public void testRequireTruePredicateExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("invalid test");

    ParameterValidation.requireTrue(Predicate.isEqual("test"), "not test", "invalid test");
  }

  @Test
  public void testRequireTrueBiPredicateExpectIllegalArgumentException() {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("invalid test");

    ParameterValidation.requireTrue(String::equals, "test", "not test", "invalid test");
  }
}
