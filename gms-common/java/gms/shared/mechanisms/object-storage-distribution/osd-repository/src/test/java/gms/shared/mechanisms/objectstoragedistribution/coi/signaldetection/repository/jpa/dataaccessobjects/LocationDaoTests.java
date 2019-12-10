package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.TestFixtures;
import org.junit.jupiter.api.Test;

public class LocationDaoTests {

  @Test
  void testFromValidation() {
    Location location = TestFixtures.location;

    assertAll("LocationDao from Validation",
        () -> assertThrows(NullPointerException.class,
            () -> LocationDao.from(null)),
        () -> assertDoesNotThrow(() -> LocationDao.from(location)));
  }

  @Test
  void testFromToCoi() {
    Location location = TestFixtures.location;
    assertEquals(location, LocationDao.from(location).toCoi());
  }
}
