package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.utility;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link QcMaskTypeConverter}
 */
public class QcMaskTypeConverterTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  /**
   * Tests all of the {@link QcMaskType} literals have a conversion and can be recreated
   * from that conversion.
   */
  @Test
  public void testConverter() {
    QcMaskTypeConverter converter = new QcMaskTypeConverter();
    Assert.assertTrue(Arrays.stream(QcMaskType.values())
        .allMatch(q -> converter.convertToEntityAttribute(converter.convertToDatabaseColumn(q))
            .equals(q)));
  }

}
