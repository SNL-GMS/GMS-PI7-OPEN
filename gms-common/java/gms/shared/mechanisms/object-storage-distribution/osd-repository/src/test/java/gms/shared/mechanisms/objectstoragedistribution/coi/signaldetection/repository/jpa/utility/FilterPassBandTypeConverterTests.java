package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;


import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterPassBandType;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class FilterPassBandTypeConverterTests {

  /**
   * Tests all of the {@link FilterPassBandType}
   * literals have a conversion and can be recreated from that conversion.
   */
  @Test
  public void testConverter() {
    FilterPassBandTypeConverter converter = new FilterPassBandTypeConverter();
    Assert.assertTrue(Arrays.stream(FilterPassBandType.values()).allMatch(
        q -> converter.convertToEntityAttribute(converter.convertToDatabaseColumn(q)).equals(q)));
  }
}
