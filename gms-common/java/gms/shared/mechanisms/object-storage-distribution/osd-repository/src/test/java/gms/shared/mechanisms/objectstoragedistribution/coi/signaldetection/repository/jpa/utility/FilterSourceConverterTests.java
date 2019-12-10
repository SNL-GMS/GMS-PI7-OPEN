package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;


import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterSource;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class FilterSourceConverterTests {
  /**
   * Tests all of the {@link FilterSource}
   * literals have a conversion and can be recreated from that conversion.
   */
  @Test
  public void testConverter() {
    FilterSourceConverter converter = new FilterSourceConverter();
    Assert.assertTrue(Arrays.stream(FilterSource.values()).allMatch(
        q -> converter.convertToEntityAttribute(converter.convertToDatabaseColumn(q)).equals(q)));
  }
}
