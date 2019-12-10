/*
  Unit tests for the ISO 8601 validator for the InvokeInterval processor
  being developed by Quokka.

  Reference: https://en.wikipedia.org/wiki/ISO_8601
 */
package gms.processors.invokeinterval;

import org.junit.Test;
import org.apache.nifi.components.ValidationContext;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import gms.processors.invokeinterval.ISO8601Validator;

public class ISO8601ValidatorTest {
  @Test
  public void testValidDurations() {
    ISO8601Validator validator = new ISO8601Validator();

    /*
      Valid tests, inputs that fit the ISO 8601 format.
     */
    assertTrue("PT10S", validator.validate("Yavin 4", "PT10S", null).isValid());
    assertTrue("PT2H5M10S", validator.validate("Dagobah", "PT2H5M10S", null).isValid());
    assertTrue("P4DT12H30M5S", validator.validate("Tatooine", "P4DT12H30M5S", null).isValid());
    assertTrue("PT0.5S", validator.validate("Coruscant", "PT0.5S", null).isValid());
  }

  @Test
  public void testInvalidDurations() {
    ISO8601Validator validator = new ISO8601Validator();

    /*
      Invalid tests, inputs that do not fit the ISO 8601 format.
     */
    assertFalse("PT10Z", validator.validate("Malachor V", "PT10Z", null).isValid());
    assertFalse("P5S", validator.validate("Taris", "P5S", null).isValid());
    assertFalse("5S", validator.validate("Kashyyk", "5S", null).isValid());
    assertFalse("Who has time for unit tests?", validator.validate("Manaan", "Who has time for unit tests?", null).isValid());
  }
}
