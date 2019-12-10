package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.TestFixtures;
import org.junit.jupiter.api.Test;

public class BeamDefinitionDaoTests {

  @Test
  void testFromToCoi() {
    assertEquals(TestFixtures.BEAM_DEFINITION,
        BeamDefinitionDao.from(TestFixtures.BEAM_DEFINITION).toCoi());
  }

}
