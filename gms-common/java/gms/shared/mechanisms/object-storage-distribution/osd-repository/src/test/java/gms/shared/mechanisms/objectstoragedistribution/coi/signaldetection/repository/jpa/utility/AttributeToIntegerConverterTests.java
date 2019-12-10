package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;


import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AttributeToIntegerConverterTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private enum TestEnum {
    A, B, C
  }

  private final static Map<TestEnum, Integer> forward =
      Map.of(TestEnum.A, 1, TestEnum.B, 2, TestEnum.C, 3);

  @Test
  public void testConstruct() {
    AttributeToIntegerConverter<TestEnum> converter = new AttributeToIntegerConverter<>(forward);
    assertTrue(Arrays.stream(TestEnum.values()).allMatch(
        q -> converter.convertToEntityAttribute(converter.convertToDatabaseColumn(q)).equals(q)));
  }

  @Test
  public void testConstructNullForwardMappingExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("Cannot create AttributeToIntegerConverter with a null forwardMapping");
    new AttributeToIntegerConverter<>(null);
  }
}
