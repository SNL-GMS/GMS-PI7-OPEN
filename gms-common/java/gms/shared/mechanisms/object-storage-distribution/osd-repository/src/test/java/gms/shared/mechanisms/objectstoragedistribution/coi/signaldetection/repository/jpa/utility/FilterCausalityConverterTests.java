package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FilterCausality;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

public class FilterCausalityConverterTests {

  /**
   * Tests all of the {@link FilterCausality}
   * literals have a conversion and can be recreated from that conversion.
   */
  @Test
  public void testConverter() {
    FilterCausalityConverter converter = new FilterCausalityConverter();
    Assert.assertTrue(Arrays.stream(FilterCausality.values()).allMatch(
        q -> converter.convertToEntityAttribute(converter.convertToDatabaseColumn(q)).equals(q)));
  }
}
