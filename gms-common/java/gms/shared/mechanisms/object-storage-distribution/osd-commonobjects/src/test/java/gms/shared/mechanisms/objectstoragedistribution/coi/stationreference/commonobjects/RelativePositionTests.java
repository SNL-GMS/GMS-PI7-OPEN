package gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.TestFixtures;
import org.junit.jupiter.api.Test;

public class RelativePositionTests {
  private final double northDisplacementKm = 12.34;
  private final double eastDisplacementKm = 56.78;
  private final double verticalDisplacementKm = 91.23;
  private final double precision = 1E-5;

  @Test
  void testFrom() {
    RelativePosition relativePosition = RelativePosition
        .from(northDisplacementKm, eastDisplacementKm,
            verticalDisplacementKm);

    assertAll("RelativePosition from equality checks",
        () -> assertEquals(northDisplacementKm, relativePosition.getNorthDisplacementKm(),
            precision),
        () -> assertEquals(eastDisplacementKm, relativePosition.getEastDisplacementKm(),
            precision),
        () -> assertEquals(verticalDisplacementKm, relativePosition.getVerticalDisplacementKm(),
            precision));
  }

  @Test
  public void testSerialization() throws Exception {
    TestUtilities.testSerialization(TestFixtures.position, RelativePosition.class);
  }

}
