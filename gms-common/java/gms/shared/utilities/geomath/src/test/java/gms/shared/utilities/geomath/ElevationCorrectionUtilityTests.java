package gms.shared.utilities.geomath;

import static gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType.I;
import static gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType.LQ;
import static gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType.LR;
import static gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType.T;
import static org.junit.Assert.assertEquals;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

// TODO: Use BOTH types of DTOs
//@RunWith(MockitoJUnitRunner.class)
// TODO: Mockito doenst work in java 9 modules unless to-be-determined conditions are met
public class ElevationCorrectionUtilityTests {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private MediumVelocities generickMockedMediumVelocityRetriever = new MediumVelocities() {
    // TODO: Mockito doenst work in java 9 modules unless to-be-determined conditions are met

    @Override
    public void initialize(String modelName) {

    }

    @Override
    public double getMediumVelocity(PhaseType p) throws IllegalArgumentException {
      return 21.646;
    }
  };

  @Test
  public void testCorrectNullLocationThrowsNullPointerException() {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage("ElevationCorrectionUtility::correct() requires non-null location");

    ElevationCorrectionUtility utility = ElevationCorrectionUtility
        .from(generickMockedMediumVelocityRetriever);
    utility.initialize();
    utility.correct(null, 1.0, PhaseType.P);
  }

  @Test
  public void testCorrectNullPhaseTypeThrowsNullPointerException() {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage("ElevationCorrectionUtility::correct() requires non-null phaseType");

    ElevationCorrectionUtility utility = ElevationCorrectionUtility
        .from(generickMockedMediumVelocityRetriever);
    utility.initialize();
    utility.correct(Location.from(0.0, 0.0, 0.0, 0.0), 1.0, null);
  }

  @Test
  public void testCorrectValueReturned() {
    ElevationCorrectionUtility utility = ElevationCorrectionUtility
        .from(generickMockedMediumVelocityRetriever);
    utility.initialize();
    Location location = Location.from(65.0, 65.0, 0.0, 334.029);
    assertEquals(9.296745505, utility.correct(location, 4.100091, PhaseType.P), 1E-7);
  }

  @Test
  public void testSurfacePhaseThrowsIllegalArgumentException() {
    List<PhaseType> invalidPhases = List.of(
        //Note: These invalid phases are not in PhaseType
        //L,G,H,HPg,HSg,HRg,IPg,ISg,IRg,TPg,TSg,TRg,
        LQ,LR,I,T
    );

    invalidPhases.forEach(phaseType -> {
      exception.expect(IllegalArgumentException.class);
      exception
          .expectMessage("Invalid phase for elevation correction: " + phaseType);

      ElevationCorrectionUtility utility = ElevationCorrectionUtility
          .from(generickMockedMediumVelocityRetriever);
      utility.initialize();
      utility.correct(Location.from(0.0, 0.0, 0.0, 0.0), 1.0, phaseType);
    });
  }
}
