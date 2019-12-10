package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PhaseTypeTests {

  @Test
  public void testPorSCheckForSampleOfPhases() {
    assertEquals(PhaseType.pPdiff.getFinalPhase(), PhaseType.P );
    assertEquals(PhaseType.pPKiKP.getFinalPhase(), PhaseType.P );
    assertEquals(PhaseType.pPKP  .getFinalPhase(), PhaseType.P );
    assertEquals(PhaseType.SKKSdf.getFinalPhase(), PhaseType.S );
    assertEquals(PhaseType.SKP   .getFinalPhase(), PhaseType.P );
    assertEquals(PhaseType.SKPab .getFinalPhase(), PhaseType.P );
    assertEquals(PhaseType.SKPbc .getFinalPhase(), PhaseType.P );
    assertEquals(PhaseType.PPP   .getFinalPhase(), PhaseType.P );
    assertEquals(PhaseType.PPP_B .getFinalPhase(), PhaseType.P );
    assertEquals(PhaseType.PPS   .getFinalPhase(), PhaseType.S );
    assertEquals(PhaseType.PPS_B .getFinalPhase(), PhaseType.S );
    assertEquals(PhaseType.PS    .getFinalPhase(), PhaseType.S );
    assertEquals(PhaseType.PS_1  .getFinalPhase(), PhaseType.S );
    assertEquals(PhaseType.pSdiff.getFinalPhase(), PhaseType.S );

  }

}
