package gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.repository.jpa.dataaccessobjects.FkSpectraDao.MetadataDao;
import java.time.Instant;
import java.util.List;
import org.junit.Test;

public class FkSpectraDaoTests {

  @Test
  public void testEquality() {
    FkSpectraDao fk1 = createFkSpectraDao();
    FkSpectraDao fk2 = createFkSpectraDao();

    assertEquals(fk1, fk2);
    assertEquals(fk1.hashCode(), fk2.hashCode());

    // not equal daoId's
    long daoId = fk1.getPrimaryKey();
    fk1.setPrimaryKey(daoId + 1);
    assertNotEquals(fk1, fk2);
    fk1.setPrimaryKey(daoId);
    assertEquals(fk1, fk2);

    fk1.getMetadata().setPhaseType(PhaseType.S);
    assertNotEquals(fk1, fk2);
  }

  public static FkSpectraDao createFkSpectraDao() {
    Instant startTime = Instant.EPOCH;
    double sampleRate = 1.23;
    long sampleCount = 456;
    PhaseType phaseType = PhaseType.P;
    double slowStartX = 3.3;
    double slowDeltaX = 4.4;
    double slowStartY = 5.5;
    double slowDeltaY = 6.6;

    TimeseriesDao timeSeriesDao = new TimeseriesDao(startTime, sampleRate, sampleCount);

    MetadataDao metadataDao = new MetadataDao(
        phaseType,
        slowStartX,
        slowStartY,
        slowDeltaX,
        slowDeltaY);

    List<FkSpectrumDao> values = List.of(FkSpectrumDaoTests.createFkSpectrumDao());

    return new FkSpectraDao(timeSeriesDao, metadataDao, values);
  }
}
