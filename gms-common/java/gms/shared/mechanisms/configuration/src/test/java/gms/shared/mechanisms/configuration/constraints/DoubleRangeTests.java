package gms.shared.mechanisms.configuration.constraints;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DoubleRangeTests {

  @Test
  void testFromVerifiesMinLessEqualToThanMax() {

    assertAll(
        () -> assertDoesNotThrow(() -> DoubleRange.from(-10.0, -10.0)),
        () -> assertEquals("Minimum must be <= maximum but -11.0 < -10.0",
            assertThrows(IllegalArgumentException.class,
                () -> DoubleRange.from(-10.0, -11.0)).getMessage())
    );

  }

  @Test
  void testInfiniteRange() {
    final DoubleRange dr = DoubleRange.fromInfiniteRange();

    assertAll(
        () -> assertEquals(0, Double.compare(dr.getMin(), Double.NEGATIVE_INFINITY)),
        () -> assertEquals(0, Double.compare(dr.getMax(), Double.POSITIVE_INFINITY))
    );
  }

  @Test
  void testContains() {
    final double min = 1.0;
    final double max = 10.0;
    final DoubleRange dr = DoubleRange.from(min, max);

    assertAll(
        () -> assertTrue(dr.contains(1.0)),
        () -> assertTrue(dr.contains(1.0, true, true)),
        () -> assertTrue(dr.contains(10.0)),
        () -> assertTrue(dr.contains(10.0, true, true)),
        () -> assertFalse(dr.contains(.5)),
        () -> assertFalse(dr.contains(11.0)),
        () -> assertFalse(dr.contains(min, false, false)),
        () -> assertFalse(dr.contains(max, false, false))
    );
  }
}
