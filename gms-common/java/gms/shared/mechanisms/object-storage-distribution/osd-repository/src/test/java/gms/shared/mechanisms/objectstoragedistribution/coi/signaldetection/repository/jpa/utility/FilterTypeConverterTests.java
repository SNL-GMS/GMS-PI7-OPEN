package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterType;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class FilterTypeConverterTests {

  /**
   * Tests all of the {@link FilterType}
   * literals have a conversion and can be recreated from that conversion.
   */
  @Test
  public void testConverter() {
    FilterTypeConverter converter = new FilterTypeConverter();
    Assert.assertTrue(Arrays.stream(FilterType.values()).allMatch(
        q -> converter.convertToEntityAttribute(converter.convertToDatabaseColumn(q)).equals(q)));
  }
}
