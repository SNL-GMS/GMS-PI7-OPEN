package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects;


import static org.junit.jupiter.api.Assertions.assertThrows;

import gms.shared.mechanisms.objectstoragedistribution.coi.common.TestUtilities;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.SignalDetectionTestFixtures;
import org.junit.Test;

public class BeamDefinitionTests {

  private final BeamDefinition beamDefinition = SignalDetectionTestFixtures.BEAM_DEFINITION;

  @Test
  public void testSerialization() throws Exception {
    TestUtilities
        .testSerialization(beamDefinition, BeamDefinition.class);
  }

  @Test
  public void testAziumithValidation() {
    assertThrows(IllegalStateException.class,
        () -> beamDefinition.toBuilder().setAzimuth(-1).build());

    assertThrows(IllegalStateException.class,
        () -> beamDefinition.toBuilder().setAzimuth(361).build());
  }

  @Test
  public void testMinimumWaveformsValidation() {
    assertThrows(IllegalStateException.class,
        () -> beamDefinition.toBuilder().setMinimumWaveformsForBeam(0).build());
  }
}
