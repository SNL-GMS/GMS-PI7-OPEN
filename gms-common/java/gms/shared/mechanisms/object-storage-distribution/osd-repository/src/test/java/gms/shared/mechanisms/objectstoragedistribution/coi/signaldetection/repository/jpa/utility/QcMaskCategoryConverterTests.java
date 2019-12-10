package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link QcMaskCategoryConverter}
 */
public class QcMaskCategoryConverterTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  /**
   * Tests all of the {@link QcMaskCategory} literals have a conversion and can be recreated
   * from that conversion.
   */
  @Test
  public void testConverter() {
    QcMaskCategoryConverter converter = new QcMaskCategoryConverter();
    Assert.assertTrue(Arrays.stream(QcMaskCategory.values())
        .allMatch(q -> converter.convertToEntityAttribute(converter.convertToDatabaseColumn(q))
            .equals(q)));
  }
}
