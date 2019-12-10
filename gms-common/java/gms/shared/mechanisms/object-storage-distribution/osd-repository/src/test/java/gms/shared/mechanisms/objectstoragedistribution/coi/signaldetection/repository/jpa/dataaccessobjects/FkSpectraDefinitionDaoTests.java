package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FkSpectraDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.TestFixtures;
import org.junit.jupiter.api.Test;

public class FkSpectraDefinitionDaoTests {

  @Test
  void testFromValidation() {
    FkSpectraDefinition definition = TestFixtures.FK_SPECTRA_DEFINITION;

    assertAll("FkSpectraDefinitionDao from Validation",
        () -> assertThrows(NullPointerException.class,
            () -> FkSpectraDefinitionDao.from(null)),
        () -> assertDoesNotThrow(() -> FkSpectraDefinitionDao.from(definition)));
  }

  @Test
  void testFromToCoi() {
    FkSpectraDefinition definition = TestFixtures.FK_SPECTRA_DEFINITION;
    assertEquals(definition, FkSpectraDefinitionDao.from(definition).toCoi());
  }
}
