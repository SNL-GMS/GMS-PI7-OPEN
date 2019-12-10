package gms.shared.mechanisms.configuration.constraints;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.configuration.Constraint;
import gms.shared.mechanisms.configuration.Operator;
import gms.shared.mechanisms.configuration.Operator.Type;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import org.junit.jupiter.api.Test;

class NumericScalarConstraintTests {

  private ObjectMapper objectMapper = ObjectSerialization.getObjectMapper();

  @Test
  void testSerialization() throws Exception {
    final NumericScalarConstraint snrIs10 = NumericScalarConstraint
        .from("sta", Operator.from(Type.EQ, false), 5.0, 100);

    final String json = objectMapper.writeValueAsString(snrIs10);
    assertNotNull(json);

    final Constraint deserialized = objectMapper.readValue(json, Constraint.class);
    assertEquals(snrIs10, deserialized);
  }

  @Test
  void testFromValidatesArguments() {
    assertTrue(
        assertThrows(IllegalArgumentException.class,
            () -> NumericScalarConstraint.from("snr", Operator.from(Type.IN, false), 5.0, 1))
            .getMessage().contains("Operator Type: IN is not supported"));
  }

  @Test
  void testIsSatisfied() {
    final double value = 5.0;
    final NumericScalarConstraint scalar = NumericScalarConstraint
        .from("snr", Operator.from(Type.EQ, false), value, 1);

    assertAll(
        () -> assertTrue(scalar.test(value)),
        () -> assertFalse(scalar.test(value + 1.0)),
        () -> assertFalse(scalar.test(value - 1.0e-15))
    );
  }

  @Test
  void testIsSatisfiedValidatesParameter() {
    assertTrue(
        assertThrows(NullPointerException.class,
            () -> NumericScalarConstraint.from("snr", Operator.from(Type.EQ, false), 5.0, 1)
                .test(null))
            .getMessage().contains("selector can't be null"));
  }
}
