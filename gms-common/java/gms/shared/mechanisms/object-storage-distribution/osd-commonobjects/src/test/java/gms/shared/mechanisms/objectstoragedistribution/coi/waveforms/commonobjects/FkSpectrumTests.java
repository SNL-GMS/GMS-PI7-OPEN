package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.TestFixtures;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class FkSpectrumTests {

  @Test
  public void testFkSpectrum() {
    double[][] power = new double[][]{{0.1, 0.1, 0.1}, {0.1, 0.5, 0.1}, {0.1, 0.1, 0.1}};
    double[][] fstat = new double[][]{{0.1, 0.1, 0.1}, {0.1, 0.1, 0.1}, {0.1, 0.1, 0.1}};
    int quality = 1;
    FkAttributes attributes = FkAttributes.from(0.1, 0.2,
        0.3, 0.4, 0.5);

    FkSpectrum fk = FkSpectrum.from(power, fstat, quality);

    assertTrue(Arrays.deepEquals(power, fk.getPower().copyOf()));
    assertTrue(Arrays.deepEquals(fstat, fk.getFstat().copyOf()));
    assertEquals(quality, fk.getQuality());

    fk = FkSpectrum.from(power, fstat, quality, List.of(attributes));
    assertTrue(Arrays.deepEquals(power, fk.getPower().copyOf()));
    assertTrue(Arrays.deepEquals(fstat, fk.getFstat().copyOf()));
    assertEquals(quality, fk.getQuality());
    assertEquals(1, fk.getAttributes().size());
    assertEquals(attributes, fk.getAttributes().get(0));

  }

  @Test
  public void testValidation() {

    assertAll("Empty array validation",
        () -> assertThrows(IllegalArgumentException.class,
            () -> FkSpectrum.from(new double[][]{}, new double[][]{{0.1}}, 1),
            "Expected exception with empty power array"),
        () -> assertThrows(IllegalArgumentException.class,
            () -> FkSpectrum.from(new double[][]{{0.1}}, new double[][]{}, 1),
            "Expected exception with empty fstat array"));

    assertThrows(IllegalStateException.class,
        () -> FkSpectrum.from(new double[][]{{0.1}},
            new double[][]{{0.1, 0.1}, {0.1, 0.1}}, 1),
        "Expected exception when power and fstat dimensions do not match.");
  }

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.fkSpectrum, FkSpectrum.class);
  }

}
