package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.TestFixtures;
import java.util.List;
import org.junit.Test;

public class FkSpectrumDaoTests {

  //TODO: Implement testEquality() method following osd-repository/.../signaldetection.repository.jpa/dataaccessobjects/*Test.java as examples

  @Test
  public void testEquality() {
    FkSpectrumDao fk1 = createFkSpectrumDao();
    FkSpectrumDao fk2 = createFkSpectrumDao();

    assertEquals(fk1, fk2);
    assertEquals(fk1.hashCode(), fk2.hashCode());

    // not equal daoId's
    long daoId = fk1.getPrimaryKey();
    fk1.setPrimaryKey(daoId + 1);
    assertNotEquals(fk1, fk2);
    fk1.setPrimaryKey(daoId);
    assertEquals(fk1, fk2);
  }

  public static FkSpectrumDao createFkSpectrumDao() {
    int fkQual = 4;
    double[][] values = TestFixtures.FK_SPECTRUM_POWER;
    List<FkAttributesDao> fkAttributesList = List.of(FkAttributesDaoTests.createFkAttributesDao());

    FkSpectrumDao fk = new FkSpectrumDao();
    fk.setQuality(fkQual);
    fk.setPower(values);
    fk.setAttributes(fkAttributesList);

    return fk;
  }
}
