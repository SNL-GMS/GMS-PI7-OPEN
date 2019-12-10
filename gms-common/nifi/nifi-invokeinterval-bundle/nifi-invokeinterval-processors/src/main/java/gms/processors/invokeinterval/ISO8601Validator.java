package gms.processors.invokeinterval;

import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import java.time.Instant;
import java.time.Duration;
import java.lang.String;


public class ISO8601Validator implements Validator {
  @Override
  public ValidationResult validate(final String subject, final String input, final ValidationContext context) {
    final ValidationResult.Builder builder = new ValidationResult.Builder();
    builder.subject(subject).input(input);

    try {
      Duration.parse(input);
      builder.valid(true);
    } catch (java.time.format.DateTimeParseException e) {
      builder.valid(false).explanation("[" + input + "] is not a valid ISO-8601 format.");
    }

    return builder.build();
  }
  
}
