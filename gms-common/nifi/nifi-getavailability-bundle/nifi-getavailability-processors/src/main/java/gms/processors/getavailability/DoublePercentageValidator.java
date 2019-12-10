package gms.processors.getavailability;

import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;

public class DoublePercentageValidator implements Validator {

  @Override
  public ValidationResult validate(String subject, String input, ValidationContext context) {
    String reason = null;
    try {
      final double doubleVal = Double.parseDouble(input);

      if (doubleVal < 0 || doubleVal > 1) {
        reason = "input must fall between 0 and 1";
      }
    } catch (final NumberFormatException e) {
      reason = "input is not a valid double";
    }

    return new ValidationResult.Builder()
        .subject(subject)
        .input(input)
        .explanation(reason)
        .valid(reason == null)
        .build();
  }
}
