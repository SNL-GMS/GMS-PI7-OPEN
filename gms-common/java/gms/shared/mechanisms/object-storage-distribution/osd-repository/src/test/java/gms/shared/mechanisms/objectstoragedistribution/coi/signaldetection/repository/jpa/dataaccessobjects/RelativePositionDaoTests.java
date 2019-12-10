package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.TestFixtures;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import org.junit.jupiter.api.Test;

public class RelativePositionDaoTests {

  @Test
  void testFromValidation() {
    RelativePosition relativePosition = TestFixtures.relativePosition;

    assertAll("RelativePositionDao from Validation",
        () -> assertThrows(NullPointerException.class,
            () -> RelativePositionDao.from(null)),
        () -> assertDoesNotThrow(() -> RelativePositionDao.from(relativePosition)));
  }

  @Test
  void testFromToCoi() {
    RelativePosition relativePosition = TestFixtures.relativePosition;

    assertEquals(relativePosition, RelativePositionDao.from(relativePosition).toCoi());
  }
}
