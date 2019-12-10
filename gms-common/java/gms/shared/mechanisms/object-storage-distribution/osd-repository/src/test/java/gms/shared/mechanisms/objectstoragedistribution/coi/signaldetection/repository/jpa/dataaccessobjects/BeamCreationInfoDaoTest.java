package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.dataaccessobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamCreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.jpa.TestFixtures;
import org.junit.jupiter.api.Test;

public class BeamCreationInfoDaoTest {

  @Test
  void testFromToCoi() {
    BeamCreationInfo beamCreationInfo = TestFixtures.BEAM_CREATION_INFO;
    assertEquals(beamCreationInfo, BeamCreationInfoDao.from(beamCreationInfo).toCoi());
  }
}
